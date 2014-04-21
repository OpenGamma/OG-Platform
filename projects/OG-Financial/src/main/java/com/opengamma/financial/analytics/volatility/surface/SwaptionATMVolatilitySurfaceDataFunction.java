/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SwaptionATMVolatilitySurfaceDataFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(SwaptionATMVolatilitySurfaceDataFunction.class);

  private ConfigDBVolatilitySurfaceSpecificationSource _volatilitySurfaceSpecificationSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _volatilitySurfaceSpecificationSource = ConfigDBVolatilitySurfaceSpecificationSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    String surfaceQuoteType = null;
    String surfaceQuoteUnits = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      final ValueSpecification spec = input.getSpecification();
      final String valueName = spec.getValueName();
      if (valueName.equals(ValueRequirementNames.VOLATILITY_SURFACE_DATA)) {
        surfaceQuoteType = spec.getProperty(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE);
        surfaceQuoteUnits = spec.getProperty(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS);
        break;
      }
    }
    if (surfaceQuoteType == null) {
      throw new OpenGammaRuntimeException("Could not get surface quote type");
    }
    if (surfaceQuoteUnits == null) {
      throw new OpenGammaRuntimeException("Could not get surface quote units");
    }
    final ValueProperties surfaceProperties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.SWAPTION_ATM)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, surfaceQuoteType).with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, surfaceQuoteUnits).get();
    final Object volatilityDataObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), surfaceProperties));
    if (volatilityDataObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Tenor, Tenor> surfaceData = (VolatilitySurfaceData<Tenor, Tenor>) volatilityDataObject;
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.SWAPTION_ATM).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, getSurface(surfaceData)));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.SURFACE)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.SWAPTION_ATM).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> surfaceNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      throw new OpenGammaRuntimeException("Can only get a single surface; asked for " + surfaceNames);
    }
    final String surfaceName = surfaceNames.iterator().next();
    final String fullSpecificationName = surfaceName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceSpecification specification = _volatilitySurfaceSpecificationSource.getSpecification(fullSpecificationName, InstrumentTypeProperties.SWAPTION_ATM);
    if (specification == null) {
      s_logger.error("Could not get volatility surface specification named " + fullSpecificationName);
      return null;
    }
    final String surfaceQuoteType = specification.getSurfaceQuoteType();
    final String surfaceQuoteUnits = specification.getQuoteUnits();
    if (!surfaceQuoteType.equals(SurfaceAndCubeQuoteType.EXPIRY_MATURITY_ATM)) {
      s_logger.error("Cannot use this function for surfaces with quote types other than {}, asked for {}", SurfaceAndCubeQuoteType.EXPIRY_MATURITY_ATM, surfaceQuoteType);
      return null;
    }
    if (!surfaceQuoteUnits.equals(SurfaceAndCubePropertyNames.VOLATILITY_QUOTE)) {
      s_logger.error("Cannot use this function for surfaces with quote types other than {}, asked for {}", SurfaceAndCubePropertyNames.VOLATILITY_QUOTE, surfaceQuoteUnits);
      return null;
    }
    final ValueProperties surfaceProperties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.SWAPTION_ATM)
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, specification.getSurfaceQuoteType())
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, specification.getQuoteUnits()).get();
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(), surfaceProperties));
  }

  private static VolatilitySurfaceData<Double, Double> getSurface(final VolatilitySurfaceData<Tenor, Tenor> volatilities) {
    final Map<Pair<Double, Double>, Double> volatilityValues = new HashMap<Pair<Double, Double>, Double>();
    final DoubleArrayList xList = new DoubleArrayList();
    final DoubleArrayList yList = new DoubleArrayList();
    for (final Tenor x : volatilities.getUniqueXValues()) {
      final double xTime = getTime(x);
      final List<ObjectsPair<Tenor, Double>> yValuesForX = volatilities.getYValuesForX(x);
      for (final ObjectsPair<Tenor, Double> pair : yValuesForX) {
        final double yTime = getTime(pair.getFirst());
        final Double volatility = pair.getSecond();
        if (volatility != null) {
          xList.add(xTime);
          yList.add(yTime);
          volatilityValues.put(DoublesPair.of(xTime, yTime), volatility);
        }
      }
    }
    return new VolatilitySurfaceData<Double, Double>(volatilities.getDefinitionName(), volatilities.getSpecificationName(), volatilities.getTarget(), xList.toArray(new Double[0]),
        yList.toArray(new Double[0]), volatilityValues);
  }

  //TODO not the best way to do this
  private static double getTime(final Tenor tenor) {
    final Period period = tenor.getPeriod();
    if (period.getYears() != 0) {
      return period.getYears();
    } else if (period.getMonths() != 0) {
      return period.getMonths() / 12.;
    } else if (period.getDays() != 0) {
      return period.getDays() / 365.;
    }
    throw new OpenGammaRuntimeException("Cannot handle tenor " + period);
  }
}
