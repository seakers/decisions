package graph.decision;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graph.Decision;
import graph.neo4j.DatabaseClient;
import org.neo4j.driver.Record;

import java.util.*;


public class Permuting extends Decision {

//     __      __        _       _     _
//     \ \    / /       (_)     | |   | |
//      \ \  / /_ _ _ __ _  __ _| |__ | | ___  ___
//       \ \/ / _` | '__| |/ _` | '_ \| |/ _ \/ __|
//        \  / (_| | |  | | (_| | |_) | |  __/\__ \
//         \/ \__,_|_|  |_|\__,_|_.__/|_|\___||___/



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

        public Permuting build() { return new Permuting(this); }
    }

    protected Permuting(Builder builder){
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
            JsonObject dependency = parent.getLastDecision(this.node_name, this.node_type, 0);
            JsonArray  dependency_elements = dependency.get("elements").getAsJsonArray();
            Iterator dependency_iterator = dependency_elements.iterator();
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
        ArrayList<Integer> active_indicies     = this.getActiveIndicies(parent_dependencies);

        if(active_indicies.isEmpty()){
            throw new Exception("DownSelecting - generateRandomDesign - dependencies are empty " + this.gson.toJson(parent_dependencies));
        }

        JsonArray new_design_elements = parent_dependencies.deepCopy();

        ArrayList<Integer> indicies_before = (ArrayList<Integer>) active_indicies.clone();
        Collections.shuffle(active_indicies);
        ArrayList<Integer> indicies_after  = (ArrayList<Integer>) active_indicies.clone();
        System.out.println("--> active indicies before " + indicies_before.toString());
        System.out.println("--> active indicies after " + indicies_after.toString());


        for(int x=0;x<indicies_before.size();x++){
            new_design_elements.set(indicies_before.get(x), parent_dependencies.get(indicies_after.get(x)));
        }
        this.indexNewDesign(parent_dependencies, new_design_elements);
        this.updateNodeDecisions();
    }

    @Override
    public void generateRandomDesign(JsonArray dependency) throws Exception{
        JsonArray          parent_dependencies = dependency.deepCopy();
        ArrayList<Integer> active_indicies     = this.getActiveIndicies(parent_dependencies);

        if(active_indicies.isEmpty()){
            throw new Exception("DownSelecting - generateRandomDesign - dependencies are empty " + this.gson.toJson(parent_dependencies));
        }

        JsonArray new_design_elements = parent_dependencies.deepCopy();

        ArrayList<Integer> indicies_before = (ArrayList<Integer>) active_indicies.clone();
        Collections.shuffle(active_indicies);
        ArrayList<Integer> indicies_after  = (ArrayList<Integer>) active_indicies.clone();
        System.out.println("--> active indicies before " + indicies_before.toString());
        System.out.println("--> active indicies after " + indicies_after.toString());


        for(int x=0;x<indicies_before.size();x++){
            new_design_elements.set(indicies_before.get(x), parent_dependencies.get(indicies_after.get(x)));
        }
        this.indexNewDesign(parent_dependencies, new_design_elements);
        this.updateNodeDecisions();
    }



