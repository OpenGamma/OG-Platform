/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import java.util.Set;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Source;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of {@link Resolver} based on a {@link Source}.
 * 
 * @param <S> the type of the source
 */
public abstract class AbstractSourceResolver<T extends UniqueIdentifiable, S extends Source<T> & ChangeProvider> extends AbstractIdentifierResolver implements Resolver<T> { //CSIGNORE

  private final ExternalScheme _identifierScheme;
  private final S _underlying;

  public AbstractSourceResolver(final ExternalScheme identifierScheme, S underlying) {
    ArgumentChecker.notNull(identifierScheme, "identifierScheme");
    ArgumentChecker.notNull(underlying, "underlying");
    _identifierScheme = identifierScheme;
    _underlying = underlying;
  }

  protected ExternalScheme getIdentifierScheme() {
    return _identifierScheme;
  }

  protected S getUnderlying() {
    return _underlying;
  }

  /**
   * Queries the underlying using whatever API calls are available to resolve the symbolic name.
   * 
   * @param name the symbolic name of the item to retrieve, not null
   * @param versionCorrection the version/correction to resolve at, not null
   * @return the resolved object, or null if none
   */
  protected abstract UniqueIdentifiable lookupByName(String name, VersionCorrection versionCorrection);

  // Resolver

  @Override
  public T resolveObject(UniqueId uniqueId, VersionCorrection versionCorrection) {
    try {
      return getUnderlying().get(uniqueId);
    } catch (DataNotFoundException e) {
      return null;
    }
  }

  @Override
  public DeepResolver deepResolver() {
    return null;
  }

  // IdentifierResolver

  @Override
  public UniqueId resolveExternalId(ExternalIdBundle identifiers, VersionCorrection versionCorrection) {
    final Set<String> names = identifiers.getValues(getIdentifierScheme());
    for (String name : names) {
      final UniqueIdentifiable value = lookupByName(name, versionCorrection);
      if (value != null) {
        return value.getUniqueId();
      }
    }
    return null;
  }

  @Override
  public UniqueId resolveObjectId(ObjectId identifier, VersionCorrection versionCorrection) {
    try {
      return getUnderlying().get(identifier, versionCorrection).getUniqueId();
    } catch (DataNotFoundException e) {
      return null;
    }
  }

  // ChangeProvider

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

}
