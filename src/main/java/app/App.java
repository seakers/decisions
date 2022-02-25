package app;




//import com.opencsv.CSVWriter;
import com.google.gson.*;
import com.opencsv.CSVWriter;
import evaluation.GNC_Evaluator;
import graph.formulations.Decadal;
import graph.Graph;
import graph.neo4j.DatabaseClient;
import graph.utils.Design;
import moea.Algorithm;
import moea.Results;
import moea.problems.ADDProblem;
import moea.solutions.InfoSolution;
import org.moeaframework.Analyzer;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryIntegerVariable;
import graph.formulations.Smap;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import sqs.Consumer;

import java.io.FileWriter;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {


        int runs = 1;
        for(int run_number = 0; run_number < runs; run_number++) {
            System.gc();


//  _____ _   _ _____ _______
// |_   _| \ | |_   _|__   __|
//   | | |  \| | | |    | |
//   | | | . ` | | |    | |
//  _| |_| |\  |_| |_   | |
// |_____|_| \_|_____|  |_|
//

            // MOEA
            boolean moea_full         = false;
            boolean moea_selecting    = false;
            boolean moea_partitioning = false;
            boolean moea_permuting    = false;

            // ENUMERATION
            boolean enumeration              = false;
            boolean enumeration_gnc          = false;
            boolean enumeration_selecting    = false;
            boolean enumeration_partitioning = false;
            boolean enumeration_permuting    = false;

            // OPTIMIZATION TOOLS
            boolean NDSM             = false;
            boolean ContinuityMatrix = false;

            // EVALUATION TESTING
            boolean test_eval        = false;
            boolean test_eval2       = false;
            boolean test_reliability = false;

            // DECISION TESTING
            boolean test_down_selecting = false;
            boolean test_partitioning   = false;
            boolean test_permuting      = false;
            boolean test_assigning      = false;
            boolean test_standard_form  = false;
            boolean test_connecting     = false;

            // MUTATION: JOINT | DISJOINT
            String mutation_type = "DISJOINT";

            // GRAPH TYPE: Decadal / GNC / SMAP / GNC_TEST
            String graph_type = "Decadal";

            // RESET SWITCHES (clears all graphs / nodes when Graph obj constructor starts)
            boolean reset_nodes  = true;
            boolean reset_graphs = true;

            // ENVIRONMENT VARIABLES
            String localstackEndpoint = System.getenv("AWS_STACK_ENDPOINT");
            String uri                = System.getenv("NEO4J_URI");
            String user               = System.getenv("NEO4J_USER");
            String password           = System.getenv("NEO4J_PASSWORD");
            String problem            = System.getenv("PROBLEM");
            // String eval_queue         = System.getenv("EVAL_QUEUE");
            // String eval_queue         = "add_queue_10";
            String eval_queue         = App.getSaltString(15);
            String vassar_queue_url   = System.getenv("VASSAR_QUEUE");


            System.out.println("\n------------------ ADD INIT ------------------");
            System.out.println("----> AWS ENDPOINT URL: " + localstackEndpoint);
            System.out.println("-----------> NEO4J URI: " + uri);
            System.out.println("----------> NEO4J USER: " + user);
            System.out.println("------> NEO4J PASSWORD: " + password);
            System.out.println("-------------> PROBLEM: " + problem);
            System.out.println("--------> RETURN QUEUE: " + eval_queue);
            System.out.println("--------> VASSAR QUEUE: " + vassar_queue_url);
            System.out.println("----------------------------------------------------\n");
            // App.sleep(3);


            if(test_reliability){
                Test.reliabilityCalculation();
                System.exit(0);
            }


//  ____        _ _     _
// |  _ \      (_) |   | |
// | |_) |_   _ _| | __| |
// |  _ <| | | | | |/ _` |
// | |_) | |_| | | | (_| |
// |____/ \__,_|_|_|\__,_|


            // 1. Build SQS Client
            final SqsClient sqsClient = App.createSqsClient(localstackEndpoint, Region.US_EAST_2);

            // 1.1 Create / Purge return queue
            String eval_queue_url = App.createQueue(sqsClient, eval_queue);
            App.purgeQueue(sqsClient, eval_queue_url);

            // EVALUATION TESTING
            if (test_eval) {
                Consumer.testEvalMessage(sqsClient, eval_queue_url, vassar_queue_url);
                System.exit(0);
            }

            if (test_eval2) {
                Consumer.testEvaluation(sqsClient, vassar_queue_url);
                System.exit(0);
            }




            // 2. Build Neo4j Database Client
            DatabaseClient client = new DatabaseClient.Builder(uri)
                    .setCredentials(user, password)
                    .setFormulation(problem)
                    .build();

            // 3. Build Graph Object
            Graph graph = new Graph.Builder(client, "EOSS", problem, reset_nodes, reset_graphs)
                    .setMutationType(mutation_type)
                    .indexGraph(graph_type)
                    .buildTopologicalOrdering()
                    .projectGraph()
                    .build();



            // DECISION TESTING
            if(test_down_selecting){
                graph.testDownSelecting();
            }
            if(test_partitioning){
                graph.testPartitioning();
            }
            if(test_permuting){
                graph.testPermuting();
            }
            if(test_assigning){
                graph.testAssigning();
            }
            if(test_standard_form){
                graph.testStandardForm();
            }
            if(test_connecting){
                graph.testConnecting();
            }

//  ______                                      _   _
// |  ____|                                    | | (_)
// | |__   _ __  _   _ _ __ ___   ___ _ __ __ _| |_ _  ___  _ __
// |  __| | '_ \| | | | '_ ` _ \ / _ \ '__/ _` | __| |/ _ \| '_ \
// | |____| | | | |_| | | | | | |  __/ | | (_| | |_| | (_) | | | |
// |______|_| |_|\__,_|_| |_| |_|\___|_|  \__,_|\__|_|\___/|_| |_|

            if(enumeration){
                HashMap<Integer, ArrayList<Double>> scores = App.enumerateAndEvaluate(sqsClient, graph, eval_queue_url, vassar_queue_url);
            }
            if(enumeration_gnc){
                App.enumerateAndEvaluateGNC(graph, sqsClient, vassar_queue_url);
            }
            if(enumeration_selecting){
                JsonArray dependency = Smap.getSelectingEnumerationDependency();
                ArrayList<JsonArray> designs = graph.enumerateDesignSpace(dependency, "Instrument Selection");
                App.evaluateNodeEnumeration(designs, sqsClient, eval_queue_url, vassar_queue_url, graph, "SELECTING-DECISION");
            }
            if(enumeration_partitioning){
                JsonArray dependency = Smap.getPartitioningEnumerationDependency();
                ArrayList<JsonArray> designs = graph.enumerateDesignSpace(dependency, "Instrument Partitioning");
                App.evaluateNodeEnumeration(designs, sqsClient, eval_queue_url, vassar_queue_url, graph, "PARTITIONING-DECISION");
            }
            if(enumeration_permuting){

            }


//  _   _ _____   _____ __  __
// | \ | |  __ \ / ____|  \/  |
// |  \| | |  | | (___ | \  / |
// | . ` | |  | |\___ \| |\/| |
// | |\  | |__| |____) | |  | |
// |_| \_|_____/|_____/|_|  |_|


            if(NDSM){
                Consumer.computeNDSM_Message(sqsClient, vassar_queue_url);
            }

            if(ContinuityMatrix){
                Consumer.computeContinuityMatrix_Message(sqsClient, vassar_queue_url);
            }


//  __  __  ____  ______
// |  \/  |/ __ \|  ____|   /\
// | \  / | |  | | |__     /  \
// | |\/| | |  | |  __|   / /\ \
// | |  | | |__| | |____ / ____ \
// |_|  |_|\____/|______/_/    \_\

            int max_evals = 1000;
            double mutation_probability = 0.25;
            double crossover_probability = 1;
            int initial_pop_size = 30;

            // - MUTATION ARRAY PROBABILITIES
            ArrayList<Double> mutation_probabilities = new ArrayList<>();

            // - GN&C
//            mutation_probabilities.add(1.0); // ROOT
//            mutation_probabilities.add(0.2); // - DOWN SELECTION
//            mutation_probabilities.add(0.2); // - DOWN SELECTION
//            mutation_probabilities.add(0.2); // - DOWN SELECTION
//            mutation_probabilities.add(0.2); // - STANDARD FORM
//            mutation_probabilities.add(0.2); // - STANDARD FORM
//            mutation_probabilities.add(0.2); // - STANDARD FORM
//            mutation_probabilities.add(0.2); // - ASSIGNING
//            mutation_probabilities.add(0.2); // - ASSIGNING
//            mutation_probabilities.add(1.0); // DESIGN

            // - EOSS
            mutation_probabilities.add(1.0); // ROOT
            mutation_probabilities.add(0.6); // - DOWN SELECTION
            mutation_probabilities.add(0.7); // - PARTITIONING
            mutation_probabilities.add(0.5); // - PERMUTING
            mutation_probabilities.add(1.0); // DESIGN




            if (moea_full) {

                // 4. Build MOEA Algorithm
                Algorithm ga = new Algorithm.Builder(graph, sqsClient)
                        .setNumObjectives(3)
                        .setID("test_add_ga")
                        .setEvalQueueUrl(eval_queue_url)
                        .setMutationArrayProbabilities(mutation_probabilities)
                        .setProperties(max_evals, initial_pop_size, crossover_probability, mutation_probability)
                        .setRunNumber(run_number)
                        .build();

                // 4.1 Start MOEA
                Thread moea_thread = new Thread(ga);
                moea_thread.start();

                // 4.2 JOIN MOEA THREAD
                try {
                    moea_thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Solving graph.formulations individually
            if (moea_selecting){
                Algorithm ga = new Algorithm.Builder(graph, sqsClient)
                        .setTargetNode("Instrument Selection")
                        .setTargetDependency(Decadal.getSelectingEnumerationDependency())
                        .setNumObjectives(3)
                        .setID("test_add_ga")
                        .setEvalQueueUrl(eval_queue_url)
                        .setProperties(max_evals, initial_pop_size, crossover_probability, mutation_probability)
                        .build();

                // 4.1 Start MOEA
                Thread moea_thread = new Thread(ga);
                moea_thread.start();

                // 4.2 JOIN MOEA THREAD
                try {
                    moea_thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (moea_partitioning){
                Algorithm ga = new Algorithm.Builder(graph, sqsClient)
                        .setTargetNode("Instrument Partitioning")
                        .setTargetDependency(Decadal.getPartitioningEnumerationDependency())
                        .setNumObjectives(3)
                        .setID("test_add_ga")
                        .setEvalQueueUrl(eval_queue_url)
                        .setProperties(max_evals, initial_pop_size, crossover_probability, mutation_probability)
                        .build();

                // 4.1 Start MOEA
                Thread moea_thread = new Thread(ga);
                moea_thread.start();

                // 4.2 JOIN MOEA THREAD
                try {
                    moea_thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (moea_permuting){
                Algorithm ga = new Algorithm.Builder(graph, sqsClient)
                        .setTargetNode("Satellite Scheduling")
                        .setTargetDependency(Smap.getPermutingEnumerationDependency())
                        .setNumObjectives(3)
                        .setID("test_add_ga")
                        .setEvalQueueUrl(eval_queue_url)
                        .setProperties(max_evals, initial_pop_size, crossover_probability, mutation_probability)
                        .build();

                // 4.1 Start MOEA
                Thread moea_thread = new Thread(ga);
                moea_thread.start();

                // 4.2 JOIN MOEA THREAD
                try {
                    moea_thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }




        // 5. Build Consumer (for full daphne integration)
        // graph.closeDatabaseConnection();
    }








//     _    _ _______ _____ _      _____ _________     __
//    | |  | |__   __|_   _| |    |_   _|__   __\ \   / /
//    | |  | |  | |    | | | |      | |    | |   \ \_/ /
//    | |  | |  | |    | | | |      | |    | |    \   /
//    | |__| |  | |   _| |_| |____ _| |_   | |     | |
//     \____/   |_|  |_____|______|_____|  |_|     |_|





    // Defaults to region us-east-2
    public static SqsClient createSqsClient(String endpoint_override){

        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_EAST_2)
                .endpointOverride(URI.create(endpoint_override))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
        return sqsClient;
    }

    // Pass region
    public static SqsClient createSqsClient(String endpoint_override, Region region){

        SqsClient sqsClient = SqsClient.builder()
                .region(region)
                .endpointOverride(URI.create(endpoint_override))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
        return sqsClient;
    }



    public static String createQueue(SqsClient sqsClient, String queue_name){
        Map<String, String> queueParams = new HashMap<>();
        queueParams.put("type", "add_eval_return");

        CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                .queueName(queue_name)
                .tags(queueParams)
                .build();
        sqsClient.createQueue(createQueueRequest);

        GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queue_name).build());
        String queue_url = getQueueUrlResponse.queueUrl();
        return queue_url;
    }

    public static boolean purgeQueue(SqsClient sqsClient, String queue_url){
        System.out.println("---> PURGE QUEUE: " + queue_url);
        // App.sleep(2);

        PurgeQueueRequest purgeQueueRequest = PurgeQueueRequest.builder()
                .queueUrl(queue_url)
                .build();
        sqsClient.purgeQueue(purgeQueueRequest);
        return true;
    }

    public static String getSaltString(int length) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    // ---> SLEEP
    public static void sleep(int seconds){
        try                            { TimeUnit.SECONDS.sleep(seconds); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }






    public static void enumerateAndEvaluateGNC(Graph graph, SqsClient client, String eval_queue){

        // 1. Enumerate Design Space
        graph.enumerateDesignSpace();

        // 2. Get all designs
        ArrayList<String> designs = graph.getEnumeratedDesignStrings();

        // 3. Build GNC_Evaluator
        GNC_Evaluator gnc_evaluator = new GNC_Evaluator((5/3), 9, 1, 10);

        // 4. Initialize non-dominated population
        NondominatedPopulation pop = new NondominatedPopulation();

        // 5. Evaluate designs
        int progress_counter = 0;
        for(String design: designs){

            // Evaluate
            ArrayList<Double> results = gnc_evaluator.evaluate(design);
            double reliability = results.get(0);
            double mass = results.get(1);

            // Solution
            InfoSolution arch = new InfoSolution(1, 2, design);
            arch.setObjective(0, -reliability);
            arch.setObjective(1, mass);

            BinaryIntegerVariable var = new BinaryIntegerVariable(progress_counter, 0, 1000000);
            arch.setVariable(0, var);
            pop.add(arch);

            System.out.println("--> EVALUATION " + progress_counter);
            progress_counter++;
        }

        // 6. Initialize problem
        ADDProblem problem = new ADDProblem(graph, 2, client, eval_queue);

        Analyzer analyzer = new Analyzer()
                .withProblem(problem)
                .withIdealPoint(-10.1, -0.1)
                .withReferencePoint(0, 100)
                .includeHypervolume();

        analyzer.add("population", pop);
        analyzer.printAnalysis();
    }





    public static HashMap<Integer, ArrayList<Double>> enumerateAndEvaluate(SqsClient client, Graph graph, String eval_queue_url, String vassar_queue){
        // ArrayList<Double>: 1 - cost, 2 - science, 3 - data continuity
        HashMap<Integer, ArrayList<Double>> design_scores = new HashMap<>();

        // Solutions
        ArrayList<Solution> solutions = new ArrayList<>();

        // Problem
        ADDProblem problem = new ADDProblem(graph, 3, client, eval_queue_url);

        NondominatedPopulation pop = new NondominatedPopulation();

        // 1. Enumerate Design Space
        graph.enumerateDesignSpace();

        // 2. Get all designs
        ArrayList<String> designs = graph.getEnumeratedDesignStrings();

        // 3. Iterate over list and get designs
        ArrayList<String> UUID_list = new ArrayList<>();
        for(String design: designs) {
            String UUID = java.util.UUID.randomUUID().toString();
            UUID_list.add(UUID);
            Map<String, MessageAttributeValue> messageAttributes = Design.getEvalMessageAttributes(eval_queue_url, design, UUID);

            client.sendMessage(SendMessageRequest.builder()
                    .queueUrl(vassar_queue)
                    .messageBody("")
                    .messageAttributes(messageAttributes)
                    .delaySeconds(0)
                    .build());

        }

        int result_counter = 0;
        int counter = 0;

        // --> EVALUATE
        for(String UUID: UUID_list){

            // GET EVAL RESULTS
            HashMap<String, String> results = Consumer.getReturnMessage(client, eval_queue_url, UUID, 1);
            while(results.isEmpty()){
                results = Consumer.getReturnMessage(client, eval_queue_url, UUID, 1);
            }
            System.out.println("-----> RESULT RECEIVED: " + result_counter);
            result_counter++;

            // PROCESS RESULTS
            double science            = Double.parseDouble(results.get("science"));
            double cost               = Double.parseDouble(results.get("cost"));
            double data_continuity    = Double.parseDouble(results.get("data_continuity"));
            String design_str         = results.get("design");

            ArrayList<Double> scores = new ArrayList<>();
            scores.add(cost);
            scores.add(science);
            scores.add(data_continuity);

            design_scores.put(counter, scores);
            counter++;

            // CREATE SOLUTION
            InfoSolution arch = new InfoSolution(1, 3, design_str);
            arch.setObjective(0, -science);
            arch.setObjective(1, cost);
            arch.setObjective(2, data_continuity);

            BinaryIntegerVariable var = new BinaryIntegerVariable(counter, 0, 10000);
            arch.setVariable(0, var);
            solutions.add(arch);
            pop.add(arch);


//            if(counter == 50){
//                break;
//            }
        }




        // --> RECORD RESULTS

        Analyzer analyzer = new Analyzer()
                .withProblem(problem)
                .withIdealPoint(-1.1, -0.1, -0.1)
                .withReferencePoint(0, 10000, 5000)
                .includeHypervolume();

        analyzer.add("population", pop);
        analyzer.printAnalysis();

        Iterator<Solution> itr = pop.iterator();

        // --> GLOBAL METRICS INIT
        String results_file = "non_dominated.csv";
        String[] header = { "science", "cost", "data_continuity" };
        Results.writeSMAP_FF_HV(header, false);

        // ITERATE OVER RESULTS
        while(itr.hasNext()){

            Solution soln = itr.next();
            double science = -(soln.getObjective(0)); // Science
            double cost = (soln.getObjective(1)); // Cost
            double data_continuity = (soln.getObjective(2)); // Data Continuity

            // --> GLOBAL METRICS
            String[] res = { Double.toString(science), Double.toString(cost), Double.toString(data_continuity) };
            Results.writeSMAP_FF_HV(res, true);

        }

        Results.writeSMAP_FF_Pop(pop);

        return design_scores;
    }




    public static void solveProblemSequentially(){

        // 1. Fully enumerate selecting problem and get non-dominated population S

        // 2. For each design x in population S, fully enumerate the partitioning problem and get non-dominated population xPA

        // 3. Combine xPA into one non-dominated population PA

        // 4. For each design x in PA, fully enumerate the permuting problem and get non-dominated population xPE

        // 5. Combine xPE into one non-dominated population PE

        // 6. Compare the designs in PE to designs from the non-dominated population found by the ADD GA
        //    -- Look at any designs found by the ADD GA that dominate designs in PE, evaluate these designs for couplings that might have prevent them from being found by the sequential method




    }



    public static void evaluateNodeEnumeration(ArrayList<JsonArray> designs_json, SqsClient client, String eval_queue_url, String vassar_queue, Graph graph, String eval_type){
        Gson gson = Smap.getGson(true);

        // 1. Get design strings
        ArrayList<String> designs = new ArrayList<>();
        for(JsonArray design: designs_json){
            designs.add(gson.toJson(design));
        }

        // 2. Send all evaluation messages
        ArrayList<String> UUID_list = new ArrayList<>();
        for(String design: designs) {
            String UUID = java.util.UUID.randomUUID().toString();
            UUID_list.add(UUID);
            Map<String, MessageAttributeValue> messageAttributes = Design.getNodeEvalMessageAttributes(eval_queue_url, design, UUID, eval_type);

            client.sendMessage(SendMessageRequest.builder()
                    .queueUrl(vassar_queue)
                    .messageBody("")
                    .messageAttributes(messageAttributes)
                    .delaySeconds(0)
                    .build());
        }

        // 3. Evaluate
        int result_counter = 0;
        int counter = 0;
        NondominatedPopulation pop = new NondominatedPopulation();
        for(String UUID: UUID_list){

            // GET EVAL RESULTS
            HashMap<String, String> results = Consumer.getReturnMessage(client, eval_queue_url, UUID, 1);
            while(results.isEmpty()){
                results = Consumer.getReturnMessage(client, eval_queue_url, UUID, 1);
            }

            System.out.println("-----> RESULT RECEIVED: " + result_counter);
            result_counter++;

            // PROCESS RESULTS
            double science            = Double.parseDouble(results.get("science"));
            double cost               = Double.parseDouble(results.get("cost"));
            double data_continuity    = Double.parseDouble(results.get("data_continuity"));
            String design_str         = results.get("design");

            // CREATE SOLUTION
            InfoSolution arch = new InfoSolution(1, 3, design_str);
            arch.setObjective(0, -science);
            arch.setObjective(1, cost);
            arch.setObjective(2, data_continuity);

            BinaryIntegerVariable var = new BinaryIntegerVariable(counter, 0, 10000);
            arch.setVariable(0, var);
            pop.add(arch);

        }

        // Problem
        ADDProblem problem = new ADDProblem(graph, 3, client, eval_queue_url);

        // ANALYZE RESULTS
        Analyzer analyzer = new Analyzer()
                .withProblem(problem)
                .withIdealPoint(-1.1, -0.1, -0.1)
                .withReferencePoint(0, 10000, 2000)
                .includeHypervolume();
        analyzer.add("population", pop);
        analyzer.printAnalysis();


        // RECORD METRICS
        String[] header = { "science", "cost", "data_continuity" };
        if(eval_type.equals("SELECTING-DECISION")){
            Results.writeSMAP_SELECTING_HV(header, false, "selecting");
        }
        else if(eval_type.equals("PARTITIONING-DECISION")){
            Results.writeSMAP_SELECTING_HV(header, false, "partitioning");
        }


        Iterator<Solution> itr = pop.iterator();
        while(itr.hasNext()){

            Solution soln = itr.next();
            double science = -(soln.getObjective(0)); // Science
            double cost = (soln.getObjective(1)); // Cost
            double data_continuity = (soln.getObjective(2)); // Data Continuity

            // --> GLOBAL METRICS
            String[] res = { Double.toString(science), Double.toString(cost), Double.toString(data_continuity) };
            if(eval_type.equals("SELECTING-DECISION")){
                Results.writeSMAP_SELECTING_HV(res, true, "selecting");
            }
            else if(eval_type.equals("PARTITIONING-DECISION")){
                Results.writeSMAP_SELECTING_HV(res, true, "partitioning");
            }


        }


        if(eval_type.equals("SELECTING-DECISION")){
            Results.writeSMAP_SELECTING_POP(pop, "selecting");
        }
        else if(eval_type.equals("PARTITIONING-DECISION")){
            Results.writeSMAP_SELECTING_POP(pop, "partitioning");
        }
    }


    public static void createResultsFiles(){
        String[] header = { "HV_START", "HV_END" };

        ArrayList<Integer> record_indicies = new ArrayList<>();
        record_indicies.add(50);
        record_indicies.add(100);
        record_indicies.add(150);
        record_indicies.add(200);
        record_indicies.add(300);
        record_indicies.add(500);
        record_indicies.add(1000);
        record_indicies.add(2000);
        for(Integer idx: record_indicies){
            String file_name = "results_20_"+idx+".csv";

            String directory = Files.decadalAddDir + "/" + file_name;
            try{
                FileWriter outputfile = new FileWriter(directory, false);
                CSVWriter writer = new CSVWriter(outputfile);
                writer.writeNext(header);
                writer.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }







}
