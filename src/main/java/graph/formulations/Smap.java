package graph.formulations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Smap {


    public static JsonArray getRootParameters(){

        // ORBITS
        JsonArray orbit_elements = new JsonArray();
        Smap.addElement(orbit_elements, "LEO-600-polar-NA", true);
        Smap.addElement(orbit_elements, "SSO-600-SSO-AM", true);
        Smap.addElement(orbit_elements, "SSO-600-SSO-DD", true);
        Smap.addElement(orbit_elements, "SSO-800-SSO-AM", true);
        Smap.addElement(orbit_elements, "SSO-800-SSO-DD", true);

        JsonObject orbit_select_obj = new JsonObject();
        orbit_select_obj.addProperty("child_name", "Orbit Selection");
        orbit_select_obj.addProperty("child_type", "DownSelecting");
        orbit_select_obj.add("elements", orbit_elements);

        // INSTRUMENTS
        JsonArray instrumnet_elements = new JsonArray();
        Smap.addElement(instrumnet_elements, "SMAP_MWR", true);
        Smap.addElement(instrumnet_elements, "SMAP_RAD", true);
        Smap.addElement(instrumnet_elements, "BIOMASS", true);
        Smap.addElement(instrumnet_elements, "CMIS", true);
        Smap.addElement(instrumnet_elements, "VIIRS", true);

        JsonObject instrument_select_obj = new JsonObject();
        instrument_select_obj.addProperty("child_name", "Instrument Selection");
        instrument_select_obj.addProperty("child_type", "DownSelecting");
        instrument_select_obj.add("elements", instrumnet_elements);

        JsonArray final_ary = new JsonArray();
        final_ary.add(orbit_select_obj);
        final_ary.add(instrument_select_obj);

        return final_ary;
    }



    public static Gson getGson(boolean prettyPrint){
        Gson gson;
        if(prettyPrint){
            gson = new GsonBuilder().setPrettyPrinting().create();
        }
        else{
            gson = new GsonBuilder().create();
        }
        return gson;
    }



    public static JsonArray getSelectingEnumerationDependency(){

        JsonArray dependencies = new JsonArray();

        Smap.addElement(dependencies, "BIOMASS", true);
        Smap.addElement(dependencies, "VIIRS", true);
        Smap.addElement(dependencies, "CMIS", true);
        Smap.addElement(dependencies, "SMAP_RAD", true);
        Smap.addElement(dependencies, "SMAP_MWR", true);

        Gson gson = Smap.getGson(true);
        System.out.println("---> SMAP SELECTING DEPENDENCIES");
        System.out.println(gson.toJson(dependencies));

        return dependencies;
    }



    public static JsonArray getPartitioningEnumerationDependency(){

        JsonArray dependencies = new JsonArray();

        Smap.addElement(dependencies, "BIOMASS", true);
        Smap.addElement(dependencies, "VIIRS", false);
        Smap.addElement(dependencies, "CMIS", true);
        Smap.addElement(dependencies, "SMAP_RAD", true);
        Smap.addElement(dependencies, "SMAP_MWR", true);

        Gson gson = Smap.getGson(true);
        System.out.println("---> SMAP SELECTING DEPENDENCIES");
        System.out.println(gson.toJson(dependencies));

        return dependencies;
    }

    public static JsonArray getPermutingEnumerationDependency(){

        JsonArray dependencies = new JsonArray();

        Smap.addElement(dependencies, "BIOMASS", true);
        Smap.addElement(dependencies, "VIIRS", false);
        Smap.addElement(dependencies, "CMIS", true);
        Smap.addElement(dependencies, "SMAP_RAD", true);
        Smap.addElement(dependencies, "SMAP_MWR", true);

        Gson gson = Smap.getGson(true);
        System.out.println("---> SMAP SELECTING DEPENDENCIES");
        System.out.println(gson.toJson(dependencies));

        return dependencies;
    }





    public static void addElement(JsonArray add_to, String name, boolean active){

        JsonObject element = new JsonObject();
        element.addProperty("id", add_to.size());
        element.addProperty("active", active);
        element.addProperty("type", "item");
        element.addProperty("name", name);

        add_to.add(element);
    }



    //                "  {\n" +
//                        "    \"active\": true,\n" +
//                        "    \"id\": 1,\n" +
//                        "    \"type\": \"list\",\n" +
//                        "    \"elements\": [\n" +
//                        "      {\n" +
//                        "        \"active\": true,\n" +
//                        "        \"type\": \"item\",\n" +
//                        "        \"id\": \"2\",\n" +
//                        "        \"name\": \"IR-Spectrometer\"\n" +
//                        "      }\n" +
//                        "    ]\n" +
//                        "  },\n" +
//                        "  {\n" +
//                        "    \"active\": true,\n" +
//                        "    \"id\": 0,\n" +
//                        "    \"type\": \"list\",\n" +
//                        "    \"elements\": [\n" +
//                        "      {\n" +
//                        "        \"active\": true,\n" +
//                        "        \"type\": \"item\",\n" +
//                        "        \"id\": \"1\",\n" +
//                        "        \"name\": \"EARTHCARE-CPR\"\n" +
//                        "      }\n" +
//                        "    ]\n" +
//                        "  }\n" +
//                        "]\n";
    public static void addGroup(JsonArray add_to, JsonArray elements){

        JsonObject group = new JsonObject();

        group.addProperty("id", add_to.size());
        group.addProperty("type", "list");
        group.addProperty("active", true);
        group.add("elements", elements);

        add_to.add(group);

    }



















}
