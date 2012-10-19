/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * A {@link ObjectResolver} for {@link ComputationTargetType#UNORDERED_CURRENCY_PAIR}.
 */
public class UnorderedCurrencyPairResolver implements ObjectResolver<UnorderedCurrencyPair> {

  @Override
  public UnorderedCurrencyPair resolve(final UniqueId uniqueId) {
    if (UnorderedCurrencyPair.OBJECT_SCHEME.equals(uniqueId.getScheme())) {
      return UnorderedCurrencyPair.of(uniqueId);
    } else {
      return null;
    }
  }

}
