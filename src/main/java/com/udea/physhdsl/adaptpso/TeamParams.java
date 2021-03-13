package com.udea.physhdsl.adaptpso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.udea.physhdsl.solver.Metaheuristic.Type;

public class TeamParams {

	private static final Logger LOGGER = Logger.getLogger( TeamParams.class.getName() );
	private RoTParams bestRoTParams;
	private EOParams bestEOParams;
	private List<RoTParams> bestRoTParamList;
	private List<EOParams> bestEOParamList;
	
	private int globalReports;
	private int rotReports;
	private int eoReports;
	private int psoDelMem;
	
	private int rotNoImprovement;
	private int eoNoImprovement;
	private long initialTime;
	
	// Variables for cooperative parameters adaptation
	int paramPoolSize;
	int nWorkers;
	
	
	public TeamParams(int psoDelMemGlobal, int nWorkers) {
		super();
		bestRoTParams = new RoTParams(-1, -1, -1); 
		bestEOParams = new EOParams(-1, -1, -1);
		bestRoTParamList = new ArrayList<RoTParams>();
		bestEOParamList = new ArrayList<EOParams>();
		globalReports = 0;
		psoDelMem = psoDelMemGlobal*nWorkers;
		rotNoImprovement = 0;
		eoNoImprovement = 0;
		initialTime = System.nanoTime();
		
		// Cooperative parameters adaptation
		paramPoolSize = psoDelMemGlobal;
		this.nWorkers = nWorkers;
		eoReports = 0;
		rotReports = 0;
	}
	
	public synchronized RoTParams updateGlobalRoTParams(RoTParams pParams) {
		globalReports++;
		//if(psoDelMem > 0 && globalReports % psoDelMem == 0) {
		if(psoDelMem > 0 && rotNoImprovement == psoDelMem) {
			bestRoTParams = new RoTParams(-1, -1, -1);
			LOGGER.log(Level.INFO, "------------------------------------------Delete global memory "+rotNoImprovement);
			rotNoImprovement = 0;
		}
		
		
		if (pParams.getGain() > bestRoTParams.getGain()) {
			LOGGER.log(Level.INFO, "New RoTs parameters in TEAM");
			bestRoTParams.setTabuDurationFactor(pParams.getTabuDurationFactor());
			bestRoTParams.setAspirationFactor(pParams.getAspirationFactor());
			bestRoTParams.setGain(pParams.getGain());;
			double time = (System.nanoTime()-initialTime)/1e9;
			bestRoTParamList.add(new RoTParams(pParams, time));
			rotNoImprovement = 0;
			return pParams;
		} else {
			rotNoImprovement++;
			return new RoTParams(bestRoTParams);
		}
	}
	
	public synchronized EOParams updateGlobalEOParams(EOParams pParams) {
		globalReports++;
		//if(psoDelMem > 0 && globalReports % psoDelMem == 0) {
		if(psoDelMem > 0 && eoNoImprovement == psoDelMem) {
			eoNoImprovement = 0;
			bestEOParams = new EOParams(-1, -1, -1);
			LOGGER.log(Level.INFO, "********************************************Delete global memory");
		}
		
		if (pParams.getGain() > bestEOParams.getGain()) {
			LOGGER.log(Level.INFO, "New RoTs parameters in TEAM: "+pParams.getGain() +" > "+ bestRoTParams.getGain());
			bestEOParams.setTau(pParams.getTau());
			bestEOParams.setPdf(pParams.getPdf());
			bestEOParams.setGain(pParams.getGain());
			double time = (System.nanoTime()-initialTime)/1e9;
			bestEOParamList.add(new EOParams(pParams, time));
			eoNoImprovement = 0;
			return pParams;
		} else {
			eoNoImprovement++;
			return new EOParams(bestEOParams);
		}
	}

