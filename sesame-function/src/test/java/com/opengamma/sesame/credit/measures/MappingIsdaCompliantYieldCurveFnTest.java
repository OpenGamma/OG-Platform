/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.measures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.credit.CreditPricingSampleData;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.engine.FunctionRunner;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.interestrate.InterestRateMockSources;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataFactory;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.util.function.Function;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test the CDS PV using the mapping ISDA compliant yield curve provider
 * Pricing CDS tenor P5Y - validated in CDSYieldCurveExampleTest
 * log-linear: pv = -37154.039151	cs01 = 531.936202
 */
@Test(groups = TestGroup.UNIT)
public class MappingIsdaCompliantYieldCurveFnTest {

  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 10, 16);
  private static final double STD_TOLERANCE_PV = 1.0E-3;
  // Validated in OG Analytics
  public static final double SINGLE_NAME_EXPECTED_PV =  -37154.043;
  private static final double BP = 1e-3;
  private static final CalculationArguments ARGS =
      CalculationArguments.builder()
          .valuationTime(VALUATION_TIME)
          .marketDataSpecification(LiveMarketDataSpecification.LIVE_SPEC)
          .build();

  private FunctionRunner _functionRunner;
  private DefaultCreditPvFn _pvFunction;

  private static final Interpolator1D s_interpolator =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator("LogNaturalCubicWithMonotonicity",
                                                              "QuadraticLeftExtrapolator",
                                                              "LinearExtrapolator");

  @BeforeMethod
  public void setUpClass()  {
    FunctionModelConfig config = CreditPricingSampleData.createYCMappingFunctionModelConfig();
    ImmutableMap<Class<?>, Object> components = InterestRateMockSources.generateBaseComponents();

    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext = ServiceContext.of(components).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);

    ComponentMap componentMap = ComponentMap.of(components);

    _functionRunner = new FunctionRunner(new MarketDataEnvironmentFactory(mock(MarketDataFactory.class)));
    _pvFunction = FunctionModel.build(DefaultCreditPvFn.class, config, componentMap);

  }

  @BeforeMethod
  private MarketDataEnvironment getSuppliedData() {

    MulticurveBundle multicurveBundle = getMulticurveBundle();
    MulticurveId multicurveId = MulticurveId.of("Curve Bundle");
    MarketDataEnvironment suppliedData = new MarketDataEnvironmentBuilder()
        .add(multicurveId, multicurveBundle)
        .valuationTime(VALUATION_TIME)
        .build();
  return suppliedData;
  }

  @Test
  public void testStandardCdsPV() {

    final StandardCDSSecurity security = CreditPricingSampleData.createStandardCDSSecurity();
    double tolerance = security.getNotional().getAmount() * BP;
    Result<CurrencyAmount> postResult = _functionRunner.runFunction(ARGS, getSuppliedData(), new Function<Environment, Result<CurrencyAmount>>() {
      @Override
      public Result<CurrencyAmount> apply(Environment env) {
        return _pvFunction.priceStandardCds(env, security);
      }
    });

    assertThat(postResult.isSuccess(), is(true));
    CurrencyAmount postCa = postResult.getValue();
    assertThat(postCa.getCurrency(), is(Currency.USD));
    assertThat(postCa.getAmount(), is(closeTo(CreditPvFnTest.SINGLE_NAME_EXPECTED_PV, tolerance)));

    assertThat(postCa.getAmount(), is(closeTo(SINGLE_NAME_EXPECTED_PV, STD_TOLERANCE_PV)));

  }

  @Test
  public void testIndexCdsPV() {

    final IndexCDSSecurity security = CreditPricingSampleData.createIndexCDSSecurity();
    double tolerance = security.getNotional().getAmount() * BP;
    Result<CurrencyAmount> postResult = _functionRunner.runFunction(ARGS, getSuppliedData(), new Function<Environment, Result<CurrencyAmount>>() {
      @Override
      public Result<CurrencyAmount> apply(Environment env) {
        return _pvFunction.priceIndexCds(env, security);
      }
    });

    assertThat(postResult.isSuccess(), is(true));
    CurrencyAmount postCa = postResult.getValue();
    assertThat(postCa.getCurrency(), is(Currency.USD));
    assertThat(postCa.getAmount(), is(closeTo(CreditPvFnTest.INDEX_EXPECTED_PV, tolerance)));

  }

  private static final double[] DAY = new double[] {91, 183, 274, 365, 457, 548, 639, 731, 1096, 1461, 1826, 2192,
      2557, 2922, 3287, 3653, 4383, 5479, 7305, 9131, 10958, 14610, 18263 };
  private static final int NUM_NODE = DAY.length;
  private static final double[] TIME;
  static {
    TIME = new double[NUM_NODE];
    for (int i = 0; i < NUM_NODE; ++i) {
      TIME[i] = DAY[i] / 365.;
    }
  }

  private static final double[] DISCOUNT_FACTOR = new double[] {0.999829198540859, 0.9996527611741278,
      0.9994706948329838, 0.9992773181206462, 0.999064682720019, 0.9988244611565431, 0.9985396977323168,
      0.9981914957448704, 0.9958878795040178, 0.9913266485465384, 0.9837191754362143, 0.9727087439132421,
      0.9585650232460775, 0.9416595859820656, 0.922635706673141, 0.90211598325509, 0.8592002467360544,
      0.7954480462459721, 0.7016145412095421, 0.6251659631564156, 0.5609548680303263, 0.45423350825036723,
      0.3726483241596532 };

  private static MulticurveBundle getMulticurveBundle() {

    String curveName = "USD DISC";
    InterpolatedDoublesCurve rawCurve = InterpolatedDoublesCurve.from(TIME, DISCOUNT_FACTOR, s_interpolator, curveName);
    YieldAndDiscountCurve yc = new DiscountCurve(curveName, rawCurve);
    MulticurveProviderDiscount multicurveProvider = new MulticurveProviderDiscount();
    CurveBuildingBlockBundle curveBuildingBlockBundle = new CurveBuildingBlockBundle();
    multicurveProvider.setCurve(Currency.USD, yc);
    MulticurveBundle bundle = new MulticurveBundle(multicurveProvider, curveBuildingBlockBundle);
    return bundle;

  }

}
