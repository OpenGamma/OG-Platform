/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.equity.EquityDerivative;
import com.opengamma.analytics.financial.equity.EquityInstrumentDefinition;
import com.opengamma.analytics.financial.equity.EquityInstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

/**
 * Calendar aware version of an EquityIndexOption
 * The definition is responsible for constructing the 'Derivative' for pricing visitors.
 */
public class EquityIndexOptionDefinition implements EquityInstrumentDefinition<EquityDerivative> {

  /** Call if true, Put if false */
  private final boolean _isCall;
  /** Strike, with same scaling as index has. 
   * For example, DJX is 1/100 DOW JONES INDUSTRIAL AVERAGE */
  private final double _strike;
  /** Currency */
  private final Currency _currency;
  /** Identifier of the underlying index */
  private final ExternalIdBundle _underlyingId;
  /** Exercise type, European or American */
  private final ExerciseDecisionType _exerciseType;
  /** Expiry, date and time of last, or only, exercise decision */
  private final ZonedDateTime _expiryDT;
  /** Cash settlement occurs on this LocalDate */
  private final LocalDate _settlementDate;
  /** Point value, scaling of standard contract. 
   * Unit notional. A unit move in price is multiplied by this to give pnl of a single contract */
  private final double _pointValue;

  /**
   * @param isCall Call if true, Put if false 
   * @param strike Strike, with same scaling as index has. 
   * @param currency Currency of settlement
   * @param indexId Identifier of the underlying index
   * @param exerciseType Exercise type, European or American
   * @param expiryDT Expiry, date and time of last, or only, exercise decision
   * @param settlementDate Cash settlement occurs on this LocalDate
   * @param pointValue Unit notional. A unit move in price is multiplied by this to give pnl of a single contract
   */
  public EquityIndexOptionDefinition(boolean isCall, double strike, Currency currency, ExternalIdBundle indexId,
      ExerciseDecisionType exerciseType, ZonedDateTime expiryDT, LocalDate settlementDate, double pointValue) {
    _isCall = isCall;
    _strike = strike;
    _currency = currency;
    _underlyingId = indexId;
    _exerciseType = exerciseType;
    _expiryDT = expiryDT;
    _settlementDate = settlementDate;
    _pointValue = pointValue;
  }

  /**
   * Creates Analytics version of Option, as of exact dateTime, ready for pricing
   * @param valueDate Date at which valuation will occur
   * @return EquityIndexOption derivative as of valDT
   */
  EquityIndexOption toDerivative(final ZonedDateTime valDT) {
    Validate.notNull(valDT, "valuationDate was null");
    final double timeToSettlement = TimeCalculator.getTimeBetween(valDT, _settlementDate);
    final double timeToExpiry = TimeCalculator.getTimeBetween(valDT, _expiryDT);
    return new EquityIndexOption(timeToExpiry, timeToSettlement, _strike, _isCall, _currency, _pointValue);
  }

  @Override
  /**
   * Creates Analytics version of Option, as of exact dateTime, ready for pricing
   * @param valueDate Date at which valuation will occur
   * @return EquityIndexOption derivative as of date
   */
  public EquityDerivative toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    return toDerivative(date);
  }

  @Override
  public <U, V> V accept(EquityInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitEquityIndexOptionDefinition(this, data);
  }

  @Override
  public <V> V accept(EquityInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitEquityIndexOptionDefinition(this);
  }
}
