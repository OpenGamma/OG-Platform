/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_MAX_ITERATIONS;
import static com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults.PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.CurveNodeConverter;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeToDefinitionConverter;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class MulticurveProviderDiscountingFunction extends AbstractFunction {
  private static final String CALCULATION_METHOD = "Discounting"; //TODO move me
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private final String _configurationName;
  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  public MulticurveProviderDiscountingFunction(final String configurationName) {
    ArgumentChecker.notNull(configurationName, "configuration name");
    _configurationName = configurationName;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final CurveConstructionConfigurationSource curveConfigurationSource = new ConfigDBCurveConstructionConfigurationSource(configSource);
    final Instant versionTime = atZDT.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS).toInstant();
    //TODO work out a way to use dependency graph to get curve information for this config
    final CurveConstructionConfiguration curveConstructionConfiguration = curveConfigurationSource.getCurveConstructionConfiguration(_configurationName,
        VersionCorrection.of(versionTime, versionTime));
    if (curveConstructionConfiguration == null) {
      throw new OpenGammaRuntimeException("Could not get curve construction configuration called " + _configurationName);
    }
    final Set<ValueRequirement> exogenousRequirements = new HashSet<>();
    if (curveConstructionConfiguration.getExogenousConfigurations() != null) {
      final List<String> exogenousConfigurations = curveConstructionConfiguration.getExogenousConfigurations();
      for (final String name : exogenousConfigurations) {
        //TODO deal with arbitrary depth
        final ValueProperties properties = ValueProperties.builder()
            .with(CURVE_CALCULATION_METHOD, CALCULATION_METHOD)
            .with(CURVE_CONSTRUCTION_CONFIG, name)
            .get();
        exogenousRequirements.add(new ValueRequirement(ValueRequirementNames.CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
      }
    }
    final String[] curveNames = CurveUtils.getCurveNamesForConstructionConfiguration(curveConstructionConfiguration);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final CurveNodeToDefinitionConverter curveNodeToDefinitionConverter = new CurveNodeToDefinitionConverter(conventionSource, holidaySource, regionSource);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final FXMatrix fxMatrix = (FXMatrix) inputs.getValue(ValueRequirementNames.FX_MATRIX);
        MulticurveProviderDiscount knownData;
        if (exogenousRequirements.isEmpty()) {
          knownData = new MulticurveProviderDiscount(fxMatrix);
        } else {
          knownData = (MulticurveProviderDiscount) inputs.getValue(ValueRequirementNames.CURVE_BUNDLE);
          knownData.setForexMatrix(fxMatrix);
        }
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
        ValueProperties bundleProperties = null;
        for (final ValueRequirement desiredValue : desiredValues) {
          if (desiredValue.getValueName().equals(ValueRequirementNames.CURVE_BUNDLE)) {
            bundleProperties = desiredValue.getConstraints();
            break;
          } else if (desiredValue.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
            bundleProperties = desiredValue.getConstraints()
                .withoutAny(CURVE);
            break;
          }
        }
        if (bundleProperties == null) {
          throw new OpenGammaRuntimeException("Could not get bundle properties from desired values");
        }
        final double absoluteTolerance = Double.parseDouble(Iterables.getOnlyElement(bundleProperties.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)));
        final double relativeTolerance = Double.parseDouble(Iterables.getOnlyElement(bundleProperties.getValues(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)));
        final int maxIterations = Integer.parseInt(Iterables.getOnlyElement(bundleProperties.getValues(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)));
        final MulticurveDiscountBuildingRepository builder = new MulticurveDiscountBuildingRepository(absoluteTolerance, relativeTolerance, maxIterations);
        final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> pair = getCurves(curveConstructionConfiguration, inputs, now, builder, knownData); //, fxMatrix);
        final ValueSpecification bundleSpec = new ValueSpecification(ValueRequirementNames.CURVE_BUNDLE, ComputationTargetSpecification.NULL, bundleProperties);
        final Set<ComputedValue> result = new HashSet<>();
        result.add(new ComputedValue(bundleSpec, pair.getFirst()));
        for (final String curveName : curveNames) {
          final ValueProperties curveProperties = bundleProperties.copy()
              .with(CURVE, curveName)
              .get();
          final ValueSpecification curveSpec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties);
          result.add(new ComputedValue(curveSpec, pair.getFirst().getCurve(curveName)));
        }
        return result;
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.NULL;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final Set<ValueSpecification> results = new HashSet<>();
        final ValueProperties properties = getBundleProperties();
        for (final String curveName : curveNames) {
          final ValueProperties curveProperties = getCurveProperties(curveName);
          results.add(new ValueSpecification(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
        }
        results.add(new ValueSpecification(ValueRequirementNames.CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
        return results;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> rootFinderAbsoluteTolerance = constraints.getValues(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE);
        if (rootFinderAbsoluteTolerance == null || rootFinderAbsoluteTolerance.size() != 1) {
          return null;
        }
        final Set<String> rootFinderRelativeTolerance = constraints.getValues(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE);
        if (rootFinderRelativeTolerance == null || rootFinderRelativeTolerance.size() != 1) {
          return null;
        }
        final Set<String> maxIterations = constraints.getValues(PROPERTY_ROOT_FINDER_MAX_ITERATIONS);
        if (maxIterations == null || maxIterations.size() != 1) {
          return null;
        }
        final Set<ValueRequirement> requirements = new HashSet<>();
        for (final String curveName : curveNames) {
          final ValueProperties properties = ValueProperties.builder()
              .with(CURVE, curveName)
              .get();
          requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_DEFINITION, ComputationTargetSpecification.NULL, properties));
          requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, properties));
          requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, properties));
        }
        final ValueProperties properties = ValueProperties.builder()
            .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
            .get();
        requirements.add(new ValueRequirement(ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES, ComputationTargetSpecification.NULL, properties));
        requirements.add(new ValueRequirement(ValueRequirementNames.FX_MATRIX, ComputationTargetSpecification.NULL, properties));
        requirements.addAll(exogenousRequirements);
        return requirements;
      }

      @SuppressWarnings("synthetic-access")
      private ValueProperties getCurveProperties(final String curveName) {
        return createValueProperties()
            .with(CURVE, curveName)
            .with(CURVE_CALCULATION_METHOD, CALCULATION_METHOD)
            .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
            .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
            .get();
      }

      @SuppressWarnings("synthetic-access")
      private ValueProperties getBundleProperties() {
        return createValueProperties()
            .with(CURVE_CALCULATION_METHOD, CALCULATION_METHOD)
            .with(CURVE_CONSTRUCTION_CONFIG, _configurationName)
            .withAny(PROPERTY_ROOT_FINDER_ABSOLUTE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_RELATIVE_TOLERANCE)
            .withAny(PROPERTY_ROOT_FINDER_MAX_ITERATIONS)
            .get();
      }

      @SuppressWarnings("synthetic-access")
      private Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurves(final CurveConstructionConfiguration constructionConfiguration,
          final FunctionInputs inputs, final ZonedDateTime now, final MulticurveDiscountBuildingRepository builder, final MulticurveProviderDiscount knownData) {
        final ValueProperties curveConstructionProperties = ValueProperties.builder()
            .with(CURVE_CONSTRUCTION_CONFIG, constructionConfiguration.getName())
            .get();
        final HistoricalTimeSeriesBundle timeSeries =
            (HistoricalTimeSeriesBundle) inputs.getValue(new ValueRequirement(ValueRequirementNames.CURVE_INSTRUMENT_CONVERSION_HISTORICAL_TIME_SERIES,
                ComputationTargetSpecification.NULL, curveConstructionProperties));
        final int nGroups = constructionConfiguration.getCurveGroups().size();
        final InstrumentDerivative[][][] definitions = new InstrumentDerivative[nGroups][][];
        final GeneratorYDCurve[][] curveGenerators = new GeneratorYDCurve[nGroups][];
        final String[][] curves = new String[nGroups][];
        final double[][] parameterGuess = new double[nGroups][];
        final LinkedHashMap<String, Currency> discountingMap = new LinkedHashMap<>();
        final LinkedHashMap<String, IborIndex[]> forwardIborMap = new LinkedHashMap<>();
        final LinkedHashMap<String, IndexON[]> forwardONMap = new LinkedHashMap<>();
        //TODO comparator to sort groups by order
        int i = 0; // Implementation Note: loop on the groups
        for (final CurveGroupConfiguration group : constructionConfiguration.getCurveGroups()) { // Group - start
          int j = 0;
          final int nCurves = group.getTypesForCurves().size();
          definitions[i] = new InstrumentDerivative[nCurves][];
          curveGenerators[i] = new GeneratorYDCurve[nCurves];
          curves[i] = new String[nCurves];
          parameterGuess[i] = new double[nCurves];
          final DoubleArrayList parameterGuessForCurves = new DoubleArrayList();
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
            final double[] marketDataForCurve = new double[nNodes]; // FIXME: Where is this used?
            int k = 0;
            for (final CurveNodeWithIdentifier node : specification.getNodes()) { // Node points - start
              final Double marketData = snapshot.getDataPoint(node.getIdentifier());
              if (marketData == null) {
                throw new OpenGammaRuntimeException("Could not get market data for " + node.getIdentifier());
              }
              marketDataForCurve[k] = marketData;
              parameterGuessForCurves.add(0.02); // For FX forward, the FX rate is not a good initial guess. // TODO: change this // marketData
              final InstrumentDefinition<?> definitionForNode = curveNodeToDefinitionConverter.getDefinitionForNode(node.getCurveNode(), node.getIdentifier(), now, snapshot,
                  timeSeries, curveName);
              derivativesForCurve[k++] = CurveNodeConverter.getDerivative(node, definitionForNode, now, timeSeries);
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
                final Convention convention = conventionSource.getConvention(ibor.getConvention());
                if (!(convention instanceof IborIndexConvention)) {
                  throw new OpenGammaRuntimeException("Expecting convention of type IborIndexConvention; have " + convention.getClass());
                }
                final IborIndexConvention iborIndexConvention = (IborIndexConvention) convention;
                final int spotLag = 0; //TODO
                iborIndex.add(new IborIndex(iborIndexConvention.getCurrency(), ibor.getTenor().getPeriod(), spotLag, iborIndexConvention.getDayCount(),
                    iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(), iborIndexConvention.getName()));
              } else if (type instanceof OvernightCurveTypeConfiguration) {
                final OvernightCurveTypeConfiguration overnight = (OvernightCurveTypeConfiguration) type;
                final Convention convention = conventionSource.getConvention(overnight.getConvention());
                if (!(convention instanceof OvernightIndexConvention)) {
                  throw new OpenGammaRuntimeException("Expecting convention of type OvernightIndexConvention; have " + convention.getClass());
                }
                final OvernightIndexConvention overnightConvention = (OvernightIndexConvention) convention;
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
            definitions[i][j] = derivativesForCurve;
            curveGenerators[i][j] = getGenerator(definition);
            curves[i][j] = curveName;
            parameterGuess[i] = parameterGuessForCurves.toDoubleArray();
            j++;
          }
          i++;
        } // Group - end
        return builder.makeCurvesFromDerivatives(definitions, curveGenerators, curves, parameterGuess, knownData, discountingMap, forwardIborMap, forwardONMap, PSMQC, PSMQCSC);
      }

      private GeneratorYDCurve getGenerator(final CurveDefinition definition) {
        if (definition instanceof InterpolatedCurveDefinition) {
          final InterpolatedCurveDefinition interpolatedDefinition = (InterpolatedCurveDefinition) definition;
          final String interpolatorName = interpolatedDefinition.getInterpolatorName();
          final String leftExtrapolatorName = interpolatedDefinition.getLeftExtrapolatorName();
          final String rightExtrapolatorName = interpolatedDefinition.getRightExtrapolatorName();
          final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
          return new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, interpolator);
        }
        throw new OpenGammaRuntimeException("Cannot handle curves of type " + definition.getClass());
      }
    };
  }

}
