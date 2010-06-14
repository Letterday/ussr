package robustReversible;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;



import ussr.remote.AbstractSimulationBatch;
import ussr.remote.SimulationBatch2000;
import ussr.remote.facade.ParameterHolder;

public class EightToCarRobustnessBatch extends SimulationBatch2000 {
    public static class Parameters extends ParameterHolder {
        public int number;
        public float minR, maxR, completeR, completeDegree, maxTime, resetRisk,resetInterval;
        public Integer seedMaybe;
        public Parameters(Class<?> mainClass, int number, float minR, float maxR, float completeR, float completeDegree, float maxTime, float resetRisk, float resetInterval) {
            this(mainClass,number,minR,maxR,completeR,completeDegree,maxTime,resetRisk,resetInterval,null);
        }
        public Parameters(Class<?> mainClass, int number, float minR, float maxR, float completeR, float completeDegree, float maxTime, float resetRisk, float resetInterval, Integer seed) {
            super(mainClass);
            this.number = number;
            this.minR = minR;
            this.maxR = maxR;
            this.completeR = completeR;
            this.completeDegree = completeDegree;
            this.maxTime = maxTime;
            this.resetRisk = resetRisk;
            this.resetInterval = resetInterval;
            this.seedMaybe = seed;
        }
        public String toString() {
            NumberFormat formatter = new DecimalFormat("0000");
            return (super.mainClass==null?"_":super.mainClass.getName())+"#"+formatter.format(number)+":minR="+minR+",maxR="+maxR+",comR="+completeR+",comD="+completeDegree+",maxT="+maxTime+(seedMaybe==null?",noseed":",aseed")+",rR="+resetRisk+",rI="+resetInterval;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Float.floatToIntBits(completeDegree);
            result = prime * result + Float.floatToIntBits(completeR);
            result = prime * result + Float.floatToIntBits(maxR);
            result = prime * result + Float.floatToIntBits(maxTime);
            result = prime * result + Float.floatToIntBits(minR);
            result = prime * result + number;
            result = prime * result + Float.floatToIntBits(resetInterval);
            result = prime * result + Float.floatToIntBits(resetRisk);
            return result;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            Parameters other = (Parameters) obj;
            if (Float.floatToIntBits(completeDegree) != Float
                    .floatToIntBits(other.completeDegree))
                return false;
            if (Float.floatToIntBits(completeR) != Float
                    .floatToIntBits(other.completeR))
                return false;
            if (Float.floatToIntBits(maxR) != Float.floatToIntBits(other.maxR))
                return false;
            if (Float.floatToIntBits(maxTime) != Float
                    .floatToIntBits(other.maxTime))
                return false;
            if (Float.floatToIntBits(minR) != Float.floatToIntBits(other.minR))
                return false;
            if (number != other.number)
                return false;
            if (Float.floatToIntBits(resetInterval) != Float
                    .floatToIntBits(other.resetInterval))
                return false;
            if (Float.floatToIntBits(resetRisk) != Float
                    .floatToIntBits(other.resetRisk))
                return false;
            return true;
        }
    }

    private List<ParameterHolder> parameters = new LinkedList<ParameterHolder>();
    private List<Class<? extends EightToCarRobustnessExperiment>> experiments = new ArrayList<Class<? extends EightToCarRobustnessExperiment>>();
    private PrintWriter logfile;
    
    public static void main(String argv[]) {
        new EightToCarRobustnessBatch(EightToCarSettings.EXPERIMENTS).start(EightToCarSettings.N_PARALLEL_SIMS,true);
    }

    private int sequenceIndex = -1;
    private List<Integer> randomSequence = new ArrayList<Integer>();
    private Random sequenceRandomizer = new Random(87);
    
    private void resetRandomSequence() {
        sequenceIndex = 0;
    }
    
    private int nextRandomFromSequence() {
        while(sequenceIndex>=randomSequence.size())
            randomSequence.add(sequenceRandomizer.nextInt());
        return randomSequence.get(sequenceIndex++);
    }
    
    private void newRandomSequence() {
        randomSequence = new ArrayList<Integer>();
        sequenceRandomizer = new Random(87);
        resetRandomSequence();
    }
    
