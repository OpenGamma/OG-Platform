/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.util.Collection;
import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link SecuritySource} wrapper which sets a specific version/correction on all requests that would otherwise request "latest".
 * <p>
 * Where possible, code should be written that explicitly passes the necessary version/correction information around - this is an intermediate solution for working with existing code that is not
 * properly version aware.
 * 
 * @deprecated Call code that is properly version aware (whenever possible)
 */
@Deprecated
public class VersionLockedSecuritySource implements SecuritySource {

  private final SecuritySource _underlying;
  private final VersionCorrection _versionCorrection;

  public VersionLockedSecuritySource(final SecuritySource underlying, final VersionCorrection versionCorrection) {
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
    _versionCorrection = ArgumentChecker.notNull(versionCorrection, "versionCorrection");
  }

  protected SecuritySource getUnderlying() {
    return _underlying;
  }

  protected VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  protected VersionCorrection lockVersionCorrection(final VersionCorrection versionCorrection) {
    if (versionCorrection.containsLatest()) {
      final Instant version = (versionCorrection.getVersionAsOf() == null) ? getVersionCorrection().getVersionAsOf() : versionCorrection.getVersionAsOf();
      final Instant correction = (versionCorrection.getCorrectedTo() == null) ? getVersionCorrection().getCorrectedTo() : versionCorrection.getCorrectedTo();
      return VersionCorrection.of(version, correction);
    } else {
      return versionCorrection;
    }
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return getUnderlying().get(bundle, lockVersionCorrection(versionCorrection));
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return getUnderlying().getAll(bundles, lockVersionCorrection(versionCorrection));
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle) {
    return getUnderlying().get(bundle, getVersionCorrection());
  }

  @Override
  public Security getSingle(ExternalIdBundle bundle) {
    return getUnderlying().getSingle(bundle, getVersionCorrection());
  }

  @Override
  public Security getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return getUnderlying().getSingle(bundle, lockVersionCorrection(versionCorrection));
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection) {
    return getUnderlying().getSingle(bundles, lockVersionCorrection(versionCorrection));
  }

  @Override
  public Security get(UniqueId uniqueId) {
    return getUnderlying().get(uniqueId);
  }

  @Override
  public Security get(ObjectId objectId, VersionCorrection versionCorrection) {
    return getUnderlying().get(objectId, lockVersionCorrection(versionCorrection));
  }

  @Override
  public Map<UniqueId, Security> get(Collection<UniqueId> uniqueIds) {
    return getUnderlying().get(uniqueIds);
  }

  @Override
  public Map<ObjectId, Security> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection) {
    return getUnderlying().get(objectIds, lockVersionCorrection(versionCorrection));
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }
}
