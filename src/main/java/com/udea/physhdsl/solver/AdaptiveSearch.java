package com.udea.physhdsl.solver;

import com.udea.physhdsl.ParamInformation;
import com.udea.physhdsl.model.QAPModel;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class AdaptiveSearch extends Metaheuristic{
    private int[] mark;
    private int listInb;

    private MovePermutation[] listIJ;
    private int[] listI;

    private int nVarMarked = 0;

    /**	Statistics	*/
    private int nReset;
    private int nSameVar;
    private int nLocalMin;

    /** Total Statistics */
    private int nResetTot;
    private int nSameVarTot;
    private int nLocalMinTot;
    private int nInPlateau;

    /** Parameters of the AS solver */
    private int nVarToReset;
    private int resetPercent;
    private boolean exhaustive;
    private int freezeLocMin;
    private int freezeSwap;
    private int resetLimit;
    private int probSelectLocMin;
    private boolean firstBest;

    public AdaptiveSearch(int size){
        super(size);
        mySolverType = Type.AS;
    }

    public void configHeuristic(QAPModel problemModel, Map<String, Object> configuration){
        super.configHeuristic(problemModel, configuration);
        mark = new int[problemModel.getSize()];
        listIJ = new MovePermutation[problemModel.getSize()];
        listI = new int[problemModel.getSize()];
        nVarToReset = -1; //opts("--AS_varToReset",-1);
        
        Object valOrNull = configuration.get("AS.resetPercent");
        this.resetPercent = valOrNull == null ? 10 : Integer.parseInt((String) valOrNull);
        valOrNull = configuration.get("AS.freezeLocMin");
        this.freezeLocMin = valOrNull == null ? 5 : Integer.parseInt((String) valOrNull); 
        freezeSwap = 5; //opts("--AS_freezeSwap",5n);
        resetLimit = 5; //opts("--AS_resetLimit",5n);
        probSelectLocMin = 60; //opts("--AS_probSelecLocMin", 0n);
        firstBest = false; //opts("--AS_firstBest",0n) == 1n;
        exhaustive = false; //opts("--AS_exhaustive",0n) == 1n;
    }


    public void initVar(){
        super.initVar();
        if (nVarToReset == -1){
            nVarToReset = (((problemModel.getSize() * resetPercent) + 99) / 100);
            if (nVarToReset < 2){
                nVarToReset = 2;
            }
        }
        Arrays.fill(mark, 0);
        Arrays.fill(listI, 0);
        Arrays.fill(listIJ, null);
        //nRestart = 0n;
        nSameVar = 0;
        nLocalMin = 0;
        nReset = 0;
        nInPlateau = 0;
        nResetTot = 0;
        nSameVarTot = 0;
        nLocalMinTot = 0;
    }

    /**
     *  Search process (in loop functionality)
     *  To be overwrited for each child class (solver) 
     */
    public int search(int currentCost, int bestCost, int nIter) {
        int newCost = -1;
        //sz = this.problemModel.size;
        if(!exhaustive){
            selectVarHighCost();
            newCost = selectVarMinConflict(currentCost);
        } else {
            newCost = selectVarsToSwap(currentCost);
        }
        if (currentCost != newCost) {
            nInPlateau = 0;
        }
        nInPlateau++;
        int returnCost = currentCost;
        if (move.getFirst() == move.getSecond()){
            nLocalMin++;
            mark[move.getFirst()] = nSwap + freezeLocMin; //Mark(maxI, freeze_loc_min);

            if (nVarMarked + 1 >= resetLimit){
                onLocMin();
                returnCost = doReset(nVarToReset, currentCost);//doReset(nb_var_to_reset,csp);
                //returnCost = currentCost;
            }
            //return otherCost;
        }else{
            mark[move.getFirst()] = nSwap + freezeSwap; //Mark(maxI, ad.freeze_swap);
            mark[move.getSecond()] = nSwap + freezeSwap; //Mark(minJ, ad.freeze_swap);
            swapVariables(move.getFirst(), move.getSecond()); //adSwap(maxI, minJ,csp);
            nSwap++;
            problemModel.executedSwap(move.getFirst(), move.getSecond(), variables);
            //currentCost = newCost;
            returnCost = newCost ;
        }
        //sz = problemModel.getSize();
        //variables:Rail[Int] = new Rail[Int](sz as Int, 0n);
        //Rail.copy(problemModel.getVariables(), variables as Valuation(sz));
 		/*if(returnCost < currentCost){
 			Console.OUT.print("Costo (AdaptiveSearch): " + returnCost);
 			Utils.show(". Con variables: " , super.variables);
 		}*/
        //displaySolution();
        //Console.OUT.print("\n");
        return returnCost;
    }

    public void restartVar(){
        //super.restartVar();
        Arrays.fill(mark, 0);
    }

    private int doReset(int n, int currentCost) {
        int cost = -1;		//reset(n, csp);
        cost = reset(n, currentCost);
        Arrays.fill(mark, 0);
        nReset++;
        return (cost < 0) ? this.problemModel.costOfSolution(size, true, variables) : cost; //Arg costofsol(1)
    }

    /**
     * 	SelectVarHighCost 
     * 	Select the maximum cost variable of the problem 
     *   (Modify the first variable of the move object )
     *   Also computes the number of marked variables.
     *
     *
     */
    private void selectVarHighCost(){
        int i =-1;
        int maxCost = 0;
        int maxVar = -1;
        listInb = 0; //Number of elements
        nVarMarked = 0;

        while((i = problemModel.nextI(i)) < problemModel.getSize()) { //False if i < 0
            if (nSwap < mark[i]) {
                nVarMarked++;
                continue;
            }
            int x = problemModel.costOnVariable(i);
            if (x >= maxCost){
                if (x > maxCost){
                    maxCost = x;
                    listInb = 0;
                }
                listI[listInb++] = i;
            }
        }
        if (listInb == 0) // all variables are OK but the global cost is > 0 (can occur in SMTI with no BP but singles)
        maxVar = ThreadLocalRandom.current().nextInt(problemModel.getSize());
 		else {
            // select a maxCost variable from array
            int sel = ThreadLocalRandom.current().nextInt(listInb);
            maxVar = listI[sel]; //This maxI must be local or only returns the value
        }
        nSameVar += listInb;
        move.setFirst(maxVar);
    }

    /**
     * 	selectVarMinConflict( csp : ModelAS) : Int
     * 	Computes swap and selects the minimum of cost if swap
     *
     * 	@return new cost of the possible move
     */
    private int selectVarMinConflict(int currentCost) {
        int j;
        int cost;
        boolean flagOut;
        int second = -1;
        int nCost;
        int first = move.getFirst();

        do {
            flagOut = false;
            int listJnb = 0;
            nCost = currentCost;
            j = -1;
            while(++j < problemModel.getSize()){// false if j < 0 //solverP.exhaustive???
                if (nSwap < mark[j]) {
                    continue;
                }
                cost = problemModel.costIfSwap(currentCost, j, first);
                if (probSelectLocMin <= 100 && j == first) continue;

                if (cost < nCost){
                    listJnb = 1;
                    nCost = cost;
                    second = j;
                    if (firstBest){
                        move.setSecond(second);
                        return nCost;
                    }
                } else if (cost == nCost){
                    if (ThreadLocalRandom.current().nextInt(++listJnb) == 0)
                        second = j;
                }
            }

            if (probSelectLocMin <= 100) {
                if (nCost >= currentCost && (ThreadLocalRandom.current().nextInt(100) < probSelectLocMin || (listInb <= 1 && listJnb <= 1))) {
                    second = first;
                    move.setSecond(second);
                    return nCost;
                }
                if (listJnb == 0) {
                    //this.nIter++;
                    int sel = ThreadLocalRandom.current().nextInt(listInb);
                    first = listI[sel];
                    move.setFirst(first);
                    flagOut = true;
                }
            }
        } while(flagOut);
        move.setSecond(second);
        return nCost;
    }

    /**
     *  Computes maxI and minJ, the 2 variables to swap.
     *  All possible pairs are tested exhaustively.
     *
     *  @return new cost of the possible move
     */
    private int selectVarsToSwap(int currentCost) {
        int first;
        int second;
        //x : Int;
        /** For Exhaustive search */
        int nListIJ = 0;
        int nCost = Integer.MAX_VALUE ;
        nVarMarked = 0;
        first = -1;

        while((first = problemModel.nextI(first)) < problemModel.getSize()) {
            if (nSwap < mark[first]) {
                nVarMarked++;
                continue;
            }
            second = -1;
            while((second = problemModel.nextJ(first, second, true)) < problemModel.getSize()){
                if (nSwap < mark[second]) {
                    continue;
                }
                int x = problemModel.costIfSwap(currentCost, first, second);
                if (x <= nCost) {
                    if (x < nCost) {
                        nCost = x;
                        nListIJ = 0;
                        if (firstBest && x < currentCost) {
                            move.setFirst(first);
                            move.setSecond(second);
                            return nCost;
                        }
                    }
                    listIJ[nListIJ] = new MovePermutation(first,second);
                    nListIJ = ((nListIJ + 1) % problemModel.getSize());
                }
            }
        }

        nSameVar += nListIJ;
        if (nCost >= currentCost) {
            if (nListIJ == 0 || (( probSelectLocMin <= 100) && ThreadLocalRandom.current().nextInt(100) < probSelectLocMin)) {
                int i;
                for(i = 0; nSwap < mark[i]; i++){}
                move.setFirst(i);
                move.setSecond(i);
                return nCost;//goto end;
            }

            int lm;
            if (!(probSelectLocMin <= 100) && (lm = ThreadLocalRandom.current().nextInt(nListIJ + problemModel.getSize())) < problemModel.getSize()) {
                move.setFirst(lm);
                move.setSecond(lm);
                return nCost;//goto end;
            }
        }

        int sel = ThreadLocalRandom.current().nextInt(nListIJ);
        move.setFirst(listIJ[sel].getFirst());
        move.setSecond(listIJ[sel].getSecond());
        return nCost;
    }

    /**
     * 	Report statistics from the solving process
     */
    /*public void reportStats( GlobalStats c){
        //super.reportStats(c);
        c.locmin = this.nLocalMinTot;
        c.reset = this.nResetTot;
        c.same = this.nSameVarTot;
    }*/

    protected void updateTotStats(){
        //super.updateTotStats();
        nResetTot += nReset;
        nSameVarTot += nSameVar;
        nLocalMinTot += nLocalMin;
        nSameVar = 0;
        nLocalMin = 0;
        nReset = 0;
    }

    /**
     *  Interact when Loc min is reached
     */
    private void onLocMin(){
        // communicate Local Minimum
        //solver.communicateLM( this.currentCost, cop.getVariables() as Valuation(sz));
        //solverState = createSolverState();
        //solver.communicateLM( new State(sz,this.currentCost, cop.getVariables() as Valuation(sz), here.id as Int, solverState) );
    }
    
    public void adaptParameters(ParamInformation paramInfo) {
    	//TODO: Adapt Parameters according to  info received
    }
}
