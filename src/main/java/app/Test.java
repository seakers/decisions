package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import evaluation.GNC_Evaluator;
import graph.structure.Structure;

import java.util.ArrayList;

public class Test {



    public static void reliabilityCalculation(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // 1. Create evaluator
        GNC_Evaluator evaluator = new GNC_Evaluator((5/3), 9, 1, 10);

        // 2. Create test design
        JsonArray design = new JsonArray();

        // Computer 1
        JsonArray computer_1_elements = new JsonArray();
        Structure.addItemElement(computer_1_elements, "s3-1", true);
//        Structure.addItemElement(computer_1_elements, "s3-2", true);
//        Structure.addItemElement(computer_1_elements, "s3-3", true);
        Structure.addListElement(design, "c1-1", true, computer_1_elements);

//        // Computer 2
//        JsonArray computer_2_elements = new JsonArray();
//        Structure.addItemElement(computer_2_elements, "s3-1", true);
//        Structure.addItemElement(computer_2_elements, "s3-2", true);
//        Structure.addItemElement(computer_2_elements, "s3-3", true);
//        Structure.addListElement(design, "c1-2", true, computer_2_elements);
//
//        // Computer 3
//        JsonArray computer_3_elements = new JsonArray();
//        Structure.addItemElement(computer_3_elements, "s3-1", true);
//        Structure.addItemElement(computer_3_elements, "s3-2", true);
//        Structure.addItemElement(computer_3_elements, "s3-3", true);
//        Structure.addListElement(design, "c1-3", true, computer_3_elements);

        String design_str = gson.toJson(design);


        // 3. Evaluate
        ArrayList<Double> results = evaluator.evaluate(design_str);

        System.out.println("---> RELIABILITY " + results.get(0));
        System.out.println("----------> MASS " + results.get(1));


    }












}
