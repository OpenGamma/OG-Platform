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

/**
 * A {@link ObjectResolver} for {@link ComputationTargetType#PRIMITIVE}.
 */
public class PrimitiveResolver implements ObjectResolver<UniqueId> {

  @Override
  public UniqueId resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    return uniqueId;
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

}
