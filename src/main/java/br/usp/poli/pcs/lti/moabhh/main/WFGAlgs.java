/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.usp.poli.pcs.lti.moabhh.main;

import br.usp.poli.pcs.lti.jmetalhhhelper.core.ParametersforAlgorithm;
import br.usp.poli.pcs.lti.jmetalhhhelper.core.ParametersforHeuristics;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.AlgorithmBuilder;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.ProblemFactory;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.EpsilonCalculator;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.HypervolumeCalculator;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.IgdCalculator;
import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.RniCalculator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.management.JMException;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.gde3.GDE3;
import org.uma.jmetal.algorithm.multiobjective.ibea.IBEA;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.paes.PAES;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 *
 * @author vinicius
 */
public class WFGAlgs {

    public static void main(String[] args) throws ConfigurationException, JMException, FileNotFoundException, IOException {
        String problemString;
        int populationSize, numGenerations, m, k, l;
        populationSize = 100;
        numGenerations = 750;
        l = 20;
        String algName;
        if (args.length == 3) {
            m = Integer.parseInt(args[0]);
            algName = args[1];
            problemString = args[2];
        } else {
            m = 2;
            algName = "GDE3";
            problemString = "WFG1";
        }
        k = 2 * (m - 1);

        Problem problem = ProblemFactory.getProblem(problemString, k, l, m);

        for (int i = 0; i < 40; i++) {
            Algorithm alg = WFGAlgs.createAlg(algName, populationSize, numGenerations, problem);

            alg.run();
            List result = (List) alg.getResult();
            List<? extends Solution<?>> archive = generateNonDominated(result, problem);
            System.out.println(WFGAlgs.printResults(archive, result, problem, populationSize));
            System.err.println(i + 1);
            String dir = "result/" + alg.getName() + "/" + problem.getName() + "_" + problem.getNumberOfObjectives();
            new File(dir).mkdirs();
            String funFile = dir + "/FUN" + i + ".tsv";
            String varFile = dir + "/VAR" + i + ".tsv";
            System.err.println(alg.getClass().getCanonicalName() + " " + funFile);
            new SolutionListOutput(archive)
                    .setSeparator("\t")
                    .setFunFileOutputContext(new DefaultFileOutputContext(funFile))
                    .setVarFileOutputContext(new DefaultFileOutputContext(varFile))
                    .print();
        }

    }

