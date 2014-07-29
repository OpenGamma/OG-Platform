/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Month;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.generator.TreePortfolioNodeGenerator;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Generates 10 years of synthetic ER future securities and stores them in a portfolio.
 */
public class ERFutureForCurvePortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The currency */
  private static final Currency CURRENCY = Currency.EUR;
  /** Contains month codes for futures */
  private static final Map<Month, String> MONTHS;
  /** The underlying id */
  private static final ExternalId EURIBOR_3M = ExternalSchemes.syntheticSecurityId("EUREURIBORP3M");
  /** Gets the future expiry date */
  private static final TemporalAdjuster THIRD_WED_ADJUSTER = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);
  /** The counterparty */
  private static final String COUNTERPARTY = "Cpty";

  static {
    MONTHS = new HashMap<>();
    MONTHS.put(Month.MARCH, "H");
    MONTHS.put(Month.JUNE, "M");
    MONTHS.put(Month.SEPTEMBER, "U");
    MONTHS.put(Month.DECEMBER, "Z");
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final FutureSecurityGenerator<ManageableSecurity> generator = getIRFutureSecurityGenerator();
    configure(generator);
    final TreePortfolioNodeGenerator rootNode = new TreePortfolioNodeGenerator(new StaticNameGenerator("ER Future Portfolio"));
    rootNode.addChildNode(generator);
    return rootNode;
  }

  private FutureSecurityGenerator<ManageableSecurity> getIRFutureSecurityGenerator() {
    final ZonedDateTime tradeDate = DateUtils.getUTCDate(2013, 3, 1);
    final ZonedDateTime startDate = tradeDate;
    final FutureSecurity[] securities = new FutureSecurity[40];
    final int[] amounts = new int[40];
    final double[] prices = new double[40];
    for (int i = 0; i < 40; i++) {
      final Expiry expiry = new Expiry(startDate.plusMonths(3 * i).with(THIRD_WED_ADJUSTER));
      final String letter = MONTHS.get(expiry.getExpiry().getMonth());
      final String year = Integer.toString(expiry.getExpiry().getYear() - 2000);
      final String code = "ER" + letter + year;
      final FutureSecurity security = new InterestRateFutureSecurity(expiry, "EUREX", "EUREX", CURRENCY, 2500, EURIBOR_3M, "Interest rate");
      security.setName(code);
      security.setExternalIdBundle(ExternalIdBundle.of(ExternalSchemes.syntheticSecurityId(code)));
      securities[i] = security;
      amounts[i] = 1;
      prices[i] = 1 - ((i + 1) * 0.001);
    }
    return new FutureSecurityGenerator<>(securities, amounts, prices, tradeDate, "Euribor futures");
  }

  /**
   * Generates future trades and adds them to a portfolio.
   * @param <T> The type of the security
   */
  private class FutureSecurityGenerator<T extends ManageableSecurity> extends SecurityGenerator<T> implements PortfolioNodeGenerator {
    /** The securities */
    private final ManageableSecurity[] _securities;
    /** The amounts */
    private final int[] _amounts;
    /** The prices */
    private final double[] _prices;
    /** The trade date */
    private final ZonedDateTime _tradeDate;
    /** The name */
    private final String _name;

    /**
     * @param securities The futures
     * @param amounts The amounts of each future
     * @param prices The price of each future
     * @param tradeDate The trade date
     * @param name The portfolio node name
     */
    public FutureSecurityGenerator(final ManageableSecurity[] securities, final int[] amounts, final double[] prices, final ZonedDateTime tradeDate, final String name) {
      _securities = securities;
      _amounts = amounts;
      _prices = prices;
      _tradeDate = tradeDate;
      _name = name;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public PortfolioNode createPortfolioNode() {
      final SimplePortfolioNode node = new SimplePortfolioNode(_name);
      for (int i = 0; i < _securities.length; i++) {
        final BigDecimal n = new BigDecimal(_amounts[i]);
        final double premium = _prices[i];
        final ManageableTrade trade = new ManageableTrade(n, getSecurityPersister().storeSecurity(_securities[i]), _tradeDate.toLocalDate(),
            _tradeDate.toOffsetDateTime().toOffsetTime(), ExternalId.of(Counterparty.DEFAULT_SCHEME, COUNTERPARTY));
        trade.setPremium(premium);
        trade.setPremiumCurrency(CURRENCY);
        final Position position = SimplePositionGenerator.createPositionFromTrade(trade);
        node.addPosition(position);
      }
      return node;
    }

    @Override
    public T createSecurity() {
      return null;
    }
  }
}
