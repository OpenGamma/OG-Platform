/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.trs;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.datasets.CalendarGBP;
import com.opengamma.analytics.financial.equity.Equity;
import com.opengamma.analytics.financial.equity.EquityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the description of an equity total return swap.
 */
public class EquityTotalReturnSwapDefinitionTest {

  private static final ZonedDateTime EFFECTIVE_DATE_1 = DateUtils.getUTCDate(2012, 2, 9);
  private static final ZonedDateTime TERMINATION_DATE_1 = DateUtils.getUTCDate(2012, 5, 9);

  private static final Currency GBP = Currency.GBP;
  private static final Calendar LON = new CalendarGBP("LON");
  private static final double NOTIONAL_TRS = 123456000;
  // Equity
  private static final double NB_SHARES = 1000000;
  // Funding: unique fixed coupon in GBP: pay TRS equity, receive funding
  private static final double RATE = 0.0043;
  private static final CouponFixedDefinition FUNDING_FIXED_CPN_REC_DEFINITION = new CouponFixedDefinition(GBP,
      TERMINATION_DATE_1, EFFECTIVE_DATE_1, TERMINATION_DATE_1, 0.25, NOTIONAL_TRS, RATE);
  private static final AnnuityDefinition<? extends PaymentDefinition> FUNDING_LEG_FIXED_REC_DEFINITION =
      new AnnuityDefinition<>(new CouponFixedDefinition[] {FUNDING_FIXED_CPN_REC_DEFINITION }, LON);
  private static final String OG_NAME = "OpenGamma Ltd";
  private static final String OG_TICKER = "OG";
  private static final LegalEntity OG_LEGAL_ENTITY = new LegalEntity(OG_TICKER, OG_NAME, Sets.newHashSet(CreditRating.of("AAA", "ABC", true)), Sector.of("Technology"),
      Region.of("UK", Country.GB, Currency.GBP));
  private static final EquityDefinition EQUITY_DEFINITION = new EquityDefinition(OG_LEGAL_ENTITY, GBP, NB_SHARES);
  private static final double DIVIDEND_RATIO = 1.0;
  private static final EquityTotalReturnSwapDefinition TRS_PAY_FIXED_REC_DEFINITION = new EquityTotalReturnSwapDefinition(EFFECTIVE_DATE_1,
      TERMINATION_DATE_1, FUNDING_LEG_FIXED_REC_DEFINITION, EQUITY_DEFINITION, NOTIONAL_TRS, GBP, DIVIDEND_RATIO);

  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2012, 2, 2); // Before effective date.

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEffectiveDate() {
    new EquityTotalReturnSwapDefinition(null, TERMINATION_DATE_1, FUNDING_LEG_FIXED_REC_DEFINITION, EQUITY_DEFINITION, NOTIONAL_TRS, GBP, 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTerminationDate() {
    new EquityTotalReturnSwapDefinition(EFFECTIVE_DATE_1, null, FUNDING_LEG_FIXED_REC_DEFINITION, EQUITY_DEFINITION, NOTIONAL_TRS, GBP, 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFundingLeg() {
    new EquityTotalReturnSwapDefinition(EFFECTIVE_DATE_1, TERMINATION_DATE_1, null, EQUITY_DEFINITION, NOTIONAL_TRS, GBP, 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullUnderlyingEquity() {
    new EquityTotalReturnSwapDefinition(EFFECTIVE_DATE_1, TERMINATION_DATE_1, FUNDING_LEG_FIXED_REC_DEFINITION, null, NOTIONAL_TRS, GBP, 1.0);
  }

  @Test
  public void getter() {
    assertEquals("EquityTotalReturnSwapDefinition: getter", EFFECTIVE_DATE_1, TRS_PAY_FIXED_REC_DEFINITION.getEffectiveDate());
    assertEquals("EquityTotalReturnSwapDefinition: getter", TERMINATION_DATE_1, TRS_PAY_FIXED_REC_DEFINITION.getTerminationDate());
    assertEquals("EquityTotalReturnSwapDefinition: getter", FUNDING_LEG_FIXED_REC_DEFINITION, TRS_PAY_FIXED_REC_DEFINITION.getFundingLeg());
    assertEquals("EquityTotalReturnSwapDefinition: getter", EQUITY_DEFINITION, TRS_PAY_FIXED_REC_DEFINITION.getAsset());
    assertEquals("EquityTotalReturnSwapDefinition: getter", DIVIDEND_RATIO, TRS_PAY_FIXED_REC_DEFINITION.getDividendPercentage());
  }

  @Test
  public void toDerivative() {
    double effectiveTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, EFFECTIVE_DATE_1);
    double terminationTime = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, TERMINATION_DATE_1);
    Annuity<? extends Payment> fundingLeg = FUNDING_LEG_FIXED_REC_DEFINITION.toDerivative(REFERENCE_DATE_1);
    Equity equity = new Equity(OG_LEGAL_ENTITY, GBP, NB_SHARES);
    EquityTotalReturnSwap trsExpected = new EquityTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, equity, NOTIONAL_TRS, GBP, DIVIDEND_RATIO);
    EquityTotalReturnSwap trsConverted = TRS_PAY_FIXED_REC_DEFINITION.toDerivative(REFERENCE_DATE_1);
    assertEquals("BondTotalReturnSwapDefinition: toDerivative", trsExpected, trsConverted);
  }

}
