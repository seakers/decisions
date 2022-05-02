package sqs;


import graph.Graph;
import graph.neo4j.DatabaseClient;
import moea.Algorithm;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;
import java.util.HashMap;

public class ConsumerProd implements Runnable{

    private enum State {
        WAITING_FOR_USER, READY
    }

    private boolean        debug;
    private boolean        running;
    private SqsClient      sqsClient;
    private Algorithm      moea;
    private Graph          graph;
    private State          currentState = State.WAITING_FOR_USER;


    public static class Builder{

        private HashMap<String, String> env;

        private boolean        debug;
        private SqsClient      sqsClient;
        private Algorithm      moea;
        private Graph          graph;

        public Builder(HashMap<String, String> env){
            this.debug = false;
            this.moea = null;
            this.graph = null;
            this.env = env;
            this.sqsClient = SqsClient.builder()
                    .region(Region.US_EAST_2)
                    .endpointOverride(URI.create(env.get("localstackEndpoint")))
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .build();
        }

        public DatabaseClient buildNeo4jClient(){
            return new DatabaseClient.Builder(this.env.get("uri"))
                    .setCredentials(this.env.get("user"), this.env.get("password"))
                    .setFormulation(this.env.get("formulation"))
                    .setProblem(this.env.get("problem"))
                    .build();
        }

        public Graph buildGraph(){
            DatabaseClient client = this.buildNeo4jClient();
            return new Graph.Builder(client, this.env.get("formulation"), this.env.get("problem"), true, true)
                    .setMutationType(this.env.get("mutation_type"))
                    .indexGraph(this.env.get("formulation"))
                    .buildTopologicalOrdering()
                    .projectGraph()
                    .build();
        }

        public Algorithm buildMOEA(){
            return null;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public ConsumerProd build(){
            ConsumerProd build    = new ConsumerProd();
            build.debug           = this.debug;
            build.sqsClient       = this.sqsClient;
            build.graph           = this.buildGraph();
            build.moea            = this.buildMOEA();
            build.running         = true;
            return build;
        }
    }


    /*
        Message Types
        - 1. Load New Formulation
        - 2. Configure MOEA
        - 3. Run MOEA
        - 4. Stop MOEA
     */

    public void run(){
        System.out.println("--> RUNNING CONSUMER");



        try{
//            this.graph.generateRandomDesign();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }










        while(this.running){
            this.running = false;
        }
    }
}
