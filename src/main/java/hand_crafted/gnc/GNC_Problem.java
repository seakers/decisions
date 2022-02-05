package hand_crafted.gnc;

import moea.solutions.ADDSolution;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import java.util.ArrayList;

public class GNC_Problem extends AbstractProblem {

    public ArrayList<Double> sensors;
    public ArrayList<Double> computers;
    public ArrayList<Double> actuators;
    public double connection_success_rate;


    public GNC_Problem(){
        super(1, 2);


        this.connection_success_rate = 1;

        this.sensors   = new ArrayList<>();
        sensors.add(0.9985);
        sensors.add(0.999);
        sensors.add(0.9995);

        this.computers = new ArrayList<>();
        computers.add(0.999);
        computers.add(0.9996);
        computers.add(0.9998);

        this.actuators = new ArrayList<>();
        actuators.add(0.9992);
        actuators.add(0.998);
        actuators.add(0.999);
    }

    @Override
    public void evaluate(Solution sltn){
        System.out.println("--> EVALUATING DESIGN");

        // CAST
        GNC_Solution design = (GNC_Solution) sltn;

        if(!design.already_evaluated){

            // EVALUATION
            ArrayList<Double> results = this.evaluate_gnc(design.model);
            double mass = results.get(0);
            double reliability = results.get(1);

            // SET OBJECTIVE VALUES
            design.setObjective(0, -reliability);
            design.setObjective(1, mass);
            design.already_evaluated = true;
        }
    }




    public ArrayList<Double> evaluate_gnc(GNC_Model model){
        ArrayList<Double> results = new ArrayList<>();







        return results;
    }








    @Override
    public Solution newSolution(){
        return new GNC_Solution();
    }
}
