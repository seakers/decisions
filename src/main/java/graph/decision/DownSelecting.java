package graph.decision;

import app.App;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graph.Decision;
import graph.structure.Structure;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;


public class DownSelecting extends Decision {



//     ____        _ _     _
//    |  _ \      (_) |   | |
//    | |_) |_   _ _| | __| | ___ _ __
//    |  _ <| | | | | |/ _` |/ _ \ '__|
//    | |_) | |_| | | | (_| |  __/ |
//    |____/ \__,_|_|_|\__,_|\___|_|


    public static class Builder extends Decision.Builder<Builder>{

        public Builder(Record node) {
            super(node);
        }

        public DownSelecting build() { return new DownSelecting(this); }
    }

    protected DownSelecting(Builder builder){
        super(builder);

    }


//     __  __                       _____                            _                 _
//    |  \/  |                     |  __ \                          | |               (_)
//    | \  / | ___ _ __ __ _  ___  | |  | | ___ _ __   ___ _ __   __| | ___ _ __   ___ _  ___  ___
//    | |\/| |/ _ \ '__/ _` |/ _ \ | |  | |/ _ \ '_ \ / _ \ '_ \ / _` |/ _ \ '_ \ / __| |/ _ \/ __|
//    | |  | |  __/ | | (_| |  __/ | |__| |  __/ |_) |  __/ | | | (_| |  __/ | | | (__| |  __/\__ \
//    |_|  |_|\___|_|  \__, |\___| |_____/ \___| .__/ \___|_| |_|\__,_|\___|_| |_|\___|_|\___||___/
//                      __/ |                  | |
//                     |___/                   |_|


    private JsonArray mergeLastParentDecisions(boolean print){
        JsonArray parents_merged = new JsonArray();
        for(Decision parent: this.parents){
            JsonObject dependency = parent.getLastDecision();
            JsonArray  dependency_elements = dependency.get("elements").getAsJsonArray();
            Iterator   dependency_iterator = dependency_elements.iterator();
            while(dependency_iterator.hasNext()){
                parents_merged.add(((JsonElement) dependency_iterator.next()).getAsJsonObject());
            }
        }
        if (print) {System.out.println("--> Merged Parents: " + this.gson.toJson(parents_merged));}
        return parents_merged;
    }



//  _____                 _                   _____            _
// |  __ \               | |                 |  __ \          (_)
// | |__) |__ _ _ __   __| | ___  _ __ ___   | |  | | ___  ___ _  __ _ _ __
// |  _  // _` | '_ \ / _` |/ _ \| '_ ` _ \  | |  | |/ _ \/ __| |/ _` | '_ \
// | | \ \ (_| | | | | (_| | (_) | | | | | | | |__| |  __/\__ \ | (_| | | | |
// |_|  \_\__,_|_| |_|\__,_|\___/|_| |_| |_| |_____/ \___||___/_|\__, |_| |_|
//                                                                __/ |
//                                                               |___/


    @Override
    public void generateRandomDesign() throws Exception{
        JsonArray          parent_dependencies = this.mergeLastParentDecisions(false);
        ArrayList<Integer> active_indicies     = this.getActiveIndices(parent_dependencies);

        if(active_indicies.isEmpty()){
            throw new Exception("DownSelecting - generateRandomDesign - dependencies are empty " + this.gson.toJson(parent_dependencies));
        }

        JsonArray new_design_elements = parent_dependencies.deepCopy();
        Random    rand                = new Random();

//        Iterator element_itr = new_design_elements.iterator();
//        while(element_itr.hasNext()){
//            JsonObject element = ((JsonElement) element_itr.next()).getAsJsonObject();
//            boolean val        = rand.nextBoolean();
//            element.addProperty("active", val);
//        }

        new_design_elements = this.randomDesignFromCardinality(new_design_elements);

        // Repair design if needed
        new_design_elements = this.repairOperator(new_design_elements);


        // Index design
        this.indexNewDesign(parent_dependencies, new_design_elements);
        this.updateNodeDecisions();
    }

    @Override
    public void generateRandomDesign(JsonArray dependency) throws Exception{
        JsonArray          parent_dependencies = dependency.deepCopy();
        ArrayList<Integer> active_indicies     = this.getActiveIndices(parent_dependencies);

        if(active_indicies.isEmpty()){
            throw new Exception("DownSelecting - generateRandomDesign - dependencies are empty " + this.gson.toJson(parent_dependencies));
        }

        JsonArray new_design_elements = parent_dependencies.deepCopy();
        Random    rand                = new Random();

        Iterator element_itr = new_design_elements.iterator();
        while(element_itr.hasNext()){
            JsonObject element = ((JsonElement) element_itr.next()).getAsJsonObject();
            boolean val        = rand.nextBoolean();
            element.addProperty("active", val);
        }

        // Repair design if needed
        new_design_elements = this.repairOperator(new_design_elements);

        // Index design
        this.indexNewDesign(parent_dependencies, new_design_elements);
        this.updateNodeDecisions();
    }


