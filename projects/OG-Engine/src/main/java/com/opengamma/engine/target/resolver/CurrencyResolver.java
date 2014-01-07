/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.util.money.Currency;

/**
 * A {@link ObjectResolver} for {@link ComputationTargetType#CURRENCY}.
 */
public class CurrencyResolver extends AbstractPrimitiveResolver<Currency> {

  public CurrencyResolver() {
    super(Currency.OBJECT_SCHEME);
  }

  @Override
  protected Currency resolveObject(final String identifier) {
    return Currency.of(identifier);
  }

}
