package com.udea.physhdsl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pool {
	private static final Logger LOGGER = Logger.getLogger( Pool.class.getName() );
    private int size;
    private int id;
    private String policy;
    private List<ContextInformation> pool;
    
    private int worstCostInPool = -1;
    private double distanceThreshold;
    private double tolerance;

    public Pool(int size) {
        this.size = size;
        // max. distance accepted in pool (if 0 -> must be not equal) 
        this.distanceThreshold = 0.1;
        this.tolerance = 1;
    }

    public Pool(int size, int id, String policy) {
		this.size = size;
		this.id = id;
		this.policy = policy;
		pool = new ArrayList<>(size);
		if (policy.compareTo("Diverse") == 0) {
			// Must be "very" different but accept bad solutions
			LOGGER.log(Level.INFO, "Pool "+id+" is Diverse");
			this.distanceThreshold = 0.5;
	        this.tolerance = 1.2;
		}else { 
			// Accept similar solution but must be very good ones
			LOGGER.log(Level.INFO, "Pool "+id+" is Elite");
			this.distanceThreshold = 0.1;
	        this.tolerance = 1;
		}

	}
    
	public int getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	public synchronized void insertInfo(ContextInformation message){
		
        if (pool.size() < size){
        	if (isAlreadyInPool(message)){
        		//System.out.println(id+": Already in pool, conf discarded");
        		return;
        	}
        		
            pool.add(message);
            if (message.cost > worstCostInPool){
            	worstCostInPool = message.cost;
            }
        }else{
            //pool is full
            //int index = pool.stream().min(Comparator.comparing(ContextInformation::getCost)).hashCode();
            
        	// 1st condition: better than worst in pool (minimization problem)
        	if (message.cost > worstCostInPool *  tolerance) {
        		//System.out.println(id+": conf is worst than worst cost in pool tolerance = "+tolerance);
        		return;
        	}
        	
        		
        	// Select configuration with worst cost and check distances
        	int secWorstCost = -1;
            int worstIndex = -1;
            for(int i = 0; i < pool.size(); i++){
            	double dis = distance(pool.get(i).variables, message.variables);
            	if(dis <= distanceThreshold) {
            		//System.out.println(id+": conf is discarded because is similar to an existing one. dis = "+dis);
            		return;
            	}
                if(pool.get(i).getCost() == worstCostInPool){
                    worstIndex = i;
                }else if(pool.get(i).getCost() > secWorstCost) {
                	// update second worst cost in Pool
                	secWorstCost = pool.get(i).getCost();
                }
                
            }
            // replacing worst configuration in pool with the new one
            pool.set(worstIndex, message);
            
            // What is the worst? the new one or the second worst cost 
            // (note worst cost was replaced)
            if(message.cost > secWorstCost) {
            	worstCostInPool = message.cost;
            } else {
            	worstCostInPool = secWorstCost;
            }
        }
    }

    public synchronized ContextInformation getInfo(){
    	int size = pool.size();
    	if (size > 0) {
    		return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    	} else {
    		System.out.println(id+": getinfo size "+size);
    		return null;
    	}
    }
    
    //private Boolean isWorst(ContextInformation message) {
    	// check if cost is worst than worst cost in pool
    //	return message.cost > worstCostInPool;
    //}
    
    private Boolean isAlreadyInPool(ContextInformation message) {
    	for(int i = 0; i < pool.size(); i++){
    		// Same configurations have same cost
            if(pool.get(i).getCost() == message.cost){
                if (distance(message.getVariables(), pool.get(i).variables) == 0) {
                	return true;
                }
            }
        }
    	return false;
    }
    
    private double distance(int[] conf1, int[] conf2) {
    	double dis = 0.0;
    	int same = 0;
    	for(int i = 0; i < conf1.length; i++) {
    		if(conf1[i] == conf2[i]) same++;
    	}
    	dis = (conf1.length - same)/(double)conf1.length;
    	return dis;
    }
    
    public void clean() {
    	pool.clear();
    	worstCostInPool = -1;
    }
}
