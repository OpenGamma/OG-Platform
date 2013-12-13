/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class YieldCurveBundleTest {
  private static final String[] NAMES = new String[] {"A", "B", "C"};
  private static final YieldAndDiscountCurve[] CURVES = new YieldAndDiscountCurve[3];
  private static final Map<String, YieldAndDiscountCurve> MAP = new HashMap<>();
  private static final YieldCurveBundle BUNDLE;

  static {
    CURVES[0] = YieldCurve.from(ConstantDoublesCurve.from(0.03));
    CURVES[1] = YieldCurve.from(ConstantDoublesCurve.from(0.04));
    CURVES[2] = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    for (int i = 0; i < 3; i++) {
      MAP.put(NAMES[i], CURVES[i]);
    }
    BUNDLE = new YieldCurveBundle(NAMES, CURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNameArray() {
    new YieldCurveBundle(null, CURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveArray() {
    new YieldCurveBundle(NAMES, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongNameArrayLength() {
    new YieldCurveBundle(new String[] {"A", "B"}, CURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCurveArrayLength() {
    new YieldCurveBundle(NAMES, new YieldAndDiscountCurve[] {CURVES[0], CURVES[1]});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNameInArray() {
    new YieldCurveBundle(new String[] {"A", "B", null}, CURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveInArray() {
    new YieldCurveBundle(NAMES, new YieldAndDiscountCurve[] {CURVES[0], CURVES[1], null});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNameInMap() {
    final Map<String, YieldAndDiscountCurve> map = new HashMap<>();
    for (int i = 0; i < 2; i++) {
      map.put(NAMES[i], CURVES[i]);
    }
    map.put(null, CURVES[2]);
    new YieldCurveBundle(map);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveInMap() {
    final Map<String, YieldAndDiscountCurve> map = new HashMap<>();
    for (int i = 0; i < 2; i++) {
      map.put(NAMES[i], CURVES[i]);
    }
    map.put(NAMES[2], null);
    new YieldCurveBundle(map);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetNullName() {
    BUNDLE.setCurve(null, CURVES[1]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetNullCurve() {
    BUNDLE.setCurve("D", null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddPreviousCurve() {
    BUNDLE.setCurve(NAMES[0], CURVES[2]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetNonExistentCurve() {
    BUNDLE.getCurve("D");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testReplaceNonExistentCurve() {
    BUNDLE.replaceCurve("E", CURVES[1]);
  }

  @Test
  public void testGetters() {
    final YieldCurveBundle bundle = new YieldCurveBundle(MAP);
    assertEquals(bundle.size(), 3);
    assertEquals(bundle.getAllNames(), MAP.keySet());
    for (int i = 0; i < 3; i++) {
      assertEquals(bundle.getCurve(NAMES[i]), CURVES[i]);
    }
  }

  @Test
  public void testHashCodeAndEquals() {
    YieldCurveBundle other = new YieldCurveBundle(NAMES, CURVES);
    assertEquals(BUNDLE, other);
    assertEquals(BUNDLE.hashCode(), other.hashCode());
    other = new YieldCurveBundle(MAP);
    assertEquals(BUNDLE, other);
    assertEquals(BUNDLE.hashCode(), other.hashCode());
    other = new YieldCurveBundle(new String[] {NAMES[0], NAMES[1]}, new YieldAndDiscountCurve[] {CURVES[0], CURVES[1]});
    assertFalse(other.equals(BUNDLE));
  }

  @Test
  public void testSetter() {
    final YieldCurveBundle bundle1 = new YieldCurveBundle();
    final YieldCurveBundle bundle2 = new YieldCurveBundle(MAP);
    bundle1.addAll(bundle2);
    assertEquals(bundle1, bundle2);
  }
}
