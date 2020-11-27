package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;
import moea.solutions.ADDSolution;
import org.moeaframework.Analyzer;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class Files {

    public static String selectingDir = "/app/eos_formulation/smap/selecting/";
    public static String decadalAddDir = "/app/eos_formulation/decadal/add";
    public static String decadalAddDir2 = "/app/eos_formulation/decadal/add/";



    // DIRECTORIES: Decadal
    public static String decadalAdd = "/app/eos_formulation/decadal/add";
    public static String decadalSelecting = "/app/eos_formulation/decadal/selecting";
    public static String decadalPermuting = "/app/eos_formulation/decadal/permuting";
    public static String decadalPartitioning = "/app/eos_formulation/decadal/partitioning";

    // DIRECTORIES: Decadal2007
    public static String decadalAdd2007 = "/app/eos_formulation/decadal2007/add";
    public static String decadalSelecting2007 = "/app/eos_formulation/decadal2007/selecting";
    public static String decadalPermuting2007 = "/app/eos_formulation/decadal2007/permuting";
    public static String decadalPartitioning2007 = "/app/eos_formulation/decadal2007/partitioning";



    // DIRECTORIES: GN&C
    public static String gncDir = "/app/gnc_formulation/two_branch";


    // SET DIRECTORY
    public static String currentFile = Files.gncDir;


    public static void writeAlgorithmMetrics(Analyzer analyzer, int nfe){
        String dir = Files.currentFile;

        NondominatedPopulation non_dominated = analyzer.getReferenceSet();
        System.out.println("---> NUM NON-DOMINATED SOLNS " + non_dominated.size());


        File root_dir_file = new File(dir);
        if(!root_dir_file.isDirectory()){
            root_dir_file.mkdir();
        }


        // LOCAL METRICS
        File nfe_dir_file = new File(dir + "/" + nfe);
        if(!nfe_dir_file.isDirectory()){
            nfe_dir_file.mkdir();
        }

        String pop_name    = nfe_dir_file.getAbsolutePath() + "/" + "pop_" + (nfe_dir_file.list().length / 2) + ".csv";
        String design_name = nfe_dir_file.getAbsolutePath() + "/" + "designs_" + (nfe_dir_file.list().length / 2) + ".json";

        Files.writePopulationDesignScores2(pop_name, non_dominated);
        Files.writePopulationDesigns(design_name, non_dominated);



        // GLOBAL METRICS
        String hv_file     = dir + "/nfe_" + nfe + ".csv";
        File   hv_file_obj = new File(hv_file);
        if(!hv_file_obj.isFile()){
            Files.writeCSVLine(hv_file, (new String[]{ "HV_START", "HV_END" }), false);
        }

        double   result_hv_min = analyzer.getAnalysis().get("popADD").get("Hypervolume").getMin();
        double   result_hv_max = analyzer.getAnalysis().get("popADD").get("Hypervolume").getMax();
        String[] csv_input     = { Double.toString(result_hv_min), Double.toString(result_hv_max) };

        Files.writeCSVLine(hv_file, csv_input, true);



    }




    public static void writeCSVLine(String file_path, String[] line, boolean append){
        try{
            FileWriter outputfile = new FileWriter(file_path, append);
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeNext(line);
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }



    public static void writePopulationDesigns(String file_path, NondominatedPopulation population){

        JsonArray designs   = new JsonArray();
        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter(file_path);

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



    public static void writePopulationDesignScores(String file_path, NondominatedPopulation population){

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

    public static void writePopulationDesignScores2(String file_path, NondominatedPopulation population){

        try{
            FileWriter outputfile = new FileWriter(file_path, false);
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeNext(new String[]{"reliability", "mass"});

            Iterator<Solution> itr = population.iterator();
            while(itr.hasNext()){

                Solution soln = itr.next();
                double reliability = -(soln.getObjective(0)); // Science
                double mass = (soln.getObjective(1)); // Cost
                String[] res = { Double.toString(reliability), Double.toString(mass)};
                writer.writeNext(res);

            }

            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }






    public static void writeDebugFile(String full_file_path, JsonArray elements){

        try{
            Gson       gson       = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter(full_file_path);

            gson.toJson(elements, outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }























}
