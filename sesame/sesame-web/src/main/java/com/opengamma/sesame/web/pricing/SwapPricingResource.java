package com.opengamma.sesame.web.pricing;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import com.opengamma.sesame.web.curves.CurveBundleResource;
import com.opengamma.util.ArgumentChecker;
import org.joda.beans.Bean;
import org.joda.beans.ser.JodaBeanSer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
 * REST endpoint to price a swap
 */
@Path("swappricing")
public class SwapPricingResource {

  /**
   * Builds market data in response to specified requirements
   */
  private final MarketDataEnvironmentFactory _environmentFactory;

  private final ViewRunner _viewRunner;

  private final String _currencyMatrix;

  private static final Logger s_logger = LoggerFactory.getLogger(CurveBundleResource.class);

  /**
   * @param environmentFactory         builds market data in response to specified requirements
   * @param viewRunner
   */
  public SwapPricingResource(MarketDataEnvironmentFactory environmentFactory, ViewRunner viewRunner, String currencyMatrix) {
    _viewRunner = ArgumentChecker.notNull(viewRunner, "viewRunnerFactory");
    _environmentFactory = ArgumentChecker.notNull(environmentFactory, "environmentFactory");
    _currencyMatrix = currencyMatrix;
  }

  @POST
  @Path("{bundle}/{spec}/{exposureFns}")
  public String priceSwap(@PathParam("bundle") String bundle,
                          @PathParam("spec") String spec,
                          @PathParam("exposureFns") String exposureFns,
                          String input) throws UnsupportedEncodingException {

    List<InterestRateSwapTrade> portfolio;

    Bean trade = JodaBeanSer.COMPACT.jsonReader().read(input);

    try {
      portfolio = ImmutableList.<InterestRateSwapTrade>of((InterestRateSwapTrade) trade);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Error in submitted trade: " + e);
    }
    Results swapResults = getSwapPv(portfolio, spec, bundle, URLDecoder.decode(exposureFns, "UTF-8"));

    ResultRow row = swapResults.get(0);

    ResultItem resultItem = row.get(swapResults.getColumnIndex(OutputNames.PRESENT_VALUE));

    String swapPv = resultItem.getResult().getValue().toString();

    return swapPv;
  }

  @POST
  @Path("json/{bundle}/{spec}/{exposureFns}")
  @Produces("application/json")
  public String priceSwapJson(@PathParam("bundle") String bundle,
                          @PathParam("spec") String spec,
                          @PathParam("exposureFns") String exposureFns,
                          String input) throws UnsupportedEncodingException {

    List<InterestRateSwapTrade> portfolio;
    Bean trade = JodaBeanSer.COMPACT.jsonReader().read(input);

    try {
      portfolio = ImmutableList.<InterestRateSwapTrade>of((InterestRateSwapTrade) trade);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Error in submitted trade: " + e);
    }
    Results swapResults = getSwapPv(portfolio, spec, bundle, URLDecoder.decode(exposureFns, "UTF-8"));
    String res = JodaBeanSer.PRETTY.jsonWriter().write(swapResults);
    return res;

  }

  private Results getSwapPv(List<InterestRateSwapTrade> portfolio, String spec, String bundle, String exposureFunctions) {

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

    ConfigLink<ExposureFunctions> exposureFns = ConfigLink.resolvable(exposureFunctions, ExposureFunctions.class);
    ConfigLink<CurrencyMatrix> currencyMatrix = ConfigLink.resolvable(_currencyMatrix, CurrencyMatrix.class);

    ViewConfig viewConfig = createViewConfig(exposureFns, currencyMatrix);

    Results results = _viewRunner.runView(viewConfig, calculationArguments, marketData, portfolio);


    return results;

  }

  private ViewConfig createViewConfig(ConfigLink<ExposureFunctions> exposureConfig, ConfigLink<CurrencyMatrix> currencyMatrixLink) {
    return
        configureView(
            "IRS Remote view",
            createInterestRateSwapViewColumn(OutputNames.PRESENT_VALUE,
                exposureConfig,
                currencyMatrixLink),
            createInterestRateSwapViewColumn(OutputNames.PAY_LEG_PRESENT_VALUE,
                exposureConfig,
                currencyMatrixLink),
            createInterestRateSwapViewColumn(OutputNames.RECEIVE_LEG_PRESENT_VALUE,
                exposureConfig,
                currencyMatrixLink),
            createInterestRateSwapViewColumn(OutputNames.PAR_RATE,
                exposureConfig,
                currencyMatrixLink),
            createInterestRateSwapViewColumn(OutputNames.PAY_LEG_CASH_FLOWS,
                exposureConfig,
                currencyMatrixLink),
            createInterestRateSwapViewColumn(OutputNames.RECEIVE_LEG_CASH_FLOWS,
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