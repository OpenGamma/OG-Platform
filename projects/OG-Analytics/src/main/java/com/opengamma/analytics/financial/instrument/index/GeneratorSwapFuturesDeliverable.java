/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableTransactionDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Class used to store future description and generate instruments.
 */
public class GeneratorSwapFuturesDeliverable extends GeneratorInstrument<GeneratorAttribute> {

  /**
   * The underlying Deliverable swap futures security.
   */
  private final SwapFuturesPriceDeliverableSecurityDefinition _security;

  /**
   * Constructor.
   * @param name The generator name.
   * @param security The underlying deliverable swap futures security.
   */
  public GeneratorSwapFuturesDeliverable(final String name, final SwapFuturesPriceDeliverableSecurityDefinition security) {
    super(name);
    ArgumentChecker.notNull(security, "STIR futures security");
    _security = security;
  }

  /**
   * Gets the deliverable swap futures security.
   * @return The futures.
   */
  public SwapFuturesPriceDeliverableSecurityDefinition getFutures() {
    return _security;
  }

  /**
   * {@inheritDoc}
   * The quantity is selected to be in line with the required nominal.
   */
  @Override
  public SwapFuturesPriceDeliverableTransactionDefinition generateInstrument(final ZonedDateTime date, final double marketQuote, final double notional, final GeneratorAttribute attribute) {
    final int quantity = (int) Math.ceil(notional / _security.getNotional());
    return new SwapFuturesPriceDeliverableTransactionDefinition(_security, quantity, date, marketQuote);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _security.hashCode();
    return result;
  }

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
    final GeneratorSwapFuturesDeliverable other = (GeneratorSwapFuturesDeliverable) obj;
    if (!ObjectUtils.equals(_security, other._security)) {
      return false;
    }
    return true;
  }

}
