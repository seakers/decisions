package graph.neo4j;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import formulations.Smap;
import graph.Decision;
import graph.Graph;
import graph.sort.Topological;
import org.neo4j.driver.*;
import formulations.Decadal;
import formulations.GuidanceNavigationAndControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class DatabaseClient {

    public Driver  driver;
    public String  problem;
    public Gson    gson;

    public static class Builder {

        public String uri;
        public String user;
        public String password;
        public String problem;

        public Builder(String uri) {
            this.uri = uri;
        }

        public Builder credentials(String user, String password){
            this.user     = user;
            this.password = password;
            return this;
        }

        public Builder problem(String problem){
            this.problem = problem;
            return this;
        }

        public DatabaseClient build(){
            DatabaseClient build = new DatabaseClient();
            build.driver         = GraphDatabase.driver(this.uri, AuthTokens.basic(this.user, this.password));
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


    public ArrayList<Record> getNodeParents(String nodeName){
        Session session = this.driver.session();
        return  session.writeTransaction( tx -> nodeParentQuery(tx, nodeName));
    }
    private ArrayList<Record> nodeParentQuery(final Transaction tx, final String nodeName){
        Result query = tx.run(
                "MATCH (m)-->(dec) " +
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


    public ArrayList<Record> getNodeParameter(String node_name, String node_type, String parameter){
        Session session = this.driver.session();
        return  session.writeTransaction( tx -> parameterQuery(tx, node_name, node_type, parameter));
    }
    private ArrayList<Record> parameterQuery(final Transaction tx, final String node_name, final String node_type, final String parameter_name){
        String node_str   = " MATCH (n:" + this.problem + ":" + node_type + ") ";
        String where_str  = " WHERE n.name = \"" + node_name + "\"  ";
        String return_str = " RETURN n." + parameter_name;

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


    public ArrayList<Record> getNodeChildren(String node_name, String node_type){
        Session session = this.driver.session();
        return  session.writeTransaction( tx -> childQuery(tx, node_name, node_type));
    }
    private ArrayList<Record> childQuery(final Transaction tx, final String node_name, final String node_type){
        String node_str  = "MATCH (m:" + this.problem + ":" + node_type + ")-->(dec)";

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
        return session.writeTransaction( tx -> relatshipQuery(tx, parent.node_type, parent.node_name, child.node_type, child.node_name));
    }
    private ArrayList<Record> relatshipQuery(final Transaction tx, String p_type, String p_name, String c_type, String c_name){
        String relationship_str = "MATCH (m:" + this.problem + ":" + p_type + " { name: $p_name})-[r]->(n:" + this.problem + ":" + c_type + " { name: $c_name})";
        Result query = tx.run(
                relationship_str + "RETURN (r.type)",
                Values.parameters("p_name", p_name, "c_name", c_name)
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
        return session.writeTransaction( tx -> relationshipAttributeQuery(tx, parent.node_type, parent.node_name, child.node_type, child.node_name, attribute));
    }
    private ArrayList<Record> relationshipAttributeQuery(final Transaction tx, String p_type, String p_name, String c_type, String c_name, String attribute){
        String relationship_str = "MATCH (m:" + this.problem + ":" + p_type + " { name: $p_name})-[r]->(n:" + this.problem + ":" + c_type + " { name: $c_name})";
        Result query = tx.run(
                relationship_str + "RETURN (r."+attribute+")",
                Values.parameters("p_name", p_name, "c_name", c_name)
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
        String node_str   = " MATCH (names:" + this.problem + ") ";
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

    public ArrayList<Record> setNodeJsonParameter(String node_name, String node_type, String parameter, JsonArray elements){
        Session session = this.driver.session();
        return  session.writeTransaction( tx -> nodeParameterMutation(tx, node_name, node_type, parameter, elements));
    }
    private ArrayList<Record> nodeParameterMutation(final Transaction tx, String node_name, String node_type, String parameter, JsonArray elements){
        String node_str     = " MATCH (n:" + this.problem + ":" + node_type + ") ";
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
    public ArrayList<Record> genericTraversal(String graphName, String traversal){
        Session session = this.driver.session();
        return  session.writeTransaction( tx -> traversalQuery(tx, graphName, traversal));
    }
    private ArrayList<Record> traversalQuery(final Transaction tx, final String graphName, final String traversal){
        String root          = "MATCH (a:" + this.problem + ":Root {name: \"Start\"}) ";
        String traversalCall = "CALL gds.alpha." + traversal + ".stream($graphName, {startNode: startNode}) ";

        Result query = tx.run(
                root +
                        "WITH id(a) AS startNode " +
                        traversalCall +
                        "YIELD path " +
                        "UNWIND [ n in nodes(path) | n ] AS names " +
                        "RETURN names.name, names.type",
                Values.parameters("rootName", this.problem, "graphName", graphName)
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
        ArrayList<Record> children = this.getNodeChildren(node_name, node_type);
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

    public void buildGenericGraph(String graphName, String node_labels, String dependency_labels){
        Result result = this.driver.session().writeTransaction( tx -> genericGraphQuery(tx, graphName, node_labels, dependency_labels));
    }
    private Result genericGraphQuery(final Transaction tx, final String graphName, final String node_labels, final String dependency_labels){
        // --------> node_labels: ['Decision', 'Root']
        // --> dependency_labels: ['DEPENDENCY', 'ROOT_DEPENDENCY', 'FINAL_DEPENDENCY']
        String call = "CALL gds.graph.create($graphName, " + node_labels + ", " + dependency_labels + ")";
        return tx.run(call, Values.parameters("graphName", graphName));
    }


    public void obliterateGraphs(){
        System.out.println("---> Obliterating graphs");
        Session            session = this.driver.session();
        ArrayList<String>  graphs  = session.writeTransaction( tx -> listGraphs(tx));
        for(String graph : graphs){
            System.out.println("--> Deleting graph: " + graph);
            session.writeTransaction( tx -> obliterateGraph(tx, graph));
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
        return tx.run("MATCH (n) DETACH DELETE n");
    }

    public void closeConnection(){
        this.driver.close();
    }



//   _____                     _
//  / ____|                   | |
// | |  __  _ __  __ _  _ __  | |__   ___
// | | |_ || '__|/ _` || '_ \ | '_ \ / __|
// | |__| || |  | (_| || |_) || | | |\__ \
//  \_____||_|   \__,_|| .__/ |_| |_||___/
//                     | |
//                     |_|


    public void indexGNCTest(){
        try (Session session1 = this.driver.session()){

            String problem         = this.problem;
            String root_parameters = this.gson.toJson(GuidanceNavigationAndControl.getTestRootParameters());

            // 1. Create nodes
            session1.writeTransaction( tx -> addGenericRoot(tx, problem, root_parameters));

            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "DownSelecting", "Num Sensor Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "DownSelecting", "Num Computer Selection"));

            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "StandardForm", "Sensor Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "StandardForm", "Computer Selection"));

            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "Assigning", "Sensor to Computer"));

            session1.writeTransaction( tx -> addGenericFinal(tx, problem));

            // 2. Edges

            // ROOT -> DOWN SELECTING
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Root",
                            "Start",
                            "Decision",
                            "Num Sensor Selection",
                            "ROOT_DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Root",
                            "Start",
                            "Decision",
                            "Num Computer Selection",
                            "ROOT_DEPENDENCY"
                    )
            );


            // DOWN SELECTING -> STANDARD FORM
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Num Computer Selection",
                            "Decision",
                            "Computer Selection",
                            "DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Num Sensor Selection",
                            "Decision",
                            "Sensor Selection",
                            "DEPENDENCY"
                    )
            );



            // ASSIGNING
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Sensor Selection",
                            "Decision",
                            "Sensor to Computer",
                            "DEPENDENCY",
                            "FROM"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Computer Selection",
                            "Decision",
                            "Sensor to Computer",
                            "DEPENDENCY",
                            "TO"
                    )
            );



            // FINAL
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Sensor to Computer",
                            "Design",
                            "Finish",
                            "FINAL_DEPENDENCY"
                    )
            );



        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public void indexSMAPAssigning(){
        try (Session session1 = this.driver.session()){

            String problem         = this.problem;
            String root_parameters = this.gson.toJson(Smap.getRootParameters());

            // 1. Create nodes
            session1.writeTransaction( tx -> addGenericRoot(tx, problem, root_parameters));

            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "DownSelecting", "Orbit Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "DownSelecting", "Instrument Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "Assigning", "Instrument to Orbit"));

            session1.writeTransaction( tx -> addGenericFinal(tx, problem));


            // 2. Create edges

            // ROOT
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Root",
                            "Start",
                            "Decision",
                            "Orbit Selection",
                            "ROOT_DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Root",
                            "Start",
                            "Decision",
                            "Instrument Selection",
                            "ROOT_DEPENDENCY"
                    )
            );

            // ASSIGNING
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Instrument Selection",
                            "Decision",
                            "Instrument to Orbit",
                            "DEPENDENCY",
                            "FROM"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Orbit Selection",
                            "Decision",
                            "Instrument to Orbit",
                            "DEPENDENCY",
                            "TO"
                    )
            );

            // FINAL
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Instrument to Orbit",
                            "Design",
                            "Finish",
                            "FINAL_DEPENDENCY"
                    )
            );
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public void indexGNCGeneric(){
        try (Session session1 = this.driver.session()){

            String problem         = this.problem;
            String root_parameters = this.gson.toJson(GuidanceNavigationAndControl.getProdRootParameters());

            // 1. Create nodes
            session1.writeTransaction( tx -> addGenericRoot(tx, problem, root_parameters));

            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "DownSelecting", "Num Sensor Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "DownSelecting", "Num Computer Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "DownSelecting", "Num Actuator Selection"));

            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "StandardForm", "Sensor Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "StandardForm", "Computer Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "StandardForm", "Actuator Selection"));

            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "Assigning", "Sensor to Computer"));
            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "Assigning", "Computer to Actuator"));

            session1.writeTransaction( tx -> addGenericFinal(tx, problem));

            // 2. Create dependencies

            // ROOT -> DOWN SELECTING
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Root",
                            "Start",
                            "Decision",
                            "Num Sensor Selection",
                            "ROOT_DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Root",
                            "Start",
                            "Decision",
                            "Num Computer Selection",
                            "ROOT_DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Root",
                            "Start",
                            "Decision",
                            "Num Actuator Selection",
                            "ROOT_DEPENDENCY"
                    )
            );


            // DOWN SELECTION -> STANDARD FORM
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Num Sensor Selection",
                            "Decision",
                            "Sensor Selection",
                            "DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Num Computer Selection",
                            "Decision",
                            "Computer Selection",
                            "DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Num Actuator Selection",
                            "Decision",
                            "Actuator Selection",
                            "DEPENDENCY"
                    )
            );

            // ASSIGNING
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Sensor Selection",
                            "Decision",
                            "Sensor to Computer",
                            "DEPENDENCY",
                            "FROM"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Computer Selection",
                            "Decision",
                            "Sensor to Computer",
                            "DEPENDENCY",
                            "TO"
                    )
            );


            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Computer Selection",
                            "Decision",
                            "Computer to Actuator",
                            "DEPENDENCY",
                            "FROM"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Actuator Selection",
                            "Decision",
                            "Computer to Actuator",
                            "DEPENDENCY",
                            "TO"
                    )
            );


            // FINAL
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Sensor to Computer",
                            "Design",
                            "Finish",
                            "FINAL_DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Computer to Actuator",
                            "Design",
                            "Finish",
                            "FINAL_DEPENDENCY"
                    )
            );

        }
    }


    public void indexClimateCentricGeneric(){
        try (Session session1 = this.driver.session()){

            String problem         = this.problem;
            String root_parameters = this.gson.toJson(Decadal.getRootParameters());
            // String root_parameters = this.gson.toJson(Decadal.getBigRootParameters());
//            String root_parameters = this.gson.toJson(Decadal.get2007Parameters());

            // 1. Create nodes
            session1.writeTransaction( tx -> addGenericRoot(tx, problem, root_parameters));
            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "DownSelecting", "Instrument Selection"));
            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "Partitioning", "Instrument Partitioning"));
            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "Permuting", "Satellite Scheduling"));
            session1.writeTransaction( tx -> addGenericFinal(tx, problem));

            // TEST
