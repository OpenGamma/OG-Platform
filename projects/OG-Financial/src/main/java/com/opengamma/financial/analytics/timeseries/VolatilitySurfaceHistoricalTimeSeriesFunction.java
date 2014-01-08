/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceDefinitionSource;
import com.opengamma.financial.analytics.volatility.surface.ConfigDBVolatilitySurfaceSpecificationSource;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * 
 */
public abstract class VolatilitySurfaceHistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(VolatilitySurfaceHistoricalTimeSeriesFunction.class);

  private ConfigDBVolatilitySurfaceDefinitionSource _volatilitySurfaceDefinitionSource;
  private ConfigDBVolatilitySurfaceSpecificationSource _volatilitySurfaceSpecificationSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _volatilitySurfaceDefinitionSource = ConfigDBVolatilitySurfaceDefinitionSource.init(context, this);
    _volatilitySurfaceSpecificationSource = ConfigDBVolatilitySurfaceSpecificationSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
      throws AsynchronousExecution {
    final HistoricalTimeSeriesSource timeSeriesSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String dataField = desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
    final String resolutionKey;
    final Set<String> resolutionKeyConstraint = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY);
    if (resolutionKeyConstraint == null || resolutionKeyConstraint.size() != 1) {
      resolutionKey = "Null";
    } else {
      resolutionKey = Iterables.getOnlyElement(resolutionKeyConstraint);
    }
    final LocalDate startDate = DateConstraint.evaluate(executionContext, desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY));
    final boolean includeStart = HistoricalTimeSeriesFunctionUtils.parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY));
    final Set<String> endDateConstraint = desiredValue.getConstraints().getValues(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
    final String endDateString;
    if (endDateConstraint == null || endDateConstraint.size() != 1) {
      endDateString = "Now";
    } else {
      endDateString = Iterables.getOnlyElement(endDateConstraint);
    }
    LocalDate endDate = DateConstraint.evaluate(executionContext, endDateString);
    final boolean includeEnd = HistoricalTimeSeriesFunctionUtils.parseBoolean(desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY));
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final VolatilitySurfaceDefinition<Object, Object> definition = getSurfaceDefinition(target, surfaceName);
    final VolatilitySurfaceSpecification specification = getSurfaceSpecification(target, surfaceName);
    final SurfaceInstrumentProvider<Object, Object> provider = (SurfaceInstrumentProvider<Object, Object>) specification.getSurfaceInstrumentProvider();
    final HistoricalTimeSeriesBundle bundle = new HistoricalTimeSeriesBundle();
    for (final Object x : definition.getXs()) {
      for (final Object y : definition.getYs()) {
        ExternalId id = provider.getInstrument(x, y, endDate);
        if (id.getScheme().equals(ExternalSchemes.BLOOMBERG_TICKER_WEAK)) {
          id = ExternalSchemes.bloombergTickerSecurityId(id.getValue());
        }
        final ExternalIdBundle identifier = ExternalIdBundle.of(id);
        final HistoricalTimeSeries timeSeries = timeSeriesSource.getHistoricalTimeSeries(dataField, identifier, resolutionKey, startDate, includeStart, endDate, includeEnd);
        if (timeSeries != null) {
          bundle.add(dataField, identifier, timeSeries);
        } else {
          s_logger.warn("Could not get time series for {}", identifier);
        }
      }
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_HISTORICAL_TIME_SERIES, target.toSpecification(), desiredValue
        .getConstraints()), bundle));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.SURFACE).with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, getInstrumentType())
        .with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, getDataField()).withAny(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY)
        .withAny(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE)
        .withAny(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY)
        .with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE, HistoricalTimeSeriesFunctionUtils.NO_VALUE).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.VOLATILITY_SURFACE_HISTORICAL_TIME_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    ValueProperties.Builder constraints = null;
    final ValueProperties desiredValueConstraints = desiredValue.getConstraints();
    Set<String> values = desiredValueConstraints.getValues(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      constraints = desiredValueConstraints.copy().with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, MarketDataRequirementNames.MARKET_VALUE);
    } else if (values.size() > 1) {
      constraints = desiredValueConstraints.copy().withoutAny(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY)
          .with(HistoricalTimeSeriesFunctionUtils.DATA_FIELD_PROPERTY, values.iterator().next());
    }
    values = desiredValueConstraints.getValues(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValueConstraints.copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY, "");
    } else if (values.size() > 1) {
      if (constraints == null) {
        constraints = desiredValueConstraints.copy();
      }
      constraints.withoutAny(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY).with(HistoricalTimeSeriesFunctionUtils.RESOLUTION_KEY_PROPERTY, values.iterator().next());
    }
    values = desiredValueConstraints.getValues(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValueConstraints.copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY, "");
    }
    values = desiredValueConstraints.getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (constraints == null) {
        constraints = desiredValueConstraints.copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_START_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE);
    }
    values = desiredValueConstraints.getValues(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY);
    if ((values == null) || values.isEmpty()) {
      if (constraints == null) {
        constraints = desiredValueConstraints.copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.END_DATE_PROPERTY, "");
    }
    values = desiredValueConstraints.getValues(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY);
    if ((values == null) || (values.size() != 1)) {
      if (constraints == null) {
        constraints = desiredValueConstraints.copy();
      }
      constraints.with(HistoricalTimeSeriesFunctionUtils.INCLUDE_END_PROPERTY, HistoricalTimeSeriesFunctionUtils.YES_VALUE);
    }
    return Collections.emptySet();
    //    if (constraints == null) {
    //      return Collections.singleton(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_HISTORICAL_TIME_SERIES, target.toSpecification(), constraints.get()));
    //    }
    //    // We need to substitute ourselves with the adjusted constraints
    //    return Collections.singleton(new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_HISTORICAL_TIME_SERIES, target.toSpecification(), constraints.get()));
  }

  protected abstract String getInstrumentType();

  protected abstract String getDataField();

  private VolatilitySurfaceDefinition<Object, Object> getSurfaceDefinition(final ComputationTarget target, final String definitionName) {
    final String fullDefinitionName = definitionName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceDefinition<Object, Object> definition = (VolatilitySurfaceDefinition<Object, Object>) _volatilitySurfaceDefinitionSource.getDefinition(fullDefinitionName,
        getInstrumentType());
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface definition named " + fullDefinitionName + " for instrument type " + getInstrumentType());
    }
    return definition;
  }

  private VolatilitySurfaceSpecification getSurfaceSpecification(final ComputationTarget target, final String specificationName) {
    final String fullSpecificationName = specificationName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceSpecification specification = _volatilitySurfaceSpecificationSource.getSpecification(fullSpecificationName, getInstrumentType());
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName);
    }
    return specification;
  }

}