    public JsonArray randomDesignFromCardinality(JsonArray new_design_elements){
        int num_elements = new_design_elements.size();
        int picked_num = (this.rand.nextInt(num_elements) + 1);

        String bit_string = this.random_bit_string_on_true_values(num_elements, picked_num);

        Iterator element_itr = new_design_elements.iterator();
        int counter = 0;
        while(element_itr.hasNext()){
            JsonObject element = ((JsonElement) element_itr.next()).getAsJsonObject();
            if(bit_string.charAt(counter) == '1'){
                element.addProperty("active", true);
            }
            else{
                element.addProperty("active", false);
            }
            counter++;
        }
        return new_design_elements;
    }

    public String random_bit_string_on_true_values(int length, int true_occurrances){
        ArrayList<String> bit_strings = new ArrayList<>();
        int[] arr = new int[length];
        this.generateAllBinaryStrings(length, arr, 0, bit_strings);

        ArrayList<String> valid_strings = new ArrayList<>();
        for(String bit_str: bit_strings){
            int occurrances = StringUtils.countMatches(bit_str, "1");
            if(occurrances == true_occurrances){
                valid_strings.add(bit_str);
            }
        }

        if(valid_strings.isEmpty()){
            System.out.println("--> NO VALID STRINGS IN LIST " + bit_strings + " FOR " + true_occurrances + " OCCURRANCES OF 1");
            System.exit(0);
        }
        return (valid_strings.get(this.rand.nextInt(valid_strings.size())));
    }



//     _____                  _
//    |  __ \                (_)
//    | |__) |___ _ __   __ _ _ _ __
//    |  _  // _ \ '_ \ / _` | | '__|
//    | | \ \  __/ |_) | (_| | | |
//    |_|  \_\___| .__/ \__,_|_|_|
//               | |
//               |_|


    public JsonArray repairOperator(JsonArray decision){
        Iterator dec_itr = decision.iterator();
        boolean needs_repair = true;

        // At least one element has to be down selected upon
        while(dec_itr.hasNext()){
            JsonObject element = ((JsonElement) dec_itr.next()).getAsJsonObject();
            if(element.get("active").getAsBoolean()){
                needs_repair = false;
            }
        }

        // If repair is needed, filp a random bit to 1 for feasibility
        if(needs_repair){
            int idx = Decision.getRandomIdx(decision);
            decision.get(idx).getAsJsonObject().addProperty("active", true);
        }

        return decision;
    }


//      _____
//     / ____|
//    | |     _ __ ___  ___ ___  _____   _____ _ __
//    | |    | '__/ _ \/ __/ __|/ _ \ \ / / _ \ '__|
//    | |____| | | (_) \__ \__ \ (_) \ V /  __/ |
//     \_____|_|  \___/|___/___/\___/ \_/ \___|_|


    @Override
    public void crossoverDesigns(int papa, int mama, double mutation_probability, JsonArray dependency) throws Exception{
        // Parent Dependencies: used to resolve the result of the crossover
        JsonArray parent_dependencies = dependency;

        this.crossover(papa, mama, mutation_probability, parent_dependencies);
    }

    @Override
    public void crossoverDesigns(int papa, int mama, double mutation_probability) throws Exception{
        // Parent Dependencies: used to resolve the result of the crossover
        JsonArray parent_dependencies = this.mergeLastParentDecisions(false);

        this.crossover(papa, mama, mutation_probability, parent_dependencies);
    }

    public void crossover(int papa, int mama, double mutation_probability, JsonArray parent_dependencies) throws Exception{
        // Parent Dependencies: used to resolve the result of the crossover
        JsonArray child =  parent_dependencies.deepCopy();

        // PAPA
        JsonObject papa_obj      = ((JsonElement) this.decisions.get(papa)).getAsJsonObject();
        JsonArray  papa_elements = papa_obj.get("elements").getAsJsonArray();

        // MAMA
        JsonObject mama_obj      = ((JsonElement) this.decisions.get(mama)).getAsJsonObject();
        JsonArray  mama_elements = mama_obj.get("elements").getAsJsonArray();

        // CROSSOVER
        JsonArray crossover = this.uniformCrossover(papa_elements, mama_elements);

        // RESOLUTION
        child = this.resolutionOperator(crossover, child);

        // MUTATION
        if(Decision.getProbabilityResult(mutation_probability)){ child = this.mutationOperator(child); }
        // child = this.mutationOperator(child);

        // REPAIR
        child = this.repairOperator(child);

        this.indexNewDesign(parent_dependencies, child);
        this.updateNodeDecisions();
    }





