/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import static com.opengamma.util.result.FailureStatus.ERROR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.LinkedListMultimap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolatedAnchorNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.issuer.IssuerDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.core.link.SecurityLink;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.AbstractCurveSpecification;
import com.opengamma.financial.analytics.curve.BillNodeConverter;
import com.opengamma.financial.analytics.curve.BondNodeConverter;
import com.opengamma.financial.analytics.curve.CashNodeConverter;
import com.opengamma.financial.analytics.curve.ConverterUtils;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
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
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.RateFutureNodeConverter;
import com.opengamma.financial.analytics.curve.RollDateFRANodeConverter;
import com.opengamma.financial.analytics.curve.RollDateSwapNodeConverter;
import com.opengamma.financial.analytics.curve.SwapNodeConverter;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

/**
 * Utility class for building elements of curve bundles
 */
//TODO SSM-164 this provider is currently only working for Issue Provider curves, not Multicurve
public final class CurveBundleProvider {

  private final SecuritySource _securitySource;
  private final ConventionSource _conventionSource;
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final CurveNodeConverterFn _curveNodeConverter;
  private final LegalEntitySource _legalEntitySource;
  private final ConventionBundleSource _conventionBundleSource;
  private final CurveSpecificationFn _curveSpecificationProvider;
  private final CurveSpecificationMarketDataFn _curveSpecMarketDataProvider;

  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator DISCOUNTING_CALCULATOR =
      ParSpreadMarketQuoteIssuerDiscountingCalculator.getInstance();

  private static final ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator CURVE_SENSITIVITY_CALCULATOR =
      ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();


  /**
   * Creates the curve bundle provider.
   *
   * @param conventionSource source for conventions, not null.
   * @param conventionBundleSource source for convention bundle, not null.
   * @param holidaySource source for holidays, not null.
   * @param regionSource source for regions, not null.
   * @param legalEntitySource source for legal entities, not null.
   * @param securitySource source for securities, not null.
   * @param curveNodeConverter converter bor curve nodes, not null.
   * @param curveSpecificationProvider provides the curve spec, not null.
   * @param curveSpecMarketDataProvider market data required for a curve specification, not null.
   *
   */
  //TODO PLAT-6801 remove conventionBundleSource once BondNodeConverter no longer uses it
  public CurveBundleProvider(ConventionSource conventionSource,
                             ConventionBundleSource conventionBundleSource,
                             HolidaySource holidaySource,
                             RegionSource regionSource,
                             LegalEntitySource legalEntitySource,
                             SecuritySource securitySource,
                             CurveNodeConverterFn curveNodeConverter,
                             CurveSpecificationFn curveSpecificationProvider,
                             CurveSpecificationMarketDataFn curveSpecMarketDataProvider) {
    _conventionSource = ArgumentChecker.notNull(conventionSource, "conventionSource");
    _conventionBundleSource = ArgumentChecker.notNull(conventionBundleSource, "conventionBundleSource");
    _holidaySource = ArgumentChecker.notNull(holidaySource, "holidaySource");
    _regionSource = ArgumentChecker.notNull(regionSource, "regionSource");
    _legalEntitySource = ArgumentChecker.notNull(legalEntitySource, "legalEntitySource");
    _curveNodeConverter = ArgumentChecker.notNull(curveNodeConverter, "curveNodeConverter");
    _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
    _curveSpecificationProvider = ArgumentChecker.notNull(curveSpecificationProvider, "curveSpecificationProvider");
    _curveSpecMarketDataProvider = ArgumentChecker.notNull(curveSpecMarketDataProvider, "curveSpecMarketDataProvider");
  }

  private SnapshotDataBundle createSnapshotDataBundle(Map<ExternalIdBundle, Double> marketData) {
    SnapshotDataBundle snapshotDataBundle = new SnapshotDataBundle();

    for (Map.Entry<ExternalIdBundle, Double> entry : marketData.entrySet()) {
      snapshotDataBundle.setDataPoint(entry.getKey(), entry.getValue());
    }
    return snapshotDataBundle;
  }

