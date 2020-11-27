package moea.solutions;

import org.moeaframework.core.Solution;

public class InfoSolution extends Solution {

    public String design_str;

    public InfoSolution(int num_variables, int num_objectives, String design_str){
        super(num_variables, num_objectives);
        this.design_str = design_str;
    }

}
