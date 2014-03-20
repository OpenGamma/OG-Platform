/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

/**
 * 
 */
public class InflationIssuerProviderDiscountingFunction { /*extends
MultiCurveFunction<InflationIssuerProviderInterface, InflationDiscountBuildingRepository, GeneratorPriceIndexCurve, InflationSensitivity>*/

  /*  *//** The logger */
  /*

  private static final Logger s_logger = LoggerFactory.getLogger(InflationProviderDiscountingFunction.class);
  *//** The calculator */
  /*

  private static final ParSpreadInflationMarketQuoteDiscountingCalculator PSIMQC = ParSpreadInflationMarketQuoteDiscountingCalculator.getInstance();
  *//** The sensitivity calculator */
  /*

  private static final ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator PSIMQCSC =
  ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  *//**
    * @param configurationName The configuration name, not null
    */
  /*

  public InflationIssuerProviderDiscountingFunction(final String configurationName) {
  super(configurationName);
  }

  @Override
  public CompiledFunctionDefinition getCompiledFunction(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
  final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
  return new MyCompiledFunctionDefinition(earliestInvokation, latestInvokation, curveNames, exogenousRequirements, curveConstructionConfiguration);
  }

  @Override
  protected InstrumentDerivativeVisitor<InflationProviderInterface, Double> getCalculator() {
  return PSIMQC;
  }

  @Override
  protected InstrumentDerivativeVisitor<InflationProviderInterface, InflationSensitivity> getSensitivityCalculator() {
  return PSIMQCSC;
  }

  @Override
  protected String getCurveTypeProperty() {
  return DISCOUNTING;
  }

  *//**
    * Compiled function implementation.
    */
  /*

  protected class MyCompiledFunctionDefinition extends CurveCompiledFunctionDefinition {
  *//** The curve construction configuration */
  /*

  private final CurveConstructionConfiguration _curveConstructionConfiguration;

  *//**
    * @param earliestInvokation The earliest time for which this function is valid, null if there is no bound
    * @param latestInvokation The latest time for which this function is valid, null if there is no bound
    * @param curveNames The names of the curves produced by this function, not null
    * @param exogenousRequirements The exogenous requirements, not null
    * @param curveConstructionConfiguration The curve construction configuration, not null
    */
  /*

  protected MyCompiledFunctionDefinition(final ZonedDateTime earliestInvokation, final ZonedDateTime latestInvokation, final String[] curveNames,
  final Set<ValueRequirement> exogenousRequirements, final CurveConstructionConfiguration curveConstructionConfiguration) {
  super(earliestInvokation, latestInvokation, curveNames, ValueRequirementNames.PRICE_INDEX_CURVE, exogenousRequirements);
  ArgumentChecker.notNull(curveConstructionConfiguration, "curve construction configuration");
  _curveConstructionConfiguration = curveConstructionConfiguration;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Pair<InflationProviderInterface, CurveBuildingBlockBundle> getCurves(final FunctionInputs inputs, final ZonedDateTime now, final InflationDiscountBuildingRepository builder,
  final InflationProviderInterface knownData, final FunctionExecutionContext context, final FXMatrix fx) {
  final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
  final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(context);
  final ValueProperties curveConstructionProperties = ValueProperties.builder()
    .with(CURVE_CONSTRUCTION_CONFIG, _curveConstructionConfiguration.getName())
    .get();
  final HistoricalTimeSeriesBundle timeSeries =
    (HistoricalTimeSeriesBundle) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES,
        ComputationTargetSpecification.NULL, curveConstructionProperties));
  final int nGroups = _curveConstructionConfiguration.getCurveGroups().size();
  final MultiCurveBundle<GeneratorPriceIndexCurve>[] curveBundles = new MultiCurveBundle[nGroups];
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
  final SingleCurveBundle<GeneratorPriceIndexCurve>[] singleCurves = new SingleCurveBundle[nCurves];
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
      if (type instanceof InflationCurveTypeConfiguration) {
        final InflationCurveTypeConfiguration inflationConfiguration = (InflationCurveTypeConfiguration) type;
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
        inflation.add(ConverterUtils.indexPrice(priceIndexConvention.getName(), priceIndexConvention));
      } else {
        throw new OpenGammaRuntimeException("Cannot handle " + type.getClass());
      }
    } // type - end
    if (!inflation.isEmpty()) {
      inflationMap.put(curveName, inflation.toArray(new IndexPrice[inflation.size()]));
    }
    final GeneratorPriceIndexCurve generator = getGenerator(definition, now.toLocalDate());
    singleCurves[j++] = new SingleCurveBundle<>(curveName, derivativesForCurve, generator.initialGuess(parameterGuessForCurves), generator);
    // seasonal curve construction
    // TODO : inputs () should be retrieve from historical data, for this we need two things :
    // 1) historical value of the price index (this can be retrieve from bloomberg using the appropriate ticker)
    // 2) A statistical treatment on this data should be done, usually a kind of specific ARIMA.
  }
  final MultiCurveBundle<GeneratorPriceIndexCurve> groupBundle = new MultiCurveBundle<>(singleCurves);
  curveBundles[i++] = groupBundle;
  } // Group - end
  //TODO this is only in here because the code in analytics doesn't use generics properly
  final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> temp = builder.makeCurvesFromDerivatives(curveBundles,
    (InflationProviderDiscount) knownData, inflationMap, getCalculator(), getSensitivityCalculator());
  final Pair<InflationProviderInterface, CurveBuildingBlockBundle> result = Pairs.of((InflationProviderInterface) temp.getFirst(), temp.getSecond());
  return result;
  }

  @Override
  protected InflationProviderInterface getKnownData(final FunctionInputs inputs) {
  final FXMatrix fxMatrix = (FXMatrix) inputs.getValue(ValueRequirementNames.FX_MATRIX);
  //TODO requires that the discounting curves are supplied externally
  InflationProviderDiscount knownData;
  if (getExogenousRequirements().isEmpty()) {
  knownData = new InflationProviderDiscount(fxMatrix);
  } else {
  knownData = new InflationProviderDiscount((MulticurveProviderDiscount) inputs.getValue(ValueRequirementNames.CURVE_BUNDLE));
  knownData.getMulticurveProvider().setForexMatrix(fxMatrix);
  }
  return knownData;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
  return getResults(context, target);
  }

  @Override
  protected InflationDiscountBuildingRepository getBuilder(final double absoluteTolerance, final double relativeTolerance, final int maxIterations) {
  return new InflationDiscountBuildingRepository(absoluteTolerance, relativeTolerance, maxIterations);
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
  final ValueProperties bundleProperties, final Pair<InflationProviderInterface, CurveBuildingBlockBundle> pair) {
  final Set<ComputedValue> result = new HashSet<>();
  final InflationProviderDiscount provider = (InflationProviderDiscount) pair.getFirst();
  result.add(new ComputedValue(bundleSpec, provider));
  result.add(new ComputedValue(jacobianSpec, pair.getSecond()));
  for (final String curveName : getCurveNames()) {
  final ValueProperties curveProperties = bundleProperties.copy()
      .withoutAny(CURVE)
      .with(CURVE, curveName)
      .get();
  final PriceIndexCurve curve = provider.getCurve(curveName);
  if (curve == null) {
    s_logger.error("Could not get curve called {} from configuration {}", curveName, getCurveConstructionConfigurationName());
  } else {
    final ValueSpecification curveSpec = new ValueSpecification(PRICE_INDEX_CURVE, ComputationTargetSpecification.NULL, curveProperties);
    result.add(new ComputedValue(curveSpec, curve));
  }
  }
  return result;
  }
  }*/

}
