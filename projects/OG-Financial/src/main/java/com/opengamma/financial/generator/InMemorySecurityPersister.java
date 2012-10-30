/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link SecurityPersister} associated with a {@link MockPositionSource}.
 */
public class InMemorySecurityPersister extends SecurityPersister {

  private final InMemorySecuritySource _source;

  public InMemorySecurityPersister() {
    this(new InMemorySecuritySource());
  }

  public InMemorySecurityPersister(final InMemorySecuritySource source) {
    ArgumentChecker.notNull(source, "source");
    _source = source;
  }

  public InMemorySecuritySource getSecuritySource() {
    return _source;
  }

  @Override
  protected void storeSecurityImpl(final ManageableSecurity security) {
    getSecuritySource().addSecurity(security);
  }

}
