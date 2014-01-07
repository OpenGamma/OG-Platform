/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AgricultureFutureTransaction extends CommodityFutureTransaction {

  public AgricultureFutureTransaction(final AgricultureFutureSecurity underlying, final int quantity, final double referencePrice) {
    super(underlying, quantity, referencePrice);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitAgricultureFutureTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitAgricultureFutureTransaction(this);
  }

}
