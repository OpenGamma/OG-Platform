/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * Market data provider that sources data from historical time-series.
 */
public abstract class AbstractHistoricalMarketDataProvider extends AbstractMarketDataProvider implements MarketDataAvailabilityProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractHistoricalMarketDataProvider.class);

  private final HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  private final String _timeSeriesResolverKey;
  private final MarketDataPermissionProvider _permissionProvider;

  /**
   * Creates a new market data provider.
   *
   * @param historicalTimeSeriesSource  the underlying source of historical data, not null
   * @param securitySource  the source of securities, not null
   * @param timeSeriesResolverKey  the source resolver key, or null to use the source default
   */
  public AbstractHistoricalMarketDataProvider(final HistoricalTimeSeriesSource historicalTimeSeriesSource,
      final SecuritySource securitySource,
      final String timeSeriesResolverKey) {
    ArgumentChecker.notNull(historicalTimeSeriesSource, "historicalTimeSeriesSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _historicalTimeSeriesSource = historicalTimeSeriesSource;
    _timeSeriesResolverKey = timeSeriesResolverKey;
    _permissionProvider = new PermissiveMarketDataPermissionProvider();
  }

  public AbstractHistoricalMarketDataProvider(final HistoricalTimeSeriesSource historicalTimeSeriesSource, final SecuritySource securitySource) {
    this(historicalTimeSeriesSource, securitySource, null);
  }

  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    s_logger.debug("Added subscriptions to {}", valueSpecification);
    subscriptionSucceeded(valueSpecification);
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    s_logger.debug("Added subscriptions to {}", valueSpecifications);
    subscriptionSucceeded(valueSpecifications);
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    s_logger.debug("Removed subscription from {}", valueSpecification);
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    s_logger.debug("Removed subscriptions from {}", valueSpecifications);
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
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof HistoricalMarketDataSpecification)) {
      return false;
    }
    final HistoricalMarketDataSpecification historicalSpec = (HistoricalMarketDataSpecification) marketDataSpec;
    return ObjectUtils.equals(_timeSeriesResolverKey, historicalSpec.getTimeSeriesResolverKey());
  }

  @Override
  public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    final ExternalIdBundle identifiers;
    if (target instanceof ExternalBundleIdentifiable) {
      identifiers = ((ExternalBundleIdentifiable) target).getExternalIdBundle();
    } else if (target instanceof ExternalIdentifiable) {
      identifiers = ((ExternalIdentifiable) target).getExternalId().toBundle();
    } else {
      return null;
    }
    final HistoricalTimeSeries hts = _historicalTimeSeriesSource.getHistoricalTimeSeries(desiredValue.getValueName(), identifiers, null, _timeSeriesResolverKey, null, true, null, true, 0);
    if (hts == null) {
      if (s_logger.isDebugEnabled() && desiredValue.getValueName().equals(MarketDataRequirementNames.MARKET_VALUE)) {
        s_logger.debug("Missing market data {}", desiredValue);
      }
      return null;
    } else {
      // [PLAT-3044] Using hts.getUniqueId means the consuming functions can't find the value correctly
      // TODO: the code in develop worked with the R examples, this code does not because of the HTS UniqueId issue
      return new ValueSpecification(desiredValue.getValueName(), ComputationTargetSpecification.of(hts.getUniqueId()), ValueProperties.with(ValuePropertyNames.FUNCTION, getSyntheticFunctionName())
          .get());
    }
  }

  protected HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  public String getTimeSeriesResolverKey() {
    return _timeSeriesResolverKey;
  }

  /**
   * The function name used in value specifications describing items sourced by this provider. This is only used for diagnostics when browsing the dependency graph.
   *
   * @return the function name
   */
  protected String getSyntheticFunctionName() {
    return getClass().getSimpleName();
  }

}
