/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class PresentValueCreditDefaultSwapTest {
  
  private static final String buySellProtection = "Buy";

  private static final String protectionBuyer = "ABC";
  private static final String protectionSeller = "XYZ";
  private static final String referenceEntity = "C";

  private static final Currency currency = Currency.USD;

  private static final String debtSeniority = "Senior";
  private static final String restructuringClause = "NR";

  private static final Calendar calendar = new MondayToFridayCalendar("A");
  
  private static final ZonedDateTime startDate = DateUtils.getUTCDate(2012, 8, 21);
  private static final ZonedDateTime effectiveDate = DateUtils.getUTCDate(2012, 8, 22);
  private static final ZonedDateTime maturityDate = DateUtils.getUTCDate(2017, 9, 20);
  private static final ZonedDateTime valuationDate = DateUtils.getUTCDate(2012, 8, 22);
    
  private static final String scheduleGenerationMethod = "Backward";
  private static final String couponFrequency = "Quarterly";
  private static final String daycountFractionConvention = "ACT/360";
  private static final String businessdayAdjustmentConvention = "Following";

  private static final double notional = 10000000.0;
  private static final double parSpread = 0.0;

  private static final double valuationRecoveryRate = 0.40;
  private static final double curveRecoveryRate = 0.40;

  private static final boolean includeAccruedPremium = true;
  private static final boolean adjustMaturityDate = false;
  
  private static final int numberOfIntegrationSteps = 20;
  
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
  
 
  // TODO : Sort out what exception to throw
  @Test //(expectedExceptions = testng.TestException.class)
  public void testGetPresentValueCreditDefaultSwap() {
    
    // Hardcode the number of cashflows
    int n = 20;
    
    double pV = 0.0;
    
    // Array to hold the dummy schedule
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
}
