/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.financial.security.master.SecurityDocument;
import com.opengamma.financial.security.master.SecurityMaster;
import com.opengamma.financial.security.master.SecuritySearchRequest;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@code SecuritySource} implemented using an underlying {@code SecurityMaster}.
 * <p>
 * The {@link SecuritySource} interface provides securities to the engine via a narrow API.
 * This class provides the source on top of a standard {@link SecurityMaster}.
 */
public class MasterSecuritySource extends SecurityMasterAdapter implements SecuritySource {

  /**
   * Creates an instance with an underlying security master.
   * @param securityMaster  the security master, not null
   */
  public MasterSecuritySource(final SecurityMaster securityMaster) {
    super(securityMaster);
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    try {
      final SecurityDocument doc = get(uid);
      return doc.getSecurity();
    } catch (DataNotFoundException e) {
      return null;
    }
  }

  @Override
  public Collection<Security> getSecurities(final IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final SecuritySearchRequest req = new SecuritySearchRequest();
    req.setIdentityKey(securityKey);
    req.setFullDetail(true);
    final Collection<SecurityDocument> documents = search(req).getDocuments();
    if (documents == null) {
      return Collections.emptyList();
    }
    final Collection<Security> result = new ArrayList<Security>(documents.size());
    for (SecurityDocument document : documents) {
      result.add(document.getSecurity());
    }
    return result;
  }

  @Override
  public Security getSecurity(final IdentifierBundle securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    final Collection<Security> securities = getSecurities(securityKey);
    return securities.isEmpty() ? null : securities.iterator().next();
  }

}
