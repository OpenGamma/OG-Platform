/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.credit.CreditCurveIdentifier;

/**
 * 
 */
public class CreditCurveIdentifierResolver implements ObjectResolver<CreditCurveIdentifier> {

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  @Override
  public CreditCurveIdentifier resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    if (CreditCurveIdentifier.OBJECT_SCHEME.equals(uniqueId.getScheme())) {
      return CreditCurveIdentifier.of(uniqueId);
    }
    return null;
  }

  @Override
  public boolean isDeepResolver() {
    return false;
  }

}
