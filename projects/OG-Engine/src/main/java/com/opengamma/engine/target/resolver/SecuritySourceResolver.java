/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link ObjectResolver} built on a {@link SecuritySource}.
 */
public class SecuritySourceResolver implements Resolver<Security> {

  private final SecuritySource _underlying;

  public SecuritySourceResolver(final SecuritySource underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected SecuritySource getUnderlying() {
    return _underlying;
  }

  // ObjectResolver

  @Override
  public Security resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    try {
      return getUnderlying().get(uniqueId);
    } catch (DataNotFoundException e) {
      return null;
    }
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  // IdentifierResolver

  @Override
  public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
    final Security security = getUnderlying().getSingle(identifiers, versionCorrection);
    if (security == null) {
      return null;
    } else {
      return security.getUniqueId();
    }
  }

  @Override
  public Map<ExternalIdBundle, UniqueId> resolveExternalIds(final Set<ExternalIdBundle> identifiers, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, UniqueId> result = Maps.newHashMapWithExpectedSize(identifiers.size());
    for (ExternalIdBundle identifier : identifiers) {
      final UniqueId uid = resolveExternalId(identifier, versionCorrection);
      if (uid != null) {
        result.put(identifier, uid);
      }
    }
    return result;
  }

  @Override
  public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
    try {
      return getUnderlying().get(identifier, versionCorrection).getUniqueId();
    } catch (DataNotFoundException e) {
      return null;
    }
  }

  @Override
  public Map<ObjectId, UniqueId> resolveObjectIds(final Set<ObjectId> identifiers, final VersionCorrection versionCorrection) {
    final Map<ObjectId, UniqueId> result = Maps.newHashMapWithExpectedSize(identifiers.size());
    for (ObjectId identifier : identifiers) {
      final UniqueId uid = resolveObjectId(identifier, versionCorrection);
      if (uid != null) {
        result.put(identifier, uid);
      }
    }
    return result;
  }

}