  private GeneratorYDCurve getGenerator(final AbstractCurveDefinition definition, LocalDate valuationDate) {

    if (definition instanceof InterpolatedCurveDefinition) {
      InterpolatedCurveDefinition interpolatedDefinition = (InterpolatedCurveDefinition) definition;
      String interpolatorName = interpolatedDefinition.getInterpolatorName();
      String leftExtrapolatorName = interpolatedDefinition.getLeftExtrapolatorName();
      String rightExtrapolatorName = interpolatedDefinition.getRightExtrapolatorName();
      Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName,
                                                                                                  leftExtrapolatorName,
                                                                                                  rightExtrapolatorName);
      if (definition instanceof FixedDateInterpolatedCurveDefinition) {
        FixedDateInterpolatedCurveDefinition fixedDateDefinition = (FixedDateInterpolatedCurveDefinition) definition;
        List<LocalDate> fixedDates = fixedDateDefinition.getFixedDates();
        DoubleArrayList nodePoints = new DoubleArrayList(fixedDates.size()); //TODO what about equal node points?
        for (final LocalDate fixedDate : fixedDates) {
          //TODO what to do if the fixed date is before the valuation date?
          nodePoints.add(TimeCalculator.getTimeBetween(valuationDate, fixedDate));
        }
        final double anchor = nodePoints.get(0); //TODO should the anchor go into the definition?
        return new GeneratorCurveYieldInterpolatedAnchorNode(nodePoints.toDoubleArray(), anchor, interpolator);
      }
      return new GeneratorCurveYieldInterpolated(LastTimeCalculator.getInstance(), interpolator);
    }