	public void printBestStats(Type mhtype) {
		if(mhtype == Type.ROT) {
			System.out.println("RoT best params in TEAM");
			for(RoTParams p : bestRoTParamList) {
				System.out.printf("%5.1f, %5.4f, %5.4f, %5.4f\n", p.getTime(), p.getTabuDurationFactor(), p.getAspirationFactor(), p.getGain());
			}
		} else if (mhtype == Type.EO) {
			System.out.println("EO best params in TEAM");
			for(EOParams p : bestEOParamList) {
				System.out.printf("%5.1f, %5.4f, %3d, %5.4f\n", p.getTime(), p.getTau(), p.getPdf(), p.getGain());
			}
		}
		
	}
	
	
	// Methods for cooperative parameters adaptation
	public synchronized RoTParams updateCoopRoTParams(RoTParams pParams) {
		
		if (rotReports % nWorkers == 0) {
			if(bestRoTParamList.size() > 0) {
				for(int i = 0; i < bestRoTParamList.size(); i++) {
					double newGain = bestRoTParamList.get(i).getGain() / 5.0;
					bestRoTParamList.get(i).setGain(newGain);
				}	
			}
			
		}
		rotReports++;
		
		//System.out.println("ROT: received gain: "+pParams.getGain());
		if(bestRoTParamList.size() == 0) {
			// Pool is empty
			bestRoTParamList.add(pParams);
			return pParams;
		} else if(bestRoTParamList.size() < paramPoolSize) {
			// Pool is not full
			if(isAlreadyInPoolRoT(pParams)){
				//System.out.println("Params Already in pool, params discarded");
				//return random parameters in pool
				return getRoTCrossedParam();
			} else {
				//RoTParams pReturn = bestRoTParamList.get(ThreadLocalRandom.current().nextInt(bestRoTParamList.size()));
				RoTParams pReturn = getRoTCrossedParam();
				bestRoTParamList.add(pParams);
				return pReturn;
			}
		} else {
			// Pool is full
			
			int victim = -1;
			double worstGain = 100.0;
			
			for(int i = 0; i < bestRoTParamList.size(); i++) {
				RoTParams p = bestRoTParamList.get(i);
				if(p.getTabuDurationFactor() == pParams.getTabuDurationFactor() &&
						p.getAspirationFactor() == pParams.getAspirationFactor()) {
					//return bestRoTParamList.get(ThreadLocalRandom.current().nextInt(bestRoTParamList.size()));
					return getRoTCrossedParam();
				}
				if(bestRoTParamList.get(i).getGain() < worstGain) {
					worstGain = bestRoTParamList.get(i).getGain();
					victim = i;
				}
			}
			
			//RoTParams pReturn = bestRoTParamList.get(ThreadLocalRandom.current().nextInt(bestRoTParamList.size()));
			RoTParams pReturn = getRoTCrossedParam();
			// replace worst params with new params
			if (pParams.getGain() > worstGain) {
				bestRoTParamList.set(victim, pParams);
			}
			return pReturn;
		}
		
	}
	
