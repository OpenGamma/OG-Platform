/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataTargetResolver;
import com.opengamma.engine.marketdata.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Market data provider that sources data from historical time-series.
 */
public abstract class AbstractHistoricalMarketDataProvider extends AbstractMarketDataProvider implements MarketDataAvailabilityProvider, MarketDataTargetResolver {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractHistoricalMarketDataProvider.class);
  
  private final HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  private final SecuritySource _securitySource;
  private final String _timeSeriesResolverKey;
  private final String _timeSeriesFieldResolverKey;
  private final MarketDataPermissionProvider _permissionProvider;
  private final ConcurrentMap<ValueRequirement, Pair<ExternalIdBundle, Integer>> _subscriptionIdBundleMap = new ConcurrentHashMap<ValueRequirement, Pair<ExternalIdBundle, Integer>>();

  /**
   * Creates a new market data provider.
   * 
   * @param historicalTimeSeriesSource  the underlying source of historical data, not null
   * @param securitySource  the source of securities, not null
   * @param timeSeriesResolverKey  the source resolver key, or null to use the source default
   * @param fieldResolverKey  the field name resolver resolution key, or null to use the resolver default
   */
  public AbstractHistoricalMarketDataProvider(final HistoricalTimeSeriesSource historicalTimeSeriesSource,
      final SecuritySource securitySource, final String timeSeriesResolverKey, final String fieldResolverKey) {
    ArgumentChecker.notNull(historicalTimeSeriesSource, "historicalTimeSeriesSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _historicalTimeSeriesSource = historicalTimeSeriesSource;
    _securitySource = securitySource;
    _timeSeriesResolverKey = timeSeriesResolverKey;
    _timeSeriesFieldResolverKey = fieldResolverKey;
    _permissionProvider = new PermissiveMarketDataPermissionProvider();
  }
  
  public AbstractHistoricalMarketDataProvider(final HistoricalTimeSeriesSource historicalTimeSeriesSource, final SecuritySource securitySource) {
    this(historicalTimeSeriesSource, securitySource, null, null);
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscribe(ValueRequirement valueRequirement) {
    subscribe(Collections.singleton(valueRequirement));
  }

  @Override
  public void subscribe(Set<ValueRequirement> valueRequirements) {
    synchronized (_subscriptionIdBundleMap) {
      for (ValueRequirement requirement : valueRequirements) {
        Pair<ExternalIdBundle, Integer> existing = _subscriptionIdBundleMap.get(requirement);
        if (existing != null) {
          _subscriptionIdBundleMap.put(requirement, Pair.of(existing.getFirst(), existing.getSecond() + 1));
        } else {
          _subscriptionIdBundleMap.put(requirement, Pair.of(lookupExternalIdBundle(requirement), 1));
        }
      }
    }
    s_logger.debug("Added subscriptions to {}", valueRequirements);
    subscriptionSucceeded(valueRequirements);
  }
  
  @Override
  public void unsubscribe(ValueRequirement valueRequirement) {
    unsubscribe(Collections.singleton(valueRequirement));
  }

  @Override
  public void unsubscribe(Set<ValueRequirement> valueRequirements) {
    synchronized (_subscriptionIdBundleMap) {
      for (ValueRequirement requirement : valueRequirements) {
        Pair<ExternalIdBundle, Integer> existing = _subscriptionIdBundleMap.get(requirement);
        if (existing == null) {
          s_logger.warn("Attempted to unsubscribe from {} for which no subscription exists", requirement);
          continue;
        }
        if (existing.getSecond() == 1) {
          _subscriptionIdBundleMap.remove(requirement);
        } else {
          _subscriptionIdBundleMap.put(requirement, Pair.of(existing.getFirst(), existing.getSecond() - 1));
        }
      }
    }
    s_logger.debug("Removed subscriptions from {}", valueRequirements);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return this;
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionProvider;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof HistoricalMarketDataSpecification)) {
      return false;
    }
    HistoricalMarketDataSpecification historicalSpec = (HistoricalMarketDataSpecification) marketDataSpec;
    return ObjectUtils.equals(getTimeSeriesResolverKey(), historicalSpec.getTimeSeriesResolverKey())
        && ObjectUtils.equals(getTimeSeriesFieldResolverKey(), historicalSpec.getTimeSeriesFieldResolverKey());
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailability getAvailability(final ValueRequirement requirement) {
    final ExternalIdBundle idBundle = getExternalIdBundle(requirement);
    if (idBundle == null) {
      return MarketDataAvailability.NOT_AVAILABLE;
    }
    final HistoricalTimeSeries hts = getTimeSeriesSource().getHistoricalTimeSeries(requirement.getValueName(), idBundle, null, getTimeSeriesResolverKey());
    if (hts == null) {
      if (s_logger.isDebugEnabled() && requirement.getValueName().equals(MarketDataRequirementNames.MARKET_VALUE)) {
        s_logger.debug("Missing market data {}", requirement);
      }
      return MarketDataAvailability.NOT_AVAILABLE;
    } else {
      return MarketDataAvailability.AVAILABLE;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ExternalIdBundle getExternalIdBundle(final ValueRequirement requirement) {
    Pair<ExternalIdBundle, Integer> existingSubscription = _subscriptionIdBundleMap.get(requirement);
    if (existingSubscription != null) {
      return existingSubscription.getFirst();
    }
    return lookupExternalIdBundle(requirement);
  }

  private ExternalIdBundle lookupExternalIdBundle(final ValueRequirement requirement) {
    switch (requirement.getTargetSpecification().getType()) {
      case PRIMITIVE:
        return ExternalIdBundle.of(requirement.getTargetSpecification().getIdentifier());
      case SECURITY:
        final Security security;
        try {
          security = getSecuritySource().getSecurity(requirement.getTargetSpecification().getUniqueId());
        } catch (DataNotFoundException ex) {
          return null;
        }
        return security.getExternalIdBundle();
      default:
        return null;
    }
  }
  
  //-------------------------------------------------------------------------
  protected HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }
  
  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }
  
  public String getTimeSeriesResolverKey() {
    return _timeSeriesResolverKey;
  }
  
  public String getTimeSeriesFieldResolverKey() {
    return _timeSeriesFieldResolverKey;
  }

}
