/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.util.ArgumentChecker;

/**
 * Permission provider for live market data which delegates to a {@link LiveDataEntitlementChecker}.
 */
public class LiveMarketDataPermissionProvider implements MarketDataPermissionProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(LiveMarketDataPermissionProvider.class);

  private final LiveDataEntitlementChecker _entitlementChecker;

  public LiveMarketDataPermissionProvider(final LiveDataEntitlementChecker entitlementChecker) {
    ArgumentChecker.notNull(entitlementChecker, "entitlementChecker");
    _entitlementChecker = entitlementChecker;
  }

  //-------------------------------------------------------------------------
  @Override
  public Set<ValueSpecification> checkMarketDataPermissions(final UserPrincipal user, final Set<ValueSpecification> specifications) {
    s_logger.info("Checking that {} is entitled to computation results", user);
    final Map<LiveDataSpecification, Collection<ValueSpecification>> requiredLiveData = getRequiredLiveDataSpecifications(specifications);
    Map<LiveDataSpecification, Boolean> entitlements;
    try {
      entitlements = _entitlementChecker.isEntitled(user, requiredLiveData.keySet());
    } catch (final Exception e) {
      // 2013-02-04 Andrew -- The message below said this was failing open, but it's returning the full set of specifications which is no access. I kept
      // this behaviour but changed the logging message.
      s_logger.warn("Failed to perform entitlement checking. Assuming no access to data.", e);
      return Sets.newHashSet();  //specifications;
    }
    final Set<ValueSpecification> failures = Sets.newHashSet();
    for (final Map.Entry<LiveDataSpecification, Boolean> entry : entitlements.entrySet()) {
      if (!entry.getValue()) {
        failures.addAll(requiredLiveData.get(entry.getKey()));
      }
    }
    if (!failures.isEmpty()) {
      s_logger.warn("User {} does not have permission to access {} out of {} market data values",
                    user, failures.size(), specifications.size());
      s_logger.info("User {} does not have permission to access {}", user, failures);
    }
    return failures;
  }

  private Map<LiveDataSpecification, Collection<ValueSpecification>> getRequiredLiveDataSpecifications(final Set<ValueSpecification> specifications) {
    final Map<LiveDataSpecification, Collection<ValueSpecification>> returnValue = Maps.newHashMapWithExpectedSize(specifications.size());
    for (final ValueSpecification specification : specifications) {
      final LiveDataSpecification liveDataSpec = LiveMarketDataAvailabilityProvider.getLiveDataSpecification(specification);
      Collection<ValueSpecification> specs = returnValue.get(liveDataSpec);
      if (specs == null) {
        specs = new ArrayList<ValueSpecification>();
        returnValue.put(liveDataSpec, specs);
      }
      specs.add(specification);
    }
    return returnValue;
  }

}
