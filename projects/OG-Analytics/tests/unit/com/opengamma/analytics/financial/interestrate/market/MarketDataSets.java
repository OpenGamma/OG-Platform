/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.analytics.financial.interestrate.market.MarketBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.SeasonalCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.MultiplyCurveSpreadFunction;
import com.opengamma.analytics.math.curve.SpreadDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
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
  private static final YieldAndDiscountCurve CURVE_EUR_50 = new YieldCurve(ConstantDoublesCurve.from(0.0500, "EUR 5.00"));
  private static final YieldAndDiscountCurve CURVE_EUR_45 = new YieldCurve(ConstantDoublesCurve.from(0.0450, "EUR 4.50"));
  private static final YieldAndDiscountCurve CURVE_EUR_40 = new YieldCurve(ConstantDoublesCurve.from(0.0400, "EUR 4.00"));
  private static final YieldAndDiscountCurve CURVE_GBP_35 = new YieldCurve(ConstantDoublesCurve.from(0.0350, "GBP 3.50"));
  private static final YieldAndDiscountCurve CURVE_GBP_30 = new YieldCurve(ConstantDoublesCurve.from(0.0400, "GBP 3.00"));
  private static final YieldAndDiscountCurve CURVE_USD_35 = new YieldCurve(ConstantDoublesCurve.from(0.0350, "USD 3.50"));
  private static final YieldAndDiscountCurve CURVE_USD_30 = new YieldCurve(ConstantDoublesCurve.from(0.0300, "USD 3.00"));
  private static final IborIndex EURIBOR_3M = IndexIborTestsMaster.getInstance().getIndex("EURIBOR3M", CALENDAR_EUR);
  private static final IborIndex EURIBOR_6M = IndexIborTestsMaster.getInstance().getIndex("EURIBOR6M", CALENDAR_EUR);
  private static final IborIndex USDLIBOR_3M = IndexIborTestsMaster.getInstance().getIndex("USDLIBOR3M", CALENDAR_USD);

  private static final String NAME_EUR_PRICE_INDEX = "Euro HICP x";
  private static final Period LAG_EUR = Period.ofDays(14);
  private static final IndexPrice PRICE_INDEX_EUR = new IndexPrice(NAME_EUR_PRICE_INDEX, Currency.EUR, Currency.EUR, LAG_EUR);
  private static final double[] INDEX_VALUE_EUR = new double[] {113.11, 113.10, 115.12, 123.23, 133.33, 155.55}; // May11, June11, 1Y, 5Y, 10Y, 20Y
  private static final double[] TIME_VALUE_EUR = new double[] {-3.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 4.0 + 9.0 / 12.0, 9.0 + 9.0 / 12.0, 19.0 + 9.0 / 12.0};
  private static final InterpolatedDoublesCurve CURVE_EUR = InterpolatedDoublesCurve.from(TIME_VALUE_EUR, INDEX_VALUE_EUR, new LinearInterpolator1D(), NAME_EUR_PRICE_INDEX);
  private static final PriceIndexCurve PRICE_INDEX_CURVE_EUR = new PriceIndexCurve(CURVE_EUR);

  private static final String NAME_GBP_PRICE_INDEX = "UK RPI";
  private static final Period LAG_GBP = Period.ofDays(14);
  private static final IndexPrice PRICE_INDEX_GBP = new IndexPrice(NAME_GBP_PRICE_INDEX, Currency.GBP, Currency.GBP, LAG_GBP);
  private static final double[] INDEX_VALUE_GBP = new double[] {228.4, 232.0, 240.0, 251.1, 275.2, 456.7}; // Dec10, 1Y, 5Y, 10Y, 20Y, 50Y
  private static final double[] TIME_VALUE_GBP = new double[] {-8.0 / 12.0, 4.0 / 12.0, 4.0 + 4.0 / 12.0, 9.0 + 4.0 / 12.0, 19.0 + 4.0 / 12.0, 49.0 + 4.0 / 12.0};
  private static final InterpolatedDoublesCurve CURVE_GBP = InterpolatedDoublesCurve.from(TIME_VALUE_GBP, INDEX_VALUE_GBP, new LinearInterpolator1D(), NAME_GBP_PRICE_INDEX);
  private static final PriceIndexCurve PRICE_INDEX_CURVE_GBP = new PriceIndexCurve(CURVE_GBP);

  private static final String NAME_USD_PRICE_INDEX = "US CPI-U";
  private static final Period LAG_USD = Period.ofDays(14);
  private static final IndexPrice PRICE_INDEX_USD = new IndexPrice(NAME_USD_PRICE_INDEX, Currency.USD, Currency.USD, LAG_USD);
  private static final double[] INDEX_VALUE_USD = new double[] {225.964, 225.722, 230.0, 251.1, 280.2, 452.7}; // May11, June11, 1Y, 5Y, 10Y, 20Y, 50Y
  private static final double[] TIME_VALUE_USD = new double[] {-8.0 / 12.0, 4.0 / 12.0, 4.0 + 4.0 / 12.0, 9.0 + 4.0 / 12.0, 19.0 + 4.0 / 12.0, 49.0 + 4.0 / 12.0};
  private static final InterpolatedDoublesCurve CURVE_USD = InterpolatedDoublesCurve.from(TIME_VALUE_USD, INDEX_VALUE_USD, new LinearInterpolator1D(), NAME_USD_PRICE_INDEX);
  private static final PriceIndexCurve PRICE_INDEX_CURVE_USD = new PriceIndexCurve(CURVE_USD);
  private static final int MONTH_LAG_US = 3;
  private static final int SPOT_LAG_US = 1;
  private static final BusinessDayConvention BUSINESS_DAY_USD = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final String ISSUER_UK_GOVT = "UK GOVT";
  private static final String ISSUER_US_GOVT = "US GOVT";
  static {
    MARKET_1.setCurve(Currency.EUR, CURVE_EUR_40);
    MARKET_1.setCurve(Currency.USD, CURVE_USD_30);
    MARKET_1.setCurve(Currency.GBP, CURVE_GBP_35);
    MARKET_1.setCurve(EURIBOR_3M, CURVE_EUR_45);
    MARKET_1.setCurve(EURIBOR_6M, CURVE_EUR_50);
    MARKET_1.setCurve(USDLIBOR_3M, CURVE_USD_35);
    MARKET_1.setCurve(PRICE_INDEX_EUR, PRICE_INDEX_CURVE_EUR);
    MARKET_1.setCurve(PRICE_INDEX_GBP, PRICE_INDEX_CURVE_GBP);
    MARKET_1.setCurve(PRICE_INDEX_USD, PRICE_INDEX_CURVE_USD);
    MARKET_1.setCurve(ISSUER_UK_GOVT, CURVE_GBP_30);
    MARKET_1.setCurve(ISSUER_US_GOVT, CURVE_USD_30);
  }
  // Seasonal factors (from February/January to December/November)
  //  private static final double[] SEASONAL_FACTOR_EUR = new double[] {1.0010, 1.0010, 1.0020, 0.9990, 0.9990, 0.9990, 0.9990, 1.0000, 1.0010, 1.0010, 1.0010};
  private static final double[] SEASONAL_FACTOR_USD = new double[] {1.0010, 1.0010, 1.0020, 0.9990, 0.9990, 0.9990, 0.9990, 1.0000, 1.0010, 1.0010, 1.0010};
  //  private static final double[] SEASONAL_FACTOR_GBP = new double[] {1.0010, 1.0010, 1.0020, 0.9990, 0.9990, 0.9990, 0.9990, 1.0000, 1.0010, 1.0010, 1.0010};
  // Price index data
  private static final double[] UKRPI_VALUE = new double[] {217.9, 219.2, 220.7, 222.8, 223.6, 224.1, 223.6, 224.5, 225.3, 225.8, 226.8, 228.4, 229, 231.3, 232.5, 234.4, 235.2, 235.2};
  private static final ZonedDateTime[] UKRPI_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1),
      DateUtils.getUTCDate(2010, 4, 1), DateUtils.getUTCDate(2010, 5, 1), DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 1),
      DateUtils.getUTCDate(2010, 9, 1), DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1), DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2011, 1, 1),
      DateUtils.getUTCDate(2011, 2, 1), DateUtils.getUTCDate(2011, 3, 1), DateUtils.getUTCDate(2011, 4, 1), DateUtils.getUTCDate(2011, 5, 1), DateUtils.getUTCDate(2011, 6, 1)};
  private static final ArrayZonedDateTimeDoubleTimeSeries UKRPI_TIME_SERIES = new ArrayZonedDateTimeDoubleTimeSeries(UKRPI_DATE, UKRPI_VALUE);
  // US : CPI-U 2009-2011
  private static final double[] USCPI_VALUE_2009 = new double[] {211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949, 214.537};
  private static final double[] USCPI_VALUE_2010 = new double[] {216.687, 216.741, 217.631, 218.009, 218.178, 217.965, 218.011, 218.312, 218.439, 218.711, 218.803, 219.179, 218.056};
  private static final double[] USCPI_VALUE_2011 = new double[] {220.223, 221.309, 223.467, 224.906, 225.964, 225.722};
  private static final double[] USCPI_VALUE = new double[2 * 12 + USCPI_VALUE_2011.length];
  static {
    System.arraycopy(USCPI_VALUE_2009, 0, USCPI_VALUE, 0, 12);
    System.arraycopy(USCPI_VALUE_2010, 0, USCPI_VALUE, 12, 12);
    System.arraycopy(USCPI_VALUE_2011, 0, USCPI_VALUE, 24, USCPI_VALUE_2011.length);
  }
  private static final ZonedDateTime[] USCPI_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2009, 1, 1), DateUtils.getUTCDate(2009, 2, 1), DateUtils.getUTCDate(2009, 3, 1),
      DateUtils.getUTCDate(2009, 4, 1), DateUtils.getUTCDate(2009, 5, 1), DateUtils.getUTCDate(2009, 6, 1), DateUtils.getUTCDate(2009, 7, 1), DateUtils.getUTCDate(2009, 8, 1),
      DateUtils.getUTCDate(2009, 9, 1), DateUtils.getUTCDate(2009, 10, 1), DateUtils.getUTCDate(2009, 11, 1), DateUtils.getUTCDate(2009, 12, 1), DateUtils.getUTCDate(2010, 1, 1),
      DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1), DateUtils.getUTCDate(2010, 5, 1), DateUtils.getUTCDate(2010, 6, 1),
      DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 1), DateUtils.getUTCDate(2010, 9, 1), DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1),
      DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2011, 1, 1), DateUtils.getUTCDate(2011, 2, 1), DateUtils.getUTCDate(2011, 3, 1), DateUtils.getUTCDate(2011, 4, 1),
      DateUtils.getUTCDate(2011, 5, 1), DateUtils.getUTCDate(2011, 6, 1)};
  private static final ArrayZonedDateTimeDoubleTimeSeries USCPI_TIME_SERIES = new ArrayZonedDateTimeDoubleTimeSeries(USCPI_DATE, USCPI_VALUE);

  // Europe : EURO HICP-X 2009-2011
  private static final double[] EUROHICPX_VALUE_2008 = new double[] {105.80, 106.17, 107.21, 107.55, 108.23, 108.64, 108.47, 108.32, 108.52, 108.55, 108.02, 107.88};
  private static final double[] EUROHICPX_VALUE_2009 = new double[] {106.98, 107.42, 107.82, 108.21, 108.27, 108.48, 107.77, 108.14, 108.16, 108.41, 108.54, 108.88};
  private static final double[] EUROHICPX_VALUE_2010 = new double[] {107.99, 108.33, 109.53, 109.98, 110.10, 110.10, 109.63, 109.85, 110.19, 110.52, 110.62, 111.29};
  private static final double[] EUROHICPX_VALUE_2011 = new double[] {110.50, 110.96, 112.47, 113.10, 113.11, 113.10};
  private static final double[] EUROHICPX_VALUE = new double[3 * 12 + USCPI_VALUE_2011.length];
  static {
    System.arraycopy(EUROHICPX_VALUE_2008, 0, EUROHICPX_VALUE, 0, 12);
    System.arraycopy(EUROHICPX_VALUE_2009, 0, EUROHICPX_VALUE, 12, 12);
    System.arraycopy(EUROHICPX_VALUE_2010, 0, EUROHICPX_VALUE, 24, 12);
    System.arraycopy(EUROHICPX_VALUE_2011, 0, EUROHICPX_VALUE, 36, USCPI_VALUE_2011.length);
  }
  private static final ZonedDateTime[] EUROHICPX_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2008, 1, 1), DateUtils.getUTCDate(2008, 2, 1), DateUtils.getUTCDate(2008, 3, 1),
      DateUtils.getUTCDate(2008, 4, 1), DateUtils.getUTCDate(2008, 5, 1), DateUtils.getUTCDate(2008, 6, 1), DateUtils.getUTCDate(2008, 7, 1), DateUtils.getUTCDate(2008, 8, 1),
      DateUtils.getUTCDate(2008, 9, 1), DateUtils.getUTCDate(2008, 10, 1), DateUtils.getUTCDate(2008, 11, 1), DateUtils.getUTCDate(2008, 12, 1), DateUtils.getUTCDate(2009, 1, 1),
      DateUtils.getUTCDate(2009, 2, 1), DateUtils.getUTCDate(2009, 3, 1), DateUtils.getUTCDate(2009, 4, 1), DateUtils.getUTCDate(2009, 5, 1), DateUtils.getUTCDate(2009, 6, 1),
      DateUtils.getUTCDate(2009, 7, 1), DateUtils.getUTCDate(2009, 8, 1), DateUtils.getUTCDate(2009, 9, 1), DateUtils.getUTCDate(2009, 10, 1), DateUtils.getUTCDate(2009, 11, 1),
      DateUtils.getUTCDate(2009, 12, 1), DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 2, 1), DateUtils.getUTCDate(2010, 3, 1), DateUtils.getUTCDate(2010, 4, 1),
      DateUtils.getUTCDate(2010, 5, 1), DateUtils.getUTCDate(2010, 6, 1), DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 1), DateUtils.getUTCDate(2010, 9, 1),
      DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1), DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2011, 1, 1), DateUtils.getUTCDate(2011, 2, 1),
      DateUtils.getUTCDate(2011, 3, 1), DateUtils.getUTCDate(2011, 4, 1), DateUtils.getUTCDate(2011, 5, 1), DateUtils.getUTCDate(2011, 6, 1)};
  private static final ArrayZonedDateTimeDoubleTimeSeries EUROHICPX_TIME_SERIES = new ArrayZonedDateTimeDoubleTimeSeries(EUROHICPX_DATE, EUROHICPX_VALUE);

  /**
   * Returns a market with three currencies (EUR, USD, GBP), three Ibor indexes (Euribor3M, Euribor6M, UsdLibor3M) and three inflation (Euro HICP x, UK RPI and US CPI-U).
   * @return The market.
   */
  public static MarketBundle createMarket1() {
    return MARKET_1;
  }

  /**
   * Creates a market with three currencies (EUR, USD, GBP), three Ibor indexes (Euribor3M, Euribor6M, UsdLibor3M) and three inflation (Euro HICP x, UK RPI and US CPI-U).
   * The US CPI-U price curve is constructed to have the correct past data (if available in the time series) and a fake 2% inflation for the future. 
   * No seasonal adjustment is done.
   * @param pricingDate The data for which the curve is constructed.
   * @return The market.
   */
  public static MarketBundle createMarket1(ZonedDateTime pricingDate) {
    MarketBundle market = new MarketBundle();
    market.setCurve(Currency.EUR, CURVE_EUR_40);
    market.setCurve(Currency.USD, CURVE_USD_30);
    market.setCurve(Currency.GBP, CURVE_GBP_35);
    market.setCurve(EURIBOR_3M, CURVE_EUR_45);
    market.setCurve(EURIBOR_6M, CURVE_EUR_50);
    market.setCurve(USDLIBOR_3M, CURVE_USD_35);
    market.setCurve(PRICE_INDEX_EUR, PRICE_INDEX_CURVE_EUR);
    market.setCurve(PRICE_INDEX_GBP, PRICE_INDEX_CURVE_GBP);
    market.setCurve(ISSUER_UK_GOVT, CURVE_GBP_30);
    market.setCurve(ISSUER_US_GOVT, CURVE_USD_30);
    ZonedDateTime spotUs = ScheduleCalculator.getAdjustedDate(pricingDate, SPOT_LAG_US, CALENDAR_USD);
    ZonedDateTime referenceInterpolatedDate = spotUs.minusMonths(MONTH_LAG_US);
    ZonedDateTime[] referenceDate = new ZonedDateTime[2];
    referenceDate[0] = referenceInterpolatedDate.withDayOfMonth(1);
    referenceDate[1] = referenceDate[0].plusMonths(1);
    int[] yearUs = new int[] {1, 5, 10, 20, 50};
    double[] indexValueUs = new double[2 + 2 * yearUs.length];
    double[] timeValueUs = new double[2 + 2 * yearUs.length];
    indexValueUs[0] = USCPI_TIME_SERIES.getValue(referenceDate[0]);
    indexValueUs[1] = USCPI_TIME_SERIES.getValue(referenceDate[1]);
    timeValueUs[0] = TimeCalculator.getTimeBetween(pricingDate, referenceDate[0]);
    timeValueUs[1] = TimeCalculator.getTimeBetween(pricingDate, referenceDate[1]);
    ZonedDateTime[] maturityDateUs = new ZonedDateTime[2 * yearUs.length];
    //    double[] maturityTimeUs = new double[yearUs.length];
    for (int loopus = 0; loopus < yearUs.length; loopus++) {
      maturityDateUs[2 * loopus] = ScheduleCalculator.getAdjustedDate(referenceDate[0], Period.ofYears(yearUs[loopus]), BUSINESS_DAY_USD, CALENDAR_USD);
      maturityDateUs[2 * loopus + 1] = ScheduleCalculator.getAdjustedDate(referenceDate[1], Period.ofYears(yearUs[loopus]), BUSINESS_DAY_USD, CALENDAR_USD);
      timeValueUs[2 + 2 * loopus] = TimeCalculator.getTimeBetween(pricingDate, maturityDateUs[2 * loopus]);
      timeValueUs[2 + 2 * loopus + 1] = TimeCalculator.getTimeBetween(pricingDate, maturityDateUs[2 * loopus + 1]);
      indexValueUs[2 + 2 * loopus] = indexValueUs[0] * Math.pow(1 + 0.02, yearUs[loopus]); // 2% inflation a year.
      indexValueUs[2 + 2 * loopus + 1] = indexValueUs[1] * Math.pow(1 + 0.02, yearUs[loopus]); // 2% inflation a year.
    }
    InterpolatedDoublesCurve curveUs = InterpolatedDoublesCurve.from(timeValueUs, indexValueUs, new LinearInterpolator1D(), NAME_USD_PRICE_INDEX);
    PriceIndexCurve priceIndexCurveUs = new PriceIndexCurve(curveUs);
    market.setCurve(PRICE_INDEX_USD, priceIndexCurveUs);
    return market;
  }

  public static MarketBundle createMarket2(ZonedDateTime pricingDate) {
    MarketBundle market = createMarket1(pricingDate);
    Curve<Double, Double> curveNoAdj = market.getCurve(PRICE_INDEX_USD).getCurve();
    Curve<Double, Double> adj = new SeasonalCurve(curveNoAdj.getXData()[0], SEASONAL_FACTOR_USD);
    @SuppressWarnings("unchecked")
    Curve<Double, Double>[] curveSet = new Curve[] {curveNoAdj, adj};
    Curve<Double, Double> curveAdj = new SpreadDoublesCurve(new MultiplyCurveSpreadFunction(), curveSet);
    market.replaceCurve(PRICE_INDEX_USD, new PriceIndexCurve(curveAdj));
    return market;
  }

  /**
   * Returns the UK RPI time series (2010-2011).
   * @return The time series.
   */
  public static DoubleTimeSeries<ZonedDateTime> ukRpiFrom2010() {
    return UKRPI_TIME_SERIES;
  }

  /**
   * Returns the US CPI-U time series (2009-2011).
   * @return The time series.
   */
  public static DoubleTimeSeries<ZonedDateTime> usCpiFrom2009() {
    return USCPI_TIME_SERIES;
  }

  /**
   * Returns the EURO HICP-X time series (2009-2011).
   * @return The time series.
   */
  public static DoubleTimeSeries<ZonedDateTime> euroHICPXFrom2009() {
    return EUROHICPX_TIME_SERIES;
  }

  public static IndexPrice[] getPriceIndexes() {
    return new IndexPrice[] {PRICE_INDEX_EUR, PRICE_INDEX_GBP, PRICE_INDEX_USD};
  }

  public static IborIndex[] getIborIndexes() {
    return new IborIndex[] {EURIBOR_3M, EURIBOR_6M, USDLIBOR_3M};
  }

}
