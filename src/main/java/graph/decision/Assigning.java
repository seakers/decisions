package graph.decision;

import app.App;
import app.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import graph.Decision;
import graph.structure.Structure;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class Assigning extends Decision {

    private String debug_dir = "/app/debug/assigning/";


//     ____        _ _     _
//    |  _ \      (_) |   | |
//    | |_) |_   _ _| | __| | ___ _ __
//    |  _ <| | | | | |/ _` |/ _ \ '__|
//    | |_) | |_| | | | (_| |  __/ |
//    |____/ \__,_|_|_|\__,_|\___|_|


    public static class Builder extends Decision.Builder<Assigning.Builder>{

        public Builder(Record node){
            super(node);
        }

        public Assigning build() { return new Assigning(this); }
    }

    protected Assigning(Assigning.Builder builder){
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



    /*
        Will return a JsonArray containing two items
            1. (from) A JsonArray containing all the items to assign from
            2. (to) A JsonArray containing all the items to assign to

        - Inactive indices are removed when parent dependencies are aggregated
     */
    private JsonArray mergeLastParentDecisions(boolean print){

        // ----- CASES -----

        // 1. Single Parent Dependency
        if(this.parents.size() == 1){
            return this.mergeSingleDependency();
        }
        // 2. Multiple Parent Dependencies (N > 1)
        else if(this.parents.size() > 1){
            return this.mergeMultiDependency();
        }
        else{
            System.out.println("---> ASSIGNING DECISION HAS NO PARENTS !!!");
            System.exit(0);
        }

        return (new JsonArray());
    }

    // One parent
    private JsonArray mergeSingleDependency(){
        JsonArray  parents_merged   = new JsonArray();
        JsonObject dependency       = this.parents.get(0).getLastDecision(this.node_name, this.node_type, 0);

        // Dependency depth not yet implemented in assigning decision
        int        dependency_depth = this.getConstantDecisionDepth(dependency);

        JsonArray components_from;
        JsonArray components_to;
        if(dependency.has("elements_to") && dependency.has("elements_from")){
            components_from = Structure.pruneInactiveElements(dependency.get("elements_from").getAsJsonArray());
            components_to   = Structure.pruneInactiveElements(dependency.get("elements_to").getAsJsonArray());
        }
        else{
            components_from = Structure.pruneInactiveElements(dependency.get("elements").getAsJsonArray());
            components_to   = Structure.pruneInactiveElements(dependency.get("elements").getAsJsonArray());
        }




        parents_merged.add(components_from);
        parents_merged.add(components_to);

        return parents_merged;
    }

    // Multiple parents
    private JsonArray mergeMultiDependency(){
        JsonArray parents_merged = new JsonArray();
        JsonArray assign_from    = new JsonArray();
        JsonArray assign_to      = new JsonArray();

        for(Decision parent: this.parents){
            String     parent_type         = this.getParentRelationshipAttribute(parent, "type");
            JsonObject dependency          = parent.getLastDecision(this.node_name, this.node_type, 0);
            JsonArray  dependency_elements = dependency.get("elements").getAsJsonArray();
            dependency_elements = Structure.pruneInactiveElements(dependency_elements);
            Iterator   dependency_iterator = dependency_elements.iterator();

            if(parent_type.equals("FROM")){
                while(dependency_iterator.hasNext()){
                    assign_from.add(((JsonElement) dependency_iterator.next()).getAsJsonObject().deepCopy());
                }
            }
            else if(parent_type.equals("TO")){
                while(dependency_iterator.hasNext()){
                    assign_to.add(((JsonElement) dependency_iterator.next()).getAsJsonObject().deepCopy());
                }
            }
            else{
                System.out.println("---> PARENT RELATIONSHIP IMPROPERLY SET FOR ASSIGNATION DECISION !!! " + parent_type);
                System.exit(0);
            }
        }
        parents_merged.add(assign_from);
        parents_merged.add(assign_to);

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
        JsonArray parent_dependencies = this.mergeLastParentDecisions(false);

        this.randomDesign(parent_dependencies);
    }

    @Override
    public void generateRandomDesign(JsonArray dependency) throws Exception{

        this.randomDesign(dependency);
    }

    // dependency: will have two elements (at the end the third element will be the integer representation)
    // 1. JsonArray with elements to assign from
    // 2. JsonArray with elements to assign to
    public void randomDesign(JsonArray dependency) throws Exception{

        JsonArray assign_from = dependency.get(0).getAsJsonArray();
        JsonArray assign_to   = dependency.get(1).getAsJsonArray();

        // DEBUG
        this.writeChildDeps(assign_to, assign_from, "random/1-child-components.json");

        ArrayList<Integer> assign_from_active_indicies = this.getActiveIndicies(assign_from);
        ArrayList<Integer> assign_to_active_indicies   = this.getActiveIndicies(assign_to);

        if(assign_from_active_indicies.isEmpty() || assign_to_active_indicies.isEmpty()){
            throw new Exception("Assigning - randomDesign - dependencies are all inactive " + this.gson.toJson(dependency));
        }

        // 1. Find size of bit-string with assigning encoding
        int bit_string_size = assign_from_active_indicies.size() * assign_to_active_indicies.size();

        // 2. Get random bit-string
        ArrayList<Integer> bit_string = this.getRandomBitString(bit_string_size);
        JsonObject bit_string_representation = new JsonObject();
        bit_string_representation.addProperty("bitstring", Decision.bitArrayToString(bit_string));
        dependency.add(bit_string_representation);

        // REPAIR
        bit_string = this.enforce_min_assignation(bit_string, assign_to_active_indicies.size(), assign_from_active_indicies.size());

        // DEBUG
        this.writeChromosome(bit_string, "random/2-rand-chromosome.json");

        // 3. Assign corresponding elements to: assign_to
        JsonArray new_design = this.applyChromosome(bit_string, assign_to, assign_from);

        // DEBUG
        this.writeFinalCrossover(new_design, Decision.bitArrayToString(bit_string), "random/3-final-design.json");

        // Index design
        JsonArray new_design_elements = new_design.deepCopy();
        this.indexNewDesign(dependency, new_design_elements);
    }


    /*
            This function takes a bit array and applies its assignation encoding to child_to and child_from.
        - It is assumed that all the JsonObjects in child_to and child_from are active
     */
    private JsonArray applyChromosome(ArrayList<Integer> child_chromosome, JsonArray child_to, JsonArray child_from){
        JsonArray child = new JsonArray();

        int counter = 0;
        for(int x = 0; x < child_to.size(); x++){
            JsonObject to_element = child_to.get(x).getAsJsonObject().deepCopy();

            boolean has_assignation = false;
            for(int y = 0; y < child_from.size(); y++){
                JsonObject from_element = child_from.get(y).getAsJsonObject().deepCopy();

                if(child_chromosome.get(counter) == 1){
                    has_assignation = true;
                    /*
                            If we are assigning an element A to an element B of type 'item', element B must
                        be turned into an element of type 'list' and keep its name.
                     */
                    if(to_element.get("type").getAsString().equals("item")){
                        to_element.addProperty("type", "list");
                        JsonArray new_elements = new JsonArray();
                        new_elements.add(from_element);
                        to_element.add("elements", new_elements);
                    }
                    // Assigning to a 'list' element
                    else{
                        to_element.getAsJsonArray("elements").add(from_element);
                    }
                }
                counter++;
            }
            if(has_assignation){
                child.add(to_element);
            }
        }

        return child;
    }


//     _____                  _
//    |  __ \                (_)
//    | |__) |___ _ __   __ _ _ _ __
//    |  _  // _ \ '_ \ / _` | | '__|
//    | | \ \  __/ |_) | (_| | | |
//    |_|  \_\___| .__/ \__,_|_|_|
//               | |
//               |_|


    public ArrayList<Integer> repair_null_chromosome(ArrayList<Integer> chromosome){

        // 1. Check if chromosome is completely null
        for(Integer bit: chromosome){
            if(bit == 1){
                return chromosome;
            }
        }

        int rand_idx = this.rand.nextInt(chromosome.size());
        chromosome.set(rand_idx, 1);

        return chromosome;
    }



    /*
        The purpose of this operator is to enforce the following two constraints
            1. Each (assign to) element will have at least one (assign from) element assigned to it
            2. Each (assign from) element will be assigned to at least one (assign to) element
     */
    public ArrayList<Integer> enforce_min_assignation(ArrayList<Integer> chromosome, int num_assign_to, int num_assign_from){

        // 1. Enforce first constraint
        int idx = 0;
        for(int x = 0; x < num_assign_to; x++){

            boolean assign_to_sat = false;
            ArrayList<Integer> assign_to_indices = new ArrayList<>();
            for(int y = 0; y < num_assign_from; y++){
                Integer bit = chromosome.get(idx);
                if(bit.equals(1)){
                    // This assign_to element has at least one assign_from element assigned to it
                    assign_to_sat = true;
                }
                assign_to_indices.add(idx);
                idx++;
            }
            // Assign 1 to a random bit
            if(!assign_to_sat){
                int rand_idx = assign_to_indices.get(this.rand.nextInt(assign_to_indices.size()));
                chromosome.set(rand_idx, 1);
            }
        }

        // 2. Enforce second constraint
        for(int x = 0; x < num_assign_from; x++){

            // For each assign_from element, find all its corresponding bit positions in the chromosome
            ArrayList<Integer> bit_positions = new ArrayList<>();
            for(int y = 0; y < num_assign_to; y++){
                int pos = x + (num_assign_from * y);
                bit_positions.add(pos);
            }

            // Check if each assign_from element is assigned to at least one assign_to element
            boolean assign_from_sat = false;
            for(Integer pos: bit_positions){
                Integer bit = chromosome.get(pos);
                if(bit.equals(1)){
                    assign_from_sat = true;
                }
            }

            // Assign 1 to a random bit pos if the constraint isn't satisfied
            if(!assign_from_sat){
                int rand_idx = bit_positions.get(this.rand.nextInt(bit_positions.size()));
                chromosome.set(rand_idx, 1);
            }
        }

        return chromosome;
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
        JsonArray  child      = parent_dependencies.deepCopy();
        JsonArray  child_from = child.get(0).getAsJsonArray();
        JsonArray  child_to   = child.get(1).getAsJsonArray();

        this.writeChildDeps(child_to, child_from, "crossover/child-components.json");

        // PAPA
        JsonObject papa_obj       = ((JsonElement) this.decisions.get(papa)).getAsJsonObject();
        JsonArray  papa_elements  = papa_obj.get("elements").getAsJsonArray();
        JsonArray  papa_deps      = papa_obj.get("dependencies").getAsJsonArray();
        JsonArray  papa_from      = papa_deps.get(0).getAsJsonArray();
        JsonArray  papa_to        = papa_deps.get(1).getAsJsonArray();
        String     papa_bitstring = papa_deps.get(2).getAsJsonObject().get("bitstring").getAsString();

        this.writeChildDeps(papa_to, papa_from, "crossover/papa-components.json");
        this.writeFinalCrossover(papa_elements, papa_bitstring, "crossover/papa-components-product.json");

        // MAMA
        JsonObject mama_obj       = ((JsonElement) this.decisions.get(mama)).getAsJsonObject();
        JsonArray  mama_elements  = mama_obj.get("elements").getAsJsonArray();
        JsonArray  mama_deps      = mama_obj.get("dependencies").getAsJsonArray();
        JsonArray  mama_from      = mama_deps.get(0).getAsJsonArray();
        JsonArray  mama_to        = mama_deps.get(1).getAsJsonArray();
        String     mama_bitstring = mama_deps.get(2).getAsJsonObject().get("bitstring").getAsString();

        this.writeChildDeps(mama_to, mama_from, "crossover/mama-components.json");
        this.writeFinalCrossover(mama_elements, mama_bitstring, "crossover/mama-components-product.json");


        ArrayList<Integer> papa_chromosome_integrated = this.get_integrated_chromosome(child_to, child_from, papa_bitstring, papa_to, papa_from);
        ArrayList<Integer> mama_chromosome_integrated = this.get_integrated_chromosome(child_to, child_from, mama_bitstring, mama_to, mama_from);


        ArrayList<Integer> child_chromosome = this.combine_integrated_chromosomes(papa_chromosome_integrated, mama_chromosome_integrated);

        // ----- MUTATION OPERATOR -----
        child_chromosome = this.mutationOperator(child_chromosome);

        // ----- REPAIR OPERATOR -----
        // child_chromosome = this.repair_null_chromosome(child_chromosome);
        child_chromosome = this.enforce_min_assignation(child_chromosome, child_to.size(), child_from.size());


        String child_bitstring  = Decision.bitArrayToString(child_chromosome);
        JsonObject bit_string_representation = new JsonObject();
        bit_string_representation.addProperty("bitstring", child_bitstring);
        child.add(bit_string_representation);

        JsonArray child_design = this.applyChromosome(child_chromosome, child_to, child_from);

        // DEBUG
        this.writeFinalCrossover(child_design, child_bitstring, "crossover/child-components-product.json");

        // 7. Index new design
        this.indexNewDesign(child, child_design);
    }


    /*
            This function combines two integrated child chromosomes such that each parent is taken
        from equally. If there are any -1 (null) values in the chromosome left over, these decisions will be randomly decided.
     */
    private ArrayList<Integer> combine_integrated_chromosomes(ArrayList<Integer> papa, ArrayList<Integer> mama){
        ArrayList<Integer> combined_chromosome = new ArrayList<>();

        if(mama.size() != papa.size()){
            System.out.println("--> INTEGRATED CHROMOSOMES ARE OF DIFFERENT LENGTHS!!");
            System.out.println(papa);
            System.out.println(mama);
            System.exit(0);
        }


        int papa_integrations = 0;
        int mama_integrations = 0;

        for(int x = 0; x < papa.size(); x++){
            int papa_bit = papa.get(x);
            int mama_bit = mama.get(x);

            if(papa_bit == -1 && mama_bit == -1){
                if(this.rand.nextBoolean()){
                    combined_chromosome.add(1);
                }
                else{
                    combined_chromosome.add(0);
                }
            }
            else if(papa_bit != -1 && mama_bit == -1){
                combined_chromosome.add(papa_bit);
                papa_integrations++;
            }
            else if(papa_bit == -1 && mama_bit != -1){
                combined_chromosome.add(mama_bit);
                mama_integrations++;
            }
            else{
                if(papa_integrations <= mama_integrations){
                    combined_chromosome.add(papa_bit);
                    papa_integrations++;
                }
                else{
                    combined_chromosome.add(mama_bit);
                    mama_integrations++;
                }
            }
        }

        return combined_chromosome;
    }



    private ArrayList<Integer> get_integrated_chromosome(JsonArray child_to, JsonArray child_from, String parent_bitstring, JsonArray parent_to, JsonArray parent_from){
        ArrayList<Integer> integrated_chromosome = new ArrayList<>();
        for(int x = 0; x < (child_to.size() * child_from.size()); x++){
            integrated_chromosome.add(-1);
        }

        int parent_to_size = parent_to.size();
        int parent_from_size = parent_from.size();
        int child_to_size = child_to.size();
        int child_from_size = child_from.size();

        HashMap<Integer, Integer> to_idx_commonality_map   = this.idx_commonality_map(child_to, parent_to);
        HashMap<Integer, Integer> from_idx_commonality_map = this.idx_commonality_map(child_from, parent_from);


        for(Integer parent_to_idx: to_idx_commonality_map.keySet()){
            Integer child_to_idx = to_idx_commonality_map.get(parent_to_idx);

            for(Integer parent_from_idx: from_idx_commonality_map.keySet()){
                Integer child_from_idx = from_idx_commonality_map.get(parent_from_idx);

                int parent_bit_pos = (parent_to_idx * parent_from_size) + parent_from_idx;
                int parent_bit = Integer.parseInt("" + parent_bitstring.charAt(parent_bit_pos));

                int child_bit_pos = (child_to_idx * child_from_size) + child_from_idx;
                integrated_chromosome.set(child_bit_pos, parent_bit);
            }
        }

        return integrated_chromosome;
    }

    /*
        This function returns a hashmap mapping common parent element indices with common child indices
     */
    private HashMap<Integer, Integer> idx_commonality_map(JsonArray child_elements, JsonArray parent_elements){
        HashMap<Integer, Integer> idx_commonality_map = new HashMap<>();

        for(int x = 0; x < parent_elements.size(); x++){
            JsonObject parent_element = parent_elements.get(x).getAsJsonObject();
            int idx_in_child = this.find_element_index(child_elements, parent_element);
            if(idx_in_child >= 0){
                idx_commonality_map.put(x, idx_in_child);
            }
        }
        return idx_commonality_map;
    }

    private int find_element_index(JsonArray elements, JsonObject to_find){
        for(int x = 0; x < elements.size(); x++){
            JsonObject element = elements.get(x).getAsJsonObject();
            if(element.equals(to_find)){
                return x;
            }
        }
        return -1;
    }



//     __  __       _        _   _
//    |  \/  |     | |      | | (_)
//    | \  / |_   _| |_ __ _| |_ _  ___  _ __
//    | |\/| | | | | __/ _` | __| |/ _ \| '_ \
//    | |  | | |_| | || (_| | |_| | (_) | | | |
//    |_|  |_|\__,_|\__\__,_|\__|_|\___/|_| |_|


    public ArrayList<Integer> mutationOperator(ArrayList<Integer> elements){

        return this.flipOneBitMutation(elements);
    }

    // Constant probability distributed among all bits that the bit gets flipped
    private ArrayList<Integer> flipOneBitMutation(ArrayList<Integer> elements){
        int    num_elements = elements.size();
        double probability  = 1.0 / num_elements;

        for(int x = 0; x < elements.size(); x++){
            if(Decision.getProbabilityResult(probability)){
                if(elements.get(x) == 1){
                    elements.set(x, 0);
                }
                else{
                    elements.set(x, 1);
                }
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

    /*
        ----- NOTES -----
        - Not currently functional with only one dependency


     */


    /*
        Return: HashMap<String, ArrayList<HashMap<Integer, JsonArray>>>

        HashMap<
            String,
            ArrayList<
                HashMap<
                    Integer,
                    JsonArray
                >
            >
        >

        1. HashMap<String, ...>              - splits the TO and FROM dependencies
        2. ArrayList< ...parent... >         - array list of TO or FROM parents
        3. HashMap<Integer, ...decision... > - enumerated parent decisions
        4. JsonArray                         - one decision made by a parent


     */
    private HashMap<String, ArrayList<HashMap<Integer, JsonArray>>> getParentEnumerations(){
        ArrayList<HashMap<Integer, JsonArray>> parent_enumerations_to = new ArrayList<>();
        ArrayList<HashMap<Integer, JsonArray>> parent_enumerations_from = new ArrayList<>();
        HashMap<String, ArrayList<HashMap<Integer, JsonArray>>> parent_enumerations = new HashMap<>();
        parent_enumerations.put("TO", parent_enumerations_to);
        parent_enumerations.put("FROM", parent_enumerations_from);

        for(Decision parent: this.parents){
            String parent_type = this.getParentRelationshipAttribute(parent, "type");

            if(parent_type.equals("FROM")){
                parent_enumerations_from.add(parent.getEnumerations(this.node_name, this.node_type));
            }
            else if(parent_type.equals("TO")){
                parent_enumerations_to.add(parent.getEnumerations(this.node_name, this.node_type));
            }
        }

        return parent_enumerations;
    }



    @Override
    public void enumerateDesignSpace(){
        System.out.println("--> ENUMERATING ASSIGNING DECISION");


        // 1. Get parent enumeration info
        HashMap<String, ArrayList<HashMap<Integer, JsonArray>>> parent_enumerations = this.getParentEnumerations();


        ArrayList<HashMap<Integer, JsonArray>> parent_to_enum_list   = parent_enumerations.get("TO");
        ArrayList<HashMap<Integer, JsonArray>> parent_from_enum_list = parent_enumerations.get("FROM");

         /*
                For the purposes of the GN&C formulation, this enumeration functionality will written assuming
            one TO and one FROM parent dependency where each of the states of the objects are TRUE
         */
        HashMap<Integer, JsonArray> parent_to_enum   = parent_to_enum_list.get(0);
        HashMap<Integer, JsonArray> parent_from_enum = parent_from_enum_list.get(0);

//        System.out.println("----> TO ENUMERATIONS: " + parent_to_enum.keySet().size());
//        System.out.println("--> FROM ENUMERATIONS: " + parent_from_enum.keySet().size());

        // For each TO decision
        int to_counter = 0;
        for(Integer parent_to_key: parent_to_enum.keySet()){
            System.out.println("--> TO COUNTER: " + to_counter + " - " + this.enumeration_store.keySet().size());
            to_counter++;

            // TO decision
            JsonArray parent_to_decision = parent_to_enum.get(parent_to_key);


            // For each FROM decision
            for(Integer parent_from_key: parent_from_enum.keySet()){

                // FROM decision
                JsonArray parent_from_decision = parent_from_enum.get(parent_from_key);

                // For this specific combination of TO and FROM decision, enumerate all the possible assigning decisions
                this.enumerate_combination(parent_to_decision, parent_from_decision);
            }
        }

    }



    /*
        This function assumes all the elements of to_decision and from_decision are active
     */
    private void enumerate_combination(JsonArray to_decision, JsonArray from_decision){

        int assign_to_size   = to_decision.size();
        int assign_from_size = from_decision.size();
        int bit_str_length   = assign_to_size * assign_from_size;

        // Enumerate all possible assignations
        ArrayList<String> bit_strings = new ArrayList<>();
        int[] arr = new int[bit_str_length];
        this.generateAllBinaryStrings(bit_str_length, arr, 0, bit_strings);

        // Prune invalid enumerations
        ArrayList<String> valid_enumerations = this.prune_invalid_chromosomes(bit_strings);

        int enum_counter = this.enumeration_store.keySet().size();
        for(String enumeration: valid_enumerations){

            ArrayList<Integer> bit_array = Decision.bitStringToArray(enumeration);
            JsonArray new_elements = this.applyChromosome(bit_array, to_decision, from_decision);

            this.enumeration_store.put(enum_counter, new_elements);
            enum_counter++;
        }
    }













    @Override
    public ArrayList<JsonArray> enumerateDecision(JsonArray elements){

        return new ArrayList<>();
    }







//     _    _ _   _ _ _ _
//    | |  | | | (_) (_) |
//    | |  | | |_ _| |_| |_ _   _
//    | |  | | __| | | | __| | | |
//    | |__| | |_| | | | |_| |_| |
//     \____/ \__|_|_|_|\__|\__, |
//                           __/ |
//                          |___/


    private ArrayList<String> prune_invalid_chromosomes(ArrayList<String> enumerations){
        ArrayList<String> pruned_list = new ArrayList<>();

        for(String chromosome: enumerations){
            if(chromosome.indexOf('1') != -1){
                pruned_list.add(chromosome);
            }
        }
        return pruned_list;
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


    private void writeChildDeps(JsonArray child_to, JsonArray child_from, String f_name){
        String full_file_path = this.debug_dir + f_name;
        JsonArray to_write = new JsonArray();

        JsonObject to   = new JsonObject();
        to.add("assign to", child_to);

        JsonObject from = new JsonObject();
        from.add("assign from", child_from);

        to_write.add(from);
        to_write.add(to);

        Files.writeDebugFile(full_file_path, to_write);
    }

    private void writeMappings(ArrayList<ArrayList<String>> parent_mappings, ArrayList<ArrayList<String>> parent_pruned, String f_name){
        String full_file_path = this.debug_dir + f_name;

        JsonElement mappings = this.gson.toJsonTree(parent_mappings, new TypeToken<ArrayList<ArrayList<String>>>() {}.getType() );
        JsonElement pruned   = this.gson.toJsonTree(parent_pruned, new TypeToken<ArrayList<ArrayList<String>>>() {}.getType() );

        JsonArray to_write = new JsonArray();
        to_write.add(mappings);
        to_write.add(pruned);

        Files.writeDebugFile(full_file_path, to_write);
    }

    private void writeMapping(ArrayList<ArrayList<String>> combined, String f_name){
        String full_file_path = this.debug_dir + f_name;

        JsonElement mappings = this.gson.toJsonTree(combined, new TypeToken<ArrayList<ArrayList<String>>>() {}.getType() );

        JsonArray to_write = new JsonArray();
        to_write.add(mappings);

        Files.writeDebugFile(full_file_path, to_write);
    }

    private void writeChromosome(ArrayList<Integer> chromosome, String f_name){
        String full_file_path = this.debug_dir + f_name;

        JsonElement mappings = this.gson.toJsonTree(chromosome, new TypeToken<ArrayList<Integer>>() {}.getType() );

        JsonArray to_write = new JsonArray();
        to_write.add(mappings);

        Files.writeDebugFile(full_file_path, to_write);
    }

    private void writeFinalCrossover(JsonArray decision, String chromosome, String f_name){
        String full_file_path = this.debug_dir + f_name;

        JsonArray to_write = new JsonArray();

        JsonObject obj = new JsonObject();
        obj.addProperty("Chromosome", chromosome);
        obj.add("Design", decision);

        to_write.add(obj);

        Files.writeDebugFile(full_file_path, to_write);
    }



    @Override
    public String getDesignString(int idx){
        JsonObject design   = this.decisions.get(idx).getAsJsonObject();
        JsonArray  elements = design.getAsJsonArray("elements");
        return this.gson.toJson(elements);
    }







}
