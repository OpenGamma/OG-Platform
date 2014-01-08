/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.equity.varianceswap.EquityVarianceSwapStaticReplicationFunction;
import com.opengamma.util.time.DateUtils;

/**
 * @deprecated This has been replaced by the pair, RawEquityOptionVolatilitySurfaceDataFunction, EquityFutureOptionVolatilitySurfaceDataFunction
 */
@Deprecated
public class Grid2DInterpolatedVolatilitySurfaceFunctionDeprecated extends AbstractFunction.NonCompiledInvoker { //TODO rename or make less specific to equity vol surfaces
  private final String _definitionName;
  private final String _instrumentType;
  private final GridInterpolator2D _interpolator;
  private VolatilitySurfaceDefinition<?, ?> _definition;
  private ValueSpecification _result;
  private Set<ValueSpecification> _results;
  private ValueRequirement _requirement;

  public Grid2DInterpolatedVolatilitySurfaceFunctionDeprecated(final String definitionName, final String instrumentType, final String tInterpolatorName, final String tLeftExtrapolatorName,
      final String tRightExtrapolatorName, final String kInterpolatorName, final String kLeftExtrapolatorName, final String kRightExtrapolatorName) {
    Validate.notNull(definitionName, "definition name");
    Validate.notNull(instrumentType, "instrument type");
    Validate.notNull(tInterpolatorName, "t interpolator name");
    Validate.notNull(tLeftExtrapolatorName, "t left extrapolator name");
    Validate.notNull(tRightExtrapolatorName, "t right extrapolator name");
    Validate.notNull(kInterpolatorName, "k interpolator name");
    Validate.notNull(kLeftExtrapolatorName, "k left extrapolator name");
    Validate.notNull(kRightExtrapolatorName, "k right extrapolator name");
    _definitionName = definitionName;
    _instrumentType = instrumentType;
    final Interpolator1D tInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(tInterpolatorName, tLeftExtrapolatorName, tRightExtrapolatorName);
    final Interpolator1D kInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(kInterpolatorName, kLeftExtrapolatorName, kRightExtrapolatorName);
    _interpolator = new GridInterpolator2D(tInterpolator, kInterpolator);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _definition = ConfigDBVolatilitySurfaceDefinitionSource.init(context, this).getDefinition(_definitionName, _instrumentType);
    if (_definition == null) {
      throw new OpenGammaRuntimeException("Couldn't find Volatility Surface Definition for " + _instrumentType + " called " + _definitionName);
    }
    final ValueProperties surfaceProperties = ValueProperties.builder().with(ValuePropertyNames.SURFACE, _definitionName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType)
        .withAny(EquityVarianceSwapStaticReplicationFunction.STRIKE_PARAMETERIZATION_METHOD/*, VarianceSwapStaticReplication.StrikeParameterization.STRIKE.toString()*/).get();
    final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(_definition.getTarget().getUniqueId());
    _requirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, targetSpec, surfaceProperties);
    _result = new ValueSpecification(ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE, targetSpec, createValueProperties().with(ValuePropertyNames.SURFACE, _definitionName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, _instrumentType)
        .withAny(EquityVarianceSwapStaticReplicationFunction.STRIKE_PARAMETERIZATION_METHOD/*, VarianceSwapStaticReplication.StrikeParameterization.STRIKE.toString()*/).get());
    _results = Collections.singleton(_result);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final Object volatilitySurfaceDataObject = inputs.getValue(_requirement);
    if (volatilitySurfaceDataObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + _requirement);
    }
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<LocalDate, Double> volatilitySurfaceData = (VolatilitySurfaceData<LocalDate, Double>) volatilitySurfaceDataObject;
    final int n = volatilitySurfaceData.getXs().length;
    final int m = volatilitySurfaceData.getYs().length;
    final DoubleArrayList t = new DoubleArrayList();
    final DoubleArrayList k = new DoubleArrayList();
    final DoubleArrayList sigma = new DoubleArrayList();
    final LocalDate[] xDates = volatilitySurfaceData.getXs();
    final Double[] y = volatilitySurfaceData.getYs();
    for (int i = 0; i < n; i++) {
      final Double time = DateUtils.getDifferenceInYears(now.toLocalDate(), xDates[i]);
      for (int j = 0; j < m; j++) {
        final Double strike = y[j];
        final Double vol = volatilitySurfaceData.getVolatility(xDates[i], y[j]);
        if (time != null && strike != null && vol != null) {
          t.add(time);
          k.add(strike);
          sigma.add(vol);
        }
      }
    }
    final Surface<Double, Double, Double> surface = InterpolatedDoublesSurface.from(t.toDoubleArray(), k.toDoubleArray(), sigma.toDoubleArray(), _interpolator);
    final VolatilitySurface volatilitySurface = new VolatilitySurface(surface);
    return Collections.singleton(new ComputedValue(_result, volatilitySurface));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.ANYTHING; // [PLAT-2286] - Should this be something more specific? The target of the definition is anything unique identifiable
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return ObjectUtils.equals(target.getUniqueId(), _definition.getTarget().getUniqueId());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.singleton(_requirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return _results;
  }
}
