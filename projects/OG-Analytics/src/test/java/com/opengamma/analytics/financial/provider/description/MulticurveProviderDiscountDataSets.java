/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import java.util.LinkedHashMap;
import java.util.Map;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Sets of market data used in tests.
 */
public class MulticurveProviderDiscountDataSets {

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD");
  private static final Calendar CALENDAR_EUR = new MondayToFridayCalendar("EUR");
  private static final Calendar CALENDAR_CAD = new MondayToFridayCalendar("CAD");
  private static final Calendar CALENDAR_AUD = new MondayToFridayCalendar("AUD");

  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.GBP, Currency.USD, 1.60);
  static {
    FX_MATRIX.addCurrency(Currency.EUR, Currency.USD, 1.40);
  }

  private static final double[] USD_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] USD_DSC_RATE = new double[] {0.0100, 0.0120, 0.0120, 0.0140, 0.0140, 0.0140 };
  private static final String USD_DSC_NAME = "USD Dsc";
  private static final YieldAndDiscountCurve USD_DSC = new YieldCurve(USD_DSC_NAME, new InterpolatedDoublesCurve(USD_DSC_TIME, USD_DSC_RATE, LINEAR_FLAT, true, USD_DSC_NAME));
  private static final double[] USD_FWD3_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] USD_FWD3_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0150, 0.0150 };
  private static final String USD_FWD3_NAME = "USD LIBOR 3M";
  private static final YieldAndDiscountCurve USD_FWD3 = new YieldCurve(USD_FWD3_NAME, new InterpolatedDoublesCurve(USD_FWD3_TIME, USD_FWD3_RATE, LINEAR_FLAT, true, USD_FWD3_NAME));
  private static final double[] USD_FWD6_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] USD_FWD6_RATE = new double[] {0.0175, 0.0150, 0.0170, 0.0190, 0.0165, 0.0165 };
  private static final String USD_FWD6_NAME = "USD LIBOR 6M";
  private static final YieldAndDiscountCurve USD_FWD6 = new YieldCurve(USD_FWD6_NAME, new InterpolatedDoublesCurve(USD_FWD6_TIME, USD_FWD6_RATE, LINEAR_FLAT, true, USD_FWD6_NAME));

  private static final double[] EUR_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] EUR_DSC_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0150, 0.0150 };
  private static final String EUR_DSC_NAME = "EUR Dsc";
  private static final YieldAndDiscountCurve EUR_DSC = new YieldCurve(EUR_DSC_NAME, new InterpolatedDoublesCurve(EUR_DSC_TIME, EUR_DSC_RATE, LINEAR_FLAT, true, EUR_DSC_NAME));
  private static final double[] EUR_FWD3_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 10.0 };
  private static final double[] EUR_FWD3_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0175, 0.0190, 0.0200, 0.0210 };
  private static final String EUR_FWD3_NAME = "EUR EURIBOR 3M";
  private static final YieldAndDiscountCurve EUR_FWD3 = new YieldCurve(EUR_FWD3_NAME, new InterpolatedDoublesCurve(EUR_FWD3_TIME, EUR_FWD3_RATE, LINEAR_FLAT, true, EUR_FWD3_NAME));
  private static final double[] EUR_FWD6_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] EUR_FWD6_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0150, 0.0150 };
  private static final String EUR_FWD6_NAME = "EUR EURIBOR 6M";
  private static final YieldAndDiscountCurve EUR_FWD6 = new YieldCurve(EUR_FWD6_NAME, new InterpolatedDoublesCurve(EUR_FWD6_TIME, EUR_FWD6_RATE, LINEAR_FLAT, true, EUR_FWD6_NAME));

  private static final double[] CAD_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] CAD_DSC_RATE = new double[] {0.0100, 0.0120, 0.0120, 0.0140, 0.0140, 0.0140 };
  private static final String CAD_DSC_NAME = "CAD Dsc";
  private static final YieldAndDiscountCurve CAD_DSC = new YieldCurve(CAD_DSC_NAME, new InterpolatedDoublesCurve(CAD_DSC_TIME, CAD_DSC_RATE, LINEAR_FLAT, true, CAD_DSC_NAME));
  private static final double[] CAD_FWD3_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] CAD_FWD3_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0150, 0.0150 };
  private static final String CAD_FWD3_NAME = "CAD CDOR3M 3M";
  private static final YieldAndDiscountCurve CAD_FWD3 = new YieldCurve(CAD_FWD3_NAME, new InterpolatedDoublesCurve(CAD_FWD3_TIME, CAD_FWD3_RATE, LINEAR_FLAT, true, CAD_FWD3_NAME));

  private static final double[] AUD_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] AUD_DSC_RATE = new double[] {0.0100, 0.0120, 0.0120, 0.0140, 0.0140, 0.0140 };
  private static final String AUD_DSC_NAME = "AUD Dsc";
  private static final YieldAndDiscountCurve AUD_DSC = new YieldCurve(AUD_DSC_NAME, new InterpolatedDoublesCurve(AUD_DSC_TIME, AUD_DSC_RATE, LINEAR_FLAT, true, AUD_DSC_NAME));
  private static final double[] AUD_FWD3_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] AUD_FWD3_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0150, 0.0150 };
  private static final String AUD_FWD3_NAME = "AUD AUDBB3M 3M";
  private static final YieldAndDiscountCurve AUD_FWD3 = new YieldCurve(AUD_FWD3_NAME, new InterpolatedDoublesCurve(AUD_FWD3_TIME, AUD_FWD3_RATE, LINEAR_FLAT, true, AUD_FWD3_NAME));
  private static final double[] AUD_FWD6_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] AUD_FWD6_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0150, 0.0150 };
  private static final String AUD_FWD6_NAME = "AUD AUDBB6M 6M";
  private static final YieldAndDiscountCurve AUD_FWD6 = new YieldCurve(AUD_FWD6_NAME, new InterpolatedDoublesCurve(AUD_FWD6_TIME, AUD_FWD6_RATE, LINEAR_FLAT, true, AUD_FWD6_NAME));

  private static final double[] GBP_SINGLE_TIME = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068,
    0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9,
    10, 12, 15, 20, 25, 30, 35, 40, 45, 50 };
  private static final double[] GBP_SINGLE_RATE = new double[] {0.00466247, 0.004799779, 0.004933739, 0.005211567, 0.005421216,
    0.006076053, 0.007009252, 0.008085954, 0.010315153, 0.012462716, 0.017006001, 0.019770703, 0.02182399, 0.023484499, 0.024924994, 0.026172219, 0.027254055,
    0.028183251, 0.029699473, 0.031347025, 0.032871429, 0.033250028, 0.033227865, 0.032909084, 0.032620029, 0.032487606, 0.032366873 };
  private static final String GBP_SINGLE_NAME = "GBP Single curve";
  private static final YieldAndDiscountCurve GBP_SINGLE = new YieldCurve(GBP_SINGLE_NAME, new InterpolatedDoublesCurve(GBP_SINGLE_TIME, GBP_SINGLE_RATE, LINEAR_FLAT, true, GBP_SINGLE_NAME));

  private static final String ISSUER_NAME = "Corporate";
  private static final double[] EUR_ISSUER_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] EUR_ISSUER_RATE = new double[] {0.0250, 0.0225, 0.0250, 0.0275, 0.0250, 0.0250 };
  private static final String EUR_ISSUER_NAME = "EUR " + ISSUER_NAME;
  private static final YieldAndDiscountCurve EUR_ISSUER = new YieldCurve(EUR_ISSUER_NAME, new InterpolatedDoublesCurve(EUR_ISSUER_TIME, EUR_ISSUER_RATE, LINEAR_FLAT, true, EUR_ISSUER_NAME));

  private static final String GBP35 = "GBP 3.50";
  private static final double[] GBP35_TIME = new double[] {0.0, 10.0 };
  private static final double[] GBP35_RATE = new double[] {0.0350, 0.0350 };
  private static final YieldAndDiscountCurve CURVE_GBP_35 = new YieldCurve(GBP35, new InterpolatedDoublesCurve(GBP35_TIME, GBP35_RATE, LINEAR_FLAT, true, GBP35));

  private static final String GBP30 = "GBP 3.00";
  private static final double[] GBP30_TIME = new double[] {0.0, 10.0 };
  private static final double[] GBP30_RATE = new double[] {0.030, 0.030 };
  private static final YieldAndDiscountCurve CURVE_GBP_30 = new YieldCurve(GBP30, new InterpolatedDoublesCurve(GBP30_TIME, GBP30_RATE, LINEAR_FLAT, true, GBP30));

  private static final String USD30 = "USD 3.00";
  private static final double[] USD30_TIME = new double[] {0.0, 10.0 };
  private static final double[] USD30_RATE = new double[] {0.030, 0.030 };
  private static final YieldAndDiscountCurve CURVE_USD_30 = new YieldCurve(USD30, new InterpolatedDoublesCurve(USD30_TIME, USD30_RATE, LINEAR_FLAT, true, USD30));

  private static final String AUD30 = "GBP 3.00";
  private static final double[] AUD30_TIME = new double[] {0.0, 10.0 };
  private static final double[] AUD30_RATE = new double[] {0.030, 0.030 };
  private static final YieldAndDiscountCurve CURVE_AUD_30 = new YieldCurve(AUD30, new InterpolatedDoublesCurve(AUD30_TIME, AUD30_RATE, LINEAR_FLAT, true, AUD30));

  private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR3M = MASTER_IBOR_INDEX.getIndex("USDLIBOR3M");
  private static final IborIndex USDLIBOR6M = MASTER_IBOR_INDEX.getIndex("USDLIBOR6M");
  private static final IborIndex EURIBOR3M = MASTER_IBOR_INDEX.getIndex("EURIBOR3M");
  private static final IborIndex EURIBOR6M = MASTER_IBOR_INDEX.getIndex("EURIBOR6M");
  private static final IborIndex GBPLIBOR3M = MASTER_IBOR_INDEX.getIndex("GBPLIBOR3M");
  private static final IborIndex GBPLIBOR6M = MASTER_IBOR_INDEX.getIndex("GBPLIBOR6M");
  private static final IborIndex CADCDOR3M = MASTER_IBOR_INDEX.getIndex("CADCDOR3M");
  private static final IborIndex AUDBB3M = MASTER_IBOR_INDEX.getIndex("AUDBB3M");
  private static final IborIndex AUDBB6M = MASTER_IBOR_INDEX.getIndex("AUDBB6M");
  private static final IndexON EONIA = IndexONMaster.getInstance().getIndex("EONIA");
  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final IndexON SONIA = IndexONMaster.getInstance().getIndex("SONIA");
  private static final IndexON BRAZIL_CDI = IndexONMaster.getInstance().getIndex("CDI");

  private static final String NAME_EUR_PRICE_INDEX = "EUR HICP";
  private static final IndexPrice PRICE_INDEX_EUR = new IndexPrice(NAME_EUR_PRICE_INDEX, Currency.EUR);
  private static final double[] INDEX_VALUE_EUR = new double[] {113.11, 113.10, 115.12, 123.23, 133.33, 155.55, 175.55, 195.55 }; // May11, June11, 1Y, 5Y, 10Y, 20Y
  private static final double[] TIME_VALUE_EUR = new double[] {-4.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 4.0 + 9.0 / 12.0, 9.0 + 9.0 / 12.0, 19.0 + 9.0 / 12.0, 29.0 + 9.0 / 12.0, 39.0 + 9.0 / 12.0 };
  private static final InterpolatedDoublesCurve CURVE_EUR = InterpolatedDoublesCurve.from(TIME_VALUE_EUR, INDEX_VALUE_EUR, new LinearInterpolator1D(), NAME_EUR_PRICE_INDEX);
  private static final PriceIndexCurve PRICE_INDEX_CURVE_EUR = new PriceIndexCurve(CURVE_EUR);

  private static final String NAME_GBP_PRICE_INDEX = "UK RPI";
  private static final IndexPrice PRICE_INDEX_GBP = new IndexPrice(NAME_GBP_PRICE_INDEX, Currency.GBP);
  private static final double[] INDEX_VALUE_GBP = new double[] {228.4, 232.0, 240.0, 251.1, 275.2, 456.7 }; // Dec10, 1Y, 5Y, 10Y, 20Y, 50Y
  private static final double[] TIME_VALUE_GBP = new double[] {-8.0 / 12.0, 4.0 / 12.0, 4.0 + 4.0 / 12.0, 9.0 + 4.0 / 12.0, 19.0 + 4.0 / 12.0, 49.0 + 4.0 / 12.0 };
  private static final InterpolatedDoublesCurve CURVE_GBP = InterpolatedDoublesCurve.from(TIME_VALUE_GBP, INDEX_VALUE_GBP, new LinearInterpolator1D(), NAME_GBP_PRICE_INDEX);
  private static final PriceIndexCurve PRICE_INDEX_CURVE_GBP = new PriceIndexCurve(CURVE_GBP);

  private static final String NAME_USD_PRICE_INDEX = "US CPI-U";
  private static final IndexPrice PRICE_INDEX_USD = new IndexPrice(NAME_USD_PRICE_INDEX, Currency.EUR);
  private static final double[] INDEX_VALUE_USD = new double[] {225.964, 225.722, 230.0, 251.1, 280.2, 452.7 }; // May11, June11, 1Y, 5Y, 10Y, 20Y, 50Y
  private static final double[] TIME_VALUE_USD = new double[] {-8.0 / 12.0, 4.0 / 12.0, 4.0 + 4.0 / 12.0, 9.0 + 4.0 / 12.0, 19.0 + 4.0 / 12.0, 49.0 + 4.0 / 12.0 };
  private static final InterpolatedDoublesCurve CURVE_USD = InterpolatedDoublesCurve.from(TIME_VALUE_USD, INDEX_VALUE_USD, new LinearInterpolator1D(), NAME_USD_PRICE_INDEX);
  private static final PriceIndexCurve PRICE_INDEX_CURVE_USD = new PriceIndexCurve(CURVE_USD);
  private static final int MONTH_LAG_US = 3;
  private static final int SPOT_LAG_US = 1;
  private static final BusinessDayConvention BUSINESS_DAY_USD = BusinessDayConventions.FOLLOWING;

  private static final String NAME_AUD_PRICE_INDEX = "AUD CPI";
  private static final IndexPrice PRICE_INDEX_AUD = new IndexPrice(NAME_AUD_PRICE_INDEX, Currency.AUD);
  private static final double[] INDEX_VALUE_AUD = new double[] {113.10, 115.12, 123.23, 133.33, 155.55, 175.55, 195.55 }; // May11, June11, 1Y, 5Y, 10Y, 20Y
  private static final double[] TIME_VALUE_AUD = new double[] {-3.0 / 12.0, 9.0 / 12.0, 4.0 + 9.0 / 12.0, 9.0 + 9.0 / 12.0, 19.0 + 9.0 / 12.0, 29.0 + 9.0 / 12.0, 39.0 + 9.0 / 12.0 };
  private static final InterpolatedDoublesCurve CURVE_AUD = InterpolatedDoublesCurve.from(TIME_VALUE_AUD, INDEX_VALUE_AUD, new LinearInterpolator1D(), NAME_AUD_PRICE_INDEX);
  private static final PriceIndexCurve PRICE_INDEX_CURVE_AUD = new PriceIndexCurve(CURVE_AUD);

  private static final LegalEntityFilter<LegalEntity> META = new LegalEntityShortName();
  private static final Pair<Object, LegalEntityFilter<LegalEntity>> ISSUER_UK_GOVT = Pairs.of((Object) "UK GOVT", META);
  private static final Pair<Object, LegalEntityFilter<LegalEntity>> ISSUER_US_GOVT = Pairs.of((Object) "US GOVT", META);
  private static final Pair<Object, LegalEntityFilter<LegalEntity>> ISSUER_AUD_GOVT = Pairs.of((Object) "AUD GOVT", META);

  private static final InflationIssuerProviderDiscount MARKET_1 = new InflationIssuerProviderDiscount();
  static {
    MARKET_1.setCurve(Currency.AUD, AUD_DSC);
    MARKET_1.setCurve(AUDBB3M, AUD_FWD3);
    MARKET_1.setCurve(AUDBB6M, AUD_FWD6);
    MARKET_1.setCurve(Currency.USD, USD_DSC);
    MARKET_1.setCurve(Currency.EUR, EUR_DSC);
    MARKET_1.setCurve(Currency.GBP, CURVE_GBP_35);
    MARKET_1.setCurve(USDLIBOR3M, USD_FWD3);
    MARKET_1.setCurve(EONIA, EUR_DSC);
    MARKET_1.setCurve(EURIBOR3M, EUR_FWD3);
    MARKET_1.setCurve(EURIBOR6M, EUR_FWD6);
    MARKET_1.setCurve(PRICE_INDEX_EUR, PRICE_INDEX_CURVE_EUR);
    MARKET_1.setCurve(PRICE_INDEX_GBP, PRICE_INDEX_CURVE_GBP);
    MARKET_1.setCurve(PRICE_INDEX_USD, PRICE_INDEX_CURVE_USD);
    MARKET_1.setCurve(PRICE_INDEX_AUD, PRICE_INDEX_CURVE_AUD);
    MARKET_1.setCurve(ISSUER_UK_GOVT, CURVE_GBP_30);
    MARKET_1.setCurve(ISSUER_US_GOVT, CURVE_USD_30);
    MARKET_1.setCurve(ISSUER_AUD_GOVT, CURVE_AUD_30);
  }

  private static final MulticurveProviderDiscount SINGLECURVES_USD = new MulticurveProviderDiscount();
  static {
    SINGLECURVES_USD.setCurve(Currency.USD, USD_DSC);
    SINGLECURVES_USD.setCurve(FEDFUND, USD_DSC);
    SINGLECURVES_USD.setCurve(USDLIBOR3M, USD_DSC);
    SINGLECURVES_USD.setCurve(USDLIBOR6M, USD_DSC);
  }

  private static final MulticurveProviderDiscount MULTICURVES_EUR_USD = new MulticurveProviderDiscount();
  static {
    MULTICURVES_EUR_USD.setCurve(Currency.USD, USD_DSC);
    MULTICURVES_EUR_USD.setCurve(FEDFUND, USD_DSC);
    MULTICURVES_EUR_USD.setCurve(Currency.EUR, EUR_DSC);
    MULTICURVES_EUR_USD.setCurve(USDLIBOR3M, USD_FWD3);
    MULTICURVES_EUR_USD.setCurve(USDLIBOR6M, USD_FWD6);
    MULTICURVES_EUR_USD.setCurve(EONIA, EUR_DSC);
    MULTICURVES_EUR_USD.setCurve(EURIBOR3M, EUR_FWD3);
    MULTICURVES_EUR_USD.setCurve(EURIBOR6M, EUR_FWD6);
  }

  private static final MulticurveProviderDiscount MULTICURVES_GBP_USD = new MulticurveProviderDiscount();
  static {
    MULTICURVES_GBP_USD.setCurve(Currency.USD, USD_DSC);
    MULTICURVES_GBP_USD.setCurve(FEDFUND, USD_DSC);
    MULTICURVES_GBP_USD.setCurve(USDLIBOR3M, USD_FWD3);
    MULTICURVES_GBP_USD.setCurve(USDLIBOR6M, USD_FWD6);
    MULTICURVES_GBP_USD.setCurve(Currency.GBP, CURVE_GBP_30);
    MULTICURVES_GBP_USD.setCurve(SONIA, CURVE_GBP_30);
    MULTICURVES_GBP_USD.setForexMatrix(FX_MATRIX);
  }

  private static final MulticurveProviderDiscount SINGLECURVES_GBP = new MulticurveProviderDiscount();
  static {
    SINGLECURVES_GBP.setCurve(Currency.GBP, GBP_SINGLE);
    SINGLECURVES_GBP.setCurve(SONIA, GBP_SINGLE);
    SINGLECURVES_GBP.setCurve(GBPLIBOR3M, GBP_SINGLE);
    SINGLECURVES_GBP.setCurve(GBPLIBOR6M, GBP_SINGLE);
  }

  private static final MulticurveProviderDiscount MULTICURVES_USD_WITHOUT_DISCOUNT = new MulticurveProviderDiscount();

  private static final MulticurveProviderDiscount MULTICURVES_CAD = new MulticurveProviderDiscount();
  static {
    MULTICURVES_CAD.setCurve(Currency.CAD, CAD_DSC);
    MULTICURVES_CAD.setCurve(CADCDOR3M, CAD_FWD3);
  }

  private static final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> ISSUER_CURVES = new LinkedHashMap<>();
  static {
    ISSUER_CURVES.put(Pairs.of((Object) ISSUER_NAME, META), EUR_ISSUER);
  }
  private static final IssuerProviderDiscount PROVIDER_ISSUER = new IssuerProviderDiscount(MULTICURVES_EUR_USD, ISSUER_CURVES);

  // Seasonal factors (from February/January to December/November)
  //  private static final double[] SEASONAL_FACTOR_EUR = new double[] {1.0010, 1.0010, 1.0020, 0.9990, 0.9990, 0.9990, 0.9990, 1.0000, 1.0010, 1.0010, 1.0010};
  //  private static final double[] SEASONAL_FACTOR_USD = new double[] {1.0010, 1.0010, 1.0020, 0.9990, 0.9990, 0.9990, 0.9990, 1.0000, 1.0010, 1.0010, 1.0010 };
  //  private static final double[] SEASONAL_FACTOR_GBP = new double[] {1.0010, 1.0010, 1.0020, 0.9990, 0.9990, 0.9990, 0.9990, 1.0000, 1.0010, 1.0010, 1.0010};
  // Price index data
  // Implementation note : we have had the value of November 2001 for test purpose.
  private static final double[] UKRPI_VALUE_2001 = new double[] {217.9 };
  private static final double[] UKRPI_VALUE_2010 = new double[] {217.9, 219.2, 220.7, 222.8, 223.6, 224.1, 223.6, 224.5, 225.3, 225.8, 226.8, 228.4 };
  private static final double[] UKRPI_VALUE_2011 = new double[] {229, 231.3, 232.5, 234.4, 235.2, 235.2, 234.7, 236.1, 237.9, 238.0, 238.5, 239.4 };
  private static final double[] UKRPI_VALUE_2012 = new double[] {238.0, 239.9, 240.8, 242.5, 242.4, 241.8, 242.1, 243.0, 244.2, 245.6, 245.6, 246.8 };
  private static final double[] UKRPI_VALUE_2013 = new double[] {245.8, 247.6, 248.7, 249.5, 250.0, 249.7, 249.7, 251.0, 251.9, 251.9, 252.1, 253.4 };
  private static final double[] UKRPI_VALUE_2014 = new double[] {252.6, 254.2, 254.8, 255.7 };
  private static final double[] UKRPI_VALUE = new double[1 + 4 * 12 + UKRPI_VALUE_2014.length];
  static {
    System.arraycopy(UKRPI_VALUE_2001, 0, UKRPI_VALUE, 0, 1);
    System.arraycopy(UKRPI_VALUE_2010, 0, UKRPI_VALUE, 1, 12);
    System.arraycopy(UKRPI_VALUE_2011, 0, UKRPI_VALUE, 13, 12);
    System.arraycopy(UKRPI_VALUE_2012, 0, UKRPI_VALUE, 25, 12);
    System.arraycopy(UKRPI_VALUE_2013, 0, UKRPI_VALUE, 37, 12);
    System.arraycopy(UKRPI_VALUE_2014, 0, UKRPI_VALUE, 49, UKRPI_VALUE_2014.length);
  }
  private static final ZonedDateTime[] UKRPI_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2001, 11, 30),
    DateUtils.getUTCDate(2010, 1, 31), DateUtils.getUTCDate(2010, 2, 28), DateUtils.getUTCDate(2010, 3, 31),
    DateUtils.getUTCDate(2010, 4, 30), DateUtils.getUTCDate(2010, 5, 31), DateUtils.getUTCDate(2010, 6, 30),
    DateUtils.getUTCDate(2010, 7, 31), DateUtils.getUTCDate(2010, 8, 31), DateUtils.getUTCDate(2010, 9, 30),
    DateUtils.getUTCDate(2010, 10, 31), DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31),
    DateUtils.getUTCDate(2011, 1, 31), DateUtils.getUTCDate(2011, 2, 28), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31),
    DateUtils.getUTCDate(2011, 6, 30), DateUtils.getUTCDate(2011, 7, 31), DateUtils.getUTCDate(2011, 8, 31), DateUtils.getUTCDate(2011, 9, 30), DateUtils.getUTCDate(2011, 10, 30),
    DateUtils.getUTCDate(2011, 11, 30), DateUtils.getUTCDate(2011, 12, 31),
    DateUtils.getUTCDate(2012, 1, 31), DateUtils.getUTCDate(2012, 2, 29), DateUtils.getUTCDate(2012, 3, 31),
    DateUtils.getUTCDate(2012, 4, 30), DateUtils.getUTCDate(2012, 5, 31), DateUtils.getUTCDate(2012, 6, 30),
    DateUtils.getUTCDate(2012, 7, 31), DateUtils.getUTCDate(2012, 8, 31), DateUtils.getUTCDate(2012, 9, 30),
    DateUtils.getUTCDate(2012, 10, 31), DateUtils.getUTCDate(2012, 11, 30), DateUtils.getUTCDate(2012, 12, 31),
    DateUtils.getUTCDate(2013, 1, 31), DateUtils.getUTCDate(2013, 2, 28), DateUtils.getUTCDate(2013, 3, 31),
    DateUtils.getUTCDate(2013, 4, 30), DateUtils.getUTCDate(2013, 5, 31), DateUtils.getUTCDate(2013, 6, 30),
    DateUtils.getUTCDate(2013, 7, 31), DateUtils.getUTCDate(2013, 8, 31), DateUtils.getUTCDate(2013, 9, 30),
    DateUtils.getUTCDate(2013, 10, 31), DateUtils.getUTCDate(2013, 11, 30), DateUtils.getUTCDate(2013, 12, 31),
    DateUtils.getUTCDate(2014, 1, 31), DateUtils.getUTCDate(2014, 2, 28), DateUtils.getUTCDate(2014, 3, 31),
    DateUtils.getUTCDate(2014, 4, 30) };
  private static final ZonedDateTimeDoubleTimeSeries UKRPI_TIME_SERIES = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(UKRPI_DATE, UKRPI_VALUE);
  // US : CPI-U 2009-2011
  private static final double[] USCPI_VALUE_2005 = new double[] {211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949, 214.537 };
  private static final double[] USCPI_VALUE_2006 = new double[] {211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949, 214.537 };
  private static final double[] USCPI_VALUE_2007 = new double[] {211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949, 214.537 };
  private static final double[] USCPI_VALUE_2008 = new double[] {211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949, 214.537 };// TODO : put the right value for 2005, 2006, 2007, 2008
  private static final double[] USCPI_VALUE_2009 = new double[] {211.143, 212.193, 212.709, 213.240, 213.856, 215.693, 215.351, 215.834, 215.969, 216.177, 216.330, 215.949, 214.537 };
  private static final double[] USCPI_VALUE_2010 = new double[] {216.687, 216.741, 217.631, 218.009, 218.178, 217.965, 218.011, 218.312, 218.439, 218.711, 218.803, 219.179, 218.056 };
  private static final double[] USCPI_VALUE_2011 = new double[] {220.223, 221.309, 223.467, 224.906, 225.964, 225.722, 225.922, 226.545, 226.889, 226.421, 226.230, 225.672, 224.939 };
  private static final double[] USCPI_VALUE_2012 = new double[] {226.655, 227.663, 229.392, 230.085, 229.815, 229.478, 229.104, 230.379, 231.407, 231.317, 230.221, 229.601, 229.594 };
  private static final double[] USCPI_VALUE_2013 = new double[] {230.280 };
  private static final double[] USCPI_VALUE = new double[8 * 12 + USCPI_VALUE_2013.length];
  static {
    System.arraycopy(USCPI_VALUE_2005, 0, USCPI_VALUE, 0, 12);
    System.arraycopy(USCPI_VALUE_2006, 0, USCPI_VALUE, 12, 12);
    System.arraycopy(USCPI_VALUE_2007, 0, USCPI_VALUE, 24, 12);
    System.arraycopy(USCPI_VALUE_2008, 0, USCPI_VALUE, 36, 12);
    System.arraycopy(USCPI_VALUE_2009, 0, USCPI_VALUE, 48, 12);
    System.arraycopy(USCPI_VALUE_2010, 0, USCPI_VALUE, 60, 12);
    System.arraycopy(USCPI_VALUE_2011, 0, USCPI_VALUE, 72, 12);
    System.arraycopy(USCPI_VALUE_2012, 0, USCPI_VALUE, 84, 12);
    System.arraycopy(USCPI_VALUE_2013, 0, USCPI_VALUE, 96, USCPI_VALUE_2013.length);

  }
  private static final ZonedDateTime[] USCPI_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2004, 12, 31), DateUtils.getUTCDate(2005, 1, 31), DateUtils.getUTCDate(2005, 2, 28),
    DateUtils.getUTCDate(2005, 3, 31), DateUtils.getUTCDate(2005, 4, 30), DateUtils.getUTCDate(2005, 5, 31), DateUtils.getUTCDate(2005, 6, 30), DateUtils.getUTCDate(2005, 7, 31),
    DateUtils.getUTCDate(2005, 8, 31), DateUtils.getUTCDate(2005, 9, 30), DateUtils.getUTCDate(2005, 10, 31), DateUtils.getUTCDate(2005, 11, 30), DateUtils.getUTCDate(2005, 12, 31),
    DateUtils.getUTCDate(2006, 1, 31), DateUtils.getUTCDate(2006, 2, 28), DateUtils.getUTCDate(2006, 3, 31), DateUtils.getUTCDate(2006, 4, 30), DateUtils.getUTCDate(2006, 5, 31),
    DateUtils.getUTCDate(2006, 6, 30), DateUtils.getUTCDate(2006, 7, 31), DateUtils.getUTCDate(2006, 8, 31), DateUtils.getUTCDate(2006, 9, 30), DateUtils.getUTCDate(2006, 10, 31),
    DateUtils.getUTCDate(2006, 11, 30), DateUtils.getUTCDate(2006, 12, 31), DateUtils.getUTCDate(2007, 1, 31), DateUtils.getUTCDate(2007, 2, 28), DateUtils.getUTCDate(2007, 3, 31),
    DateUtils.getUTCDate(2007, 4, 30), DateUtils.getUTCDate(2007, 5, 31), DateUtils.getUTCDate(2007, 6, 30), DateUtils.getUTCDate(2007, 7, 31), DateUtils.getUTCDate(2007, 8, 31),
    DateUtils.getUTCDate(2007, 9, 30), DateUtils.getUTCDate(2007, 10, 31), DateUtils.getUTCDate(2007, 11, 30), DateUtils.getUTCDate(2007, 12, 31), DateUtils.getUTCDate(2008, 1, 31),
    DateUtils.getUTCDate(2008, 2, 28), DateUtils.getUTCDate(2008, 3, 31), DateUtils.getUTCDate(2008, 4, 30), DateUtils.getUTCDate(2008, 5, 31), DateUtils.getUTCDate(2008, 6, 30),
    DateUtils.getUTCDate(2008, 7, 31), DateUtils.getUTCDate(2008, 8, 31), DateUtils.getUTCDate(2008, 9, 30), DateUtils.getUTCDate(2008, 10, 31), DateUtils.getUTCDate(2008, 11, 30),
    DateUtils.getUTCDate(2008, 12, 31), DateUtils.getUTCDate(2009, 1, 31), DateUtils.getUTCDate(2009, 2, 28), DateUtils.getUTCDate(2009, 3, 31), DateUtils.getUTCDate(2009, 4, 30),
    DateUtils.getUTCDate(2009, 5, 31), DateUtils.getUTCDate(2009, 6, 30), DateUtils.getUTCDate(2009, 7, 31), DateUtils.getUTCDate(2009, 8, 31), DateUtils.getUTCDate(2009, 9, 30),
    DateUtils.getUTCDate(2009, 10, 31), DateUtils.getUTCDate(2009, 11, 30), DateUtils.getUTCDate(2009, 12, 31), DateUtils.getUTCDate(2010, 1, 31), DateUtils.getUTCDate(2010, 2, 28),
    DateUtils.getUTCDate(2010, 3, 31), DateUtils.getUTCDate(2010, 4, 30), DateUtils.getUTCDate(2010, 5, 31), DateUtils.getUTCDate(2010, 6, 30), DateUtils.getUTCDate(2010, 7, 31),
    DateUtils.getUTCDate(2010, 8, 31), DateUtils.getUTCDate(2010, 9, 30), DateUtils.getUTCDate(2010, 10, 31), DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31),
    DateUtils.getUTCDate(2011, 1, 31), DateUtils.getUTCDate(2011, 2, 28), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31),
    DateUtils.getUTCDate(2011, 6, 30), DateUtils.getUTCDate(2011, 7, 31), DateUtils.getUTCDate(2011, 8, 31), DateUtils.getUTCDate(2011, 9, 30), DateUtils.getUTCDate(2011, 10, 31),
    DateUtils.getUTCDate(2011, 11, 30), DateUtils.getUTCDate(2011, 12, 31), DateUtils.getUTCDate(2012, 1, 31), DateUtils.getUTCDate(2012, 2, 29), DateUtils.getUTCDate(2012, 3, 31),
    DateUtils.getUTCDate(2012, 4, 30), DateUtils.getUTCDate(2012, 5, 31), DateUtils.getUTCDate(2012, 6, 30), DateUtils.getUTCDate(2012, 7, 31), DateUtils.getUTCDate(2012, 8, 31),
    DateUtils.getUTCDate(2012, 9, 30), DateUtils.getUTCDate(2012, 10, 31), DateUtils.getUTCDate(2012, 11, 30), DateUtils.getUTCDate(2012, 12, 31) };
  private static final ZonedDateTimeDoubleTimeSeries USCPI_TIME_SERIES = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(USCPI_DATE, USCPI_VALUE);

  // Europe : EURO HICP-X 2009-2011
  private static final double[] EUROHICPX_VALUE_2008 = new double[] {105.80, 106.17, 107.21, 107.55, 108.23, 108.64, 108.47, 108.32, 108.52, 108.55, 108.02, 107.88 };
  private static final double[] EUROHICPX_VALUE_2009 = new double[] {106.98, 107.42, 107.82, 108.21, 108.27, 108.48, 107.77, 108.14, 108.16, 108.41, 108.54, 108.88 };
  private static final double[] EUROHICPX_VALUE_2010 = new double[] {107.99, 108.33, 109.53, 109.98, 110.10, 110.10, 109.63, 109.85, 110.19, 110.52, 110.62, 111.29 };
  private static final double[] EUROHICPX_VALUE_2011 = new double[] {110.49, 110.96, 112.46, 113.09, 113.09, 113.08, 112.44, 112.65, 113.47, 113.87, 113.97, 114.35 };
  private static final double[] EUROHICPX_VALUE_2012 = new double[] {113.42, 113.99, 115.46, 116.00, 115.84, 115.75, 115.15, 115.59, 116.43, 116.71, 116.47, 115.67 };
  private static final double[] EUROHICPX_VALUE_2013 = new double[] {116.09 };
  private static final double[] EUROHICPX_VALUE = new double[5 * 12 + EUROHICPX_VALUE_2013.length];
  static {
    System.arraycopy(EUROHICPX_VALUE_2008, 0, EUROHICPX_VALUE, 0, 12);
    System.arraycopy(EUROHICPX_VALUE_2009, 0, EUROHICPX_VALUE, 12, 12);
    System.arraycopy(EUROHICPX_VALUE_2010, 0, EUROHICPX_VALUE, 24, 12);
    System.arraycopy(EUROHICPX_VALUE_2011, 0, EUROHICPX_VALUE, 36, 12);
    System.arraycopy(EUROHICPX_VALUE_2012, 0, EUROHICPX_VALUE, 48, 12);
    System.arraycopy(EUROHICPX_VALUE_2013, 0, EUROHICPX_VALUE, 60, EUROHICPX_VALUE_2013.length);
  }
  private static final ZonedDateTime[] EUROHICPX_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2007, 12, 31), DateUtils.getUTCDate(2008, 1, 31), DateUtils.getUTCDate(2008, 2, 29),
    DateUtils.getUTCDate(2008, 3, 31), DateUtils.getUTCDate(2008, 4, 30), DateUtils.getUTCDate(2008, 5, 31), DateUtils.getUTCDate(2008, 6, 30), DateUtils.getUTCDate(2008, 7, 31),
    DateUtils.getUTCDate(2008, 8, 31), DateUtils.getUTCDate(2008, 9, 30), DateUtils.getUTCDate(2008, 10, 30), DateUtils.getUTCDate(2008, 11, 30), DateUtils.getUTCDate(2008, 12, 31),
    DateUtils.getUTCDate(2009, 1, 31), DateUtils.getUTCDate(2009, 2, 28), DateUtils.getUTCDate(2009, 3, 31), DateUtils.getUTCDate(2009, 4, 30), DateUtils.getUTCDate(2009, 5, 31),
    DateUtils.getUTCDate(2009, 6, 30), DateUtils.getUTCDate(2009, 7, 31), DateUtils.getUTCDate(2009, 8, 31), DateUtils.getUTCDate(2009, 9, 30), DateUtils.getUTCDate(2009, 10, 30),
    DateUtils.getUTCDate(2009, 11, 30), DateUtils.getUTCDate(2009, 12, 31), DateUtils.getUTCDate(2010, 1, 31), DateUtils.getUTCDate(2010, 2, 28), DateUtils.getUTCDate(2010, 3, 31),
    DateUtils.getUTCDate(2010, 4, 30), DateUtils.getUTCDate(2010, 5, 31), DateUtils.getUTCDate(2010, 6, 30), DateUtils.getUTCDate(2010, 7, 31), DateUtils.getUTCDate(2010, 8, 31),
    DateUtils.getUTCDate(2010, 9, 30), DateUtils.getUTCDate(2010, 10, 31), DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
    DateUtils.getUTCDate(2011, 2, 28), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31), DateUtils.getUTCDate(2011, 6, 30),
    DateUtils.getUTCDate(2011, 7, 31), DateUtils.getUTCDate(2011, 8, 31), DateUtils.getUTCDate(2011, 9, 30), DateUtils.getUTCDate(2011, 10, 31), DateUtils.getUTCDate(2011, 11, 30),
    DateUtils.getUTCDate(2011, 12, 31), DateUtils.getUTCDate(2012, 1, 31), DateUtils.getUTCDate(2012, 2, 29), DateUtils.getUTCDate(2012, 3, 31), DateUtils.getUTCDate(2012, 4, 30),
    DateUtils.getUTCDate(2012, 5, 31), DateUtils.getUTCDate(2012, 6, 30), DateUtils.getUTCDate(2012, 7, 31), DateUtils.getUTCDate(2012, 8, 31), DateUtils.getUTCDate(2012, 9, 30),
    DateUtils.getUTCDate(2012, 10, 30), DateUtils.getUTCDate(2012, 11, 30), DateUtils.getUTCDate(2012, 12, 31) };
  private static final ZonedDateTimeDoubleTimeSeries EUROHICPX_TIME_SERIES = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(EUROHICPX_DATE, EUROHICPX_VALUE);

  // Australia : AUD CPI 2009-2013
  private static final double[] AUDCPI_VALUE_2008 = new double[] {90.3, 91.6, 92.7, 92.4 };
  private static final double[] AUDCPI_VALUE_2009 = new double[] {92.5, 92.9, 93.8, 94.3 };
  private static final double[] AUDCPI_VALUE_2010 = new double[] {95.2, 95.8, 96.5, 96.9 };
  private static final double[] AUDCPI_VALUE_2011 = new double[] {98.3, 99.2, 99.8, 99.8 };
  private static final double[] AUDCPI_VALUE_2012 = new double[] {99.9, 100.4, 101.8, 102.0 };
  private static final double[] AUDCPI_VALUE_2013 = new double[] {102.4, 102.8, 104.0 };
  private static final double[] AUDCPI_VALUE = new double[5 * 4 + AUDCPI_VALUE_2013.length];
  static {
    System.arraycopy(AUDCPI_VALUE_2008, 0, AUDCPI_VALUE, 0, 4);
    System.arraycopy(AUDCPI_VALUE_2009, 0, AUDCPI_VALUE, 4, 4);
    System.arraycopy(AUDCPI_VALUE_2010, 0, AUDCPI_VALUE, 8, 4);
    System.arraycopy(AUDCPI_VALUE_2011, 0, AUDCPI_VALUE, 12, 4);
    System.arraycopy(AUDCPI_VALUE_2012, 0, AUDCPI_VALUE, 16, 4);
    System.arraycopy(AUDCPI_VALUE_2013, 0, AUDCPI_VALUE, 20, AUDCPI_VALUE_2013.length);
  }
  private static final ZonedDateTime[] AUDCPI_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2008, 3, 31), DateUtils.getUTCDate(2008, 6, 30), DateUtils.getUTCDate(2008, 9, 30),
    DateUtils.getUTCDate(2008, 12, 31), DateUtils.getUTCDate(2009, 3, 31), DateUtils.getUTCDate(2009, 6, 30), DateUtils.getUTCDate(2009, 9, 30), DateUtils.getUTCDate(2009, 12, 31),
    DateUtils.getUTCDate(2010, 3, 31), DateUtils.getUTCDate(2010, 6, 30), DateUtils.getUTCDate(2010, 9, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 3, 31),
    DateUtils.getUTCDate(2011, 6, 30), DateUtils.getUTCDate(2011, 9, 30), DateUtils.getUTCDate(2011, 12, 31), DateUtils.getUTCDate(2012, 3, 31), DateUtils.getUTCDate(2012, 6, 30),
    DateUtils.getUTCDate(2012, 9, 30), DateUtils.getUTCDate(2012, 12, 31), DateUtils.getUTCDate(2013, 3, 31), DateUtils.getUTCDate(2013, 6, 30), DateUtils.getUTCDate(2013, 9, 30), };
  private static final ZonedDateTimeDoubleTimeSeries AUDCPI_TIME_SERIES = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(AUDCPI_DATE, AUDCPI_VALUE);

  /**
   * Returns a market with three currencies (EUR, USD, GBP), three Ibor indexes (Euribor3M, Euribor6M, UsdLibor3M) and three inflation (Euro HICP x, UK RPI and US CPI-U).
   * @return The market.
   */
  public static InflationIssuerProviderDiscount createMarket1() {
    return MARKET_1;
  }

  /**
   * Creates a market with three currencies (EUR, USD, GBP), three Ibor indexes (Euribor3M, Euribor6M, UsdLibor3M) and three inflation (Euro HICP x, UK RPI and US CPI-U).
   * The US CPI-U price curve is constructed to have the correct past data (if available in the time series) and a fake 2% inflation for the future.
   * No seasonal adjustment is done.
   * @param pricingDate The data for which the curve is constructed.
   * @return The market.
   */
  public static InflationIssuerProviderDiscount createMarket1(final ZonedDateTime pricingDate) {
    final InflationIssuerProviderDiscount market = new InflationIssuerProviderDiscount();
    market.setCurve(Currency.USD, USD_DSC);
    market.setCurve(Currency.EUR, EUR_DSC);
    market.setCurve(Currency.GBP, CURVE_GBP_35);
    market.setCurve(Currency.AUD, AUD_DSC);
    market.setCurve(USDLIBOR3M, USD_FWD3);
    market.setCurve(EURIBOR3M, EUR_FWD3);
    market.setCurve(EURIBOR6M, EUR_FWD6);
    market.setCurve(AUDBB3M, AUD_FWD3);
    market.setCurve(AUDBB6M, AUD_FWD6);
    market.setCurve(PRICE_INDEX_EUR, PRICE_INDEX_CURVE_EUR);
    market.setCurve(PRICE_INDEX_GBP, PRICE_INDEX_CURVE_GBP);
    market.setCurve(ISSUER_UK_GOVT, CURVE_GBP_30);
    market.setCurve(ISSUER_US_GOVT, CURVE_USD_30);
    final ZonedDateTime spotUs = ScheduleCalculator.getAdjustedDate(pricingDate, SPOT_LAG_US, CALENDAR_USD);
    final ZonedDateTime referenceInterpolatedDate = spotUs.minusMonths(MONTH_LAG_US);
    final ZonedDateTime[] referenceDate = new ZonedDateTime[2];
    referenceDate[0] = referenceInterpolatedDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
    referenceDate[1] = referenceDate[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
    final int[] yearUs = new int[] {1, 5, 10, 20, 50 };
    final double[] indexValueUs = new double[2 + 2 * yearUs.length];
    final double[] timeValueUs = new double[2 + 2 * yearUs.length];
    indexValueUs[0] = USCPI_TIME_SERIES.getValue(referenceDate[0]);
    indexValueUs[1] = USCPI_TIME_SERIES.getValue(referenceDate[1]);
    timeValueUs[0] = TimeCalculator.getTimeBetween(pricingDate, referenceDate[0]);
    timeValueUs[1] = TimeCalculator.getTimeBetween(pricingDate, referenceDate[1]);
    final ZonedDateTime[] maturityDateUs = new ZonedDateTime[2 * yearUs.length];
    //    double[] maturityTimeUs = new double[yearUs.length];
    for (int loopus = 0; loopus < yearUs.length; loopus++) {
      maturityDateUs[2 * loopus] = ScheduleCalculator.getAdjustedDate(referenceDate[0], Period.ofYears(yearUs[loopus]), BUSINESS_DAY_USD, CALENDAR_USD);
      maturityDateUs[2 * loopus + 1] = ScheduleCalculator.getAdjustedDate(referenceDate[1], Period.ofYears(yearUs[loopus]), BUSINESS_DAY_USD, CALENDAR_USD);
      timeValueUs[2 + 2 * loopus] = TimeCalculator.getTimeBetween(pricingDate, maturityDateUs[2 * loopus]);
      timeValueUs[2 + 2 * loopus + 1] = TimeCalculator.getTimeBetween(pricingDate, maturityDateUs[2 * loopus + 1]);
      indexValueUs[2 + 2 * loopus] = indexValueUs[0] * Math.pow(1 + 0.02, yearUs[loopus]); // 2% inflation a year.
      indexValueUs[2 + 2 * loopus + 1] = indexValueUs[1] * Math.pow(1 + 0.02, yearUs[loopus]); // 2% inflation a year.
    }
    final InterpolatedDoublesCurve curveUs = InterpolatedDoublesCurve.from(timeValueUs, indexValueUs, new LinearInterpolator1D(), NAME_USD_PRICE_INDEX);
    final PriceIndexCurve priceIndexCurveUs = new PriceIndexCurve(curveUs);
    market.setCurve(PRICE_INDEX_USD, priceIndexCurveUs);
    return market;
  }

  public static InflationIssuerProviderDiscount createMarket2(final ZonedDateTime pricingDate) {
    final InflationIssuerProviderDiscount market = createMarket1(pricingDate);
    //    final DoublesCurve curveNoAdj = market.getCurve(PRICE_INDEX_USD).getCurve();
    //TODO: seasonal have modified, so the following have to be modified
    /*final DoublesCurve adj = new SeasonalCurve(curveNoAdj.getXData()[0], SEASONAL_FACTOR_USD, false);*/
    /* final DoublesCurve[] curveSet = new DoublesCurve[] {curveNoAdj, adj };*/
    /*final DoublesCurve[] curveSet = new DoublesCurve[] {curveNoAdj};
    final DoublesCurve curveAdj = new SpreadDoublesCurve(new MultiplyCurveSpreadFunction(), curveSet);
    market.replaceCurve(PRICE_INDEX_USD, new PriceIndexCurve(curveAdj));*/
    return market;
  }

  /**
   * Returns a multi-curves provider with two currencies (EUR, USD), four Ibor indexes (Euribor3M, Euribor6M, UsdLibor3M, UsdLibor6M).
   * @return The provider.
   */
  public static MulticurveProviderDiscount createMulticurveEurUsd() {
    return MULTICURVES_EUR_USD;
  }

  public static MulticurveProviderDiscount createSingleCurveUsd() {
    return SINGLECURVES_USD;
  }

  /**
  * Returns a multi-curves provider with two currencies (GBP, USD), four Ibor indexes (UsdLibor3M, UsdLibor6M).
  * @return The provider.
  */
  public static MulticurveProviderDiscount createMulticurveGbpUsd() {
    return MULTICURVES_GBP_USD;
  }

  /**
   * Returns a multi-curve provider with a unique curve for GBP, SONIA, GBPLIBOR3M and GBPLIBOR6M.
   * @return The provider.
   */
  public static MulticurveProviderDiscount createSinglecurveGbp() {
    return SINGLECURVES_GBP;
  }

  /**
   * Returns a multi-curves provider with two currencies USD without discount curve, four Ibor indexes (Euribor3M, Euribor6M, UsdLibor3M, UsdLibor6M).
   * @return The provider.
   */
  public static MulticurveProviderDiscount createMulticurveUsdWithoutDiscount() {
    return MULTICURVES_USD_WITHOUT_DISCOUNT;
  }

  /**
   * Returns a multi-curves provider with one currency (CAD), one Ibor index (CadCDOR3M).
   * @return The provider.
   */
  public static MulticurveProviderDiscount createMulticurveCad() {
    return MULTICURVES_CAD;
  }

  /**
   * Returns a multi-curves provider with one currency (EUR), two Ibor index (EURIBOR3M and EURIBOR6M).
   * @return The provider.
   */
  public static MulticurveProviderDiscount createMulticurveEUR() {
    final MulticurveProviderDiscount provideurEUR = new MulticurveProviderDiscount();
    provideurEUR.setCurve(Currency.EUR, EUR_DSC);
    provideurEUR.setCurve(EONIA, EUR_DSC);
    provideurEUR.setCurve(EURIBOR3M, EUR_FWD3);
    provideurEUR.setCurve(EURIBOR6M, EUR_FWD6);
    return provideurEUR;
  }

  /**
   * Returns an issuer provider with two currencies (EUR, USD), four Ibor indexes (Euribor3M, Euribor6M, UsdLibor3M, UsdLibor6M) and one issuer curve.
   * @return The provider.
   */
  public static IssuerProviderDiscount createIssuerProvider() {
    return PROVIDER_ISSUER;
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

  /**
   * Returns the EURO HICP-X time series (2009-2011).
   * @return The time series.
   */
  public static DoubleTimeSeries<ZonedDateTime> audCPIFrom2009() {
    return AUDCPI_TIME_SERIES;
  }

  public static IndexPrice[] getPriceIndexes() {
    return new IndexPrice[] {PRICE_INDEX_EUR, PRICE_INDEX_GBP, PRICE_INDEX_USD, PRICE_INDEX_AUD };
  }

  public static IborIndex[] getIndexesIborMulticurveEurUsd() {
    return new IborIndex[] {EURIBOR3M, EURIBOR6M, USDLIBOR3M, USDLIBOR6M };
  }

  public static IborIndex[] getIndexesIborMulticurveCad() {
    return new IborIndex[] {CADCDOR3M };
  }

  public static IndexON[] getIndexesON() {
    return new IndexON[] {FEDFUND, EONIA, BRAZIL_CDI };
  }

  public static String[] getIssuerNames() {
    return new String[] {(String) ISSUER_US_GOVT.getKey(), (String) ISSUER_UK_GOVT.getKey(), ISSUER_NAME, (String) ISSUER_AUD_GOVT.getKey() };
  }

  public static Calendar getCADCalendar() {
    return CALENDAR_CAD;
  }

  public static Calendar getEURCalendar() {
    return CALENDAR_EUR;
  }

  public static Calendar getUSDCalendar() {
    return CALENDAR_USD;
  }

  public static Calendar getAUDCalendar() {
    return CALENDAR_AUD;
  }
}
