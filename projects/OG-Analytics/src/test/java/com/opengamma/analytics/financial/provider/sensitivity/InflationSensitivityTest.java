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

import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to test the PresentValueSensitivity class.
 */
@Test(groups = TestGroup.UNIT)
public class InflationSensitivityTest {

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
  public void nullDsc1() {
    InflationSensitivity.of(null, new HashMap<String, List<ForwardSensitivity>>(), new HashMap<String, List<DoublesPair>>());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFwd1() {
    InflationSensitivity.of(new HashMap<String, List<DoublesPair>>(), null, new HashMap<String, List<DoublesPair>>());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullPrice1() {
    InflationSensitivity.of(new HashMap<String, List<DoublesPair>>(), new HashMap<String, List<ForwardSensitivity>>(), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDsc2() {
    InflationSensitivity.ofYieldDiscountingAndPriceIndex(null, new HashMap<String, List<DoublesPair>>());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullPrice2() {
    InflationSensitivity.ofYieldDiscountingAndPriceIndex(new HashMap<String, List<DoublesPair>>(), null);
  }

  @Test
  public void of() {
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    mapDsc.put(CURVE_NAME_1, SENSI_DATA_1);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    mapFwd.put(CURVE_NAME_2, SENSI_FWD_1);
    final Map<String, List<DoublesPair>> mapIn = new HashMap<>();
    mapIn.put(CURVE_NAME_3, SENSI_DATA_3);
    final InflationSensitivity of = InflationSensitivity.of(mapDsc, mapFwd, mapIn);
    assertEquals("InflationSensitivity: of", mapDsc, of.getYieldDiscountingSensitivities());
    assertEquals("InflationSensitivity: of", mapFwd, of.getForwardSensitivities());
    assertEquals("InflationSensitivity: of", mapIn, of.getPriceCurveSensitivities());

    final InflationSensitivity ofDscIn = InflationSensitivity.ofYieldDiscountingAndPriceIndex(mapDsc, mapIn);
    assertEquals("InflationSensitivity: of", mapDsc, ofDscIn.getYieldDiscountingSensitivities());
    assertEquals("InflationSensitivity: of", mapIn, ofDscIn.getPriceCurveSensitivities());
    AssertSensivityObjects.assertEquals("InflationSensitivity: of", InflationSensitivity.of(mapDsc, new HashMap<String, List<ForwardSensitivity>>(), mapIn), ofDscIn, TOLERANCE);

    final InflationSensitivity ofFwd = InflationSensitivity.of(new HashMap<String, List<DoublesPair>>(), mapFwd, new HashMap<String, List<DoublesPair>>());
    InflationSensitivity constructor = new InflationSensitivity();
    constructor = constructor.plus(ofDscIn);
    constructor = constructor.plus(ofFwd).cleaned();
    AssertSensivityObjects.assertEquals("InflationSensitivity: of", constructor, of.cleaned(), TOLERANCE);
  }

  @Test
  public void plusMultipliedByDsc() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<>();
    final Map<String, List<DoublesPair>> sensi12 = new HashMap<>();
    final Map<String, List<DoublesPair>> sensi22 = new HashMap<>();
    final Map<String, List<DoublesPair>> sensi33 = new HashMap<>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    final InflationSensitivity pvSensi_11 = InflationSensitivity.ofYieldDiscounting(sensi11);
    sensi22.put(CURVE_NAME_2, SENSI_DATA_2);
    final InflationSensitivity pvSensi_22 = InflationSensitivity.ofYieldDiscounting(sensi22);
    sensi12.put(CURVE_NAME_1, SENSI_DATA_2);
    final InflationSensitivity pvSensi_12 = InflationSensitivity.ofYieldDiscounting(sensi12);
    sensi33.put(CURVE_NAME_3, SENSI_DATA_3);
    final InflationSensitivity pvSensi_33 = InflationSensitivity.ofYieldDiscounting(sensi33);
    // Simple add
    final Map<String, List<DoublesPair>> expectedSensi11add22 = new HashMap<>();
    expectedSensi11add22.put(CURVE_NAME_1, SENSI_DATA_1);
    expectedSensi11add22.put(CURVE_NAME_2, SENSI_DATA_2);
    assertEquals(expectedSensi11add22, pvSensi_11.plus(pvSensi_22).getYieldDiscountingSensitivities());
    assertEquals(InflationSensitivity.ofYieldDiscounting(expectedSensi11add22), pvSensi_11.plus(pvSensi_22));
    // Multiply
    final List<DoublesPair> sensiData1Multiply050 = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, 5.0d), DoublesPair.of(2d, 10.0d), DoublesPair.of(3d, 15.0d), DoublesPair.of(4d, 20.0d) });
    final Map<String, List<DoublesPair>> expectedSensi1Multiply05 = new HashMap<>();
    expectedSensi1Multiply05.put(CURVE_NAME_1, sensiData1Multiply050);
    assertEquals(expectedSensi1Multiply05, pvSensi_11.multipliedBy(0.5).getYieldDiscountingSensitivities());
    assertEquals(InflationSensitivity.ofYieldDiscounting(expectedSensi1Multiply05), pvSensi_11.multipliedBy(0.5));
    // Add on the same curve
    final List<DoublesPair> expectedSensiData1add2 = new ArrayList<>();
    expectedSensiData1add2.addAll(SENSI_DATA_1);
    expectedSensiData1add2.addAll(SENSI_DATA_2);
    final Map<String, List<DoublesPair>> expectedSensi11add12 = new HashMap<>();
    expectedSensi11add12.put(CURVE_NAME_1, expectedSensiData1add2);
    assertEquals(expectedSensi11add12, pvSensi_11.plus(pvSensi_12).getYieldDiscountingSensitivities());
    assertEquals(InflationSensitivity.ofYieldDiscounting(expectedSensi11add12), pvSensi_11.plus(pvSensi_12));
    // Add multi-curve
    final Map<String, List<DoublesPair>> expectedSensiAddMulti = new HashMap<>();
    expectedSensiAddMulti.put(CURVE_NAME_1, expectedSensiData1add2);
    expectedSensiAddMulti.put(CURVE_NAME_2, SENSI_DATA_2);
    expectedSensiAddMulti.put(CURVE_NAME_3, SENSI_DATA_3);
    assertEquals(expectedSensiAddMulti, pvSensi_11.plus(pvSensi_22.plus(pvSensi_33.plus(pvSensi_12))).getYieldDiscountingSensitivities());
    assertEquals(InflationSensitivity.ofYieldDiscounting(expectedSensiAddMulti), pvSensi_11.plus(pvSensi_22.plus(pvSensi_33.plus(pvSensi_12))));
  }

  @Test
  public void plusMultipliedByDscIn() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    final Map<String, List<DoublesPair>> sensi22 = new HashMap<>();
    sensi22.put(CURVE_NAME_2, SENSI_DATA_2);
    final InflationSensitivity pvSensiDscIn = InflationSensitivity.ofYieldDiscountingAndPriceIndex(sensi11, sensi22);
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: plusMultipliedBy", pvSensiDscIn.plus(pvSensiDscIn).cleaned(), pvSensiDscIn.multipliedBy(2.0).cleaned(), TOLERANCE);
  }

