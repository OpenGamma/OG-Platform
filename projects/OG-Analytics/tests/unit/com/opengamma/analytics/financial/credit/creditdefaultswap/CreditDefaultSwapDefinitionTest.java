/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CreditDefaultSwapDefinitionTest {
  
  private static final String buySellProtection = "Buy";

  private static final String protectionBuyer = "ABC";
  private static final String protectionSeller = "XYZ";
  private static final String referenceEntity = "C";

  private static final Currency currency = Currency.USD;

  private static final String debtSeniority = "Senior";
  private static final String restructuringClause = "NR";

  private static final Calendar calendar = new MondayToFridayCalendar("A");

  private static final String scheduleGenerationMethod = "Backward";
  private static final String couponFrequency = "Quarterly";
  private static final String daycountFractionConvention = "ACT/360";
  private static final String businessdayAdjustmentConvention = "Following";

  private static final double notional = 10000000.0;
  private static final double parSpread = 60.0;

  private static final double valuationRecoveryRate = 0.40;
  private static final double curveRecoveryRate = 0.40;

  private static final boolean includeAccruedPremium = true;
  private static final boolean adjustMaturityDate = false;

  private static final CreditDefaultSwapDefinition CDS_DEFINITION = new CreditDefaultSwapDefinition(buySellProtection, 
                                                                                                    protectionBuyer, 
                                                                                                    protectionSeller, 
                                                                                                    referenceEntity,
                                                                                                    currency, 
                                                                                                    debtSeniority, 
                                                                                                    restructuringClause, 
                                                                                                    calendar,
                                                                                                    //startDate,
                                                                                                    //effectiveDate,
                                                                                                    //maturityDate,
                                                                                                    //valuationDate,
                                                                                                    scheduleGenerationMethod,
                                                                                                    couponFrequency,
                                                                                                    daycountFractionConvention,
                                                                                                    businessdayAdjustmentConvention,
                                                                                                    notional, 
                                                                                                    parSpread, 
                                                                                                    valuationRecoveryRate, 
                                                                                                    curveRecoveryRate, 
                                                                                                    includeAccruedPremium,
                                                                                                    adjustMaturityDate); //, 
                                                                                                    //interestRateTenors,
                                                                                                    //interestRates,
                                                                                                    //creditSpreadTenors,
                                                                                                    //creditSpreads);

  public void testNullBuySellProtectionFlag() {
    assertEquals(null, CDS_DEFINITION.getBuySellProtection(), 1e-15);
  }


     
}