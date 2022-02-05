package graph.decision;

import app.App;
import app.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import graph.Decision;
import graph.neo4j.DatabaseClient;
import org.neo4j.driver.Record;

import java.util.*;

public class Partitioning extends Decision {

// __      __        _       _     _
// \ \    / /       (_)     | |   | |
//  \ \  / /_ _ _ __ _  __ _| |__ | | ___  ___
//   \ \/ / _` | '__| |/ _` | '_ \| |/ _ \/ __|
//    \  / (_| | |  | | (_| | |_) | |  __/\__ \
//     \/ \__,_|_|  |_|\__,_|_.__/|_|\___||___/

    private String debug_dir = "/app/debug/partitioning/";



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

        public Partitioning build() { return new Partitioning(this); }
    }

    protected Partitioning(Builder builder){
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
        JsonArray parent_dependencies = this.mergeLastParentDecisions(false);

        this.randomDesign(parent_dependencies);
    }

    @Override
    public void generateRandomDesign(JsonArray dependency) throws Exception{

        this.randomDesign(dependency);
    }

    public void randomDesign(JsonArray dependency) throws Exception{
        JsonArray          parent_dependencies = dependency.deepCopy();
        ArrayList<Integer> active_indicies     = this.getActiveIndicies(parent_dependencies);

        if(active_indicies.isEmpty()){
            throw new Exception("Partitioning - generateRandomDesign - dependencies are empty " + this.gson.toJson(parent_dependencies));
        }

        // 1. Randomize number of groups
        JsonArray empty_groups = new JsonArray();
        Random    rand                = new Random();
        int       numGroups           = rand.nextInt(active_indicies.size())+1;

        // 1.1 Correct for group size
        // if(numGroups > active_indicies.size()){ numGroups = active_indicies.size();}

        // 1.2 Create empty groups
        for(int x=0;x<numGroups;x++){
            JsonObject element = new JsonObject();
            element.addProperty("active",true);
            element.addProperty("id", x);
            element.addProperty("type", "list");
            element.add("elements", new JsonArray());
            empty_groups.add(element);
        }


        // METHOD TWO -- START
        // 2. Randomly assign elements to empty groups
        for(Integer active_index: active_indicies){
            JsonObject random_group = empty_groups.get(rand.nextInt(empty_groups.size())).getAsJsonObject();
            random_group.getAsJsonArray("elements").add(parent_dependencies.get(active_index).getAsJsonObject());
        }

        // 3. Iterate over groups to see if any are empty
        for(int x = 0; x < empty_groups.size(); x++){
            JsonObject group = empty_groups.get(x).getAsJsonObject();

            // If a group is still empty, one of the groups must have multiple elements
            // Find a free element to add to the group
            if(group.getAsJsonArray("elements").size() == 0){
                JsonObject free_element = this.get_free_element(empty_groups);
                group.getAsJsonArray("elements").add(free_element);
            }
        }
        // METHOD TWO -- END

        // METHOD ONE -- START
        // 2. Randomly assign elements to empty groups
//        int counter = 0;
//        ArrayList<Integer> indicies_used = new ArrayList<>();
//        for(int x = 0; x < active_indicies.size(); x++){
//            int idx = rand.nextInt(active_indicies.size());
//            while(indicies_used.contains(idx)){
//                idx = rand.nextInt(active_indicies.size());
//            }
//            indicies_used.add(idx);
//
//            int group_idx = counter % numGroups;
//            if(counter >= numGroups){
//                group_idx = rand.nextInt(numGroups);
//            }
//
//            empty_groups.get(group_idx).getAsJsonObject().get("elements").getAsJsonArray().add(parent_dependencies.get(active_indicies.get(idx)).getAsJsonObject());
//            counter++;
//        }
        // METHOD ONE -- END


//        for(Integer idx: active_indicies){
//            int group_idx = counter % numGroups;
//            new_design_elements.get(group_idx).getAsJsonObject().get("elements").getAsJsonArray().add(parent_dependencies.get(idx).getAsJsonObject());
//            counter++;
//        }

        JsonArray repaired_groups = this.enforce_notation_constraint(empty_groups, parent_dependencies);

        this.indexNewDesign(parent_dependencies, repaired_groups);
        this.updateNodeDecisions();

    }


    public JsonArray enforce_notation_constraint(JsonArray entity_groups, JsonArray entities){
        JsonArray repaired_entity_groups = new JsonArray();

        ArrayList<Integer> short_notation_zeros = this.decisionToShortNotation(entity_groups, entities);
        // Prune 0's off of arraylist
        ArrayList<Integer> short_notation = new ArrayList<>();
        for(Integer pos: short_notation_zeros){
            if(pos != 0){
                short_notation.add(pos);
            }
        }

        ArrayList<Integer> repaired_notation = this.repairOperator(short_notation);


        try{
            ArrayList<Integer> groups_handled = new ArrayList<>();
            for(int x = 0; x < short_notation.size(); x++){
                int current_group_pos = short_notation.get(x) - 1;
                if(!groups_handled.contains(current_group_pos)){
                    repaired_entity_groups.add(entity_groups.get(current_group_pos).getAsJsonObject().deepCopy());
                    groups_handled.add(current_group_pos);
                }
            }
        }
        catch (IndexOutOfBoundsException e){
            System.out.println(this.gson.toJson(entity_groups));
            System.out.println(short_notation);
            e.printStackTrace();
            System.exit(0);

        }


//        ArrayList<Integer> repair_check = this.decisionToShortNotation(repaired_entity_groups, entities);
//        System.out.println("--> ORIGINAL: " + short_notation);
//        System.out.println("---> DESIRED: " + repaired_notation);
//        System.out.println("-------> NEW: " + repair_check);
//
//        System.out.println(this.gson.toJson(entity_groups));
//        System.out.println("------");
//        System.out.println(this.gson.toJson(repaired_entity_groups));

        return repaired_entity_groups;
    }


    public JsonObject get_free_element(JsonArray groups){
        boolean found = false;
        JsonObject free_element = new JsonObject();
        while(!found){
            // Get a random group
            JsonObject group = groups.get(this.rand.nextInt(groups.size())).getAsJsonObject();
            if(group.getAsJsonArray("elements").size() > 1){
                int rand_idx = this.rand.nextInt(group.getAsJsonArray("elements").size());
                free_element = group.getAsJsonArray("elements").get(rand_idx).getAsJsonObject().deepCopy();
                group.getAsJsonArray("elements").remove(rand_idx);
                found = true;
            }
        }
        return free_element;
    }




