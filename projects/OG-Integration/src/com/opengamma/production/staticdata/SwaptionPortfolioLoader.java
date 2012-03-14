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
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
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
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SwaptionPortfolioLoader extends AbstractProductionTool {

  private static Logger s_logger = LoggerFactory.getLogger(SwaptionPortfolioLoader.class);
  private static final String ID_SCHEME = "SWAPTION_LOADER";
  
  private static final String EXPIRY = "expiry";
  private static final String IS_LONG = "long";
  private static final String IS_PAYER = "payer";
  private static final String CURRENCY = "currency";
  private static final String TRADE_DATE = "trade date";
  //private static final String PREMIUM_DATE = "premium date";
  //private static final String PREMIUM_AMOUNT = "premium amount";
  private static final String STRIKE = "strike";
  private static final String NOTIONAL = "notional";
  private static final String COUNTERPARTY = "counterparty";
  private static final String SWAP_LENGTH = "swap length"; 
  
  private static final ConventionBundleSource CONVENTIONS = new DefaultConventionBundleSource(new InMemoryConventionBundleMaster());

  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    new SwaptionPortfolioLoader().initAndRun(args);
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
    
    Collection<Pair<SwaptionSecurity, SwapSecurity>> swaptions = parseSwaptions(filename);
    if (swaptions.size() == 0) {
      throw new OpenGammaRuntimeException("No (valid) swaptions were found in the specified file.");
    }
    
    if (getCommandLine().hasOption(PortfolioLoaderHelper.WRITE_OPT)) {
      persistToPortfolio(swaptions, portfolioName);
    }
  }
  
  private void persistToPortfolio(Collection<Pair<SwaptionSecurity, SwapSecurity>> swaptions, String portfolioName) {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    PositionMaster positionMaster = getToolContext().getPositionMaster();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    
    ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);
    
    for (Pair<SwaptionSecurity, SwapSecurity> swaption : swaptions) {
      SecurityDocument swaptionToAddDoc = new SecurityDocument();       
      swaptionToAddDoc.setSecurity(swaption.getFirst());
      securityMaster.add(swaptionToAddDoc);
      SecurityDocument swapToAddDoc = new SecurityDocument();        
      swapToAddDoc.setSecurity(swaption.getSecond());
      securityMaster.add(swapToAddDoc);
      ManageablePosition swaptionPosition = new ManageablePosition(BigDecimal.ONE, swaption.getFirst().getExternalIdBundle());
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(swaptionPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }
  
  private Collection<Pair<SwaptionSecurity, SwapSecurity>> parseSwaptions(String filename) {
    Collection<Pair<SwaptionSecurity, SwapSecurity>> swaptions = new ArrayList<Pair<SwaptionSecurity, SwapSecurity>>();
    FileReader fileReader;
    try {
      fileReader = new FileReader(filename);
      CSVReader csvReader = new CSVReader(fileReader);
      
      String[] headers = csvReader.readNext();
      PortfolioLoaderHelper.normaliseHeaders(headers);
      
      String[] line;
      int rowIndex = 1;
      while ((line = csvReader.readNext()) != null) {
        Map<String, String> swaptionDetails = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
          if (i >= line.length) {
            // Run out of headers for this line
            break;
          }
          swaptionDetails.put(headers[i], line[i]);
        }
        try {
          Pair<SwaptionSecurity, SwapSecurity> swaptionTrade = parseSwaption(swaptionDetails);
          swaptionTrade.getFirst().addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
          swaptions.add(swaptionTrade);
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
    sb.append("Parsed ").append(swaptions.size()).append(" swaptions:\n");
    for (Pair<SwaptionSecurity, SwapSecurity> swaption : swaptions) {
      sb.append("\t").append(swaption.getFirst().getName()).append("\n");
    }
    s_logger.info(sb.toString());    
    return swaptions;
  }

  private Pair<SwaptionSecurity, SwapSecurity> parseSwaption(Map<String, String> swaptionDetails) {
    String counterparty = getWithException(swaptionDetails, COUNTERPARTY);
    Currency currency = Currency.of(getWithException(swaptionDetails, CURRENCY));
    ConventionBundle swaptionConvention = CONVENTIONS.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_SWAPTION")); 
    ConventionBundle swapConvention = CONVENTIONS.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_SWAP"));
    final ConventionBundle floatingRateConvention = CONVENTIONS.getConventionBundle(swapConvention.getSwapFloatingLegInitialRate());
    Expiry swaptionExpiry = new Expiry(
        ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(getWithException(swaptionDetails, EXPIRY), PortfolioLoaderHelper.CSV_DATE_FORMATTER), LocalTime.MIDNIGHT), TimeZone.UTC), 
        ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    boolean isLong = Boolean.parseBoolean(getWithException(swaptionDetails, IS_LONG));
    boolean isCashSettled = swaptionConvention.isCashSettled();    
    boolean isPayer = Boolean.parseBoolean(getWithException(swaptionDetails, IS_PAYER));
    double strike = Double.parseDouble(getWithException(swaptionDetails, STRIKE));
    double notional = 1000000 * Double.parseDouble(getWithException(swaptionDetails, NOTIONAL));
    InterestRateNotional fixedNotional = new InterestRateNotional(currency, notional);
    InterestRateNotional floatingNotional = new InterestRateNotional(currency, notional);
    final ExternalId floatingRateBloombergTicker = floatingRateConvention.getIdentifiers().getExternalId(SecurityUtils.BLOOMBERG_TICKER);
    FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(swapConvention.getSwapFixedLegDayCount(), 
        swapConvention.getSwapFixedLegFrequency(), 
        swapConvention.getSwapFixedLegRegion(), 
        swapConvention.getSwapFixedLegBusinessDayConvention(), 
        fixedNotional, 
        false, strike);
    
    FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(swapConvention.getSwapFloatingLegDayCount(), 
        swapConvention.getSwapFloatingLegFrequency(), 
        swapConvention.getSwapFloatingLegRegion(), 
        swapConvention.getSwapFloatingLegBusinessDayConvention(), 
        floatingNotional,
        false, floatingRateBloombergTicker,
        FloatingRateType.IBOR);
    
    ZonedDateTime swapTradeDate = swaptionExpiry.getExpiry();
    ZonedDateTime swapEffectiveDate = swaptionExpiry.getExpiry();
    String swapLength = getWithException(swaptionDetails, SWAP_LENGTH);
    Period swapMaturity = Period.ofYears(Integer.parseInt(swapLength));
    ZonedDateTime swapMaturityDate = swaptionExpiry.getExpiry().plus(swapMaturity);
    SwapSecurity swap = new SwapSecurity(swapTradeDate, swapEffectiveDate, swapMaturityDate, counterparty, floatingLeg, fixedLeg);
    ExternalId swapIdentifier = ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString());
    swap.addExternalId(swapIdentifier);
    LocalDate tradeDate = LocalDate.parse(getWithException(swaptionDetails, TRADE_DATE), PortfolioLoaderHelper.CSV_DATE_FORMATTER);
//    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(11, 0), ZoneOffset.ofHours(0));
//    LocalDate premiumDate =  LocalDate.parse(getWithException(swaptionDetails, PREMIUM_DATE), PortfolioLoaderHelper.CSV_DATE_FORMATTER);
//    OffsetTime premiumTime = OffsetTime.of(LocalTime.of(11, 0), ZoneOffset.ofHours(0));
//    double premium = Double.parseDouble(getWithException(swaptionDetails, PREMIUM_AMOUNT));    
    SwaptionSecurity swaption = new SwaptionSecurity(isPayer, swapIdentifier, isLong, swaptionExpiry, isCashSettled, currency);
    swaption.setName("Vanilla swaption, " + getSwaptionString(swapLength, tradeDate, swaptionExpiry.getExpiry()) + ", " + currency.getCode()
        + " " + PortfolioLoaderHelper.NOTIONAL_FORMATTER.format(notional) + " @ " + 
        PortfolioLoaderHelper.RATE_FORMATTER.format(strike));
//    TradeImpl swaptionTrade = new TradeImpl();
//    swaptionTrade.setPremium(premium);
//    swaptionTrade.setPremiumDate(premiumDate);
//    swaptionTrade.setPremiumTime(premiumTime);
//    swaptionTrade.setQuantity(new BigDecimal(1));
//    swaptionTrade.setSecurity(swaption);
//    swaptionTrade.setTradeDate(tradeDate);
//    swaptionTrade.setTradeTime(tradeTime);
//    swaptionTrade.setCounterparty(new CounterpartyImpl(Identifier.of(ID_SCHEME, counterparty)));
//    swaptionTrade.setSecurityKey(securityKey)
    return Pair.of(swaption, swap);
  }

  private String getSwaptionString(String swapLength, LocalDate tradeDate, ZonedDateTime expiry) {
    long daysBetween = DateUtils.getDaysBetween(tradeDate, expiry);
    if (daysBetween < 365) {
      int months = (int) (daysBetween / 12.);
      return months + "M x " + swapLength + "Y";
    }
    int years = (int) (daysBetween / 365);
    return years + "Y x " + swapLength + "Y";
  }
}
