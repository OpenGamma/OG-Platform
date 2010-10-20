/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.opengamma.math.ParallelArrayBinarySort;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.FirstThenSecondDoublesPairComparator;

/**
 * 
 */
public class DoublesCurveTestCase {
  protected static final String NAME1 = "a";
  protected static final String NAME2 = "b";
  protected static final double[] X_PRIMITIVE;
  protected static final double[] Y_PRIMITIVE;
  protected static final double[] X_PRIMITIVE_SORTED;
  protected static final double[] Y_PRIMITIVE_SORTED;
  protected static final Double[] X_OBJECT;
  protected static final Double[] Y_OBJECT;
  protected static final Double[] X_OBJECT_SORTED;
  protected static final Double[] Y_OBJECT_SORTED;
  protected static final Map<Double, Double> MAP;
  protected static final Map<Double, Double> MAP_SORTED;
  protected static final DoublesPair[] PAIR_ARRAY;
  protected static final DoublesPair[] PAIR_ARRAY_SORTED;
  protected static final Set<DoublesPair> PAIR_SET;
  protected static final Set<DoublesPair> PAIR_SET_SORTED;
  protected static final List<Double> X_LIST;
  protected static final List<Double> Y_LIST;
  protected static final List<Double> X_LIST_SORTED;
  protected static final List<Double> Y_LIST_SORTED;
  protected static final List<DoublesPair> PAIR_LIST;
  protected static final List<DoublesPair> PAIR_LIST_SORTED;

  static {
    final int n = 10;
    X_PRIMITIVE = new double[n];
    Y_PRIMITIVE = new double[n];
    X_OBJECT = new Double[n];
    Y_OBJECT = new Double[n];
    MAP = new HashMap<Double, Double>();
    PAIR_ARRAY = new DoublesPair[n];
    PAIR_SET = new HashSet<DoublesPair>();
    X_LIST = new ArrayList<Double>();
    Y_LIST = new ArrayList<Double>();
    X_LIST_SORTED = new ArrayList<Double>();
    Y_LIST_SORTED = new ArrayList<Double>();
    PAIR_LIST = new ArrayList<DoublesPair>();
    PAIR_LIST_SORTED = new ArrayList<DoublesPair>();
    double x, y;
    for (int i = 0; i < 5; i++) {
      x = 2 * i;
      y = 3 * x;
      X_PRIMITIVE[i] = x;
      Y_PRIMITIVE[i] = y;
      X_OBJECT[i] = x;
      Y_OBJECT[i] = y;
      MAP.put(x, y);
      PAIR_ARRAY[i] = DoublesPair.of(x, y);
      PAIR_SET.add(DoublesPair.of(x, y));
      X_LIST.add(x);
      Y_LIST.add(y);
      PAIR_LIST.add(DoublesPair.of(x, y));
    }
    for (int i = 5, j = 0; i < 10; i++, j++) {
      x = 2 * j + 1;
      y = 3 * x;
      X_PRIMITIVE[i] = x;
      Y_PRIMITIVE[i] = y;
      X_OBJECT[i] = x;
      Y_OBJECT[i] = y;
      MAP.put(x, y);
      PAIR_ARRAY[i] = DoublesPair.of(x, y);
      PAIR_SET.add(DoublesPair.of(x, y));
      X_LIST.add(x);
      Y_LIST.add(y);
      PAIR_LIST.add(DoublesPair.of(x, y));
    }
    X_PRIMITIVE_SORTED = Arrays.copyOf(X_PRIMITIVE, n);
    Y_PRIMITIVE_SORTED = Arrays.copyOf(Y_PRIMITIVE, n);
    ParallelArrayBinarySort.parallelBinarySort(X_PRIMITIVE_SORTED, Y_PRIMITIVE_SORTED);
    X_OBJECT_SORTED = Arrays.copyOf(X_OBJECT, n);
    Y_OBJECT_SORTED = Arrays.copyOf(Y_OBJECT, n);
    ParallelArrayBinarySort.parallelBinarySort(X_OBJECT_SORTED, Y_OBJECT_SORTED);
    MAP_SORTED = new TreeMap<Double, Double>(MAP);
    PAIR_SET_SORTED = new TreeSet<DoublesPair>(FirstThenSecondDoublesPairComparator.INSTANCE);
    PAIR_SET_SORTED.addAll(PAIR_SET);
    PAIR_ARRAY_SORTED = PAIR_SET_SORTED.toArray(new DoublesPair[0]);
    for (int i = 0; i < n; i++) {
      x = X_PRIMITIVE_SORTED[i];
      y = Y_PRIMITIVE_SORTED[i];
      X_LIST_SORTED.add(x);
      Y_LIST_SORTED.add(y);
      PAIR_LIST_SORTED.add(DoublesPair.of(x, y));
    }
  }
}
