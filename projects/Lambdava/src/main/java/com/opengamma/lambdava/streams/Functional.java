/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.lambdava.functions.Function1;
import com.opengamma.lambdava.functions.Function2;

/**
 * Lambdava λ-flavoured java.
 *
 * @param <S> the type of the iterable
 */
public interface Functional<S> extends Iterable<S> {


  /**
   * Returns Functional object holding sorted collection of elements from unsorted underlying one.
   *
   * @return Functional object holding sorted collection of elements using internal entries' {@link Comparable#compareTo(Object)} compareTo} method.
   */
  Functional<S> sort();

  Functional<S> sortBy(java.util.Comparator<? super S> comparator);

  <T> T reduce(final T acc, final Function2<T, S, T> reducer);

  S reduce(final Function2<S, S, S> reducer);

  <T> Map<T, Functional<S>> groupBy(final Function1<S, T> mapper);

  boolean any(final Function1<S, Boolean> predicate);

  boolean all(final Function1<S, Boolean> predicate);

  Functional<S> filter(final Function1<S, Boolean> predicate);

  <T> Functional<T> map(Function1<S, T> mapper);

  Functional<S> dorun(Function1<S, Void> executor);

  /**
   * Forces lazy stream to be realized
   * @return realized stream                             
   */
  Functional<S> doall();

  <T, M extends Function1<S, ? extends Iterable<T>>> Functional<T> flatMap(M mapper);

  boolean isEmpty();

  List<S> asList();

  Set<S> asSet();
  
  StreamI<S> asStream();

  S first();

  Functional<S> take(int count);

  S last();

  int count();


  /////   STREAM   /////////////


  S head();

  StreamI<S> rest();

  StreamI<S> append(Iterable<S> other);
  
  StreamI<S> append(StreamI<S> other);
  
  StreamI<S> interleave(StreamI<S> other);

  StreamI<S> append(S element);

  StreamI<S> reverse();

  StreamI<S> cons(S a);

  boolean contains(S t);

}


