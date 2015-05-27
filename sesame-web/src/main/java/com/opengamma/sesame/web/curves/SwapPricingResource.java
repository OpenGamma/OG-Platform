package com.opengamma.sesame.web.curves;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecificationParser;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.CurveSelectorMulticurveBundleFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.ViewColumn;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.RemoteViewRunner;
import com.opengamma.sesame.engine.ResultItem;
import com.opengamma.sesame.engine.ResultRow;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.engine.ViewRunner;
import com.opengamma.sesame.irs.DefaultInterestRateSwapConverterFn;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapFn;
import com.opengamma.sesame.irs.InterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.InterestRateSwapConverterFn;
import com.opengamma.sesame.irs.InterestRateSwapFn;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.marketdata.scenarios.SingleScenarioDefinition;
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import org.apache.commons.lang.ArrayUtils;
import org.joda.beans.Bean;
import org.joda.beans.ser.JodaBeanSer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.column;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;

/**
 * REST endpoint to obtain a curve bundle
 */
@Path("swappricing/{bundle}/{spec}")
public class SwapPricingResource  {

  /** Builds market data in response to specified requirements */
  private final MarketDataEnvironmentFactory _environmentFactory;

  private static final Logger s_logger = LoggerFactory.getLogger(CurveBundleResource.class);

  /**
   *  @param environmentFactory builds market data in response to specified requirements
   */
  public SwapPricingResource(MarketDataEnvironmentFactory environmentFactory) {
    _environmentFactory = ArgumentChecker.notNull(environmentFactory, "environmentFactory");
  }

  /**
   * Rest endpoint to obtain curve bundle data
   * @param bundle the name of the bundle
   * @param spec the market data specification in the format {type}:{name/id}
   * @return a json representation of the bundle, split into discounting, forward ON and forward Ibor Curves
   */
  @GET
  @Produces("application/json")
  public String buildCurveBundle(@PathParam("bundle") String bundle,
                                 @PathParam("spec") String spec) {

    MarketDataSpecification marketDataSpec = MarketDataSpecificationParser.parse(spec);
    ZonedDateTime valuationTime = ZonedDateTime.now();
    MarketDataEnvironment suppliedData = MarketDataEnvironmentBuilder.empty();
    MulticurveId multicurveId = MulticurveId.of(bundle);
    SingleValueRequirement requirement = SingleValueRequirement.of(multicurveId);
    Set<MarketDataRequirement> requirements = ImmutableSet.<MarketDataRequirement>of(requirement);
    SingleScenarioDefinition perturbations = SingleScenarioDefinition.base();
    MarketDataEnvironment marketData = _environmentFactory.build(suppliedData,
        requirements,
        perturbations,
        marketDataSpec,
        valuationTime);
    MulticurveBundle curveBundle = (MulticurveBundle) marketData.getData().get(requirement);
    MulticurveProviderDiscount multicurveProvider = curveBundle.getMulticurveProvider();

    Map<Currency, YieldAndDiscountCurve> discountingCurves = multicurveProvider.getDiscountingCurves();
    Map<IndexON, YieldAndDiscountCurve> forwardONCurves = multicurveProvider.getForwardONCurves();
    Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = multicurveProvider.getForwardIborCurves();

    Map<String, Map<String, Object>> discounting = new HashMap<>();
    Map<String, Map<String, Object>> forwardON = new HashMap<>();
    Map<String, Map<String, Object>> forwardIbor = new HashMap<>();

    for (Map.Entry<Currency, YieldAndDiscountCurve> entry : discountingCurves.entrySet()) {
      discounting.put(entry.getKey().getCode(), getCurveData(entry.getValue()));
    }

    for (Map.Entry<IndexON, YieldAndDiscountCurve> entry : forwardONCurves.entrySet()) {
      forwardON.put(entry.getKey().toString(), getCurveData(entry.getValue()));
    }

    for (Map.Entry<IborIndex, YieldAndDiscountCurve> entry : forwardIborCurves.entrySet()) {
      forwardIbor.put(entry.getKey().toString(), getCurveData(entry.getValue()));
    }

    Map<String, Map> curves = new HashMap<>();
    curves.put("discounting", discounting);
    curves.put("forwardOn", forwardON);
    curves.put("forwardIbor", forwardIbor);

    Gson gson = new Gson();
    return gson.toJson(curves);
  }

  @POST
  public String priceSwap(@PathParam("bundle") String bundle,
                          @PathParam("spec") String spec, String input){

    Bean trade = JodaBeanSer.COMPACT.jsonReader().read(input);

    List<InterestRateSwapTrade> portfolio =
        ImmutableList.<InterestRateSwapTrade>of((InterestRateSwapTrade) trade);

    Results swapResults = getSwapPv(portfolio, spec, bundle);

    ResultRow row = swapResults.get(0);

    ResultItem resultItem = row.get(swapResults.getColumnIndex("Present Value"));

    String swapPv = resultItem.getResult().getValue().toString();

    Gson gson = new Gson();

    return swapPv;
  }

