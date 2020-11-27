package moea;

import app.App;
import com.google.gson.JsonArray;
import graph.Graph;

import moea.operators.ADDCrossover;
import moea.problems.ADDProblem;
import moea.search.ADDSearch;
import moea.solutions.ADDSolution;
import org.moeaframework.Analyzer;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.ParetoObjectiveComparator;
import org.moeaframework.core.operator.InjectedInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.util.TypedProperties;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Algorithm implements Runnable{


    private Graph graph;
    private int numObjectives;

    private Problem         addProblem;
    private List<Solution>  solutions;
    private EpsilonMOEA     eMOEA;
    private TypedProperties properties;
    private String          id;
    private String          eval_queue_url;

    private int initialPopSize;
    private int maxEvals;
    private double crossoverProbability;
    private double mutationProbability;
    private ArrayList<Double> mutation_probabilities;
    private SqsClient sqs;


    // --> IF targeted_run THEN we are solving the problem for just one add node
    // - For a targeted run, you must provide the target node
    private boolean   targeted_run;
    private String    target_node;
    private JsonArray target_dependency;





    public static class Builder{

        private Graph graph;
        private int numObjectives;
        private TypedProperties properties;
        private String id;
        private SqsClient sqs;
        private String eval_queue_url;

        private int initialPopSize;
        private int maxEvals;
        private double crossoverProbability;
        private double mutationProbability;
        private ArrayList<Double> mutation_probabilities;

        private String target_node;
        private boolean targeted_run;
        private JsonArray target_dependency;

        public Builder(Graph graph, SqsClient sqs){
            this.graph = graph;
            this.sqs = sqs;
            this.properties = new TypedProperties();

            this.targeted_run = false;
            this.target_dependency = null;
            this.target_node = null;
            this.mutation_probabilities = new ArrayList<>();
        }

        public Builder setNumObjectives(int numObjectives){
            this.numObjectives = numObjectives;
            return this;
        }

        public Builder setEvalQueueUrl(String eval_queue_url){
            this.eval_queue_url = eval_queue_url;
            return this;
        }


        public Builder setTargetNode(String target_node){
            this.target_node = target_node;
            this.targeted_run = true;
            return this;
        }

        public Builder setTargetDependency(JsonArray target_dependency){
            this.target_dependency = target_dependency;
            this.targeted_run = true;
            return this;
        }

        public Builder setID(String id){
            this.id = id;
            return this;
        }

        public Builder setMutationArrayProbabilities(ArrayList<Double> mutation_probabilities){
            this.mutation_probabilities = mutation_probabilities;
            return this;
        }

        public Builder setProperties(int maxEvals, int initialPopSize, double crossoverProbability, double mutationProbability){
            this.initialPopSize = initialPopSize;
            this.maxEvals = maxEvals;
            this.crossoverProbability = crossoverProbability;
            this.mutationProbability = mutationProbability;
            this.properties.setInt("maxEvaluations", maxEvals);
            this.properties.setInt("populationSize", initialPopSize);
            this.properties.setDouble("crossoverProbability", crossoverProbability);
            this.properties.setDouble("mutationProbability", mutationProbability);
            return this;
        }


        public Algorithm build() {
            Algorithm build      = new Algorithm();

            build.target_node            = this.target_node;
            build.target_dependency      = this.target_dependency;
            build.targeted_run           = this.targeted_run;
            build.graph                  = this.graph;
            build.initialPopSize         = this.initialPopSize;
            build.maxEvals               = this.maxEvals;
            build.crossoverProbability   = this.crossoverProbability;
            build.mutation_probabilities = this.mutation_probabilities;
            build.mutationProbability    = this.mutationProbability;
            build.numObjectives  = this.numObjectives;
            build.id             = this.id;
            build.properties     = this.properties;
            build.sqs            = this.sqs;
            build.eval_queue_url = this.eval_queue_url;

            // --> BUILD PROBLEM
            if(build.targeted_run){
                build.addProblem = new ADDProblem(this.graph, this.numObjectives, this.sqs, this.eval_queue_url, this.target_node, this.target_dependency);
            }
            else{
                build.addProblem = new ADDProblem(this.graph, this.numObjectives, this.sqs, this.eval_queue_url);
            }

            // --> VALIDATE ALGORITHM BUILD
            if(build.targeted_run){
                if(build.target_dependency == null || build.target_node == null){
                    System.out.println("--> TARGETED RUN REQUIRES NODE AND DEPENDENCY");
                    System.exit(0);
                }
            }
            return build;

        }
    }




    public void buildInitialSolutions(){
        System.out.println("---------- GENERATING INITIAL POPULATION ----------");

        this.solutions = new ArrayList<>(this.initialPopSize);

        // Generate Random Designs
        for(int x = 0; x < this.initialPopSize; x++){

            // GENERATE SOLUTION
            ADDSolution new_design = null;
            if(this.targeted_run){
                new_design = new ADDSolution(this.graph, this.numObjectives, this.target_node, this.target_dependency);
            }
            else{
                new_design = new ADDSolution(this.graph, this.numObjectives);
            }
            new_design.setAlreadyEvaluated(false);

            // ADD SOLUTION
            this.solutions.add(new_design);
        }

        // PRINT RANDOM DESIGNS
        if(this.targeted_run){
            this.graph.printDesigns(this.target_node);
        }
        // App.sleep(5);
    }


    public void initialize(){

        System.out.println("---------- INITIALIZING MOEA ----------");
        // App.sleep(2);

        // BUILD: this.solutions
        this.buildInitialSolutions();

        // INJECTED INITIALIZATION
        InjectedInitialization initialization = new InjectedInitialization(addProblem, this.solutions.size(), this.solutions);

        double[]                   epsilonDouble = new double[]{0.001, 1};
        Population                 population    = new Population();
        EpsilonBoxDominanceArchive archive       = new EpsilonBoxDominanceArchive(epsilonDouble);

        ChainedComparator   comp      = new ChainedComparator(new ParetoObjectiveComparator());
        TournamentSelection selection = new TournamentSelection(2, comp);

        // BUILD: Variation Operator
        ADDCrossover var;
        if(this.targeted_run){
            var = new ADDCrossover(this.graph, this.numObjectives, this.mutationProbability, this.target_node, this.target_dependency);
        }
        else{
            if(this.mutation_probabilities.isEmpty()){
                var = new ADDCrossover(this.graph, this.numObjectives, this.mutationProbability);
            }
            else{
                var = new ADDCrossover(this.graph, this.numObjectives, this.mutation_probabilities);
            }
        }

        // BUILD: MOEA
        this.eMOEA = new EpsilonMOEA(this.addProblem, population, archive, selection, var, initialization);

    }


    public void run() {

        // INITIALIZE
        this.initialize();

        // SUBMIT MODA
        ExecutorService                                     pool   = Executors.newFixedThreadPool(1);
        CompletionService<org.moeaframework.core.Algorithm> ecs    = new ExecutorCompletionService<>(pool);
        ecs.submit(new ADDSearch(this.eMOEA, this.properties, this.id));

        // JOIN MOEA ON FINISH
        try {
            org.moeaframework.core.Algorithm alg = ecs.take().get();

            // ANALYZE
            NondominatedPopulation result = alg.getResult();

            Analyzer analyzer = new Analyzer()
                    .withProblem(this.addProblem)
                    .withIdealPoint(-1.1, -0.1, -0.1)
                    .withReferencePoint(0, 10000, 2000)
                    .includeHypervolume()
                    .includeGenerationalDistance()
                    .includeAdditiveEpsilonIndicator();

            analyzer.add("addMOEA", result);
            analyzer.printAnalysis();



            double result_hv = analyzer.getAnalysis().get("addMOEA").get("Hypervolume").getMax();
            System.out.println(result_hv);





        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }

        pool.shutdown();
        System.out.println("DONE");


        // Analyzer Code




    }


}
