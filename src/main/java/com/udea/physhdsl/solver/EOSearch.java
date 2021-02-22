package com.udea.physhdsl.solver;

import com.udea.physhdsl.ParamInformation;
import com.udea.physhdsl.adaptpso.EOParams;
import com.udea.physhdsl.adaptpso.TeamParams;
import com.udea.physhdsl.model.QAPModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EOSearch extends Metaheuristic{
	
	private static final Logger LOGGER = Logger.getLogger( EOSearch.class.getName() );
	

    private double[] pdf;
    private int[] fit;

    public enum Func {
        POWER(0), EXPONENTIAL(1), GAMMA(2);

        private static final Map<Integer, Func> mappingMap = new HashMap<Integer, Func>();

        static {
            for (Func m : Func.values()) {
                mappingMap.put(m.getValue(), m);
            }
        }
        private final int func;

        Func(int aFunc) {
            func = aFunc;
        }

        public int getValue() {
            return func;
        }
        public static Func getByFunc(int aFunc) {
            return mappingMap.get(aFunc);
        }
    }

    private static final RandomEnum<Func> randomFunc =
            new RandomEnum<Func>(Func.class);

    // var index is stored in the 10 LSB and the cost is stored on the remaining MSB
    /*public static int cmp (int a, int b){
        return((b >> 10) - (a >> 10));
    }*/

    private double powFunc(double tau, int x){ return Math.pow(x, -tau);}
    private double expFunc(double tau, int x){ return Math.exp(-tau * x);}
    private double gammaFunc(double tau, int x){
        double theta = Math.exp(tau);
        double constK =  Math.pow(theta, tau) * this.gamma(tau);
        return Math.pow(x, tau - 1) * Math.exp(-x / theta) / constK;
    }

    private double gamma(double n) {
        double invn = 1.0 / n;
        double g = ( 2.506628274631 * Math.sqrt(invn) +
                0.208885689552583 * Math.pow(invn, 1.5) +
                0.00870357039802431 * Math.pow(invn, 2.5) -
                (174.210665086855 * Math.pow(invn, 3.5)) / 25920.0 -
                (715.642372407151 * Math.pow(invn, 4.5)) / 1244160.0
        ) * Math.exp((-Math.log(invn) - 1) * n);
        return g;
    }

    // Communication Variables
    private double  tauUserSel;
    private double  tau;
    private int pdfUserSel; //pdf initial selection
    private Func pdfS = Func.POWER;  //pdf state
    private int selSecond;
    private double expDown;
    private double expUp;
    private double powDown;
    private double powUp;
    private double gammaDown;
    private double gammaUp;
    
    // PSO-adapt
    private EOParams pBest;
    private EOParams pCurrent;
    
    private double tauV;
    //PSO params
	private double c1 = 2.0, c2 = 2.0;
	//double tetha = c1+c2;
	private double w = 0.9;
	//double chi = 2*(0.5)/Math.abs(2-tetha-Math.sqrt(tetha*tetha-4*tetha));
	private int psoIters;
	private int psoDelMem;
	private int psoNoImprovement;
	
	private List<EOParams> paramHisto;
	
	private long iniTime;
	

    public EOSearch (int size){
        super(size);
        setMySolverType(Type.EO);
    }

    public void configHeuristic(QAPModel problemModel, Map<String, Object> configuration){
        super.configHeuristic(problemModel, configuration);
        pdf = new double[problemModel.getSize() + 1];// +1 since x in 1..size
        fit = new int[problemModel.getSize()];
        expDown = 6.385378048 * Math.pow(problemModel.getSize(), -1.033400799);
        expUp = 8.867754442 * Math.pow(problemModel.getSize(), -0.895936426);
        //powDown = 1.575467001 * Math.pow(problemModel.getSize(), -0.1448643794);
        //powUp = 2.426369897 * Math.pow(problemModel.getSize(), -0.1435045369);
        powDown = 0.2;
        powUp = 2;
        
        
        gammaDown = 1.575467001 * Math.pow(problemModel.getSize(), -0.1448643794);
        gammaUp = 2.426369897 * Math.pow(problemModel.getSize(), -0.1435045369);
        
        Object valOrNull = configuration.get("EO.tau");
        tauUserSel = valOrNull == null ? (1.0 + 1.0 / Math.log(problemModel.getSize())) : Double.parseDouble((String) valOrNull);
        valOrNull = configuration.get("EO.pdf");
        pdfUserSel = valOrNull == null ? -1 : (int) valOrNull;
        selSecond = 1; //opts("-EO_ss", 1);
        LOGGER.log(Level.INFO, "tau: "+tauUserSel+" -  pdf: "+pdfUserSel);
        
        valOrNull = configuration.get("Adapt.delMem");
        psoDelMem = valOrNull == null ? -1 : (int) valOrNull;
        
        
        paramHisto = new ArrayList<EOParams>();
        iniTime = System.nanoTime();
        
    }

    /**
     *  Initialize variables of the solver
     *  Executed once before the main solving loop
     */
    public void initVar(){
        super.initVar();
        //Console.OUT.println("Se llega al initvar de EOSearch");
        if ( pdfUserSel == -1 ) { // Select a random PDF
            pdfS = randomFunc.random();
        } else {
            if (pdfUserSel == 1) {
                pdfS = Func.POWER;
            } else if(pdfUserSel == 2){
                pdfS = Func.EXPONENTIAL;
            } else { //if(pdfUserSel == 3){
                pdfS = Func.GAMMA;
            }
        }

        if (tauUserSel == -1.0 ) { // Select a random tau from 0 to tau
            if (pdfS == Func.POWER) {
                tau = powDown + (powUp - powDown) * ThreadLocalRandom.current().nextDouble();
            }else if (pdfS == Func.EXPONENTIAL) {
                tau = expDown + (expUp - expDown) * ThreadLocalRandom.current().nextDouble();
            }if (pdfS == Func.GAMMA) {
                tau = gammaDown + (gammaUp - gammaDown) * ThreadLocalRandom.current().nextDouble();
            }
        }else {
            tau = tauUserSel;
        }
        
        
        LOGGER.log(Level.INFO, "pdf: "+pdfS.toString()+" tau: "+tau);
        
        tauV = 0;
        psoIters = 0;
        psoNoImprovement = 0;
        pCurrent = new EOParams(tau, pdfS.getValue(), -1);
        pBest = new EOParams(tau, pdfS.getValue(), -1);
        
        initPDF(pdfS);
    }

    private void initPDF(Func func){
    	
    	double time = (System.nanoTime() - iniTime)/1e9;
    	paramHisto.add(new EOParams(pCurrent, time));
    	
        double sum = 0.0;
        double  y = 0.0;

        for (int x = 1; x <= problemModel.getSize(); x++){
            if (func == Func.GAMMA){
                y = gammaFunc(tau, x);
            } else if (func == Func.EXPONENTIAL){
                y = expFunc(tau, x);
            }
            else if (func == Func.POWER){ //( this.pdfS == 1n )
                y = powFunc(tau, x);
            }
            pdf[x] = y;
            sum += y;
        }
        for (int x = 1; x <= problemModel.getSize(); x++){
            pdf[x] /= sum;
            //Console.OUT.println("Se inicializa pdf, posiciion: " + x + ". Valor: " + this.pdf(x));
        }
        // for (x in pdf.range())
    }

    /**
     *  Extremal Search process (in loop functionality)
     */
    public int search(int currentCost, int bestCost, int nIter) {
        selFirstVar();
        //val sz = this.problemModel.getSize();
        int newCost;
        if (selSecond == 0){
            newCost = selSecondRandom(currentCost);
        } else {
            newCost = selSecondMinConf(currentCost);
        }
        swapVariables(move.getFirst(), move.getSecond()); //adSwap(maxI, minJ,csp);
        nSwap++;
        problemModel.executedSwap(move.getFirst(), move.getSecond(), variables);
 		/*if(newCost < bestCost){
 			Console.OUT.print("Costo (EOSearch) in " + here + ". " + Runtime.worker() + ": " + newCost);
 			Utils.show(". Con variables: " , super.variables);
 			//displaySolution();
 		}*/
        return newCost;
    }


    private int pdfPick() {
        double p = ThreadLocalRandom.current().nextDouble();
        double fx;
        int x = 0;
        while( (fx = pdf[++x]) < p ){
            //Console.OUT.println("Valor fx: " + fx + ". Valor p: " + p);
            p -= fx;
        }
        return x - 1 ;
    }

    private void selFirstVar(){
        int i = -1;
        int cost;
        int selIndex = 0;
        boolean locMin = true;
        while((i = i + 1) < problemModel.getSize()) { //False if i < 0
            cost = problemModel.costOnVariable(i);
            // each position on the fit array is divided to contain both i and cost
            // variable index "i" is stored in the 10 LSB
            // the cost is stored in the remaining MSB
            fit[i] = cost << 10 | i;
            //Console.OUT.printf("%d %X %X \n",cost,cost,fit(i));
            // Detect local min:
            // can be applied on "first variable selction" only for QAP
            if (cost > 0) // cost is -delta for QAP.
                locMin = false;
        }

        fit = Arrays.stream(fit)
                .boxed()
                .sorted((a, b) -> (b >> 10) - (a >> 10))
                .mapToInt(si -> si)
                .toArray();

        if (locMin) onLocMin();
        // for (v in fit)
        //   Console.OUT.printf("%d %d \n",(v & 0xFFF),(v >>12));
        int index = pdfPick();
        //int sVar = fit[index] & 0x3FF;
        int sCost = fit[index] >> 10;
        //Console.OUT.printf("svar %d scost %d \n",sVar,sCost);
        int nSameFit = 0;
        for(int k =0; k < problemModel.getSize(); k++){
            int cCost = fit[k] >> 10;
            //Console.OUT.printf("cCost %d scost %d \n",cCost,sCost);
            if ( cCost < sCost)   // descending order
                break;

            if (cCost == sCost && ThreadLocalRandom.current().nextInt(++nSameFit) == 0)
            selIndex = fit[k] & 0x3FF;
        }
        // Save first variable selected into the move object
        move.setFirst(selIndex);
    }

    /**
     * 	selectVarMinConflict( csp : ModelAS) : Int
     * 	Computes swap and selects the minimum of cost if swap
     * 	@return the cost of the best move
     */
    private int selSecondMinConf(int currentCost) {
        int j;
        int cost;
        int second = 0;
        int nSameMin = 0;
        int minCost = Integer.MAX_VALUE;
        int first = move.getFirst();
        for (j = 0; j < problemModel.getSize(); j++){
            if (first == j) continue;
            cost = problemModel.costIfSwap(currentCost, j, first);
            if (cost < minCost){
                minCost = cost;
                second = j;
                nSameMin = 1;
            } else if (cost == minCost && ThreadLocalRandom.current().nextInt(++nSameMin) == 0){
                second = j;
            }
        }
        move.setSecond(second);
        return minCost;
    }

    private int selSecondRandom(int currentCost) {
        int randomJ = ThreadLocalRandom.current().nextInt(problemModel.getSize());
        int newCost = problemModel.costIfSwap(currentCost, randomJ, move.getFirst());
        move.setSecond(randomJ);
        return newCost;
    }

    /**
     *  Interact when Loc min is reached
     */
    private void onLocMin(){
        // communicate Local Minimum
        // solver.communicateLM( this.currentCost, cop.getVariables() as Valuation(sz));
        //int[] solverState = this.createSolverState();
        //this.solver.communicateLM( new State(sz, this.currentCost, problemModel.getVariables() as Valuation(sz), here.id as Int, solverState) );
    }

    /**
     *  Create EO Solver State array to be send to Pool
     *  oeState(0) = solverType
     *  oeState(1) = EO pdf type
     *  oeState(2) = EO "tau" value
     */
    /*private int[] createSolverState() {
        int[] eoState = new int[3];
        eoState[0] = getMySolverType().getValue();
        eoState[1] = pdfS.getValue();
        eoState[2] = (int)(tau * 1000.0); // TODO: convert double to Int??? levels ranges ???
        return eoState;
    }*/

    /**
     *  Process Solver State Array received from Pool
     *
     */
    protected void processSolverState( int[] state){
        // Random Search has no parameters to process
        int inSolverType = state[0];
        if (inSolverType == getMySolverType().getValue()){
            int inpdf = state[1];
            int intau = (int) (state[2] / 1000.0);
            if (pdfS.getValue() == inpdf) {
                tau = intau;
            } else {
                if (pdfS == Func.POWER) {
                    tau = powDown + (powUp - powDown) * ThreadLocalRandom.current().nextDouble();
                }else if (pdfS == Func.EXPONENTIAL) {
                    tau = expDown + (expUp - expDown) * ThreadLocalRandom.current().nextDouble();
                }
            }
            initPDF(pdfS);
        }
    }
    private static class RandomEnum<E extends Enum<E>> {

        //private static final Random RND = new Random();
        private final E[] values;

        public RandomEnum(Class<E> token) {
            values = token.getEnumConstants();
        }

        public E random() {
            return values[ThreadLocalRandom.current().nextInt(values.length)];
        }
    }
    
    public void adaptParameters(ParamInformation paramInfo, double divPercentageLimit) {
    	//double diversify_percentage_limit  =  1; //is necessary calculate depends on the current iteration
    	//TODO: Adapt Parameters according to  info received
    	LOGGER.log(Level.INFO, "param in ROTS, gain:"+paramInfo.gain()+" distance: "+paramInfo.distance());
    	if(paramInfo.gain() > 0) {
    	
			if (paramInfo.gain() <= divPercentageLimit && paramInfo.distance() > 0.66) {
				// is necessary diversify
				if (pdfS == Func.POWER) {
					tau -= (powUp - powDown) * 0.06;//6% change  the value of tau

				} else if (pdfS == Func.EXPONENTIAL) {
					tau -= (expUp - expDown) * 0.06;
				} else if (pdfS == Func.GAMMA) {
					tau += (gammaUp - gammaDown) * 0.06;
				}

			} else {
				// is necessary intensify
				if (pdfS == Func.POWER) {
					tau += (powUp - powDown) * 0.06;
				} else if (pdfS == Func.EXPONENTIAL) {
					tau += (expUp - expDown) * 0.06;
				} else if (pdfS == Func.GAMMA) {
					tau -= (gammaUp - gammaDown) * 0.06;
				}

			}
			
			// when tau overpass the maximum values
			if (pdfS == Func.POWER && tau > powUp) {
				tau = powDown + (powUp - powDown) * ThreadLocalRandom.current().nextDouble();
			} else if (pdfS == Func.EXPONENTIAL && tau > expUp) {
				tau = expDown + (expUp - expDown) * ThreadLocalRandom.current().nextDouble();
			} else if (pdfS == Func.GAMMA && tau > gammaUp) {
				tau = gammaDown + (gammaUp - gammaDown) * ThreadLocalRandom.current().nextDouble();
			}
			
			// when tau is less than the lower limit
			if (pdfS == Func.POWER && tau < powDown) {
				tau = powDown + (powUp - powDown) * ThreadLocalRandom.current().nextDouble();
			} else if (pdfS == Func.EXPONENTIAL && tau < expDown) {
				tau = expDown + (expUp - expDown) * ThreadLocalRandom.current().nextDouble();
			} else if (pdfS == Func.GAMMA && tau < gammaDown) {
				tau = gammaDown + (gammaUp - gammaDown) * ThreadLocalRandom.current().nextDouble();
			}
			
			double change_pdf_percentage_limit = 5; // necessary calculate this limits, depends on the current iterations
			if (paramInfo.gain() <= change_pdf_percentage_limit) {
				Func new_pdfS;
				do {
					new_pdfS = randomFunc.random();
				} while (new_pdfS == pdfS);
				pdfS = new_pdfS;
			}
		
    	}else {
    		//necessary create new parameters or continue with the same
    	}
    	        
        
        LOGGER.log(Level.INFO, "pdf: "+pdfS.toString()+" tau: "+tau);
        
        initPDF(pdfS);
    	
    }
    
    public void adaptParametersPSO(ParamInformation paramInfo, TeamParams tRef) {
    	
    	LOGGER.log(Level.INFO, "******************** Adapting parameters PSO EO");
    	
    	pCurrent.setGain(paramInfo.gain());
    	
    	if(pCurrent.getGain() > pBest.getGain()) {
    		// new best particle params
    		psoNoImprovement = 0;
    		pBest = new EOParams(pCurrent.getTau(), pCurrent.getPdf(), pCurrent.getGain());
    		// If current parameters are the best in particle, no adaption is carried out
    		return;
    	}else { 
    		psoNoImprovement++;
    	}
    	
    	EOParams gBest = tRef.updateGlobalEOParams(pCurrent);
    	
    	
    	LOGGER.log(Level.INFO, "*** PSO EO: tauV: "+tauV+" old tau:"+ pCurrent.getTau()+" best particle" + pBest.getTau()+" best global "+gBest.getTau());
    	
    	
    	// compute new velocity
    	double pVelocityTau = c1 * ThreadLocalRandom.current().nextDouble() * (pBest.getTau() - pCurrent.getTau());
    	double gVelocityTau = c2 * ThreadLocalRandom.current().nextDouble() * (gBest.getTau() - pCurrent.getTau());
    	
    	// TODO: implement adaption in pdf selection
    	    	
    	// new current
    	tauV = (w * tauV) + pVelocityTau + gVelocityTau;
    	//tauV = chi * (tauV + pVelocityTau + gVelocityTau);
    	
    	
    	tau = tau + tauV;
    	
    	if (tau < powDown) tau = powDown;
    	if (tau > powUp) tau = powUp; 	
    	
    	LOGGER.log(Level.INFO, "******************** PSO EO: old tau:"+ pCurrent.getTau()+" new tau " + tau);

    	pCurrent = new EOParams(tau, pdfS.getValue(), -1); 
    	initPDF(pdfS);   
    	
    	psoIters++;
    	//if(psoDelMem > 0 && psoIters % psoDelMem == 0) {
    	if(psoDelMem > 0 && psoNoImprovement == psoDelMem) {
    		//delete memory
    		LOGGER.log(Level.INFO, "-------------------- Delete local mem in particle EO");
    		psoNoImprovement = 0;
    		pBest = new EOParams(-1, -1, -1);
    	}
	}
    
    public void adaptParametersRandom() {
    	if (pdfS == Func.POWER) {
            tau = powDown + (powUp - powDown) * ThreadLocalRandom.current().nextDouble();
        }else if (pdfS == Func.EXPONENTIAL) {
            tau = expDown + (expUp - expDown) * ThreadLocalRandom.current().nextDouble();
        }if (pdfS == Func.GAMMA) {
            tau = gammaDown + (gammaUp - gammaDown) * ThreadLocalRandom.current().nextDouble();
        }
        pCurrent = new EOParams(tau, pdfS.getValue(), -1); 
    	initPDF(pdfS); 
    }
    
    public void printParams() {
    	System.out.println("EO particle params");
		for(EOParams p : paramHisto) {
			System.out.printf("%5.1f, %5.4f, %2d, %5.4f\n", p.getTime(), p.getTau(), p.getPdf(), p.getGain());
		}
    }
}