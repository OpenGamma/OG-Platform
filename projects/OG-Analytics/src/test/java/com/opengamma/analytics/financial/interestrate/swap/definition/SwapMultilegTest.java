/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.definition;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test the swaps with multiple legs constructor and to derivative.
 */
@Test(groups = TestGroup.UNIT)
public class SwapMultilegTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TRAGET");
  private static final IndexIborMaster INDEX_MASTER = IndexIborMaster.getInstance();
  private static final IborIndex EURIBOR3M = INDEX_MASTER.getIndex("EURIBOR3M");
  private static final IborIndex EURIBOR6M = INDEX_MASTER.getIndex("EURIBOR6M");
  private static final GeneratorSwapFixedIborMaster SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final Currency EUR = EURIBOR3M.getCurrency();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 3, 20);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2013, 10, 16);
  private static final double NOTIONAL = 1000000; // 1m
  private static final double SPREAD = 0.0010; // 10 bps
  private static final StubType STUB = StubType.SHORT_START;

  // Swap represeting a EUR basis swap: 1 spread leg and 2 Euribor leg.
  private static final boolean IS_PAYER_SPREAD = true;
  private static final ZonedDateTime MATURITY_DATE = SETTLEMENT_DATE.plus(ANNUITY_TENOR);
  private static final int NB_LEGS = 3;
  @SuppressWarnings("rawtypes")
  private static final AnnuityDefinition[] LEGS_DEFINITION = new AnnuityDefinition[NB_LEGS];
  static {
    LEGS_DEFINITION[0] = AnnuityDefinitionBuilder.couponFixed(EUR, SETTLEMENT_DATE, MATURITY_DATE, EUR1YEURIBOR6M.getFixedLegPeriod(), TARGET,
        EUR1YEURIBOR6M.getFixedLegDayCount(), EUR1YEURIBOR6M.getBusinessDayConvention(), EUR1YEURIBOR6M.isEndOfMonth(), NOTIONAL, SPREAD, IS_PAYER_SPREAD, STUB, 0);
    LEGS_DEFINITION[1] = AnnuityDefinitionBuilder.couponIbor(SETTLEMENT_DATE, MATURITY_DATE, EURIBOR3M.getTenor(), NOTIONAL, EURIBOR3M,
        IS_PAYER_SPREAD, EURIBOR3M.getDayCount(), EURIBOR3M.getBusinessDayConvention(), EURIBOR3M.isEndOfMonth(), TARGET, STUB, 0);
    LEGS_DEFINITION[2] = AnnuityDefinitionBuilder.couponIbor(SETTLEMENT_DATE, MATURITY_DATE, EURIBOR6M.getTenor(), NOTIONAL, EURIBOR6M,
        !IS_PAYER_SPREAD, EURIBOR6M.getDayCount(), EURIBOR6M.getBusinessDayConvention(), EURIBOR6M.isEndOfMonth(), TARGET, STUB, 0);
  }
  @SuppressWarnings("rawtypes")
  private static final Annuity[] LEGS = new Annuity[NB_LEGS];
  static {
    for (int loopleg = 0; loopleg < NB_LEGS; loopleg++) {
      LEGS[loopleg] = LEGS_DEFINITION[loopleg].toDerivative(REFERENCE_DATE);
    }
  }
  @SuppressWarnings("unchecked")
  private static final SwapMultileg SWAP_MULTI_LEG = new SwapMultileg(LEGS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLegs() {
    new SwapMultileg(null);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullZeroLeg() {
    new SwapMultileg(new Annuity[0]);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLeg2() {
    @SuppressWarnings("rawtypes")
    final Annuity[] legs2 = LEGS.clone();
    legs2[1] = null;
    new SwapMultileg(legs2);
  }

  @Test
  public void getter() {
    assertEquals("SwapMultileg: getter", LEGS, SWAP_MULTI_LEG.getLegs());
  }

}
