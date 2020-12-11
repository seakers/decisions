package moea.problems;

import app.App;
import com.google.gson.JsonArray;
import evaluation.GNC_Evaluator;
import graph.Graph;
import moea.solutions.ADDSolution;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

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


    public ADDProblem(Graph graph, int numObjectives, SqsClient sqs, String eval_queue_url){
        super(1, numObjectives);
        this.graph = graph;
        this.numObjectives = numObjectives;
        this.sqs = sqs;
        this.eval_queue_url = eval_queue_url;
        this.vassar_queue = System.getenv("VASSAR_QUEUE");
        this.targeted_run = false;
        this.gnc_evaluator = new GNC_Evaluator((5/3), 9, 1, 10);
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
    }







    @Override
    public void evaluate(Solution sltn){

        // 1. Cast to subclass
        ADDSolution arch = (ADDSolution) sltn;

        // 2. Evaluate if not evaluated
        if(!arch.getAlreadyEvaluated()){
            evaluateArch_GNC(arch);
            // evaluateArch(arch);
        }
        else{
            System.out.println("---> Architecture already evaluated!!!");
            // App.sleep(1);
        }
    }


    public void evaluateArch_GNC(ADDSolution arch){

//        double connection_weight = (5/3);
//        double dissimiliar_component_property = 9;
//        double connection_reliability = 1;
//        double years = 10;
//        GNC_Evaluator evaluator = new GNC_Evaluator(connection_weight, dissimiliar_component_property, connection_reliability, years);

        String design = arch.getDesignString();

        ArrayList<Double> results = this.gnc_evaluator.evaluate(design);

        double reliability = results.get(0);
        double mass = results.get(1);
//        double reliability = evaluator.evaluate_reliability(design);
//        double mass        = evaluator.evaluate_mass(design);

        // Maximize reliability and minimize mass
        arch.setObjective(0, -reliability);
        arch.setObjective(1, mass);
        arch.design_str = arch.getDesignString();
        arch.setAlreadyEvaluated(true);
    }



    public void evaluateArch(ADDSolution arch){
        System.out.println("---------- EVALUATING ARCHITECTURE ----------");

        if(this.targeted_run){
            arch.printDesign(this.target_node);
        }
        else{
            arch.printDesign();
        }



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