//     _____                  _
//    |  __ \                (_)
//    | |__) |___ _ __   __ _ _ _ __
//    |  _  // _ \ '_ \ / _` | | '__|
//    | | \ \  __/ |_) | (_| | | |
//    |_|  \_\___| .__/ \__,_|_|_|
//               | |
//               |_|

    // Projects non-sensical architecture representation into the set of sensical architecture representations
    // Integer grouping representation
    public ArrayList<Integer> repairOperator(ArrayList<Integer> partition){
        HashMap<Integer, Integer> pivs            = new HashMap<>();
        ArrayList<Integer>        fixed_partition = new ArrayList<>();

        // Relabel subset indices in increasing order starting from 1
        for(int i=0;i<partition.size();i++){
            int piv = partition.get(i);
            if(pivs.containsKey(piv)){
                fixed_partition.add(i, pivs.get(piv));
            }
            else {
                int n = pivs.size();
                pivs.put(piv, (n+1));
                fixed_partition.add(i, n + 1);
            }
        }

        // Remove empty items ???
        for(int i=1;i<fixed_partition.size();i++){
            int value = (this.aryMax(fixed_partition, i-1) + 1);
            if(fixed_partition.get(i) > value){
                fixed_partition.add(i, value);
            }
        }

        return fixed_partition;
    }

    private JsonArray repairGroupIDs(JsonArray groups){
        JsonArray repaired = new JsonArray();

        for(int x = 0; x < groups.size(); x++){
            JsonObject group = groups.get(x).getAsJsonObject();
            group.addProperty("id", x);
            repaired.add(group);
        }

        return repaired;
    }

    private JsonArray repairEmptyGroups(JsonArray groups){
        ArrayList<Boolean> groups_to_delete = new ArrayList<>();

        if(groups.size() == 0){
            return groups;
        }

        for(int x = (groups.size()-1); x >= 0; x--){
            JsonObject group       = groups.get(x).getAsJsonObject();
            JsonArray  group_items = group.getAsJsonArray("elements");
            if(group_items.size() == 0){
                groups.remove(x);
            }
        }

        return groups;
    }

    private int aryMax(ArrayList<Integer> ary, int idx){
        int max = 0;
        for(int i=0;i<=idx;i++){
            int temp = ary.get(i);
            if(max < temp){
                max = temp;
            }
        }
        return max;
    }


