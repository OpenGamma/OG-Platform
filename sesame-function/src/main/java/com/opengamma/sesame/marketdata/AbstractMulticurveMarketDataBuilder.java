/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolatedAnchorNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.core.link.SecurityLink;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.ConverterUtils;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitor;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.FixedDateInterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.CurveNodeConverterFn;
import com.opengamma.sesame.CurveNodeInstrumentDefinitionFactory;
import com.opengamma.sesame.SimpleEnvironment;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.scenarios.CurveInputs;
import com.opengamma.sesame.marketdata.scenarios.CyclePerturbations;
import com.opengamma.sesame.marketdata.scenarios.FilteredPerturbation;
import com.opengamma.sesame.marketdata.scenarios.MulticurveMatchDetails;
import com.opengamma.sesame.marketdata.scenarios.StandardMatchDetails;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

/**
 * Abstract base class for market data builders that build multicurve bundles.
 *
 * @param <T> the type of the multicurve bundle
 */
abstract class AbstractMulticurveMarketDataBuilder<T> implements MarketDataBuilder {

  private final CurveSpecificationBuilder _curveSpecBuilder;
  private final CurveNodeConverterFn _curveNodeConverter;
  private final CurveNodeInstrumentDefinitionFactory _definitionFactory;

  /**
   * @param curveSpecBuilder for building curve specifications
   * @param curveNodeConverter for converting curve node instruments to derivatives
   * @param definitionFactory creates instrument definitions for curve node instruments
   */
  AbstractMulticurveMarketDataBuilder(CurveSpecificationBuilder curveSpecBuilder,
                                      CurveNodeConverterFn curveNodeConverter,
                                      CurveNodeInstrumentDefinitionFactory definitionFactory) {
    _curveSpecBuilder = ArgumentChecker.notNull(curveSpecBuilder, "curveSpecBuilder");
    _curveNodeConverter = ArgumentChecker.notNull(curveNodeConverter, "curveNodeConverter");
    _definitionFactory = ArgumentChecker.notNull(definitionFactory, "definitionFactory");
  }

  @Override
  public Set<MarketDataRequirement> getSingleValueRequirements(SingleValueRequirement requirement,
                                                               ZonedDateTime valuationTime,
                                                               Set<? extends MarketDataRequirement> suppliedData) {
    CurveConstructionConfiguration curveConfig = getCurveConfig(requirement);
    Set<MarketDataRequirement> parentBundleRequirements = getParentBundleRequirements(requirement, curveConfig);

    ImmutableSet.Builder<MarketDataRequirement> requirements = ImmutableSet.builder();
    requirements.addAll(parentBundleRequirements);
    // TODO this can go into a shared method
    Set<CurveNodeWithIdentifier> curveNodes = getBundleNodes(curveConfig, valuationTime);
    requirements.addAll(getRequirementsForCurveNodes(requirement, curveNodes));

    Set<Currency> currencies = getCurrencies(curveConfig, valuationTime);
    FxMatrixId fxMatrixId = FxMatrixId.of(currencies);
    requirements.add(SingleValueRequirement.of(fxMatrixId, requirement.getMarketDataTime()));
    return requirements.build();
  }

  /**
   * Extracts the curve configuration from the curve bundle requirement.
   * <p>
   * This is necessary because there are multiple types of market data IDs for curves and they don't share an
   * interface. The {@code getConfig()} methods on the ID classes are package-private and would have to be public if
   * they were defined on a common interface.
   *
   * @param requirement the requirement for the curve bundle
   * @return the configuration for building the curve bundle
   */
  abstract CurveConstructionConfiguration getCurveConfig(SingleValueRequirement requirement);

  /**
   * Returns the requirements for all the curve bundles that the curve configuration depends on.
   * <p>
   * This recursively includes the bundles it depends on directly and any dependencies of the parent bundles
   *
   * @param requirement the requirement for the curve bundle
   * @param curveConfig the curve bundle configuration
   * @return the requirements for all the curve bundles that the curve configuration depends on
   */
  abstract Set<MarketDataRequirement> getParentBundleRequirements(SingleValueRequirement requirement,
                                                                  CurveConstructionConfiguration curveConfig);

