/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PoolExecutor;

/**
 * Partial implementation of {@link IdentifierResolver}.
 */
public abstract class AbstractIdentifierResolver implements IdentifierResolver {

  public static Map<ExternalIdBundle, UniqueId> resolveExternalIds(final PoolExecutor executor, final IdentifierResolver resolver, final Collection<ExternalIdBundle> identifiers,
      final VersionCorrection versionCorrection) {
    final PoolExecutor.Service<Void> jobs = executor.createService(null);
    final Map<ExternalIdBundle, UniqueId> result = Maps.newHashMapWithExpectedSize(identifiers.size());
    for (final ExternalIdBundle identifier : identifiers) {
      jobs.execute(new Runnable() {
        @Override
        public void run() {
          final UniqueId uid = resolver.resolveExternalId(identifier, versionCorrection);
          if (uid != null) {
            synchronized (result) {
              result.put(identifier, uid);
            }
          }
        }
      });
    }
    try {
      jobs.join();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    return result;
  }

  public static Map<ExternalIdBundle, UniqueId> resolveExternalIds(final IdentifierResolver resolver, final Collection<ExternalIdBundle> identifiers,
      final VersionCorrection versionCorrection) {
    final PoolExecutor executor = PoolExecutor.instance();
    if (executor != null) {
      return resolveExternalIds(executor, resolver, identifiers, versionCorrection);
    } else {
      final Map<ExternalIdBundle, UniqueId> result = Maps.newHashMapWithExpectedSize(identifiers.size());
      for (final ExternalIdBundle identifier : identifiers) {
        final UniqueId uid = resolver.resolveExternalId(identifier, versionCorrection);
        if (uid != null) {
          result.put(identifier, uid);
        }
      }
      return result;
    }
  }

  public static Map<ObjectId, UniqueId> resolveObjectIds(final PoolExecutor executor, final IdentifierResolver resolver, final Collection<ObjectId> identifiers,
      final VersionCorrection versionCorrection) {
    final PoolExecutor.Service<Void> jobs = executor.createService(null);
    final Map<ObjectId, UniqueId> result = Maps.newHashMapWithExpectedSize(identifiers.size());
    for (final ObjectId identifier : identifiers) {
      jobs.execute(new Runnable() {
        @Override
        public void run() {
          final UniqueId uid = resolver.resolveObjectId(identifier, versionCorrection);
          if (uid != null) {
            synchronized (result) {
              result.put(identifier, uid);
            }
          }
        }
      });
    }
    try {
      jobs.join();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    return result;
  }

  public static Map<ObjectId, UniqueId> resolveObjectIds(final IdentifierResolver resolver, final Collection<ObjectId> identifiers, final VersionCorrection versionCorrection) {
    final PoolExecutor executor = PoolExecutor.instance();
    if (executor != null) {
      return resolveObjectIds(executor, resolver, identifiers, versionCorrection);
    } else {
      final Map<ObjectId, UniqueId> result = Maps.newHashMapWithExpectedSize(identifiers.size());
      for (final ObjectId identifier : identifiers) {
        final UniqueId uid = resolver.resolveObjectId(identifier, versionCorrection);
        if (uid != null) {
          result.put(identifier, uid);
        }
      }
      return result;
    }
  }

  // IdentifierResolver

  @Override
  public Map<ExternalIdBundle, UniqueId> resolveExternalIds(Collection<ExternalIdBundle> identifiers, VersionCorrection versionCorrection) {
    return resolveExternalIds(this, identifiers, versionCorrection);
  }

  @Override
  public Map<ObjectId, UniqueId> resolveObjectIds(Collection<ObjectId> identifiers, VersionCorrection versionCorrection) {
    return resolveObjectIds(this, identifiers, versionCorrection);
  }

}
