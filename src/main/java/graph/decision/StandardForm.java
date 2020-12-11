package graph.decision;

import app.App;
import app.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graph.Decision;
import graph.structure.Structure;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class StandardForm extends Decision {

//     __      __        _       _     _
//     \ \    / /       (_)     | |   | |
//      \ \  / /_ _ _ __ _  __ _| |__ | | ___  ___
//       \ \/ / _` | '__| |/ _` | '_ \| |/ _ \/ __|
//        \  / (_| | |  | | (_| | |_) | |  __/\__ \
//         \/ \__,_|_|  |_|\__,_|_.__/|_|\___||___/

    private String debug_dir = "/app/debug/standard_form/";


//     ____        _ _     _
//    |  _ \      (_) |   | |
//    | |_) |_   _ _| | __| | ___ _ __
//    |  _ <| | | | | |/ _` |/ _ \ '__|
//    | |_) | |_| | | | (_| |  __/ |
//    |____/ \__,_|_|_|\__,_|\___|_|



    public static class Builder extends Decision.Builder<StandardForm.Builder>{

        public Builder(Record node){
            super(node);
        }

        public StandardForm build() { return new StandardForm(this); }
    }

    protected StandardForm(StandardForm.Builder builder){
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
        JsonArray decision_components = new JsonArray();

        for(Decision parent: this.parents){

            JsonObject decision          = parent.getLastDecision(this.node_name, this.node_type, 0);
            JsonArray  decision_elements = decision.get("elements").getAsJsonArray().deepCopy();
            int        decision_depth    = this.getConstantDecisionDepth(decision);

            /*
                    If the design depth is 1, the design elements are represented by a 1D array. This array
                will be a single component of the standard form decision (one element from it will be selected).
             */
            if(decision_depth == 1){
                // decision_components.add(decision_elements);
                decision_components.add(Structure.pruneInactiveElements(decision_elements));
            }

            /*
                    If the design depth is > 1, then the design elements are represented by a 2D+ array. Each
                element of this 2D+ array will be treated as a single component of the standard form decision. So,
                if there are 5 1D arrays in the 2D array, the standard form decision will have 5 components.

                - note, standard form components are only added if their state is active
             */
            else if(decision_depth > 1){

                for(int x = 0; x < decision_elements.size(); x++){
                    JsonObject element = decision_elements.get(x).getAsJsonObject();
                    if(!Structure.isActive(element)){
                        continue;
                    }
                    JsonArray sub_elements = Structure.getElements(element, true);
                    decision_components.add(Structure.pruneInactiveElements(sub_elements));
                    // decision_components.add(Structure.getElements(element, true));
                }
            }
        }

        return decision_components;
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

        // List of standard form decision components
        JsonArray decision_components = this.mergeLastParentDecisions(false);
        this.writeJsonArray(decision_components.deepCopy(), "random/1-decision-components.json");

        // Final elements to be returned
        JsonArray final_elements = new JsonArray();

        // Solve each sf component and add to final design elements
        for(int x = 0; x < decision_components.size(); x++){
            JsonArray  sf_component          = decision_components.get(x).getAsJsonArray();
            JsonObject sf_component_decision = this.random_component_decision(sf_component);
            final_elements.add(sf_component_decision);

            // JsonArray sf_component_solved = this.random_SF_decision(sf_component);
            // Structure.addListElement(final_elements, "", true, sf_component_solved);
        }

        this.writeJsonArray(final_elements.deepCopy(), "random/2-solved-components.json");
        this.indexNewDesign(decision_components, final_elements);
        this.updateNodeDecisions();
    }

    private JsonObject random_component_decision(JsonArray decision_component){
        JsonObject component_result = new JsonObject();

        // 1. Determine the active indices of the decision component
        ArrayList<Integer> active_indices = Structure.getActiveIndices(decision_component);

        // 2. Pick a random index to keep active
        int rand_idx        = this.rand.nextInt(active_indices.size());
        int rand_active_idx = active_indices.get(rand_idx);

        return (decision_component.get(rand_active_idx).getAsJsonObject());
    }


//      _____
//     / ____|
//    | |     _ __ ___  ___ ___  _____   _____ _ __
//    | |    | '__/ _ \/ __/ __|/ _ \ \ / / _ \ '__|
//    | |____| | | (_) \__ \__ \ (_) \ V /  __/ |
//     \_____|_|  \___/|___/___/\___/ \_/ \___|_|


    /*
            A child decision starts off with a list of child decision components. For each child decision
        component, iterate over the parent decision components and determine which parent component is most
        like the child component. Once this is found, take the decision in the parent component and apply
        it to the child component. Remove this parent component so it can't be chosen again.
        Track how many component decisions are taken from each parent and balance how much is taken
        from each parent. If this iteration ends and undecided child components still exist, randomly make a
        decision on the undecided components.

            For the mutation operator, take your set of decided child components. Randomly select one component
        and make a new random decision for that component.
     */

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
        JsonArray  child_components = parent_dependencies.deepCopy();
        this.writeJsonArray(child_components, "crossover/child-components.json");

        // ----- PAPA -----
        JsonObject papa_obj      = ((JsonElement) this.decisions.get(papa)).getAsJsonObject();

        // --> papa_elements: a JsonArray of decision components products (JsonObject)
        JsonArray  papa_elements = papa_obj.get("elements").getAsJsonArray().deepCopy();

        // --> papa_deps: a JsonArray of papa's original decision components (JsonArray)
        JsonArray  papa_deps      = papa_obj.get("dependencies").getAsJsonArray().deepCopy();

        // --> papa_component_map: maps each component to the component product
        HashMap<JsonArray, JsonObject> papa_component_map = this.build_component_map(papa_deps, papa_elements);

        this.writeJsonArray(papa_elements, "crossover/papa-component-products.json");
        this.writeJsonArray(papa_deps, "crossover/papa-components.json");

        // ----- MAMA -----
        JsonObject mama_obj      = ((JsonElement) this.decisions.get(mama)).getAsJsonObject();
        JsonArray  mama_elements = mama_obj.get("elements").getAsJsonArray().deepCopy();
        JsonArray  mama_deps      = mama_obj.get("dependencies").getAsJsonArray().deepCopy();
        HashMap<JsonArray, JsonObject> mama_component_map = this.build_component_map(mama_deps, mama_elements);

        this.writeJsonArray(mama_elements, "crossover/mama-component-products.json");
        this.writeJsonArray(mama_deps, "crossover/mama-components.json");


        // ----- CHILD COMPONENT DECISIONS -----
        JsonArray child_component_products = this.integration_operator(child_components, papa_component_map, mama_component_map);
        this.writeJsonArray(child_component_products, "crossover/child-component-products.json");


        // ----- MUTATION OPERATOR -----
        if(Decision.getProbabilityResult(mutation_probability)){
            child_component_products = this.mutation_operator(child_components, child_component_products);
        }
        this.writeJsonArray(child_component_products, "crossover/child-component-products-mutation.json");

        this.indexNewDesign(child_components, child_component_products);
    }

    private JsonArray integration_operator(JsonArray child_components, HashMap<JsonArray, JsonObject> papa_component_map, HashMap<JsonArray, JsonObject> mama_component_map){
        JsonArray child_component_products = new JsonArray();

        int papa_integrations = 0;
        int mama_integrations = 0;

        // ----- FIND CHILD COMPONENT PRODUCTS -----
        for(int x = 0; x < child_components.size(); x++){
            JsonArray child_component = child_components.get(x).getAsJsonArray();

            HashMap<Integer, JsonArray> papa_component_product_similarity = this.component_similarity_score(child_component, papa_component_map);
            HashMap<Integer, JsonArray> mama_component_product_similarity = this.component_similarity_score(child_component, mama_component_map);

            // If no parent components have any similarities
            if(papa_component_product_similarity.isEmpty() && mama_component_product_similarity.isEmpty()){
                child_component_products.add(this.random_component_decision(child_component));
            }
            // If only mama components have similarities
            else if(papa_component_product_similarity.isEmpty()){
                JsonArray  mama_component = (JsonArray) mama_component_product_similarity.values().toArray()[0];
                JsonObject mama_component_product = mama_component_map.get(mama_component);
                child_component_products.add(mama_component_product.deepCopy());
                mama_component_map.remove(mama_component);
                mama_integrations++;
            }
            // If only papa components have similarities
            else if(mama_component_product_similarity.isEmpty()){
                JsonArray  papa_component = (JsonArray) papa_component_product_similarity.values().toArray()[0];
                JsonObject papa_component_product = papa_component_map.get(papa_component);
                child_component_products.add(papa_component_product.deepCopy());
                papa_component_map.remove(papa_component);
                papa_integrations++;
            }
            // If both parents components have similarities
            else{
                int papa_similarity = (int) papa_component_product_similarity.keySet().toArray()[0];
                int mama_similarity = (int) mama_component_product_similarity.keySet().toArray()[0];

                int integration_diff = Math.abs(papa_integrations - mama_integrations);
                int similarity_diff  = Math.abs(papa_similarity - mama_similarity);

                /*
                        If the difference in similarity is greater than the difference in integrations, choose
                    which to integrate based on similarity
                 */
                if(similarity_diff > integration_diff){
                    if(papa_similarity > mama_similarity){
                        JsonArray  papa_component = (JsonArray) papa_component_product_similarity.values().toArray()[0];
                        JsonObject papa_component_product = papa_component_map.get(papa_component);
                        child_component_products.add(papa_component_product.deepCopy());
                        papa_component_map.remove(papa_component);
                        papa_integrations++;
                    }
                    else{
                        JsonArray  mama_component = (JsonArray) mama_component_product_similarity.values().toArray()[0];
                        JsonObject mama_component_product = mama_component_map.get(mama_component);
                        child_component_products.add(mama_component_product.deepCopy());
                        mama_component_map.remove(mama_component);
                        mama_integrations++;
                    }
                }
                /*
                        If the difference in integrations is greater than the difference in similarities, choose
                    which to integrate based on integrations
                 */
                else{
                    if(papa_integrations < mama_integrations){
                        JsonArray  papa_component = (JsonArray) papa_component_product_similarity.values().toArray()[0];
                        JsonObject papa_component_product = papa_component_map.get(papa_component);
                        child_component_products.add(papa_component_product.deepCopy());
                        papa_component_map.remove(papa_component);
                        papa_integrations++;
                    }
                    else{
                        JsonArray  mama_component = (JsonArray) mama_component_product_similarity.values().toArray()[0];
                        JsonObject mama_component_product = mama_component_map.get(mama_component);
                        child_component_products.add(mama_component_product.deepCopy());
                        mama_component_map.remove(mama_component);
                        mama_integrations++;
                    }
                }
            }
        }
        return child_component_products;
    }

    /*
            This function finds the most similar parent component and returns the parent component along with
        that parent component's similarity score.
     */
    private HashMap<Integer, JsonArray> component_similarity_score(JsonArray child_component, HashMap<JsonArray, JsonObject> parent_component_map){
        HashMap<Integer, JsonArray> component_product_similarity = new HashMap<>();
        int max_similarity_score = 0;

        for(JsonArray parent_component: parent_component_map.keySet()){
            JsonObject parent_component_product = parent_component_map.get(parent_component);

            /*
                    We validate that the child component contains the parent component product. If it
                doesn't, the child component product can't be the parent component product. Thus, skip.
             */
            if(!child_component.contains(parent_component_product)){
                continue;
            }

            /*
                    Calculate similarity score between parent and child
             */
            int similarity_score = this.calculate_similarity_score(child_component, parent_component);
            if(similarity_score > max_similarity_score){
                max_similarity_score = similarity_score;
                component_product_similarity.clear();
                component_product_similarity.put(similarity_score, parent_component);
            }
        }

        return component_product_similarity;
    }


    private int calculate_similarity_score(JsonArray child_component, JsonArray parent_component){
        int similarity_score = 0;

        for(int x = 0; x < child_component.size(); x++){
            JsonObject child_component_element = child_component.get(x).getAsJsonObject();
            if(parent_component.contains(child_component_element)){
                similarity_score++;
            }
        }

        return similarity_score;
    }


    private HashMap<JsonArray, JsonObject> build_component_map(JsonArray parent_components, JsonArray parent_component_products){
        if(parent_components.size() != parent_component_products.size()){
            System.out.println("--> PARENT COMPONENET / COMPONENT PRODUCTS NOT THE SAME LENGTH ");
            System.out.println(this.gson.toJson(parent_components));
            System.out.println(this.gson.toJson(parent_component_products));
            System.exit(0);
        }

        HashMap<JsonArray, JsonObject> component_map = new HashMap<>();
        for(int x = 0; x < parent_components.size(); x++){
            JsonArray  parent_component = parent_components.get(x).getAsJsonArray();
            JsonObject parent_component_product = parent_component_products.get(x).getAsJsonObject();
            component_map.put(parent_component, parent_component_product);
        }

        return component_map;
    }



