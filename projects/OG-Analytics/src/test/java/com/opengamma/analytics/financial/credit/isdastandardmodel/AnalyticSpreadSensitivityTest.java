/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.model.BumpType;

/**
 * 
 */
public class AnalyticSpreadSensitivityTest extends ISDABaseTest {

  private static final AnalyticSpreadSensitivityCalculator ANAL_CS01_CAL = new AnalyticSpreadSensitivityCalculator();

  // common data
  private static final LocalDate TODAY = LocalDate.of(2013, 4, 21);
  private static final LocalDate EFFECTIVE_DATE = TODAY.plusDays(1); // AKA stepin date
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TODAY, 3, DEFAULT_CALENDAR); // AKA valuation date

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

    final double dealSpread = DEAL_SPREAD * ONE_BP;
    final double[] mrkSpreads = new double[NUM_MARKET_CDS];
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      mrkSpreads[i] = PAR_SPREADS[i] * ONE_BP;
    }

    // compare with bump and reprice
    final double[] an_CS01 = ANAL_CS01_CAL.bucketedCS01FromParSpreads(CDS, dealSpread, YIELD_CURVE, MARKET_CDS, mrkSpreads);
    final double[] fd_CS01 = CS01_CAL.bucketedCS01FromParSpreads(CDS, dealSpread, YIELD_CURVE, MARKET_CDS, mrkSpreads, 1e-7, BumpType.ADDITIVE);

    final int n = fd_CS01.length;
    for (int i = 0; i < n; i++) {
      assertEquals(fd_CS01[i], an_CS01[i], 1e-6); // the fd is only forward difference - so accuracy is not great
    }
  }
}
