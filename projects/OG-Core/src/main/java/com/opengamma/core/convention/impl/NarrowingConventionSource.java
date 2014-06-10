/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.convention.impl;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A convention source that delegates to an another source, but which ensures
 * that it only calls the get methods on the delegate. This is intended to
 * allow the use of proxy classes as the delegates which allows different
 * behaviours e.g. capturing the data returned from sources.
 */
public class NarrowingConventionSource implements ConventionSource {

  private final ConventionSource _delegate;

  /**
   * Create a narrowing source, wrapping the provided source.
   *
   * @param delegate the source to delegate to, not null
   */
  public NarrowingConventionSource(ConventionSource delegate) {
    _delegate = ArgumentChecker.notNull(delegate, "delegate");
  }

  @Override
  public <T extends Convention> T get(UniqueId uniqueId, Class<T> type) {
    return _delegate.get(uniqueId, type);
  }

  @Override
  public <T extends Convention> T get(ObjectId objectId, VersionCorrection versionCorrection, Class<T> type) {
    return _delegate.get(objectId, versionCorrection, type);
  }

  @Override
  public Convention getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    Collection<Convention> conventions = get(bundle, versionCorrection);
    if (conventions.isEmpty()) {
      throw new DataNotFoundException("No convention found for bundle: " + bundle);
    } else {
      return conventions.iterator().next();
    }
  }

  @Override
  public Map<ExternalIdBundle, Convention> getSingle(Collection<ExternalIdBundle> bundles,
                                                     VersionCorrection versionCorrection) {

    ImmutableMap.Builder<ExternalIdBundle, Convention> builder = ImmutableMap.builder();

    // Iterating and requesting singly is horrible but is
    // all the underlying source will do anyway so there
    // is no performance downside to doing it
    for (ExternalIdBundle bundle : bundles) {
      Collection<Convention> conventions = get(bundle, versionCorrection);
      if (!conventions.isEmpty()) {
        builder.put(bundle, conventions.iterator().next());
      }
    }
    return builder.build();
  }

  @Override
  public <T extends Convention> T getSingle(ExternalIdBundle bundle,
                                            VersionCorrection versionCorrection,
                                            Class<T> type) {
    Collection<Convention> conventions = get(bundle, versionCorrection);
    if (conventions.isEmpty()) {
      throw new DataNotFoundException("No convention found for bundle: " + bundle);
    } else {

      // Return first item matching name and type
      for (Convention convention : conventions) {
        if (type.isAssignableFrom(convention.getClass())) {
          return type.cast(convention);
        }
      }
      throw new DataNotFoundException("No convention of type: " + type + " found for bundle: " + bundle);
    }
  }

  @Override
  public Convention getSingle(ExternalId externalId) {
    return getSingle(externalId.toBundle());
  }

  @Override
  public <T extends Convention> T getSingle(ExternalId externalId, Class<T> type) {
    return getSingle(externalId.toBundle(), type);
  }

  @Override
  public Convention getSingle(ExternalIdBundle bundle) {
    return getSingle(bundle, VersionCorrection.LATEST);
  }

  @Override
  public <T extends Convention> T getSingle(ExternalIdBundle bundle, Class<T> type) {
    return getSingle(bundle, VersionCorrection.LATEST, type);
  }

  @Override
  public Collection<Convention> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return _delegate.get(bundle, versionCorrection);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Convention>> getAll(Collection<ExternalIdBundle> bundles,
                                                              VersionCorrection versionCorrection) {

    ImmutableMap.Builder<ExternalIdBundle, Collection<Convention>> builder = ImmutableMap.builder();

    // Iterating and requesting singly is horrible but is
    // all the underlying source will do anyway so there
    // is no performance downside to doing it
    for (ExternalIdBundle bundle : bundles) {
      Collection<Convention> conventions = get(bundle, versionCorrection);
      if (!conventions.isEmpty()) {
        builder.put(bundle, conventions);
      }
    }
    return builder.build();
  }

  @Override
  public Collection<Convention> get(ExternalIdBundle bundle) {
    return _delegate.get(bundle);
  }

  @Override
  public ChangeManager changeManager() {
    return _delegate.changeManager();
  }

  @Override
  public Convention get(UniqueId uniqueId) {
    return _delegate.get(uniqueId);
  }

  @Override
  public Convention get(ObjectId objectId, VersionCorrection versionCorrection) {
    return _delegate.get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, Convention> get(Collection<UniqueId> uniqueIds) {
    return _delegate.get(uniqueIds);
  }

  @Override
  public Map<ObjectId, Convention> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    return _delegate.get(objectIds, versionCorrection);
  }
}
