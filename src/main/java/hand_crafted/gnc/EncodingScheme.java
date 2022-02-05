package hand_crafted.gnc;


/*
    This class is the hand crafted encoding scheme for a GN&C architecture containing the following components
    - sensors
    - computers
    - actuators
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class EncodingScheme {

    // 1D Array
    public ArrayList<Integer> actuators;

    // 2D Array
    public ArrayList<ArrayList<Integer>> computers;

    // 3D Array
    public ArrayList<ArrayList<ArrayList<Integer>>> sensors;


    public EncodingScheme(boolean random){

        this.actuators = this.create_actuators();
        this.computers = this.create_computers();
//        this.sensors   = this.create_sensors();
    }



    public ArrayList<Integer> create_actuators(){
        return this.get_random_triple(true);
    }

    public ArrayList<ArrayList<Integer>> create_computers(){
        ArrayList<ArrayList<Integer>> computers = new ArrayList<>();
        for(Integer actuator_val: this.actuators){
            if(actuator_val != 0){
                computers.add(this.get_random_triple(true));
            }
            else{
                computers.add(this.get_empty_triple());
            }
        }
        return computers;
    }


    public ArrayList<ArrayList<ArrayList<Integer>>> create_sensors(){
        ArrayList<ArrayList<ArrayList<Integer>>> sensors = new ArrayList<>();

        for(ArrayList<Integer> first_dimen: this.computers){
            ArrayList<ArrayList<Integer>> mid = new ArrayList<>();
            for(Integer computer_val: first_dimen){
                if(computer_val != 0){

                }
                else{

                }

            }
        }


        return sensors;
    }


//    public ArrayList<ArrayList<ArrayList<Integer>>> create_sensors(boolean random){
//        ArrayList<ArrayList<ArrayList<Integer>>> sensors = new ArrayList<>();
//        sensors.add(this.create_computers(random));
//        sensors.add(this.create_computers(random));
//        sensors.add(this.create_computers(random));
//        return sensors;
//    }





    public ArrayList<Integer> get_random_triple(boolean non_empty){
        ArrayList<Integer> triple = new ArrayList();
        triple.add(this.get_random_bit());
        triple.add(this.get_random_bit());
        triple.add(this.get_random_bit());
        if(non_empty && !triple.contains(1)){
            Random rand = new Random();
            int rand_idx = rand.nextInt(3);
            triple.set(rand_idx, 1);
        }
        return triple;
    }

    public ArrayList<Integer> get_empty_triple(){
        ArrayList<Integer> triple = new ArrayList<>();
        triple.add(0);
        triple.add(0);
        triple.add(0);
        return triple;
    }

    public Integer get_random_bit(){
        Random rand = new Random();
        return rand.nextInt(2);
    }




    public void print(){
        System.out.println("\n----- ACTUATORS -----");
        System.out.println(this.actuators);

        System.out.println("\n----- COMPUTERS -----");
        System.out.println(this.computers.get(0));
        System.out.println(this.computers.get(1));
        System.out.println(this.computers.get(2));

//        System.out.println("\n----- SENSORS -----");
//        System.out.println(this.sensors);
    }













}
