/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.ShutdownUtils;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Example code to load a simple swaption portfolio.
 * <p>
 * This code is kept deliberately as simple as possible.
 * There are no checks for the securities or portfolios already existing, so if you run it
 * more than once you will get multiple copies portfolios and securities with the same names.
 * It is designed to run against the HSQLDB example database.
 */
public class ExampleSwaptionPortfolioLoader extends AbstractTool<IntegrationToolContext> {

  /** Logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleSwaptionPortfolioLoader.class);
  /** The currencies */
  public static final Currency[] CCYS = new Currency[] {Currency.USD};
  /** The swaption expiries */
  private static final Tenor[] EXPIRIES = new Tenor[] {Tenor.SIX_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS};
  /** The swap maturities */
  private static final Tenor[] MATURITIES = new Tenor[] {Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.FIVE_YEARS, Tenor.TEN_YEARS};
  /** The portfolio name */
  public static final String PORTFOLIO_NAME = "Swaption Portfolio";
  /** The size of the portfolio */
  private static final int SIZE = 300;
  /** The day-count */
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  /** The business day convention */
  private static final BusinessDayConvention BDC = BusinessDayConventions.MODIFIED_FOLLOWING;
  /** Map of currency to underlying libor index */
  private static final Map<Currency, ExternalId> IBOR = new HashMap<>();
  /** Map of currency to region */
  private static final Map<Currency, ExternalId> REGIONS = new HashMap<>();
  /** The strike formatter */
  private static final Format STRIKE_FORMATTER = new DecimalFormat("##.###");
  /** The counterparty */
  private static final String COUNTERPARTY = "Ctpty";
  /** The scheme used for an identifier which is added to each swaption created */
  private static final String ID_SCHEME = "SWAPTION_GENERATOR";

