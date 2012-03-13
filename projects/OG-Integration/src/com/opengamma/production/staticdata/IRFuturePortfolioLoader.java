/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.production.staticdata;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.production.tool.AbstractProductionTool;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 *
 */
public class IRFuturePortfolioLoader extends AbstractProductionTool {

  private static final Logger s_logger = LoggerFactory.getLogger(IRFuturePortfolioLoader.class);
  private static final String ID_SCHEME = "IR_FUTURE_LOADER";

  private static final String EXPIRY = "expiry";
  private static final String TRADING_EXCHANGE = "trading exchange";
  private static final String SETTLEMENT_EXCHANGE = "settlement exchange";
  private static final String CURRENCY = "currency";
  private static final String UNIT_AMOUNT = "unit amount";
  private static final String UNDERLYING_ID = "underlying id";
  private static final String NAME = "name";
  private static final String BBG_CODE = "bbg code";

  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    new IRFuturePortfolioLoader().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);
    PortfolioLoaderHelper.buildOptions(options);
    return options;
  }

  @Override
  protected void doRun() throws Exception {
    String filename = getCommandLine().getOptionValue(PortfolioLoaderHelper.FILE_NAME_OPT);
    String portfolioName = getCommandLine().getOptionValue(PortfolioLoaderHelper.PORTFOLIO_NAME_OPT);
    
    Collection<InterestRateFutureSecurity> irFuture = parseIRFuture(filename);
    if (irFuture.size() == 0) {
      throw new OpenGammaRuntimeException("No valid IR future trades were found in the specified file");
    }
    if (getCommandLine().hasOption(PortfolioLoaderHelper.WRITE_OPT)) {
      persistToPortfolio(irFuture, portfolioName);
    }
  }

  private Collection<InterestRateFutureSecurity> parseIRFuture(String filename) {
    Collection<InterestRateFutureSecurity> irFuture = new ArrayList<InterestRateFutureSecurity>();
    try {
      FileReader fileReader = new FileReader(filename);
      CSVReader csvReader = new CSVReader(fileReader);
      String[] headers = csvReader.readNext();
      PortfolioLoaderHelper.normaliseHeaders(headers);
      String[] line;
      int rowIndex = 1;
      while ((line = csvReader.readNext()) != null) {
        Map<String, String> irFutureDetails = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
          if (i >= line.length) {
            break;
          }
          irFutureDetails.put(headers[i], line[i]);
        }
        try {
          InterestRateFutureSecurity security = parseIRFuture(irFutureDetails);
          security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
          irFuture.add(security);
        } catch (Exception e) {
          s_logger.warn("Skipped row " + rowIndex + "; " + e);
        }
        rowIndex++;
      }
    } catch (FileNotFoundException e) {
      throw new OpenGammaRuntimeException("File " + filename + " not found");
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("An error occurred while reading file " + filename);
    }

    StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(irFuture.size()).append(" IR futures:\n");
    for (InterestRateFutureSecurity f : irFuture) {
      sb.append("\t").append(f.getName()).append("\n");
    }
    s_logger.info(sb.toString());
    return irFuture;
  }

  private void persistToPortfolio(Collection<InterestRateFutureSecurity> irFuture, String portfolioName) {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    for (InterestRateFutureSecurity f : irFuture) {
      SecurityDocument irFutureToAddDoc = new SecurityDocument();
      irFutureToAddDoc.setSecurity(f);
      securityMaster.add(irFutureToAddDoc);
      ManageablePosition irFuturePosition = new ManageablePosition(BigDecimal.ONE, f.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(irFuturePosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }

  private InterestRateFutureSecurity parseIRFuture(Map<String, String> irFutureDetails) {
    Currency ccy = Currency.of(PortfolioLoaderHelper.getWithException(irFutureDetails, CURRENCY));
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(
        PortfolioLoaderHelper.getWithException(irFutureDetails, EXPIRY), PortfolioLoaderHelper.CSV_DATE_FORMATTER),
        LocalTime.of(16, 0)), TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR); //TODO shouldn't be hard-coding time and zone
    String tradingExchange = PortfolioLoaderHelper.getWithException(irFutureDetails, TRADING_EXCHANGE);
    String settlementExchange = PortfolioLoaderHelper.getWithException(irFutureDetails, SETTLEMENT_EXCHANGE);
    double unitAmount = Double.parseDouble(PortfolioLoaderHelper.getWithException(irFutureDetails, UNIT_AMOUNT));
    String bbgId = PortfolioLoaderHelper.getWithException(irFutureDetails, UNDERLYING_ID);
    ExternalId underlyingID = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, bbgId);
    InterestRateFutureSecurity irFuture = new InterestRateFutureSecurity(expiry, tradingExchange, settlementExchange, ccy, unitAmount, underlyingID);
    String identifierValue = PortfolioLoaderHelper.getWithException(irFutureDetails, BBG_CODE);
    irFuture.addExternalId(ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, identifierValue));
    String name = PortfolioLoaderHelper.getWithException(irFutureDetails, NAME);
    irFuture.setName(name);
    return irFuture;
  }
}
