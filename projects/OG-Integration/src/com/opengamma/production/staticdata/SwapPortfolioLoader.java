/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
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
 * Quick utility that constructs a portfolio from a CSV file of swaps.
 */
public class SwapPortfolioLoader extends AbstractProductionTool {

  private static Logger s_logger = LoggerFactory.getLogger(SwapPortfolioLoader.class);

  /**
   * The scheme used for an identifier which is added to each swap created from the CSV file
   */
  private static final String ID_SCHEME = "SWAP_LOADER";

  // Fields expected in CSV file (case insensitive).
  // Using headers since there are so many columns that indices get messy.
  private static final String TRADE_DATE = "trade date";
  private static final String EFFECTIVE_DATE = "effective date";
  private static final String TERMINATION_DATE = "termination date";
  private static final String PAY_FIXED = "pay fixed";

  private static final String FIXED_LEG_CURRENCY = "fixed currency";
  private static final String FIXED_LEG_NOTIONAL = "fixed notional (mm)";
  private static final String FIXED_LEG_DAYCOUNT = "fixed daycount";
  private static final String FIXED_LEG_BUS_DAY_CONVENTION = "fixed business day convention";
  private static final String FIXED_LEG_FREQUENCY = "fixed frequency";
  private static final String FIXED_LEG_REGION = "fixed region";
  private static final String FIXED_LEG_RATE = "fixed rate";

  private static final String FLOATING_LEG_CURRENCY = "floating currency";
  private static final String FLOATING_LEG_NOTIONAL = "floating notional (mm)";
  private static final String FLOATING_LEG_DAYCOUNT = "floating daycount";
  private static final String FLOATING_LEG_BUS_DAY_CONVENTION = "floating business day convention";
  private static final String FLOATING_LEG_FREQUENCY = "floating frequency";
  private static final String FLOATING_LEG_REGION = "floating region";
  private static final String FLOATING_LEG_RATE = "initial floating rate";
  private static final String FLOATING_LEG_REFERENCE = "floating reference";

  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    new SwapPortfolioLoader().initAndRun(args);
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
    
    Collection<SwapSecurity> swaps = parseSwaps(filename);
    if (swaps.size() == 0) {
      throw new OpenGammaRuntimeException("No (valid) swaps were found in the specified file.");
    }
    
