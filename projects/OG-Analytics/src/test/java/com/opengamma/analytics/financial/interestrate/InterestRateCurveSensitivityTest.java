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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to test the PresentValueSensitivity class.
 *
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateCurveSensitivityTest {

  private static final List<DoublesPair> SENSITIVITY_DATA_1 = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, 10d), DoublesPair.of(2d, 20d), DoublesPair.of(3d, 30d), DoublesPair.of(4d, 40d)});
  private static final List<DoublesPair> SENSITIVITY_DATA_2 = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, 40d), DoublesPair.of(2d, 30d), DoublesPair.of(3d, 20d), DoublesPair.of(4d, 10d)});
  private static final List<DoublesPair> SENSITIVITY_DATA_3 = Arrays.asList(new DoublesPair[] {DoublesPair.of(11d, 40d), DoublesPair.of(12d, 30d), DoublesPair.of(13d, 20d), DoublesPair.of(14d, 10d)});
  private static final String CURVE_NAME_1 = "A";
  private static final String CURVE_NAME_2 = "B";
  private static final String CURVE_NAME_3 = "C";
  private static final Map<String, List<DoublesPair>> SENSITIVITY_11 = new HashMap<>();
  private static final Map<String, List<DoublesPair>> SENSITIVITY_12 = new HashMap<>();
  private static final Map<String, List<DoublesPair>> SENSITIVITY_22 = new HashMap<>();
  private static final Map<String, List<DoublesPair>> SENSITIVITY_33 = new HashMap<>();
  private static final double EPS = 1e-12;

  static {
    SENSITIVITY_11.put(CURVE_NAME_1, SENSITIVITY_DATA_1);
    SENSITIVITY_22.put(CURVE_NAME_2, SENSITIVITY_DATA_2);
    SENSITIVITY_12.put(CURVE_NAME_1, SENSITIVITY_DATA_2);
    SENSITIVITY_33.put(CURVE_NAME_3, SENSITIVITY_DATA_3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSensitivity1() {
    new InterestRateCurveSensitivity(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSensitivity2() {
    InterestRateCurveSensitivity.of(CURVE_NAME_1, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveName() {
    InterestRateCurveSensitivity.of(null, SENSITIVITY_DATA_1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSensitivities() {
    InterestRateCurveSensitivity.of("Name", null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullName() {
    new InterestRateCurveSensitivity().plus(null, SENSITIVITY_DATA_1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullList() {
    new InterestRateCurveSensitivity().plus(CURVE_NAME_1, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullSensitivity() {
    new InterestRateCurveSensitivity().plus(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCleanNegativeAbsoluteTolerance() {
    new InterestRateCurveSensitivity(SENSITIVITY_11).cleaned(EPS, -EPS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCleanNegativeRelativeTolerance() {
    new InterestRateCurveSensitivity(SENSITIVITY_11).cleaned(-EPS, EPS);
  }

  // FIXME
  //  @Test(expectedExceptions = IllegalArgumentException.class)
  //  public void testCompareNull1() {
  //    InterestRateCurveSensitivity.compare(null, new InterestRateCurveSensitivity(), EPS);
  //  }
  //
  //  @Test(expectedExceptions = IllegalArgumentException.class)
  //  public void testCompareNull2() {
  //    InterestRateCurveSensitivity.compare(new InterestRateCurveSensitivity(), null, EPS);
  //  }
  //
  //  @Test(expectedExceptions = IllegalArgumentException.class)
  //  public void testCompareNegativeTolerance() {
  //    InterestRateCurveSensitivity.compare(new InterestRateCurveSensitivity(), new InterestRateCurveSensitivity(), -EPS);
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
    other = InterestRateCurveSensitivity.of(CURVE_NAME_1, SENSITIVITY_DATA_1);
    assertEquals(sensitivities, other);
    assertFalse(SENSITIVITY_11 == new InterestRateCurveSensitivity(SENSITIVITY_11).getSensitivities());
    assertFalse(sensitivities.equals(new InterestRateCurveSensitivity(SENSITIVITY_12)));
    other = new InterestRateCurveSensitivity();
    assertTrue(other.getCurves().isEmpty());
    assertTrue(other.getSensitivities().isEmpty());
    other = new InterestRateCurveSensitivity(SENSITIVITY_11);
    assertEquals(sensitivities, other);
  }

  @Test
  public void testPlusDifferentCurves() {
    // Simple add
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_22);
    final Map<String, List<DoublesPair>> map = new HashMap<>();
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
    final List<DoublesPair> data = new ArrayList<>();
    data.addAll(SENSITIVITY_DATA_1);
    data.addAll(SENSITIVITY_DATA_2);
    final Map<String, List<DoublesPair>> map = new HashMap<>();
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
    final List<DoublesPair> list = new ArrayList<>();
    list.addAll(SENSITIVITY_DATA_1);
    list.addAll(SENSITIVITY_DATA_2);
    final Map<String, List<DoublesPair>> map = new HashMap<>();
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
    final List<DoublesPair> list1 = new ArrayList<>();
    final List<DoublesPair> list2 = new ArrayList<>();
    final List<DoublesPair> list3 = new ArrayList<>();
    for (final DoublesPair pair : SENSITIVITY_DATA_1) {
      list1.add(DoublesPair.of(pair.first, pair.second * factor));
    }
    for (final DoublesPair pair : SENSITIVITY_DATA_2) {
      final DoublesPair scaledPair = DoublesPair.of(pair.first, pair.second * factor);
      list1.add(scaledPair);
      list2.add(scaledPair);
    }
    for (final DoublesPair pair : SENSITIVITY_DATA_3) {
      list3.add(DoublesPair.of(pair.first, pair.second * factor));
    }
    final Map<String, List<DoublesPair>> map = new HashMap<>();
    map.put(CURVE_NAME_1, list1);
    map.put(CURVE_NAME_2, list2);
    map.put(CURVE_NAME_3, list3);
    final InterestRateCurveSensitivity expected = new InterestRateCurveSensitivity(map);
    final InterestRateCurveSensitivity actualUnscaled = sensitivity1.plus(sensitivity2).plus(sensitivity3).plus(sensitivity4);
    InterestRateCurveSensitivity actual = actualUnscaled.multipliedBy(factor);
    assertFalse(actualUnscaled == actual);
    assertFalse(actualUnscaled.getSensitivities() == actual.getSensitivities());
    assertEquals(expected, actual);
    actual = sensitivity1.multipliedBy(factor).plus(sensitivity2.multipliedBy(factor)).plus(sensitivity3.multipliedBy(factor)).plus(sensitivity4.multipliedBy(factor));
    assertEquals(expected, actual);
  }

  @Test
  public void testCleanSameCurves() {
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_12);
    final List<DoublesPair> list = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, 50d), DoublesPair.of(2d, 50d), DoublesPair.of(3d, 50d), DoublesPair.of(4d, 50d)});
    final Map<String, List<DoublesPair>> map = new HashMap<>();
    map.put(CURVE_NAME_1, list);
    final InterestRateCurveSensitivity expected = new InterestRateCurveSensitivity(map);
    final InterestRateCurveSensitivity actualUncleaned = sensitivity1.plus(sensitivity2);
    final InterestRateCurveSensitivity actual = actualUncleaned.cleaned();
    assertFalse(actualUncleaned == actual);
    assertFalse(actualUncleaned.getSensitivities() == actual.getSensitivities());
    assertEquals(expected, actual);
  }

  @Test
  public void testCleanDifferentCurves() {
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_22);
    final Map<String, List<DoublesPair>> map = new HashMap<>();
    map.put(CURVE_NAME_1, SENSITIVITY_DATA_1);
    map.put(CURVE_NAME_2, SENSITIVITY_DATA_2);
    final InterestRateCurveSensitivity expected = new InterestRateCurveSensitivity(map);
    final InterestRateCurveSensitivity actualUncleaned = sensitivity1.plus(sensitivity2);
    final InterestRateCurveSensitivity actual = actualUncleaned.cleaned();
    assertFalse(actualUncleaned == actual);
    assertFalse(actualUncleaned.getSensitivities() == actual.getSensitivities());
    assertEquals(expected, actual);
    assertEquals(actualUncleaned, actual);
  }

  @Test
  public void testCleanSameCurvesWithAbsoluteTolerance1() {
    final double eps = 1e-3;
    final double eps2 = 5e-4;
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    final List<DoublesPair> fuzzyList = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, -10 - eps2), DoublesPair.of(2d, 30d), DoublesPair.of(3d, -30d + eps2), DoublesPair.of(4d, 10d)});
    List<DoublesPair> expectedList = Arrays.asList(new DoublesPair[] {DoublesPair.of(2d, 50d), DoublesPair.of(4d, 50d)});
    Map<String, List<DoublesPair>> expectedMap = new HashMap<>();
    expectedMap.put(CURVE_NAME_1, expectedList);
    final Map<String, List<DoublesPair>> fuzzyMap = new HashMap<>();
    fuzzyMap.put(CURVE_NAME_1, fuzzyList);
    final InterestRateCurveSensitivity fuzzySensitivity = new InterestRateCurveSensitivity(fuzzyMap);
    InterestRateCurveSensitivity expected = new InterestRateCurveSensitivity(expectedMap);
    final InterestRateCurveSensitivity actualUncleaned = sensitivity1.plus(fuzzySensitivity);
    InterestRateCurveSensitivity actual = actualUncleaned.cleaned(eps / 10, eps);
    assertFalse(actualUncleaned == actual);
    assertFalse(actualUncleaned.getSensitivities() == actual.getSensitivities());
    assertEquals(expected, actual);
    expectedList = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, -eps2), DoublesPair.of(2d, 50d), DoublesPair.of(3d, eps2), DoublesPair.of(4d, 50d)});
    expectedMap = new HashMap<>();
    expectedMap.put(CURVE_NAME_1, expectedList);
    expected = new InterestRateCurveSensitivity(expectedMap);
    actual = actualUncleaned.cleaned(eps / 1000, eps / 100);
    assertEquals(expected.getSensitivities().size(), actual.getSensitivities().size());
    assertIRCSEquals(expected, actual);
  }

  @Test
  public void testCleanSameCurvesWithRelativeTolerance1() {
    final double eps = 1e-3;
    final double eps2 = 5e-4;
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    final List<DoublesPair> fuzzyList = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, -10d - 10 * eps2), DoublesPair.of(2d, 30d), DoublesPair.of(3d, -30d + 30 * eps2), DoublesPair.of(4d, 10d)});
    List<DoublesPair> expectedList = Arrays.asList(new DoublesPair[] {DoublesPair.of(2d, 50d), DoublesPair.of(4d, 50d)});
    Map<String, List<DoublesPair>> expectedMap = new HashMap<>();
    expectedMap.put(CURVE_NAME_1, expectedList);
    final Map<String, List<DoublesPair>> fuzzyMap = new HashMap<>();
    fuzzyMap.put(CURVE_NAME_1, fuzzyList);
    final InterestRateCurveSensitivity fuzzySensitivity = new InterestRateCurveSensitivity(fuzzyMap);
    InterestRateCurveSensitivity expected = new InterestRateCurveSensitivity(expectedMap);
    final InterestRateCurveSensitivity actualUncleaned = sensitivity1.plus(fuzzySensitivity);
    InterestRateCurveSensitivity actual = actualUncleaned.cleaned(eps, eps / 10);
    assertFalse(actualUncleaned == actual);
    assertFalse(actualUncleaned.getSensitivities() == actual.getSensitivities());
    assertEquals(expected, actual);
    expectedList = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, -10d * eps2), DoublesPair.of(2d, 50d), DoublesPair.of(3d, 30d * eps2), DoublesPair.of(4d, 50d)});
    expectedMap = new HashMap<>();
    expectedMap.put(CURVE_NAME_1, expectedList);
    expected = new InterestRateCurveSensitivity(expectedMap);
    actual = actualUncleaned.cleaned(eps / 100, eps / 10);
    assertEquals(expected.getSensitivities().size(), actual.getSensitivities().size());
    assertIRCSEquals(expected, actual);
  }

  @Test
  public void testTotalSensitivityByCurve() {
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_12);
    final InterestRateCurveSensitivity sensitivity3 = new InterestRateCurveSensitivity(SENSITIVITY_22);
    final InterestRateCurveSensitivity sensitivity4 = new InterestRateCurveSensitivity(SENSITIVITY_33);
    final Map<String, Double> actual = sensitivity1.plus(sensitivity2).plus(sensitivity3).plus(sensitivity4).totalSensitivityByCurve();
    final Map<String, Double> expected = new HashMap<>();
    expected.put(CURVE_NAME_1, 200.);
    expected.put(CURVE_NAME_2, 100.);
    expected.put(CURVE_NAME_3, 100.);
    assertEquals(expected, actual);
  }

  @Test
  public void testTotalSensitivity() {
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(SENSITIVITY_11);
    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_12);
    final InterestRateCurveSensitivity sensitivity3 = new InterestRateCurveSensitivity(SENSITIVITY_22);
    final InterestRateCurveSensitivity sensitivity4 = new InterestRateCurveSensitivity(SENSITIVITY_33);
    final double actual = sensitivity1.plus(sensitivity2).plus(sensitivity3).plus(sensitivity4).totalSensitivity();
    final double expected = 400;
    assertEquals(expected, actual);
  }

  @Test
  public void testCompareDifferentTimes() {
    AssertSensitivityObjects.assertEquals("", new InterestRateCurveSensitivity(), new InterestRateCurveSensitivity(), EPS);
    final TreeMap<String, List<DoublesPair>> sortedMap = new TreeMap<>(SENSITIVITY_11);
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(sortedMap);
    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_12);
    AssertSensitivityObjects.assertDoesNotEqual("", sensitivity1, sensitivity2, EPS);
    final Map<String, List<DoublesPair>> map = Maps.newTreeMap();
    final double eps = 1e-4;
    for (final Map.Entry<String, List<DoublesPair>> entry : sortedMap.entrySet()) {
      final List<DoublesPair> list = new ArrayList<>();
      for (final DoublesPair pair : entry.getValue()) {
        list.add(DoublesPair.of(pair.first + 0.01 * eps, pair.second));
      }
      map.put(entry.getKey(), list);
    }
    final InterestRateCurveSensitivity sensitivity3 = new InterestRateCurveSensitivity(map);
    AssertSensitivityObjects.assertEquals("", sensitivity1, sensitivity3, eps);
    AssertSensitivityObjects.assertDoesNotEqual("", sensitivity1, sensitivity3, EPS);
  }

  @Test
  public void testCompareDifferentValues() {
    AssertSensitivityObjects.assertEquals("", new InterestRateCurveSensitivity(), new InterestRateCurveSensitivity(), EPS);
    final TreeMap<String, List<DoublesPair>> sortedMap = new TreeMap<>(SENSITIVITY_11);
    final InterestRateCurveSensitivity sensitivity1 = new InterestRateCurveSensitivity(sortedMap);
    final InterestRateCurveSensitivity sensitivity2 = new InterestRateCurveSensitivity(SENSITIVITY_12);
    AssertSensitivityObjects.assertDoesNotEqual("", sensitivity1, sensitivity2, EPS);
    final Map<String, List<DoublesPair>> map = Maps.newTreeMap();
    final double eps = 1e-4;
    for (final Map.Entry<String, List<DoublesPair>> entry : sortedMap.entrySet()) {
      final List<DoublesPair> list = new ArrayList<>();
      for (final DoublesPair pair : entry.getValue()) {
        list.add(DoublesPair.of(pair.first, pair.second + 0.01 * eps));
      }
      map.put(entry.getKey(), list);
    }
    final InterestRateCurveSensitivity sensitivity3 = new InterestRateCurveSensitivity(map);
    AssertSensitivityObjects.assertEquals("", sensitivity1, sensitivity3, eps);
    AssertSensitivityObjects.assertDoesNotEqual("", sensitivity1, sensitivity3, EPS);
  }

  private void assertIRCSEquals(final InterestRateCurveSensitivity expected, final InterestRateCurveSensitivity actual) {
    final Iterator<Map.Entry<String, List<DoublesPair>>> expectedIterator = new TreeMap<>(expected.getSensitivities()).entrySet().iterator();
    final Iterator<Map.Entry<String, List<DoublesPair>>> actualIterator = new TreeMap<>(actual.getSensitivities()).entrySet().iterator();
    do {
      final Map.Entry<String, List<DoublesPair>> expectedEntry = expectedIterator.next();
      final Map.Entry<String, List<DoublesPair>> actualEntry = actualIterator.next();
      assertEquals(expectedEntry.getKey(), actualEntry.getKey());
      assertEquals(expectedEntry.getValue().size(), actualEntry.getValue().size());
      for (int i = 0; i < expectedEntry.getValue().size(); i++) {
        final DoublesPair expectedPair = expectedEntry.getValue().get(i);
        final DoublesPair actualPair = actualEntry.getValue().get(i);
        assertEquals(expectedPair.first, actualPair.first, EPS);
        assertEquals(expectedPair.second, actualPair.second, EPS);
      }

    } while (expectedIterator.hasNext() && actualIterator.hasNext());
  }
}
