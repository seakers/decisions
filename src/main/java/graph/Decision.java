package graph;




//   _____            _     _
//  |  __ \          (_)   (_)
//  | |  | | ___  ___ _ ___ _  ___  _ __
//  | |  | |/ _ \/ __| / __| |/ _ \| '_ \
//  | |__| |  __/ (__| \__ \ | (_) | | | |
//  |_____/ \___|\___|_|___/_|\___/|_| |_|


import app.App;
import com.google.gson.*;
import graph.neo4j.DatabaseClient;
import org.neo4j.driver.Record;

import java.util.*;

public class Decision {

//     __      __        _       _     _
//     \ \    / /       (_)     | |   | |
//      \ \  / /_ _ _ __ _  __ _| |__ | | ___  ___
//       \ \/ / _` | '__| |/ _` | '_ \| |/ _ \/ __|
//        \  / (_| | |  | | (_| | |_) | |  __/\__ \
//         \/ \__,_|_|  |_|\__,_|_.__/|_|\___||___/

    public String                       node_name;
    public String                       node_type;
    protected String                    node_writes;

    protected DatabaseClient            client;
    protected Record                    node;
    protected ArrayList<Decision>       parents;

    protected ArrayList<Record>         children;
    protected HashMap<String, Decision> decision_nodes;
    protected Gson                      gson;

    // RANDOM
    protected Random                    rand;

    // DECISION
    protected JsonArray                 decisions;

    // ROOT
    protected JsonArray                 parameters;
    protected JsonObject                inputs;

    // DESIGN
    protected JsonArray                 designs;

    // Last decisions made form either: crossover, random decision...
    public    JsonArray                 last_decision;



    /*
        This variable denotes the target field that the decision is to operate on. The following describes the behavior
            of each decision type with regards to this variable.

        1. StandardForm
            - The decision searches each dimension of the data structure to see if it contains the operates_on key. If
                it does, choose the corresponding policy for the StandardForm decision
     */


    // ENUMERATION
    protected HashMap<Integer, JsonArray> enumeration_store;







//     ____        _ _     _
//    |  _ \      (_) |   | |
//    | |_) |_   _ _| | __| | ___ _ __
//    |  _ <| | | | | |/ _` |/ _ \ '__|
//    | |_) | |_| | | | (_| |  __/ |
//    |____/ \__,_|_|_|\__,_|\___|_|


    public static class Builder<T extends Builder<T>>{

        // --> Common to all decisions
        protected DatabaseClient            client;
        protected String                    node_name;
        protected String                    node_type;
        protected String                    node_writes;
        protected Record                    node;
        protected ArrayList<Decision>       parents;
        protected ArrayList<Record>         children;
        protected HashMap<String, Decision> decision_nodes;
        protected Gson                      gson;
        protected JsonArray                 decisions;
        protected JsonArray                 parameters;
        protected JsonObject                inputs;
        protected JsonArray                 designs;
        protected Random                    rand;

        public Builder(Record node){
            this.node        = node;
            this.node_name   = node.get("names.name").toString().replace("\"", "");
            this.node_type   = node.get("names.type").toString().replace("\"", "");
            this.node_writes = node.get("names.writes").toString().replace("\"", "");
            this.parents        = new ArrayList<>();
            this.decision_nodes = new HashMap<>();
            this.gson           = new GsonBuilder().setPrettyPrinting().create();
            this.decisions      = new JsonArray();
            this.parameters     = new JsonArray();
            this.inputs         = new JsonObject();
            this.designs        = new JsonArray();
            this.rand           = new Random();
        }

        public Builder setDatabaseClient(DatabaseClient client){
            this.client = client;
            return this;
        }

        public Builder setParents(HashMap<String, Decision> decisions){
            this.decision_nodes = decisions;
            ArrayList<Record> parents = this.client.getNodeParents(this.node_name);
            for(Record parent: parents){
                String parent_name = parent.get("m.name").toString().replace("\"", "");
                if(this.decision_nodes.containsKey(parent_name) && !this.parents.contains(this.decision_nodes.get(parent_name))){
                    this.parents.add(this.decision_nodes.get(parent_name));
                }
                else{
                    System.out.println("-----> PARENT NOT FOUND: " + parent_name);
                }
            }
            return this;
        }

