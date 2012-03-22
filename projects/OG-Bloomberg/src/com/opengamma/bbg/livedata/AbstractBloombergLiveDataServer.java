/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeMsg;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataResult;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.normalization.StandardRuleResolver;
import com.opengamma.livedata.resolver.DefaultDistributionSpecificationResolver;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.resolver.EHCachingDistributionSpecificationResolver;
import com.opengamma.livedata.resolver.IdResolver;
import com.opengamma.livedata.resolver.NormalizationRuleResolver;
import com.opengamma.livedata.server.AbstractLiveDataServer;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;


/**
 * Allows common functionality to be shared between the live and recorded Bloomberg data servers
 */
public abstract class AbstractBloombergLiveDataServer extends AbstractLiveDataServer {

  private NormalizationRuleResolver _normalizationRules;
  private IdResolver _idResolver;
  private DistributionSpecificationResolver _defaultDistributionSpecificationResolver;
  
  /**
   * Gets a reference data provider for use when cached results are acceptable, and perhaps preferred.
   * 
   * @return  a reference data provider, which might be caching
   */
  protected abstract ReferenceDataProvider getCachingReferenceDataProvider();
  
  /**
   * Gets a reference data provider for use when cached results are not acceptable.
   * 
   * @return  a non-caching reference data provider
   */
  protected abstract ReferenceDataProvider getUnderlyingReferenceDataProvider();
  
  @Override
  protected ExternalScheme getUniqueIdDomain() {
    return SecurityUtils.BLOOMBERG_BUID;
  }
  
  @Override
  protected boolean snapshotOnSubscriptionStartRequired(Subscription subscription) {
    // As per Kirk, it is possible that you don't get all fields initially.
    // Should we optimize this by asset type?
    return true;
  }
  
  @Override
  public Map<String, FudgeMsg> doSnapshot(Collection<String> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "Unique IDs");
    if (uniqueIds.isEmpty()) {
      return Collections.emptyMap();
    }
    
    Set<String> buids = new HashSet<String>();    
    for (String uniqueId : uniqueIds) {
      String buid = "/buid/" + uniqueId;
      buids.add(buid);
    }
    
    // caching ref data provider must not be used here
    ReferenceDataResult referenceData = getUnderlyingReferenceDataProvider().getFields(buids, BloombergDataUtils.STANDARD_FIELDS_SET);
    if (referenceData == null) {
      throw new OpenGammaRuntimeException("Could not obtain reference data for " + buids);      
    }
    
    Map<String, FudgeMsg> returnValue = new HashMap<String, FudgeMsg>();
    for (String buid : buids) {
      PerSecurityReferenceDataResult result = referenceData.getResult(buid);
      if (result == null) {
        throw new OpenGammaRuntimeException("Result for " + buid + " was not found");
      }
      
      String securityUniqueId = buid.substring("/buid/".length());
      FudgeMsg fieldData = result.getFieldData();
      if (fieldData == null) {
        throw new OpenGammaRuntimeException("Reference data provider " + getUnderlyingReferenceDataProvider() + " returned null fieldData for " + buid);
      } 
      returnValue.put(securityUniqueId, fieldData);
    }
    
    return returnValue;
  }
  
  public synchronized NormalizationRuleResolver getNormalizationRules() {
    if (_normalizationRules == null) {
      _normalizationRules = new StandardRuleResolver(BloombergDataUtils.getDefaultNormalizationRules(getCachingReferenceDataProvider(), EHCacheUtils.createCacheManager()));
    }
    return _normalizationRules;
  }

  public synchronized IdResolver getIdResolver() {
    if (_idResolver == null) {
      _idResolver = new BloombergIdResolver(getCachingReferenceDataProvider());
    }
    return _idResolver;
  }

  public synchronized DistributionSpecificationResolver getDefaultDistributionSpecificationResolver() {
    if (_defaultDistributionSpecificationResolver == null) {
      CacheManager cacheManager = EHCacheUtils.createCacheManager();
      DefaultDistributionSpecificationResolver distributionSpecResolver = new DefaultDistributionSpecificationResolver(getIdResolver(), getNormalizationRules(), new BloombergJmsTopicNameResolver(
          getCachingReferenceDataProvider()));
      return new EHCachingDistributionSpecificationResolver(distributionSpecResolver, cacheManager, "BBG");
    }
    return _defaultDistributionSpecificationResolver;
  }

}
