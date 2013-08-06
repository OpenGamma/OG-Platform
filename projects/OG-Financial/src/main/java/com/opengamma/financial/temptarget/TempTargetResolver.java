/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.engine.target.resolver.DeepResolver;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A target resolver implementation to connect the temporary target repository to the engine framework.
 */
public class TempTargetResolver implements ObjectResolver<UniqueIdentifiable> {

  private final TempTargetSource _targets;

  public TempTargetResolver(final TempTargetSource targets) {
    ArgumentChecker.notNull(targets, "targets");
    _targets = targets;
  }

  protected TempTargetSource getTargets() {
    return _targets;
  }

  @Override
  public ChangeManager changeManager() {
    return getTargets().changeManager();
  }

  @Override
  public UniqueIdentifiable resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    return getTargets().get(uniqueId);
  }

  @Override
  public DeepResolver deepResolver() {
    return null;
  }

}
