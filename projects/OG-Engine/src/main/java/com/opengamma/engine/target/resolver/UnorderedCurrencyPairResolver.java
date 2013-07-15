/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * A {@link ObjectResolver} for {@link ComputationTargetType#UNORDERED_CURRENCY_PAIR}.
 */
public class UnorderedCurrencyPairResolver implements ObjectResolver<UnorderedCurrencyPair> {

  @Override
  public UnorderedCurrencyPair resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    if (UnorderedCurrencyPair.OBJECT_SCHEME.equals(uniqueId.getScheme())) {
      return UnorderedCurrencyPair.of(uniqueId);
    } else {
      return null;
    }
  }

  @Override
  public boolean isDeepResolver() {
    return false;
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

}
