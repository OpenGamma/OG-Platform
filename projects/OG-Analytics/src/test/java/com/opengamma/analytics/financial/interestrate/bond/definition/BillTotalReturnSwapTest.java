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
import com.opengamma.analytics.financial.instrument.bond.BillDataSets;
import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the description of a bill total return swap with an underlying bill and a funding leg.
 */
public class BillTotalReturnSwapTest {

  private static final Currency EUR = Currency.EUR;

  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2014, 6, 25);
  private static final ZonedDateTime TERMINATION_DATE = DateUtils.getUTCDate(2014, 12, 22);

  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2014, 6, 23); // Before effective date.

  private static final double EFFECTIVE_TIME_1_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, EFFECTIVE_DATE);
  private static final double TERMINATION_TIME_1_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, TERMINATION_DATE);

  private static final BillSecurityDefinition BELDEC14_DEFINITION = BillDataSets.billBel_20141218();
  private static final BillSecurity BELDEC14 = BELDEC14_DEFINITION.toDerivative(REFERENCE_DATE_1, EFFECTIVE_DATE);
  // Funding: unique fixed coupon in EUR: pay TRS bill, receive funding
  private static final double NOTIONAL_TRS = 123456000;
  private static final double NOTIONAL_BILL = 100000000;

  // Funding: unique fixed coupon in GBP: receive TRS bond, pay funding
  private static final double RATE = 0.0043;
  private static final CouponFixedDefinition FUNDING_FIXED_CPN_REC_DEFINITION = new CouponFixedDefinition(EUR,
      TERMINATION_DATE, EFFECTIVE_DATE, TERMINATION_DATE, 0.50, NOTIONAL_TRS, RATE);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_FIXED_REC_DEFINITION =
      new AnnuityDefinition<>(new CouponFixedDefinition[] {FUNDING_FIXED_CPN_REC_DEFINITION }, BELDEC14_DEFINITION.getCalendar());
  private static final Annuity<? extends Payment> FUNDING_LEG_FIXED_REC_1 = FUNDING_LEG_FIXED_REC_DEFINITION.toDerivative(REFERENCE_DATE_1);
  private static final BillTotalReturnSwap TRS_PAY_FIXED_REC_1 =
      new BillTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, FUNDING_LEG_FIXED_REC_1, BELDEC14, -NOTIONAL_BILL);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFundingLeg() {
    new BillTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, null, BELDEC14, -NOTIONAL_BILL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBond() {
    new BillTotalReturnSwap(EFFECTIVE_TIME_1_1, TERMINATION_TIME_1_1, FUNDING_LEG_FIXED_REC_1, null, -NOTIONAL_BILL);
  }

  @Test
  public void getter() {
    assertEquals("BondTotalReturnSwap: getter", EFFECTIVE_TIME_1_1, TRS_PAY_FIXED_REC_1.getEffectiveTime());
    assertEquals("BondTotalReturnSwap: getter", TERMINATION_TIME_1_1, TRS_PAY_FIXED_REC_1.getTerminationTime());
    assertEquals("BondTotalReturnSwap: getter", FUNDING_LEG_FIXED_REC_1, TRS_PAY_FIXED_REC_1.getFundingLeg());
    assertEquals("BondTotalReturnSwap: getter", BELDEC14, TRS_PAY_FIXED_REC_1.getAsset());
    assertEquals("BondTotalReturnSwap: getter", -NOTIONAL_BILL, TRS_PAY_FIXED_REC_1.getQuantity());
  }

}
