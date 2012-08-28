/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CouponFrequency;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.ScheduleGenerationMethod;
import com.opengamma.analytics.financial.credit.creditdefaultswap.PresentValueCreditDefaultSwapTest.MyCalendar;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 *  Test the implementation of the schedule generation methods for the premium leg of a CDS
 */
public class GenerateCreditDefaultSwapPremiumLegScheduleTest {
  
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
  
  /*
  @Test
  public void testIMMAdjustedMaturityDate() {
    
    System.out.println("Running schedule generation tests ...");

    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();
    
    //final PresentValueCreditDefaultSwap CDS_2 = new PresentValueCreditDefaultSwap();

    cashflowSchedule.getCreditDefaultSwapPremiumLegSchedule(CDS_1);

    //ZonedDateTime immAdjustedMaturityDate;
    ZonedDateTime maturityDate = DateUtils.getUTCDate(2016, 12, 19);

    int numberOfTests = 2;

    for (int i = 0; i < numberOfTests; i++) {
      maturityDate = maturityDate.plusDays(1);

      //immAdjustedMaturityDate = cashflowSchedule.getCreditDefaultSwapPremiumLegSchedule(CDS_1);
    }
  }
  */
}
