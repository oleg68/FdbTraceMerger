package org.os.dbkernel.fdb.fdbtracemerger;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

public class FdbTraceParser implements Iterator<TraceEntry>, Closeable {
  
  private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

  private final Path tracePath;
  private final String source;

  private BufferedReader bufferedReader = null;
  private XMLEventReader xmlReader = null;
  private TraceEntry nextEvent = null;
  private boolean isNotFinished = true;

  public FdbTraceParser(final Path tracePath) {
    this.tracePath = tracePath;
    
    final String cs[] = tracePath.getFileName().toString().split("\\.");
    
    source = cs.length > 6 ? MessageFormat.format("{0}.{1}.{2}.{3}:{4}", cs[1], cs[2], cs[3], cs[4], cs[5]) : null;
  }
  
  private void assureNext() {
    if (isNotFinished && nextEvent == null) {
      try {
	if (bufferedReader == null) {
	  bufferedReader = Files.newBufferedReader(tracePath);
	}
	if (xmlReader == null) {
	  xmlReader = xmlInputFactory.createXMLEventReader(bufferedReader);
	}
	while (nextEvent == null && xmlReader.hasNext()) {
	  final XMLEvent event = xmlReader.nextEvent();

	  if (event.isStartElement()) {
	    final StartElement start = event.asStartElement();

	    if (start.getName().toString().equals("Event")) {
	      nextEvent = new TraceEntry();

	      final Iterator<Attribute> xai = start.getAttributes();

	      nextEvent.source = source;
	      while (xai.hasNext()) {
		final Attribute xa = xai.next();

		nextEvent.addAttribute(xa.getName().toString(), xa.getValue());
	      }
	      if (nextEvent.time == null) {
		nextEvent = null; // bad event
	      }
	    }
	  }
	}
	if (nextEvent == null) {
	  isNotFinished = false;
	}
      } catch (XMLStreamException | IOException ex ) {
	// maybe the xml file is not complete. It is an ordinar situation
	boolean isEof = false;
	
	if (bufferedReader != null) {
	  try {
	    isEof = bufferedReader.read() < 0;
	  } catch (IOException ex1) {
	    isEof = true;
	  }
	}
	
	if (ex instanceof XMLStreamException && isEof) {
	  // the xml file is not complete. It is an ordinar situation
	} else {
	  throw new RuntimeException(ex);
	}
      }
    }
  }
  
  @Override
  public boolean hasNext() {
    assureNext();
    return isNotFinished;
  }

  @Override
  public TraceEntry next() {
    assureNext();

    TraceEntry e = nextEvent;
    
    nextEvent = null;
    return e;
  }
  
  public void close() {
    if (xmlReader != null) {
      try {
	xmlReader.close();
      } catch (XMLStreamException ex) {
	ex.printStackTrace();
      }
      xmlReader = null;
    }
  }
  
}