        public Builder setChildren(){
            this.children = this.client.getNodeChildren(this.node_name);
            return this;
        }

        public Builder setInputs(){
            this.inputs = this.client.getRootProblemInfo(this.node_name);
            return this;
        }

        public Builder setParameters(){
            this.parameters = this.client.getNodeProblemInfo(this.node_name);
            return this;
        }

        public Builder setDecisions(){
            this.decisions = this.client.getNodeProblemInfo(this.node_name);
            return this;
        }

        public Builder setDesigns(){
            this.designs = this.client.getNodeProblemInfo(this.node_name);
            return this;
        }

        public Decision build() { return new Decision(this);}
    }

    public void print(){
        System.out.println("\n-------- DECISION --------");
        System.out.println("--------- name: " + this.node_name);
        System.out.println("--------- type: " + this.node_type);
        System.out.println("------- writes: " + this.node_writes);
        System.out.println("------ parents: " + this.parents);
        System.out.println("----- children: " + this.children);
        System.out.println("------- inputs: " + this.gson.toJson(this.inputs));
        System.out.println("--- parameters: " + this.gson.toJson(this.parameters));
        System.out.println("---- decisions: " + this.gson.toJson(this.decisions));
        System.out.println("------ designs: " + this.gson.toJson(this.designs));
        System.out.println("--------------------------\n");
    }

    protected Decision(Builder<?> builder) {
        this.client         = builder.client;
        this.node           = builder.node;
        this.node_name      = builder.node_name;
        this.node_type      = builder.node_type;
        this.node_writes    = builder.node_writes;
        this.parents        = builder.parents;
        this.decision_nodes = builder.decision_nodes;
        this.children       = builder.children;
        this.gson           = builder.gson;
        this.parameters     = builder.parameters;
        this.inputs         = builder.inputs;
        this.decisions      = builder.decisions;
        this.designs        = builder.designs;
        this.rand           = builder.rand;
        this.last_decision  = new JsonArray();
        this.enumeration_store = new HashMap<>();
        this.print();
    }


//      ____                      _     _
//     / __ \                    (_)   | |
//    | |  | |_   _____ _ __ _ __ _  __| | ___
//    | |  | \ \ / / _ \ '__| '__| |/ _` |/ _ \
//    | |__| |\ V /  __/ |  | |  | | (_| |  __/
//     \____/  \_/ \___|_|  |_|  |_|\__,_|\___|


    public String getDesignString(int idx){
        JsonObject design   = this.decisions.get(idx).getAsJsonObject();
        JsonArray  elements = design.getAsJsonArray("elements");
        return this.gson.toJson(elements);
    }

    public void enumerateDesignSpace(){
        System.out.println("---> Enumerate Design Space: " + this.node_name + " - " + this.node_type);
    }

    public ArrayList<JsonArray> enumerateDecision(JsonArray dependency){
        System.out.println("---> Enumerate Decision: " + this.node_name + " - " + this.node_type);
        return (new ArrayList<>());
    }

    public HashMap<Integer, JsonArray> getEnumerations(String node_name, String node_type){
        if(this.enumeration_store.isEmpty()){
            System.out.println("--> Parent enumeration store is empty!!!");
            App.sleep(10);
            return new HashMap<>();
        }
        return this.enumeration_store;
    }


    public void generateRandomDesign() throws Exception{
        System.out.println("---> Generating Random Design: " + this.node_name + " - " + this.node_type);
    }

    public void generateRandomDesign(JsonArray dependency) throws Exception{
        System.out.println("---> Generating Random Design (with dependency): " + this.node_name + " - " + this.node_type);
    }




    public void crossoverDesigns(int papa, int mama, double mutation_probability) throws Exception{
        System.out.println("---> Crossing Over Designs (" + papa +", " + mama + ") : " + this.node_name + " - " + this.node_type);
    }

    public void crossoverDesigns(int papa, int mama, double mutation_probability, JsonArray dependency) throws Exception{
        System.out.println("---> Crossing Over Designs (" + papa +", " + mama + ") : " + this.node_name + " - " + this.node_type);
    }


