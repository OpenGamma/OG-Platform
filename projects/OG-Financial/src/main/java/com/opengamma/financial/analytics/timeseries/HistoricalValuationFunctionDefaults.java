/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.tuple.Pair;

/**
 * Injection function to supply the valuation time and timezone for exchange traded instruments based on the exchange information. For other instruments that have a region, that is used to infer a
 * timezone.
 */
public abstract class HistoricalValuationFunctionDefaults extends DefaultPropertyFunction {

  protected HistoricalValuationFunctionDefaults(final ComputationTargetType targetType) {
    super(targetType, false);
  }

  /**
   * Implementation for portfolio node targets. The default is injected based on the securities under the positions under this node.
   */
  public static class PortfolioNodeDefaults extends HistoricalValuationFunctionDefaults {

    public PortfolioNodeDefaults() {
      super(ComputationTargetType.PORTFOLIO_NODE);
    }

    private Pair<ZoneId, LocalTime> getDefaults(final GetDefaults getDefaults, final PortfolioNode node) {
      ZoneId timeZone = null;
      LocalTime valuationTime = null;
      for (final Position position : node.getPositions()) {
        final Pair<ZoneId, LocalTime> found = getDefaults(getDefaults, position);
        if (found != null) {
          if (found.getFirst() != null) {
            if (timeZone != null) {
              if (!timeZone.equals(found.getFirst())) {
                throw new IllegalStateException();
              }
            } else {
              timeZone = found.getFirst();
            }
          }
          if (found.getSecond() != null) {
            if (valuationTime != null) {
              if (!valuationTime.equals(found.getSecond())) {
                throw new IllegalStateException();
              }
            } else {
              valuationTime = found.getSecond();
            }
          }
        }
      }
      for (final PortfolioNode child : node.getChildNodes()) {
        final Pair<ZoneId, LocalTime> found = getDefaults(getDefaults, child);
        if (found != null) {
          if (found.getFirst() != null) {
            if (timeZone != null) {
              if (!timeZone.equals(found.getFirst())) {
                throw new IllegalStateException();
              }
            } else {
              timeZone = found.getFirst();
            }
          }
          if (found.getSecond() != null) {
            if (valuationTime != null) {
              if (!valuationTime.equals(found.getSecond())) {
                throw new IllegalStateException();
              }
            } else {
              valuationTime = found.getSecond();
            }
          }
        }
      }
      if ((timeZone != null) || (valuationTime != null)) {
        return Pair.of(timeZone, valuationTime);
      } else {
        return null;
      }
    }

    @Override
    protected Pair<ZoneId, LocalTime> getDefaults(final GetDefaults getDefaults, final ComputationTarget target) {
      try {
        return getDefaults(getDefaults, target.getPortfolioNode());
      } catch (final IllegalStateException e) {
        return null;
      }
    }

  }

  /**
   * Implementation for position or trade targets. The default is injected based on the security under the position.
   */
  public static class PositionOrTradeDefaults extends HistoricalValuationFunctionDefaults {

    public PositionOrTradeDefaults() {
      super(ComputationTargetType.POSITION_OR_TRADE);
    }

    @Override
    protected Pair<ZoneId, LocalTime> getDefaults(final GetDefaults getDefaults, final ComputationTarget target) {
      return getDefaults(getDefaults, target.getPositionOrTrade());
    }

  }

  /**
   * Implementation for security targets. The default is injected based on the exchange the security is traded on.
   */
  public static class SecurityDefaults extends HistoricalValuationFunctionDefaults {

    public SecurityDefaults() {
      super(FinancialSecurityTypes.FINANCIAL_SECURITY);
    }

    @Override
    protected Pair<ZoneId, LocalTime> getDefaults(final GetDefaults getDefaults, final ComputationTarget target) {
      return getDefaults.call(target.getSecurity());
    }

  }

  private static class GetDefaults {

    private final RegionSource _regions;
    private final ExchangeSource _exchanges;

    public GetDefaults(final RegionSource regions, final ExchangeSource exchanges) {
      _regions = regions;
      _exchanges = exchanges;
    }

    public Pair<ZoneId, LocalTime> call(final Security security) {
      if (_exchanges != null) {
        final ExternalId exchangeId = FinancialSecurityUtils.getExchange(security);
        if (exchangeId != null) {
          final Exchange exchange = _exchanges.getSingle(exchangeId);
          if (exchange != null) {
            final ZoneId tz = exchange.getTimeZone();
            // TODO: how do we get a closing time which we can use for the valuation time?
            if (tz != null) {
              return Pair.of(tz, null);
            }
          }
        }
      }
      if (_regions != null) {
        final ExternalId regionId = FinancialSecurityUtils.getRegion(security);
        if (regionId != null) {
          try {
            final Collection<? extends Region> regions = _regions.get(regionId.toBundle(), VersionCorrection.LATEST); // TODO: this should be version = compilation time, correction = LATEST
            ZoneId tz = null;
            for (final Region region : regions) {
              if (region.getTimeZone() != null) {
                tz = region.getTimeZone();
                break;
              }
            }
            // TODO: what should we use as the valuation time?
            if (tz != null) {
              return Pair.of(tz, null);
            }
          } catch (final DataNotFoundException e) {
            // Ignore
          }
        }
      }
      return null;
    }

  }

  protected Pair<ZoneId, LocalTime> getDefaults(final GetDefaults getDefaults, final PositionOrTrade position) {
    return getDefaults.call(position.getSecurity());
  }

  protected abstract Pair<ZoneId, LocalTime> getDefaults(final GetDefaults getDefaults, final ComputationTarget target);

  // DefaultPropertyFunction

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.HISTORICAL_TIME_SERIES, HistoricalValuationFunction.TIMEZONE_PROPERTY);
    defaults.addValuePropertyName(ValueRequirementNames.HISTORICAL_TIME_SERIES, HistoricalValuationFunction.VALUATION_TIME_PROPERTY);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final GetDefaults getDefaults = new GetDefaults(OpenGammaCompilationContext.getRegionSource(context), OpenGammaCompilationContext.getExchangeSource(context));
    final Pair<ZoneId, LocalTime> defaults = getDefaults(getDefaults, target);
    if (defaults == null) {
      return null;
    }
    if (HistoricalValuationFunction.TIMEZONE_PROPERTY.equals(propertyName)) {
      return Collections.singleton(defaults.getFirst().getId());
    } else if (HistoricalValuationFunction.VALUATION_TIME_PROPERTY.equals(propertyName)) {
      return Collections.singleton(defaults.getSecond().toString());
    }
    return null;
  }

}