//      _____
//     / ____|
//    | |     _ __ ___  ___ ___  _____   _____ _ __
//    | |    | '__/ _ \/ __/ __|/ _ \ \ / / _ \ '__|
//    | |____| | | (_) \__ \__ \ (_) \ V /  __/ |
//     \_____|_|  \___/|___/___/\___/ \_/ \___|_|


    @Override
    public void crossoverDesigns(int papa, int mama, double mutation_probability, JsonArray dependency) throws Exception{
        System.out.println("-----> PERMUTING CROSSOVER: " + papa + " " + mama);
        Random rand = new Random();

        // PAPA
        JsonObject papa_obj      = ((JsonElement) this.decisions.get(papa)).getAsJsonObject();
        JsonArray  papa_elements = papa_obj.get("elements").getAsJsonArray();
        JsonArray  papa_deps     = papa_obj.get("dependencies").getAsJsonArray();
        int        papa_size     = papa_elements.size();

        // MAMA
        JsonObject mama_obj      = ((JsonElement) this.decisions.get(mama)).getAsJsonObject();
        JsonArray  mama_elements = mama_obj.get("elements").getAsJsonArray();
        JsonArray  mama_deps     = mama_obj.get("dependencies").getAsJsonArray();
        int        mama_size     = mama_elements.size();

        // DEPENDENCIES
        JsonArray parent_dependencies = dependency;
        ArrayList<Integer> short_papa = this.decisionToShortNotation(papa_obj);
        ArrayList<Integer> short_mama = this.decisionToShortNotation(mama_obj);

        System.out.println("---> PAPA ELEMENTS: " + this.gson.toJson(papa_elements));
        System.out.println("---> MAMA ELEMENTS: " + this.gson.toJson(mama_elements));

        System.out.println("----> SHORT PAPA: "+ short_papa);
        System.out.println("----> SHORT MAMA: "+ short_mama);

        // 1. Get first half of papa
        ArrayList<Integer> split_papa = this.splitPapa(short_papa);

        System.out.println("----> SPLIT PAPA: "+ split_papa);

        // 2. Integrate mama into split_papa
        ArrayList<Integer> child_order = this.integrateMama(split_papa, short_mama);

        // 3. Resolve dependencies
        JsonArray child = this.crossoverResolutionOperator(child_order, parent_dependencies);

        // 4. Mutation operator
        if(Decision.getProbabilityResult(mutation_probability)){
            child = this.mutationSwapsTwoElements(child);
        }

        // 4. Index child design
        this.indexNewDesign(parent_dependencies, child);

        // 5. Update node database
        this.updateNodeDecisions();

    }


    @Override
    public void crossoverDesigns(int papa, int mama, double mutation_probability) throws Exception{
        System.out.println("-----> PERMUTING CROSSOVER: " + papa + " " + mama);
        Random rand = new Random();

        // PAPA
        JsonObject papa_obj      = ((JsonElement) this.decisions.get(papa)).getAsJsonObject();
        JsonArray  papa_elements = papa_obj.get("elements").getAsJsonArray();
        JsonArray  papa_deps     = papa_obj.get("dependencies").getAsJsonArray();
        int        papa_size     = papa_elements.size();

        // MAMA
        JsonObject mama_obj      = ((JsonElement) this.decisions.get(mama)).getAsJsonObject();
        JsonArray  mama_elements = mama_obj.get("elements").getAsJsonArray();
        JsonArray  mama_deps     = mama_obj.get("dependencies").getAsJsonArray();
        int        mama_size     = mama_elements.size();

        // DEPENDENCIES
        JsonArray parent_dependencies = this.mergeLastParentDecisions(false);
        ArrayList<Integer> short_papa = this.decisionToShortNotation(papa_obj);
        ArrayList<Integer> short_mama = this.decisionToShortNotation(mama_obj);

        System.out.println("---> PAPA ELEMENTS: " + this.gson.toJson(papa_elements));
        System.out.println("---> MAMA ELEMENTS: " + this.gson.toJson(mama_elements));

        System.out.println("----> SHORT PAPA: "+ short_papa);
        System.out.println("----> SHORT MAMA: "+ short_mama);

        // 1. Get first half of papa
        ArrayList<Integer> split_papa = this.splitPapa(short_papa);

        System.out.println("----> SPLIT PAPA: "+ split_papa);

        // 2. Integrate mama into split_papa
        ArrayList<Integer> child_order = this.integrateMama(split_papa, short_mama);

        // 3. Resolve dependencies
        JsonArray child = this.crossoverResolutionOperator(child_order, parent_dependencies);

        // 4. Mutation operator
        if(Decision.getProbabilityResult(mutation_probability)){
            child = this.mutationSwapsTwoElements(child);
        }

        // 4. Index child design
        this.indexNewDesign(parent_dependencies, child);

        // 5. Update node database
        this.updateNodeDecisions();



    }

    // CHANGE TO NOT ASSUME CHILD DEPS ARE ACTIVE !!!
    private JsonArray crossoverResolutionOperator(ArrayList<Integer> child_order, JsonArray child_deps){
        System.out.println("\n\n\n---> PREMUTING CROSSOVER RESOLUTION");
        System.out.println("---> child_order: " + child_order);
        System.out.println("---> CROSSOVER ORDERING: " + child_order); // [0, 1]
        System.out.println("---> ITEMS FROM PARENT: " + this.gson.toJson(child_deps));

        JsonArray child = new JsonArray();

        // 1. Get repaired child order
        ArrayList<Integer> repaired_child_order = this.repairChildOrder(child_order, child_deps);

        // 2. Reindex repaired child order
        // ArrayList<Integer> repaired_child_order_indexed = this.reindexChildOrder(repaired_child_order);

        System.out.println("---> REPAIRED CROSSOVER ORDERING: " + repaired_child_order);
        // System.out.println("---> REPAIRED CROSSOVER ORDERING INDEXED: " + repaired_child_order_indexed);


        // 2. Shuffle child_deps based on child_order
        for(Integer idx: repaired_child_order){
            child.add(child_deps.get(idx).getAsJsonObject());
        }

        return child;
    }

    private ArrayList<Integer> repairChildOrder(ArrayList<Integer> child_order, JsonArray child_deps){ // [0, 1]
        child_order = this.repairIndexOrdering(child_order);

        ArrayList<Integer> repaired_order = new ArrayList<>();
        int num_deps = this.numActiveDeps(child_deps);

        if(num_deps < child_order.size()){
            // CASE: child_order has more elements than child_deps
            //     : resolution - remove elements from child_order >= child_deps.size()
            for(Integer idx: child_order){
                if(idx < num_deps){
                    repaired_order.add(idx);
                }
            }
        }
        else if(num_deps > child_order.size()){
            // CASE: child_order has less elements than child_deps
            //     : resolution - add extra elements to child_order end after shuffling

            // 1. Find out how many new items to add and what their indicies are
            ArrayList<Integer> temp = new ArrayList<>();
            for(int x = child_order.size(); x < num_deps; x++){
                temp.add(x);
            }
            Collections.shuffle(temp);
            for(Integer idx: temp){
                child_order.add(idx);
            }
            return child_order;
        }
        else{
            return child_order;
        }
        return repaired_order;
    }


    private ArrayList<Integer> repairIndexOrdering(ArrayList<Integer> order){
        ArrayList<Integer> repaired_ordering = new ArrayList<>();

        HashMap<Integer, Integer> index_to_value = new HashMap<>();
        for(int x = 0; x < order.size(); x++){
            index_to_value.put(x, order.get(x));
        }


        ArrayList<Integer> order_sorted = new ArrayList<>();
        for(Integer itm: order){
            order_sorted.add(itm);
        }
        Collections.sort(order_sorted);

        HashMap<Integer, Integer> value_to_new_value = new HashMap<>();
        for(int x = 0; x < order_sorted.size(); x++){
            value_to_new_value.put(order_sorted.get(x), x);
        }

        for(int x = 0; x < index_to_value.size(); x++){
            repaired_ordering.add(value_to_new_value.get(index_to_value.get(x)));
        }

        return repaired_ordering;
    }

    private ArrayList<Integer> reindexChildOrder(ArrayList<Integer> repaired_child_order){

        ArrayList<Integer> repaired_child_order_sorted = new ArrayList<>();
        for(Integer itm: repaired_child_order){
            repaired_child_order_sorted.add(itm);
        }
        Collections.sort(repaired_child_order_sorted);

        HashMap<Integer, Integer> element_id_to_new_id = new HashMap<>();
        int counter = 0;
        for(Integer itm: repaired_child_order_sorted){
            element_id_to_new_id.put(itm, counter);
        }

        ArrayList<Integer> reindexed_order = new ArrayList<>();
        for(Integer itm: repaired_child_order){
            reindexed_order.add(element_id_to_new_id.get(itm));
        }

        return reindexed_order;
    }




    private ArrayList<Integer> splitPapa(ArrayList<Integer> short_papa){
        if(short_papa.size() == 1){
            return short_papa;
        }
        ArrayList<Integer> split_papa = new ArrayList<>();
        for(int x = 0;x < (short_papa.size()/2); x++){
            split_papa.add(short_papa.get(x));
        }
        return split_papa;
    }

    private ArrayList<Integer> integrateMama(ArrayList<Integer> split_papa, ArrayList<Integer> short_mama){

        for(Integer mama_pos: short_mama){
            if(!split_papa.contains(mama_pos)){
                split_papa.add(mama_pos);
            }
        }
        return split_papa;
    }


