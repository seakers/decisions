package graph.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graph.Decision;
import org.neo4j.driver.Record;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Design extends Decision {


// __      __        _       _     _
// \ \    / /       (_)     | |   | |
//  \ \  / /_ _ _ __ _  __ _| |__ | | ___  ___
//   \ \/ / _` | '__| |/ _` | '_ \| |/ _ \/ __|
//    \  / (_| | |  | | (_| | |_) | |  __/\__ \
//     \/ \__,_|_|  |_|\__,_|_.__/|_|\___||___/






//     ____        _ _     _
//    |  _ \      (_) |   | |
//    | |_) |_   _ _| | __| | ___ _ __
//    |  _ <| | | | | |/ _` |/ _ \ '__|
//    | |_) | |_| | | | (_| |  __/ |
//    |____/ \__,_|_|_|\__,_|\___|_|


    public static class Builder extends Decision.Builder<Design.Builder>{

        public Builder(Record node) {
            super(node);
        }


        public Design build() { return new Design(this); }
    }

    protected Design(Design.Builder builder){
        super(builder);



    }


//     __  __                       _____                            _                 _
//    |  \/  |                     |  __ \                          | |               (_)
//    | \  / | ___ _ __ __ _  ___  | |  | | ___ _ __   ___ _ __   __| | ___ _ __   ___ _  ___  ___
//    | |\/| |/ _ \ '__/ _` |/ _ \ | |  | |/ _ \ '_ \ / _ \ '_ \ / _` |/ _ \ '_ \ / __| |/ _ \/ __|
//    | |  | |  __/ | | (_| |  __/ | |__| |  __/ |_) |  __/ | | | (_| |  __/ | | | (__| |  __/\__ \
//    |_|  |_|\___|_|  \__, |\___| |_____/ \___| .__/ \___|_| |_|\__,_|\___|_| |_|\___|_|\___||___/
//                      __/ |                  | |
//                     |___/                   |_|


    private JsonArray mergeLastParentDecisions(boolean print){
        JsonArray parents_merged = new JsonArray();
        for(Decision parent: this.parents){
            JsonObject dependency = parent.getLastDecision(this.node_name, this.node_type, 0);
            JsonArray  dependency_elements = dependency.get("elements").getAsJsonArray();
            Iterator dependency_iterator = dependency_elements.iterator();
            while(dependency_iterator.hasNext()){
                parents_merged.add(((JsonElement) dependency_iterator.next()).getAsJsonObject());
            }
        }
        if (print) {System.out.println("--> Merged Parents: " + this.gson.toJson(parents_merged));}
        return parents_merged;
    }



//  _____                 _                   _____            _
// |  __ \               | |                 |  __ \          (_)
// | |__) |__ _ _ __   __| | ___  _ __ ___   | |  | | ___  ___ _  __ _ _ __
// |  _  // _` | '_ \ / _` |/ _ \| '_ ` _ \  | |  | |/ _ \/ __| |/ _` | '_ \
// | | \ \ (_| | | | | (_| | (_) | | | | | | | |__| |  __/\__ \ | (_| | | | |
// |_|  \_\__,_|_| |_|\__,_|\___/|_| |_| |_| |_____/ \___||___/_|\__, |_| |_|
//                                                                __/ |
//

    @Override
    public void generateRandomDesign() throws Exception{
        this.processLastDesign();
    }


//      _____
//     / ____|
//    | |     _ __ ___  ___ ___  _____   _____ _ __
//    | |    | '__/ _ \/ __/ __|/ _ \ \ / / _ \ '__|
//    | |____| | | (_) \__ \__ \ (_) \ V /  __/ |
//     \_____|_|  \___/|___/___/\___/ \_/ \___|_|

    @Override
    public void crossoverDesigns(int papa, int mama, double mutation_probability) throws Exception{
        this.processLastDesign();
    }



//     ______                                      _   _
//    |  ____|                                    | | (_)
//    | |__   _ __  _   _ _ __ ___   ___ _ __ __ _| |_ _  ___  _ __
//    |  __| | '_ \| | | | '_ ` _ \ / _ \ '__/ _` | __| |/ _ \| '_ \
//    | |____| | | | |_| | | | | | |  __/ | | (_| | |_| | (_) | | | |
//    |______|_| |_|\__,_|_| |_| |_|\___|_|  \__,_|\__|_|\___/|_| |_|

    @Override
    public void enumerateDesignSpace(){

        if(this.parents.size() ==1){
            this.mergeEnumerationsSingle();
        }
        else{
            this.mergeEnumerationsMultiple();
        }

        System.out.println("\n\n---------- ENUMERATION DATA ----------");
        System.out.println("--> DESIGNS: " + this.enumeration_store.size());
        System.out.println("--------------------------------------\n\n");
    }

    public void mergeEnumerationsSingle(){
        Decision parent = this.parents.get(0);
        this.enumeration_store = parent.getEnumerations(this.node_name, this.node_type);
    }

    public void mergeEnumerationsMultiple(){

    }


//     _____                               _____            _
//    |  __ \                             |  __ \          (_)
//    | |__) | __ ___   ___ ___  ___ ___  | |  | | ___  ___ _  __ _ _ __
//    |  ___/ '__/ _ \ / __/ _ \/ __/ __| | |  | |/ _ \/ __| |/ _` | '_ \
//    | |   | | | (_) | (_|  __/\__ \__ \ | |__| |  __/\__ \ | (_| | | | |
//    |_|   |_|  \___/ \___\___||___/___/ |_____/ \___||___/_|\__, |_| |_|
//                                                             __/ |
//                                                            |___/

    public void processLastDesign(){
        JsonArray merged_design = this.mergeLastParentDecisions(false);
        JsonArray dependencies  = new JsonArray();
        this.indexNewDesign(dependencies, merged_design);
        this.updateFinalDesigns();
    }


    public void commitDesignScores(String objective_name, double objective_value, int design_idx){
        JsonObject design = this.decisions.get(design_idx).getAsJsonObject();
        design.getAsJsonObject("scores").addProperty(objective_name, objective_value);
        this.updateFinalDesigns();
    }

    @Override
    public String getDesignString(int idx){
        JsonObject design   = this.decisions.get(idx).getAsJsonObject();
        JsonArray  elements = design.getAsJsonArray("elements");
        return this.gson.toJson(elements);
    }

    public ArrayList<String> getEnumeratedDesignStrings(){
        ArrayList<String> all_designs = new ArrayList<>();

        for(Integer key: this.enumeration_store.keySet()){
            String design_str = this.gson.toJson(this.enumeration_store.get(key));
            all_designs.add(design_str);
        }
        return all_designs;
    }










    // STATIC
    public static Map<String, MessageAttributeValue> getEvalMessageAttributes(String eval_queue_url, String design, String UUID){
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
                        .stringValue(design)
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
                        .stringValue(UUID)
                        .build()
        );

        return messageAttributes;
    }



    public static Map<String, MessageAttributeValue> getNodeEvalMessageAttributes(String eval_queue_url, String design, String UUID, String msgType){
        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("msgType",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(msgType)
                        .build()
        );
        messageAttributes.put("input",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(design)
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
                        .stringValue(UUID)
                        .build()
        );

        return messageAttributes;
    }




    public static Map<String, MessageAttributeValue> getSelectingEvalMessageAttributes(String eval_queue_url, String design, String UUID){
        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("msgType",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("SELECTING-DECISION")
                        .build()
        );
        messageAttributes.put("input",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(design)
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
                        .stringValue(UUID)
                        .build()
        );

        return messageAttributes;
    }










}
