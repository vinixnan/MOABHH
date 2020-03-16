package br.usp.poli.pcs.lti.moabhh.main;

import br.usp.poli.pcs.lti.moabhh.agents.ElectionBasedHyperHeuristicAgent;
import br.usp.poli.pcs.lti.moabhh.agents.HyperHeuristicAgent;
import br.usp.poli.pcs.lti.moabhh.agents.IndicatorVoter;

import br.usp.poli.pcs.lti.jmetalhhhelper.util.IndicatorFactory;

import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.HypervolumeCalculator;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.IgdCalculator;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.RniCalculator;
import br.usp.poli.pcs.lti.moabhh.agents.AlgorithmAgentv2;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.Borda;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.Copeland;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.Kemeny;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.VotingMethod;

import cartago.CartagoService;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.ExtraPseudoRandom;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.ProblemFactoryExtra;
import br.usp.poli.pcs.lti.moabhh.agents.ProblemManager;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 * The type Cartago j metal.
 *
 * @param <S> the type parameter
 */
@SuppressWarnings("serial")
public class CartagoJMetalNottinghamLimitedEval<S extends Solution<?>> {

    /**
     * The entry point of application.
     *
     * @param args the command line arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {
        String problemName, path;
        int populationSize, numGenerations, l, k, m;
        int delta, epsilon, beta;
        String votingMethodName;
        int executionCounter = 1;
        int tipo=1;
        if (args.length == 10) {
            problemName = args[0];
            m = Integer.parseInt(args[1]);
            beta = Integer.parseInt(args[2]);
            delta = Integer.parseInt(args[3]);
            epsilon = Integer.parseInt(args[4]);
            k = Integer.parseInt(args[5]);
            l = Integer.parseInt(args[6]);
            votingMethodName = args[7];
            executionCounter = Integer.parseInt(args[8]);
            tipo = Integer.parseInt(args[9]);
        } else {
            problemName = "AucMaximization";
            //problemName = "NeuralNetDoublePoleBalancing";
            //problemName = "WFG1";
            m = 2; //M Number of objective functions
            beta = 100;//25
            epsilon = 50;//50
            delta = epsilon;
            k = 4; //Number of position parameters
            l = 20; //Number of distance parameters
            votingMethodName = "Copeland";
            //problemName = "WFG1";
            //delta=10;
            //epsilon=1000;
        }
        //System.out.println(beta+" "+delta+" "+epsilon+" in "+problemName+" "+m);
        long seed=-1;
        //seed=42;
        if(seed!=-1){
            ExtraPseudoRandom.getInstance().setSeed(seed);
            JMetalRandom.getInstance().setSeed(seed);
        }
        VotingMethod votingmethod;
        switch (votingMethodName) {
            case "Copeland":
                votingmethod = new Copeland();
                break;
            case "Kemeny":
                votingmethod = new Kemeny();
                break;
            case "Borda":
                votingmethod = new Borda();
                break;
            default:
                votingmethod = new Copeland();
        }
        populationSize = 24;
        numGenerations = 1000;//HARDCODED, in future change to 6250 750
        double factor=0;
        //problems="     "
        if(problemName.equals("DiskBrakeDesign") || problemName.equals("WeldedBeamDesign") || problemName.equals("KernelRidgeRegressionParameterTuning")){
            numGenerations=100;
            factor=0.1;
            
            //TESTE
            epsilon=1;
            delta=2;
        }
        else if(problemName.equals("VibratingPlatformDesign") || problemName.equals("OpticalFilter")){
            numGenerations=200;
            factor=0.2;
            
            //TESTE
            epsilon=1;
            delta=2;
        }
        else if(problemName.equals("HeatExchanger") || problemName.equals("AucMaximization")){
            numGenerations=500;
            factor=0.5;
            
            //TESTE
            epsilon=5;
            delta=2;
        }
        else if(problemName.equals("HydroDynamics") || problemName.equals("FacilityPlacement")){
            numGenerations=1000;
            factor=1;
            
            //TESTE
            epsilon=10;
            delta=2;
            
        }
        else if(problemName.equals("NeuralNetDoublePoleBalancing")){
            numGenerations=2000;
            factor=2;
        }
        /*
        if(tipo==1){
            //no beta
            epsilon=(int) (((double)epsilon)*factor);
            delta=epsilon;
        }
        if(tipo==2){
            //with beta
            beta=(int) (((double)beta)*factor);
            epsilon=(int) (((double)epsilon)*factor);
            delta=epsilon;
        }
        else if(tipo==3){
            //nothing
        }
        */
        numGenerations=numGenerations/populationSize;
        
