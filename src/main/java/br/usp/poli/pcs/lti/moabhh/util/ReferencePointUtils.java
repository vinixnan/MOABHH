package br.usp.poli.pcs.lti.moabhh.util;

import br.usp.poli.pcs.lti.jmetalhhhelper.core.ArrayFront;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.util.FrontUtils;

/**
 *
 * @author vinicius
 */
public class ReferencePointUtils {

    public static double[] nadir = null;
    public static double[] ideal = null;

    public static void findReference(String problemName, int m) throws FileNotFoundException {
        if (nadir == null) {
            nadir = new double[m];
            String pf = "pareto_fronts/" + problemName + "." + m + "D.pf";
            ArrayFront pfFront = new ArrayFront(pf);
            if (problemName.contains("WFG")) {
                double[] base = new double[]{3.0, 5.0, 7.0, 9.0, 11.0, 13.0, 15.0, 17.0, 19.0, 21.0, 23.0, 25.0, 27.0, 29.0, 31.0, 33.0, 35.0, 37.0, 39.0, 41.0, 43.0};
                nadir = Arrays.copyOf(base, m);
            } else if (problemName.contains("DTLZ1")) {
                Arrays.fill(nadir, 0.5);
            } else if (problemName.contains("DTLZ")) {
                Arrays.fill(nadir, 1.0);
            } else {
                ReferencePointUtils.nadir = FrontUtils.getMaximumValues(pfFront);
            }
            ReferencePointUtils.ideal = FrontUtils.getMinimumValues(pfFront);
        }
    }

    public static List removeWorseThanNadir(List population, double[] nadir, int m) {
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

    public static List removeWorseThanNadir(List population, String problem, int m) throws FileNotFoundException {
        double[] nadir = getNadir(problem, m);
        return removeWorseThanNadir(population, nadir, m);
    }

    public static Front removeWorseThanNadir(Front front, String problem, int m) throws FileNotFoundException {
        List population = FrontUtils.convertFrontToSolutionList(front);
        double[] nadir = getNadir(problem, m);
        List newpopulation = ReferencePointUtils.removeWorseThanNadir(population, nadir, m);
        if (!newpopulation.isEmpty()) {
            return new ArrayFront(newpopulation);
        }
        return null;
    }

    public static double[] getNadir(String problem, int m) throws FileNotFoundException {
        findReference(problem, m);
        return nadir;
    }

    public static double[] getIdeal(String problem, int m) throws FileNotFoundException {
        findReference(problem, m);
        return ideal;
    }
}
