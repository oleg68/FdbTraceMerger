package org.os.dbkernel.fdb.fdbtracemerger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Parameters extends AbstractParameters {
  public static final int QUE_CAPACITY_DEFAULT = 1000;
  public static final String OUTPUT_PATH_DEFAULT = "-";
  public static final DateTimeFormatter BASE_TIME_FORMATTER
    = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]");
  
  private final List<String> SRC_LIST_DEFAULT = Arrays.asList(new String[] {"."});
  
  public final ArrayList<String> srcList = new ArrayList<>();
  
  private class Parser extends AbstractParser {
    
    protected Parser(final Iterator<String> iter) {
      super(iter);
    }

    void consumeSwitch(final String sw) {
      if (sw.equals("o")) {
	outputPath = checkNext(outputPath, sw);
      } else if (sw.equals("from")) {
	timeFromStr = checkNext(timeFromStr, sw);;
      } else if (sw.equals("to")) {
	timeToStr = checkNext(timeToStr, sw);;
      } else if (sw.equals("help") || sw.equals("?")) {
	isToHelp = true;
      } else if (sw.equals("tz")) {
	if (timeZone != null) {
	  throw new IllegalArgumentException("Duplicated " + sw);
	}  
	if (! argsIter.hasNext()) {
	  throw new IllegalArgumentException("No value for " + sw);
	}
	timeZone = ZoneId.of(argsIter.next());
      } else {
	throw new IllegalArgumentException("Unknown switch " + sw);
      }
    }
    
    void consumeArg(final String arg) {
      srcList.add(arg);
    }
  }

  String outputPath = null;
  ZoneId timeZone = null;
  boolean isToHelp = false;
  private String timeFromStr = null;
  private String timeToStr = null;
  DateTimeFormatter timeFormatter = null;
  Instant timeFrom = null;
  Instant timeTo = null;
  
  @Override
  AbstractParser getParser(final Iterator<String> iter) {
    return new Parser(iter);
  }

  @Override
  void init() {
    srcList.clear();
    timeTo = null;
    timeFrom = null;
    timeFormatter = null;
    outputPath = null;
    timeToStr = null;
    timeFromStr = null;
    timeZone = null;
  }
  
  @Override
  void fillOther() {
    timeFormatter = BASE_TIME_FORMATTER.withZone(getTimeZone());
    timeFrom = timeFromStr != null ? timeFormatter.parse(timeFromStr, Instant::from) : null;
    timeTo = timeToStr != null ? timeFormatter.parse(timeToStr, Instant::from) : null;
  }
  
  public List<String> getSrcList() {
    return srcList.isEmpty() ? SRC_LIST_DEFAULT : srcList;
  }
  
  public String getOutputPath() {
    return outputPath != null ? outputPath : OUTPUT_PATH_DEFAULT;
  }
  
  public ZoneId getTimeZone() {
    return timeZone != null ? timeZone : ZoneId.systemDefault();
  }
  
  public DateTimeFormatter getTimeFormatter() {
    return timeFormatter;
  }
  
  static void printUsage() throws IOException {
    System.out.println(
"Merge several foundationdb trace files into a single file ordered by time\n" +
"Usage: java -jar fdb-trace-merger {options}... {files-or-directories}...\n" +
"\n" +
"Options are:\n" +
"-help              Print this information\n" +
"-from DateTime     Print only the log contents with the time greater or equal to the specified time.\n" +
"                   The time must be in the format \"yyyy-MM-dd HH:mm:ss[.SSSSSS]\" and includer in quotas.\n" +
"-to DateTime       Print only the log contents with the time less than the specified time.\n" +
"                   The time must be in the format \"yyyy-MM-dd HH:mm:ss[.SSSSSS]\" and includer in quotas.\n" +
"-o FileName	    Output to the file specified. \"-o -\" means the standard output (default)\n" +
"-tz TimeZoneName   Print the time with the specified timezone. \n" +
"                   see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones\n" +
"		    for the list of timezone names supported\n" +
"		    By default, uses the local timezone\n" +
"\n" +
"Files-or-directories - a list of pathes.\n" +
"  If the path represents a file, then FdbTraceMerger considers this file as a fdb trace.\n" +
"  If the path represents a directory, then FdbTraceMerger searches all trace files \n" +
"    in the directory and all its subdirectories recursively whith the pattern\n" +
"    \"trace.*.xml\".\n" +
"  Running without Files-or-directories specifued FdbTraceMerger searches all trace \n" +
"    files under the current directory\n" +
"\n" +
"Examples:\n" +
"\n" +
"java -jar fdb-trace-merger-3.52.29.10.jar\n" +
"  merge all trace files from the current directory and all its subdirectories recursively\n" +
"  and print list of events to the standard output\n" +
"\n" +
"java -jar fdb-trace-merger-3.52.29.10.jar -tz UTC -o events.log traces/trace.192.168.56.1??.*.xml\n" +
"  merge all trace files in the traces subdirectory from the hosts with ip addresses\n" +
"  trace.192.168.56.1?? and print list of events to events.log. All timestamps are \n" +
"  printed with the UTC timezone\n"
    );
  }
}
