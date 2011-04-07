/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.DoublesPair;

/**
 * Utilities to manipulate present value sensitivities.
 */
public class PresentValueSensitivityUtil {

  /**
   * Add two maps representing sensitivities into one.
   * @param curves List of curves.
   * @param sensi1 First sensitivity.
   * @param sensi2 Second sensitivity.
   * @return The total sensitivity.
   */
  public static Map<String, List<DoublesPair>> addSensitivity(final YieldCurveBundle curves, final Map<String, List<DoublesPair>> sensi1, final Map<String, List<DoublesPair>> sensi2) {
    Validate.notNull(curves, "curve bundle");
    Validate.notNull(sensi1, "sensitivity");
    Validate.notNull(sensi2, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : curves.getAllNames()) {
      final List<DoublesPair> temp = new ArrayList<DoublesPair>();
      if (sensi1.containsKey(name)) {
        for (final DoublesPair pair : sensi1.get(name)) {
          temp.add(pair);
        }
      }
      if (sensi2.containsKey(name)) {
        for (final DoublesPair pair : sensi2.get(name)) {
          final DoublesPair newPair = new DoublesPair(pair.getFirst(), pair.getSecond());
          temp.add(newPair);
        }
      }
      result.put(name, temp);
    }
    return result;
  }

  /**
   * Multiply a sensitivity by a common factor.
   * @param sensi The original sensitivity.
   * @param factor The multiplicative factor.
   * @return The multiplied sensitivity.
   */
  public static Map<String, List<DoublesPair>> multiplySensitivity(final Map<String, List<DoublesPair>> sensi, double factor) {
    Validate.notNull(sensi, "sensitivity");
    Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : sensi.keySet()) {
      final List<DoublesPair> curveSensi = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : sensi.get(name)) {
        curveSensi.add(new DoublesPair(pair.first, pair.second * factor));
      }
      result.put(name, curveSensi);
    }
    return result;
  }

}
