/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Metal future option definition
 */
public class MetalFutureOptionDefinition extends CommodityFutureOptionDefinition<MetalFutureDefinition, MetalFutureOption> {

  /**
   * Constructor for future options
   *
   * @param expiryDate  the time and the day that a particular delivery month of a futures contract stops trading, as well as the final settlement price for that contract
   * @param underlying  underlying future
   * @param strike  strike price
   * @param exerciseType  exercise type - European or American
   * @param isCall  call if true, put if false
   */
  public MetalFutureOptionDefinition(final ZonedDateTime expiryDate, final MetalFutureDefinition underlying, final double strike, final ExerciseDecisionType exerciseType,
      final boolean isCall) {
    super(expiryDate, underlying, strike, exerciseType, isCall);
  }

  @Override
  public MetalFutureOption toDerivative(final ZonedDateTime date) {
    ArgumentChecker.inOrderOrEqual(date, this.getExpiryDate(), "date", "expiry date");
    final double timeToFixing = TimeCalculator.getTimeBetween(date, this.getExpiryDate());
    //timeToSettlement
    return new MetalFutureOption(timeToFixing, getUnderlying().toDerivative(date), getStrike(), getExerciseType(), isCall());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitMetalFutureOptionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitMetalFutureOptionDefinition(this);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MetalFutureOptionDefinition)) {
      return false;
    }
    return super.equals(obj);
  }

}