    /*
        Returns last JsonObject in this.decisions
     */
    public JsonObject getLastDecision(){
        int num_decisions = this.decisions.size();
        if(num_decisions == 0){
            System.out.println("--> getLastDecision: no decisions have been made");
            return new JsonObject();
        }
        return ((JsonElement) this.decisions.get(num_decisions - 1)).getAsJsonObject();
    }


//      ____                       _   _
//     / __ \                     | | (_)
//    | |  | |_ __   ___ _ __ __ _| |_ _  ___  _ __  ___
//    | |  | | '_ \ / _ \ '__/ _` | __| |/ _ \| '_ \/ __|
//    | |__| | |_) |  __/ | | (_| | |_| | (_) | | | \__ \
//     \____/| .__/ \___|_|  \__,_|\__|_|\___/|_| |_|___/
//           | |
//           |_|

    // Ensure feasibility
    protected ArrayList<Integer> getRandomBitString(int length){
        boolean feasibility = false;
        ArrayList<Integer> bit_str = new ArrayList<>();

        while(!feasibility){

            bit_str = new ArrayList<>();
            for(int x = 0; x < length; x++){
                if(this.rand.nextBoolean()){
                    bit_str.add(1);
                }
                else{
                    bit_str.add(0);
                }
            }
            feasibility = this.checkBitStringFeasibility(bit_str);
        }

        return bit_str;
    }

    protected boolean checkBitStringFeasibility(ArrayList<Integer> bitstring){
        for(Integer bit: bitstring){
            if(bit.equals(1)){
                return true;
            }
        }
        return false;
    }


    protected ArrayList<Integer> getActiveIndices(JsonArray array){
        ArrayList<Integer> indicies = new ArrayList<>();
        Iterator array_iterator = array.iterator();
        int counter = 0;
        while(array_iterator.hasNext()){
            JsonObject element = ((JsonElement) array_iterator.next()).getAsJsonObject();
            boolean active = element.get("active").getAsBoolean();
            if(active){
                indicies.add(counter);
            }
            counter++;
        }
        return indicies;
    }

    protected ArrayList<Integer> getInactiveIndices(JsonArray array){
        ArrayList<Integer> indicies = new ArrayList<>();
        Iterator array_iterator = array.iterator();
        int counter = 0;
        while(array_iterator.hasNext()){
            JsonObject element = ((JsonElement) array_iterator.next()).getAsJsonObject();
            boolean active = element.get("active").getAsBoolean();
            if(!active){
                indicies.add(counter);
            }
            counter++;
        }
        return indicies;
    }

    protected void indexNewDesign(JsonArray parent_design_elements, JsonArray new_design_elements){
        JsonObject new_design = new JsonObject();
        int        new_idx    = this.decisions.size();

        new_design.addProperty("id", new_idx);
        // new_design.add("elements", new_design_elements);
        new_design.add(this.node_writes, new_design_elements);
        new_design.add("dependencies", parent_design_elements);

        // SCORES
        new_design.add("scores", new JsonObject());

        this.decisions.add(new_design);
        System.out.println("\n------------ NEW DECISION ------------\n"
                + "--- node name: " + this.node_name + "\n"
                + "--- node type: " + this.node_type + "\n"
                + "------- depth: " + this.getConstantDecisionDepth(new_design) + "\n"
                + this.gson.toJson(new_design)
                + "\n--------------------------------------\n"
        );
    }

    protected void updateNodeDecisions(){
        this.client.updateNodeProblemInfo(this.node_name, this.decisions);
    }

    protected void updateFinalDesigns(){
        this.client.updateNodeProblemInfo(this.node_name, this.decisions);
    }


    protected int getConstantDecisionDepth(JsonObject design){
        if(!design.has("elements")){
            return 1;
        }

        // All designs should have at least a depth of 1
        JsonArray elements        = design.getAsJsonArray("elements");
        Iterator  elements_itr    = elements.iterator();
        int       min_child_depth = 100000;
        while(elements_itr.hasNext()){
            JsonObject child_element   = ((JsonElement) elements_itr.next()).getAsJsonObject();
            int        child_depth     = this.getElementDepth(child_element);
            if(child_depth < min_child_depth){
                min_child_depth = child_depth;
            }
        }

        return min_child_depth;
    }

