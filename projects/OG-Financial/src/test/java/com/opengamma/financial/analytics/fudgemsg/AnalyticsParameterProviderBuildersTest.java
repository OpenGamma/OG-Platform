/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityRegion;
import com.opengamma.analytics.financial.legalentity.LegalEntitySector;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class AnalyticsParameterProviderBuildersTest extends AnalyticsTestBase {

  @Test
  public void testIborIndex() {
    final IborIndex index = new IborIndex(Currency.USD, Period.ofMonths(3), 0, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false, "F");
    assertEquals(index, cycleObject(IborIndex.class, index));
  }

  @Test
  public void testOvernightIndex() {
    final IndexON index = new IndexON("ON", Currency.USD, DayCounts.ACT_365, 1);
    assertEquals(index, cycleObject(IndexON.class, index));
  }

  @Test
  public void testPriceIndex() {
    final IndexPrice index = new IndexPrice("ABC", Currency.ITL);
    assertEquals(index, cycleObject(IndexPrice.class, index));
  }

  @Test
  public void testFXMatrix() {
    final Map<Currency, Integer> map = new LinkedHashMap<>();
    final Currency[] currencies = new Currency[] {Currency.AUD, Currency.CAD, Currency.CHF, Currency.FRF, Currency.DEM, Currency.USD, Currency.GBP, Currency.EUR, Currency.HKD, Currency.DKK};
    final double[][] fxRates = new double[10][10];
    for (int i = 0; i < 10; i++) {
      map.put(currencies[i], i);
      for (int j = 0; j < 10; j++) {
        fxRates[i][j] = Math.random();
      }
    }
    final FXMatrix matrix = new FXMatrix(map, fxRates);
    assertEquals(matrix, cycleObject(FXMatrix.class, matrix));
  }

  @Test
  public void testMulticurveProviderDiscount() {
    final Map<Currency, Integer> map = new LinkedHashMap<>();
    final Currency[] currencies = new Currency[] {Currency.AUD, Currency.CAD, Currency.CHF, Currency.FRF, Currency.DEM, Currency.USD, Currency.GBP, Currency.EUR, Currency.HKD, Currency.DKK};
    final double[][] fxRates = new double[10][10];
    for (int i = 0; i < 10; i++) {
      map.put(currencies[i], i);
      for (int j = 0; j < 10; j++) {
        fxRates[i][j] = Math.random();
      }
    }
    final FXMatrix matrix = new FXMatrix(map, fxRates);
    final Map<Currency, YieldAndDiscountCurve> discounting = new LinkedHashMap<>();
    discounting.put(Currency.USD, new YieldCurve("A", ConstantDoublesCurve.from(0.06, "a")));
    discounting.put(Currency.EUR, new DiscountCurve("B", ConstantDoublesCurve.from(0.99, "b")));
    final Map<IborIndex, YieldAndDiscountCurve> ibor = new LinkedHashMap<>();
    ibor.put(new IborIndex(Currency.USD, Period.ofMonths(3), 0, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false, "F"),
        new YieldCurve("C", ConstantDoublesCurve.from(0.03, "c")));
    ibor.put(new IborIndex(Currency.EUR, Period.ofMonths(6), 1, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false, "D"),
        new YieldCurve("D", ConstantDoublesCurve.from(0.03, "d")));
    final Map<IndexON, YieldAndDiscountCurve> overnight = new LinkedHashMap<>();
    overnight.put(new IndexON("NAME1", Currency.USD, DayCounts.ACT_360, 1), new YieldCurve("E", ConstantDoublesCurve.from(0.003, "e")));
    overnight.put(new IndexON("NAME2", Currency.EUR, DayCounts.ACT_360, 0), new YieldCurve("F", ConstantDoublesCurve.from(0.006, "f")));
    final MulticurveProviderDiscount provider = new MulticurveProviderDiscount(discounting, ibor, overnight, matrix);
    assertEquals(provider, cycleObject(MulticurveProviderDiscount.class, provider));
  }

  @Test
  public void testMulticurveProviderForward() {
    final Map<Currency, Integer> map = new LinkedHashMap<>();
    final Currency[] currencies = new Currency[] {Currency.AUD, Currency.CAD, Currency.CHF, Currency.FRF, Currency.DEM, Currency.USD, Currency.GBP, Currency.EUR, Currency.HKD, Currency.DKK};
    final double[][] fxRates = new double[10][10];
    for (int i = 0; i < 10; i++) {
      map.put(currencies[i], i);
      for (int j = 0; j < 10; j++) {
        fxRates[i][j] = Math.random();
      }
    }
    final FXMatrix matrix = new FXMatrix(map, fxRates);
    final Map<Currency, YieldAndDiscountCurve> discounting = new LinkedHashMap<>();
    discounting.put(Currency.USD, new YieldCurve("A", ConstantDoublesCurve.from(0.06, "a")));
    discounting.put(Currency.EUR, new DiscountCurve("B", ConstantDoublesCurve.from(0.99, "b")));
    final Map<IborIndex, DoublesCurve> ibor = new LinkedHashMap<>();
    ibor.put(new IborIndex(Currency.USD, Period.ofMonths(3), 0, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false, "F"),
        ConstantDoublesCurve.from(0.03, "c"));
    ibor.put(new IborIndex(Currency.EUR, Period.ofMonths(6), 1, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false, "D"),
        ConstantDoublesCurve.from(0.03, "d"));
    final Map<IndexON, YieldAndDiscountCurve> overnight = new LinkedHashMap<>();
    overnight.put(new IndexON("NAME1", Currency.USD, DayCounts.ACT_360, 1), new YieldCurve("E", ConstantDoublesCurve.from(0.003, "e")));
    overnight.put(new IndexON("NAME2", Currency.EUR, DayCounts.ACT_360, 0), new YieldCurve("F", ConstantDoublesCurve.from(0.006, "f")));
    final MulticurveProviderForward provider = new MulticurveProviderForward(discounting, ibor, overnight, matrix);
    assertEquals(provider, cycleObject(MulticurveProviderForward.class, provider));
  }

  @Test
  public void testInflationProviderDiscount() {
    final Map<Currency, Integer> map = new LinkedHashMap<>();
    final Currency[] currencies = new Currency[] {Currency.AUD, Currency.CAD, Currency.CHF, Currency.FRF, Currency.DEM, Currency.USD, Currency.GBP, Currency.EUR, Currency.HKD, Currency.DKK};
    final double[][] fxRates = new double[10][10];
    for (int i = 0; i < 10; i++) {
      map.put(currencies[i], i);
      for (int j = 0; j < 10; j++) {
        fxRates[i][j] = Math.random();
      }
    }
    final FXMatrix matrix = new FXMatrix(map, fxRates);
    final Map<Currency, YieldAndDiscountCurve> discounting = new LinkedHashMap<>();
    discounting.put(Currency.USD, new YieldCurve("A", ConstantDoublesCurve.from(0.06, "a")));
    discounting.put(Currency.EUR, new DiscountCurve("B", ConstantDoublesCurve.from(0.99, "b")));
    final Map<IborIndex, YieldAndDiscountCurve> ibor = new LinkedHashMap<>();
    ibor.put(new IborIndex(Currency.USD, Period.ofMonths(3), 0, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false, "L"),
        new YieldCurve("C", ConstantDoublesCurve.from(0.03, "c")));
    ibor.put(new IborIndex(Currency.EUR, Period.ofMonths(6), 1, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false, "P"),
        new YieldCurve("D", ConstantDoublesCurve.from(0.03, "d")));
    final Map<IndexON, YieldAndDiscountCurve> overnight = new LinkedHashMap<>();
    overnight.put(new IndexON("NAME1", Currency.USD, DayCounts.ACT_360, 1), new YieldCurve("E", ConstantDoublesCurve.from(0.003, "e")));
    overnight.put(new IndexON("NAME2", Currency.EUR, DayCounts.ACT_360, 0), new YieldCurve("F", ConstantDoublesCurve.from(0.006, "f")));
    final MulticurveProviderDiscount provider = new MulticurveProviderDiscount(discounting, ibor, overnight, matrix);
    final Map<IndexPrice, PriceIndexCurveSimple> curves = new LinkedHashMap<>();
    curves.put(new IndexPrice("CPI1", Currency.USD), new PriceIndexCurveSimple(ConstantDoublesCurve.from(0.02, "A")));
    curves.put(new IndexPrice("CPI2", Currency.EUR), new PriceIndexCurveSimple(ConstantDoublesCurve.from(0.03, "B")));
    final InflationProviderDiscount inflation = new InflationProviderDiscount(provider, curves);
    assertEquals(inflation, cycleObject(InflationProviderDiscount.class, inflation));
  }

  @Test
  public void testIssuerProviderDiscount() {
    final Map<Currency, Integer> map = new LinkedHashMap<>();
    final Currency[] currencies = new Currency[] {Currency.AUD, Currency.CAD, Currency.CHF, Currency.FRF, Currency.DEM, Currency.USD, Currency.GBP, Currency.EUR, Currency.HKD, Currency.DKK};
    final double[][] fxRates = new double[10][10];
    for (int i = 0; i < 10; i++) {
      map.put(currencies[i], i);
      for (int j = 0; j < 10; j++) {
        fxRates[i][j] = Math.random();
      }
    }
    final FXMatrix matrix = new FXMatrix(map, fxRates);
    final Map<Currency, YieldAndDiscountCurve> discounting = new LinkedHashMap<>();
    discounting.put(Currency.USD, new YieldCurve("A", ConstantDoublesCurve.from(0.06, "a")));
    discounting.put(Currency.EUR, new DiscountCurve("B", ConstantDoublesCurve.from(0.99, "b")));
    final Map<IborIndex, YieldAndDiscountCurve> ibor = new LinkedHashMap<>();
    ibor.put(new IborIndex(Currency.USD, Period.ofMonths(3), 0, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false, "L"),
        new YieldCurve("C", ConstantDoublesCurve.from(0.03, "c")));
    ibor.put(new IborIndex(Currency.EUR, Period.ofMonths(6), 1, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false, "P"),
        new YieldCurve("D", ConstantDoublesCurve.from(0.03, "d")));
    final Map<IndexON, YieldAndDiscountCurve> overnight = new LinkedHashMap<>();
    overnight.put(new IndexON("NAME1", Currency.USD, DayCounts.ACT_360, 1), new YieldCurve("E", ConstantDoublesCurve.from(0.003, "e")));
    overnight.put(new IndexON("NAME2", Currency.EUR, DayCounts.ACT_360, 0), new YieldCurve("F", ConstantDoublesCurve.from(0.006, "f")));
    final MulticurveProviderDiscount provider = new MulticurveProviderDiscount(discounting, ibor, overnight, matrix);
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> curves = new HashMap<>();
    curves.put(Pairs.<Object, LegalEntityFilter<LegalEntity>>of(Collections.singleton(Currency.USD), new LegalEntityRegion(true, false, Collections.<Country>emptySet(), true, Collections.singleton(Currency.USD))), new YieldCurve("L", ConstantDoublesCurve.from(0.1234, "l")));
    final Set<String> classifications = new HashSet<>();
    classifications.add("C");
    classifications.add("E");
    curves.put(Pairs.<Object, LegalEntityFilter<LegalEntity>>of("A", new LegalEntitySector(true, false, classifications)), new YieldCurve("P", ConstantDoublesCurve.from(0.1234, "p")));
    final IssuerProviderDiscount issuer = new IssuerProviderDiscount(provider, curves);
    assertEquals(issuer, cycleObject(IssuerProviderDiscount.class, issuer));
  }

  @Test
  public void testCurveBuildingBlock() {
    final LinkedHashMap<String, Pair<Integer, Integer>> data = new LinkedHashMap<>();
    data.put("A", Pairs.of(Integer.valueOf(3), Integer.valueOf(4)));
    data.put("B", Pairs.of(Integer.valueOf(6), Integer.valueOf(8)));
    data.put("C", Pairs.of(Integer.valueOf(34), Integer.valueOf(536)));
    final CurveBuildingBlock block = new CurveBuildingBlock(data);
    assertEquals(block, cycleObject(CurveBuildingBlock.class, block));
  }

  @Test
  public void testCurveBuildingBlockBundle() {
    final LinkedHashMap<String, Pair<Integer, Integer>> data1 = new LinkedHashMap<>();
    data1.put("A", Pairs.of(Integer.valueOf(3), Integer.valueOf(4)));
    data1.put("B", Pairs.of(Integer.valueOf(6), Integer.valueOf(8)));
    data1.put("C", Pairs.of(Integer.valueOf(34), Integer.valueOf(536)));
    final CurveBuildingBlock block1 = new CurveBuildingBlock(data1);
    final LinkedHashMap<String, Pair<Integer, Integer>> data2 = new LinkedHashMap<>();
    data2.put("A", Pairs.of(Integer.valueOf(13), Integer.valueOf(14)));
    data2.put("B", Pairs.of(Integer.valueOf(16), Integer.valueOf(18)));
    data2.put("C", Pairs.of(Integer.valueOf(134), Integer.valueOf(1536)));
    final CurveBuildingBlock block2 = new CurveBuildingBlock(data2);
    final LinkedHashMap<String, Pair<Integer, Integer>> data3 = new LinkedHashMap<>();
    data3.put("A", Pairs.of(Integer.valueOf(23), Integer.valueOf(24)));
    data3.put("B", Pairs.of(Integer.valueOf(26), Integer.valueOf(28)));
    data3.put("C", Pairs.of(Integer.valueOf(234), Integer.valueOf(2536)));
    final CurveBuildingBlock block3 = new CurveBuildingBlock(data3);
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> data = new LinkedHashMap<>();
    data.put("Q", Pairs.of(block1, new DoubleMatrix2D(new double[][] {new double[] {2, 4}, new double[] {5, 6}})));
    data.put("W", Pairs.of(block2, new DoubleMatrix2D(new double[][] {new double[] {12, 14}, new double[] {15, 16}})));
    data.put("E", Pairs.of(block3, new DoubleMatrix2D(new double[][] {new double[] {22, 24}, new double[] {25, 26}})));
    final CurveBuildingBlockBundle bundle = new CurveBuildingBlockBundle(data);
    assertEquals(bundle, cycleObject(CurveBuildingBlockBundle.class, bundle));
  }

  @Test
  public void testHullWhiteParameters() {
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = new HullWhiteOneFactorPiecewiseConstantParameters(0.04,
        new double[] {0.1, 0.2, 0.3, 0.4, 0.5}, new double[] {1, 2, 3, 4, 5});
    assertEquals(parameters, cycleObject(HullWhiteOneFactorPiecewiseConstantParameters.class, parameters));
  }

  @Test
  public void testG2ppParameters() {
    final G2ppPiecewiseConstantParameters parameters = new G2ppPiecewiseConstantParameters(new double[] {0.02, 0.01},
        new double[][] {new double[] {0.03, 0.04, 0.05}, new double[] {0.06, 0.07, 0.08}}, new double[] {1, 2}, 0.9);
    assertEquals(parameters, cycleObject(G2ppPiecewiseConstantParameters.class, parameters));
  }
  @Test
  public void testHullWhiteOneFactorProviderDiscount() {
    final Map<Currency, Integer> map = new LinkedHashMap<>();
    final Currency[] currencies = new Currency[] {Currency.AUD, Currency.CAD, Currency.CHF, Currency.FRF, Currency.DEM, Currency.USD, Currency.GBP, Currency.EUR, Currency.HKD, Currency.DKK};
    final double[][] fxRates = new double[10][10];
    for (int i = 0; i < 10; i++) {
      map.put(currencies[i], i);
      for (int j = 0; j < 10; j++) {
        fxRates[i][j] = Math.random();
      }
    }
    final FXMatrix matrix = new FXMatrix(map, fxRates);
    final Map<Currency, YieldAndDiscountCurve> discounting = new LinkedHashMap<>();
    discounting.put(Currency.USD, new YieldCurve("A", ConstantDoublesCurve.from(0.06, "a")));
    discounting.put(Currency.EUR, new DiscountCurve("B", ConstantDoublesCurve.from(0.99, "b")));
    final Map<IborIndex, YieldAndDiscountCurve> ibor = new LinkedHashMap<>();
    ibor.put(new IborIndex(Currency.USD, Period.ofMonths(3), 0, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false, "T"),
        new YieldCurve("C", ConstantDoublesCurve.from(0.03, "c")));
    ibor.put(new IborIndex(Currency.EUR, Period.ofMonths(6), 1, DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, false, "U"),
        new YieldCurve("D", ConstantDoublesCurve.from(0.03, "d")));
    final Map<IndexON, YieldAndDiscountCurve> overnight = new LinkedHashMap<>();
    overnight.put(new IndexON("NAME1", Currency.USD, DayCounts.ACT_360, 1), new YieldCurve("E", ConstantDoublesCurve.from(0.003, "e")));
    overnight.put(new IndexON("NAME2", Currency.EUR, DayCounts.ACT_360, 0), new YieldCurve("F", ConstantDoublesCurve.from(0.006, "f")));
    final MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount(discounting, ibor, overnight, matrix);
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = new HullWhiteOneFactorPiecewiseConstantParameters(0.04, new double[] {0.1, 0.2, 0.3, 0.4, 0.5}, new double[] {1, 2, 3, 4, 5});
    final HullWhiteOneFactorProviderDiscount provider = new HullWhiteOneFactorProviderDiscount(multicurve, parameters, Currency.USD);
    assertEquals(provider, cycleObject(HullWhiteOneFactorProviderDiscount.class, provider));
  }
}
