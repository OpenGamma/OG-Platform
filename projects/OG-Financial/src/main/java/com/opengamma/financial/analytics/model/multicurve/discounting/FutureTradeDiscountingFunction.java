/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.multicurve.discounting;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.FutureTradeConverter;
import com.opengamma.financial.analytics.model.multicurve.MultiCurvePricingFunction;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.util.ArgumentChecker;

/**
 * Base function for all pricing and risk functions for future trades that use the discounting curve
 * construction method.
 */
public abstract class FutureTradeDiscountingFunction extends MultiCurvePricingFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FutureTradeDiscountingFunction.class);
  /** Key defining how the time series resolution is to occur */
  //TODO this should not be hard-coded
  private static final String RESOLUTION_KEY = HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;

  public FutureTradeDiscountingFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  /**
   * Constructs an object capable of converting from {@link ComputationTarget} to {@link InstrumentDefinition}.
   * @param context The compilation context, not null
   * @return The converter
   */
  protected FutureTradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    return new FutureTradeConverter(securitySource, holidaySource, conventionBundleSource, regionSource);
  }

  /**
   * Base compiled function for all pricing and risk functions for future trades that use the discounting curve
   * construction method.
   */
  protected abstract class FutureTradeDiscountingCompiledFunction extends MultiCurveCompiledFunction {
    /** Converts targets to definitions */
    private final FutureTradeConverter _converter;
    /** True if the result properties set the {@link ValuePropertyNames#CURRENCY} property */
    private final boolean _withCurrency;

    /**
     * @param converter Converts targets (where the target is a {@link Trade} to definitions, not null
     * @param withCurrency True if the result properties set the {@link ValuePropertyNames.#CURRENCY] property
     */
    public FutureTradeDiscountingCompiledFunction(final FutureTradeConverter converter, final boolean withCurrency) {
      ArgumentChecker.notNull(converter, "converter");
      _converter = converter;
      _withCurrency = withCurrency;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.TRADE;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      final Security security = target.getTrade().getSecurity();
      if (security instanceof BondFutureSecurity || security instanceof InterestRateFutureSecurity) {
        return true;
      }
      return false;
    }

    @Override
    protected FinancialSecurity getSecurityFromTarget(final ComputationTarget target) {
      return (FinancialSecurity) target.getTrade().getSecurity();
    }

    @Override
    protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
      final ValueProperties.Builder properties = createValueProperties()
          .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
          .withAny(CURVE_EXPOSURES);
      if (_withCurrency) {
        properties.with(CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
      }
      return properties;
    }

    @Override
    protected boolean requirementsSet(final ValueProperties constraints) {
      final Set<String> curveExposures = constraints.getValues(CURVE_EXPOSURES);
      if (curveExposures == null) {
        return false;
      }
      return true;
    }

    @Override
    protected Builder getCurveProperties(final ComputationTarget target, final ValueProperties constraints) {
      return ValueProperties.builder();
    }

    @Override
    protected InstrumentDefinition<?> getDefinitionFromTarget(final ComputationTarget target) {
      return _converter.convert(target.getTrade());
    }

    @Override
    protected Set<ValueRequirement> getConversionTimeSeriesRequirements(final FunctionCompilationContext context, final ComputationTarget target,
        final InstrumentDefinition<?> definition) {
      final FinancialSecurity security = getSecurityFromTarget(target);
      final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
      final ExternalIdBundle idBundle = security.getExternalIdBundle();
      final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(security.getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE,
          RESOLUTION_KEY);
      if (timeSeries == null) {
        s_logger.warn("Failed to find time series for: " + idBundle.toString());
        return null;
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofDays(7)), true, DateConstraint.VALUATION_TIME, true));
    }

    @Override
    protected InstrumentDerivative getDerivative(final ComputationTarget target, final ZonedDateTime now, final HistoricalTimeSeriesBundle timeSeries, final InstrumentDefinition<?> definition) {
      final Trade trade = target.getTrade();
      @SuppressWarnings("unchecked")
      final InstrumentDefinitionWithData<?, Double> tradeDefinition = (InstrumentDefinitionWithData<?, Double>) definition;
      final FinancialSecurity security = (FinancialSecurity) trade.getSecurity();
      Double lastMarginPrice = null;
      try {
        lastMarginPrice = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle()).getTimeSeries().getLatestValue();
      } catch (final NoSuchElementException e) {
        throw new OpenGammaRuntimeException("Time series for " + security.getExternalIdBundle() + " was empty");
      }
      double referencePrice = lastMarginPrice;
      if (trade.getTradeDate() != null) {
        if (trade.getTradeDate().isEqual(now.toLocalDate())) {
          if (trade.getPremium() != null) {
            referencePrice = trade.getPremium();
          }
        }
      }
      return tradeDefinition.toDerivative(now, referencePrice);
    }

  }
}
