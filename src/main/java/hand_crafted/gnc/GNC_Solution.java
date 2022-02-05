package hand_crafted.gnc;

import org.moeaframework.core.Solution;

public class GNC_Solution extends Solution {

    public GNC_Model model;
    public boolean already_evaluated;

    public GNC_Solution(GNC_Model model){
        super(1, 2, 0);
        this.model = model;
        this.already_evaluated = false;
    }

    public GNC_Solution(){
        super(1, 2, 0);
        this.model = new GNC_Model();
        this.already_evaluated = false;
    }


    // COPY CONSTRUCTOR CALLED IN CROSSOVER
    protected GNC_Solution(Solution solution){
        super(solution);

        GNC_Solution design = (GNC_Solution) solution;
        this.model = new GNC_Model(design.model);
        this.already_evaluated = design.already_evaluated;
    }



    public void print(){
        this.model.print();
    }
}
