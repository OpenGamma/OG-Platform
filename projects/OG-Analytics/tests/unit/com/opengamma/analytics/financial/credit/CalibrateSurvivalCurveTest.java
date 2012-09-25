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

  private static final boolean immAdjustMaturityDate = false;
  private static final boolean adjustMaturityDate = false;

  private static final double notional = 10000000.0;
  private static final double premiumLegCoupon = 60.0;
  private static final double valuationRecoveryRate = 0.40;
  private static double curveRecoveryRate = 0.40;
  private static final boolean includeAccruedPremium = false;

  // Dummy yield curve
  private static final double interestRate = 0.0;
  private static final double[] TIME = new double[] {0, 3, 5, 10, 15, 40 };
  private static final double[] RATES = new double[] {interestRate, interestRate, interestRate, interestRate, interestRate, interestRate };
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
      premiumLegCoupon,
      valuationRecoveryRate,
      curveRecoveryRate,
      includeAccruedPremium);

  // ---------------------------------------------------------------------------------------

  @Test
  public void testCalibrateSurvivalCurve() {

    final boolean outputResults = true;

    int numberOfTenors = 10;

    final ZonedDateTime[] tenors = new ZonedDateTime[numberOfTenors];

    double[] marketSpreads = new double[numberOfTenors];

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

    // Flat (tight)
    /*
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
    curveRecoveryRate = 0.40;
    */

    // Flat (distressed)
    /*
        marketSpreads[0] = 1000.0;
        marketSpreads[1] = 1000.0;
        marketSpreads[2] = 1000.0;
        marketSpreads[3] = 1000.0;
        marketSpreads[4] = 1000.0;
        marketSpreads[5] = 1000.0;
        marketSpreads[6] = 1000.0;
        marketSpreads[7] = 1000.0;
        marketSpreads[8] = 1000.0;
        marketSpreads[9] = 1000.0;
        curveRecoveryRate = 0.40;
    */
    // Flat (blown)
    /*
        marketSpreads[0] = 100000.0;
        marketSpreads[1] = 100000.0;
        marketSpreads[2] = 100000.0;
        marketSpreads[3] = 100000.0;
        marketSpreads[4] = 100000.0;
        marketSpreads[5] = 100000.0;
        marketSpreads[6] = 100000.0;
        marketSpreads[7] = 100000.0;
        marketSpreads[8] = 100000.0;
        marketSpreads[9] = 100000.0;
        curveRecoveryRate = 0.40;
    */
    // Zig-zag (Tight)
    /*
    marketSpreads[0] = 50.0;
    marketSpreads[1] = 60.0;
    marketSpreads[2] = 50.0;
    marketSpreads[3] = 60.0;
    marketSpreads[4] = 50.0;
    marketSpreads[5] = 40.0;
    marketSpreads[6] = 50.0;
    marketSpreads[7] = 60.0;
    marketSpreads[8] = 50.0;
    marketSpreads[9] = 60.0;
    curveRecoveryRate = 0.40;
    */

    // Zig-zag (Distressed)
    /*
        marketSpreads[0] = 500.0;
        marketSpreads[1] = 600.0;
        marketSpreads[2] = 500.0;
        marketSpreads[3] = 600.0;
        marketSpreads[4] = 500.0;
        marketSpreads[5] = 400.0;
        marketSpreads[6] = 500.0;
        marketSpreads[7] = 600.0;
        marketSpreads[8] = 500.0;
        marketSpreads[9] = 600.0;
        curveRecoveryRate = 0.40;
    */
    // Upward, gentle
    /*
        marketSpreads[0] = 100.0;
        marketSpreads[1] = 120.0;
        marketSpreads[2] = 140.0;
        marketSpreads[3] = 160.0;
        marketSpreads[4] = 180.0;
        marketSpreads[5] = 200.0;
        marketSpreads[6] = 220.0;
        marketSpreads[7] = 240.0;
        marketSpreads[8] = 260.0;
        marketSpreads[9] = 280.0;
        curveRecoveryRate = 0.40;
    */
    // Upward, steep

    /* marketSpreads[0] = 100.0;
     marketSpreads[1] = 200.0;
     marketSpreads[2] = 300.0;
     marketSpreads[3] = 400.0;
     marketSpreads[4] = 500.0;
     marketSpreads[5] = 600.0;
     marketSpreads[6] = 700.0;
     marketSpreads[7] = 800.0;
     marketSpreads[8] = 900.0;
     marketSpreads[9] = 1000.0;
     curveRecoveryRate = 0.40;
     */

    // Upward, steep short end
    /*
        marketSpreads[0] = 100.0;
        marketSpreads[1] = 200.0;
        marketSpreads[2] = 300.0;
        marketSpreads[3] = 400.0;
        marketSpreads[4] = 500.0;
        marketSpreads[5] = 520.0;
        marketSpreads[6] = 540.0;
        marketSpreads[7] = 560.0;
        marketSpreads[8] = 580.0;
        marketSpreads[9] = 600.0;
        curveRecoveryRate = 0.40;
    */
    // Upward, steep long end

    /*  marketSpreads[0] = 100.0;
      marketSpreads[1] = 120.0;
      marketSpreads[2] = 140.0;
      marketSpreads[3] = 160.0;
      marketSpreads[4] = 180.0;
      marketSpreads[5] = 280.0;
      marketSpreads[6] = 380.0;
      marketSpreads[7] = 480.0;
      marketSpreads[8] = 580.0;
      marketSpreads[9] = 680.0;
      curveRecoveryRate = 0.40;
    */
    // Downward, gentle

    marketSpreads[0] = 280.0;
    marketSpreads[1] = 260.0;
    marketSpreads[2] = 240.0;
    marketSpreads[3] = 220.0;
    marketSpreads[4] = 200.0;
    marketSpreads[5] = 180.0;
    marketSpreads[6] = 160.0;
    marketSpreads[7] = 140.0;
    marketSpreads[8] = 120.0;
    marketSpreads[9] = 100.0;
    curveRecoveryRate = 0.40;

    // Downward, steep
    /*
    marketSpreads[0] = 1000.0;
    marketSpreads[1] = 900.0;
    marketSpreads[2] = 800.0;
    marketSpreads[3] = 700.0;
    marketSpreads[4] = 600.0;
    marketSpreads[5] = 500.0;
    marketSpreads[6] = 400.0;
    marketSpreads[7] = 300.0;
    marketSpreads[8] = 200.0;
    marketSpreads[9] = 100.0;
    curveRecoveryRate = 0.40;
    */

    // Downward, steep short end
    /*
    marketSpreads[0] = 600.0;
    marketSpreads[1] = 500.0;
    marketSpreads[2] = 400.0;
    marketSpreads[3] = 300.0;
    marketSpreads[4] = 200.0;
    marketSpreads[5] = 180.0;
    marketSpreads[6] = 160.0;
    marketSpreads[7] = 140.0;
    marketSpreads[8] = 120.0;
    marketSpreads[9] = 100.0;
    curveRecoveryRate = 0.40;
    */

    // Downward, steep long end
    /*
    marketSpreads[0] = 680.0;
    marketSpreads[1] = 660.0;
    marketSpreads[2] = 640.0;
    marketSpreads[3] = 620.0;
    marketSpreads[4] = 600.0;
    marketSpreads[5] = 580.0;
    marketSpreads[6] = 480.0;
    marketSpreads[7] = 380.0;
    marketSpreads[8] = 280.0;
    marketSpreads[9] = 180.0;
    curveRecoveryRate = 0.40;
    */

    // Inverted Cavale
    /*
    marketSpreads[0] = 1774.0;
    marketSpreads[1] = 1805.0;
    marketSpreads[2] = 1856.0;
    marketSpreads[3] = 1994.0;
    marketSpreads[4] = 2045.0;
    marketSpreads[5] = 2141.0;
    marketSpreads[6] = 2243.0;
    marketSpreads[7] = 2559.0;
    marketSpreads[8] = 3072.0;
    marketSpreads[9] = 3865.0;
    curveRecoveryRate = 0.20;
    */

    // Cavale
    /*
    marketSpreads[0] = 3865.0;
    marketSpreads[1] = 3072.0;
    marketSpreads[2] = 2559.0;
    marketSpreads[3] = 2243.0;
    marketSpreads[4] = 2141.0;
    marketSpreads[5] = 2045.0;
    marketSpreads[6] = 1944.0;
    marketSpreads[7] = 1856.0;
    marketSpreads[8] = 1805.0;
    marketSpreads[9] = 1774.0;
    curveRecoveryRate = 0.20;
    */
    // BCPN
    /*
    marketSpreads[0] = 780.0;
    marketSpreads[1] = 812.0;
    marketSpreads[2] = 803.0;
    marketSpreads[3] = 826.0;
    marketSpreads[4] = 874.0;
    marketSpreads[5] = 896.0;
    marketSpreads[6] = 868.0;
    marketSpreads[7] = 838.0;
    marketSpreads[8] = 800.0;
    marketSpreads[9] = 780.0;
    curveRecoveryRate = 0.40;
    */

    // Create a calibration CDS (will be a modified version of the baseline CDS)
    CreditDefaultSwapDefinition calibrationCDS = cds;

    // Set the recovery rate of the calibration CDS used for the curve calibration
    calibrationCDS = calibrationCDS.withCurveRecoveryRate(curveRecoveryRate);

    // Create a survival curve object
    final CalibrateSurvivalCurve curve = new CalibrateSurvivalCurve();

    // Calibrate the survival curve to the market observed par CDS spreads
    //double[] calibratedSurvivalCurve = curve.getCalibratedSurvivalCurve(calibrationCDS, tenors, marketSpreads, yieldCurve);

    if (outputResults) {
      for (int i = 0; i < numberOfTenors; i++) {
        //  System.out.println(calibratedSurvivalCurve[i]);
      }
    }
  }
  // ---------------------------------------------------------------------------------------

}
