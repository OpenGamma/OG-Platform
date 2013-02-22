package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.integration.tool.portfolio.xml.PortfolioPosition;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.EquityVarianceSwapTrade;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FxDigitalOptionTrade;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FxOptionTrade;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.Portfolio;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.Position;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SwapTrade;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.Trade;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

public class PortfolioConverter {

  private final Portfolio _portfolio;

  // todo - this would be better using a visitor implementation
  private static final Map<Class<? extends Trade>, TradeSecurityExtractor<? extends Trade>> _extractors = ImmutableMap.of(
      FxOptionTrade.class, new FxOptionTradeSecurityExtractor(),
      FxDigitalOptionTrade.class, new FxDigitalOptionTradeSecurityExtractor(),
      SwapTrade.class, new SwapTradeSecurityExtractor(),
      EquityVarianceSwapTrade.class, new EquityVarianceSwapTradeSecurityExtractor()
  );

  public PortfolioConverter(Portfolio portfolio) {
    _portfolio = portfolio;
  }

  /**
   * Get the set of manageable positions for this portfolio. Note that this may add in positions
   * which were not in the original xml file e.g. where a set of trades were specified but no
   * positions, each trade will be added to a new position.
   */
  public Iterable<PortfolioPosition> getPositions() {

    return processPortfolio(_portfolio, true, null);
  }

  /**
   * Produce the collection of positions held in a portfolio. A portfolio may consist of any combination of:
   * <ul>
   * <li>recursively nested portfolios</li>
   * <li>positions (which may or may not contain trades)</li>
   * <li>trades</li>
   * </ul>
   * and this method will produce a List of PortfolioPosition objects by examining the above elements.
   * If portfolios are nested, this method will be called recursively noting the portfolio path as the
   * portfolio hieracrhy is decended. Note that this may add in positions which were not in the original
   * xml file e.g. where a set of trades were specified but no
   * positions, each trade will be added to a new position.
   *
   * @param portfolio the portfolio to be examined
   * @param isRoot indicates if the portfolio has no parent i.e. a root node
   * @param parentPath the path to the portfolio (an array of the names of the portfolio's ancestors).
   * @return the positions held by the portfolio
   */
  private List<PortfolioPosition> processPortfolio(Portfolio portfolio, boolean isRoot, String[] parentPath) {

    List<PortfolioPosition> managedPositions = Lists.newArrayList();

    // This is needed as we never want the name of the root portfolio to appear in the path. So for
    // a root portfolio we want an empty path, for a child of the root we want just the portfolio name etc.
    String[] portfolioPath = isRoot ? new String[0] : extendPath(parentPath, portfolio.getName());

    for (Portfolio nestedPortfolio : nullCheckIterable(portfolio.getPortfolios())) {

      managedPositions.addAll(processPortfolio(nestedPortfolio, false, portfolioPath));
    }

    for (Position position : nullCheckIterable(portfolio.getPositions())) {

      List<Trade> trades = position.getTrades();

      for (Trade trade : trades) {

        ManageableSecurity security = extractSecurityFromTrade(trade, trades.size());
        managedPositions.add(createPortfolioPosition(position, security, portfolioPath));
      }
    }

    for (Trade trade : nullCheckIterable(portfolio.getTrades())) {

      // TODO we probably want logic to allow for the aggregation of trades into positions but for now we'll create a position per trade
      ManageableSecurity security = extractSecurityFromTrade(trade, 1);
      managedPositions.add(createPortfolioPosition(trade, security, portfolioPath));
    }
    return managedPositions;
  }

  /**
   * Create a new array which is a copy of the path array with
   * the name parameter appended at the end.
   *
   * @param path the path array to be extended
   * @param name the element to append to the array
   * @return the extended array
   */
  private String[] extendPath(String[] path, String name) {

    int oldLength = path.length;
    String[] extended = Arrays.copyOf(path, oldLength  + 1);
    extended[oldLength] = name;
    return extended;
  }

  private ManageableSecurity extractSecurityFromTrade(Trade trade, int tradesSize) {

    ArgumentChecker.isFalse(tradesSize > 1 && !trade.canBePositionAggregated(), "Trade type cannot be aggregated in positions");

    if (_extractors.containsKey(trade.getClass())) {

      TradeSecurityExtractor extractor = _extractors.get(trade.getClass());
      return extractor.extractSecurity(trade);

    } else {
      throw new OpenGammaRuntimeException("Unable to handle trade with type [" + trade.getClass().getName() +
                                              "] - [" + trade + "]");
    }
  }

  private <T> Iterable<T> nullCheckIterable(Iterable<T> iterable) {
    return iterable == null ? ImmutableList.<T>of() : iterable;
  }


  private PortfolioPosition createPortfolioPosition(Position position,
                                                    ManageableSecurity security,
                                                    String[] parentPath) {
    return new PortfolioPosition(convertPosition(position, security), new ManageableSecurity[]{security}, parentPath);
  }

  private PortfolioPosition createPortfolioPosition(Trade trade, ManageableSecurity security, String[] parentPath) {
    return new PortfolioPosition(convertTradeToPosition(trade, security), new ManageableSecurity[]{security}, parentPath);
  }

  private ManageablePosition convertTradeToPosition(Trade trade, ManageableSecurity security) {
    ManageablePosition manageablePosition = new ManageablePosition(BigDecimal.ONE, security.getExternalIdBundle());
    manageablePosition.addTrade(convertTrade(trade, security));
    return manageablePosition;
  }

  private ManageablePosition convertPosition(Position position, Security security) {

    ManageablePosition manageablePosition = new ManageablePosition(position.getQuantity(), security.getExternalIdBundle());

    List<Trade> trades = position.getTrades();
    for (Trade trade : trades) {
      manageablePosition.addTrade(convertTrade(trade, security));
    }
    return manageablePosition;
  }

  private ManageableTrade convertTrade(Trade trade, Security security) {

    // Would anything other than 1 for quantity make sense here (for OTC trades)?
    ManageableTrade manageableTrade = new ManageableTrade(BigDecimal.ONE,
                                                          security.getExternalIdBundle(),
                                                          trade.getTradeDate(),
                                                          null,
                                                          trade.getCounterparty().toExternalId());

    manageableTrade.setProviderId(trade.getExternalSystemId().toExternalId());

    BigDecimal premium = trade.getPremium();
    if (premium != null) {
      manageableTrade.setPremium(premium.doubleValue());
      manageableTrade.setPremiumCurrency(Currency.of(trade.getPremiumCurrency()));
    }
    return manageableTrade;
  }


}
