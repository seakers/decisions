package evaluation;




/*
    The purpose of this class is to evaluate GN&C architectures producing 2 metrics of interest
        1. A mass metric
        2. A reliability metric

 */


import app.App;
import org.ejml.simple.SimpleMatrix;


import com.google.gson.*;
import org.jfree.chart.renderer.category.BarRenderer;

import java.util.*;
import java.lang.Math;


public class GNC_Evaluator {


    private HashMap<String, Double> component_masses;
    private HashMap<String, Double> component_reliability;

    private double dissimilar_component_penalty; // 9
    private double connection_weight;             // 5/3
    private double connection_reliability;        // 1
    private double years;                         // 10

    private Gson gson;


    public GNC_Evaluator(double connection_weight, double dissimilar_component_property, double connection_reliability, double years){
        this.dissimilar_component_penalty = dissimilar_component_property;
        this.connection_reliability = connection_reliability;
        this.connection_weight = connection_weight;
        this.years = years;
        this.gson  = new GsonBuilder().setPrettyPrinting().create();


        // Component Masses
        this.component_masses = new HashMap<>();
        this.component_masses.put("s1", 3.0);
        this.component_masses.put("s2", 6.0);
        this.component_masses.put("s3", 9.0);
        this.component_masses.put("c1", 3.0);
        this.component_masses.put("c2", 5.0);
        this.component_masses.put("c3", 10.0);
//        this.component_masses.put("a1", 3.0);
//        this.component_masses.put("a2", 5.0);
//        this.component_masses.put("a3", 10.0);

        // Component Reliability
        this.component_reliability = new HashMap<>();
        this.component_reliability.put("s1", 0.9985);
        this.component_reliability.put("s2", 0.999);
        this.component_reliability.put("s3", 0.9995);
        this.component_reliability.put("c1", 0.999);
        this.component_reliability.put("c2", 0.9996);
        this.component_reliability.put("c3", 0.9998);
//        this.component_reliability.put("a1", 0.0001);
//        this.component_reliability.put("a2", 0.00004);
//        this.component_reliability.put("a3", 0.00002);



    }


    public double evaluate_mass(String design_str){
        double mass = 0;
        boolean is_heterogeneous = false;


        JsonArray design = this.parseDesignString(design_str);

        HashMap<String, ArrayList<String>> computer_to_sensor = this.designToHashMap(design);

        // Evaluate computer mass
        for(String computer: computer_to_sensor.keySet()){
            mass += this.component_masses.get(computer.substring(0, 2));

            for(String sensor: computer_to_sensor.get(computer)){
                mass += this.component_masses.get(sensor.substring(0, 2));
            }
        }

        if(this.is_heterogeneous(computer_to_sensor)){
            mass += this.dissimilar_component_penalty;
        }

        System.out.println("--> SYSTEM MASS " + mass);
        return mass;
    }

    public boolean is_heterogeneous(HashMap<String, ArrayList<String>> computer_to_sensor){

        // Check computers
        Set<String> computers = computer_to_sensor.keySet();
        ArrayList<String> computers_dup = new ArrayList<>();
        for(String computer: computers){
            computers_dup.add(computer.substring(0, 2));
        }
        Set<String> computers_shrt = new HashSet<>(computers_dup);
        if(computers_shrt.size() < computers_dup.size()){
            return true;
        }

        // Check sensors
        ArrayList<String> sensors = new ArrayList<>();
        for(String computer: computer_to_sensor.keySet()){
            for(String sensor: computer_to_sensor.get(computer)){
                if(!sensors.contains(sensor)){
                    sensors.add(sensor);
                }
            }
        }
        ArrayList<String> sensors_dup = new ArrayList<>();
        for(String sensor: sensors){
            sensors_dup.add(sensor.substring(0, 2));
        }
        Set<String> sensors_shrt = new HashSet<>(sensors_dup);
        if(sensors_shrt.size() < sensors_dup.size()){
            return true;
        }

        return false;
    }






    public double evaluate_reliability(String design_str){

//
//
//
//        System.out.println("---> INITIAL RELIABILITY CALC " + calc_reliability);
//        App.sleep(2);
//
//
//        System.out.println("---> EVALUATING RELIABILITY");
//        System.out.println(design_str);
//
//        JsonArray design = this.parseDesignString(design_str);
//
//
//        ArrayList<String> computer_names = this.extractComputers(design);
//        ArrayList<Double> computer_reliabilities = new ArrayList<>();
//        for(String name: computer_names){
//            if(name.equals("nil")){
//                computer_reliabilities.add(0.0);
//            }
//            else{
//                computer_reliabilities.add(this.component_reliability.get(name.substring(0, 2)));
//            }
//        }
//
//        ArrayList<String> sensor_names = this.extractSensors(design);
//        ArrayList<Double> sensor_reliabilities = new ArrayList<>();
//        for(String name: sensor_names){
//            if(name.equals("nil")){
//                sensor_reliabilities.add(0.0);
//            }
//            else{
//                sensor_reliabilities.add(this.component_reliability.get(name.substring(0, 2)));
//            }
//        }
//
//        HashMap<String, ArrayList<String>> design_map = this.designToHashMap(design);
//
//        ArrayList<ArrayList<Double>> connections = this.extractConnectionMatrix(design_map,  sensor_names, computer_names);
//
//        double reliability = this.minimum_cut_sets_fast(sensor_reliabilities, computer_reliabilities, connections);
//        double n9          = this.transform_reliability(reliability);


        Reliability rel_evaluator = new Reliability(1, 10);
        double reliability = rel_evaluator.evaluate(design_str);
        double n9 = rel_evaluator.transform_reliability(reliability);


        System.out.println("--> SYSTEM RELIABILITY      " + reliability);
        System.out.println("--> SYSTEM N9s              " + n9);
        App.sleep(2);

        return n9;
    }


    public ArrayList<ArrayList<Double>> extractConnectionMatrix(HashMap<String, ArrayList<String>> design_map, ArrayList<String> sensor_list, ArrayList<String> computer_list){
        ArrayList<ArrayList<Double>> connections = new ArrayList<>();


        for(String sensor: sensor_list){
            ArrayList<Double> sensor_row = new ArrayList<>();
            if(sensor.equals("nil")){
                sensor_row.add(0.0);
                sensor_row.add(0.0);
                sensor_row.add(0.0);
                connections.add(sensor_row);
                continue;
            }

            for(String computer: computer_list){
                if(computer.equals("nil")){
                    sensor_row.add(0.0);
                }
                else if(this.isSensorAssignedToComputer(design_map, sensor, computer)){
                    // sensor_row.add(this.connection_weight);
                    sensor_row.add(1.0);
                }
                else{
                    sensor_row.add(0.0);
                }
            }
            connections.add(sensor_row);
        }



        return connections;
    }


