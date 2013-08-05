/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link ObjectResolver} built on a {@link SecuritySource}.
 */
public class SecuritySourceResolver extends AbstractIdentifierResolver implements Resolver<Security> {

  private final SecuritySource _underlying;

  public SecuritySourceResolver(final SecuritySource underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  protected SecuritySource getUnderlying() {
    return _underlying;
  }

  protected ExternalIdBundle replaceWeakTickers(final ExternalIdBundle identifiers) {
    final String bbgWeakTicker = identifiers.getValue(ExternalSchemes.BLOOMBERG_TICKER_WEAK);
    final String bbgWeakBUID = identifiers.getValue(ExternalSchemes.BLOOMBERG_BUID_WEAK);
    if ((bbgWeakTicker != null) || (bbgWeakBUID != null)) {
      final List<ExternalId> ids = new ArrayList<ExternalId>();
      for (final ExternalId identifier : identifiers) {
        if (ExternalSchemes.BLOOMBERG_TICKER_WEAK.equals(identifier.getScheme())) {
          ids.add(ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, identifier.getValue()));
        } else if (ExternalSchemes.BLOOMBERG_BUID_WEAK.equals(identifier.getScheme())) {
          ids.add(ExternalId.of(ExternalSchemes.BLOOMBERG_BUID, identifier.getValue()));
        } else {
          ids.add(identifier);
        }
      }
      return ExternalIdBundle.of(ids);
    } else {
      return identifiers;
    }
  }

  // ObjectResolver

  @Override
  public Security resolveObject(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    try {
      return getUnderlying().get(uniqueId);
    } catch (final DataNotFoundException e) {
      return null;
    }
  }

  @Override
  public DeepResolver deepResolver() {
    return null;
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  // IdentifierResolver

  @Override
  public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
    final Security security = getUnderlying().getSingle(replaceWeakTickers(identifiers), versionCorrection);
    if (security == null) {
      return null;
    } else {
      return security.getUniqueId();
    }
  }

  @Override
  public Map<ExternalIdBundle, UniqueId> resolveExternalIds(final Collection<ExternalIdBundle> identifiers, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, ? extends Security> securities = getUnderlying().getSingle(identifiers, versionCorrection);
    final Map<ExternalIdBundle, UniqueId> result = Maps.newHashMapWithExpectedSize(securities.size());
    for (Map.Entry<ExternalIdBundle, ? extends Security> security : securities.entrySet()) {
      result.put(security.getKey(), security.getValue().getUniqueId());
    }
    return result;
  }

  @Override
  public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
    try {
      return getUnderlying().get(identifier, versionCorrection).getUniqueId();
    } catch (final DataNotFoundException e) {
      return null;
    }
  }

  @Override
  public Map<ObjectId, UniqueId> resolveObjectIds(final Collection<ObjectId> identifiers, final VersionCorrection versionCorrection) {
    final Map<ObjectId, Security> securities = getUnderlying().get(identifiers, versionCorrection);
    final Map<ObjectId, UniqueId> result = Maps.newHashMapWithExpectedSize(securities.size());
    for (Map.Entry<ObjectId, Security> security : securities.entrySet()) {
      result.put(security.getKey(), security.getValue().getUniqueId());
    }
    return result;
  }

}
