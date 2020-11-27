package graph.structure;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/*
        The purpose of this class is to facilitate creating and mutating the Json data
    structure that stores decision information.

        The proposed design data structure is predicated on the assumption that each design
    will contain a list of 'something'. Each something is dependent on the formulation of the
    decision diagram, so a generic data structure is required. JsonObject is an appropriate
    structure, as it can be shaped to hold any form of data. It follows that the data structure
    holding the JsonObjects is a JsonArray


 */
public class Structure {


    /*
        Creates a new item element and adds to a JsonArray
     */
    public static void addItemElement(JsonArray add_to, String element_name, boolean element_state){

        JsonObject element = new JsonObject();
        element.addProperty("id", add_to.size());
        element.addProperty("active", element_state);
        element.addProperty("type", "item");
        element.addProperty("name", element_name);

    }

    /*
        Creates a new list element whose sub-elements are empty and adds to a JsonArray
     */
    public static void addListElement(JsonArray add_to, String element_name, boolean element_state){

        JsonObject element = new JsonObject();
        element.addProperty("id", add_to.size());
        element.addProperty("active", element_state);
        element.addProperty("type", "list");
        element.addProperty("name", element_name);

        JsonArray sub_elements = new JsonArray();
        element.add("elements", sub_elements);
    }

    /*
        Creates a new list element whose sub-elements are passed and adds to a JsonArray
     */
    public static void addListElement(JsonArray add_to, String element_name, boolean element_state, JsonArray sub_elements){
        JsonObject element = new JsonObject();

        element.addProperty("id", add_to.size());
        element.addProperty("active", element_state);
        element.addProperty("type", "list");
        element.addProperty("name", element_name);
        element.add("elements", sub_elements);
    }

    /*
        Creates a new design given dependencies, the design elements, and the index
     */
    public static JsonObject createNewDesign(JsonArray dependencies, JsonArray design_elements, int idx){
        JsonObject design = new JsonObject();

        design.addProperty("id", idx);
        design.add("elements", design_elements);
        design.add("dependencies", dependencies);
        design.add("scores", new JsonObject());

        return design;
    }



    /*
        Returns the state of an element
     */
    public static boolean isActive(JsonObject element){
        return (element.get("active").getAsBoolean());
    }



    /*
        Safely get sub-elements from an element
     */
    public static JsonArray getElements(JsonObject element, boolean copy){
        // Check to see if the typing is correct
        if(!element.get("type").getAsString().equals("list")){
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println("---> TRIED TO RETRIEVE SUB-ELEMENTS FROM NON-LIST ELEMENT " + gson.toJson(element));
            System.exit(0);
        }

        if(copy){
            return (element.getAsJsonArray("elements").deepCopy());
        }
        else{
            return (element.getAsJsonArray("elements"));
        }
    }




    /*
        Get all the active elements of a JsonArray
     */
    public static ArrayList<Integer> getActiveIndices(JsonArray elements){
        ArrayList<Integer> indicies = new ArrayList<>();

        for(int x = 0; x < elements.size(); x++){
            JsonObject element = elements.get(x).getAsJsonObject();
            if(element.get("active").getAsBoolean()){
                indicies.add(x);
            }
        }
        return indicies;
    }

    /*
        Prune inactive elements of a JsonArray
     */
    public static JsonArray pruneInactiveElements(JsonArray elements){
        JsonArray pruned = new JsonArray();

        for(int x = 0; x < elements.size(); x++){
            JsonObject element = elements.get(x).getAsJsonObject();
            if(element.get("active").getAsBoolean()){
                pruned.add(element.deepCopy());
            }
        }
        return pruned;
    }





































}
