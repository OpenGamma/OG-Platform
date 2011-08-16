/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.derivative.ForexSwap;
import com.opengamma.financial.model.option.definition.Barrier;
import com.opengamma.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Contains a set of Forex instruments that can be used in tests.
 */
public class ForexInstrumentsDescriptionDataSet {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime NEAR_DATE = DateUtils.getUTCDate(2011, 5, 26);
  private static final ZonedDateTime FAR_DATE = DateUtils.getUTCDate(2011, 7, 26); // 1m
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final double FORWARD_POINTS = -0.0007;
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, FAR_DATE, NOMINAL_1, FX_RATE);
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 7, 22);
  private static final String DISCOUNTING_EUR = "Discounting EUR";
  private static final String DISCOUNTING_USD = "Discounting USD";
  private static final String[] CURVES_NAME = new String[] {DISCOUNTING_EUR, DISCOUNTING_USD};
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 20);
  private static final ForexOptionVanillaDefinition FX_OPTION_DEFINITION = new ForexOptionVanillaDefinition(FX_DEFINITION, EXPIRATION_DATE, IS_CALL, IS_LONG);
  private static final Barrier BARRIER = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 1.5);

  public static ForexDefinition createForexDefinition() {
    return new ForexDefinition(CUR_1, CUR_2, FAR_DATE, NOMINAL_1, FX_RATE);
  }

  public static Forex createForex() {
    return createForexDefinition().toDerivative(REFERENCE_DATE, CURVES_NAME);
  }

  public static ForexSwapDefinition createForexSwapDefinition() {
    return new ForexSwapDefinition(CUR_1, CUR_2, NEAR_DATE, FAR_DATE, NOMINAL_1, FX_RATE, FORWARD_POINTS);
  }

  public static ForexSwap createForexSwap() {
    return createForexSwapDefinition().toDerivative(REFERENCE_DATE, CURVES_NAME);
  }

  public static ForexOptionVanillaDefinition createForexOptionVanillaDefinition() {
    return new ForexOptionVanillaDefinition(FX_DEFINITION, EXPIRATION_DATE, IS_CALL, IS_LONG);
  }

  public static ForexOptionVanilla createForexOptionVanilla() {
    return createForexOptionVanillaDefinition().toDerivative(REFERENCE_DATE, CURVES_NAME);
  }

  public static ForexOptionSingleBarrierDefinition createForexOptionSingleBarrierDefinition() {
    return new ForexOptionSingleBarrierDefinition(FX_OPTION_DEFINITION, BARRIER);
  }

  public static ForexOptionSingleBarrier createForexOptionSingleBarrier() {
    return createForexOptionSingleBarrierDefinition().toDerivative(REFERENCE_DATE, CURVES_NAME);
  }
}
