/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market.description;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.lambdava.tuple.DoublesPair;

/**
 * Class to test the PresentValueSensitivity class.
 */
public class CurveSensitivityMarketTest {

  private static final List<DoublesPair> SENSI_DATA_1 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 10), new DoublesPair(2, 20), new DoublesPair(3, 30), new DoublesPair(4, 40)});
  private static final List<DoublesPair> SENSI_DATA_2 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 40), new DoublesPair(2, 30), new DoublesPair(3, 20), new DoublesPair(4, 10)});
  private static final List<DoublesPair> SENSI_DATA_3 = Arrays.asList(new DoublesPair[] {new DoublesPair(11, 40), new DoublesPair(12, 30), new DoublesPair(13, 20), new DoublesPair(14, 10)});
  private static final List<MarketForwardSensitivity> SENSI_FWD_1 = new ArrayList<MarketForwardSensitivity>();
  static {
    SENSI_FWD_1.add(new MarketForwardSensitivity(0.5, 0.75, 0.26, 11));
    SENSI_FWD_1.add(new MarketForwardSensitivity(0.75, 1.00, 0.26, 12));
    SENSI_FWD_1.add(new MarketForwardSensitivity(1.00, 1.25, 0.24, 13));
  }
  private static final String CURVE_NAME_1 = "A";
  private static final String CURVE_NAME_2 = "B";
  private static final String CURVE_NAME_3 = "C";

  private static final double TOLERANCE = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDsc() {
    CurveSensitivityMarket.of(null, new HashMap<String, List<MarketForwardSensitivity>>(), new HashMap<String, List<DoublesPair>>());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDsc2() {
    CurveSensitivityMarket.ofYieldDiscounting(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDsc3() {
    CurveSensitivityMarket.ofYieldDiscountingAndPrice(null, new HashMap<String, List<DoublesPair>>());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFwd() {
    CurveSensitivityMarket.of(new HashMap<String, List<DoublesPair>>(), null, new HashMap<String, List<DoublesPair>>());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullPrice() {
    CurveSensitivityMarket.of(new HashMap<String, List<DoublesPair>>(), new HashMap<String, List<MarketForwardSensitivity>>(), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullPrice2() {
    CurveSensitivityMarket.ofYieldDiscountingAndPrice(new HashMap<String, List<DoublesPair>>(), null);
  }

  @Test
  public void of() {
    Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
    mapDsc.put(CURVE_NAME_1, SENSI_DATA_1);
    Map<String, List<MarketForwardSensitivity>> mapFwd = new HashMap<String, List<MarketForwardSensitivity>>();
    mapFwd.put(CURVE_NAME_2, SENSI_FWD_1);
    Map<String, List<DoublesPair>> mapIn = new HashMap<String, List<DoublesPair>>();
    mapIn.put(CURVE_NAME_3, SENSI_DATA_3);
    CurveSensitivityMarket of = CurveSensitivityMarket.of(mapDsc, mapFwd, mapIn);
    assertEquals("CurveSensitivityMarket: of", mapDsc, of.getYieldDiscountingSensitivities());
    assertEquals("CurveSensitivityMarket: of", mapFwd, of.getForwardSensitivities());
    assertEquals("CurveSensitivityMarket: of", mapIn, of.getPriceCurveSensitivities());

    CurveSensitivityMarket ofDsc = CurveSensitivityMarket.ofYieldDiscounting(mapDsc);
    assertEquals("CurveSensitivityMarket: of", mapDsc, ofDsc.getYieldDiscountingSensitivities());
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: of",
        CurveSensitivityMarket.of(mapDsc, new HashMap<String, List<MarketForwardSensitivity>>(), new HashMap<String, List<DoublesPair>>()), ofDsc, TOLERANCE);

    CurveSensitivityMarket ofFwd = CurveSensitivityMarket.ofForward(mapFwd);
    assertEquals("CurveSensitivityMarket: of", mapFwd, ofFwd.getForwardSensitivities());
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: of", CurveSensitivityMarket.of(new HashMap<String, List<DoublesPair>>(), mapFwd, new HashMap<String, List<DoublesPair>>()), ofFwd,
        TOLERANCE);

    CurveSensitivityMarket ofDscFwd = CurveSensitivityMarket.ofYieldDiscountingAndForward(mapDsc, mapFwd);
    assertEquals("CurveSensitivityMarket: of", mapFwd, ofFwd.getForwardSensitivities());
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: of", CurveSensitivityMarket.of(mapDsc, mapFwd, new HashMap<String, List<DoublesPair>>()), ofDscFwd, TOLERANCE);

    CurveSensitivityMarket ofDscIn = CurveSensitivityMarket.ofYieldDiscountingAndPrice(mapDsc, mapIn);
    assertEquals("CurveSensitivityMarket: of", mapFwd, ofFwd.getForwardSensitivities());
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: of", CurveSensitivityMarket.of(mapDsc, new HashMap<String, List<MarketForwardSensitivity>>(), mapIn), ofDscIn, TOLERANCE);

    CurveSensitivityMarket constructor = new CurveSensitivityMarket();
    constructor = constructor.plus(ofDscIn);
    constructor = constructor.plus(ofFwd);
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: of", constructor, of, TOLERANCE);
  }

  @Test
  public void plusMultipliedByDsc() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<String, List<DoublesPair>>();
    final Map<String, List<DoublesPair>> sensi12 = new HashMap<String, List<DoublesPair>>();
    final Map<String, List<DoublesPair>> sensi22 = new HashMap<String, List<DoublesPair>>();
    final Map<String, List<DoublesPair>> sensi33 = new HashMap<String, List<DoublesPair>>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    CurveSensitivityMarket pvSensi_11 = CurveSensitivityMarket.ofYieldDiscounting(sensi11);
    sensi22.put(CURVE_NAME_2, SENSI_DATA_2);
    CurveSensitivityMarket pvSensi_22 = CurveSensitivityMarket.ofYieldDiscounting(sensi22);
    sensi12.put(CURVE_NAME_1, SENSI_DATA_2);
    CurveSensitivityMarket pvSensi_12 = CurveSensitivityMarket.ofYieldDiscounting(sensi12);
    sensi33.put(CURVE_NAME_3, SENSI_DATA_3);
    CurveSensitivityMarket pvSensi_33 = CurveSensitivityMarket.ofYieldDiscounting(sensi33);
    // Simple add
    Map<String, List<DoublesPair>> expectedSensi11add22 = new HashMap<String, List<DoublesPair>>();
    expectedSensi11add22.put(CURVE_NAME_1, SENSI_DATA_1);
    expectedSensi11add22.put(CURVE_NAME_2, SENSI_DATA_2);
    assertEquals(expectedSensi11add22, pvSensi_11.plus(pvSensi_22).getYieldDiscountingSensitivities());
    assertEquals(CurveSensitivityMarket.ofYieldDiscounting(expectedSensi11add22), pvSensi_11.plus(pvSensi_22));
    // Multiply
    List<DoublesPair> sensiData1Multiply050 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 5.0), new DoublesPair(2, 10.0), new DoublesPair(3, 15.0), new DoublesPair(4, 20.0)});
    Map<String, List<DoublesPair>> expectedSensi1Multiply05 = new HashMap<String, List<DoublesPair>>();
    expectedSensi1Multiply05.put(CURVE_NAME_1, sensiData1Multiply050);
    assertEquals(expectedSensi1Multiply05, pvSensi_11.multipliedBy(0.5).getYieldDiscountingSensitivities());
    assertEquals(CurveSensitivityMarket.ofYieldDiscounting(expectedSensi1Multiply05), pvSensi_11.multipliedBy(0.5));
    // Add on the same curve
    List<DoublesPair> expectedSensiData1add2 = new ArrayList<DoublesPair>();
    expectedSensiData1add2.addAll(SENSI_DATA_1);
    expectedSensiData1add2.addAll(SENSI_DATA_2);
    Map<String, List<DoublesPair>> expectedSensi11add12 = new HashMap<String, List<DoublesPair>>();
    expectedSensi11add12.put(CURVE_NAME_1, expectedSensiData1add2);
    assertEquals(expectedSensi11add12, pvSensi_11.plus(pvSensi_12).getYieldDiscountingSensitivities());
    assertEquals(CurveSensitivityMarket.ofYieldDiscounting(expectedSensi11add12), pvSensi_11.plus(pvSensi_12));
    // Add multi-curve
    Map<String, List<DoublesPair>> expectedSensiAddMulti = new HashMap<String, List<DoublesPair>>();
    expectedSensiAddMulti.put(CURVE_NAME_1, expectedSensiData1add2);
    expectedSensiAddMulti.put(CURVE_NAME_2, SENSI_DATA_2);
    expectedSensiAddMulti.put(CURVE_NAME_3, SENSI_DATA_3);
    assertEquals(expectedSensiAddMulti, pvSensi_11.plus(pvSensi_22.plus(pvSensi_33.plus(pvSensi_12))).getYieldDiscountingSensitivities());
    assertEquals(CurveSensitivityMarket.ofYieldDiscounting(expectedSensiAddMulti), pvSensi_11.plus(pvSensi_22.plus(pvSensi_33.plus(pvSensi_12))));
  }

  @Test
  public void plusMultipliedByDscFwd() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<String, List<DoublesPair>>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    final Map<String, List<MarketForwardSensitivity>> sensiFwd11 = new HashMap<String, List<MarketForwardSensitivity>>();
    sensiFwd11.put(CURVE_NAME_2, SENSI_FWD_1);
    CurveSensitivityMarket pvSensiDscFwd = CurveSensitivityMarket.ofYieldDiscountingAndForward(sensi11, sensiFwd11);
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: plusMultipliedBy", pvSensiDscFwd.plus(pvSensiDscFwd).cleaned(), pvSensiDscFwd.multipliedBy(2.0).cleaned(), TOLERANCE);

    List<MarketForwardSensitivity> sensiFwd2 = new ArrayList<MarketForwardSensitivity>();
    sensiFwd2.add(new MarketForwardSensitivity(2.5, 2.75, 0.26, 11));
    final Map<String, List<DoublesPair>> sensi32 = new HashMap<String, List<DoublesPair>>();
    sensi11.put(CURVE_NAME_3, SENSI_DATA_2);
    final Map<String, List<MarketForwardSensitivity>> sensiFwd22 = new HashMap<String, List<MarketForwardSensitivity>>();
    sensiFwd22.put(CURVE_NAME_2, sensiFwd2);
    CurveSensitivityMarket pvSensiDscFwd2 = CurveSensitivityMarket.ofYieldDiscountingAndForward(sensi32, sensiFwd22);
    List<MarketForwardSensitivity> sensiFwd3 = new ArrayList<MarketForwardSensitivity>();
    sensiFwd3.addAll(SENSI_FWD_1);
    sensiFwd3.add(new MarketForwardSensitivity(2.5, 2.75, 0.26, 11));
    final Map<String, List<MarketForwardSensitivity>> sensiFwd23 = new HashMap<String, List<MarketForwardSensitivity>>();
    sensiFwd23.put(CURVE_NAME_2, sensiFwd3);
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: plusMultipliedBy",
        CurveSensitivityMarket.ofYieldDiscountingAndForward(InterestRateCurveSensitivityUtils.addSensitivity(sensi11, sensi32), sensiFwd23).cleaned(), pvSensiDscFwd.plus(pvSensiDscFwd2).cleaned(),
        TOLERANCE);

    final Map<String, List<MarketForwardSensitivity>> sensiFwd32 = new HashMap<String, List<MarketForwardSensitivity>>();
    sensiFwd22.put(CURVE_NAME_3, sensiFwd2);
    CurveSensitivityMarket pvSensiDscFwd3 = CurveSensitivityMarket.ofYieldDiscountingAndForward(sensi32, sensiFwd32);
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: plusMultipliedBy", pvSensiDscFwd3.plus(pvSensiDscFwd2).cleaned(), pvSensiDscFwd2.plus(pvSensiDscFwd3).cleaned(), TOLERANCE);
  }

  @Test
  public void plusMultipliedByDscIn() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<String, List<DoublesPair>>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    final Map<String, List<DoublesPair>> sensi22 = new HashMap<String, List<DoublesPair>>();
    sensi22.put(CURVE_NAME_2, SENSI_DATA_2);
    CurveSensitivityMarket pvSensiDscIn = CurveSensitivityMarket.ofYieldDiscountingAndPrice(sensi11, sensi22);
    AssertSensivityObjects.assertEquals("CurveSensitivityMarket: plusMultipliedBy", pvSensiDscIn.plus(pvSensiDscIn).cleaned(), pvSensiDscIn.multipliedBy(2.0).cleaned(), TOLERANCE);
  }

  @Test
  public void cleaned() {
    final Map<String, List<DoublesPair>> sensi11 = new HashMap<String, List<DoublesPair>>();
    final Map<String, List<DoublesPair>> sensi12 = new HashMap<String, List<DoublesPair>>();
    sensi11.put(CURVE_NAME_1, SENSI_DATA_1);
    CurveSensitivityMarket pvSensi_11 = CurveSensitivityMarket.ofYieldDiscounting(sensi11);
    sensi12.put(CURVE_NAME_1, SENSI_DATA_2);
    CurveSensitivityMarket pvSensi_12 = CurveSensitivityMarket.ofYieldDiscounting(sensi12);
    List<DoublesPair> expectedSensiDataClean12 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 50), new DoublesPair(2, 50), new DoublesPair(3, 50), new DoublesPair(4, 50)});
    Map<String, List<DoublesPair>> expectedSensiClean12 = new HashMap<String, List<DoublesPair>>();
    expectedSensiClean12.put(CURVE_NAME_1, expectedSensiDataClean12);
    assertEquals(CurveSensitivityMarket.ofYieldDiscounting(expectedSensiClean12).getYieldDiscountingSensitivities(), pvSensi_11.plus(pvSensi_12).cleaned().getYieldDiscountingSensitivities());
  }

  @Test
  public void equalHash() {
    Map<String, List<DoublesPair>> mapDsc = new HashMap<String, List<DoublesPair>>();
    mapDsc.put(CURVE_NAME_1, SENSI_DATA_1);
    Map<String, List<MarketForwardSensitivity>> mapFwd = new HashMap<String, List<MarketForwardSensitivity>>();
    mapFwd.put(CURVE_NAME_2, SENSI_FWD_1);
    Map<String, List<DoublesPair>> mapIn = new HashMap<String, List<DoublesPair>>();
    mapIn.put(CURVE_NAME_3, SENSI_DATA_3);
    CurveSensitivityMarket cs = CurveSensitivityMarket.of(mapDsc, mapFwd, mapIn);
    assertEquals("ParameterSensitivity: equalHash", cs, cs);
    assertEquals("ParameterSensitivity: equalHash", cs.hashCode(), cs.hashCode());
    assertFalse("ParameterSensitivity: equalHash", cs.equals(mapDsc));
    CurveSensitivityMarket modified;
    modified = CurveSensitivityMarket.of(mapDsc, mapFwd, mapDsc);
    assertFalse("ParameterSensitivity: equalHash", cs.equals(modified));
    modified = CurveSensitivityMarket.of(mapIn, mapFwd, mapIn);
    assertFalse("ParameterSensitivity: equalHash", cs.equals(modified));
    Map<String, List<MarketForwardSensitivity>> mapFwd2 = new HashMap<String, List<MarketForwardSensitivity>>();
    mapFwd2.put(CURVE_NAME_3, SENSI_FWD_1);
    modified = CurveSensitivityMarket.of(mapDsc, mapFwd2, mapIn);
    assertFalse("ParameterSensitivity: equalHash", cs.equals(modified));
  }

}
