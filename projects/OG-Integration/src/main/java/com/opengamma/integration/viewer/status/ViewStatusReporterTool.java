/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.viewer.status.impl.BloombergReferencePortfolioMaker;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.scripts.Scriptable;

/**
 * The view status reporter tool
 */
@Scriptable
public class ViewStatusReporterTool extends AbstractTool<ToolContext> {
  
  private static final Logger s_logger = LoggerFactory.getLogger(ViewStatusReporterTool.class);
  
  /** Portfolio name option flag*/
  private static final String PORTFOLIO_NAME_OPT = "n";
  

  /**
   * Main methog to run the tool.
   * 
   * @param args the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new ViewStatusReporterTool().initAndRun(args, ToolContext.class);
    System.exit(0);
  }

  @Override
  protected void doRun() throws Exception {
    String portfolioName = getCommandLine().getOptionValue(PORTFOLIO_NAME_OPT);
    UniqueId portfolioId = null;
    if (portfolioName == null) {
      portfolioId = createReferencePortfolio();
    } else {
      portfolioId = findPortfolioId(portfolioName);
    }
    if (portfolioId != null) {
      generateReport(portfolioId);
    } else {
      s_logger.error("Couldn't find portfolio {}", portfolioName);
    }
  }
  
  private void generateReport(final UniqueId portfolioId) {
    ToolContext toolContext = getToolContext();
    AvailableOutputsProvider outputsProvider = toolContext.getAvaliableOutputsProvider();
    if (outputsProvider != null) {
      doGenerateViewStatus();
    } else {
      throw new OpenGammaRuntimeException("AvailableOutputsProvider missing from ToolContext");
    }
  }

  private void doGenerateViewStatus() {
  }

  private UniqueId findPortfolioId(final String portfolioName) {
    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    final PortfolioSearchResult searchResult = getToolContext().getPortfolioMaster().search(searchRequest);
    UniqueId portfolioId = null;
    if (searchResult.getFirstPortfolio() != null) {
      portfolioId = searchResult.getFirstPortfolio().getUniqueId();
    }
    return portfolioId;
  }

  private UniqueId createReferencePortfolio() {
    ToolContext toolContext = getToolContext();
    BloombergReferencePortfolioMaker portfolioMaker = new BloombergReferencePortfolioMaker(toolContext.getPortfolioMaster(), toolContext.getPositionMaster(), toolContext.getSecurityMaster());
    portfolioMaker.run();
    return findPortfolioId(BloombergReferencePortfolioMaker.PORTFOLIO_NAME);
  }

  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
    Option portfolioNameOption = new Option(
        PORTFOLIO_NAME_OPT, "name", true, "The name of the source OpenGamma portfolio");
    options.addOption(portfolioNameOption);
    
    return options;
  }

}
