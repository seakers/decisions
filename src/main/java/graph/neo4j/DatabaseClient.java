package graph.neo4j;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import graph.formulations.Smap;
import graph.Decision;
import graph.Graph;
import graph.sort.Topological;
import org.neo4j.driver.*;
import graph.formulations.Decadal;
import graph.formulations.GuidanceNavigationAndControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class DatabaseClient {

    public Driver  driver;
    public Gson    gson;
    public String  formulation;
    public String  problem;


    public static class Builder {

        public String uri;
        public String user;
        public String password;
        public String formulation;
        public String problem;

        public Builder(String uri) {
            this.uri = uri;
        }

        public Builder setCredentials(String user, String password){
            this.user     = user;
            this.password = password;
            return this;
        }

        public Builder setFormulation(String formulation){
            this.formulation = formulation;
            return this;
        }

        public Builder setProblem(String problem){
            this.problem = problem;
            return this;
        }

        public DatabaseClient build(){
            DatabaseClient build = new DatabaseClient();
            build.driver         = GraphDatabase.driver(this.uri, AuthTokens.basic(this.user, this.password));
            build.formulation    = this.formulation;
            build.problem        = this.problem;
            build.gson           = new Gson();
            return build;
        }
    }


//      ____                  _
//     / __ \                (_)
//    | |  | |_   _  ___ _ __ _  ___  ___
//    | |  | | | | |/ _ \ '__| |/ _ \/ __|
//    | |__| | |_| |  __/ |  | |  __/\__ \
//     \___\_\\__,_|\___|_|  |_|\___||___/


    public JsonArray getNodeProblemInfo(String node_name){

        // --> 1. Get all problem info
        ArrayList<Record> problem_list = this.getNodeParameter(node_name, "problems");
        String            problem_str  = problem_list.get(0).get("n.problems").asString();
        JsonObject        problems     = JsonParser.parseString(problem_str).getAsJsonObject();

        // --> 2. If problem info not found, index empty problem info
        if(!problems.has(this.problem)){
            JsonArray elements = new JsonArray();
            this.updateNodeProblemInfo(node_name, elements);
            return elements;
        }

        // --> 3. Else, return current problem info
        return problems.getAsJsonArray("elements");
    }


    /*
        ---> Input: node name (e.x. Instrument Selection)
        --> Output: node parents (parameters: name, type)
     */
    public ArrayList<Record> getNodeParents(String nodeName){
        Session session = this.driver.session();
        return  session.writeTransaction( tx -> nodeParentQuery(tx, nodeName));
    }
    private ArrayList<Record> nodeParentQuery(final Transaction tx, final String nodeName){
        Result query = tx.run(
                "MATCH (m:" + this.formulation + ")-->(dec) " +
                        "WHERE dec.name = $nodeName " +
                        "RETURN m.name, m.type ",
                Values.parameters("nodeName", nodeName)
        );
        ArrayList<Record> nodes = new ArrayList<>();
        while(query.hasNext()){
            Record node = query.next();
//            System.out.println("--> " + node.get("m.name") + " " + node.get("m.type"));
            nodes.add(node);
        }
        return nodes;
    }

    /*
        ---> Input: node name, node type, requested parameter
        --> Output: node parameter value
     */
    public ArrayList<Record> getNodeParameter(String node_name, String parameter){
        Session session = this.driver.session();
        return  session.writeTransaction( tx -> parameterQuery(tx, node_name, parameter));
    }
    private ArrayList<Record> parameterQuery(final Transaction tx, final String node_name, final String parameter){
        String node_str   = " MATCH (n:" + this.formulation + ") ";
        String where_str  = " WHERE n.name = \"" + node_name + "\"  ";
        String return_str = " RETURN n." + parameter;

        Result query = tx.run(
                node_str + where_str + return_str,
                Values.parameters()
        );
        ArrayList<Record> items = new ArrayList<>();
        while(query.hasNext()){
            Record item = query.next();
            items.add(item);
        }
        return items;
    }

    /*
        ---> Input: node name, node type
        --> Output: node children
     */
    public ArrayList<Record> getNodeChildren(String node_name){
        Session session = this.driver.session();
        return  session.writeTransaction( tx -> childQuery(tx, node_name));
    }
    private ArrayList<Record> childQuery(final Transaction tx, final String node_name){
        String node_str  = "MATCH (m:" + this.formulation + ")-->(dec)";

        Result query = tx.run(
                node_str +
                        "WHERE m.name = $node_name " +
                        "RETURN dec.name, dec.type ",
                Values.parameters("node_name", node_name)
        );
        ArrayList<Record> nodes = new ArrayList<>();
        while(query.hasNext()){
            Record node = query.next();
            nodes.add(node);
        }
        return nodes;
    }


    public ArrayList<Record> getRelationshipType(Decision parent, Decision child){
        Session session = this.driver.session();
        return session.writeTransaction( tx -> relationshipQuery(tx, parent.node_name, child.node_name));
    }
    private ArrayList<Record> relationshipQuery(final Transaction tx, String parent_name, String child_name){
        String relationship_str = "MATCH (m:" + this.formulation + " { name: $parent_name})-[r]->(n:" + this.formulation + " { name: $child_name})";
        Result query = tx.run(
                relationship_str + "RETURN (r.type)",
                Values.parameters("parent_name", parent_name, "child_name", child_name)
        );
        ArrayList<Record> items = new ArrayList<>();
        while(query.hasNext()){
            Record item = query.next();
            items.add(item);
        }
        return items;
    }

    public ArrayList<Record> getRelationshipAttribute(Decision parent, Decision child, String attribute){
        Session session = this.driver.session();
        return session.writeTransaction( tx -> relationshipAttributeQuery(tx, parent.node_name, child.node_name, attribute));
    }
    private ArrayList<Record> relationshipAttributeQuery(final Transaction tx, String parent_name, String child_name, String attribute){
        String relationship_str = "MATCH (m:" + this.formulation + " { name: $parent_name})-[r]->(n:" + this.formulation + " { name: $child_name})";
        Result query = tx.run(
                relationship_str + "RETURN (r."+attribute+")",
                Values.parameters("parent_name", parent_name, "child_name", child_name)
        );
        ArrayList<Record> items = new ArrayList<>();
        while(query.hasNext()){
            Record item = query.next();
            items.add(item);
        }
        return items;
    }




    public ArrayList<Record> getNodeRecord(String node_name){
        Session session = this.driver.session();
        return session.writeTransaction( tx -> recordQuery(tx, node_name));
    }
    private ArrayList<Record> recordQuery(final Transaction tx, String node_name){
        String node_str   = " MATCH (names:" + this.formulation + ") ";
        String where_str  = " WHERE names.name = \"" + node_name + "\"  ";
        Result query = tx.run(
                node_str + where_str + " RETURN names.name, names.type",
                Values.parameters()
        );
        ArrayList<Record> items = new ArrayList<>();
        while(query.hasNext()){
            Record item = query.next();
            items.add(item);
        }
        return items;
    }



//     __  __       _        _   _
//    |  \/  |     | |      | | (_)
//    | \  / |_   _| |_ __ _| |_ _  ___  _ __  ___
//    | |\/| | | | | __/ _` | __| |/ _ \| '_ \/ __|
//    | |  | | |_| | || (_| | |_| | (_) | | | \__ \
//    |_|  |_|\__,_|\__\__,_|\__|_|\___/|_| |_|___/


    public ArrayList<Record> updateNodeProblemInfo(String node_name, JsonArray elements){

        // --> 1. Get current problem info from node
        ArrayList<Record> problem_list = this.getNodeParameter(node_name, "problems");
        String            problem_str  = problem_list.get(0).get("n.problems").asString();
        JsonObject        problems     = JsonParser.parseString(problem_str).getAsJsonObject();

        // --> 2. Update the current problem info in the JsonObject
        problems.add(this.problem, elements);

        // --> 3. Commit JsonObject to node
        return this.setNodeParameterJsonObject(node_name, "problems", problems);
    }



    public ArrayList<Record> setNodeParameterJsonObject(String node_name, String parameter, JsonObject elements){
        Session session = this.driver.session();
        return  session.writeTransaction( tx -> nodeParameterMutationObject(tx, node_name, parameter, elements));
    }
    private ArrayList<Record> nodeParameterMutationObject(final Transaction tx, String node_name, String parameter, JsonObject elements){
        String node_str     = " MATCH (n:" + this.formulation + ") ";
        String where_str    = " WHERE n.name = \"" + node_name + "\"  ";
        String set_str      = " SET n." + parameter + " = $elements ";
        String elements_str = this.gson.toJson(elements);
        Result query = tx.run(
                node_str +
                        where_str +
                        set_str +
                        "RETURN n",
                Values.parameters("elements", elements_str)
        );
        ArrayList<Record> nodes = new ArrayList<>();
        while(query.hasNext()){
            Record node = query.next();
            nodes.add(node);
        }
        return nodes;
    }

    public ArrayList<Record> setNodeParameterJsonArray(String node_name, String parameter, JsonArray elements){
        Session session = this.driver.session();
        return  session.writeTransaction( tx -> nodeParameterMutationArray(tx, node_name, parameter, elements));
    }
    private ArrayList<Record> nodeParameterMutationArray(final Transaction tx, String node_name, String parameter, JsonArray elements){
        String node_str     = " MATCH (n:" + this.formulation + ") ";
        String where_str    = " WHERE n.name = \"" + node_name + "\"  ";
        String set_str      = " SET n." + parameter + " = $elements ";
        String elements_str = this.gson.toJson(elements);
        Result query = tx.run(
                node_str +
                        where_str +
                        set_str +
                        "RETURN n",
                Values.parameters("elements", elements_str)
        );
        ArrayList<Record> nodes = new ArrayList<>();
        while(query.hasNext()){
            Record node = query.next();
            nodes.add(node);
        }
        return nodes;
    }




//  _______                                 _
// |__   __|                               | |
//    | |_ __ __ ___   _____ _ __ ___  __ _| |
//    | | '__/ _` \ \ / / _ \ '__/ __|/ _` | |
//    | | | | (_| |\ V /  __/ |  \__ \ (_| | |
//    |_|_|  \__,_| \_/ \___|_|  |___/\__,_|_|

    /*
        Traversal: bfs (breadth first search) | dfs (depth first search)
     */
    public ArrayList<Record> genericTraversal(String traversal){
        Session session = this.driver.session();
        return  session.writeTransaction( tx -> traversalQuery(tx, traversal));
    }
    private ArrayList<Record> traversalQuery(final Transaction tx, final String traversal){
        String root          = "MATCH (a:" + this.formulation + ":Root) ";
        String traversalCall = "CALL gds.alpha." + traversal + ".stream($graphName, {startNode: startNode}) ";

        Result query = tx.run(
                root +
                        "WITH id(a) AS startNode " +
                        traversalCall +
                        "YIELD path " +
                        "UNWIND [ n in nodes(path) | n ] AS names " +
                        "RETURN names.name, names.type",
                Values.parameters("graphName", this.problem)
        );
        ArrayList<Record> nodes = new ArrayList<>();
        System.out.println("\n--------- TRAVERSAL: " + traversal + " ---------");
        while(query.hasNext()){
            Record node = query.next();
            System.out.println(node);
            nodes.add(node);
        }
        System.out.println("----------------------------------");
        return nodes;
    }


    // TOPOLOGICAL
    public ArrayList<Record> buildTopologicalOrdering(ArrayList<Record> nodes){
        ArrayList<Record>        ordering     = new ArrayList<>();
        HashMap<Integer, String> node_map_int = new HashMap<>();
        HashMap<String, Integer> node_map_str = new HashMap<>();
        Topological              sort         = new Topological(nodes.size());

        int counter = 0;
        for(Record node: nodes){
            String node_name = Graph.getNodeName(node);
            node_map_int.put(counter, node_name);
            node_map_str.put(node_name, counter);
            counter++;
        }

        for(Record node: nodes){
            this.buildAdjacencyMatrix(sort, node, node_map_str);
        }

        Stack<Integer> int_ordering = sort.topologicalSort();
        while(!int_ordering.empty()){
            String node_name = node_map_int.get(int_ordering.pop());
            ordering.add(this.getNodeRecord(node_name).get(0));
        }

        return ordering;
    }
    private void buildAdjacencyMatrix(Topological sort, Record node, HashMap<String, Integer> node_map_str){
        String  node_name = Graph.getNodeName(node);
        String  node_type = Graph.getNodeType(node);
        Integer node_id   = node_map_str.get(node_name);
        ArrayList<Record> children = this.getNodeChildren(node_name);
        for(Record child: children){
            Integer child_id = node_map_str.get(Graph.getNodeName(child, "dec.name"));
            sort.addEdge(node_id, child_id);
        }
    }


//    _____                 _
//  / ____|               | |
// | |  __ _ __ __ _ _ __ | |__
// | | |_ | '__/ _` | '_ \| '_ \
// | |__| | | | (_| | |_) | | | |
//  \_____|_|  \__,_| .__/|_| |_|
//                  | |
//                  |_|

    public void buildGDSGraph(String node_labels, String dependency_labels){
        Result result = this.driver.session().writeTransaction( tx -> gdsGraphQuery(tx, node_labels, dependency_labels));
    }
    private Result gdsGraphQuery(final Transaction tx, final String node_labels, final String dependency_labels){
        // --------> node_labels: ['Decision', 'Root', 'Design']
        // --> dependency_labels: ['DEPENDENCY', 'ROOT_DEPENDENCY', 'FINAL_DEPENDENCY']
        String call = "CALL gds.graph.create($graphName, " + node_labels + ", " + dependency_labels + ")";
        System.out.println("--> " + call);
        return tx.run(call, Values.parameters("graphName", this.problem));
    }


    public void obliterateGraphs(){
        System.out.println("---> Obliterating graph: " + this.problem + " (if exists)");
        Session            session = this.driver.session();
        ArrayList<String>  graphs  = session.writeTransaction( tx -> listGraphs(tx));
        for(String graph : graphs){
            if(graph.equals(this.problem)){
                System.out.println("--> Deleting graph: " + graph);
                session.writeTransaction( tx -> obliterateGraph(tx, graph));
            }
        }
    }
    private ArrayList<String> listGraphs(final Transaction tx){
        Result            res    = tx.run( "CALL gds.graph.list() YIELD graphName");
        ArrayList<String> graphs = new ArrayList<>();
        while(res.hasNext()){
            String name      = res.next().get("graphName").toString();
            String shortened = name.substring(1, name.length()-1);
            graphs.add(shortened);
        }
        return graphs;
    }
    private Result obliterateGraph(final Transaction tx, final String graphName){
        return tx.run(
                "CALL gds.graph.drop($graphName) YIELD graphName",
                Values.parameters("graphName", graphName)
        );
    }




    public void obliterateNodes(){
        this.driver.session().writeTransaction( tx -> obliterateNode(tx));
    }
    private Result obliterateNode(final Transaction tx){
        return tx.run("MATCH (n:"+this.formulation+") DETACH DELETE n");
    }

    public void closeConnection(){
        this.driver.close();
    }



//     ______                              _         _    _
//    |  ____|                            | |       | |  (_)
//    | |__  ___   _ __  _ __ ___   _   _ | |  __ _ | |_  _   ___   _ __   ___
//    |  __|/ _ \ | '__|| '_ ` _ \ | | | || | / _` || __|| | / _ \ | '_ \ / __|
//    | |  | (_) || |   | | | | | || |_| || || (_| || |_ | || (_) || | | |\__ \
//    |_|   \___/ |_|   |_| |_| |_| \__,_||_| \__,_| \__||_| \___/ |_| |_||___/


    /*
        SMAP Formulation
        - Three decisions: instrument selection, orbit selection, instrument to orbit partitioning
     */
    public void indexSMAP(){
        try (Session session1 = this.driver.session()){

            String problem         = this.formulation;
            String root_parameters = this.gson.toJson(Smap.getRootParameters());

            // 1. Create nodes
            session1.writeTransaction( tx -> addGenericRoot(tx, root_parameters));

            session1.writeTransaction( tx -> addGenericDecision(tx, "DownSelecting", "Orbit Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, "DownSelecting", "Instrument Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, "Assigning", "Instrument to Orbit"));

            session1.writeTransaction( tx -> addGenericFinal(tx, problem));


            // 2. Create edges

            // ROOT
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Root",
                            "Orbit Selection",
                            "ROOT_DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Root",
                            "Instrument Selection",
                            "ROOT_DEPENDENCY"
                    )
            );

            // ASSIGNING
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Instrument Selection",
                            "Instrument to Orbit",
                            "DEPENDENCY",
                            "FROM"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Orbit Selection",
                            "Instrument to Orbit",
                            "DEPENDENCY",
                            "TO"
                    )
            );

            // FINAL
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Instrument to Orbit",
                            "Design",
                            "FINAL_DEPENDENCY"
                    )
            );
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /*
        GNC Formulation
        - Eight decisions:
     */
    public void indexGNCSmall(){
        try (Session session1 = this.driver.session()){

            String problem         = this.formulation;
            String root_parameters = this.gson.toJson(GuidanceNavigationAndControl.getTestRootParameters());

            // 1. Create nodes
            session1.writeTransaction( tx -> addGenericRoot(tx, root_parameters));

            session1.writeTransaction( tx -> addGenericDecision(tx, "DownSelecting", "Num Sensor Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, "DownSelecting", "Num Computer Selection"));

            session1.writeTransaction( tx -> addGenericDecision(tx, "StandardForm", "Sensor Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, "StandardForm", "Computer Selection"));

            session1.writeTransaction( tx -> addGenericDecision(tx, "Assigning", "Sensor to Computer"));

            session1.writeTransaction( tx -> addGenericFinal(tx, problem));

            // 2. Edges

            // ROOT -> DOWN SELECTING
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Root",
                            "Num Sensor Selection",
                            "ROOT_DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Root",
                            "Num Computer Selection",
                            "ROOT_DEPENDENCY"
                    )
            );


            // DOWN SELECTING -> STANDARD FORM
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Num Computer Selection",
                            "Computer Selection",
                            "DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Num Sensor Selection",
                            "Sensor Selection",
                            "DEPENDENCY"
                    )
            );



            // ASSIGNING
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Sensor Selection",
                            "Sensor to Computer",
                            "DEPENDENCY",
                            "FROM"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Computer Selection",
                            "Sensor to Computer",
                            "DEPENDENCY",
                            "TO"
                    )
            );



            // FINAL
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Sensor to Computer",
                            "Design",
                            "FINAL_DEPENDENCY"
                    )
            );



        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void indexGNCFull(){
        try (Session session1 = this.driver.session()){

            String problem         = this.formulation;
            String root_parameters = this.gson.toJson(GuidanceNavigationAndControl.getProdRootParameters());

            // 1. Create nodes
            session1.writeTransaction( tx -> addGenericRoot(tx, root_parameters));

            session1.writeTransaction( tx -> addGenericDecision(tx, "DownSelecting", "Num Sensor Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, "DownSelecting", "Num Computer Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, "DownSelecting", "Num Actuator Selection"));

            session1.writeTransaction( tx -> addGenericDecision(tx, "StandardForm", "Sensor Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, "StandardForm", "Computer Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, "StandardForm", "Actuator Selection"));

            session1.writeTransaction( tx -> addGenericDecision(tx, "Assigning", "Sensor to Computer"));
            session1.writeTransaction( tx -> addGenericDecision(tx, "Assigning", "Computer to Actuator"));

            session1.writeTransaction( tx -> addGenericFinal(tx, problem));

            // 2. Create dependencies

            // ROOT -> DOWN SELECTING
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Root",
                            "Num Sensor Selection",
                            "ROOT_DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Root",
                            "Num Computer Selection",
                            "ROOT_DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Root",
                            "Num Actuator Selection",
                            "ROOT_DEPENDENCY"
                    )
            );


            // DOWN SELECTION -> STANDARD FORM
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Num Sensor Selection",
                            "Sensor Selection",
                            "DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Num Computer Selection",
                            "Computer Selection",
                            "DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Num Actuator Selection",
                            "Actuator Selection",
                            "DEPENDENCY"
                    )
            );

            // ASSIGNING
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Sensor Selection",
                            "Sensor to Computer",
                            "DEPENDENCY",
                            "FROM"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Computer Selection",
                            "Sensor to Computer",
                            "DEPENDENCY",
                            "TO"
                    )
            );


            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Computer Selection",
                            "Computer to Actuator",
                            "DEPENDENCY",
                            "FROM"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Actuator Selection",
                            "Computer to Actuator",
                            "DEPENDENCY",
                            "TO"
                    )
            );


            // FINAL
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Sensor to Computer",
                            "Design",
                            "FINAL_DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Computer to Actuator",
                            "Design",
                            "FINAL_DEPENDENCY"
                    )
            );

        }
    }


    /*
        EOSS Formulation
        - Three decisions: instrument selection, instrument partitioning, satellite permuting
     */
    public void indexEOSS(){
        try (Session session1 = this.driver.session()){

            String problem         = this.formulation;
            String root_parameters = this.gson.toJson(Decadal.getRootParameters(this.problem));

            // 1. Create nodes
            session1.writeTransaction( tx -> addGenericRoot(tx, root_parameters));
            session1.writeTransaction( tx -> addGenericDecision(tx, "DownSelecting", "Instrument Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, "Partitioning", "Instrument Partitioning"));
            session1.writeTransaction( tx -> addGenericDecision(tx, "Permuting", "Satellite Scheduling"));
            session1.writeTransaction( tx -> addGenericFinal(tx, problem));

            // 2. Create dependencies
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Root",
                            "Instrument Selection",
                            "ROOT_DEPENDENCY"
                            )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Instrument Selection",
                            "Instrument Partitioning",
                            "DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Instrument Partitioning",
                            "Satellite Scheduling",
                            "DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            "Satellite Scheduling",
                            "Design",
                            "FINAL_DEPENDENCY"
                    )
            );
        }
    }







    private Result addGenericRoot(final Transaction tx, final String problems_str){

        String query = "CREATE (n:" + this.formulation + ":Root {name: \"Root\", type: \"Root\", problems: $problems_str})";
        return tx.run(query, Values.parameters("problems_str", problems_str));
    }

    private Result addGenericDecision(final Transaction tx, final String decision_type, final String node_name){
        JsonObject problems_info = new JsonObject();
        problems_info.add(this.problem, new JsonArray());
        String problems_str    = this.gson.toJson(problems_info);

        String query = "CREATE (n:" + this.formulation + ":Decision {name: $node_name, type: $decision_type, problems: $problems_str})";
        return tx.run(query, Values.parameters("node_name", node_name, "decision_type", decision_type,  "problems_str", problems_str));
    }

    private Result addGenericFinal(final Transaction tx, final String problem){
        JsonObject problems_info = new JsonObject();
        problems_info.add(this.problem, new JsonArray());
        String problems_str    = this.gson.toJson(problems_info);

        String query = "CREATE (n:" + problem + ":Design {name: \"Design\", type: \"Design\", problems: $problems_str})";
        return tx.run(query, Values.parameters("problem", problem, "problems_str", problems_str));
    }


    private Result addGenericDependency(final Transaction tx,
                                        final String parent_name,
                                        final String child_name,
                                        final String dependency_name
    ){
        String rel_type = "";
        String parent   = "MATCH  (parent:" + this.formulation + " {name: $parent_name}) ";
        String child    = "MATCH  (child:"  + this.formulation + " {name: $child_name} ) ";
        String edge     = "CREATE (parent)-[:" + dependency_name + " { type: $rel_type}]->(child)";

        return tx.run(
                parent + child + edge,
                Values.parameters(
                        "parent_name", parent_name,
                        "child_name", child_name,
                        "rel_type", rel_type
                )
        );
    }

    private Result addGenericDependency(final Transaction tx,
                                            final String parent_name,
                                            final String child_name,
                                            final String dependency_name,
                                            final String rel_type
    ){
        String parent   = "MATCH  (parent:" + this.formulation + " {name: $parent_name}) ";
        String child    = "MATCH  (child:"  + this.formulation + " {name: $child_name} ) ";
        String edge   = "CREATE (parent)-[:" + dependency_name + " { type: $rel_type}]->(child)";

        return tx.run(
                parent + child + edge,
                Values.parameters(
                        "parent_name", parent_name,
                        "child_name", child_name,
                        "rel_type", rel_type
                )
        );
    }


    private Result addGenericDependency(final Transaction tx,
                                        final String parent_name,
                                        final String child_name,
                                        final String dependency_name,
                                        final String rel_type,
                                        final HashMap<String, String> relationship_key_values
    ){
        String parent   = "MATCH  (parent:" + this.formulation + " {name: $parent_name}) ";
        String child    = "MATCH  (child:"  + this.formulation + " {name: $child_name} ) ";

        String map_values = "";
        for(String key: relationship_key_values.keySet()){
            String value = relationship_key_values.get(key);
            map_values += (", " +  key + ": " + value);
        }
        String edge   = "CREATE (parent)-[:" + dependency_name + " { type: $rel_type"+map_values+"}]->(child)";

        return tx.run(
                parent + child + edge,
                Values.parameters(
                        "parent_name", parent_name,
                        "child_name", child_name,
                        "rel_type", rel_type
                )
        );
    }

}
