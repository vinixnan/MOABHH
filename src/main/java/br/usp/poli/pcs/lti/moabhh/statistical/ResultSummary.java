package br.usp.poli.pcs.lti.moabhh.statistical;

import com.google.common.primitives.Doubles;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;

/**
 * The type Result summary.
 */
public class ResultSummary {

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   * @throws Exception the exception
   */
  public static void main(String[] args) throws Exception {
    String dirName = "estatisticaTotal/";
    String[] algNames = {"IBEA", "NSGA.II", "SPEA2", "Random", "Copeland"};
    String[] metricsNames = {"Hypervolume", "RNI", "IGD", "GD", "Spread"};
    //"DTLZ1", "DTLZ2", "DTLZ3", "DTLZ4", "DTLZ5", "DTLZ6", "DTLZ7",
    String[] problems = {"WFG1", "WFG2", "WFG3", "WFG4", "WFG5", "WFG6", "WFG7", "WFG8", "WFG9"};
    //"DTLZ1", "DTLZ2", "DTLZ3", "DTLZ4", "DTLZ5", "DTLZ6", "DTLZ7",
    String[] problemsCompleteName2obj = {"WFG1", "WFG2", "WFG3", "WFG4", "WFG5", "WFG6", "WFG7",
        "WFG8", "WFG9"};
    ResultSummary rs = new ResultSummary();
    rs.printTables(dirName, problemsCompleteName2obj, algNames, metricsNames, problems, 2);
    //System.out.println("\\newpage");
    // "DTLZ5_3_12", "DTLZ6_3_12",
    String[] problemsCompleteName3obj = {"WFG1", "WFG2", "WFG3", "WFG4", "WFG5", "WFG6", "WFG7",
        "WFG8", "WFG9"};
    rs.printTables(dirName, problemsCompleteName3obj, algNames, metricsNames, problems, 3);
  }

  /**
   * Read test results statistical test.
   *
   * @param fileName the file name
   * @return the statistical test
   * @throws IOException the io exception
   */
  public StatisticalTest readTestResults(String fileName) throws IOException {
    StatisticalTest st = new StatisticalTest();
    String read = FileUtils.readFile(fileName);
    String[] valuesFromText = read.split("\n");
    HashMap<String, Boolean> comparision = new HashMap<>();
    ArrayList<Double> averages = new ArrayList<>();
    String metric = "";
    for (String line : valuesFromText) {
      if (line.contains("---------------------------------")) {
        String aux = line.replace("---------------------------------", "").replace("\"", "")
            .replace("[1]", "").trim();
        if (averages.size() > 0) {
          st.getAllAverages().add(averages);
          st.getAllComparision().add(comparision);
          st.getMetrics().add(metric);
        }
        metric = aux;
        comparision = new HashMap<String, Boolean>();
        averages = new ArrayList<>();
      } else if (line.contains("TRUE") || line.contains("FALSE")) {
        //IS Comparation
        String[] cmps = line.trim().split(" ");
        String lastComparing = "";
        for (String str : cmps) {
          if (str.contains("-")) {
            lastComparing = str;
          } else if (str.contains("TRUE")) {
            comparision.put(lastComparing, Boolean.TRUE);
          } else if (str.contains("FALSE")) {
            comparision.put(lastComparing, Boolean.FALSE);
          }
        }
      } else if (line.contains("\"") && line.contains(";")) {
        //is averages
        String[] values = line.replace("\"", "").replace("[1]", "").trim().split(";");
        for (String value : values) {
          averages.add(Double.parseDouble(value));
        }
      } else if (line.contains("\"")) {
        //is problem name
        String problemName = line.replace("\"", "").replace("[1]", "").trim();
        st.setProblemName(problemName);
      }
    }
    if (averages.size() > 0) {
      st.getAllAverages().add(averages);
      st.getAllComparision().add(comparision);
      st.getMetrics().add(metric);
    }
    return st;
  }