  @Override
  public Set<MarketDataRequirement> getTimeSeriesRequirements(TimeSeriesRequirement requirement,
                                                              Map<MarketDataId<?>, DateTimeSeries<LocalDate, ?>> suppliedData) {
    // TODO implement getTimeSeriesRequirements()
    throw new UnsupportedOperationException("getTimeSeriesRequirements not implemented");
  }

  @Override
  public Map<TimeSeriesRequirement, Result<? extends DateTimeSeries<LocalDate, ?>>> buildTimeSeries(
      MarketDataBundle marketDataBundle,
      Set<TimeSeriesRequirement> requirements,
      MarketDataSource marketDataSource,
      CyclePerturbations cyclePerturbations) {

    // TODO implement this
    return Collections.emptyMap();
  }

  @Override
  public abstract Class<? extends MarketDataId<T>> getKeyType();

  /**
   * Returns the nodes for all curves in a bundle.
   *
   * @param curveConfig configuration for a curve bundle
   * @param valuationTime the valuation time for which the curve is required
   * @return nodes for all curves in the bundle
   */
  private Set<CurveNodeWithIdentifier> getBundleNodes(CurveConstructionConfiguration curveConfig,
                                                      ZonedDateTime valuationTime) {
    ImmutableSet.Builder<CurveNodeWithIdentifier> curveNodes = ImmutableSet.builder();

    for (CurveGroupConfiguration group : curveConfig.getCurveGroups()) {
      for (AbstractCurveDefinition curveDefinition : group.resolveTypesForCurves().keySet()) {
        curveNodes.addAll(getCurveNodes(curveDefinition, valuationTime));
      }
    }
    return curveNodes.build();
  }

  /**
   * Returns all the nodes in a curve.
   *
   * @param curveDef the curve definition
   * @param valuationTime the valuation time for which the curve is required
   * @return nodes for all curves in the bundle
   */
  private Set<CurveNodeWithIdentifier> getCurveNodes(AbstractCurveDefinition curveDef, ZonedDateTime valuationTime) {
    // the original platform code has this cast so assume it's safe
    CurveSpecification curveSpec =
        (CurveSpecification) _curveSpecBuilder.buildSpecification(valuationTime.toInstant(),
                                                                  valuationTime.toLocalDate(),
                                                                  curveDef);
    return curveSpec.getNodes();
  }

  /**
   * Returns the market data requirements for a set of curve nodes.
   *
   * @param curveBundleRequirement the requirement for the bundle containing the nodes
   * @param curveNodes the nodes
   * @return the market data requirements for the curve nodes
   */
  private Set<MarketDataRequirement> getRequirementsForCurveNodes(MarketDataRequirement curveBundleRequirement,
                                                                  Set<CurveNodeWithIdentifier> curveNodes) {
    ImmutableSet.Builder<MarketDataRequirement> requirements = ImmutableSet.builder();

    // loop through the curve nodes, create a requirement for each node's market data
    for (CurveNodeWithIdentifier node : curveNodes) {
      ExternalIdBundle nodeId = node.getIdentifier().toBundle();
      requirements.add(SingleValueRequirement.of(RawId.of(nodeId), curveBundleRequirement.getMarketDataTime()));

      if (node instanceof PointsCurveNodeWithIdentifier) {
        // For these curves there are 2 tickers and we need access to both of them
        PointsCurveNodeWithIdentifier pointsNode = (PointsCurveNodeWithIdentifier) node;
        ExternalIdBundle underlyingId = pointsNode.getUnderlyingIdentifier().toBundle();
        requirements.add(SingleValueRequirement.of(RawId.of(underlyingId), curveBundleRequirement.getMarketDataTime()));
      }
    }
    return requirements.build();
  }

