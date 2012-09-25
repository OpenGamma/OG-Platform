/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CalibrateSurvivalCurve;
import com.opengamma.analytics.financial.credit.CreditRating;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.FlatSurvivalCurve;
import com.opengamma.analytics.financial.credit.Region;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.Sector;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 *  Test of the implementation of the valuation model for a CDS 
 */
public class PresentValueCreditDefaultSwapTest {

  // ----------------------------------------------------------------------------------

  // TODO : Add all the tests
  // TODO : Move the calendar into a seperate TestCalendar class
  // TODO : Sort out what exceptions to throw from the test cases

  // ----------------------------------------------------------------------------------

  // CDS contract parameters

  private static final BuySellProtection buySellProtection = BuySellProtection.BUY;

  private static final String protectionBuyer = "ABC";
  private static final String protectionSeller = "XYZ";
  private static final String referenceEntityTicker = "MSFT";
  private static final String referenceEntityShortName = "Microsoft";
  private static final String referenceEntityREDCode = "ABC123";

  private static final Currency currency = Currency.USD;

  private static final DebtSeniority debtSeniority = DebtSeniority.SENIOR;
  private static final RestructuringClause restructuringClause = RestructuringClause.NORE;

  private static final CreditRating compositeRating = CreditRating.AA;
  private static final CreditRating impliedRating = CreditRating.A;

  private static final Sector sector = Sector.INDUSTRIALS;
  private static final Region region = Region.NORTHAMERICA;
  private static final String country = "United States";

  private static final Calendar calendar = new MyCalendar();

  private static final ZonedDateTime startDate = DateUtils.getUTCDate(2007, 10, 22);
  private static final ZonedDateTime effectiveDate = DateUtils.getUTCDate(2007, 10, 23);
  private static final ZonedDateTime maturityDate = DateUtils.getUTCDate(2012, 12, 20);
  private static final ZonedDateTime valuationDate = DateUtils.getUTCDate(2007, 10, 23);

  private static final StubType stubType = StubType.FRONTSHORT;
  private static final PeriodFrequency couponFrequency = PeriodFrequency.QUARTERLY;
  private static final DayCount daycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final BusinessDayConvention businessdayAdjustmentConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final boolean immAdjustMaturityDate = true;
  private static final boolean adjustMaturityDate = true;

  private static final double notional = 10000000.0;
  private static final double premiumLegCoupon = 100.0;
  private static final double valuationRecoveryRate = 0.40;
  private static final double curveRecoveryRate = 0.40;
  private static final boolean includeAccruedPremium = false;

  // Dummy yield curve
  private static final double interestRate = 0.0;
  private static final double[] TIME = new double[] {0, 3, 5, 10, 15, 40 };
  private static final double[] RATES = new double[] {interestRate, interestRate, interestRate, interestRate, interestRate, interestRate };
  private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(TIME, RATES, new LinearInterpolator1D());
  private static final YieldCurve yieldCurve = YieldCurve.from(R);

  // Construct a survival curve based on a flat hazard rate term structure (for testing purposes only)
  private static final FlatSurvivalCurve flatSurvivalCurve = new FlatSurvivalCurve(premiumLegCoupon, curveRecoveryRate);

  // ----------------------------------------------------------------------------------

  // Construct a CDS contract 
  private static final CreditDefaultSwapDefinition cds = new CreditDefaultSwapDefinition(buySellProtection,
      protectionBuyer,
      protectionSeller,
      referenceEntityTicker,
      referenceEntityShortName,
      referenceEntityREDCode,
      currency,
      debtSeniority,
      restructuringClause,
      compositeRating,
      impliedRating,
      sector,
      region,
      country,
      calendar,
      startDate,
      effectiveDate,
      maturityDate,
      valuationDate,
      stubType,
      couponFrequency,
      daycountFractionConvention,
      businessdayAdjustmentConvention,
      immAdjustMaturityDate,
      adjustMaturityDate,
      notional,
      premiumLegCoupon,
      valuationRecoveryRate,
      curveRecoveryRate,
      includeAccruedPremium);

  // -----------------------------------------------------------------------------------------------

  /*
  @Test
  //(expectedExceptions = testng.TestException.class)
  public void testGetPresentValueCreditDefaultSwap() {

    // -----------------------------------------------------------------------------------------------

    final boolean outputSchedule = false;

    // Call the constructor to create a CDS
    final PresentValueCreditDefaultSwap testCDS = new PresentValueCreditDefaultSwap();

    // Call the CDS PV calculator to get the current PV
    double presentValue = testCDS.getPresentValueCreditDefaultSwap(cds, yieldCurve, flatSurvivalCurve);

    // Call the CDS par spread calculator to get the par spread at time zero
    //double parSpread = testCDS.getParSpreadCreditDefaultSwap(cds, yieldCurve, flatSurvivalCurve);

    // Report the result
    if (outputSchedule) {
      //System.out.println("CDS par spread = " + parSpread);
      //System.out.println("CDS PV = " + presentValue);
    }

    // -----------------------------------------------------------------------------------------------
  }
   */

  // -----------------------------------------------------------------------------------------------