  /**
   * Read all array list.
   *
   * @param dirName the dir name
   * @param problemsCompleteName the problems complete name
   * @param qtdObj the qtd obj
   * @return the array list
   */
  public ArrayList<StatisticalTest> readAll(String dirName, String[] problemsCompleteName,
      int qtdObj) {
    String prefix = "saida";
    String sufix = "_" + qtdObj + " test.log";
    ArrayList<StatisticalTest> allProblemsResults = new ArrayList<>();
    for (String problem : problemsCompleteName) {
      String fileName = dirName + prefix + problem + sufix;
      try {
        StatisticalTest read = this.readTestResults(fileName);
        allProblemsResults.add(read);
      } catch (IOException ex) {
        Logger.getLogger(ResultSummary.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return allProblemsResults;
  }

  /**
   * Print tables.
   *
   * @param dirName the dir name
   * @param problemsCompleteName the problems complete name
   * @param algNames the alg names
   * @param metricsNames the metrics names
   * @param problems the problems
   * @param numOfObjectives the n obj
   */
  public void printTables(String dirName, String[] problemsCompleteName, String[] algNames,
      String[] metricsNames, String[] problems, int numOfObjectives) {
    ArrayList<StatisticalTest> all = this.readAll(dirName, problemsCompleteName, numOfObjectives);
    DecimalFormat df = new DecimalFormat("#0.0000");
    for (int idmetric = 0; idmetric < metricsNames.length; idmetric++) {
      System.out.println("\\begin{table}[!htb]\n"
          + "\\centering\n"
          + "\\caption{" + metricsNames[idmetric] + " for " + numOfObjectives + " objectives"
          + "}\n"
          + "\\label{" + "tab:" + metricsNames[idmetric].toLowerCase() + numOfObjectives + "obj"
          + "}\n"
          + "\\begin{tabular}{l|cccccccc}");
      System.out.print("Problem");
      String join = " & ";
      for (int i = 0; i < algNames.length; i++) {
        System.out.print(join + algNames[i]);
      }
      System.out.println(" \\\\ \\hline");
      for (int idproblem = 0; idproblem < all.size(); idproblem++) {
        ArrayList<Double> averagesArray = all.get(idproblem).getAllAverages().get(idmetric);
        double[] averages = ArrayUtils
            .toPrimitive(averagesArray.toArray(new Double[averagesArray.size()]));
        int bestPos;
        if (idmetric == 2 || idmetric == 3) {
          //GD and IGD
          double min = Doubles.min(averages);
          bestPos = Doubles.indexOf(averages, min);
        } else {
          //Spread Hypervolume and RNI
          double max = Doubles.max(averages);
          bestPos = Doubles.indexOf(averages, max);
        }
        String bestAlg = algNames[bestPos];
        HashMap<String, Boolean> comparision = all.get(idproblem).getAllComparision().get(idmetric);
        boolean[] statisticallyDifferent = new boolean[algNames.length];
        Arrays.fill(statisticallyDifferent, true);
        statisticallyDifferent[bestPos] = false;
        for (int i = 0; i < algNames.length; i++) {
          String name = algNames[i];
          if (!name.equals(bestAlg)) {
            Boolean resp = comparision.get(bestAlg + "-" + name);
            if (resp == null) {
              resp = comparision.get(name + "-" + bestAlg);
            }
            statisticallyDifferent[i] = resp;
          }
        }
        join = " & ";
        System.out.print(problems[idproblem]);
        for (int i = 0; i < algNames.length; i++) {
          if (!statisticallyDifferent[i]) {
            System.out.print(join + "\\textbf{" + df.format(averages[i]) + "}");
          } else {
            System.out.print(join + df.format(averages[i]));
          }
        }
        System.out.println(" \\\\ \\hline");
      }
      System.out.println("\\end{tabular}\n"
          + "\\end{table}");
      System.out.println("\n\n\n");
    }
  }
}
