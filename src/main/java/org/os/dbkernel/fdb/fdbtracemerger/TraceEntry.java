package org.os.dbkernel.fdb.fdbtracemerger;

import java.util.ArrayList;
import java.time.Instant;

public class TraceEntry implements Comparable<TraceEntry> {
  
  public final ArrayList<Attribute> attributes = new ArrayList<>();

  public static class Attribute {
    public final String name;
    public final String value;
    
    private Attribute(final String name, final String value) {
      this.name = name;
      this.value = value;
    }
  }

  public Instant time;
  public String source;
  public int severity;
  public String type;

  void addAttribute(final String name, final String value) {
    final Attribute a = new Attribute(name, value);
    
    if (name.equals("Time")) {
      final String[] tss = value.split("\\.");
      time = Instant.ofEpochSecond(Long.parseLong(tss[0]), Long.parseLong(tss[1]) * 1000);
    } else if (name.equals("Severity")) {
      severity = Integer.parseInt(value);
    } else if (name.equals("Type")) {
      type = value;
    } else {
      attributes.add(a);
    }
  }

  public int compareTo(final TraceEntry o) {
    return time.compareTo(o.time);
  }
}
