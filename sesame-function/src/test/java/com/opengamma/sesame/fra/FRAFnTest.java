/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.fra;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.IsNull.notNullValue;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.exposure.ConfigDBInstrumentExposuresProvider;
import com.opengamma.financial.analytics.curve.exposure.InstrumentExposuresProvider;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCrossSensitivities;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.ConfigDbMarketExposureSelectorFn;
import com.opengamma.sesame.CurrencyPairsFn;
import com.opengamma.sesame.CurveDefinitionFn;
import com.opengamma.sesame.CurveNodeConverterFn;
import com.opengamma.sesame.CurveSpecificationFn;
import com.opengamma.sesame.CurveSpecificationMarketDataFn;
import com.opengamma.sesame.DefaultCurrencyPairsFn;
import com.opengamma.sesame.DefaultCurveDefinitionFn;
import com.opengamma.sesame.DefaultCurveNodeConverterFn;
import com.opengamma.sesame.DefaultCurveSpecificationFn;
import com.opengamma.sesame.DefaultCurveSpecificationMarketDataFn;
import com.opengamma.sesame.DefaultDiscountingMulticurveBundleFn;
import com.opengamma.sesame.DefaultDiscountingMulticurveBundleResolverFn;
import com.opengamma.sesame.DefaultFXMatrixFn;
import com.opengamma.sesame.DefaultHistoricalTimeSeriesFn;
import com.opengamma.sesame.DiscountingMulticurveBundleFn;
import com.opengamma.sesame.DiscountingMulticurveBundleResolverFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.ExposureFunctionsDiscountingMulticurveCombinerFn;
import com.opengamma.sesame.FXMatrixFn;
import com.opengamma.sesame.HistoricalTimeSeriesFn;
import com.opengamma.sesame.MarketExposureSelectorFn;
import com.opengamma.sesame.RootFinderConfiguration;
import com.opengamma.sesame.SimpleEnvironment;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.ConfigBuilder;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.interestrate.InterestRateMockSources;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

@Test(groups = TestGroup.UNIT)
public class FRAFnTest {