    public JsonArray uniformCrossover(JsonArray papa, JsonArray mama){
        JsonArray child = new JsonArray();
        Random    rand  = new Random();

        Iterator papa_itr = papa.iterator();
        Iterator mama_itr = mama.iterator();

        // BOTH PARENTS
        while(papa_itr.hasNext() && mama_itr.hasNext()){
            JsonObject papa_ele = ((JsonElement) papa_itr.next()).getAsJsonObject();
            JsonObject mama_ele = ((JsonElement) mama_itr.next()).getAsJsonObject();

            if(rand.nextBoolean()){
                child.add(papa_ele);
            }
            else{
                child.add(mama_ele);
            }
        }

        // PAPA LONGER
        while(papa_itr.hasNext()){
            JsonObject papa_ele = ((JsonElement) papa_itr.next()).getAsJsonObject();
            if(rand.nextBoolean()){
                child.add(papa_ele);
            }
        }

        // MAMA LONGER
        while(mama_itr.hasNext()){
            JsonObject mama_ele = ((JsonElement) mama_itr.next()).getAsJsonObject();
            if(rand.nextBoolean()){
                child.add(mama_ele);
            }
        }

        return child;
    }

    public JsonArray resolutionOperator(JsonArray crossover, JsonArray child){

        // 1. Iterate over parent deps, down select on them based on crossover operation
        Iterator child_itr = child.iterator();
        int counter = 0;
        while(child_itr.hasNext()){
            JsonObject child_ele     = ((JsonElement) child_itr.next()).getAsJsonObject();
            JsonObject crossover_ele = crossover.get(counter).getAsJsonObject();
            if(Decision.active(child_ele)){
                child_ele.addProperty("active", Decision.active(crossover_ele));
            }
            counter++;
        }

        return child;
    }




//     __  __       _        _   _
//    |  \/  |     | |      | | (_)
//    | \  / |_   _| |_ __ _| |_ _  ___  _ __
//    | |\/| | | | | __/ _` | __| |/ _ \| '_ \
//    | |  | | |_| | || (_| | |_| | (_) | | | |
//    |_|  |_|\__,_|\__\__,_|\__|_|\___/|_| |_|

    // Flip a random bit
    public JsonArray mutationOperator(JsonArray elements){
        int        idx = Decision.getRandomIdx(elements);
        JsonObject obj = elements.get(idx).getAsJsonObject();
        obj.addProperty("active", !Decision.active(obj));
        return elements;
    }

    // Constant mutation probability distributed among each sub-chromosome
    public JsonArray mutationOperator2(JsonArray elements){
        int    num_elements = elements.size();
        double probability  = 1.0 / num_elements;

        for(int x = 0; x < elements.size(); x++){
            JsonObject element = elements.get(x).getAsJsonObject();
            if(Decision.getProbabilityResult(probability)){
                element.addProperty("active", !Decision.active(element));
            }
        }

        return elements;
    }

















//     ______                                      _   _
//    |  ____|                                    | | (_)
//    | |__   _ __  _   _ _ __ ___   ___ _ __ __ _| |_ _  ___  _ __
//    |  __| | '_ \| | | | '_ ` _ \ / _ \ '__/ _` | __| |/ _ \| '_ \
//    | |____| | | | |_| | | | | | |  __/ | | (_| | |_| | (_) | | | |
//    |______|_| |_|\__,_|_| |_| |_|\___|_|  \__,_|\__|_|\___/|_| |_|



    private ArrayList<HashMap<Integer, JsonArray>> getParentEnumerations(){
        ArrayList<HashMap<Integer, JsonArray>> parent_enumerations = new ArrayList<>();

        for(Decision parent: this.parents){
            parent_enumerations.add(parent.getEnumerations(this.node_name, this.node_type));
        }

        return parent_enumerations;
    }

