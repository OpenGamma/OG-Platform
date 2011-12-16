/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.market;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.IndexPrice;
import com.opengamma.financial.instrument.index.iborindex.EURIBOR3M;
import com.opengamma.financial.instrument.index.iborindex.EURIBOR6M;
import com.opengamma.financial.instrument.index.iborindex.USDLIBOR3M;
import com.opengamma.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;

public class MarketBundleTest {

  private static final Calendar CALENDAR_EUR = new MondayToFridayCalendar("EUR");
  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD");
  private static final MarketBundle MARKET_1 = new MarketBundle();
  private static final YieldAndDiscountCurve CURVE_50 = new YieldCurve(ConstantDoublesCurve.from(0.0500));
  private static final YieldAndDiscountCurve CURVE_45 = new YieldCurve(ConstantDoublesCurve.from(0.0450));
  private static final YieldAndDiscountCurve CURVE_40 = new YieldCurve(ConstantDoublesCurve.from(0.0400));
  private static final YieldAndDiscountCurve CURVE_35 = new YieldCurve(ConstantDoublesCurve.from(0.0350));
  private static final YieldAndDiscountCurve CURVE_30 = new YieldCurve(ConstantDoublesCurve.from(0.0300));
  private static final IborIndex EURIBOR_3M = new EURIBOR3M(CALENDAR_EUR);
  private static final IborIndex EURIBOR_6M = new EURIBOR6M(CALENDAR_EUR);
  private static final IborIndex USDLIBOR_3M = new USDLIBOR3M(CALENDAR_USD);
  private static final String NAME_EUR_PRICE_INDEX = "Euro HICP x";
  private static final Period LAG = Period.ofDays(14);
  private static final IndexPrice PRICE_INDEX_EUR = new IndexPrice(NAME_EUR_PRICE_INDEX, Currency.EUR, Currency.EUR, LAG);
  private static double[] INDEX_VALUE = new double[] {108.23, 108.64, 111.0, 119.0, 129.0, 149.0};
  private static double[] TIME_VALUE = new double[] {-3.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 4.0 + 9.0 / 12.0, 9.0 + 9.0 / 12.0, 19.0 + 9.0 / 12.0};
  private static final InterpolatedDoublesCurve CURVE = InterpolatedDoublesCurve.from(TIME_VALUE, INDEX_VALUE, new LinearInterpolator1D());
  private static final PriceIndexCurve PRICE_INDEX_CURVE = new PriceIndexCurve(CURVE);

  @Test
  public void setterGetter() {
    MARKET_1.setCurve(Currency.EUR, CURVE_40);
    MARKET_1.setCurve(Currency.USD, CURVE_30);
    assertEquals("Market: getter", CURVE_40, MARKET_1.getCurve(Currency.EUR));
    assertEquals("Market: getter", CURVE_30, MARKET_1.getCurve(Currency.USD));
    MARKET_1.setCurve(EURIBOR_3M, CURVE_45);
    MARKET_1.setCurve(EURIBOR_6M, CURVE_50);
    MARKET_1.setCurve(USDLIBOR_3M, CURVE_35);
    assertEquals("Market: getter", CURVE_45, MARKET_1.getCurve(EURIBOR_3M));
    assertEquals("Market: getter", CURVE_50, MARKET_1.getCurve(EURIBOR_6M));
    assertEquals("Market: getter", CURVE_35, MARKET_1.getCurve(USDLIBOR_3M));
    MARKET_1.setCurve(PRICE_INDEX_EUR, PRICE_INDEX_CURVE);
    assertEquals("Market: getter", PRICE_INDEX_CURVE, MARKET_1.getCurve(PRICE_INDEX_EUR));
  }

}
