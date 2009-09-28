/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

/**
 * 
 * 
 * @author emcleod
 */
public class FirstThenSecondPairComparatorTest {

  @Test
  public void test() {
    final Pair<Double, Double> first = new Pair<Double, Double>(1., 3.);
    final Pair<Double, Double> second = new Pair<Double, Double>(1., 5.);
    final Pair<Double, Double> third = new Pair<Double, Double>(1., 6.);
    final Pair<Double, Double> fourth = new Pair<Double, Double>(2., 1.);
    final List<Pair<Double, Double>> list = new ArrayList<Pair<Double, Double>>();
    list.add(first);
    list.add(second);
    list.add(third);
    list.add(fourth);
    final Set<Pair<Double, Double>> sorted = new TreeSet<Pair<Double, Double>>(new FirstThenSecondPairComparator<Double, Double>());
    sorted.add(first);
    sorted.add(second);
    sorted.add(third);
    sorted.add(fourth);
    int count = 0;
    for (final Pair<Double, Double> pair : sorted) {
      assertEquals(pair, list.get(count));
      count++;
    }
  }
}
