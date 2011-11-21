/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.InstrumentDerivative;

/**
 * Class describing a non-deliverable foreign exchange European option. The option exercise date is the underlying NDF fixing date.
 * When the option is a call, the option holder has the right to enter into the Forex NDF; when the option is a put, the option holder has the right to enter into a NDF 
 * transaction equal to the underlying but with opposite signs. The settlement is done in the second currency of the NDF.
 * A Call on a Forex on KRW / USD at strike 1124.00 is thus the right to receive 1.00 USD and pay 1124.00 KRW and cash settle the difference in USD. 
 * A put on a Forex on KRW / USD at strike 1124.00 is thus the right to pay 1.00 USD and receive 1124.00 KRW and cash settle the difference in USD.
 * There is not a full put/call parity in NDO as the two currencies do not have a fully symmetric role.
 */
public class ForexNonDeliverableOptionDefinition implements InstrumentDefinition<InstrumentDerivative> {

  /**
   * The underlying Forex non-deliverable transaction (the one entered into in case of exercise).
   * The NDF fixing date is the option exercise date.
   */
  private final ForexNonDeliverableForwardDefinition _underlyingNDF;
  /**
   * The call (true) / put (false) flag.
   */
  private final boolean _isCall;
  /**
   * The long (true) / short (false) flag.
   */
  private final boolean _isLong;

  /**
   * Constructor from the details.
   * @param forex The underlying Forex transaction.
   * @param isCall The call (true) / put (false) flag. When the option is a call, the option holder has the right to enter into the Forex NDF; when the option is a put, 
   * the option holder has the right to enter into a NDF transaction equal to the underlying but with opposite signs.
   * @param isLong The long (true) / short (false) flag.
   */
  public ForexNonDeliverableOptionDefinition(final ForexNonDeliverableForwardDefinition forex, final boolean isCall, boolean isLong) {
    Validate.notNull(forex, "Underlying forex");
    _underlyingNDF = forex;
    _isCall = isCall;
    _isLong = isLong;
  }

  /**
   * Gets the underlying Forex transaction.
   * @return The underlying Forex transaction.
   */
  public ForexNonDeliverableForwardDefinition getUnderlyingNDF() {
    return _underlyingNDF;
  }

  /**
   * Gets the call (true) / put (false) flag.
   * @return The call / put flag.
   */
  public boolean isCall() {
    return _isCall;
  }

  /**
   * Gets the long (true) / short (false) flag.
   * @return The long / short flag.
   */
  public boolean isLong() {
    return _isLong;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ForexNonDeliverableOption toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return new ForexNonDeliverableOption(_underlyingNDF.toDerivative(date, yieldCurveNames), _isCall, _isLong);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitForexNonDeliverableOptionDefinition(this, data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitForexNonDeliverableOptionDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_isCall ? 1231 : 1237);
    result = prime * result + (_isLong ? 1231 : 1237);
    result = prime * result + _underlyingNDF.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ForexNonDeliverableOptionDefinition other = (ForexNonDeliverableOptionDefinition) obj;
    if (_isCall != other._isCall) {
      return false;
    }
    if (_isLong != other._isLong) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingNDF, other._underlyingNDF)) {
      return false;
    }
    return true;
  }

}