    public boolean isSensorAssignedToComputer(HashMap<String, ArrayList<String>> design_map, String sensor_str, String computer_str){

        ArrayList<String> computer_sensors = design_map.get(computer_str);
        if(computer_sensors == null){
            return false;
        }

        return (computer_sensors.contains(sensor_str));
    }


    public HashMap<String, ArrayList<String>> designToHashMap(JsonArray design){
        HashMap<String, ArrayList<String>> design_map = new HashMap<>();

        for(int x = 0; x < design.size(); x++){
            JsonObject computer = design.get(x).getAsJsonObject();
            String computer_name = computer.get("name").getAsString();
            design_map.put(computer_name, new ArrayList<>());

            JsonArray computer_sensors = computer.getAsJsonArray("elements");
            for(int y = 0; y < computer_sensors.size(); y++){
                JsonObject sensor = computer_sensors.get(y).getAsJsonObject();
                String sensor_name = sensor.get("name").getAsString();
                design_map.get(computer_name).add(sensor_name);
            }
        }

        System.out.println("--> DESIGN HASH MAP: " + design_map);

        return design_map;
    }

    public ArrayList<String> extractSensors(JsonArray design){
        ArrayList<String> sensor_names = new ArrayList<>();

        for(int x = 0; x < design.size(); x++){
            JsonArray computer_sensors = design.get(x).getAsJsonObject().getAsJsonArray("elements");
            for(int y = 0; y < computer_sensors.size(); y++){
                sensor_names.add(computer_sensors.get(y).getAsJsonObject().get("name").getAsString());
            }
        }

        // Remove duplicates
        Set<String> set = new LinkedHashSet<>();
        set.addAll(sensor_names);
        sensor_names.clear();
        sensor_names.addAll(set);

        while(sensor_names.size() < 3){
            sensor_names.add("nil");
        }

        System.out.println("--> SENSOR NAMES");
        System.out.println(sensor_names);

        return sensor_names;
    }

    public ArrayList<String> extractComputers(JsonArray design){
        ArrayList<String> computer_names = new ArrayList<>();

        for(int x = 0; x < design.size(); x++){
            computer_names.add(design.get(x).getAsJsonObject().get("name").getAsString());
        }

        while(computer_names.size() < 3){
            computer_names.add("nil");
        }

        System.out.println("--> COMPUTER NAMES AND PROBABILITIES");
        System.out.println(computer_names);

        return computer_names;
    }