  static {
    IBOR.put(Currency.USD, ExternalSchemes.bloombergTickerSecurityId("US0003M Index"));
    REGIONS.put(Currency.USD, ExternalSchemes.countryRegionId(Country.US));
  }

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { //CSIGNORE
    try {
      boolean success = 
          new ExampleTimeSeriesRatingLoader().initAndRun(args, IntegrationToolContext.class) &&
          new ExampleSwaptionPortfolioLoader().initAndRun(args, IntegrationToolContext.class);
      ShutdownUtils.exit(success ? 0 : -1);
    } catch (Throwable ex) {
      ex.printStackTrace();
      ShutdownUtils.exit(-2);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    final Map<SwaptionSecurity, SwapSecurity> securities = createRandomSwaptions();
    if (securities.size() == 0) {
      throw new OpenGammaRuntimeException("No valid swaptions were generated");
    }
    persistToPortfolio(securities, PORTFOLIO_NAME);
  }

  private Map<SwaptionSecurity, SwapSecurity> createRandomSwaptions() {
    final Map<SwaptionSecurity, SwapSecurity> securities = new LinkedHashMap<>();
    final Random random = new Random(4567);
    for (int i = 0; i < SIZE; i++) {
      Pair<SwaptionSecurity, SwapSecurity> pair = null;
      try {
        final Currency ccy = CCYS[random.nextInt(CCYS.length)];
        final LocalDate tradeDate = getTradeDate(random, ccy);
        final Tenor swaptionExpiry = EXPIRIES[random.nextInt(EXPIRIES.length)];
        final Tenor swapMaturity = MATURITIES[random.nextInt(MATURITIES.length)];
        pair = makeSwaptionAndUnderlying(random, ccy, tradeDate, swaptionExpiry, swapMaturity);
      } catch (final Exception e) {
        e.printStackTrace();
      }
      if (pair != null) {
        securities.put(pair.getFirst(), pair.getSecond());
      }
    }
    final StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(securities.size()).append(" swaptions:\n");
    for (final Map.Entry<SwaptionSecurity, SwapSecurity> entry : securities.entrySet()) {
      sb.append("\t").append(entry.getKey().getName()).append("\n");
    }
    s_logger.info(sb.toString());
    return securities;
  }

  private Pair<SwaptionSecurity, SwapSecurity> makeSwaptionAndUnderlying(final Random random, final Currency ccy, final LocalDate tradeDate, final Tenor expiry, final Tenor maturity) {
    final ExternalId region = REGIONS.get(ccy);
    final ExternalId floatingRate = IBOR.get(ccy);
    final String swaptionString;
    final Period expiryPeriod = expiry.getPeriod();
    if (expiryPeriod.getYears() != 0) {
      swaptionString = expiryPeriod.getYears() + "Y";
    } else if (expiryPeriod.getMonths() != 0) {
      swaptionString = expiryPeriod.getMonths() + "M";
    } else {
      throw new OpenGammaRuntimeException("Could not handle swaption expiry " + expiry);
    }
    final String swapString;
    final Period maturityPeriod = maturity.getPeriod();
    if (maturityPeriod.getYears() != 0) {
      swapString = maturityPeriod.getYears() + "Y";
    } else if (maturityPeriod.getMonths() != 0) {
      swapString = maturityPeriod.getMonths() + "M";
    } else {
      throw new OpenGammaRuntimeException("Could not handle swap maturity " + maturity);
    }
    final ZonedDateTime swaptionExpiry = tradeDate.plus(expiryPeriod).atStartOfDay().atZone(ZoneOffset.UTC);
    final ZonedDateTime swapMaturity = swaptionExpiry.plus(maturity.getPeriod());
    final double amount = 100000 * (1 + random.nextInt(30));
    final InterestRateNotional notional = new InterestRateNotional(ccy, amount);
    final double rate = getSwapRate(ccy, tradeDate, maturity) * (1 + ((random.nextDouble() - 0.5) / 30.));
    final Frequency frequency = ccy.equals(Currency.USD) ? PeriodFrequency.QUARTERLY : PeriodFrequency.SEMI_ANNUAL;
    final SwapLeg fixedLeg = new FixedInterestRateLeg(DAY_COUNT, PeriodFrequency.SEMI_ANNUAL, region, BDC, notional, false, rate);
    final SwapLeg floatLeg = new FloatingInterestRateLeg(DAY_COUNT, frequency, region, BDC, notional, false, floatingRate, FloatingRateType.IBOR);
    final SwapLeg payLeg, receiveLeg;
    final String swapName, swaptionName;
    final boolean isLong = random.nextBoolean();
    final boolean isCashSettled = random.nextBoolean();
    final boolean payer;
    if (random.nextBoolean()) {
      payLeg = fixedLeg;
      receiveLeg = floatLeg;
      swapName = swapString + " pay " + ccy + " " + notional.getAmount() + " @ " + STRIKE_FORMATTER.format(rate);
      swaptionName = (isLong ? "Long " : "Short ") + swaptionString + " x " + swapString + " pay " + ccy + " " + notional.getAmount() + " @ " + STRIKE_FORMATTER.format(rate);
      payer = true;
    } else {
      payLeg = floatLeg;
      receiveLeg = fixedLeg;
      swapName = swapString + " receive " + ccy + " " + notional.getAmount() + " @ " + STRIKE_FORMATTER.format(rate);
      swaptionName = (isLong ? "Long " : "Short ") + swaptionString + " x " + swapString + " receive " + ccy + " " + notional.getAmount() + " @ " + STRIKE_FORMATTER.format(rate);
      payer = false;
    }
    final SwapSecurity swap = new SwapSecurity(swaptionExpiry, swaptionExpiry.plusDays(2), swapMaturity, COUNTERPARTY, payLeg, receiveLeg);
    swap.setName(swapName);
    final ExternalId underlyingId = ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString());
    swap.addExternalId(underlyingId);
    final SwaptionSecurity swaption = new SwaptionSecurity(payer, underlyingId, isLong, new Expiry(swaptionExpiry), isCashSettled, ccy);
    swaption.setName(swaptionName);
    swaption.addExternalId(ExternalId.of(ID_SCHEME, GUIDGenerator.generate().toString()));
    return Pairs.of(swaption, swap);
  }

