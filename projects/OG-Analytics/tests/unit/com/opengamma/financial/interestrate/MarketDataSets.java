/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.instrument.index.iborindex.Euribor3M;
import com.opengamma.financial.instrument.index.iborindex.Euribor6M;
import com.opengamma.financial.instrument.index.iborindex.UsdLibor3M;
import com.opengamma.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;

/**
 * Sets of market data used in tests.
 */
public class MarketDataSets {
  private static final Calendar CALENDAR_EUR = new MondayToFridayCalendar("EUR");
  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD");
  private static final MarketBundle MARKET_1 = new MarketBundle();
  private static final YieldAndDiscountCurve CURVE_50 = new YieldCurve(ConstantDoublesCurve.from(0.0500));
  private static final YieldAndDiscountCurve CURVE_45 = new YieldCurve(ConstantDoublesCurve.from(0.0450));
  private static final YieldAndDiscountCurve CURVE_40 = new YieldCurve(ConstantDoublesCurve.from(0.0400));
  private static final YieldAndDiscountCurve CURVE_35 = new YieldCurve(ConstantDoublesCurve.from(0.0350));
  private static final YieldAndDiscountCurve CURVE_30 = new YieldCurve(ConstantDoublesCurve.from(0.0300));
  private static final IborIndex EURIBOR_3M = new Euribor3M(CALENDAR_EUR);
  private static final IborIndex EURIBOR_6M = new Euribor6M(CALENDAR_EUR);
  private static final IborIndex USDLIBOR_3M = new UsdLibor3M(CALENDAR_USD);
  private static final String NAME_EUR_PRICE_INDEX = "Euro HICP x";
  private static final PriceIndex PRICE_INDEX_EUR = new PriceIndex(NAME_EUR_PRICE_INDEX, Currency.EUR, Currency.EUR);
  private static final double[] INDEX_VALUE = new double[] {113.11, 113.10, 115.12, 123.23, 133.33, 155.55}; // May11, June11, 1Y, 5Y, 10Y, 20Y
  private static final double[] TIME_VALUE = new double[] {-3.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 4.0 + 9.0 / 12.0, 9.0 + 9.0 / 12.0, 19.0 + 9.0 / 12.0};
  private static final InterpolatedDoublesCurve CURVE = InterpolatedDoublesCurve.from(TIME_VALUE, INDEX_VALUE, new LinearInterpolator1D());
  private static final PriceIndexCurve PRICE_INDEX_CURVE = new PriceIndexCurve(CURVE);
  static {
    MARKET_1.setCurve(Currency.EUR, CURVE_40);
    MARKET_1.setCurve(Currency.USD, CURVE_30);
    MARKET_1.setCurve(EURIBOR_3M, CURVE_45);
    MARKET_1.setCurve(EURIBOR_6M, CURVE_50);
    MARKET_1.setCurve(USDLIBOR_3M, CURVE_35);
    MARKET_1.setCurve(PRICE_INDEX_EUR, PRICE_INDEX_CURVE);
  }

  /**
   * Returns a market with two currencies (EUR, USD), three Ibor indexes (Euribor3M, Euribor6M, UsdLibor3M) and one inflation (Euro HICP x).
   * @return The market.
   */
  public static MarketBundle createMarket1() {
    return MARKET_1;
  }

}
