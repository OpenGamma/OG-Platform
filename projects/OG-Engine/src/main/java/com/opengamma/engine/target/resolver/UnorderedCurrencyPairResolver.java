/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * A {@link ObjectResolver} for {@link ComputationTargetType#UNORDERED_CURRENCY_PAIR}.
 */
public class UnorderedCurrencyPairResolver extends AbstractPrimitiveResolver<UnorderedCurrencyPair> {

  public UnorderedCurrencyPairResolver() {
    super(UnorderedCurrencyPair.OBJECT_SCHEME);
  }

  @Override
  protected UnorderedCurrencyPair resolveObject(final String identifier) {
    return UnorderedCurrencyPair.parse(identifier);
  }

}
