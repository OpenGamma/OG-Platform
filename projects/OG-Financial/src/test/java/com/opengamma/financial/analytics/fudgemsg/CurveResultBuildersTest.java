/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.AnnuallyCompoundedForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurveResultBuildersTest extends AnalyticsTestBase {

  @Test
  public void testSimplyForwardSensitivity() {
    final SimplyCompoundedForwardSensitivity forwardSensitivity = new SimplyCompoundedForwardSensitivity(12., 13., 17., 25.);
    assertEquals(forwardSensitivity, cycleObject(SimplyCompoundedForwardSensitivity.class, forwardSensitivity));
  }

  @Test
  public void testAnnuallyForwardSensitivity() {
    final AnnuallyCompoundedForwardSensitivity forwardSensitivity = new AnnuallyCompoundedForwardSensitivity(12., 13., 17., 25.);
    assertEquals(forwardSensitivity, cycleObject(AnnuallyCompoundedForwardSensitivity.class, forwardSensitivity));
  }

  @Test
  public void testMulticurveSensitivity() {
    final String name1 = "YC1";
    final String name2 = "YC2";
    final Map<String, List<DoublesPair>> yieldCurveSensitivities = new HashMap<>();
    yieldCurveSensitivities.put(name1, Arrays.asList(DoublesPair.of(1., 2.), DoublesPair.of(3.5, 6.8)));
    yieldCurveSensitivities.put(name2, Arrays.asList(DoublesPair.of(11., 12.), DoublesPair.of(13.5, 16.8), DoublesPair.of(45., 12.)));
    final Map<String, List<ForwardSensitivity>> forwardCurveSensitivities = new HashMap<>();
    final ForwardSensitivity sensi1 = new SimplyCompoundedForwardSensitivity(1, 5, 0.25, 10);
    final ForwardSensitivity sensi2 = new SimplyCompoundedForwardSensitivity(2, 3, 0.215, 20);
    final ForwardSensitivity sensi3 = new SimplyCompoundedForwardSensitivity(3, 9, 0.225, 30);
    forwardCurveSensitivities.put(name1, Arrays.asList(sensi1, sensi2, sensi3));
    final ForwardSensitivity sensi4 = new SimplyCompoundedForwardSensitivity(0.1, 4, 0.5, 10);
    forwardCurveSensitivities.put(name2, Arrays.asList(sensi4));
    final MulticurveSensitivity sensitivities = MulticurveSensitivity.of(yieldCurveSensitivities, forwardCurveSensitivities);
    assertEquals(sensitivities, cycleObject(MulticurveSensitivity.class, sensitivities));
  }

  @Test
  public void testMultipleCurrencyMulticurveSensitivity() {
    final String name1 = "YC1";
    final String name2 = "YC2";
    final Map<String, List<DoublesPair>> yieldCurveSensitivities1 = new HashMap<>();
    yieldCurveSensitivities1.put(name1, Arrays.asList(DoublesPair.of(1., 2.), DoublesPair.of(3.5, 6.8)));
    yieldCurveSensitivities1.put(name2, Arrays.asList(DoublesPair.of(11., 12.), DoublesPair.of(13.5, 16.8), DoublesPair.of(45., 12.)));
    final Map<String, List<ForwardSensitivity>> forwardCurveSensitivities1 = new HashMap<>();
    final ForwardSensitivity sensi1 = new SimplyCompoundedForwardSensitivity(1, 5, 0.25, 10);
    final ForwardSensitivity sensi2 = new SimplyCompoundedForwardSensitivity(2, 3, 0.215, 20);
    final ForwardSensitivity sensi3 = new SimplyCompoundedForwardSensitivity(3, 9, 0.225, 30);
    forwardCurveSensitivities1.put(name1, Arrays.asList(sensi1, sensi2, sensi3));
    final ForwardSensitivity sensi4 = new SimplyCompoundedForwardSensitivity(0.1, 4, 0.5, 10);
    forwardCurveSensitivities1.put(name2, Arrays.asList(sensi4));
    final MulticurveSensitivity sensitivities1 = MulticurveSensitivity.of(yieldCurveSensitivities1, forwardCurveSensitivities1);
    final String name3 = "YC3";
    final String name4 = "YC4";
    final Map<String, List<DoublesPair>> yieldCurveSensitivities2 = new HashMap<>();
    yieldCurveSensitivities2.put(name3, Arrays.asList(DoublesPair.of(10., 20.)));
    yieldCurveSensitivities2.put(name4, Arrays.asList(DoublesPair.of(110., 120.), DoublesPair.of(13.51, 16.81), DoublesPair.of(45.3, 12.3)));
    final Map<String, List<ForwardSensitivity>> forwardCurveSensitivities2 = new HashMap<>();
    final ForwardSensitivity sensi21 = new SimplyCompoundedForwardSensitivity(18, 58, 0.258, 108);
    final ForwardSensitivity sensi22 = new SimplyCompoundedForwardSensitivity(32, 92, 0.2252, 302);
    final ForwardSensitivity sensi23 = new SimplyCompoundedForwardSensitivity(0.18, 48, 0.58, 18);
    forwardCurveSensitivities2.put(name3, Arrays.asList(sensi21, sensi22));
    forwardCurveSensitivities2.put(name4, Arrays.asList(sensi23));
    final MulticurveSensitivity sensitivities2 = MulticurveSensitivity.of(yieldCurveSensitivities2, forwardCurveSensitivities2);
    MultipleCurrencyMulticurveSensitivity sensitivities = new MultipleCurrencyMulticurveSensitivity();
    sensitivities = sensitivities.plus(Currency.AUD, sensitivities1);
    sensitivities = sensitivities.plus(Currency.CAD, sensitivities2);
    assertEquals(sensitivities, cycleObject(MultipleCurrencyMulticurveSensitivity.class, sensitivities));
  }

  @Test
  public void testSimpleParameterSensitivity() {
    final String name1 = "YC1";
    final String name2 = "YC2";
    final DoubleMatrix1D sensitivities1 = new DoubleMatrix1D(new double[] {1, 2, 4, 6, 7, 9, 12 });
    final DoubleMatrix1D sensitivities2 = new DoubleMatrix1D(new double[] {89, 456, 234, 12 });
    final LinkedHashMap<String, DoubleMatrix1D> sensitivities = new LinkedHashMap<>();
    sensitivities.put(name1, sensitivities1);
    sensitivities.put(name2, sensitivities2);
    final SimpleParameterSensitivity sps = new SimpleParameterSensitivity(sensitivities);
    assertEquals(sps, cycleObject(SimpleParameterSensitivity.class, sps));
  }

  @Test
  public void testMultipleCurrencyParameterSensitivity() {
    final String name1 = "YC1";
    final String name2 = "YC2";
    final DoubleMatrix1D sensitivities1 = new DoubleMatrix1D(new double[] {1, 2, 4, 6, 7, 9, 12 });
    final DoubleMatrix1D sensitivities2 = new DoubleMatrix1D(new double[] {89, 456, 234, 12 });
    final String name3 = "YC3";
    final String name4 = "YC4";
    final DoubleMatrix1D sensitivities3 = new DoubleMatrix1D(new double[] {11, 21, 41, 61, 17, 91, 112 });
    final DoubleMatrix1D sensitivities4 = new DoubleMatrix1D(new double[] {891, 4561, 1234, 112 });
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> data = new LinkedHashMap<>();
    data.put(Pairs.of(name1, Currency.AUD), sensitivities1);
    data.put(Pairs.of(name2, Currency.EUR), sensitivities2);
    data.put(Pairs.of(name3, Currency.USD), sensitivities3);
    data.put(Pairs.of(name4, Currency.CAD), sensitivities4);
    final MultipleCurrencyParameterSensitivity sensitivities = new MultipleCurrencyParameterSensitivity(data);
    assertEquals(sensitivities, cycleObject(MultipleCurrencyParameterSensitivity.class, sensitivities));
  }

  @Test
  public void testCurveBuildingBlock() {
    final LinkedHashMap<String, Pair<Integer, Integer>> data = new LinkedHashMap<>();
    data.put("YC1", Pairs.of(Integer.valueOf(0), Integer.valueOf(20)));
    data.put("YC2", Pairs.of(Integer.valueOf(20), Integer.valueOf(30)));
    final CurveBuildingBlock curveBuildingBlock = new CurveBuildingBlock(data);
    assertEquals(curveBuildingBlock, cycleObject(CurveBuildingBlock.class, curveBuildingBlock));
  }

  @Test
  public void testCurveBuildingBlockBundle() {
    final LinkedHashMap<String, Pair<Integer, Integer>> data1 = new LinkedHashMap<>();
    data1.put("YC1", Pairs.of(Integer.valueOf(0), Integer.valueOf(2)));
    data1.put("YC2", Pairs.of(Integer.valueOf(2), Integer.valueOf(3)));
    final CurveBuildingBlock curveBuildingBlock1 = new CurveBuildingBlock(data1);
    final DoubleMatrix2D matrix1 = new DoubleMatrix2D(new double[][] {new double[] {1, 2, 3, 4 }, new double[] {5, 6, 7, 8 } });
    final LinkedHashMap<String, Pair<Integer, Integer>> data2 = new LinkedHashMap<>();
    data2.put("YC3", Pairs.of(Integer.valueOf(0), Integer.valueOf(1)));
    data2.put("YC4", Pairs.of(Integer.valueOf(1), Integer.valueOf(2)));
    final CurveBuildingBlock curveBuildingBlock2 = new CurveBuildingBlock(data2);
    final DoubleMatrix2D matrix2 = new DoubleMatrix2D(new double[][] {new double[] {9, 8, 7 }, new double[] {6, 5, 4 } });
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> data = new LinkedHashMap<>();
    data.put("B1", Pairs.of(curveBuildingBlock1, matrix1));
    data.put("B2", Pairs.of(curveBuildingBlock2, matrix2));
    final CurveBuildingBlockBundle cbbb = new CurveBuildingBlockBundle(data);
    assertEquals(cbbb, cycleObject(CurveBuildingBlockBundle.class, cbbb));
  }

  @Test
  public void testInflationSensitivity() {
    final String name1 = "YC1";
    final String name2 = "YC2";
    final Map<String, List<DoublesPair>> yieldCurveSensitivities = new HashMap<>();
    yieldCurveSensitivities.put(name1, Arrays.asList(DoublesPair.of(1., 2.), DoublesPair.of(3.5, 6.8)));
    yieldCurveSensitivities.put(name2, Arrays.asList(DoublesPair.of(11., 12.), DoublesPair.of(13.5, 16.8), DoublesPair.of(45., 12.)));
    final Map<String, List<ForwardSensitivity>> forwardCurveSensitivities = new HashMap<>();
    final ForwardSensitivity sensi1 = new SimplyCompoundedForwardSensitivity(1, 5, 0.25, 10);
    final ForwardSensitivity sensi2 = new SimplyCompoundedForwardSensitivity(2, 3, 0.215, 20);
    final ForwardSensitivity sensi3 = new SimplyCompoundedForwardSensitivity(3, 9, 0.225, 30);
    forwardCurveSensitivities.put(name1, Arrays.asList(sensi1, sensi2, sensi3));
    final ForwardSensitivity sensi4 = new SimplyCompoundedForwardSensitivity(0.1, 4, 0.5, 10);
    forwardCurveSensitivities.put(name2, Arrays.asList(sensi4));
    final MulticurveSensitivity sensitivities = MulticurveSensitivity.of(yieldCurveSensitivities, forwardCurveSensitivities);
    final String name3 = "PC1";
    final String name4 = "PC2";
    final Map<String, List<DoublesPair>> priceCurveSensitivities = new HashMap<>();
    priceCurveSensitivities.put(name3, Arrays.asList(DoublesPair.of(1.2, 2.3), DoublesPair.of(3.4, 4.5)));
    priceCurveSensitivities.put(name4, Arrays.asList(DoublesPair.of(9.8, 8.7), DoublesPair.of(5.4, 3.2), DoublesPair.of(2.33, 3.44)));
    final InflationSensitivity is = InflationSensitivity.of(sensitivities, priceCurveSensitivities);
    assertEquals(is, cycleObject(InflationSensitivity.class, is));
  }

  @Test
  public void testMultipleCurrencyInflationSensitivity() {
    final String name1 = "YC1";
    final String name2 = "YC2";
    final Map<String, List<DoublesPair>> yieldCurveSensitivities1 = new HashMap<>();
    yieldCurveSensitivities1.put(name1, Arrays.asList(DoublesPair.of(1., 2.), DoublesPair.of(3.5, 6.8)));
    yieldCurveSensitivities1.put(name2, Arrays.asList(DoublesPair.of(11., 12.), DoublesPair.of(13.5, 16.8), DoublesPair.of(45., 12.)));
    final Map<String, List<ForwardSensitivity>> forwardCurveSensitivities1 = new HashMap<>();
    final ForwardSensitivity sensi1 = new SimplyCompoundedForwardSensitivity(1, 5, 0.25, 10);
    final ForwardSensitivity sensi2 = new SimplyCompoundedForwardSensitivity(2, 3, 0.215, 20);
    final ForwardSensitivity sensi3 = new SimplyCompoundedForwardSensitivity(3, 9, 0.225, 30);
    forwardCurveSensitivities1.put(name1, Arrays.asList(sensi1, sensi2, sensi3));
    final ForwardSensitivity sensi4 = new SimplyCompoundedForwardSensitivity(0.1, 4, 0.5, 10);
    forwardCurveSensitivities1.put(name2, Arrays.asList(sensi4));
    final MulticurveSensitivity sensitivities1 = MulticurveSensitivity.of(yieldCurveSensitivities1, forwardCurveSensitivities1);
    final Map<String, List<DoublesPair>> priceCurveSensitivities1 = new HashMap<>();
    priceCurveSensitivities1.put("PC1", Arrays.asList(DoublesPair.of(1.2, 2.3), DoublesPair.of(3.4, 4.5)));
    priceCurveSensitivities1.put("PC2", Arrays.asList(DoublesPair.of(9.8, 8.7), DoublesPair.of(5.4, 3.2), DoublesPair.of(2.33, 3.44)));
    final InflationSensitivity is1 = InflationSensitivity.of(sensitivities1, priceCurveSensitivities1);
    final String name3 = "YC3";
    final String name4 = "YC4";
    final Map<String, List<DoublesPair>> yieldCurveSensitivities2 = new HashMap<>();
    yieldCurveSensitivities2.put(name3, Arrays.asList(DoublesPair.of(10., 20.)));
    yieldCurveSensitivities2.put(name4, Arrays.asList(DoublesPair.of(110., 120.), DoublesPair.of(13.51, 16.81), DoublesPair.of(45.3, 12.3)));
    final Map<String, List<ForwardSensitivity>> forwardCurveSensitivities2 = new HashMap<>();
    final ForwardSensitivity sensi21 = new SimplyCompoundedForwardSensitivity(18, 58, 0.258, 108);
    final ForwardSensitivity sensi22 = new SimplyCompoundedForwardSensitivity(32, 92, 0.2252, 302);
    final ForwardSensitivity sensi23 = new SimplyCompoundedForwardSensitivity(0.18, 48, 0.58, 18);
    forwardCurveSensitivities2.put(name3, Arrays.asList(sensi21, sensi22));
    forwardCurveSensitivities2.put(name4, Arrays.asList(sensi23));
    final MulticurveSensitivity sensitivities2 = MulticurveSensitivity.of(yieldCurveSensitivities2, forwardCurveSensitivities2);
    final Map<String, List<DoublesPair>> priceCurveSensitivities2 = new HashMap<>();
    priceCurveSensitivities1.put("PC3", Arrays.asList(DoublesPair.of(1.21, 2.31), DoublesPair.of(3.41, 4.51)));
    priceCurveSensitivities1.put("PC4", Arrays.asList(DoublesPair.of(9.81, 8.71), DoublesPair.of(5.41, 3.21), DoublesPair.of(2.331, 3.441)));
    final InflationSensitivity is2 = InflationSensitivity.of(sensitivities2, priceCurveSensitivities2);
    MultipleCurrencyInflationSensitivity sensitivities = new MultipleCurrencyInflationSensitivity();
    sensitivities = sensitivities.plus(Currency.AUD, is1);
    sensitivities = sensitivities.plus(Currency.CAD, is2);
    assertEquals(sensitivities, cycleObject(MultipleCurrencyInflationSensitivity.class, sensitivities));
  }
}
