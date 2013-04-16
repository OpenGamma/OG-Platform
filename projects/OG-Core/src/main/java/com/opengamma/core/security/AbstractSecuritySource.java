/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.AbstractSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PoolExecutor;

/**
 * Partial implementation of {@link SecuritySource}.
 */
public abstract class AbstractSecuritySource extends AbstractSource<Security> implements SecuritySource {

  public static Map<ExternalIdBundle, Collection<Security>> getAllMultiThread(final PoolExecutor executor, final SecuritySource securitySource, final Collection<ExternalIdBundle> bundles,
      final VersionCorrection versionCorrection) {
    final PoolExecutor.Service<Void> jobs = executor.createService(null);
    final Map<ExternalIdBundle, Collection<Security>> results = Maps.newHashMapWithExpectedSize(bundles.size());
    for (final ExternalIdBundle bundle : bundles) {
      jobs.execute(new Runnable() {
        @Override
        public void run() {
          final Collection<Security> result = securitySource.get(bundle, versionCorrection);
          if ((result != null) && !result.isEmpty()) {
            results.put(bundle, result);
          }
        }
      });
    }
    try {
      jobs.join();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    return results;
  }

  public static Map<ExternalIdBundle, Collection<Security>> getAllSingleThread(final SecuritySource securitySource, final Collection<ExternalIdBundle> bundles,
      final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Collection<Security>> results = Maps.newHashMapWithExpectedSize(bundles.size());
    for (ExternalIdBundle bundle : bundles) {
      final Collection<Security> result = securitySource.get(bundle, versionCorrection);
      if ((result != null) && !result.isEmpty()) {
        results.put(bundle, result);
      }
    }
    return results;
  }

  public static Map<ExternalIdBundle, Collection<Security>> getAll(final SecuritySource securitySource, final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    if (bundles.isEmpty()) {
      return Collections.emptyMap();
    } else if (bundles.size() == 1) {
      final ExternalIdBundle bundle = bundles.iterator().next();
      final Collection<Security> result = securitySource.get(bundle, versionCorrection);
      if ((result != null) && !result.isEmpty()) {
        return Collections.singletonMap(bundle, result);
      } else {
        return Collections.emptyMap();
      }
    }
    final PoolExecutor executor = PoolExecutor.instance();
    if (executor != null) {
      return getAllMultiThread(executor, securitySource, bundles, versionCorrection);
    } else {
      return getAllSingleThread(securitySource, bundles, versionCorrection);
    }
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    return getAll(this, bundles, versionCorrection);
  }

  public static Map<ExternalIdBundle, Security> getSingleMultiThread(final PoolExecutor executor, final SecuritySource securitySource, final Collection<ExternalIdBundle> bundles,
      final VersionCorrection versionCorrection) {
    final PoolExecutor.Service<Void> jobs = executor.createService(null);
    final Map<ExternalIdBundle, Security> results = Maps.newHashMapWithExpectedSize(bundles.size());
    for (final ExternalIdBundle bundle : bundles) {
      jobs.execute(new Runnable() {
        @Override
        public void run() {
          final Security result = securitySource.getSingle(bundle, versionCorrection);
          if (result != null) {
            results.put(bundle, result);
          }
        }
      });
    }
    try {
      jobs.join();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    return results;
  }

  public static Map<ExternalIdBundle, Security> getSingleSingleThread(final SecuritySource securitySource, final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Security> results = Maps.newHashMapWithExpectedSize(bundles.size());
    for (ExternalIdBundle bundle : bundles) {
      final Security result = securitySource.getSingle(bundle, versionCorrection);
      if (result != null) {
        results.put(bundle, result);
      }
    }
    return results;
  }

  public static Map<ExternalIdBundle, Security> getSingle(final SecuritySource securitySource, final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    if (bundles.isEmpty()) {
      return Collections.emptyMap();
    } else if (bundles.size() == 1) {
      final ExternalIdBundle bundle = bundles.iterator().next();
      final Security security = securitySource.getSingle(bundle, versionCorrection);
      if (security != null) {
        return Collections.singletonMap(bundle, security);
      } else {
        return Collections.emptyMap();
      }
    }
    final PoolExecutor executor = PoolExecutor.instance();
    if (executor != null) {
      return getSingleMultiThread(executor, securitySource, bundles, versionCorrection);
    } else {
      return getSingleSingleThread(securitySource, bundles, versionCorrection);
    }
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    return getSingle(this, bundles, versionCorrection);
  }

}
