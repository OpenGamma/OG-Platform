/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A target resolver implementation to connect the temporary target repository to the engine framework.
 */
public class ConfigItemTargetResolver implements ObjectResolver<UniqueIdentifiable> {

  private final ConfigSource _source;

  public ConfigItemTargetResolver(final ConfigSource source) {
    ArgumentChecker.notNull(source, "config source");
    _source = source;
  }

  protected ConfigSource getSource() {
    return _source;
  }

  @Override
  public ChangeManager changeManager() {
    return getSource().changeManager();
  }

  @Override
  public UniqueIdentifiable resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    return (UniqueIdentifiable) _source.get(uniqueId).getValue();
  }

  @Override
  public boolean isDeepResolver() {
    return false;
  }

}
