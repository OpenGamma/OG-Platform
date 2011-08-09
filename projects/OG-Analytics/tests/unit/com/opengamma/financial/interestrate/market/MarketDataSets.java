/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.market;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

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
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Sets of market data used in tests.
 */
public class MarketDataSets {
  private static final Calendar CALENDAR_EUR = new MondayToFridayCalendar("EUR");
  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD");
  //  private static final Calendar CALENDAR_GBP = new MondayToFridayCalendar("GBP");
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
  private static final Period LAG_EUR = Period.ofDays(14);
  private static final PriceIndex PRICE_INDEX_EUR = new PriceIndex(NAME_EUR_PRICE_INDEX, Currency.EUR, Currency.EUR, LAG_EUR);
  private static final double[] INDEX_VALUE_EUR = new double[] {113.11, 113.10, 115.12, 123.23, 133.33, 155.55}; // May11, June11, 1Y, 5Y, 10Y, 20Y
  private static final double[] TIME_VALUE_EUR = new double[] {-3.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 4.0 + 9.0 / 12.0, 9.0 + 9.0 / 12.0, 19.0 + 9.0 / 12.0};
  private static final InterpolatedDoublesCurve CURVE_EUR = InterpolatedDoublesCurve.from(TIME_VALUE_EUR, INDEX_VALUE_EUR, new LinearInterpolator1D());
  private static final PriceIndexCurve PRICE_INDEX_CURVE_EUR = new PriceIndexCurve(CURVE_EUR);
  private static final String NAME_GBP_PRICE_INDEX = "UK RPI";
  private static final Period LAG_GBP = Period.ofDays(14);
  private static final PriceIndex PRICE_INDEX_GBP = new PriceIndex(NAME_GBP_PRICE_INDEX, Currency.GBP, Currency.GBP, LAG_GBP);
  private static final double[] INDEX_VALUE_GBP = new double[] {228.4, 232.0, 240.0, 251.1, 275.2, 456.7}; // Dec10, 1Y, 5Y, 10Y, 20Y, 50Y
  private static final double[] TIME_VALUE_GBP = new double[] {-8.0 / 12.0, 4.0 / 12.0, 4.0 + 4.0 / 12.0, 9.0 + 4.0 / 12.0, 19.0 + 4.0 / 12.0, 49.0 + 4.0 / 12.0};
  private static final InterpolatedDoublesCurve CURVE_GBP = InterpolatedDoublesCurve.from(TIME_VALUE_GBP, INDEX_VALUE_GBP, new LinearInterpolator1D());
  private static final PriceIndexCurve PRICE_INDEX_CURVE_GBP = new PriceIndexCurve(CURVE_GBP);
  private static final String ISSUER_UK_GOVT = "UK GOVT";
  static {
    MARKET_1.setCurve(Currency.EUR, CURVE_40);
    MARKET_1.setCurve(Currency.USD, CURVE_30);
    MARKET_1.setCurve(Currency.GBP, CURVE_35);
    MARKET_1.setCurve(EURIBOR_3M, CURVE_45);
    MARKET_1.setCurve(EURIBOR_6M, CURVE_50);
    MARKET_1.setCurve(USDLIBOR_3M, CURVE_35);
    MARKET_1.setCurve(PRICE_INDEX_EUR, PRICE_INDEX_CURVE_EUR);
    MARKET_1.setCurve(PRICE_INDEX_GBP, PRICE_INDEX_CURVE_GBP);
    MARKET_1.setCurve(ISSUER_UK_GOVT, CURVE_40);
  }
  // Price index data
  private static final double[] RPI_VALUE = new double[] {217.9, 219.2, 220.7, 222.8, 223.6, 224.1, 223.6, 224.5, 225.3, 225.8, 226.8, 228.4, 229, 231.3, 232.5, 234.4, 235.2, 235.2};
  private static final ZonedDateTime[] RPI_DATE = new ZonedDateTime[] {DateUtil.getUTCDate(2010, 1, 1), DateUtil.getUTCDate(2010, 2, 1), DateUtil.getUTCDate(2010, 3, 1),
      DateUtil.getUTCDate(2010, 4, 1), DateUtil.getUTCDate(2010, 5, 1), DateUtil.getUTCDate(2010, 6, 1), DateUtil.getUTCDate(2010, 7, 1), DateUtil.getUTCDate(2010, 8, 1),
      DateUtil.getUTCDate(2010, 9, 1), DateUtil.getUTCDate(2010, 10, 1), DateUtil.getUTCDate(2010, 11, 1), DateUtil.getUTCDate(2010, 12, 1), DateUtil.getUTCDate(2011, 1, 1),
      DateUtil.getUTCDate(2011, 2, 1), DateUtil.getUTCDate(2011, 3, 1), DateUtil.getUTCDate(2011, 4, 1), DateUtil.getUTCDate(2011, 5, 1), DateUtil.getUTCDate(2011, 6, 1)};
  private static final ArrayZonedDateTimeDoubleTimeSeries RPI_TIME_SERIES = new ArrayZonedDateTimeDoubleTimeSeries(RPI_DATE, RPI_VALUE);

  /**
   * Returns a market with two currencies (EUR, USD), three Ibor indexes (Euribor3M, Euribor6M, UsdLibor3M) and one inflation (Euro HICP x).
   * @return The market.
   */
  public static MarketBundle createMarket1() {
    return MARKET_1;
  }

  /**
   * Returns the UK RPI time series (2010-2011).
   * @return The time series.
   */
  public static DoubleTimeSeries<ZonedDateTime> ukRpiFrom2010() {
    return RPI_TIME_SERIES;
  }

}
