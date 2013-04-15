/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class CapletStrippingSetup {
  private static final double[] INDEX_CURVE_NODES = new double[] {0.0438356164383561, 0.0876712328767123, 0.172602739726027, 0.254794520547945, 0.506849315068493, 0.758904109589041, 1.00547945205479,
      2.01369863013698, 3.01020285949547, 4.00547945205479, 5.00547945205479, 6.00547945205479, 7.01839958080694, 8.01095890410959, 9.00821917808219, 10.0082191780821, 15.0074706190583,
      20.0082191780821, 25.0109589041095, 30.0136986301369};
  private static final double[] INDEX_CURVE_VALUES = new double[] {0.00184088091044285, 0.00201024117395892, 0.00241264832694067, 0.00280755413825359, 0.0029541307818572, 0.00310125437814943,
      0.00320054435838637, 0.00377914611772073, 0.00483320020067661, 0.00654829256979543, 0.00877749583222556, 0.0112470678648412, 0.0136301644164456, 0.0157618031582798, 0.0176836551757772,
      0.0194174141169365, 0.0254011614777518, 0.0282527762712854, 0.0298620063409043, 0.031116719228976};

  private static final double[] DIS_CURVE_NODES = new double[] {0.00273972602739726, 0.0876712328767123, 0.172602739726027, 0.254794520547945, 0.345205479452054, 0.424657534246575, 0.506849315068493,
      0.758904109589041, 1.00547945205479, 2.00547945205479, 3.01020285949547, 4.00547945205479, 5.00547945205479, 10.0054794520547};

  private static final double[] DIS_CURVE_VALUES = new double[] {0.00212916045658802, 0.00144265912946933, 0.00144567477491987, 0.00135441424749791, 0.00134009103595346, 0.00132773752749976,
      0.00127592397233014, 0.00132302501180961, 0.00138688847322639, 0.00172748279241698, 0.00254381216780551, 0.00410024606039574, 0.00628782387356631, 0.0170033466745807};

  protected static final Currency CUR = Currency.USD;
  private static final Period TENOR = Period.ofMonths(3);
  protected static final int FREQUENCY = 4;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  protected static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

  protected static final YieldCurveBundle YIELD_CURVES;

  static {

    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC);
    final InterpolatedDoublesCurve curve1 = new InterpolatedDoublesCurve(INDEX_CURVE_NODES, INDEX_CURVE_VALUES, interpolator, true);
    final InterpolatedDoublesCurve curve2 = new InterpolatedDoublesCurve(DIS_CURVE_NODES, DIS_CURVE_VALUES, interpolator, true);
    // single curve for discount and projection (this is consistent with Bloomberg's cap quotes)
    final YieldCurve indexCurve = YieldCurve.from(curve1);
    final YieldCurve disCurve = YieldCurve.from(curve2);
    YIELD_CURVES = new YieldCurveBundle();
    YIELD_CURVES.setCurve("funding", disCurve);
    YIELD_CURVES.setCurve("3m Libor", indexCurve);
  }

}
