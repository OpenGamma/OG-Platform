/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

/**
 *
 */
public class CreditCurveCalibrationTest {

  private static final FastCreditCurveBuilder BUILDER_NEW = new FastCreditCurveBuilder();
  @SuppressWarnings("deprecation")
  private static final SimpleCreditCurveBuilder BUILDER_OLD = new SimpleCreditCurveBuilder();
  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");

  // common data
  private static final LocalDate TODAY = LocalDate.of(2013, 4, 21);
  private static final LocalDate EFFECTIVE_DATE = TODAY.plusDays(1); // AKA stepin date
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TODAY, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final double RECOVERY_RATE = 0.4;

  // valuation CDS
  private static final LocalDate PROTECTION_STATE_DATE = LocalDate.of(2013, 2, 3); // Seasoned CDS
  private static final LocalDate PROTECTION_END_DATE = LocalDate.of(2018, 3, 20);
  protected static final double DEAL_SPREAD = 101;
  protected static final CDSAnalytic CDS;

  // market CDSs
  private static final LocalDate[] PAR_SPD_DATES = new LocalDate[] {LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20), LocalDate.of(2014, 3, 20), LocalDate.of(2015, 3, 20),
    LocalDate.of(2016, 3, 20), LocalDate.of(2018, 3, 20), LocalDate.of(2023, 3, 20) };
  private static final double[] PAR_SPREADS = new double[] {50, 70, 80, 95, 100, 95, 80 };
  private static final int NUM_MARKET_CDS = PAR_SPD_DATES.length;
  private static final CDSAnalytic[] MARKET_CDS = new CDSAnalytic[NUM_MARKET_CDS];

  // yield curve
  protected static ISDACompliantYieldCurve YIELD_CURVE;

  // results from ISDA Excel
  protected static final double[] CREDIT_CURVE_KNOTS = new double[] {0.164383561643836, 0.416438356164384, 0.912328767123288, 1.91232876712329, 2.91506849315068, 4.91506849315068, 9.91780821917808 };
  private static final double[] ZERO_HAZARD_RATES = new double[] {0.00841340552675563, 0.0117803449136365, 0.013468629793501, 0.016047855558532, 0.0169115966481877, 0.0159338068105945,
    0.0129657754955384 };
  protected static ISDACompliantCreditCurve CREDIT_CURVE = new ISDACompliantCreditCurve(CREDIT_CURVE_KNOTS, ZERO_HAZARD_RATES);

  static {
    final double flatrate = 0.05;
    final double t = 20.0;
    YIELD_CURVE = new ISDACompliantYieldCurve(new double[] {t }, new double[] {flatrate });

    final boolean payAccOndefault = true;
    final Period tenor = Period.ofMonths(3);
    final StubType stubType = StubType.FRONTSHORT;
    final boolean protectionStart = true;

    CDS = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, PROTECTION_STATE_DATE, PROTECTION_END_DATE, payAccOndefault, tenor, stubType, protectionStart, RECOVERY_RATE);

    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      MARKET_CDS[i] = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, TODAY, PAR_SPD_DATES[i], payAccOndefault, tenor, stubType, protectionStart, RECOVERY_RATE);
    }
  }

  @Test
  // (enabled = false)
  public void simpleCreditCurveCalibrationTest() {
    @SuppressWarnings("deprecation")
    final SimpleCreditCurveBuilder builder = BUILDER_OLD;
    creditCurveTest(builder);
  }

  @Test
  // (enabled = false)
  public void fastCreditCurveCalibrationTest() {
    final FastCreditCurveBuilder builder = BUILDER_NEW;
    creditCurveTest(builder);
  }

  private void creditCurveTest(final ISDACompliantCreditCurveBuilder builder) {
    final double[] mrkSpreads = new double[NUM_MARKET_CDS];
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      mrkSpreads[i] = PAR_SPREADS[i] / 10000.;
    }

    final ISDACompliantCreditCurve creditCurve = builder.calibrateCreditCurve(MARKET_CDS, mrkSpreads, YIELD_CURVE);

    final int n = creditCurve.getNumberOfKnots();
    assertEquals(CREDIT_CURVE_KNOTS.length, n);
    for (int i = 0; i < n; i++) {
      final double t = creditCurve.getTimeAtIndex(i);
      final double h = creditCurve.getZeroRateAtIndex(i);
      assertEquals(CREDIT_CURVE_KNOTS[i], t, 1e-14);
      assertEquals(ZERO_HAZARD_RATES[i], h, 1e-14);
      // System.out.println(t + "\t" + h);
    }
  }

}
