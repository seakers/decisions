package evaluation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import evaluation.reliability.evaluation.Evaluation_Model_2;

import java.util.*;

public class GNC_Evaluator2 {


    public HashMap<String, Double> sensor_types;
    public HashMap<String, Double> computer_types;
    public HashMap<String, Double> actuator_types;

    public HashMap<String, Double> mass_properties;



    public GNC_Evaluator2(){

        this.sensor_types = new HashMap<>();
        this.sensor_types.put("s1", 0.9985);
        this.sensor_types.put("s2", 0.999);
        this.sensor_types.put("s3", 0.9995);

        this.computer_types = new HashMap<>();
        this.computer_types.put("c1", 0.999);
        this.computer_types.put("c2", 0.9996);
        this.computer_types.put("c3", 0.9998);

        this.actuator_types = new HashMap<>();
        this.actuator_types.put("a1", 0.9992);
        this.actuator_types.put("a2", 0.998);
        this.actuator_types.put("a3", 0.999);


        this.mass_properties = new HashMap<>();

        this.mass_properties.put("s1", 3.0);
        this.mass_properties.put("s2", 6.0);
        this.mass_properties.put("s3", 9.0);

        this.mass_properties.put("c1", 3.0);
        this.mass_properties.put("c2", 5.0);
        this.mass_properties.put("c3", 10.0);

        this.mass_properties.put("a1", 3.5);
        this.mass_properties.put("a2", 5.5);
        this.mass_properties.put("a3", 9.5);


    }

    public JsonArray parseDesignString(String design){
        return (new JsonParser().parse(design).getAsJsonArray());
    }


    public double get_component_probability(String component){
        String prefix = component.substring(0, 2);
        if(this.sensor_types.keySet().contains(prefix)){
            return this.sensor_types.get(prefix);
        }
        if(this.computer_types.keySet().contains(prefix)){
            return this.computer_types.get(prefix);
        }
        if(this.actuator_types.keySet().contains(prefix)){
            return this.actuator_types.get(prefix);
        }
        System.out.println("--> COULD NOT GET COMPONENT PROBABILITY: " + prefix);
        System.exit(0);
        return 0;
    }

    public double get_component_mass(String component){
        String prefix = component.substring(0, 2);
        return this.mass_properties.get(prefix);
    }



    public ArrayList<String> get_actuators(JsonArray design){
        ArrayList<String> actuators = new ArrayList<>();
        for(int x = 0; x < design.size(); x++){
            JsonObject component = design.get(x).getAsJsonObject();
            String component_name = component.get("name").getAsString();
            if(component_name.charAt(0) == 'a'){
                actuators.add(component_name);
            }
        }
        Collections.sort(actuators);
        return actuators;
    }

    public ArrayList<String> get_computers(JsonArray design){
        ArrayList<String> computers = new ArrayList<>();
        for(int x = 0; x < design.size(); x++){
            JsonObject component = design.get(x).getAsJsonObject();
            String component_name = component.get("name").getAsString();
            if(component_name.charAt(0) == 'c'){
                computers.add(component_name);
            }
        }
        Collections.sort(computers);
        return computers;
    }

    public ArrayList<String> get_sensors(JsonArray design){
        ArrayList<String> sensors = new ArrayList<>();


        for(int x = 0; x < design.size(); x++){
            JsonObject component = design.get(x).getAsJsonObject();
            String component_name = component.get("name").getAsString();
            if(component_name.charAt(0) == 'c'){
                JsonArray sub_components = component.getAsJsonArray("elements");
                for(int y = 0; y < sub_components.size(); y++){
                    JsonObject sub_component = sub_components.get(y).getAsJsonObject();
                    String sub_component_name = sub_component.get("name").getAsString();
                    if(!sensors.contains(sub_component_name)){
                        sensors.add(sub_component_name);
                    }
                }
            }
        }
        Collections.sort(sensors);
        return sensors;
    }



    public boolean does_component_have_assignation(JsonArray design, String component_to, String component_from){
        for(int x = 0; x < design.size(); x++){
            JsonObject component = design.get(x).getAsJsonObject();
            if(component.get("name").getAsString().equals(component_to)){
                JsonArray sub_components = component.getAsJsonArray("elements");
                for(int y = 0; y < sub_components.size(); y++){
                    JsonObject sub_component = sub_components.get(y).getAsJsonObject();
                    if(sub_component.get("name").getAsString().equals(component_from)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String get_component_assignation(JsonArray design, ArrayList<String> sensors, ArrayList<String> computers){

        String topology = "";
        for(String computer: computers){
            for(String sensor: sensors){
                if(this.does_component_have_assignation(design, computer, sensor)){
                    topology += "1";
                }
                else{
                    topology += "0";
                }
            }
        }

        return topology;
    }

    public ArrayList<Double> get_probability_list(ArrayList<String> components){
        ArrayList<Double> probabilities = new ArrayList<>();
        for(String component: components){
            probabilities.add(this.get_component_probability(component));

        }
        return probabilities;
    }




    public ArrayList<Double> evaluate(String design_str){
        double connection_success_rate = 1;

        System.out.println("\n\n---> EVALUATING DESIGN");
        System.out.println(design_str);

        ArrayList<Double> results = new ArrayList<>();

        JsonArray design = this.parseDesignString(design_str);

        ArrayList<String> actuators = this.get_actuators(design);
        ArrayList<String> computers = this.get_computers(design);
        ArrayList<String> sensors = this.get_sensors(design);

        System.out.println(actuators);
        System.out.println(computers);
        System.out.println(sensors);

        String sensor_to_computer = this.get_component_assignation(design, sensors, computers);
        String computer_to_actuator = this.get_component_assignation(design, computers, actuators);

        ArrayList<Double> actuator_probs = this.get_probability_list(actuators);
        ArrayList<Double> computer_probs = this.get_probability_list(computers);
        ArrayList<Double> sensor_probs = this.get_probability_list(sensors);


        Evaluation_Model_2 model = new Evaluation_Model_2.Builder(sensor_probs, computer_probs, actuator_probs, connection_success_rate)
                .connection_sensor_to_computer(sensor_to_computer)
                .connection_computer_to_actuator(computer_to_actuator)
                .build();

        double reliability = model.evaluate_reliability(true);
        double mass = this.evaluate_mass(sensors, computers, actuators);

        results.add(reliability);
        results.add(mass);


        System.out.println("---> MASS: " + mass);
        System.out.println("---> RELIABILITY: " + reliability);


        return results;
    }


    public double evaluate_mass(ArrayList<String> sensors, ArrayList<String> computers, ArrayList<String> actuators){
        double dissimilar_component_penalty = 5/3;
        double mass = 0;

        for(String sensor: sensors){
            mass += this.get_component_mass(sensor);
        }

        for(String computer: computers){
            mass += this.get_component_mass(computer);
        }

        for(String actuator: actuators){
            mass += this.get_component_mass(actuator);
        }

        if(this.is_heterogeneous(sensors, computers, actuators)){
            mass += dissimilar_component_penalty;
        }

        return mass;
    }

    public boolean is_heterogeneous(ArrayList<String> sensors, ArrayList<String> computers, ArrayList<String> actuators){

        // SENSORS
        Set<String> sensors_shrt = new HashSet<>(sensors);
        if(sensors_shrt.size() == 1){
            Set<String> computers_shrt = new HashSet<>(computers);
            if(computers_shrt.size() == 1){
                Set<String> actuators_shrt = new HashSet<>(actuators);
                if(actuators_shrt.size() == 1){
                    return false;
                }
            }
        }
        return true;
    }

















}
