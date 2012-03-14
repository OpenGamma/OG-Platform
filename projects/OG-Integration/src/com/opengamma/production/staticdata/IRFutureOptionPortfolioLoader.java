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
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
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
public class IRFutureOptionPortfolioLoader extends AbstractProductionTool {

  private static final Logger s_logger = LoggerFactory.getLogger(IRFutureOptionPortfolioLoader.class);
  private static final String ID_SCHEME = "IR_FUTURE_OPTION_LOADER";

  private static final String EXCHANGE = "exchange";
  private static final String EXPIRY = "expiry";
  private static final String UNDERLYING_ID = "underlying identifier";
  private static final String POINT_VALUE = "point value";
  // private static final String IS_MARGINED = "margined";
  private static final String CURRENCY = "currency";
  private static final String STRIKE = "strike";
  private static final String IS_CALL = "call";

  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    new IRFutureOptionPortfolioLoader().initAndRun(args);
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
    final String filename = getCommandLine().getOptionValue(PortfolioLoaderHelper.FILE_NAME_OPT);
    final String portfolioName = getCommandLine().getOptionValue(PortfolioLoaderHelper.PORTFOLIO_NAME_OPT);
    
    final Collection<IRFutureOptionSecurity> irFuture = parseIRFutureOption(filename);
    if (irFuture.size() == 0) {
      throw new OpenGammaRuntimeException("No valid IR future option trades were found in the specified file");
    }
    if (getCommandLine().hasOption(PortfolioLoaderHelper.WRITE_OPT)) {
      persistToPortfolio(irFuture, portfolioName);
    }
  }

  private Collection<IRFutureOptionSecurity> parseIRFutureOption(final String filename) {
    final Collection<IRFutureOptionSecurity> irOptionFuture = new ArrayList<IRFutureOptionSecurity>();
    try {
      final FileReader fileReader = new FileReader(filename);
      final CSVReader csvReader = new CSVReader(fileReader);
      final String[] headers = csvReader.readNext();
      PortfolioLoaderHelper.normaliseHeaders(headers);
      String[] line;
      int rowIndex = 1;
      while ((line = csvReader.readNext()) != null) {
        final Map<String, String> irFutureOptionDetails = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
          if (i >= line.length) {
            break;
          }
          irFutureOptionDetails.put(headers[i], line[i]);
        }
        try {
          final IRFutureOptionSecurity security = parseIRFutureOption(irFutureOptionDetails);
          security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
          irOptionFuture.add(security);
        } catch (final Exception e) {
          s_logger.warn("Skipped row " + rowIndex + "; " + e);
        }
        rowIndex++;
      }
    } catch (final FileNotFoundException e) {
      throw new OpenGammaRuntimeException("File " + filename + " not found");
    } catch (final IOException e) {
      throw new OpenGammaRuntimeException("An error occurred while reading file " + filename);
    }

    final StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(irOptionFuture.size()).append(" IR future options:\n");
    for (final IRFutureOptionSecurity f : irOptionFuture) {
      sb.append("\t").append(f.getName()).append("\n");
    }
    s_logger.info(sb.toString());
    return irOptionFuture;
  }

  private void persistToPortfolio(final Collection<IRFutureOptionSecurity> irFutureOptions, final String portfolioName) {
    final PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    final PositionMaster positionMaster = getToolContext().getPositionMaster();
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    final ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    final ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    final PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    final ExternalId ct = ExternalId.of("ID", "COUNTERPARTY");
    for (final IRFutureOptionSecurity f : irFutureOptions) {
      final SecurityDocument irFutureOptionToAddDoc = new SecurityDocument();
      irFutureOptionToAddDoc.setSecurity(f);
      securityMaster.add(irFutureOptionToAddDoc);
      final ManageablePosition irFutureOptionPosition = new ManageablePosition(new BigDecimal(100), f.getExternalIdBundle());
      final ManageableTrade trade = new ManageableTrade(new BigDecimal(100), f.getExternalIdBundle(), LocalDate.of(2011, 5, 1), OffsetTime.of(11, 0, 0, ZoneOffset.UTC), ct);
      //trade.setPremiumDate(LocalDate.of(2011, 5, 1));
      //trade.setPremiumTime(OffsetTime.of(11, 0, 0, ZoneOffset.UTC));
      irFutureOptionPosition.addTrade(trade);
      final PositionDocument addedDoc = positionMaster.add(new PositionDocument(irFutureOptionPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }

  private IRFutureOptionSecurity parseIRFutureOption(final Map<String, String> irFutureOptionDetails) {
    final Currency currency = Currency.of(PortfolioLoaderHelper.getWithException(irFutureOptionDetails, CURRENCY));
    final Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(
        PortfolioLoaderHelper.getWithException(irFutureOptionDetails, EXPIRY), PortfolioLoaderHelper.CSV_DATE_FORMATTER),
        LocalTime.of(16, 0)), TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR); //TODO shouldn't be hard-coding time and zone
    final String exchange = PortfolioLoaderHelper.getWithException(irFutureOptionDetails, EXCHANGE);
    final ExerciseType exerciseType = new AmericanExerciseType();
    final String bbgID = PortfolioLoaderHelper.getWithException(irFutureOptionDetails, UNDERLYING_ID);
    final ExternalId underlyingID = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, bbgID);
    final double pointValue = Double.parseDouble(PortfolioLoaderHelper.getWithException(irFutureOptionDetails, POINT_VALUE));
    final boolean isMargined = true; //Boolean.parseBoolean(PortfolioLoaderHelper.getWithException(irFutureOptionDetails, IS_MARGINED));
    final double strike = Double.parseDouble(PortfolioLoaderHelper.getWithException(irFutureOptionDetails, STRIKE));
    final boolean isCall = Boolean.parseBoolean(PortfolioLoaderHelper.getWithException(irFutureOptionDetails, IS_CALL));
    final OptionType optionType = isCall ? OptionType.CALL : OptionType.PUT;
    final IRFutureOptionSecurity security = new IRFutureOptionSecurity(exchange, expiry, exerciseType, underlyingID, pointValue, isMargined, currency, strike, optionType);
    security.setName("American " + (isMargined ? "margined " : "") + (isCall ? "call " : "put ") + "on " + bbgID + ", strike = " + strike + ", expiring " + expiry.getExpiry().toLocalDate());
    return security;
  }
}
