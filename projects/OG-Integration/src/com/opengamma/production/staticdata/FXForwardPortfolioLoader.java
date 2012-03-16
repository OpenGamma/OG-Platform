/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.production.staticdata;

import static com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper.getWithException;

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
import com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper;
import com.opengamma.financial.security.fx.FXForwardSecurity;
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
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class FXForwardPortfolioLoader extends AbstractProductionTool {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardPortfolioLoader.class);
  private static final String ID_SCHEME = "FX_FORWARD_LOADER";

  private static final String PAY_CURRENCY = "pay currency";
  private static final String RECEIVE_CURRENCY = "receive currency";
  private static final String PAY_AMOUNT = "pay amount";
  private static final String RECEIVE_AMOUNT = "receive amount";
  private static final String COUNTRY = "country";
  private static final String FORWARD_DATE = "forward date";

  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    new FXForwardPortfolioLoader().initAndRun(args);
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
    
    Collection<FXForwardSecurity> fxForwards = parseFXForward(filename);
    if (fxForwards.size() == 0) {
      throw new OpenGammaRuntimeException("No valid fx forward trades were found in the specified file");
    }
    if (getCommandLine().hasOption(PortfolioLoaderHelper.WRITE_OPT)) {
      persistToPortfolio(fxForwards, portfolioName);
    }
  }

  private Collection<FXForwardSecurity> parseFXForward(String filename) {
    Collection<FXForwardSecurity> fxForwards = new ArrayList<FXForwardSecurity>();
    try {
      FileReader fileReader = new FileReader(filename);
      CSVReader csvReader = new CSVReader(fileReader);
      String[] headers = csvReader.readNext();
      PortfolioLoaderHelper.normaliseHeaders(headers);
      String[] line;
      int rowIndex = 1;
      while ((line = csvReader.readNext()) != null) {
        Map<String, String> fxForwardDetails = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
          if (i >= line.length) {
            break;
          }
          fxForwardDetails.put(headers[i], line[i]);
        }
        try {
          FXForwardSecurity security = parseFXForward(fxForwardDetails);
          security.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
          fxForwards.add(security);
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
    sb.append("Parsed ").append(fxForwards.size()).append(" fx forwards:\n");
    for (FXForwardSecurity f : fxForwards) {
      sb.append("\t").append(f.getName()).append("\n");
    }
    s_logger.info(sb.toString());

    return fxForwards;
  }

  private FXForwardSecurity parseFXForward(Map<String, String> fxForwardDetails) {
    Currency payCurrency = Currency.of(PortfolioLoaderHelper.getWithException(fxForwardDetails, PAY_CURRENCY));
    Currency receiveCurrency = Currency.of(PortfolioLoaderHelper.getWithException(fxForwardDetails, RECEIVE_CURRENCY));
    double payAmount = Double.parseDouble(PortfolioLoaderHelper.getWithException(fxForwardDetails, PAY_AMOUNT));
    double receiveAmount = Double.parseDouble(PortfolioLoaderHelper.getWithException(fxForwardDetails, RECEIVE_AMOUNT));
    ExternalId region = RegionUtils.countryRegionId(Country.of(getWithException(fxForwardDetails, COUNTRY)));
    String date = PortfolioLoaderHelper.getWithException(fxForwardDetails, FORWARD_DATE);
    ZonedDateTime forwardDate = ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(date, PortfolioLoaderHelper.CSV_DATE_FORMATTER), 
        LocalTime.of(2, 0)), TimeZone.UTC);
    FXForwardSecurity fxForward = new FXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate, region);
    fxForward.setName("Pay " + payCurrency.getCode() + " " + payAmount + ", receive " + receiveCurrency.getCode() + " " + receiveAmount + " on " + date);
    return fxForward;
  }

  private void persistToPortfolio(Collection<FXForwardSecurity> fxForwards, String portfolioName) {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    for (FXForwardSecurity f : fxForwards) {
      SecurityDocument fxForwardToAddDoc = new SecurityDocument();
      fxForwardToAddDoc.setSecurity(f);
      securityMaster.add(fxForwardToAddDoc);
      ManageablePosition fxPosition = new ManageablePosition(BigDecimal.ONE, f.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(fxPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }

}
