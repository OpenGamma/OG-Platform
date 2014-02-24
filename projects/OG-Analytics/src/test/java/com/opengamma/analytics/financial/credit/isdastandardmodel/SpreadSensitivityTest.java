/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SpreadSensitivityTest {

  // private static final ISDACompliantCreditCurveBuild BUILDER = new ISDACompliantCreditCurveBuild();
  private static final ISDACompliantCreditCurveBuilder BUILDER = new FastCreditCurveBuilder();
  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();
  private static final FiniteDifferenceSpreadSensitivityCalculator CDV01_CAL = new FiniteDifferenceSpreadSensitivityCalculator();
  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");

  // common data
  private static final LocalDate TODAY = LocalDate.of(2013, 4, 21);
  private static final LocalDate EFFECTIVE_DATE = TODAY.plusDays(1); // AKA stepin date
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TODAY, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final double RECOVERY_RATE = 0.4;
  private static final double NOTIONAL = 1e7;

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
  public void parellelCreditDV01Test() {
    final double fromExcel = 4238.557409;

    final double dealSpread = DEAL_SPREAD / 10000;
    final double[] mrkSpreads = new double[NUM_MARKET_CDS];
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      mrkSpreads[i] = PAR_SPREADS[i] / 10000;
    }

    final double cdv01 = NOTIONAL / 10000 * CDV01_CAL.parallelCS01FromParSpreads(CDS, dealSpread, YIELD_CURVE, MARKET_CDS, mrkSpreads, 1e-4, BumpType.ADDITIVE);
    // System.out.println(cdv01);
    assertEquals("", fromExcel, cdv01, 1e-13 * NOTIONAL);

    /*
     * Errors checked
     */

    try {
      CDV01_CAL.parallelCS01FromParSpreads(CDS, dealSpread, YIELD_CURVE, MARKET_CDS, mrkSpreads, 1e-12, BumpType.ADDITIVE);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      final double[] mktSpShort = Arrays.copyOf(mrkSpreads, NUM_MARKET_CDS - 2);
      CDV01_CAL.parallelCS01FromParSpreads(CDS, dealSpread, YIELD_CURVE, MARKET_CDS, mktSpShort, 1e-4, BumpType.ADDITIVE);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }

  @Test(enabled = false)
  public void creditCurveTest() {
    final double[] mrkSpreads = new double[1];
    for (int i = 0; i < 1; i++) {
      mrkSpreads[i] = PAR_SPREADS[i] / 10000.;
    }
    final CDSAnalytic[] mrkCDS = new CDSAnalytic[] {MARKET_CDS[0] };
    final ISDACompliantCreditCurve creditCurve = BUILDER.calibrateCreditCurve(mrkCDS, mrkSpreads, YIELD_CURVE);

    final int n = creditCurve.getNumberOfKnots();
    for (int i = 0; i < n; i++) {
      System.out.println(creditCurve.getTimeAtIndex(i) + "\t" + creditCurve.getZeroRateAtIndex(i));
    }
    System.out.println();

    // final int step = 10;
    // for (int i = 0; i < 200; i++) {
    // final LocalDate temp = TODAY.plusDays(i * step);
    // final double t = ACT365.getDayCountFraction(TODAY, temp);
    // final double p = creditCurve.getDiscountFactor(t);
    // final double h = creditCurve.getHazardRate(t);
    // System.out.println(temp + "\t" + t + "\t" + p + "\t" + h);
    // }

    final double price = NOTIONAL * PRICER.pv(CDS, YIELD_CURVE, creditCurve, 50 / 10000.);
    System.out.println(price);
  }

  @Test(enabled = false)
  public void bucketCreditDVO1Test() {
    final double dealSpread = DEAL_SPREAD / 10000;
    final double[] mrkSpreads = new double[NUM_MARKET_CDS];
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      mrkSpreads[i] = PAR_SPREADS[i] / 10000;
    }

    final double[] bucketCdv01 = CDV01_CAL.bucketedCS01FromParSpreads(CDS, dealSpread, YIELD_CURVE, MARKET_CDS, mrkSpreads, 1e-4, BumpType.ADDITIVE);
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      System.out.println(bucketCdv01[i] * NOTIONAL / 10000);
    }

  }

  @Test(enabled = false)
  public void bucketFlatCreditDVO1Test() {
    final double dealSpread = DEAL_SPREAD / 10000;
    final double[] mrkSpreads = new double[NUM_MARKET_CDS];
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      mrkSpreads[i] = PAR_SPREADS[i] / 10000;
    }

    final double[] bucketCdv01 = CDV01_CAL.bucketedCS01FromQuotedSpreads(CDS, dealSpread, YIELD_CURVE, MARKET_CDS, mrkSpreads, 1e-4, BumpType.ADDITIVE);
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      System.out.println(bucketCdv01[i] * NOTIONAL / 10000);
    }

  }

  /**
   * 
   */
  @Test
  public void crossPrallelCS01test() {
    /*
     * Tol is not needed if exactly the same steps are taken
     */
    final double tol = 1.e-13;

    final AccrualOnDefaultFormulae form = AccrualOnDefaultFormulae.MarkitFix;
    final FiniteDifferenceSpreadSensitivityCalculator localCal = new FiniteDifferenceSpreadSensitivityCalculator(form);
    final MarketQuoteConverter conv = new MarketQuoteConverter(form);

    final ISDACompliantCreditCurveBuilder cvBuild = new FastCreditCurveBuilder(form);
    final AnalyticCDSPricer pricer = new AnalyticCDSPricer(form);

    final double bump = 1.e-4;
    final double spread = 115. * bump;
    final double fixedCoupon = 100. * bump;
    final double puf = 0.1;

    final CDSQuoteConvention quotePS = new ParSpread(spread);
    final CDSQuoteConvention quoteQS = new QuotedSpread(fixedCoupon, spread);
    final CDSQuoteConvention quotePU = new PointsUpFront(fixedCoupon, puf);
    final double pCS01PS = localCal.parallelCS01(CDS, quotePS, YIELD_CURVE, bump);
    final double pCS01QS = localCal.parallelCS01(CDS, quoteQS, YIELD_CURVE, bump);
    final double pCS01PU = localCal.parallelCS01(CDS, quotePU, YIELD_CURVE, bump);

    final double pCS01PSd = localCal.parallelCS01FromSpread(CDS, spread, YIELD_CURVE, spread, bump, BumpType.ADDITIVE);
    final double pCS01QSd = localCal.parallelCS01FromSpread(CDS, fixedCoupon, YIELD_CURVE, spread, bump, BumpType.ADDITIVE);
    final double pCS01PUd = localCal.parallelCS01FromPUF(CDS, fixedCoupon, YIELD_CURVE, puf, bump);

    assertEquals(pCS01PS, pCS01PSd, tol);
    assertEquals(pCS01QS, pCS01QSd, tol);
    assertEquals(pCS01PU, pCS01PUd, tol);

    //    final double pufFromPSpread = conv.parSpreadsToPUF(new CDSAnalytic[] {CDS }, spread, YIELD_CURVE, new double[] {spread })[0];
    //    final double pufFromBumpedPSpread = conv.parSpreadsToPUF(new CDSAnalytic[] {CDS }, new double[] {spread }, YIELD_CURVE, new double[] {spread + bump })[0];

    final ISDACompliantCreditCurve curvePSUp = cvBuild.calibrateCreditCurve(CDS, spread + bump, YIELD_CURVE);
    final ISDACompliantCreditCurve curvePS = cvBuild.calibrateCreditCurve(CDS, spread, YIELD_CURVE);
    final double pufFromBumpedPSpread = pricer.pv(CDS, YIELD_CURVE, curvePSUp, spread, PriceType.DIRTY);
    final double pufFromPSpread = pricer.pv(CDS, YIELD_CURVE, curvePS, spread, PriceType.DIRTY);

    final double pCS01PSExp = (pufFromBumpedPSpread - pufFromPSpread) / bump;
    assertEquals(pCS01PSExp, pCS01PS, tol);

    //    final double pufFromQSpread = conv.quotedSpreadToPUF(CDS, fixedCoupon, YIELD_CURVE, spread);
    //    final double pufFromBumpedQSpread = conv.quotedSpreadToPUF(CDS, fixedCoupon, YIELD_CURVE, spread + bump);

    final ISDACompliantCreditCurve curveUp = cvBuild.calibrateCreditCurve(CDS, spread + bump, YIELD_CURVE);
    final ISDACompliantCreditCurve curve = cvBuild.calibrateCreditCurve(CDS, spread, YIELD_CURVE);
    final double up = pricer.pv(CDS, YIELD_CURVE, curveUp, fixedCoupon, PriceType.DIRTY);
    final double price = pricer.pv(CDS, YIELD_CURVE, curve, fixedCoupon, PriceType.DIRTY);

    final double pCS01QSExp = (up - price) / bump;
    assertEquals(pCS01QSExp, pCS01QS, tol);

    final double bumpedQSpreadFromPUF = conv.pufToQuotedSpread(CDS, fixedCoupon, YIELD_CURVE, puf) + bump;
    final double pufFromBumpedSpread = conv.quotedSpreadToPUF(CDS, fixedCoupon, YIELD_CURVE, bumpedQSpreadFromPUF);
    final double pCS01PUExp = (pufFromBumpedSpread - puf) / bump;
    assertEquals(pCS01PUExp, pCS01PU, tol);

    final double pCS01Diff = localCal.parallelCS01FromQuotedSpread(CDS, fixedCoupon, YIELD_CURVE, MARKET_CDS[1], spread, bump, BumpType.ADDITIVE);
    final ISDACompliantCreditCurve curveAnUp = cvBuild.calibrateCreditCurve(MARKET_CDS[1], spread + bump, YIELD_CURVE);
    final ISDACompliantCreditCurve curveAn = cvBuild.calibrateCreditCurve(MARKET_CDS[1], spread, YIELD_CURVE);
    final double upAn = pricer.pv(CDS, YIELD_CURVE, curveAnUp, fixedCoupon, PriceType.DIRTY);
    final double priceAn = pricer.pv(CDS, YIELD_CURVE, curveAn, fixedCoupon, PriceType.DIRTY);
    final double pCS01DiffExp = (upAn - priceAn) / bump;
    assertEquals(pCS01DiffExp, pCS01Diff, tol);

    /*
     * Errors checked
     */

    try {
      localCal.parallelCS01FromQuotedSpread(CDS, fixedCoupon, YIELD_CURVE, MARKET_CDS[1], spread, bump * bump * bump, BumpType.ADDITIVE);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

  }

  /**
   * 
   */
  @Test
  public void CS01PillarAndCreditTest() {
    /*
     * Tol is not needed if exactly the same steps are taken
     */
    final double tol = 1.e-13;

    final AccrualOnDefaultFormulae form = AccrualOnDefaultFormulae.OrignalISDA;
    final FiniteDifferenceSpreadSensitivityCalculator localCal = new FiniteDifferenceSpreadSensitivityCalculator(form);
    final ISDACompliantCreditCurveBuilder cvBuild = new FastCreditCurveBuilder(form);
    final AnalyticCDSPricer pricer = new AnalyticCDSPricer(form);
    final MarketQuoteConverter conv = new MarketQuoteConverter(form);

    final double basisPt = 1.e-4;
    final double coupon = 125. * basisPt;
    final LocalDate startDate = IMMDateLogic.getPrevIMMDate(TODAY);
    final LocalDate nextIMM = IMMDateLogic.getNextIMMDate(TODAY);

    final double[] pillarSpreads = new double[] {107.81, 112.99, 115.26, 117.63, 120.8, 124.09, 127.81, 130.38, 136.82, 138.77, 141.3 };
    final Period[] tenors = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(6),
        Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10) };
    final LocalDate[] pillarDates = IMMDateLogic.getIMMDateSet(nextIMM, tenors);
    pillarDates[2] = pillarDates[2].minusMonths(2);
    pillarDates[3] = pillarDates[3].plusWeeks(3);

    final int nPillars = pillarDates.length;
    final CDSAnalytic[] pillarCDSs = new CDSAnalytic[nPillars];
    final CDSQuoteConvention[] pillar_quotes = new CDSQuoteConvention[nPillars];
    final CDSQuoteConvention[] pillar_quotes_bumped = new CDSQuoteConvention[nPillars];
    final double[] pillar_qSpreads = new double[nPillars];

    pillarCDSs[0] = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, startDate, pillarDates[0], true, Period.ofMonths(3), StubType.FRONTSHORT, true, 0.4);
    pillar_qSpreads[0] = pillarSpreads[0] * basisPt;
    final double puf = conv.quotedSpreadToPUF(pillarCDSs[0], coupon, YIELD_CURVE, pillar_qSpreads[0]);
    final double pufBumped = conv.quotedSpreadToPUF(pillarCDSs[0], coupon, YIELD_CURVE, pillar_qSpreads[0] + basisPt);
    pillar_quotes[0] = new PointsUpFront(coupon, puf);
    pillar_quotes_bumped[0] = new PointsUpFront(coupon, pufBumped);

    for (int i = 1; i < nPillars; i++) {
      pillarCDSs[i] = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, startDate, pillarDates[i], true, Period.ofMonths(3), StubType.FRONTSHORT, true, 0.4);
      pillar_qSpreads[i] = pillarSpreads[i] * basisPt;
      if (IMMDateLogic.isIMMDate(pillarDates[i])) {
        pillar_quotes[i] = new QuotedSpread(coupon, pillar_qSpreads[i]);
        pillar_quotes_bumped[i] = new QuotedSpread(coupon, pillar_qSpreads[i] + basisPt);
      } else {
        pillar_quotes[i] = new ParSpread(pillar_qSpreads[i]);
        pillar_quotes_bumped[i] = new ParSpread(pillar_qSpreads[i] + basisPt);
      }
    }

    /*
     * Test parallelCS01FromPillarQuotes
     */
    final double res1 = localCal.parallelCS01FromPillarQuotes(CDS, coupon, YIELD_CURVE, pillarCDSs, pillar_quotes, basisPt);
    final ISDACompliantCreditCurve curve = cvBuild.calibrateCreditCurve(pillarCDSs, pillar_quotes, YIELD_CURVE);
    final double pv = pricer.pv(CDS, YIELD_CURVE, curve, coupon);
    final ISDACompliantCreditCurve curveBumped = cvBuild.calibrateCreditCurve(pillarCDSs, pillar_quotes_bumped, YIELD_CURVE);
    final double pvBumped = pricer.pv(CDS, YIELD_CURVE, curveBumped, coupon);
    assertEquals((pvBumped - pv) / basisPt, res1, tol);

    /*
     * Test parallelCS01FromCreditCurve
     */
    final double res2 = localCal.parallelCS01FromCreditCurve(CDS, coupon, pillarCDSs, YIELD_CURVE, curve, basisPt);
    final double[] impSpreads = new double[nPillars];
    final double[] impSpreadsBumped = new double[nPillars];
    for (int i = 0; i < nPillars; ++i) {
      impSpreads[i] = pricer.parSpread(pillarCDSs[i], YIELD_CURVE, curve);
      impSpreadsBumped[i] = impSpreads[i] + basisPt;
    }
    final ISDACompliantCreditCurve curveFromImpliedSpreads = cvBuild.calibrateCreditCurve(pillarCDSs, impSpreads, YIELD_CURVE);
    final double pvBase = pricer.pv(CDS, YIELD_CURVE, curveFromImpliedSpreads, coupon);
    final ISDACompliantCreditCurve curveFromImpliedSpreadsBumped = cvBuild.calibrateCreditCurve(pillarCDSs, impSpreadsBumped, YIELD_CURVE);
    final double pvBaseBumped = pricer.pv(CDS, YIELD_CURVE, curveFromImpliedSpreadsBumped, coupon);
    assertEquals((pvBaseBumped - pvBase) / basisPt, res2, tol);

    /*
     * Test bucketedCS01FromPillarQuotes
     */
    final double[] bucketed1 = localCal.bucketedCS01FromPillarQuotes(CDS, coupon, YIELD_CURVE, pillarCDSs, pillar_quotes, basisPt);
    for (int i = 0; i < nPillars; ++i) {
      final double[] spreadsWithOneBump = Arrays.copyOf(pillar_qSpreads, nPillars);
      spreadsWithOneBump[i] += basisPt;
      final CDSQuoteConvention[] pillarQuotesLocal = new CDSQuoteConvention[nPillars];
      for (int j = 0; j < nPillars; ++j) {
        if (IMMDateLogic.isIMMDate(pillarDates[j])) {
          pillarQuotesLocal[j] = new QuotedSpread(coupon, spreadsWithOneBump[j]);
        } else {
          pillarQuotesLocal[j] = new ParSpread(spreadsWithOneBump[j]);
        }
      }
      final ISDACompliantCreditCurve curveWithOneBump = cvBuild.calibrateCreditCurve(pillarCDSs, pillarQuotesLocal, YIELD_CURVE);
      final double pvWithOneBump = pricer.pv(CDS, YIELD_CURVE, curveWithOneBump, coupon);
      assertEquals((pvWithOneBump - pv) / basisPt, bucketed1[i], tol);
    }

    /*
     * Test bucketedCS01FromPillarQuotes
     */
    final LocalDate[] pSpDates = new LocalDate[] {LocalDate.of(2013, 9, 20), LocalDate.of(2014, 6, 20), LocalDate.of(2016, 9, 20), LocalDate.of(2018, 6, 20), LocalDate.of(2023, 9, 20) };
    final CDSAnalytic[] bucketCDSs = new CDSAnalytic[pSpDates.length];
    for (int i = 0; i < pSpDates.length; i++) {
      bucketCDSs[i] = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, TODAY, pSpDates[i], true, Period.ofMonths(3), StubType.FRONTSHORT, true, RECOVERY_RATE);
    }
    final double[] bucketed2 = localCal.bucketedCS01FromParSpreads(CDS, coupon, bucketCDSs, YIELD_CURVE, pillarCDSs, pillar_qSpreads, basisPt);
    final double[] impSps = new double[pSpDates.length];
    final ISDACompliantCreditCurve curveFromSpreads = cvBuild.calibrateCreditCurve(pillarCDSs, pillar_qSpreads, YIELD_CURVE);
    for (int i = 0; i < pSpDates.length; ++i) {
      impSps[i] = pricer.parSpread(bucketCDSs[i], YIELD_CURVE, curveFromSpreads);
    }
    final ISDACompliantCreditCurve curveBucket = cvBuild.calibrateCreditCurve(bucketCDSs, impSps, YIELD_CURVE);
    final double bvBase = pricer.pv(CDS, YIELD_CURVE, curveBucket, coupon);
    for (int i = 0; i < pSpDates.length; ++i) {
      final double[] impSpsBump = Arrays.copyOf(impSps, pSpDates.length);
      impSpsBump[i] += basisPt;
      final ISDACompliantCreditCurve curveBucketBump = cvBuild.calibrateCreditCurve(bucketCDSs, impSpsBump, YIELD_CURVE);
      final double bvBaseBump = pricer.pv(CDS, YIELD_CURVE, curveBucketBump, coupon);
      assertEquals((bvBaseBump - bvBase) / basisPt, bucketed2[i], tol);
    }

    /*
     * Errors checked
     */
    final CDSAnalytic[] shortCDSs = Arrays.copyOf(pillarCDSs, nPillars - 1);
    final double[] shortSpreads = Arrays.copyOf(pillarSpreads, pillarSpreads.length - 2);
    try {
      localCal.parallelCS01FromPillarQuotes(CDS, coupon, YIELD_CURVE, pillarCDSs, pillar_quotes, basisPt * 1.e-9);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      localCal.parallelCS01FromPillarQuotes(CDS, coupon, YIELD_CURVE, shortCDSs, pillar_quotes, basisPt);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      localCal.parallelCS01FromCreditCurve(CDS, coupon, pillarCDSs, YIELD_CURVE, curve, basisPt * 1.e-9);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      final CDSAnalytic[] unsortedCDSs = Arrays.copyOf(pillarCDSs, nPillars);
      final CDSAnalytic tmp = unsortedCDSs[2];
      unsortedCDSs[2] = unsortedCDSs[1];
      unsortedCDSs[1] = tmp;
      localCal.parallelCS01FromCreditCurve(CDS, coupon, unsortedCDSs, YIELD_CURVE, curve, basisPt);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      localCal.bucketedCS01FromPillarQuotes(CDS, coupon, YIELD_CURVE, pillarCDSs, pillar_quotes, basisPt * 1.e-9);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      localCal.bucketedCS01FromPillarQuotes(CDS, coupon, YIELD_CURVE, shortCDSs, pillar_quotes, basisPt);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      localCal.bucketedCS01FromCreditCurve(CDS, coupon, pillarCDSs, YIELD_CURVE, curve, basisPt * 1.e-7);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      pillarCDSs[2] = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, startDate, pillarDates[2].plusYears(10), true, Period.ofMonths(3), StubType.FRONTSHORT, true, 0.4);
      localCal.bucketedCS01FromCreditCurve(CDS, coupon, pillarCDSs, YIELD_CURVE, curve, basisPt);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      localCal.bucketedCS01FromQuotedSpreads(new CDSAnalytic[] {CDS }, coupon, YIELD_CURVE, pillarCDSs, pillarSpreads, basisPt * 1.e-7, BumpType.ADDITIVE);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      localCal.bucketedCS01FromQuotedSpreads(new CDSAnalytic[] {CDS }, coupon, YIELD_CURVE, pillarCDSs, shortSpreads, basisPt, BumpType.ADDITIVE);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      localCal.bucketedCS01FromParSpreads(CDS, coupon, YIELD_CURVE, pillarCDSs, pillarSpreads, basisPt * 1.e-7, BumpType.ADDITIVE);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      localCal.bucketedCS01FromParSpreads(CDS, coupon, YIELD_CURVE, pillarCDSs, shortSpreads, basisPt, BumpType.ADDITIVE);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      localCal.bucketedCS01FromQuotedSpreads(CDS, coupon, YIELD_CURVE, pillarCDSs, pillarSpreads, basisPt * 1.e-7, BumpType.ADDITIVE);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      localCal.bucketedCS01FromQuotedSpreads(CDS, coupon, YIELD_CURVE, pillarCDSs, shortSpreads, basisPt, BumpType.ADDITIVE);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }

  /**
   * 
   */
  @Test
  public void finiteDifferenceSpreadSensitivityTest() {
    /*
     * Tol is not needed if exactly the same steps are taken
     */
    final double tol = 1.e-13;

    final AccrualOnDefaultFormulae form = AccrualOnDefaultFormulae.Correct;
    final FiniteDifferenceSpreadSensitivityCalculator localCal = new FiniteDifferenceSpreadSensitivityCalculator(form);
    final ISDACompliantCreditCurveBuilder cvBuild = new FastCreditCurveBuilder(form);
    final AnalyticCDSPricer pricer = new AnalyticCDSPricer(form);

    final PriceType pType = PriceType.DIRTY;
    final double basisPt = 1.e-4;
    final double coupon = 125. * basisPt;

    final double[] spreads = new double[NUM_MARKET_CDS];
    final double[] spreadBumps = new double[NUM_MARKET_CDS];
    final double[] spreadBumpUp = new double[NUM_MARKET_CDS];
    final double[] spreadBumpDw = new double[NUM_MARKET_CDS];

    for (int i = 0; i < NUM_MARKET_CDS; ++i) {
      spreads[i] = PAR_SPREADS[i] * basisPt;
      spreadBumps[i] = spreads[i] * 1.e-2;
      spreadBumpUp[i] = spreads[i] + spreadBumps[i];
      spreadBumpDw[i] = spreads[i] - spreadBumps[i];
    }

    final double resCnt = localCal.finiteDifferenceSpreadSensitivity(CDS, coupon, pType, YIELD_CURVE, MARKET_CDS, spreads, spreadBumps, FiniteDifferenceType.CENTRAL);
    final double resFwd = localCal.finiteDifferenceSpreadSensitivity(CDS, coupon, pType, YIELD_CURVE, MARKET_CDS, spreads, spreadBumps, FiniteDifferenceType.FORWARD);
    final double resBck = localCal.finiteDifferenceSpreadSensitivity(CDS, coupon, pType, YIELD_CURVE, MARKET_CDS, spreads, spreadBumps, FiniteDifferenceType.BACKWARD);

    final ISDACompliantCreditCurve curve = cvBuild.calibrateCreditCurve(MARKET_CDS, spreads, YIELD_CURVE);
    final ISDACompliantCreditCurve curveUp = cvBuild.calibrateCreditCurve(MARKET_CDS, spreadBumpUp, YIELD_CURVE);
    final ISDACompliantCreditCurve curveDw = cvBuild.calibrateCreditCurve(MARKET_CDS, spreadBumpDw, YIELD_CURVE);

    final double pv = pricer.pv(CDS, YIELD_CURVE, curve, coupon, pType);
    final double pvUp = pricer.pv(CDS, YIELD_CURVE, curveUp, coupon, pType);
    final double pvDw = pricer.pv(CDS, YIELD_CURVE, curveDw, coupon, pType);

    assertEquals(pvUp - pvDw, resCnt, tol);
    assertEquals(pvUp - pv, resFwd, tol);
    assertEquals(pv - pvDw, resBck, tol);

    /*
     * Errors checked
     */
    try {
      final double[] shortSpreads = Arrays.copyOf(spreads, NUM_MARKET_CDS - 2);
      localCal.finiteDifferenceSpreadSensitivity(CDS, coupon, pType, YIELD_CURVE, MARKET_CDS, shortSpreads, spreadBumps, FiniteDifferenceType.CENTRAL);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      final double[] shortSpreadBumps = Arrays.copyOf(spreadBumps, NUM_MARKET_CDS - 3);
      localCal.finiteDifferenceSpreadSensitivity(CDS, coupon, pType, YIELD_CURVE, MARKET_CDS, spreads, shortSpreadBumps, FiniteDifferenceType.CENTRAL);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      final double[] negativeSpreads = Arrays.copyOf(spreads, NUM_MARKET_CDS);
      negativeSpreads[1] *= -1.;
      localCal.finiteDifferenceSpreadSensitivity(CDS, coupon, pType, YIELD_CURVE, MARKET_CDS, negativeSpreads, spreadBumps, FiniteDifferenceType.CENTRAL);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      final double[] negativeSpreadBumps = Arrays.copyOf(spreadBumps, NUM_MARKET_CDS);
      negativeSpreadBumps[3] *= -1.;
      localCal.finiteDifferenceSpreadSensitivity(CDS, coupon, pType, YIELD_CURVE, MARKET_CDS, spreads, negativeSpreadBumps, FiniteDifferenceType.CENTRAL);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      final double[] largeSpreadBumps = Arrays.copyOf(spreadBumps, NUM_MARKET_CDS);
      largeSpreadBumps[1] += 1.e2;
      localCal.finiteDifferenceSpreadSensitivity(CDS, coupon, pType, YIELD_CURVE, MARKET_CDS, spreads, largeSpreadBumps, FiniteDifferenceType.CENTRAL);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

  }

}
