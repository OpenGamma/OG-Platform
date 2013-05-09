/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

import java.util.Arrays;
import java.util.Iterator;

import com.opengamma.lambdava.functions.Function0;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.lambdava.tuple.Pair;

public abstract class Stream {

  public static <T> StreamI<T> empty() {
    return EmptyStream.empty();
  }

  public static <T> StreamI<T> of(T head, StreamI<T> rest) {
    assert rest != null;
    return new FilledStream<T>(head, rest);
  }

  public static <T> StreamI<T> lazy(Thunk<T> thunk) {
    return new LazyStream<T>(thunk);
  }

  public static <T> StreamI<T> from(Iterable<T> iterable) {
    if (iterable == null) {
      return Stream.empty();
    } else if (iterable instanceof StreamI) {
      return ((StreamI<T>) iterable); // optimisation. we avoid creation of wrapper if the iterable is instance of stream already
    } else {
      return Stream.from(iterable.iterator());
    }
  }

  public static <T> StreamI<T> from(final Iterator<T> iterator) {
    if (iterator instanceof StreamI) {
      return ((StreamI<T>) iterator); // optimisation. we avoid creation of wrapper if the iterable is instance of stream already
    } else {
      StreamI<T> stream = Stream.empty();
      while (iterator.hasNext()) {
        T next = iterator.next();
        stream = stream.append(next);
      }
      return stream;
    }
  }

  static <T> Iterator<T> streamIterator(final StreamI<T> stream) {
    return new Iterator<T>() {

      StreamI<T> underlying = stream;

      @Override
      public boolean hasNext() {
        return !underlying.isEmpty();
      }

      @Override
      public T next() {
        if (underlying.isEmpty()) {
          throw new RuntimeException("there are no more elements to iterate over.");
        } else {
          T head = underlying.head();
          underlying = underlying.rest();
          return head;
        }
      }

      @Override
      public void remove() {
        throw new RuntimeException("This structure is immutable. So it is not supporting remove()");
      }
    };
  }

  /**
   * Takes a function of no args, presumably with side effects, and
   returns an infinite (or length n if supplied) lazy sequence of calls
   to it
   * @param f
   * @param count
   * @param <T>
   * @return
   */
  public static <T> StreamI<T> repeatedly(final Function0<T> f, final int count) {
    return new AbstractStream<T>() {
      @Override
      public T head() {
        if (!isEmpty()) {
          return f.execute();
        } else {
          return null;
        }
      }

      @Override
      public StreamI<T> rest() {
        if (count > 1 || count < 0) {
          return repeatedly(f, count - 1);
        } else {
          return Stream.empty();
        }
      }

      @Override
      public boolean isEmpty() {
        return count == 0;
      }

      @Override
      public int count() {
        return count;
      }
    };
  }

  public static <T> StreamI<T> repeatedly(final Function0<T> f) {
    return repeatedly(f, -1);
  }

  public static <T> StreamI<T> of(T head) {
    return new FilledStream<T>(head, EmptyStream.<T>empty());
  }

  public static <T> StreamI<T> of(T... objects) {
    return Stream.from(Arrays.asList(objects));
  }

  public static Object realizeAll(Object o) {
    if (o instanceof Pair) {
      Pair p = (Pair) o;
      return Pair.of(realizeAll(p._1()), realizeAll(p._2()));
    } else if (o instanceof StreamI) {
      StreamI s = (StreamI) o;
      return s.map(new Function1() {
        @Override
        public Object execute(Object o) {
          return realizeAll(o);
        }
      }).doall();
    } else {
      return o;
    }
  }

}