    /*
       sensors: 3x1 array holding reliability values (0 if sensor not in arch)
       computers: 3x1 array holding reliability values (0 if computer not in arch)
       connections: 3x3 array holding reliability values (0 if connection not in arch)
    */
    public double minimum_cut_sets_fast(ArrayList<Double> sensors, ArrayList<Double> computers, ArrayList<ArrayList<Double>> connections){

        double s1 = sensors.get(0);
        double s2 = sensors.get(1);
        double s3 = sensors.get(2);


        double c1 = computers.get(0);
        double c2 = computers.get(1);
        double c3 = computers.get(2);

        // ixy -> x is sensor number -> y is computer number
        double i11 = connections.get(0).get(0);
        double i12 = connections.get(0).get(1);
        double i13 = connections.get(0).get(2);

        double i21 = connections.get(1).get(0);
        double i22 = connections.get(1).get(1);
        double i23 = connections.get(1).get(2);

        double i31 = connections.get(2).get(0);
        double i32 = connections.get(2).get(1);
        double i33 = connections.get(2).get(2);


        return (s1*i12*c2+s1*i11*c1+s3*i33*c3+s1*i13*c3+s3*i32*c2+s2*i23*c3+s2*i22*c2+s3*i31*c1+s2*i21*c1+s1*s3*i12*i31*c1*c2*i32-s1*s3*i13*i31*c1*c3-s1*s2*i12*c2*i22+s3*i13*i23*i31*i33*c1*c2*s1*s2*i32*c3-s3*i13*i23*i31*i33*c1*c2*s1*s2*i11*i32*c3+s3*i13*i23*i31*i33*c1*c2*s1*s2*i12*c3-s3*i13*i23*i31*i33*c1*c2*s1*s2*i11*i12*c3-s3*i13*i22*i23*i31*i33*c1*c2*s1*s2*i11*c3-s1*s2*i11*i12*i22*i23*i31*i33*c2*s3*c1*c3-s2*s3*i23*i33*c3+s1*i12*i13*i31*i32*c1*c2*s3*i11*c3+s1*i12*i13*i31*i32*c1*c2*s3*i33*c3-s1*i12*i13*i31*i32*c1*c2*s3*c3+s1*i12*i13*i31*i32*c1*c2*s2*s3*i22*c3-s1*i12*i13*i31*i32*c1*c2*s2*s3*i23*i33*c3+s1*i12*i13*i31*i32*c1*c2*s2*s3*i11*i23*i33*c3-s1*i12*i13*i31*i32*c1*c2*s2*s3*i11*i22*c3-s1*i12*i13*i31*i32*c1*c2*s3*i11*i33*c3+s1*i12*i13*i31*i32*c1*c2*s2*s3*i23*c3-s1*i12*i13*i31*i32*c1*c2*s2*s3*i11*i23*c3+s3*i13*i22*i23*i31*i33*c1*c2*s1*s2*c3-s1*s2*i13*i21*c1*c3+s1*i13*i21*i23*i32*c3*s2*s3*i33*c1*c2-s1*i13*i21*i23*i32*c3*s2*s3*c1*c2+s1*i13*i21*i23*i32*c3*s2*s3*i11*c1*c2+s1*i13*i21*i23*i32*c3*s2*s3*i22*c1*c2-s1*i13*i21*i23*i32*c3*s2*s3*i11*i22*c1*c2-s1*i13*i21*i23*i32*c3*s2*s3*i11*i33*c1*c2+s2*i12*i13*i22*i31*i33*c2*c3*s1*s3*c1-s2*i12*i13*i22*i31*i33*c2*c3*s1*s3*i11*c1-s2*i12*i13*i22*i31*i33*c2*c3*s1*s3*i23*c1+s1*s2*s3*i11*i12*i13*i21*i22*i23*i31*i32*i33*c1*c2*c3+s2*s3*i22*i23*i33*c2*c3-s1*s2*i11*i12*i22*i23*i33*c1*c2*s3*i13*c3-s1*s2*i11*i12*i22*i23*i33*c1*c2*s3*i32*c3+s1*s2*i11*i12*i22*i23*i33*c1*c2*s3*c3-s2*i12*i21*i23*i33*c1*c3*s1*s3*c2+s2*i12*i21*i23*i33*c1*c3*s1*s3*i22*c2+s2*i12*i21*i23*i33*c1*c3*s1*s3*i11*c2-s2*i21*i31*c1*c2*s1*s3*i12*i23*c3+s2*i21*i31*c1*c2*s3*i22*i23*i32*c3-s2*i21*i31*c1*c2*s1*s3*i13*i32*c3-s2*i21*i31*c1*c2*s1*s3*i11*i12*i13*i22*c3+s2*i21*i31*c1*c2*s1*s3*i12*i13*i22*c3+s2*i21*i31*c1*c2*s1*s3*i11*i13*i22*c3+s2*i21*i31*c1*c2*s3*i32-s2*i21*i31*c1*c2*s3*i23*i32*c3+s2*i11*i12*i21*i23*i32*c1*c3*s1*s3*c2-s2*i11*i12*i21*i23*i32*c1*c3*s1*s3*i22*c2-s2*i11*i12*i21*i23*i32*c1*c3*s1*s3*i33*c2+s2*i12*i13*i21*i32*c1*s1*s3*i23*c2*c3+s2*i12*i13*i21*i32*c1*s1*s3*i31*c2*c3+s2*i12*i13*i21*i32*c1*s1*s3*i33*c2*c3-s2*i12*i13*i21*i32*c1*s1*s3*i22*i31*c2*c3-s2*i12*i13*i21*i32*c1*s1*s3*i11*i23*c2*c3-s2*i12*i13*i21*i32*c1*s1*s3*i22*i23*c2*c3-s2*i12*i13*i21*i32*c1*s1*s3*i22*i33*c2*c3-s2*i12*i13*i21*i32*c1*s1*s3*i11*i31*c2*c3+s1*s2*i22*i23*i31*i32*c1*c2*s3*i11*c3+s1*s2*i22*i23*i31*i32*c1*c2*s3*i13*c3+s1*s2*i22*i23*i31*i32*c1*c2*s3*i12*c3-s1*s2*i22*i23*i31*i32*c1*c2*s3*i11*i33*c3+s1*s2*i22*i23*i31*i32*c1*c2*s3*i11*i12*i33*c3+s1*s2*i22*i23*i31*i32*c1*c2*s3*i11*i13*i33*c3+s1*s2*i22*i23*i31*i32*c1*c2*s3*i12*i13*i33*c3-s1*s2*i22*i23*i31*i32*c1*c2*s3*i11*i12*i13*i33*c3-s1*s2*i22*i23*i31*i32*c1*c2*s3*i11*i13*c3-s1*s2*i22*i23*i31*i32*c1*c2*s3*i12*i13*c3+s1*s2*i22*i23*i31*i32*c1*c2*s3*i11*i12*i13*c3-s1*s3*i13*i33*c3+s2*i21*i31*c1*c2*s3*i22*i23*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i22*i33*c3-s2*i21*i31*c1*c2*s3*i32*i33*c3-s2*i21*i31*c1*c2*s1*s3*i11*i12*i23*i32*c3+s2*i21*i31*c1*c2*s1*s3*i11*i12*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i23*i32*c3-s2*i21*i31*c1*c2*s1*s3*i11*i22*i23*i32*c3-s2*i21*i31*c1*c2*s1*s3*i12*i22*i23*i32*c3-s2*i21*i31*c1*c2*s1*s3*i11*i13*i23*i32*c3-s2*i21*i31*c1*c2*s1*s3*i13*i22*i23*i32*c3+s2*i21*i31*c1*c2*s1*s3*i11*i13*i22*i23*i32*c3+s2*i21*i31*c1*c2*s1*s3*i11*i12*i22*i23*i32*c3+s2*i21*i31*c1*c2*s1*s3*i11*i13*i32*c3+s2*i21*i31*c1*c2*s1*s3*i13*i22*i32*c3+s2*i21*i31*c1*c2*s1*s3*i12*i23*i32*c3+s2*i21*i31*c1*c2*s1*s3*i13*i23*i32*c3-s2*i21*i31*c1*c2*s1*s3*i11*i13*i22*i32*c3+s2*i21*i31*c1*c2*s1*s3*i11*i12*i32+s2*i21*i31*c1*c2*s3*i22-s2*i21*i31*c1*c2*s1*s3*i11*i12*i13*i23*c3-s2*i21*i31*c1*c2*s1*s3*i12*i13*c3+s2*i21*i31*c1*c2*s1*s3*i11*i22*i32+s2*i21*i31*c1*c2*s1*s3*i12*i22*i32-s2*i21*i31*c1*c2*s1*s3*i11*i12*i22*i32-s2*i21*i31*c1*c2*s3*i22*i23*c3-s2*i21*i31*c1*c2*s3*i22*i33*c3-s2*i21*i31*c1*c2*s1*s3*i11*i12-s2*i21*i31*c1*c2*s1*s3*i11*i22-s2*i21*i31*c1*c2*s1*s3*i12*i22+s2*i21*i31*c1*c2*s1*s3*i12-s2*i21*i31*c1*c2*s3*i22*i32-s1*s2*i22*i23*i31*i32*c1*c2*s3*i11*i12*c3-s1*s2*i22*i23*i31*i32*c1*c2*s3*i13*i33*c3-s1*s2*i22*i23*i31*i32*c1*c2*s3*i12*i33*c3-s2*s3*i11*i12*i21*i22*i33*c1*c3*s1*i23*c2-s2*s3*i11*i12*i21*i22*i33*c1*c3*s1*i13*c2-s2*s3*i11*i12*i21*i22*i33*c1*c3*s1*i32*c2+s2*s3*i11*i12*i21*i22*i33*c1*c3*s1*c2+s2*s3*i11*i12*i21*i22*i33*c1*c3*s1*i23*i32*c2+s2*s3*i11*i12*i21*i22*i33*c1*c3*s1*i13*i23*c2-s2*i12*i13*i21*i32*c1*s1*s3*i23*i31*c2*c3-s2*i12*i13*i21*i32*c1*s1*s3*i31*i33*c2*c3-s2*i12*i13*i21*i32*c1*s1*s3*i23*i33*c2*c3+s2*i12*i13*i21*i32*c1*s1*s3*i11*i22*i33*c2*c3-s2*i12*i13*i21*i32*c1*s1*s3*i11*i33*c2*c3+s2*i12*i13*i21*i32*c1*s1*s3*i11*i22*i23*c2*c3+s2*i12*i13*i21*i32*c1*s1*s3*i11*i22*i31*c2*c3+s2*i12*i13*i21*i32*c1*s1*s3*i11*i23*i31*c2*c3+s2*i12*i13*i21*i32*c1*s1*s3*i22*i23*i31*c2*c3-s2*i12*i13*i21*i32*c1*s1*s3*i11*i22*i23*i31*c2*c3+s2*i12*i13*i21*i32*c1*s1*s3*i22*i31*i33*c2*c3+s2*i12*i13*i21*i32*c1*s1*s3*i11*i23*i33*c2*c3+s2*i12*i13*i21*i32*c1*s1*s3*i22*i23*i33*c2*c3+s2*i12*i13*i21*i32*c1*s1*s3*i11*i31*i33*c2*c3-s2*i12*i13*i21*i32*c1*s1*s3*i11*i22*i23*i33*c2*c3-s2*i12*i13*i21*i32*c1*s1*s3*i11*i22*i31*i33*c2*c3-s2*i12*i13*i21*i32*c1*s1*s3*i11*i22*c2*c3-s1*i11*i12*c1*c2+s1*s2*i11*i21*i23*c1*c3-s2*i12*i13*i21*i32*c1*s1*s3*c2*c3-s2*i21*i31*c1*c2*s1*s3*i12*i32-s2*i21*i31*c1*c2*s1*s3*i11*i32-s2*i21*i31*c1*c2*s3*i22*i23*i32*i33*c3+s2*i21*i31*c1*c2*s1*s3*i13*i32*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i32*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i12*i13*i22*i33*c3-s2*i21*i31*c1*c2*s1*s3*i12*i13*i22*i33*c3-s2*i21*i31*c1*c2*s1*s3*i11*i13*i22*i33*c3-s2*i21*i31*c1*c2*s1*s3*i11*i12*i22*i33*c3-s2*i21*i31*c1*c2*s1*s3*i12*i33*c3+s2*i21*i31*c1*c2*s3*i23*i32*i33*c3+s2*i21*i31*c1*c2*s3*i22*i32*i33*c3+s2*i21*i31*c1*c2*s1*s3*i12*i22*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i12*i23*i32*i33*c3-s2*i21*i31*c1*c2*s1*s3*i11*i12*i32*i33*c3-s2*i21*i31*c1*c2*s1*s3*i11*i22*i32*i33*c3-s2*i21*i31*c1*c2*s1*s3*i12*i22*i32*i33*c3-s2*i21*i31*c1*c2*s1*s3*i11*i23*i32*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i22*i23*i32*i33*c3+s2*i21*i31*c1*c2*s1*s3*i12*i22*i23*i32*i33*c3+s2*i12*i13*i21*i32*c1*s1*s3*i22*c2*c3+s2*i12*i13*i21*i32*c1*s1*s3*i11*c2*c3-s2*i13*i21*i23*i31*i32*i33*c3*s1*s3*c1*c2+s2*i13*i21*i23*i31*i32*i33*c3*s1*s3*i22*c1*c2+s2*i13*i21*i23*i31*i32*i33*c3*s1*s3*i12*c1*c2+s2*i13*i21*i23*i31*i32*i33*c3*s1*s3*i11*c1*c2-s2*i13*i21*i23*i31*i32*i33*c3*s1*s3*i11*i12*c1*c2-s2*i13*i21*i23*i31*i32*i33*c3*s1*s3*i11*i22*c1*c2-s2*i13*i21*i23*i31*i32*i33*c3*s1*s3*i12*i22*c1*c2-s1*i12*i13*i22*i32*i33*c1*c2*s2*s3*i31*c3-s1*i12*i13*i22*i32*i33*c1*c2*s2*s3*i11*c3+s1*i12*i13*i22*i32*i33*c1*c2*s2*s3*i11*i23*c3+s1*i12*i13*i22*i32*i33*c1*c2*s2*s3*i11*i31*c3+s1*s2*i11*i12*i13*i22*i23*i31*i33*c1*s3*c2*c3-s3*i13*i21*i22*i23*i32*i33*c2*s1*s2*c1*c3+s3*i13*i21*i22*i23*i32*i33*c2*s1*s2*i11*c1*c3+s2*i21*i31*c1*c2*s1*s3*i11*i12*i13*c3+s2*i21*i31*c1*c2*s1*s3*i11*i12*i23*c3+s2*i21*i31*c1*c2*s1*s3*i12*i22*i23*c3+s2*i21*i31*c1*c2*s1*s3*i12*i13*i23*c3+s2*i21*i31*c1*c2*s1*s3*i13*i22*i23*c3+s2*i21*i31*c1*c2*s1*s3*i11*i12*i13*i22*i23*c3-s2*i21*i31*c1*c2*s1*s3*i12*i13*i22*i23*c3-s2*i21*i31*c1*c2*s1*s3*i11*i13*i22*i23*c3-s2*i21*i31*c1*c2*s1*s3*i11*i12*i22*i23*c3+s2*i21*i31*c1*c2*s1*s3*i12*i32*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i12*i22-s2*i21*i31*c1*c2*s1*s3*i11*i12*i22*i23*i32*i33*c3-s2*i21*i31*c1*c2*s1*s3*i11*i13*i32*i33*c3-s2*i21*i31*c1*c2*s1*s3*i13*i22*i32*i33*c3-s2*i21*i31*c1*c2*s1*s3*i12*i23*i32*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i13*i22*i32*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i12*i22*i32*i33*c3-s2*i21*i31*c1*c2*s1*s3*i11*i12*i13*i33*c3-s2*i21*i31*c1*c2*s1*s3*i11*i12*i23*i33*c3-s2*i21*i31*c1*c2*s1*s3*i12*i22*i23*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i12*i13*i23*i33*c3-s2*i21*i31*c1*c2*s1*s3*i12*i13*i23*i33*c3-s2*i21*i31*c1*c2*s1*s3*i13*i22*i23*i33*c3-s2*i21*i31*c1*c2*s1*s3*i11*i12*i13*i22*i23*i33*c3+s2*i21*i31*c1*c2*s1*s3*i12*i13*i22*i23*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i13*i22*i23*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i12*i22*i23*i33*c3+s2*i21*i31*c1*c2*s1*s3*i12*i13*i33*c3+s2*i21*i31*c1*c2*s1*s3*i13*i22*i33*c3+s2*i21*i31*c1*c2*s1*s3*i12*i23*i33*c3+s2*i21*i31*c1*c2*s1*s3*i11*i22*i23*c3-s2*i21*i31*c1*c2*s1*s3*i11*i22*i23*i33*c3-s2*i21*i31*c1*c2*s1*s3*i13*i22*c3+s1*s2*i21*i31*c1*s3*i13*i23*i33*c3+s1*s2*i21*i31*c1*s3*i11*i13*i33*c3-s1*s2*i21*i31*c1*s3*i11*i13*c3+s1*s2*i21*i31*c1*s3*i11*i13*i23*c3-s1*s2*i21*i31*c1*s3*i13*i23*c3+s1*s2*i21*i31*c1*s3*i11+s1*s2*i21*i31*c1*s3*i11*i23*i33*c3-s1*s2*i21*i31*c1*s3*i13*i33*c3-s1*s2*i21*i31*c1*s3*i11*i13*i23*i33*c3-s2*s3*i22*i31*c1*c2-s3*i32*i33*c2*c3+s1*s2*i21*i31*c1*s3*i13*c3-s1*s2*i21*i31*c1*s3*i11*i23*c3-s1*s2*i21*i31*c1*s3*i11*i33*c3-s2*i12*i21*i23*i33*c1*c3*s1*s3*i11*i13*c2-s2*i12*i21*i23*i33*c1*c3*s1*s3*i13*i22*c2-s2*i12*i21*i23*i33*c1*c3*s1*s3*i22*i32*c2+s2*i12*i21*i23*i33*c1*c3*s1*s3*i13*c2+s2*i12*i21*i23*i33*c1*c3*s1*s3*i32*c2-s1*s3*i12*i32*c2-s2*i11*i13*i21*i22*i33*c1*s1*s3*i32*c2*c3+s2*i11*i13*i21*i22*i33*c1*s1*s3*c2*c3-s2*i11*i13*i21*i22*i33*c1*s1*s3*i23*c2*c3-s2*i13*i21*i33*c1*c3*s1*s3*i23-s2*i13*i21*i33*c1*c3*s1*s3*i22*c2-s2*i13*i21*i33*c1*c3*s1*s3*i12*c2+s2*i13*i21*i33*c1*c3*s1*s3*i11*i23+s2*i13*i21*i33*c1*c3*s1*s3*i22*i23*c2+s2*i13*i21*i33*c1*c3*s1*s3*i22*i32*c2+s2*i13*i21*i33*c1*c3*s1*s3*i11*i32*c2-s2*i13*i21*i33*c1*c3*s1*s3*i11+s2*i13*i21*i33*c1*c3*s1*s3*i11*i12*c2+s2*i13*i21*i33*c1*c3*s1*s3*i12*i22*c2+s2*i13*i21*i33*c1*c3*s1*s3-s2*i13*i21*i33*c1*c3*s1*s3*i32*c2+s2*i12*i21*i22*i32*i33*c1*c2*c3*s1*s3-s1*s2*i11*i22*i23*i33*c1*s3*c2*c3+s1*s2*i11*i22*i23*i33*c1*s3*i31*c2*c3+s1*s2*i11*i22*i23*i33*c1*s3*i32*c2*c3+s1*s2*i11*i22*i23*i33*c1*s3*i21*c2*c3-s1*s2*i11*i22*i23*i33*c1*s3*i21*i32*c2*c3+s1*s2*i11*i22*i23*i33*c1*s3*i13*c2*c3-s1*s2*i11*i22*i23*i33*c1*s3*i13*i32*c2*c3-s2*i11*i12*i13*i22*i23*c2*c3*s1*i21*c1-s2*i11*i12*i13*i22*i23*c2*c3*s1*s3*i32*c1+s2*i11*i12*i13*i22*i23*c2*c3*s1*c1-s2*i11*i12*i13*i22*i23*c2*c3*s1*s3*i31*c1-s1*s3*i12*i31*c1*c2-s1*s3*i12*i31*c1*c2*i11*i33*c3-s1*s3*i12*i31*c1*c2*i11*i13*c3-s1*s3*i12*i31*c1*c2*i13*i33*c3-s1*s3*i12*i31*c1*c2*i32*i33*c3+s1*s3*i12*i31*c1*c2*s2*i23*c3-s1*s3*i12*i31*c1*c2*s2*i11*i22-s1*s3*i12*i31*c1*c2*s2*i22*i32+s1*s3*i12*i31*c1*c2*i33*c3-s1*s3*i12*i31*c1*c2*s2*i22*i33*c3-s1*s3*i12*i31*c1*c2*s2*i23*i33*c3-s1*s3*i12*i31*c1*c2*s2*i22*i23*c3+s1*s3*i12*i31*c1*c2*s2*i11*i22*i32+s1*s3*i12*i31*c1*c2*s2*i11*i23*i33*c3+s1*s3*i12*i31*c1*c2*s2*i22*i23*i33*c3+s1*s3*i12*i31*c1*c2*s2*i11*i22*i23*c3+s1*s3*i12*i31*c1*c2*s2*i11*i22*i33*c3-s1*s3*i12*i31*c1*c2*i11*i32-s1*s3*i12*i31*c1*c2*s2*i11*i23*c3+s1*s3*i12*i31*c1*c2*i13*c3+s1*s3*i12*i31*c1*c2*i11*i13*i33*c3+s1*s3*i12*i31*c1*c2*i11*i32*i33*c3+s1*s3*i12*i31*c1*c2*s2*i22-s1*s3*i12*i31*c1*c2*s2*i23*i32*c3-s1*s3*i12*i31*c1*c2*s2*i13*i23*c3-s1*s3*i12*i31*c1*c2*s2*i13*i22*c3+s1*s3*i12*i31*c1*c2*s2*i11*i13*i22*c3+s1*s3*i12*i31*c1*c2*s2*i11*i13*i23*c3+s1*s3*i12*i31*c1*c2*s2*i13*i22*i23*c3+s1*s3*i12*i31*c1*c2*s2*i11*i23*i32*c3+s1*s3*i12*i31*c1*c2*s2*i22*i32*i33*c3+s1*s3*i12*i31*c1*c2*s2*i23*i32*i33*c3-s1*s3*i12*i31*c1*c2*s2*i11*i22*i32*i33*c3-s1*s3*i12*i31*c1*c2*s2*i11*i23*i32*i33*c3+s1*s3*i12*i31*c1*c2*i11-s3*i31*i33*c1*c3-s2*s3*i23*i32*c2*c3+s2*s3*i23*i32*i33*c2*c3-s1*i11*i13*c1*c3-s2*s3*i12*i23*i32*c2*s1*i22*c3-s2*s3*i12*i23*i32*c2*s1*i33*c3-s2*s3*i12*i23*i32*c2*s1*i13*c3+s2*s3*i12*i23*i32*c2*s1*c3-s2*s3*i12*i23*i32*c2*s1*i11*i13*i33*c1*c3+s2*s3*i12*i23*i32*c2*s1*i22*i33*c3-s2*s3*i12*i23*i32*c2*s1*i13*i22*i33*c3+s2*s3*i12*i23*i32*c2*s1*i13*i22*c3+s2*s3*i12*i23*i32*c2*s1*i13*i33*c3-s2*s3*i12*i23*i32*c2*s1*i11*c1*c3-s2*s3*i12*i23*i32*c2*s1*i21*c1*c3+s2*s3*i12*i23*i32*c2*s1*i11*i22*c1*c3+s2*s3*i12*i23*i32*c2*s1*i11*i13*c1*c3+s2*s3*i12*i23*i32*c2*s1*i11*i33*c1*c3+s2*s3*i12*i23*i32*c2*s1*i21*i22*c1*c3-s2*s3*i22*i32*c2+s1*i11*i12*i13*i21*i22*c2*c3*s2*c1+s2*i12*i22*i32*i33*c1*c2*c3*s1*s3*i11-s1*s3*i11*i31*c1+s2*i12*i13*i22*i23*c2*c3*s1*s3*i33+s2*i12*i13*i22*i23*c2*c3*s1*i21*c1-s2*i12*i13*i22*i23*c2*c3*s1-s1*s2*i13*i21*c1*i11*i23*c3+s1*s2*i13*i21*c1*i22*c2*c3+s1*s2*i13*i21*c1*i12*c2*c3+s1*s2*i13*i21*c1*i11*c3+s1*s2*i13*i21*c1*i23*c3+s1*s2*i13*i21*c1*i11*i12*i23*c2*c3-s1*s2*i13*i21*c1*s3*i22*i32*c2*c3-s1*s2*i13*i21*c1*i11*i22*c2*c3-s1*s2*i13*i21*c1*i22*i23*c2*c3+s1*s2*i13*i21*c1*i11*i22*i23*c2*c3-s1*s2*i13*i21*c1*i12*i22*c2*c3-s1*s2*i13*i21*c1*i11*i12*c2*c3+s1*s2*i13*i21*c1*s3*i32*c2*c3-s1*s2*i13*i21*c1*i12*i23*c2*c3+s1*s2*i13*i21*c1*s3*i11*i22*i32*c2*c3-s1*s2*i13*i21*c1*s3*i11*i32*c2*c3-s2*i21*i22*c1*c2-s1*i13*i31*i32*i33*c3*s3*c1*c2+s1*i13*i31*i32*i33*c3*s3*i11*c1*c2+s1*i13*i31*i32*i33*c3*s2*s3*i22*c1*c2-s1*i13*i31*i32*i33*c3*s2*s3*i11*i22*c1*c2+s1*i11*i21*i23*i32*c1*c2*c3*s2*s3*i33+s1*i11*i21*i23*i32*c1*c2*c3*s2*s3*i22-s1*i11*i21*i23*i32*c1*c2*c3*s2*s3-s2*i22*i23*c2*c3+s2*i13*i22*i23*i32*c2*c3*s1*s3*i33+s2*i13*i22*i23*i32*c2*c3*s1*s3*i11*c1-s2*i13*i22*i23*i32*c2*c3*s1*s3+s1*s2*i12*c2*i11*i22*c1+s1*s2*i12*c2*s3*i22*i32+s1*s2*i12*c2*i22*i23*c3+s1*s2*i12*c2*i13*i22*c3+s1*s2*i12*c2*i13*i23*c3+s1*s2*i12*c2*i11*i21*c1+s1*s2*i12*c2*i21*i22*c1-s1*s2*i12*c2*i23*c3+s1*s2*i12*c2*s3*i11*i21*i22*i32*c1-s1*s2*i12*c2*i21*c1+s1*s2*i12*c2*s3*i21*i32*c1-s1*s2*i12*c2*s3*i11*i21*i32*c1+s1*s2*i12*c2*s3*i23*i33*c3+s1*s2*i12*c2*s3*i22*i33*c3+s1*s2*i12*c2*s3*i13*i22*i32*i33*c3-s1*s2*i12*c2*s3*i22*i32*i33*c3-s1*s2*i12*c2*s3*i13*i23*i33*c3-s1*s2*i12*c2*s3*i13*i22*i33*c3-s1*s2*i12*c2*s3*i13*i22*i32*c3-s1*s2*i12*c2*s3*i22*i23*i33*c3-s1*s2*i12*c2*s3*i11*i22*i33*c1*c3-s1*s2*i12*c2*i11*i21*i22*c1-s1*s2*i12*c2*i11*i13*i22*c1*c3-s1*s2*i12*c2*s3*i11*i22*i32*c1+s1*s2*i12*c2*s3*i11*i13*i22*i32*c1*c3+s1*s2*i12*c2*i21*i23*c1*c3+s1*s2*i12*c2*i11*i23*c1*c3-s1*s2*i12*c2*s3*i21*i22*i32*c1-s1*s2*i12*c2*s3*i11*i23*i33*c1*c3-s1*s2*i12*c2*i11*i21*i23*c1*c3+s1*s2*i12*c2*s3*i11*i13*i22*i33*c1*c3+s1*s2*i12*c2*s3*i21*i33*c1*c3-s1*s2*i12*c2*s3*i21*i32*i33*c1*c3-s1*s2*i12*c2*s3*i11*i21*i33*c1*c3+s1*s2*i12*c2*s3*i11*i21*i32*i33*c1*c3-s1*s2*i12*c2*i11*i22*i23*c1*c3-s1*s2*i12*c2*i21*i22*i23*c1*c3+s1*s2*i12*c2*i11*i21*i22*i23*c1*c3-s1*s2*i12*c2*i11*i13*i23*c1*c3-s1*s2*i12*c2*s3*i21*i22*i33*c1*c3+s1*s2*i12*c2*s3*i11*i13*i23*i33*c1*c3+s1*s3*i11*i13*i31*c1*c3-s1*s3*i11*i33*c1*c3+s1*s3*i11*i31*i33*c1*c3-s1*s3*i11*i13*i31*i33*c1*c3+s1*s3*i13*i31*i33*c1*c3+s1*s3*i11*i13*i33*c1*c3+s2*s3*i13*i23*i32*i33*c1*c2*s1*i11*c3+s1*s3*i23*i32*i33*c1*c2*c3*s2*i11*i31-s1*s3*i23*i32*i33*c1*c2*c3*s2*i11-s2*s3*i22*i33*c2*c3-s1*i11*i13*i31*i32*c1*s3*c2*c3+s1*i11*i13*i31*i32*c1*s2*s3*i23*c2*c3+s1*i11*i13*i31*i32*c1*s2*s3*i22*c2*c3+s2*i22*i23*i32*i33*c1*c2*s3*i31*c3+s2*i22*i23*i32*i33*c1*c2*s3*i21*c3-s1*s2*i13*i22*i31*i33*c2*s3*c1*c3+s1*s2*i13*i22*i31*i33*c2*s3*i11*c1*c3+s2*s3*i11*i31*i32*i33*c3*s1*i22*c1*c2+s1*s2*i11*i23*i32*c3*s3*c1*c2-s1*s2*i11*i23*i32*c3*s3*i22*c1*c2-s1*s2*i11*i23*i32*c3*s3*i31*c1*c2-s1*s2*i11*i23*i32*c3*s3*i13*c1*c2-s1*i11*i22*i32*c1*c2*c3*s2*s3*i33-s1*i11*i22*i32*c1*c2*c3*s2*s3*i13+s1*i11*i22*i32*c1*c2*c3*s2*s3*i21*i33+s1*i11*i22*i32*c1*c2*c3*s2*s3*i13*i33+s2*i23*c1*c2*c3*s1*i11*i22+s2*i23*c1*c2*c3*s3*i22*i31+s2*i23*c1*c2*c3*s3*i21*i32+s2*i23*c1*c2*c3*s3*i31*i32-s2*i23*c1*c2*c3*s3*i22*i31*i33-s2*i23*c1*c2*c3*s3*i21*i22*i33-s2*i23*c1*c2*c3*s1*i11*i21*i22+s2*i23*c1*c2*c3*i21*i22-s2*i23*c1*c2*c3*s3*i22*i31*i32+s2*i23*c1*c2*c3*s1*s3*i11*i13*i22*i31-s2*i23*c1*c2*c3*s1*s3*i13*i31*i32-s2*i23*c1*c2*c3*s1*s3*i11*i22*i31-s2*i23*c1*c2*c3*s1*i11*i13*i22-s2*i23*c1*c2*c3*s1*s3*i13*i22*i31-s2*i23*c1*c2*c3*s3*i21*i32*i33-s2*i23*c1*c2*c3*s3*i31*i32*i33-s2*i23*c1*c2*c3*s3*i21*i22*i32-s3*i31*i32*c1*c2+s1*i11*i12*i13*i32*c1*c3*s3*i33*c2-s1*i11*i12*i13*i32*c1*c3*s3*c2-s1*i12*i32*i33*c1*c2*c3*s3*i11-s2*i11*i13*i22*i33*c1*c2*c3*s1*s3-s2*i21*i23*c1*c3-s1*s2*i11*i21*c1-s1*s3*i13*i32*c2*c3-s1*i12*i13*c2*c3+s1*s3*i12*i13*i32*c2*c3+s2*i21*i33*c1*s3*i23*c3+s2*i21*i33*c1*s3*i31*c3-s2*i21*i33*c1*s3*c3-s2*i21*i33*c1*s3*i22*i32*c2*c3+s2*i21*i33*c1*s3*i22*c2*c3-s2*i21*i33*c1*s3*i23*i31*c3-s2*i21*i33*c1*s1*s3*i11*i22*c2*c3+s2*i21*i33*c1*s3*i32*c2*c3+s2*i21*i33*c1*s1*s3*i11*c3-s2*i21*i33*c1*s1*s3*i11*i23*c3-s2*i21*i33*c1*s1*s3*i11*i32*c2*c3+s2*i11*i23*i31*c3*s1*s3*c1-s2*i11*i23*i31*c3*s1*s3*i33*c1-s2*i11*i23*i31*c3*s1*s3*i13*c1+s2*i11*i23*i31*c3*s1*s3*i13*i33*c1-s2*i13*i22*i31*i32*c2*s1*s3*c1*c3+s2*s3*i22*i31*i32*c1*c2-s2*s3*i21*i31*c1-s2*s3*i21*i32*c1*c2-s1*i11*i21*i32*c1*c2*s2*s3*i22+s1*i11*i21*i32*c1*c2*s2*s3-s1*s2*i13*i23*c3+s1*s2*i13*i22*i23*c2*c3-s1*s2*i13*i22*c2*c3+s1*s2*i11*i13*i22*c1*c2*c3-s1*s2*i11*i13*i22*c1*c2*c3*s3*i31-s3*i11*i22*i31*i32*c1*s1*s2*c2-s2*i11*i22*i31*i33*c2*s1*s3*c1*c3+s2*i11*i22*i33*c1*c2*c3*s1*s3+s1*s2*i13*i22*c1*c2*c3*s3*i31+s1*s3*i13*i32*i33*c2*c3+s1*s3*i12*i32*i33*c2*c3-s1*s3*i12*i33*c2*c3-s1*s3*i12*i13*i32*i33*c2*c3+s1*s3*i12*i13*i33*c2*c3+s1*i13*i22*i33*c3*s2*s3*c2-s1*i13*i22*i33*c3*s2*s3*i23*c2-s1*i13*i22*i33*c3*s2*s3*i32*c2+s3*i12*i32*c1*c2*s1*i11-s3*i22*i32*i33*c1*c2*c3*s2*i31-s1*i11*i12*i13*c2*c3*s3*i33*c1+s1*i11*i12*i13*c2*c3*c1+s2*s3*i22*i23*i32*c2*c3+s1*i11*i12*i33*c1*s3*c2*c3+s2*i21*i22*c1*s1*i11*c2+s2*i21*i22*c1*s3*i32*c2+s2*i22*i31*i33*c1*c2*s3*c3-s1*s3*i13*i32*i33*c1*c2*i11*c3+s1*i11*i13*i32*c1*s3*c2*c3-s1*s3*i11*i32*c1*c2+s1*s3*i11*i31*i32*c1*c2-s2*i22*i23*i33*c3*s3*i32*c2-s1*s2*i11*i22*c1*c2-s2*i23*i32*c3*s1*s3*i13*i33*c2+s2*i23*i32*c3*s1*s3*i13*c2-s2*i11*i13*i23*i33*c1*c3*s1*s3+s2*i23*i33*c1*c3*s3*i31+s2*i23*i33*c1*c3*s1*s3*i11-s2*i23*i33*c1*c3*s1*s3*i13*i31+s3*i31*i32*i33*c1*c2*c3-s3*i31*i32*i33*c1*s1*i11*c2*c3+s1*i11*i32*i33*c1*c2*s3*c3+s2*s3*i22*i32*i33*c2*c3+s1*i13*i31*c1*c3*s3*i32*c2+s1*i13*i31*c1*c3*s2*s3*i23+s1*i13*c1*c3*s2*i11*i23+s2*i13*i22*i32*c2*s1*s3*c3-s2*s3*i23*i31*c1*c3+s1*s3*i11*i22*c1*s2*i31*c2+s1*s3*i11*i22*c1*s2*i32*c2-s1*s2*i11*i23*c1*c3+s2*s3*i13*i23*i33*c3*s1+s2*s3*i21*i23*i31*c1*c3);
    }

