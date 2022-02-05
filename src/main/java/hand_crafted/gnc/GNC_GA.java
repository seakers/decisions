package hand_crafted.gnc;

import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.ParetoObjectiveComparator;
import org.moeaframework.core.operator.InjectedInitialization;
import org.moeaframework.core.operator.TournamentSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GNC_GA implements Runnable{

    public Problem problem;
    public double mutation_probability;
    public int num_evaluations;
    public List<Solution> solutions;
    public EpsilonMOEA eMOEA;

    public GNC_GA(int initial_pop_size, int num_evaluations, double mutation_probability){
        this.mutation_probability = mutation_probability;
        this.num_evaluations = num_evaluations;

        // CREATE PROBLEM
        this.problem = new GNC_Problem();

        // INITIALIZE SOLUTIONS
        this.solutions = new ArrayList<>(initial_pop_size);
        for(int x = 0; x < initial_pop_size; x++){
            GNC_Solution soln = new GNC_Solution();
            soln.already_evaluated = false;
            this.solutions.add(soln);
        }
    }

    public void print_solutions(){
        for(Solution soln: this.solutions){
            ((GNC_Solution) soln).print();
        }
    }


    public void initialize(){

        InjectedInitialization initialization = new InjectedInitialization(this.problem, this.solutions.size(), this.solutions);

        double[]                   epsilonDouble = new double[]{0.001, 1};
        Population                 population    = new Population();
        EpsilonBoxDominanceArchive archive       = new EpsilonBoxDominanceArchive(epsilonDouble);

        ChainedComparator   comp      = new ChainedComparator(new ParetoObjectiveComparator());
        TournamentSelection selection = new TournamentSelection(2, comp);

        GNC_Crossover crossover = new GNC_Crossover(this.mutation_probability);

        this.eMOEA = new EpsilonMOEA(this.problem, population, archive, selection, crossover, initialization);
    }


    public void run(){

        this.initialize();

        // SUBMIT MOEA
        ExecutorService              pool   = Executors.newFixedThreadPool(1);
        CompletionService<Algorithm> ecs    = new ExecutorCompletionService<>(pool);
        ecs.submit(new GNC_Search(this.eMOEA, this.num_evaluations));


        try {
            org.moeaframework.core.Algorithm alg = ecs.take().get();
            NondominatedPopulation result = alg.getResult();

        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }

        pool.shutdown();
        System.out.println("--> FINISHED");
    }











}
