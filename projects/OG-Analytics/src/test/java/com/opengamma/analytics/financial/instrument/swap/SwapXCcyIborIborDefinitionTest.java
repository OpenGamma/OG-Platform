/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapXCcyIborIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test the swap Ibor+spread to Ibor+spread constructor and to derivative.
 */
@Test(groups = TestGroup.UNIT)
public class SwapXCcyIborIborDefinitionTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final IndexIborMaster INDEX_MASTER = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR3M = INDEX_MASTER.getIndex("USDLIBOR3M");
  private static final IborIndex EURIBOR3M = INDEX_MASTER.getIndex("EURIBOR3M");
  private static final Period ANNUITY_TENOR = Period.ofYears(2);

  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2012, 4, 18);
  private static final double NOTIONAL1 = 1000000; // EUR
  private static final double NOTIONAL2 = 1300000; // USD

  private static final GeneratorSwapXCcyIborIbor XCCY_GENERATOR = new GeneratorSwapXCcyIborIbor("EURIBOR3MUSDLIBOR3M", EURIBOR3M, USDLIBOR3M, CALENDAR, CALENDAR);

  private static final boolean IS_PAYER_1 = true;
  private static final double SIGN_1 = IS_PAYER_1 ? -1.0 : 1.0;
  private static final double SPREAD_1 = 0.0012;
  private static final boolean IS_PAYER_2 = !IS_PAYER_1;
  private static final double SPREAD_2 = 0.0;
  private static final ZonedDateTime MATURITY_DATE = SETTLEMENT_DATE.plus(ANNUITY_TENOR);

  private static final SwapXCcyIborIborDefinition SWAP_XCCY_IBOR_IBOR = SwapXCcyIborIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, XCCY_GENERATOR, NOTIONAL1, NOTIONAL2, SPREAD_1, IS_PAYER_1,
      CALENDAR, CALENDAR);

  private static final AnnuityCouponIborSpreadDefinition IBOR_LEG_1 = AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL1, EURIBOR3M, SPREAD_1, IS_PAYER_1, CALENDAR);
  private static final AnnuityCouponIborSpreadDefinition IBOR_LEG_2 = AnnuityCouponIborSpreadDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL2, USDLIBOR3M, SPREAD_2, IS_PAYER_2, CALENDAR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSettleDate() {
    SwapXCcyIborIborDefinition.from(null, ANNUITY_TENOR, XCCY_GENERATOR, NOTIONAL1, NOTIONAL2, SPREAD_1, IS_PAYER_1, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTenor() {
    SwapXCcyIborIborDefinition.from(SETTLEMENT_DATE, null, XCCY_GENERATOR, NOTIONAL1, NOTIONAL2, SPREAD_1, IS_PAYER_1, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullGen() {
    SwapXCcyIborIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, null, NOTIONAL1, NOTIONAL2, SPREAD_1, IS_PAYER_1, CALENDAR, CALENDAR);
  }

  @Test
  public void leg1() {
    // The first payment is a fixed payment with -notional
    final CouponFixedDefinition exchangeNotionalStart = new CouponFixedDefinition(XCCY_GENERATOR.getIborIndex1().getCurrency(), SETTLEMENT_DATE, SETTLEMENT_DATE,
        SETTLEMENT_DATE, 1.0, -NOTIONAL1 * SIGN_1, 1.0);
    assertEquals("SwapXCcyIborIborDefinition", exchangeNotionalStart, SWAP_XCCY_IBOR_IBOR.getFirstLeg().getNthPayment(0));
    // The last payment is a fixed payment with notional
    final int nbPayments = SWAP_XCCY_IBOR_IBOR.getFirstLeg().getNumberOfPayments();
    final CouponFixedDefinition exchangeNotionalEnd = new CouponFixedDefinition(XCCY_GENERATOR.getIborIndex1().getCurrency(), MATURITY_DATE, MATURITY_DATE,
        MATURITY_DATE, 1.0, NOTIONAL1 * SIGN_1, 1.0);
    assertEquals("SwapXCcyIborIborDefinition", exchangeNotionalEnd, SWAP_XCCY_IBOR_IBOR.getFirstLeg().getNthPayment(nbPayments - 1));
    // The intermediary payments are coupons from the floating leg
    for (int loopcpn = 0; loopcpn < nbPayments - 2; loopcpn++) {
      assertEquals("SwapXCcyIborIborDefinition", IBOR_LEG_1.getNthPayment(loopcpn), SWAP_XCCY_IBOR_IBOR.getFirstLeg().getNthPayment(loopcpn + 1));
    }
  }

  @Test
  public void leg2() {
    // The first payment is a fixed payment with -notional
    final CouponFixedDefinition exchangeNotionalStart = new CouponFixedDefinition(XCCY_GENERATOR.getIborIndex2().getCurrency(), SETTLEMENT_DATE, SETTLEMENT_DATE,
        SETTLEMENT_DATE, 1.0, NOTIONAL2 * SIGN_1, 1.0);
    assertEquals("SwapXCcyIborIborDefinition", exchangeNotionalStart, SWAP_XCCY_IBOR_IBOR.getSecondLeg().getNthPayment(0));
    // The last payment is a fixed payment with notional
    final int nbPayments = SWAP_XCCY_IBOR_IBOR.getSecondLeg().getNumberOfPayments();
    final CouponFixedDefinition exchangeNotionalEnd = new CouponFixedDefinition(XCCY_GENERATOR.getIborIndex2().getCurrency(), MATURITY_DATE, MATURITY_DATE,
        MATURITY_DATE, 1.0, -NOTIONAL2 * SIGN_1, 1.0);
    assertEquals("SwapXCcyIborIborDefinition", exchangeNotionalEnd, SWAP_XCCY_IBOR_IBOR.getSecondLeg().getNthPayment(nbPayments - 1));
    // The intermediary payments are coupons from the floating leg
    for (int loopcpn = 0; loopcpn < nbPayments - 2; loopcpn++) {
      assertEquals("SwapXCcyIborIborDefinition", IBOR_LEG_2.getNthPayment(loopcpn), SWAP_XCCY_IBOR_IBOR.getSecondLeg().getNthPayment(loopcpn + 1));
    }
  }

  @Test
  public void getter() {
    assertEquals(SWAP_XCCY_IBOR_IBOR.getFirstLeg().getCurrency(), XCCY_GENERATOR.getIborIndex1().getCurrency());
    assertEquals(SWAP_XCCY_IBOR_IBOR.getSecondLeg().getCurrency(), XCCY_GENERATOR.getIborIndex2().getCurrency());
  }

  @Test
  public void from2() {
    final SwapXCcyIborIborDefinition swap2 = SwapXCcyIborIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, XCCY_GENERATOR, NOTIONAL1, NOTIONAL2, SPREAD_1, IS_PAYER_1, CALENDAR, CALENDAR);
    assertEquals("SwapXCcyIborIborDefinition", SWAP_XCCY_IBOR_IBOR, swap2);
  }

}
