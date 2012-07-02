/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.id.VersionCorrection;

/**
 * Extension to {@link LazyResolveContext} that
 */
/* package */class VersionedLazyResolveContext extends LazyResolveContext {

  private final VersionCorrection _versionCorrection;

  public VersionedLazyResolveContext(final LazyResolveContext parent, final VersionCorrection versionCorrection) {
    super(parent.getSecuritySource(), parent.getTargetResolver());
    _versionCorrection = versionCorrection;
  }

  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  protected Security resolveLinkImpl(final SecurityLink link) {
    try {
      return link.resolve(getSecuritySource(), getVersionCorrection());
    } catch (DataNotFoundException e) {
      return null;
    }
  }

}
