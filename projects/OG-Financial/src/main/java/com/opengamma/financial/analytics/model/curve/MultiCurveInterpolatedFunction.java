/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.FX_MATRIX;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveSpecification;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.PeriodicallyCompoundedRateNode;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Produces yield curves using the {@link InterpolatedDataProperties#CALCULATION_METHOD_NAME} method.
 */
public class MultiCurveInterpolatedFunction extends
  MultiCurveFunction<MulticurveProviderInterface, MulticurveDiscountBuildingRepository, GeneratorYDCurve, MulticurveSensitivity> {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(MultiCurveInterpolatedFunction.class);
  /**
   * @param curveConfigurationName The curve configuration name, not null
   */
  public MultiCurveInterpolatedFunction(final String curveConfigurationName) {
    super(curveConfigurationName);
  }

  @Override
  protected String getCurveTypeProperty() {
    return InterpolatedDataProperties.CALCULATION_METHOD_NAME;
  }

  @Override
  protected InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> getCalculator() {
    throw new UnsupportedOperationException("Curves created with the Interpolated method do not use a calculator");
  }

  @Override
  protected InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> getSensitivityCalculator() {
    throw new UnsupportedOperationException("Curves created with the Interpolated method do not use a sensitivity calculator");
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final String[] curveNames,
      final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
    return new MultiCurveInterpolatedCompiledFunctionDefinition(earliestInvocation, latestInvocation, curveNames, exogenousRequirements, curveConstructionConfiguration);
  }

  /**
   * Compiled function implementation.
   */
  protected class MultiCurveInterpolatedCompiledFunctionDefinition extends CurveCompiledFunctionDefinition {
    /** The curve construction configuration */
    private final CurveConstructionConfiguration _curveConstructionConfiguration;
    @Override
    public boolean canHandleMissingRequirements() {
      return true;
    }

    @Override
    public boolean canHandleMissingInputs() {
      return true;
    }

    /**
     * @param earliestInvocation The earliest time for which this function is valid, null if there is no bound
     * @param latestInvocation The latest time for which this function is valid, null if there is no bound
     * @param curveNames The names of the curves produced by this function, not null
     * @param exogenousRequirements The exogenous requirements, not null
     * @param curveConstructionConfiguration The curve construction configuration, not null
     */
    protected MultiCurveInterpolatedCompiledFunctionDefinition(
        final ZonedDateTime earliestInvocation,
        final ZonedDateTime latestInvocation,
        final String[] curveNames,
        final Set<ValueRequirement> exogenousRequirements,
        final CurveConstructionConfiguration curveConstructionConfiguration) {
      super(earliestInvocation, latestInvocation, curveNames, ValueRequirementNames.YIELD_CURVE, exogenousRequirements);
      ArgumentChecker.notNull(curveConstructionConfiguration, "curve construction configuration");
      _curveConstructionConfiguration = curveConstructionConfiguration;
    }

    @Override
    protected Pair<MulticurveProviderInterface, CurveBuildingBlockBundle> getCurves(final FunctionInputs inputs, final ZonedDateTime now, final MulticurveDiscountBuildingRepository builder,
        final MulticurveProviderInterface knownData, final FunctionExecutionContext context, final FXMatrix fx) {
      final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(context);
      int n = 0;
      // These loops are here because the market data snapshot might not contain all of the required information
      for (final CurveGroupConfiguration group: _curveConstructionConfiguration.getCurveGroups()) {
        for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry: group.getTypesForCurves().entrySet()) {
          final String curveName = entry.getKey();
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, curveName).get();
          final InterpolatedCurveSpecification specification =
              (InterpolatedCurveSpecification) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, curveProperties));
          n += specification.getNodes().size();
        }
      }

      final MulticurveProviderDiscount curveBundle = (MulticurveProviderDiscount) getKnownData(inputs);
      final LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<>();
      final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundles = new LinkedHashMap<>();
      int totalNodes = 0;
      for (final CurveGroupConfiguration group: _curveConstructionConfiguration.getCurveGroups()) {

        for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry: group.getTypesForCurves().entrySet()) {

          final String curveName = entry.getKey();
          final List<? extends CurveTypeConfiguration> types = entry.getValue();

          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, curveName).get();

          final Object dataObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, curveProperties));
          if (dataObject == null) {
            throw new OpenGammaRuntimeException("Could not get yield curve data");
          }
          final SnapshotDataBundle marketData = (SnapshotDataBundle) dataObject;

          final InterpolatedCurveSpecification specification =
              (InterpolatedCurveSpecification) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, curveProperties));

          n = specification.getNodes().size();

          final double[] times = new double[n];
          final double[] yields = new double[n];
          final double[][] jacobian = new double[n][n];
          boolean isYield = false;
          int i = 0;
          int compoundPeriodsPerYear = 0;
          final int nNodesForCurve = specification.getNodes().size();
          for (final CurveNodeWithIdentifier node: specification.getNodes()) {
            final CurveNode curveNode = node.getCurveNode();
            if (curveNode instanceof ContinuouslyCompoundedRateNode) {
              if (i == 0) {
                isYield = true;
              } else {
                if (!isYield) {
                  throw new OpenGammaRuntimeException("Was expecting only continuously-compounded rate nodes; have " + curveNode);
                }
              }
            } else if (curveNode instanceof DiscountFactorNode) {
              if (i == 0) {
                isYield = false;
              } else {
                if (isYield) {
                  throw new OpenGammaRuntimeException("Was expecting only discount factor nodes; have " + curveNode);
                }
              }
            } else if (curveNode instanceof PeriodicallyCompoundedRateNode) {
              if (i == 0) {
                compoundPeriodsPerYear = ((PeriodicallyCompoundedRateNode) curveNode).getCompoundingPeriodsPerYear();
                isYield = true;
              } else {
                if (!isYield) {
                  throw new OpenGammaRuntimeException("Was expecting only periodically compounded nodes; have " + curveNode);
                }
              }
            } else {
              throw new OpenGammaRuntimeException("Can only handle discount factor or continuously-compounded rate nodes; have " + curveNode);
            }
            final Double marketValue = marketData.getDataPoint(node.getIdentifier());
            if (marketValue == null) {
              throw new OpenGammaRuntimeException("Could not get market value for " + node);
            }
            final Tenor maturity = curveNode.getResolvedMaturity();
            times[i] = TimeCalculator.getTimeBetween(now, now.plus(maturity.getPeriod()));
            yields[i] = marketValue;
            jacobian[i][i] = 1;
            i++;
          }
          final String interpolatorName = specification.getInterpolatorName();
          final String rightExtrapolatorName = specification.getRightExtrapolatorName();
          final String leftExtrapolatorName = specification.getLeftExtrapolatorName();
          final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
          final InterpolatedDoublesCurve rawCurve = InterpolatedDoublesCurve.from(times, yields, interpolator, curveName);
          final YieldAndDiscountCurve discountCurve;
          if (compoundPeriodsPerYear != 0 && isYield) {
            discountCurve = YieldPeriodicCurve.from(compoundPeriodsPerYear, rawCurve);
          } else if (isYield) {
            discountCurve = new YieldCurve(curveName, rawCurve);
          } else {
            discountCurve = new DiscountCurve(curveName, rawCurve);
          }
          for (final CurveTypeConfiguration type: types) {
            if (type instanceof DiscountingCurveTypeConfiguration) {
              final Currency currency = Currency.parse(((DiscountingCurveTypeConfiguration) type).getReference());
              curveBundle.setCurve(currency, discountCurve);
            } else if (type instanceof IborCurveTypeConfiguration) {
              final IborIndexConvention iborIndexConvention = conventionSource.getSingle(((IborCurveTypeConfiguration) type).getConvention(), IborIndexConvention.class);
              final Tenor iborIndexTenor = ((IborCurveTypeConfiguration) type).getTenor();

              final int spotLag = iborIndexConvention.getSettlementDays();
              final IborIndex index = new IborIndex(iborIndexConvention.getCurrency(), iborIndexTenor.getPeriod(), spotLag, iborIndexConvention.getDayCount(),
                  iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
              curveBundle.setCurve(index, discountCurve);
            }
          }
          unitMap.put(curveName, Pairs.of(totalNodes + nNodesForCurve, nNodesForCurve));
          unitBundles.put(curveName, Pairs.of(new CurveBuildingBlock(unitMap), new DoubleMatrix2D(jacobian)));
          totalNodes += nNodesForCurve;
        }
      }
      return Pairs.of((MulticurveProviderInterface) curveBundle, new CurveBuildingBlockBundle(unitBundles));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> requirements = super.getRequirements(compilationContext, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      final Set<ValueRequirement> trimmed = new HashSet<>();
      for (final ValueRequirement requirement : requirements) {
        final String requirementName = requirement.getValueName();
        if (!(requirementName.equals(CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES) || requirementName.equals(FX_MATRIX))) {
          trimmed.add(requirement);
        }
      }
      return requirements;
    }

    @Override
    protected MulticurveProviderInterface getKnownData(final FunctionInputs inputs) {
      final FXMatrix fxMatrix = new FXMatrix();
      MulticurveProviderDiscount knownData;
      if (getExogenousRequirements().isEmpty()) {
        knownData = new MulticurveProviderDiscount(fxMatrix);
      } else {
        knownData = (MulticurveProviderDiscount) inputs.getValue(ValueRequirementNames.CURVE_BUNDLE);
        knownData.setForexMatrix(fxMatrix);
      }
      return knownData;
    }

    @Override
    protected MulticurveDiscountBuildingRepository getBuilder(final double absoluteTolerance, final double relativeTolerance, final int maxIterations) {
      // Returns null because builder is not used
      return null;
    }

    @Override
    protected GeneratorYDCurve getGenerator(final CurveDefinition definition, final LocalDate valuationDate) {
      // Returns null because generator is not used
      return null;
    }

    @Override
    protected CurveNodeVisitor<InstrumentDefinition<?>> getCurveNodeConverter(final FunctionExecutionContext context, final SnapshotDataBundle marketData,
        final ExternalId dataId, final HistoricalTimeSeriesBundle historicalData, final ZonedDateTime valuationTime, final FXMatrix fxMatrix) {
      // No need to convert to InstrumentDefinition if we are not fitting the curve.
      return null;
    }

    @Override
    protected Set<ComputedValue> getResults(final ValueSpecification bundleSpec, final ValueSpecification jacobianSpec, final ValueProperties bundleProperties,
        final Pair<MulticurveProviderInterface, CurveBuildingBlockBundle> pair) {
      final Set<ComputedValue> result = new HashSet<>();
      final MulticurveProviderDiscount provider = (MulticurveProviderDiscount) pair.getFirst();
      result.add(new ComputedValue(bundleSpec, provider));
      result.add(new ComputedValue(jacobianSpec, pair.getSecond()));
      for (final String curveName : getCurveNames()) {
        final ValueProperties curveProperties = bundleProperties.copy()
            .with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, getCurveTypeProperty())
            .withoutAny(CURVE)
            .with(CURVE, curveName)
            .get();
        final YieldAndDiscountCurve curve = provider.getCurve(curveName);
        if (curve == null) {
          s_logger.error("Could not get curve called {} from configuration {}", curveName, getCurveConstructionConfigurationName());
        } else {
          final ValueSpecification curveSpec = new ValueSpecification(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties);
          result.add(new ComputedValue(curveSpec, curve));
        }
      }
      return result;
    }

  }
}
