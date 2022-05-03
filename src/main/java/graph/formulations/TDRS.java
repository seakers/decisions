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

        JsonArray constellations = new JsonArray();
        constellations.add(const1);
        constellations.add(const2);
        constellations.add(const3);


        JsonObject ant1 = new JsonObject();
        ant1.addProperty("active", Boolean.TRUE);
        ant1.addProperty("type", "item");
        ant1.addProperty("id", "0");
        ant1.addProperty("name", "ANT-1");

        JsonObject ant2 = new JsonObject();
        ant2.addProperty("active", Boolean.TRUE);
        ant2.addProperty("type", "item");
        ant2.addProperty("id", "1");
        ant2.addProperty("name", "ANT-2");

        JsonObject ant3 = new JsonObject();
        ant3.addProperty("active", Boolean.TRUE);
        ant3.addProperty("type", "item");
        ant3.addProperty("id", "2");
        ant3.addProperty("name", "ANT-3");

        JsonArray antennas = new JsonArray();
        antennas.add(ant1);
        antennas.add(ant2);
        antennas.add(ant3);


        // --> This is the child dependency passed through each ROOT_DEPENDENCY
        JsonObject assigning_obj = new JsonObject();
        assigning_obj.addProperty("child_name", "Antenna Assignment");
        assigning_obj.addProperty("child_type", "Assigning");
        assigning_obj.add("constellations", constellations);
        assigning_obj.add("antennas", antennas);



        // --> This object is 'this.parameters' in the Root node
        JsonArray root_children = new JsonArray();
        root_children.add(assigning_obj);



        JsonObject problems_obj = new JsonObject();
        problems_obj.add(problem, root_children);


        return problems_obj;
    }



}