    public EightToCarRobustnessBatch(Class<?>[] mainClasses) {
        int counter = 0;
        for(int ci=0; ci<mainClasses.length; ci++) {
            // Efficiency experiments, 0% failure risk, varying packet loss
            if(!EightToCarSettings.SKIP_EFFICIENCY)
                for(float risk = EightToCarSettings.START_RISK; risk<=EightToCarSettings.END_RISK; risk+=EightToCarSettings.RISK_INC) {
                    for(int i=0; i<EightToCarSettings.N_REPEAT; i++) {
                        parameters.add(new EightToCarRobustnessBatch.Parameters(mainClasses[ci],counter,Math.max(0, risk-EightToCarSettings.RISK_DELTA),risk,0,0,EightToCarSettings.TIMEOUT,0,0));
                    }
                    counter++;
                }
            if(!EightToCarSettings.SKIP_EFFICIENCY_EXTRA) {
                newRandomSequence();
                for(float risk = EightToCarSettings.START_RISK; risk<=EightToCarSettings.END_RISK; risk+=EightToCarSettings.RISK_INC) {
                    resetRandomSequence();
                    for(int i=0; i<EightToCarSettings.N_REPEAT; i++) {
                        parameters.add(new EightToCarRobustnessBatch.Parameters(mainClasses[ci],counter,Math.max(0, risk-EightToCarSettings.RISK_DELTA),risk,0,0,EightToCarSettings.TIMEOUT,0,0,nextRandomFromSequence()));
                    }
                    counter++;
                }
            }
            // Robustness experiments, varying failure risk, no packet loss
            if(!EightToCarSettings.SKIP_ROBUSTNESS) {
                newRandomSequence();
                for(float fail = EightToCarSettings.START_FAIL; fail<=EightToCarSettings.END_FAIL; fail+=EightToCarSettings.FAIL_INC) {
                    resetRandomSequence();
                    for(int i=0; i<EightToCarSettings.N_REPEAT; i++) {
                        parameters.add(new EightToCarRobustnessBatch.Parameters(mainClasses[ci],counter,EightToCarSettings.FAIL_COMM_RISK,EightToCarSettings.FAIL_COMM_RISK,fail,EightToCarSettings.COMPLETE_FAILURE_DEGREE,EightToCarSettings.TIMEOUT,0,0,nextRandomFromSequence()));
                    }
                    counter++;
                }
            }
            if(!EightToCarSettings.SKIP_RESET) {
                newRandomSequence();
                for(float interval = EightToCarSettings.RESET_RISK_TS_SIZE_MIN; interval<=EightToCarSettings.RESET_RISK_TS_SIZE_MAX; interval+=EightToCarSettings.RESET_RISK_TS_SIZE_DELTA)
                    for(float reset = EightToCarSettings.RESET_RISK_PER_TS_MIN; reset<=EightToCarSettings.RESET_RISK_PER_TS_MAX; reset+=EightToCarSettings.RESET_RISK_PER_TS_DELTA) {
                        resetRandomSequence();
                        for(int i=0; i<EightToCarSettings.N_REPEAT; i++)
                            parameters.add(new Parameters(mainClasses[ci],counter,0,0,0,0,EightToCarSettings.TIMEOUT,reset,interval,nextRandomFromSequence()));
                        counter++;
                    }
            }
        }
        try {
            logfile = new PrintWriter(new BufferedWriter(new FileWriter("eight-log.txt")));
        } catch(IOException exn) {
            throw new Error("Unable to open log file");
        }
        logfile.println("Starting "+parameters.size()+" experiments");
        System.out.println("Starting #batches="+parameters.size());
    }

    @Override
    protected ParameterHolder getNextParameters() {
        logfile.println("experiment "+parameters.get(0)+" starting"); logfile.flush();
        return parameters.remove(0);
    }

    @Override
    protected boolean runMoreSimulations() {
        return parameters.size()>0;
    }

    public void provideReturnValue(String experiment, String name, Object value) throws RemoteException {
        logfile.print("experiment "+experiment+" completed: ");
        if(name.equals("success")) {
            Object[] values = (Object[])value;
            float time = (Float)values[0];
            int packetCount = (Integer)values[1];
            logfile.println("Time taken:"+time);
            recordSuccess(experiment,time,packetCount);
        } else if(name.equals("timeout")) {
            logfile.println("Timeout:X");
            int packetCount = (Integer)value;
            recordFailure(experiment,packetCount);
        }
        else {
            logfile.println("Unknown value: "+name);
            recordFailure(experiment,0);
        }
        logfile.flush();
    }

    @Override
    protected void reportHook(Set<String> experimentsNames,
            Map<String, List<Float>> successes,
            Map<String, Integer> failures,
            Map<String, ParameterHolder> experimentParameters) {
        
    }

    @Override
    public void provideEventNotification(String experiment, String name, float time) throws RemoteException {
        super.recordEvent(experiment,name,time);
    }
    
    @Override
    protected String reportEventHook(String name, List<Float> set, int count) { 
        if(!name.equals("RESET")) {
            return "[unknown event "+name+"] ";
        }
        StringBuffer result = new StringBuffer(";reset=total,maxIn2; ");
        result.append(set.size()/(float)count+" ");
        float max = max(set);
        int maxInterval = 0;
        for(int i=0; i<=max; i++) {
            int thisInterval = count(i,i+2,set);
            if(thisInterval>maxInterval) maxInterval = thisInterval;
        }
        result.append(maxInterval/(float)count+" ");
        return result.toString();
    }

    private int count(int min, int max, List<Float> set) {
        int result = 0;
        for(float f: set)
            if(f>=min && f<max) result++;
        return result;
    }

    private float max(List<Float> set) {
        float result = Float.MIN_VALUE;
        for(float f: set)
            result=Math.max(result, f);
        return result;
    }

}