  @Test
  //(expectedExceptions = testng.TestException.class)
  public void testGetPresentValueCreditDefaultSwap() {

    // -----------------------------------------------------------------------------------------------

    final boolean outputSchedule = false;

    // -----------------------------------------------------------------------------------------------

    // Define the market data for this test

    double z = yieldCurve.getDiscountFactor(0);

    int numberOfTenors = 10;

    final ZonedDateTime[] tenors = new ZonedDateTime[numberOfTenors];

    final double[] marketSpreads = new double[numberOfTenors];

    // Set the tenors at which we have market observed par CDS spread quotes
    tenors[0] = DateUtils.getUTCDate(2008, 12, 20);
    tenors[1] = DateUtils.getUTCDate(2009, 6, 20);
    tenors[2] = DateUtils.getUTCDate(2010, 6, 20);
    tenors[3] = DateUtils.getUTCDate(2011, 6, 20);
    tenors[4] = DateUtils.getUTCDate(2012, 6, 20);
    tenors[5] = DateUtils.getUTCDate(2014, 6, 20);
    tenors[6] = DateUtils.getUTCDate(2017, 6, 20);
    tenors[7] = DateUtils.getUTCDate(2022, 6, 20);
    tenors[8] = DateUtils.getUTCDate(2030, 6, 20);
    tenors[9] = DateUtils.getUTCDate(2040, 6, 20);

    marketSpreads[0] = 100.0;
    marketSpreads[1] = 100.0;
    marketSpreads[2] = 100.0;
    marketSpreads[3] = 100.0;
    marketSpreads[4] = 100.0;
    marketSpreads[5] = 100.0;
    marketSpreads[6] = 100.0;
    marketSpreads[7] = 100.0;
    marketSpreads[8] = 100.0;
    marketSpreads[9] = 100.0;

    // -----------------------------------------------------------------------------------------------

    // Create the interpolation/extrapolation object

    String interpolatorName = Interpolator1DFactory.LINEAR;
    String leftExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    String rightExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;

    Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);

    // -----------------------------------------------------------------------------------------------

    // Step 1 - Calibrate the survival curve to the market observed data

    // Create a calibration CDS (will be a modified version of the baseline CDS)
    CreditDefaultSwapDefinition calibrationCDS = cds;

    // Get the daycount fraction convention of the CDS to be calibrated
    DayCount dayCount = calibrationCDS.getDayCountFractionConvention();

    // Set the recovery rate of the calibration CDS to be the rate used for the curve calibration
    calibrationCDS = calibrationCDS.withCurveRecoveryRate(curveRecoveryRate);

    GenerateCreditDefaultSwapPremiumLegSchedule temp = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Convert the ZonedDateTime tenors into doubles (measured from valuationDate)
    double[] tenorsAsDoubles = temp.convertDatesToDoubles(valuationDate, tenors, dayCount);

    // Create a survival curve object
    final CalibrateSurvivalCurve survivalCurve = new CalibrateSurvivalCurve();

    // Calibrate the survival curve to the market observed par CDS spreads (returned as a vector of doubles)
    double[] calibratedSurvivalCurve = survivalCurve.getCalibratedSurvivalCurve(calibrationCDS, tenors, marketSpreads, yieldCurve);

    // Build the survival curve interpolation object from the calibrated survival probabilities
    InterpolatedDoublesCurve S = InterpolatedDoublesCurve.from(tenorsAsDoubles, calibratedSurvivalCurve, interpolator);

    // Construct the survival curve from the DoublesCurve (complete with interpolator/extrapolator)
    //final SurvivalCurve testCurve = SurvivalCurve.from(S);

    // -----------------------------------------------------------------------------------------------

    // Step 2 - Calculate the CDS PV

    // Call the constructor to create a CDS whose PV we will compute
    //final PresentValueCreditDefaultSwap testCDS = new PresentValueCreditDefaultSwap();

    // Call the CDS PV calculator to get the current PV
    //double presentValue = testCDS.getPresentValueCreditDefaultSwap(cds, yieldCurve, testCurve/*survivalCurve*/);

    // Report the result
    if (outputSchedule) {
      //System.out.println("CDS par spread = " + parSpread);
      //System.out.println("CDS PV = " + presentValue);

      for (int i = 0; i < numberOfTenors; i++) {
        System.out.println(calibratedSurvivalCurve[i]);
      }
    }

    // -----------------------------------------------------------------------------------------------
  }

  // -----------------------------------------------------------------------------------------------

  // Bespoke calendar class (have made this public - may want to change this)
  public static class MyCalendar implements Calendar {

    private static final Calendar weekend = new MondayToFridayCalendar("GBP");

    @Override
    public boolean isWorkingDay(LocalDate date) {

      if (!weekend.isWorkingDay(date)) {
        return false;
      }

      /*
      // Custom bank holiday
      if (date.equals(LocalDate.of(2012, 8, 27))) {
        return false;
      }

      // Custom bank holiday
      if (date.equals(LocalDate.of(2012, 8, 28))) {
        return false;
      }

      // Custom bank holiday
      if (date.equals(LocalDate.of(2017, 8, 28))) {
        return false;
      }

      // Custom bank holiday
      if (date.equals(LocalDate.of(2017, 8, 29))) {
        return false;
      }
       */

      return true;
    }

    @Override
    public String getConventionName() {
      return "";
    }

  }

  // -----------------------------------------------------------------------------------------------
}

//-----------------------------------------------------------------------------------------------