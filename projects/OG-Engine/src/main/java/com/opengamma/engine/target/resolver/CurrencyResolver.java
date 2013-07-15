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
import com.opengamma.util.money.Currency;

/**
 * A {@link ObjectResolver} for {@link ComputationTargetType#CURRENCY}.
 */
public class CurrencyResolver implements ObjectResolver<Currency> {

  @Override
  public Currency resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    if (Currency.OBJECT_SCHEME.equals(uniqueId.getScheme())) {
      return Currency.of(uniqueId.getValue());
    } else {
      return null;
    }
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  @Override
  public boolean isDeepResolver() {
    return false;
  }

}
