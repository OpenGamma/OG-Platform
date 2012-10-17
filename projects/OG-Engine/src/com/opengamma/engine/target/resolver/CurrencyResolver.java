/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * A {@link Resolver} for {@link ComputationTargetType#CURRENCY}.
 */
public class CurrencyResolver implements Resolver<Currency> {

  @Override
  public Currency resolve(final UniqueId uniqueId) {
    if (Currency.OBJECT_SCHEME.equals(uniqueId.getScheme())) {
      return Currency.of(uniqueId.getValue());
    } else {
      return null;
    }
  }

}