    if (getCommandLine().hasOption(PortfolioLoaderHelper.WRITE_OPT)) {
      persistToPortfolio(swaps, portfolioName);
    }
  }

  private void persistToPortfolio(Collection<SwapSecurity> swaps, String portfolioName) {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    
    for (SwapSecurity swap : swaps) {
      SecurityDocument swapToAddDoc = new SecurityDocument();
      swapToAddDoc.setSecurity(swap);
      securityMaster.add(swapToAddDoc);
      ManageablePosition swapPosition = new ManageablePosition(BigDecimal.ONE, swap.getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(swapPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }

  private Collection<SwapSecurity> parseSwaps(String filename) {
    Collection<SwapSecurity> swaps = new ArrayList<SwapSecurity>();
    FileReader fileReader;
    try {
      fileReader = new FileReader(filename);
      CSVReader csvReader = new CSVReader(fileReader);
      
      String[] headers = csvReader.readNext();
      PortfolioLoaderHelper.normaliseHeaders(headers);
      
      String[] line;
      int rowIndex = 1;
      while ((line = csvReader.readNext()) != null) {
        Map<String, String> swapDetails = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
          if (i >= line.length) {
            // Run out of headers for this line
            break;
          }
          swapDetails.put(headers[i], line[i]);
        }
        try {
          SwapSecurity swap = parseSwap(swapDetails);
          swap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
          swaps.add(swap);
        } catch (Exception e) {
          s_logger.warn("Skipped row " + rowIndex + " because of an error", e);
        }
        rowIndex++;
      }
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("File '" + filename + "' could not be found");
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("An error occurred while reading file '" + filename + "'");
    }
    
    StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(swaps.size()).append(" swaps:\n");
    for (SwapSecurity swap : swaps) {
      sb.append("\t").append(swap.getName()).append("\n");
    }
    s_logger.info(sb.toString());
    
    return swaps;
  }

  private SwapSecurity parseSwap(Map<String, String> swapDetails) {
    // REVIEW jonathan 2010-08-03 --
    // Admittedly this looks a bit messy, but it's going to be less error-prone than relying on column indices.
    
    DayCount fixedDayCount = DayCountFactory.INSTANCE.getDayCount(getWithException(swapDetails, FIXED_LEG_DAYCOUNT));
    Frequency fixedFrequency = SimpleFrequencyFactory.INSTANCE.getFrequency(getWithException(swapDetails, FIXED_LEG_FREQUENCY));
    ExternalId fixedRegionIdentifier = RegionUtils.countryRegionId(Country.of(getWithException(swapDetails, FIXED_LEG_REGION)));
    BusinessDayConvention fixedBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(getWithException(swapDetails, FIXED_LEG_BUS_DAY_CONVENTION));
    Currency fixedCurrency = Currency.of(getWithException(swapDetails, FIXED_LEG_CURRENCY));
    double fixedNotionalAmount = Double.parseDouble(getWithException(swapDetails, FIXED_LEG_NOTIONAL));
    Notional fixedNotional = new InterestRateNotional(fixedCurrency, fixedNotionalAmount);
    double fixedRate = Double.parseDouble(getWithException(swapDetails, FIXED_LEG_RATE));
    FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(fixedDayCount, fixedFrequency, fixedRegionIdentifier, fixedBusinessDayConvention, fixedNotional, false, fixedRate);
    
    DayCount floatingDayCount = DayCountFactory.INSTANCE.getDayCount(getWithException(swapDetails, FLOATING_LEG_DAYCOUNT));
    Frequency floatingFrequency = SimpleFrequencyFactory.INSTANCE.getFrequency(getWithException(swapDetails, FLOATING_LEG_FREQUENCY));
    ExternalId floatingRegionIdentifier = RegionUtils.countryRegionId(Country.of(getWithException(swapDetails, FLOATING_LEG_REGION)));
    BusinessDayConvention floatingBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(getWithException(swapDetails, FLOATING_LEG_BUS_DAY_CONVENTION));
    Currency floatingCurrency = Currency.of(getWithException(swapDetails, FLOATING_LEG_CURRENCY));
    double floatingNotionalAmount = Double.parseDouble(getWithException(swapDetails, FLOATING_LEG_NOTIONAL));
    Notional floatingNotional = new InterestRateNotional(floatingCurrency, floatingNotionalAmount);
    // TODO: not sure that this actually does anything, or what identifier we're looking for - just invented something for now
    String floatingReferenceRate = getWithException(swapDetails, FLOATING_LEG_REFERENCE);
    ExternalId floatingReferenceRateIdentifier = ExternalId.of("Ref", floatingReferenceRate);
    
    FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(floatingDayCount, floatingFrequency,
        floatingRegionIdentifier, floatingBusinessDayConvention, floatingNotional, false, floatingReferenceRateIdentifier, FloatingRateType.IBOR);
    
    double floatingInitialRate = Double.parseDouble(getWithException(swapDetails, FLOATING_LEG_RATE));
    floatingLeg.setInitialFloatingRate(floatingInitialRate);
    
    LocalDateTime tradeDate = LocalDateTime.of(LocalDate.parse(getWithException(swapDetails, TRADE_DATE), PortfolioLoaderHelper.CSV_DATE_FORMATTER), LocalTime.MIDNIGHT);
    LocalDateTime effectiveDate = LocalDateTime.of(LocalDate.parse(getWithException(swapDetails, EFFECTIVE_DATE), PortfolioLoaderHelper.CSV_DATE_FORMATTER), LocalTime.MIDNIGHT);
    LocalDateTime terminationDate = LocalDateTime.of(LocalDate.parse(getWithException(swapDetails, TERMINATION_DATE), PortfolioLoaderHelper.CSV_DATE_FORMATTER), LocalTime.MIDNIGHT);
    
    String fixedLegDescription = PortfolioLoaderHelper.RATE_FORMATTER.format(fixedRate);
    String floatingLegDescription = floatingReferenceRate;
    
    boolean isPayFixed = Boolean.parseBoolean(getWithException(swapDetails, PAY_FIXED));
    SwapLeg payLeg;
    String payLegDescription;
    SwapLeg receiveLeg;
    String receiveLegDescription;
    if (isPayFixed) {
      payLeg = fixedLeg;
      payLegDescription = fixedLegDescription;
      receiveLeg = floatingLeg;
      receiveLegDescription = floatingLegDescription;
    } else {
      payLeg = floatingLeg;
      payLegDescription = floatingLegDescription;
      receiveLeg = fixedLeg;
      receiveLegDescription = fixedLegDescription;
    }
    
    SwapSecurity swap = new SwapSecurity(tradeDate.atZone(TimeZone.UTC), effectiveDate.atZone(TimeZone.UTC),
        terminationDate.atZone(TimeZone.UTC), "Cpty Name", payLeg, receiveLeg);
    
    // Assume notional / currencies are the same for both legs - the name is really just to give us something to display anyway
    swap.setName("IR Swap " + PortfolioLoaderHelper.NOTIONAL_FORMATTER.format(fixedNotionalAmount) + " " + fixedCurrency + " " +
        terminationDate.toString(PortfolioLoaderHelper.OUTPUT_DATE_FORMATTER) + " - " + payLegDescription + " / " + receiveLegDescription);
    
    return swap;
  }

}
