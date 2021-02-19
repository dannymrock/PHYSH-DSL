package com.udea.physhdsl.adaptpso;

public class RoTParams {
	
	private double tabuDurationFactor;
	private double aspirationFactor;
	private double gain;
	private double time;
	
	public double getTime() {
		return time;
	}

	public RoTParams(double tabuDurationFactor, double aspirationFactor, double gain) {
		super();
		this.tabuDurationFactor = tabuDurationFactor;
		this.aspirationFactor = aspirationFactor;
		this.gain = gain;
	}
	
	public RoTParams(RoTParams p) {
		this.tabuDurationFactor = p.getTabuDurationFactor();
		this.aspirationFactor = p.getAspirationFactor();
		this.gain = p.getGain();
	}
	
	public RoTParams(RoTParams p, double time) {
		this.tabuDurationFactor = p.getTabuDurationFactor();
		this.aspirationFactor = p.getAspirationFactor();
		this.gain = p.getGain();
		this.time = time;
	}
	
	public double getTabuDurationFactor() {
		return tabuDurationFactor;
	}
	public void setTabuDurationFactor(double tabuDurationFactor) {
		this.tabuDurationFactor = tabuDurationFactor;
	}
	public double getAspirationFactor() {
		return aspirationFactor;
	}
	public void setAspirationFactor(double aspirationFactor) {
		this.aspirationFactor = aspirationFactor;
	}
	public double getGain() {
		return gain;
	}
	public void setGain(double gain) {
		this.gain = gain;
	}
}
