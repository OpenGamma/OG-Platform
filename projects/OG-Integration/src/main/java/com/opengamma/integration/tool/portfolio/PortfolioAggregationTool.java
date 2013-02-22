/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.financial.aggregation.AggregationFunction;
import com.opengamma.financial.aggregation.AssetClassAggregationFunction;
import com.opengamma.financial.aggregation.CurrencyAggregationFunction;
import com.opengamma.financial.aggregation.DetailedAssetClassAggregationFunction;
import com.opengamma.financial.aggregation.PortfolioAggregator;
import com.opengamma.financial.aggregation.PositionAttributeAggregationFunction;
import com.opengamma.financial.aggregation.UnderlyingAggregationFunction;
import com.opengamma.financial.portfolio.save.SavePortfolio;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.generate.scripts.Scriptable;
import com.opengamma.util.time.DateUtils;

/**
 * Tool to aggregate portfolios
 */
@Scriptable
public class PortfolioAggregationTool extends AbstractTool<IntegrationToolContext> {
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioAggregationTool.class);
  /** The scheme to use. */
  private static final String SCHEME = "BLOOMBERG_BUID";
  /** The period to remove. */
  private static final Period IN_FUTURE = DateUtils.periodOfMonths(3);
  /** Whether to actually remove the positions. */
  private static final boolean REMOVE = false;

  private final Map<String, AggregationFunction<?>> _aggregationFunctions = new HashMap<String, AggregationFunction<?>>();
  private static final String PORTFOLIO_OPT = "p";
  private static final String AGGREGATION_OPT = "a";
  
  private void populate(SecuritySource secSource) {
    _aggregationFunctions.put("AssetClass", new AssetClassAggregationFunction());
    _aggregationFunctions.put("Currency", new CurrencyAggregationFunction());
    _aggregationFunctions.put("DetailedAssetClass", new DetailedAssetClassAggregationFunction());
    _aggregationFunctions.put("Account", new PositionAttributeAggregationFunction("account"));
    _aggregationFunctions.put("SubAccount", new PositionAttributeAggregationFunction("subaccount"));
    _aggregationFunctions.put("Underlying", new UnderlyingAggregationFunction(secSource, "BLOOMBERG_TICKER"));
  }
  
  protected Options createOptions(boolean contextProvided) {
    Options options = super.createOptions(contextProvided);
    
    @SuppressWarnings("static-access")
    Option baseViewOption = OptionBuilder.withLongOpt("portfolio")
                                         .hasArg()
                                         .isRequired()
                                         .withDescription("The portfolio name")
                                         .create(PORTFOLIO_OPT);
    options.addOption(baseViewOption);
    @SuppressWarnings("static-access")
    Option aggregationTypesOption = OptionBuilder.withLongOpt("aggregation-types")
                                                 .hasArgs()
                                                 .isRequired()
                                                 .withValueSeparator(',')
                                                 .withDescription("The (comma, no space seperated) names of the aggregation" +
                                                                  " styles to use: e.g AssetClass, Currency, DetailtedAssetClass")
                                                 .create(AGGREGATION_OPT);
    options.addOption(aggregationTypesOption);
    return options; 
  }
  
  @Override
  protected void doRun() {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    SecuritySource secSource = getToolContext().getSecuritySource();
    populate(secSource);
    AggregationFunction<?>[] aggregationFunctions = createAggregationFunctions(getCommandLine().getOptionValues(AGGREGATION_OPT));
    PortfolioSearchRequest searchReq = new PortfolioSearchRequest();
    String portfolioName = getCommandLine().getOptionValue(PORTFOLIO_OPT);
    searchReq.setName(portfolioName);
    s_logger.info("Searching for portfolio " + portfolioName + "...");
    PortfolioSearchResult searchResult = portfolioMaster.search(searchReq);
    s_logger.info("Done. Got " + searchResult.getDocuments().size() + " results");
    ManageablePortfolio manageablePortfolio = searchResult.getFirstPortfolio();
    if (manageablePortfolio == null) {
      s_logger.error("Portfolio " + portfolioName + " was not found");
      System.exit(1);
    }
    PositionSource positionSource = getToolContext().getPositionSource();
    s_logger.info("Reloading portfolio from position source...");
    Portfolio portfolio = positionSource.getPortfolio(manageablePortfolio.getUniqueId(), VersionCorrection.LATEST);
    if (portfolio == null) {
      s_logger.error("Portfolio " + portfolioName + " was not found from PositionSource");
      System.exit(1);
    }
    s_logger.info("Done.");
    ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(20);
    s_logger.info("Resolving portfolio positions and securities...");
    Portfolio resolvedPortfolio = PortfolioCompiler.resolvePortfolio(portfolio, newFixedThreadPool, secSource);
    if (resolvedPortfolio == null) {
      s_logger.error("Portfolio " + portfolioName + " was not correctly resolved by PortfolioCompiler");
      System.exit(1);
    }
    s_logger.info("Resolution Complete.");
    PortfolioAggregator aggregator = new PortfolioAggregator(aggregationFunctions);
    s_logger.info("Beginning aggregation");
    Portfolio aggregatedPortfolio = aggregator.aggregate(resolvedPortfolio);
    s_logger.info("Aggregation complete, about to persist...");
    if (aggregatedPortfolio == null) {
      s_logger.error("Portfolio " + portfolioName + " was not correctly aggregated by the Portfolio Aggregator");
      System.exit(1);
    }
    SavePortfolio savePortfolio = new SavePortfolio(newFixedThreadPool, portfolioMaster, positionMaster);
    savePortfolio.savePortfolio(aggregatedPortfolio, true); // update matching named portfolio.
    s_logger.info("Saved.");
  }
  
  private AggregationFunction<?>[] createAggregationFunctions(String[] aggregatorNames) {
    if (aggregatorNames == null) {
      s_logger.error("No aggregators specified");
      System.exit(1);
      return null; // idiot compiler...
    } else { 
      @SuppressWarnings("unchecked")
      AggregationFunction<?>[] results = new AggregationFunction<?>[aggregatorNames.length];
      for (int i=0; i < aggregatorNames.length; i++) {
        AggregationFunction<?> aggregationFunction = _aggregationFunctions.get(aggregatorNames[i].trim());
        if (aggregationFunction != null) {
          results[i] = aggregationFunction;
        } else {
          s_logger.error("Couldn't find an aggregator called " + aggregatorNames[i]);
          System.exit(1);
        }
      }
      return results;
    }
  }

  /**
   * Runs the tool.
   * 
   * @param args  empty arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    new PortfolioAggregationTool().initAndRun(args, IntegrationToolContext.class);
  }

}