//     __  __       _        _   _
//    |  \/  |     | |      | | (_)
//    | \  / |_   _| |_ __ _| |_ _  ___  _ __
//    | |\/| | | | | __/ _` | __| |/ _ \| '_ \
//    | |  | | |_| | || (_| | |_| | (_) | | | |
//    |_|  |_|\__,_|\__\__,_|\__|_|\___/|_| |_|


    // 1. Mutation operator that changes the position of two random elements from different subsets
    public JsonArray mutationSwapsTwoElements(JsonArray elements){
        Random rand = new Random();

        int num_elements = elements.size();
        if(num_elements == 1){
            return elements;
        }

        // Find random elements to swap
        int element_swap_1 = rand.nextInt(num_elements);
        int element_swap_2  = rand.nextInt(num_elements);
        while(element_swap_2 == element_swap_1){
            element_swap_2 = rand.nextInt(num_elements);
        }

        // Clone elements to be swapped
        JsonObject clone_1 = elements.get(element_swap_1).getAsJsonObject().deepCopy();
        JsonObject clone_2 = elements.get(element_swap_2).getAsJsonObject().deepCopy();

        // Swap items
        elements.set(element_swap_1, clone_2);
        elements.set(element_swap_2, clone_1);

        return elements;
    }


//     _____                  _
//    |  __ \                (_)
//    | |__) |___ _ __   __ _ _ _ __
//    |  _  // _ \ '_ \ / _` | | '__|
//    | | \ \  __/ |_) | (_| | | |
//    |_|  \_\___| .__/ \__,_|_|_|
//               | |
//               |_|






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

        System.out.println("-----> PERMUTING ENUMERATION");
        System.out.println(this.enumeration_store.size());

    }

    // Multiple Parents
    public void enumerateMultiDependency(ArrayList<HashMap<Integer, JsonArray>> parent_enumerations){


    }

    // Single Parent
    public void enumerateSingleDependency(HashMap<Integer, JsonArray> parent_enumerations){
        for(Integer key: parent_enumerations.keySet()){
            JsonArray elements = parent_enumerations.get(key);

            // 1. Determine the number of groups
            int num_groups = elements.size();

            ArrayList<ArrayList<ArrayList<Integer>>> all_archs = this.enumeratePermutations(num_groups);
            if(all_archs.isEmpty()){
                continue;
            }
            ArrayList<ArrayList<Integer>> architectures = all_archs.get(all_archs.size()-1);

            this.buildEnumerationStore(elements, architectures);
        }
    }

    private ArrayList<ArrayList<ArrayList<Integer>>> enumeratePermutations(int num_elements){
        ArrayList<ArrayList<ArrayList<Integer>>> architectures = new ArrayList<>();

        if(num_elements == 0){
            return architectures;
        }

        // This is the 0th element - all partition architectures for 0 elements (empty)
        architectures.add(new ArrayList<>());

        // This is the 1st element - all partition architectures for 1 element
        ArrayList<ArrayList<Integer>> one_element = new ArrayList<>();
        ArrayList<Integer> one_element_a1 = new ArrayList<>();
        one_element_a1.add(1);
        one_element.add(one_element_a1);
        architectures.add(one_element);

        if(num_elements == 1){
            return architectures;
        }

        // This is the 2nd element - all partition architectures for 2 elements
        ArrayList<ArrayList<Integer>> two_elements = new ArrayList<>();
        ArrayList<Integer> two_elements_a1 = new ArrayList<>();
        two_elements_a1.add(1);
        two_elements_a1.add(2);
        ArrayList<Integer> two_elements_a2 = new ArrayList<>();
        two_elements_a2.add(2);
        two_elements_a2.add(1);
        two_elements.add(two_elements_a1);
        two_elements.add(two_elements_a2);
        architectures.add(two_elements);

        for(int i = 3; i <= num_elements; i++){
            architectures.add(new ArrayList<ArrayList<Integer>>());

            int num_prev_archs = architectures.get(i-1).size();
            for(int a = 0; a < num_prev_archs; a++){

                ArrayList<Integer> arch = architectures.get(i-1).get(a);
                Integer mx = arch.size() + 1;

                for(int j = 1; j <= mx; j++){
                    ArrayList<Integer> copy_arch = new ArrayList(arch);
                    ArrayList<Integer> new_arch = new ArrayList<>();

                    if(j == 1){
                        new_arch.add(mx);
                        new_arch.addAll(copy_arch.subList(j-1, copy_arch.size()));
                    }
                    else if(j == mx){
                        new_arch.addAll(copy_arch.subList(0, j-1));
                        new_arch.add(mx);
                    }
                    else{
                        new_arch.addAll(copy_arch.subList(0, j-1));
                        new_arch.add(mx);
                        new_arch.addAll(copy_arch.subList(j-1, copy_arch.size()));
                    }
                    architectures.get(architectures.size()-1).add(new_arch);
                }
            }

        }

        return architectures;
    }

    private void buildEnumerationStore(JsonArray elements, ArrayList<ArrayList<Integer>> architectures){
        int enum_counter = this.enumeration_store.keySet().size();

        for(ArrayList<Integer> arch: architectures){
            JsonArray new_elements = new JsonArray();

            for(Integer idx: arch){
                JsonObject item = elements.get(idx-1).getAsJsonObject();
                new_elements.add(item);
            }

            this.enumeration_store.put(enum_counter, new_elements);
            enum_counter++;

        }
    }



    @Override
    public ArrayList<JsonArray> enumerateDecision(JsonArray elements){
        ArrayList<JsonArray> enumerations = new ArrayList<>();

        // 1. Determine the number of groups
        int num_groups = elements.size();

        // 2.
        ArrayList<ArrayList<ArrayList<Integer>>> all_archs = this.enumeratePermutations(num_groups);
        if(all_archs.isEmpty()){
            return enumerations;
        }
        ArrayList<ArrayList<Integer>> architectures = all_archs.get(all_archs.size()-1);

        // 3.
        for(ArrayList<Integer> arch: architectures){
            JsonArray new_elements = new JsonArray();

            for(Integer idx: arch){
                JsonObject item = elements.get(idx-1).getAsJsonObject();
                new_elements.add(item);
            }
            enumerations.add(new_elements);
        }

        return enumerations;
    }





