/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import net.sf.ehcache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.Request;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.AbstractBloombergStaticDataProvider;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Checks that the user has entitlement to access Bloomberg.
 * <p>
 * To understand what's going on this class, read Bloomberg Server API 3.0 Developer Guide, Chapter 7.
 */
public class BloombergEntitlementChecker extends AbstractBloombergStaticDataProvider implements LiveDataEntitlementChecker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergEntitlementChecker.class);
  /**
   * The length of half a day in seconds.
   */
  private static final long HALF_A_DAY_IN_SECONDS = 12 * 60 * 60;

  /**
   * The Bloomberg reference data provider.
   */
  private final ReferenceDataProvider _refDataProvider;
  /**
   * Cache: UserPrincipal -> Identity
   */
  private final Cache _userIdentityCache;
  /**
   * Cache: DistributionSpecification -> com.bloomberglp.blpapi.Element (containing Bloomberg Entitlement IDs)
   */
  private final Cache _eidCache;
  /**
   * The distribution resolver.
   */
  private final DistributionSpecificationResolver _resolver;

  /**
   * Creates an instance.
   * 
   * @param bloombergConnector the Bloomberg connector, not null
   * @param referenceDataProvider the reference data provider, not null
   * @param resolver the resolver, not null
   */
  public BloombergEntitlementChecker(BloombergConnector bloombergConnector, ReferenceDataProvider referenceDataProvider, DistributionSpecificationResolver resolver) {
    super(bloombergConnector, BloombergConstants.AUTH_SVC_NAME);
    ArgumentChecker.notNull(referenceDataProvider, "referenceDataProvider");
    ArgumentChecker.notNull(resolver, "resolver");

    _refDataProvider = referenceDataProvider;
    _resolver = resolver;

    // Cache will contain max 100 entries, each of which will expire in 12 hours  
    _userIdentityCache = new Cache("Bloomberg user identity cache", 100, false, false, HALF_A_DAY_IN_SECONDS, HALF_A_DAY_IN_SECONDS);
    _userIdentityCache.initialise();

    // Cache will contain max 100 entries, each of which will expire in 12 hours
    _eidCache = new Cache("Bloomberg EID cache", 100, false, false, HALF_A_DAY_IN_SECONDS, HALF_A_DAY_IN_SECONDS);
    _eidCache.initialise();
  }

  @Override
  protected Logger getLogger() {
    return s_logger;
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications) {
    Map<LiveDataSpecification, Boolean> returnValue = new HashMap<>();
    for (LiveDataSpecification spec : requestedSpecifications) {
      boolean entitled = isEntitled(user, spec);
      returnValue.put(spec, entitled);
    }
    return returnValue;
  }

  @Override
  public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
    DistributionSpecification distributionSpecification = _resolver.resolve(requestedSpecification);
    return isEntitled(user, distributionSpecification);
  }

  //-------------------------------------------------------------------------
  public boolean isEntitled(UserPrincipal user, DistributionSpecification distributionSpec) {
    Identity userIdentity = getUserIdentity(user);
    if (userIdentity == null) {
      return false;
    }

    Set<Integer> neededEntitlements = getEids(distributionSpec);
    if (neededEntitlements == null || neededEntitlements.isEmpty()) {
      return true;
    }

    List<Integer> failedEntitlements = new ArrayList<Integer>();
    boolean isEntitled = userIdentity.hasEntitlements(Ints.toArray(neededEntitlements), getService(), failedEntitlements);
    if (!failedEntitlements.isEmpty()) {
      s_logger.warn("user: {} is missing entitlements: {}", user, failedEntitlements);
    }
    return isEntitled;
  }

  private Identity getUserIdentity(UserPrincipal user) {
    net.sf.ehcache.Element cachedUserIdentity = _userIdentityCache.get(user);
    if (cachedUserIdentity == null) {
      Request authorizationRequest = getService().createAuthorizationRequest();

      Integer uuid;
      try {
        uuid = Integer.parseInt(user.getUserName());
      } catch (NumberFormatException e) {
        s_logger.info("Bloomberg user IDs are integers - so " + user.getUserName() + " cannot be entitled to anything");
        return null;
      }

      authorizationRequest.set("uuid", uuid);
      authorizationRequest.set("ipAddress", user.getIpAddress());
      Identity userIdentity = getSession().createIdentity();

      try {
        List<Element> resultElements = submitAuthorizationRequest(authorizationRequest, userIdentity).get();
        if (resultElements == null || resultElements.isEmpty()) {
          s_logger.info("Unable to get authorization info from Bloomberg for {}", user);
          return null;
        }

        boolean authorizedSuccessfully = false;
        for (Element resultElem : resultElements) {
          if (resultElem.name().equals(BloombergConstants.AUTHORIZATION_SUCCESS)) {
            cachedUserIdentity = new net.sf.ehcache.Element(user, userIdentity);
            _userIdentityCache.put(cachedUserIdentity);
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
      } catch (InterruptedException | ExecutionException ex) {
        s_logger.warn(String.format("Error authenticating user:%s", user), ex);
        return null;
      }
    }
    return (Identity) cachedUserIdentity.getObjectValue();
  }


  @SuppressWarnings("unchecked")
  private Set<Integer> getEids(DistributionSpecification distributionSpec) {
    net.sf.ehcache.Element cachedEids = _eidCache.get(distributionSpec);
    if (cachedEids == null) {
      String lookupKey = BloombergDomainIdentifierResolver.toBloombergKey(distributionSpec.getMarketDataId());
      Set<String> fields = Sets.newHashSet(BloombergConstants.FIELD_ID_BBG_UNIQUE, // TODO, this is necessary because otherwise the request would not get any real fields
          BloombergConstants.FIELD_EID_DATA);
      ReferenceDataProviderGetRequest rdRequest = ReferenceDataProviderGetRequest.createGet(Collections.singleton(lookupKey), fields, true);
      ReferenceDataProviderGetResult refData = _refDataProvider.getReferenceData(rdRequest);

      ReferenceData result = refData.getReferenceData(lookupKey);
      if (result.getErrors().size() > 0) {
        throw new OpenGammaRuntimeException("Error while obtaining entitlement information: " + lookupKey);
      }
      cachedEids = new net.sf.ehcache.Element(distributionSpec, new HashSet<>(result.getEidValues()));
      _eidCache.put(cachedEids);
    }

    return (Set<Integer>) cachedEids.getObjectValue();
  }

}
