package moea.problems;

import app.App;
import app.Files;
import com.google.gson.*;
import evaluation.GNC_Evaluator;
import evaluation.GNC_Evaluator2;
import graph.Graph;
import moea.solutions.ADDSolution;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ADDProblem extends AbstractProblem {

    public Graph     graph;
    public int       numObjectives;
    public SqsClient sqs;
    public String    eval_queue_url;
    public String    vassar_queue;

    private boolean   targeted_run;
    private String    target_node;
    private JsonArray target_dependency;

    private GNC_Evaluator gnc_evaluator;

    private JsonObject eval_metrics;
    private JsonObject initial_pop_metrics;

    private int num_evals;


    public ADDProblem(Graph graph, int numObjectives, SqsClient sqs, String eval_queue_url){
        super(1, numObjectives);
        this.graph = graph;
        this.numObjectives = numObjectives;
        this.sqs = sqs;
        this.eval_queue_url = eval_queue_url;
        this.vassar_queue = System.getenv("VASSAR_QUEUE");
        this.targeted_run = false;
        this.gnc_evaluator = new GNC_Evaluator((5/3), 9, 1, 10);

        this.eval_metrics = this.init_eval_metrics();
        this.initial_pop_metrics = this.init_eval_metrics();
        this.num_evals = 0;
    }

    public ADDProblem(Graph graph, int numObjectives, SqsClient sqs, String eval_queue_url, String target_node, JsonArray target_dependency){
        super(1, numObjectives);
        this.graph = graph;
        this.numObjectives = numObjectives;
        this.sqs = sqs;
        this.eval_queue_url = eval_queue_url;
        this.vassar_queue = System.getenv("VASSAR_QUEUE");
        this.targeted_run = true;
        this.target_node = target_node;
        this.target_dependency = target_dependency;
        this.gnc_evaluator = new GNC_Evaluator((5/3), 9, 1, 10);

        this.eval_metrics = this.init_eval_metrics();
        this.initial_pop_metrics = this.init_eval_metrics();
        this.num_evals = 0;
    }


    public JsonObject init_eval_metrics(){
        JsonObject metrics = new JsonObject();

        metrics.addProperty("1 instrument", 0);
        metrics.addProperty("2 instrument", 0);
        metrics.addProperty("3 instrument", 0);
        metrics.addProperty("4 instrument", 0);
        metrics.addProperty("5 instrument", 0);

        metrics.addProperty("1 satellite", 0);
        metrics.addProperty("2 satellite", 0);
        metrics.addProperty("3 satellite", 0);
        metrics.addProperty("4 satellite", 0);
        metrics.addProperty("5 satellite", 0);

        return metrics;
    }





    @Override
    public void evaluate(Solution sltn){

        // 1. Cast to subclass
        ADDSolution arch = (ADDSolution) sltn;

        // 2. Evaluate if not evaluated
        if(!arch.getAlreadyEvaluated()){
            // evaluateArch_GNC(arch);
            evaluateArch(arch);
            this.record_eval_metrics(arch, this.eval_metrics);
            if(this.num_evals < 30){
                this.record_eval_metrics(arch, this.initial_pop_metrics);
            }


            this.num_evals++;
        }
        else{
            System.out.println("---> Architecture already evaluated!!!");
            // App.sleep(1);
        }
    }


    public void evaluateArch_GNC(ADDSolution arch){

        GNC_Evaluator2 evaluator = new GNC_Evaluator2();

        String design = arch.getDesignString();

        ArrayList<Double> results = evaluator.evaluate(design);

        double reliability = results.get(0);
        double mass = results.get(1);

        // Maximize reliability and minimize mass
        arch.setObjective(0, -reliability);
        arch.setObjective(1, mass);
        arch.design_str = arch.getDesignString();
        arch.setAlreadyEvaluated(true);
    }



    public void record_eval_metrics(ADDSolution solution, JsonObject eval_metrics){
        String design_string = solution.getDesignString();
        JsonArray array = (new JsonParser().parse(design_string).getAsJsonArray());
        int num_satellites = array.size();

        if(num_satellites == 1){
            int current_evals = eval_metrics.get("1 satellite").getAsInt() + 1;
            eval_metrics.addProperty("1 satellite", current_evals);
        }
        if(num_satellites == 2){
            int current_evals = eval_metrics.get("2 satellite").getAsInt() + 1;
            eval_metrics.addProperty("2 satellite", current_evals);
        }
        if(num_satellites == 3){
            int current_evals = eval_metrics.get("3 satellite").getAsInt() + 1;
            eval_metrics.addProperty("3 satellite", current_evals);
        }
        if(num_satellites == 4){
            int current_evals = eval_metrics.get("4 satellite").getAsInt() + 1;
            eval_metrics.addProperty("4 satellite", current_evals);
        }
        if(num_satellites == 5){
            int current_evals = eval_metrics.get("5 satellite").getAsInt() + 1;
            eval_metrics.addProperty("5 satellite", current_evals);
        }

        ArrayList<String> instruments = new ArrayList<>();
        for(int x = 0; x < num_satellites; x++){
            JsonObject sat = array.get(x).getAsJsonObject();
            JsonArray sat_insts = sat.getAsJsonArray("elements");
            for(int y = 0; y < sat_insts.size(); y++){
                String inst = sat_insts.get(y).getAsJsonObject().get("name").getAsString();
                if(!instruments.contains(inst)){
                    instruments.add(inst);
                }
            }
        }

        int num_instruments = instruments.size();

        if(num_instruments == 1){
            int current_evals = eval_metrics.get("1 instrument").getAsInt() + 1;
            eval_metrics.addProperty("1 instrument", current_evals);
        }
        if(num_instruments == 2){
            int current_evals = eval_metrics.get("2 instrument").getAsInt() + 1;
            eval_metrics.addProperty("2 instrument", current_evals);
        }
        if(num_instruments == 3){
            int current_evals = eval_metrics.get("3 instrument").getAsInt() + 1;
            eval_metrics.addProperty("3 instrument", current_evals);
        }
        if(num_instruments == 4){
            int current_evals = eval_metrics.get("4 instrument").getAsInt() + 1;
            eval_metrics.addProperty("4 instrument", current_evals);
        }
        if(num_instruments == 5){
            int current_evals = eval_metrics.get("5 instrument").getAsInt() + 1;
            eval_metrics.addProperty("5 instrument", current_evals);
        }
    }

    public void write_metrics(int run_number){
        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter(Files.get_metrics_file(run_number));
            gson.toJson(this.eval_metrics, outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void write_init_pop_metrics(int run_number){
        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter(Files.get_init_metrics_file(run_number));
            gson.toJson(this.initial_pop_metrics, outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    public void evaluateArch(ADDSolution arch){
        System.out.println("---------- EVALUATING ARCHITECTURE ----------");

        if(this.targeted_run){
            arch.printDesign(this.target_node);
        }
        else{
            arch.printDesign();
        }

        // System.exit(0);

        // App.sleep(2);

        // 1. SEND EVAL MESSAGE
        final Map<String, MessageAttributeValue> messageAttributes;
        if(this.targeted_run){
            messageAttributes = arch.getEvalMessageAttributes(this.eval_queue_url, this.target_node);
        }
        else{
            messageAttributes = arch.getEvalMessageAttributes(this.eval_queue_url);
        }
        this.sqs.sendMessage(SendMessageRequest.builder()
                .queueUrl(this.vassar_queue)
                .messageBody("")
                .messageAttributes(messageAttributes)
                .delaySeconds(0)
                .build());

        // 2. GET EVAL RESULTS
        HashMap<String, String> results = this.getReturnMessage(this.eval_queue_url, arch.ID, 1);
        while(results.isEmpty()){
            results = this.getReturnMessage(this.eval_queue_url, arch.ID, 2);
        }
        System.out.println("-----> MESSAGE RECEIVED");

        // PROCESS RESULTS
        double science            = Double.parseDouble(results.get("science"));
        double cost               = Double.parseDouble(results.get("cost"));
        double data_continuity    = Double.parseDouble(results.get("data_continuity"));
        String design_str         = results.get("design");



//        arch.commitObjectiveScore("science", science);
//        arch.commitObjectiveScore("cost", cost);
//        arch.commitObjectiveScore("data_continuity", data_continuity);

        arch.setObjective(0, -science);
        arch.setObjective(1, cost);
        arch.setObjective(2, data_continuity);
        arch.design_str = design_str;
        arch.setAlreadyEvaluated(true);
    }


    public HashMap<String, String> getReturnMessage(String url, String UUID, int waitTime){
        HashMap<String, String> results = new HashMap<>();
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(url)
                .waitTimeSeconds(waitTime)
                .maxNumberOfMessages(1)
                .attributeNames(QueueAttributeName.ALL)
                .messageAttributeNames("All")
                .build();
        List<Message> messages = this.sqs.receiveMessage(receiveMessageRequest).messages();
        for(Message message: messages){
            HashMap<String, String> msg_attributes = this.processMessage(message);
            if(msg_attributes.containsKey("UUID")){
                if(UUID.contains(msg_attributes.get("UUID")) || msg_attributes.get("UUID").contains(UUID)){

                    // DELETE ALL MESSAGES
                    this.deleteMessages(messages, url);

                    // RETURN MESSAGE ATTRIBUTES
                    return msg_attributes;
                }
                else{
                    System.out.println("----> UUID DOES NOT MATCH");
                    System.out.println(msg_attributes.get("UUID"));
                    System.out.println(UUID);
                }
            }
        }
        return results;
    }

    public HashMap<String, String> processMessage(Message msg){
        HashMap<String, String> contents = new HashMap<>();
        contents.put("body", msg.body());
        System.out.println("\n--------------- SQS MESSAGE ---------------");
        System.out.println("--------> BODY: " + msg.body());
        for(String key: msg.messageAttributes().keySet()){
            contents.put(key, msg.messageAttributes().get(key).stringValue());
            if(!key.equals("design")){
                System.out.println("---> ATTRIBUTE: " + key + " - " + msg.messageAttributes().get(key).stringValue());
            }
        }
        System.out.println("-------------------------------------------\n");
        // App.sleep(2);
        return contents;
    }

    public void deleteMessages(List<Message> messages, String url){
        for (Message message : messages) {
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(url)
                    .receiptHandle(message.receiptHandle())
                    .build();
            this.sqs.deleteMessage(deleteMessageRequest);
        }
    }


    @Override
    public Solution newSolution(){
        return new ADDSolution(this.graph, this.numObjectives);
    }
}
