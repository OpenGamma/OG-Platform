/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link SecurityPersister} associated with a {@link MockPositionSource}.
 */
public class MockSecurityPersister extends SecurityPersister {

  private final MockSecuritySource _source;

  public MockSecurityPersister() {
    this(new MockSecuritySource());
  }

  public MockSecurityPersister(final MockSecuritySource source) {
    ArgumentChecker.notNull(source, "source");
    _source = source;
  }

  public MockSecuritySource getMockSecuritySource() {
    return _source;
  }

  @Override
  protected void storeSecurityImpl(final ManageableSecurity security) {
    getMockSecuritySource().addSecurity(security);
  }

}
