/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.market.PresentValueCurveSensitivityMarket;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to test the PresentValueSensitivity class.
 */
public class PresentValueCurveSensitivityMarketTest {

  private static final List<DoublesPair> SENSI_DATA_1 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 10), new DoublesPair(2, 20), new DoublesPair(3, 30), new DoublesPair(4, 40)});
  private static final List<DoublesPair> SENSI_DATA_2 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 40), new DoublesPair(2, 30), new DoublesPair(3, 20), new DoublesPair(4, 10)});
  private static final List<DoublesPair> SENSI_DATA_3 = Arrays.asList(new DoublesPair[] {new DoublesPair(11, 40), new DoublesPair(12, 30), new DoublesPair(13, 20), new DoublesPair(14, 10)});
  private static final String CURVE_NAME_1 = "A";
  private static final String CURVE_NAME_2 = "B";
  private static final String CURVE_NAME_3 = "C";
  private static final Map<String, List<DoublesPair>> SENSI_11 = new HashMap<String, List<DoublesPair>>();
  private static final Map<String, List<DoublesPair>> SENSI_12 = new HashMap<String, List<DoublesPair>>();
  private static final Map<String, List<DoublesPair>> SENSI_22 = new HashMap<String, List<DoublesPair>>();
  private static final Map<String, List<DoublesPair>> SENSI_33 = new HashMap<String, List<DoublesPair>>();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSensi() {
    new PresentValueCurveSensitivityMarket(null);
  }

  @Test
  public void testAddMultiply() {
    SENSI_11.put(CURVE_NAME_1, SENSI_DATA_1);
    PresentValueCurveSensitivityMarket pvSensi_11 = new PresentValueCurveSensitivityMarket(SENSI_11);
    SENSI_22.put(CURVE_NAME_2, SENSI_DATA_2);
    PresentValueCurveSensitivityMarket pvSensi_22 = new PresentValueCurveSensitivityMarket(SENSI_22);
    SENSI_12.put(CURVE_NAME_1, SENSI_DATA_2);
    PresentValueCurveSensitivityMarket pvSensi_12 = new PresentValueCurveSensitivityMarket(SENSI_12);
    SENSI_33.put(CURVE_NAME_3, SENSI_DATA_3);
    PresentValueCurveSensitivityMarket pvSensi_33 = new PresentValueCurveSensitivityMarket(SENSI_33);
    // Simple add
    Map<String, List<DoublesPair>> expectedSensi11add22 = new HashMap<String, List<DoublesPair>>();
    expectedSensi11add22.put(CURVE_NAME_1, SENSI_DATA_1);
    expectedSensi11add22.put(CURVE_NAME_2, SENSI_DATA_2);
    assertEquals(expectedSensi11add22, pvSensi_11.add(pvSensi_22).getYieldCurveSensitivities());
    assertEquals(new PresentValueCurveSensitivityMarket(expectedSensi11add22), pvSensi_11.add(pvSensi_22));
    // Multiply
    List<DoublesPair> sensiData1Multiply050 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 5.0), new DoublesPair(2, 10.0), new DoublesPair(3, 15.0), new DoublesPair(4, 20.0)});
    Map<String, List<DoublesPair>> expectedSensi1Multiply05 = new HashMap<String, List<DoublesPair>>();
    expectedSensi1Multiply05.put(CURVE_NAME_1, sensiData1Multiply050);
    assertEquals(expectedSensi1Multiply05, pvSensi_11.multiply(0.5).getYieldCurveSensitivities());
    assertEquals(new PresentValueCurveSensitivityMarket(expectedSensi1Multiply05), pvSensi_11.multiply(0.5));
    // Add on the same curve
    List<DoublesPair> expectedSensiData1add2 = new ArrayList<DoublesPair>();
    expectedSensiData1add2.addAll(SENSI_DATA_1);
    expectedSensiData1add2.addAll(SENSI_DATA_2);
    Map<String, List<DoublesPair>> expectedSensi11add12 = new HashMap<String, List<DoublesPair>>();
    expectedSensi11add12.put(CURVE_NAME_1, expectedSensiData1add2);
    assertEquals(expectedSensi11add12, pvSensi_11.add(pvSensi_12).getYieldCurveSensitivities());
    assertEquals(new PresentValueCurveSensitivityMarket(expectedSensi11add12), pvSensi_11.add(pvSensi_12));
    // Add multi-curve
    Map<String, List<DoublesPair>> expectedSensiAddMulti = new HashMap<String, List<DoublesPair>>();
    expectedSensiAddMulti.put(CURVE_NAME_1, expectedSensiData1add2);
    expectedSensiAddMulti.put(CURVE_NAME_2, SENSI_DATA_2);
    expectedSensiAddMulti.put(CURVE_NAME_3, SENSI_DATA_3);
    assertEquals(expectedSensiAddMulti, pvSensi_11.add(pvSensi_22.add(pvSensi_33.add(pvSensi_12))).getYieldCurveSensitivities());
    assertEquals(new PresentValueCurveSensitivityMarket(expectedSensiAddMulti), pvSensi_11.add(pvSensi_22.add(pvSensi_33.add(pvSensi_12))));
  }

  @Test
  public void testClean() {

    SENSI_11.put(CURVE_NAME_1, SENSI_DATA_1);
    PresentValueCurveSensitivityMarket pvSensi_11 = new PresentValueCurveSensitivityMarket(SENSI_11);
    SENSI_12.put(CURVE_NAME_1, SENSI_DATA_2);
    PresentValueCurveSensitivityMarket pvSensi_12 = new PresentValueCurveSensitivityMarket(SENSI_12);
    List<DoublesPair> expectedSensiDataClean12 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 50), new DoublesPair(2, 50), new DoublesPair(3, 50), new DoublesPair(4, 50)});
    Map<String, List<DoublesPair>> expectedSensiClean12 = new HashMap<String, List<DoublesPair>>();
    expectedSensiClean12.put(CURVE_NAME_1, expectedSensiDataClean12);
    assertEquals((new PresentValueCurveSensitivityMarket(expectedSensiClean12)).getYieldCurveSensitivities(), pvSensi_11.add(pvSensi_12).clean().getYieldCurveSensitivities());
  }
}
