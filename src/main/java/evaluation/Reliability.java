package evaluation;

import com.google.gson.*;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.HashMap;

public class Reliability {


    private HashMap<String, Double> component_reliability;

    private double connection_reliability;        // 1
    private double years;                         // 10

    private Gson gson;


    public Reliability(double connection_reliability, double years){
        this.connection_reliability = connection_reliability;
        this.years = years;
        this.gson  = new GsonBuilder().setPrettyPrinting().create();


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





    public double evaluate(String design_str){

        JsonArray design = this.parseDesignString(design_str);

        // Data Extraction Methods
        HashMap<String, ArrayList<String>> design_map = this.designToHashMap(design);

        ArrayList<String> computer_names = this.get_computer_names(design_map);
        ArrayList<Double> computer_probs = this.get_computer_probs(computer_names);

        ArrayList<String> sensor_names = this.get_sensor_names(design_map);
        ArrayList<Double> sensor_probs = this.get_sensor_probs(sensor_names);

        String sensor_computer_assignation = this.get_assignation_bit_string(computer_names, sensor_names, design_map);

        // Evaluation Methods
        double reliability = this.full_factorial_evaluation(sensor_probs, computer_probs, sensor_computer_assignation);

        return reliability;
    }


//     _____          _           ______        _                      _    _
//    |  __ \        | |         |  ____|      | |                    | |  (_)
//    | |  | |  __ _ | |_  __ _  | |__   __  __| |_  _ __  __ _   ___ | |_  _   ___   _ __
//    | |  | | / _` || __|/ _` | |  __|  \ \/ /| __|| '__|/ _` | / __|| __|| | / _ \ | '_ \
//    | |__| || (_| || |_| (_| | | |____  >  < | |_ | |  | (_| || (__ | |_ | || (_) || | | |
//    |_____/  \__,_| \__|\__,_| |______|/_/\_\ \__||_|   \__,_| \___| \__||_| \___/ |_| |_|


    public String get_assignation_bit_string(ArrayList<String> computer_names, ArrayList<String> sensor_names, HashMap<String, ArrayList<String>> assignation){
        String bit_string = "";

        for(String computer: computer_names){
            ArrayList<String> computer_sensors = assignation.get(computer);

            for(String sensor: sensor_names){
                if(computer_sensors.contains(sensor)){
                    bit_string += "1";
                }
                else{
                    bit_string += "0";
                }
            }
        }
        return bit_string;
    }

    public ArrayList<String> get_sensor_names(HashMap<String, ArrayList<String>> design_map){
        ArrayList<String> sensor_names = new ArrayList<>();

        for(ArrayList<String> sensors: design_map.values()){
            for(String sensor: sensors){
                if(!sensor_names.contains(sensor)){
                    sensor_names.add(sensor);
                }
            }
        }
        return sensor_names;
    }
    public ArrayList<Double> get_sensor_probs(ArrayList<String> sensor_names){
        ArrayList<Double> sensor_probs = new ArrayList<>();
        for(String sensor: sensor_names){
            String short_name = sensor.substring(0, 2);
            sensor_probs.add(this.component_reliability.get(short_name));
        }
        return sensor_probs;
    }

    public ArrayList<String> get_computer_names(HashMap<String, ArrayList<String>> design_map){

        ArrayList<String> computer_names = new ArrayList<>();
        for(String computer: design_map.keySet()){
            computer_names.add(computer);
        }
        return computer_names;
    }
    public ArrayList<Double> get_computer_probs(ArrayList<String> computer_names){
        ArrayList<Double> computer_probs = new ArrayList<>();
        for(String computer: computer_names){
            String short_name = computer.substring(0, 2);
            computer_probs.add(this.component_reliability.get(short_name));
        }
        return computer_probs;
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

        // System.out.println("--> DESIGN HASH MAP: " + design_map);

        return design_map;
    }

    public JsonArray parseDesignString(String design){
        return (new JsonParser().parse(design).getAsJsonArray());
    }




//     ______       _  _   ______            _                _         _   ______             _                _    _
//    |  ____|     | || | |  ____|          | |              (_)       | | |  ____|           | |              | |  (_)
//    | |__  _   _ | || | | |__  __ _   ___ | |_  ___   _ __  _   __ _ | | | |__ __   __ __ _ | | _   _   __ _ | |_  _   ___   _ __
//    |  __|| | | || || | |  __|/ _` | / __|| __|/ _ \ | '__|| | / _` || | |  __|\ \ / // _` || || | | | / _` || __|| | / _ \ | '_ \
//    | |   | |_| || || | | |  | (_| || (__ | |_| (_) || |   | || (_| || | | |____\ V /| (_| || || |_| || (_| || |_ | || (_) || | | |
//    |_|    \__,_||_||_| |_|   \__,_| \___| \__|\___/ |_|   |_| \__,_||_| |______|\_/  \__,_||_| \__,_| \__,_| \__||_| \___/ |_| |_|



    public double full_factorial_evaluation(ArrayList<Double> sensors, ArrayList<Double> computers, String sensor_computer_assignation){
        double result_reliability = 0;

        double prob_conneciton_success = 1;

        ArrayList<String> sensor_failures = this.generate_component_failures(sensors);

        ArrayList<String> computer_failures = this.generate_component_failures(computers);

        ArrayList<String> connection_failures = this.generate_connection_failures(sensor_computer_assignation);


        int counter = 0;
        for(String sensor_failure: sensor_failures){

            double       prob_s                = this.component_failure_probability(sensors, sensor_failure);
            SimpleMatrix sensor_failure_matrix = this.get_failure_matrix(sensor_failure, 1, sensors.size());
            // 1xn matrix

            for(String computer_failure: computer_failures){

                double       prob_c                  = this.component_failure_probability(computers, computer_failure);
                SimpleMatrix computer_failure_matrix = this.get_failure_matrix(computer_failure, computers.size(), 1);
                // 1xm matrix

                for(String connection_failure: connection_failures){

                    // If any of the connections fail prob_i = 0
                    double       prob_i                    = this.connection_failure_probability(sensor_computer_assignation, connection_failure);
                    SimpleMatrix connection_failure_matrix = this.get_failure_matrix(connection_failure, sensors.size(), computers.size());
                    // nxm matrix

                    double system_status = (double) this.determine_system_failure(sensor_failure_matrix, computer_failure_matrix, connection_failure_matrix);

                    result_reliability += (prob_s*prob_c*prob_i*system_status);

                    // System.out.println("--> RELIABILITY ITR: " + prob_s + " " + prob_c + " " + prob_i + " " + system_status);

                    counter++;
                }
            }
        }

        return result_reliability;
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

    public ArrayList<String> generate_component_failures(ArrayList<Double> components){
        int num_components = components.size();
        int[] component_arr = new int[num_components];
        ArrayList<String> component_bit_strings = new ArrayList<>();
        this.generateAllBinaryStrings(num_components, component_arr, 0, component_bit_strings);
        return component_bit_strings;
    }

    public ArrayList<String> generate_component_failures2(ArrayList<Integer> components){
        int num_components = components.size();
        int[] component_arr = new int[num_components];
        ArrayList<String> component_bit_strings = new ArrayList<>();
        this.generateAllBinaryStrings(num_components, component_arr, 0, component_bit_strings);
        return component_bit_strings;
    }

    public ArrayList<String> generate_connection_failures(String component_assignation){

        // 1. Find all indices where component assignation bit == 1
        ArrayList<Integer> active_indices = new ArrayList<>();
        for(int x = 0; x < component_assignation.length(); x++){
            String bit = (component_assignation.charAt(x) + "");
            if(bit.equals("1")){
                active_indices.add(x);
            }
        }

        // 2. Generate all possible connection failures
        ArrayList<String> failure_scenarios = this.generate_component_failures2(active_indices);

        // 3. Integrate possible connection failures into original chromosome and record
        ArrayList<String> connection_failure_chromosomes = new ArrayList<>();
        for(String failure_scenario: failure_scenarios){
            StringBuilder scenario_integration = new StringBuilder(component_assignation);

            for(int x = 0; x < active_indices.size(); x++){
                int idx = active_indices.get(x);
                char bit = failure_scenario.charAt(x);
                scenario_integration.setCharAt(idx, bit);
            }

            connection_failure_chromosomes.add(scenario_integration.toString());
        }

        return connection_failure_chromosomes;
    }

    private double component_failure_probability(ArrayList<Double> probabilities, String bit_string){
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

    public SimpleMatrix get_failure_matrix(String bitstring, int num_rows, int num_cols){
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

    private double connection_failure_probability(String connection_bit_string, String failure_bit_string){
        double prob = 1;

        for(int x = 0; x < connection_bit_string.length(); x++){
            String orig_bit = (connection_bit_string.charAt(x) + "");
            String new_bit  = (failure_bit_string.charAt(x) + "");

            // Check if there was an original connection
            if(orig_bit.equals("1")){

                // Connection passed
                if(new_bit.equals("1")){
                    prob *= (this.connection_reliability);
                }

                // Connection failed - returned prob is automatically 0
                else{
                    prob *= (1 - this.connection_reliability);
                }

            }
        }

        return prob;
    }

    public int determine_system_failure(SimpleMatrix sensor_failure, SimpleMatrix computer_failure, SimpleMatrix connection_failure){

        SimpleMatrix first_result  = sensor_failure.mult(connection_failure);
        SimpleMatrix result_matrix = first_result.mult(computer_failure);

        double result = result_matrix.get(0, 0);
        if(result > 0.0){
            return 1;
        }
        else{
            return 0;
        }
    }






    public double transform_reliability(double reliability){

        double result = -(Math.log10(1.0-reliability));
        return result;
    }

}
