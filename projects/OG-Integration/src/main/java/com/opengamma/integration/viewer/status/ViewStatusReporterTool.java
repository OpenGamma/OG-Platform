/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.engine.view.helper.AvailableOutput;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
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
    UserPrincipal user = option.getUser();
    String format = option.getFormat();
    generateViewStatusReport(portfolioId, user, ResultFormat.of(format));
  }
  
  private void generateViewStatusReport(final UniqueId portfolioId, UserPrincipal user, ResultFormat resultFormat) {
    
    Map<String, Collection<String>> valueRequirementBySecType = scanValueRequirementBySecType(portfolioId);
    
    if (s_logger.isDebugEnabled()) {
      StringBuilder strBuf = new StringBuilder();
      for (String securityType : Sets.newTreeSet(valueRequirementBySecType.keySet())) {
        Set<String> valueNames = Sets.newTreeSet(valueRequirementBySecType.get(securityType));
        strBuf.append(String.format("%s\t%s\n", StringUtils.rightPad(securityType, 40), valueNames.toString()));
      }
      s_logger.debug("\n{}\n", strBuf.toString());
    }
    
    ViewStatusCalculationWorker calculationWorker = new ViewStatusCalculationWorker(valueRequirementBySecType, getToolContext(), portfolioId, user);
    ViewStatusResultAggregator resultAggregator = calculationWorker.run();
    
    ViewStatusResultProducer resultProducer = new ViewStatusResultProducer();
    String statusResult = resultProducer.statusResult(resultAggregator, resultFormat);
    try {
      File filename = getFileName(resultFormat);
      s_logger.debug("Writing status report into : {}", filename.getPath());
      FileUtils.writeStringToFile(filename, statusResult);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Error writing view-status report file", ex.getCause());
    }
   
  }

  private File getFileName(ResultFormat resultFormat) {
    return new File(FileUtils.getUserDirectory(), "view-status" + "." + resultFormat.getExtension());
  }

  private Map<String, Collection<String>> scanValueRequirementBySecType(UniqueId portfolioId) {
    ToolContext toolContext = getToolContext();
    AvailableOutputsProvider availableOutputsProvider = toolContext.getAvaliableOutputsProvider();
    if (availableOutputsProvider == null) {
      throw new OpenGammaRuntimeException("AvailableOutputsProvider missing from ToolContext");
    }
    final SetMultimap<String, String> valueNamesBySecurityType = TreeMultimap.create();
    
    AvailableOutputs portfolioOutputs = availableOutputsProvider.getPortfolioOutputs(portfolioId, null);
    Set<String> securityTypes = portfolioOutputs.getSecurityTypes();
    for (String securityType : securityTypes) {
      Set<AvailableOutput> positionOutputs = portfolioOutputs.getPositionOutputs(securityType);
      for (AvailableOutput availableOutput : positionOutputs) {
        valueNamesBySecurityType.put(securityType, availableOutput.getValueName());
      }        
    }
    return valueNamesBySecurityType.asMap();
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
      toolOptions.addOption(option);
    }
    return toolOptions;
  }
  
}
