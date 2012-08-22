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
  private static final double parSpread = 100.0;

  private static final double valuationRecoveryRate = 0.40;
  private static final double curveRecoveryRate = 0.40;

  private static final boolean includeAccruedPremium = true;
  private static final boolean adjustMaturityDate = false;
  
  private static final int numberOfIntegrationSteps = 10;
  
  // TODO : replace this with something more meaningful
  // Dummy yield curve
  private static final double[] TIME = new double[] {0, 3, 5};
  private static final double[] RATES = new double[] {0.0, 0.0, 0.0};
  //private static final double[] DF_VALUES = new double[] {Math.exp(-0.03), Math.exp(-0.08), Math.exp(-0.15)};
  private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(TIME, RATES, new LinearInterpolator1D());
  private static final YieldCurve yieldCurve = YieldCurve.from(R);
  
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
                                                                                            yieldCurve);
  
 
  // TODO : Sort out what exception to throw
  @Test //(expectedExceptions = testng.TestException.class)
  public void testGetPresentValueCreditDefaultSwap() {
    
    int n = 20;
    
    double pV = 0.0;
    double h = 0.01;
    
    double cashflowSchedule[][] = new double [n + 1][2];
    
    double irCurve[][] = new double [n + 1][2];
    double survCurve[][] = new double [n + 1][2];

    System.out.println("There are " + cashflowSchedule.length + " cashflow dates (including time zero)");
    
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
      
      irCurve[i][0] = cashflowSchedule[i][0];

      /*
      if (i > 0) {
        irCurve[i][1] = yieldCurve.getDiscountFactor(t);
      } else {
        irCurve[i][1] = 0.0;
      }
      */

      irCurve[i][1] = yieldCurve.getDiscountFactor(t);
      
      survCurve[i][0] = cashflowSchedule[i][0];
      survCurve[i][1] = Math.exp(-h * t);
      
      //System.out.println(cashflowSchedule[i][0] + " " + cashflowSchedule[i][1]);
      System.out.println(irCurve[i][0] + " " + irCurve[i][1]);
      //System.out.println(survCurve[i][0] + " " + survCurve[i][1]);
    }
    
    //System.out.println("Generated schedule");
    
    // -----------------------------------------------------------------------------------------------
    
    // Call the ctor to create a CDS
    final PresentValueCreditDefaultSwap cds = new PresentValueCreditDefaultSwap();
    
    // Call the CDS PV calculator to get the current PV
    pV = cds.getPresentValueCreditDefaultSwap(CDS_1, cashflowSchedule, irCurve, survCurve);
    
    System.out.println("CDS PV = " + pV);
    
    // -----------------------------------------------------------------------------------------------
  
  }

}
