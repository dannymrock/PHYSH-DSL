package com.udea.physhdsl;

import com.udea.physhdsl.solver.Metaheuristic;
import com.udea.physhdsl.solver.Metaheuristic.Type;

public class SolverStats {
	
	private int totalIters;
	private int totalChanges;
	private int bestCost;
	private int[] bestConf;
	private Metaheuristic.Type mhtype;
	private int wId;
	private int tId;
	private int target;

	
	public SolverStats(int totalIters, int totalChanges, int bestCost, int[] bestConf,
			Metaheuristic.Type mhtype, int wId, int target) {
		this.totalIters = totalIters;
		this.totalChanges = totalChanges;
		this.bestCost = bestCost;
		this.bestConf = bestConf.clone();
		this.mhtype = mhtype;
		this.wId = wId;
		this.target = target;
	}
	
	
	public int getBestCost() {
		return bestCost;
	}
	public void setBestCost(int bestCost) {
		this.bestCost = bestCost;
	}
	public int[] getBestConf() {
		return bestConf;
	}
	public void setBestConf(int[] bestConf) {
		this.bestConf = bestConf.clone();
	}
	public int getTotalIters() {
		return totalIters;
	}
	public void setTotalIters(int totalIters) {
		this.totalIters = totalIters;
	}
	public int getTotalChanges() {
		return totalChanges;
	}
	public void setTotalChanges(int totalChanges) {
		this.totalChanges = totalChanges;
	}
	public Metaheuristic.Type getMhtype() {
		return mhtype;
	}
	public void setMhtype(Metaheuristic.Type mhtype) {
		this.mhtype = mhtype;
	}
	public int getWId() {
		return wId;
	}
	public void setWId(int wId) {
		this.wId = wId;
	}
	public int getTId() {
		return tId;
	}
	public void setTId(int tId) {
		this.tId = tId;
	}
	public int getTarget() {
		return target;
	}
	
	public double getPD() {
		return ((bestCost - target) / (double)target) * 100.0;
	}
	
	public boolean hitTarget() {
		return bestCost == target;
	}
}
