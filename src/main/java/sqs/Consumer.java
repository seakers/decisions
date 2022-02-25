package sqs;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import graph.formulations.Decadal;
import graph.Graph;
import moea.Algorithm;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Consumer implements Runnable{

    private boolean        debug;
    private boolean        running;
    private SqsClient      sqsClient;
    private Algorithm      moea;
    private Graph          graph;
    private String         queueUrl;
    private String         privateQueueUrl;


    public static class Builder{

        private boolean        debug;
        private SqsClient      sqsClient;
        private Algorithm      moea;
        private Graph          graph;
        private String         queueUrl;
        private String         privateQueueUrl;

        public Builder(SqsClient sqsClient){
            this.sqsClient = sqsClient;
            this.debug = false;
        }


        public Builder setQueueUrl(String queueUrl) {
            this.queueUrl = queueUrl;
            return this;
        }

        public Builder setMOEA(Algorithm moea) {
            this.moea = moea;
            return this;
        }

        public Builder setGraph(Graph graph) {
            this.graph = graph;
            return this;
        }

        public Builder setPrivateQueueUrl(String privateQueueUrl) {
            this.privateQueueUrl = privateQueueUrl;
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Consumer build(){
            Consumer build        = new Consumer();
            build.sqsClient       = this.sqsClient;
            build.debug           = this.debug;
            build.queueUrl        = this.queueUrl;
            build.privateQueueUrl = this.privateQueueUrl;
            build.graph           = this.graph;
            build.moea            = this.moea;
            build.running         = true;
            return build;
        }


    }


    public void run(){




    }








    public static HashMap<String, String> getReturnMessage(SqsClient sqs, String url, String UUID, int waitTime){
        HashMap<String, String> results = new HashMap<>();
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(url)
                .waitTimeSeconds(waitTime)
                .maxNumberOfMessages(10)
                .attributeNames(QueueAttributeName.ALL)
                .messageAttributeNames("All")
                .visibilityTimeout(0)
                .build();
        List<Message> messages = sqs.receiveMessage(receiveMessageRequest).messages();
        for(Message message: messages){
            HashMap<String, String> msg_attributes = Consumer.processMessage(message);
            if(msg_attributes.containsKey("UUID")){
                if(UUID.contains(msg_attributes.get("UUID")) || msg_attributes.get("UUID").contains(UUID)){

                    // DELETE ALL MESSAGES
                    Consumer.deleteMessage(sqs, message, url);

                    // RETURN MESSAGE ATTRIBUTES
                    System.out.println("---> UUID FOUND !!!");
                    return msg_attributes;
                }
                else{
                    //System.out.println("----> UUID DOES NOT MATCH");
                    //System.out.println(msg_attributes.get("UUID"));
                    //System.out.println(UUID);
                }
            }
        }
        return results;
    }


    public static HashMap<String, String> processMessage(Message msg){
        HashMap<String, String> contents = new HashMap<>();
        contents.put("body", msg.body());
        // System.out.println("\n--------------- SQS MESSAGE ---------------");
        // System.out.println("--------> BODY: " + msg.body());
        for(String key: msg.messageAttributes().keySet()){
            contents.put(key, msg.messageAttributes().get(key).stringValue());
            // System.out.println("---> ATTRIBUTE: " + key + " - " + msg.messageAttributes().get(key).stringValue());
        }
        // System.out.println("-------------------------------------------\n");
        // App.sleep(2);
        return contents;
    }



    public static void deleteMessages(SqsClient sqs, List<Message> messages, String url){
        for (Message message : messages) {
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(url)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqs.deleteMessage(deleteMessageRequest);
        }
    }

    public static void deleteMessage(SqsClient sqs, Message message, String url){
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(url)
                .receiptHandle(message.receiptHandle())
                .build();
        sqs.deleteMessage(deleteMessageRequest);
    }










    public static void testEvalMessage(SqsClient client, String eval_queue_url, String vassar_queue){
        String design_string = "[\n" +
                "  {\n" +
                "    \"active\": true,\n" +
                "    \"id\": 1,\n" +
                "    \"type\": \"list\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"active\": true,\n" +
                "        \"type\": \"item\",\n" +
                "        \"id\": \"2\",\n" +
                "        \"name\": \"IR-Spectrometer\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"active\": true,\n" +
                "    \"id\": 0,\n" +
                "    \"type\": \"list\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"active\": true,\n" +
                "        \"type\": \"item\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"name\": \"EARTHCARE-CPR\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]\n";




        String design_string2 = "[\n" +
                "  {\n" +
                "    \"active\": true,\n" +
                "    \"id\": 0,\n" +
                "    \"type\": \"list\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"active\": true,\n" +
                "        \"type\": \"item\",\n" +
                "        \"id\": \"1\",\n" +
                "        \"name\": \"BIOMASS\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"active\": true,\n" +
                "    \"id\": 1,\n" +
                "    \"type\": \"list\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"active\": true,\n" +
                "        \"type\": \"item\",\n" +
                "        \"id\": \"2\",\n" +
                "        \"name\": \"SMAP_MWR\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]\n";



        String design_string1 = "[\n" +
                "  {\n" +
                "    \"active\": true,\n" +
                "    \"id\": 1,\n" +
                "    \"type\": \"list\",\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"active\": true,\n" +
                "        \"type\": \"item\",\n" +
                "        \"id\": \"2\",\n" +
                "        \"name\": \"ICI\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]\n";



        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String decadal_test_design = gson.toJson(Decadal.getAddTestDesign2007());


        System.out.println(vassar_queue);
        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("msgType",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("add")
                        .build()
        );
        messageAttributes.put("input",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(decadal_test_design)
                        .build()
        );
        messageAttributes.put("rQueue",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(eval_queue_url)
                        .build()
        );
        messageAttributes.put("UUID",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("10101")
                        .build()
        );
        try{
            client.sendMessage(SendMessageRequest.builder()
                    .queueUrl(vassar_queue)
                    .messageBody("")
                    .messageAttributes(messageAttributes)
                    .delaySeconds(0)
                    .build());
        }
        catch(SqsException e){
            e.printStackTrace();
            System.out.println(e.toString());
        }

    }

    public static void computeNDSM_Message(SqsClient client, String vassar_queue){


        System.out.println(vassar_queue);
        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("msgType",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("NDSM")
                        .build()
        );

        try{
            client.sendMessage(SendMessageRequest.builder()
                    .queueUrl(vassar_queue)
                    .messageBody("")
                    .messageAttributes(messageAttributes)
                    .delaySeconds(0)
                    .build());
        }
        catch(SqsException e){
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }

    public static void computeContinuityMatrix_Message(SqsClient client, String vassar_queue){

        System.out.println(vassar_queue);
        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("msgType",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("ContinuityMatrix")
                        .build()
        );

        try{
            client.sendMessage(SendMessageRequest.builder()
                    .queueUrl(vassar_queue)
                    .messageBody("")
                    .messageAttributes(messageAttributes)
                    .delaySeconds(0)
                    .build());
        }
        catch(SqsException e){
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }


    public static void testEvaluation(SqsClient client, String vassar_queue){

        System.out.println(vassar_queue);
        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("msgType",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("TEST-EVAL")
                        .build()
        );

        try{
            client.sendMessage(SendMessageRequest.builder()
                    .queueUrl(vassar_queue)
                    .messageBody("")
                    .messageAttributes(messageAttributes)
                    .delaySeconds(0)
                    .build());
        }
        catch(SqsException e){
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }












}
