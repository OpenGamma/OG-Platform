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
  private static final ZonedDateTime valuationDate = DateUtils.getUTCDate(2007, 10, 23);

  private static final StubType stubType = StubType.FRONTSHORT;
  private static final PeriodFrequency couponFrequency = PeriodFrequency.QUARTERLY;
  private static final DayCount daycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final BusinessDayConvention businessdayAdjustmentConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final boolean immAdjustMaturityDate = true;
  private static final boolean adjustMaturityDate = true;

  private static final double notional = 10000000.0;
  private static final double parSpread = 100.0;
  private static final double valuationRecoveryRate = 0.40;
  private static final double curveRecoveryRate = 0.40;
  private static final boolean includeAccruedPremium = false;

  // Dummy yield curve
  private static final double interestRate = 0.0;
  private static final double[] TIME = new double[] {0, 3, 5, 10 };
  private static final double[] RATES = new double[] {interestRate, interestRate, interestRate, interestRate };
  private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(TIME, RATES, new LinearInterpolator1D());
  private static final YieldCurve yieldCurve = YieldCurve.from(R);

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
      parSpread,
      valuationRecoveryRate,
      curveRecoveryRate,
      includeAccruedPremium);

  // ---------------------------------------------------------------------------------------

  //@Test
  public void testRootFinder() {

    System.out.println("Root finder test ...");

  }

  @Test
  public void testCalibrateSurvivalCurve() {

    final boolean outputResults = false;

    int numberOfTenors = 5;

    final ZonedDateTime[] tenors = new ZonedDateTime[5];

    tenors[0] = cds.getValuationDate().plusYears(1);
    tenors[1] = cds.getValuationDate().plusYears(3);
    tenors[2] = cds.getValuationDate().plusYears(5);
    tenors[3] = cds.getValuationDate().plusYears(7);
    tenors[4] = cds.getValuationDate().plusYears(10);

    double[] parCDSSpreads = new double[numberOfTenors];

    parCDSSpreads[0] = 50.0;
    parCDSSpreads[0] = 50.0;
    parCDSSpreads[0] = 50.0;
    parCDSSpreads[0] = 50.0;
    parCDSSpreads[0] = 50.0;

    final CalibrateSurvivalCurve curve = new CalibrateSurvivalCurve();

    double[][] calibratedSurvivalCurve = curve.getCalibratedSurvivalCurve(cds, tenors, parCDSSpreads, yieldCurve);
  }

  // ---------------------------------------------------------------------------------------

}
