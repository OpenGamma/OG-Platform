/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;

import javax.time.calendar.ZonedDateTime;

/**
 * Energy future option definition
 */
public class EnergyFutureOptionDefinition extends CommodityFutureOptionDefinition<EnergyFutureDefinition, EnergyFutureOption> {

  /**
   * Constructor for future options
   *
   * @param expiryDate is the time and the day that a particular delivery month of a futures contract stops trading, as well as the final settlement price for that contract.
   * @param underlying Underlying future
   * @param strike Strike price
   * @param exerciseType Exercise type - European or American
   * @param isCall Call if true, Put if false
   */
  public EnergyFutureOptionDefinition(final ZonedDateTime expiryDate, final EnergyFutureDefinition underlying, final double strike, final ExerciseDecisionType exerciseType,
      final boolean isCall) {
    super(expiryDate, underlying, strike, exerciseType, isCall);
  }

  /**
   * Get the derivative at a given fix time from the definition
   * @param date fixing time
   * @param yieldCurveNames  
   * @return the fixed derivative
   */
  @Override
  public EnergyFutureOption toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.inOrderOrEqual(date, this.getExpiryDate(), "date", "expiry date");
    double timeToFixing = TimeCalculator.getTimeBetween(date, this.getExpiryDate());
    return new EnergyFutureOption(timeToFixing, getUnderlying().toDerivative(date, yieldCurveNames), getStrike(), getExerciseType(), isCall());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitEnergyFutureOptionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitEnergyFutureOptionDefinition(this);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EnergyFutureOptionDefinition)) {
      return false;
    }
    return super.equals(obj);
  }

}
