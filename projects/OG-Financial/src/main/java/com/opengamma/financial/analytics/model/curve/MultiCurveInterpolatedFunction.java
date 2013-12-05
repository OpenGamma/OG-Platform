/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveSpecification;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Produces yield curves using the 'Interpolated' method.
 */
public class MultiCurveInterpolatedFunction extends
  MultiCurveFunction<MulticurveProviderInterface, MulticurveDiscountBuildingRepository, GeneratorYDCurve, MulticurveSensitivity> {

  public MultiCurveInterpolatedFunction(String curveConfigurationName) {
    super(curveConfigurationName);
  }
  
  @Override
  protected String getCurveTypeProperty() {
    return InterpolatedDataProperties.CALCULATION_METHOD_NAME;
  }

  @Override
  protected InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> getCalculator() {
    return null;
  }

  @Override
  protected InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> getSensitivityCalculator() {
    return null;
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(ZonedDateTime earliestInvocation, ZonedDateTime latestInvocation, String[] curveNames, Set<ValueRequirement> exogenousRequirements,
      CurveConstructionConfiguration curveConstructionConfiguration) {
    return new MultiCurveInterpolatedCompiledFunctionDefinition(earliestInvocation, latestInvocation, curveNames, exogenousRequirements, curveConstructionConfiguration);
  }
  
  /**
   * Compiled function implementation.
   */
  protected class MultiCurveInterpolatedCompiledFunctionDefinition extends CurveCompiledFunctionDefinition {
    /** The curve construction configuration */
    private final CurveConstructionConfiguration _curveConstructionConfiguration;
    
    protected MultiCurveInterpolatedCompiledFunctionDefinition(
        ZonedDateTime earliestInvocation,
        ZonedDateTime latestInvocation,
        String[] curveNames,
        Set<ValueRequirement> exogenousRequirements,
        CurveConstructionConfiguration curveConstructionConfiguration) {
      super(earliestInvocation, latestInvocation, curveNames, ValueRequirementNames.YIELD_CURVE, exogenousRequirements);
      ArgumentChecker.notNull(curveConstructionConfiguration, "curve construction configuration");
      _curveConstructionConfiguration = curveConstructionConfiguration;
    }
    
    @Override
    protected Pair<MulticurveProviderInterface, CurveBuildingBlockBundle> getCurves(FunctionInputs inputs, ZonedDateTime now, MulticurveDiscountBuildingRepository builder,
        MulticurveProviderInterface knownData, ConventionSource conventionSource, HolidaySource holidaySource, RegionSource regionSource, FXMatrix fx) {
      

      final Object dataObject = inputs.getValue(ValueRequirementNames.CURVE_MARKET_DATA);
      if (dataObject == null) {
        throw new OpenGammaRuntimeException("Could not get yield curve data");
      }
      final SnapshotDataBundle marketData = (SnapshotDataBundle) dataObject;
      final int n = marketData.size();
      
      MulticurveProviderDiscount curveBundle = (MulticurveProviderDiscount) getKnownData(inputs);
      
      for (CurveGroupConfiguration group: _curveConstructionConfiguration.getCurveGroups()) {
        
        for (Map.Entry<String, List<CurveTypeConfiguration>> entry: group.getTypesForCurves().entrySet()) {

          String curveName = entry.getKey();
          List<CurveTypeConfiguration> types = entry.getValue();
          
          ValueProperties curveProperties = ValueProperties.builder().with(CURVE, curveName).get();
          
          InterpolatedCurveSpecification specification =
              (InterpolatedCurveSpecification) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, curveProperties));

          final double[] times = new double[n];
          final double[] yields = new double[n];
          final double[][] jacobian = new double[n][n];
          boolean isYield = false;
          int i = 0;
          for (CurveNodeWithIdentifier node: specification.getNodes()) {
            if (node.getCurveNode() instanceof ContinuouslyCompoundedRateNode) {
              if (i == 0) {
                isYield = true;
              } else {
                if (!isYield) {
                  throw new OpenGammaRuntimeException("Was expecting only continuously-compounded rate nodes; have " + node.getCurveNode());
                }
              }
            } else if (node.getCurveNode() instanceof DiscountFactorNode) {
              if (i == 0) {
                isYield = false;
              } else {
                if (isYield) {
                  throw new OpenGammaRuntimeException("Was expecting only discount factor nodes; have " + node.getCurveNode());
                }
              }
            } else {
              throw new OpenGammaRuntimeException("Can only handle discount factor or continuously-compounded rate nodes; have " + node.getCurveNode());
            }
            final Double marketValue = marketData.getDataPoint(node.getIdentifier());
            final Tenor maturity = node.getCurveNode().getResolvedMaturity();
            times[i] = DateUtils.estimatedDuration(maturity.getPeriod()).toDays() / 365.0; //TODO check if this is correct
            yields[i] = marketValue;
            jacobian[i][i] = 1;
            i++;
          }
          final String interpolatorName = specification.getInterpolatorName();
          final String rightExtrapolatorName = specification.getRightExtrapolatorName();
          final String leftExtrapolatorName = specification.getLeftExtrapolatorName();
          final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
          final InterpolatedDoublesCurve rawCurve = InterpolatedDoublesCurve.from(times, yields, interpolator, curveName);
          YieldAndDiscountCurve discountCurve = isYield ? new YieldCurve(curveName, rawCurve) : new DiscountCurve(curveName, rawCurve);
          
          for (CurveTypeConfiguration type: types) {
            if (type instanceof DiscountingCurveTypeConfiguration) {
              Currency currency = Currency.parse(((DiscountingCurveTypeConfiguration) type).getReference());
              curveBundle.setCurve(currency, discountCurve);
            } else if (type instanceof IborCurveTypeConfiguration) {
              IborIndexConvention iborIndexConvention = conventionSource.getSingle(((IborCurveTypeConfiguration) type).getConvention(), IborIndexConvention.class);
              Tenor iborIndexTenor = ((IborCurveTypeConfiguration) type).getTenor();

              int spotLag = iborIndexConvention.getSettlementDays();
              IborIndex index = new IborIndex(iborIndexConvention.getCurrency(), iborIndexTenor.getPeriod(), spotLag, iborIndexConvention.getDayCount(),
                  iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
              curveBundle.setCurve(index, discountCurve);
            }
          }
        }
      }
      return Pairs.of((MulticurveProviderInterface) curveBundle, new CurveBuildingBlockBundle());
    }

    @Override
    protected MulticurveProviderInterface getKnownData(FunctionInputs inputs) {
      final FXMatrix fxMatrix = (FXMatrix) inputs.getValue(ValueRequirementNames.FX_MATRIX);
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
    protected MulticurveDiscountBuildingRepository getBuilder(double absoluteTolerance, double relativeTolerance, int maxIterations) {
      // No building is done.
      return null;
    }

    @Override
    protected GeneratorYDCurve getGenerator(CurveDefinition definition, LocalDate valuationDate) {
      // unused
      return null;
    }

    @Override
    protected CurveNodeVisitor<InstrumentDefinition<?>> getCurveNodeConverter(ConventionSource conventionSource, HolidaySource holidaySource, RegionSource regionSource, SnapshotDataBundle marketData,
        ExternalId dataId, HistoricalTimeSeriesBundle historicalData, ZonedDateTime valuationTime, FXMatrix fxMatrix) {
      // No need to convert to InstrumentDefinition if we are not fitting the curve.
      return null;
    }

    @Override
    protected Set<ComputedValue> getResults(ValueSpecification bundleSpec, ValueSpecification jacobianSpec, ValueProperties bundleProperties,
        Pair<MulticurveProviderInterface, CurveBuildingBlockBundle> pair) {
      final Set<ComputedValue> result = new HashSet<>();
      final MulticurveProviderDiscount provider = (MulticurveProviderDiscount) pair.getFirst();
      result.add(new ComputedValue(bundleSpec, provider));
      result.add(new ComputedValue(jacobianSpec, pair.getSecond()));
      for (final String curveName : getCurveNames()) {
        final ValueProperties curveProperties = bundleProperties.copy()
            .with(CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE, getCurveTypeProperty())
            .with(CURVE, curveName)
            .get();
        final ValueSpecification curveSpec = new ValueSpecification(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties);
        result.add(new ComputedValue(curveSpec, provider.getCurve(curveName)));
      }
      return result;
    }
  }
}
