/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES;
import static com.opengamma.engine.value.ValueRequirementNames.FX_MATRIX;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.financial.analytics.curve.AbstractCurveSpecification;
import com.opengamma.financial.analytics.curve.ConstantCurveSpecification;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveSpecification;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Produces bond yield curves using the {@link InterpolatedDataProperties#CALCULATION_METHOD_NAME} method. This function is accepts
 * only curves that contain bond nodes and assumes that the market data for the curve is a yield.
 */
public class IssuerMultiCurveInterpolatedFunction extends
  MultiCurveFunction<IssuerProviderInterface, MulticurveDiscountBuildingRepository, GeneratorYDCurve, MulticurveSensitivity> {

  /**
   * @param curveConfigurationName The curve configuration name, not null
   */
  public IssuerMultiCurveInterpolatedFunction(final String curveConfigurationName) {
    super(curveConfigurationName);
  }

  @Override
  protected String getCurveTypeProperty() {
    return InterpolatedDataProperties.CALCULATION_METHOD_NAME;
  }

  @Override
  protected InstrumentDerivativeVisitor<IssuerProviderInterface, Double> getCalculator() {
    throw new UnsupportedOperationException("Curves created with the Interpolated method do not use a calculator");
  }

  @Override
  protected InstrumentDerivativeVisitor<IssuerProviderInterface, MulticurveSensitivity> getSensitivityCalculator() {
    throw new UnsupportedOperationException("Curves created with the Interpolated method do not use a sensitivity calculator");
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final String[] curveNames,
      final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
    return new IssuerMultiCurveInterpolatedCompiledFunctionDefinition(earliestInvocation, latestInvocation, curveNames, exogenousRequirements, curveConstructionConfiguration);
  }

  /**
   * Compiled function implementation.
   */
  protected class IssuerMultiCurveInterpolatedCompiledFunctionDefinition extends CurveCompiledFunctionDefinition {
    /** The curve construction configuration */
    private final CurveConstructionConfiguration _curveConstructionConfiguration;

    /**
     * @param earliestInvocation The earliest time for which this function is valid, null if there is no bound
     * @param latestInvocation The latest time for which this function is valid, null if there is no bound
     * @param curveNames The names of the curves produced by this function, not null
     * @param exogenousRequirements The exogenous requirements, not null
     * @param curveConstructionConfiguration The curve construction configuration, not null
     */
    protected IssuerMultiCurveInterpolatedCompiledFunctionDefinition(
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
    protected Pair<IssuerProviderInterface, CurveBuildingBlockBundle> getCurves(final FunctionInputs inputs, final ZonedDateTime now, final MulticurveDiscountBuildingRepository builder,
        final IssuerProviderInterface knownData, final FunctionExecutionContext context, final FXMatrix fx) {
      final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
      final Object dataObject = inputs.getValue(ValueRequirementNames.CURVE_MARKET_DATA);
      if (dataObject == null) {
        throw new OpenGammaRuntimeException("Could not get yield curve data");
      }
      final SnapshotDataBundle marketData = (SnapshotDataBundle) dataObject;
      int n = 0;
      // These loops are here because the market data snapshot might not contain all of the required information
      for (final CurveGroupConfiguration group: _curveConstructionConfiguration.getCurveGroups()) {
        for (final Map.Entry<String, List<CurveTypeConfiguration>> entry: group.getTypesForCurves().entrySet()) {
          final String curveName = entry.getKey();
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, curveName).get();
          final AbstractCurveSpecification specification =
              (AbstractCurveSpecification) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, curveProperties));
          if (specification instanceof CurveSpecification) {
            n += ((CurveSpecification) specification).getNodes().size();
          } else if (specification instanceof ConstantCurveSpecification) {
            n++;
          }
        }
      }
      final IssuerProviderDiscount curveBundle = (IssuerProviderDiscount) getKnownData(inputs);
      final LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<>();
      final CurveBuildingBlockBundle exogenousJacobians = new CurveBuildingBlockBundle();
      for (final ComputedValue input : inputs.getAllValues()) {
        final String valueName = input.getSpecification().getValueName();
        if (valueName.equals(JACOBIAN_BUNDLE)) {
          exogenousJacobians.addAll((CurveBuildingBlockBundle) input.getValue());
        }
      }
      final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundles = new LinkedHashMap<>();
      for (final Map.Entry<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> entry : exogenousJacobians.getData().entrySet()) {
        unitBundles.put(entry.getKey(), entry.getValue());
      }
      int totalNodes = 0;
      for (final CurveGroupConfiguration group: _curveConstructionConfiguration.getCurveGroups()) {
        for (final Map.Entry<String, List<CurveTypeConfiguration>> entry: group.getTypesForCurves().entrySet()) {
          final String curveName = entry.getKey();
          final List<CurveTypeConfiguration> types = entry.getValue();
          final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, curveName).get();
          final AbstractCurveSpecification specification =
              (AbstractCurveSpecification) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, curveProperties));
          final DoublesCurve curve;
          int nNodesForCurve;
          double[][] jacobian;
          if (specification instanceof ConstantCurveSpecification) {
            final ConstantCurveSpecification constantCurveSpecification = (ConstantCurveSpecification) specification;
            final Double marketValue = marketData.getDataPoint(constantCurveSpecification.getIdentifier().toBundle());
            if (marketValue == null) {
              throw new OpenGammaRuntimeException("Could not get market value for " + constantCurveSpecification.getIdentifier());
            }
            curve = ConstantDoublesCurve.from(marketValue, curveName);
            nNodesForCurve = 1;
            jacobian = new double[][]{new double[] {1}};
            totalNodes++;
          } else if (specification instanceof InterpolatedCurveSpecification) {
            final InterpolatedCurveSpecification interpolatedSpecification = (InterpolatedCurveSpecification) specification;
            final double[] times = new double[n];
            final double[] yields = new double[n];
            jacobian = new double[n][n];
            int i = 0;
            nNodesForCurve = interpolatedSpecification.getNodes().size();
            for (final CurveNodeWithIdentifier node: interpolatedSpecification.getNodes()) {
              final CurveNode curveNode = node.getCurveNode();
              if (!(curveNode instanceof BondNode)) {
                throw new OpenGammaRuntimeException("Was expecting only bond nodes; have " + curveNode);
              }
              final Double marketValue = marketData.getDataPoint(node.getIdentifier());
              if (marketValue == null) {
                throw new OpenGammaRuntimeException("Could not get market value for " + node);
              }
              times[i] = getTimeToMaturity(now, node, securitySource);
              yields[i] = marketValue;
              jacobian[i][i] = 1;
              i++;
            }
            final String interpolatorName = interpolatedSpecification.getInterpolatorName();
            final String rightExtrapolatorName = interpolatedSpecification.getRightExtrapolatorName();
            final String leftExtrapolatorName = interpolatedSpecification.getLeftExtrapolatorName();
            final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
            curve = InterpolatedDoublesCurve.from(times, yields, interpolator, curveName);
          } else {
            throw new OpenGammaRuntimeException("Cannot handle curves of type " + specification.getClass());
          }
          final YieldAndDiscountCurve yieldCurve = new YieldCurve(curveName, curve);
          for (final CurveTypeConfiguration type: types) {
            if (type instanceof IssuerCurveTypeConfiguration) {
              final IssuerCurveTypeConfiguration issuer = (IssuerCurveTypeConfiguration) type;
              curveBundle.setCurve(Pairs.<Object, LegalEntityFilter<LegalEntity>>of(issuer.getKeys(), issuer.getFilters()), yieldCurve);
            } else {
              throw new OpenGammaRuntimeException("Can only handle configurations of type IssuerCurveTypeConfiguration");
            }
          }
          unitMap.put(curveName, Pairs.of(totalNodes, totalNodes + nNodesForCurve));
          unitBundles.put(curveName, Pairs.of(new CurveBuildingBlock(unitMap), new DoubleMatrix2D(jacobian)));
          totalNodes += nNodesForCurve;
        }
      }
      return Pairs.of((IssuerProviderInterface) curveBundle, new CurveBuildingBlockBundle(unitBundles));
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
      return trimmed;
    }

    @Override
    protected IssuerProviderInterface getKnownData(final FunctionInputs inputs) {
      final FXMatrix fxMatrix = new FXMatrix();
      MulticurveProviderDiscount knownData;
      if (getExogenousRequirements().isEmpty()) {
        knownData = new MulticurveProviderDiscount(fxMatrix);
      } else {
        knownData = (MulticurveProviderDiscount) inputs.getValue(ValueRequirementNames.CURVE_BUNDLE);
        knownData.setForexMatrix(fxMatrix);
      }
      return new IssuerProviderDiscount(knownData);
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
        final Pair<IssuerProviderInterface, CurveBuildingBlockBundle> pair) {
      final Set<ComputedValue> result = new HashSet<>();
      final IssuerProviderDiscount provider = (IssuerProviderDiscount) pair.getFirst();
      result.add(new ComputedValue(bundleSpec, provider));
      result.add(new ComputedValue(jacobianSpec, pair.getSecond()));
      for (final String curveName : getCurveNames()) {
        final ValueProperties curveProperties = bundleProperties.copy()
            .with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, getCurveTypeProperty())
            .with(CURVE, curveName)
            .get();
        final ValueSpecification curveSpec = new ValueSpecification(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties);
        result.add(new ComputedValue(curveSpec, provider.getIssuerCurve(curveName)));
      }
      return result;
    }

    /**
     * Gets the time to maturity of a bond node from the referenced security.
     * @param curveDate The curve date
     * @param curveNode The curve node
     * @param securitySource The security source
     * @return The time to maturity of the bond
     */
    private double getTimeToMaturity(final ZonedDateTime curveDate, final CurveNodeWithIdentifier curveNode, final SecuritySource securitySource) {
      if (curveNode.getCurveNode() instanceof BondNode) {
        final Security security = securitySource.getSingle(ExternalIdBundle.of(curveNode.getIdentifier()));
        if (security instanceof BondSecurity) {
          final ZonedDateTime maturity = ((BondSecurity) security).getLastTradeDate().getExpiry();
          return TimeCalculator.getTimeBetween(curveDate, maturity);
        }
        throw new OpenGammaRuntimeException("Cannot handle security type " + security);
      }
      throw new OpenGammaRuntimeException("Cannot handle node type " + curveNode.getCurveNode());
    }

  }
}