//     __  __       _        _   _
//    |  \/  |     | |      | | (_)
//    | \  / |_   _| |_ __ _| |_ _  ___  _ __
//    | |\/| | | | | __/ _` | __| |/ _ \| '_ \
//    | |  | | |_| | || (_| | |_| | (_) | | | |
//    |_|  |_|\__,_|\__\__,_|\__|_|\___/|_| |_|

    /*
        Returns a mutated set of design component products
     */
    private JsonArray mutation_operator(JsonArray design_components, JsonArray design_component_products){
        JsonArray design_component_products_copy = design_component_products.deepCopy();

        // Choose a random component to mutate
        int rand_component_idx = this.rand.nextInt(design_components.size());
        JsonArray design_component = design_components.get(rand_component_idx).getAsJsonArray();


        int rand_component_product_idx = this.rand.nextInt(design_component.size());
        JsonObject design_component_product = design_component.get(rand_component_product_idx).getAsJsonObject().deepCopy();

        design_component_products_copy.set(rand_component_idx, design_component_product);

        return design_component_products_copy;
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

        System.out.println("---> FINISHED ENUMERATING STANDARD FORM DECISION");
    }

    /*
        Each JsonArray contains multiple JsonObjects where each JsonObject is a list element
        contains all the sensors to be decided upon.
     */
    private void enumerateSingleDependency(HashMap<Integer, JsonArray> parent_enumerations){
        System.out.println("--> ENUMERATING SF DECISION - SINGLE DEPENDENCY");

        // Each JsonArray represents a possible decision taken by the parent
        for(Integer key: parent_enumerations.keySet()){

            // Parent decision
            JsonArray elements = parent_enumerations.get(key);

            /*
                    For this specific parent decision, we will enumerate all the possible standard form
                decisions that can be made on this parent decision.
             */

            // 1. Determine the number of decision components
            int num_components = elements.size();

            // 2. Determine the cardinality of each decision component
            // - this essentially encodes the structure of this SF decision, this structure will be used to enumerate all the possible SF decisions
            ArrayList<Integer> component_cardinalities = new ArrayList<>();
            for(int x = 0; x < num_components; x++){
                JsonObject list_element = elements.get(x).getAsJsonObject();
                component_cardinalities.add(list_element.getAsJsonArray("elements").size());
            }

            // 3. Generate all possible enumerations
            ArrayList<ArrayList<String>> enumerations = this.generateAllBinarySF_Strings(component_cardinalities);

            // 4. Build enumeration store
            this.buildEnumerationStore(elements, enumerations);

            System.out.println("--> STANDARD FORM ENUMERATIONS: " + this.enumeration_store.size());
        }
    }

    private void enumerateMultiDependency(ArrayList<HashMap<Integer, JsonArray>> parent_enumerations){

    }



    /*
        Convert all possible SF decisions (designs) into JsonObject design representation and index into enumeration_store
     */
    private void buildEnumerationStore(JsonArray elements, ArrayList<ArrayList<String>> designs){
        int enum_counter = this.enumeration_store.keySet().size();

        for(ArrayList<String> design: designs){
            JsonArray new_elements = new JsonArray();

            /*  - Augment new_elements to our bit representation
                - The size of new_elements should be the same as the size of design
                - Iterate over each sf component and make the decision
             */
            for(int x = 0; x < elements.size(); x++){
                JsonObject list_element         = elements.get(x).getAsJsonObject();
                JsonArray  list_element_objects = list_element.getAsJsonArray("elements").deepCopy();
                String     bit_str              = design.get(x);
                int        sf_obj_idx           = bit_str.indexOf('1');

                new_elements.add(list_element_objects.get(sf_obj_idx).getAsJsonObject().deepCopy());
            }
            this.enumeration_store.put(enum_counter, new_elements);
            enum_counter++;
        }
    }




    /*
        Generate all bit strings for a single Standard Form decision
     */
    private ArrayList<ArrayList<String>> generateAllBinarySF_Strings(ArrayList<Integer> component_cardinalities){

        /*
            Each inner ArrayList<String> represents all the possible bit strings for that component
         */
        ArrayList<ArrayList<String>> component_bit_strings = new ArrayList<>();
        for(Integer component_cardinality: component_cardinalities){

            component_bit_strings.add(this.generateAllSF_ComponentStrings(component_cardinality));
        }

        ArrayList<ArrayList<String>> designs = new ArrayList<>();
        for(ArrayList<String> component_strings: component_bit_strings){
            if(designs.isEmpty()){
                for(String component_string: component_strings){
                    ArrayList<String> new_design = new ArrayList<>();
                    new_design.add(component_string);
                    designs.add(new_design);
                }
                continue;
            }

            ArrayList<ArrayList<String>> new_designs = new ArrayList<>();
            for(ArrayList<String> design: designs){
                /*
                    Duplicate each design and add the next decided decision component string
                 */
                for(String component_string: component_strings){
                    ArrayList<String> new_design = new ArrayList<>(design);
                    new_design.add(component_string);
                    new_designs.add(new_design);
                }
            }
            designs = new_designs;
        }

        return designs;
    }


    private void recur_through_components(ArrayList<ArrayList<String>> component_strings, int level){
       if(level == component_strings.size()){

       }
       else{

       }
    }




    private ArrayList<String> generateAllSF_ComponentStrings(Integer component_cardinality){
        ArrayList<String> strings = new ArrayList<>();

        for(int x = 0; x < component_cardinality; x++){
            StringBuilder str_build = new StringBuilder("");

            for(int y = 0; y < component_cardinality; y++){
                if(x == y){
                    str_build.append('1');
                }
                else{
                    str_build.append('0');
                }
            }
            strings.add(str_build.toString());
        }

        return strings;
    }




















//     _    _ _   _ _ _ _
//    | |  | | | (_) (_) |
//    | |  | | |_ _| |_| |_ _   _
//    | |  | | __| | | | __| | | |
//    | |__| | |_| | | | |_| |_| |
//     \____/ \__|_|_|_|\__|\__, |
//                           __/ |
//                          |___/


    private void writeJsonArray(JsonArray deps, String f_name){
        String full_file_path = this.debug_dir + f_name;
        Files.writeDebugFile(full_file_path, deps);
    }









}
