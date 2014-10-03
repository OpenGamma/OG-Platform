/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.generic;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the last time calculator for the times related to a specific index.
 */
public class LastFixingTimeIndexCalculatorTest {

  /** Generators */
  private static final Calendar TARGET = new CalendarTarget("TARGET");
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final IborIndex EURIBOR3M = EUR1YEURIBOR3M.getIborIndex();
  private static final IborIndex EURIBOR6M = EUR1YEURIBOR6M.getIborIndex();
  private static final GeneratorSwapIborIborMaster GENERATOR_BS_MASTER = GeneratorSwapIborIborMaster.getInstance();
  private static final GeneratorSwapIborIbor EUREURIBOR3MEURIBOR6M = GENERATOR_BS_MASTER.getGenerator("EUREURIBOR3MEURIBOR6M", TARGET);
  /** Dates */
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 7, 17);
  /** Instruments */
  private static final GeneratorAttributeIR ATTRIBUTE_1 = new GeneratorAttributeIR(Period.ofYears(5));
  private static final SwapFixedIborDefinition IRS_1_DEFINITION = EUR1YEURIBOR3M.generateInstrument(REFERENCE_DATE, 0.01, 1, ATTRIBUTE_1);
  private static final Swap<?, ?> IRS_1 = IRS_1_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwapDefinition BS_1_DEFINITION = EUREURIBOR3MEURIBOR6M.generateInstrument(REFERENCE_DATE, 0.0010, 1, ATTRIBUTE_1);
  private static final Swap<?, ?> BS_1 = BS_1_DEFINITION.toDerivative(REFERENCE_DATE);
  /** Calculators with different indexes **/
  private static final LastFixingTimeIndexCalculator LAST_TIME_EURIBOR3M = new LastFixingTimeIndexCalculator(EURIBOR3M);
  private static final LastFixingTimeIndexCalculator LAST_TIME_EURIBOR6M = new LastFixingTimeIndexCalculator(EURIBOR6M);

  private static final double TOLERANCE_TIME = 1.0E-10;

  @Test
  public void fixedLeg() {
    Double lastFixed = IRS_1.getFirstLeg().accept(LAST_TIME_EURIBOR3M);
    assertEquals("LastFixingTimeIndexCalculator: fixed leg", lastFixed, 0.0, TOLERANCE_TIME);
  }

  @Test
  public void iborLeg() {
    double lastTimeComputed = IRS_1.getSecondLeg().accept(LAST_TIME_EURIBOR3M);
    double lastTimeExpected = ((CouponIbor) (IRS_1.getSecondLeg().getNthPayment(IRS_1.getSecondLeg().getNumberOfPayments() - 1))).getFixingPeriodEndTime();
    assertEquals("LastFixingTimeIndexCalculator: Ibor leg", lastTimeComputed, lastTimeExpected, TOLERANCE_TIME);
  }

  @Test
  public void irs() {
    double lastTimeComputed = IRS_1.accept(LAST_TIME_EURIBOR3M);
    double lastTimeExpected = ((CouponIbor) (IRS_1.getSecondLeg().getNthPayment(IRS_1.getSecondLeg().getNumberOfPayments() - 1))).getFixingPeriodEndTime();
    assertEquals("LastFixingTimeIndexCalculator: IRS", lastTimeComputed, lastTimeExpected, TOLERANCE_TIME);
  }

  @Test
  public void basisSwap() {
    // The reference date has been chosen such that the last date of the two legs are different.
    double lastTimeComputedE3Swap = BS_1.accept(LAST_TIME_EURIBOR3M);
    double lastTimeComputedE3Leg = BS_1.getFirstLeg().accept(LAST_TIME_EURIBOR3M);
    double lastTimeComputedE6Swap = BS_1.accept(LAST_TIME_EURIBOR6M);
    double lastTimeComputedE6Leg = BS_1.getSecondLeg().accept(LAST_TIME_EURIBOR6M);
    assertEquals("LastFixingTimeIndexCalculator: basis swap", lastTimeComputedE3Leg, lastTimeComputedE3Swap, TOLERANCE_TIME);
    assertEquals("LastFixingTimeIndexCalculator: basis swap", lastTimeComputedE6Leg, lastTimeComputedE6Swap, TOLERANCE_TIME);
    assertFalse("LastFixingTimeIndexCalculator: basis swap", Math.abs(lastTimeComputedE3Swap - lastTimeComputedE6Swap) < TOLERANCE_TIME);
  }

}