  /**
   * Builds the intermediate results required by subclasses when building curve bundles.
   *
   * @param marketDataBundle the market data
   * @param valuationTime the valuation time for which the curve bundle should be built
   * @param bundleConfig the configuration for the multicurve bundle
   * @return a multicurve bundle built from the configuration
   */
  @SuppressWarnings("unchecked")
  IntermediateResults buildIntermediateResults(MarketDataBundle marketDataBundle,
                                               ZonedDateTime valuationTime,
                                               CurveConstructionConfiguration bundleConfig,
                                               MarketDataRequirement bundleRequirement,
                                               CyclePerturbations cyclePerturbations) {

    Set<Currency> currencies = getCurrencies(bundleConfig, valuationTime);
    FxMatrixId fxMatrixKey = FxMatrixId.of(currencies);
    FXMatrix fxMatrix = marketDataBundle.get(fxMatrixKey, FXMatrix.class).getValue();
    Multimap<String, CurveTypeConfiguration> configTypes = ArrayListMultimap.create();

    // gather data about each curve
    Map<String, Currency> currenciesByCurveName = new HashMap<>();
    Multimap<String, IborIndex> iborIndexByCurveName = ArrayListMultimap.create();
    Multimap<String, IndexON> onIndexByCurveName = ArrayListMultimap.create();
    List<MultiCurveBundle<GeneratorYDCurve>> curveBundles = new ArrayList<>();
    Multimap<MarketDataRequirement, FilteredPerturbation> perturbations = cyclePerturbations.getInputPerturbations();
    // the market data perturbations that apply to this curve bundle
    Collection<FilteredPerturbation> filteredPerturbations = perturbations.get(bundleRequirement);
    // market data perturbations that perturb the data in a SnapshotDataBundle
    Collection<FilteredPerturbation> dataBundlePerturbations =
        MarketDataUtils.filterPerturbations(
            filteredPerturbations,
            CurveInputs.class,
            MulticurveMatchDetails.class);

    // loop over the curve groups in the bundle
    for (CurveGroupConfiguration groupConfig : bundleConfig.getCurveGroups()) {
      List<SingleCurveBundle<GeneratorYDCurve>> singleCurveBundles = new ArrayList<>();

      // loop over the individual curves in the curve group
      for (Map.Entry<AbstractCurveDefinition, List<? extends CurveTypeConfiguration>> entry :
          groupConfig.resolveTypesForCurves().entrySet()) {

        AbstractCurveDefinition curveDefinition = entry.getKey();
        List<? extends CurveTypeConfiguration> curveConfigTypes = entry.getValue();
        Set<CurveNodeWithIdentifier> curveNodes = getCurveNodes(curveDefinition, valuationTime);
        SnapshotDataBundle dataBundle = createDataBundle(marketDataBundle, bundleRequirement, curveNodes);
        String curveName = curveDefinition.getName();
        FilteredPerturbation perturbation = perturbationForCurve(curveName, dataBundlePerturbations);
        SnapshotDataBundle perturbedData;

        if (perturbation != null) {
          CurveInputs curveInputs = new CurveInputs(curveNodes, dataBundle);
          perturbedData = ((CurveInputs) perturbation.apply(curveInputs)).getNodeData();
        } else {
          perturbedData = dataBundle;
        }
        List<InstrumentDerivative> derivatives =
            createInstrumentDerivatives(marketDataBundle, perturbedData, fxMatrix, valuationTime, curveNodes);
        configTypes.putAll(curveName, curveConfigTypes);

        iborIndexByCurveName.putAll(curveName, createIborIndices(curveConfigTypes));
        onIndexByCurveName.putAll(curveName, createOvernightIndices(curveConfigTypes));
        Currency currency = getCurrency(curveConfigTypes);

        if (currency != null) {
          currenciesByCurveName.put(curveName, currency);
        }
        SingleCurveBundle<GeneratorYDCurve> singleCurveBundle =
            createSingleCurveBundle(valuationTime, curveDefinition, curveNodes, derivatives);
        singleCurveBundles.add(singleCurveBundle);
      }
      SingleCurveBundle[] singleBundleArray = singleCurveBundles.toArray(new SingleCurveBundle[singleCurveBundles.size()]);
      curveBundles.add(new MultiCurveBundle<GeneratorYDCurve>(singleBundleArray));
    }
    return new IntermediateResults(currenciesByCurveName,
                                   iborIndexByCurveName,
                                   onIndexByCurveName,
                                   configTypes,
                                   curveBundles);
  }

