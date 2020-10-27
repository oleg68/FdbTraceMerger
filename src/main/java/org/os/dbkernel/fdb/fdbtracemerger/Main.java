package org.os.dbkernel.fdb.fdbtracemerger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class Main {

  final Parameters prms = new Parameters();
  final FdbTraceFormatter fmtr = new FdbTraceFormatter();

  public static void main(String[] args) {
    try {
      int rc = new Main(args).run();
      
      System.exit(rc);
    } catch (Throwable th) {
      th.printStackTrace();
      System.exit(1);
    }
  }

  Main(String[] args) {
    prms.parseArgs(args);
  }
  
  int run() throws Exception {
    int rc = 0;
    
    if (prms.isToHelp) {
      Parameters.printUsage();
    } else {
      final ArrayList<FdbTraceParser> parsers = new ArrayList<>();

      for (String srcName: prms.getSrcList()) {
	final Path p = Paths.get(srcName);

	if (Files.isDirectory(p)) {
	  try (DirectoryStream<Path> filesStream = Files.newDirectoryStream(Paths.get(srcName), "trace.*.xml")) {
	    for (Path entry: filesStream) {
	      parsers.add(new FdbTraceParser(entry));
	    }
	  }
	} else {
	  parsers.add(new FdbTraceParser(p));
	}
      }

      final int nTraces = parsers.size();
      
      if (nTraces > 0) {
	final MergeSortedIterator<TraceEntry> resIter 
	  = new MergeSortedIterator(parsers.toArray(new FdbTraceParser[parsers.size()]));

	fmtr.process(prms, resIter);
      } else {
	System.err.println("No tracefiles have been found");
	rc = 1;
      }
    }
    return rc;
  }

  /*  
  TraceEntry fetchNext(final List<PeekableBlockingQueue<TraceEntry>> queues) throws InterruptedException {
    TraceEntry el = null;
    PeekableBlockingQueue<TraceEntry> qFrom = null;
    
    for (PeekableBlockingQueue<TraceEntry> q: queues) {
      final TraceEntry e = q.peek();
      
      if (!e.isFinal()) {
	if (el == null || e.compareTo(el) < 0) {
	  el = e;
	  qFrom =q;
	}
      }
    }
    if (qFrom != null) {
      qFrom.remove();
    }
    return el;
  }
  */
}
