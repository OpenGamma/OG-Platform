/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
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
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionUtils;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.math.MathException;
import com.opengamma.math.curve.NodalDoublesCurve;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class IRFutureOptionVolatilitySurfaceDataFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(IRFutureOptionVolatilitySurfaceDataFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBVolatilitySurfaceSpecificationSource source = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    final String fullSpecificationName = surfaceName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceSpecification specification = source.getSpecification(fullSpecificationName, InstrumentTypeProperties.IR_FUTURE_OPTION);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName);
    }
    String surfaceQuoteType = null;
    String surfaceQuoteUnits = null;
    String curveName = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      final ValueSpecification spec = input.getSpecification();
      final String valueName = spec.getValueName();
      if (valueName.equals(ValueRequirementNames.VOLATILITY_SURFACE_DATA)) {
        surfaceQuoteType = spec.getProperty(SurfacePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE);
        surfaceQuoteUnits = spec.getProperty(SurfacePropertyNames.PROPERTY_SURFACE_UNITS);
      } else if (valueName.equals(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA)) {
        curveName = spec.getProperty(ValuePropertyNames.CURVE);
      }
    }
    if (surfaceQuoteType == null) {
      throw new OpenGammaRuntimeException("Could not get surface quote type");
    }
    if (surfaceQuoteUnits == null) {
      throw new OpenGammaRuntimeException("Could not get surface quote units");
    }
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final ValueProperties surfaceProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION)
        .with(SurfacePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, surfaceQuoteType)
        .with(SurfacePropertyNames.PROPERTY_SURFACE_UNITS, surfaceQuoteUnits).get();
    final Object volatilityDataObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), surfaceProperties));
    if (volatilityDataObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Number, Double> data = (VolatilitySurfaceData<Number, Double>) volatilityDataObject;
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), properties);
    if (surfaceQuoteUnits.equals(SurfacePropertyNames.VOLATILITY_QUOTE)) {
      return Collections.singleton(new ComputedValue(spec, getSurfaceFromVolatilityQuote(data)));
    }
    if (curveName == null) {
      throw new OpenGammaRuntimeException("Could not get curve name");
    }
    final ValueRequirement futuresRequirement = new ValueRequirement(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(),
        ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_PRICE).get());
    final Object futurePricesObject = inputs.getValue(futuresRequirement);
    if (futurePricesObject == null) {
      throw new OpenGammaRuntimeException("Could not get futures price data");
    }
    final NodalDoublesCurve futurePrices = (NodalDoublesCurve) futurePricesObject;
    return Collections.singleton(new ComputedValue(spec, getSurfaceFromPriceQuote(specification, data, futurePrices, now, surfaceQuoteType)));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(),
        createValueProperties()
        .withAny(ValuePropertyNames.SURFACE)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      throw new OpenGammaRuntimeException("Can only get a single surface; asked for " + surfaceNames);
    }
    final String surfaceName = surfaceNames.iterator().next();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBVolatilitySurfaceSpecificationSource source = new ConfigDBVolatilitySurfaceSpecificationSource(configSource);
    final String fullSpecificationName = surfaceName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceSpecification specification = source.getSpecification(fullSpecificationName, InstrumentTypeProperties.IR_FUTURE_OPTION);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName);
    }
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final ValueProperties surfaceProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION)
        .with(SurfacePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
        .with(SurfacePropertyNames.PROPERTY_SURFACE_UNITS, specification.getQuoteUnits()).get();
    requirements.add(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), surfaceProperties));
    if (specification.getQuoteUnits().equals(SurfacePropertyNames.PRICE_QUOTE)) {
      final Set<String> curveNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
      final String curveName;
      if (curveNames == null || curveNames.size() != 1) {
        curveName = surfaceName;
      } else {
        curveName = curveNames.iterator().next();
      }
      final ValueProperties curveProperties = ValueProperties.builder()
          .with(ValuePropertyNames.CURVE, curveName)
          .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_PRICE).get();
      final ValueRequirement curveRequirement = new ValueRequirement(ValueRequirementNames.FUTURE_PRICE_CURVE_DATA, target.toSpecification(), curveProperties);
      requirements.add(curveRequirement);
    }
    return requirements;
  }

  private static VolatilitySurfaceData<Number, Double> getSurfaceFromVolatilityQuote(final VolatilitySurfaceData<Number, Double> optionVolatilities) {
    final Map<Pair<Number, Double>, Double> volatilityValues = new HashMap<Pair<Number, Double>, Double>();
    final ObjectArrayList<Number> xList = new ObjectArrayList<Number>();
    final DoubleArrayList yList = new DoubleArrayList();
    for (final Number x : optionVolatilities.getXs()) {
      for (final Double y : optionVolatilities.getYs()) {
        final Double volatility = optionVolatilities.getVolatility(x, y);
        if (volatility != null) {
          xList.add(x);
          yList.add(y);
          volatilityValues.put(Pair.of(x, y), volatility / 100);
        }
      }
    }
    return new VolatilitySurfaceData<Number, Double>(optionVolatilities.getDefinitionName(), optionVolatilities.getSpecificationName(),
        optionVolatilities.getTarget(), xList.toArray(new Number[0]), yList.toArray(new Double[0]), volatilityValues);
  }

  private static VolatilitySurfaceData<Number, Double> getSurfaceFromPriceQuote(final VolatilitySurfaceSpecification specification,
      final VolatilitySurfaceData<Number, Double> optionPrices, final NodalDoublesCurve futurePrices, final ZonedDateTime now, final String surfaceQuoteType) {
    double callAboveStrike = 0;
    if (specification.getSurfaceInstrumentProvider() instanceof BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider) {
      callAboveStrike = ((BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider) specification.getSurfaceInstrumentProvider()).useCallAboveStrike();
    }
    final Map<Pair<Number, Double>, Double> volatilityValues = new HashMap<Pair<Number, Double>, Double>();
    final ObjectArrayList<Number> xList = new ObjectArrayList<Number>();
    final DoubleArrayList yList = new DoubleArrayList();
    for (final Number x : optionPrices.getXs()) {
      final Number t = IRFutureOptionUtils.getTime(x.doubleValue(), now);
      final double forward = futurePrices.getYValue(x.doubleValue());
      for (final Double y : optionPrices.getYs()) {
        final Double price = optionPrices.getVolatility(x, y);
        if (price != null) {
          try {
            final double volatility = getVolatility(surfaceQuoteType, y / 100, price, forward, t.doubleValue(), callAboveStrike);
            xList.add(x);
            yList.add(y);
            volatilityValues.put(Pair.of(x, y), volatility);
          } catch (final MathException e) {
            s_logger.info("Could not imply volatility for ({}, {}); error was {}", new Object[] {x, y, e.getMessage()});
          } catch (final IllegalArgumentException e) {
            s_logger.info("Could not imply volatility for ({}, {}); error was {}", new Object[] {x, y, e.getMessage()});
          }
        }
      }
    }
    return new VolatilitySurfaceData<Number, Double>(optionPrices.getDefinitionName(), optionPrices.getSpecificationName(),
        optionPrices.getTarget(), xList.toArray(new Number[0]), yList.toArray(new Double[0]), volatilityValues);
  }

  private static double getVolatility(final String surfaceQuoteType, final double strike, final double price, final double forward, final double t, final Double callAboveStrike) {
    if (surfaceQuoteType.equals(SurfaceQuoteType.CALL_STRIKE)) {
      return BlackFormulaRepository.impliedVolatility(price, forward, strike, t, true);
    }
    if (surfaceQuoteType.equals(SurfaceQuoteType.PUT_STRIKE)) {
      return BlackFormulaRepository.impliedVolatility(price, forward, strike, t, false);
    }
    if (surfaceQuoteType.equals(SurfaceQuoteType.CALL_AND_PUT_STRIKE)) {
      if (callAboveStrike == null) {
        throw new OpenGammaRuntimeException("Value for call above strike was null");
      }
      if (strike > callAboveStrike) {
        return BlackFormulaRepository.impliedVolatility(price, forward, strike, t, true);
      }
      return BlackFormulaRepository.impliedVolatility(price, forward, strike, t, false);
    }
    throw new OpenGammaRuntimeException("Cannot handle surface quote type " + surfaceQuoteType);
  }

}