	public synchronized EOParams updateCoopEOParams(EOParams pParams) {
		
		if (eoReports % nWorkers == 1) {
			for(int i = 0; i < bestEOParamList.size(); i++) {
				double newGain = bestEOParamList.get(i).getGain() / 5.0;
				bestEOParamList.get(i).setGain(newGain);
			}
		}
		eoReports++;
		
		//System.out.println("EO: received gain: "+pParams.getGain() );
		
		if(bestEOParamList.size() == 0) {
			// Pool is empty
			bestEOParamList.add(pParams);
			return pParams;
		} else if(bestEOParamList.size() < paramPoolSize) {
			// Pool is not full
			if(isAlreadyInPoolEO(pParams)){
				//System.out.println("Params Already in pool, params discarded");
				//return random parameters in pool
				//return bestEOParamList.get(ThreadLocalRandom.current().nextInt(bestEOParamList.size()));
				return getEOCrossedParam();
			} else {
				//EOParams pReturn = bestEOParamList.get(ThreadLocalRandom.current().nextInt(bestEOParamList.size()));
				EOParams pReturn = getEOCrossedParam();
				bestEOParamList.add(pParams);
				return pReturn;
			}
		} else {
			// Pool is full
			
			int victim = -1;
			double worstGain = 100.0;
			
			for(int i = 0; i < bestEOParamList.size(); i++) {
				EOParams p = bestEOParamList.get(i);
				if(p.getTau() == pParams.getTau()) {
					//return bestEOParamList.get(ThreadLocalRandom.current().nextInt(bestEOParamList.size()));
					return getEOCrossedParam();
				}
				if(bestEOParamList.get(i).getGain() < worstGain) {
					worstGain = bestEOParamList.get(i).getGain();
					victim = i;
				}
			}
			
			//EOParams pReturn = bestEOParamList.get(ThreadLocalRandom.current().nextInt(bestEOParamList.size()));
			EOParams pReturn = getEOCrossedParam();
			// replace worst params with new params
			if (pParams.getGain() > worstGain) {
				bestEOParamList.set(victim, pParams);
			}
			return pReturn;
		}
	}
	
	
	private RoTParams getRoTCrossedParam() {
		if (bestRoTParamList.size() == 1) {
			return bestRoTParamList.get(0);
		} else {
			int firstParentIndex = ThreadLocalRandom.current().nextInt(bestRoTParamList.size());
			int secondParentIndex;
			do {
				secondParentIndex = ThreadLocalRandom.current().nextInt(bestRoTParamList.size());
			} while(firstParentIndex == secondParentIndex);
			
			RoTParams firstParent = bestRoTParamList.get(firstParentIndex);
			RoTParams secondParent = bestRoTParamList.get(secondParentIndex);
			
			RoTParams offspring = new RoTParams(firstParent.getTabuDurationFactor(), secondParent.getAspirationFactor(), -2.0);
			
			double delta = 0.5;
			// mutation probability 0.1
			if(ThreadLocalRandom.current().nextDouble() < 0.1) {
				//Limits should be checked at MH code
				double af = offspring.getAspirationFactor() +  (ThreadLocalRandom.current().nextDouble(-1.0, 1) * delta);
				offspring.setAspirationFactor(af);
				double td = offspring.getTabuDurationFactor() +  (ThreadLocalRandom.current().nextDouble(-1.0, 1) * delta);
				offspring.setTabuDurationFactor(td);
				// -3 is a flag to know when a parameter was mutated
				offspring.setGain(-3.0);
			}
			
			return offspring;
		}
	}
	
	private EOParams getEOCrossedParam() {
		if (bestEOParamList.size() == 1) {
			return bestEOParamList.get(0);
		} else {
			int firstParentIndex = ThreadLocalRandom.current().nextInt(bestEOParamList.size());
			int secondParentIndex;
			do {
				secondParentIndex = ThreadLocalRandom.current().nextInt(bestEOParamList.size());
			} while(firstParentIndex == secondParentIndex);
			
			EOParams firstParent = bestEOParamList.get(firstParentIndex);
			EOParams secondParent = bestEOParamList.get(secondParentIndex);
			
			double newTau = (firstParent.getTau() + secondParent.getTau()) / 2.0;

			
			EOParams offspring = new EOParams(newTau, 1, -2.0);
			
			double delta = 0.2;
			// mutation probability 0.1
			if(ThreadLocalRandom.current().nextDouble() < 0.1) {
				//Limits should be checked at MH code
				double mutTau = offspring.getTau() +  (ThreadLocalRandom.current().nextDouble(-1.0, 1) * delta);
				offspring.setTau(mutTau);
				// -3 is a flag to know when a parameter was mutated
				offspring.setGain(-3.0);
			}
			
			return offspring;
		}
	}
	
	
	private boolean isAlreadyInPoolRoT(RoTParams pnew) {
		for(RoTParams p: bestRoTParamList) {
			if(p.getTabuDurationFactor() == pnew.getTabuDurationFactor() &&
					p.getAspirationFactor() == pnew.getAspirationFactor()) {
				return true;
			}			
		}
		return false;
	}
	
	private boolean isAlreadyInPoolEO(EOParams pnew) {
		for(EOParams p: bestEOParamList) {
			if(p.getTau() == pnew.getTau()) {
				return true;
			}			
		}
		return false;
	}
	
	
	
	
	
	
	
	
}
