package org.os.dbkernel.fdb.fdbtracemerger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Iterator;

public class FdbTraceFormatter {

  DateTimeFormatter timeFmt = null;
  boolean isStdout = false;
  BufferedWriter writer = null;
  
  public void setup(final Parameters prms) throws IOException {
    timeFmt = prms.getTimeFormatter();
    final String outputPath = prms.getOutputPath();
    isStdout = outputPath.equals(Parameters.OUTPUT_PATH_DEFAULT);
    writer 
      = isStdout
	? new BufferedWriter(new PrintWriter(System.out))
	: Files.newBufferedWriter(Path.of(outputPath));
  }

  public void process(Iterator<TraceEntry> iter) throws IOException {
    TraceEntry e;

    while ((e = iter.next()) != null) {
      printEvent(e);
    }
  }
  
  void printEvent(final TraceEntry e) throws IOException {
    writer.append(timeFmt.format(e.time));
    writer.append(" ").append(e.source);
    writer.append(" Severity=").append(Integer.toString(e.severity));
    writer.append(" Type=\"").append(e.type).append("\"");
    for (TraceEntry.Attribute a : e.attributes) {
      writer.append(" ").append(a.name).append("=\"").append(a.value).append("\"");
    }
    writer.newLine();
  }
  
  public void cleanup() {
    try {
      if (isStdout) {
	writer.flush();
      } else {
	writer.close();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    writer = null;
    isStdout = false;
    timeFmt = null;
  }
  
}
