/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Generates a portfolio of FX forwards.
 */
public class FxForwardPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The list of forwards */
  private static final List<FXForwardSecurity> FX_FORWARDS = new ArrayList<>();
  /** The spot rates for a currency pair */
  private static final List<Pair<UnorderedCurrencyPair, Double>> SPOT_RATES = new ArrayList<>();
  /** The region */
  private static final ExternalId REGION = ExternalId.of("Region", "US");
  /** The forward rate formatter */
  private static final DecimalFormat RATE_FORMATTER = new DecimalFormat("###.###");

  static {
    SPOT_RATES.add(Pairs.of(UnorderedCurrencyPair.of(Currency.USD, Currency.EUR), 1.328));
    SPOT_RATES.add(Pairs.of(UnorderedCurrencyPair.of(Currency.USD, Currency.CHF), 0.84));
    SPOT_RATES.add(Pairs.of(UnorderedCurrencyPair.of(Currency.USD, Currency.AUD), 1.1));
    SPOT_RATES.add(Pairs.of(UnorderedCurrencyPair.of(Currency.USD, Currency.GBP), 1.588));
    SPOT_RATES.add(Pairs.of(UnorderedCurrencyPair.of(Currency.USD, Currency.JPY), 80.));
    final Random rng = new Random(1239);
    final ZonedDateTime date = DateUtils.previousWeekDay().atStartOfDay(ZoneOffset.UTC);
    for (int i = 0; i < 100; i++) {
      final int n = rng.nextInt(4);
      final Pair<UnorderedCurrencyPair, Double> pair = SPOT_RATES.get(n);
      final UnorderedCurrencyPair ccys = pair.getFirst();
      final double spot = pair.getSecond();
      final Currency payCurrency, receiveCurrency;
      final double payAmount, receiveAmount;
      final double forwardRate;
      if (rng.nextBoolean()) {
        payCurrency = ccys.getFirstCurrency();
        payAmount = 100000 * (1 + rng.nextInt(10)) / 100.;
        receiveCurrency = ccys.getSecondCurrency();
        receiveAmount = payAmount * spot * (1 + rng.nextDouble() / 20);
        forwardRate = payAmount / receiveAmount;
      } else {
        receiveCurrency = ccys.getFirstCurrency();
        receiveAmount = 100000 * (1 + rng.nextInt(10)) / 100.;
        payCurrency = ccys.getSecondCurrency();
        payAmount = receiveAmount * spot * (1 + rng.nextDouble() / 20);
        forwardRate = payAmount / receiveAmount;
      }
      final ZonedDateTime maturity = date.plusMonths(1 + rng.nextInt(20));
      final FXForwardSecurity forward = new FXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, maturity, REGION);
      final StringBuilder sb = new StringBuilder();
      sb.append(maturity.toLocalDate());
      sb.append(" ");
      sb.append(payCurrency);
      sb.append("/");
      sb.append(receiveCurrency);
      sb.append(" @ ");
      sb.append(RATE_FORMATTER.format(forwardRate));
      forward.setName(sb.toString());
      FX_FORWARDS.add(forward);
    }
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SecurityGenerator<FXForwardSecurity> securities = createFXForwardSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("FX Forwards"), positions, FX_FORWARDS.size());
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final SecurityGenerator<FXForwardSecurity> securities = createFXForwardSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("FX Forwards"), positions, FX_FORWARDS.size());
  }

  private SecurityGenerator<FXForwardSecurity> createFXForwardSecurityGenerator() {
    final SecurityGenerator<FXForwardSecurity> securities = new SecurityGenerator<FXForwardSecurity>() {
      private int _count;

      @SuppressWarnings("synthetic-access")
      @Override
      public FXForwardSecurity createSecurity() {
        final FXForwardSecurity fxForward = FX_FORWARDS.get(_count++);
        return fxForward;
      }

    };
    configure(securities);
    return securities;
  }

}
