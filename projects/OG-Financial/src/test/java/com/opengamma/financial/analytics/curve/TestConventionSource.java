/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 *
 */
public class TestConventionSource implements ConventionSource {
  private final Map<ExternalId, Convention> _conventions;

  public TestConventionSource(final Map<ExternalId, Convention> conventions) {
    _conventions = conventions;
  }

  @Override
  public Convention getSingle(final ExternalId identifier) {
    Convention convention = _conventions.get(identifier);
    if (convention == null) {
      throw new DataNotFoundException("No convention found: " + identifier);
    }
    return convention;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Convention> T getSingle(final ExternalId identifier, final Class<T> clazz) {
    final Convention convention = _conventions.get(identifier);
    if (convention == null) {
      throw new DataNotFoundException("No convention found: " + identifier);
    }
    if (clazz.isAssignableFrom(convention.getClass())) {
      return (T) convention;
    }
    throw new OpenGammaRuntimeException("Convention for " + identifier + " was not of expected type " + clazz);
  }

  @Override
  public <T extends Convention> T getSingle(final ExternalIdBundle identifiers, final Class<T> clazz) {
    return null;
  }

  @Override
  public Collection<Convention> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public Map<ExternalIdBundle, Collection<Convention>> getAll(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public Map<ExternalIdBundle, Convention> getSingle(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public Convention get(UniqueId uniqueId) {
    return null;
  }

  @Override
  public Convention get(ObjectId objectId, VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public Map<UniqueId, Convention> get(Collection<UniqueId> uniqueIds) {
    return null;
  }

  @Override
  public Map<ObjectId, Convention> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    return null;
  }

  @Override
  public ChangeManager changeManager() {
    return null;
  }

  @Override
  public <T extends Convention> T get(UniqueId uniqueId, Class<T> type) {
    return null;
  }

  @Override
  public <T extends Convention> T get(ObjectId objectId, VersionCorrection versionCorrection, Class<T> type) {
    return null;
  }

  @Override
  public <T extends Convention> T getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection, Class<T> type) {
    return null;
  }

  @Override
  public Collection<Convention> get(ExternalIdBundle bundle) {
    return null;
  }

  @Override
  public Convention getSingle(ExternalIdBundle bundle) {
    return null;
  }

  @Override
  public Convention getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return null;
  }

}
