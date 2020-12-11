package moea.search;

import app.App;
import app.Files;
import moea.Results;
import org.moeaframework.Analyzer;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.util.TypedProperties;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ADDSearch implements Callable<Algorithm> {

    private TypedProperties properties;
    private Analyzer analyzer;
    private final   Algorithm alg;
    private final   String id;
    private boolean isStopped;
    private int pop_counter;

    private ArrayList<Integer> record_indicies;

    public ADDSearch(Algorithm alg, TypedProperties properties, String id){
        this.alg        = alg;
        this.properties = properties;
        this.id         = id;
        this.isStopped  = false;

        this.analyzer = new Analyzer()
                .withProblem(this.alg.getProblem())
                .withIdealPoint(-10.1, -0.1)
                .withReferencePoint(0, 100)
                .includeHypervolume()
                .includeAdditiveEpsilonIndicator();
        this.pop_counter = 0;

        this.record_indicies = new ArrayList<>();
        record_indicies.add(50);
        record_indicies.add(100);
        record_indicies.add(150);
        record_indicies.add(200);
        record_indicies.add(300);
        record_indicies.add(500);
        record_indicies.add(1000);
        record_indicies.add(2000);
        record_indicies.add(3000);
        record_indicies.add(4000);
        record_indicies.add(5000);
    }


    @Override
    public Algorithm call(){
        System.out.println("---------- ALGORITHM BEGIN ----------");
        // App.sleep(2);

        // OPERATIONS
        int populationSize = (int) properties.getDouble("populationSize", 600);
        int maxEvaluations = (int) properties.getDouble("maxEvaluations", 10000);

        alg.step();

        // INITIAL POPULATION
        Population archive = new Population(((AbstractEvolutionaryAlgorithm)alg).getArchive());

        String pop_name = "pop_" + this.pop_counter;
        this.analyzer.add(pop_name, this.alg.getResult());


        // ITERATIONS
        while (!alg.isTerminated() && (alg.getNumberOfEvaluations() < maxEvaluations) && !isStopped){


            if (this.isStopped) {
                break;
            }

            // ALGORITHM STEP
            alg.step();

            // NEW POPULATION
            Population newArchive = ((AbstractEvolutionaryAlgorithm)alg).getArchive();
            System.out.println("---> Archive size: " + newArchive.size());

            // NEW DESIGN FUNCTIONALITY
            for (int i = 0; i < newArchive.size(); ++i){

                Solution newSol       = newArchive.get(i);
                boolean  alreadyThere = archive.contains(newSol);
                if (!alreadyThere){
                    System.out.println("---> NEW DESIGN FOUND, NEW HV");

                    this.analyzer.add("popADD", this.alg.getResult());
                    // this.pop_counter++;
                    // this.analyzer.printAnalysis();
                    // App.sleep(10);


                }
            }

            // UPDATE REFERENCE POPULATION
            archive = new Population(newArchive);




            // --> EVALUATION DATA STORAGE
            // - 20 for the initial designs
            int num_evals = alg.getNumberOfEvaluations() - 20;
            if(this.record_indicies.contains(num_evals)){
                Files.writeAlgorithmMetrics(analyzer, num_evals);
            }



        }

        this.analyzer.printAnalysis();

        return this.alg;
    }



}
