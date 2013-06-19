/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.viewer.status.ViewStatusReporterOption.ResultFormat;
import com.opengamma.integration.viewer.status.impl.BloombergReferencePortfolioMaker;
import com.opengamma.integration.viewer.status.impl.ViewStatusCalculationWorker;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 * The view status reporter tool
 */
@Scriptable
public class ViewStatusReporterTool extends AbstractTool<ToolContext> {
  
  private static final Logger s_logger = LoggerFactory.getLogger(ViewStatusReporterTool.class);
    
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
    ViewStatusReporterOption option = ViewStatusReporterOption.getViewStatusReporterOption(getCommandLine());
    
    String portfolioName = option.getPortfolioName();
    UniqueId portfolioId = null;
    if (portfolioName == null) {
      portfolioId = createReferencePortfolio();
    } else {
      portfolioId = findPortfolioId(portfolioName);
    }
    if (portfolioId == null) {
      throw new OpenGammaRuntimeException("Couldn't find portfolio " + portfolioName);
    }
    generateViewStatusReport(portfolioId, option.getUser(), option.getFormat(), option.getAggregateType(), option.getOutputFile());
  }
  
  private void generateViewStatusReport(final UniqueId portfolioId, final UserPrincipal user, final ResultFormat resultFormat, 
      final AggregateType aggregateType, final File outputFile) {
    
    ViewStatusCalculationWorker calculationWorker = new ViewStatusCalculationWorker(getToolContext(), portfolioId, user);
    ViewStatusResultAggregator resultAggregator = calculationWorker.run();
    
    ViewStatusResultProducer resultProducer = new ViewStatusResultProducer();
    String statusResult = resultProducer.statusResult(resultAggregator, resultFormat, aggregateType);
    try {
      s_logger.debug("Writing status report into : {}", outputFile.getPath());
      FileUtils.writeStringToFile(outputFile, statusResult);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Error writing view-status report file", ex.getCause());
    }
   
  }

  private UniqueId findPortfolioId(final String portfolioName) {
    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    final PortfolioSearchResult searchResult = getToolContext().getPortfolioMaster().search(searchRequest);
    UniqueId portfolioId = null;
    if (searchResult.getFirstPortfolio() != null) {
      portfolioId = searchResult.getFirstPortfolio().getUniqueId();
    }
    return portfolioId.toLatest();
  }

  private UniqueId createReferencePortfolio() {
    ToolContext toolContext = getToolContext();
    BloombergReferencePortfolioMaker portfolioMaker = new BloombergReferencePortfolioMaker(toolContext.getPortfolioMaster(), toolContext.getPositionMaster(), toolContext.getSecurityMaster());
    portfolioMaker.run();
    return findPortfolioId(BloombergReferencePortfolioMaker.PORTFOLIO_NAME);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Options createOptions(boolean contextProvided) {
    final Options toolOptions = super.createOptions(contextProvided);
    
    Options viewStatusOptions = ViewStatusReporterOption.createOptions();
    for (Option option : (Collection<Option>) viewStatusOptions.getOptions()) {
      s_logger.debug("adding {} to tool options", option);
      toolOptions.addOption(option);
    }
    return toolOptions;
  }
  
}
