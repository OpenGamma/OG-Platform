/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link Resolver} built on a {@link SecuritySource}.
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

  @Override
  public Security resolve(final UniqueId uniqueId) {
    try {
      return getUnderlying().getSecurity(uniqueId);
    } catch (DataNotFoundException e) {
      return null;
    }
  }

}
