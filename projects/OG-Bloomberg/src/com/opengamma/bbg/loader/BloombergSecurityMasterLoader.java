/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Loads security from bloomberg and populates the security master
 */
public class BloombergSecurityMasterLoader implements SecurityLoader {
  
  private final SecurityMaster _secMaster;
  
  private final BloombergBulkSecurityLoader _bbgBulkSecLoader;
  
  /**
   * Creates BloombergSecurityMasterLoader
   * 
   * @param secMaster the security master, not-null
   * @param bbgBulkSecLoader bloomberg security loader, not-null
   */
  public BloombergSecurityMasterLoader(SecurityMaster secMaster, BloombergBulkSecurityLoader bbgBulkSecLoader) {
    ArgumentChecker.notNull(secMaster, "HibernateSecMaster");
    ArgumentChecker.notNull(bbgBulkSecLoader, "BloombergBulkSecurityLoader");
    _secMaster = secMaster;
    _bbgBulkSecLoader = bbgBulkSecLoader;
  }

  @Override
  public Map<ExternalIdBundle, UniqueId> loadSecurity(final Collection<ExternalIdBundle> identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    
    Map<ExternalIdBundle, UniqueId> result = new HashMap<ExternalIdBundle, UniqueId>();
    
    Map<ExternalIdBundle, ManageableSecurity> securities = _bbgBulkSecLoader.loadSecurity(identifiers);
    
    UnderlyingIdentifierCollector identifierCollector = new UnderlyingIdentifierCollector();
        
    for (Entry<ExternalIdBundle, ManageableSecurity> entry : securities.entrySet()) {
      ExternalIdBundle requestBundle = entry.getKey();
      ManageableSecurity security = entry.getValue();
      
      //getUnderlying identifiers
      if (security instanceof FinancialSecurity) {
        FinancialSecurity financialSecurity = (FinancialSecurity) security;
        financialSecurity.accept(identifierCollector.getFinancialSecurityVisitor());
      }
      
      UniqueId uid = null;
      if (security != null) {
        ExternalId buid = security.getExternalIdBundle().getExternalId(SecurityUtils.BLOOMBERG_BUID);
        uid = getUid(buid);
        if (uid == null) {
          uid = addSecurity(security);
        } else {
          uid = updateSecurity(security, uid);
        }
      }
      result.put(requestBundle, uid);
    }
    
    addOrUpdateUnderlying(identifierCollector.getUnderlyings());
    
    return result;
  }

  private void addOrUpdateUnderlying(Set<ExternalIdBundle> underlyingIdentifiers) {
    Map<ExternalIdBundle, ManageableSecurity> securities = _bbgBulkSecLoader.loadSecurity(underlyingIdentifiers);
    for (Entry<ExternalIdBundle, ManageableSecurity> entry : securities.entrySet()) {
      ManageableSecurity security = entry.getValue();
      if (security != null) {
        ExternalId buid = security.getExternalIdBundle().getExternalId(SecurityUtils.BLOOMBERG_BUID);
        UniqueId uid = getUid(buid);
        if (uid == null) {
          uid = addSecurity(security);
        } else {
          updateSecurity(security, uid);
        }
      }
    }
  }

  private UniqueId updateSecurity(ManageableSecurity security, UniqueId uid) {
    final SecurityDocument document = new SecurityDocument();
    document.setSecurity(security);
    document.setUniqueId(uid);
    getSecurityMaster().update(document);
    return document.getUniqueId();
  }

  private UniqueId addSecurity(ManageableSecurity security) {
    final SecurityDocument document = new SecurityDocument();
    document.setSecurity(security);
    SecurityDocument addedSecurity = getSecurityMaster().add(document);
    return addedSecurity.getUniqueId();
  }

  private UniqueId getUid(ExternalId identifier) {
    SecuritySearchRequest request = new SecuritySearchRequest(ExternalIdBundle.of(identifier));
    SecuritySearchResult result = getSecurityMaster().search(request);
    UniqueId uid = null;
    if (!result.getDocuments().isEmpty()) {
      SecurityDocument document = result.getFirstDocument();
      uid = document.getUniqueId();
    }
    return uid;
  }

  @Override
  public SecurityMaster getSecurityMaster() {
    return _secMaster;
  }

}
