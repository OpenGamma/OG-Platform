/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.bbg.util.ReferenceDataProviderUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.resolver.AbstractResolver;
import com.opengamma.livedata.resolver.IdResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * Resolves Bloomberg IDs. Accepts IDs of types:
 * <ul>
 * <li>{@link SecurityUtils#BLOOMBERG_BUID} 
 * <li>{@link SecurityUtils#BLOOMBERG_TICKER}
 * <li>{@link SecurityUtils#BLOOMBERG_TCM}
 * <li>{@link SecurityUtils#ISIN}
 * <li>{@link SecurityUtils#CUSIP}
 * </ul>
 * Returns an ID collection with {@link SecurityUtils#BLOOMBERG_BUID}.
 * All other IDs are stripped out.  
 *
 */
public class BloombergIdResolver extends AbstractResolver<ExternalIdBundle, ExternalId> implements IdResolver {
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergIdResolver.class);
  private final ReferenceDataProvider _referenceDataProvider;
  
  public BloombergIdResolver(ReferenceDataProvider referenceDataProvider) {
    ArgumentChecker.notNull(referenceDataProvider, "Reference Data Provider");
    _referenceDataProvider = referenceDataProvider;
  }

  /**
   * @return the referenceDataProvider
   */
  public ReferenceDataProvider getReferenceDataProvider() {
    return _referenceDataProvider;
  }

  @Override
  public Map<ExternalIdBundle, ExternalId> resolve(Collection<ExternalIdBundle> bundles) {
    Map<ExternalIdBundle, ExternalId> result = new HashMap<ExternalIdBundle, ExternalId>();
    
    Set<String> bbgKeys = new HashSet<String>();
    Map<String, Collection<ExternalIdBundle>> bbgKey2Bundle = new HashMap<String, Collection<ExternalIdBundle>>();
    
    for (ExternalIdBundle bundle : bundles) {
      String bbgUniqueId = null;
      ExternalId preferredIdentifier = BloombergDomainIdentifierResolver.resolvePreferredIdentifier(bundle);
      if (preferredIdentifier != null) {
        if (!preferredIdentifier.getScheme().equals(SecurityUtils.BLOOMBERG_BUID)) {
          
          String bloombergKey = BloombergDomainIdentifierResolver.toBloombergKey(preferredIdentifier);
          bbgKeys.add(bloombergKey);
          
          Collection<ExternalIdBundle> bundlesForKey = bbgKey2Bundle.get(bloombergKey);
          if (bundlesForKey == null) {
            bundlesForKey = new ArrayList<ExternalIdBundle>();
            bbgKey2Bundle.put(bloombergKey, bundlesForKey);
          }
          bundlesForKey.add(bundle);
          
        } else {
          bbgUniqueId = preferredIdentifier.getValue();
          result.put(bundle, SecurityUtils.bloombergBuidSecurityId(bbgUniqueId));
        }
      } else {
        s_logger.warn("Unable to identify any Bloomberg compatible identifier for {}", bundle);
        result.put(bundle, null);
      }
    }
    
    Map<String, String> bbgKey2BbgUniqueId = ReferenceDataProviderUtils.getBloombergUniqueIDs(bbgKeys, getReferenceDataProvider());
    
    for (String bbgKey : bbgKey2Bundle.keySet()) {
      String bbgUniqueId = bbgKey2BbgUniqueId.get(bbgKey);
       
      ExternalId identifier;
      if (bbgUniqueId == null) {
        identifier = null;
      } else {
        identifier = SecurityUtils.bloombergBuidSecurityId(bbgUniqueId);
      }
      
      for (ExternalIdBundle bundle : bbgKey2Bundle.get(bbgKey)) {
        if (identifier == null) {
          s_logger.warn("Unable to get Bloomberg unique ID for {}", bundle);
        }
        
        result.put(bundle, identifier);          
      }
    }
    
    return result;
  }

}
