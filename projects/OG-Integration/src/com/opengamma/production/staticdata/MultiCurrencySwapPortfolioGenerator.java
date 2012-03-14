/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.staticdata;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.portfolio.loader.PortfolioLoaderHelper;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
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
import com.opengamma.util.time.Tenor;

/**
 * Quick utility that constructs a portfolio from a CSV file of swaps.
 */
public class MultiCurrencySwapPortfolioGenerator extends AbstractProductionTool {

  private static Logger s_logger = LoggerFactory.getLogger(MultiCurrencySwapPortfolioGenerator.class);

  private static Currency[] s_currencies;

  private static Tenor[] s_tenors;

  /**
   * The scheme used for an identifier which is added to each swap created from the CSV file
   */
  private static final String ID_SCHEME = "SWAP_GENERATOR";

  private static final int DAYS_TRADING = 60;

  private static final String HISTORICAL_CONFIG_DOC = "DEFAULT_TSS_CONFIG";

  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    new MultiCurrencySwapPortfolioGenerator().initAndRun(args);
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
    String portfolioName = getCommandLine().getOptionValue(PortfolioGeneratorHelper.PORTFOLIO_NAME_OPT);
    
    Collection<SwapSecurity> swaps = createRandomSwaps();
    if (swaps.size() == 0) {
      throw new OpenGammaRuntimeException("No (valid) swaps were found in the specified file.");
    }
    
