package br.usp.poli.pcs.lti.moabhh.core;

import br.usp.poli.pcs.lti.jmetalhhhelper.core.DoubleTaggedSolution;
import br.usp.poli.pcs.lti.jmetalhhhelper.core.PermutationTaggedSolution;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;
import org.uma.jmetal.solution.impl.DefaultIntegerPermutationSolution;

/**
 *
 * @author vinicius
 */
public class PopulationUtils{

    public static List realClone(List pop) {
        if (pop == null) {
            return pop;
        }
        List toReturn = new ArrayList<>();
        for (Object sol : pop) {
            if (sol instanceof DoubleTaggedSolution) {
                DoubleTaggedSolution s;
                if (sol instanceof DoubleTaggedSolution) {
                    s = new DoubleTaggedSolution((DoubleTaggedSolution) sol);
                } else {
                    s = new DoubleTaggedSolution((DefaultDoubleSolution) sol);
                }
                toReturn.add(s);
            } else if (sol instanceof DefaultIntegerPermutationSolution) {
                PermutationTaggedSolution s = new PermutationTaggedSolution((DefaultIntegerPermutationSolution) sol);
                toReturn.add(s);
            }
        }
        //toReturn.addAll(pop);
        return toReturn;
    }
}
