package br.usp.poli.pcs.lti.moabhh.core;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This customized Log4j appender will seperate the log messages based on their
 * LEVELS and will write them' into separate files. For example, all DEBUG
 * messages will go to a file and all INFO messages will go to a different file.
 *
 * @author Veera Sundar | http://veerasundar.com
 */
public class LogLevelFilterFileAppender extends FileAppender {

  private final static String DOT = ".";
  private final static String HIPHEN = "-";
  private static final String ORIG_LOG_FILE_NAME = "OrginalLogFileName";

  public LogLevelFilterFileAppender() {

  }

  public LogLevelFilterFileAppender(Layout layout, String fileName,
      boolean append, boolean bufferedIO, int bufferSize)
      throws IOException {
    super(layout, fileName, append, bufferedIO, bufferSize);
  }

  public LogLevelFilterFileAppender(Layout layout, String fileName,
      boolean append) throws IOException {
    super(layout, fileName, append);
  }

  public LogLevelFilterFileAppender(Layout layout, String fileName)
      throws IOException {
    super(layout, fileName);
  }

  @Override
  public void activateOptions() {
    MDC.put(ORIG_LOG_FILE_NAME, fileName);
    super.activateOptions();
  }

  @Override
  public void append(LoggingEvent event) {
    try {
      setFile(appendLevelToFileName((String) MDC.get(ORIG_LOG_FILE_NAME),
          event.getLevel().toString()), fileAppend, bufferedIO,
          bufferSize);
    } catch (IOException ie) {
      errorHandler
          .error(
              "Error occured while setting file for the log level "
                  + event.getLevel(), ie,
              ErrorCode.FILE_OPEN_FAILURE);
    }
    super.append(event);
  }

  private String appendLevelToFileName(String oldLogFileName, String level) {
    if (oldLogFileName != null) {
      final File logFile = new File(oldLogFileName);
      String newFileName = "";
      final String fn = logFile.getName();
      final int dotIndex = fn.indexOf(DOT);
      if (dotIndex != -1) {
        // the file name has an extension. so, insert the level
        // between the file name and the extension
        newFileName = fn.substring(0, dotIndex) + HIPHEN + level + DOT
            + fn.substring(dotIndex + 1);
      } else {
        // the file name has no extension. So, just append the level
        // at the end.
        newFileName = fn + HIPHEN + level;
      }
      return logFile.getParent() + File.separator + newFileName;
    }
    return null;
  }
}