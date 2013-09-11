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
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

/**
 * 
 */
public class AnalyticCDV01Test {

  // private static final ISDACompliantCreditCurveBuild BUILDER = new ISDACompliantCreditCurveBuild();
  private static final ISDACompliantCreditCurveBuilder BUILDER = new FastCreditCurveBuilder();
  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();
  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");

  // common data
  private static final LocalDate TODAY = LocalDate.of(2013, 4, 21);
  private static final LocalDate EFFECTIVE_DATE = TODAY.plusDays(1); // AKA stepin date
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TODAY, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final double RECOVERY_RATE = 0.4;
  // valuation CDS
  private static final LocalDate PROTECTION_STATE_DATE = LocalDate.of(2013, 2, 3); // Seasoned CDS
  private static final LocalDate PROTECTION_END_DATE = LocalDate.of(2018, 3, 20);
  private static final double DEAL_SPREAD = 101;
  private static final CDSAnalytic CDS;

  // market CDSs
  private static final LocalDate[] PAR_SPD_DATES = new LocalDate[] {LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20), LocalDate.of(2014, 3, 20), LocalDate.of(2015, 3, 20),
    LocalDate.of(2016, 3, 20), LocalDate.of(2018, 3, 20), LocalDate.of(2023, 3, 20) };
  private static final double[] PAR_SPREADS = new double[] {50, 70, 80, 95, 100, 95, 80 };
  private static final int NUM_MARKET_CDS = PAR_SPD_DATES.length;
  private static final CDSAnalytic[] MARKET_CDS = new CDSAnalytic[NUM_MARKET_CDS];

  // yield curve
  private static ISDACompliantYieldCurve YIELD_CURVE;

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
  public void Test() {
    final boolean print = false;
    if (print) {
      System.out.println("AnalyticCDV01Test set print =false before push");
    }

    final double dealSpread = DEAL_SPREAD / 10000;
    final double[] mrkSpreads = new double[NUM_MARKET_CDS];
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      mrkSpreads[i] = PAR_SPREADS[i] / 10000;
    }
    final ISDACompliantCreditCurve creditCurve = BUILDER.calibrateCreditCurve(MARKET_CDS, mrkSpreads, YIELD_CURVE);
    final int n = creditCurve.getNumberOfKnots();
    final double[][] jacA = new double[n][n];
    for (int j = 0; j < n; j++) {
      final CDSAnalytic cds = MARKET_CDS[j];
      for (int i = 0; i < n; i++) {
        final double dDdH = PRICER.parSpreadCreditSensitivity(cds, YIELD_CURVE, creditCurve, i);
        jacA[i][j] = dDdH;
      }
    }
    final DoubleMatrix2D jac = new DoubleMatrix2D(jacA);
    if (print) {
      System.out.println(jac);
      System.out.println();
    }

    final double[] temp = new double[n];
    for (int i = 0; i < n; i++) {
      temp[i] = PRICER.pvCreditSensitivity(CDS, YIELD_CURVE, creditCurve, dealSpread, i);
    }
    final DoubleMatrix1D dVdH = new DoubleMatrix1D(temp);
    if (print) {
      System.out.println(dVdH);
      System.out.println();
    }

    final LUDecompositionCommons decomp = new LUDecompositionCommons();
    final LUDecompositionResult res = decomp.evaluate(jac);
    final DoubleMatrix1D dVdS = res.solve(dVdH);

    // compare with bump and reprice
    final SpreadSensitivityCalculator bumpCal = new SpreadSensitivityCalculator();
    final double[] fd = bumpCal.bucketedCS01FromParSpreads(CDS, dealSpread, YIELD_CURVE, MARKET_CDS, mrkSpreads, 1e-7, BumpType.ADDITIVE);
    final DoubleMatrix1D fd_dVdS = new DoubleMatrix1D(fd);
    if (print) {
      System.out.println(dVdS);
      System.out.println(fd_dVdS);
    }
    for (int i = 0; i < n; i++) {
      assertEquals(fd_dVdS.getEntry(i), dVdS.getEntry(i), 1e-6); // the fd is only forward difference - so accuracy is not great
    }
  }

}
