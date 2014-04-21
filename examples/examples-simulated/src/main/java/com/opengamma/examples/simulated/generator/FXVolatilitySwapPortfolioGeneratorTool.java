/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

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
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.financial.security.swap.VolatilitySwapType;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Creates a portfolio of FX volatility swaps.
 */
public class FXVolatilitySwapPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The list of volatility swaps */
  private static final List<ManageableSecurity> FX_VOLATILITY_SWAPS = new ArrayList<>();
  /** The strike formatter */
  private static final DecimalFormat STRIKE_FORMATTER = new DecimalFormat("###.###");

  static {
    final List<Currency> counterCurrencies = Arrays.asList(Currency.JPY, Currency.GBP, Currency.EUR, Currency.CHF);
    final double baseStrike = 0.1;
    final double baseNotional = 100000;
    final Random rng = new Random(129);
    for (int i = 0; i < 4; i++) {
      final int years = 1 + rng.nextInt(4);
      final double notional = baseNotional * years;
      final double strike = baseStrike * (0.001 + rng.nextDouble());
      final ZonedDateTime baseDate = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTime settlementDate = baseDate.plusMonths(5 - rng.nextInt(10));
      final ZonedDateTime maturityDate = settlementDate.plusYears(years);
      final ZonedDateTime firstObservationDate = settlementDate;
      final ZonedDateTime lastObservationDate = maturityDate;
      final Currency counterCurrency = counterCurrencies.get(rng.nextInt(4));
      final FXVolatilitySwapSecurity security = new FXVolatilitySwapSecurity(Currency.USD, notional, VolatilitySwapType.VOLATILITY, strike, settlementDate,
          maturityDate, 252., firstObservationDate, lastObservationDate, PeriodFrequency.DAILY, Currency.USD, counterCurrency);
      final StringBuilder sb = new StringBuilder("USD/");
      sb.append(counterCurrency.getCode());
      sb.append(" @ ");
      sb.append(STRIKE_FORMATTER.format(100 * strike));
      sb.append("%, start=");
      sb.append(settlementDate.toLocalDate());
      sb.append(", maturity=");
      sb.append(maturityDate.toLocalDate());
      security.setName(sb.toString());
      FX_VOLATILITY_SWAPS.add(security);
    }
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SecurityGenerator<ManageableSecurity> securities = createFXVolatilitySwapSecurityGenerator(FX_VOLATILITY_SWAPS.size());
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("FX Volatility Swaps"), positions, FX_VOLATILITY_SWAPS.size());
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final SecurityGenerator<ManageableSecurity> securities = createFXVolatilitySwapSecurityGenerator(FX_VOLATILITY_SWAPS.size());
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("FX Volatility Swaps"), positions, FX_VOLATILITY_SWAPS.size());
  }

  /**
   * Creates a security generator that loops over the list of FX volatility swaps.
   * @size The expected size of the portfolio
   * @return The security generator
   */
  private SecurityGenerator<ManageableSecurity> createFXVolatilitySwapSecurityGenerator(final int size) {
    final SecurityGenerator<ManageableSecurity> securities = new SecurityGenerator<ManageableSecurity>() {
      private int _count;

      @SuppressWarnings("synthetic-access")
      @Override
      public ManageableSecurity createSecurity() {
        if (_count > size - 1) {
          throw new IllegalStateException("Should not ask for more than " + size + " securities");
        }
        final ManageableSecurity fxVolatilitySwap = FX_VOLATILITY_SWAPS.get(_count++);
        return fxVolatilitySwap;
      }

    };
    configure(securities);
    return securities;
  }
}