    protected int getElementDepth(JsonObject element){

        String element_type = element.get("type").toString();
        if(element_type.equals("\"item\"")){
            return 1;
        }

        // The element type must be: list
        JsonArray child_elements     = element.getAsJsonArray("elements");
        Iterator  child_elements_itr = child_elements.iterator();
        int       min_depth_child    = 10000;
        while(child_elements_itr.hasNext()){
            JsonObject child_element = ((JsonElement) child_elements_itr.next()).getAsJsonObject();
            int        child_depth   = this.getElementDepth(child_element);
            if(child_depth < min_depth_child){
                min_depth_child = child_depth;
            }
        }

        // Add 1 to the child with the smallest depth to account for this call
        return (1 + min_depth_child);
    }


    protected String getParentRelationshipType(Decision parent) {
        ArrayList<Record> type_obj = this.client.getRelationshipType(parent, this);
        return type_obj.get(0).get("(r.type)").asString();
    }

    protected String getParentRelationshipAttribute(Decision parent, String attribute){
        ArrayList<Record> type_obj = this.client.getRelationshipAttribute(parent, this, attribute);
        return type_obj.get(0).get("(r."+attribute+")").asString();
    }

    protected ArrayList<String> getParentMultiRelationshipAttribute(Decision parent, String attribute){
        ArrayList<String> attributes = new ArrayList<>();
        ArrayList<Record> type_obj = this.client.getRelationshipAttribute(parent, this, attribute);
        for(Record rec: type_obj){
            attributes.add(rec.get("(r."+attribute+")").asString());
        }
        return attributes;
    }

    protected int getParentRelationshipCardinality(Decision parent, String attribute){
        ArrayList<Record> type_obj = this.client.getRelationshipAttribute(parent, this, attribute);
        return type_obj.size();
    }


    public void printDecision(int idx){

    }

    public void printDecisions(){

    }



//   _____ _        _   _
//  / ____| |      | | (_)
// | (___ | |_ __ _| |_ _  ___
//  \___ \| __/ _` | __| |/ __|
//  ____) | || (_| | |_| | (__
// |_____/ \__\__,_|\__|_|\___|


    public static ArrayList<Integer> bitStringToArray(String bitstring){
        ArrayList<Integer> bits = new ArrayList<>();
        for(char ch: bitstring.toCharArray()){
            String chr = "" + ch;
            bits.add(Integer.parseInt(chr));
        }
        return bits;
    }

    public static String bitArrayToString(ArrayList<Integer> elements){
        String bit_string = "";
        for(Integer element: elements){
            bit_string += element;
        }
        return bit_string;
    }

    public static int getRandomIdx(JsonArray ary){
        Random rand = new Random();
        return rand.nextInt(ary.size());
    }

    public static boolean active(JsonObject obj){
        return obj.get("active").getAsBoolean();
    }

    public static ArrayList<Integer> elementsToBitString(JsonArray elements){
        ArrayList<Integer> bit_string = new ArrayList<>();
        Iterator element_itr = elements.iterator();
        while(element_itr.hasNext()){
            JsonObject element = ((JsonElement) element_itr.next()).getAsJsonObject();
            if(element.get("active").getAsBoolean()){
                bit_string.add(1);
            }
            else{
                bit_string.add(0);
            }
        }
        return bit_string;
    }

    public static List[] splitList(List<Integer> list){
        List<Integer> first  = new ArrayList<Integer>();
        List<Integer> second = new ArrayList<Integer>();

        int size = list.size();

        for (int i = 0; i < size / 2; i++)
            first.add(list.get(i));

        for (int i = size / 2; i < size; i++)
            second.add(list.get(i));

        return new List[] { first, second };
    }

    public static int numActiveElements(JsonArray elements){
        int counter = 0;
        Iterator element_itr = elements.iterator();
        while(element_itr.hasNext()){
            JsonObject element = ((JsonElement) element_itr.next()).getAsJsonObject();
            if(element.get("active").getAsBoolean()){
                counter++;
            }
        }
        return counter;
    }

    public static boolean getProbabilityResult(double probability){
        Random rand = new Random();
        return (rand.nextDouble() <= probability);
    }


}


