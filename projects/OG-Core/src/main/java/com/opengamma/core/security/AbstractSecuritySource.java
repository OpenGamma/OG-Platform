/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.core.AbstractSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;

/**
 * Partial implementation of {@link SecuritySource}.
 */
public abstract class AbstractSecuritySource extends AbstractSource<Security> implements SecuritySource {

  public static Map<ExternalIdBundle, Collection<Security>> getAll(final SecuritySource securitySource, final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Collection<Security>> results = Maps.newHashMapWithExpectedSize(bundles.size());
    for (ExternalIdBundle bundle : bundles) {
      final Collection<Security> result = securitySource.get(bundle, versionCorrection);
      if ((result != null) && !result.isEmpty()) {
        results.put(bundle, result);
      }
    }
    return results;
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    return getAll(this, bundles, versionCorrection);
  }

  public static Map<ExternalIdBundle, Security> getSingle(final SecuritySource securitySource, final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Security> results = Maps.newHashMapWithExpectedSize(bundles.size());
    for (ExternalIdBundle bundle : bundles) {
      final Security result = securitySource.getSingle(bundle, versionCorrection);
      if (result != null) {
        results.put(bundle, result);
      }
    }
    return results;
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    return getSingle(this, bundles, versionCorrection);
  }

}
