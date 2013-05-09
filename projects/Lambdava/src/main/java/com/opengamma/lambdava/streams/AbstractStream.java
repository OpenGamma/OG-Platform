/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.lambdava.functions.Function1;
import com.opengamma.lambdava.functions.Function2;
import com.opengamma.lambdava.tuple.Pair;

abstract class AbstractStream<S> implements StreamI<S> {
  @Override
  public Iterator<S> iterator() {
    return Stream.streamIterator(this);
  }


  static class LazyTake<T> extends LazyStream<T> {
    LazyTake(final int count, final StreamI<T> underlying) {
      super(new Thunk<T>() {

        @Override
        public StreamI<T> execute() {
          if(count > 0)
            if (underlying.isEmpty())
              return underlying;
            else {
              if (count == 1) {
                return Stream.of(underlying.head());
              } else {
                return Stream.of(underlying.head(), new LazyTake<T>(count - 1, underlying.rest()));              
              }
            }
          else{
            return Stream.empty();
          }
        }
      });
    }
  }

  @Override
  public StreamI<S> take(int count) {
    if (this.isEmpty() || count <= 0)
      return Stream.empty();
    else
      return new LazyTake<S>(count, this);
  }

  //TODO make it lazy
  private static <S> StreamI<S> reverse(StreamI<S> in, StreamI<S> acc) {
    if (in.isEmpty())
      return acc;
    else
      return reverse(in.rest(), acc.cons(in.head()));
  }

  //TODO make it lazy
  @Override
  public StreamI<S> reverse() {
    return reverse(this, EmptyStream.<S>empty());
  }

  @Override
  public StreamI<S> append(Iterable<S> other) {
    if (other instanceof StreamI)
      return append((StreamI<S>) other);
    else
      return append(Stream.from(other));
  }

  final public StreamI<S> append(StreamI<S> other) {
    return new AppendingStream<S>(this, other);
//    if (!this.isEmpty() && !other.isEmpty())
//      return new AppendingStream<S>(this, other);
//    else if (this.isEmpty() && !other.isEmpty()) {
//      return other;
//    } else {
//      return this;
//    }
  }

  final public StreamI<S> interleave(StreamI<S> other) {
    return new InterleavingStream<S>(this, other);
  }

  final public StreamI<S> append(S element) {
    if (this.isEmpty()) {
      return Stream.of(element);
    } else {
      return new AppendingStream<S>(this, Stream.of(element));
    }
  }

  @Override
  final public StreamI<S> cons(S t) {
    return Stream.of(t, this);
  }

  @Override
  final public boolean contains(final S t) {
    if (isEmpty())
      return false;
    else
      return (head().equals(t) || rest().contains(t));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Iterable)) return false;

    Iterable that = (Iterable) o;

    Object h = head();
    StreamI<S> r = rest();

    Iterator it = that.iterator();
    while (it.hasNext()) {
      Object next = it.next();
      if (!h.equals(next))
        return false;
      if ((it.hasNext() && r.isEmpty()) || (!r.isEmpty() && !it.hasNext()))
        return false;
      h = rest().head();
      r = rest().rest();
    }
    return true;
  }


  @Override
  public int hashCode() {
    int result = head().hashCode();
    result = 31 * result + rest().hashCode();
    return result;
  }

  @Override
  public String toString() {
    StreamI s = this;
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    while (!s.isEmpty()) {


      sb.append(s.head());

      if (!s.rest().isEmpty()) {
        sb.append(", ");
      }
      s = s.rest();
    }
    sb.append(']');
    return sb.toString();
  }

  static String spaces(int count) {
    StringBuilder ss = new StringBuilder();
    for (int i = 0; i < count; i++) {
      ss.append(' ');
    }
    return ss.toString();
  }

  static String toPPString(StreamI s, int tab) {

    String tabSpace = spaces(tab);

    StringBuilder sb = new StringBuilder();
    sb.append('[');
    while (!s.isEmpty()) {

      sb.append('\n');
      sb.append(tabSpace);
      if (s.head() instanceof StreamI) {
        sb.append(toPPString((StreamI) s.head(), tab + 4));
      } else {
        sb.append(s.head());
      }
      if (!s.rest().isEmpty()) {
        sb.append(", ");
      }
      s = s.rest();
    }
    sb.append('\n');
    sb.append(spaces(tab - 4));
    sb.append(']');
    return sb.toString();
  }

  @Override
  public boolean all(Function1<S, Boolean> predicate) {
    return (new FunctionalStream<S>(this)).all(predicate);
  }

  @Override
  public boolean any(Function1<S, Boolean> predicate) {
    return (new FunctionalStream<S>(this)).any(predicate);
  }

  @Override
  public List<S> asList() {
    return (new FunctionalStream<S>(this)).asList();
  }

  @Override
  public Set<S> asSet() {
    return (new FunctionalStream<S>(this)).asSet();
  }

  @Override
  public StreamI<S> asStream() {
    return this;
  }

  @Override
  public Functional<S> dorun(Function1<S, Void> executor) {
    return (new FunctionalStream<S>(this)).dorun(executor);
  }

  @Override
  public Functional<S> doall() {
    StreamI<S> s = Stream.empty();
    for (S e : this) {
      s = s.append(e);
    }
    return s;
  }

  @Override
  public Functional<S> filter(Function1<S, Boolean> predicate) {
    return (new FunctionalStream<S>(this)).filter(predicate);
  }

  @Override
  public S first() {
    return (new FunctionalStream<S>(this)).first();
  }

  @Override
  public <T, M extends Function1<S, ? extends Iterable<T>>> Functional<T> flatMap(M mapper) {
    return (new FunctionalStream<S>(this)).flatMap(mapper);
  }

  @Override
  public <T> Map<T, Functional<S>> groupBy(Function1<S, T> mapper) {
    return (new FunctionalStream<S>(this)).groupBy(mapper);
  }

  @Override
  public S last() {
    return (new FunctionalStream<S>(this)).last();
  }

  @Override
  public <T> Functional<T> map(Function1<S, T> mapper) {
    return (new FunctionalStream<S>(this)).map(mapper);
  }

  @Override
  public <T> T reduce(T acc, Function2<T, S, T> reducer) {
    return (new FunctionalStream<S>(this)).reduce(acc, reducer);
  }

  @Override
  public S reduce(Function2<S, S, S> reducer) {
    return (new FunctionalStream<S>(this)).reduce(reducer);
  }

  @Override
  public Functional<S> sort() {
    return (new FunctionalStream<S>(this)).sort();
  }

  @Override
  public Functional<S> sortBy(Comparator<? super S> comparator) {
    return (new FunctionalStream<S>(this)).sortBy(comparator);
  }
}
