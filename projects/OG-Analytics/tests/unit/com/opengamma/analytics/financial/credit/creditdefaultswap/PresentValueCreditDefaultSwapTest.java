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
import com.opengamma.analytics.financial.credit.CreditRating;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.FlatSurvivalCurve;
import com.opengamma.analytics.financial.credit.Region;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.Sector;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
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
  private static final double[] TIME = new double[] {0, 3, 5, 10 };
  private static final double[] RATES = new double[] {interestRate, interestRate, interestRate, interestRate };
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
    double parSpread = testCDS.getParSpreadCreditDefaultSwap(cds, yieldCurve, flatSurvivalCurve);

    // Report the result
    if (outputSchedule) {
      System.out.println("CDS par spread = " + parSpread);
      System.out.println("CDS PV = " + presentValue);
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