/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import java.util.Collection;
import java.util.Collections;
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

  public static Map<ExternalIdBundle, UniqueId> resolveExternalIdsMultiThread(final PoolExecutor executor, final IdentifierResolver resolver, final Collection<ExternalIdBundle> identifiers,
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

  public static Map<ExternalIdBundle, UniqueId> resolveExternalIdsSingleThread(final IdentifierResolver resolver, final Collection<ExternalIdBundle> identifiers,
      final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, UniqueId> result = Maps.newHashMapWithExpectedSize(identifiers.size());
    for (final ExternalIdBundle identifier : identifiers) {
      final UniqueId uid = resolver.resolveExternalId(identifier, versionCorrection);
      if (uid != null) {
        result.put(identifier, uid);
      }
    }
    return result;
  }

  public static Map<ExternalIdBundle, UniqueId> resolveExternalIds(final IdentifierResolver resolver, final Collection<ExternalIdBundle> identifiers,
      final VersionCorrection versionCorrection) {
    if (identifiers.isEmpty()) {
      return Collections.emptyMap();
    } else if (identifiers.size() == 1) {
      final ExternalIdBundle identifier = identifiers.iterator().next();
      final UniqueId uid = resolver.resolveExternalId(identifier, versionCorrection);
      if (uid != null) {
        return Collections.singletonMap(identifier, uid);
      } else {
        return Collections.emptyMap();
      }
    }
    final PoolExecutor executor = PoolExecutor.instance();
    if (executor != null) {
      return resolveExternalIdsMultiThread(executor, resolver, identifiers, versionCorrection);
    } else {
      return resolveExternalIdsSingleThread(resolver, identifiers, versionCorrection);
    }
  }

  public static Map<ObjectId, UniqueId> resolveObjectIdsMultiThread(final PoolExecutor executor, final IdentifierResolver resolver, final Collection<ObjectId> identifiers,
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

  public static Map<ObjectId, UniqueId> resolveObjectIdsSingleThread(final IdentifierResolver resolver, final Collection<ObjectId> identifiers, final VersionCorrection versionCorrection) {
    final Map<ObjectId, UniqueId> result = Maps.newHashMapWithExpectedSize(identifiers.size());
    for (final ObjectId identifier : identifiers) {
      final UniqueId uid = resolver.resolveObjectId(identifier, versionCorrection);
      if (uid != null) {
        result.put(identifier, uid);
      }
    }
    return result;
  }

  public static Map<ObjectId, UniqueId> resolveObjectIds(final IdentifierResolver resolver, final Collection<ObjectId> identifiers, final VersionCorrection versionCorrection) {
    if (identifiers.isEmpty()) {
      return Collections.emptyMap();
    } else if (identifiers.size() == 1) {
      final ObjectId identifier = identifiers.iterator().next();
      final UniqueId uid = resolver.resolveObjectId(identifier, versionCorrection);
      if (uid != null) {
        return Collections.singletonMap(identifier, uid);
      } else {
        return Collections.emptyMap();
      }
    }
    final PoolExecutor executor = PoolExecutor.instance();
    if (executor != null) {
      return resolveObjectIdsMultiThread(executor, resolver, identifiers, versionCorrection);
    } else {
      return resolveObjectIdsSingleThread(resolver, identifiers, versionCorrection);
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
