/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Contains a set of Forex instruments that can be used in tests.
 */
public class ForexInstrumentsDescriptionDataSet {

  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final Currency KRW = Currency.of("KRW");
  private static final ZonedDateTime NEAR_DATE = DateUtils.getUTCDate(2011, 5, 26);
  private static final ZonedDateTime FAR_DATE = DateUtils.getUTCDate(2011, 7, 26); // 1 month
  private static final ZonedDateTime NDF_FIXING_DATE = DateUtils.getUTCDate(2012, 5, 2);
  private static final ZonedDateTime NDF_PAYMENT_DATE = DateUtils.getUTCDate(2012, 5, 4);
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE_EUR_USD = 1.4177;
  private static final double FX_RATE_KRW_USD = 1123.45;
  private static final double FORWARD_POINTS = -0.0007;
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(EUR, USD, FAR_DATE, NOMINAL_1, FX_RATE_EUR_USD);
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 7, 22);
  private static final String DISCOUNTING_EUR = "Discounting EUR";
  private static final String DISCOUNTING_USD = "Discounting USD";
  private static final String[] CURVES_NAME = new String[] {DISCOUNTING_EUR, DISCOUNTING_USD};
  private static final ForexOptionVanillaDefinition FX_OPTION_DEFINITION = new ForexOptionVanillaDefinition(FX_DEFINITION, EXPIRATION_DATE, IS_CALL, IS_LONG);
  private static final Barrier BARRIER = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 1.5);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 20);

  public static ForexDefinition createForexDefinition() {
    return new ForexDefinition(EUR, USD, FAR_DATE, NOMINAL_1, FX_RATE_EUR_USD);
  }

  /**
   * Creates a FX
   * @return A FX
   */
  public static Forex createForex() {
    return createForexDefinition().toDerivative(REFERENCE_DATE);
  }

  /**
   * Creates a FX swap definition
   * @return A FX swap definition
   */
  public static ForexSwapDefinition createForexSwapDefinition() {
    return new ForexSwapDefinition(EUR, USD, NEAR_DATE, FAR_DATE, NOMINAL_1, FX_RATE_EUR_USD, FORWARD_POINTS);
  }

  /**
   * Creates a FX swap
   * @return A FX swap
   * @deprecated Use the non-deprecated method that does not use yield curve names
   */
  @Deprecated
  public static ForexSwap createForexSwapDeprecated() {
    return (ForexSwap) createForexSwapDefinition().toDerivative(REFERENCE_DATE, CURVES_NAME);
  }

  /**
   * Creates a FX swap
   * @return A FX swap
   */
  public static ForexSwap createForexSwap() {
    return (ForexSwap) createForexSwapDefinition().toDerivative(REFERENCE_DATE);
  }

  /**
   * Creates a vanilla FX option definition
   * @return A vanilla FX option definition
   */
  public static ForexOptionVanillaDefinition createForexOptionVanillaDefinition() {
    return new ForexOptionVanillaDefinition(FX_DEFINITION, EXPIRATION_DATE, IS_CALL, IS_LONG);
  }

  /**
   * Creates a vanilla FX option
   * @return A vanilla FX option
   * @deprecated Use the non-deprecated method that does not use yield curve names
   */
  @Deprecated
  public static ForexOptionVanilla createForexOptionVanillaDeprecated() {
    return createForexOptionVanillaDefinition().toDerivative(REFERENCE_DATE, CURVES_NAME);
  }

  /**
   * Creates a vanilla FX option
   * @return A vanilla FX option
   */
  public static ForexOptionVanilla createForexOptionVanilla() {
    return createForexOptionVanillaDefinition().toDerivative(REFERENCE_DATE);
  }

  /**
   * Creates a single-barrier FX option definition
   * @return A single-barrier FX option definition
   */
  public static ForexOptionSingleBarrierDefinition createForexOptionSingleBarrierDefinition() {
    return new ForexOptionSingleBarrierDefinition(FX_OPTION_DEFINITION, BARRIER);
  }

  /**
   * Creates a single-barrier FX option
   * @return A single-barrier FX option
   * @deprecated Use the non-deprecated method that does not use yield curve names
   */
  @Deprecated
  public static ForexOptionSingleBarrier createForexOptionSingleBarrierDeprecated() {
    return createForexOptionSingleBarrierDefinition().toDerivative(REFERENCE_DATE, CURVES_NAME);
  }

  /**
   * Creates a single-barrier FX option
   * @return A single-barrier FX option
   */
  public static ForexOptionSingleBarrier createForexOptionSingleBarrier() {
    return createForexOptionSingleBarrierDefinition().toDerivative(REFERENCE_DATE);
  }

  /**
   * Creates a non-deliverable FX forward definition
   * @return A non-deliverable FX forward
   */
  public static ForexNonDeliverableForwardDefinition createForexNonDeliverableForwardDefinition() {
    return new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_1, FX_RATE_KRW_USD, NDF_FIXING_DATE, NDF_PAYMENT_DATE);
  }

  /**
   * Creates a non-deliverable FX forward
   * @return A non-deliverable FX forward
   * @deprecated Use the non-deprecated method that does not use yield curve names
   */
  @Deprecated
  public static ForexNonDeliverableForward createForexNonDeliverableForwardDeprecated() {
    return createForexNonDeliverableForwardDefinition().toDerivative(REFERENCE_DATE, CURVES_NAME);
  }

  /**
   * Creates a non-deliverable FX forward
   * @return A non-deliverable FX forward
   */
  public static ForexNonDeliverableForward createForexNonDeliverableForward() {
    return createForexNonDeliverableForwardDefinition().toDerivative(REFERENCE_DATE);
  }

  /**
   * Creates a non-deliverable FX option definition
   * @return A non-deliverable FX option definition
   */
  public static ForexNonDeliverableOptionDefinition createForexNonDeliverableOptionDefinition() {
    return new ForexNonDeliverableOptionDefinition(createForexNonDeliverableForwardDefinition(), IS_CALL, IS_LONG);
  }

  /**
   * Creates a non-deliverable FX option
   * @return A non-deliverable FX option
   * @deprecated Use the non-deprecated method that does not use yield curve names
   */
  @Deprecated
  public static ForexNonDeliverableOption createForexNonDeliverableOptionDeprecated() {
    return createForexNonDeliverableOptionDefinition().toDerivative(REFERENCE_DATE, CURVES_NAME);
  }

  /**
   * Creates a non-deliverable FX option
   * @return A non-deliverable FX option
   */
  public static ForexNonDeliverableOption createForexNonDeliverableOption() {
    return createForexNonDeliverableOptionDefinition().toDerivative(REFERENCE_DATE);
  }

  /**
   * Creates a FX digital option definition
   * @return A FX digital option definition
   */
  public static ForexOptionDigitalDefinition createForexOptionDigitalDefinition() {
    return new ForexOptionDigitalDefinition(FX_DEFINITION, EXPIRATION_DATE, IS_CALL, IS_LONG);
  }

  /**
   * Creates a FX digital option
   * @return A FX digital option
   * @deprecated Use the non-deprecated method that does not use yield curve names
   */
  @Deprecated
  public static ForexOptionDigital createForexOptionDigitalDeprecated() {
    return createForexOptionDigitalDefinition().toDerivative(REFERENCE_DATE, CURVES_NAME);
  }

  /**
   * Creates a FX digital option
   * @return A FX digital option
   */
  public static ForexOptionDigital createForexOptionDigital() {
    return createForexOptionDigitalDefinition().toDerivative(REFERENCE_DATE);
  }
}
