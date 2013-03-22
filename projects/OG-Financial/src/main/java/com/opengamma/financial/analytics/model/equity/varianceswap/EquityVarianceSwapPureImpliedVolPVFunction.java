/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.varianceswap;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwap;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwapDefinition;
import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapStaticReplicationPricer;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class EquityVarianceSwapPureImpliedVolPVFunction extends EquityVarianceSwapFunction {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock).minusYears(3); //TODO remove me - just for testing
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final EquityVarianceSwapSecurity security = (EquityVarianceSwapSecurity) target.getSecurity();
    final EquityVarianceSwapDefinition definition = security.accept(getConverter());
    final Object spotObject = inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Spot value was null");
    }
    final Object yieldCurveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (yieldCurveObject == null) {
      throw new OpenGammaRuntimeException("Yield curve was null");
    }
    final Object dividendsObject = inputs.getValue(ValueRequirementNames.AFFINE_DIVIDENDS);
    if (dividendsObject == null) {
      throw new OpenGammaRuntimeException("Dividends were null");
    }
    final Object forwardCurveObject = inputs.getValue(ValueRequirementNames.FORWARD_CURVE);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Forward curve was null");
    }
    final Object volatilitiesObject = inputs.getValue(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA);
    if (volatilitiesObject == null) {
      throw new OpenGammaRuntimeException("Volatility data were null");
    }
    final Object tsObject = inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    final double spot = (Double) spotObject;
    final YieldAndDiscountCurve yieldCurve = (YieldAndDiscountCurve) yieldCurveObject;
    final AffineDividends dividends = (AffineDividends) dividendsObject;
    final SmileSurfaceDataBundle volatilities = getData(inputs);
    final DoubleTimeSeries<LocalDate> underlyingTS = ((HistoricalTimeSeries) tsObject).getTimeSeries();
    final EquityVarianceSwap swap = definition.toDerivative(now, underlyingTS);
    final EquityVarianceSwapStaticReplicationPricer pricer = EquityVarianceSwapStaticReplicationPricer.builder().create(); //TODO don't just use defaults
    final double pv = pricer.priceFromImpliedVols(swap, spot, yieldCurve, dividends, volatilities);
    final ValueProperties properties = desiredValue.getConstraints().copy()
        .withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, pv));
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.PRESENT_VALUE;
  }

  @Override
  protected String getCalculationMethod() {
    return EquityVarianceSwapStaticReplicationFunction.CALCULATION_METHOD;
  }

  @Override
  protected String getVolatilitySurfaceType() {
    return PURE_IMPLIED_VOLATILITY;
  }
}
