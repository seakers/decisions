package formulations;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Decadal{




    public static JsonArray getAddTestDesign2007(){
        JsonArray design = new JsonArray();

        JsonArray group_1 = new JsonArray();
        JsonArray group_2 = new JsonArray();
        JsonArray group_3 = new JsonArray();

//        Smap.addElement(group_1, "ACE_CPR", true); //624.1 0.05175 102
//        Smap.addElement(group_1, "ACE_ORCA", true);// 250.88 0.07715 115
//        Smap.addElement(group_1, "ACE_POL", true); // 258.75 0.0282 127
//        Smap.addElement(group_1, "ACE_LID", true); // 678.166 0.07734 195

//        Smap.addElement(group_1, "ASC_LID", true); // 476.65 0.0654 118
//        Smap.addElement(group_1, "ASC_GCR", true); // 166.836 0.0109 39
//        Smap.addElement(group_1, "ASC_IRR", true); // 180.48 0.00264 102
//
//        Smap.addElement(group_1, "CLAR_TIR", true); // 218.45 0.011 95
//        Smap.addElement(group_1, "CLAR_VNIR", true);// 556.97 .000209 101
//        Smap.addElement(group_1, "CLAR_GPS", true); // 132.31 0.066 102
//
//        Smap.addElement(group_1, "DESD_SAR", true); // 917.74 0.0069 389
//        Smap.addElement(group_1, "DESD_LID", true); // 514.96 0.1007 229
//
//        Smap.addElement(group_1, "GACM_SWIR", true);// 641.157 0.024323 28
//        Smap.addElement(group_1, "GACM_MWSP", true); // 386 0.07 168
//        Smap.addElement(group_1, "GACM_VIS", true); // done
//        Smap.addElement(group_1, "GACM_DIAL", true); // done
//
//        Smap.addElement(group_1, "GEO_STEER", true); // done
//        Smap.addElement(group_1, "GEO_WAIS", true); // done
//        Smap.addElement(group_1, "GEO_GCR", true); // done
//
//        Smap.addElement(group_1, "GPS", true);
//        Smap.addElement(group_1, "GRAC_RANG", true);
//
//        Smap.addElement(group_1, "HYSP_TIR", true);
//        Smap.addElement(group_1, "HYSP_VIS", true);
//
//        Smap.addElement(group_1, "ICE_LID", true);
//        Smap.addElement(group_1, "LIST_LID", true);
//        Smap.addElement(group_1, "PATH_GEOSTAR", true);
//
//        Smap.addElement(group_1, "SCLP_SAR", true);
//        Smap.addElement(group_1, "SCLP_MWR", true);
//
//        Smap.addElement(group_1, "SMAP_ANT", true); // DONT FLY
//        Smap.addElement(group_1, "SMAP_RAD", true); // NEEDS FIXING
//
        Smap.addElement(group_1, "SWOT_GPS", true);
//        Smap.addElement(group_1, "SWOT_KaRIN", true);
//        Smap.addElement(group_1, "SWOT_RAD", true);
//        Smap.addElement(group_1, "SWOT_MWR", true);
//
//        Smap.addElement(group_1, "XOV_SAR", true);
//        Smap.addElement(group_1, "XOV_RAD", true);
//        Smap.addElement(group_1, "XOV_MWR", true);
//
//        Smap.addElement(group_1, "3D_CLID", true);
//        Smap.addElement(group_1, "3D_NCLID", true);
//
//        Smap.addElement(group_1, "CLOUD_MASK", true);





        Smap.addGroup(design, group_1);

        return design;
    }





    public static JsonArray getAddTestDesign(){
        JsonArray design = new JsonArray();

        JsonArray group_1 = new JsonArray();
        JsonArray group_2 = new JsonArray();
        JsonArray group_3 = new JsonArray();
        JsonArray group_4 = new JsonArray();
        JsonArray group_5 = new JsonArray();
        JsonArray group_6 = new JsonArray();
        JsonArray group_7 = new JsonArray();
        JsonArray group_8 = new JsonArray();
        JsonArray group_9 = new JsonArray();

//        Smap.addElement(group_1, "ACE-CPR", true);

        Smap.addElement(group_1, "EARTHCARE-ATLID", true);
        Smap.addElement(group_1, "ACE-OCI", true);
        Smap.addElement(group_1, "ACE-POL", true);
        Smap.addElement(group_1, "ACE-LID", true);
        Smap.addElement(group_1, "CALIPSO-CALIOP", true);
        Smap.addElement(group_1, "CALIPSO-IIR", true);
        Smap.addElement(group_1, "EARTHCARE-CPR", true);
        Smap.addElement(group_1, "EARTHCARE-MSI", true);
        Smap.addElement(group_1, "DIAL", true);

        Smap.addGroup(design, group_1);
//        Smap.addGroup(design, group_2);
//        Smap.addGroup(design, group_3);
//        Smap.addGroup(design, group_4);
//        Smap.addGroup(design, group_5);
//        Smap.addGroup(design, group_6);
//        Smap.addGroup(design, group_7);
//        Smap.addGroup(design, group_8);
//        Smap.addGroup(design, group_9);

        return design;
    }



    public static JsonArray getPartitioningEnumerationDependency(){

        JsonArray dependencies = new JsonArray();

        Smap.addElement(dependencies, "EARTHCARE-ATLID", true);
//        Smap.addElement(dependencies, "ACE-CPR", true);
        Smap.addElement(dependencies, "ACE-OCI", true);
        Smap.addElement(dependencies, "ACE-POL", true);
        Smap.addElement(dependencies, "ACE-LID", true);
        Smap.addElement(dependencies, "CALIPSO-CALIOP", true);
        Smap.addElement(dependencies, "CALIPSO-IIR", true);
        Smap.addElement(dependencies, "EARTHCARE-CPR", true);
        Smap.addElement(dependencies, "EARTHCARE-MSI", true);
        Smap.addElement(dependencies, "DIAL", true);


        Gson gson = Smap.getGson(true);
        System.out.println("---> DECADAL SELECTING DEPENDENCIES");
        System.out.println(gson.toJson(dependencies));

        return dependencies;
    }

    public static JsonArray getSelectingEnumerationDependency(){

        JsonArray dependencies = new JsonArray();

        Smap.addElement(dependencies, "ACE-CPR", true);
        Smap.addElement(dependencies, "ACE-OCI", true);
        Smap.addElement(dependencies, "ACE-POL", true);
        Smap.addElement(dependencies, "ACE-LID", true);





        // Multipurpose imaging VIS/IR Radiometer
        Smap.addElement(dependencies, "CALIPSO-WFC", true);
        Smap.addElement(dependencies, "CALIPSO-IIR", true);
        Smap.addElement(dependencies, "EARTHCARE-MSI", true);

        // Cloud and Precipitation Radar
        Smap.addElement(dependencies, "EARTHCARE-CPR", true);

        // Atmospheric Lidar
        Smap.addElement(dependencies, "EARTHCARE-ATLID", true);
        Smap.addElement(dependencies, "CALIPSO-CALIOP", true);





        Smap.addElement(dependencies, "ICI", true);
        Smap.addElement(dependencies, "AQUARIUS", true);
        Smap.addElement(dependencies, "DIAL", true);
        Smap.addElement(dependencies, "IR-Spectrometer", true);

        Gson gson = Smap.getGson(true);
        System.out.println("---> SMAP SELECTING DEPENDENCIES");
        System.out.println(gson.toJson(dependencies));

        return dependencies;
    }




    public static JsonArray get2007Parameters(){

        JsonArray elements = new JsonArray();

        Smap.addElement(elements, "ACE_CPR", true); // 624.1   0.05175 102
        Smap.addElement(elements, "ACE_ORCA", true);// 250.88  0.07715 115
        Smap.addElement(elements, "ACE_POL", true); // 258.75  0.0282 127
        Smap.addElement(elements, "ACE_LID", true); // 678.166 0.07734 195

        Smap.addElement(elements, "ASC_LID", true); // 476.65  0.0654 118
        Smap.addElement(elements, "ASC_GCR", true); // 166.836 0.0109 39
        Smap.addElement(elements, "ASC_IRR", true); // 180.48  0.00264 102

        Smap.addElement(elements, "CLAR_TIR", true); // 218.45 0.011 95
        Smap.addElement(elements, "CLAR_VNIR", true);// 556.97 0.000209 101
        Smap.addElement(elements, "CLAR_GPS", true); // 132.31 0.066 102

        Smap.addElement(elements, "DESD_SAR", true); // 917.74 0.0069 389
        Smap.addElement(elements, "DESD_LID", true); // 514.96 0.1007 229


        JsonObject down_select_obj = new JsonObject();
        down_select_obj.addProperty("child_name", "Instrument Selection");
        down_select_obj.addProperty("child_type", "DownSelecting");
        down_select_obj.add("elements", elements);

        JsonArray final_ary = new JsonArray();
        final_ary.add(down_select_obj);
        return final_ary;
    }


    public static JsonArray getBigRootParameters(){

        JsonObject inst1 = new JsonObject();
        inst1.addProperty("active", Boolean.TRUE);
        inst1.addProperty("type", "item");
        inst1.addProperty("id", "0");
        inst1.addProperty("name", "ACE-CPR");
        JsonObject inst2 = new JsonObject();
        inst2.addProperty("active", Boolean.TRUE);
        inst2.addProperty("type", "item");
        inst2.addProperty("id", "1");
        inst2.addProperty("name", "ACE-OCI");
        JsonObject inst3 = new JsonObject();
        inst3.addProperty("active", Boolean.TRUE);
        inst3.addProperty("type", "item");
        inst3.addProperty("id", "2");
        inst3.addProperty("name", "ACE-POL");
        JsonObject inst4 = new JsonObject();
        inst4.addProperty("active", Boolean.TRUE);
        inst4.addProperty("type", "item");
        inst4.addProperty("id", "3");
        inst4.addProperty("name", "ACE-LID");
        JsonObject inst5 = new JsonObject();
        inst5.addProperty("active", Boolean.TRUE);
        inst5.addProperty("type", "item");
        inst5.addProperty("id", "4");
        inst5.addProperty("name", "CALIPSO-CALIOP");
        JsonObject inst6 = new JsonObject();
        inst6.addProperty("active", Boolean.TRUE);
        inst6.addProperty("type", "item");
        inst6.addProperty("id", "5");
        inst6.addProperty("name", "CALIPSO-WFC");
        JsonObject inst7 = new JsonObject();
        inst7.addProperty("active", Boolean.TRUE);
        inst7.addProperty("type", "item");
        inst7.addProperty("id", "6");
        inst7.addProperty("name", "CALIPSO-IIR");
        JsonObject inst8 = new JsonObject();
        inst8.addProperty("active", Boolean.TRUE);
        inst8.addProperty("type", "item");
        inst8.addProperty("id", "7");
        inst8.addProperty("name", "EARTHCARE-ATLID");
        JsonObject inst9 = new JsonObject();
        inst9.addProperty("active", Boolean.TRUE);
        inst9.addProperty("type", "item");
        inst9.addProperty("id", "8");
        inst9.addProperty("name", "EARTHCARE-CPR");
        JsonObject inst10 = new JsonObject();
        inst10.addProperty("active", Boolean.TRUE);
        inst10.addProperty("type", "item");
        inst10.addProperty("id", "9");
        inst10.addProperty("name", "EARTHCARE-MSI");
        JsonObject inst11 = new JsonObject();
        inst11.addProperty("active", Boolean.TRUE);
        inst11.addProperty("type", "item");
        inst11.addProperty("id", "10");
        inst11.addProperty("name", "ICI");
        JsonObject inst12 = new JsonObject();
        inst12.addProperty("active", Boolean.TRUE);
        inst12.addProperty("type", "item");
        inst12.addProperty("id", "11");
        inst12.addProperty("name", "AQUARIUS");
        JsonObject inst13 = new JsonObject();
        inst13.addProperty("active", Boolean.TRUE);
        inst13.addProperty("type", "item");
        inst13.addProperty("id", "12");
        inst13.addProperty("name", "DIAL");
        JsonObject inst14 = new JsonObject();
        inst14.addProperty("active", Boolean.TRUE);
        inst14.addProperty("type", "item");
        inst14.addProperty("id", "13");
        inst14.addProperty("name", "IR-Spectrometer");


        JsonArray elements = new JsonArray();
        elements.add(inst1);
        elements.add(inst2);
        elements.add(inst3);
        elements.add(inst4);
        elements.add(inst5);
        elements.add(inst6);
        elements.add(inst7);
        elements.add(inst8);
        elements.add(inst9);
        elements.add(inst10);
        elements.add(inst11);
        elements.add(inst12);
        elements.add(inst13);
        elements.add(inst14);

        JsonObject down_select_obj = new JsonObject();
        down_select_obj.addProperty("child_name", "Instrument Selection");
        down_select_obj.addProperty("child_type", "DownSelecting");
        down_select_obj.add("elements", elements);

        JsonArray final_ary = new JsonArray();
        final_ary.add(down_select_obj);
        return final_ary;
    }

    public static JsonArray getRootParameters(){

        JsonObject inst1 = new JsonObject();
        inst1.addProperty("active", Boolean.TRUE);
        inst1.addProperty("type", "item");
        inst1.addProperty("id", "0");
        inst1.addProperty("name", "VIIRS");
        JsonObject inst2 = new JsonObject();
        inst2.addProperty("active", Boolean.TRUE);
        inst2.addProperty("type", "item");
        inst2.addProperty("id", "1");
        inst2.addProperty("name", "BIOMASS");
        JsonObject inst3 = new JsonObject();
        inst3.addProperty("active", Boolean.TRUE);
        inst3.addProperty("type", "item");
        inst3.addProperty("id", "2");
        inst3.addProperty("name", "SMAP_MWR");
        JsonObject inst4 = new JsonObject();
        inst4.addProperty("active", Boolean.TRUE);
        inst4.addProperty("type", "item");
        inst4.addProperty("id", "3");
        inst4.addProperty("name", "SMAP_RAD");
        JsonObject inst5 = new JsonObject();
        inst5.addProperty("active", Boolean.TRUE);
        inst5.addProperty("type", "item");
        inst5.addProperty("id", "4");
        inst5.addProperty("name", "CMIS");

        // ELEMENTS
        JsonArray elements = new JsonArray();
        elements.add(inst1);
        elements.add(inst2);
        elements.add(inst3);
        elements.add(inst4);
        elements.add(inst5);

        JsonObject down_select_obj = new JsonObject();
        down_select_obj.addProperty("child_name", "Instrument Selection");
        down_select_obj.addProperty("child_type", "DownSelecting");
        down_select_obj.add("elements", elements);

        JsonArray final_ary = new JsonArray();
        final_ary.add(down_select_obj);
        return final_ary;
    }






}
