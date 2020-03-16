package br.usp.poli.pcs.lti.moabhh.main;

import br.usp.poli.pcs.lti.jmetalhhhelper.util.metrics.HypervolumeCalculator;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.PointSolution;

/**
 *
 * @author vinicius
 */
public class calculateHypervolumeForTSP {

    public static void main(String[] args) throws FileNotFoundException {
        
        String ibeaPath = args[0];
        String spea2Path = args[1];
        String nsgaiiPath = args[2];
        String maebPathCopeland = args[3];
        String maebPathBorda = args[4];
        String maebPathKemeny = args[5];
        String pfKnown = args[6];
        int m = Integer.parseInt(args[7]);

        //read files
        ArrayFront ibeaFront = new ArrayFront(ibeaPath);
        ArrayFront spea2Front = new ArrayFront(spea2Path);
        ArrayFront nsgaiiFront = new ArrayFront(nsgaiiPath);
        ArrayFront maebCopelandFront = new ArrayFront(maebPathCopeland);
        ArrayFront maebBordaFront = new ArrayFront(maebPathBorda);
        ArrayFront maebKemenyFront = new ArrayFront(maebPathKemeny);
        ArrayFront pfFront = new ArrayFront(pfKnown);

        String dir = pfKnown + ".removedDominated.file";

        File f = new File(dir);
        if (f.exists()) {
            pfFront = new ArrayFront(dir);
        } else {
            //generate ndominated front
            List<PointSolution> pflist = FrontUtils.convertFrontToSolutionList(pfFront);
            NonDominatedSolutionListArchive nd = new NonDominatedSolutionListArchive();
            for (PointSolution sol : pflist) {
                nd.add(sol);
            }
            pflist = nd.getSolutionList();
            pfFront = new ArrayFront(pflist);
            new SolutionListOutput(pflist)
                    .setSeparator("\t")
                    .setFunFileOutputContext(new DefaultFileOutputContext(dir))
                    .print();
        }

        //remove worser points
        double[] nadir = FrontUtils.getMaximumValues(pfFront);
        ibeaFront = calculateHypervolumeForTSP.removeWorseThanNadir(ibeaFront, nadir, m);
        spea2Front = calculateHypervolumeForTSP.removeWorseThanNadir(spea2Front, nadir, m);
        nsgaiiFront = calculateHypervolumeForTSP.removeWorseThanNadir(nsgaiiFront, nadir, m);
        maebCopelandFront = calculateHypervolumeForTSP.removeWorseThanNadir(maebCopelandFront, nadir, m);
        maebBordaFront = calculateHypervolumeForTSP.removeWorseThanNadir(maebBordaFront, nadir, m);
        maebKemenyFront = calculateHypervolumeForTSP.removeWorseThanNadir(maebKemenyFront, nadir, m);

        HypervolumeCalculator fhc = new HypervolumeCalculator(m, pfFront);
        
        double ibeaHyp = 0;
        double spea2Hyp = 0;
        double nsgaiiHyp = 0;
        double copelandHyp = 0;
        double bordaHyp = 0;
        double kemenyHyp = 0;

         ibeaHyp = calculateHypervolumeForTSP.calc(fhc, ibeaFront);
         spea2Hyp = calculateHypervolumeForTSP.calc(fhc,spea2Front);
         nsgaiiHyp = calculateHypervolumeForTSP.calc(fhc,nsgaiiFront);
         copelandHyp = calculateHypervolumeForTSP.calc(fhc,maebCopelandFront);
         bordaHyp = calculateHypervolumeForTSP.calc(fhc,maebBordaFront);
         kemenyHyp = calculateHypervolumeForTSP.calc(fhc,maebKemenyFront);

        DecimalFormat nf = new DecimalFormat("###.####################");
        System.out.println(nf.format(ibeaHyp) + ";" + nf.format(spea2Hyp) + ";" + nf.format(nsgaiiHyp) + ";" + nf.format(bordaHyp) + ";" + nf.format(copelandHyp) + ";" + nf.format(kemenyHyp));

    }
    
    public static double calc(HypervolumeCalculator fhc, Front front){
        if(front==null){
            return 0D;
        }
        return fhc.execute(front);
    }

    public static ArrayFront removeWorseThanNadir(Front front, double[] nadir, int m) {
        List population = FrontUtils.convertFrontToSolutionList(front);
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
        if (!newpopulation.isEmpty()) {
            return new ArrayFront(newpopulation);
        }
        return null;
    }
}
