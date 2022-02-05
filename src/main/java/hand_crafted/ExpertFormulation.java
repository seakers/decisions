package hand_crafted;

import hand_crafted.gnc.GNC_GA;
import hand_crafted.gnc.GNC_Model;
import hand_crafted.gnc.GNC_Solution;

public class ExpertFormulation {


    public ExpertFormulation(){

    }

    public void run_tests(){


        GNC_GA ga = new GNC_GA(20, 200, 0.2);
        ga.print_solutions();
        ga.run();






        System.exit(0);
    }
}
