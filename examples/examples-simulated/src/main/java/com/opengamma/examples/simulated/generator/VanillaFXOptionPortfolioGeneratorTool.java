/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.LeafPortfolioNodeGenerator;
import com.opengamma.financial.generator.NameGenerator;
import com.opengamma.financial.generator.PortfolioGenerator;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.PositionGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Generates a portfolio of approximately ATM FX options.
 */
public class VanillaFXOptionPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The list of options */
  private static final List<FXOptionSecurity> FX_OPTIONS = new ArrayList<>();
  /** The spot rates for a currency pair */
  private static final List<Pair<UnorderedCurrencyPair, Double>> SPOT_RATES = new ArrayList<>();
  /** The strike formatter */
  private static final DecimalFormat STRIKE_FORMATTER = new DecimalFormat("###.###");

  static {
    SPOT_RATES.add(Pairs.of(UnorderedCurrencyPair.of(Currency.USD, Currency.EUR), 1.328));
    SPOT_RATES.add(Pairs.of(UnorderedCurrencyPair.of(Currency.USD, Currency.CHF), 0.84));
    SPOT_RATES.add(Pairs.of(UnorderedCurrencyPair.of(Currency.USD, Currency.AUD), 1.1));
    SPOT_RATES.add(Pairs.of(UnorderedCurrencyPair.of(Currency.USD, Currency.GBP), 1.588));
    SPOT_RATES.add(Pairs.of(UnorderedCurrencyPair.of(Currency.USD, Currency.JPY), 80.));
    SPOT_RATES.add(Pairs.of(UnorderedCurrencyPair.of(Currency.GBP, Currency.EUR), 1.2));
    SPOT_RATES.add(Pairs.of(UnorderedCurrencyPair.of(Currency.CHF, Currency.JPY), 100.));
    final ExerciseType european = new EuropeanExerciseType();
    final Random rng = new Random(1237);
    final ZonedDateTime date = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
    for (int i = 0; i < 100; i++) {
      final int n = rng.nextInt(6);
      final Pair<UnorderedCurrencyPair, Double> pair = SPOT_RATES.get(n);
      final UnorderedCurrencyPair ccys = pair.getFirst();
      final double spot = pair.getSecond();
      final Currency putCurrency, callCurrency;
      final double putAmount, callAmount;
      double strike;
      if (rng.nextBoolean()) {
        putCurrency = ccys.getFirstCurrency();
        putAmount = 100000 * (1 + rng.nextInt(10)) / 100.;
        callCurrency = ccys.getSecondCurrency();
        callAmount = putAmount * spot * (1 + rng.nextDouble() / 20);
        strike = putAmount / callAmount;
      } else {
        callCurrency = ccys.getFirstCurrency();
        callAmount = 100000 * (1 + rng.nextInt(10)) / 100.;
        putCurrency = ccys.getSecondCurrency();
        putAmount = callAmount * spot * (1 + rng.nextDouble() / 20);
        strike = putAmount / callAmount;
      }
      final boolean isLong = rng.nextBoolean() ? true : false;
      final Expiry expiry = new Expiry(date.plusMonths(1 + rng.nextInt(20)));
      final ZonedDateTime settlementDate = expiry.getExpiry().plusDays(2);
      final FXOptionSecurity option = new FXOptionSecurity(putCurrency, callCurrency, putAmount, callAmount, expiry, settlementDate, isLong, european);
      final StringBuilder sb = new StringBuilder();
      sb.append(isLong ? "Long " : "Short ");
      sb.append(expiry.getExpiry().toLocalDate());
      sb.append(" ");
      sb.append(putCurrency);
      sb.append("/");
      sb.append(callCurrency);
      sb.append(" @ ");
      sb.append(STRIKE_FORMATTER.format(strike));
      option.setName(sb.toString());
      FX_OPTIONS.add(option);
    }
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SecurityGenerator<FXOptionSecurity> securities = createFXOptionSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("FX Options"), positions, FX_OPTIONS.size());
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final SecurityGenerator<FXOptionSecurity> securities = createFXOptionSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("FX Options"), positions, FX_OPTIONS.size());
  }

  private SecurityGenerator<FXOptionSecurity> createFXOptionSecurityGenerator() {
    final SecurityGenerator<FXOptionSecurity> securities = new SecurityGenerator<FXOptionSecurity>() {
      private int _count;

      @SuppressWarnings("synthetic-access")
      @Override
      public FXOptionSecurity createSecurity() {
        final FXOptionSecurity fxOption = FX_OPTIONS.get(_count++);
        return fxOption;
      }

    };
    configure(securities);
    return securities;
  }
}
