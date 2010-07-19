/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;

import org.apache.commons.lang.Validate;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * A {@code SecuritySource} implemented using an underlying {@code SecurityMaster}.
 * <p>
 * The {@link SecuritySource} interface provides securities to the engine via a narrow API.
 * This class provides the source on top of a standard {@link SecurityMaster}.
 */
public class MasterSecuritySource implements SecuritySource {

  /**
   * The security master.
   */
  private final SecurityMaster _securityMaster;

  /**
   * Creates an instance with an underlying security master.
   * @param securityMaster  the security master, not null
   */
  public MasterSecuritySource(final SecurityMaster securityMaster) {
    Validate.notNull(securityMaster, "securityMaster");
    _securityMaster = securityMaster;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(final UniqueIdentifier uid) {
    Validate.notNull(uid, "uid");
    final SecurityDocument doc = _securityMaster.get(uid);
    return doc != null ? doc.getSecurity() : null;
  }

  @Override
  public Collection<Security> getSecurities(final IdentifierBundle securityKey) {
    Validate.notNull(securityKey, "securityKey");
    final SecuritySearchRequest req = new SecuritySearchRequest();
    req.setPagingRequest(PagingRequest.ALL);
    req.setIdentifiers(securityKey);
    return _securityMaster.search(req).getSecurities();
  }

  @Override
  public Security getSecurity(final IdentifierBundle securityKey) {
    Validate.notNull(securityKey, "securityKey");
    final Collection<Security> securities = getSecurities(securityKey);
    return securities.isEmpty() ? null : securities.iterator().next();
  }

}
