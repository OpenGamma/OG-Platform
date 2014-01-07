/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of {@link Resolver} for trivial conversions of unique identifier encoded values to primitive objects.
 * 
 * @param <T> the target type resolved by this class
 */
public abstract class AbstractPrimitiveResolver<T extends UniqueIdentifiable> implements ObjectResolver<T> {

  private final String _identifierScheme;

  public AbstractPrimitiveResolver(final String identifierScheme) {
    ArgumentChecker.notNull(identifierScheme, "identifierScheme");
    _identifierScheme = identifierScheme;
  }

  protected String getIdentifierScheme() {
    return _identifierScheme;
  }

  protected T resolveObject(final String value) {
    throw new UnsupportedOperationException("Not implemented");
  }

  protected T resolveObject(final UniqueId identifier) {
    return resolveObject(identifier.getValue());
  }

  // Resolver

  @Override
  public T resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    if (getIdentifierScheme().equals(uniqueId.getScheme())) {
      return resolveObject(uniqueId);
    } else {
      return null;
    }
  }

  @Override
  public DeepResolver deepResolver() {
    return null;
  }

  // ChangeProvider

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

}
