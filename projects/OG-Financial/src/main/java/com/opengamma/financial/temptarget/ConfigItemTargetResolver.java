/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.target.resolver.DeepResolver;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A target resolver implementation to connect a config source to the engine framework.
 */
public class ConfigItemTargetResolver implements ObjectResolver<UniqueIdentifiable> {

  // REVIEW: 2013-08-15 Andrew -- Is this necessary; we should probably have more strongly typed resolvers that relate to the
  // config item types they are returning. Extending AbstractSourceResolver would probably be better too.

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
  public DeepResolver deepResolver() {
    return null;
  }

}
