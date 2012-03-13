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
import com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.FXOptionSecurity;
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
public class VanillaFXOptionPortfolioLoader extends AbstractProductionTool {

  private static Logger s_logger = LoggerFactory.getLogger(VanillaFXOptionPortfolioLoader.class);
  private static final String PUT_CURRENCY = "put currency";
  private static final String CALL_CURRENCY = "call currency";
  private static final String PUT_AMOUNT = "put amount";
  private static final String CALL_AMOUNT = "call amount";
  private static final String EXPIRY = "expiry";
  private static final String IS_LONG = "is long";
  private static final String ID_SCHEME = "VANILLA_FX_OPTION_LOADER";

  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    new VanillaFXOptionPortfolioLoader().initAndRun(args);
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
    
    Collection<FXOptionSecurity> fxOptions = parseFXOption(filename);
    if (fxOptions.size() == 0) {
      throw new OpenGammaRuntimeException("No valid FX option trades were found in the specified file");
    }
    if (getCommandLine().hasOption(PortfolioLoaderHelper.WRITE_OPT)) {
      persistToPortfolio(fxOptions, portfolioName);
    }
  }
  
  private Collection<FXOptionSecurity> parseFXOption(String filename) {
    Collection<FXOptionSecurity> fxOptions = new ArrayList<FXOptionSecurity>();
    try {
      FileReader fileReader = new FileReader(filename);
      CSVReader csvReader = new CSVReader(fileReader);
      String[] headers = csvReader.readNext();
      PortfolioLoaderHelper.normaliseHeaders(headers);
      String[] line;
      int rowIndex = 1;
      while ((line = csvReader.readNext()) != null) {
        Map<String, String> fxOptionDetails = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
          if (i >= line.length) {
            break;
          }
          fxOptionDetails.put(headers[i], line[i]);
        }
        try {
          FXOptionSecurity security = parseFXOption(fxOptionDetails);
          security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
          fxOptions.add(security);
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
    sb.append("Parsed ").append(fxOptions.size()).append(" FX options:\n");
    for (FXOptionSecurity f : fxOptions) {
      sb.append("\t").append(f.getName()).append("\n");
    }
    s_logger.info(sb.toString());
    return fxOptions;
  }
  
  private void persistToPortfolio(Collection<FXOptionSecurity> fxOptions, String portfolioName) {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    for (FXOptionSecurity f : fxOptions) {
      SecurityDocument fxOptionToAddDoc = new SecurityDocument();
      fxOptionToAddDoc.setSecurity(f);
      securityMaster.add(fxOptionToAddDoc);
      ManageablePosition fxOptionPosition = new ManageablePosition(BigDecimal.ONE, f.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(fxOptionPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }

  private FXOptionSecurity parseFXOption(Map<String, String> fxOptionDetails) {
    Currency putCurrency = Currency.of(PortfolioLoaderHelper.getWithException(fxOptionDetails, PUT_CURRENCY));
    Currency callCurrency = Currency.of(PortfolioLoaderHelper.getWithException(fxOptionDetails, CALL_CURRENCY));
    double putAmount = Double.parseDouble(PortfolioLoaderHelper.getWithException(fxOptionDetails, PUT_AMOUNT));
    double callAmount = Double.parseDouble(PortfolioLoaderHelper.getWithException(fxOptionDetails, CALL_AMOUNT));
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(
        PortfolioLoaderHelper.getWithException(fxOptionDetails, EXPIRY), PortfolioLoaderHelper.CSV_DATE_FORMATTER),
        LocalTime.of(16, 0)), TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR); //TODO shouldn't be hard-coding time and zone
    ZonedDateTime settlementDate = expiry.getExpiry().plusDays(2);
    boolean isLong = Boolean.parseBoolean(PortfolioLoaderHelper.getWithException(fxOptionDetails, IS_LONG));
    FXOptionSecurity security = new FXOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, expiry, settlementDate, isLong, new EuropeanExerciseType()); 
    String name = (isLong ? "Long " : "Short ") + "put " + putCurrency.getCode() + " " + putAmount + ", call " + callCurrency.getCode() + " " + callAmount + " on " + expiry.getExpiry().toLocalDate();
    security.setName(name);
    return security;
  }

}
