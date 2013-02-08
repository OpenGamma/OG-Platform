/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;


import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.opengamma.lambdava.functions.Function1;
import com.opengamma.lambdava.functions.Function2;

/**
 * Implementation of functional using streams as underlying collections
 *
 * @param <S> the type of the iterable
 */
class FunctionalStream<S> extends Lambdava<S> implements StreamI<S> {

  private StreamI<S> _collection;

  FunctionalStream(final Iterable<S> i) {
    _collection = Stream.from(i);
  }

  public static <T> Lambdava<T> functional(final Iterable<T> i) {
    if (i instanceof FunctionalStream)
      return (FunctionalStream<T>) i;
    else
      return new FunctionalStream<T>(i);
  }

  @Override
  public FunctionalStream<S> sort() {
    return null; //TODO implement me!!!
  }

  @Override
  public FunctionalStream<S> sortBy(Comparator<? super S> comparator) {
    return null; //TODO implement me!!!
  }

  @Override
  public <T> T reduce(T acc, Function2<T, S, T> reducer) {
    StreamI<S> stream = _collection;
    T result = acc;
    while (!stream.isEmpty()) {
      S head = stream.head();
      result = reducer.execute(result, head);
      stream = stream.rest();
    }
    return result;
  }

  public S reduce(final Function2<S, S, S> reducer) {
    StreamI<S> stream = _collection;
    if (stream.isEmpty()) {
      return null;
    } else {
      S acc = stream.head();
      return new FunctionalStream<S>(stream.rest()).reduce(acc, reducer);
    }
  }

  private <T> Map<T, StreamI<S>> _groupBy(final Function1<S, T> mapper) {
    Map<T, StreamI<S>> grouping = new HashMap<T, StreamI<S>>();
    return reduce(grouping, new Function2<Map<T, StreamI<S>>, S, Map<T, StreamI<S>>>() {
      @Override
      public Map<T, StreamI<S>> execute(Map<T, StreamI<S>> acc, S s) {
        T key = mapper.execute(s);
        if (!acc.containsKey(key)) {
          acc.put(key, Stream.<S>empty());
        }
        StreamI<S> values = acc.get(key);
        values.cons(s);
        return acc;
      }
    });
  }

  public <T> Map<T, Functional<S>> groupBy(final Function1<S, T> mapper) {
    Map<T, StreamI<S>> m = _groupBy(mapper);
    Map<T, Functional<S>> grouping = new HashMap<T, Functional<S>>();
    for (T t : m.keySet()) {
      grouping.put(
        t,
        new FunctionalStream<S>(m.get(t))
      );
    }
    return grouping;
  }

  public boolean any(final Function1<S, Boolean> predicate) {
    boolean any = false;
    StreamI<S> stream = _collection;
    while (!any && !stream.isEmpty()) {
      any = predicate.execute(stream.head());
      stream = stream.rest();
    }
    return any;
  }

  public boolean all(final Function1<S, Boolean> predicate) {

    boolean all = true;
    StreamI<S> stream = _collection;
    while (all && !stream.isEmpty()) {
      all = predicate.execute(stream.head());
      stream = stream.rest();
    }
    return all;
  }

  private StreamI<S> _filter(final Function1<S, Boolean> predicate) {
    return reduce(Stream.<S>empty(), new Function2<StreamI<S>, S, StreamI<S>>() {
      @Override
      public StreamI<S> execute(StreamI<S> acc, S e) {
        if (predicate.execute(e)) {
          return acc.append(e);
        } else {
          return acc;
        }
      }
    });
  }

  public Functional<S> filter(final Function1<S, Boolean> predicate) {
    return new FunctionalStream<S>(_filter(predicate));
  }

  static class LazyMappingStream<X, Y> extends LazyStream<Y> {

    LazyMappingStream(final StreamI<X> underlying, final Function1<X, Y> mapper) {
      super(new Thunk<Y>() {

        @Override
        public StreamI<Y> execute() {
          if (underlying.isEmpty())
            return Stream.empty();
          else {
            Y newHead = mapper.execute(underlying.head());
            return Stream.of(
              newHead,
              new LazyMappingStream<X, Y>(underlying.rest(), mapper)
            );
          }
        }
      });
    }
  }