        System.out.println(tipo+"_"+beta+"_"+epsilon+"_"+delta+"_"+numGenerations);
        path = "result/";
        long uId = ExtraPseudoRandom.getInstance().nextInt(0, 1000000000);
        Problem[] problems = ProblemFactoryExtra.getProblems(problemName, k, l, m);
        CartagoService.startNode();
        //CartagoService.registerLogger("default", new BasicLoggerOnFile("log.txt"));
        //CartagoService.addArtifactFactory("", new DefaultArtifactFactory());
        //System.out.println(problems[0].getName() + "." + problems[0].getNumberOfObjectives() + "D.pf");
        /*create manager and artifacts with no agent reference*/
        ProblemManager manager = new ProblemManager(problems[0], uId, populationSize, numGenerations, beta, delta, epsilon, votingmethod, executionCounter, seed);
        manager.setFileNameAppendix("BBBOX_"+votingMethodName+"_"+tipo);
        manager.initArtifacts();
        //System.out.println(votingmethod.getClass().getCanonicalName());
        //System.out.println(problems[0].getName());
        /*create others agents*/
        ArrayList<IndicatorVoter> voters=new ArrayList<>();
        //voters.add(new IndicatorVoter(IndicatorFactory.Random, uId, seed));
        voters.add(new IndicatorVoter(IndicatorFactory.Hypervolume, uId, seed));
        voters.add(new IndicatorVoter(IndicatorFactory.RNI, uId, seed));
        voters.add(new IndicatorVoter(IndicatorFactory.NR, uId, seed));
        voters.add(new IndicatorVoter(IndicatorFactory.HR, uId, seed));
        voters.add(new IndicatorVoter(IndicatorFactory.UD, uId, seed));
        voters.add(new IndicatorVoter(IndicatorFactory.Spread, uId, seed));
        if(seed!=-1){
            voters.add(new IndicatorVoter(IndicatorFactory.dummyAlgorithmEffort, uId, seed));
        }
        else{
            voters.add(new IndicatorVoter(IndicatorFactory.AlgorithmEffort, uId, seed));
        }
        
        

        /*
        voters.add(new IndicatorVoter(IndicatorFactory.R, uId));
        voters.add(new IndicatorVoter(IndicatorFactory.Spread, uId));
        voters.add(new IndicatorVoter(IndicatorFactory.AlgorithmEffort, uId));
        voters.add(new IndicatorVoter(IndicatorFactory.Spacing, uId));
        */
        
        ArrayList<AlgorithmAgentv2> algs=new ArrayList<>();
        algs.add(new AlgorithmAgentv2("GDE3", uId, "GDE3.default", "DE.Poly.default", seed));
        algs.add(new AlgorithmAgentv2("IBEA", uId, "IBEA.default", "SBX.Poly.default", seed));
        algs.add(new AlgorithmAgentv2("NSGAII", uId, "NSGAII.default", "SBX.Poly.default", seed));
        algs.add(new AlgorithmAgentv2("SPEA2", uId, "SPEA2.default", "SBX.Poly.default", seed));
        algs.add(new AlgorithmAgentv2("mIBEA", uId, "mIBEA.default", "SBX.Poly.default", seed));

        
        System.out.println("Run "+problems[0].getName());

        //HHAgent hh = new RandomHHAgent("HH");
        HyperHeuristicAgent hh = new ElectionBasedHyperHeuristicAgent("HH", uId, seed);


        /*create artifacts with agents reference*/
        manager.initArtifactsSecondPart();

        /*init other agents*/
        algs.forEach((v)->{
            v.init();
        });
        manager.init();

        voters.forEach((v) -> {
            v.init();
        });
        
        hh.init();
        if(seed!=-1){
            ExtraPseudoRandom.getInstance().setSeed(seed);
            JMetalRandom.getInstance().setSeed(seed);
        }
        /*START ALL*/
        manager.start();
        algs.forEach((v)->{
            v.start();
        });
        voters.forEach((v) -> {
            v.start();
        });

        hh.start();
    }

    /**
     * Print results string.
     *
     * @param currentPopulation the current population
     * @param problem the problem
     * @return the string
     * @throws FileNotFoundException the file not found exception
     */
    public static String printResults(List<? extends Solution<?>> currentPopulation,
            Problem problem) throws FileNotFoundException {
        List<? extends Solution<?>> archive = generateNonDominated(currentPopulation);
        String pf
                = "pareto_fronts/" + problem.getName() + "." + problem.getNumberOfObjectives() + "D.pf";
        HypervolumeCalculator hyp = new HypervolumeCalculator(problem.getNumberOfObjectives(), pf);
        double hypValue = hyp.execute(archive);
        RniCalculator rni = new RniCalculator(problem.getNumberOfObjectives(), currentPopulation.size(),
                pf);
        double rniValue = rni.execute(archive);
        IgdCalculator igd = new IgdCalculator(problem.getNumberOfObjectives(), pf);
        double igdValue = igd.execute(archive);
        return hypValue + ";" + igdValue + ";" + rniValue;
    }

    /**
     * Generate non dominated list.
     *
     * @param population the population
     * @return the list
     */
    public static List<? extends Solution<?>> generateNonDominated(
            List<? extends Solution<?>> population) {
        return SolutionListUtils.getNondominatedSolutions(population);
    }

}
