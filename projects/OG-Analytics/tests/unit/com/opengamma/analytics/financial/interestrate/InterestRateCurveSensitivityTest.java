/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class to test the PresentValueSensitivity class.
 */
public class InterestRateCurveSensitivityTest {

  private static final List<DoublesPair> SENSITIVITY_DATA_1 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 10), new DoublesPair(2, 20), new DoublesPair(3, 30),
      new DoublesPair(4, 40)});
  private static final List<DoublesPair> SENSITIVITY_DATA_2 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 40), new DoublesPair(2, 30), new DoublesPair(3, 20),
      new DoublesPair(4, 10)});
  private static final List<DoublesPair> SENSITIVITY_DATA_3 = Arrays.asList(new DoublesPair[] {new DoublesPair(11, 40), new DoublesPair(12, 30), new DoublesPair(13, 20),
      new DoublesPair(14, 10)});
  private static final String CURVE_NAME_1 = "A";
  private static final String CURVE_NAME_2 = "B";
  private static final String CURVE_NAME_3 = "C";
  private static final Map<String, List<DoublesPair>> SENSITIVITY_11 = new HashMap<String, List<DoublesPair>>();
  private static final Map<String, List<DoublesPair>> SENSITIVITY_12 = new HashMap<String, List<DoublesPair>>();
  private static final Map<String, List<DoublesPair>> SENSITIVITY_22 = new HashMap<String, List<DoublesPair>>();
  private static final Map<String, List<DoublesPair>> SENSITIVITY_33 = new HashMap<String, List<DoublesPair>>();

  static {
    SENSITIVITY_11.put(CURVE_NAME_1, SENSITIVITY_DATA_1);
    SENSITIVITY_22.put(CURVE_NAME_2, SENSITIVITY_DATA_2);
    SENSITIVITY_12.put(CURVE_NAME_1, SENSITIVITY_DATA_2);
    SENSITIVITY_33.put(CURVE_NAME_3, SENSITIVITY_DATA_3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSensitivity() {
    new InterestRateCurveSensitivity(null);
  }

  //TODO uncomment me
  //  @Test(expectedExceptions = IllegalArgumentException.class)
  //  public void testNullCurveName() {
  //    InterestRateCurveSensitivity.of(null, SENSITIVITY_DATA_1);
  //  }
  //
  //  @Test(expectedExceptions = IllegalArgumentException.class)
  //  public void testNullSensitivities() {
  //    InterestRateCurveSensitivity.of("Name", null);
  //  }
  //
  //  @Test(expectedExceptions = IllegalArgumentException.class)
  //  public void testNullData() {
  //    InterestRateCurveSensitivity.of(null);
  //  }
  //
  //  @Test(expectedExceptions = IllegalArgumentException.class)
  //  public void testPlusNullName() {
  //    new InterestRateCurveSensitivity().plus(null, SENSITIVITY_DATA_1);
  //  }
  //
  //  @Test(expectedExceptions = IllegalArgumentException.class)
  //  public void testPlusNullList() {
  //    new InterestRateCurveSensitivity().plus(CURVE_NAME_1, null);
  //  }
  //
  //  @Test(expectedExceptions = IllegalArgumentException.class)
  //  public void testPlusNullSensitivity() {
  //    new InterestRateCurveSensitivity().plus(null);
  //  }

  @Test
  public void testObject() {
    final Map<String, List<DoublesPair>> map = Maps.newHashMap(SENSITIVITY_11);
    InterestRateCurveSensitivity sensitivities = new InterestRateCurveSensitivity(map);
    map.put("DUMMY", SENSITIVITY_DATA_3);
    assertFalse(sensitivities.getSensitivities().equals(map));
    sensitivities = new InterestRateCurveSensitivity(SENSITIVITY_11);
    assertEquals(SENSITIVITY_11.keySet(), sensitivities.getCurves());
    assertEquals(SENSITIVITY_11, sensitivities.getSensitivities());
    InterestRateCurveSensitivity other = new InterestRateCurveSensitivity(SENSITIVITY_11);
    assertEquals(sensitivities.hashCode(), other.hashCode());
    assertEquals(sensitivities, other);
    other = InterestRateCurveSensitivity.from(CURVE_NAME_1, SENSITIVITY_DATA_1);
    assertEquals(sensitivities, other);
    assertFalse(SENSITIVITY_11 == new InterestRateCurveSensitivity(SENSITIVITY_11).getSensitivities());
    assertFalse(sensitivities.equals(new InterestRateCurveSensitivity(SENSITIVITY_12)));
    other = new InterestRateCurveSensitivity();
    assertTrue(other.getCurves().isEmpty());
    assertTrue(other.getSensitivities().isEmpty());
    //TODO uncomment me
    //    other = InterestRateCurveSensitivity.of();
    //    assertTrue(other.getCurves().isEmpty());
    //    assertTrue(other.getSensitivities().isEmpty());
    //    other = InterestRateCurveSensitivity.of();
    //    other = InterestRateCurveSensitivity.of(SENSITIVITY11);
    //    assertEquals(sensitivities, other);
  }

  @Test
  public void testPlusDifferentCurves() {
    // Simple add
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_22);
    final Map<String, List<DoublesPair>> map = new HashMap<String, List<DoublesPair>>();
    map.put(CURVE_NAME_1, SENSITIVITY_DATA_1);
    map.put(CURVE_NAME_2, SENSITIVITY_DATA_2);
    final InterestRateCurveSensitivity expected = new InterestRateCurveSensitivity(map);
    InterestRateCurveSensitivity actual = sensitivity1.plus(sensitivity2);
    assertFalse(sensitivity1 == actual);
    assertFalse(sensitivity2 == actual);
    assertEquals(expected, actual);
    actual = sensitivity1.plus(CURVE_NAME_2, SENSITIVITY_DATA_2);
    assertEquals(expected, actual);
  }

  @Test
  public void testPlusSameCurves() {
    // Add on the same curve    
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_12);
    final List<DoublesPair> data = new ArrayList<DoublesPair>();
    data.addAll(SENSITIVITY_DATA_1);
    data.addAll(SENSITIVITY_DATA_2);
    final Map<String, List<DoublesPair>> map = new HashMap<String, List<DoublesPair>>();
    map.put(CURVE_NAME_1, data);
    final InterestRateCurveSensitivity expected = new InterestRateCurveSensitivity(map);
    InterestRateCurveSensitivity actual = sensitivity1.plus(sensitivity2);
    assertFalse(sensitivity1 == actual);
    assertFalse(sensitivity2 == actual);
    assertEquals(expected, actual);
    actual = sensitivity1.plus(CURVE_NAME_1, SENSITIVITY_DATA_2);
    assertEquals(expected, actual);
  }

  @Test
  public void testPlusMultiCurve() {
    // Add multi-curve
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_12);
    final InterestRateCurveSensitivity sensitivity3 = new InterestRateCurveSensitivity(SENSITIVITY_22);
    final InterestRateCurveSensitivity sensitivity4 = new InterestRateCurveSensitivity(SENSITIVITY_33);
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.addAll(SENSITIVITY_DATA_1);
    list.addAll(SENSITIVITY_DATA_2);
    final Map<String, List<DoublesPair>> map = new HashMap<String, List<DoublesPair>>();
    map.put(CURVE_NAME_1, list);
    map.put(CURVE_NAME_2, SENSITIVITY_DATA_2);
    map.put(CURVE_NAME_3, SENSITIVITY_DATA_3);
    final InterestRateCurveSensitivity expected = new InterestRateCurveSensitivity(map);
    InterestRateCurveSensitivity actual = sensitivity1.plus(sensitivity2).plus(sensitivity3).plus(sensitivity4);
    assertEquals(expected, actual);
    actual = sensitivity1.plus(CURVE_NAME_1, SENSITIVITY_DATA_2).plus(CURVE_NAME_2, SENSITIVITY_DATA_2).plus(CURVE_NAME_3, SENSITIVITY_DATA_3);
    assertEquals(expected, actual);
  }

  @Test
  public void testMultiply() {
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_12);
    final InterestRateCurveSensitivity sensitivity3 = new InterestRateCurveSensitivity(SENSITIVITY_22);
    final InterestRateCurveSensitivity sensitivity4 = new InterestRateCurveSensitivity(SENSITIVITY_33);
    final double factor = Math.random() - 1;
    final List<DoublesPair> list1 = new ArrayList<DoublesPair>();
    final List<DoublesPair> list2 = new ArrayList<DoublesPair>();
    final List<DoublesPair> list3 = new ArrayList<DoublesPair>();
    for (final DoublesPair pair : SENSITIVITY_DATA_1) {
      list1.add(Pair.of(pair.first, pair.second * factor));
    }
    for (final DoublesPair pair : SENSITIVITY_DATA_2) {
      final DoublesPair scaledPair = Pair.of(pair.first, pair.second * factor);
      list1.add(scaledPair);
      list2.add(scaledPair);
    }
    for (final DoublesPair pair : SENSITIVITY_DATA_3) {
      list3.add(Pair.of(pair.first, pair.second * factor));
    }
    final Map<String, List<DoublesPair>> map = new HashMap<String, List<DoublesPair>>();
    map.put(CURVE_NAME_1, list1);
    map.put(CURVE_NAME_2, list2);
    map.put(CURVE_NAME_3, list3);
    final InterestRateCurveSensitivity expected = new InterestRateCurveSensitivity(map);
    final InterestRateCurveSensitivity actualUnscaled = sensitivity1.plus(sensitivity2).plus(sensitivity3).plus(sensitivity4);
    InterestRateCurveSensitivity actual = actualUnscaled.multiply(factor);
    assertFalse(actualUnscaled == actual);
    assertFalse(actualUnscaled.getSensitivities() == actual.getSensitivities());
    assertEquals(expected, actual);
    actual = sensitivity1.multiply(factor).plus(sensitivity2.multiply(factor)).plus(sensitivity3.multiply(factor)).plus(sensitivity4.multiply(factor));
    assertEquals(expected, actual);
  }

  @Test
  public void testCleanSameCurves() {
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_12);
    final List<DoublesPair> list = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 50), new DoublesPair(2, 50), new DoublesPair(3, 50), new DoublesPair(4, 50)});
    final Map<String, List<DoublesPair>> map = new HashMap<String, List<DoublesPair>>();
    map.put(CURVE_NAME_1, list);
    final InterestRateCurveSensitivity expected = new InterestRateCurveSensitivity(map);
    final InterestRateCurveSensitivity actualUncleaned = sensitivity1.plus(sensitivity2);
    final InterestRateCurveSensitivity actual = actualUncleaned.cleaned();
    assertFalse(actualUncleaned == actual);
    assertFalse(actualUncleaned.getSensitivities() == actual.getSensitivities());
    assertEquals(expected, actual);
  }

  @Test
  public void testCleanSameCurvesWithTolerance() {
    final double eps = 1e-3;
    final double eps2 = 2e-3;
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    //    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_12);
    //    final List<DoublesPair> list = Arrays.asList(new DoublesPair[] {new DoublesPair(1 + relativeTolerance, 50), new DoublesPair(2, 50), new DoublesPair(3, 50),
    //        new DoublesPair(4, 50)});
    //    final Map<String, List<DoublesPair>> map = new HashMap<String, List<DoublesPair>>();
    //    map.put(CURVE_NAME_1, list);
    //    final InterestRateCurveSensitivity expected = new InterestRateCurveSensitivity(map);
    //    final InterestRateCurveSensitivity actualUncleaned = sensitivity1.plus(sensitivity2);
    //    final InterestRateCurveSensitivity actual = actualUncleaned.cleaned();
    //    assertFalse(actualUncleaned == actual);
    //    assertFalse(actualUncleaned.getSensitivities() == actual.getSensitivities());
    //    assertEquals(expected, actual);
  }
}