//      _____
//     / ____|
//    | |     _ __ ___  ___ ___  _____   _____ _ __
//    | |    | '__/ _ \/ __/ __|/ _ \ \ / / _ \ '__|
//    | |____| | | (_) \__ \__ \ (_) \ V /  __/ |
//     \_____|_|  \___/|___/___/\___/ \_/ \___|_|


    @Override
    public void crossoverDesigns(int papa, int mama, double mutation_probability, JsonArray dependency) throws Exception{

        // CROSSOVER
        this.crossover(papa, mama, mutation_probability, dependency);
    }

    @Override
    public void crossoverDesigns(int papa, int mama, double mutation_probability) throws Exception{

        // DEPENDENCIES
        JsonArray parent_dependencies = this.mergeLastParentDecisions(false).deepCopy();

        // CROSSOVER
        this.crossover(papa, mama, mutation_probability, parent_dependencies);
    }



    public void crossover2(int papa, int mama, double mutation_probability, JsonArray parent_dependencies) throws Exception{

    }


    public void crossover(int papa, int mama, double mutation_probability, JsonArray parent_dependencies) throws Exception{


        System.out.println("----> PARTONING CROSSOVER");

        // PAPA
        JsonObject papa_obj      = ((JsonElement) this.decisions.get(papa)).getAsJsonObject().deepCopy();
        JsonArray  papa_elements = papa_obj.get("elements").getAsJsonArray();
        JsonArray  papa_deps     = papa_obj.get("dependencies").getAsJsonArray();
        Random     rand          = new Random();

        // MAMA
        JsonObject mama_obj      = ((JsonElement) this.decisions.get(mama)).getAsJsonObject().deepCopy();
        JsonArray  mama_elements = mama_obj.get("elements").getAsJsonArray();
        JsonArray  mama_deps     = mama_obj.get("dependencies").getAsJsonArray();

        // SHORTS
        ArrayList<Integer> short_papa = this.decisionToShortNotation(papa_obj);
        ArrayList<Integer> short_mama = this.decisionToShortNotation(mama_obj);

//        this.writeParentInfo(papa_elements, short_papa, "papa_info.json");
//        this.writeParentInfo(mama_elements, short_mama, "mama_info.json");


        // --> METHOD: split chromosomes based on greatest element crossover

        // 1. Get the first half of papa
        int       papa_half       = short_papa.size() / 2;
        JsonArray first_half_papa = this.PACK_arch2sats(papa_deps, short_papa, 0, papa_half).deepCopy();
        System.out.println("---> first_half_papa " + this.gson.toJson(first_half_papa));

        // 2. Get the second half of mama
        int       mama_half        = short_mama.size() / 2;
        JsonArray second_half_mama = this.PACK_arch2sats(mama_deps, short_mama, mama_half, short_mama.size()).deepCopy();
        System.out.println("---> second_half_mama " + this.gson.toJson(second_half_mama));


//        this.writeParentHalfs(first_half_papa, second_half_mama, parent_dependencies, "parent_halfs.json");



        // 3. Determine which groups from second_half_mama need to be merged with first_half_papa then merge
        JsonArray child = this.crossoverParentGroups(first_half_papa, second_half_mama, short_mama, mama_half).deepCopy();
//        System.exit(0);


        // 4. Repair child group ids
        child = this.repairGroupIDs(child);

        // 5. Call resolution operator for child dependencies
        child = this.crossoverResolutionOperator(child, parent_dependencies);

        // 6. Repair empty groups
        child = this.repairEmptyGroups(child);

        // 7. Mutation operator
        if(Decision.getProbabilityResult(mutation_probability)){
            child = this.applyRandomMutationOperator(child);
        }

        // 7.1 Enforce notation constraint - NEW
        JsonArray child_enforce = this.enforce_notation_constraint(child, parent_dependencies);

        // 7.2 Index child design
        this.indexNewDesign(parent_dependencies, child_enforce);

        // 8. Update node database
        this.updateNodeDecisions();
    }

    private JsonArray crossoverResolutionOperator(JsonArray child, JsonArray child_deps){
        ArrayList<Integer> child_deps_ary = Decision.elementsToBitString(child_deps);
        int add_resolutions = 0;
        int rmv_resolutions = 0;

        for(int x = 0; x < child_deps_ary.size(); x++){
            JsonObject resolution_obj = child_deps.get(x).getAsJsonObject();
            if(child_deps_ary.get(x) == 1){
                child = this.crossoverResolutionAdd(child, resolution_obj);
                add_resolutions++;
            }
            else{
                if(child.size() > 0){
                    child = this.crossoverResolutionRemove(child, resolution_obj);
                    rmv_resolutions++;
                }
            }
        }

//        System.out.println("---> add resolutions: " + add_resolutions);
//        System.out.println("---> rmv resolutions: " + rmv_resolutions);
        return child;
    }

    private JsonArray crossoverResolutionAdd(JsonArray child, JsonObject resolution_obj){

        // 1. If the child design has no groups, create new group for object
        if(child.size() == 0){
            JsonObject new_group_obj = new JsonObject();
            new_group_obj.addProperty("id", 0);
            new_group_obj.addProperty("type", "list");
            new_group_obj.addProperty("active", true);
            JsonArray new_group_elements = new JsonArray();
            new_group_elements.add(resolution_obj);
            new_group_obj.add("elements", new_group_elements);
            child.add(new_group_obj);
            return child;
        }

        // 2. If a child group contains this object, return child unmodified
        ArrayList<Integer> position = this.positionInGroup(child, resolution_obj);
        if(!position.isEmpty()){
            return child;
        }

        // 3. Else, add this object to a random group
        Random rand           = new Random();
        int    rand_group_idx = rand.nextInt(child.size());
        child.get(rand_group_idx).getAsJsonObject().getAsJsonArray("elements").add(resolution_obj);
        return child;
    }

    private JsonArray crossoverResolutionRemove(JsonArray child, JsonObject resolution_obj){
        // 1. If no child group contains this object, return child unmodified
        ArrayList<Integer> position = this.positionInGroup(child, resolution_obj);
        if(position.isEmpty()){
            return child;
        }

        // 2. Else, remove this object from the corresponding position
        child.get(position.get(0)).getAsJsonObject().getAsJsonArray("elements").remove(position.get(1));
        return child;
    }

    // RETURNS: 2D position of item inside group
    private ArrayList<Integer> positionInGroup(JsonArray child, JsonObject resolution_obj){

        ArrayList<Integer> ps = new ArrayList<>();
        int resolution_obj_id = resolution_obj.get("id").getAsInt();

        for(int x = 0; x < child.size(); x++){
            JsonObject child_group_obj = child.get(x).getAsJsonObject();
            JsonArray  child_group_ary = child_group_obj.getAsJsonArray("elements");
            for(int y = 0; y < child_group_ary.size(); y++){
                JsonObject child_group_item    = child_group_ary.get(y).getAsJsonObject();
                int        child_group_item_id = child_group_item.get("id").getAsInt();
                if(child_group_item_id == resolution_obj_id){
                    ps.add(x);
                    ps.add(y);
                    return ps;
                }
            }
        }
        return ps;
    }

    private JsonArray PACK_arch2sats(JsonArray deps, ArrayList<Integer> grouping, int first_idx, int last_idx){

        // Feasibility Check
        if(!this.containsElements(grouping, first_idx, last_idx)){
            return new JsonArray();
        }


        // Normal Operations
        JsonArray                 new_grouping = new JsonArray();
        HashMap<Integer, Integer> group_mapper = new HashMap<>();
        int num_new_groups = 0;

        for(int x=first_idx;x<last_idx;x++){
            int group_idx = grouping.get(x);

            // CONTINUE IF 0
            if(group_idx == 0){
                continue;
            }
            else{
                JsonObject element       = deps.get(x).getAsJsonObject();
                JsonObject group;
                int        new_group_idx = 0;


                // CHECK IF GROUP EXISTS
                if(group_mapper.containsKey(group_idx)){
                    new_group_idx = group_mapper.get(group_idx);
                    group = new_grouping.get(new_group_idx).getAsJsonObject();
                }
                else{
                    group_mapper.put(group_idx, num_new_groups);
                    new_group_idx = num_new_groups;
                    num_new_groups++;
                    group = new JsonObject();
                    group.addProperty("id", new_group_idx);
                    group.addProperty("type", "list");
                    group.addProperty("active", true);
                    JsonArray new_group_elements = new JsonArray();
                    group.add("elements", new_group_elements);
                    new_grouping.add(group);
                }

                JsonArray group_elements = group.getAsJsonArray("elements");
                group_elements.add(element);
            }
        }
        return new_grouping;
    }

    private JsonArray crossoverParentGroups(JsonArray first_half_papa, JsonArray second_half_mama, ArrayList<Integer> short_mama, int short_mama_start_idx){
        System.out.println("\n------- CROSSING OVER PARENTS -------");
        System.out.println("--- SHORT MAMA: " + short_mama);
        System.out.println("--- MAMA START: " + short_mama_start_idx);

        JsonArray child_chromosome = new JsonArray();

        // 1. Find largest group # in the first half of short_mama
        // int largest_group_mama_fh = Collections.max(short_mama);
        int largest_group_mama_fh = 0; // 2
        for(int x = 0; x < short_mama_start_idx; x++){
            int mama_group = short_mama.get(x);
            if(mama_group > largest_group_mama_fh){
                largest_group_mama_fh = mama_group;
            }
        }
        System.out.println("--- LARGEST GROUP FH: " + largest_group_mama_fh);

        // 2. See if any group #s in mama second half are <= largest group # in mama first half
        ArrayList<Integer> merge_mama_group = new ArrayList<>();
        for(int x = short_mama_start_idx; x < short_mama.size(); x++){
            int mama_group = short_mama.get(x);
            if(mama_group == 0){
                continue;
            }
            else if(mama_group <= largest_group_mama_fh){
                merge_mama_group.add(1);
            }
            else{
                merge_mama_group.add(0);
            }
        }
        System.out.println("--- MAMA MERGE GROUP: " + merge_mama_group);

        // 3. Determine which groups in second_half_mama need to be merged with random first_half_papa group
        int mama_item_counter = 0;
        ArrayList<Boolean> mama_group_merge_key = new ArrayList<>();
        for(int x = 0; x < second_half_mama.size(); x++){
            JsonObject mama_group       = second_half_mama.get(x).getAsJsonObject();
            JsonArray  mama_group_items = mama_group.getAsJsonArray("elements");
            boolean    needs_merging    = false;
            for(int y = 0; y < mama_group_items.size(); y++){
                if(merge_mama_group.get(mama_item_counter) == 1){
                    needs_merging = true;
                }
                mama_item_counter++;
            }
            mama_group_merge_key.add(needs_merging);
        }

        // 4. Iterate over the mama merge key list and merge if necessary
        JsonArray new_mama_groups = new JsonArray();
        for(int x = 0; x < mama_group_merge_key.size(); x++){
            JsonObject mama_group = second_half_mama.get(x).getAsJsonObject();

            // 4.1 Merge mama group with random papa group if it needs merging !!!
            if(mama_group_merge_key.get(x)){
                first_half_papa = this.randomMamaGroupMergeIntoPapa(first_half_papa, mama_group);
            }
            else{
                new_mama_groups.add(mama_group);
            }
        }

        return this.mergeChromosomes(first_half_papa, new_mama_groups);
    }

    private JsonArray randomMamaGroupMergeIntoPapa(JsonArray first_half_papa, JsonObject mama_group){
        // 1. CASE: first_half_papa is empty
        if(first_half_papa.size() == 0){
            mama_group.addProperty("id", 0);
            first_half_papa.add(mama_group);
            return first_half_papa;
        }

        // 2. CASE: first_half_papa is not empty
        Random rand = new Random();

        int papa_groups = first_half_papa.size();
        int merge_group = rand.nextInt(papa_groups);

        JsonArray mama_group_elements = mama_group.getAsJsonArray("elements");
        for(int x = 0; x < mama_group_elements.size(); x++){
            JsonObject mama_group_element = mama_group_elements.get(x).getAsJsonObject();
            first_half_papa.get(merge_group).getAsJsonObject().getAsJsonArray("elements").add(mama_group_element);
        }

        return first_half_papa;
    }

    private boolean containsElements(ArrayList<Integer> grouping, int start, int end){
        for(int x = start; x < end; x++){
            if(grouping.get(x) != 0){
                return true;
            }
        }
        return false;
    }

    private JsonArray mergeChromosomes(JsonArray half_papa, JsonArray half_mama){
        JsonArray merged = new JsonArray();

        // papa
        for(int x=0;x<half_papa.size();x++){
            merged.add(half_papa.get(x).getAsJsonObject());
        }

        //mama
        for(int x=0;x<half_mama.size();x++){
            merged.add(half_mama.get(x).getAsJsonObject());
        }

        return merged;
    }