  private static final ZonedDateTime STD_REFERENCE_DATE = DateUtils.getUTCDate(2014, 1, 22);
  private static final ZonedDateTime STD_ACCRUAL_START_DATE = DateUtils.getUTCDate(2014, 9, 12);
  private static final ZonedDateTime STD_ACCRUAL_END_DATE = DateUtils.getUTCDate(2014, 12, 12);
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 1, 22);

  private static final double STD_TOLERANCE_PV = 1.0E-3;
  private static final double STD_TOLERANCE_RATE = 1.0E-5;

  private static final double EXPECTED_PV = 23182.5437;
  private static final double EXPECTED_PAR_RATE = 0.003315;
  private static final Map<Pair<String, Currency>, DoubleMatrix1D> EXPECTED_BUCKETED_PV01 =
      ImmutableMap.<Pair<String, Currency>, DoubleMatrix1D>builder().
          put(Pairs.of(InterestRateMockSources.USD_LIBOR3M_CURVE_NAME, Currency.USD),
              new DoubleMatrix1D(119.7375367728506, 120.92969446825524, -26.46239944124458, -460.7550335902164, 0, 0, 0,
                                 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d)
          ).
          put(Pairs.of(InterestRateMockSources.USD_OIS_CURVE_NAME, Currency.USD),
              new DoubleMatrix1D(-0.006618053208045675, -0.006618053208189062, 3.4931205919522354E-4,
                                 -0.004729686527856287, -0.031139542713136088, -0.5519190718364266, -1.0406804627639228,
                                 0.24701401372751566, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d)
          ).

          build();

  private static final Map<String, DoubleMatrix2D> EXPECTED_GAMMA_MATRICES =
      ImmutableMap.<String, DoubleMatrix2D>builder().
          put(InterestRateMockSources.USD_OIS_CURVE_NAME,
              new DoubleMatrix2D(new Double[][] {
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 2.361676E-5, 2.361676E-5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 2.361676E-5, 2.361676E-5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                  {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
              }))
          .put(InterestRateMockSources.USD_LIBOR3M_CURVE_NAME,
               new DoubleMatrix2D(new Double[][] {
                   {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, 0.008507, -0.002277, -0.013508, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, -0.002277, 6.09676e-4, 0.003616, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, -0.0135072, 0.003615977, 0.021447, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                   {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
               }))
          .build();

  private FRAFn _fraFunction;
  private FRASecurity _fraSecurity = createSingleFra();
  private ForwardRateAgreementSecurity _forwardRateAgreementSecurity = createSingleForwardRateAgreement();

  @BeforeClass
  public void setUpClass() throws IOException {
    FunctionModelConfig config = config(
        arguments(
            function(ConfigDbMarketExposureSelectorFn.class,
                     argument("exposureConfig", ConfigLink.resolved(InterestRateMockSources.mockExposureFunctions()))),
            function(RootFinderConfiguration.class,
                     argument("rootFinderAbsoluteTolerance", 1e-9),
                     argument("rootFinderRelativeTolerance", 1e-9),
                     argument("rootFinderMaxIterations", 1000)),
            function(DefaultCurveNodeConverterFn.class,
                     argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
            function(DefaultHistoricalMarketDataFn.class,
                     argument("dataSource", "BLOOMBERG")),
            function(DefaultHistoricalTimeSeriesFn.class,
                     argument("resolutionKey", "DEFAULT_TSS"),
                     argument("htsRetrievalPeriod", RetrievalPeriod.of(Period.ofYears(1)))),
            function(DefaultDiscountingMulticurveBundleFn.class,
                     argument("impliedCurveNames", StringSet.of()))),
        implementations(FRAFn.class, DiscountingFRAFn.class,
                        CurrencyPairsFn.class, DefaultCurrencyPairsFn.class,
                        InstrumentExposuresProvider.class, ConfigDBInstrumentExposuresProvider.class,
                        FRACalculatorFactory.class, DiscountingFRACalculatorFactory.class,
                        FRACalculator.class, DiscountingFRACalculator.class,
                        CurveSpecificationMarketDataFn.class, DefaultCurveSpecificationMarketDataFn.class,
                        FXMatrixFn.class, DefaultFXMatrixFn.class,
                        DiscountingMulticurveCombinerFn.class, ExposureFunctionsDiscountingMulticurveCombinerFn.class,
                        CurveNodeConverterFn.class, DefaultCurveNodeConverterFn.class,
                        CurveDefinitionFn.class, DefaultCurveDefinitionFn.class,
                        DiscountingMulticurveBundleFn.class, DefaultDiscountingMulticurveBundleFn.class,
                        DiscountingMulticurveBundleResolverFn.class, DefaultDiscountingMulticurveBundleResolverFn.class,
                        CurveSpecificationFn.class, DefaultCurveSpecificationFn.class,
                        HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class,
                        HistoricalTimeSeriesFn.class, DefaultHistoricalTimeSeriesFn.class,
                        CurveConstructionConfigurationSource.class, ConfigDBCurveConstructionConfigurationSource.class,
                        MarketExposureSelectorFn.class, ConfigDbMarketExposureSelectorFn.class,
                        MarketDataFn.class, DefaultMarketDataFn.class));

    Map<Class<?>, Object> components = InterestRateMockSources.generateBaseComponents();
    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext = ServiceContext.of(components).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);

    _fraFunction = FunctionModel.build(FRAFn.class, config, ComponentMap.of(components));
  }

  @Test
  public void discountingFRAPV() {
    MarketDataSource dataSource = InterestRateMockSources.createMarketDataSource();
    Environment env = new SimpleEnvironment(VALUATION_TIME, dataSource);
    Result<MultipleCurrencyAmount> resultPV = _fraFunction.calculatePV(env, _fraSecurity);
    assertThat(resultPV.isSuccess(), is((true)));

    MultipleCurrencyAmount mca = resultPV.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(EXPECTED_PV, STD_TOLERANCE_PV)));
  }

  @Test
  public void parRateFRA() {
    MarketDataSource dataSource = InterestRateMockSources.createMarketDataSource();
    Environment env = new SimpleEnvironment(VALUATION_TIME, dataSource);
    Result<Double> resultParRate = _fraFunction.calculateParRate(env, _fraSecurity);
    assertThat(resultParRate.isSuccess(), is((true)));

    Double parRate = resultParRate.getValue();
    assertThat(parRate, is(closeTo(EXPECTED_PAR_RATE, STD_TOLERANCE_RATE)));
  }

  @Test
  public void discountingForwardRateAgreementPV() {
    MarketDataSource dataSource = InterestRateMockSources.createMarketDataSource();
    Environment env = new SimpleEnvironment(VALUATION_TIME, dataSource);
    Result<MultipleCurrencyAmount> resultPV = _fraFunction.calculatePV(env, _forwardRateAgreementSecurity);
    assertThat(resultPV.isSuccess(), is((true)));

    MultipleCurrencyAmount mca = resultPV.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(EXPECTED_PV, STD_TOLERANCE_PV)));
  }

  @Test
  public void parRateForwardRateAgreement() {
    MarketDataSource dataSource = InterestRateMockSources.createMarketDataSource();
    Environment env = new SimpleEnvironment(VALUATION_TIME, dataSource);
    Result<Double> resultParRate = _fraFunction.calculateParRate(env, _forwardRateAgreementSecurity);
    assertThat(resultParRate.isSuccess(), is((true)));

    Double parRate = resultParRate.getValue();
    assertThat(parRate, is(closeTo(EXPECTED_PAR_RATE, STD_TOLERANCE_RATE)));
  }

  @Test
  public void discountingForwardRateAgreementPV01() {
    MarketDataSource dataSource = InterestRateMockSources.createMarketDataSource();
    Environment env = new SimpleEnvironment(VALUATION_TIME, dataSource);
    Result<ReferenceAmount<Pair<String, Currency>>> pv01 = _fraFunction.calculatePV01(env, _forwardRateAgreementSecurity);
    assertThat(pv01.isSuccess(), is((true)));

    assertThat(pv01.getValue().getMap().size(), is(2));
    assertThat(pv01.getValue().getMap().get(Pairs.of(InterestRateMockSources.USD_LIBOR3M_CURVE_NAME, Currency.USD)),
               closeTo(-249.73451494798297, STD_TOLERANCE_PV));
    assertThat(pv01.getValue().getMap().get(Pairs.of(InterestRateMockSources.USD_OIS_CURVE_NAME, Currency.USD)),
               closeTo(-1.479871968614848, STD_TOLERANCE_PV));
  }

  @Test
  public void discountingForwardRateAgreementBucketedPV01() {
    MarketDataSource dataSource = InterestRateMockSources.createMarketDataSource();
    Environment env = new SimpleEnvironment(VALUATION_TIME, dataSource);
    Result<BucketedCurveSensitivities> pv01 = _fraFunction.calculateBucketedPV01(env, _forwardRateAgreementSecurity);
    assertThat(pv01.isSuccess(), is((true)));
    Map<Pair<String, Currency>, DoubleLabelledMatrix1D> pv01s = pv01.getValue().getSensitivities();

    assertThat(pv01s.size(), is(EXPECTED_BUCKETED_PV01.size()));
    for (Map.Entry<Pair<String, Currency>, DoubleLabelledMatrix1D> sensitivity : pv01s.entrySet()) {
      DoubleMatrix1D expectedSensitivities = EXPECTED_BUCKETED_PV01.get(sensitivity.getKey());
      assertThat(sensitivity.getKey() + " not an expected sensitivity", expectedSensitivities, is(notNullValue()));
      assertThat(sensitivity.getValue().size(), is(expectedSensitivities.getNumberOfElements()));
      for (int i = 0; i < expectedSensitivities.getNumberOfElements(); i++) {
        assertThat(sensitivity.getValue().getValues()[i],
                   is(closeTo(expectedSensitivities.getEntry(i), STD_TOLERANCE_PV)));
      }
    }
  }

  @Test
  public void interestRateSwapBucketedGamma() {
    MarketDataSource dataSource = InterestRateMockSources.createMarketDataSource();
    Environment env = new SimpleEnvironment(VALUATION_TIME, dataSource);
    Result<BucketedCrossSensitivities> resultCrossGamma = _fraFunction.calculateBucketedGamma(env,
        _forwardRateAgreementSecurity);
    assertThat(resultCrossGamma.isSuccess(), is(true));

    Map<String, DoubleLabelledMatrix2D> bucketedGamma = resultCrossGamma.getValue().getCrossSensitivities();
    assertThat(bucketedGamma.size(), is(EXPECTED_GAMMA_MATRICES.size()));
    for (Map.Entry<String, DoubleLabelledMatrix2D> sensitivity : bucketedGamma.entrySet()) {
      DoubleMatrix2D expectedSensitivities = new DoubleMatrix2D(EXPECTED_GAMMA_MATRICES.get(sensitivity.getKey()).getData());
      assertThat(sensitivity.getValue().getXKeys().length, is(expectedSensitivities.getNumberOfColumns()));
      assertThat(sensitivity.getValue().getYKeys().length, is(expectedSensitivities.getNumberOfRows()));
      for (int i = 0; i < expectedSensitivities.getNumberOfColumns(); i++) {
        for (int j = 0; j < expectedSensitivities.getNumberOfRows(); j++) {
          assertThat(expectedSensitivities.getData()[i][j],
                     is(closeTo(sensitivity.getValue().getValues()[i][j], STD_TOLERANCE_PV)));
        }
      }
    }
  }

  private FRASecurity createSingleFra() {
    return new FRASecurity(Currency.USD, ExternalSchemes.financialRegionId("US"), STD_ACCRUAL_START_DATE,
                           STD_ACCRUAL_END_DATE, 0.0125, -10000000, InterestRateMockSources.getLiborIndexId(),
                           STD_REFERENCE_DATE);
  }

  private ForwardRateAgreementSecurity createSingleForwardRateAgreement() {
    return new ForwardRateAgreementSecurity(
        Currency.USD,
        InterestRateMockSources.getLiborIndexId(),
        SimpleFrequency.QUARTERLY,
        STD_ACCRUAL_START_DATE.toLocalDate(),
        STD_ACCRUAL_END_DATE.toLocalDate(),
        0.0125,
        -10000000,
        DayCountFactory.of("30/360"), 
        BusinessDayConventionFactory.of("Modified Following"), 
        Collections.singleton(ExternalSchemes.financialRegionId("US")), 
        Collections.singleton(ExternalSchemes.financialRegionId("US")),
        2);
  }

}
