/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.target;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.resolver.DeepResolver;
import com.opengamma.engine.target.resolver.Resolver;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * Target resolver for configuration sourced objects.
 * 
 * @param <T> type resolved by this resolver
 */
public class ConfigTargetResolver<T extends UniqueIdentifiable> implements Resolver<T> {

  private final ConfigItemTargetResolver<T> _underlying;

  public ConfigTargetResolver(final Class<T> type, final ExternalScheme scheme, final ConfigSource configSource) {
    _underlying = new ConfigItemTargetResolver<T>(type, scheme, configSource);
  }

  public ConfigTargetResolver(final Class<T> type, final ConfigSource configSource) {
    _underlying = new ConfigItemTargetResolver<T>(type, configSource);
  }

  public static <T extends UniqueIdentifiable> void initResolver(final DefaultComputationTargetResolver resolver, final Class<T> clazz, final ConfigSource configSource) {
    resolver.addResolver(ComputationTargetType.of(clazz), new ConfigTargetResolver<T>(clazz, configSource));
  }

  protected ConfigItemTargetResolver<T> getUnderlying() {
    return _underlying;
  }

  // Resolver

  @SuppressWarnings("unchecked")
  @Override
  public T resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    final ConfigItem<?> item = getUnderlying().resolveObject(uniqueId, versionCorrection);
    if (item != null) {
      return (T) item.getValue();
    }
    return null;
  }

  @Override
  public DeepResolver deepResolver() {
    return getUnderlying().deepResolver();
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  @Override
  public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
    return getUnderlying().resolveExternalId(identifiers, versionCorrection);
  }

  @Override
  public Map<ExternalIdBundle, UniqueId> resolveExternalIds(final Collection<ExternalIdBundle> identifiers, final VersionCorrection versionCorrection) {
    return getUnderlying().resolveExternalIds(identifiers, versionCorrection);
  }

  @Override
  public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
    return getUnderlying().resolveObjectId(identifier, versionCorrection);
  }

  @Override
  public Map<ObjectId, UniqueId> resolveObjectIds(final Collection<ObjectId> identifiers, final VersionCorrection versionCorrection) {
    return getUnderlying().resolveObjectIds(identifiers, versionCorrection);
  }

}
