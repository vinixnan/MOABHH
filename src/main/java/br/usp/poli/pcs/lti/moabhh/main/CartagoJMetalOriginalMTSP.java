package br.usp.poli.pcs.lti.moabhh.main;

import br.usp.poli.pcs.lti.moabhh.agents.AlgorithmAgent;
import br.usp.poli.pcs.lti.moabhh.agents.ElectionBasedHyperHeuristicAgent;
import br.usp.poli.pcs.lti.moabhh.agents.HyperHeuristicAgent;
import br.usp.poli.pcs.lti.moabhh.agents.IndicatorVoter;
import br.usp.poli.pcs.lti.moabhh.agents.ProblemManager;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.IndicatorFactory;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.HypervolumeCalculator;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.IgdCalculator;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.RniCalculator;
import br.usp.poli.pcs.lti.moabhh.core.CompleteMTSP;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.ExtraPseudoRandom;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.Borda;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.Copeland;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.Kemeny;
import br.usp.poli.pcs.lti.moabhh.core.votingmethods.VotingMethod;

import cartago.CartagoService;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;

/**
 * The type Cartago j metal.
 *
 * @param <S> the type parameter
 */
@SuppressWarnings("serial")
public class CartagoJMetalOriginalMTSP<S extends Solution<?>> {

  /**
   * The entry point of application.
   *
   * @param args the command line arguments
   * @throws Exception the exception
   */
  public static void main(String[] args) throws Exception {
    String problemString, path;
    int populationSize, numGenerations, m;
    populationSize = 300;
    numGenerations = 750;
    
    int delta, epsilon, beta;
    String votingMethodName;
    int executionCounter=1;
    String[] files;
    if (args.length > 7) {
      m = Integer.parseInt(args[0]);
      beta = Integer.parseInt(args[1]);
      delta = Integer.parseInt(args[2]);
      epsilon = Integer.parseInt(args[3]);
      votingMethodName = args[4];
      executionCounter = Integer.parseInt(args[5]);
      problemString=args[6]+"_"+populationSize+"_"+votingMethodName+"_"+delta+"_"+epsilon+"_"+beta;
      files=Arrays.copyOfRange(args, 7, args.length);
    } else {
      files=new String[4];
      files[0]="/tsp/Kro/kroA100.tsp";
      files[1] = "/tsp/Kro/kroE100.tsp";
      files[2] = "/tsp/Kro/kroC100.tsp";
      files[3] = "/tsp/Kro/kroB100.tsp";
      m=files.length;
      votingMethodName="Copeland";
      beta = 3;
      epsilon = 5;//artigo TSP
      delta = 50;//artigo TSP
      problemString="A_E_"+populationSize+"_"+votingMethodName+"_"+delta+"_"+epsilon+"_"+beta;
    }
    //System.out.println(beta+" "+delta+" "+epsilon+" in "+problemName+" "+nObj);
    long seed=42;
    VotingMethod votingmethod;
    switch(votingMethodName){
        case "Copeland":
            votingmethod=new Copeland();
            break;
        case "Kemeny":
            votingmethod=new Kemeny();
            break;
        case "Borda":
            votingmethod=new Borda();
            break;
        default:
            votingmethod=new Copeland();
    }
    //System.out.println(Arrays.toString(files));
    path = "result/";
    
      try {
          Thread.sleep(ExtraPseudoRandom.getInstance().nextInt(0, 10000));                 //1000 milliseconds is one second.
      } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
      }
    long uId=System.currentTimeMillis();
    Problem problem = new CompleteMTSP(files);
    m=files.length;
    CartagoService.startNode();
    //CartagoService.registerLogger("default", new BasicLoggerOnFile("log.txt"));
    //CartagoService.addArtifactFactory("", new DefaultArtifactFactory());

        /*create manager and artifacts with no agent reference*/
    ProblemManager manager = new ProblemManager(problem,uId, populationSize, numGenerations, beta, delta, epsilon, votingmethod, executionCounter, seed);
    manager.setFileNameAppendix(problemString);
    manager.initArtifacts();

        /*create others agents*/
    IndicatorVoter idv1 = new IndicatorVoter(IndicatorFactory.Hypervolume,uId, seed);
    //IndicatorVoter idv2 = new IndicatorVoter(IndicatorFactory.IGD,uId);
    //IndicatorVoter idv3 = new IndicatorVoter(IndicatorFactory.GD,uId);
    IndicatorVoter idv4 = new IndicatorVoter(IndicatorFactory.RNI,uId, seed);
    IndicatorVoter idv5 = new IndicatorVoter(IndicatorFactory.Spread,uId, seed);
    IndicatorVoter idv6 = new IndicatorVoter(IndicatorFactory.AlgorithmEffort,uId, seed);
    //IndicatorVoter idv7 = new IndicatorVoter(IndicatorFactory.Epsilon,uId);

    AlgorithmAgent ag1 = new AlgorithmAgent("NSGAII",uId, "NSGAII.default", "PMX.PermutationSwap.default", seed);
    AlgorithmAgent ag2 = new AlgorithmAgent("SPEA2",uId, "SPEA2.default", "PMX.PermutationSwap.default", seed);
    AlgorithmAgent ag3 = new AlgorithmAgent("IBEA",uId, "IBEA.default", "PMX.PermutationSwap.default", seed);

    //HHAgent hh = new RandomHHAgent("HH");
    HyperHeuristicAgent hh = new ElectionBasedHyperHeuristicAgent("HH",uId, seed);


        /*create artifacts with agents reference*/
    manager.initArtifactsSecondPart();

        /*init other agents*/
    ag1.init();
    ag2.init();
    ag3.init();
    //ag4.init();
    manager.init();
    idv1.init();
    //idv2.init();
    //idv3.init();
    idv4.init();
    idv5.init();
    idv6.init();
    //idv7.init();
    hh.init();

        /*START ALL*/
    manager.start();
    ag1.start();
    ag2.start();
    ag3.start();
    //ag4.start();

    idv1.start();
    //idv2.start();
    //idv3.start();
    idv4.start();
    idv5.start();
    idv6.start();
    //idv7.start();

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
    String pf =
        "pareto_fronts/" + problem.getName() + "." + problem.getNumberOfObjectives() + "D.pf";
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
