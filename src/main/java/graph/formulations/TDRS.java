package graph.formulations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class TDRS {


    public static JsonObject getRootParameters(String problem){


        JsonObject const1 = new JsonObject();
        const1.addProperty("active", Boolean.TRUE);
        const1.addProperty("type", "item");
        const1.addProperty("id", "0");
        const1.addProperty("name", "GEOa-1-1");

        JsonObject const2 = new JsonObject();
        const2.addProperty("active", Boolean.TRUE);
        const2.addProperty("type", "item");
        const2.addProperty("id", "1");
        const2.addProperty("name", "GEOb-1-1");

        JsonObject const3 = new JsonObject();
        const3.addProperty("active", Boolean.TRUE);
        const3.addProperty("type", "item");
        const3.addProperty("id", "2");
        const3.addProperty("name", "GEOc-1-1");

        JsonArray elements_to = new JsonArray();
        elements_to.add(const1);
        elements_to.add(const2);
        elements_to.add(const3);


        JsonObject ant1 = new JsonObject();
        ant1.addProperty("active", Boolean.TRUE);
        ant1.addProperty("type", "item");
        ant1.addProperty("id", "0");
        ant1.addProperty("name", "GEOa-1-1");

        JsonObject ant2 = new JsonObject();
        ant2.addProperty("active", Boolean.TRUE);
        ant2.addProperty("type", "item");
        ant2.addProperty("id", "1");
        ant2.addProperty("name", "GEOb-1-1");

        JsonObject ant3 = new JsonObject();
        ant3.addProperty("active", Boolean.TRUE);
        ant3.addProperty("type", "item");
        ant3.addProperty("id", "2");
        ant3.addProperty("name", "GEOc-1-1");

        JsonArray elements_from = new JsonArray();
        elements_from.add(ant1);
        elements_from.add(ant2);
        elements_from.add(ant3);

        JsonObject assigning_obj = new JsonObject();
        assigning_obj.addProperty("child_name", "Antenna Assignment");
        assigning_obj.addProperty("child_type", "Assigning");
        assigning_obj.add("elements_to", elements_to);
        assigning_obj.add("elements_from", elements_from);




        JsonArray root_children = new JsonArray();
        root_children.add(assigning_obj);



        JsonObject problems_obj = new JsonObject();
        problems_obj.add(problem, root_children);


        return problems_obj;
    }



}
