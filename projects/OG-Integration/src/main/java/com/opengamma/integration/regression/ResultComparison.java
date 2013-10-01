/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.Map;

import com.opengamma.util.tuple.Pair;

/**
 *
 */
public final class ResultComparison {

  // TODO handlers for every structured data type so I can dive in and compare individual values?

  private ResultComparison() {
  }



  // TODO different deltas for different columns?
  // TODO return type needs 3 fields
  // in 1 but not 2 - map<key, value>
  // in 2 but not 1 - map<key, value>
  // in both but different value - map<key, pair<value, value>>
  public Map<CalculationResultKey, Pair<Object, Object>> compare(CalculationResults results1,
                                                                 CalculationResults results2,
                                                                 double delta) {
    throw new UnsupportedOperationException();
  }

}


