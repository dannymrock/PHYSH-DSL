package com.udea.physhdsl.adaptpso;

import java.util.ArrayList;
import java.util.List;
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
	private int psoDelMem;
	
	private int rotNoImprovement;
	private int eoNoImprovement;
	private long initialTime;
	
	public TeamParams(int psoDelMemGlobal) {
		super();
		bestRoTParams = new RoTParams(-1, -1, -1); 
		bestEOParams = new EOParams(-1, -1, -1);
		bestRoTParamList = new ArrayList<RoTParams>();
		bestEOParamList = new ArrayList<EOParams>();
		globalReports = 0;
		psoDelMem = psoDelMemGlobal;
		rotNoImprovement = 1;
		eoNoImprovement = 0;
		initialTime = System.nanoTime();
	}
	
	public synchronized RoTParams updateGlobalRoTParams(RoTParams pParams) {
		globalReports++;
		//if(psoDelMem > 0 && globalReports % psoDelMem == 0) {
		if(psoDelMem > 0 && rotNoImprovement % psoDelMem == 0) {
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
			rotNoImprovement = 1;
			return pParams;
		} else {
			rotNoImprovement++;
			return new RoTParams(bestRoTParams);
		}
	}
	
	public synchronized EOParams updateGlobalEOParams(EOParams pParams) {
		globalReports++;
		//if(psoDelMem > 0 && globalReports % psoDelMem == 0) {
		if(psoDelMem > 0 && eoNoImprovement % psoDelMem == 0) {
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
			eoNoImprovement = 1;
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
}
