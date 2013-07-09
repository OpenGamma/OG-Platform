/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class AnalyticsParameterProviderBuildersTest extends AnalyticsTestBase {

  @Test
  public void testIborIndex() {
    final IborIndex index = new IborIndex(Currency.USD, Period.ofMonths(3), 0, DayCountFactory.INSTANCE.getDayCount("Act/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false);
    assertEquals(index, cycleObject(IborIndex.class, index));
  }

  @Test
  public void testOvernightIndex() {
    final IndexON index = new IndexON("ON", Currency.USD, DayCountFactory.INSTANCE.getDayCount("Act/365"), 1);
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
    ibor.put(new IborIndex(Currency.USD, Period.ofMonths(3), 0, DayCountFactory.INSTANCE.getDayCount("Act/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false),
        new YieldCurve("C", ConstantDoublesCurve.from(0.03, "c")));
    ibor.put(new IborIndex(Currency.EUR, Period.ofMonths(6), 1, DayCountFactory.INSTANCE.getDayCount("Act/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false),
        new YieldCurve("D", ConstantDoublesCurve.from(0.03, "d")));
    final Map<IndexON, YieldAndDiscountCurve> overnight = new LinkedHashMap<>();
    overnight.put(new IndexON("NAME1", Currency.USD, DayCountFactory.INSTANCE.getDayCount("Act/360"), 1), new YieldCurve("E", ConstantDoublesCurve.from(0.003, "e")));
    overnight.put(new IndexON("NAME2", Currency.EUR, DayCountFactory.INSTANCE.getDayCount("Act/360"), 0), new YieldCurve("F", ConstantDoublesCurve.from(0.006, "f")));
    final MulticurveProviderDiscount provider = new MulticurveProviderDiscount(discounting, ibor, overnight, matrix);
    assertEquals(provider, cycleObject(MulticurveProviderDiscount.class, provider));
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
    ibor.put(new IborIndex(Currency.USD, Period.ofMonths(3), 0, DayCountFactory.INSTANCE.getDayCount("Act/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false),
        new YieldCurve("C", ConstantDoublesCurve.from(0.03, "c")));
    ibor.put(new IborIndex(Currency.EUR, Period.ofMonths(6), 1, DayCountFactory.INSTANCE.getDayCount("Act/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false),
        new YieldCurve("D", ConstantDoublesCurve.from(0.03, "d")));
    final Map<IndexON, YieldAndDiscountCurve> overnight = new LinkedHashMap<>();
    overnight.put(new IndexON("NAME1", Currency.USD, DayCountFactory.INSTANCE.getDayCount("Act/360"), 1), new YieldCurve("E", ConstantDoublesCurve.from(0.003, "e")));
    overnight.put(new IndexON("NAME2", Currency.EUR, DayCountFactory.INSTANCE.getDayCount("Act/360"), 0), new YieldCurve("F", ConstantDoublesCurve.from(0.006, "f")));
    final MulticurveProviderDiscount provider = new MulticurveProviderDiscount(discounting, ibor, overnight, matrix);
    final Map<IndexPrice, PriceIndexCurve> curves = new LinkedHashMap<>();
    curves.put(new IndexPrice("CPI1", Currency.USD), new PriceIndexCurve(ConstantDoublesCurve.from(0.02, "A")));
    curves.put(new IndexPrice("CPI2", Currency.EUR), new PriceIndexCurve(ConstantDoublesCurve.from(0.03, "B")));
    final InflationProviderDiscount inflation = new InflationProviderDiscount(provider, curves);
    assertEquals(inflation, cycleObject(InflationProviderDiscount.class, inflation));
  }

  @Test
  public void testCurveBuildingBlock() {
    final LinkedHashMap<String, Pair<Integer, Integer>> data = new LinkedHashMap<>();
    data.put("A", Pair.of(Integer.valueOf(3), Integer.valueOf(4)));
    data.put("B", Pair.of(Integer.valueOf(6), Integer.valueOf(8)));
    data.put("C", Pair.of(Integer.valueOf(34), Integer.valueOf(536)));
    final CurveBuildingBlock block = new CurveBuildingBlock(data);
    assertEquals(block, cycleObject(CurveBuildingBlock.class, block));
  }

  @Test
  public void testCurveBuildingBlockBundle() {
    final LinkedHashMap<String, Pair<Integer, Integer>> data1 = new LinkedHashMap<>();
    data1.put("A", Pair.of(Integer.valueOf(3), Integer.valueOf(4)));
    data1.put("B", Pair.of(Integer.valueOf(6), Integer.valueOf(8)));
    data1.put("C", Pair.of(Integer.valueOf(34), Integer.valueOf(536)));
    final CurveBuildingBlock block1 = new CurveBuildingBlock(data1);
    final LinkedHashMap<String, Pair<Integer, Integer>> data2 = new LinkedHashMap<>();
    data2.put("A", Pair.of(Integer.valueOf(13), Integer.valueOf(14)));
    data2.put("B", Pair.of(Integer.valueOf(16), Integer.valueOf(18)));
    data2.put("C", Pair.of(Integer.valueOf(134), Integer.valueOf(1536)));
    final CurveBuildingBlock block2 = new CurveBuildingBlock(data2);
    final LinkedHashMap<String, Pair<Integer, Integer>> data3 = new LinkedHashMap<>();
    data3.put("A", Pair.of(Integer.valueOf(23), Integer.valueOf(24)));
    data3.put("B", Pair.of(Integer.valueOf(26), Integer.valueOf(28)));
    data3.put("C", Pair.of(Integer.valueOf(234), Integer.valueOf(2536)));
    final CurveBuildingBlock block3 = new CurveBuildingBlock(data3);
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> data = new LinkedHashMap<>();
    data.put("Q", Pair.of(block1, new DoubleMatrix2D(new double[][] {new double[] {2, 4}, new double[] {5, 6}})));
    data.put("W", Pair.of(block2, new DoubleMatrix2D(new double[][] {new double[] {12, 14}, new double[] {15, 16}})));
    data.put("E", Pair.of(block3, new DoubleMatrix2D(new double[][] {new double[] {22, 24}, new double[] {25, 26}})));
    final CurveBuildingBlockBundle bundle = new CurveBuildingBlockBundle(data);
    assertEquals(bundle, cycleObject(CurveBuildingBlockBundle.class, bundle));
  }
}
