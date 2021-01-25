package com.udea.degreework;

import com.udea.degreework.model.QAPModel;
import com.udea.degreework.solver.Metaheuristic;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;

public class Worker extends RecursiveAction {    
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

    
    private int currentCost;
    private int[] bestConf;
    private int bestCost;
    public int[] getBestConf() {
		return bestConf;
	}

	public int getBestCost() {
		return bestCost;
	}

	private boolean bestSent = false;

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

    public int getnChange() {
        return nChange;
    }

    private int nChange;
    private long initialTime;
    private Metaheuristic.Type MHType;
    public Metaheuristic.Type getMHType() {
		return MHType;
	}

	private AtomicBoolean kill;
    
    
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
        metaheuristic.configHeuristic(model);
    	bestConf = new int[size];
    	
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

        System.out.println("Starting solving process "+ MHType.toString()+"-"+id);
        initialTime = System.nanoTime();
        cost = solve();
        double exTime = (System.nanoTime() - initialTime)/1e6;

        System.out.println("Solving process finished Meta type "+ MHType.toString()+"-"+id+". Time: "+exTime+" ms best cost: "+ bestCost);
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
        //bestSent = false;
        //nForceRestart = 0n;
        nChange = 0;
        //nChangeforiwi = 0n;
        //attempsChangeForIwi = 0n;

        //if (this.nodeConfig.getAdaptiveComm())
          //  this.nodeConfig.setUpdateI(2n * this.nodeConfig.getrequestI());
    }

    public int solve() {
        System.out.println("WORKER " +MHType+" pasando por solve.");
        initVar(target, strictLow);
        currentCost = metaheuristic.costOfSolution();
        bestConf = metaheuristic.getVariables().clone();

        System.out.println(MHType.toString()+": initial cost= "+ currentCost);

        bestCost = currentCost; //Best solution is the initial solution

        while(currentCost >= 0){
            if (nIter >= 1000000){ //TODO: get parameter//this.nodeConfig.getMaxIters()){
                //restart or finish
                if(nRestart >= 0){ //TODO: get parameter //this.nodeConfig.getMaxRestarts()){
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

            //System.out.println("Type:"+MHType.toString()+"-"+id+" In main LOOP  time "+(System.nanoTime() - this.initialTime)/1e6 +" cost="+this.currentCost);
            //Time out
            int maxTime = 100000;
            if(maxTime > 0 ){ //TODO: get parameter //nodeConfig.getMaxTime() > 0){
                double eTime = System.nanoTime() - this.initialTime;
                if(eTime/1e6 >= maxTime){ //comparison in miliseconds
                    //Console.OUT.print("MsgType_0. Nodo " + here.id + ", finalizacion por Time Out: (tiempo) " + eTime/1e6
                    //	 + ". mi costo: " + bestCost + ", mis variables: ");
                    //printVector(bestConf);
                    //Logger.debug(()=>{" Time Out"});
                    System.out.println("Time out!");
                    break;
                }
            }
            //val eTime = System.nanoTime() - this.initialTime;
            //if((eTime/1e6)/30000 > count){
            //	count++;
            //	this.heuristicSolver.displayInfo();
            //}
            interact();
        }
        //this.heuristicSolver.printPopulation();
        updateTotStats();
        System.out.println(MHType.toString()+"-"+id+": Saliendo best cost: "+ bestCost+ "  iters: "+ nIterTot + " Changes: "+nChange);
        metaheuristic.verify(bestConf);

        for(int i = 0; i < bestConf.length; i++)
            System.out.print(" "+bestConf[i]);
        System.out.println(" ");

        return this.bestCost;
    }

    private void updateCosts(){
        //val sz = this.heuristicSolver.getSizeProblem();
        if(currentCost < bestCost){ //(totalCost <= bestCost)
            bestConf = metaheuristic.getVariables().clone();

            System.out.println("                    "+ MHType.toString() +"-"+id+ ": Current time: " + (System.nanoTime()-initialTime)/1e6 + ". Cost: " + this.currentCost);
            metaheuristic.verify(bestConf);
            bestCost = currentCost;

            bestSent = false; // new best found, I must send it!

            /*if (nodeConfig.getReportPart()){
                val eT = (System.nanoTime() - initialTime)/1e9;
                val gap = (this.bestCost-this.target)/(this.bestCost as Double)*100.0;

                Utils.show("Solution",this.bestConf);
                Console.OUT.printf("%s\ttime: %5.1f s\tbest cost: %10d\tgap: %5.2f%% \n",here,eT,this.bestCost,gap);
            }*/
            if ((strictLow && bestCost < target)
                    || (!strictLow && bestCost <= target)){
                targetSucc = true;
                kill.set(true);
                //Console.OUT.println("Soy nodo " + here + " y he encontrado la solucion");
            }
            //Console.OUT.println("La heuristica consigue mejorar el costo. CPLSNode en " + here);
            itersWhitoutImprovements = 0;

        }else{
            itersWhitoutImprovements++;
        }
    }

    private void updateTotStats(){
        //Console.OUT.println("Ingresa a reportar las estadisticas totales");
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
        if(nIter % requestI == 0){
            requestPool.sendInfo(new ContextInformation(metaheuristic.variables.clone(), currentCost, MHType));
        }
        if(nIter % updateI == 0){
            ContextInformation recv = updatePool.getInfo();
            if(recv.getCost() <= currentCost){
                metaheuristic.variables = recv.getVariables().clone();
                currentCost = metaheuristic.costOfSolution();
                nChange++;
            }
        }
    }
}
