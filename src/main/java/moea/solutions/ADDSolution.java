package moea.solutions;

import com.google.gson.JsonArray;
import graph.Graph;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryIntegerVariable;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ADDSolution extends Solution {

    public final int design_idx;
    public Graph     graph;
    public boolean   alreadyEvaluated;
    public String    ID;

    public String    design_str;

    // --> SOLUTION FROM A RANDOMLY CREATED DESIGN
    public ADDSolution(Graph graph, int num_objectives){
        super(1, num_objectives, 0);

        int generated_design_id = -1;
        try{
            generated_design_id = graph.generateRandomDesign();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        this.design_idx       = generated_design_id;
        this.alreadyEvaluated = false;
        this.ID               = UUID.randomUUID().toString();
        this.graph            = graph;
        this.design_str = "";

        BinaryIntegerVariable var = new BinaryIntegerVariable(generated_design_id, 0, 10000);
        this.setVariable(0, var);
    }
    // -> Targeted run
    public ADDSolution(Graph graph, int num_objectives, String node_name, JsonArray dependency){
        super(1, num_objectives, 0);

        int generated_design_id = -1;
        try{
            generated_design_id = graph.generateRandomDesign(dependency, node_name);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        this.design_idx       = generated_design_id;
        this.alreadyEvaluated = false;
        this.ID               = UUID.randomUUID().toString();
        this.graph            = graph;
        this.design_str = "";

        BinaryIntegerVariable var = new BinaryIntegerVariable(generated_design_id, 0, 10000);
        this.setVariable(0, var);
    }

    // --> SOLUTION FROM A PRE-EXISTING DESIGN
    public ADDSolution(Graph graph, int num_objectives, int design_idx){
        super(1, num_objectives, 0);
        this.graph            = graph;
        this.design_idx       = design_idx;
        this.alreadyEvaluated = false;
        this.design_str = "";
        this.ID = UUID.randomUUID().toString();

        BinaryIntegerVariable var = new BinaryIntegerVariable(design_idx, 0, 10000);
        this.setVariable(0, var);
    }


    // COPY CONSTRUCTOR CALLED IN CROSSOVER
    protected ADDSolution(Solution solution){
        super(solution);


        ADDSolution design    = (ADDSolution) solution;
        this.design_idx       = design.design_idx;
        this.graph            = design.graph;
        this.alreadyEvaluated = design.alreadyEvaluated;
        this.ID               = design.ID;
        this.design_str       = design.design_str;
    }

    public void setAlreadyEvaluated(boolean alreadyEvaluated){
        this.alreadyEvaluated = alreadyEvaluated;
    }

    public boolean getAlreadyEvaluated(){
        return this.alreadyEvaluated;
    }


    // SINGLE NODE
    public Map<String, MessageAttributeValue> getEvalMessageAttributes(String eval_queue_url, String node_name){
        String message_type  = node_name;
        String design_string = this.graph.getDesignString(this.design_idx, node_name);

        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("msgType",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(message_type)
                        .build()
        );
        messageAttributes.put("input",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(design_string)
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
                        .stringValue(this.ID)
                        .build()
        );

        return messageAttributes;
    }

    // ENTIRE ADD
    public Map<String, MessageAttributeValue> getEvalMessageAttributes(String eval_queue_url){
        String message_type  = "add";
        String design_string = this.graph.getDesignString(this.design_idx);

        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("msgType",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(message_type)
                        .build()
        );
        messageAttributes.put("input",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(design_string)
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
                        .stringValue(this.ID)
                        .build()
        );

        return messageAttributes;
    }


    public String getDesignString(){
        return this.graph.getDesignString(this.design_idx);
    }




    public void printDesign(){
        System.out.println("---> DESIGN INDEX: " + this.design_idx);
        System.out.println(this.graph.getDesignString(this.design_idx));
    }

    public void printDesign(String node_name){
        System.out.println("---> DESIGN INDEX: " + this.design_idx);
        this.graph.printDesign(this.design_idx, node_name);
        // System.out.println(this.graph.getDesignString(this.design_idx, node_name));
    }



    public void commitObjectiveScore(String objective_name, double objective_value){
        this.graph.commitDesignObjectiveScore(objective_name, objective_value, this.design_idx);
    }





    @Override
    public String toString(){
        return Integer.toString(this.design_idx);
    }

    @Override
    public int hashCode(){
        return this.design_idx;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ADDSolution other = (ADDSolution) obj;
        if(this.design_idx == ((ADDSolution) obj).design_idx){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public Solution copy(){
        return new ADDSolution(this);
    }
}
