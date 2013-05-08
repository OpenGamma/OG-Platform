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
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

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
}
