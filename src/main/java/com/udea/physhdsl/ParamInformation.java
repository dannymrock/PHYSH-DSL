package com.udea.physhdsl;

public class ParamInformation {

	private int initialCost;
	private int[] initialConf;	
	private int bestCostInterval;
	private int[] bestConfInterval;	
	
	
	//TODO: how to implement iteration for the diversification limits
	
	public ParamInformation(int initialCost, int[] initialConf) {
		super();
		this.initialCost = initialCost;
		this.initialConf = initialConf.clone();
		this.bestCostInterval = initialCost;
		this.bestConfInterval = initialConf.clone();
	
	}
	
	public void setNewInitial(int initialCost, int[] initialConf) {
		this.initialCost = initialCost;
		this.initialConf = initialConf.clone();
		this.bestCostInterval = initialCost;
		this.bestConfInterval = initialConf.clone();
	}
	
	
	public int getBestCostInInterval() {
		return bestCostInterval;
	}

	public void setBestCostInInterval(int bestCostInInterval) {
		this.bestCostInterval = bestCostInInterval;
	}

	public void setBestConfInInterval(int[] bestConfInInterval) {
		this.bestConfInterval = bestConfInInterval;
	}
	
	
	public void setNewBest(int bestCost, int[] bestConf) {
		this.bestCostInterval = bestCost;
		this.bestConfInterval = bestConf.clone();
	}
	
	public double distance() {
		double dis = 0.0;
	    int same = 0;
	    for(int i = 0; i < initialConf.length; i++) {
	    	if(initialConf[i] == bestConfInterval[i]) same++;
	    }
	    dis = (initialConf.length - same)/(double)initialConf.length;
	    return dis;
	}

	public double gain() {
		return (initialCost - bestCostInterval) / (double) initialCost;
	}
}
