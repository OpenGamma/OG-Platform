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
import com.opengamma.core.region.RegionUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper;
import com.opengamma.financial.security.fra.FRASecurity;
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

/**
 * 
 */
public class FRAPortfolioLoader extends AbstractProductionTool {

  private static final Logger s_logger = LoggerFactory.getLogger(FRAPortfolioLoader.class);
  private static final String ID_SCHEME = "FRA_LOADER";

  private static final String CURRENCY = "currency";
  private static final String REGION = "region";
  private static final String START_DATE = "start date";
  private static final String END_DATE = "end date";
  private static final String RATE = "rate";
  private static final String AMOUNT = "amount";
  private static final String BBG_ID = "bloomberg identifier";

  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    new FRAPortfolioLoader().initAndRun(args);
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
    
    Collection<FRASecurity> fra = parseFRA(filename);
    if (fra.size() == 0) {
      throw new OpenGammaRuntimeException("No valid FRA trades were found in the specified file");
    }
    if (getCommandLine().hasOption(PortfolioLoaderHelper.WRITE_OPT)) {
      persistToPortfolio(fra, portfolioName);
    }
  }

  private Collection<FRASecurity> parseFRA(String filename) {
    Collection<FRASecurity> fra = new ArrayList<FRASecurity>();
    try {
      FileReader fileReader = new FileReader(filename);
      CSVReader csvReader = new CSVReader(fileReader);
      String[] headers = csvReader.readNext();
      PortfolioLoaderHelper.normaliseHeaders(headers);
      String[] line;
      int rowIndex = 1;
      while ((line = csvReader.readNext()) != null) {
        Map<String, String> fraDetails = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
          if (i >= line.length) {
            break;
          }
          fraDetails.put(headers[i], line[i]);
        }
        try {
          FRASecurity security = parseFRA(fraDetails);
          security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
          fra.add(security);
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
    sb.append("Parsed ").append(fra.size()).append(" FRAs:\n");
    for (FRASecurity f : fra) {
      sb.append("\t").append(f.getName()).append("\n");
    }
    s_logger.info(sb.toString());

    return fra;
  }

  private FRASecurity parseFRA(Map<String, String> fraDetails) {
    Currency ccy = Currency.of(PortfolioLoaderHelper.getWithException(fraDetails, CURRENCY));
    ExternalId region = ExternalId.of(RegionUtils.ISO_COUNTRY_ALPHA2, REGION);
    LocalDateTime startDate = LocalDateTime.of(LocalDate.parse(
        PortfolioLoaderHelper.getWithException(fraDetails, START_DATE), PortfolioLoaderHelper.CSV_DATE_FORMATTER),
        LocalTime.MIDNIGHT);
    LocalDateTime endDate = LocalDateTime.of(LocalDate.parse(
        PortfolioLoaderHelper.getWithException(fraDetails, END_DATE), PortfolioLoaderHelper.CSV_DATE_FORMATTER),
        LocalTime.MIDNIGHT);
    double rate = Double.parseDouble(PortfolioLoaderHelper.getWithException(fraDetails, RATE));
    double amount = Double.parseDouble(PortfolioLoaderHelper.getWithException(fraDetails, AMOUNT));
    ZonedDateTime zonedStartDate = startDate.atZone(TimeZone.UTC);
    ZonedDateTime zonedEndDate = endDate.atZone(TimeZone.UTC);
    if (!zonedEndDate.isAfter(zonedStartDate)) {
      throw new IllegalArgumentException("Start date must be before end date");
    }
    ZonedDateTime zonedFixingDate = zonedStartDate.minusDays(2);
    String bbgId = PortfolioLoaderHelper.getWithException(fraDetails, BBG_ID);
    ExternalId underlyingID = ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, bbgId);
    FRASecurity fra = new FRASecurity(ccy, region, zonedStartDate, endDate.atZone(TimeZone.UTC), rate, 1000000 * amount, underlyingID, zonedFixingDate);
    fra.setName("FRA " + ccy.getCode() + " " + PortfolioLoaderHelper.NOTIONAL_FORMATTER.format(amount) + " @ "
        + PortfolioLoaderHelper.RATE_FORMATTER.format(rate) + ", from "
        + startDate.toString(PortfolioLoaderHelper.OUTPUT_DATE_FORMATTER) + " to "
        + endDate.toString(PortfolioLoaderHelper.OUTPUT_DATE_FORMATTER));
    return fra;
  }

  private void persistToPortfolio(Collection<FRASecurity> fra, String portfolioName) {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    for (FRASecurity f : fra) {
      SecurityDocument fraToAddDoc = new SecurityDocument();
      fraToAddDoc.setSecurity(f);
      securityMaster.add(fraToAddDoc);
      ManageablePosition fraPosition = new ManageablePosition(BigDecimal.ONE, f.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(fraPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }

}
