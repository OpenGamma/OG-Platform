/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import net.sf.ehcache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.SessionOptions;
import com.bloomberglp.blpapi.UserHandle;
import com.google.common.collect.Sets;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.BloombergDataProvider;
import com.opengamma.bbg.BloombergReferenceDataProvider;
import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.bbg.ReferenceDataResult;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * To understand what's going on this class, read Bloomberg Server API 3.0 Developer Guide, Chapter 7.
 * 
 */
public class BloombergEntitlementChecker extends BloombergDataProvider implements LiveDataEntitlementChecker {
  
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergEntitlementChecker.class);
  
  private final BloombergReferenceDataProvider _refDataProvider;
  
  private static final long HALF_A_DAY_IN_SECONDS = 12 * 60 * 60;
  
  /** Bloomberg //blp/apiauth service */
  private Service _apiAuthSvc;
  
  /** 
   * UserPrincipal -> UserHandle 
   */
  private final Cache _userHandleCache;
  
  /** 
   * DistributionSpecification -> com.bloomberglp.blpapi.Element (containing Bloomberg Entitlement IDs)
   */
  private final Cache _eidCache;
  
  private final DistributionSpecificationResolver _resolver;
  
  
  public BloombergEntitlementChecker(SessionOptions sessionOptions,
      BloombergReferenceDataProvider refDataProvider,
      DistributionSpecificationResolver resolver) {
    super(sessionOptions);
    
    ArgumentChecker.notNull(refDataProvider, "Reference data provider");
    _refDataProvider = refDataProvider;
    
    // Cache will contain max 100 entries, each of which will expire in 12 hours  
    _userHandleCache = new Cache("Bloomberg user handle cache", 100, false, false, HALF_A_DAY_IN_SECONDS, HALF_A_DAY_IN_SECONDS);
    _userHandleCache.initialise();
    
    // Cache will contain max 100 entries, each of which will expire in 12 hours
    _eidCache = new Cache("Bloomberg EID cache", 100, false, false, HALF_A_DAY_IN_SECONDS, HALF_A_DAY_IN_SECONDS);
    _eidCache.initialise();
    
    ArgumentChecker.notNull(resolver, "Distribution spec resolver");
    _resolver = resolver;
  }
  
  @Override
  protected void openServices() {
    Service authService = openService(BloombergConstants.AUTH_SVC_NAME);
    _apiAuthSvc = authService;
  }
  
  @Override
  protected Logger getLogger() {
    return s_logger;
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
    DistributionSpecification distributionSpecification = _resolver.resolve(requestedSpecification); 
    return isEntitled(user, distributionSpecification);
  }
  
  @Override
  public Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications) {
    Map<LiveDataSpecification, Boolean> returnValue = new HashMap<LiveDataSpecification, Boolean>();
    for (LiveDataSpecification spec : requestedSpecifications) {
      boolean entitled = isEntitled(user,  spec);
      returnValue.put(spec, entitled);                  
    }
    return returnValue;
  }
  
  // --------------------------------------------------------------------------
  
  public boolean isEntitled(UserPrincipal user, DistributionSpecification distributionSpec) {
    UserHandle userHandle = getUserHandle(user);
    if (userHandle == null) {
      return false;
    }
    
    Element neededEntitlements = getEids(distributionSpec);
    if (neededEntitlements == null) {
      return true;
    }
    
    boolean isEntitled = userHandle.hasEntitlements(neededEntitlements, _apiAuthSvc);
    return isEntitled;
  }

  
  private Element getEids(DistributionSpecification distributionSpec) {
    net.sf.ehcache.Element cachedEids = _eidCache.get(distributionSpec);
    if (cachedEids == null) {
      
      String lookupKey = BloombergDomainIdentifierResolver.toBloombergKey(distributionSpec.getMarketDataId());
      ReferenceDataResult referenceData = _refDataProvider.getFields(Collections.singleton(lookupKey), 
          Sets.newHashSet(
              BloombergConstants.FIELD_ID_BBG_UNIQUE, // TODO, this is necessary because otherwise the request would not get any real fields
              BloombergConstants.FIELD_EID_DATA));
      
      PerSecurityReferenceDataResult result = referenceData.getResult(lookupKey);
      Element eids = result.getEidData();
      
      cachedEids = new net.sf.ehcache.Element(distributionSpec, eids);
      _eidCache.put(cachedEids);
    }

    Element neededEntitlements = (Element) cachedEids.getObjectValue();
    return neededEntitlements;
  }

  
  private UserHandle getUserHandle(UserPrincipal user) {
    net.sf.ehcache.Element cachedUserHandle = _userHandleCache.get(user);
    if (cachedUserHandle == null) {
      Request authorizationRequest = _apiAuthSvc.createAuthorizationRequest();
      
      Integer uuid;
      try {
        uuid = Integer.parseInt(user.getUserName());
      } catch (NumberFormatException e) {
        s_logger.info("Bloomberg user IDs are integers - so " + user.getUserName() + " cannot be entitled to anything");        
        return null;
      }
     
      authorizationRequest.set("uuid", uuid);
      authorizationRequest.set("ipAddress", user.getIpAddress());
      UserHandle userHandle = getSession().createUserHandle();
      
      CorrelationID cid = submitBloombergAuthorizationRequest(authorizationRequest, userHandle);
      BlockingQueue<Element> resultElements = _correlationIDElementMap.remove(cid);
      //clear correlation maps
      _correlationIDMap.remove(cid);
      
      if (resultElements == null || resultElements.isEmpty()) {
        s_logger.info("Unable to get authorization info from Bloomberg for {}", user);
        return null;
      }
      
      boolean authorizedSuccessfully = false;
      for (Element resultElem : resultElements) {
        
        if (resultElem.name().equals(BloombergConstants.AUTHORIZATION_SUCCESS)) {

          cachedUserHandle = new net.sf.ehcache.Element(user, userHandle);
          _userHandleCache.put(cachedUserHandle);

          authorizedSuccessfully = true;          

        } else if (resultElem.name().equals(BloombergConstants.AUTHORIZATION_FAILURE)) {
          
          Element reasonElem = resultElem.getElement(BloombergConstants.REASON);
          
          s_logger.info("Bloomberg authorization failed {}", reasonElem);
        
        } else {
          s_logger.info("Bloomberg authorization result {}", resultElem);
        }
      }
      
      if (!authorizedSuccessfully) {
        return null;        
      }
    }
    
    UserHandle userHandle = (UserHandle) cachedUserHandle.getObjectValue();
    return userHandle;
  }
  
  
}
