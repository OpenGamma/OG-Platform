/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CouponFrequency;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.ScheduleGenerationMethod;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 *  Test of the implementation of the valuation model for a CDS 
 */
public class PresentValueCreditDefaultSwapTest {
  
  private static final BuySellProtection buySellProtection = BuySellProtection.BUY;

  private static final String protectionBuyer = "ABC";
  private static final String protectionSeller = "XYZ";
  private static final String referenceEntity = "C";

  private static final Currency currency = Currency.GBP;

  private static final DebtSeniority debtSeniority = DebtSeniority.SENIOR;
  private static final RestructuringClause restructuringClause = RestructuringClause.NORE;

  private static final Calendar calendar = new MyCalendar();

  private static final ZonedDateTime startDate = DateUtils.getUTCDate(2012, 8, 24);
  private static final ZonedDateTime effectiveDate = DateUtils.getUTCDate(2012, 8, 22);
  private static final ZonedDateTime maturityDate = DateUtils.getUTCDate(2017, 8, 26);
  private static final ZonedDateTime valuationDate = DateUtils.getUTCDate(2012, 8, 24);

  private static final ScheduleGenerationMethod scheduleGenerationMethod = ScheduleGenerationMethod.BACKWARD;
  private static final CouponFrequency couponFrequency = CouponFrequency.QUARTERLY;
  private static final String daycountFractionConvention = "ACT/360";
  private static final String businessdayAdjustmentConvention = "Following";

  //private static final FollowingBusinessDayConvention businessdayAdjustmentConvention = new FollowingBusinessDayConvention();

  private static final double notional = 10000000.0;
  private static final double parSpread = 60.0;

  private static final double valuationRecoveryRate = 1.0;
  private static final double curveRecoveryRate = 0.40;

  private static final boolean includeAccruedPremium = true;
  private static final boolean adjustMaturityDate = true;

  private static final int numberOfIntegrationSteps = 12;

  // Dummy yield curve
  private static final double[] TIME = new double[] {0, 3, 5};
  private static final double[] RATES = new double[] {0.05, 0.05, 0.05};
  private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(TIME, RATES, new LinearInterpolator1D());
  private static final YieldCurve yieldCurve = YieldCurve.from(R);

  // Dummy survival curve (proxied by a yield curve for now)
  private static final double[] survivalTIME = new double[] {0, 3, 5};
  private static final double[] survivalProbs = new double[] {0.01, 0.01, 0.01};
  private static final InterpolatedDoublesCurve S = InterpolatedDoublesCurve.from(survivalTIME, survivalProbs, new LinearInterpolator1D());
  private static final YieldCurve survivalCurve = YieldCurve.from(S);
  
  // Create a CDS contract object
  private static final CreditDefaultSwapDefinition CDS_1 = new CreditDefaultSwapDefinition(buySellProtection, 
                                                                                            protectionBuyer, 
                                                                                            protectionSeller, 
                                                                                            referenceEntity,
                                                                                            currency, 
                                                                                            debtSeniority, 
                                                                                            restructuringClause, 
                                                                                            calendar,
                                                                                            startDate,
                                                                                            effectiveDate,
                                                                                            maturityDate,
                                                                                            valuationDate,
                                                                                            scheduleGenerationMethod,
                                                                                            couponFrequency,
                                                                                            daycountFractionConvention,
                                                                                            businessdayAdjustmentConvention,
                                                                                            notional, 
                                                                                            parSpread, 
                                                                                            valuationRecoveryRate, 
                                                                                            curveRecoveryRate, 
                                                                                            includeAccruedPremium,
                                                                                            adjustMaturityDate,
                                                                                            numberOfIntegrationSteps,
                                                                                            yieldCurve,
                                                                                            survivalCurve);
  
  // -----------------------------------------------------------------------------------------------
  
  /*
  // TODO : Sort out what exception to throw
  @Test //(expectedExceptions = testng.TestException.class)
  public void testGetPresentValueCreditDefaultSwap() {
    
    // Hardcode the number of cashflows for testing purposes - will change this when implement the schedule generator
    int n = 20;
    
    double pV = 0.0;
    
    // Array to hold the dummy cashflow schedule
    double cashflowSchedule[][] = new double [n + 1][2];
    
    // -----------------------------------------------------------------------------------------------
    
    // Generate a dummy cashflow schedule 'object'
    for(int i = 0; i <= n; i++)
    {
      // Calculate the dummy time
      double t = (double)i/4;
      
      // Store dummy time in the cashflow schedule 'object'
      cashflowSchedule[i][0] = t;
      
      // Calculate the time difference (in years) between consecutive coupon payment dates 
      if(i > 0) {
        cashflowSchedule[i][1] = cashflowSchedule[i][0] - cashflowSchedule[i - 1][0];
      }
    }
    
    // -----------------------------------------------------------------------------------------------
    
    // Call the constructor to create a CDS
    final PresentValueCreditDefaultSwap cds = new PresentValueCreditDefaultSwap();
    
    // Call the CDS PV calculator to get the current PV
    pV = cds.getPresentValueCreditDefaultSwap(CDS_1, cashflowSchedule);
    
    // Report the result
    System.out.println("CDS PV = " + pV);
    
    // -----------------------------------------------------------------------------------------------
  }
  */
  
// -----------------------------------------------------------------------------------------------

  // Bespoke calendar class (have made this public - may want to change this)
  public static class MyCalendar implements Calendar {
    
    private static final Calendar weekend = new MondayToFridayCalendar("GBP");

    @Override
    public boolean isWorkingDay(LocalDate date) {
      
      if (!weekend.isWorkingDay(date)) {
        return false; 
      }
      
      // Custom bank holiday
      if (date.equals(LocalDate.of(2012, 8, 27))) {
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