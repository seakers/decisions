package formulations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GuidanceNavigationAndControl{






    public static JsonArray getTestRootParameters(){

        // SENSOR
        JsonArray sensors = new JsonArray();
        Smap.addElement(sensors, "s1-1", true);
        Smap.addElement(sensors, "s2-1", true);
        Smap.addElement(sensors, "s3-1", true);

        JsonArray sensors1 = new JsonArray();
        Smap.addElement(sensors1, "s1-2", true);
        Smap.addElement(sensors1, "s2-2", true);
        Smap.addElement(sensors1, "s3-2", true);

        JsonArray sensors2 = new JsonArray();
        Smap.addElement(sensors2, "s1-3", true);
        Smap.addElement(sensors2, "s2-3", true);
        Smap.addElement(sensors2, "s3-3", true);

        JsonObject sensor_obj = new JsonObject();
        sensor_obj.addProperty("active", true);
        sensor_obj.addProperty("type", "list");
        sensor_obj.add("elements", sensors.deepCopy());

        JsonObject sensor_obj1 = new JsonObject();
        sensor_obj1.addProperty("active", true);
        sensor_obj1.addProperty("type", "list");
        sensor_obj1.add("elements", sensors1.deepCopy());

        JsonObject sensor_obj2 = new JsonObject();
        sensor_obj2.addProperty("active", true);
        sensor_obj2.addProperty("type", "list");
        sensor_obj2.add("elements", sensors2.deepCopy());

        JsonArray sensor_elements = new JsonArray();
        sensor_elements.add(sensor_obj.deepCopy());
        sensor_elements.add(sensor_obj1.deepCopy());
        sensor_elements.add(sensor_obj2.deepCopy());

        JsonObject sensor_select_obj = new JsonObject();
        sensor_select_obj.addProperty("child_name", "Num Sensor Selection");
        sensor_select_obj.addProperty("child_type", "DownSelecting");
        sensor_select_obj.add("elements", sensor_elements);






        // COMPUTER
        JsonArray computers = new JsonArray();
        Smap.addElement(computers, "c1-1", true);
        Smap.addElement(computers, "c2-1", true);
        Smap.addElement(computers, "c3-1", true);

        JsonArray computers1 = new JsonArray();
        Smap.addElement(computers1, "c1-2", true);
        Smap.addElement(computers1, "c2-2", true);
        Smap.addElement(computers1, "c3-2", true);

        JsonArray computers2 = new JsonArray();
        Smap.addElement(computers2, "c1-3", true);
        Smap.addElement(computers2, "c2-3", true);
        Smap.addElement(computers2, "c3-3", true);

        JsonObject computer_obj = new JsonObject();
        computer_obj.addProperty("active", true);
        computer_obj.addProperty("type", "list");
        computer_obj.add("elements", computers.deepCopy());

        JsonObject computer_obj1 = new JsonObject();
        computer_obj1.addProperty("active", true);
        computer_obj1.addProperty("type", "list");
        computer_obj1.add("elements", computers1.deepCopy());

        JsonObject computer_obj2 = new JsonObject();
        computer_obj2.addProperty("active", true);
        computer_obj2.addProperty("type", "list");
        computer_obj2.add("elements", computers2.deepCopy());

        JsonArray computer_elements = new JsonArray();
        computer_elements.add(computer_obj.deepCopy());
        computer_elements.add(computer_obj1.deepCopy());
        computer_elements.add(computer_obj2.deepCopy());

        JsonObject computer_select_obj = new JsonObject();
        computer_select_obj.addProperty("child_name", "Num Computer Selection");
        computer_select_obj.addProperty("child_type", "DownSelecting");
        computer_select_obj.add("elements", computer_elements);




        JsonArray final_ary = new JsonArray();
        final_ary.add(sensor_select_obj);
        final_ary.add(computer_select_obj);

        return final_ary;
    }




    public static JsonArray getProdRootParameters(){


        // SENSOR
        JsonArray sensors = new JsonArray();
        Smap.addElement(sensors, "s1-1", true);
        Smap.addElement(sensors, "s2-1", true);
        Smap.addElement(sensors, "s3-1", true);

        JsonArray sensors1 = new JsonArray();
        Smap.addElement(sensors1, "s1-2", true);
        Smap.addElement(sensors1, "s2-2", true);
        Smap.addElement(sensors1, "s3-2", true);

        JsonArray sensors2 = new JsonArray();
        Smap.addElement(sensors2, "s1-3", true);
        Smap.addElement(sensors2, "s2-3", true);
        Smap.addElement(sensors2, "s3-3", true);

        JsonObject sensor_obj = new JsonObject();
        sensor_obj.addProperty("active", true);
        sensor_obj.addProperty("type", "list");
        sensor_obj.add("elements", sensors.deepCopy());

        JsonObject sensor_obj1 = new JsonObject();
        sensor_obj1.addProperty("active", true);
        sensor_obj1.addProperty("type", "list");
        sensor_obj1.add("elements", sensors1.deepCopy());

        JsonObject sensor_obj2 = new JsonObject();
        sensor_obj2.addProperty("active", true);
        sensor_obj2.addProperty("type", "list");
        sensor_obj2.add("elements", sensors2.deepCopy());

        JsonArray sensor_elements = new JsonArray();
        sensor_elements.add(sensor_obj.deepCopy());
        sensor_elements.add(sensor_obj1.deepCopy());
        sensor_elements.add(sensor_obj2.deepCopy());

        JsonObject sensor_select_obj = new JsonObject();
        sensor_select_obj.addProperty("child_name", "Num Sensor Selection");
        sensor_select_obj.addProperty("child_type", "DownSelecting");
        sensor_select_obj.add("elements", sensor_elements);





        // COMPUTER
        JsonArray computers = new JsonArray();
        Smap.addElement(computers, "c1-1", true);
        Smap.addElement(computers, "c2-1", true);
        Smap.addElement(computers, "c3-1", true);

        JsonArray computers1 = new JsonArray();
        Smap.addElement(computers1, "c1-2", true);
        Smap.addElement(computers1, "c2-2", true);
        Smap.addElement(computers1, "c3-2", true);

        JsonArray computers2 = new JsonArray();
        Smap.addElement(computers2, "c1-3", true);
        Smap.addElement(computers2, "c2-3", true);
        Smap.addElement(computers2, "c3-3", true);

        JsonObject computer_obj = new JsonObject();
        computer_obj.addProperty("active", true);
        computer_obj.addProperty("type", "list");
        computer_obj.add("elements", computers.deepCopy());

        JsonObject computer_obj1 = new JsonObject();
        computer_obj1.addProperty("active", true);
        computer_obj1.addProperty("type", "list");
        computer_obj1.add("elements", computers1.deepCopy());

        JsonObject computer_obj2 = new JsonObject();
        computer_obj2.addProperty("active", true);
        computer_obj2.addProperty("type", "list");
        computer_obj2.add("elements", computers2.deepCopy());

        JsonArray computer_elements = new JsonArray();
        computer_elements.add(computer_obj.deepCopy());
        computer_elements.add(computer_obj1.deepCopy());
        computer_elements.add(computer_obj2.deepCopy());

        JsonObject computer_select_obj = new JsonObject();
        computer_select_obj.addProperty("child_name", "Num Computer Selection");
        computer_select_obj.addProperty("child_type", "DownSelecting");
        computer_select_obj.add("elements", computer_elements);



        // ACTUATOR
        JsonArray actuators = new JsonArray();
        Smap.addElement(actuators, "a1-1", true);
        Smap.addElement(actuators, "a2-1", true);
        Smap.addElement(actuators, "a3-1", true);

        JsonArray actuators1 = new JsonArray();
        Smap.addElement(actuators1, "a1-2", true);
        Smap.addElement(actuators1, "a2-2", true);
        Smap.addElement(actuators1, "a3-2", true);

        JsonArray actuators2 = new JsonArray();
        Smap.addElement(actuators2, "a1-3", true);
        Smap.addElement(actuators2, "a2-3", true);
        Smap.addElement(actuators2, "a3-3", true);

        JsonObject actuator_obj = new JsonObject();
        actuator_obj.addProperty("active", true);
        actuator_obj.addProperty("type", "list");
        actuator_obj.add("elements", actuators.deepCopy());

        JsonObject actuator_obj1 = new JsonObject();
        actuator_obj1.addProperty("active", true);
        actuator_obj1.addProperty("type", "list");
        actuator_obj1.add("elements", actuators1.deepCopy());

        JsonObject actuator_obj2 = new JsonObject();
        actuator_obj2.addProperty("active", true);
        actuator_obj2.addProperty("type", "list");
        actuator_obj2.add("elements", actuators2.deepCopy());

        JsonArray actuator_elements = new JsonArray();
        actuator_elements.add(actuator_obj.deepCopy());
        actuator_elements.add(actuator_obj1.deepCopy());
        actuator_elements.add(actuator_obj2.deepCopy());

        JsonObject actuator_select_obj = new JsonObject();
        actuator_select_obj.addProperty("child_name", "Num Actuator Selection");
        actuator_select_obj.addProperty("child_type", "DownSelecting");
        actuator_select_obj.add("elements", actuator_elements);



        JsonArray final_ary = new JsonArray();
        final_ary.add(sensor_select_obj);
        final_ary.add(computer_select_obj);
        final_ary.add(actuator_select_obj);


        return final_ary;
    }




    public static JsonArray getRootParameters(){


//   _____
//  / ____|
// | (___   ___ _ __  ___  ___  _ __ ___
//  \___ \ / _ \ '_ \/ __|/ _ \| '__/ __|
//  ____) |  __/ | | \__ \ (_) | |  \__ \
// |_____/ \___|_| |_|___/\___/|_|  |___/

        // GENERIC SENSORS
        JsonObject gs1 = new JsonObject();
        gs1.addProperty("active", Boolean.TRUE);
        gs1.addProperty("type", "item");
        gs1.addProperty("id", "0");
        gs1.addProperty("name", "SENSOR");
        JsonObject gs2 = new JsonObject();
        gs2.addProperty("active", Boolean.TRUE);
        gs2.addProperty("type", "item");
        gs2.addProperty("id", "1");
        gs2.addProperty("name", "SENSOR");
        JsonObject gs3 = new JsonObject();
        gs3.addProperty("active", Boolean.TRUE);
        gs3.addProperty("type", "item");
        gs3.addProperty("id", "2");
        gs3.addProperty("name", "SENSOR");
        JsonObject gs4 = new JsonObject();
        gs4.addProperty("active", Boolean.TRUE);
        gs4.addProperty("type", "item");
        gs4.addProperty("id", "3");
        gs4.addProperty("name", "SENSOR");
        JsonObject gs5 = new JsonObject();
        gs5.addProperty("active", Boolean.TRUE);
        gs5.addProperty("type", "item");
        gs5.addProperty("id", "4");
        gs5.addProperty("name", "SENSOR");

        // GENERIC SENSOR ELEMENTS
        JsonArray gsElements = new JsonArray();
        gsElements.add(gs1);
        gsElements.add(gs2);
        gsElements.add(gs3);
        gsElements.add(gs4);
        gsElements.add(gs5);

        // GENERIC SENSOR OBJECT
        JsonObject gsObject = new JsonObject();
        gsObject.addProperty("child_name", "Sensor Selection");
        gsObject.addProperty("child_type", "DownSelecting");
        gsObject.add("elements", gsElements);



//   _____                             _______
//  / ____|                           |__   __|
// | (___   ___ _ __  ___  ___  _ __     | |_   _ _ __   ___  ___
//  \___ \ / _ \ '_ \/ __|/ _ \| '__|    | | | | | '_ \ / _ \/ __|
//  ____) |  __/ | | \__ \ (_) | |       | | |_| | |_) |  __/\__ \
// |_____/ \___|_| |_|___/\___/|_|       |_|\__, | .__/ \___||___/
//                                           __/ | |
//                                          |___/|_|

        JsonObject st1 = new JsonObject();
        st1.addProperty("active", Boolean.TRUE);
        st1.addProperty("type", "item");
        st1.addProperty("id", "0");
        st1.addProperty("name", "ACTIVE");
        JsonObject st2 = new JsonObject();
        st2.addProperty("active", Boolean.TRUE);
        st2.addProperty("type", "item");
        st2.addProperty("id", "1");
        st2.addProperty("name", "PASSIVE");

        JsonArray stElements = new JsonArray();
        stElements.add(st1);
        stElements.add(st2);

        JsonObject stObject = new JsonObject();
        stObject.addProperty("child_name", "Sensor Type Selection");
        stObject.addProperty("child_type", "StandardForm");
        stObject.add("elements", stElements);

//
//   _____                            _
//  / ____|                          | |
// | |     ___  _ __ ___  _ __  _   _| |_ ___ _ __ ___
// | |    / _ \| '_ ` _ \| '_ \| | | | __/ _ \ '__/ __|
// | |___| (_) | | | | | | |_) | |_| | ||  __/ |  \__ \
//  \_____\___/|_| |_| |_| .__/ \__,_|\__\___|_|  |___/
//                       | |
//                       |_|


        JsonObject gc1 = new JsonObject();
        gc1.addProperty("active", Boolean.TRUE);
        gc1.addProperty("type", "item");
        gc1.addProperty("id", "0");
        gc1.addProperty("name", "COMPUTER");
        JsonObject gc2 = new JsonObject();
        gc2.addProperty("active", Boolean.TRUE);
        gc2.addProperty("type", "item");
        gc2.addProperty("id", "1");
        gc2.addProperty("name", "COMPUTER");
        JsonObject gc3 = new JsonObject();
        gc3.addProperty("active", Boolean.TRUE);
        gc3.addProperty("type", "item");
        gc3.addProperty("id", "2");
        gc3.addProperty("name", "COMPUTER");

        JsonArray gcElements = new JsonArray();
        gcElements.add(gc1);
        gcElements.add(gc2);
        gcElements.add(gc3);

        JsonObject gcObject = new JsonObject();
        gcObject.addProperty("child_name", "Computer Selection");
        gcObject.addProperty("child_type", "DownSelecting");
        gcObject.add("elements", gcElements);

//   _____                            _              _______
//  / ____|                          | |            |__   __|
// | |     ___  _ __ ___  _ __  _   _| |_ ___ _ __     | |_   _ _ __   ___  ___
// | |    / _ \| '_ ` _ \| '_ \| | | | __/ _ \ '__|    | | | | | '_ \ / _ \/ __|
// | |___| (_) | | | | | | |_) | |_| | ||  __/ |       | | |_| | |_) |  __/\__ \
//  \_____\___/|_| |_| |_| .__/ \__,_|\__\___|_|       |_|\__, | .__/ \___||___/
//                       | |                               __/ | |
//                       |_|                              |___/|_|


        JsonObject ct1 = new JsonObject();
        ct1.addProperty("active", Boolean.TRUE);
        ct1.addProperty("type", "item");
        ct1.addProperty("id", "0");
        ct1.addProperty("name", "WINDOWS");
        JsonObject ct2 = new JsonObject();
        ct2.addProperty("active", Boolean.TRUE);
        ct2.addProperty("type", "item");
        ct2.addProperty("id", "1");
        ct2.addProperty("name", "MAC");
        JsonObject ct3 = new JsonObject();
        ct3.addProperty("active", Boolean.TRUE);
        ct3.addProperty("type", "item");
        ct3.addProperty("id", "2");
        ct3.addProperty("name", "LINUX");

        JsonArray ctElements = new JsonArray();
        ctElements.add(ct1);
        ctElements.add(ct2);
        ctElements.add(ct3);

        JsonObject ctObject = new JsonObject();
        ctObject.addProperty("child_name", "Computer Type Selection");
        ctObject.addProperty("child_type", "StandardForm");
        ctObject.add("elements", ctElements);



//               _               _
//     /\       | |             | |
//    /  \   ___| |_ _   _  __ _| |_ ___  _ __ ___
//   / /\ \ / __| __| | | |/ _` | __/ _ \| '__/ __|
//  / ____ \ (__| |_| |_| | (_| | || (_) | |  \__ \
// /_/    \_\___|\__|\__,_|\__,_|\__\___/|_|  |___/


        JsonObject ga1 = new JsonObject();
        ga1.addProperty("active", Boolean.TRUE);
        ga1.addProperty("type", "item");
        ga1.addProperty("id", "0");
        ga1.addProperty("name", "ACTUATOR");
        JsonObject ga2 = new JsonObject();
        ga2.addProperty("active", Boolean.TRUE);
        ga2.addProperty("type", "item");
        ga2.addProperty("id", "1");
        ga2.addProperty("name", "ACTUATOR");
        JsonObject ga3 = new JsonObject();
        ga3.addProperty("active", Boolean.TRUE);
        ga3.addProperty("type", "item");
        ga3.addProperty("id", "2");
        ga3.addProperty("name", "ACTUATOR");
        JsonObject ga4 = new JsonObject();
        ga4.addProperty("active", Boolean.TRUE);
        ga4.addProperty("type", "item");
        ga4.addProperty("id", "3");
        ga4.addProperty("name", "ACTUATOR");

        JsonArray gaElements = new JsonArray();
        gaElements.add(ga1);
        gaElements.add(ga2);
        gaElements.add(ga3);
        gaElements.add(ga4);

        JsonObject gaObject = new JsonObject();
        gaObject.addProperty("child_name", "Actuator Selection");
        gaObject.addProperty("child_type", "DownSelecting");
        gaObject.add("elements", gaElements);



//               _               _               _______
//     /\       | |             | |             |__   __|
//    /  \   ___| |_ _   _  __ _| |_ ___  _ __     | |_   _ _ __   ___  ___
//   / /\ \ / __| __| | | |/ _` | __/ _ \| '__|    | | | | | '_ \ / _ \/ __|
//  / ____ \ (__| |_| |_| | (_| | || (_) | |       | | |_| | |_) |  __/\__ \
// /_/    \_\___|\__|\__,_|\__,_|\__\___/|_|       |_|\__, | .__/ \___||___/
//                                                     __/ | |
//                                                    |___/|_|

        JsonObject at1 = new JsonObject();
        at1.addProperty("active", Boolean.TRUE);
        at1.addProperty("type", "item");
        at1.addProperty("id", "0");
        at1.addProperty("name", "ELECTRIC");
        JsonObject at2 = new JsonObject();
        at2.addProperty("active", Boolean.TRUE);
        at2.addProperty("type", "item");
        at2.addProperty("id", "1");
        at2.addProperty("name", "PNEUMATIC");
        JsonObject at3 = new JsonObject();
        at3.addProperty("active", Boolean.TRUE);
        at3.addProperty("type", "item");
        at3.addProperty("id", "2");
        at3.addProperty("name", "SPRING");

        JsonArray atElements = new JsonArray();
        atElements.add(at1);
        atElements.add(at2);
        atElements.add(at3);

        JsonObject atObject = new JsonObject();
        atObject.addProperty("child_name", "Actuator Type Selection");
        atObject.addProperty("child_type", "StandardForm");
        atObject.add("elements", atElements);


//  ____        _ _     _
// |  _ \      (_) |   | |
// | |_) |_   _ _| | __| |
// |  _ <| | | | | |/ _` |
// | |_) | |_| | | | (_| |
// |____/ \__,_|_|_|\__,_|


        JsonArray final_ary = new JsonArray();
        final_ary.add(gsObject);
        final_ary.add(stObject);
        final_ary.add(gcObject);
        final_ary.add(ctObject);
        final_ary.add(gaObject);
        final_ary.add(atObject);

        return final_ary;
    }






}
