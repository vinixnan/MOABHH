package br.usp.poli.pcs.lti.moabhh.statistical;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * The type File utils.
 */
public class FileUtils {

  /**
   * Read formatted string [ ].
   *
   * @param filename the filename
   * @return the string [ ]
   */
  public static String[] readFormatted(String filename) {
    String linesFromFile = "";
    try {
      linesFromFile = readFile(filename);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    linesFromFile = linesFromFile.trim().replaceAll("\n", " ");
    linesFromFile = linesFromFile.replaceAll("\\s+", ",");
    String[] valuesFromText = linesFromFile.split(",");
    return valuesFromText;
  }

  /**
   * Read file string.
   *
   * @param fileName the file name
   * @return the string
   * @throws IOException the io exception
   */
  public static String readFile(String fileName) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    try {
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();
      while (line != null) {
        sb.append(line);
        sb.append("\n");
        line = br.readLine();
      }
      return sb.toString();
    } finally {
      br.close();
    }
  }
}
