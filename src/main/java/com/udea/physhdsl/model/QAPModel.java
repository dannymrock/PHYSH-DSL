package com.udea.physhdsl.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.udea.physhdsl.Team;

public class QAPModel {
	
	private static final Logger LOGGER = Logger.getLogger( QAPModel.class.getName() );
    private int[][] flow;
    private int[][] dist;
    private int[][] delta;
    private int opt = 0;    /** optimal cost (0 if unknown) */
    private int bound = 0;  /** best bound (0 if unknown) */
    private int bks = 0;    /** best known solution cost (0 if unknown) */
    private int size;
    private int baseValue;
    private Random random;
    //protected String inPathVectorSol;

    public QAPModel(int size){
        this.size = size;
        random = new Random();
        flow = new int[size][size];
        dist = new int[size][size];
        delta = new int[size][size];
        baseValue = 0;
    }

    public QAPModel(QAPModel model){
        size = model.getSize();
        random = new Random();
        flow = model.flow;
        dist = model.dist;
        delta = new int[size][size];
        opt = model.opt;
        bound = model.bound;
        bks = model.bks;
        baseValue = model.baseValue;
    }

    public int getSize(){
        return size;
    }


    /**
     *  Compute the cost difference if elements i and j are permuted
     */
    public int computeDelta(int i, int j, int[] variables) {
        int pI = variables[i];
        int pJ = variables[j];
        int k;
        int dis = (flow[i][i] - flow[j][j]) * (dist[pJ][pJ] - dist[pI][pI]) +
                (flow[i][j] - flow[j][i]) * (dist[pJ][pI] - dist[pI][pJ]);

        for(k = 0; k < i; k++){
            dis = getDis(i, j, variables, pI, pJ, k, dis);
        }

        while(++k < j){
            dis = getDis(i, j, variables, pI, pJ, k, dis);
        }

        while(++k < size){
            dis = getDis(i, j, variables, pI, pJ, k, dis);
        }
        return dis;
    }

    private int getDis(int i, int j, int[] variables, int pI, int pJ, int k, int dis) {
        int pK;
        pK = variables[k];
        dis += (flow[k][i] - flow[k][j]) * (dist[pK][pJ] - dist[pK][pI]) +
                (flow[i][k] - flow[j][k]) * (dist[pJ][pK] - dist[pI][pK]);
        return dis;
    }

    /**
     *  As above, compute the cost difference if elements i and j are permuted
     *  but the value of delta[i][j] is supposed to be known before
     *  the transposition of elements r and s.
     */
    private int computeDeltaPart(int i, int j, int r, int s, int[] variables) {
        int pI = variables[i];
        int pJ = variables[j];
        int pR = variables[r];
        int pS = variables[s];

        return (delta[i][j] +
                (flow[r][i] - flow[r][j] + flow[s][j] - flow[s][i]) *
        (dist[pS][pI] - dist[pS][pJ] + dist[pR][pJ] - dist[pR][pI]) +
                (flow[i][r] - flow[j][r] + flow[j][s] - flow[i][s]) *
        (dist[pI][pS] - dist[pJ][pS] + dist[pJ][pR] - dist[pI][pR]));
    }

    public int costOfSolution(int size, boolean shouldBeRecorded, int[] variables) {
        int r = 0;
        for(int i = 0; i < size; i++)
            for(int j = 0; j < size; j++)
                r += flow[i][j] * dist[variables[i]][variables[j]];
        if (shouldBeRecorded)
            for(int i = 0; i < size; i++)
                for(int j = i + 1; j < size; j++){
                    delta[i][j] = computeDelta(i, j, variables);
                    //System.out.println("DeltaPart calculado");
                }
        //System.out.println("MsgType_0. CostOfSolution ejecutado en QAPModel");
        return r;
    }

    //Jason: New method for calculate costofSolution for an input solution
    /*public int costOfSolution(int size, int[] solution) {
        int r = 0;
        for(int i = 0; i < size; i++)
            for(int j = 0; j < size; j++)
                r += flow[i][j] * dist[solution[i]][solution[j]];
        return r;
    }*/

    public int costIfSwap(int currentCost, int i1, int i2) {
        //return currentCost + delta(i1 as Int , i2 as Int) as Int;
        int i1v = i1;
        int i2v = i2;
        if (i1 > i2){
            i1v = i2;
            i2v = i1;
        }
        //System.out.println("currentCost + delta(i1v, i2v): " + (currentCost + delta(i1v, i2v)));
        return currentCost + delta[i1v][i2v];
    }

    public void executedSwap(int i1, int i2, int[] variables) {
        int temp = variables[i1];
        if (i1 >= i2){
            int tmp = i1;
            i1 = i2;
            i2 = tmp;
        }
        for (int i = 0; i < size; i++)
            for (int j = i + 1; j < size; j++)
                if (i != i1 && i != i2 && j != i1 && j != i2)
                    delta[i][j] = computeDeltaPart(i, j, i1, i2, variables);
 				else
                    delta[i][j] = computeDelta(i, j, variables);
    }

    /**
     * 	costOnVariable( i : Int ) : Int
     * 	This function computes the cost of individual variable i
     * 	@param i This is the variable that we want to know the cost
     *  @return Int value with the cost of this variable
     */
    public int costOnVariable(int i) {
        int r = Integer.MIN_VALUE;
        for (int j = 0; j < this.size; j++){
            if (i == j) continue;
            int d = (i < j) ? delta[i][j] : delta[j][i];
            d = -d;
            if (d > r) r = d;
        }
        return r;
    }