    throw new OpenGammaRuntimeException("Cannot handle curves of type " + definition.getClass());
  }


  private IndexON createOvernightIndex(OvernightCurveTypeConfiguration type) {
    OvernightIndex index  = SecurityLink.resolvable(type.getConvention().toBundle(), OvernightIndex.class).resolve();
    OvernightIndexConvention indexConvention =
        ConventionLink.resolvable(index.getConventionId(), OvernightIndexConvention.class).resolve();
    return ConverterUtils.indexON(index.getName(), indexConvention);
  }

  private IborIndex createIborIndex(IborCurveTypeConfiguration type) {

    com.opengamma.financial.security.index.IborIndex indexSecurity =
        SecurityLink.resolvable(type.getConvention(), com.opengamma.financial.security.index.IborIndex.class).resolve();

    IborIndexConvention indexConvention =
        ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();

    return ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
  }


  // TODO sort this out [SSM-164]
  public Result<Pair<IssuerProviderDiscount, CurveBuildingBlockBundle>> getCurves(
    Environment env,
    CurveConstructionConfiguration config,
    Result<IssuerProviderDiscount> exogenousBundle,
    Result<FXMatrix> fxMatrixResult,
    Set<String> impliedCurveNames,
    IssuerDiscountBuildingRepository builder) {


    final int nGroups = config.getCurveGroups().size();

    @SuppressWarnings("unchecked")
    MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nGroups];
    LinkedHashMap<String, Currency> discountingMap = new LinkedHashMap<>();
    LinkedHashMap<String, IborIndex[]> forwardIborMap = new LinkedHashMap<>();
    LinkedHashMap<String, IndexON[]> forwardONMap = new LinkedHashMap<>();
    LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> issuerMap = LinkedListMultimap.create();


    //TODO comparator to sort groups by order
    int i = 0; // Implementation Note: loop on the groups

    // Result to allow us to capture any failures in all these loops, the
    // actual value if a success is of no consequence
    Result<Boolean> curveBundleResult = Result.success(true);

    for (final CurveGroupConfiguration group : config.getCurveGroups()) { // Group - start

      final int nCurves = group.getTypesForCurves().size();

      @SuppressWarnings("unchecked")
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];

      int j = 0;

      for (final Map.Entry<AbstractCurveDefinition, List<? extends CurveTypeConfiguration>> entry : group.resolveTypesForCurves().entrySet()) {

        AbstractCurveDefinition curve = entry.getKey();
        String curveName = curve.getName();

        //TODO PLAT-6800 check implied curves first

        Result<AbstractCurveSpecification> curveSpecResult =
            _curveSpecificationProvider.getCurveSpecification(env, curve);

        if (curveSpecResult.isSuccess()) {

          final CurveSpecification specification = (CurveSpecification) curveSpecResult.getValue();

          Result<Map<ExternalIdBundle, Double>> marketDataResult =
              _curveSpecMarketDataProvider.requestData(env, specification);

          // Only proceed if we have all market data values available to us
          if (Result.allSuccessful(fxMatrixResult, marketDataResult)) {

            FXMatrix fxMatrix = fxMatrixResult.getValue();

            // todo this is temporary to allow us to get up and running fast
            SnapshotDataBundle snapshot = createSnapshotDataBundle(marketDataResult.getValue());

            int nNodes = specification.getNodes().size();
            double[] parameterGuessForCurves = new double[nNodes];
            // For FX forward, the FX rate is not a good initial guess. // TODO: change this // marketData
            Arrays.fill(parameterGuessForCurves, 0.02);
            Result<InstrumentDerivative[]> derivativesForCurve =
                extractInstrumentDerivatives(env, specification, snapshot, fxMatrix, env.getValuationTime());
            List<IborIndex> iborIndex = new ArrayList<>();
            List<IndexON> overnightIndex = new ArrayList<>();

            for (final CurveTypeConfiguration type : entry.getValue()) {
              if (type instanceof DiscountingCurveTypeConfiguration) {
                final String reference = ((DiscountingCurveTypeConfiguration) type).getReference();
                try {
                  Currency currency = Currency.of(reference);
                  //should this map check that the curve name has not already been entered?
                  discountingMap.put(curveName, currency);
                } catch (final IllegalArgumentException e) {
                  throw new OpenGammaRuntimeException("Cannot handle reference type " + reference + " for discounting curves");
                }
              } else if (type instanceof IborCurveTypeConfiguration) {
                iborIndex.add(createIborIndex((IborCurveTypeConfiguration) type));
              } else if (type instanceof OvernightCurveTypeConfiguration) {
                overnightIndex.add(createOvernightIndex((OvernightCurveTypeConfiguration) type));
              } else if (type instanceof IssuerCurveTypeConfiguration) {
                final IssuerCurveTypeConfiguration issuer = (IssuerCurveTypeConfiguration) type;
                issuerMap.put(curveName, Pairs.<Object, LegalEntityFilter<LegalEntity>>of(issuer.getKeys(),
                                                                                          issuer.getFilters()));
              } else {
                Result<?> typeFailure =
                    Result.failure(ERROR, "Cannot handle curveTypeConfiguration with type {} whilst building curve: {}",
                                   type.getClass(), curveName);

                curveBundleResult = Result.failure(curveBundleResult, typeFailure);
              }
            }

            if (!iborIndex.isEmpty()) {
              forwardIborMap.put(curveName, iborIndex.toArray(new IborIndex[iborIndex.size()]));
            }
            if (!overnightIndex.isEmpty()) {
              forwardONMap.put(curveName, overnightIndex.toArray(new IndexON[overnightIndex.size()]));
            }
            if (derivativesForCurve.isSuccess()) {
              GeneratorYDCurve generator = getGenerator(curve, env.getValuationDate());
              singleCurves[j] = new SingleCurveBundle<>(curveName,
                                                        derivativesForCurve.getValue(),
                                                        generator.initialGuess(parameterGuessForCurves),
                                                        generator);
            } else {
              curveBundleResult = Result.failure(curveBundleResult, derivativesForCurve);
            }
          } else {
            curveBundleResult = Result.failure(curveBundleResult, fxMatrixResult, marketDataResult);
          }
        } else {
          curveBundleResult = Result.failure(curveBundleResult, curveSpecResult);
        }

        j++;
      }

      if (curveBundleResult.isSuccess()) {
        curveBundles[i++] = new MultiCurveBundle<>(singleCurves);
      }

    } // Group - end

    if (Result.allSuccessful(exogenousBundle, curveBundleResult)) {

      //TODO PLAT-6800 remove implied curves
      IssuerProviderDiscount knownData = exogenousBundle.getValue();

      Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> calibratedCurves =
          builder.makeCurvesFromDerivatives(curveBundles,
                                            knownData.getIssuerProvider(),
                                            discountingMap,
                                            forwardIborMap,
                                            forwardONMap,
                                            issuerMap,
                                            DISCOUNTING_CALCULATOR,
                                            CURVE_SENSITIVITY_CALCULATOR);

      return Result.success(calibratedCurves);
    } else {
      return Result.failure(exogenousBundle, curveBundleResult);
    }
  }

  private Result<InstrumentDerivative[]> extractInstrumentDerivatives(Environment env,
                                                                      CurveSpecification specification,
                                                                      SnapshotDataBundle snapshot,
                                                                      FXMatrix fxMatrix,
                                                                      ZonedDateTime valuationTime) {
    Set<CurveNodeWithIdentifier> nodes = specification.getNodes();
    List<InstrumentDerivative> derivativesForCurve = new ArrayList<>(nodes.size());
    List<Result<?>> failures = new ArrayList<>();

    for (CurveNodeWithIdentifier node : nodes) {
      CurveNodeVisitor<InstrumentDefinition<?>> nodeVisitor =
          createCurveNodeVisitor(node.getIdentifier(),
                                 snapshot,
                                 valuationTime,
                                 fxMatrix);
      InstrumentDefinition<?> definitionForNode = node.getCurveNode().accept(nodeVisitor);
      Result<InstrumentDerivative> derivativeResult =
          _curveNodeConverter.getDerivative(env, node, definitionForNode, valuationTime);
      if (derivativeResult.isSuccess()) {
        derivativesForCurve.add(derivativeResult.getValue());
      } else {
        failures.add(derivativeResult);
      }
    }
    if (failures.isEmpty()) {
      return Result.success(derivativesForCurve.toArray(new InstrumentDerivative[derivativesForCurve.size()]));
    } else {
      return Result.failure(failures);
    }
  }

  private CurveNodeVisitor<InstrumentDefinition<?>> createCurveNodeVisitor(ExternalId dataId,
                                                                           SnapshotDataBundle marketData,
                                                                           ZonedDateTime valuationTime,
                                                                           FXMatrix fxMatrix) {
    return CurveNodeVisitorAdapter.<InstrumentDefinition<?>>builder()
        .cashNodeVisitor(new CashNodeConverter(
            _securitySource,
            _conventionSource,
            _holidaySource,
            _regionSource,
            marketData,
            dataId,
            valuationTime))
        .fraNode(new FRANodeConverter(
            _securitySource,
            _conventionSource,
            _holidaySource,
            _regionSource,
            marketData,
            dataId,
            valuationTime))
        .fxForwardNode(new FXForwardNodeConverter(
            _conventionSource,
            _holidaySource,
            _regionSource,
            marketData,
            dataId,
            valuationTime))
        .immFRANode(new RollDateFRANodeConverter(
            _securitySource,
            _conventionSource,
            _holidaySource,
            _regionSource,
            marketData,
            dataId,
            valuationTime))
        .immSwapNode(new RollDateSwapNodeConverter(
            _securitySource,
            _conventionSource,
            _holidaySource,
            _regionSource,
            marketData,
            dataId,
            valuationTime))
        .rateFutureNode(new RateFutureNodeConverter(
            _securitySource,
            _conventionSource,
            _holidaySource,
            _regionSource,
            marketData,
            dataId,
            valuationTime))
        .swapNode(new SwapNodeConverter(
            _securitySource,
            _conventionSource,
            _holidaySource,
            _regionSource,
            marketData,
            dataId,
            valuationTime,
            fxMatrix))
        .billNodeVisitor(new BillNodeConverter(
            _holidaySource,
            _regionSource,
            _securitySource,
            _legalEntitySource,
            marketData,
            dataId,
            valuationTime))
        .bondNodeVisitor(new BondNodeConverter(
            _conventionBundleSource,
            _holidaySource,
            _regionSource,
            _securitySource,
            marketData,
            dataId,
            valuationTime))

        .create();
  }

}
