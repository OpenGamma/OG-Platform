/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to test the PresentValueSensitivity class.
 */
@Test(groups = TestGroup.UNIT)
public class MulticurveSensitivityTest {

  private static final List<DoublesPair> SENSI_DATA_1 = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, 10d), DoublesPair.of(2d, 20d), DoublesPair.of(3d, 30d), DoublesPair.of(4d, 40d) });
  private static final List<DoublesPair> SENSI_DATA_2 = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, 40d), DoublesPair.of(2d, 30d), DoublesPair.of(3d, 20d), DoublesPair.of(4d, 10d) });
  private static final List<DoublesPair> SENSI_DATA_3 = Arrays.asList(new DoublesPair[] {DoublesPair.of(11d, 40d), DoublesPair.of(12d, 30d), DoublesPair.of(13d, 20d), DoublesPair.of(14d, 10d) });
  private static final List<ForwardSensitivity> SENSI_FWD_1 = new ArrayList<>();
  static {
    SENSI_FWD_1.add(new SimplyCompoundedForwardSensitivity(0.5, 0.75, 0.26, 11));
    SENSI_FWD_1.add(new SimplyCompoundedForwardSensitivity(0.75, 1.00, 0.26, 12));
    SENSI_FWD_1.add(new SimplyCompoundedForwardSensitivity(1.00, 1.25, 0.24, 13));
  }
  private static final String CURVE_NAME_1 = "A";
  private static final String CURVE_NAME_2 = "B";
  private static final String CURVE_NAME_3 = "C";

  private static final double TOLERANCE = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDsc() {
    MulticurveSensitivity.of(null, new HashMap<String, List<ForwardSensitivity>>());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDsc2() {
    MulticurveSensitivity.ofYieldDiscounting(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFwd() {
    MulticurveSensitivity.of(new HashMap<String, List<DoublesPair>>(), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFwd2() {
    MulticurveSensitivity.ofForward(null);
  }

  @Test
  public void of() {
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    mapDsc.put(CURVE_NAME_1, SENSI_DATA_1);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    mapFwd.put(CURVE_NAME_2, SENSI_FWD_1);
    final MulticurveSensitivity of = MulticurveSensitivity.of(mapDsc, mapFwd);
    assertEquals("CurveSensitivityMarket: of", mapDsc, of.getYieldDiscountingSensitivities());
    assertEquals("CurveSensitivityMarket: of", mapFwd, of.getForwardSensitivities());

    final MulticurveSensitivity ofDsc = MulticurveSensitivity.ofYieldDiscounting(mapDsc);
    assertEquals("CurveSensitivityMarket: of", mapDsc, ofDsc.getYieldDiscountingSensitivities());
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: of", MulticurveSensitivity.of(mapDsc, new HashMap<String, List<ForwardSensitivity>>()), ofDsc, TOLERANCE);

    final MulticurveSensitivity ofFwd = MulticurveSensitivity.ofForward(mapFwd);
    assertEquals("CurveSensitivityMarket: of", mapFwd, ofFwd.getForwardSensitivities());
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: of", MulticurveSensitivity.of(new HashMap<String, List<DoublesPair>>(), mapFwd), ofFwd, TOLERANCE);
  }

  @Test
  public void plusMultipliedByDsc() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<>();
    final Map<String, List<DoublesPair>> sensi12 = new HashMap<>();
    final Map<String, List<DoublesPair>> sensi22 = new HashMap<>();
    final Map<String, List<DoublesPair>> sensi33 = new HashMap<>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    final MulticurveSensitivity pvSensi_11 = MulticurveSensitivity.ofYieldDiscounting(sensi11);
    sensi22.put(CURVE_NAME_2, SENSI_DATA_2);
    final MulticurveSensitivity pvSensi_22 = MulticurveSensitivity.ofYieldDiscounting(sensi22);
    sensi12.put(CURVE_NAME_1, SENSI_DATA_2);
    final MulticurveSensitivity pvSensi_12 = MulticurveSensitivity.ofYieldDiscounting(sensi12);
    sensi33.put(CURVE_NAME_3, SENSI_DATA_3);
    final MulticurveSensitivity pvSensi_33 = MulticurveSensitivity.ofYieldDiscounting(sensi33);
    // Simple add
    final Map<String, List<DoublesPair>> expectedSensi11add22 = new HashMap<>();
    expectedSensi11add22.put(CURVE_NAME_1, SENSI_DATA_1);
    expectedSensi11add22.put(CURVE_NAME_2, SENSI_DATA_2);
    assertEquals(expectedSensi11add22, pvSensi_11.plus(pvSensi_22).getYieldDiscountingSensitivities());
    assertEquals(MulticurveSensitivity.ofYieldDiscounting(expectedSensi11add22), pvSensi_11.plus(pvSensi_22));
    // Multiply
    final List<DoublesPair> sensiData1Multiply050 = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, 5d), DoublesPair.of(2d, 10d), DoublesPair.of(3d, 15d), DoublesPair.of(4d, 20d) });
    final Map<String, List<DoublesPair>> expectedSensi1Multiply05 = new HashMap<>();
    expectedSensi1Multiply05.put(CURVE_NAME_1, sensiData1Multiply050);
    assertEquals(expectedSensi1Multiply05, pvSensi_11.multipliedBy(0.5).getYieldDiscountingSensitivities());
    assertEquals(MulticurveSensitivity.ofYieldDiscounting(expectedSensi1Multiply05), pvSensi_11.multipliedBy(0.5));
    // Add on the same curve
    final List<DoublesPair> expectedSensiData1add2 = new ArrayList<>();
    expectedSensiData1add2.addAll(SENSI_DATA_1);
    expectedSensiData1add2.addAll(SENSI_DATA_2);
    final Map<String, List<DoublesPair>> expectedSensi11add12 = new HashMap<>();
    expectedSensi11add12.put(CURVE_NAME_1, expectedSensiData1add2);
    assertEquals(expectedSensi11add12, pvSensi_11.plus(pvSensi_12).getYieldDiscountingSensitivities());
    assertEquals(MulticurveSensitivity.ofYieldDiscounting(expectedSensi11add12), pvSensi_11.plus(pvSensi_12));
    // Add multi-curve
    final Map<String, List<DoublesPair>> expectedSensiAddMulti = new HashMap<>();
    expectedSensiAddMulti.put(CURVE_NAME_1, expectedSensiData1add2);
    expectedSensiAddMulti.put(CURVE_NAME_2, SENSI_DATA_2);
    expectedSensiAddMulti.put(CURVE_NAME_3, SENSI_DATA_3);
    assertEquals(expectedSensiAddMulti, pvSensi_11.plus(pvSensi_22.plus(pvSensi_33.plus(pvSensi_12))).getYieldDiscountingSensitivities());
    assertEquals(MulticurveSensitivity.ofYieldDiscounting(expectedSensiAddMulti), pvSensi_11.plus(pvSensi_22.plus(pvSensi_33.plus(pvSensi_12))));
  }

  @Test
  public void plusMultipliedByDscFwd() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    final Map<String, List<ForwardSensitivity>> sensiFwd11 = new HashMap<>();
    sensiFwd11.put(CURVE_NAME_2, SENSI_FWD_1);
    final MulticurveSensitivity pvSensiDscFwd = MulticurveSensitivity.of(sensi11, sensiFwd11);
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: plusMultipliedBy", pvSensiDscFwd.plus(pvSensiDscFwd).cleaned(), pvSensiDscFwd.multipliedBy(2.0).cleaned(), TOLERANCE);

    final List<ForwardSensitivity> sensiFwd2 = new ArrayList<>();
    sensiFwd2.add(new SimplyCompoundedForwardSensitivity(2.5, 2.75, 0.26, 11));
    final Map<String, List<DoublesPair>> sensi32 = new HashMap<>();
    sensi11.put(CURVE_NAME_3, SENSI_DATA_2);
    final Map<String, List<ForwardSensitivity>> sensiFwd22 = new HashMap<>();
    sensiFwd22.put(CURVE_NAME_2, sensiFwd2);
    final MulticurveSensitivity pvSensiDscFwd2 = MulticurveSensitivity.of(sensi32, sensiFwd22);
    final List<ForwardSensitivity> sensiFwd3 = new ArrayList<>();
    sensiFwd3.addAll(SENSI_FWD_1);
    sensiFwd3.add(new SimplyCompoundedForwardSensitivity(2.5, 2.75, 0.26, 11));
    final Map<String, List<ForwardSensitivity>> sensiFwd23 = new HashMap<>();
    sensiFwd23.put(CURVE_NAME_2, sensiFwd3);
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: plusMultipliedBy", MulticurveSensitivity.of(InterestRateCurveSensitivityUtils.addSensitivity(sensi11, sensi32), sensiFwd23).cleaned(),
        pvSensiDscFwd.plus(pvSensiDscFwd2).cleaned(), TOLERANCE);

    final Map<String, List<ForwardSensitivity>> sensiFwd32 = new HashMap<>();
    sensiFwd22.put(CURVE_NAME_3, sensiFwd2);
    final MulticurveSensitivity pvSensiDscFwd3 = MulticurveSensitivity.of(sensi32, sensiFwd32);
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: plusMultipliedBy", pvSensiDscFwd3.plus(pvSensiDscFwd2).cleaned(), pvSensiDscFwd2.plus(pvSensiDscFwd3).cleaned(), TOLERANCE);
  }

  @Test
  public void cleaned() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<>();
    final Map<String, List<DoublesPair>> sensi12 = new HashMap<>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    final MulticurveSensitivity pvSensi_11 = MulticurveSensitivity.ofYieldDiscounting(sensi11);
    sensi12.put(CURVE_NAME_1, SENSI_DATA_2);
    final MulticurveSensitivity pvSensi_12 = MulticurveSensitivity.ofYieldDiscounting(sensi12);
    final List<DoublesPair> expectedSensiDataClean12 = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, 50d), DoublesPair.of(2d, 50d), DoublesPair.of(3d, 50d), DoublesPair.of(4d, 50d) });
    final Map<String, List<DoublesPair>> expectedSensiClean12 = new HashMap<>();
    expectedSensiClean12.put(CURVE_NAME_1, expectedSensiDataClean12);
    assertEquals(MulticurveSensitivity.ofYieldDiscounting(expectedSensiClean12).getYieldDiscountingSensitivities(), pvSensi_11.plus(pvSensi_12).cleaned().getYieldDiscountingSensitivities());
  }

  @Test
  public void equalHash() {
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    mapDsc.put(CURVE_NAME_1, SENSI_DATA_1);
    final Map<String, List<DoublesPair>> mapDsc2 = new HashMap<>();
    mapDsc.put(CURVE_NAME_2, SENSI_DATA_1);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    mapFwd.put(CURVE_NAME_2, SENSI_FWD_1);
    final Map<String, List<ForwardSensitivity>> mapFwd2 = new HashMap<>();
    mapFwd2.put(CURVE_NAME_3, SENSI_FWD_1);
    final MulticurveSensitivity cs = MulticurveSensitivity.of(mapDsc, mapFwd);
    assertEquals("ParameterSensitivity: equalHash", cs, cs);
    assertEquals("ParameterSensitivity: equalHash", cs.hashCode(), cs.hashCode());
    assertFalse("ParameterSensitivity: equalHash", cs.equals(mapDsc));
    MulticurveSensitivity modified;
    modified = MulticurveSensitivity.of(mapDsc2, mapFwd);
    assertFalse("ParameterSensitivity: equalHash", cs.equals(modified));
    modified = MulticurveSensitivity.of(mapDsc, mapFwd2);
    assertFalse("ParameterSensitivity: equalHash", cs.equals(modified));
  }

}
