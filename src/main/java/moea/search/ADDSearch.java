package moea.search;

import app.App;
import app.Files;
import moea.Results;
import org.moeaframework.Analyzer;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.util.TypedProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ADDSearch implements Callable<Algorithm> {

    private TypedProperties properties;
    private Analyzer analyzer;
    private Accumulator accumulator;
    private final   Algorithm alg;
    private final   String id;
    private boolean isStopped;
    private int pop_counter;
    private int run_number;

    private ArrayList<Integer> record_indicies;

    public ADDSearch(Algorithm alg, TypedProperties properties, String id, int run_number){
        this.alg        = alg;
        this.properties = properties;
        this.id         = id;
        this.isStopped  = false;
        this.run_number = run_number;

        this.analyzer = new Analyzer()
                .withProblem(this.alg.getProblem())
                .withIdealPoint(-1.1, -0.1, -0.1)
                .withReferencePoint(0, 10000, 5000)
                .includeHypervolume()
                .includeAdditiveEpsilonIndicator();
        this.accumulator = new Accumulator();
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


        // NFE ITERATIONS
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
            int num_evals = alg.getNumberOfEvaluations();
//            if(this.record_indicies.contains(num_evals)){
//                Files.writeAlgorithmMetrics(analyzer, num_evals);
//            }

            // RECORD THE HYPERVOLUME AT EACH EVALUATION
            if(num_evals > 50){
                double current_hv = this.analyzer.getAnalysis().get("popADD").get("Hypervolume").getMax();
                this.accumulator.add("NFE", (num_evals));
                this.accumulator.add("HV", current_hv);
            }



        }

        this.analyzer.printAnalysis();

        // Write the designs with their scores to a json file
        // Files.writeReliabilityResults2(((AbstractEvolutionaryAlgorithm) alg).getArchive(), this.run_number);
        Files.writePopulationDesigns(Files.get_design_file(this.run_number), ((AbstractEvolutionaryAlgorithm) alg).getArchive());


        try{
            File hv_file = new File(Files.get_hv_file(this.run_number));
            this.accumulator.saveCSV(hv_file);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return this.alg;
    }



}
