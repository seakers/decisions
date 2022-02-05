package hand_crafted.gnc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class GNC_Model {


    public int[][][] connections;
    public int[] sensors;
    public int[] computers;
    public int[] actuators;




    public GNC_Model(int[][][] connections, int[] sensors, int[] computers, int[] actuators){
        this.connections = connections;
        this.sensors = sensors;
        this.computers = computers;
        this.actuators = actuators;
    }

    public GNC_Model(){

        // 3x3x3 Array
        this.connections = this.random_topology();

        this.sensors = this.random_triple();
        this.computers = this.random_triple();
        this.actuators = this.random_triple();
    }

    // COPY CONSTRUCTOR
    protected GNC_Model(GNC_Model model){
        this.connections = model.connections.clone();
        this.sensors = model.sensors.clone();
        this.computers = model.computers.clone();
        this.actuators = model.actuators.clone();
    }

    public int[][][] random_topology(){
        int[][][] topology = {
                {
                        {this.random_bit(), this.random_bit(), this.random_bit()},
                        {this.random_bit(), this.random_bit(), this.random_bit()},
                        {this.random_bit(), this.random_bit(), this.random_bit()}
                },
                {
                        {this.random_bit(), this.random_bit(), this.random_bit()},
                        {this.random_bit(), this.random_bit(), this.random_bit()},
                        {this.random_bit(), this.random_bit(), this.random_bit()}
                },
                {
                        {this.random_bit(), this.random_bit(), this.random_bit()},
                        {this.random_bit(), this.random_bit(), this.random_bit()},
                        {this.random_bit(), this.random_bit(), this.random_bit()}
                }
        };
        return topology;
    }

    public int[] random_triple(){
        int triple[] = { this.random_component(), this.random_component(), this.random_component() };
        return triple;
    }


    public int random_bit(){
        Random rand = new Random();
        return rand.nextInt(2);
    }

    public int random_component(){
        Random rand = new Random();
        return (rand.nextInt(3) + 1);
    }


    public void mutate(){
        this.mutate_topology();
        this.mutate_components();
    }

    public void mutate_topology(){
        Random rand = new Random();
        int dim1 = rand.nextInt(3);
        int dim2 = rand.nextInt(3);
        int dim3 = rand.nextInt(3);

        int val = this.connections[dim1][dim2][dim3];
        if(val != 0){
            this.connections[dim1][dim2][dim3] = 0;
        }
        else{
            this.connections[dim1][dim2][dim3] = 1;
        }
    }

    public void mutate_components(){
        Random rand = new Random();
        int component_to_mutate = rand.nextInt(3);
        int new_component = rand.nextInt(3)+1;
        int component_idx = rand.nextInt(3);

        // MUTATE SENSOR
        if(component_to_mutate == 0){
            this.sensors[component_idx] = new_component;
        }

        // MUTATE COMPUTER
        else if(component_to_mutate == 1){
            this.computers[component_idx] = new_component;
        }

        // MUTATE ACTUATOR
        else{
            this.actuators[component_idx] = new_component;
        }
    }













    /*
        this.connections[x][y][z]
        x-dimension: encodes actuators
        y-dimension: encodes computers
        z-dimension: encodes sensors
     */


    public String get_computer_to_actuators(){
        StringBuilder builder = new StringBuilder("000000000");

        for(int x = 0; x < 3; x++){
            for(int y = 0; y < 3; y++){
                for(int z = 0; z < 3; z++){
                    int value = this.connections[x][y][z];
                    if(value != 0){
                        int idx = (x*3) + y;
                        builder.setCharAt(idx, '1');
                    }
                }
            }
        }
        return builder.toString();
    }


    public String get_sensors_to_computers(){
        StringBuilder builder = new StringBuilder("000000000");

        for(int x = 0; x < 3; x++){
            for(int y = 0; y < 3; y++){
                for(int z = 0; z < 3; z++){
                    int value = this.connections[x][y][z];
                    if(value != 0){
                        int idx = (y*3) + z;
                        builder.setCharAt(idx, '1');
                    }
                }
            }
        }
        return builder.toString();
    }






    public void print(){
        System.out.println("\n----- SENSORS -----");
        System.out.println(Arrays.toString(this.sensors));

        System.out.println("\n----- COMPUTERS -----");
        System.out.println(Arrays.toString(this.computers));

        System.out.println("\n----- ACTUATORS -----");
        System.out.println(Arrays.toString(this.actuators));

        System.out.println("\n----- TOPOLOGY -----");
        System.out.println(Arrays.deepToString(this.connections));
    }






}
