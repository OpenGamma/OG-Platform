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
import javax.time.calendar.OffsetTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.production.tool.AbstractProductionTool;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * 1. Parses a CSV that represents a portfolio of EquityFutureSecurity *Trades* <p>
 * 2. Check if Security exists in database 'sec_future'. If not, create a new one with identifier ID_SCHEME    
 * 3. Create new Trades as described, assign unique keys to them, and persist them to db: 'pos_trade'.
 * 4. Create new Positions.
 * 4. Check if Portfolio exists in db: 'prt_portfolio'. Create a new one if not. Update Portfolio with new positions, 
 */
public class EquityFuturePortfolioLoader extends AbstractProductionTool {

  private static final Logger s_logger = LoggerFactory.getLogger(EquityFuturePortfolioLoader.class);
  private static final String ID_SCHEME = "MANUAL_LOAD";

  private static final String EXPIRY = "expiry";
  private static final String SETTLEMENT_DATE = "settlement date";
  private static final String TRADING_EXCHANGE = "trading exchange";
  private static final String SETTLEMENT_EXCHANGE = "settlement exchange";
  private static final String CURRENCY = "currency";
  private static final String UNIT_AMOUNT = "unit amount";
  private static final String UNDERLYING_ID = "underlying id";
  private static final String NAME = "name";
  private static final String BBG_CODE = "bbg code";
  private static final String NUMBER_OF_CONTRACTS = "number of contracts";

  private static final String TRADE_DATE = "trade date";
  private static final String REFERENCE_PRICE = "reference price";

  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    new EquityFuturePortfolioLoader().initAndRun(args);
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
    