    public double transform_reliability(double reliability){

        double result = -(Math.log(1.0-reliability));
        return result;
    }

    public JsonArray parseDesignString(String design){
        return (new JsonParser().parse(design).getAsJsonArray());
    }















    public double full_factorial_reliability(ArrayList<Double> sensor_probabilities, ArrayList<Double> computer_probabilities, String sensor_computer_assignation_bitstring) {
        double result_reliability = 0;

        int num_sensors = sensor_probabilities.size();
        int[] sensor_arr = new int[num_sensors];
        ArrayList<String> sensor_bitstrings = new ArrayList<>();
        this.generateAllBinaryStrings(num_sensors, sensor_arr, 0, sensor_bitstrings);


        int num_computers = computer_probabilities.size();
        int[] computer_arr = new int[num_computers];
        ArrayList<String> computer_bitstrings = new ArrayList<>();
        this.generateAllBinaryStrings(num_computers, computer_arr, 0, computer_bitstrings);

        int num_computer_connections = num_computers * num_sensors;
        int[] computer_connection_arr = new int[num_computer_connections];
        ArrayList<String> computer_connection_bitstrings = new ArrayList<>();
        this.generateAllBinaryStrings(num_computer_connections, computer_connection_arr, 0, computer_connection_bitstrings);


        for (String sensor_bitstr : sensor_bitstrings) {

            double prob_s = this.failure_probability(sensor_probabilities, sensor_bitstr);
            SimpleMatrix sensor_failure_matrix = this.failure_matrix(sensor_bitstr, 1, num_sensors);

            for (String computer_bitstr : computer_bitstrings) {

                double prob_c = this.failure_probability(computer_probabilities, computer_bitstr);
                SimpleMatrix computer_failure_matrix = this.failure_matrix(computer_bitstr, 1, num_computers);

                for(String computer_connection_bitstr: computer_connection_bitstrings){

                 SimpleMatrix sensor_computer_connection_failure_matrix = this.failure_matrix(computer_connection_bitstr, num_sensors, num_computers);
                 SimpleMatrix sensor_computer_assignation_matrix = this.failure_matrix(sensor_computer_assignation_bitstring, num_sensors, num_computers);
                 double prob_i1 = this.connection_failure_probability(sensor_computer_connection_failure_matrix, sensor_computer_assignation_matrix, num_sensors, num_computers);

                 int system_status = this.does_system_fail(sensor_failure_matrix, computer_failure_matrix, sensor_computer_connection_failure_matrix);

                 result_reliability += (prob_s*prob_c*prob_i1*system_status);
                }
            }
        }

        return result_reliability;
    }

