/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;
import java.util.Set;

import javax.time.InstantProvider;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * This SecurityMaster retrieves all securities as of a fixed historical date.
 */
public class HistoricallyFixedSecurityMaster implements SecurityMaster {
  
  private final ManageableSecurityMaster _delegate;
  private final InstantProvider _fixTime;
  private final InstantProvider _asViewedAt;
  
  public HistoricallyFixedSecurityMaster(ManageableSecurityMaster delegate,
      InstantProvider fixTime,
      InstantProvider asViewedAt) {
    ArgumentChecker.notNull(delegate, "Delegate Security Master");
    ArgumentChecker.notNull(fixTime, "Fix Time");
    ArgumentChecker.notNull(asViewedAt, "As Viewed At Time");
    
    _delegate = delegate;
    _fixTime = fixTime;
    _asViewedAt = asViewedAt;
  }

  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    return _delegate.getSecurity(uid, _fixTime, _asViewedAt);
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    return _delegate.getSecurities(secKey); // TODO
  }

  @Override
  public Security getSecurity(IdentifierBundle secKey) {
    return _delegate.getSecurity(secKey); // TODO
  }

  @Override
  public Set<String> getAllSecurityTypes() {
    return _delegate.getAllSecurityTypes(); // TODO
  }
  
}
