package graph.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graph.Decision;
import graph.Graph;
import org.neo4j.driver.Record;

import java.util.HashMap;
import java.util.Iterator;

public class Root extends Decision {


    public static class Builder extends Decision.Builder<Root.Builder>{

        public Builder(Record node) {
            super(node);
        }

        public Root build() { return new Root(this); }
    }


    protected Root(Root.Builder builder){
        super(builder);
    }





    @Override
    public JsonObject getLastDecision(){
        return this.inputs;
    }





//     ______                                      _   _
//    |  ____|                                    | | (_)
//    | |__   _ __  _   _ _ __ ___   ___ _ __ __ _| |_ _  ___  _ __
//    |  __| | '_ \| | | | '_ ` _ \ / _ \ '__/ _` | __| |/ _ \| '_ \
//    | |____| | | | |_| | | | | | |  __/ | | (_| | |_| | (_) | | | |
//    |______|_| |_|\__,_|_| |_| |_|\___|_|  \__,_|\__|_|\___/|_| |_|

    @Override
    public HashMap<Integer, JsonArray> getEnumerations(String node_name, String node_type){

        Iterator dependency_iterator = this.parameters.iterator();
        while(dependency_iterator.hasNext()){
            JsonObject dependency = ((JsonElement) dependency_iterator.next()).getAsJsonObject();
            String dependency_name = Graph.removeQuotes(dependency.get("child_name").toString());
            String dependency_type = Graph.removeQuotes(dependency.get("child_type").toString());

            if(dependency_name.equals(node_name) && dependency_type.equals(node_type)){
                HashMap<Integer, JsonArray> enumeration_dependencies = new HashMap<>();
                enumeration_dependencies.put(0, dependency.getAsJsonArray("elements"));
                return enumeration_dependencies;
            }
        }
        return new HashMap<>();
    }


}
