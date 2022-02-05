package hand_crafted.gnc;

import graph.Decision;
import moea.solutions.ADDSolution;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import java.util.Random;

public class GNC_Crossover implements Variation {


    public double mutation_probability;
    public Random rand;

    public GNC_Crossover(double mutation_probability){
        this.rand = new Random();
        this.mutation_probability = mutation_probability;
    }


    @Override
    public Solution[] evolve(Solution[] parents){

        System.out.println("----> GNC CROSSOVER OPERATION");

        // TWO PARENTS FOR CROSSOVER
        Solution parent1 = parents[0].copy();
        Solution parent2 = parents[1].copy();

        // CAST APPROPRIATELY
        GNC_Solution res1 = (GNC_Solution) parent1;
        GNC_Solution res2 = (GNC_Solution) parent2;

        // PERFORM CROSSOVER
        GNC_Model child_model = this.crossover(res1.model, res2.model);

        // PERFORM MUTATION
        if(Decision.getProbabilityResult(this.mutation_probability)){
            child_model.mutate();
        }

        // CREATE SOLUTION
        GNC_Solution child = new GNC_Solution(child_model);
        Solution[]   soln  = new Solution[] { child };
        return soln;
    }

    public GNC_Model crossover(GNC_Model papa, GNC_Model mama){

        // TOPOLOGY CROSSOVER
        int[][][] child_topology = this.uniform_topology_crossover(papa.connections, mama.connections);

        // COMPONENT CROSSOVER
        int[] child_sensors = this.uniform_component_crossover(papa.sensors, mama.sensors);
        int[] child_computers = this.uniform_component_crossover(papa.computers, mama.computers);
        int[] child_actuators = this.uniform_component_crossover(papa.actuators, mama.actuators);

        return new GNC_Model(child_topology, child_sensors, child_computers, child_actuators);
    }

    public int[][][] uniform_topology_crossover(int[][][] papa, int[][][] mama){
        int[][][] child = new int[3][3][3];

        for(int x = 0; x < 3; x++){
            for(int y = 0; y < 3; y++){
                for(int z = 0; z < 3; z++){
                    int parent_val = 0;
                    if(this.rand.nextInt(2) != 0){
                        parent_val = papa[x][y][z];
                    }
                    else{
                        parent_val = mama[x][y][z];
                    }
                    child[x][y][z] = parent_val;
                }
            }
        }
        return child;
    }

    public int[] uniform_component_crossover(int[] papa, int[] mama){
        int[] child = new int[3];

        for(int x = 0; x < 3; x++){
            int parent_val = 0;
            if(this.rand.nextInt(2) != 0){
                parent_val = papa[x];
            }
            else{
                parent_val = mama[x];
            }
            child[x] = parent_val;
        }
        return child;
    }











    // NUM PARENTS REQUIRED
    @Override
    public int getArity(){
        return 2;
    }

}
