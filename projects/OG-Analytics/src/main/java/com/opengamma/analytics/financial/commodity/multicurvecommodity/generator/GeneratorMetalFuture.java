/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.generator;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.MetalFutureSecurityDefinition;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.MetalFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.util.ArgumentChecker;

/**
 *Class used to store and generate an Metal Future  characteristics.
 */
public class GeneratorMetalFuture extends GeneratorInstrument<GeneratorAttribute> {

  /**
   * The underlying  futures security.
   */
  private final MetalFutureSecurityDefinition _security;

  /**
   * Constructor.
   * @param name The generator name.
   * @param security The underlying Metal futures security.
   */
  public GeneratorMetalFuture(final String name, final MetalFutureSecurityDefinition security) {
    super(name);
    ArgumentChecker.notNull(security, "Metal futures security");
    _security = security;
  }

  /**
   * Gets the metal futures security.
   * @return The futures.
   */
  public MetalFutureSecurityDefinition getFutures() {
    return _security;
  }

  /**
   * {@inheritDoc}
   * The quantity is modified to be in line with the required notional.
   */
  @Override
  public MetalFutureTransactionDefinition generateInstrument(final ZonedDateTime date, final double marketQuote, final double notional, final GeneratorAttribute attribute) {
    final int quantity = (int) Math.ceil(notional / _security.getUnitAmount());
    return new MetalFutureTransactionDefinition(_security, date, marketQuote, quantity);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_security == null) ? 0 : _security.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final GeneratorMetalFuture other = (GeneratorMetalFuture) obj;
    if (_security == null) {
      if (other._security != null) {
        return false;
      }
    } else if (!_security.equals(other._security)) {
      return false;
    }
    return true;
  }

}
