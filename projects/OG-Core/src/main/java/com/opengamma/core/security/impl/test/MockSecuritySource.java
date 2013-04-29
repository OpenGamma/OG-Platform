/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl.test;

import java.util.Collection;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.AbstractSecuritySource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * A mock security source.
 */
public class MockSecuritySource extends AbstractSecuritySource implements SecuritySource {

  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Security get(UniqueId uniqueId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Security get(ObjectId objectId, VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Security getSingle(ExternalIdBundle bundle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Security getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException();
  }

}
