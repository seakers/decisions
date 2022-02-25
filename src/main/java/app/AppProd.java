package app;

import graph.Graph;
import graph.neo4j.DatabaseClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import sqs.ConsumerProd;

import java.util.HashMap;

/*
    App2
    - The purpose of this main method is to read an ADD straight from the database
 */

public class AppProd {

    public static void main(String[] args) {



    //     ______               _                                             _
    //    |  ____|             (_)                                           | |
    //    | |__    _ __ __   __ _  _ __  ___   _ __   _ __ ___    ___  _ __  | |_
    //    |  __|  | '_ \\ \ / /| || '__|/ _ \ | '_ \ | '_ ` _ \  / _ \| '_ \ | __|
    //    | |____ | | | |\ V / | || |  | (_) || | | || | | | | ||  __/| | | || |_
    //    |______||_| |_| \_/  |_||_|   \___/ |_| |_||_| |_| |_| \___||_| |_| \__|


        // --> 1. Get environment variables
        String localstackEndpoint = System.getenv("AWS_STACK_ENDPOINT");
        String uri                = System.getenv("NEO4J_URI");
        String user               = System.getenv("NEO4J_USER");
        String password           = System.getenv("NEO4J_PASSWORD");
        String problem            = System.getenv("PROBLEM");
        String formulation        = System.getenv("FORMULATION");
        String eval_queue         = App.getSaltString(15);
        String vassar_queue_url   = System.getenv("VASSAR_QUEUE");

        // --> 2. Override variables as necessary
        formulation = "EOSS";
        problem     = "SMAP";

        // --> 3. Place variables into hashmap
        HashMap<String, String> env = new HashMap<>();
        env.put("localstackEndpoint", localstackEndpoint);
        env.put("uri", uri);
        env.put("user", user);
        env.put("password", password);
        env.put("eval_queue", eval_queue);
        env.put("vassar_queue_url", vassar_queue_url);
        env.put("problem", problem);
        env.put("formulation", formulation);
        env.put("mutation_type", "DISJOINT");

        // --> 4. Print env for validation
        AppProd.printEnv(env);



    //      _____
    //     / ____|
    //    | |      ___   _ __   ___  _   _  _ __ ___    ___  _ __
    //    | |     / _ \ | '_ \ / __|| | | || '_ ` _ \  / _ \| '__|
    //    | |____| (_) || | | |\__ \| |_| || | | | | ||  __/| |
    //     \_____|\___/ |_| |_||___/ \__,_||_| |_| |_| \___||_|


        // --> 1. Build Consumer
        ConsumerProd consumer = new ConsumerProd.Builder(env).build();

        // --> 2. Run Consumer
        try {
            consumer.run();
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            System.out.println("--> FINISHED RUNNING CONSUMER");
        }


    }



    public static void printEnv(HashMap<String, String> env){
        System.out.println("\n------------------ ADD ENV ------------------");
        for(String key: env.keySet()){
            System.out.println("---> " + key + ": " + env.get(key));
        }
        System.out.println("----------------------------------------------------\n");
    }

}
