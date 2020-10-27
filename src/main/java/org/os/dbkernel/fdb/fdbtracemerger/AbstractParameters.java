package org.os.dbkernel.fdb.fdbtracemerger;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Iterator;

public abstract class AbstractParameters {

  protected abstract class AbstractParser {
    protected final Iterator<String> argsIter;
    
    protected AbstractParser(final Iterator<String> iter) {
      this.argsIter = iter;
    }
    
    private final void parse() {
      while (argsIter.hasNext()) {
	final String arg = argsIter.next();

	if (arg.startsWith("-")) {
	  consumeSwitch(arg.substring(1));
	} else {
	  consumeArg(arg);
	}
      }
    }

    abstract void consumeSwitch(final String sw);
    
    abstract void consumeArg(final String arg);

    protected String checkNext(final Object oldV, final String argName) {
      if (oldV != null) {
	throw new IllegalArgumentException("Duplicated switch " + argName);
      }
      if (! argsIter.hasNext()) {
	throw new IllegalArgumentException("No value of the " + argName + " switch");
      }
      return argsIter.next();
    }
    
    protected int checkNext(final int oldV, final String argName) {
      if (oldV > 0) {
	throw new IllegalArgumentException("Duplicated switch " + argName);
      }
      if (! argsIter.hasNext()) {
	throw new IllegalArgumentException("No value of the " + argName + " switch");
      }
      
      final int newV = Integer.valueOf(argsIter.next());
      
      if (newV <= 0) {
	throw new IllegalArgumentException("Invalid value of " + argName + " switch: " + newV);
      }
      return newV;
    }
  }

  public final void parseArgs(final String[] args) {
    parse(Arrays.asList(args).iterator());
  }
  
  public final void parse(final Iterator<String> iter) {
    init();
    getParser(iter).parse();
    fillOther();
  }
  
  abstract AbstractParser getParser(final Iterator<String> iter);
  
  abstract void init();
  
  void fillOther() {}
}
