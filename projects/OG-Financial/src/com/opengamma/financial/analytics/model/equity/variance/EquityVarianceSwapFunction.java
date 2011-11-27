/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.variance;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.equity.EquityVarianceSwapConverter;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.financial.equity.variance.VarianceSwapDataBundle;
import com.opengamma.financial.equity.variance.definition.VarianceSwapDefinition;
import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication.StrikeParameterization;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.time.TimeCalculator;

/**
 * 
 */
public abstract class EquityVarianceSwapFunction extends AbstractFunction.NonCompiledInvoker {
  /** */
  public static final String STRIKE_PARAMETERIZATION_METHOD = "StrikeParameterizationMethod";
  private static final String FIELD_NAME = HistoricalTimeSeriesFields.LAST_PRICE;

  private EquityVarianceSwapConverter _converter; // set in init()

  private final String _curveDefinitionName;
  private final String _surfaceDefinitionName;
  @SuppressWarnings("unused")
  private final String _forwardCalculationMethod;
  private final String _strikeParameterizationMethodName;
  private final StrikeParameterization _strikeParameterizationMethod;

  public EquityVarianceSwapFunction(String curveDefinitionName, String surfaceDefinitionName, String forwardCalculationMethod, String strikeParameterizationMethodName) {
    Validate.notNull(curveDefinitionName, "curve definition name");
    Validate.notNull(surfaceDefinitionName, "surface definition name");
    Validate.notNull(forwardCalculationMethod, "forward calculation method");
    Validate.notNull(strikeParameterizationMethodName, "strike parameterization method");
    _curveDefinitionName = curveDefinitionName;
    _surfaceDefinitionName = surfaceDefinitionName;
    _forwardCalculationMethod = forwardCalculationMethod;
    _strikeParameterizationMethodName = strikeParameterizationMethodName;
    _strikeParameterizationMethod = StrikeParameterization.valueOf(strikeParameterizationMethodName);
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {

    // 1. Build the analytic derivative to be priced
    EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    ExternalId id = security.getSpotUnderlyingId();

    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);

    VarianceSwapDefinition defn = _converter.visitEquityVarianceSwapTrade(security);
    final HistoricalTimeSeries timeSeries = dataSource.getHistoricalTimeSeries(FIELD_NAME, ExternalIdBundle.of(id), null);
    VarianceSwap deriv = defn.toDerivative(now, timeSeries.getTimeSeries());

    // 2. Build up the market data bundle
    Object volSurfaceObject = inputs.getValue(getVolatilitySurfaceRequirement(security));
    if (volSurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get Volatility Surface");
    }
    VolatilitySurface volSurface = (VolatilitySurface) volSurfaceObject;
    BlackVolatilitySurface blackVolSurf = new BlackVolatilitySurface(volSurface.getSurface(), _strikeParameterizationMethod);

    Object discountObject = inputs.getValue(getDiscountCurveRequirement(security));
    if (discountObject == null) {
      throw new OpenGammaRuntimeException("Could not get Discount Curve");
    }
    YieldAndDiscountCurve discountCurve = (YieldAndDiscountCurve) discountObject;

    Object spotObject = inputs.getValue(getSpotRequirement(security));
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get Underlying's Spot value");
    }
    double spot = (Double) spotObject;

//    Object forwardObject = inputs.getValue(getForwardRequirement(security));
//    ValueRequirement temp = getForwardRequirement(security);
//    if (forwardObject == null) {
//      throw new OpenGammaRuntimeException("Could not get Underlying's Forward Value");
//    }
    
    //TODO remove this when the forward is coming through the engine
    double expiry = TimeCalculator.getTimeBetween(executionContext.getValuationClock().zonedDateTime(), security.getLastObservationDate());
    final double discountFactor = discountCurve.getDiscountFactor(expiry);
    Validate.isTrue(discountFactor != 0, "The discount curve has returned a zero value for a discount bond. Check rates.");
    final double forward = spot / discountFactor;
//
    VarianceSwapDataBundle market = new VarianceSwapDataBundle(blackVolSurf, discountCurve, spot, forward);
//    // 3. Compute and return the value (ComputedValue)
    return getResults(target, inputs, deriv, market);
  }

  protected abstract Set<ComputedValue> getResults(final ComputationTarget target, final FunctionInputs inputs, final VarianceSwap derivative, final VarianceSwapDataBundle market);

  protected abstract ValueSpecification getValueSpecification(final ComputationTarget target);
  
  protected String getCurveDefinitionName() {
    return _curveDefinitionName;
  }
  
  protected String getSurfaceName() {
    return _surfaceDefinitionName;
  }
//  private ValueRequirement getForwardRequirement(EquityVarianceSwapSecurity security) {
//    ExternalId id = security.getSpotUnderlyingId();
//    ValueProperties properties = ValueProperties.builder().with(EquityForwardFromSpotAndYieldCurveFunction.FORWARD_CALCULATION_METHOD, _forwardCalculationMethod)
//                                                          .with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode())
//                                                          .get();
//    return new ValueRequirement(ValueRequirementNames.FORWARD, ComputationTargetType.PRIMITIVE, UniqueId.of(id.getScheme().getName(), id.getValue()), properties);
//  }

  private ValueRequirement getSpotRequirement(EquityVarianceSwapSecurity security) {
    ExternalId id = security.getSpotUnderlyingId();
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, UniqueId.of(id.getScheme().getName(), id.getValue()));
  }

  // Note that createValueProperties is _not_ used - use will mean the engine can't find the requirement
  private ValueRequirement getDiscountCurveRequirement(EquityVarianceSwapSecurity security) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, _curveDefinitionName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, security.getCurrency().getUniqueId(), properties);
  }

  private ValueRequirement getVolatilitySurfaceRequirement(EquityVarianceSwapSecurity security) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, _surfaceDefinitionName)
                                                          .with(RawVolatilitySurfaceDataFunction.PROPERTY_SURFACE_INSTRUMENT_TYPE, "EQUITY_OPTION")
                                                          .with(STRIKE_PARAMETERIZATION_METHOD, _strikeParameterizationMethodName)
                                                          .get();
    ExternalId id = security.getSpotUnderlyingId();
    UniqueId newId = id.getScheme().equals(SecurityUtils.BLOOMBERG_TICKER) ? UniqueId.of(SecurityUtils.BLOOMBERG_TICKER_WEAK.getName(), id.getValue()) :
                                                                              UniqueId.of(id.getScheme().getName(), id.getValue());
    //UniqueId temp = UniqueId.of(SecurityUtils.BLOOMBERG_TICKER_WEAK.getName(), "DJX Index");
    return new ValueRequirement(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, newId, properties);
  }

  @Override
  public void init(final FunctionCompilationContext context) { 
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    _converter = new EquityVarianceSwapConverter(holidaySource);

  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof EquityVarianceSwapSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    return Sets.newHashSet(getDiscountCurveRequirement(security), getVolatilitySurfaceRequirement(security), getSpotRequirement(security));
    //TODO
//    return Sets.newHashSet(getForwardRequirement(security), getSpotRequirement(security), getDiscountRequirement(security), getVolatilitySurfaceRequirement(security));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(getValueSpecification(target));
  }
}