  public <T> Functional<T> map(final Function1<S, T> mapper) {
    if (this.isEmpty())
      return Stream.empty();
    else
      return new LazyMappingStream<S, T>(_collection, mapper);
  }

//  static public <T> StreamI<T> concat2(final Functional<Functional<T>> fs) {
//    if (fs.isEmpty())
//      return Stream.empty();
//    else {
//      return fs.head().append(Stream.lazy(new Thunk<T>() {
//        @Override
//        public StreamI<T> execute() {
//          return concat(fs.rest());
//        }
//      }));
//    }
//  }


  static private <T> StreamI<T> cat(final Functional<T> xys, final StreamI<Functional<T>> zs) {
    return Stream.lazy(new Thunk<T>() {
      @Override
      public StreamI<T> execute() {
        if (!xys.isEmpty()) {
          // (cons (first xys) (cat (rest xys) zs)))
          return Stream.of(xys.head(), cat(xys.rest(), zs));
        } else {
          if (!zs.isEmpty()) {
            // (when zs (cat (first zs) (next zs)))
            return cat(zs.head(), zs.rest());
          } else {
            return Stream.empty();
          }
        }
      }
    });
  }


  static class StreamConcatenator<T> extends AbstractStream<T> {

    StreamI<T> _head;
    private StreamI<StreamI<T>> _streams;

    boolean _empty;

    StreamConcatenator(StreamI<StreamI<T>> streams) {
      _streams = streams;
      _head = streams.head();
      while (!_streams.isEmpty() && _head != null && _head.isEmpty()) {
        _streams = _streams.rest();
        _head = _streams.head();
      }
      _empty = _streams.isEmpty();
    }

    @Override
    public int count() {
      if (_empty)
        return 0;
      else
        return 0;  //TODO implement it !!!!!!
    }

    @Override
    public String toString() {
      if (_empty)
        return "[]";
      else
        return "StreamConcatenator{" +
          "_head=" + _head +
          ", _streams=" + _streams +
          '}';
    }

    @Override
    public boolean isEmpty() {
      return _empty;
    }

    @Override
    public T head() {
      if (_streams.isEmpty())
        return null;
      else
        return _head.head();
    }

    @Override
    public StreamI<T> rest() {
      if (_empty)
        return Stream.empty();
      else
        return new StreamConcatenator<T>(_streams.rest().cons(_head.rest()));
    }
  }

  static public <T> StreamI<T> concat(final StreamI<StreamI<T>> fs) {
    return new StreamConcatenator<T>(fs);
  }


  @Override
  public <T, M extends Function1<S, ? extends Iterable<T>>> Functional<T> flatMap(final M mapper) {
    StreamI<StreamI<T>> map = map(new Function1<S, StreamI<T>>() {
      @Override
      public StreamI<T> execute(S s) {
        return new FunctionalStream<T>(mapper.execute(s));
      }
    }).asStream();
    return concat(map);
  }

  @Override
  public Functional<S> dorun(Function1<S, Void> executor) {
    for (S s : _collection) {
      executor.execute(s);
    }
    return this;
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
  protected Iterable<S> underlying() {
    return _collection;
  }

  public StreamI<S> append(StreamI<S> other) {
    return _collection.append(other);
  }

  @Override
  public StreamI<S> interleave(StreamI<S> other) {
    return _collection.interleave(other);
  }

  public StreamI<S> append(S element) {
    return _collection.append(element);
  }

  public StreamI<S> cons(S a) {
    return _collection.cons(a);
  }

  public boolean contains(S s) {
    return _collection.contains(s);
  }

  public static <T> StreamI<T> empty() {
    return Stream.empty();
  }

  @Override
  public S head() {
    return _collection.head();
  }

  @Override
  public boolean isEmpty() {
    return _collection.isEmpty();
  }

  @Override
  public StreamI<S> rest() {
    return _collection.rest();
  }

  @Override
  public StreamI<S> reverse() {
    return _collection.reverse();
  }

  @Override
  public Iterator<S> iterator() {
    return _collection.iterator();
  }

  @Override
  public S first() {
    return _collection.head();
  }

  @Override
  public S last() {
    return _collection.reverse().head();
  }

  @Override
  public int count() {
    return _collection.count();
  }

  @Override
  public Lambdava<S> take(int count) {
    return functional(_collection.take(count));
  }

  @Override
  public StreamI<S> append(Iterable<S> other) {
    return new FunctionalStream<S>(_collection.append(other));
  }
}