//            session1.writeTransaction( tx -> addGenericDecision(tx, problem, "Permuting", "Satellite Scheduling2"));

            // 2. Create dependencies
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Root",
                            "Start",
                            "Decision",
                            "Instrument Selection",
                            "ROOT_DEPENDENCY"
                            )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Instrument Selection",
                            "Decision",
                            "Instrument Partitioning",
                            "DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Instrument Partitioning",
                            "Decision",
                            "Satellite Scheduling",
                            "DEPENDENCY"
                    )
            );
            session1.writeTransaction(
                    tx -> addGenericDependency(tx,
                            problem,
                            "Decision",
                            "Satellite Scheduling",
                            "Design",
                            "Finish",
                            "FINAL_DEPENDENCY"
                    )
            );

            // TEST
//            session1.writeTransaction(
//                    tx -> addGenericDependency(tx,
//                            problem,
//                            "Decision",
//                            "Instrument Partitioning",
//                            "Decision",
//                            "Satellite Scheduling2",
//                            "DEPENDENCY"
//                    )
//            );
//            session1.writeTransaction(
//                    tx -> addGenericDependency(tx,
//                            problem,
//                            "Decision",
//                            "Satellite Scheduling2",
//                            "Design",
//                            problem,
//                            "FINAL_DEPENDENCY"
//                    )
//            );

        }
    }







    private Result addGenericRoot(final Transaction tx, final String problem, final String deps_str){

        String query = "CREATE (n:" + problem + ":Root {name: \"Start\", type: $type, initial_params: $deps_str})";
        return tx.run(query, Values.parameters("problem", problem, "type", "Root", "deps_str", deps_str));
    }

    private Result addGenericDecision(final Transaction tx, final String problem, final String decision_type, final String node_name){
        JsonArray decisions     = new JsonArray();
        JsonArray dependencies  = new JsonArray();

        String decisions_str    = this.gson.toJson(decisions);
        String dependencies_str = this.gson.toJson(dependencies);

//        String query = "CREATE (n:" + problem + ":Decision:" + decision_type + " {name: $decision_desc, type: $decision_type, decisions: $decisions_str, dependencies: $dependencies_str})";
        String query = "CREATE (n:" + problem + ":Decision:" + decision_type + " {name: $decision_desc, type: $decision_type, decisions: $decisions_str})";
        return tx.run(query, Values.parameters("decision_type", decision_type, "decision_desc", node_name, "decisions_str", decisions_str, "dependencies_str", dependencies_str));
    }

    private Result addGenericFinal(final Transaction tx, final String problem){
        JsonArray designs        = new JsonArray();
        String    designs_str    = this.gson.toJson(designs);

//        String query = "CREATE (n:" + problem + ":Design {name: $problem, type: $type, designs: $designs_str})";
        String query = "CREATE (n:" + problem + ":Design {name: \"Finish\", type: $type, designs: $designs_str})";
        return tx.run(query, Values.parameters("problem", problem, "type", "Design", "designs_str", designs_str));
    }

    private Result addGenericDependency(final Transaction tx,
                                        final String problem,
                                        final String parent_label,
                                        final String parent_name,
                                        final String child_label,
                                        final String child_name,
                                        final String dependency_name
    ){
        String rel_type = "";
        String parent   = "MATCH  (parent:" + problem + ":" + parent_label + " {name: $parent_name}) ";
        String child    = "MATCH  (child:"  + problem + ":" + child_label  + " {name: $child_name} ) ";
        String edge     = "CREATE (parent)-[:" + dependency_name + " { type: $rel_type}]->(child)";

        return tx.run(
                parent + child + edge,
                Values.parameters(
                        "problem", problem,
                        "parent_label", parent_label,
                        "parent_name", parent_name,
                        "child_label", child_label,
                        "child_name", child_name,
                        "dependency_name", dependency_name,
                        "rel_type", rel_type
                )
        );
    }

    private Result addGenericDependency(final Transaction tx,
                                            final String problem,
                                            final String parent_label,
                                            final String parent_name,
                                            final String child_label,
                                            final String child_name,
                                            final String dependency_name,
                                            final String rel_type
    ){
        String parent = "MATCH  (parent:" + problem + ":" + parent_label + " {name: $parent_name}) ";
        String child  = "MATCH  (child:"  + problem + ":" + child_label  + " {name: $child_name} ) ";
        String edge   = "CREATE (parent)-[:" + dependency_name + " { type: $rel_type}]->(child)";

        return tx.run(
                parent + child + edge,
                Values.parameters(
                        "problem", problem,
                        "parent_label", parent_label,
                        "parent_name", parent_name,
                        "child_label", child_label,
                        "child_name", child_name,
                        "dependency_name", dependency_name,
                        "rel_type", rel_type
                )
        );
    }




    private Result addGenericDependency(final Transaction tx,
                                        final String problem,
                                        final String parent_label,
                                        final String parent_name,
                                        final String child_label,
                                        final String child_name,
                                        final String dependency_name,
                                        final String rel_type,
                                        final HashMap<String, String> relationship_key_values
    ){
        String parent = "MATCH  (parent:" + problem + ":" + parent_label + " {name: $parent_name}) ";
        String child  = "MATCH  (child:"  + problem + ":" + child_label  + " {name: $child_name} ) ";

        String map_values = "";
        for(String key: relationship_key_values.keySet()){
            String value = relationship_key_values.get(key);
            map_values += (", " +  key + ": " + value);
        }
        String edge   = "CREATE (parent)-[:" + dependency_name + " { type: $rel_type"+map_values+"}]->(child)";

        return tx.run(
                parent + child + edge,
                Values.parameters(
                        "problem", problem,
                        "parent_label", parent_label,
                        "parent_name", parent_name,
                        "child_label", child_label,
                        "child_name", child_name,
                        "dependency_name", dependency_name,
                        "rel_type", rel_type
                )
        );
    }




}
