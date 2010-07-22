/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;
import java.util.HashSet;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * A base implementation of a combined SecurityMaster and SecuritySource.
 */
public abstract class AbstractSecurityMaster implements SecurityMaster, SecuritySource {

  // SecurityMaster

  @Override
  public SecurityDocument correct(SecurityDocument document) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public SecurityDocument get(UniqueIdentifier uid) {
    final Security security = getSecurity(uid);
    if (security == null) {
      return null;
    }
    return new SecurityDocument(security);
  }

  @Override
  public SecuritySearchResult search(SecuritySearchRequest request) {
    final SecuritySearchResult result = new SecuritySearchResult();
    final Collection<SecurityDocument> results = result.getDocuments();
    for (Security security : getSecurities(request.getIdentifiers())) {
      results.add(new SecurityDocument(security));
    }
    return result;
  }

  @Override
  public SecuritySearchHistoricResult searchHistoric(SecuritySearchHistoricRequest request) {
    throw new UnsupportedOperationException("not implemented");
  }

  // SecuritySource

  @Override
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    final Collection<Security> results = new HashSet<Security>();
    for (Identifier id : secKey) {
      final Security security = getSecurity(new IdentifierBundle(id));
      if (security != null) {
        results.add(security);
      }
    }
    return results;
  }

}
