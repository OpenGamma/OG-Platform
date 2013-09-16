/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolatedAnchorNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
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
import com.opengamma.financial.analytics.curve.CashNodeConverter;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeVisitorAdapter;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.FRANodeConverter;
import com.opengamma.financial.analytics.curve.FXForwardNodeConverter;
import com.opengamma.financial.analytics.curve.FixedDateInterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.RateFutureNodeConverter;
import com.opengamma.financial.analytics.curve.SwapNodeConverter;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Produces yield curves using the discounting method.
 */
public class MultiCurveDiscountingFunction extends
  MultiCurveFunction<MulticurveProviderInterface, MulticurveDiscountBuildingRepository, GeneratorYDCurve, MulticurveSensitivity> {
  /** The calculator */
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  /** The sensitivity calculator */
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  /**
   * @param configurationName The configuration name, not null
   */
  public MultiCurveDiscountingFunction(final String configurationName) {
    super(configurationName);
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
      final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
    return new MyCompiledFunctionDefinition(earliestInvokation, latestInvokation, curveNames, exogenousRequirements, curveConstructionConfiguration);
  }

  /**
   * Compiled function implementation.
   */
  protected class MyCompiledFunctionDefinition extends CurveCompiledFunctionDefinition {
    /** The curve construction configuration */
    private final CurveConstructionConfiguration _curveConstructionConfiguration;

    /**
     * @param earliestInvokation The earliest time for which this function is valid, null if there is no bound
     * @param latestInvokation The latest time for which this function is valid, null if there is no bound
     * @param curveNames The names of the curves produced by this function, not null
     * @param exogenousRequirements The exogenous requirements, not null
     * @param curveConstructionConfiguration The curve construction configuration, not null
     */
    protected MyCompiledFunctionDefinition(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
        final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
      super(earliestInvokation, latestInvokation, curveNames, ValueRequirementNames.YIELD_CURVE, exogenousRequirements);
      ArgumentChecker.notNull(curveConstructionConfiguration, "curve construction configuration");
      _curveConstructionConfiguration = curveConstructionConfiguration;
    }

    @Override
    @SuppressWarnings("synthetic-access")
    protected Pair<MulticurveProviderInterface, CurveBuildingBlockBundle> getCurves(final FunctionInputs inputs, final ZonedDateTime now,
        final MulticurveDiscountBuildingRepository builder, final MulticurveProviderInterface knownData, final ConventionSource conventionSource,
        final HolidaySource holidaySource, final RegionSource regionSource) {
      final ValueProperties curveConstructionProperties = ValueProperties.builder()
          .with(CURVE_CONSTRUCTION_CONFIG, _curveConstructionConfiguration.getName())
          .get();
      final HistoricalTimeSeriesBundle timeSeries =
          (HistoricalTimeSeriesBundle) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES,
              ComputationTargetSpecification.NULL, curveConstructionProperties));
      final int nGroups = _curveConstructionConfiguration.getCurveGroups().size();
      @SuppressWarnings("unchecked")
      final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nGroups];
      final LinkedHashMap<String, Currency> discountingMap = new LinkedHashMap<>();
      final LinkedHashMap<String, IborIndex[]> forwardIborMap = new LinkedHashMap<>();
      final LinkedHashMap<String, IndexON[]> forwardONMap = new LinkedHashMap<>();
      //TODO comparator to sort groups by order
      int i = 0; // Implementation Note: loop on the groups
      for (final CurveGroupConfiguration group : _curveConstructionConfiguration.getCurveGroups()) { // Group - start
        final int nCurves = group.getTypesForCurves().size();
        @SuppressWarnings("unchecked")
        final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
        int j = 0;
        for (final Map.Entry<String, List<CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
          final List<IborIndex> iborIndex = new ArrayList<>();
          final List<IndexON> overnightIndex = new ArrayList<>();
          final String curveName = entry.getKey();
          final ValueProperties properties = ValueProperties.builder().with(CURVE, curveName).get();
          final CurveSpecification specification =
              (CurveSpecification) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
          final CurveDefinition definition =
              (CurveDefinition) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties));
          final SnapshotDataBundle snapshot =
              (SnapshotDataBundle) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
          final int nNodes = specification.getNodes().size();
          final InstrumentDerivative[] derivativesForCurve = new InstrumentDerivative[nNodes];
          final double[] parameterGuessForCurves = new double[nNodes];
          int k = 0;
          for (final CurveNodeWithIdentifier node : specification.getNodes()) { // Node points - start
            final Double marketData = snapshot.getDataPoint(node.getIdentifier());
            if (marketData == null) {
              throw new OpenGammaRuntimeException("Could not get market data for " + node.getIdentifier());
            }
            parameterGuessForCurves[k] = 0.02; // For FX forward, the FX rate is not a good initial guess. // TODO: change this // marketData
            final InstrumentDefinition<?> definitionForNode = node.getCurveNode().accept(getCurveNodeConverter(conventionSource, holidaySource, regionSource,
                snapshot, node.getIdentifier(), timeSeries, now));
            derivativesForCurve[k++] = getCurveNodeConverter(conventionSource).getDerivative(node, definitionForNode, now, timeSeries);
          } // Node points - end
          for (final CurveTypeConfiguration type : entry.getValue()) { // Type - start
            if (type instanceof DiscountingCurveTypeConfiguration) {
              final String reference = ((DiscountingCurveTypeConfiguration) type).getReference();
              try {
                final Currency currency = Currency.of(reference);
                //should this map check that the curve name has not already been entered?
                discountingMap.put(curveName, currency);
              } catch (final IllegalArgumentException e) {
                throw new OpenGammaRuntimeException("Cannot handle reference type " + reference + " for discounting curves");
              }
            } else if (type instanceof IborCurveTypeConfiguration) {
              final IborCurveTypeConfiguration ibor = (IborCurveTypeConfiguration) type;
              final IborIndexConvention iborIndexConvention = conventionSource.getConvention(IborIndexConvention.class, ibor.getConvention());
              if (iborIndexConvention == null) {
                throw new OpenGammaRuntimeException("Ibor index convention called " + ibor.getConvention() + " was null");
              }
              final int spotLag = iborIndexConvention.getSettlementDays();
              iborIndex.add(new IborIndex(iborIndexConvention.getCurrency(), ibor.getTenor().getPeriod(), spotLag, iborIndexConvention.getDayCount(),
                  iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(), iborIndexConvention.getName()));
            } else if (type instanceof OvernightCurveTypeConfiguration) {
              final OvernightCurveTypeConfiguration overnight = (OvernightCurveTypeConfiguration) type;
              final OvernightIndexConvention overnightConvention = conventionSource.getConvention(OvernightIndexConvention.class, overnight.getConvention());
              if (overnightConvention == null) {
                throw new OpenGammaRuntimeException("Overnight convention called " + overnight.getConvention() + " was null");
              }
              overnightIndex.add(new IndexON(overnightConvention.getName(), overnightConvention.getCurrency(), overnightConvention.getDayCount(), overnightConvention.getPublicationLag()));
            } else {
              throw new OpenGammaRuntimeException("Cannot handle " + type.getClass());
            }
          } // type - end
          if (!iborIndex.isEmpty()) {
            forwardIborMap.put(curveName, iborIndex.toArray(new IborIndex[iborIndex.size()]));
          }
          if (!overnightIndex.isEmpty()) {
            forwardONMap.put(curveName, overnightIndex.toArray(new IndexON[overnightIndex.size()]));
          }
          final GeneratorYDCurve generator = getGenerator(definition, now.toLocalDate());
          singleCurves[j++] = new SingleCurveBundle<>(curveName, derivativesForCurve, generator.initialGuess(parameterGuessForCurves), generator);
        }
        final MultiCurveBundle<GeneratorYDCurve> groupBundle = new MultiCurveBundle<>(singleCurves);
        curveBundles[i++] = groupBundle;
      } // Group - end
      //TODO this is only in here because the code in analytics doesn't use generics properly
      final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> temp = builder.makeCurvesFromDerivatives(curveBundles,
          (MulticurveProviderDiscount) knownData, discountingMap, forwardIborMap, forwardONMap, getCalculator(), getSensitivityCalculator());
      final Pair<MulticurveProviderInterface, CurveBuildingBlockBundle> result = Pair.of((MulticurveProviderInterface) temp.getFirst(), temp.getSecond());
      return result;
    }

    @Override
    protected InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> getCalculator() {
      return PSMQC;
    }

    @Override
    protected InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> getSensitivityCalculator() {
      return PSMQCSC;
    }

    @Override
    protected String getCurveTypeProperty() {
      return DISCOUNTING;
    }

    @Override
    protected MulticurveProviderInterface getKnownData(final FunctionInputs inputs) {
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
    protected MulticurveDiscountBuildingRepository getBuilder(final double absoluteTolerance, final double relativeTolerance, final int maxIterations) {
      return new MulticurveDiscountBuildingRepository(absoluteTolerance, relativeTolerance, maxIterations);
    }

    @Override
    protected GeneratorYDCurve getGenerator(final CurveDefinition definition, final LocalDate valuationDate) {
      if (definition instanceof InterpolatedCurveDefinition) {
        final InterpolatedCurveDefinition interpolatedDefinition = (InterpolatedCurveDefinition) definition;
        final String interpolatorName = interpolatedDefinition.getInterpolatorName();
        final String leftExtrapolatorName = interpolatedDefinition.getLeftExtrapolatorName();
        final String rightExtrapolatorName = interpolatedDefinition.getRightExtrapolatorName();
        final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        if (definition instanceof FixedDateInterpolatedCurveDefinition) {
          final FixedDateInterpolatedCurveDefinition fixedDateDefinition = (FixedDateInterpolatedCurveDefinition) definition;
          final List<LocalDate> fixedDates = fixedDateDefinition.getFixedDates();
          final DoubleArrayList nodePoints = new DoubleArrayList(fixedDates.size()); //TODO what about equal node points?
          for (final LocalDate fixedDate : fixedDates) {
            nodePoints.add(TimeCalculator.getTimeBetween(valuationDate, fixedDate)); //TODO what to do if the fixed date is before the valuation date?
          }
          final double anchor = nodePoints.get(0); //TODO should the anchor go into the definition?
          return new GeneratorCurveYieldInterpolatedAnchorNode(nodePoints.toDoubleArray(), anchor, interpolator);
        }
        return new GeneratorCurveYieldInterpolated(getMaturityCalculator(), interpolator);
      }
      throw new OpenGammaRuntimeException("Cannot handle curves of type " + definition.getClass());
    }

    @Override
    protected CurveNodeVisitor<InstrumentDefinition<?>> getCurveNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource,
        final RegionSource regionSource, final SnapshotDataBundle marketData, final ExternalId dataId, final HistoricalTimeSeriesBundle historicalData,
        final ZonedDateTime valuationTime) {
      return CurveNodeVisitorAdapter.<InstrumentDefinition<?>>builder()
          .cashNodeVisitor(new CashNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .fraNode(new FRANodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .fxForwardNode(new FXForwardNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .rateFutureNode(new RateFutureNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .swapNode(new SwapNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .create();
    }

    @Override
    protected Set<ComputedValue> getResults(final ValueSpecification bundleSpec, final ValueSpecification jacobianSpec,
        final ValueProperties bundleProperties, final Pair<MulticurveProviderInterface, CurveBuildingBlockBundle> pair) {
      final Set<ComputedValue> result = new HashSet<>();
      final MulticurveProviderDiscount provider = (MulticurveProviderDiscount) pair.getFirst();
      result.add(new ComputedValue(bundleSpec, provider));
      result.add(new ComputedValue(jacobianSpec, pair.getSecond()));
      for (final String curveName : getCurveNames()) {
        final ValueProperties curveProperties = bundleProperties.copy()
            .with(CURVE, curveName)
            .get();
        final ValueSpecification curveSpec = new ValueSpecification(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties);
        result.add(new ComputedValue(curveSpec, provider.getCurve(curveName)));
      }
      return result;
    }
  }
}