    public double distance(int size, int[] conf1, int[] conf2) {
        int count = 0;
        for (int i = 0; i < size; i++){
            //Logger.debug("comparing: "+conf1(i)+" - "+conf2(i));
            if(conf1[i] != conf2[i]) count++;
        }
        double dis = ((double) count) / size;
        //Console.OUT.println("distance in ModelAS = "+dis);
        return dis;
    }

    /** load data
     *  load the data in filePath to the data structures matrixFlow and matrixDist
     *  @param filePath path of the data file to be loaded
     *  @return true if success, false if filePath is a directory
     */
    public boolean loadData(String filePath){
        File filep = new File(filePath);
        if (filep.isDirectory()) return false;
        LOGGER.log(Level.INFO,"\n--   Solving "+filePath+" ");
        //Load first line with headers size p1 p2
        Scanner fr = null;
        try {
            fr = new Scanner(filep);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String fLine = fr.nextLine(); //get first line
        int[] header = readParameters(fLine);
        int sizeF = header[0];
        opt = header[1];
        bks = header[2];
        bound = 0;
        if(opt < 0){
            bound = -opt;
            opt = 0;
        }else{
            bound = opt;
        }
        LOGGER.log(Level.INFO, "file: "+filePath+" size: "+sizeF+" bound: "+bound+" opt: "+opt+" bks: "+bks);

        //Load Problem
        readMatrix(fr, sizeF);
        fr.close();
        return true;
    }

    static int[] readParameters(String line) {
        int j = 0;
        String buffer =  "";
        int[] x = new int[3]; // three parameters are expected
        for(int i = 0 ; i < line.length() ; i++){
            if( line.toCharArray()[i] == ' ' || line.toCharArray()[i] == '\n' ){// Skip blank spaces and new line
                x[j++] = Integer.parseInt(buffer);
                //System.out.println("x "+(j-1)+" = "+x(j-1));
                buffer = "";
            }else{
                buffer += line.toCharArray()[i];
            }
        }
        x[j] = Integer.parseInt(buffer);
        return x;
    }

    private void readMatrix(Scanner fr, int sizeF){
        try{
            int i = 0;
            int j;
            String line;
            int fLine = 0;
            int dLine = 0;
            while (fr.hasNextLine()){// It seems that the end of line characters '\n' are removed from the line
                line = fr.nextLine();
                //System.out.println("Línea: " + line);

                String[] splitStr = line.split("\\s+");
                i++;
                j = 0;
                if (i >= 2 && i < sizeF + 2){
                	//System.out.println("mF:"+i+" :"+line);
                    //reading flow matrix
                    if (j < sizeF){
                        /*for (int k = 0; k < sizeF; k++) {
                            if (splitStr[k].isEmpty()){continue;}
                            //System.out.println(k+": "+splitStr[k]);
                            flow[fLine][k] = Integer.parseInt(splitStr[k]);;
                        }*/
                        int k = 0;
                        for(String s: splitStr) {
                        	if(s.isEmpty()) {continue;}
                        	flow[fLine][k++] = Integer.parseInt(s);
                        }
                        
                    }
                    fLine++;
                }else if (i > sizeF + 2 && i <= sizeF * 2 + 2){
                    //System.out.println("mD:"+i+" :"+line);
                    // Reading Distance Matrix
                    if (j < sizeF){
                        /*for (int k = 0; k < sizeF; k++) {
                            if (splitStr[k].isEmpty()){continue;}
                            dist[dLine][k] = Integer.parseInt(splitStr[k]);
                        }*/
                        int k = 0;
                        for(String s: splitStr) {
                        	if(s.isEmpty()) {continue;}
                        	dist[dLine][k++] = Integer.parseInt(s);
                        }
                    }
                    dLine++;
                }
            }
            //printMatrix(sizeF, flow);
            //printMatrix(sizeF, dist);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void clearProblemModel(){
        delta = new int[size][size];
    }

    public void printMatrix(int size, int[][] matrix){
        System.out.println("*******");
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println("");
        }
    }

    /**
     *  CHECK_SOLUTION
     *
     *  Checks if the solution is valid.
     */

    public boolean verify(int size, int[] match) {
        //Check Permutation
        int[] permutV = new int[size];
        int baseV = this.baseValue;
        for (int value : match) {
            permutV[value - baseV]++;
            if (permutV[value - baseV] > 1) {
                LOGGER.log(Level.SEVERE,"Not valid permutation, value " + value + " is repeted");
            }
        }
        int r  = 0;
        for(int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                r += this.flow[i][j] * this.dist[match[i]][match[j]];
            }
        }
        LOGGER.log(Level.INFO, "Verified Cost: "+ r);
        return (r == 0);
    }

    private void printMatrices(){
        System.out.println("\nMatrix1");
        int i = 0;
        for (i = 0; i < size; i++ ){
            System.out.print( (i+1) + " : ");
            for(int j: this.flow[i])
                System.out.print( j + " ");
            System.out.println("");
        }
        System.out.println("Matrix2");
        for ( i = 0; i < size; i++ ){
            System.out.print((i+1) + ": ");
            for(int j: this.dist[i])
                System.out.print( j + " ");
            System.out.println("");
        }
    }


    public int nextJ(int i, int j, boolean exhaustive) {
        ///Console.OUT.println("i= "+i+"  j= "+j+"  bp-i= "+bpi(i));
        int newj = j;
        if (j < 0 && exhaustive) // != 0n)
            newj = i;
        return newj + 1;
    }

    public int nextI(int i) {
        return i + 1;
    }
}
