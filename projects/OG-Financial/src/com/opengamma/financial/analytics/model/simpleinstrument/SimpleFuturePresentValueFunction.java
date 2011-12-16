/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.simpleinstrument;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.analytics.conversion.SimpleFutureConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.simpleinstruments.derivative.SimpleInstrument;
import com.opengamma.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.financial.simpleinstruments.pricing.SimpleFuturePresentValueCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 * 
 */
public class SimpleFuturePresentValueFunction extends AbstractFunction.NonCompiledInvoker {
  private static final SimpleFutureConverter CONVERTER = new SimpleFutureConverter();
  private static final SimpleFuturePresentValueCalculator CALCULATOR = new SimpleFuturePresentValueCalculator();
  private final String _curveName;

  public SimpleFuturePresentValueFunction(final String curveName) {
    Validate.notNull(curveName, "curve name");
    _curveName = curveName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FutureSecurity security = (FutureSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final Currency currency = security.getCurrency();
    final Object curveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(currency, _curveName, null, null));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + _curveName + " curve");
    }
    final ExternalId underlyingIdentifier = getUnderlyingIdentifier(security);
    final Object spotObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, underlyingIdentifier));
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + underlyingIdentifier);
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    final double spot = (Double) spotObject;
    final SimpleFutureDataBundle data = new SimpleFutureDataBundle(curve, spot, 0);
    final SimpleInstrument instrument = security.accept(CONVERTER).toDerivative(now);
    final CurrencyAmount pv = instrument.accept(CALCULATOR, data);
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, _curveName)
        .with(ValuePropertyNames.CURRENCY, pv.getCurrency().getCode()).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, pv.getAmount()));
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
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    return security instanceof EnergyFutureSecurity || security instanceof MetalFutureSecurity || security instanceof IndexFutureSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, _curveName)
        .with(ValuePropertyNames.CURRENCY, ((FutureSecurity) target.getSecurity()).getCurrency().getCode()).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final FutureSecurity future = (FutureSecurity) target.getSecurity();
    final ExternalId underlyingIdentifier = getUnderlyingIdentifier(future);
    final ValueRequirement yieldCurve = YieldCurveFunction.getCurveRequirement(future.getCurrency(), _curveName, null, null);
    final ValueRequirement spot = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, underlyingIdentifier);
    return Sets.newHashSet(yieldCurve, spot);
  }

  private ExternalId getUnderlyingIdentifier(final FutureSecurity future) {
    final ExternalId underlyingIdentifier;
    if (future instanceof EnergyFutureSecurity) {
      underlyingIdentifier = ((EnergyFutureSecurity) future).getUnderlyingId();
    } else if (future instanceof MetalFutureSecurity) {
      underlyingIdentifier = ((MetalFutureSecurity) future).getUnderlyingId();
    } else if (future instanceof IndexFutureSecurity) {
      underlyingIdentifier = ((IndexFutureSecurity) future).getUnderlyingId();
    } else {
      throw new OpenGammaRuntimeException("Future was not an energy, index or metal future; should never happen");
    }
    if (underlyingIdentifier == null) {
      throw new OpenGammaRuntimeException("Underlying identifier for " + future + " was null");
    }
    return underlyingIdentifier;
  }

}
