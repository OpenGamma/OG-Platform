/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeMsg;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
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
   * Gets the reference data provider.
   * 
   * @return the reference data provider
   */
  protected abstract ReferenceDataProvider getReferenceDataProvider();
  @Override
  protected ExternalScheme getUniqueIdDomain() {
    return ExternalSchemes.BLOOMBERG_BUID;
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
    Map<String, FudgeMsg> snapshotValues = getReferenceDataProvider().getReferenceDataIgnoreCache(buids, BloombergDataUtils.STANDARD_FIELDS_SET);
    Map<String, FudgeMsg> returnValue = Maps.newHashMap();
    for (String buid : buids) {
      FudgeMsg fieldData = snapshotValues.get(buid);
      if (fieldData == null) {
        throw new OpenGammaRuntimeException("Result for " + buid + " was not found");
      }
      String securityUniqueId = buid.substring("/buid/".length());
      returnValue.put(securityUniqueId, fieldData);
    }
    return returnValue;
  }

  public synchronized NormalizationRuleResolver getNormalizationRules() {
    if (_normalizationRules == null) {
      _normalizationRules = new StandardRuleResolver(BloombergDataUtils.getDefaultNormalizationRules(getReferenceDataProvider(), EHCacheUtils.createCacheManager()));
    }
    return _normalizationRules;
  }

  public synchronized IdResolver getIdResolver() {
    if (_idResolver == null) {
      _idResolver = new BloombergIdResolver(getReferenceDataProvider());
    }
    return _idResolver;
  }

  public synchronized DistributionSpecificationResolver getDefaultDistributionSpecificationResolver() {
    if (_defaultDistributionSpecificationResolver == null) {
      CacheManager cacheManager = EHCacheUtils.createCacheManager();
      DefaultDistributionSpecificationResolver distributionSpecResolver = new DefaultDistributionSpecificationResolver(
          getIdResolver(), getNormalizationRules(), new BloombergJmsTopicNameResolver(getReferenceDataProvider()));
      return new EHCachingDistributionSpecificationResolver(distributionSpecResolver, cacheManager, "BBG");
    }
    return _defaultDistributionSpecificationResolver;
  }

}