//     _    _ _   _ _ _ _
//    | |  | | | (_) (_) |
//    | |  | | |_ _| |_| |_ _   _
//    | |  | | __| | | | __| | | |
//    | |__| | |_| | | | |_| |_| |
//     \____/ \__|_|_|_|\__|\__, |
//                           __/ |
//                          |___/

    private int numActiveDeps(JsonArray deps){
        int counter = 0;
        for(int x = 0; x < deps.size(); x++){
            JsonObject item = deps.get(x).getAsJsonObject();
            if(item.get("active").getAsBoolean()){
                counter++;
            }
        }
        return counter;
    }


    public ArrayList<Integer> decisionToShortNotation(JsonObject decision){
        JsonArray items   = decision.get("elements").getAsJsonArray();
        JsonArray deps    = decision.get("dependencies").getAsJsonArray();
        return this.decisionToShortNotation(items);
    }

    public ArrayList<Integer> decisionToShortNotation(JsonArray groups){
        Iterator  item_itr = groups.iterator();

        ArrayList<Integer> short_notation = new ArrayList<Integer>(groups.size());
        while(item_itr.hasNext()){
            JsonObject item = ((JsonElement) item_itr.next()).getAsJsonObject();
            int item_id = item.get("id").getAsInt();
            if(item.get("active").getAsBoolean()){
                short_notation.add(item_id);
            }
            else{
                short_notation.add(-1);
            }
        }
        return short_notation;
    }




}
