package com.udea.physhdsl.solver;

import com.udea.physhdsl.ParamInformation;
import com.udea.physhdsl.model.QAPModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Metaheuristic {
	private static final Logger LOGGER = Logger.getLogger( Metaheuristic.class.getName() );
    
    //protected move =new MovePermutation(-1n, -1n);

    public enum Type {
        AS(0), EO(1), ROT(2);
        private static final Map<Integer, Type> mappingMap = new HashMap<Integer, Type>();
        static {
            for (Type m : Type.values()) {
                mappingMap.put(m.getValue(), m);
            }
        }
        private final int type;
        Type(int aType) {
            type = aType;
        }
        public int getValue() {
            return type;
        }
        public static Type getByType(int aType) {
            return mappingMap.get(aType);
        }
    }

    //Random random;
    protected int nSwap;
    protected QAPModel problemModel;

    protected Type mySolverType;
    public int[] variables;
    protected MovePermutation move = new MovePermutation(-1, -1);
    protected int size;

    public Metaheuristic(int size) {
        this.size = size;
        variables = new int[size];
        //random = new Random();
    }


    public static Metaheuristic make(Type MHtype, int size){
        switch (MHtype){
            case EO:
                return new EOSearch(size);
            case ROT:
                return new RoTSearch(size);
            default:
                return new AdaptiveSearch(size);
        }
    }

    public void setMySolverType(Type mySolverType) {
        this.mySolverType = mySolverType;
    }

    public Type getMySolverType() {
        return mySolverType;
    }

    public void configHeuristic(QAPModel problemModel, Map<String, Object> configuration) {
        this.problemModel = problemModel;    
    }

    /**
     * Search process (in loop functionality)
     * To be overwrited for each child class (solver)
     */
    public int search(int currentCost, int bestCost, int nIter) {
        // Swap two random variables
        int size = problemModel.getSize();
        move.setFirst(ThreadLocalRandom.current().nextInt(problemModel.getSize()));
        move.setSecond(ThreadLocalRandom.current().nextInt(problemModel.getSize()));
        swapVariables(move.getFirst(), move.getSecond());
        nSwap++;
        problemModel.executedSwap(move.getFirst(), move.getSecond(), variables);
        /*if(costo < currentCost){
 			System.out.print("Costo (RandomSearch): " + costo + ". Con variables: ");
 			System.out.print("\n");
 		}*/
        return problemModel.costOfSolution(size, true, variables);
    }

    public void initVar() {
        nSwap = 0;
    }

    public void applyLS() {
    }

    public void clearNSwap() {
        this.nSwap = 0;
    }

    public int getNSwap() {
        return nSwap;
    }

    public int[] getVariables() {
        //System.out.println("Nodo: " + here.id + " pidiendo variables para crear State");
        return this.variables;
    }

    public void setVariables(int[] variables) {
        this.variables = variables;
    }

    public void swapVariables(int i, int j) {
        int x = variables[i];
        variables[i] = variables[j];
        variables[j] = x;
    }

    //Jason: Migration
    public boolean tryInsertIndividual(int[] variables, int size) {
        LOGGER.log(Level.SEVERE, "BadInvocation of tryInsertIndividual, this its not a GA Heuristic");
        return false;
    }

    public int getWorstCost() {
    	LOGGER.log(Level.SEVERE, "BadInvocation of getWorstCost, this its not a GA Heuristic");
        return Integer.MAX_VALUE;
    }

    //Jason: Migration
    public int[] getConfigForPop(boolean replace) {
    	LOGGER.log(Level.SEVERE, "BadInvocation of getConfigForPop, this is not a GA Heuristic");
        return null;
    }

    /*public getRandomConfigSol():ConfigSol
    {
        System.out.println("BadInvocation of getRandomConfigSol, this its not a GA Heuristic");
        return null;
    }*/

    public boolean launchEventForStagnation() {
        return false;//System.out.println("Error. launchEventForStagnation() invokation in HeuristicSolver");
    }

    public void setKill(boolean value) {

    }

    public void displayInfo(String s) {
    	LOGGER.log(Level.SEVERE, "Error. displayInfo() invokation in HeuristicSolver");
    }

    public int reset(int n, int totalCost) {
        while (n-- != 0) {
            int i = ThreadLocalRandom.current().nextInt(variables.length);
            int j = ThreadLocalRandom.current().nextInt(variables.length);
            swapVariables(i, j);
            //System.out.println("Este es el While problematico");
        }
        return -1;
    }

    public void restartVar() {

    }

    public void initVariables() {
    	LOGGER.log(Level.FINE, mySolverType.toString()+": initilizing model");
        int baseValue = 0;
        for (int i = 0; i < variables.length; i++){
            variables[i] = baseValue + i;
        }
        for (int i = size - 1 ; i > 0; i--) {
            int j = ThreadLocalRandom.current().nextInt(i + 1);
            int x = variables[i];
            variables[i] = variables[j];
            variables[j] = x;
        }
    }

    public void clearProblemModel() {
        problemModel.clearProblemModel();
    }

    /*public void createNewSol() {
        val conf = this.problemModel.createNewSol();
        //System.out.println("at Heuristic Sover created Solution: " + conf);
        return conf;
    }*/

    /**
     * Método agregado para Migración, para crear una nueva solución cuando en el stackForDiv
     * no hay nada
     * su solución
     */
    //public int[] createNewConf() {
    //    return this.problemModel.initialize(this.random.nextInt());
    //}

    public int costOfSolution() {
        return problemModel.costOfSolution(size, true, variables);
    }

    public int getSizeProblem() {
        return problemModel.getSize();
    }

    public double getDistance(int[] a, int[] b) {
        //val size = this.problemModel.size;
        return problemModel.distance(size, a, b);
    }

    public boolean verify(int[] conf) {
        //val size = this.problemModel.size;
        return problemModel.verify(size, conf);
    }
    
    class MovePermutation {
        private int first = -1;
        private int second = -1;

        public MovePermutation(int f, int s){
            this.first = f;
            this.second = s;
        }

        public int getFirst() {
            return this.first;
        }
        public int getSecond() {
            return this.second;
        }
        public void setFirst(int f){
            this.first = f;
        }
        public void setSecond(int s){
            this.second = s;
        }
    }
    
    public void adaptParameters(ParamInformation paramInfo, double divPercentageLimit) {
    	LOGGER.log(Level.WARNING, "must be overwritten by each descendant");
    }

}

