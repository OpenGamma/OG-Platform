/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.marketdata.ExternalIdBundleResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.view.ConfigDocumentWatchSetProvider;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * For a given curve name, returns a {@link SnapshotDataBundle} containing the market data for the nodes
 * of that curve. This function does not require that any or all of the market data is available for
 * it to return the snapshot.
 */
public class CurveMarketDataFunction extends AbstractFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(CurveMarketDataFunction.class);
  /** The curve name */
  private final String _curveName;

  /**
   * @param curveName The curve name, not null
   */
  public CurveMarketDataFunction(final String curveName) {
    ArgumentChecker.notNull(curveName, "curve name");
    _curveName = curveName;
  }

  /**
   * Gets the curve name.
   * @return The curve name
   */
  public String getCurveName() {
    return _curveName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    ConfigDocumentWatchSetProvider.reinitOnChanges(context, null, CurveDefinition.class);
    ConfigDocumentWatchSetProvider.reinitOnChanges(context, null, InterpolatedCurveDefinition.class);
    ConfigDocumentWatchSetProvider.reinitOnChanges(context, null, ConstantCurveDefinition.class);
    ConfigDocumentWatchSetProvider.reinitOnChanges(context, null, SpreadCurveDefinition.class);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURVE, _curveName)
        .get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    try {
      final AbstractCurveSpecification specification = CurveUtils.getSpecification(atInstant, configSource, atZDT.toLocalDate(), _curveName);
      return new MyCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000), specification, spec);
    } catch (final Exception e) {
      throw new OpenGammaRuntimeException(e.getMessage() + ": problem in CurveDefinition called " + _curveName);
    }
  }

  /**
   * Function that gets market data for a curve.
   */
  protected class MyCompiledFunction extends AbstractInvokingCompiledFunction {
    /** The curve specification */
    private final AbstractCurveSpecification _specification;
    /** The result specification */
    private final ValueSpecification _spec;

    /**
     * @param earliestInvocation The earliest time at which this function can be invoked
     * @param latestInvocation The latest time at which this function can be invoked
     * @param specification The curve specification
     * @param spec The result specification
     */
    public MyCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final AbstractCurveSpecification specification,
        final ValueSpecification spec) {
      super(earliestInvocation, latestInvocation);
      _specification = specification;
      _spec = spec;
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      final ExternalIdBundleResolver resolver = new ExternalIdBundleResolver(executionContext.getComputationTargetResolver());
      SnapshotDataBundle marketData;
      if (_specification instanceof ConstantCurveSpecification) {
        final ConstantCurveSpecification constantCurve = (ConstantCurveSpecification) _specification;
        marketData = getDataForConstantCurve(constantCurve, inputs, resolver);
      } else if (_specification instanceof CurveSpecification) {
        final Collection<CurveNodeWithIdentifier> nodes = ((CurveSpecification) _specification).getNodes();
        marketData = getDataForNodes(nodes, inputs, resolver);
      } else {
        throw new OpenGammaRuntimeException("Cannot handle specifications of type " + _specification.getClass());
      }
      return Collections.singleton(new ComputedValue(_spec, marketData));
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.NULL;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
      return Collections.singleton(_spec);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> requirements = new HashSet<>();
      if (_specification instanceof ConstantCurveSpecification) {
        final ConstantCurveSpecification constantCurve = (ConstantCurveSpecification) _specification;
        requirements.add(new ValueRequirement(constantCurve.getDataField(), ComputationTargetType.PRIMITIVE, constantCurve.getIdentifier()));
      } else if (_specification instanceof CurveSpecification) {
        final Collection<CurveNodeWithIdentifier> nodes = ((CurveSpecification) _specification).getNodes();
        for (final CurveNodeWithIdentifier id : nodes) {
          try {
            if (id.getDataField() != null) {
              if (id.getCurveNode() instanceof BondNode) {
                requirements.add(new ValueRequirement(id.getDataField(), ComputationTargetType.SECURITY, id.getIdentifier()));
              } else {
                requirements.add(new ValueRequirement(id.getDataField(), ComputationTargetType.PRIMITIVE, id.getIdentifier()));
                if (id instanceof PointsCurveNodeWithIdentifier) {
                  final PointsCurveNodeWithIdentifier node = (PointsCurveNodeWithIdentifier) id;
                  requirements.add(new ValueRequirement(node.getUnderlyingDataField(), ComputationTargetType.PRIMITIVE, node.getUnderlyingIdentifier()));
                }
              }
            } else {
              requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, id.getIdentifier()));
            }
          } catch (final OpenGammaRuntimeException e) {
            s_logger.error(_curveName + " " + e.getMessage());
            return null;
          }
        }
      }
      return requirements;
    }

    @Override
    public boolean canHandleMissingRequirements() {
      return true;
    }

    @Override
    public boolean canHandleMissingInputs() {
      return true;
    }
  }

  /**
   * Gets the single data point requirement for a constant curve.
   * @param specification The curve specification
   * @param inputs The function inputs
   * @param resolver An id bundle resolver
   * @return A market data bundle, empty if the data is not found.
   */
  /* package */ static SnapshotDataBundle getDataForConstantCurve(final ConstantCurveSpecification specification, final FunctionInputs inputs, final ExternalIdBundleResolver resolver) {
    final SnapshotDataBundle marketData = new SnapshotDataBundle();
    final ComputedValue value = inputs.getComputedValue(new ValueRequirement(specification.getDataField(), ComputationTargetType.PRIMITIVE, specification.getIdentifier()));
    if (value != null) {
      final ExternalIdBundle identifiers = value.getSpecification().getTargetSpecification().accept(resolver);
      final Object valueObject = value.getValue();
      if (!(valueObject instanceof Double)) {
        throw new OpenGammaRuntimeException("Value for " + identifiers + " was not a number; have " + valueObject);
      }
      marketData.setDataPoint(identifiers, (Double) valueObject);
    } else {
      s_logger.info("Could not get market data for {} with field {}", specification.getIdentifier(), specification.getDataField());
    }
    return marketData;
  }

  /**
   * Gets the market data for a collection of curve nodes.
   * @param nodes The nodes
   * @param inputs The function inputs
   * @param resolver An id bundle resolver
   * @return A market data bundle, empty if the data are not found.
   */
  /* package */ static SnapshotDataBundle getDataForNodes(final Collection<CurveNodeWithIdentifier> nodes, final FunctionInputs inputs, final ExternalIdBundleResolver resolver) {
    final SnapshotDataBundle marketData = new SnapshotDataBundle();
    for (final CurveNodeWithIdentifier id : nodes) {
      if (id.getDataField() != null) {
        final ComputedValue value;
        if (id.getCurveNode() instanceof BondNode) {
          value = inputs.getComputedValue(new ValueRequirement(id.getDataField(), ComputationTargetType.SECURITY, id.getIdentifier()));
        } else {
          value = inputs.getComputedValue(new ValueRequirement(id.getDataField(), ComputationTargetType.PRIMITIVE, id.getIdentifier()));
        }
        if (value != null) {
          final Object valueObject = value.getValue();
          if (!(valueObject instanceof Double)) {
            throw new OpenGammaRuntimeException("Value for " + id.getIdentifier() + " was not a number; have " + valueObject);
          }
          final ExternalIdBundle identifiers = value.getSpecification().getTargetSpecification().accept(resolver);
          if (id instanceof PointsCurveNodeWithIdentifier) {
            final PointsCurveNodeWithIdentifier pointsId = (PointsCurveNodeWithIdentifier) id;
            final ComputedValue base = inputs.getComputedValue(new ValueRequirement(pointsId.getUnderlyingDataField(), ComputationTargetType.PRIMITIVE, pointsId.getUnderlyingIdentifier()));
            if (base != null) {
              final Object baseObject = base.getValue();
              if (!(baseObject instanceof Double)) {
                throw new OpenGammaRuntimeException("Value for " + pointsId.getUnderlyingIdentifier() + " was not a number; have " + valueObject);
              }
              final ExternalIdBundle spreadIdentifiers = value.getSpecification().getTargetSpecification().accept(resolver);
              if (value.getValue() == null || base.getValue() == null) {
                marketData.setDataPoint(spreadIdentifiers, null);
              } else {
                marketData.setDataPoint(spreadIdentifiers, (Double) valueObject + (Double) baseObject);
              }
            } else {
              s_logger.info("Could not get market data for {}", pointsId.getUnderlyingIdentifier());
            }
          } else {
            marketData.setDataPoint(identifiers, (Double) valueObject);
          }
        } else {
          s_logger.info("Could not get market data for {}", id.getIdentifier());
        }
      } else {
        final ComputedValue value = inputs.getComputedValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, id.getIdentifier()));
        if (value != null) {
          final ExternalIdBundle identifiers = value.getSpecification().getTargetSpecification().accept(resolver);
          marketData.setDataPoint(identifiers, (Double) value.getValue());
        } else {
          s_logger.info("Could not get market data for {}", id.getIdentifier());
        }
      }
    }
    return marketData;
  }

}
