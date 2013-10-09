/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Sets of market data used in tests. With issuers.
 */
public class IssuerProviderDiscountDataSets {

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final String US_NAME = "US GOVT";
  private static final String BEL_NAME = "BELGIUM GOVT";
  private static final String GER_NAME = "GERMANY GOVT";
  private static final String UK_NAME = "UK GOVT";

  private static final Currency USD = Currency.USD;
  private static final Currency EUR = Currency.EUR;
  private static final Currency GBP = Currency.GBP;

  private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();
  private static final IborIndex EURIBOR3M = MASTER_IBOR_INDEX.getIndex("EURIBOR3M");

  private static final double[] USD_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] USD_DSC_RATE = new double[] {0.0120, 0.0120, 0.0120, 0.0140, 0.0140, 0.0140 };
  private static final String USD_DSC_NAME = "USD Dsc";
  private static final YieldAndDiscountCurve USD_DSC = new YieldCurve(USD_DSC_NAME, new InterpolatedDoublesCurve(USD_DSC_TIME, USD_DSC_RATE, LINEAR_FLAT, true, USD_DSC_NAME));

  private static final double[] EUR_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] EUR_DSC_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0150, 0.0150 };
  private static final String EUR_DSC_NAME = "EUR Dsc";
  private static final YieldAndDiscountCurve EUR_DSC = new YieldCurve(EUR_DSC_NAME, new InterpolatedDoublesCurve(EUR_DSC_TIME, EUR_DSC_RATE, LINEAR_FLAT, true, EUR_DSC_NAME));
  private static final double[] EUR_FWD3_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 10.0 };
  private static final double[] EUR_FWD3_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0175, 0.0190, 0.0200, 0.0210 };
  private static final String EUR_FWD3_NAME = "EUR EURIBOR 3M";
  private static final YieldAndDiscountCurve EUR_FWD3 = new YieldCurve(EUR_FWD3_NAME, new InterpolatedDoublesCurve(EUR_FWD3_TIME, EUR_FWD3_RATE, LINEAR_FLAT, true, EUR_FWD3_NAME));

  private static final double[] GBP_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] GBP_DSC_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0150, 0.0150 };
  private static final String GBP_DSC_NAME = "GBP Dsc";
  private static final YieldAndDiscountCurve GBP_DSC = new YieldCurve(GBP_DSC_NAME, new InterpolatedDoublesCurve(GBP_DSC_TIME, GBP_DSC_RATE, LINEAR_FLAT, true, GBP_DSC_NAME));

  private static final Pair<String, Currency> US_USD = new ObjectsPair<>(US_NAME, USD);
  private static final double[] USD_US_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] USD_US_RATE = new double[] {0.0100, 0.0100, 0.0100, 0.0120, 0.0120, 0.0120 };
  private static final String USD_US_CURVE_NAME = "USD " + US_NAME;
  private static final YieldAndDiscountCurve US_USD_CURVE = new YieldCurve(USD_US_CURVE_NAME, new InterpolatedDoublesCurve(USD_US_TIME, USD_US_RATE, LINEAR_FLAT, true, USD_US_CURVE_NAME));
  private static final YieldAndDiscountCurve US_USD_CURVE_6 = new YieldCurve(USD_US_CURVE_NAME, new ConstantDoublesCurve(0.06, USD_US_CURVE_NAME));

  private static final Pair<String, Currency> BEL_EUR = new ObjectsPair<>(BEL_NAME, EUR);
  private static final double[] EUR_BEL_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] EUR_BEL_RATE = new double[] {0.0250, 0.0225, 0.0250, 0.0275, 0.0250, 0.0250 };
  private static final String EUR_BEL_CURVE_NAME = "EUR " + BEL_NAME;
  private static final YieldAndDiscountCurve BEL_EUR_CURVE = new YieldCurve(EUR_BEL_CURVE_NAME, new InterpolatedDoublesCurve(EUR_BEL_TIME, EUR_BEL_RATE, LINEAR_FLAT, true, EUR_BEL_CURVE_NAME));

  private static final Pair<String, Currency> GER_EUR = new ObjectsPair<>(GER_NAME, EUR);
  private static final double[] EUR_GER_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] EUR_GER_RATE = new double[] {0.0250, 0.0225, 0.0250, 0.0275, 0.0250, 0.0250 };
  private static final String GER_EUR_CURVE_NAME = "EUR " + GER_NAME;
  private static final YieldAndDiscountCurve GER_EUR_CURVE = new YieldCurve(GER_EUR_CURVE_NAME, new InterpolatedDoublesCurve(EUR_GER_TIME, EUR_GER_RATE, LINEAR_FLAT, true, USD_US_CURVE_NAME));

  private static final Pair<String, Currency> UK_GBP = new ObjectsPair<>(UK_NAME, GBP);
  private static final double[] UK_GBP_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] UK_GBP_RATE = new double[] {0.0250, 0.0225, 0.0250, 0.0275, 0.0250, 0.0250 };
  private static final String UK_GBP_CURVE_NAME = "GBP " + UK_NAME;
  private static final YieldAndDiscountCurve UK_GBP_CURVE = new YieldCurve(UK_GBP_CURVE_NAME, new InterpolatedDoublesCurve(UK_GBP_TIME, UK_GBP_RATE, LINEAR_FLAT, true, UK_GBP_CURVE_NAME));

  private static final MulticurveProviderDiscount MULTICURVE = new MulticurveProviderDiscount();
  static {
    MULTICURVE.setCurve(USD, USD_DSC);
    MULTICURVE.setCurve(EUR, EUR_DSC);
    MULTICURVE.setCurve(EURIBOR3M, EUR_FWD3);
    MULTICURVE.setCurve(GBP, GBP_DSC);
  }
  private static final Map<Pair<String, Currency>, YieldAndDiscountCurve> ISSUER = new LinkedHashMap<>();
  static {
    ISSUER.put(US_USD, US_USD_CURVE);
    ISSUER.put(BEL_EUR, BEL_EUR_CURVE);
    ISSUER.put(GER_EUR, GER_EUR_CURVE);
    ISSUER.put(UK_GBP, UK_GBP_CURVE);
  }
  private static final Map<Pair<String, Currency>, YieldAndDiscountCurve> ISSUER_6 = new LinkedHashMap<>();
  static {
    ISSUER_6.put(US_USD, US_USD_CURVE_6);
  }

  private static final IssuerProviderDiscount ISSUER_MULTICURVE = new IssuerProviderDiscount(MULTICURVE, ISSUER);

  /**
   * Returns a multi-curves provider with three currencies (USD, EUR, GBP), one Ibor (EURIBOR3M) and three issuers (US GOVT, BELGIUM GOVT, GERMAN GOVT, UK GOVT).
   * @return The provider.
   */
  public static IssuerProviderDiscount createIssuerProvider() {
    return ISSUER_MULTICURVE;
  }

  /**
   * Returns a multi-curves provider with one currency (EUR) and one issuer (GERMAN GOVT). The issuer curve is at 6% (useful for futures).
   * @return The provider.
   */
  public static IssuerProviderDiscount createIssuerProvider6() {
    return new IssuerProviderDiscount(MULTICURVE, ISSUER_6);
  }

  public static String[] getIssuerNames() {
    return new String[] {US_NAME, BEL_NAME, GER_NAME, UK_NAME };
  }

}
