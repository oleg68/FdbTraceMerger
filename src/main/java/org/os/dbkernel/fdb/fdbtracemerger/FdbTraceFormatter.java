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

  public void process(Parameters prms, Iterator<TraceEntry> iter) throws IOException {
    final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(prms.getTimeZone());
    final String outputPath = prms.getOutputPath();
    final boolean isStdout = outputPath.equals(Parameters.OUTPUT_PATH_DEFAULT);
    final BufferedWriter w 
      = isStdout
	? new BufferedWriter(new PrintWriter(System.out))
	: Files.newBufferedWriter(Path.of(outputPath));
    
    try {
      TraceEntry e;
      
      while ((e = iter.next()) != null) {
	printEvent(f, w, e);
      }
    } finally {
      if (isStdout) {
	w.flush();
      } else {
	w.close();
      }
    }
  }
  
  void printEvent(final DateTimeFormatter f, final BufferedWriter w, final TraceEntry e) throws IOException {
    w.append(f.format(e.time));
    w.append(" ");
    w.append(e.source);
    for (TraceEntry.Attribute a : e.attributes) {
      w.append(" ");
      w.append(a.name);
      w.append("=\"");
      w.append(a.value);
      w.append("\"");
    }
    w.newLine();
  }
  
}
