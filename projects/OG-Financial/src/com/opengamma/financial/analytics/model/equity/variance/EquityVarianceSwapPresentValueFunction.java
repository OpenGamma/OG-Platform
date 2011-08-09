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
import com.opengamma.core.position.impl.TradeImpl;
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
import com.opengamma.financial.equity.variance.definition.VarianceSwapDefinition;
import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapDataBundle;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 */
public class EquityVarianceSwapPresentValueFunction extends AbstractFunction.NonCompiledInvoker {
  private static final VarianceSwapStaticReplication CALCULATOR = new VarianceSwapStaticReplication();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.PRESENT_VALUE;
  private EquityVarianceSwapConverter _converter;
  private static final String FIELD_NAME = HistoricalTimeSeriesFields.LAST_PRICE;
  private final String _curveDefinitionName;
  private final String _surfaceDefinitionName;

  public EquityVarianceSwapPresentValueFunction(String curveDefinitionName, String surfaceDefinitionName) {
    Validate.notNull(curveDefinitionName, "curve definition name");
    Validate.notNull(surfaceDefinitionName, "surface definition name");
    _curveDefinitionName = curveDefinitionName;
    _surfaceDefinitionName = surfaceDefinitionName;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {

    // 1. Build the analytic derivative to be priced
    TradeImpl trade = (TradeImpl) target.getTrade(); // confirms that the ComputationTargetType == TRADE
    EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) trade.getSecurity();
    ExternalId id = security.getSpotUnderlyingIdentifier();

    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);

    VarianceSwapDefinition defn = _converter.visitEquityVarianceSwapTrade(trade);
    final HistoricalTimeSeries timeSeries = dataSource.getHistoricalTimeSeries(FIELD_NAME, ExternalIdBundle.of(id), null);
    VarianceSwap deriv = defn.toDerivative(now, timeSeries.getTimeSeries());

    // 2. Build up the market data bundle
    Object volSurfaceObject = inputs.getValue(getVolatilitySurfaceRequirement(security));
    if (volSurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get Volatility Surface");
    }
    BlackVolatilitySurface volSurf = (BlackVolatilitySurface) volSurfaceObject;

    Object discountObject = inputs.getValue(getDiscountRequirement(security));
    if (discountObject == null) {
      throw new OpenGammaRuntimeException("Could not get Discount Curve");
    }
    YieldAndDiscountCurve discountCurve = (YieldAndDiscountCurve) discountObject;

    Object spotObject = inputs.getValue(getSpotRequirement(security));
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get Underlying's Spot value");
    }
    double spot = (Double) spotObject;

    Object forwardObject = inputs.getValue(getForwardRequirement(security));
    if (forwardObject == null) {
      throw new OpenGammaRuntimeException("Could not get Underlying's Forward Value");
    }
    double forward = (Double) forwardObject;

    VarianceSwapDataBundle market = new VarianceSwapDataBundle(volSurf, discountCurve, spot, forward);

    // 3. Compute and return the present value (ComputedValue)
    // Note that this may later be done via a getComputedValues method specific to the Function ( e.g. for EquityVarianceSwapPV01Function ) 
    double presentValue = CALCULATOR.presentValue(deriv, market);

    ValueSpecification valueSpec = getValueSpecification(security);
    return Collections.singleton(new ComputedValue(valueSpec, presentValue));
  }

  // Note that the properties are created using createValueProperties() - this sets the name of the function in the properties.
  // Not using this means that this function will not work
  private ValueSpecification getValueSpecification(EquityVarianceSwapSecurity security) {
    ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURRENCY, security.getCurrency().getCode()).get();
    ValueRequirement requirement = new ValueRequirement(VALUE_REQUIREMENT, ComputationTargetType.SECURITY, security.getUniqueId());
    return new ValueSpecification(requirement, properties);
  }

  private ValueRequirement getForwardRequirement(EquityVarianceSwapSecurity security) {
    ExternalId id = security.getSpotUnderlyingIdentifier();
    return new ValueRequirement(ValueRequirementNames.FORWARD, ComputationTargetType.SECURITY, UniqueId.of(id));
  }

  private ValueRequirement getSpotRequirement(EquityVarianceSwapSecurity security) {
    ExternalId id = security.getSpotUnderlyingIdentifier();
    return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, UniqueId.of(id));
  }

  // Note that createValueProperties is _not_ used - use will mean the engine can't find the requirement
  private ValueRequirement getDiscountRequirement(EquityVarianceSwapSecurity security) {
    ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, security.getCurrency().getCode()).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, security.getCurrency().getUniqueId(), properties);
  }

  private ValueRequirement getVolatilitySurfaceRequirement(EquityVarianceSwapSecurity security) {
    ExternalId id = security.getSpotUnderlyingIdentifier(); // TODO Review - when thinking about vol surface definitions
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE, ComputationTargetType.PRIMITIVE, UniqueId.of(id));
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    _converter = new EquityVarianceSwapConverter(holidaySource);

  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    if (target.getType() != ComputationTargetType.TRADE) {
      return false;
    }
    return target.getTrade().getSecurity() instanceof EquityVarianceSwapSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    return Sets.newHashSet(getForwardRequirement(security), getSpotRequirement(security), getDiscountRequirement(security), getVolatilitySurfaceRequirement(security));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(getValueSpecification((EquityVarianceSwapSecurity) target.getSecurity()));
  }
}
