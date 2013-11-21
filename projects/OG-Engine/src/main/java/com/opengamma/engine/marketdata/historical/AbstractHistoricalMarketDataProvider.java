/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjustment;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.availability.AbstractMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Market data provider that sources data from historical time-series.
 */
public abstract class AbstractHistoricalMarketDataProvider extends AbstractMarketDataProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractHistoricalMarketDataProvider.class);

  /**
   * The property put onto the value specifications created.
   */
  protected static final String NORMALIZATION_PROPERTY = "Normalization";

  private final HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  private final HistoricalTimeSeriesResolver _historicalTimeSeriesResolver;
  private final String _timeSeriesResolverKey;
  private final MarketDataPermissionProvider _permissionProvider;

  /**
   * Creates a new market data provider.
   * 
   * @param historicalTimeSeriesSource the underlying source of historical data, not null
   * @param historicalTimeSeriesResolver the resolver to identifier historical data, not null
   * @param timeSeriesResolverKey the source resolver key, or null to use the source default
   */
  public AbstractHistoricalMarketDataProvider(final HistoricalTimeSeriesSource historicalTimeSeriesSource, final HistoricalTimeSeriesResolver historicalTimeSeriesResolver,
      final String timeSeriesResolverKey) {
    ArgumentChecker.notNull(historicalTimeSeriesSource, "historicalTimeSeriesSource");
    ArgumentChecker.notNull(historicalTimeSeriesResolver, "historicalTimeSeriesResolver");
    _historicalTimeSeriesSource = historicalTimeSeriesSource;
    _historicalTimeSeriesResolver = historicalTimeSeriesResolver;
    _timeSeriesResolverKey = timeSeriesResolverKey;
    _permissionProvider = new PermissiveMarketDataPermissionProvider();
  }

  public AbstractHistoricalMarketDataProvider(final HistoricalTimeSeriesSource historicalTimeSeriesSource, final HistoricalTimeSeriesResolver historicalTimeSeriesResolver) {
    this(historicalTimeSeriesSource, historicalTimeSeriesResolver, null);
  }

  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    s_logger.debug("Added subscriptions to {}", valueSpecification);
    subscriptionSucceeded(valueSpecification);
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    s_logger.debug("Added subscriptions to {}", valueSpecifications);
    subscriptionsSucceeded(valueSpecifications);
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    s_logger.debug("Removed subscription from {}", valueSpecification);
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    s_logger.debug("Removed subscriptions from {}", valueSpecifications);
  }

  protected abstract LocalDate getHistoricalResolutionDate(final MarketDataSpecification marketDataSpec);

  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider(final MarketDataSpecification marketDataSpec) {
    final LocalDate date = getHistoricalResolutionDate(marketDataSpec);
    return new AbstractMarketDataAvailabilityProvider() {

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
        return getAvailability(targetSpec, identifier.toBundle(), desiredValue);
      }

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
        HistoricalTimeSeriesResolutionResult resolved = getTimeSeriesResolver().resolve(identifiers, date, null, null, desiredValue.getValueName(), getTimeSeriesResolverKey());
        if (resolved == null) {
          if (s_logger.isDebugEnabled() && desiredValue.getValueName().equals(MarketDataRequirementNames.MARKET_VALUE)) {
            s_logger.debug("Missing market data {}", desiredValue);
          }
          return null;
        } else {
          final ValueProperties.Builder properties = ValueProperties.with(ValuePropertyNames.FUNCTION, getSyntheticFunctionName());
          if (resolved.getAdjuster() != null) {
            final ExternalIdBundle resolvedIdentifiers = resolved.getHistoricalTimeSeriesInfo().getExternalIdBundle().toBundle(date);
            final HistoricalTimeSeriesAdjustment adjustment = resolved.getAdjuster().getAdjustment(resolvedIdentifiers);
            properties.with(NORMALIZATION_PROPERTY, adjustment.toString());
          }
          return new ValueSpecification(desiredValue.getValueName(), ComputationTargetSpecification.of(resolved.getHistoricalTimeSeriesInfo().getUniqueId()), properties.get());
        }
      }

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
        return null;
      }

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue) {
        return null;
      }

      @Override
      public Serializable getAvailabilityHintKey() {
        final ArrayList<Serializable> key = new ArrayList<Serializable>(3);
        key.add(AbstractHistoricalMarketDataProvider.this.getClass().getName());
        key.add(date);
        key.add(getTimeSeriesResolverKey());
        return key;
      }

    };
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
    return ObjectUtils.equals(getTimeSeriesResolverKey(), historicalSpec.getTimeSeriesResolverKey());
  }

  protected HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  protected HistoricalTimeSeriesResolver getTimeSeriesResolver() {
    return _historicalTimeSeriesResolver;
  }

  protected String getTimeSeriesResolverKey() {
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
