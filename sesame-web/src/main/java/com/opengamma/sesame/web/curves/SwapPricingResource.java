package com.opengamma.sesame.web.curves;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecificationParser;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.CurveSelectorMulticurveBundleFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.MarketExposureSelector;
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
import org.joda.beans.Bean;
import org.joda.beans.ser.JodaBeanSer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.net.URI;
import java.util.List;
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
   *
   * @param bundle Curve Construction Config
   * @param spec Data source
   * @param input Swap in Body of POST
   * @return PV
   */
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


}
