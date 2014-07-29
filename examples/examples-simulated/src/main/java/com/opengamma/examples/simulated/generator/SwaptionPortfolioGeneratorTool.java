/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.LeafPortfolioNodeGenerator;
import com.opengamma.financial.generator.NameGenerator;
import com.opengamma.financial.generator.PortfolioGenerator;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.PositionGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Generates a portfolio of multi-currency swaptions.
 */
public class SwaptionPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The strike formatter */
  private static final DecimalFormat STRIKE_FORMATTER = new DecimalFormat("#.####");
  /** The day count */
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  /** The business day convention */
  private static final BusinessDayConvention BDC = BusinessDayConventions.FOLLOWING;
  /** Map of currency to region */
  private static final Map<Currency, ExternalId> REGIONS = new HashMap<>();
  /** Map of currency to synthetic ibor tickers */
  private static final Map<Currency, ExternalId> TICKERS = new HashMap<>();

  static {
    REGIONS.put(Currency.USD, ExternalSchemes.financialRegionId("US"));
    REGIONS.put(Currency.EUR, ExternalSchemes.financialRegionId("EU"));
    REGIONS.put(Currency.GBP, ExternalSchemes.financialRegionId("GB"));
    REGIONS.put(Currency.JPY, ExternalSchemes.financialRegionId("JP"));
    REGIONS.put(Currency.CHF, ExternalSchemes.financialRegionId("CH"));
    TICKERS.put(Currency.USD, ExternalSchemes.syntheticSecurityId("USDLIBORP3M"));
    TICKERS.put(Currency.EUR, ExternalSchemes.syntheticSecurityId("EUREURIBORP6M"));
    TICKERS.put(Currency.GBP, ExternalSchemes.syntheticSecurityId("GBPLIBORP6M"));
    TICKERS.put(Currency.JPY, ExternalSchemes.syntheticSecurityId("JPYLIBORP6M"));
    TICKERS.put(Currency.CHF, ExternalSchemes.syntheticSecurityId("CHFLIBORP6M"));
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SwaptionSecurity[] swaptions = createSwaptions(PORTFOLIO_SIZE);
    final SecurityGenerator<SwaptionSecurity> securities = createSwaptionSecurityGenerator(swaptions, PORTFOLIO_SIZE);
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("Swaptions"), positions, PORTFOLIO_SIZE);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final SwaptionSecurity[] swaptions = createSwaptions(size);
    final SecurityGenerator<SwaptionSecurity> securities = createSwaptionSecurityGenerator(swaptions, size);
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Swaptions"), positions, size);
  }

  private SwaptionSecurity[] createSwaptions(final int size) {
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    final List<Currency> currencies = new ArrayList<>(REGIONS.keySet());
    final ZonedDateTime[] tradeDates = new ZonedDateTime[size];
    final Random rng = new Random(123);
    final ZonedDateTime date = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
    Arrays.fill(tradeDates, date);
    final SwaptionSecurity[] swaptions = new SwaptionSecurity[size];
    for (int i = 0; i < size; i++) {
      final Currency currency = currencies.get(rng.nextInt(currencies.size()));
      final ExternalId region = REGIONS.get(currency);
      final ExternalId floatingRate = TICKERS.get(currency);
      final int swaptionYears = 1 + rng.nextInt(9);
      final ZonedDateTime swaptionExpiry = date.plusYears(swaptionYears);
      final int swapYears = 1 + rng.nextInt(28);
      final ZonedDateTime swapMaturity = swaptionExpiry.plusMonths(swapYears);
      final double amount = 100000 * (1 + rng.nextInt(30));
      final InterestRateNotional notional = new InterestRateNotional(currency, amount);
      final double rate = swapYears * rng.nextDouble() / 500;
      final Frequency frequency = currency.equals(Currency.USD) ? PeriodFrequency.QUARTERLY : PeriodFrequency.SEMI_ANNUAL;
      final SwapLeg fixedLeg = new FixedInterestRateLeg(DAY_COUNT, PeriodFrequency.SEMI_ANNUAL, region, BDC, notional, false, rate);
      final SwapLeg floatLeg = new FloatingInterestRateLeg(DAY_COUNT, frequency, region, BDC, notional, false, floatingRate, FloatingRateType.IBOR);
      final SwapLeg payLeg, receiveLeg;
      final String swapName, swaptionName;
      final boolean isLong = rng.nextBoolean();
      final boolean isCashSettled = rng.nextBoolean();
      final boolean payer;
      if (rng.nextBoolean()) {
        payLeg = fixedLeg;
        receiveLeg = floatLeg;
        swapName = swapYears + "Y pay " + currency + " " + notional.getAmount() + " @ " + STRIKE_FORMATTER.format(rate);
        swaptionName = (isLong ? "Long " : "Short ") + swaptionYears + "Y x " + swapYears + "Y pay " + currency + " " + notional.getAmount() + " @ " + STRIKE_FORMATTER.format(rate);
        payer = true;
      } else {
        payLeg = floatLeg;
        receiveLeg = fixedLeg;
        swapName = swapYears + "Y receive " + currency + " " + notional.getAmount() + " @ " + STRIKE_FORMATTER.format(rate);
        swaptionName = (isLong ? "Long " : "Short ") + swaptionYears + "Y x " + swapYears + "Y receive " + currency + " " + notional.getAmount() + " @ " + STRIKE_FORMATTER.format(rate);
        payer = false;
      }
      final SwapSecurity swap = new SwapSecurity(swaptionExpiry, swaptionExpiry.plusDays(2), swapMaturity, COUNTER_PARTY_OPT, payLeg, receiveLeg);
      swap.setName(swapName);
      final SecurityDocument toAddDoc = new SecurityDocument();
      toAddDoc.setSecurity(swap);
      securityMaster.add(toAddDoc);
      final ExternalId swapId = getSecurityPersister().storeSecurity(swap).iterator().next();
      final SwaptionSecurity swaption = new SwaptionSecurity(payer, swapId, isLong, new Expiry(swaptionExpiry), isCashSettled, currency);
      swaption.setName(swaptionName);
      swaptions[i] = swaption;
    }
    return swaptions;
  }

  private SecurityGenerator<SwaptionSecurity> createSwaptionSecurityGenerator(final SwaptionSecurity[] swaptions, final int size) {
    final SecurityGenerator<SwaptionSecurity> securities = new SecurityGenerator<SwaptionSecurity>() {
      private int _count;

      @Override
      public SwaptionSecurity createSecurity() {
        if (_count > size - 1) {
          throw new IllegalStateException("Should not ask for more than " + size + " securities");
        }
        return swaptions[_count++];
      }
    };
    configure(securities);
    return securities;
  };

}