  // TODO Java 8 - use Optional
  @SuppressWarnings("unchecked")
  @Nullable
  private static FilteredPerturbation perturbationForCurve(
      String curveName,
      Collection<FilteredPerturbation> perturbations) {

    MulticurveMatchDetails matchDetails = StandardMatchDetails.multicurve(curveName);

    for (FilteredPerturbation perturbation : perturbations) {
      if (perturbation.detailsMatch(matchDetails)) {
        return perturbation;
      }
    }
    return null;
  }

  /**
   * Creates a {@code SingleCurveBundle} from a curve definition, curve generator and its instrument derivatives.
   *
   * @param valuationTime the valuation time for which the curve is required
   * @param curveDefinition the curve definition
   * @param curveNodes the curve nodes
   * @param derivatives the derivatives for the curve nodes
   * @return a bundle containing the curve generator and derivatives
   */
  private SingleCurveBundle<GeneratorYDCurve> createSingleCurveBundle(ZonedDateTime valuationTime,
                                                                      AbstractCurveDefinition curveDefinition,
                                                                      Set<CurveNodeWithIdentifier> curveNodes,
                                                                      List<InstrumentDerivative> derivatives) {
    double[] parameterGuessForCurves = new double[curveNodes.size()];
    Arrays.fill(parameterGuessForCurves, 0.02);
    GeneratorYDCurve curveGenerator = createCurveGenerator(curveDefinition, valuationTime.toLocalDate());
    double[] startingPoint = curveGenerator.initialGuess(parameterGuessForCurves);
    InstrumentDerivative[] derivativeArray = derivatives.toArray(new InstrumentDerivative[derivatives.size()]);
    return new SingleCurveBundle<>(curveDefinition.getName(), derivativeArray, startingPoint, curveGenerator);
  }

  // TODO this is required by the legacy curve code but should be replaced when the curves are overhauled
  /**
   * Creates a {@link SnapshotDataBundle} containing the market data for a set of curve nodes.
   *
   * @param marketDataBundle the market data
   * @return a bundle of market data for the curve nodes
   */
  private SnapshotDataBundle createDataBundle(MarketDataBundle marketDataBundle,
                                              MarketDataRequirement curveBundleRequirement,
                                              Set<CurveNodeWithIdentifier> curveNodes) {
    // TODO this implementation is dodgy - the long term goal should be to stop using SnapshotDataBundles at all
    // node converters should use the market data key and the MarketDataEnvironment could be passed in
    // instead of the data bundle
    Set<MarketDataRequirement> nodeRequirements = getRequirementsForCurveNodes(curveBundleRequirement, curveNodes);
    SnapshotDataBundle dataBundle = new SnapshotDataBundle();

    for (MarketDataRequirement requirement : nodeRequirements) {
      @SuppressWarnings("unchecked")
      MarketDataId<Double> marketDataId = (RawId<Double>) requirement.getMarketDataId();
      Double nodeData = marketDataBundle.get(marketDataId, Double.class).getValue();
      ExternalIdBundle id = ((RawId<Double>) marketDataId).getId();
      dataBundle.setDataPoint(id, nodeData);
    }
    return dataBundle;
  }

