package graph;


import com.google.gson.JsonArray;
import graph.decision.*;
import graph.neo4j.DatabaseClient;
import graph.utils.Design;
import graph.utils.Root;
import org.neo4j.driver.Record;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Graph {

    private DatabaseClient      client;
    private ArrayList<Record>   depthFirstNodes;
    private ArrayList<Record>   topologicalNodes;
    private HashMap<String, Decision> decisions;
    private Decision root;
    private Decision end_node;

    private int numDesigns;
    private boolean isEnumerated;

    private String mutationType;

    private String problem;
    private String formulation;




//     ____        _ _     _
//    |  _ \      (_) |   | |
//    | |_) |_   _ _| | __| | ___ _ __
//    |  _ <| | | | | |/ _` |/ _ \ '__|
//    | |_) | |_| | | | (_| |  __/ |
//    |____/ \__,_|_|_|\__,_|\___|_|

    public static class Builder {

        private DatabaseClient            client;
        private ArrayList<Record>         depthFirstNodes;
        private ArrayList<Record>         topologicalNodes;
        private HashMap<String, Decision> decisions;
        private Decision                  root;
        private Decision                  end_node;
        private String                    problem;
        private String                    formulation;
        private String                    mutationType;

        public Builder(DatabaseClient client, String formulation, String problem, boolean reset_nodes, boolean reset_graphs) {
            this.client  = client;
            this.problem = problem;
            this.formulation = formulation;
            this.depthFirstNodes   = new ArrayList<>();
            this.topologicalNodes  = new ArrayList<>();
            this.decisions         = new HashMap<>();
            this.mutationType      = "JOINT";
            if(reset_nodes){
                this.client.obliterateNodes();
            }
            if(reset_graphs){
                this.client.obliterateGraphs();
            }
        }

        public Builder setMutationType(String mutationType){
            this.mutationType = mutationType;
            return this;
        }

        public Builder indexGraph(String graph_type){

            if(graph_type.equals("EOSS")){
                this.client.indexEOSS();
            }
            else if(graph_type.equals("TDRS")){
                this.client.indexTDRS();
            }
//            else if(graph_type.equals("GNC")){
//                this.client.indexGNCFull();
//            }
//            else if(graph_type.equals("GNC_SMALL")){
//                this.client.indexGNCSmall();
//            }
//            else if(graph_type.equals("SMAP")){
//                this.client.indexSMAP();
//            }

            return this;
        }

        public Builder buildTopologicalOrdering(){

            // --> 1. Define node and edge labels
            String node_labels       = "['Decision', 'Root', 'Design']";
            String dependency_labels = "['DEPENDENCY', 'ROOT_DEPENDENCY', 'FINAL_DEPENDENCY']";

            // --> 2. Build depth first ordering
            this.client.buildGDSGraph(node_labels, dependency_labels);
            this.depthFirstNodes = this.client.genericTraversal("dfs");

            // --> 3. Build topological ordering
            this.topologicalNodes = this.client.buildTopologicalOrdering(this.depthFirstNodes);
            System.out.println("\n----- TOPOLOGICAL ORDER -----");
            for(Record node: this.topologicalNodes){
                System.out.println(node);
            }
            System.out.println("-----------------------------\n");
            return this;
        }

        public Builder projectGraph(){
            int counter  = 0;
            int idxRoot  = 0;
            int idxFinal = this.topologicalNodes.size() - 1;
            for(Record node: this.topologicalNodes){

                // ROOT
                if(counter == idxRoot){
                    this.projectRoot(node);
                }

                // FINAL
                else if(counter == idxFinal){
                    this.projectFinal(node);
                }

                // DECISION
                else{
                    this.projectDecision(node);
                }

                counter ++;
            }
            this.printDecisions();
            return this;
        }

        private void projectRoot(Record node){
            String node_name = Graph.getNodeName(node);
            this.root = new Root.Builder(node)
                    .setDatabaseClient(this.client)
                    .setChildren()
                    .setInputs()
                    .build();
            this.decisions.put(node_name, this.root);
        }

        private void projectDecision(Record node){
            String node_name = Graph.getNodeName(node);
            String node_type = Graph.getNodeType(node);

            Decision selection = null;
            if(node_type.equals("DownSelecting")){
                selection = new DownSelecting.Builder(node)
                        .setDatabaseClient(this.client)
                        .setParents(this.decisions)
                        .setChildren()
                        .setDecisions()
                        .build();
            }
            else if(node_type.equals("Partitioning")){
                selection = new Partitioning.Builder(node)
                        .setDatabaseClient(this.client)
                        .setParents(this.decisions)
                        .setChildren()
                        .setDecisions()
                        .build();
            }
            else if(node_type.equals("Permuting")){
                selection = new Permuting.Builder(node)
                        .setDatabaseClient(this.client)
                        .setParents(this.decisions)
                        .setChildren()
                        .setDecisions()
                        .build();
            }
            else if(node_type.equals("Assigning")){
                selection = new Assigning.Builder(node)
                        .setDatabaseClient(this.client)
                        .setParents(this.decisions)
                        .setChildren()
                        .setDecisions()
                        .build();
            }
            else if(node_type.equals("Connecting")){
                selection = new Connecting.Builder(node)
                        .setDatabaseClient(this.client)
                        .setParents(this.decisions)
                        .setChildren()
                        .setDecisions()
                        .build();
            }
            else if(node_type.equals("StandardForm")){
                selection = new StandardForm.Builder(node)
                        .setDatabaseClient(this.client)
                        .setParents(this.decisions)
                        .setChildren()
                        .setDecisions()
                        .build();
            }
            this.decisions.put(node_name, selection);
        }

        private void projectFinal(Record node){
            String node_type   = Graph.getNodeType(node);
            String node_name   = Graph.getNodeName(node);
            this.end_node = new Design.Builder(node)
                    .setDatabaseClient(this.client)
                    .setParents(this.decisions)
                    .setDesigns()
                    .build();
            this.decisions.put(node_name, this.end_node);
        }

        private void printDecisions(){
            System.out.println("\n----- ALL DECISIONS -----");
            for(String key: this.decisions.keySet()){
                System.out.println(key + ": " + this.decisions.get(key));
            }
            System.out.println("-------------------------\n");
        }

        public Graph build(){

            Graph build             = new Graph();
            build.client            = this.client;
            build.depthFirstNodes   = this.depthFirstNodes;
            build.topologicalNodes  = this.topologicalNodes;
            build.decisions         = this.decisions;
            build.root              = this.root;
            build.end_node          = this.end_node;
            build.numDesigns        = 0;
            build.isEnumerated      = false;
            build.mutationType      = this.mutationType;
            build.problem = problem;
            build.formulation = formulation;
            return build;
        }
    }









//  _______          _    _
// |__   __|        | |  (_)
//    | |  ___  ___ | |_  _  _ __    __ _
//    | | / _ \/ __|| __|| || '_ \  / _` |
//    | ||  __/\__ \| |_ | || | | || (_| |
//    |_| \___||___/ \__||_||_| |_| \__, |
//                                   __/ |
//                                  |___/

    public void testDownSelecting(){

    }
    public void testPartitioning(){

    }
    public void testPermuting(){

    }
    public void testAssigning(){
        System.out.println("---> TESTING ASSIGNING DECISION");
        try{
            int idx1 = this.generateRandomDesign();
            int idx2 = this.generateRandomDesign();
            this.crossover(idx1, idx2, 0);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void testStandardForm(){
        System.out.println("---> TESTING ASSIGNING DECISION");
        try{
            int idx1 = this.generateRandomDesign();
            int idx2 = this.generateRandomDesign();
            this.crossover(idx1, idx2, 0);

//            int num_rand = 30;
//            int num_corss = 20;
//            for(int x = 0; x < num_rand; x++){
//                int idx1 = this.generateRandomDesign();
//            }
//            for(int x = 0; x < num_corss; x++){
//                this.crossover(x, 25, 0);
//            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void testConnecting(){

    }



//      ____                       _   _
//     / __ \                     | | (_)
//    | |  | |_ __   ___ _ __ __ _| |_ _  ___  _ __  ___
//    | |  | | '_ \ / _ \ '__/ _` | __| |/ _ \| '_ \/ __|
//    | |__| | |_) |  __/ | | (_| | |_| | (_) | | | \__ \
//     \____/| .__/ \___|_|  \__,_|\__|_|\___/|_| |_|___/
//           | |
//           |_|


    // -------------------------
    // ----- RANDOM DESIGN -----
    // -------------------------
    public int generateRandomDesign() throws Exception{


        // Iterate over nodes in topological order
        for(Record node: this.topologicalNodes){
            String node_name = Graph.getNodeName(node);
            String node_type = Graph.getNodeType(node);

            Decision current = this.decisions.get(node_name);
            current.generateRandomDesign();
        }
        this.numDesigns++;
        return (this.numDesigns - 1);
    }

    public int generateRandomDesign(JsonArray dependency, String node_name) throws Exception{
        // node_name
        // -- Instrument Selection
        // -- Instrument Partitioning
        // -- Satellite Scheduling

        Decision node = this.decisions.get(node_name);
        node.generateRandomDesign(dependency);
        this.numDesigns++;
        return (this.numDesigns - 1);
    }



    // -----------------------
    // ----- ENUMERATION -----
    // -----------------------

    // --> ENTIRE ADD
    public void enumerateDesignSpace(){

        // 1. Iterate over nodes in topological order
        for(Record node: this.topologicalNodes){
            String node_name = Graph.getNodeName(node);
            String node_type = Graph.getNodeType(node);

            Decision current = this.decisions.get(node_name);
            current.enumerateDesignSpace();
        }

        this.isEnumerated = true;
    }
    public ArrayList<String> getEnumeratedDesignStrings(){
        Design d_node = (Design) this.end_node;
        return d_node.getEnumeratedDesignStrings();
    }

    // --> SINGLE NODE
    public ArrayList<JsonArray> enumerateDesignSpace(JsonArray dependency, String node_name){
        // node_name
        // -- Instrument Selection
        // -- Instrument Partitioning
        // -- Satellite Scheduling

        Decision node = this.decisions.get(node_name);
        return   node.enumerateDecision(dependency);
    }




    // ---------------------
    // ----- CROSSOVER -----
    // ---------------------

    // --> ENTIRE ADD: resolves with parent decision
    public int crossover(int papa, int mama, double mutationProbability) throws Exception{
        Random rand = new Random();
        double trueMutationProbability = mutationProbability;

        // JOINT
        if(this.mutationType.equalsIgnoreCase("JOINT")){
            trueMutationProbability = 0;
            if(rand.nextDouble() <= mutationProbability){
                trueMutationProbability = 1;
            }
        }
        // DISJOINT
        else{
            trueMutationProbability = mutationProbability;
        }

        for(Record node: this.topologicalNodes){
            String node_name = Graph.getNodeName(node);
            String node_type = Graph.getNodeType(node);

            Decision current = this.decisions.get(node_name);
            current.crossoverDesigns(papa, mama, trueMutationProbability);
        }
        this.numDesigns++;
        return (this.numDesigns - 1);
    }

    // DISJOINT - each node will have a different probability of mutation
    public int crossover(int papa, int mama, ArrayList<Double> mutation_probabilities) throws Exception{

        if(this.topologicalNodes.size() != mutation_probabilities.size()){
            System.out.println("---> PROBABILITY ARRAY MUST HAVE SAME SIZE AS NUMBER OF DECISIONS");
            System.exit(0);
        }

        int counter = 0;
        for(int x = 0; x < this.topologicalNodes.size(); x++){
            Record node        = this.topologicalNodes.get(x);
            Double probability = mutation_probabilities.get(x);
            String node_name   = Graph.getNodeName(node);
            String node_type   = Graph.getNodeType(node);

            Decision current = this.decisions.get(node_name);
            current.crossoverDesigns(papa, mama, probability);
        }
        this.numDesigns++;
        return (this.numDesigns - 1);
    }



    // --> SINGLE NODE: resolves with provided dependency
    public int crossover(int papa, int mama, double mutationProbability, String node_name, JsonArray dependency) throws Exception{
//        System.out.println("---> GRAPH CROSSOVER");
//        App.sleep(1);
        Decision node = this.decisions.get(node_name);
        node.crossoverDesigns(papa, mama, mutationProbability, dependency);
        this.numDesigns++;
        return (this.numDesigns - 1);
    }


    // --------------------
    // ----- MUTATION -----
    // --------------------

    public void mutate(int idxDesign){

    }


    // ----------------------
    // ----- GET DESIGN -----
    // ----------------------

    // --> ENTIRE ADD
    public String getDesignString(int idx){
        Design d_node = (Design) this.end_node;
        return d_node.getDesignString(idx);
    }

    // --> SINGLE NODE
    public String getDesignString(int idx, String node_name){
        Decision node = this.decisions.get(node_name);
        return node.getDesignString(idx);
    }


    // --------------------
    // ----- DATABASE -----
    // --------------------

    public void commitDesignObjectiveScore(String objective_name, double objective_value, int design_idx){
        Design d_node = (Design) this.end_node;
        d_node.commitDesignScores(objective_name, objective_value, design_idx);
    }


    // -----------------
    // ----- PRINT -----
    // -----------------

    public void printDesign(int idx){

    }

    public void printDesign(int idx, String node_name){
        Decision node = this.decisions.get(node_name);
        node.printDecision(idx);
    }

    public void printDesigns(String node_name){
        Decision node = this.decisions.get(node_name);
        node.printDecisions();
    }










    public void randomAlternative() {

    }

    public void featureMining() {

    }

    public void designDistance(int idxFirst, int idxSecond){

    }

    public void localSearch(int idxDesign){

    }



    public void closeDatabaseConnection(){
        this.client.closeConnection();
    }

    // DESIGN STRING FROM: end_node



    // HARDCODE: FOR EOS FORMULATION
    public String getDownSelectingDesignString(int idx){
        DownSelecting node = (DownSelecting) this.decisions.get("Instrument Selection");
        return node.getDesignString(idx);
    }

    public String getPartitioningDesignString(int idx){
        Partitioning node = (Partitioning) this.decisions.get("Instrument Partitioning");
        return node.getDesignString(idx);
    }


    public static String getNodeName(Record node){
        return node.get("names.name").toString().replace("\"", "");
    }
    public static String getNodeName(Record node, String key){
        return node.get(key).toString().replace("\"", "");
    }

    public static String getNodeType(Record node){
        return node.get("names.type").toString().replace("\"", "");
    }
    public static String getNodeType(Record node, String key){
        return node.get(key).toString().replace("\"", "");
    }

    public static String removeQuotes(String obj){
        return obj.replace("\"", "");
    }



}