  @Test
  public void cleaned() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<>();
    final Map<String, List<DoublesPair>> sensi12 = new HashMap<>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    final InflationSensitivity pvSensi_11 = InflationSensitivity.ofYieldDiscounting(sensi11);
    sensi12.put(CURVE_NAME_1, SENSI_DATA_2);
    final InflationSensitivity pvSensi_12 = InflationSensitivity.ofYieldDiscounting(sensi12);
    final List<DoublesPair> expectedSensiDataClean12 = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, 50d), DoublesPair.of(2d, 50d), DoublesPair.of(3d, 50d), DoublesPair.of(4d, 50d) });
    final Map<String, List<DoublesPair>> expectedSensiClean12 = new HashMap<>();
    expectedSensiClean12.put(CURVE_NAME_1, expectedSensiDataClean12);
    assertEquals(InflationSensitivity.ofYieldDiscounting(expectedSensiClean12).getYieldDiscountingSensitivities(), pvSensi_11.plus(pvSensi_12).cleaned().getYieldDiscountingSensitivities());
  }

  @Test
  public void equalHash() {
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    mapDsc.put(CURVE_NAME_1, SENSI_DATA_1);
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    mapFwd.put(CURVE_NAME_2, SENSI_FWD_1);
    final Map<String, List<DoublesPair>> mapIn = new HashMap<>();
    mapIn.put(CURVE_NAME_3, SENSI_DATA_3);
    final InflationSensitivity cs = InflationSensitivity.of(mapDsc, mapFwd, mapIn);
    assertEquals("ParameterSensitivity: equalHash", cs, cs);
    assertEquals("ParameterSensitivity: equalHash", cs.hashCode(), cs.hashCode());
    assertFalse("ParameterSensitivity: equalHash", cs.equals(mapDsc));
    InflationSensitivity modified;
    modified = InflationSensitivity.of(mapDsc, mapFwd, mapDsc);
    assertFalse("ParameterSensitivity: equalHash", cs.equals(modified));
    modified = InflationSensitivity.of(mapIn, mapFwd, mapIn);
    assertFalse("ParameterSensitivity: equalHash", cs.equals(modified));
    final Map<String, List<ForwardSensitivity>> mapFwd2 = new HashMap<>();
    mapFwd2.put(CURVE_NAME_3, SENSI_FWD_1);
    modified = InflationSensitivity.of(mapDsc, mapFwd2, mapIn);
    assertFalse("ParameterSensitivity: equalHash", cs.equals(modified));
  }

}
