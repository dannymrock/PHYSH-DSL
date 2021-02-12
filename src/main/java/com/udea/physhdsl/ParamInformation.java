package com.udea.physhdsl;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ParamInformation {
	
	private static final Logger LOGGER = Logger.getLogger( ParamInformation.class.getName() );

	private int initialCost;
	private int[] initialConf;	
	private int bestCostInterval;
	private int[] bestConfInterval;	
	
	
	private int totalValues;
	private double[] divLimit;
	private double totalTime;
	private double timeInterval;
	
	
	//TODO: how to implement iteration for the diversification limits
	
	public ParamInformation(int initialCost, int[] initialConf, double totalTime) {
		super();
		this.initialCost = initialCost;
		this.initialConf = initialConf.clone();
		this.bestCostInterval = initialCost;
		this.bestConfInterval = initialConf.clone();
		
		this.totalTime = totalTime;
		
		totalValues = 20;
		
		this.timeInterval = totalTime / totalValues;
		
		divLimit = new double[totalValues];
		computeLimits();
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
	
	public double getCurrentDivLimit(double currentTimems) {
		LOGGER.log(Level.INFO, " current time: "+ currentTimems + " Total time: " + totalTime);
		return divLimit[(int) (currentTimems/timeInterval)];
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
	
	
	private void computeLimits() {
		double a = 94.67597;
		double b = 0.31811;
		double c = 0.15699;

		double y = 0;

		for (int x = 0; x < totalValues; x++) {
			y = (a * Math.exp(-b * (x + 1)) + c)/100.0;
			//System.out.println("f(" + x + ") = " + (float) y);

			divLimit[x] = y;
		}
	}
	
}