    private int does_system_fail(SimpleMatrix sensor_matrix, SimpleMatrix computer_matrix, SimpleMatrix connection_matrix){

        // The resultant matrix should be 1 x 1 !!!!
        SimpleMatrix result_matrix = sensor_matrix.mult(connection_matrix).mult(computer_matrix);
        double result = result_matrix.get(0, 0);
        if(result > 0.0){
            return 1;
        }
        else{
            return 0;
        }
    }



    private SimpleMatrix failure_matrix(String bitstring, int num_rows, int num_cols){
        double[][] matrix = new double[num_rows][num_cols];

        int counter = 0;
        for(int row = 0; row < num_rows; row++){

            for(int col = 0; col < num_cols; col++){
                String bit = (bitstring.charAt(counter) + "");
                if(bit.equals("1")){
                    matrix[row][col] = 1;
                }
                else{
                    matrix[row][col] = 0;
                }
                counter++;
            }
        }
        return (new SimpleMatrix(matrix));
    }


    private double connection_failure_probability(SimpleMatrix component_failures, SimpleMatrix system_connections, int num_rows, int num_cols){
        double prob = 1;

        for(int x = 0; x < num_rows; x++){
            for(int y = 0; y < num_cols; y++){
                if(component_failures.get(x, y) == 1.0){
                    // Probability the connection functions
                    prob = prob * system_connections.get(x, y);
                }
                else{
                    // Probability the connection fails
                    prob = prob * (1 - system_connections.get(x, y));
                }
            }
        }

        return prob;
    }


    /*
        This assume bit_string and probabilities have the same length
     */
    private double failure_probability(ArrayList<Double> probabilities, String bit_string){
        double prob = 1;

        for(int x = 0; x < bit_string.length(); x++){
            String bit = (bit_string.charAt(x) + "");
            if(bit.equals("1")){
                prob = prob * probabilities.get(x);
            }
            else{
                prob = prob * (1 - probabilities.get(x));
            }
        }
        return prob;
    }









    private void generateAllBinaryStrings(int n, int arr[], int i, ArrayList<String> bit_strings) {
        if (i == n)
        {
            String bit_string = "";
            for (int c = 0; c < n; c++)
            {
                bit_string += Integer.toString(arr[c]);
            }
            bit_strings.add(bit_string);
            return;
        }

        arr[i] = 0;
        this.generateAllBinaryStrings(n, arr, i + 1, bit_strings);

        arr[i] = 1;
        this.generateAllBinaryStrings(n, arr, i + 1, bit_strings);
    }









}
