/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_SENSITIVITY_CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.HULL_WHITE_DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_HULL_WHITE_CURRENCY;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_HULL_WHITE_PARAMETERS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.hullwhite.HullWhiteProviderDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
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
import com.opengamma.financial.analytics.curve.DeliverableSwapFutureNodeConverter;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.FRANodeConverter;
import com.opengamma.financial.analytics.curve.FXForwardNodeConverter;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.RateFutureNodeConverter;
import com.opengamma.financial.analytics.curve.RollDateFRANodeConverter;
import com.opengamma.financial.analytics.curve.RollDateSwapNodeConverter;
import com.opengamma.financial.analytics.curve.SwapNodeConverter;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Produces yield curves using the Hull-White one-factor discounting method.
 */
public class HullWhiteOneFactorDiscountingCurveFunction extends
  MultiCurveFunction<HullWhiteOneFactorProviderInterface, HullWhiteProviderDiscountBuildingRepository, GeneratorYDCurve, MulticurveSensitivity> {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(HullWhiteOneFactorDiscountingCurveFunction.class);
  /** The calculator */
  private static final ParSpreadMarketQuoteHullWhiteCalculator PSMQHWC = ParSpreadMarketQuoteHullWhiteCalculator.getInstance();
  /** The sensitivity calculator */
  private static final ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator PSMQCSHWC = ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator.getInstance();

  /**
   * @param configurationName The configuration name, not null
   */
  public HullWhiteOneFactorDiscountingCurveFunction(final String configurationName) {
    super(configurationName);
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
      final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
    return new MyCompiledFunctionDefinition(earliestInvokation, latestInvokation, curveNames, exogenousRequirements, curveConstructionConfiguration);
  }

  public CompiledFunctionDefinition getCompiledFunction(ZonedDateTime earliestInvokation, ZonedDateTime latestInvokation, String[] curveNames,
                                                        Set<ValueRequirement> exogenousRequirements, CurveConstructionConfiguration curveConstructionConfiguration,
                                                        String[] currencies) {
    return new MyCompiledFunctionDefinition(earliestInvokation, latestInvokation, curveNames, exogenousRequirements, curveConstructionConfiguration, currencies);
  }

  @Override
  protected InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> getCalculator() {
    return PSMQHWC;
  }

  @Override
  protected InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> getSensitivityCalculator() {
    return PSMQCSHWC;
  }

  @Override
  protected String getCurveTypeProperty() {
    return HULL_WHITE_DISCOUNTING;
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

    /**
     * @param earliestInvokation The earliest time for which this function is valid, null if there is no bound
     * @param latestInvokation The latest time for which this function is valid, null if there is no bound
     * @param curveNames The names of the curves produced by this function, not null
     * @param exogenousRequirements The exogenous requirements, not null
     * @param curveConstructionConfiguration The curve construction configuration, not null
     * @param currencies The set of currencies to which the curves produce sensitivities
     */
    protected MyCompiledFunctionDefinition(ZonedDateTime earliestInvokation,
                                        ZonedDateTime latestInvokation,
                                        String[] curveNames,
                                        Set<ValueRequirement> exogenousRequirements,
                                        CurveConstructionConfiguration curveConstructionConfiguration,
                                        String[] currencies) {

      super(earliestInvokation, latestInvokation, curveNames, ValueRequirementNames.YIELD_CURVE, exogenousRequirements, currencies);
      ArgumentChecker.notNull(curveConstructionConfiguration, "curve construction configuration");
      _curveConstructionConfiguration = curveConstructionConfiguration;
    }

    @Override
    protected Pair<HullWhiteOneFactorProviderInterface, CurveBuildingBlockBundle> getCurves(final FunctionInputs inputs, final ZonedDateTime now,
        final HullWhiteProviderDiscountBuildingRepository builder, final HullWhiteOneFactorProviderInterface knownData,
        final FunctionExecutionContext context, final FXMatrix fx) {
      final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
      final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(context);
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
        int j = 0;
        final int nCurves = group.getTypesForCurves().size();
        @SuppressWarnings("unchecked")
        final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
        for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : group.getTypesForCurves().entrySet()) {
          final List<IborIndex> iborIndexList = new ArrayList<>();
          final List<IndexON> overnightIndexList = new ArrayList<>();
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
            Double marketData = snapshot.getDataPoint(node.getIdentifier());
            if (marketData == null) {
              marketData = 0.99;
            }
            if (node.getCurveNode() instanceof RateFutureNode) {
              parameterGuessForCurves[k] = (1 - marketData);
            } else {
              if (node.getCurveNode() instanceof DeliverableSwapFutureNode) {
                parameterGuessForCurves[k] = 0.01d; // Implementation note: The relation between price, coupon and rate is complex. There is no good initial guess.
              } else {
                parameterGuessForCurves[k] = marketData;
              }
            }
            final InstrumentDefinition<?> definitionForNode = node.getCurveNode().accept(getCurveNodeConverter(context, snapshot,
                node.getIdentifier(), timeSeries, now, fx));
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
              final Security sec = securitySource.getSingle(ibor.getConvention().toBundle()); 
              final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
              final IborIndexConvention indexConvention = conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
              iborIndexList.add(ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor()));
            } else if (type instanceof OvernightCurveTypeConfiguration) {
              final OvernightCurveTypeConfiguration overnight = (OvernightCurveTypeConfiguration) type;
              final OvernightIndex overnightIndex = (OvernightIndex) securitySource.getSingle(overnight.getConvention().toBundle());
              final OvernightIndexConvention overnightConvention = conventionSource.getSingle(overnightIndex.getConventionId(), OvernightIndexConvention.class);
              overnightIndexList.add(ConverterUtils.indexON(overnightIndex.getName(), overnightConvention));
            } else {
              throw new OpenGammaRuntimeException("Cannot handle " + type.getClass());
            }
          } // type - end
          if (!iborIndexList.isEmpty()) {
            forwardIborMap.put(curveName, iborIndexList.toArray(new IborIndex[iborIndexList.size()]));
          }
          if (!overnightIndexList.isEmpty()) {
            forwardONMap.put(curveName, overnightIndexList.toArray(new IndexON[overnightIndexList.size()]));
          }
          final GeneratorYDCurve generator = getGenerator(definition, now.toLocalDate());
          singleCurves[j++] = new SingleCurveBundle<>(curveName, derivativesForCurve, generator.initialGuess(parameterGuessForCurves), generator);
        }
        final MultiCurveBundle<GeneratorYDCurve> groupBundle = new MultiCurveBundle<>(singleCurves);
        curveBundles[i++] = groupBundle;
      } // Group - end
      //TODO this is only in here because the code in analytics doesn't use generics properly
      final Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> temp = builder.makeCurvesFromDerivatives(curveBundles,
          (HullWhiteOneFactorProviderDiscount) knownData, discountingMap, forwardIborMap, forwardONMap, getCalculator(), getSensitivityCalculator());
      final Pair<HullWhiteOneFactorProviderInterface, CurveBuildingBlockBundle> result = Pairs.of((HullWhiteOneFactorProviderInterface) temp.getFirst(), temp.getSecond());
      return result;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> requirements = super.getRequirements(compilationContext, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> hwPropertyNames = constraints.getValues(PROPERTY_HULL_WHITE_PARAMETERS);
      if (hwPropertyNames == null || hwPropertyNames.size() != 1) {
        return null;
      }
      final Set<String> hwCurrencies = constraints.getValues(PROPERTY_HULL_WHITE_CURRENCY);
      if (hwCurrencies == null || hwCurrencies.size() != 1) {
        return null;
      }
      final Currency hwCurrency = Currency.of(Iterables.getOnlyElement(hwCurrencies));
      final ValueProperties hwProperties = ValueProperties.builder()
          .with(PROPERTY_HULL_WHITE_PARAMETERS, hwPropertyNames)
          .with(PROPERTY_HULL_WHITE_CURRENCY, hwCurrencies)
          .get();
      requirements.add(new ValueRequirement(ValueRequirementNames.HULL_WHITE_ONE_FACTOR_PARAMETERS, ComputationTargetSpecification.of(hwCurrency), hwProperties));
      return requirements;
    }

    @Override
    protected ValueProperties getCurveProperties(final String curveName) {
      return super.getCurveProperties(curveName).copy()
          .withAny(PROPERTY_HULL_WHITE_PARAMETERS)
          .withAny(PROPERTY_HULL_WHITE_CURRENCY)
          .get();
    }

    @Override
    protected ValueProperties getBundleProperties(final String[] curveNames) {
      return super.getBundleProperties(curveNames).copy()
          .withAny(PROPERTY_HULL_WHITE_PARAMETERS)
          .withAny(PROPERTY_HULL_WHITE_CURRENCY)
          .get();
    }

    @Override
    protected HullWhiteOneFactorProviderInterface getKnownData(final FunctionInputs inputs) {
      final HullWhiteOneFactorPiecewiseConstantParameters modelParameters = (HullWhiteOneFactorPiecewiseConstantParameters) inputs.getValue(ValueRequirementNames.HULL_WHITE_ONE_FACTOR_PARAMETERS);
      if (modelParameters == null) {
        throw new OpenGammaRuntimeException("Could not get the Hull-White model parameters");
      }
      Currency currency = null;
      for (final ComputedValue input : inputs.getAllValues()) {
        if (input.getSpecification().getValueName().equals(ValueRequirementNames.HULL_WHITE_ONE_FACTOR_PARAMETERS)) {
          currency = Currency.of(input.getSpecification().getProperty(PROPERTY_HULL_WHITE_CURRENCY));
          break;
        }
      }
      if (currency == null) {
        throw new OpenGammaRuntimeException("Could not get the currency for this set of Hull-White one factor parameters");
      }
      final FXMatrix fxMatrix = (FXMatrix) inputs.getValue(ValueRequirementNames.FX_MATRIX);
      HullWhiteOneFactorProviderDiscount knownData;
      if (getExogenousRequirements().isEmpty()) {
        knownData = new HullWhiteOneFactorProviderDiscount(new MulticurveProviderDiscount(fxMatrix), modelParameters, currency);
      } else {
        final Object curveBundle = inputs.getValue(ValueRequirementNames.CURVE_BUNDLE);
        if (curveBundle instanceof MulticurveProviderDiscount) {
          knownData = new HullWhiteOneFactorProviderDiscount((MulticurveProviderDiscount) curveBundle, modelParameters, currency);
        }
        knownData = (HullWhiteOneFactorProviderDiscount) inputs.getValue(ValueRequirementNames.CURVE_BUNDLE);
      }
      return knownData;
    }

    @Override
    protected HullWhiteProviderDiscountBuildingRepository getBuilder(final double absoluteTolerance, final double relativeTolerance, final int maxIterations) {
      return new HullWhiteProviderDiscountBuildingRepository(absoluteTolerance, relativeTolerance, maxIterations);
    }

    @Override
    protected GeneratorYDCurve getGenerator(final CurveDefinition definition, final LocalDate valuationDate) {
      if (definition instanceof InterpolatedCurveDefinition) {
        final InterpolatedCurveDefinition interpolatedDefinition = (InterpolatedCurveDefinition) definition;
        final String interpolatorName = interpolatedDefinition.getInterpolatorName();
        final String leftExtrapolatorName = interpolatedDefinition.getLeftExtrapolatorName();
        final String rightExtrapolatorName = interpolatedDefinition.getRightExtrapolatorName();
        final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
        return new GeneratorCurveYieldInterpolated(getMaturityCalculator(), interpolator);
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
          .deliverableSwapFutureNode(new DeliverableSwapFutureNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .fraNode(new FRANodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .fxForwardNode(new FXForwardNodeConverter(conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .immFRANode(new RollDateFRANodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .immSwapNode(new RollDateSwapNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .rateFutureNode(new RateFutureNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime))
          .swapNode(new SwapNodeConverter(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime, fx))
          .create();
    }

    @Override
    protected Set<ComputedValue> getResults(final ValueSpecification bundleSpec, final ValueSpecification jacobianSpec, final ValueProperties bundleProperties,
        final Pair<HullWhiteOneFactorProviderInterface, CurveBuildingBlockBundle> pair) {
      final Set<ComputedValue> result = new HashSet<>();
      final HullWhiteOneFactorProviderDiscount provider = (HullWhiteOneFactorProviderDiscount) pair.getFirst();
      result.add(new ComputedValue(bundleSpec, pair.getFirst()));
      result.add(new ComputedValue(jacobianSpec, pair.getSecond()));
      for (final String curveName : getCurveNames()) {
        final ValueProperties curveProperties = bundleProperties.copy()
            .withoutAny(CURVE)
            .withoutAny(CURVE_SENSITIVITY_CURRENCY)
            .with(CURVE, curveName)
            .get();
        final YieldAndDiscountCurve curve = provider.getMulticurveProvider().getCurve(curveName);
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