    if (getCommandLine().hasOption(PortfolioLoaderHelper.WRITE_OPT)) {
      persistToPortfolio(swaps, portfolioName);
    }
  }

  private Collection<SwapSecurity> createRandomSwaps() {
    Collection<SwapSecurity> swaps = new ArrayList<SwapSecurity>();
    try {
      ConfigMaster configMaster = getToolContext().getConfigMaster();
      Validate.notNull(configMaster, "ConfigMaster");
      ConfigSource configSource = new MasterConfigSource(configMaster);
      Validate.notNull(configSource, "ConfigSource");
      ConventionBundleSource conventionBundleSource = getToolContext().getConventionBundleSource();
      Validate.notNull(conventionBundleSource, "ConventionBundleSource");
      HolidaySource holidaySource = getToolContext().getHolidaySource();
      Validate.notNull(holidaySource, "HolidaySource");
      HistoricalTimeSeriesSource historicalSource = getToolContext().getHistoricalTimeSeriesSource();
      Validate.notNull(historicalSource, "HistoricalTimeSeriesSource");
      SecureRandom random = new SecureRandom();
      for (int i = 0; i < 200; i++) {
        SwapSecurity swap = makeSwap(random, conventionBundleSource, configSource, holidaySource, historicalSource);
        if (swap != null) {
          swaps.add(swap);
        }
      }
      s_logger.info("Created " + swaps.size() + " swaps");
      
    } catch (OpenGammaRuntimeException ogre) {
      s_logger.error("An OpenGamma runtime execption occurred", ogre);
    }
    return swaps;
  }
  
  static {
    s_currencies = new Currency[] {Currency.USD, Currency.GBP, Currency.EUR, Currency.JPY, Currency.CHF }; 
                                   //Currency.AUD, Currency.SEK, Currency.NOK };
    s_tenors = new Tenor[] {Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS, 
                            Tenor.ofYears(7), Tenor.ofYears(10), Tenor.ofYears(12), Tenor.ofYears(15), Tenor.ofYears(20) };
  }
  
  private SwapSecurity makeSwap(
      Random random, ConventionBundleSource conventionSource, ConfigSource configSource,
      HolidaySource holidaySource, HistoricalTimeSeriesSource historicalSource) {
    int offset = random.nextInt(s_currencies.length);
    Currency ccy = s_currencies[offset];
    LocalDate tradeDate;
    do {
      tradeDate = LocalDate.now().minusDays(random.nextInt(DAYS_TRADING));
    } while (holidaySource.isHoliday(tradeDate, ccy));
    ConventionBundle swapConvention = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, ccy.getCode() + "_SWAP"));
    if (swapConvention == null) {
      s_logger.error("Couldn't get swap convention for " + ccy.getCode());
      return null;
    }
    Tenor maturity = s_tenors[random.nextInt(s_tenors.length)];
    // get the convention of the identifier of the initial rate
    ConventionBundle liborConvention = conventionSource.getConventionBundle(swapConvention.getSwapFloatingLegInitialRate());
    if (liborConvention == null) {
      s_logger.error("Couldn't get libor convention for " + swapConvention.getSwapFloatingLegInitialRate());
      return null;
    }
    // look up the Bloomberg ticker out of the bundle
    ExternalId bbgIdentifier = liborConvention.getIdentifiers().getExternalId(SecurityUtils.BLOOMBERG_TICKER);
    // look up the value on our chosen trade date
    HistoricalTimeSeries initialRateSeries = historicalSource.getHistoricalTimeSeries(
        MarketDataRequirementNames.MARKET_VALUE, bbgIdentifier.toBundle(), HISTORICAL_CONFIG_DOC, tradeDate, true, tradeDate, true);
    if (initialRateSeries == null || initialRateSeries.getTimeSeries().isEmpty()) {
      s_logger.error("couldn't get series for " + bbgIdentifier + " on " + tradeDate);
      return null;
    }
    Double initialRate = initialRateSeries.getTimeSeries().getEarliestValue();  
    
    // get the identifier for the swap rate for the maturity we're interested in (assuming the fixed rate will be =~ swap rate)
    ExternalId swapRateForMaturityIdentifier = getSwapRateFor(configSource, ccy, tradeDate, maturity);
    if (swapRateForMaturityIdentifier == null) {
      s_logger.error("Couldn't get swap rate identifier for " + ccy + " [" + maturity + "]" + " from " + tradeDate);
      return null;
    }
    HistoricalTimeSeries fixedRateSeries = historicalSource.getHistoricalTimeSeries(
        MarketDataRequirementNames.MARKET_VALUE, swapRateForMaturityIdentifier.toBundle(), HISTORICAL_CONFIG_DOC, tradeDate, true, tradeDate, true);
    if (fixedRateSeries == null) {
      s_logger.error("can't find time series for " + swapRateForMaturityIdentifier + " on " + tradeDate);
      return null;
    }
    Double fixedRate = (fixedRateSeries.getTimeSeries().getEarliestValue() + random.nextDouble()) / 100d;
    Double notional = (double) random.nextInt(100000) * 1000;
    ZonedDateTime tradeDateTime = ZonedDateTime.of(tradeDate, LocalTime.MIDNIGHT, TimeZone.UTC);
    ZonedDateTime maturityDateTime = ZonedDateTime.of(tradeDate.plus(maturity.getPeriod()), LocalTime.MIDNIGHT, TimeZone.UTC);
    String counterparty = "CParty";
    
    SwapLeg fixedLeg = new FixedInterestRateLeg(swapConvention.getSwapFixedLegDayCount(), 
        swapConvention.getSwapFixedLegFrequency(), 
        swapConvention.getSwapFixedLegRegion(), 
        swapConvention.getSwapFixedLegBusinessDayConvention(), 
        new InterestRateNotional(ccy, notional), 
        false, fixedRate);
    
    FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(swapConvention.getSwapFloatingLegDayCount(), 
        swapConvention.getSwapFloatingLegFrequency(), 
        swapConvention.getSwapFloatingLegRegion(), 
        swapConvention.getSwapFloatingLegBusinessDayConvention(), 
        new InterestRateNotional(ccy, notional), 
        false, ExternalId.of(bbgIdentifier.getScheme().toString(), bbgIdentifier.getValue()), 
        FloatingRateType.IBOR);
    floatingLeg.setInitialFloatingRate(initialRate);

    String fixedLegDescription = PortfolioLoaderHelper.RATE_FORMATTER.format(fixedRate);
    String floatingLegDescription = swapConvention.getSwapFloatingLegInitialRate().getValue();
    boolean isPayFixed = random.nextBoolean();
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
    SwapSecurity swap = new SwapSecurity(tradeDateTime, tradeDateTime, maturityDateTime, counterparty, payLeg, receiveLeg);
    swap.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    swap.setName("IR Swap " + ccy + " " + PortfolioLoaderHelper.NOTIONAL_FORMATTER.format(notional) + " " +
        maturityDateTime.toString(PortfolioLoaderHelper.OUTPUT_DATE_FORMATTER) + " - " + payLegDescription + " / " + receiveLegDescription);
    return swap;  
  }
  
  private ExternalId getSwapRateFor(ConfigSource configSource, Currency ccy, LocalDate tradeDate, Tenor tenor) {
    CurveSpecificationBuilderConfiguration curveSpecConfig = configSource.getByName(CurveSpecificationBuilderConfiguration.class, "DEFAULT_" + ccy.getCode(), null);
    ExternalId swapSecurity;
    if (ccy.equals(Currency.USD)) {
      // Standard (i.e. matches convention) floating leg tenor for USD is 3M
      swapSecurity = curveSpecConfig.getSwap3MSecurity(tradeDate, tenor);
    } else {
      // Standard (i.e. matches convention) floating leg tenor for CHF, JPY, GBP, EUR is 6M
      swapSecurity = curveSpecConfig.getSwap6MSecurity(tradeDate, tenor);
    }
    return swapSecurity;
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
      ManageableTrade swapTrade = new ManageableTrade(BigDecimal.ONE, swap.getExternalIdBundle(), swap.getTradeDate().toLocalDate(), 
                                                      swap.getTradeDate().toOffsetTime(), 
                                                      ExternalId.of("CParty", swap.getCounterparty()));
      swapPosition.addTrade(swapTrade);
      PositionDocument addedDoc = positionMaster.add(new PositionDocument(swapPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }

}