  /**
   * Creates derivatives for the nodes on a curve.
   *
   * @param marketDataBundle the market data environment
   * @param snapshot market data for the nodes TODO remove this all the way down through the analytics
   * @param fxMatrix FX rates for the currencies used by the curve node instruments
   * @param valuationTime the valuation time for which the curve is required
   * @param nodes the curve nodes
   * @return the derivatives for the instruments used by the curve nodes
   */
  private List<InstrumentDerivative> createInstrumentDerivatives(MarketDataBundle marketDataBundle,
                                                                 SnapshotDataBundle snapshot,
                                                                 FXMatrix fxMatrix,
                                                                 ZonedDateTime valuationTime,
                                                                 Set<CurveNodeWithIdentifier> nodes) {
    ImmutableList.Builder<InstrumentDerivative> derivativesForCurve = ImmutableList.builder();

    // TODO this is required because the definition factory and converter were originally engine functions
    // they could be migrated away from using environment now they're not used in the engine
    SimpleEnvironment env = new SimpleEnvironment(valuationTime, marketDataBundle);

    for (CurveNodeWithIdentifier node : nodes) {
      InstrumentDefinition<?> instrumentDefn =
          _definitionFactory.createInstrumentDefinition(node, snapshot, valuationTime, fxMatrix);
      Result<InstrumentDerivative> derivativeResult =
          _curveNodeConverter.getDerivative(env, node, instrumentDefn, valuationTime);
      derivativesForCurve.add(derivativeResult.getValue());
    }
    return derivativesForCurve.build();
  }

  /**
   * Returns the currency for the first config type that is discounting or null if there is no discounting type.
   *
   * @param configTypes the curve config types
   * @return the currency for the first config type that is discounting, null if none of them are
   */
  @Nullable
  private Currency getCurrency(List<? extends CurveTypeConfiguration> configTypes) {
    for (CurveTypeConfiguration configType : configTypes) {
      if (configType instanceof DiscountingCurveTypeConfiguration) {
        String reference = ((DiscountingCurveTypeConfiguration) configType).getReference();
        return Currency.of(reference);
      }
    }
    return null;
  }

  /**
   * Creates overnight indices for any of {@code configTypes} that are overnight config types.
   *
   * @param configTypes the curve config types
   * @return overnight indices for any of the config types that are overnight
   */
  private List<IndexON> createOvernightIndices(List<? extends CurveTypeConfiguration> configTypes) {
    ImmutableList.Builder<IndexON> indices = ImmutableList.builder();

    for (CurveTypeConfiguration configType : configTypes) {
      if (configType instanceof OvernightCurveTypeConfiguration) {
        OvernightCurveTypeConfiguration onType = (OvernightCurveTypeConfiguration) configType;
        OvernightIndex index  = SecurityLink.resolvable(onType.getConvention().toBundle(), OvernightIndex.class).resolve();
        OvernightIndexConvention indexConvention =
            ConventionLink.resolvable(index.getConventionId(), OvernightIndexConvention.class).resolve();
        IndexON indexON = ConverterUtils.indexON(index.getName(), indexConvention);
        indices.add(indexON);
      }
    }
    return indices.build();
  }

  /**
   * Creates IBOR indices for any of {@code configType} that are IBOR config types.
   *
   * @param configTypes the curve config types
   * @return IBOR indices for any of the config types that are IBOR
   */
  private List<IborIndex> createIborIndices(List<? extends CurveTypeConfiguration> configTypes) {
    ImmutableList.Builder<IborIndex> indices = ImmutableList.builder();

    for (CurveTypeConfiguration configType : configTypes) {
      if (configType instanceof IborCurveTypeConfiguration) {
        IborCurveTypeConfiguration iborType = (IborCurveTypeConfiguration) configType;
        com.opengamma.financial.security.index.IborIndex indexSecurity =
            SecurityLink.resolvable(iborType.getConvention(),
                                    com.opengamma.financial.security.index.IborIndex.class).resolve();

        IborIndexConvention indexConvention =
            ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();

        IborIndex iborIndex = ConverterUtils.indexIbor(indexSecurity.getName(),
                                                       indexConvention,
                                                       indexSecurity.getTenor());
        indices.add(iborIndex);
      }
    }
    return indices.build();
  }

