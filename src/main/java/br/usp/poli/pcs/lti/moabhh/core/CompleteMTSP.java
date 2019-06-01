package br.usp.poli.pcs.lti.moabhh.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.problem.impl.AbstractIntegerPermutationProblem;
import org.uma.jmetal.solution.PermutationSolution;
import org.uma.jmetal.util.JMetalException;

public class CompleteMTSP extends AbstractIntegerPermutationProblem {

    protected List<double[][]> allvariablesMatrix;
    protected int numberOfCities;

    public CompleteMTSP(String[] fileNames) throws IOException {
        allvariablesMatrix = new ArrayList<>();
        for (String fileName : fileNames) {
            allvariablesMatrix.add(readProblem(fileName));
        }
        setNumberOfVariables(numberOfCities);
        setNumberOfObjectives(allvariablesMatrix.size());
        setName("MultiobjectiveTSP");
    }

    /**
     * Evaluate() method
     * @param solution to evaluate
     */
    @Override
    public void evaluate(PermutationSolution<Integer> solution) {
        double[] fitness = new double[getNumberOfObjectives()];
        for (int i = 0; i < (numberOfCities - 1); i++) {
            int x;
            int y;

            x = solution.getVariableValue(i);
            y = solution.getVariableValue(i + 1);

            for (int j = 0; j < getNumberOfObjectives(); j++) {
                fitness[j] += allvariablesMatrix.get(j)[x][y];
            }
        }
        int firstCity;
        int lastCity;

        firstCity = solution.getVariableValue(0);
        lastCity = solution.getVariableValue(numberOfCities - 1);

        for (int j = 0; j < getNumberOfObjectives(); j++) {
            fitness[j] += allvariablesMatrix.get(j)[firstCity][lastCity];
        }

        for (int i = 0; i < getNumberOfObjectives(); i++) {
            solution.setObjective(i, fitness[i]);
        }
    }

    protected double[][] readProblem(String file) throws IOException {
        double[][] matrix = null;

        InputStream in = getClass().getResourceAsStream(file);
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);

        StreamTokenizer token = new StreamTokenizer(br);
        try {
            boolean found;
            found = false;

            token.nextToken();
            while (!found) {
                if ((token.sval != null) && ((token.sval.compareTo("DIMENSION") == 0))) {
                    found = true;
                } else {
                    token.nextToken();
                }
            }

            token.nextToken();
            token.nextToken();

            numberOfCities = (int) token.nval;

            matrix = new double[numberOfCities][numberOfCities];

            // Find the string SECTION  
            found = false;
            token.nextToken();
            while (!found) {
                if ((token.sval != null)
                        && ((token.sval.compareTo("SECTION") == 0))) {
                    found = true;
                } else {
                    token.nextToken();
                }
            }

            double[] c = new double[2 * numberOfCities];

            for (int i = 0; i < numberOfCities; i++) {
                token.nextToken();
                int j = (int) token.nval;

                token.nextToken();
                c[2 * (j - 1)] = token.nval;
                token.nextToken();
                c[2 * (j - 1) + 1] = token.nval;
            } // for

            double dist;
            for (int k = 0; k < numberOfCities; k++) {
                matrix[k][k] = 0;
                for (int j = k + 1; j < numberOfCities; j++) {
                    dist = Math.sqrt(Math.pow((c[k * 2] - c[j * 2]), 2.0)
                            + Math.pow((c[k * 2 + 1] - c[j * 2 + 1]), 2));
                    dist = (int) (dist + .5);
                    matrix[k][j] = dist;
                    matrix[j][k] = dist;
                }
            }
        } catch (IOException e) {
            new JMetalException("TSP.readProblem(): error when reading data file " + e);
        }
        return matrix;
    }

    @Override
    public int getPermutationLength() {
        return numberOfCities;
    }
}
