/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.DataNotFoundException;
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
  public Security resolve(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    try {
      return getUnderlying().getSecurity(uniqueId);
    } catch (DataNotFoundException e) {
      return null;
    }
  }

  // IdentifierResolver

  @Override
  public UniqueId resolve(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
    final Security security = getUnderlying().getSecurity(identifiers, versionCorrection);
    if (security == null) {
      return null;
    } else {
      return security.getUniqueId();
    }
  }

  @Override
  public UniqueId resolve(final ObjectId identifier, final VersionCorrection versionCorrection) {
    try {
      return getUnderlying().getSecurity(identifier, versionCorrection).getUniqueId();
    } catch (DataNotFoundException e) {
      return null;
    }
  }

}