    @Override
    public void enumerateDesignSpace(){

        // 1. Get parent enumerations
        ArrayList<HashMap<Integer, JsonArray>> parent_enumerations = this.getParentEnumerations();

        // 2. Enumerate based on number of dependencies
        if(parent_enumerations.size() == 1){
            this.enumerateSingleDependency(parent_enumerations.get(0));
        }
        else if(parent_enumerations.size() > 1){
            this.enumerateMultiDependency(parent_enumerations);
        }
        else{
            System.out.println("--> Enumeration has invalid amount of parents: " + this.node_name);
            App.sleep(10);
        }
    }

    // Single Parent
    public void enumerateSingleDependency(HashMap<Integer, JsonArray> parent_enumerations){
        for(Integer key: parent_enumerations.keySet()){
            JsonArray elements = parent_enumerations.get(key);

            // 1. Determine the number of elements (num_active) with the (active: true) key
            int num_active = Decision.numActiveElements(elements);

            // 2. Generate all binary strings of length (num_active) in an ArrayList
            ArrayList<String> bit_strings = new ArrayList<>();
            int[] arr = new int[num_active];
            this.generateAllBinaryStrings(num_active, arr, 0, bit_strings);
            System.out.println(bit_strings);

            // 2.1 Prune invalid chromosomes
            ArrayList<String> pruned_chromosomes = this.prune_invalid_chromosomes(bit_strings);

            // 3. Build enumeration_store with this parent enumeration and all possible bit strings
            this.buildEnumerationStore(elements, pruned_chromosomes);
        }
    }

    // Multiple Parents
    public void enumerateMultiDependency(ArrayList<HashMap<Integer, JsonArray>> parent_enumerations){



    }

    /*
        At least one element has to be picked in the down selecting decision
     */
    private ArrayList<String> prune_invalid_chromosomes(ArrayList<String> designs){
        ArrayList<String> pruned_chromosomes = new ArrayList<>();
        for(String design: designs){
            if(design.indexOf('1') != -1){
                pruned_chromosomes.add(design);
            }
        }
        return pruned_chromosomes;
    }



    // Add to this.enumeration_store object
    private void buildEnumerationStore(JsonArray elements, ArrayList<String> bit_strings){
        int enum_counter = this.enumeration_store.keySet().size();
        ArrayList<Integer> active_indicies = this.getActiveIndices(elements);

        for(String bit_string: bit_strings){
            JsonArray new_elements = elements.deepCopy();

            // Augment new_elements to our bit string
            for(int x = 0;x < bit_string.length(); x++){
                char bit = bit_string.charAt(x);
                if(bit == '0'){
                    new_elements.get(active_indicies.get(x)).getAsJsonObject().addProperty("active", false);
                }
                else{
                    new_elements.get(active_indicies.get(x)).getAsJsonObject().addProperty("active", true);
                }
            }

            // this.enumeration_store.put(enum_counter, new_elements);
            this.enumeration_store.put(enum_counter, Structure.pruneInactiveElements(new_elements));
            enum_counter++;
        }
    }

    // Generate all binary strings of length n
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




    @Override
    public ArrayList<JsonArray> enumerateDecision(JsonArray elements){
        ArrayList<JsonArray> enumerations = new ArrayList<>();

        // 1. Determine the number of elements (num_active) with the (active: true) key
        int num_active = Decision.numActiveElements(elements);

        // 2. Generate all binary strings of length (num_active) in an ArrayList
        ArrayList<String> bit_strings = new ArrayList<>();
        int[] arr = new int[num_active];
        this.generateAllBinaryStrings(num_active, arr, 0, bit_strings);

        // 2.1 Remove the '00000' solution
        int chromosome_size = bit_strings.get(0).length();
        String to_remove = "";
        for(int x = 0; x < chromosome_size; x++){
            to_remove += "0";
        }
        if(bit_strings.contains(to_remove)){
            bit_strings.remove(to_remove);
        }


        // 3. Convert binray strings to JsonArray of proper elements
        ArrayList<Integer> active_indicies = this.getActiveIndices(elements);
        for(String bit_string: bit_strings){
            JsonArray new_elements = elements.deepCopy();

            // Augment new_elements to our bit string
            for(int x = 0;x < bit_string.length(); x++){
                char bit = bit_string.charAt(x);
                if(bit == '0'){
                    new_elements.get(active_indicies.get(x)).getAsJsonObject().addProperty("active", false);
                }
                else{
                    new_elements.get(active_indicies.get(x)).getAsJsonObject().addProperty("active", true);
                }
            }
            enumerations.add(new_elements);
        }

        return enumerations;
    }











    @Override
    public String getDesignString(int idx){
        JsonObject design   = this.decisions.get(idx).getAsJsonObject();
        JsonArray  elements = design.getAsJsonArray("elements");
        return this.gson.toJson(elements);
    }




}