    Collection<ManageableTrade> eqFutureTrades = parsePortfolioFile(filename);
    if (eqFutureTrades.size() == 0) {
      throw new OpenGammaRuntimeException("No valid equity future trades were found in the specified file");
    }
    if (getCommandLine().hasOption(PortfolioLoaderHelper.WRITE_OPT)) {
      persistToPortfolio(eqFutureTrades, portfolioName);
    }
    System.out.println("Evaluation of EquityFuturePortfolioLoader.main() complete");
  }

  private Collection<ManageableTrade> parsePortfolioFile(String filename) {
    Collection<ManageableTrade> equityFutureTrades = new ArrayList<ManageableTrade>();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    try {
      FileReader fileReader = new FileReader(filename);
      CSVReader csvReader = new CSVReader(fileReader);
      String[] headers = csvReader.readNext();
      PortfolioLoaderHelper.normaliseHeaders(headers);
      String[] line;
      int rowIndex = 1;
      // Each line represents a trade
      while ((line = csvReader.readNext()) != null) {
        //  Parse the data as strings
        Map<String, String> equityFutureDetails = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
          if (i >= line.length) {
            break;
          }
          equityFutureDetails.put(headers[i], line[i]);
        }
        // Look up, or construct, the security
        try {
          ManageableSecurity security;
          // First, check whether the security already exists. If so, use that 
          SecuritySearchRequest nameRequest = new SecuritySearchRequest();
          nameRequest.setName(PortfolioLoaderHelper.getWithException(equityFutureDetails, NAME));

          SecuritySearchResult searchResult = securityMaster.search(nameRequest);
          security = searchResult.getFirstSecurity();

          if (security == null) { // If not, construct a security from the file's inputs 
            security = constructEquityFutureSecurity(equityFutureDetails);

          }

          // Construct a trade
          ManageableTrade trade = constructEquityFutureTrade(equityFutureDetails, security);

          equityFutureTrades.add(trade);
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
    sb.append("Parsed ").append(equityFutureTrades.size()).append(" EquityFutureSecurity's:\n");
    for (ManageableTrade trade : equityFutureTrades) {
      sb.append("\t").append(trade.getSecurity().getName()).append("\n");
    }
    s_logger.info(sb.toString());
    return equityFutureTrades;
  }

  private void persistToPortfolio(Collection<ManageableTrade> tradeSet, String portfolioName) {
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();

    // Check to see whether the portfolio already exists
    PortfolioSearchRequest portSearchRequest = new PortfolioSearchRequest();
    portSearchRequest.setName(portfolioName);
    PortfolioSearchResult portSearchResult = portfolioMaster.search(portSearchRequest);
    ManageablePortfolio portfolio = portSearchResult.getFirstPortfolio();
    PortfolioDocument portfolioDoc = portSearchResult.getFirstDocument();

    // If it doesn't, add it
    if (portfolio == null) {
      ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
      portfolio = new ManageablePortfolio(portfolioName, rootNode);
      portfolioDoc = new PortfolioDocument();
      portfolioDoc.setPortfolio(portfolio);
      portfolioMaster.add(portfolioDoc);
    }

    // Add Trades
    for (ManageableTrade trade : tradeSet) {

      // TODO Rethink   - Position *must* contain a security, one security, and one quantity. Then you can add trades to it.. 
      //                - Position has a list of securities, Would make sense to have a list of securities and a list of quantities
      //                - Position on a number of trades... What is the significance of the security?
      ManageablePosition position = new ManageablePosition(BigDecimal.ONE, trade.getSecurity().getExternalIdBundle());
      position.addTrade(trade);
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(position));

      // Add the position to the, new or existing, portfolio
      portfolio.getRootNode().addPosition(addedDoc.getUniqueId());

    }
    portfolioMaster.update(portfolioDoc);
  }

  /**
   * Creates a Trade from a Security and details provided from file
   * @param eqFutureDetails The parsed values of the input file
   * @param security The security
   */
  private ManageableTrade constructEquityFutureTrade(Map<String, String> eqFutureDetails, ManageableSecurity security) {

    final LocalDate tradeDate = PortfolioLoaderHelper.getDateWithException(eqFutureDetails, TRADE_DATE);
    ExternalId ct = ExternalId.of("ID", "COUNTERPARTY"); // TODO: Hardcoded COUNTERPARTY
    final BigDecimal nContracts = new BigDecimal(Double.parseDouble(PortfolioLoaderHelper.getWithException(eqFutureDetails, NUMBER_OF_CONTRACTS)));

    ManageableTrade trade = new ManageableTrade(nContracts, security.getExternalIdBundle(), tradeDate, OffsetTime.of(13, 30, 0, ZoneOffset.UTC), ct);
    trade.setSecurityLink(ManageableSecurityLink.of(security));

    // TODO ELAINE/CASE! Overloaded trade._premium as a reference price!?
    final Double referencePrice = Double.parseDouble(PortfolioLoaderHelper.getWithException(eqFutureDetails, REFERENCE_PRICE));
    trade.setPremium(referencePrice);

    return trade;
  }

  /**
   * Creates a Security from details provided from file
   * @param eqFutureDetails The parsed values of the input file
   */
  private EquityFutureSecurity constructEquityFutureSecurity(Map<String, String> eqFutureDetails) {

    final Currency ccy = Currency.of(PortfolioLoaderHelper.getWithException(eqFutureDetails, CURRENCY));
    final String tradingExchange = PortfolioLoaderHelper.getWithException(eqFutureDetails, TRADING_EXCHANGE);
    final String settlementExchange = PortfolioLoaderHelper.getWithException(eqFutureDetails, SETTLEMENT_EXCHANGE);
    final double unitAmount = Double.parseDouble(PortfolioLoaderHelper.getWithException(eqFutureDetails, UNIT_AMOUNT));
    final String bbgId = PortfolioLoaderHelper.getWithException(eqFutureDetails, UNDERLYING_ID);
    final ExternalId underlyingID = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, bbgId);

    // TODO: Make Date/Time/Zone treatment consistent : As it stands, the portfolio definition files only specify dates as dd/MM/yyyy. 
    // Information of time and Zone, as read by various methods is stubbed in.
    final LocalDate expiryDate = PortfolioLoaderHelper.getDateWithException(eqFutureDetails, EXPIRY);
    final Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(expiryDate, LocalTime.of(16, 0)), TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    final ZonedDateTime settlementDate = ZonedDateTime.of(LocalDateTime.of(PortfolioLoaderHelper.getDateWithException(eqFutureDetails, SETTLEMENT_DATE), LocalTime.of(16, 0)), TimeZone.UTC);

    final EquityFutureSecurity security = new EquityFutureSecurity(expiry, tradingExchange, settlementExchange, ccy, unitAmount, settlementDate, underlyingID);
    final String identifierValue = PortfolioLoaderHelper.getWithException(eqFutureDetails, BBG_CODE);
    security.addExternalId(ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, identifierValue));
    final String name = PortfolioLoaderHelper.getWithException(eqFutureDetails, NAME);
    security.setName(name);
    security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    return security;
  }

}