  private LocalDate getTradeDate(final Random random, final Currency ccy) {
    final HolidaySource holidaySource = getToolContext().getHolidaySource();
    LocalDate tradeDate;
    do {
      tradeDate = DateUtils.previousWeekDay().minusDays(random.nextInt(30));
    } while (holidaySource.isHoliday(tradeDate, ccy));
    return tradeDate;
  }

  private Double getSwapRate(final Currency ccy, final LocalDate tradeDate, final Tenor maturity) {
    final HistoricalTimeSeriesSource historicalSource = getToolContext().getHistoricalTimeSeriesSource();
    final MasterConfigSource configSource = new MasterConfigSource(getToolContext().getConfigMaster());
    final ExternalId swapRateForMaturityIdentifier = getSwapRateFor(configSource, ccy, tradeDate, maturity);
    if (swapRateForMaturityIdentifier == null) {
      throw new OpenGammaRuntimeException("Couldn't get swap rate identifier for " + ccy + " [" + maturity + "]" + " from " + tradeDate);
    }

    final HistoricalTimeSeries fixedRateSeries = historicalSource.getHistoricalTimeSeries("PX_LAST",
        swapRateForMaturityIdentifier.toBundle(), HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, tradeDate.minusDays(30), true, tradeDate, true);
    if (fixedRateSeries == null) {
      throw new OpenGammaRuntimeException("Time series for " + swapRateForMaturityIdentifier + " was null");
    }
    if (fixedRateSeries.getTimeSeries().isEmpty()) {
      throw new OpenGammaRuntimeException("Time series for " + swapRateForMaturityIdentifier + " was empty");
    }
    return fixedRateSeries.getTimeSeries().getLatestValue() / 100;
  }

  private static ExternalId getSwapRateFor(final ConfigSource configSource, final Currency ccy, final LocalDate tradeDate, final Tenor tenor) {
    final CurveSpecificationBuilderConfiguration curveSpecConfig = configSource.getSingle(CurveSpecificationBuilderConfiguration.class, "DEFAULT_" + ccy.getCode(), VersionCorrection.LATEST);
    if (curveSpecConfig == null) {
      throw new OpenGammaRuntimeException("No curve spec builder configuration for DEFAULT_" + ccy.getCode());
    }
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

  private void persistToPortfolio(final Map<SwaptionSecurity, SwapSecurity> securities, final String portfolioName) {
    final PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    final PositionMaster positionMaster = getToolContext().getPositionMaster();
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();

    final ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);
    final ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName, rootNode);
    final PortfolioDocument portfolioDoc = new PortfolioDocument();
    portfolioDoc.setPortfolio(portfolio);

    for (final Map.Entry<SwaptionSecurity, SwapSecurity> entry : securities.entrySet()) {
      final SecurityDocument swapToAddDoc = new SecurityDocument();
      final SwapSecurity swap = entry.getValue();
      swapToAddDoc.setSecurity(swap);
      final SecurityDocument swaptionToAddDoc = new SecurityDocument();
      final SwaptionSecurity swaption = entry.getKey();
      swaptionToAddDoc.setSecurity(swaption);
      securityMaster.add(swaptionToAddDoc);
      securityMaster.add(swapToAddDoc);
      final ManageablePosition swaptionPosition = new ManageablePosition(BigDecimal.ONE, swaption.getExternalIdBundle());
      final PositionDocument addedDoc = positionMaster.add(new PositionDocument(swaptionPosition));
      rootNode.addPosition(addedDoc.getUniqueId());
    }
    portfolioMaster.add(portfolioDoc);
  }
}
