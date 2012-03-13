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

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper;
import com.opengamma.financial.security.cash.CashSecurity;
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
public class CashPortfolioLoader extends AbstractProductionTool {

  private static final Logger s_logger = LoggerFactory.getLogger(CashPortfolioLoader.class);
  private static final String ID_SCHEME = "CASH_LOADER";

  private static final String CURRENCY = "currency";
  private static final String REGION = "region";
  private static final String START = "start";
  private static final String MATURITY = "maturity";
  private static final String DAYCOUNT = "daycount";
  private static final String RATE = "rate";
  private static final String AMOUNT = "amount";

  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    new CashPortfolioLoader().initAndRun(args);
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
    final Collection<CashSecurity> cash = parseCash(filename);
    if (cash.size() == 0) {
      throw new OpenGammaRuntimeException("No valid cash trades were found in the specified file");
    }
    if (getCommandLine().hasOption(PortfolioLoaderHelper.WRITE_OPT)) {
      persistToPortfolio(cash, portfolioName);
    }
  }

  private Collection<CashSecurity> parseCash(String filename) {
    Collection<CashSecurity> cash = new ArrayList<CashSecurity>();
    try {
      FileReader fileReader = new FileReader(filename);
      CSVReader csvReader = new CSVReader(fileReader);
      String[] headers = csvReader.readNext();
      PortfolioLoaderHelper.normaliseHeaders(headers);
      String[] line;
      int rowIndex = 1;
      while ((line = csvReader.readNext()) != null) {
        Map<String, String> cashDetails = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
          if (i >= line.length) {
            break;
          }
          cashDetails.put(headers[i], line[i]);
        }
        try {
          CashSecurity security = parseCash(cashDetails);
          security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
          cash.add(security);
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
    sb.append("Parsed ").append(cash.size()).append(" cash:\n");
    for (CashSecurity c : cash) {
      sb.append("\t").append(c.getName()).append("\n");
    }
    s_logger.info(sb.toString());

    return cash;
  }

  private CashSecurity parseCash(Map<String, String> cashDetails) {
    Currency ccy = Currency.of(PortfolioLoaderHelper.getWithException(cashDetails, CURRENCY));
    ExternalId region = ExternalId.of(RegionUtils.ISO_COUNTRY_ALPHA2, REGION);
    LocalDateTime start = LocalDateTime.of(
        LocalDate.parse(PortfolioLoaderHelper.getWithException(cashDetails, START), PortfolioLoaderHelper.CSV_DATE_FORMATTER),
        LocalTime.MIDNIGHT);
    LocalDateTime maturity = LocalDateTime.of(
        LocalDate.parse(PortfolioLoaderHelper.getWithException(cashDetails, MATURITY), PortfolioLoaderHelper.CSV_DATE_FORMATTER),
        LocalTime.MIDNIGHT);
    DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(PortfolioLoaderHelper.getWithException(cashDetails, DAYCOUNT));
    double rate = Double.parseDouble(PortfolioLoaderHelper.getWithException(cashDetails, RATE));
    double amount = Double.parseDouble(PortfolioLoaderHelper.getWithException(cashDetails, AMOUNT));
    CashSecurity cash = new CashSecurity(ccy, region, start.atZone(TimeZone.UTC), maturity.atZone(TimeZone.UTC), dayCount, rate, amount);
    cash.setName("Cash " + ccy.getCode() + " " + PortfolioLoaderHelper.NOTIONAL_FORMATTER.format(amount) + " @ "
        + PortfolioLoaderHelper.RATE_FORMATTER.format(rate) + ", maturity "
        + maturity.toString(PortfolioLoaderHelper.OUTPUT_DATE_FORMATTER));
    return cash;
  }

  private void persistToPortfolio(Collection<CashSecurity> cash, String portfolioName) {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    for (CashSecurity c : cash) {
      SecurityDocument cashToAddDoc = new SecurityDocument();
      cashToAddDoc.setSecurity(c);
      securityMaster.add(cashToAddDoc);
      ManageablePosition cashPosition = new ManageablePosition(BigDecimal.ONE, c.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(cashPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }

}
