package com.udea.physhdsl.adaptpso;

public class EOParams {
	
	private double tau;
	private int pdf;
	private double gain;
	private double time;
	
	public double getTime() {
		return time;
	}

	public EOParams(double tau, int pdf, double gain) {
		super();
		this.tau = tau;
		this.pdf = pdf;
		this.gain = gain;
	}
	
	public EOParams(EOParams p) {
		this.tau = p.getTau();
		this.pdf = p.getPdf();
		this.gain = p.getGain();
	}
	
	public EOParams(EOParams p, double time) {
		this.tau = p.getTau();
		this.pdf = p.getPdf();
		this.gain = p.getGain();
		this.time = time;
	}
	
	public double getTau() {
		return tau;
	}
	public void setTau(double tau) {
		this.tau = tau;
	}
	public int getPdf() {
		return pdf;
	}
	public void setPdf(int pdf) {
		this.pdf = pdf;
	}
	public double getGain() {
		return gain;
	}
	public void setGain(double gain) {
		this.gain = gain;
	}

}
