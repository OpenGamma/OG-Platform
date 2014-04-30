/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDAModelDatasets.ISDA_Results;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ISDACompliantPresentValueCreditDefaultSwapTest {

  private static final double NOTIONAL = 1e7;
  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");
  private static final ISDACompliantPresentValueCreditDefaultSwap PRICER = new ISDACompliantPresentValueCreditDefaultSwap();

  private static final LocalDate TODAY = LocalDate.of(2013, 4, 21);
  private static final LocalDate BASE_DATE = TODAY;
  // private static final HazardRateCurve HAZARD_RATE_CURVE;
  private static final double RECOVERY_RATE = 0.4;

  // points related to the yield curve (note: here we use yield curve points directly rather than fit from IR instruments)
  private static final LocalDate[] YC_DATES = new LocalDate[] {LocalDate.of(2013, 6, 27), LocalDate.of(2013, 8, 27), LocalDate.of(2013, 11, 27), LocalDate.of(2014, 5, 27), LocalDate.of(2015, 5, 27),
      LocalDate.of(2016, 5, 27), LocalDate.of(2018, 5, 27), LocalDate.of(2020, 5, 27), LocalDate.of(2023, 5, 27), LocalDate.of(2028, 5, 27), LocalDate.of(2033, 5, 27), LocalDate.of(2043, 5, 27) };

  private static final double[] DISCOUNT_FACT;
  private static final double[] YC_TIMES;
  // private static final ISDADateCurve YIELD_CURVE_ZERO_FLAT;
  // private static final ISDADateCurve YIELD_CURVE_5PC_FLAT;
  private static final ISDACompliantDateYieldCurve YIELD_CURVE_ZERO_FLAT;
  private static final ISDACompliantDateYieldCurve YIELD_CURVE_5PC_FLAT;

  private static final DayCount ACT365 = DayCounts.ACT_365;
  // examples
  private static final ISDA_Results[] EXAMPLE1 = ISDAModelDatasets.getExample1();
  private static final ISDA_Results[] EXAMPLE3 = ISDAModelDatasets.getExample3();

  static {
    final int ycPoints = YC_DATES.length;

    final double[] zeros = new double[ycPoints];
    final double[] fivePC = new double[ycPoints];
    Arrays.fill(fivePC, 0.05);

    DISCOUNT_FACT = new double[ycPoints];
    Arrays.fill(DISCOUNT_FACT, 1.0);
    YC_TIMES = new double[ycPoints];
    for (int i = 0; i < ycPoints; i++) {
      YC_TIMES[i] = ACT365.getDayCountFraction(BASE_DATE, YC_DATES[i]);
    }
    // YIELD_CURVE_ZERO_FLAT = new ISDADateCurve("ISDA", BASE_DATE, YC_DATES, zeros, OFFSET); // Remake: this constructor assumes ACT/365
    // YIELD_CURVE_5PC_FLAT = new ISDADateCurve("ISDA", BASE_DATE, YC_DATES, fivePC, OFFSET);
    // CURVES = new ISDAYieldCurveAndHazardRateCurveProvider(YIELD_CURVE_ZERO_FLAT, HAZARD_RATE_CURVE);
    YIELD_CURVE_ZERO_FLAT = new ISDACompliantDateYieldCurve(BASE_DATE, YC_DATES, zeros);
    YIELD_CURVE_5PC_FLAT = new ISDACompliantDateYieldCurve(BASE_DATE, YC_DATES, fivePC);

  }

  private void testISDA_Results(final ISDA_Results[] data, final ISDACompliantDateYieldCurve yieldCurve, final boolean debug) {
    if (debug) {
      System.out.println("ISDACompliantPremiumLegCalculatorAgaintISDATest2.testISDA_Results DO NOT PUSH WITH DEBUG ON\n");
    }
    int failCount = 0;
    final int constructionFailCount = 0;

    final int nEx = data.length;
    final int[] failedList = new int[nEx];
    for (int count = 0; count < nEx; count++) {

      final ISDA_Results res = data[count];

      final LocalDate today = res.today;
      final LocalDate stepinDate = today.plusDays(1); // aka effective date
      final LocalDate valueDate = addWorkDays(today, 3, DEFAULT_CALENDAR); // 3 working days on
      final LocalDate startDate = res.startDate;
      final LocalDate endDate = res.endDate;
      final Period tenor = Period.ofMonths(3); // TODO should be part of the CSV
      final StubType stubType = StubType.FRONTSHORT; // TODO ditto
      final boolean protectionStart = true; // TODO ditto

      // build an 'analytic' cds, then price with the new date free pricer
      double protectionLeg_new = 0;
      double premLeg_clean_new = 0;

      if (!today.isAfter(endDate)) {
        final boolean payAccOnDefault = true;
        try {
          final CDSAnalytic cds = new CDSAnalytic(today, stepinDate, valueDate, startDate, endDate, payAccOnDefault, tenor, stubType, protectionStart, RECOVERY_RATE);
          final AnalyticCDSPricer analPricer = new AnalyticCDSPricer();
          protectionLeg_new = NOTIONAL * analPricer.protectionLeg(cds, yieldCurve, res.creditCurve);
          final double rpv01_clean_new = NOTIONAL * analPricer.annuity(cds, yieldCurve, res.creditCurve, PriceType.CLEAN);
          premLeg_clean_new = res.fracSpread * rpv01_clean_new;
        } catch (final Exception e) {
        }

      }

      // price with code written to mimic ISDA c - i.e. date logic in the analytics
      final double rpv01_clean_ISDA = NOTIONAL *
          PRICER.pvPremiumLegPerUnitSpread(today, stepinDate, valueDate, startDate, endDate, true, tenor, stubType, yieldCurve, res.creditCurve, protectionStart, PriceType.CLEAN);
      final double rpv01_clean_ISDA_noAccOnDefault = NOTIONAL *
          PRICER.pvPremiumLegPerUnitSpread(today, stepinDate, valueDate, startDate, endDate, false, tenor, stubType, yieldCurve, res.creditCurve, protectionStart, PriceType.CLEAN);
      final double rpv01_dirty_ISDA = NOTIONAL *
          PRICER.pvPremiumLegPerUnitSpread(today, stepinDate, valueDate, startDate, endDate, true, tenor, stubType, yieldCurve, res.creditCurve, protectionStart, PriceType.DIRTY);
      final double contLeg_ISDA = NOTIONAL * PRICER.calculateProtectionLeg(today, stepinDate, valueDate, startDate, endDate, yieldCurve, res.creditCurve, RECOVERY_RATE, protectionStart);

      final double premLeg_clean_ISDA = res.fracSpread * rpv01_clean_ISDA;
      final double defaultAcc = res.fracSpread * (rpv01_clean_ISDA - rpv01_clean_ISDA_noAccOnDefault);
      final double rpv01_accrued = rpv01_dirty_ISDA - rpv01_clean_ISDA;
      final double accruedPrem = rpv01_accrued * res.fracSpread;

      // back out the accrued-days by inverting the accrued premium formula (which is ACT/360) - this matched the formula on the ISDA spread sheet
      final int accruedDays = (int) Math.round(360 * rpv01_accrued / NOTIONAL);

      if (debug) {
        System.out.println(count + "\t" + res.premiumLeg + "\t" + premLeg_clean_ISDA + "\t" + premLeg_clean_new + "\t\t" + res.protectionLeg + "\t" + contLeg_ISDA + "\t" + protectionLeg_new + "\t\t" +
            res.defaultAcc + "\t" + defaultAcc + "\t\t" + res.accruedPremium + "\t" + accruedPrem + "\t\t" + res.accruedDays + "\t" + accruedDays);
        try {
          // tests against ISDA c
          assertEquals("Premium Leg:", res.premiumLeg, premLeg_clean_ISDA, 1e-12 * NOTIONAL); // This should be 1e-15*NOTIONAL
          assertEquals("Protection Leg:", res.protectionLeg, contLeg_ISDA, 1e-11 * NOTIONAL); // ditto
          assertEquals("Default Acc:", res.defaultAcc, defaultAcc, 1e-13 * NOTIONAL);
          assertEquals("Accrued Premium: ", res.accruedPremium, accruedPrem, 1e-15 * NOTIONAL); // the accrued is trivial, so should be highly accurate
          assertEquals("Accrued Days: ", res.accruedDays, accruedDays);

          // tests date free vs date-full code
          assertEquals("Premium Leg:", premLeg_clean_ISDA, premLeg_clean_new, 1e-13 * NOTIONAL);
          assertEquals("Protection Leg:", contLeg_ISDA, protectionLeg_new, 1e-16 * NOTIONAL);
        } catch (final AssertionError e) {
          failedList[failCount++] = count;
        }
      } else {
        // tests against ISDA c
        assertEquals("Premium Leg:", res.premiumLeg, premLeg_clean_ISDA, 1e-12 * NOTIONAL); // This should be 1e-15*NOTIONAL
        assertEquals("Protection Leg:", res.protectionLeg, contLeg_ISDA, 1e-11 * NOTIONAL); // ditto
        assertEquals("Default Acc:", res.defaultAcc, defaultAcc, 1e-13 * NOTIONAL);
        assertEquals("Accrued Premium: ", res.accruedPremium, accruedPrem, 1e-15 * NOTIONAL); // the accrued is trivial, so should be highly accurate
        assertEquals("Accrued Days: ", res.accruedDays, accruedDays);

        // tests date free vs date-full code
        assertEquals("Premium Leg:", premLeg_clean_ISDA, premLeg_clean_new, 1e-13 * NOTIONAL);
        assertEquals("Protection Leg:", contLeg_ISDA, protectionLeg_new, 1e-15 * NOTIONAL);
      }
    }
    if (debug) {
      System.out.println("\nFailed to construct: " + constructionFailCount + " Failed: " + failCount);
      if (failCount > 0) {
        System.out.print("failed index:");
        for (int i = 0; i < failCount; i++) {
          System.out.print("\t" + failedList[i]);
        }
        System.out.print("\n");
      }
      System.out.print("\n");

    }
  }

  @Test
  // (enabled = false)
  public void example1Test() {
    testISDA_Results(EXAMPLE1, YIELD_CURVE_ZERO_FLAT, false);
  }

  @Test
  // (enabled = false)
  public void example3Test() {
    testISDA_Results(EXAMPLE3, YIELD_CURVE_ZERO_FLAT, false);
  }

  @Test
  // (enabled = false)
  public void exampleSheet1Test() {
    testISDA_Results(ISDAModelDatasetsSheetReader.loadSheet("yield_curve_flat_0pc.csv", RECOVERY_RATE), YIELD_CURVE_ZERO_FLAT, false);
  }

  @Test
  // (enabled = false)
  public void exampleSheet2Test() {
    testISDA_Results(ISDAModelDatasetsSheetReader.loadSheet("yield_curve_flat_5pc.csv", RECOVERY_RATE), YIELD_CURVE_5PC_FLAT, false);
  }

  @Test
  public void yieldCurveTest() {
    final int n = YC_TIMES.length;
    for (int i = 0; i < n; i++) {
      final double t = YC_TIMES[i];
      final double df = YIELD_CURVE_ZERO_FLAT.getDiscountFactor(t);
      assertEquals(DISCOUNT_FACT[i], df, 1e-10);
    }
  }

}
