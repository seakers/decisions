package graph.formulations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class AbstractFormulation {

    public static void addDependency(JsonArray dependencies, String child_name, String child_type, JsonArray elements){

        JsonObject dependency = new JsonObject();
        dependency.addProperty("child_name", child_name);
        dependency.addProperty("child_type", child_type);
        dependency.add("elements", elements);

        dependencies.add(dependency);
    }

    public static void addElement(JsonArray add_to, String name, boolean active){

        JsonObject element = new JsonObject();
        element.addProperty("id", add_to.size());
        element.addProperty("active", Boolean.TRUE);

        element.addProperty("type", "item");
        element.addProperty("name", name);

        add_to.add(element);
    }

    public static void addGroupElement(JsonArray add_to, JsonArray elements){

        JsonObject group = new JsonObject();
        group.addProperty("id", add_to.size());
        group.addProperty("active", Boolean.TRUE);

        group.addProperty("type", "list");
        group.add("elements", elements);

        add_to.add(group);
    }

}
