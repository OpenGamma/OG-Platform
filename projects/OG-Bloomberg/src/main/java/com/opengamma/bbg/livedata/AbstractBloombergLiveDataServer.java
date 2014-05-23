/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataError;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.normalization.StandardRuleResolver;
import com.opengamma.livedata.permission.PermissionUtils;
import com.opengamma.livedata.resolver.DefaultDistributionSpecificationResolver;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.resolver.EHCachingDistributionSpecificationResolver;
import com.opengamma.livedata.resolver.IdResolver;
import com.opengamma.livedata.resolver.NormalizationRuleResolver;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;


/**
 * Allows common functionality to be shared between the live and recorded Bloomberg data servers
 */
public abstract class AbstractBloombergLiveDataServer extends StandardLiveDataServer {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractBloombergLiveDataServer.class);
  private static final String DEFAULT_BBG_SUB_PREFIX = "/buid/";
  
  private NormalizationRuleResolver _normalizationRules;  
  private IdResolver _idResolver;
  private DistributionSpecificationResolver _defaultDistributionSpecificationResolver;

  /**
   * Creates an instance.
   * 
   * @param cacheManager  the cache manager, not null
   */
  public AbstractBloombergLiveDataServer(CacheManager cacheManager) {
    super(cacheManager);
  }

  //-------------------------------------------------------------------------
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
  
  /**
   * 
   * @return the prefix to use when making the subscription, including slashes.
   * e.g. "/buid/"
   */
  protected String getBloombergSubscriptionPathPrefix() {
    return DEFAULT_BBG_SUB_PREFIX;
  }
  
  
  @Override
  public Map<String, FudgeMsg> doSnapshot(Collection<String> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "Unique IDs");
    if (uniqueIds.isEmpty()) {
      return Collections.emptyMap();
    }
    
    Set<String> buids = Sets.newHashSetWithExpectedSize(uniqueIds.size());
    for (String uniqueId : uniqueIds) {
      String buid = getBloombergSubscriptionPathPrefix() + uniqueId;
      buids.add(buid);
    }
    
    // caching ref data provider must not be used here
    ReferenceDataProviderGetRequest refDataRequest = ReferenceDataProviderGetRequest.createGet(
        buids, BloombergDataUtils.STANDARD_FIELDS_SET, false);
    Map<String, FudgeMsg> snapshotValues = queryRefData(refDataRequest);
    Map<String, FudgeMsg> returnValue = Maps.newHashMap();
    for (String buid : buids) {
      FudgeMsg fieldData = snapshotValues.get(buid);
      if (fieldData == null) {
        s_logger.error("Could not find result for {} in data snapshot, skipping", buid);
      } else {
        String securityUniqueId = buid.substring(getBloombergSubscriptionPathPrefix().length());
        returnValue.put(securityUniqueId, fieldData);
      }
    }
    return returnValue;
  }

  // broken out from AbstractReferenceDataProvider to handle errors differently
  private Map<String, FudgeMsg> queryRefData(ReferenceDataProviderGetRequest request) {
    Set<String> identifiers = ImmutableSet.copyOf(request.getIdentifiers()); // copy to avoid implementation bugs
    Set<String> fields = ImmutableSet.copyOf(request.getFields()); // copy to avoid implementation bugs
    ReferenceDataProviderGetResult result = getReferenceDataProvider().getReferenceData(request);
    // extract identifier to field-values
    Map<String, FudgeMsg> map = Maps.newHashMap();
    for (String identifier : identifiers) {
      ReferenceData data = result.getReferenceDataOrNull(identifier);
      if (data != null) {
        // filter results by error list
        if (data.getErrors().isEmpty()) {
          map.put(identifier, data.getFieldValues());
        } else {
          FudgeMsg fieldValues = handleRefDataError(data, fields);
          if (fieldValues != null) {
            map.put(identifier, fieldValues);
          }
        }
      }
    }
    return map;
  }

  // handle errors in reference data, notably permission denied
  protected FudgeMsg handleRefDataError(ReferenceData data, Set<String> fields) {
    if (data.isIdentifierError()) {
      // whole response is in error
      // entitlement errors are handled, other errors will look like missing data
      for (ReferenceDataError error : data.getErrors()) {
        if (error.isEntitlementError()) {
          String message = error.getMessage();
          if (message.startsWith("Security Entitlement Check Failed! ")) {
            message = message.substring("Security Entitlement Check Failed! ".length());
          }
          // need to communicate error message via the API available
          // using LIVE_DATA_PERMISSION_DENIED_FIELD is not ideal but best avaiable option
          // solution is a much larger rewrite
          MutableFudgeMsg values = OpenGammaFudgeContext.getInstance().newMessage();
          values.add(PermissionUtils.LIVE_DATA_PERMISSION_DENIED_FIELD, "Permission denied (Bloomberg): " + message);
          // only return first entitlement error
          return values;
        }
      }
      // null used to remove result entirely, effectively indicating missing data
      return null;
    }
    // some fields are in error, so ensure they are not present
    MutableFudgeMsg values = OpenGammaFudgeContext.getInstance().newMessage(data.getFieldValues());
    for (String field : fields) {
      if (data.isError(field)) {
        values.remove(field);
      }
    }
    return values;
  }

  public synchronized NormalizationRuleResolver getNormalizationRules() {
    if (_normalizationRules == null) {
      _normalizationRules = new StandardRuleResolver(BloombergDataUtils.getDefaultNormalizationRules(getReferenceDataProvider(), getCacheManager(), getUniqueIdDomain()));
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
      BloombergJmsTopicNameResolver topicResolver = new BloombergJmsTopicNameResolver(getReferenceDataProvider());
      DefaultDistributionSpecificationResolver distributionSpecResolver = new DefaultDistributionSpecificationResolver(getIdResolver(), getNormalizationRules(), topicResolver);
      return new EHCachingDistributionSpecificationResolver(distributionSpecResolver, getCacheManager(), "BBG");
    }
    return _defaultDistributionSpecificationResolver;
  }

}
