/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import com.opengamma.util.PublicAPI;

/**
 * Static utilities for {@code Pair}.
 * <p>
 * These are focused around creating the right type of pair.
 * <p>
 * Note that when using methods on this class, you do not know what
 * type of pair is created, and the type may change in the future.
 * Since {@code Pair} declares {@code equals} this should not be a problem.
 */
@PublicAPI
public final class Pairs {
  // it is important that all return types are Pair, and not more specific
  // this avoids issues when adding additional overrides in the future
  // some methods exist to avoid as many ambiguous method errors as possible
  // note there are no methods from A to primitive as they cause problems

  //-------------------------------------------------------------------------
  /**
   * Creates a pair of null {@code Object}s inferring the types.
   * 
   * @param <A> the first element type
   * @param <B> the second element type
   * @return a pair of two nulls, not null
   */
  public static <A, B> Pair<A, B> ofNulls() {
    return ObjectsPair.of(null, null);
  }

  /**
   * Creates a pair of {@code Object}s optimizing the storage type.
   * 
   * @param <A> the first element type
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  @SuppressWarnings("unchecked")
  public static <A, B> Pair<A, B> ofOptimized(A first, B second) {
    if (second instanceof Double) {
      if (first instanceof Double) {
        return (Pair<A, B>) DoublesPair.of(((Double) first).doubleValue(), ((Double) second).doubleValue());
      } else if (first instanceof Integer) {
        return (Pair<A, B>) IntDoublePair.of(((Integer) first).intValue(), ((Double) second).doubleValue());
      } else if (first instanceof Long) {
        return (Pair<A, B>) LongDoublePair.of(((Long) first).longValue(), ((Double) second).doubleValue());
      }
    } else if (first instanceof Integer) {
      return (Pair<A, B>) IntObjectPair.<B>of(((Integer) first).intValue(), second);
    } else if (first instanceof Long) {
      return (Pair<A, B>) LongObjectPair.<B>of(((Long) first).longValue(), second);
    }
    return ObjectsPair.of(first, second);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a pair of {@code Object}s inferring the types.
   * 
   * @param <A> the first element type
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <A, B> Pair<A, B> of(A first, B second) {
    return ObjectsPair.of(first, second);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a pair of {@code Double}s.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Double, Double> of(Double first, Double second) {
    if (first != null && second != null) {
      return DoublesPair.of(first.doubleValue(), second.doubleValue());
    }
    return ObjectsPair.of(first, (Double) second);
  }

  /**
   * Creates a pair of {@code Double}s.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Double, Double> of(Double first, double second) {
    if (first != null) {
      return DoublesPair.of(first.doubleValue(), second);
    }
    return ObjectsPair.of(first, (Double) second);
  }

  /**
   * Creates a pair of {@code Double}s.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Double, Double> of(double first, Double second) {
    if (second != null) {
      return DoublesPair.of(first, second.doubleValue());
    }
    return ObjectsPair.of((Double) first, second);
  }

  /**
   * Creates a pair of {@code Double}s.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Double, Double> of(double first, double second) {
    return DoublesPair.of(first, second);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a pair of {@code Integer} to {@code Object}.
   * 
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <B> Pair<Integer, B> of(Integer first, B second) {
    if (first != null) {
      return IntObjectPair.of(first.intValue(), second);
    }
    return ObjectsPair.of(first, second);
  }

  /**
   * Creates a pair of {@code Integer} to {@code Double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Integer, Double> of(Integer first, Double second) {
    if (first != null && second != null) {
      return IntDoublePair.of(first.intValue(), second.doubleValue());
    }
    return ObjectsPair.of(first, second);
  }

  /**
   * Creates a pair of {@code Integer} to {@code Double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Integer, Double> of(Integer first, double second) {
    if (first != null) {
      return IntDoublePair.of(first.intValue(), second);
    }
    return ObjectsPair.of(first, (Double) second);
  }

  /**
   * Creates a pair of {@code Integer} to {@code Integer}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Integer, Integer> of(Integer first, int second) {
    return ObjectsPair.of(first, (Integer) second);
  }

  /**
   * Creates a pair of {@code Integer} to {@code Long}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Integer, Long> of(Integer first, long second) {
    return ObjectsPair.of(first, (Long) second);
  }

  /**
   * Creates a pair of {@code Integer} to {@code Boolean}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Integer, Boolean> of(Integer first, boolean second) {
    return ObjectsPair.of(first, (Boolean) second);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a pair of {@code Integer} to {@code Object}.
   * 
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <B> Pair<Integer, B> of(int first, B second) {
    return IntObjectPair.of(first, second);
  }

  /**
   * Creates a pair of {@code Integer} to {@code Double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Integer, Double> of(int first, Double second) {
    if (second != null) {
      return IntDoublePair.of(first, second.doubleValue());
    }
    return ObjectsPair.of((Integer) first, second);
  }

  /**
   * Creates a pair of {@code Integer} to {@code Double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Integer, Double> of(int first, double second) {
    return IntDoublePair.of(first, second);
  }

  /**
   * Creates a pair of {@code Integer} to {@code Integer}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Integer, Integer> of(int first, int second) {
    return ObjectsPair.of((Integer) first, (Integer) second);
  }

  /**
   * Creates a pair of {@code Integer} to {@code Long}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Integer, Long> of(int first, long second) {
    return ObjectsPair.of((Integer) first, (Long) second);
  }

  /**
   * Creates a pair of {@code Integer} to {@code Boolean}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Integer, Boolean> of(int first, boolean second) {
    return ObjectsPair.of((Integer) first, (Boolean) second);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a pair of {@code Long} to {@code Object}.
   * 
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <B> Pair<Long, B> of(Long first, B second) {
    if (first != null) {
      return LongObjectPair.of(first.longValue(), second);
    }
    return ObjectsPair.of(first, second);
  }

  /**
   * Creates a pair of {@code Long} to {@code Double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Long, Double> of(Long first, Double second) {
    if (first != null && second != null) {
      return LongDoublePair.of(first.longValue(), second.doubleValue());
    }
    return ObjectsPair.of(first, second);
  }

  /**
   * Creates a pair of {@code Long} to {@code Double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Long, Double> of(Long first, double second) {
    if (first != null) {
      return LongDoublePair.of(first.longValue(), second);
    }
    return ObjectsPair.of(first, (Double) second);
  }

  /**
   * Creates a pair of {@code Long} to {@code Integer}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Long, Integer> of(Long first, int second) {
    return ObjectsPair.of(first, (Integer) second);
  }

  /**
   * Creates a pair of {@code Long} to {@code Long}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Long, Long> of(Long first, long second) {
    return ObjectsPair.of(first, (Long) second);
  }

  /**
   * Creates a pair of {@code Long} to {@code Boolean}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Long, Boolean> of(Long first, boolean second) {
    return ObjectsPair.of(first, (Boolean) second);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a pair of {@code Long} to {@code Object}.
   * 
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <B> Pair<Long, B> of(long first, B second) {
    return LongObjectPair.of(first, second);
  }

  /**
   * Creates a pair of {@code Long} to {@code Double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Long, Double> of(long first, Double second) {
    if (second != null) {
      return LongDoublePair.of(first, second.doubleValue());
    }
    return ObjectsPair.of((Long) first, second);
  }

  /**
   * Creates a pair of {@code Long} to {@code Double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Long, Double> of(long first, double second) {
    return LongDoublePair.of(first, second);
  }

  /**
   * Creates a pair of {@code Long} to {@code Integer}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Long, Integer> of(long first, int second) {
    return ObjectsPair.of((Long) first, (Integer) second);
  }

  /**
   * Creates a pair of {@code Long} to {@code Long}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Long, Long> of(long first, long second) {
    return ObjectsPair.of((Long) first, (Long) second);
  }

  /**
   * Creates a pair of {@code Long} to {@code Boolean}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Long, Boolean> of(long first, boolean second) {
    return ObjectsPair.of((Long) first, (Boolean) second);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a pair of {@code Boolean} to {@code Object}.
   * 
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <B> Pair<Boolean, B> of(Boolean first, B second) {
    return ObjectsPair.of(first, second);
  }

  /**
   * Creates a pair of {@code Boolean} to {@code Double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Boolean, Double> of(Boolean first, Double second) {
    return ObjectsPair.of(first, second);
  }

  /**
   * Creates a pair of {@code Boolean} to {@code Double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Boolean, Double> of(Boolean first, double second) {
    return ObjectsPair.of(first, (Double) second);
  }

  /**
   * Creates a pair of {@code Boolean} to {@code Integer}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Boolean, Integer> of(Boolean first, int second) {
    return ObjectsPair.of(first, (Integer) second);
  }

  /**
   * Creates a pair of {@code Boolean} to {@code Long}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Boolean, Long> of(Boolean first, long second) {
    return ObjectsPair.of(first, (Long) second);
  }

  /**
   * Creates a pair of {@code Boolean} to {@code Boolean}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Boolean, Boolean> of(Boolean first, boolean second) {
    return ObjectsPair.of(first, (Boolean) second);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a pair of {@code Boolean} to {@code Object}.
   * 
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <B> Pair<Boolean, B> of(boolean first, B second) {
    return ObjectsPair.of((Boolean) first, second);
  }

  /**
   * Creates a pair of {@code Boolean} to {@code Double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Boolean, Double> of(boolean first, Double second) {
    return ObjectsPair.of((Boolean) first, second);
  }

  /**
   * Creates a pair of {@code Boolean} to {@code Double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Boolean, Double> of(boolean first, double second) {
    return ObjectsPair.of((Boolean) first, (Double) second);
  }

  /**
   * Creates a pair of {@code Boolean} to {@code Integer}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Boolean, Integer> of(boolean first, int second) {
    return ObjectsPair.of((Boolean) first, (Integer) second);
  }

  /**
   * Creates a pair of {@code Boolean} to {@code Long}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Boolean, Long> of(boolean first, long second) {
    return ObjectsPair.of((Boolean) first, (Long) second);
  }

  /**
   * Creates a pair of {@code Boolean} to {@code Boolean}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static Pair<Boolean, Boolean> of(boolean first, boolean second) {
    return ObjectsPair.of((Boolean) first, (Boolean) second);
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor.
   */
  private Pairs() {
  }

}
