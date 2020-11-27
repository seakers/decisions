package moea;

import app.App;
import app.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;
import moea.solutions.ADDSolution;
import moea.solutions.InfoSolution;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import java.io.*;
import java.util.Iterator;

public class Results {


    public static void writeSMAP_SELECTING_HV(String[] line_contents, boolean append, String dir){
        String file_path = "/app/eos_formulation/smap/"+dir+"/non_dominated_ff.csv";

        try{
            FileWriter outputfile = new FileWriter(file_path, append);
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeNext(line_contents);
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void writeSMAP_SELECTING_POP(NondominatedPopulation population, String dir){
        String file_path = "/app/eos_formulation/smap/"+dir+"/non_dominated_designs.json";

        JsonArray designs = new JsonArray();
        JsonObject test_obj = new JsonObject();
        test_obj.addProperty("test", true);
        designs.add(test_obj);
        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter(file_path);
            // OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file_path), "UTF-8");
            // BufferedWriter bufWriter = new BufferedWriter(writer);

            Iterator<Solution> itr = population.iterator();
            while(itr.hasNext()){
                InfoSolution soln = (InfoSolution) itr.next();
                JsonArray design = new GsonBuilder().create().fromJson(soln.design_str, JsonArray.class).getAsJsonArray();
                designs.add(design);
            }


            gson.toJson(designs, outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("WRITING EXCEPTION");
            App.sleep(100);
        }
    }



    public static void writeSMAP_FF_HV(String[] line_contents, boolean append){
        String file_path = "/app/eos_formulation/smap/non_dominated_hv.csv";

        try{
            FileWriter outputfile = new FileWriter(file_path, append);
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeNext(line_contents);
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void writeSMAP_FF_Pop(NondominatedPopulation population){
        String file_path = "/app/eos_formulation/smap/non_dominated_designs.json";

        JsonArray designs = new JsonArray();
        JsonObject test_obj = new JsonObject();
        test_obj.addProperty("test", true);
        designs.add(test_obj);
        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter(file_path);
            // OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file_path), "UTF-8");
            // BufferedWriter bufWriter = new BufferedWriter(writer);

            Iterator<Solution> itr = population.iterator();
            while(itr.hasNext()){
                InfoSolution soln = (InfoSolution) itr.next();
                JsonArray design = new GsonBuilder().create().fromJson(soln.design_str, JsonArray.class).getAsJsonArray();
                designs.add(design);
            }


            gson.toJson(designs, outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("WRITING EXCEPTION");
            App.sleep(100);
        }
    }








    public static void writeRowSELECTING(String file_name, String[] line_contents, boolean append){
        String file_path = Files.selectingDir + file_name;

        try{
            FileWriter outputfile = new FileWriter(file_path, append);
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeNext(line_contents);
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void writeRowRESULT(String file_name, String[] line_contents, boolean append, String dir){
        String file_path = dir + file_name;

        try{
            FileWriter outputfile = new FileWriter(file_path, append);
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeNext(line_contents);
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }







    public static void writeRow(String file_name, String[] line_contents, boolean append){
        String directory = "/app/results/";
        String file_path = directory + file_name;

        try{
            FileWriter outputfile = new FileWriter(file_path, append);
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeNext(line_contents);
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }




    public static void writePopulation(String directory, NondominatedPopulation population){

        // Get the filename based on the number of files in the directory
        int file_count = (new File(directory)).list().length;
        String file_name = "pop_" + file_count + ".csv";
        String file_path = directory + "/" + file_name;



        try{
            FileWriter outputfile = new FileWriter(file_path, false);
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeNext(new String[]{"science", "cost", "data_continuity"});

            Iterator<Solution> itr = population.iterator();
            while(itr.hasNext()){

                Solution soln = itr.next();
                double science = -(soln.getObjective(0)); // Science
                double cost = (soln.getObjective(1)); // Cost
                double data_continuity = (soln.getObjective(2)); // Data Continuity
                String[] res = { Double.toString(science), Double.toString(cost), Double.toString(data_continuity) };
                writer.writeNext(res);

            }

            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void writePopulationDesigns(String directory, NondominatedPopulation population){

        int file_count = (new File(directory)).list().length;
        String file_name = "pop_" + file_count + "_designs.json";
        String file_path = directory + "/" + file_name;

        JsonArray designs = new JsonArray();

        JsonObject test_obj = new JsonObject();
        designs.add(test_obj);
        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter(file_path);
            // OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file_path), "UTF-8");
            // BufferedWriter bufWriter = new BufferedWriter(writer);

            Iterator<Solution> itr = population.iterator();
            while(itr.hasNext()){
                ADDSolution soln = (ADDSolution) itr.next();
                JsonArray design = new GsonBuilder().create().fromJson(soln.design_str, JsonArray.class).getAsJsonArray();
                designs.add(design);
            }


            gson.toJson(designs, outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("WRITING EXCEPTION");
            App.sleep(100);
        }
    }










}
