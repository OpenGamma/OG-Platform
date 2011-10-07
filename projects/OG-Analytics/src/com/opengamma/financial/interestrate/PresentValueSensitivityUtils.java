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
 * <p>
 * This is a thread-safe static utility class.
 */
public class PresentValueSensitivityUtils {

  /**
   * Restricted constructor.
   */
  protected PresentValueSensitivityUtils() {
    super();
  }

  /**
   * Add two list representing sensitivities into one. No attempt is made to net off sensitivities occurring at the same time
   * @param sensi1 First list of sensitivities 
   * @param sensi2 Second list of sensitivities
   * @return combined list 
   */
  public static List<DoublesPair> addSensitivity(final List<DoublesPair> sensi1, final List<DoublesPair> sensi2) {
    List<DoublesPair> temp = new ArrayList<DoublesPair>();
    for (final DoublesPair pair : sensi1) {
      temp.add(pair);
    }
    for (final DoublesPair pair : sensi2) {
      temp.add(pair);
    }
    return temp;
  }

  //-------------------------------------------------------------------------
  /**
   * Add two maps representing sensitivities into one.
   * 
   * @param sensi1  the first sensitivity, not null
   * @param sensi2  the second sensitivity, not null
   * @return the total sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> addSensitivity(final Map<String, List<DoublesPair>> sensi1, final Map<String, List<DoublesPair>> sensi2) {

    Validate.notNull(sensi1, "sensitivity");
    Validate.notNull(sensi2, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : sensi1.keySet()) {
      if (sensi2.containsKey(name)) {
        result.put(name, addSensitivity(sensi1.get(name), sensi2.get(name)));
      } else {
        result.put(name, sensi1.get(name));
      }
    }

    for (final String name : sensi2.keySet()) {
      if (!result.containsKey(name)) {
        result.put(name, sensi2.get(name));
      }
    }

    //      List<DoublesPair> temp = new ArrayList<DoublesPair>();
    //      if (sensi1.containsKey(name)) {
    //        for (final DoublesPair pair : sensi1.get(name)) {
    //          temp.add(pair);
    //        }
    //      }
    //      if (sensi2.containsKey(name)) {
    //        for (final DoublesPair pair : sensi2.get(name)) {
    //          final DoublesPair newPair = new DoublesPair(pair.getFirst(), pair.getSecond());
    //          temp.add(newPair);
    //        }
    //      }
    //      result.put(name, temp);
    //    }
    return result;
  }

  //TODO smarter way to do this?
  public static Map<String, List<DoublesPair>> addSensitivity(final Map<String, List<DoublesPair>> sensi1, final Map<String, List<DoublesPair>> sensi2,
      final Map<String, List<DoublesPair>> sensi3) {
    return addSensitivity(addSensitivity(sensi1, sensi2), sensi3);
  }

  /**
   * Multiply a sensitivity map by a common factor.
   * 
   * @param sensitivity  the original sensitivity, not null
   * @param factor  the multiplicative factor, not null
   * @return the multiplied sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> multiplySensitivity(final Map<String, List<DoublesPair>> sensitivity, double factor) {
    Validate.notNull(sensitivity, "sensitivity");
    Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : sensitivity.keySet()) {
      final List<DoublesPair> curveSensi = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : sensitivity.get(name)) {
        curveSensi.add(new DoublesPair(pair.first, pair.second * factor));
      }
      result.put(name, curveSensi);
    }
    return result;
  }

}
