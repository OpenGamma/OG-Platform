/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.aggregation.AggregationFunction;
import com.opengamma.financial.aggregation.AssetClassAggregationFunction;
import com.opengamma.financial.aggregation.CdsObligorNameAggregationFunction;
import com.opengamma.financial.aggregation.CdsObligorTickerAggregationFunction;
import com.opengamma.financial.aggregation.CdsRedCodeAggregationFunction;
import com.opengamma.financial.aggregation.CdsSeniorityAggregationFunction;
import com.opengamma.financial.aggregation.CurrencyAggregationFunction;
import com.opengamma.financial.aggregation.DetailedAssetClassAggregationFunction;
import com.opengamma.financial.aggregation.GICSAggregationFunction;
import com.opengamma.financial.aggregation.PortfolioAggregator;
import com.opengamma.financial.aggregation.PositionAttributeAggregationFunction;
import com.opengamma.financial.aggregation.UnderlyingAggregationFunction;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to aggregate portfolios
 */
@Scriptable
public class PortfolioAggregationTool extends AbstractTool<IntegrationToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioAggregationTool.class);

  private final Map<String, AggregationFunction<?>> _aggregationFunctions = new HashMap<>();
  private static final String PORTFOLIO_OPT = "p";
  private static final String AGGREGATION_OPT = "a";
  private static final String SPLIT_OPT = "s";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new PortfolioAggregationTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    populateAggregationFunctionMap(getToolContext().getSecuritySource());
    PortfolioAggregator.aggregate(getCommandLine().getOptionValue(PORTFOLIO_OPT),
                                  getCommandLine().getOptionValues(AGGREGATION_OPT)[0],
                                  getToolContext().getPortfolioMaster(),
                                  getToolContext().getPositionMaster(),
                                  getToolContext().getPositionSource(),
                                  getToolContext().getSecuritySource(),
                                  createAggregationFunctions(getCommandLine().getOptionValues(AGGREGATION_OPT)),
                                  getCommandLine().hasOption(SPLIT_OPT));
  }


  private void populateAggregationFunctionMap(SecuritySource secSource) {
    _aggregationFunctions.put("AssetClass", new AssetClassAggregationFunction());
    _aggregationFunctions.put("Currency", new CurrencyAggregationFunction());
    _aggregationFunctions.put("DetailedAssetClass", new DetailedAssetClassAggregationFunction());
    _aggregationFunctions.put("Underlying", new UnderlyingAggregationFunction(secSource, "BLOOMBERG_TICKER"));
    _aggregationFunctions.put("ReferenceEntityName", new CdsObligorNameAggregationFunction(getToolContext().getSecuritySource(), getToolContext().getLegalEntitySource()));
    _aggregationFunctions.put("ReferenceEntityTicker", new CdsObligorTickerAggregationFunction(getToolContext().getSecuritySource(), getToolContext().getLegalEntitySource()));
    _aggregationFunctions.put("Sector", new GICSAggregationFunction(getToolContext().getSecuritySource(),
                                                                    getToolContext().getLegalEntitySource(),
                                                                    GICSAggregationFunction.Level.SECTOR, false, false));
    _aggregationFunctions.put("RedCode", new CdsRedCodeAggregationFunction(getToolContext().getSecuritySource()));
    _aggregationFunctions.put("Seniority", new CdsSeniorityAggregationFunction(getToolContext().getSecuritySource()));
  }
  
  private AggregationFunction<?>[] createAggregationFunctions(String[] aggregatorNames) {
    if (aggregatorNames == null) {
      s_logger.error("No aggregators specified");
      System.exit(1);
      return null; // idiot compiler...
    } else { 
      AggregationFunction<?>[] results = new AggregationFunction<?>[aggregatorNames.length];
      for (int i = 0; i < aggregatorNames.length; i++) {
        AggregationFunction<?> aggregationFunction = _aggregationFunctions.get(aggregatorNames[i].trim());
        if (aggregationFunction != null) {
          results[i] = aggregationFunction;
        } else {
          results[i] =  new PositionAttributeAggregationFunction(aggregatorNames[i].trim());
        }
      }
      return results;
    }
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
                                                                  " styles to use: e.g AssetClass,Currency,DetailedAssetClass")
                                                 .create(AGGREGATION_OPT);
    options.addOption(aggregationTypesOption);
    @SuppressWarnings("static-access")
    Option splitPortfoliosOption =  OptionBuilder.withLongOpt("split")
                                                 .withDescription(
                                                     "Split into separate portfolios grouped by the top-level aggregator" +
                                                         " instead of aggregating the existing portfoliio")
                                                 .create(SPLIT_OPT);
    options.addOption(splitPortfoliosOption);
    return options;
  }
}
