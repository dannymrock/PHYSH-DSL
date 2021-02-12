package com.udea.physhdsl.solver;

import com.udea.physhdsl.ParamInformation;
import com.udea.physhdsl.model.QAPModel;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoTSearch extends Metaheuristic{

	private static final Logger LOGGER = Logger.getLogger( RoTSearch.class.getName() );
	private double tabuDurationFactorUS;
    private double aspirationFactorUS;
    private double tabuDurationFactor;
    private double aspirationFactor;
    private int tabuDuration;
    private int aspiration;

    private boolean autorized;
    private boolean aspired;
    private boolean alreadyAspired;

    /** Tabu List Matrix */
    private int[][] tabuList;

    //tdl = 0.9;
    //private double tdl = 0.2;
    //private double tdu = 1.8;
    
    private double tdl = 0.2;
    private double tdu = 8;

    private double al = 2.0;
    private double au = 5.0;

    public RoTSearch(int size){
        super(size);
        mySolverType = Type.ROT;
    }

    public void configHeuristic(QAPModel problemModel, Map<String, Object> configuration){
        super.configHeuristic(problemModel, configuration);
        tabuList = new int[problemModel.getSize()][problemModel.getSize()];
        
        
        Object valOrNull = configuration.get("RoTS.tabuDurationFactor");
        tabuDurationFactorUS = valOrNull == null ? 8 : Double.parseDouble((String) valOrNull);
        valOrNull = configuration.get("RoTS.aspirationFactor");
        aspirationFactorUS = valOrNull == null ? 5 : Double.parseDouble((String) valOrNull);
    }

    //private int tabuDurationLower;
    //private int tabuDurationUpper;

    /**
     *  Initialize variables of the solver
     *  Executed once before the main solving loop
     */
    public void initVar(){
        super.initVar();
        for(int x = 0; x < tabuList.length; x++){
            Arrays.fill(tabuList[x], 0);
        }
        if (tabuDurationFactorUS < 0){
            //tabuDurationFactor = -tabuDurationFactorUS;
            tabuDurationFactor  = tdl + (tdu - tdl) * ThreadLocalRandom.current().nextDouble();
        } else {
            tabuDurationFactor = tabuDurationFactorUS;
        }
        //Console.OUT.println("this.tabuDurationFactor: " + this.tabuDurationFactor);
        tabuDuration = (int)(tabuDurationFactor * problemModel.getSize());
        LOGGER.log(Level.INFO, "tabuDuration Factor: "+tabuDurationFactor+" tabuDuration: " + tabuDuration);
        
        if (aspirationFactorUS == -1.0)
            aspirationFactor = al + (au - al) * ThreadLocalRandom.current().nextDouble();
        else
            aspirationFactor = aspirationFactorUS;
        LOGGER.log(Level.INFO, "aspirationFactor: " + this.aspirationFactor);

        aspiration = (int) (aspirationFactor * problemModel.getSize() * problemModel.getSize());
        for (int i = 0 ; i < problemModel.getSize(); i++){
            for (int j = 0 ; j < problemModel.getSize(); j++){
                this.tabuList[i][j] = -(problemModel.getSize() * i + j);
            }
        }
    }

    public int search(int currentCost, int bestCost, int nIter) {
        int i, j;
        int newCost;
        int delta;
        int minDelta = Integer.MAX_VALUE;
        move.setFirst(Integer.MAX_VALUE);
        move.setSecond(Integer.MAX_VALUE);
        alreadyAspired = false;

        //Utils.show("Solution",super.variables);

        for (i = 0; i < problemModel.getSize() - 1; i++){
            for (j = i + 1; j < problemModel.getSize(); j++) {
                newCost = problemModel.costIfSwap(currentCost,i,j);
                //System.out.println("Costifswap in RoTS " + i + "," + j + ": " + newCost);
                delta = newCost - currentCost;

                autorized =
                        (tabuList [i][variables[j]] < nIter) ||
                                (tabuList [j][variables[i]] < nIter);

                aspired =
                        (tabuList[i][variables[j]] < (nIter - aspiration)) ||
                                (tabuList[j][variables[i]] < (nIter - aspiration)) ||
                                (newCost < bestCost);

                if ((aspired && !alreadyAspired) ||	/* first move aspired */
                        (aspired && alreadyAspired &&	/* many move aspired */
                                (delta <= minDelta)) ||	/* => take best one */
                        (!aspired && !alreadyAspired &&	/* no move aspired yet */
                                (delta <= minDelta) && autorized)) {

                    //   #ifdef USE_RANDOM_ON_BEST
                    //   if (delta[i][j] == min_delta){
                    // 		if (Random(++best_nb) > 0)
                    // 			 continue;
                    //   }
                    //   else
                    // 		best_nb = 1;
                    //   #endif

                    move.setFirst(i);
                    move.setSecond(j);
                    minDelta = delta;

                    // #ifdef FIRST_BEST
                    // if (current_cost + min_delta < best_cost)
                    // goto found;
                    // #endif

                    if (aspired)
                        alreadyAspired = true;
                }
            }
        }


        if(move.getFirst() == Integer.MAX_VALUE){
            LOGGER.log(Level.INFO,"All moves are tabu!");
            return currentCost;
        }else{
            //System.out.println("swap pos "+move.getFirst()+" "+move.getSecond());
            swapVariables(move.getFirst(), move.getSecond()); //adSwap(maxI, minJ,csp);	
            nSwap++;
            //sz =  super.problemModel.size;
            problemModel.executedSwap(move.getFirst(), move.getSecond(), variables);
            /* forbid reverse move for a random number of iterations */

            //tabuList( move.getFirst(), cop_.variables(move.getSecond())) = this.nIter + (cube() * this.tabuDuration) as Int;
            int t1, t2;
            t1 = (int)(cube() * tabuDuration); 
            t2 = (int)(cube() * tabuDuration);
            //do t1 = (int) (cube() * tabuDuration); while(t1 <= 2);
            //do t2 = (int) (cube() * tabuDuration); while(t2 <= 2);


            tabuList[move.getFirst()][variables[move.getSecond()]] = nIter + t1;
            tabuList[move.getSecond()][variables[move.getFirst()]] = nIter + t2;

            //Utils.show("after swap", super.variables);
            // detect loc min
            //if (minDelta >= 0)
            //	onLocMin();

            //System.out.println("ROT end search iteratrion ");
            return currentCost + minDelta;
        }
    }

    public int randomInterval(int low, int up) {
        return (int)(ThreadLocalRandom.current().nextDouble()*(up - low + 1)) + low;
    }

    private double cube(){
        double ran1 = ThreadLocalRandom.current().nextDouble();
        if (tabuDurationFactorUS < 0)
            return ran1;
        return ran1 * ran1 * ran1;
    }

    /**
     *  Create RoTS Solver State array to be send to Pool
     *  oeState(0) = solverType  
     *  oeState(1) = RoTS tabu duration Factor * 100
     *  oeState(2) = RoTS aspiration Factor * 100
     */
    protected int[] createSolverState() {
        int[] rotsState = new int[3];
        rotsState[0] = getMySolverType().getValue();
        rotsState[1] = (int) (tabuDurationFactor * 10.0);
        rotsState[2] = (int) (aspirationFactor * 10.0);
        return rotsState;
    }

    /**
     *  Process Solver State Array received from Pool
     *
     */
    protected void processSolverState(int[] state){
        // Random Search has no parameters to process

        int inSolverType = state[0];

        if (inSolverType == getMySolverType().getValue()){
            int intdf = (int) (state[1]/ 10.0);
            int inaf = (int) (state[2] / 10.0);

            // this.tabuDurationFactor = intdf;
            // this.aspirationFactor = inaf;

            tabuDurationFactor = (tabuDurationFactor + intdf) / 2.0;
            aspirationFactor = (aspirationFactor + inaf) / 2.0;

            if (tabuDuration != -1)
            tabuDuration = (int) (tabuDurationFactor * problemModel.getSize());

            aspiration = (int) (aspirationFactor * problemModel.getSize() * problemModel.getSize());
        }
    }

    //public def restartVar(){
    //	this.tabuList.clear();
    //}

    /**
     *  Interact when Loc min is reached
     */
    //private def onLocMin(){
    // communicate Local Minimum
    // solver.communicateLM( this.currentCost, cop.getVariables() as Valuation(sz));
    //solverState = this.createSolverState();
    //this.solver.communicateLM( new State(sz, this.currentCost, cop.getVariables() as Valuation(sz), here.id as Int, solverState) );
    //}
    public void adaptParameters(ParamInformation paramInfo, double divPercentageLimit) {
    	//ouble diversify_percentage_limit  =  paramInfo.getCurrentDivLimit(); //is necessary calculate
    	//TODO: Adapt Parameters according to  info received
    	LOGGER.log(Level.INFO, "param in ROTS, gain:"+paramInfo.gain()+" distance: "+paramInfo.distance()+" divLimit: "+ divPercentageLimit);
		if (paramInfo.gain() > 0) {

			if (paramInfo.gain() <= divPercentageLimit && paramInfo.distance() > 0.66) {
				// is necessary diversify
				tabuDuration = tabuDuration + Math.floorDiv(size, 2);
				aspiration = aspiration + Math.floorDiv(size * size, 2);
			} else {
				// is necessary intensify
				tabuDuration = tabuDuration - Math.floorDiv(size, 3);
				aspiration = aspiration - Math.floorDiv(size, 2);
			}

			// when parameters overpass the maximum values
			if (tabuDuration > 20 * size) {
				tabuDuration = ThreadLocalRandom.current().nextInt(16 * size) + 4 * size; // 4n to 20n
			}

			if (aspiration > 10 * size * size) {
				aspiration = ThreadLocalRandom.current().nextInt(9 * size * size) + size * size; // n*n to 10*n*n
			}

		} else {
			// necessary create new parameters or continue with the same
		}
    	
    }
    
    public void adaptParameters2(ParamInformation paramInfo) {
    	//TODO: Adapt Parameters according to  info received
    	LOGGER.log(Level.INFO, "param in ROTS, gain:"+paramInfo.gain()+" distance: "+paramInfo.distance());
    	if(paramInfo.gain() <= 0.1) {
    		// If no improvement change parameters
    		if(paramInfo.distance() > 0.2) {
    			// Intensify
    			if(tabuDuration > variables.length/4) { //to avoid negative values
    				tabuDuration = tabuDuration - variables.length/4;
    			}
    			aspiration =  (int)(aspiration + variables.length/2);
    			LOGGER.log(Level.INFO, "AUTO-PARAM: no improvement change params to intensify "+tabuDuration+" "+aspiration);
    		} else {
    			// Diversify
    			tabuDuration = tabuDuration + variables.length/4;
    			if(aspiration > variables.length/2) {
    				aspiration =  (int)(aspiration - variables.length/2);
    			}
    			LOGGER.log(Level.INFO, "AUTO-PARAM: no improvement change params to diversify"+tabuDuration+" "+aspiration);
    			
    		}
    	}
    	
    }
}
