/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Function to source time series data for each of the instruments in a {@link CurveSpecification} from a {@link HistoricalTimeSeriesSource} attached to the execution context. These time series are
 * used to convert {@link InstrumentDefinition}s into the {@link InstrumentDerivative}s used in pricing and curve construction.
 */
public class CurveConfigurationHistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(CurveConfigurationHistoricalTimeSeriesFunction.class);

  private ConfigDBCurveConstructionConfigurationSource _curveConstructionConfigurationSource;

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveConstructionConfigurationSource = ConfigDBCurveConstructionConfigurationSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, 
      final Set<ValueRequirement> desiredValues)
    throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final HistoricalTimeSeriesBundle bundle = new HistoricalTimeSeriesBundle();
    final CurveConstructionConfiguration constructionConfig = (CurveConstructionConfiguration) inputs.getValue(ValueRequirementNames.CURVE_CONSTRUCTION_CONFIG);
    final List<CurveGroupConfiguration> groups = constructionConfig.getCurveGroups();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    for (final CurveGroupConfiguration group : groups) {
      //TODO do we want to put information in about whether or not to use fixing time series?
      //TODO do we want to exclude node types that definitely don't need fixing data?
      for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
        final String curveName = entry.getKey();
        final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
        final ValueRequirement bundleRequirement = new ValueRequirement(ValueRequirementNames.CURVE_HISTORICAL_TIME_SERIES, targetSpec, properties);
        final ValueRequirement specRequirement = new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, targetSpec, properties);
        final HistoricalTimeSeriesBundle bundleForCurve = (HistoricalTimeSeriesBundle) inputs.getValue(bundleRequirement);
        final CurveSpecification curveSpec = (CurveSpecification) inputs.getValue(specRequirement);
        for (final CurveNodeWithIdentifier node : curveSpec.getNodes()) {
          String dataField = node.getDataField();
          ExternalIdBundle ids = ExternalIdBundle.of(node.getIdentifier());
          HistoricalTimeSeries ts = bundleForCurve.get(dataField, ids);
          if (ts != null) {
            bundle.add(dataField, ids, bundleForCurve.get(dataField, ids));
          } else {
            s_logger.info("Could not get historical time series for {}", ids);
          }
          if (node instanceof PointsCurveNodeWithIdentifier) {
            final PointsCurveNodeWithIdentifier pointsNode = (PointsCurveNodeWithIdentifier) node;
            dataField = pointsNode.getUnderlyingDataField();
            ids = ExternalIdBundle.of(pointsNode.getUnderlyingIdentifier());
            ts = bundleForCurve.get(dataField, ids);
            if (ts != null) {
              bundle.add(dataField, ids, bundleForCurve.get(dataField, ids));
            } else {
              s_logger.info("Could not get historical time series for {}", ids);
            }
          }
          if (node.getCurveNode() instanceof ZeroCouponInflationNode) {
            final ZeroCouponInflationNode inflationNode = (ZeroCouponInflationNode) node.getCurveNode();
            final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
            final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
            InflationLegConvention inflationLegConvention = conventionSource.getSingle(inflationNode.getInflationLegConvention(), InflationLegConvention.class);
            final Security sec = securitySource.getSingle(inflationLegConvention.getPriceIndexConvention().toBundle());
            if (sec == null) {
              throw new OpenGammaRuntimeException("CurveNodeCurrencyVisitor.visitInflationLegConvention: index with id " + inflationLegConvention.getPriceIndexConvention()
                  + " was null");
            }
            if (!(sec instanceof PriceIndex)) {
              throw new OpenGammaRuntimeException("CurveNodeCurrencyVisitor.visitInflationLegConvention: index with id " + inflationLegConvention.getPriceIndexConvention()
                  + " not of type PriceIndex");
            }
            final PriceIndex indexSecurity = (PriceIndex) sec;
            ids = indexSecurity.getExternalIdBundle();
            final HistoricalTimeSeries priceIndexSeries = bundleForCurve.get(dataField, ids);
            if (priceIndexSeries != null) {
              if (priceIndexSeries.getTimeSeries().isEmpty()) {
                s_logger.info("Could for get historical time series for {}", ids);
              } else {
                bundle.add(dataField, ids, bundleForCurve.get(dataField, ids));
              }
            } else {
              s_logger.info("Couldn't get time series for {}", ids);
            }
          }
        }
      }
    }
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, targetSpec, desiredValue.getConstraints().copy().get());
    return Collections.singleton(new ComputedValue(spec, bundle));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> curveConstructionConfigs = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG);
    if (curveConstructionConfigs == null || curveConstructionConfigs.size() != 1) {
      return null;
    }
    final String curveConstructionConfig = Iterables.getOnlyElement(curveConstructionConfigs);
    final CurveConstructionConfiguration constructionConfig = _curveConstructionConfigurationSource.getCurveConstructionConfiguration(curveConstructionConfig);
    final Set<ValueRequirement> requirements = new HashSet<>();
    final List<CurveGroupConfiguration> groups = constructionConfig.getCurveGroups();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    for (final CurveGroupConfiguration group : groups) {
      //TODO do we want to put information in about whether or not to use fixing time series?
      //TODO do we want to exclude node types that definitely don't need fixing data?
      for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
        final String curveName = entry.getKey();
        final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName).get();
        requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_HISTORICAL_TIME_SERIES, targetSpec, properties));
        requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, targetSpec, properties));
      }
    }
    final ValueProperties properties = ValueProperties.builder().with(ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG, curveConstructionConfig).get();
    requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_CONSTRUCTION_CONFIG, targetSpec, properties));
    return requirements;
  }

}
