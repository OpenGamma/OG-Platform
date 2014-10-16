/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;
import static com.opengamma.engine.value.ValueRequirementNames.PRICE_INDEX_CURVE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurve;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurveInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.ParSpreadInflationMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.inflationissuer.InflationIssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.analytics.curve.CashNodeConverter;
import com.opengamma.financial.analytics.curve.ConverterUtils;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeVisitorAdapter;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.FRANodeConverter;
import com.opengamma.financial.analytics.curve.FXForwardNodeConverter;
import com.opengamma.financial.analytics.curve.InflationIssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.RateFutureNodeConverter;
import com.opengamma.financial.analytics.curve.RollDateFRANodeConverter;
import com.opengamma.financial.analytics.curve.RollDateSwapNodeConverter;
import com.opengamma.financial.analytics.curve.SwapNodeConverter;
import com.opengamma.financial.analytics.curve.ZeroCouponInflationNodeConverter;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Produces price index curves using the discounting method. The object return contian issuer discount curves from the known data.
 */
public class InflationIssuerProviderDiscountingFunction extends

    MultiCurveFunction<ParameterInflationIssuerProviderInterface, InflationIssuerDiscountBuildingRepository, GeneratorPriceIndexCurve, InflationSensitivity> {

  /** The logger */

  private static final Logger s_logger = LoggerFactory.getLogger(InflationIssuerProviderDiscountingFunction.class);
  /** The calculator */

  private static final ParSpreadInflationMarketQuoteIssuerDiscountingCalculator PSIMQC = ParSpreadInflationMarketQuoteIssuerDiscountingCalculator.getInstance();
  /** The sensitivity calculator */

  private static final ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator PSIMQCSC =
      ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();

  private static final ParSpreadInflationMarketQuoteIssuerDiscountingCalculator PSIMQCWI = ParSpreadInflationMarketQuoteIssuerDiscountingCalculator.getInstance();
  /** The sensitivity calculator */

  private static final ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator PSIMQCSCWI =
      ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();

  /**
    * @param configurationName The configuration name, not null
    */

  public InflationIssuerProviderDiscountingFunction(final String configurationName) {
    super(configurationName);
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
      final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
    return new MyCompiledFunctionDefinition(earliestInvokation, latestInvokation, curveNames, exogenousRequirements, curveConstructionConfiguration);
  }

  protected InstrumentDerivativeVisitor<ParameterInflationIssuerProviderInterface, Double> getCalculatorWithoutIssuer() {
    return PSIMQCWI;
  }

  protected InstrumentDerivativeVisitor<ParameterInflationIssuerProviderInterface, InflationSensitivity> getSensitivityCalculatorWithoutIssuer() {
    return PSIMQCSCWI;
  }

  @Override
  protected InstrumentDerivativeVisitor<ParameterInflationIssuerProviderInterface, Double> getCalculator() {
    return PSIMQC;
  }

  @Override
  protected InstrumentDerivativeVisitor<ParameterInflationIssuerProviderInterface, InflationSensitivity> getSensitivityCalculator() {
    return PSIMQCSC;
  }

  @Override
  protected String getCurveTypeProperty() {
    return DISCOUNTING;
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
      super(earliestInvokation, latestInvokation, curveNames, ValueRequirementNames.PRICE_INDEX_CURVE, exogenousRequirements);
      ArgumentChecker.notNull(curveConstructionConfiguration, "curve construction configuration");
      _curveConstructionConfiguration = curveConstructionConfiguration;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Pair<ParameterInflationIssuerProviderInterface, CurveBuildingBlockBundle> getCurves(
        final FunctionInputs inputs, final ZonedDateTime now, final InflationIssuerDiscountBuildingRepository builder,
        final ParameterInflationIssuerProviderInterface knownData, final FunctionExecutionContext context, final FXMatrix fx) {
      final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
      final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(context);
      final ValueProperties curveConstructionProperties = ValueProperties.builder()
          .with(CURVE_CONSTRUCTION_CONFIG, _curveConstructionConfiguration.getName())
          .get();
      final HistoricalTimeSeriesBundle timeSeries =
          (HistoricalTimeSeriesBundle) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES,
              ComputationTargetSpecification.NULL, curveConstructionProperties));
      final int nGroups = _curveConstructionConfiguration.getCurveGroups().size();
      final MultiCurveBundle<GeneratorCurve>[] curveBundles = new MultiCurveBundle[nGroups];
      final LinkedHashMap<String, IndexPrice[]> inflationMap = new LinkedHashMap<>();
      // seasonal time step construction
      final ZonedDateTime[] seasonalityDate = ScheduleCalculator.getUnadjustedDateSchedule(now.withDayOfMonth(1), now.withDayOfMonth(1).plusYears(50), Period.ofMonths(1), true, false);
      final double[] seasonalStep = new double[seasonalityDate.length];
      for (int loopins = 0; loopins < seasonalityDate.length; loopins++) {
        seasonalStep[loopins] = TimeCalculator.getTimeBetween(now, seasonalityDate[loopins]);
      }
      //TODO comparator to sort groups by order
      int i = 0; // Implementation Note: loop on the groups
      for (final CurveGroupConfiguration group : _curveConstructionConfiguration.getCurveGroups()) { // Group - start
        int j = 0;
        final int nCurves = group.getTypesForCurves().size();
        final SingleCurveBundle<GeneratorCurve>[] singleCurves = new SingleCurveBundle[nCurves];
        for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
          final List<IndexPrice> inflation = new ArrayList<>();
          final String curveName = entry.getKey();
          final ValueProperties properties = ValueProperties.builder().with(CURVE, curveName).get();
          final CurveSpecification specification =
              (CurveSpecification) inputs.getValue(new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
          final CurveDefinition definition =
              (CurveDefinition) inputs.getValue(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties));
          final SnapshotDataBundle snapshot =
              (SnapshotDataBundle) inputs.getValue(new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
          final int nNodes = specification.getNodes().size();
          final InstrumentDerivative[] derivativesForCurve = new InstrumentDerivative[nNodes];
          final double[] parameterGuessForCurves = new double[nNodes];
          int k = 0;
          for (final CurveNodeWithIdentifier node : specification.getNodes()) { // Node points - start
            final Double marketData = snapshot.getDataPoint(node.getIdentifier());
            if (marketData == null) {
              throw new OpenGammaRuntimeException("Could not get market data for " + node.getIdentifier());
            }
            final InstrumentDefinition<?> definitionForNode = node.getCurveNode().accept(getCurveNodeConverter(context,
                snapshot, node.getIdentifier(), timeSeries, now, fx));
            // Construction of the first guess for the root finder
            final SwapFixedInflationZeroCouponDefinition swap = (SwapFixedInflationZeroCouponDefinition) definitionForNode;
            final CouponInflationDefinition couponInflation = (CouponInflationDefinition) swap.getSecondLeg().getNthPayment(swap.getSecondLeg().getNumberOfPayments() - 1);
            final CouponFixedCompoundingDefinition couponFix = (CouponFixedCompoundingDefinition) swap.getFirstLeg().getNthPayment(swap.getFirstLeg().getNumberOfPayments() - 1);
            if (couponInflation instanceof CouponInflationZeroCouponInterpolationDefinition) {
              parameterGuessForCurves[k] = 100.0 * Math.pow((1 + marketData), couponFix.getPaymentAccrualFactors().length);
            } else {
              parameterGuessForCurves[k] = 100.0 * Math.pow((1 + marketData), couponFix.getPaymentAccrualFactors().length);
            }
            derivativesForCurve[k++] = getCurveNodeConverter(conventionSource).getDerivative(node, definitionForNode, now, timeSeries);
          } // Node points - end
          for (final CurveTypeConfiguration type : entry.getValue()) { // Type - start
            if (type instanceof InflationIssuerCurveTypeConfiguration) {
              final InflationIssuerCurveTypeConfiguration inflationConfiguration = (InflationIssuerCurveTypeConfiguration) type;
              final Security sec = securitySource.getSingle(inflationConfiguration.getPriceIndex().toBundle());
              if (sec == null) {
                throw new OpenGammaRuntimeException("CurveNodeCurrencyVisitor.visitInflationLegConvention: index with id " + inflationConfiguration.getPriceIndex()
                    + " was null");
              }
              if (!(sec instanceof PriceIndex)) {
                throw new OpenGammaRuntimeException("CurveNodeCurrencyVisitor.visitInflationLegConvention: index with id " + inflationConfiguration.getPriceIndex()
                    + " not of type PriceIndex");
              }
              final PriceIndex indexSecurity = (PriceIndex) sec;
              final PriceIndexConvention priceIndexConvention = conventionSource.getSingle(indexSecurity.getConventionId(), PriceIndexConvention.class);
              if (priceIndexConvention == null) {
                throw new OpenGammaRuntimeException("CurveNodeCurrencyVisitor.visitInflationLegConvention: Convention with id " + indexSecurity.getConventionId() + " was null");
              }
              inflation.add(ConverterUtils.indexPrice(indexSecurity.getName(), priceIndexConvention));
            } else {
              throw new OpenGammaRuntimeException("Cannot handle " + type.getClass());
            }
          } // type - end
          if (!inflation.isEmpty()) {
            inflationMap.put(curveName, inflation.toArray(new IndexPrice[inflation.size()]));
          }
          final GeneratorCurve generator = getGenerator(definition, now.toLocalDate());
          singleCurves[j++] = new SingleCurveBundle<>(curveName, derivativesForCurve, generator.initialGuess(parameterGuessForCurves), generator);
          // seasonal curve construction
          // TODO : inputs () should be retrieve from historical data, for this we need two things :
          // 1) historical value of the price index (this can be retrieve from bloomberg using the appropriate ticker)
          // 2) A statistical treatment on this data should be done, usually a kind of specific ARIMA.
        }
        final MultiCurveBundle<GeneratorCurve> groupBundle = new MultiCurveBundle<>(singleCurves);
        curveBundles[i++] = groupBundle;
      } // Group - end
      //TODO this is only in here because the code in analytics doesn't use generics properly
      final CurveBuildingBlockBundle knownbundle = getKnownBundle(inputs);
      final Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> temp = builder.makeCurvesFromDerivatives(
          curveBundles, (InflationIssuerProviderDiscount) knownData, knownbundle, inflationMap, 
          getCalculatorWithoutIssuer(), getSensitivityCalculatorWithoutIssuer());
      final Pair<ParameterInflationIssuerProviderInterface, CurveBuildingBlockBundle> result = 
          Pairs.of((ParameterInflationIssuerProviderInterface) temp.getFirst(), temp.getSecond());
      return result;
    }

    @Override
    protected InflationIssuerProviderInterface getKnownData(final FunctionInputs inputs) {
      final FXMatrix fxMatrix = (FXMatrix) inputs.getValue(ValueRequirementNames.FX_MATRIX);
      //TODO requires that the discounting curves are supplied externally
      InflationIssuerProviderDiscount knownData;
      if (getExogenousRequirements().isEmpty()) {
        knownData = new InflationIssuerProviderDiscount(fxMatrix);
      } else {
        knownData = new InflationIssuerProviderDiscount((IssuerProviderDiscount) inputs.getValue(ValueRequirementNames.CURVE_BUNDLE));
        knownData.getMulticurveProvider().setForexMatrix(fxMatrix);
      }
      return knownData;
    }

    protected CurveBuildingBlockBundle getKnownBundle(final FunctionInputs inputs) {
      //TODO requires that the discounting curves are supplied externally
      CurveBuildingBlockBundle knownBundle;
      if (getExogenousRequirements().isEmpty()) {
        knownBundle = new CurveBuildingBlockBundle();
      } else {
        knownBundle = (CurveBuildingBlockBundle) inputs.getValue(ValueRequirementNames.JACOBIAN_BUNDLE);
      }
      return knownBundle;
    }

    @Override
    public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
      return getResults(context, target);
    }

    @Override
    protected InflationIssuerDiscountBuildingRepository getBuilder(final double absoluteTolerance, final double relativeTolerance, final int maxIterations) {
      return new InflationIssuerDiscountBuildingRepository(absoluteTolerance, relativeTolerance, maxIterations);
    }

    @Override
    protected GeneratorPriceIndexCurve getGenerator(final CurveDefinition definition, final LocalDate valuationDate) {
      if (definition instanceof InterpolatedCurveDefinition) {
        final InterpolatedCurveDefinition interpolatedDefinition = (InterpolatedCurveDefinition) definition;
        final String interpolatorName = interpolatedDefinition.getInterpolatorName();
        final String leftExtrapolatorName = interpolatedDefinition.getLeftExtrapolatorName();
        final String rightExtrapolatorName = interpolatedDefinition.getRightExtrapolatorName();
        final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        return new GeneratorPriceIndexCurveInterpolated(getMaturityCalculator(), interpolator);
      }
      throw new OpenGammaRuntimeException("Cannot handle curves of type " + definition.getClass());
    }

    @Override
    protected CurveNodeVisitor<InstrumentDefinition<?>> getCurveNodeConverter(final FunctionExecutionContext context,
        final SnapshotDataBundle marketData, final ExternalId dataId, final HistoricalTimeSeriesBundle historicalData,
        final ZonedDateTime valuationTime, final FXMatrix fx) {
      final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
      final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(context);
      final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
      final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
      return CurveNodeVisitorAdapter.<InstrumentDefinition<?>>builder()
          .cashNodeVisitor(new CashNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .fraNode(new FRANodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .fxForwardNode(new FXForwardNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .immFRANode(new RollDateFRANodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .immSwapNode(new RollDateSwapNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .rateFutureNode(new RateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .swapNode(new SwapNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime, fx))
          .zeroCouponInflationNode(new ZeroCouponInflationNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime, historicalData))
          .create();
    }

    @Override
    protected Set<ComputedValue> getResults(final ValueSpecification bundleSpec, final ValueSpecification jacobianSpec,
        final ValueProperties bundleProperties, final Pair<ParameterInflationIssuerProviderInterface, CurveBuildingBlockBundle> pair) {
      final Set<ComputedValue> result = new HashSet<>();
      final InflationIssuerProviderDiscount provider = (InflationIssuerProviderDiscount) pair.getFirst();
      result.add(new ComputedValue(bundleSpec, provider));
      result.add(new ComputedValue(jacobianSpec, pair.getSecond()));
      for (final String curveName : getCurveNames()) {
        final ValueProperties curveProperties = bundleProperties.copy()
            .withoutAny(CURVE)
            .with(CURVE, curveName)
            .get();
        final PriceIndexCurve curve = provider.getInflationProvider().getCurve(curveName);
        if (curve == null) {
          s_logger.error("Could not get curve called {} from configuration {}", curveName, getCurveConstructionConfigurationName());
        } else {
          final ValueSpecification curveSpec = new ValueSpecification(PRICE_INDEX_CURVE, ComputationTargetSpecification.NULL, curveProperties);
          result.add(new ComputedValue(curveSpec, curve));
        }
      }
      return result;
    }
  }

}