//     __  __       _        _   _
//    |  \/  |     | |      | | (_)
//    | \  / |_   _| |_ __ _| |_ _  ___  _ __
//    | |\/| | | | | __/ _` | __| |/ _ \| '_ \
//    | |  | | |_| | || (_| | |_| | (_) | | | |
//    |_|  |_|\__,_|\__\__,_|\__|_|\___/|_| |_|

    // Randomly chooses one of the mutation operators below to apply
    public JsonArray applyRandomMutationOperator(JsonArray groups) {
        Random rand = new Random();
        int num_groups = groups.size();
        if(num_groups == 1){
            System.out.println("----> BREAK ONE BIG SUBSET");
//            return this.mutationBreakOneBigSubset_into_n(groups);
            return this.mutationBreakOneBigSubset(groups);
        }
        else if(num_groups > 1){
            double num = rand.nextDouble();
            if(num < 0.25){
                System.out.println("----> CHANGE ONE ELEMENT");
                return this.mutationChangeOneElement(groups);
            }
            else if(num < 0.5){
                System.out.println("----> SWAP TWO ELEMENTS");
                return this.mutationSwapsTwoElements(groups);
            }
            else if(num < 0.75){
                System.out.println("----> BREAK ONE BIG SUBSET");
//                return this.mutationBreakOneBigSubset_into_n(groups);
                return this.mutationBreakOneBigSubset(groups);
            }
            else{
                System.out.println("----> COMBINE TWO SMALL SUBSETS");
                return this.mutationCombineTwoSmallSubsets(groups);
            }
        }
        System.out.println("---> MUTATION GROUP HAS NO GROUPS!!!!");
        App.sleep(10);
        return groups;
    }

    // 1. Mutation operator that changes the position of a random element into a different random group: requires min 2 groups
    public JsonArray mutationChangeOneElement(JsonArray groups){
        Random rand = new Random();

        int num_groups = groups.size();

        int group_take_idx = rand.nextInt(num_groups);
        int group_put_idx  = rand.nextInt(num_groups);
        while(group_put_idx == group_take_idx){
            group_put_idx = rand.nextInt(num_groups);
        }

        int group_take_element_idx = rand.nextInt(groups.get(group_take_idx).getAsJsonObject().getAsJsonArray("elements").size());

        // Copy swap element
        JsonObject swap_element = groups.get(group_take_idx).getAsJsonObject().getAsJsonArray("elements").get(group_take_element_idx).getAsJsonObject().deepCopy();

        // Remove swap element from take group
        groups.get(group_take_idx).getAsJsonObject().getAsJsonArray("elements").remove(group_take_element_idx);

        // Insert swap element in put group
        groups.get(group_put_idx).getAsJsonObject().getAsJsonArray("elements").add(swap_element);

        groups = this.repairEmptyGroups(groups);

        return groups;
    }


    // 2. Mutation operator that changes the position of two random elements from different groups: requires min 2 groups
    public JsonArray mutationSwapsTwoElements(JsonArray groups){
        Random rand = new Random();

        int num_groups = groups.size();

        int group_swap_1 = rand.nextInt(num_groups);
        int group_swap_2  = rand.nextInt(num_groups);
        while(group_swap_2 == group_swap_1){
            group_swap_2 = rand.nextInt(num_groups);
        }

        int group_swap_1_element_idx = rand.nextInt(groups.get(group_swap_1).getAsJsonObject().getAsJsonArray("elements").size());
        int group_swap_2_element_idx = rand.nextInt(groups.get(group_swap_2).getAsJsonObject().getAsJsonArray("elements").size());

        // Copy swap elements
        JsonObject swap_element_1 = groups.get(group_swap_1).getAsJsonObject().getAsJsonArray("elements").get(group_swap_1_element_idx).getAsJsonObject().deepCopy();
        JsonObject swap_element_2 = groups.get(group_swap_2).getAsJsonObject().getAsJsonArray("elements").get(group_swap_2_element_idx).getAsJsonObject().deepCopy();

        // Remove swap elements from take groups
        groups.get(group_swap_1).getAsJsonObject().getAsJsonArray("elements").remove(group_swap_1_element_idx);
        groups.get(group_swap_2).getAsJsonObject().getAsJsonArray("elements").remove(group_swap_2_element_idx);

        // Insert swap elements in appropriate group
        groups.get(group_swap_1).getAsJsonObject().getAsJsonArray("elements").add(swap_element_2);
        groups.get(group_swap_2).getAsJsonObject().getAsJsonArray("elements").add(swap_element_1);

        return groups;
    }


    // 3. Mutation operator that breaks one random large group in a partition into two smaller groups: requires one group to have at least 2 elements
    public JsonArray mutationBreakOneBigSubset(JsonArray groups){
        Random rand = new Random();

        int num_groups = groups.size();

        // Find feasible groups to split
        ArrayList<Integer> feasible_group_idx = this.findFeasibleGroupsToSplit(groups);
        if(feasible_group_idx.isEmpty()){
            return groups;
        }



        // Choose a random feasible group to split
        int split_group_idx = feasible_group_idx.get(rand.nextInt(feasible_group_idx.size()));

        // Create new group
        JsonObject new_group = new JsonObject();
        new_group.addProperty("id", num_groups);
        new_group.addProperty("type", "list");
        new_group.addProperty("active", true);
        JsonArray new_group_elements = new JsonArray();
        new_group.add("elements", new_group_elements);

        // Split group
        int split_group_item_count = groups.get(split_group_idx).getAsJsonObject().getAsJsonArray("elements").size();
        for(int x = 0; x < split_group_item_count/2; x++){
            // Clone split group element
            JsonObject split_group_element_copy = groups.get(split_group_idx).getAsJsonObject().getAsJsonArray("elements").get(x).getAsJsonObject().deepCopy();
            // Remove split group element
            groups.get(split_group_idx).getAsJsonObject().getAsJsonArray("elements").remove(x);
            // Add split group element to new group
            new_group_elements.add(split_group_element_copy);
        }

        // Add new group
        groups.add(new_group);

        return groups;
    }

    // 3.1 Same as mutation operator 3, but breaks the group into n smaller groups
    public JsonArray mutationBreakOneBigSubset_into_n(JsonArray groups){
//        System.out.println("---> BREAK ONE BIG GROUP INTO N SMALLER GROUPS");
//        System.out.println(this.gson.toJson(groups));

        Random rand = new Random();

        int num_groups = groups.size();

        // Find feasible groups to split
        ArrayList<Integer> feasible_group_idx = this.findFeasibleGroupsToSplit(groups);
        if(feasible_group_idx.isEmpty()){
            return groups;
        }

        // Choose a random feasible group to split
        int split_group_idx = feasible_group_idx.get(rand.nextInt(feasible_group_idx.size()));

        // Choose how many sub-groups to create
        int split_group_item_count = groups.get(split_group_idx).getAsJsonObject().getAsJsonArray("elements").size();
        int num_subgroups = (rand.nextInt(split_group_item_count) + 1);

        // Create the new sub-groups
        ArrayList<JsonArray> sub_groups = new ArrayList<>();
        for(int x = 0; x < num_subgroups; x++){
            JsonArray sub_group_elements = new JsonArray();
            sub_groups.add(sub_group_elements);
        }

        // For each sub-group, assign a random element from the group to split
        for(int x = 0; x < num_subgroups; x++){
            int current_split_group_item_count = groups.get(split_group_idx).getAsJsonObject().getAsJsonArray("elements").size();
            int idx_item_to_move = rand.nextInt(current_split_group_item_count);

            // Clone split group element to move
            JsonObject split_group_element_copy = groups.get(split_group_idx).getAsJsonObject().getAsJsonArray("elements").get(idx_item_to_move).getAsJsonObject().deepCopy();

            // Remove split group element
            groups.get(split_group_idx).getAsJsonObject().getAsJsonArray("elements").remove(idx_item_to_move);

            sub_groups.get(x).add(split_group_element_copy);
        }

        // For any elements left in the group to split, randomly assign to one of the sub-groups
        int current_split_group_item_count = groups.get(split_group_idx).getAsJsonObject().getAsJsonArray("elements").size();
        for(int x = 0; x < current_split_group_item_count; x++){

            // Clone split group element to move
            JsonObject split_group_element_copy = groups.get(split_group_idx).getAsJsonObject().getAsJsonArray("elements").get(x).getAsJsonObject().deepCopy();

            // Add element to random sub-group
            int rand_sub_group_idx = rand.nextInt(sub_groups.size());
            sub_groups.get(rand_sub_group_idx).add(split_group_element_copy);
        }

        // Remove split group
        groups.remove(split_group_idx);

        // Repair group ids
        groups = this.repairGroupIDs(groups);

        // Add new sub-groups to design
        for(JsonArray sub_group: sub_groups){
            JsonObject new_group = new JsonObject();
            new_group.addProperty("id", groups.size());
            new_group.addProperty("type", "list");
            new_group.addProperty("active", true);
            new_group.add("elements", sub_group);
            groups.add(new_group);
        }

//        System.out.println("------ AFTER");
//        System.out.println(this.gson.toJson(groups));
//        App.sleep(30);

        return groups;
    }


    // 4. Mutation operator that combines two random small groups to create a new larger group: requires min 2 groups
    public JsonArray mutationCombineTwoSmallSubsets(JsonArray groups){

        Random rand       = new Random();
        int    num_groups = groups.size();

        if(num_groups == 1){
            return groups;
        }

        // Ensure groups are different
        int group_merge_1 = rand.nextInt(num_groups);
        int group_merge_2  = rand.nextInt(num_groups);
        while(group_merge_2 == group_merge_1){
            group_merge_2 = rand.nextInt(num_groups);
        }

        // Create new group
        JsonObject new_group = new JsonObject();
        new_group.addProperty("id", num_groups-1);
        new_group.addProperty("type", "list");
        new_group.addProperty("active", true);
        JsonArray new_group_elements = new JsonArray();
        new_group.add("elements", new_group_elements);

        // Add group 1 items to new group
        int num_group_1_elements = groups.get(group_merge_1).getAsJsonObject().getAsJsonArray("elements").size();
        for(int x = 0; x < num_group_1_elements; x++){
            JsonObject element_copy = groups.get(group_merge_1).getAsJsonObject().getAsJsonArray("elements").get(x).getAsJsonObject().deepCopy();
            new_group_elements.add(element_copy);
        }

        // Add group 2 items to new group
        int num_group_2_elements = groups.get(group_merge_2).getAsJsonObject().getAsJsonArray("elements").size();
        for(int x = 0; x < num_group_2_elements; x++){
            JsonObject element_copy = groups.get(group_merge_2).getAsJsonObject().getAsJsonArray("elements").get(x).getAsJsonObject().deepCopy();
            new_group_elements.add(element_copy);
        }

        // Remove groups that were merged
        if(group_merge_1 > group_merge_2){
            groups.remove(group_merge_1);
            groups.remove(group_merge_2);
        }
        else{
            groups.remove(group_merge_2);
            groups.remove(group_merge_1);
        }

        // Add merged group
        groups.add(new_group);

        return groups;
    }

    // 4.1 Same as mutation operator 4, but combines n small groups into one big group
    public JsonArray mutationCombine_n_SmallSubsets(JsonArray groups){
        return new JsonArray();
    }


