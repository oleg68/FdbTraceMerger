package org.os.dbkernel.fdb.fdbtracemerger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.PriorityQueue;

public class MergeSortedIterator<E> implements Iterator<E>, Closeable {
  
  private final Iterator<E>[] iters;
  private final int nIters;
  private final PriorityQueue<ElementEntry<E>> q;
  
  private static class ElementEntry<E> implements Comparable<ElementEntry<E>> {
    private E element;
    private int iterN;
    
    public int compareTo(final ElementEntry<E> o) {
      final int ec = ((Comparable<E>) element).compareTo(o.element);
      return ec != 0 ? ec : Integer.compare(iterN, o.iterN);
    }
  }
  
  boolean hasInited = false;
  
  public MergeSortedIterator (Iterator<E>[] iters) {
    this.iters = iters;
    this.nIters = iters.length;
    this.q = new PriorityQueue<>(this.nIters);
  }
  
  public boolean hasNext() {
    assureInited();
    
    return ! q.isEmpty();
  }
  
  public E next() {
    assureInited();
    
    E e = null;
    final ElementEntry<E> ee = q.poll();

    if (ee != null) {
      fetchFrom(ee.iterN);
      e = ee.element;
    }
    return e;
  }
  
  private void assureInited() {
    if (! hasInited) {
      for (int i = 0; i < nIters; i ++) {
	fetchFrom(i);
      }
      hasInited = true;
    }
  }
  
  private void fetchFrom(final int iterN) {
    final E e = iters[iterN].next();
    
    if (e != null) {
      final ElementEntry<E> ee = new ElementEntry<>();
      
      ee.element = e;
      ee.iterN = iterN;
      q.add(ee);
    }
  }
  
  public void close() {
    q.clear();
    hasInited = false;
    for (Iterator<E> iter: iters) {
      if (iter instanceof Closeable) {
	try {
	  ((Closeable) iter).close();
	} catch (IOException ex) {
	  ex.printStackTrace();
	}
      }
    }
  }
}
