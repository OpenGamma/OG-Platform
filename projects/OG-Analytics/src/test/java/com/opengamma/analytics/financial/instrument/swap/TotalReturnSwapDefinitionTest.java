/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondDataSets;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the description of a total return swap with an underlying asset and a funding leg.
 */
public class TotalReturnSwapDefinitionTest {

  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2012, 2, 9);
  private static final ZonedDateTime TERMINATION_DATE = DateUtils.getUTCDate(2012, 5, 9);

  // Bond (UKT)
  private static final BondFixedSecurityDefinition UKT14_DEFINITION = BondDataSets.bondUKT5_20140907();
  // Funding: unique fixed coupon in GBP
  private static final double NOTIONAL = 123456000;
  private static final double RATE = 0.0043;
  private static final CouponFixedDefinition FUNDING_FIXED_CPN_DEFINITION = new CouponFixedDefinition(UKT14_DEFINITION.getCurrency(),
      TERMINATION_DATE, EFFECTIVE_DATE, TERMINATION_DATE, 0.25, NOTIONAL, RATE);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_DEFINITION =
      new AnnuityDefinition<>(new CouponFixedDefinition[] {FUNDING_FIXED_CPN_DEFINITION }, UKT14_DEFINITION.getCalendar());
  private static final TotalReturnSwapDefinition TRS_DEFINITION = new BondTotalReturnSwapDefinition(EFFECTIVE_DATE, TERMINATION_DATE, FUNDING_LEG_DEFINITION, UKT14_DEFINITION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEffectiveDate() {
    new BondTotalReturnSwapDefinition(null, TERMINATION_DATE, FUNDING_LEG_DEFINITION, UKT14_DEFINITION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTerminationDate() {
    new BondTotalReturnSwapDefinition(EFFECTIVE_DATE, null, FUNDING_LEG_DEFINITION, UKT14_DEFINITION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFundingLeg() {
    new BondTotalReturnSwapDefinition(EFFECTIVE_DATE, TERMINATION_DATE, null, UKT14_DEFINITION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullUnderlyingBond() {
    new BondTotalReturnSwapDefinition(EFFECTIVE_DATE, TERMINATION_DATE, FUNDING_LEG_DEFINITION, null);
  }

  @Test
  public void getter() {
    assertEquals("TotalReturnSwapDefinition: getter", EFFECTIVE_DATE, TRS_DEFINITION.getEffectiveDate());
    assertEquals("TotalReturnSwapDefinition: getter", TERMINATION_DATE, TRS_DEFINITION.getTerminationDate());
    assertEquals("TotalReturnSwapDefinition: getter", FUNDING_LEG_DEFINITION, TRS_DEFINITION.getFundingLeg());
    assertEquals("TotalReturnSwapDefinition: getter", UKT14_DEFINITION, TRS_DEFINITION.getAsset());
  }

}