//     _    _                 _     _   _
//    | |  | |               (_)   | | (_)
//    | |__| | ___ _   _ _ __ _ ___| |_ _  ___ ___
//    |  __  |/ _ \ | | | '__| / __| __| |/ __/ __|
//    | |  | |  __/ |_| | |  | \__ \ |_| | (__\__ \
//    |_|  |_|\___|\__,_|_|  |_|___/\__|_|\___|___/

    /*
        Improve by adding synergies: This heuristic rule identifies the missing synergies in an
        architecture, selects one of these missing synergies randomly, and swaps the position of two
        elements in order to capture that synergy.
     */
    public void improveByAddingSynergy(){

    }

    /*
        Improve by eliminating interference: This heuristic rule identifies a current interference in an
        architecture, and swaps the position of two elements in order to break that interference.
     */
    public void improveByRemovingInterference(){

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

        System.out.println("-----> PARTITIONING ENUMERATION");
        System.out.println(this.enumeration_store.size());

    }

    // Multiple Parents
    public void enumerateMultiDependency(ArrayList<HashMap<Integer, JsonArray>> parent_enumerations){


    }

    // Single Parent
    public void enumerateSingleDependency(HashMap<Integer, JsonArray> parent_enumerations){
        for(Integer key: parent_enumerations.keySet()){
            JsonArray elements = parent_enumerations.get(key);

            // 1. Determine the number of elements (num_active) with the (active: true) key
            int num_active = Decision.numActiveElements(elements);

            ArrayList<ArrayList<ArrayList<Integer>>> all_archs = this.enumeratePartitions(num_active);
            if(all_archs.isEmpty()){
                continue;
            }
            ArrayList<ArrayList<Integer>> architectures = all_archs.get(all_archs.size()-1);

            this.buildEnumerationStore(elements, architectures);
        }
    }

    /*
        Takes the number of elements to partition and returns a list of
        ArrayList - this holds a list of all the possible partition architectures for (x = idx) elements in restricted growth format
        <
            ArrayList - if this is the (Nth) entry in the above array list, this holds all possible partitions for N objects in restricted growth format
            <
                ArrayList<> This represents one possible partition for N objects
            >
        >
     */
    private ArrayList<ArrayList<ArrayList<Integer>>> enumeratePartitions(int num_elements){

        // outer level: each element contains all partition architectures for 'idx' elements
        // middle level: all partition architectures for 'idx' elements
        // inner level: one architecture
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
        two_elements_a1.add(1);
        ArrayList<Integer> two_elements_a2 = new ArrayList<>();
        two_elements_a2.add(1);
        two_elements_a2.add(2);
        two_elements.add(two_elements_a1);
        two_elements.add(two_elements_a2);
        architectures.add(two_elements);


        // For our third+ elements - all partition architecture for 3+ elements
        for(int i = 3; i <= num_elements; i++){
            architectures.add(new ArrayList<ArrayList<Integer>>());

            int num_prev_archs = architectures.get(i-1).size();
            for(int a = 0; a < num_prev_archs; a++){

                ArrayList<Integer> arch = architectures.get(i-1).get(a);

                Integer mx = Collections.max(arch) + 1;
                for(int j = 1; j <= mx; j++){
                    ArrayList<Integer> new_arch = new ArrayList(arch);
                    new_arch.add(j);
                    architectures.get(architectures.size()-1).add(new_arch);
                }
            }
        }

        return architectures;
    }


    // Add to this.enumeration_store object
    private void buildEnumerationStore(JsonArray elements, ArrayList<ArrayList<Integer>> architectures){
        int enum_counter = this.enumeration_store.keySet().size();
        ArrayList<Integer> active_indicies = this.getActiveIndicies(elements);

        for(ArrayList<Integer> arch: architectures){
            int num_groups = Collections.max(arch);
            JsonArray new_elements = new JsonArray();

            // Add Groups
            for(int x = 0; x < num_groups; x++){
                JsonObject group = new JsonObject();
                group.addProperty("id", x);
                group.addProperty("type", "list");
                group.addProperty("active", true);
                group.add("elements", new JsonArray());
                new_elements.add(group);
            }

            // Add elements to groups
            for(int x = 0; x < arch.size(); x++){
                int element_idx = active_indicies.get(x);
                JsonObject element = elements.get(element_idx).getAsJsonObject().deepCopy();

                int group_num = arch.get(x)-1; // 0 up numbering

                new_elements.get(group_num).getAsJsonObject().getAsJsonArray("elements").add(element);
            }
            this.enumeration_store.put(enum_counter, new_elements);
            enum_counter++;
        }
    }

    private ArrayList<ArrayList<String>> reformatPartitions(ArrayList<ArrayList<ArrayList<Integer>>> architectures){
        ArrayList<ArrayList<String>> reformatted = new ArrayList<>();

        for(ArrayList<ArrayList<Integer>> archs_of_element_num: architectures){
            ArrayList<String> archs = new ArrayList<>();

            for(ArrayList<Integer> itm: archs_of_element_num){
                archs.add(this.integerArrayListToString(itm));
            }

            reformatted.add(archs);
        }
        return reformatted;
    }

    private String integerArrayListToString(ArrayList<Integer> int_array){
        String ret = "";
        for(Integer itm: int_array){
            ret += Integer.toString(itm);
        }
        return ret;
    }



    @Override
    public ArrayList<JsonArray> enumerateDecision(JsonArray elements){
        ArrayList<JsonArray> enumerations = new ArrayList<>();

        // 1. Determine the number of elements (num_active) with the (active: true) key
        int num_active = Decision.numActiveElements(elements);

        // 2.
        ArrayList<ArrayList<ArrayList<Integer>>> all_archs = this.enumeratePartitions(num_active);
        if(all_archs.isEmpty()){
            return enumerations;
        }
        ArrayList<ArrayList<Integer>> architectures = all_archs.get(all_archs.size()-1);

        // 3.
        ArrayList<Integer> active_indicies = this.getActiveIndicies(elements);
        for(ArrayList<Integer> arch: architectures){
            int num_groups = Collections.max(arch);
            JsonArray new_elements = new JsonArray();

            // Add Groups
            for(int x = 0; x < num_groups; x++){
                JsonObject group = new JsonObject();
                group.addProperty("id", x);
                group.addProperty("type", "list");
                group.addProperty("active", true);
                group.add("elements", new JsonArray());
                new_elements.add(group);
            }

            // Add elements to groups
            for(int x = 0; x < arch.size(); x++){
                int element_idx = active_indicies.get(x);
                JsonObject element = elements.get(element_idx).getAsJsonObject().deepCopy();

                int group_num = arch.get(x)-1; // 0 up numbering

                new_elements.get(group_num).getAsJsonObject().getAsJsonArray("elements").add(element);
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




    public ArrayList<Integer> findFeasibleGroupsToSplit(JsonArray groups){
        int num_groups = groups.size();
        ArrayList<Integer> feasible_group_idx = new ArrayList<>();
        for(int x = 0; x < num_groups; x++){
            if(groups.get(x).getAsJsonObject().getAsJsonArray("elements").size() > 1){
                feasible_group_idx.add(x);
            }
        }
        return feasible_group_idx;
    }



    @Override
    public void printDecisions(){
        for(int x = 0; x < this.decisions.size(); x++){
            this.printDecision(x);
        }
    }

    @Override
    public void printDecision(int idx){
        JsonObject decision = this.decisions.get(idx).getAsJsonObject();
        JsonArray  groups   = decision.getAsJsonArray("elements");

        System.out.println("\n" + idx + " -----------------------------");
        for(int x = 0; x < groups.size(); x++){
            JsonObject group = groups.get(x).getAsJsonObject();
            JsonArray  group_elements = group.getAsJsonArray("elements");
            ArrayList<String> group_ary = new ArrayList<>();
            for(int y = 0; y < group_elements.size(); y++){
                JsonObject element = group_elements.get(y).getAsJsonObject();
                group_ary.add(element.get("name").getAsString());
            }
            System.out.println(group_ary);
        }
        System.out.println(idx + " -----------------------------\n");
    }

    public ArrayList<Integer> decisionToShortNotation(JsonObject decision){
        JsonArray groups   = decision.get("elements").getAsJsonArray();
        JsonArray items    = decision.get("dependencies").getAsJsonArray();
        return this.decisionToShortNotation(groups, items);
    }

    public ArrayList<Integer> decisionToShortNotation(JsonArray groups, JsonArray items){
        Iterator  item_itr = items.iterator();

        ArrayList<Integer> short_notation = new ArrayList<Integer>(items.size());

        while(item_itr.hasNext()){
            JsonObject item = ((JsonElement) item_itr.next()).getAsJsonObject();
            if(item.get("active").getAsBoolean()){
                int item_id = item.get("id").getAsInt();
                int item_group = this.getGroupIdxFromId(groups, item_id);
                if(item_group == -1){
                    System.out.println("----> Element not found in group when creating short notation: Partitioning");
                    System.exit(0);
                }
                short_notation.add(item_group);
            }
            else{
                short_notation.add(0);
            }
        }
        return short_notation;
    }

    private int getGroupIdxFromId(JsonArray groups, int id){
        Iterator group_itr = groups.iterator();
        int counter = 1;

        // Iterate over groups
        while(group_itr.hasNext()){
            JsonObject group       = ((JsonElement) group_itr.next()).getAsJsonObject();
            JsonArray  elements    = group.get("elements").getAsJsonArray();
            Iterator   element_itr = elements.iterator();

            // Iterate over group elements
            while(element_itr.hasNext()){
                JsonObject element    = ((JsonElement) element_itr.next()).getAsJsonObject();
                int        element_id = element.get("id").getAsInt();
                if(id == element_id){
                    return counter;
                }
            }
            counter++;
        }
        return -1;
    }



    public void writeParentInfo(JsonArray parent_elements, ArrayList<Integer> parent_grouping, String file_name){

        String full_file_path = this.debug_dir + "crossover/" + file_name;

        JsonElement parent_short_json = this.gson.toJsonTree(parent_grouping, new TypeToken<ArrayList<Integer>>() {}.getType() );
        JsonArray to_write = new JsonArray();
        to_write.add(parent_elements);
        to_write.add(parent_short_json);

        Files.writeDebugFile(full_file_path, to_write);
    }

    public void writeParentHalfs(JsonArray papa_half, JsonArray mama_half, JsonArray child_elements, String file_name){

        String full_file_path = this.debug_dir + "crossover/" + file_name;

        JsonObject to_write = new JsonObject();
        to_write.add("papa_half", papa_half);
        to_write.add("mama_half", mama_half);
        to_write.add("child_entities", child_elements);

        Files.writeDebugFile(full_file_path, to_write);
    }



    @Override
    public String getDesignString(int idx){
        JsonObject design   = this.decisions.get(idx).getAsJsonObject();
        JsonArray  elements = design.getAsJsonArray("elements");
        return this.gson.toJson(elements);
    }





}
