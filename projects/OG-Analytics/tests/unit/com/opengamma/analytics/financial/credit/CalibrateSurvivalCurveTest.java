/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.creditdefaultswap.PresentValueCreditDefaultSwapTest.MyCalendar;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Class to test the implementation of the calibration of the survival curve
 */
public class CalibrateSurvivalCurveTest {

  // ---------------------------------------------------------------------------------------

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
  private static final ZonedDateTime valuationDate = DateUtils.getUTCDate(2009, 4, 25);

  private static final ScheduleGenerationMethod scheduleGenerationMethod = ScheduleGenerationMethod.BACKWARD;
  private static final PeriodFrequency couponFrequency = PeriodFrequency.QUARTERLY;
  private static final DayCount daycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final BusinessDayConvention businessdayAdjustmentConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final boolean adjustMaturityDate = true;

  private static final double notional = 10000000.0;
  private static final double parSpread = 60.0;
  private static final double valuationRecoveryRate = 0.40;
  private static final double curveRecoveryRate = 0.40;
  private static final boolean includeAccruedPremium = true;

  // Dummy yield curve
  private static final double interestRate = 0.05;
  private static final double[] TIME = new double[] {0, 3, 5, 10 };
  private static final double[] RATES = new double[] {interestRate, interestRate, interestRate, interestRate };
  private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(TIME, RATES, new LinearInterpolator1D());
  private static final YieldCurve yieldCurve = YieldCurve.from(R);

  // Construct a survival curve based on a flat hazard rate term structure (for testing purposes only)
  private static final FlatSurvivalCurve survivalCurve = new FlatSurvivalCurve(parSpread, curveRecoveryRate);

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
      scheduleGenerationMethod,
      couponFrequency,
      daycountFractionConvention,
      businessdayAdjustmentConvention,
      adjustMaturityDate,
      notional,
      parSpread,
      valuationRecoveryRate,
      curveRecoveryRate,
      includeAccruedPremium);

  // ---------------------------------------------------------------------------------------

  @Test
  public void testCalibrateSurvivalCurve() {

    System.out.println("Running tests on survival curve construction ...");

    //final CalibrateSurvivalCurve curve = new CalibrateSurvivalCurve();

    //double[][] calibratedSurvivalCurve = curve.getCalibratedSurvivalCurve(cds, null, null);
  }
  // ---------------------------------------------------------------------------------------
}
