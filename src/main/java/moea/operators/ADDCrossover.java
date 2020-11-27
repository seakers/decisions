package moea.operators;

import app.App;
import com.google.gson.JsonArray;
import graph.Graph;
import moea.solutions.ADDSolution;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import java.util.ArrayList;

public class ADDCrossover implements Variation {

    private Graph graph;
    private int   numObjectives;
    private double mutationProbability;
    private ArrayList<Double> mutation_probabilities;


    private boolean   targeted_run;
    private String    target_node;
    private JsonArray target_dependency;

    public ADDCrossover(Graph graph, int numObjectives, double mutationProbability){
        this.graph         = graph;
        this.numObjectives = numObjectives;
        this.mutationProbability = mutationProbability;
        this.targeted_run = false;
        this.mutation_probabilities = new ArrayList<>();
    }

    public ADDCrossover(Graph graph, int numObjectives, ArrayList<Double> mutation_probabilities){
        this.graph         = graph;
        this.numObjectives = numObjectives;
        this.mutationProbability = 0;
        this.targeted_run = false;
        this.mutation_probabilities = mutation_probabilities;
    }

    public ADDCrossover(Graph graph, int numObjectives, double mutationProbability, String target_node, JsonArray target_dependency){
        this.graph         = graph;
        this.numObjectives = numObjectives;
        this.mutationProbability = mutationProbability;
        this.targeted_run        = true;
        this.target_node         = target_node;
        this.target_dependency   = target_dependency;
    }


    @Override
    public Solution[] evolve(Solution[] parents){

        System.out.println("---> ADD CROSSOVER OPERATOR");

        // TWO PARENTS FOR CROSSOVER
        Solution result1 = parents[0].copy();
        Solution result2 = parents[1].copy();

        // CAST APPROPRIATELY
        ADDSolution res1 = (ADDSolution) result1;
        ADDSolution res2 = (ADDSolution) result2;

        // CROSSOVER
        int child_id = -1;
        try {
            if(this.targeted_run){
                child_id = this.graph.crossover(res1.design_idx, res2.design_idx, mutationProbability, this.target_node, this.target_dependency);
            }
            else{
                if(this.mutation_probabilities.isEmpty()){
                    child_id = this.graph.crossover(res1.design_idx, res2.design_idx, mutationProbability);
                }
                else{
                    child_id = this.graph.crossover(res1.design_idx, res2.design_idx, mutation_probabilities);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // CREATE CHILD
        ADDSolution child = new ADDSolution(this.graph, this.numObjectives, child_id);

        // RETURN CHILD
        Solution[] soln = new Solution[] { child };
//        System.out.println(child_id);
//        App.sleep(1);
        return soln;
    }


    // NUM PARENTS REQUIRED
    @Override
    public int getArity(){
        return 2;
    }


}
