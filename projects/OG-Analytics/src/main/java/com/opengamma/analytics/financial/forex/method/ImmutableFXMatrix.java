/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import com.opengamma.util.money.Currency;

/**
 * A sub-class of {@link FXMatrix} that has strict immutabilty
 * guarantees. This allows the {@link FXMatrix#copy()} operator
 * to just return a handle to this instance.
 */
public class ImmutableFXMatrix extends FXMatrix {
  private static final long serialVersionUID = 1L;
  
  public ImmutableFXMatrix() {
    super();
  }
  
  public ImmutableFXMatrix(FXMatrix matrix) {
    super(matrix);
  }

  @Override
  public void addCurrency(Currency ccyToAdd, Currency ccyReference, double fxRate) {
    throw new UnsupportedOperationException("This instance is immutable.");
  }

  @Override
  public void updateRates(Currency ccyToUpdate, Currency ccyReference, double fxRate) {
    throw new UnsupportedOperationException("This instance is immutable.");
  }

  @Override
  public FXMatrix copy() {
    return this;
  }

}
