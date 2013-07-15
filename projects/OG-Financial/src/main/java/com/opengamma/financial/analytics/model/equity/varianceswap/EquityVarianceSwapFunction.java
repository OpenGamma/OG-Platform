/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.varianceswap;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.EquityVarianceSwapConverter;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfaceUtils;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.money.Currency;

/**
 *
 */
public abstract class EquityVarianceSwapFunction extends AbstractFunction.NonCompiledInvoker {
  /** Property for the type of volatility surface to use */
  public static final String PROPERTY_VOLATILITY_SURFACE_TYPE = "VolatilitySurfaceType";
  /** Pure implied volatility calculation method */
  public static final String PURE_IMPLIED_VOLATILITY = "PureImpliedVolatility";
  /** Pure local volatility calculation method */
  public static final String PURE_LOCAL_VOLATILITY = "PureLocalVolatility";
  private EquityVarianceSwapConverter _converter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    _converter = new EquityVarianceSwapConverter(holidaySource);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_VARIANCE_SWAP_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(PDEPropertyNamesAndValues.PROPERTY_DISCOUNTING_CURVE_NAME)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD)
        .withAny(ValuePropertyNames.SURFACE)
        .with(ValuePropertyNames.CALCULATION_METHOD, getCalculationMethod())
        .with(PROPERTY_VOLATILITY_SURFACE_TYPE, getVolatilitySurfaceType())
        .get();
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> discountingCurveNames = constraints.getValues(PDEPropertyNamesAndValues.PROPERTY_DISCOUNTING_CURVE_NAME);
    if (discountingCurveNames == null || discountingCurveNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveCalculationConfigs = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (forwardCurveCalculationConfigs == null || forwardCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (forwardCurveNames == null || forwardCurveNames.size() != 1) {
      return null;
    }
    final Set<String> forwardCurveCalculationMethods = constraints.getValues(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    if (forwardCurveCalculationMethods == null || forwardCurveCalculationMethods.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> curveCurrencies = constraints.getValues(ValuePropertyNames.CURVE_CURRENCY);
    if (curveCurrencies == null || curveCurrencies.size() != 1) {
      return null;
    }
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    ExternalId underlyingId = security.getSpotUnderlyingId();
    if (underlyingId.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER)) {
      underlyingId = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, underlyingId.getValue());
    }
    final ComputationTargetRequirement underlyingTarget = ComputationTargetRequirement.of(underlyingId);
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, underlyingTarget);
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final ValueRequirement discountingCurveRequirement = getCurveRequirement(currency, desiredValue);
    final ValueRequirement dividendsRequirement = getDividendRequirement(underlyingTarget);
    final ValueRequirement forwardCurveRequirement = getForwardCurveRequirement(underlyingTarget, desiredValue);
    final ValueRequirement volatilityRequirement = getVolatilityRequirement(underlyingTarget, desiredValue);
    final ValueRequirement underlyingTSRequirement = getTimeSeriesRequirement(context, security);
    return Sets.newHashSet(spotRequirement, discountingCurveRequirement, forwardCurveRequirement, volatilityRequirement, underlyingTSRequirement, dividendsRequirement);
    //dividendsRequirement
  }

  private ValueRequirement getCurveRequirement(final Currency currency, final ValueRequirement desiredValue) {
    final String curveName = desiredValue.getConstraint(PDEPropertyNamesAndValues.PROPERTY_DISCOUNTING_CURVE_NAME);
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties);
  }

  private ValueRequirement getDividendRequirement(final ComputationTargetReference target) {
    return new ValueRequirement(ValueRequirementNames.AFFINE_DIVIDENDS, target, ValueProperties.none());
  }

  private ValueRequirement getForwardCurveRequirement(final ComputationTargetReference target, final ValueRequirement desiredValue) {
    final String forwardCurveCcyName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String discountingCurveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveCalculationMethod = desiredValue.getConstraint(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CURRENCY, forwardCurveCcyName)
        .with(ValuePropertyNames.CURVE, discountingCurveName)
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, curveCalculationMethod)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target, properties);
  }

  private ValueRequirement getVolatilityRequirement(final ComputationTargetReference target, final ValueRequirement desiredValue) {
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)
        .get();
    return new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target, properties);
  }

  private ValueRequirement getTimeSeriesRequirement(final FunctionCompilationContext context, final EquityVarianceSwapSecurity security) {
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(security.getSpotUnderlyingId().toBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
    if (timeSeries == null) {
      return null;
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE, DateConstraint.NULL, true, DateConstraint.VALUATION_TIME, true);
  }

  protected SmileSurfaceDataBundle getData(final FunctionInputs inputs) {
    final Object volatilitySurfaceObject = inputs.getValue(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }

    final Object forwardCurveObject = inputs.getValue(ValueRequirementNames.FORWARD_CURVE);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;

    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Object, Object> volatilitySurface = (VolatilitySurfaceData<Object, Object>) volatilitySurfaceObject;
    return BlackVolatilitySurfaceUtils.getDataFromStandardQuotes(forwardCurve, volatilitySurface);
  }

  protected EquityVarianceSwapConverter getConverter() {
    return _converter;
  }

  protected abstract String getValueRequirementName();

  protected abstract String getCalculationMethod();

  protected abstract String getVolatilitySurfaceType();

}