  private Results getSwapPv(List<InterestRateSwapTrade> portfolio, String spec, String bundle){


    ViewRunner viewRunner = new RemoteViewRunner(URI.create("http://localhost:8080/jax"));

    MarketDataEnvironment marketDataEnvironment = MarketDataEnvironmentBuilder.empty();
    MarketDataSpecification marketDataSpec = MarketDataSpecificationParser.parse(spec);
    ZonedDateTime valuationTime = ZonedDateTime.now();
    MarketDataEnvironment suppliedData = MarketDataEnvironmentBuilder.empty();
    MulticurveId multicurveId = MulticurveId.of(bundle);
    SingleValueRequirement requirement = SingleValueRequirement.of(multicurveId);
    Set<MarketDataRequirement> requirements = ImmutableSet.<MarketDataRequirement>of(requirement);
    SingleScenarioDefinition perturbations = SingleScenarioDefinition.base();
    MarketDataEnvironment marketData = _environmentFactory.build(suppliedData,
        requirements,
        perturbations,
        marketDataSpec,
        valuationTime);

    CalculationArguments calculationArguments =
        CalculationArguments.builder()
            .valuationTime(ZonedDateTime.now())
            .marketDataSpecification(marketDataSpec)
            .build();

    ConfigLink<ExposureFunctions> exposureFns = ConfigLink.resolvable("GBP CSA Exposure Functions For Swaps", ExposureFunctions.class);
    ConfigLink<CurrencyMatrix> currencyMatrix = ConfigLink.resolvable("BloombergLiveData", CurrencyMatrix.class);

    ViewConfig viewConfig = createViewConfig(exposureFns, currencyMatrix);

    Results results = viewRunner.runView(viewConfig,calculationArguments,marketData,portfolio);

    return results;

  }

//  ****Commented out because Matrix1D is a pain to JSON-ify
//  private ViewConfig createViewConfig(ConfigLink<ExposureFunctions> exposureConfig, ConfigLink<CurrencyMatrix> currencyMatrixLink) {
//    return
//        configureView(
//            "IRS Remote view",
//            createInterestRateSwapViewColumn(OutputNames.PRESENT_VALUE,
//                exposureConfig,
//                currencyMatrixLink),
//            createInterestRateSwapViewColumn(OutputNames.BUCKETED_PV01,
//                exposureConfig,
//                currencyMatrixLink),
//            createInterestRateSwapViewColumn(OutputNames.PAY_LEG_CASH_FLOWS,
//                exposureConfig,
//                currencyMatrixLink),
//            createInterestRateSwapViewColumn(OutputNames.RECEIVE_LEG_CASH_FLOWS,
//                exposureConfig,
//                currencyMatrixLink));
//  }

  private ViewConfig createViewConfig(ConfigLink<ExposureFunctions> exposureConfig, ConfigLink<CurrencyMatrix> currencyMatrixLink) {
    return
        configureView(
            "IRS Remote view",
            createInterestRateSwapViewColumn(OutputNames.PRESENT_VALUE,
                exposureConfig,
                currencyMatrixLink),
            createInterestRateSwapViewColumn(OutputNames.PAR_RATE,
                exposureConfig,
                currencyMatrixLink));
  }

  private ViewColumn createInterestRateSwapViewColumn(String output,
                                                            ConfigLink<ExposureFunctions> exposureConfig,
                                                            ConfigLink<CurrencyMatrix> currencyMatrixLink) {
    ArgumentChecker.notNull(output, "output");
    ArgumentChecker.notNull(exposureConfig, "exposureConfig");
    ArgumentChecker.notNull(currencyMatrixLink, "currencyMatrixLink");

    return
        column(
            output,
            config(
                arguments(
                    function(
                        MarketExposureSelector.class,
                        argument("exposureFunctions", exposureConfig)),
                    function(
                        DefaultHistoricalMarketDataFn.class,
                        argument("currencyMatrix", currencyMatrixLink))),
                implementations(
                    CurveSelector.class, MarketExposureSelector.class,
                    DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class,
                    InterestRateSwapFn.class, DiscountingInterestRateSwapFn.class,
                    InterestRateSwapConverterFn.class, DefaultInterestRateSwapConverterFn.class,
                    InterestRateSwapCalculatorFactory.class, DiscountingInterestRateSwapCalculatorFactory.class)));
  }

  private Map<String, Object> getCurveData(YieldAndDiscountCurve curve) {
    Double[] tenors;
    Double[] rates;
    Map<String, Object> data = new HashMap<>();

    try {
      if (curve instanceof YieldCurve) {
        tenors = ((YieldCurve) curve).getCurve().getXData();
        rates = ((YieldCurve) curve).getCurve().getYData();
      } else if (curve instanceof DiscountCurve) {
        tenors = ((DiscountCurve) curve).getCurve().getXData();
        rates = ((DiscountCurve) curve).getCurve().getYData();
      } else {
        tenors = new Double[0];
        rates = new Double[0];
        s_logger.error(curve.getName() + " is in instance of " + curve.getClass() + " CurveBundleResource only " +
            "supports YieldCurve and DiscountCurve instances of YieldAndDiscountCurve");
      }
    } catch (UnsupportedOperationException e) {
      throw new IllegalArgumentException("Curve type: " + curve.getClass().getName() + ", does not support " +
          "the ability to get X or Y data", e);
    }

    data.put("name", curve.getName());
    data.put("x", ArrayUtils.toPrimitive(tenors));
    data.put("y", ArrayUtils.toPrimitive(rates));

    return data;
  }

}