  /**
   * Creates a curve generator for a curve definition and valuation date.
   *
   * @param definition the curve definition
   * @param valuationDate the valuation date
   * @return a generator capable of generating the curve
   */
  private GeneratorYDCurve createCurveGenerator(AbstractCurveDefinition definition, LocalDate valuationDate) {
    if (definition instanceof InterpolatedCurveDefinition) {
      InterpolatedCurveDefinition interpolatedDefinition = (InterpolatedCurveDefinition) definition;
      Interpolator1D interpolator =
          CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatedDefinition.getInterpolatorName(),
                                                                  interpolatedDefinition.getLeftExtrapolatorName(),
                                                                  interpolatedDefinition.getRightExtrapolatorName());
      if (definition instanceof FixedDateInterpolatedCurveDefinition) {
        FixedDateInterpolatedCurveDefinition fixedDateDefinition = (FixedDateInterpolatedCurveDefinition) definition;
        List<LocalDate> fixedDates = fixedDateDefinition.getFixedDates();
        DoubleArrayList nodePoints = new DoubleArrayList(fixedDates.size()); //TODO what about equal node points?

        for (LocalDate fixedDate : fixedDates) {
          nodePoints.add(TimeCalculator.getTimeBetween(valuationDate, fixedDate)); //TODO what to do if the fixed date is before the valuation date?
        }
        double anchor = nodePoints.get(0); //TODO should the anchor go into the definition?
        return new GeneratorCurveYieldInterpolatedAnchorNode(nodePoints.toDoubleArray(), anchor, interpolator);
      }
      return new GeneratorCurveYieldInterpolated(LastTimeCalculator.getInstance(), interpolator);
    }
    throw new OpenGammaRuntimeException("Cannot handle curves of type " + definition.getClass());
  }

  /**
   * Returns the currencies used by a curve bundle and all its ancestor curve bundles.
   *
   * @param curveConfig the curve bundle configuration
   * @param valuationTime the valuation time of the curve
   * @return the currencies used by the curve bundle and all its ancestor bundles
   */
  Set<Currency> getCurrencies(CurveConstructionConfiguration curveConfig, ZonedDateTime valuationTime) {
    ImmutableSet.Builder<Currency> currencies = ImmutableSet.builder();
    CurveNodeCurrencyVisitor currencyVisitor = new CurveNodeCurrencyVisitor();

    for (final CurveGroupConfiguration group : curveConfig.getCurveGroups()) {
      Map<AbstractCurveDefinition, List<? extends CurveTypeConfiguration>> typesByCurve = group.resolveTypesForCurves();

      for (AbstractCurveDefinition curveDefinition : typesByCurve.keySet()) {
        Set<CurveNodeWithIdentifier> nodes = getCurveNodes(curveDefinition, valuationTime);

        for (CurveNodeWithIdentifier node : nodes) {
          currencies.addAll(node.getCurveNode().accept(currencyVisitor));
        }
      }
    }
    return currencies.build();
  }

  /**
   * A set of intermediate values used for building curves.
   */
  static class IntermediateResults {

    private final Map<String, Currency> _currenciesByCurveName;
    private final Multimap<String, IborIndex> _iborIndexByCurveName;
    private final Multimap<String, IndexON> _onIndexByCurveName;
    private final Multimap<String, CurveTypeConfiguration> _configTypes;
    private final List<MultiCurveBundle<GeneratorYDCurve>> _curveBundles;

    private IntermediateResults(Map<String, Currency> currenciesByCurveName,
                                Multimap<String, IborIndex> iborIndexByCurveName,
                                Multimap<String, IndexON> onIndexByCurveName,
                                Multimap<String, CurveTypeConfiguration> configTypes,
                                List<MultiCurveBundle<GeneratorYDCurve>> curveBundles) {
      _currenciesByCurveName = currenciesByCurveName;
      _iborIndexByCurveName = iborIndexByCurveName;
      _onIndexByCurveName = onIndexByCurveName;
      _curveBundles = curveBundles;
      _configTypes = configTypes;
    }

    Map<String, Currency> getCurrenciesByCurveName() {
      return _currenciesByCurveName;
    }

    Multimap<String, IborIndex> getIborIndexByCurveName() {
      return _iborIndexByCurveName;
    }

    Multimap<String, IndexON> getOnIndexByCurveName() {
      return _onIndexByCurveName;
    }

    Multimap<String, CurveTypeConfiguration> getConfigTypes() {
      return _configTypes;
    }

    List<MultiCurveBundle<GeneratorYDCurve>> getCurveBundles() {
      return _curveBundles;
    }
  }
}
