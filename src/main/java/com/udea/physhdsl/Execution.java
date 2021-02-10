package com.udea.physhdsl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.udea.physhdsl.model.QAPModel;
import com.udea.physhdsl.solver.Metaheuristic;

public class Execution {
	private static final Logger LOGGER = Logger.getLogger( Execution.class.getName() );
	private List<Team> teams = new ArrayList<Team>();
	private Map<String, Object> config = new HashMap<String, Object>();
	private int sampleSize;
	private double sumTimes;
	private long sumCosts;
	private long sumIters;
	private long sumChanges;
	private int sumTargets;
	private int sumRots;
	private int sumEO;
	private int sumAS;
	
	private SolverStats bestWorker;
	private FileWriter csvWriter; 
	private String instName;
	

	public Execution(List<Team> teams) {
		this.teams = teams;
	}

	public void loadConfig(Map<String, Object> config) {
		this.config = config;
		validateData();
	}

	public void start() {
		
        QAPModel model = new QAPModel((int)config.get("size"));
        String filePath = (String) config.get("filePath"); 
        instName = filePath.split(".+?/(?=[^/]+$)")[1].split("\\.(?=[^\\.]+$)")[0];
        
        model.loadData(String.valueOf(filePath));
        AtomicBoolean kill = new AtomicBoolean(false);
        Object valOrNull = config.get("sampleSize");
    	sampleSize = valOrNull == null ? 1 : (int) valOrNull;
		
    	LOGGER.log(Level.INFO, "Solving QAP instance "+instName+", "+sampleSize+" times");
    	
    	int nProc = Runtime.getRuntime().availableProcessors();
    	ForkJoinPool myPool = new ForkJoinPool(nProc);
    	
    	
    	boolean createFile = config.get("outFile") != null;
    	if(createFile) {
    		try {
				csvWriter = new FileWriter((String) config.get("outFile"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	printHeader(createFile);

    	sumTimes = 0.0;
    	sumCosts = 0l;
    	sumIters = 0l;
    	sumChanges = 0l;
    	sumTargets = 0;
    	sumRots = 0;
    	sumEO = 0;
    	sumAS = 0;
    	
    	for(int rep = 0; rep < sampleSize; rep++) {
    		
    	
    		double initialTime = System.nanoTime();
        
    		for (int i = 0; i < teams.size(); i++) {
    			teams.get(i).setTeam(i+1, model, config, kill);
    			myPool.submit(teams.get(i));
    		    //teams.get(i).fork();
    		}
        
    		for (int i = 0; i < teams.size(); i++) {
    			teams.get(i).join();
    		}
        
    		int bestCost = Integer.MAX_VALUE;
    		int bestTIndex = -1;
    		
    		for (int i = 0; i < teams.size(); i++)  {
    			if(teams.get(i).getBestCost() < bestCost) {
    				bestCost = teams.get(i).getBestCost();
    				bestTIndex = i;
    			}
    		}
    		
    		bestWorker = teams.get(bestTIndex).getBestWorkerStats();
        
    		double endTime = System.nanoTime();
    		double time = (endTime-initialTime)/1e9;
    		sumTimes += time;
    		sumCosts += bestCost;
    		sumIters += bestWorker.getTotalIters();
    		sumChanges += bestWorker.getTotalChanges();
    		sumTargets += bestWorker.hitTarget()?1:0;
    		switch (bestWorker.getMhtype()) {
			case AS:
				sumAS++;				
				break;
			case EO:
				sumEO++;
				break;
			case ROT:
				sumRots++;
			default:
				break;
			}
    		
    		printStats(rep, bestWorker, time, createFile);
    		
    		printSolution(createFile);
    		
    		model.verify(bestWorker.getBestConf().length, bestWorker.getBestConf());
    		LOGGER.log(Level.INFO,"\nExecution "+rep+": all teams have finished");
    		LOGGER.log(Level.INFO,"Best worker of whole execution is TeamID: "+ bestWorker.getTId()+" workerID: " 
    		+ bestWorker.getWId()+"-"+bestWorker.getMhtype()+" BestCost: "+bestWorker.getBestCost());
    		LOGGER.log(Level.INFO,"Execution time: "+time+" ms");
    		for (int i = 0; i < teams.size(); i++)  {
    			teams.get(i).clean();
    			teams.get(i).reinitialize();
    		}
    		kill.set(false);
    	}
    	printAVGs(bestWorker.getTarget(), createFile);
    	if (createFile) {
    		try {
				csvWriter.flush();
				csvWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
	}

	private void validateData() {
		try {
			if (config.get("target") == null) {
				throw new Exception("Config: target is Expected");
			} else if (config.get("errorRange") == null) {
				throw new Exception("Config: errorRange is Expected");
			} 
//			else if (config.get("") == null) {
//				throw new Exception("Config:  is Expected");
//			}
			
			if (config.get("numberOfTeams") != null) {
				if(teams.size() != (int)config.get("numberOfTeams")) {
					throw new Exception("Config: the expected numberOfTeams is different from the declared teams");
				}
			}
			
			if (config.get("equalNumberOfWorkersPerTeam") != null) {
				boolean equalNumberOfWorkersPerTeam = Boolean.valueOf(String.valueOf(config.get("equalNumberOfWorkersPerTeam")));
			
				if (config.get("numberOfWorkersPerTeam") != null && equalNumberOfWorkersPerTeam) {
					int numberOfWorkersPerTeam = (int) config.get("numberOfWorkersPerTeam");
					for (Team team : teams) {
						if (team.getWorkers().size() != numberOfWorkersPerTeam) {
							throw new Exception("Config: the expected numberOfWorkersPerTeam("+ numberOfWorkersPerTeam +") is different from the declared workers per team(Total: " + team.getWorkers().size() + ")");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
			System.exit(1);
		}
	}
	
	private void printHeader(boolean createFile){
		System.out.println("|----|--------|-----|-----|---------|---------|------------|----|-------|-----|");
		System.out.println("|  # |RT-AS-EO| TID | WID | Time(s) |  iters  |    Cost    | Tg |  APD  | nCh | sol");
		System.out.println("|----|--------|-----|-----|---------|---------|------------|----|-------|-----|");
		if (createFile) {
			try {
				csvWriter.append("0-Repetition, 1-MHType, 2-TeamID, 3-WorkerID, 4-Time, 5-iters, 6-Cost, 7-HitTarget, 8-APD, 9-nChanges, 10-Solution\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private void printStats(int rep, SolverStats st, double time, boolean createFile) {
		System.out.format( "| %2d |  %4s  | %3s | %3s | %7.3f | %7d | %10d | %2d | %5.2f | %3d |" , 
				rep, st.getMhtype(), st.getTId(), st.getWId(), time, st.getTotalIters(),
				st.getBestCost(), st.hitTarget()?1:0, st.getPD(), st.getTotalChanges());
		if (createFile) {
			String hit = st.hitTarget() ? "1":"0"; 
			try {
				csvWriter.append(rep+","+st.getMhtype().toString()+","+st.getTId()+
						","+st.getWId()+","+time+","+st.getTotalIters()+","+
						st.getBestCost()+","+ hit+","+ st.getPD()+","+ st.getTotalChanges()
						+", ");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void printSolution(boolean createFile) {
		for(int i = 0; i < bestWorker.getBestConf().length; i++) {
			System.out.print(" "+bestWorker.getBestConf()[i]);
			if(createFile) {
				try {
					csvWriter.append(bestWorker.getBestConf()[i]+" ");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("");
		if(createFile) {
			try {
				csvWriter.append("\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void printAVGs(int target, boolean createFile) {
		double avgTime = sumTimes/sampleSize;
		double avgIters = sumIters/(double)sampleSize;
		double avgCost = sumCosts/(double)sampleSize;
		double apd = ((avgCost - (double)target) / target) * 100.0;
		double avgChanges = sumChanges /(double)sampleSize;
		System.out.println("|----|--------|-----|-----|---------|---------|------------|----|-------|-----|");
    	System.out.format( "|AVGs|%2d-%2d-%2d|     |     |%9.4f|%9.1f| %10.1f | %2d | %5.2f | %3.1f |\n" , 
    			sumRots, sumAS, sumEO ,avgTime, avgIters, avgCost, sumTargets, apd, avgChanges);
    	
    	if (createFile) {
			try {
				csvWriter.append(instName+","+ sumRots+"-"+sumAS+"-"+sumEO+" , , ,"+avgTime+","+avgIters+","+avgCost+","
								+sumTargets+","+apd+","+avgChanges+",");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
