/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.FirstThenSecondDoublesPairComparator;

/**
 * Utilities to manipulate present value sensitivities.
 * <p>
 * This is a thread-safe static utility class.
 */
public class InterestRateCurveSensitivityUtils {

  /**
   * Restricted constructor.
   */
  protected InterestRateCurveSensitivityUtils() {
    super();
  }

  /**
   * Takes a list of curve sensitivities (i.e. an unordered list of pairs of times and sensitivities) and returns a list order by ascending
   * time, and with sensitivities that occur at the same time netted (zero net sensitivities are removed)
   * @param old An unordered list of pairs of times and sensitivities
   * @param relTol Relative tolerance - if the net divided by gross sensitivity is less than this it is ignored/removed
   * @param absTol Absolute tolerance  - is the net sensitivity is less than this it is ignored/removed
   * @return A time ordered netted list
   */
  static final List<DoublesPair> clean(final List<DoublesPair> old, final double relTol, final double absTol) {

    Validate.notNull(old, "null list");
    Validate.isTrue(relTol >= 0.0 && absTol >= 0.0);
    if (old.size() == 0) {
      return new ArrayList<DoublesPair>();
    }
    final List<DoublesPair> res = new ArrayList<DoublesPair>();
    final DoublesPair[] sort = old.toArray(new DoublesPair[] {});
    Arrays.sort(sort, FirstThenSecondDoublesPairComparator.INSTANCE);
    final DoublesPair pairOld = sort[0];
    double tOld = pairOld.first;
    double sum = pairOld.getSecond();
    double scale = Math.abs(sum);
    double t = tOld;
    for (int i = 1; i < sort.length; i++) {
      final DoublesPair pair = sort[i];
      t = pair.first;
      if (t > tOld) {
        if (Math.abs(sum) > absTol && Math.abs(sum) / scale > relTol) {
          res.add(new DoublesPair(tOld, sum));
        }
        tOld = t;
        sum = pair.getSecondDouble();
        scale = Math.abs(sum);
      } else {
        sum += pair.getSecondDouble();
        scale += Math.abs(pair.getSecondDouble());
      }
    }

    if (Math.abs(sum) > absTol && Math.abs(sum) / scale > relTol) {
      res.add(new DoublesPair(t, sum));
    }

    return res;
  }

  /**
   * Takes a map of curve sensitivities (i.e. a map between curve names and a unordered lists of pairs of times and sensitivities)
   *  and returns a similar map where the lists order by ascending time, and with sensitivities that occur at the same time netted
   *  (zero net sensitivities are removed)
   * @param old A map between curve names and unordered lists of pairs of times and sensitivities
   * @param relTol Relative tolerance - if the net divided by gross sensitivity is less than this it is ignored/removed
   * @param absTol Absolute tolerance  - is the net sensitivity is less than this it is ignored/removed
   * @return A map between curve names and time ordered netted lists
   */
  public static Map<String, List<DoublesPair>> clean(final Map<String, List<DoublesPair>> old, final double relTol, final double absTol) {
    final Map<String, List<DoublesPair>> res = new HashMap<String, List<DoublesPair>>();
    final Set<Entry<String, List<DoublesPair>>> sense = old.entrySet();
    final Iterator<Entry<String, List<DoublesPair>>> interator = sense.iterator();
    while (interator.hasNext()) {
      final Entry<String, List<DoublesPair>> entry = interator.next();
      res.put(entry.getKey(), clean(entry.getValue(), relTol, absTol));
    }
    return res;
  }

  /**
   * Add two list representing sensitivities into one. No attempt is made to net off sensitivities occurring at the same time - Use clean()
   * to do this
   * @param sensi1 First list of sensitivities
   * @param sensi2 Second list of sensitivities
   * @return combined list
   */
  public static List<DoublesPair> addSensitivity(final List<DoublesPair> sensi1, final List<DoublesPair> sensi2) {
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
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

    return result;
  }

  /**
   * Add the list representing the sensitivity to one curve to the map of sensitivities to several curves.
   * @param sensi The multi-curves sensitivity. Not null.
   * @param curveName  The name of the curve the sensitivity of which is added. Not null.
   * @param list The sensitivity as a list. Not null.
   * @return The total sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> addSensitivity(final Map<String, List<DoublesPair>> sensi, final String curveName, final List<DoublesPair> list) {
    Validate.notNull(sensi, "sensitivity");
    Validate.notNull(list, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : sensi.keySet()) {
      if (name.equals(curveName)) {
        result.put(name, addSensitivity(sensi.get(name), list));
      } else {
        result.put(name, sensi.get(name));
      }
    }
    if (!result.containsKey(curveName)) {
      result.put(curveName, list);
    }
    return result;
  }

  //TODO smarter way to do this?
  public static Map<String, List<DoublesPair>> addSensitivity(final Map<String, List<DoublesPair>> sensi1, final Map<String, List<DoublesPair>> sensi2, final Map<String, List<DoublesPair>> sensi3) {
    return addSensitivity(addSensitivity(sensi1, sensi2), sensi3);
  }

  /**
   * Multiply a sensitivity map by a common factor.
   * 
   * @param sensitivity  the original sensitivity, not null
   * @param factor  the multiplicative factor, not null
   * @return the multiplied sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> multiplySensitivity(final Map<String, List<DoublesPair>> sensitivity, final double factor) {
    Validate.notNull(sensitivity, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : sensitivity.keySet()) {
      result.put(name, multiplySensitivity(sensitivity.get(name), factor));
    }
    return result;
  }

  public static List<DoublesPair> multiplySensitivity(final List<DoublesPair> sensitivity, final double factor) {
    Validate.notNull(sensitivity, "sensitivity");

    final List<DoublesPair> curveSensi = new ArrayList<DoublesPair>();
    for (final DoublesPair pair : sensitivity) {
      curveSensi.add(new DoublesPair(pair.first, pair.second * factor));
    }
    return curveSensi;
  }

  /**
   * Compare two lists of sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * @param sensi1 The first sensitivity (as a list).
   * @param sensi2 The second sensitivity (as a list).
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not.
   */
  public static boolean compare(final List<DoublesPair> sensi1, final List<DoublesPair> sensi2, final double tolerance) {
    for (int looptime = 0; looptime < sensi1.size(); looptime++) {
      if ((Math.abs(sensi1.get(looptime).first - sensi2.get(looptime).first) > tolerance) || (Math.abs(sensi1.get(looptime).second - sensi2.get(looptime).second) >= tolerance)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compare two maps of sensitivities with a given tolerance. The tolerance is used for both the time and the value. The two sensitivities are suppose to be in the same time order.
   * @param sensi1 The first sensitivity (as a map).
   * @param sensi2 The second sensitivity (as a map).
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the curves are not the same it returns False.
   */
  public static boolean compare(final Map<String, List<DoublesPair>> sensi1, final Map<String, List<DoublesPair>> sensi2, final double tolerance) {
    Validate.notNull(sensi1, "sensitivity");
    Validate.notNull(sensi2, "sensitivity");
    for (final String name : sensi1.keySet()) {
      if (sensi2.containsKey(name)) {
        if (!compare(sensi1.get(name), sensi2.get(name), tolerance)) {
          return false;
        }
      } else {
        return false;
      }
    }
    for (final String name : sensi2.keySet()) {
      if (!(sensi1.containsKey(name))) {
        return false;
      }
    }
    return true;
  }
}