    public static Algorithm createAlg(String algName, int populationSize, int numGenerations, Problem problem) throws ConfigurationException, JMException, FileNotFoundException {

        System.err.println(algName);
        AlgorithmBuilder ab = new AlgorithmBuilder(problem, () -> JMetalRandom.getInstance().nextDouble());
        ParametersforHeuristics pmxswap = new ParametersforHeuristics("SBX.Poly.default", problem.getNumberOfVariables());
        //the same parameter as maashi
        //pmxswap.setCrossoverDistribution(10);
        //pmxswap.setMutationDistribution(20);
        //pmxswap.setCrossoverProbality(0.9);
        //the same parameter as maashi
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
            case "PAES":
                ParametersforAlgorithm paes = new ParametersforAlgorithm("IBEA.default");//use the same
                if (populationSize % 2 != 0) {
                    populationSize++;
                }
                int biSections = 3;
                paes.setPopulationSize(populationSize);
                paes.setArchiveSize(populationSize);
                paes.setMaxIteractions(numGenerations);
                return new PAES(problem, paes.getPopulationSize(), paes.getMaxIteractions() * paes.getPopulationSize(), biSections, ab.generateMuta(pmxswap, paes.getMaxIteractions()));
            case "GDE3":
                ParametersforHeuristics de = new ParametersforHeuristics("DE.Poly.default", problem.getNumberOfVariables());
                de.setDeCr(0.2);//TEMP @TODO new parameter file
                de.setDeF(0.2);
                //0.1 0.5 de https://books.google.com.br/books?id=PPxMVCKTbtoC&pg=PA662&lpg=PA662&dq=gde3+dtlz&source=bl&ots=chg6-dFh4P&sig=UXStuTF2UQI7eiXbnxHBX64fv3s&hl=pt-BR&sa=X&ved=0ahUKEwig4cGr2ebVAhUSPJAKHcnnAYoQ6AEINjAC#v=onepage&q=gde3%20dtlz&f=false
                return new GDE3((DoubleProblem) problem, populationSize, populationSize * numGenerations, new DifferentialEvolutionSelection(), (DifferentialEvolutionCrossover) ab.generateCross(de), new SequentialSolutionListEvaluator());

        }
        return null;
    }

    /**
     * Print results string.
     *
     * @param archive
     * @param currentPopulation the current population
     * @param problem the problem
     * @param populationsize
     * @return the string
     * @throws FileNotFoundException the file not found exception
     */
    public static String printResults(List archive, List<? extends Solution<?>> currentPopulation,
            Problem problem, int populationsize) throws FileNotFoundException, IOException {
        String strToReturn = "";
        if (problem.getName().contains("WFG")) {
            String pf
                    = "pareto_fronts/" + problem.getName() + "." + problem.getNumberOfObjectives() + "D.pf";
            HypervolumeCalculator hyp = new HypervolumeCalculator(problem.getNumberOfObjectives(), pf);
            hyp.updatePointsUsingNadir(WFGAlgs.getNadir(problem.getName(), problem.getNumberOfObjectives()));

            double hypValue = hyp.execute(archive);
            RniCalculator rni = new RniCalculator(problem.getNumberOfObjectives(), currentPopulation.size(),
                    pf);
            double rniValue = rni.execute(archive);
            IgdCalculator igd = new IgdCalculator(problem.getNumberOfObjectives(), pf);
            double igdValue = igd.execute(archive);
            EpsilonCalculator eps=new EpsilonCalculator(problem.getNumberOfObjectives(), pf);
            double epsilonvalue=eps.execute(archive);
            strToReturn=hypValue + ";" + igdValue + ";" + rniValue+ ";" +epsilonvalue;
        }
        return strToReturn;
    }

    public static List generateNonDominated(List population) {
        return SolutionListUtils.getNondominatedSolutions(population);
    }

    /**
     * Generate non dominated list.
     *
     * @param population the population
     * @param problem
     * @return the list
     */
    public static List generateNonDominated(List population, Problem problem) {
        if (problem.getName().contains("WFG")) {
            double[] nadir = WFGAlgs.getNadir(problem.getName(), problem.getNumberOfObjectives());
            List aux = WFGAlgs.removeWorseThanNadir(population, nadir, problem.getNumberOfObjectives());
            return SolutionListUtils.getNondominatedSolutions(aux);
        }
        return SolutionListUtils.getNondominatedSolutions(population);
    }

    protected static List removeWorseThanNadir(List population, double[] nadir, int m) {
        List newpopulation = new ArrayList();
        for (Object o : population) {
            Solution s = (Solution) o;
            boolean stillOk = true;
            for (int i = 0; i < m && stillOk; i++) {
                if (nadir[i] < s.getObjective(i)) {
                    //System.err.println(nadir[i]+" menor "+s.getObjective(i));
                    stillOk = false;
                }
            }
            if (stillOk) {
                newpopulation.add(s);
            }
        }
        return newpopulation;
    }

    protected static double[] getNadir(String problemName, int m) {
        double[] nadir = new double[m];
        if (problemName.contains("WFG")) {
            double[] base = new double[]{3.0, 5.0, 7.0, 9.0, 11.0, 13.0, 15.0, 17.0, 19.0, 21.0, 23.0, 25.0, 27.0, 29.0, 31.0, 33.0, 35.0, 37.0, 39.0, 41.0, 43.0};
            nadir = Arrays.copyOf(base, m);
        } else if (problemName.contains("DTLZ1") && m == 3) {
            Arrays.fill(nadir, 1.0);
        } else if (problemName.contains("DTLZ1")) {
            Arrays.fill(nadir, 10.0);
        } else if (problemName.contains("DTLZ") && m == 3) {
            Arrays.fill(nadir, 2.0);
        } else if (problemName.contains("DTLZ")) {
            Arrays.fill(nadir, 20.0);
        }
        return nadir;
    }

}
