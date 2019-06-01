package br.usp.poli.pcs.lti.moabhh.statistical;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The type Statistical test.
 */
public class StatisticalTest {

  private ArrayList<HashMap<String, Boolean>> allComparision;
  private ArrayList<ArrayList<Double>> allAverages;
  private ArrayList<String> metrics;
  private String problemName;

  /**
   * Instantiates a new Statistical test.
   */
  public StatisticalTest() {
    allComparision = new ArrayList<>();
    allAverages = new ArrayList<>();
    metrics = new ArrayList<>();
    this.problemName = "";
  }

  /**
   * Gets all comparision.
   *
   * @return the all comparision
   */
  public ArrayList<HashMap<String, Boolean>> getAllComparision() {
    return allComparision;
  }

  /**
   * Sets all comparision.
   *
   * @param allComparision the all comparision
   */
  public void setAllComparision(ArrayList<HashMap<String, Boolean>> allComparision) {
    this.allComparision = allComparision;
  }

  /**
   * Gets all averages.
   *
   * @return the all averages
   */
  public ArrayList<ArrayList<Double>> getAllAverages() {
    return allAverages;
  }

  /**
   * Sets all averages.
   *
   * @param allAverages the all averages
   */
  public void setAllAverages(ArrayList<ArrayList<Double>> allAverages) {
    this.allAverages = allAverages;
  }

  /**
   * Gets metrics.
   *
   * @return the metrics
   */
  public ArrayList<String> getMetrics() {
    return metrics;
  }

  /**
   * Sets metrics.
   *
   * @param metrics the metrics
   */
  public void setMetrics(ArrayList<String> metrics) {
    this.metrics = metrics;
  }

  /**
   * Gets problem name.
   *
   * @return the problem name
   */
  public String getProblemName() {
    return problemName;
  }

  /**
   * Sets problem name.
   *
   * @param problemName the problem name
   */
  public void setProblemName(String problemName) {
    this.problemName = problemName;
  }
}
