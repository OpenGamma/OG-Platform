/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondDataSetsGbp;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the description of a bond total return swap with an underlying bond and a funding leg.
 */
public class BondTotalReturnSwapTest {

  private static final ZonedDateTime EFFECTIVE_DATE_1 = DateUtils.getUTCDate(2012, 2, 9);
  private static final ZonedDateTime TERMINATION_DATE_1 = DateUtils.getUTCDate(2012, 5, 9);

  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2012, 2, 2); // Before effective date.

  private static final double EFFECTIVE_TIME_1_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, EFFECTIVE_DATE_1);
  private static final double TERMINATION_TIME_1_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, TERMINATION_DATE_1);

  private static final double NOTIONAL_TRS = 123456000;
  // Bond (UKT)
  private static final double NOTIONAL_BND = 100000000;
  private static final BondFixedSecurityDefinition UKT14_DEFINITION = BondDataSetsGbp.bondUKT5_20140907();
  private static final BondFixedSecurity UKT14_1_1 = UKT14_DEFINITION.toDerivative(REFERENCE_DATE_1, EFFECTIVE_DATE_1);
  // Funding: unique fixed coupon in GBP: receive TRS bond, pay funding
  private static final double RATE = 0.0043;
  private static final CouponFixedDefinition FUNDING_FIXED_CPN_REC_DEFINITION = new CouponFixedDefinition(UKT14_DEFINITION.getCurrency(),
      TERMINATION_DATE_1, EFFECTIVE_DATE_1, TERMINATION_DATE_1, 0.25, NOTIONAL_TRS, RATE);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_FIXED_REC_DEFINITION =
      new AnnuityDefinition<>(new CouponFixedDefinition[] {FUNDING_FIXED_CPN_REC_DEFINITION }, UKT14_DEFINITION.getCalendar());
  private static final Annuity<? extends Payment> FUNDING_LEG_FIXED_REC_1 = FUNDING_LEG_FIXED_REC_DEFINITION.toDerivative(REFERENCE_DATE_1);
  private static final BondTotalReturnSwap TRS_PAY_FIXED_REC_1 = new BondTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, FUNDING_LEG_FIXED_REC_1, UKT14_1_1, -NOTIONAL_BND);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFundingLeg() {
    new BondTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, null, UKT14_1_1, -NOTIONAL_BND);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBond() {
    new BondTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, FUNDING_LEG_FIXED_REC_1, null, -NOTIONAL_BND);
  }

  @Test
  public void getter() {
    assertEquals("BondTotalReturnSwap: getter", EFFECTIVE_TIME_1_1, TRS_PAY_FIXED_REC_1.getEffectiveTime());
    assertEquals("BondTotalReturnSwap: getter", TERMINATION_TIME_1_1, TRS_PAY_FIXED_REC_1.getTerminationTime());
    assertEquals("BondTotalReturnSwap: getter", FUNDING_LEG_FIXED_REC_1, TRS_PAY_FIXED_REC_1.getFundingLeg());
    assertEquals("BondTotalReturnSwap: getter", UKT14_1_1, TRS_PAY_FIXED_REC_1.getAsset());
    assertEquals("BondTotalReturnSwap: getter", -NOTIONAL_BND, TRS_PAY_FIXED_REC_1.getQuantity());
  }

}
