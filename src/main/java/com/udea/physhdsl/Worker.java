package com.udea.physhdsl;

import com.udea.physhdsl.model.QAPModel;
import com.udea.physhdsl.solver.Metaheuristic;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker extends RecursiveAction {    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger( Worker.class.getName() );
	
    private int requestPoolId;
    private int updatePoolId;
    
    private int id;
    private int size;
    private int updateI;
    private int requestI;
    private Date endTime;
    private Date startTime;
    private Pool requestPool;
    private Pool updatePool;
    private Metaheuristic metaheuristic;
    private ParamInformation paramInfo;

    
    private int currentCost;
    private int[] bestConf;
    private int bestCost;
    
	private boolean bestSent = false;
	
	// stopping criteria
	private int maxTime;
	private int maxIters;
	private int maxRestart;
    private int target = 0;
    private boolean strictLow = false;
    private boolean targetSucc = false;

    // -> Statistics
    private int nRestart = 0;
    private int nIter;
    /** Total Statistics */
    private int nIterTot;
    private int nSwapTot;
    private int itersWhitoutImprovements;
    private int nChange;
    private long initialTime;
    private Metaheuristic.Type MHType;
    private AtomicBoolean kill;
    
    private boolean autoParam;
    private int paramI;
    
    public int getnChange() {
        return nChange;
    }
    
	public Metaheuristic.Type getMHType() {
		return MHType;
	}
	
	public int[] getBestConf() {
		return bestConf;
	}

	public int getBestCost() {
		return bestCost;
	}
    
    public Worker(String metaheuristicType, int requestPoolId, int updatePoolId) {
		super();
		this.requestPoolId = requestPoolId;
		this.updatePoolId = updatePoolId;

		int type = getMHTypeId(metaheuristicType);
		this.MHType = Metaheuristic.Type.getByType(type);
	}
    
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public int getId() {
    	return id;
    }
    
    public void setId(int id) {
    	this.id = id;
    }

    public int getSize() {
    	return size;
    }
    
    public void setSize(int size) {
    	this.size = size;
    }
    
    public void setWorker(int i, QAPModel model, List<Pool> pools, Map<String, Object> configuration, AtomicBoolean kill, int tCost, boolean sLow){
        setId(i);
        setPools(pools, configuration);
        setSize((int)configuration.get("size"));
        
        metaheuristic =  Metaheuristic.make(MHType, size);
        metaheuristic.configHeuristic(model, configuration);
    	bestConf = new int[size];
    	
    	Object valOrNull = configuration.get("maxTime");
    	maxTime = valOrNull == null ? 60000 : (int) valOrNull;
    	
    	valOrNull = configuration.get("maxIters");
    	maxIters = valOrNull == null  ? Integer.MAX_VALUE : (int) valOrNull;   
    	
    	valOrNull = configuration.get("maxRestart");
    	maxRestart = valOrNull == null ? 0 : (int) valOrNull; 
    	
    	
    	valOrNull = configuration.get("autoParam");
    	autoParam = (valOrNull != null);
    	paramI = autoParam ? (int) valOrNull: -1;
    	
    	LOGGER.log(Level.INFO, MHType.toString()+"-"+id+": MaxTime "+maxTime+" MaxIters "+maxIters+" MaxRestart "+maxRestart
    			+(autoParam?" with auto parametrization":" with fixed parameters"));
 	
    	this.kill = kill;
    	target = tCost;
    	strictLow = sLow;
    }
    
    /**
     * Assign respective (update and report) pools to the worker.
     * Set request and update Iterations.
     * @param pools
     * @param configuration
     */
    public void setPools(List<Pool> pools, Map<String, Object> configuration) {
		for (Pool pool : pools) {
			int poolId = pool.getId();
			if (poolId == updatePoolId) {
				updatePool = pool;
			}
			if (poolId == requestPoolId) {
				requestPool = pool;
			}
			requestI = (int)configuration.get("requestPoolI");
			updateI = (int)configuration.get("updatePoolI");	
		}
    }
    
    /**
     * Map Metaheuristic type to the Id
     * @param metaheuristicType
     * @return Id of the metaheuristic according to the type 
     */
    public int getMHTypeId(String metaheuristicType) {
    	switch (metaheuristicType) {
			case "Adaptive":
				return 0;
			case "Extremal":
				return 1;
			case "RoT":
				return 2;
			default:
				return 0;
		}
    }
    
    @Override
    public void compute() {
        int cost;

        LOGGER.log(Level.INFO, MHType.toString()+"-"+id+": Starting solving proces");
        initialTime = System.nanoTime();
        cost = solve();
        double exTime = (System.nanoTime() - initialTime)/1e6;

        LOGGER.log(Level.INFO,"             Solving process finished Meta type "+ MHType.toString()+"-"+id+". Time: "+exTime+" ms best cost: "+ bestCost);
    }

    protected void initVar(int tCost, boolean sLow){
        metaheuristic.initVar();
        metaheuristic.initVariables();

        target = tCost;
        strictLow = sLow;
        targetSucc = false;
        nIter = 0;
        nRestart = 0;
        bestConf = new int[metaheuristic.getSizeProblem()]; //new Rail[Int](this.heuristicSolver.getSizeProblem(), 0n);
        // clear Tot stats
        nIterTot = 0;
        //Jason: Migration begin
        itersWhitoutImprovements = 0;
        //this.nItersForUpdate = 0n;
        //Jason: Migration end
        nSwapTot = 0;
        initialTime = System.nanoTime();
        // Comm
        bestSent = false;
        //nForceRestart = 0n;
        nChange = 0;
        //nChangeforiwi = 0n;
        //attempsChangeForIwi = 0n;

        //if (this.nodeConfig.getAdaptiveComm())
          //  this.nodeConfig.setUpdateI(2n * this.nodeConfig.getrequestI());
    }

    public int solve() {
    	LOGGER.log(Level.FINE,"WORKER " +MHType+" pasando por solve.");
        initVar(target, strictLow);
        currentCost = metaheuristic.costOfSolution();
        bestConf = metaheuristic.getVariables().clone();

        LOGGER.log(Level.FINE, MHType.toString()+"-"+id+": initial cost= "+ currentCost);

        bestCost = currentCost; //Best solution is the initial solution
        if (autoParam) {
        	paramInfo = new ParamInformation(currentCost, metaheuristic.getVariables(), (double) maxTime);        
        }

        while(currentCost >= 0){
            if (nIter >= maxIters){ 
                //restart or finish
                if(nRestart >= maxRestart){ 
                    System.out.println(MHType.toString()+"-"+id+": MaxIters and Restarts done");
                	break;                
                }else{
                    nRestart++;
                    metaheuristic.initVariables();
                    currentCost = metaheuristic.costOfSolution();
                    updateTotStats();
                    restartVar();
                    //bestSent = false;
                    continue;
                }
            }

            nIter++;
            currentCost = metaheuristic.search(currentCost, bestCost, nIter);

            //Update the best configuration found so far
            updateCosts();

            //Kill solving process
            if(kill.get()){
                break;  // kill: End solving process
            }

            LOGGER.log(Level.FINE, "Type:"+MHType.toString()+"-"+id+" In main LOOP  time "+(System.nanoTime() - this.initialTime)/1e6 +" cost="+this.currentCost);
            //Time out
            if(maxTime > 0 ){ //TODO: get parameter //nodeConfig.getMaxTime() > 0){
                double eTime = System.nanoTime() - this.initialTime;
                if(eTime/1e6 >= maxTime){ //comparison in miliseconds
                    //Console.OUT.print("MsgType_0. Nodo " + here.id + ", finalizacion por Time Out: (tiempo) " + eTime/1e6
                    //	 + ". mi costo: " + bestCost + ", mis variables: ");
                    //printVector(bestConf);
                    //Logger.debug(()=>{" Time Out"});
                	LOGGER.log(Level.INFO,MHType.toString()+"-"+id+": Time out!");
                    break;
                }
            }
            //val eTime = System.nanoTime() - this.initialTime;
            //if((eTime/1e6)/30000 > count){
            //	count++;
            //	this.heuristicSolver.displayInfo();
            //}
            interact();
            if(autoParam) {
            	adaptParameters();
            }
             
        }
        //this.heuristicSolver.printPopulation();
        updateTotStats();
        LOGGER.log(Level.FINE, MHType.toString()+"-"+id+": solving process has finished best cost: "+ bestCost+ "  iters: "+ nIterTot + " Changes: "+nChange);
        metaheuristic.verify(bestConf);

        /*for(int i = 0; i < bestConf.length; i++)
            System.out.print(" "+bestConf[i]);
        System.out.println(" ");*/

        return this.bestCost;
    }

    private void updateCosts(){
        //val sz = this.heuristicSolver.getSizeProblem();
        if(currentCost < bestCost){ //(totalCost <= bestCost)
            bestConf = metaheuristic.getVariables().clone();

            LOGGER.log(Level.FINER,"                    "+ MHType.toString() +"-"+id+ ": Current time: " + (System.nanoTime()-initialTime)/1e6 + ". Cost: " + this.currentCost);
            //metaheuristic.verify(bestConf);
            bestCost = currentCost;

            bestSent = false; // new best found, I must send it!

            if ((strictLow && bestCost < target)
                    || (!strictLow && bestCost <= target)){
                targetSucc = true;
                kill.set(true);
            }
            //Console.OUT.println("La heuristica consigue mejorar el costo. CPLSNode en " + here);
            itersWhitoutImprovements = 0;

        }else{
            itersWhitoutImprovements++;
        }
        
        // Check whether it is best cost in Interval (for parameters adaptation)
        if(autoParam && currentCost < paramInfo.getBestCostInInterval()) {
        	paramInfo.setNewBest(currentCost, metaheuristic.getVariables());
        }
        
    }

    private void updateTotStats(){
        nIterTot += nIter;
        nSwapTot += metaheuristic.getNSwap();
        metaheuristic.clearNSwap();
        nIter = 0;
    }

    private void restartVar(){
        bestSent = false;
        //this.heuristicSolver.restartVar();
    }

    private void interact(){
        interactForIntensification();
    }

    private void interactForIntensification(){
        if(nIter % updateI == 0){
        	if(!bestSent) {
        		updatePool.insertInfo(new ContextInformation(bestConf.clone(), bestCost, MHType));
        		bestSent = true;
        	} else {
        		updatePool.insertInfo(new ContextInformation(metaheuristic.variables.clone(), currentCost, MHType));
        	}
        }
        if(nIter % requestI == 0){
            ContextInformation recv = requestPool.getInfo();
            
            // Adapt Configuration
            if(recv != null) {	
	            if(recv.getCost() <= currentCost){
	                metaheuristic.variables = recv.getVariables().clone();
	                currentCost = metaheuristic.costOfSolution();
	                nChange++;
	            }
            }
            // TODO: change I've not been able to improve in a while}
         
            if(autoParam) {
            	// Adapt Parameters
            	double currentTimems = (System.nanoTime() - initialTime)/1e6;
            	metaheuristic.adaptParameters(paramInfo, paramInfo.getCurrentDivLimit(currentTimems));
            	paramInfo.setNewInitial(currentCost, metaheuristic.variables);
            }         
        }
    }
    
    public void clean() {
    	
    }
    
    public SolverStats getSolverStats() {
    	return new SolverStats(nIterTot, nChange, bestCost, bestConf, MHType, id, target);
    }
    
    public void adaptParameters() {
    	if(nIter % paramI == 0){
    		// Adapt Parameters
    		double currentTimems = (System.nanoTime() - initialTime)/1e6; 
        	metaheuristic.adaptParameters(paramInfo, paramInfo.getCurrentDivLimit(currentTimems));
        	paramInfo.setNewInitial(currentCost, metaheuristic.variables);
    	}
    }
    
}
