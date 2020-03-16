/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.usp.poli.pcs.lti.moabhh.main;

import br.usp.poli.pcs.lti.jmetalhhhelper.core.ParametersforAlgorithm;
import br.usp.poli.pcs.lti.jmetalhhhelper.core.ParametersforHeuristics;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.AlgorithmBuilder;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.HypervolumeCalculator;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.IgdCalculator;
import br.usp.poli.pcs.lti.moabhh.core.CompleteMTSP;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.management.JMException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.ibea.IBEA;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.CrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 *
 * @author vinicius
 */
public class TSPAlgs {

    public static void main(String[] args) throws ConfigurationException, JMException, FileNotFoundException, IOException {
        String problemString;
        int populationSize, numGenerations, m;
        populationSize = 300;
        numGenerations = 750;
        String algName;
        String[] files;
        if (args.length > 3) {
            m = Integer.parseInt(args[0]);
            algName = args[1];
            problemString = args[2] + "_" + populationSize + "_" + algName;
            files = Arrays.copyOfRange(args, 3, args.length);
        } else {
            m = 2;
            algName = "NSGAII";
            problemString = "A_E_" + populationSize + "_" + algName;
            files = new String[2];
            files[0] = "/tsp/Kro/kroA100.tsp";
            files[1] = "/tsp/Kro/kroE100.tsp";
        }
        System.out.println(Arrays.toString(files));
        Problem problem = new CompleteMTSP(files);
        m=files.length;
        
        for (int i = 0; i < 40; i++) {
            Algorithm alg = TSPAlgs.createAlg(algName, populationSize, numGenerations, problem);

            
            alg.run();
            List result = (List) alg.getResult();
            //System.out.println(Experiment.printResults(result, problems[0], populationSize));
            System.out.println(i + 1);
            String dir = "result/" + alg.getName() + "/" + problem.getName() + "_" + problemString + "_" + problem.getNumberOfObjectives()+ "_" +populationSize+ "_" +numGenerations;
            new File(dir).mkdirs();
            String funFile = dir + "/FUN" + i + ".tsv";
            String varFile = dir + "/VAR" + i + ".tsv";
            System.out.println(alg.getClass().getCanonicalName()+" "+funFile);
            List<? extends Solution<?>> archive = generateNonDominated(result);
            new SolutionListOutput(archive)
                    .setSeparator("\t")
                    .setFunFileOutputContext(new DefaultFileOutputContext(funFile))
                    .setVarFileOutputContext(new DefaultFileOutputContext(varFile))
                    .print();
        }

    }

    public static Algorithm createAlg(String algName, int populationSize, int numGenerations, Problem problem) throws ConfigurationException, JMException, FileNotFoundException {

        AlgorithmBuilder ab = new AlgorithmBuilder(problem, () -> JMetalRandom.getInstance().nextDouble());
        ParametersforHeuristics pmxswap = new ParametersforHeuristics("PMX.PermutationSwap.default", problem.getNumberOfVariables());
        switch (algName) {
            case "IBEA":
                ParametersforAlgorithm ibea = new ParametersforAlgorithm("IBEA.default");
                if (populationSize % 2 != 0) {
                    populationSize++;
                }
                ibea.setPopulationSize(populationSize);
                ibea.setArchiveSize(populationSize);
                ibea.setMaxIteractions(numGenerations);
                return new IBEA(problem, populationSize, populationSize, numGenerations * populationSize, ab.generateSelection(), ab.generateCross(pmxswap), ab.generateMuta(pmxswap, ibea.getMaxIteractions()));
            case "NSGAII":
                ParametersforAlgorithm nsgaii = new ParametersforAlgorithm("NSGAII.default");
                if (populationSize % 2 != 0) {
                    populationSize++;
                }
                nsgaii.setPopulationSize(populationSize);
                nsgaii.setArchiveSize(populationSize);
                nsgaii.setMaxIteractions(numGenerations);
                SelectionOperator selection = ab.generateSelection();
                CrossoverOperator crossover = ab.generateCross(pmxswap);
                MutationOperator mutation = ab.generateMuta(pmxswap, nsgaii.getMaxIteractions());
                Comparator comparator = ab.generateComparator();

                int matingPoolSize = nsgaii.getPopulationSize();
                int offspringPopulationSize = nsgaii.getPopulationSize();
                
                return new NSGAII(problem,
                nsgaii.getMaxIteractions() * nsgaii.getPopulationSize(),
                nsgaii.getPopulationSize(), matingPoolSize, offspringPopulationSize, crossover,
                mutation, selection, comparator, new SequentialSolutionListEvaluator());
            case "SPEA2":
                ParametersforAlgorithm spea2 = new ParametersforAlgorithm("SPEA2.default");
                if (populationSize % 2 != 0) {
                    populationSize++;
                }
                spea2.setPopulationSize(populationSize);
                spea2.setArchiveSize(populationSize);
                spea2.setMaxIteractions(numGenerations);
                int k=1;
                return new SPEA2(problem, numGenerations, populationSize, ab.generateCross(pmxswap), ab.generateMuta(pmxswap, spea2.getMaxIteractions()), ab.generateSelection(), new SequentialSolutionListEvaluator<>(), k);
        }
        return null;
    }

    /**
     * Print results string.
     *
     * @param currentPopulation the current population
     * @param problem the problem
     * @param populationsize
     * @return the string
     * @throws FileNotFoundException the file not found exception
     */
    public static String printResults(List<? extends Solution<?>> currentPopulation,
            Problem problem, int populationsize) throws FileNotFoundException, IOException {
        List<? extends Solution<?>> archive = generateNonDominated(currentPopulation);
        String pf
                = "pareto_fronts/" + problem.getName() + "." + problem.getNumberOfObjectives() + "D.pf";
        HypervolumeCalculator hyp = new HypervolumeCalculator(problem.getNumberOfObjectives(), pf);
        double hypValue = hyp.execute(archive);
        IgdCalculator igd = new IgdCalculator(problem.getNumberOfObjectives(), pf);
        double igdValue = igd.execute(archive);

        return hypValue + ";" + igdValue + ";";
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
