package com.opengamma.component.tool;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Created by julian on 04/04/14.
 */
public interface TradeAdapter<T> {

  /**
   * Uses data from the trade object to determine what portfolio it should
   * be inserted into.
   * @param trade
   * @return
   */
  String determinePortfolioForTrade(T trade);

  boolean isTradeUsingListedProduct(T trade);

  ExternalIdBundle determineSecurityIdForTrade(T trade);

  ManageableSecurity buildSecurityForTrade(T trade);

  ManageableTrade buildManageableTrade(T trade);
}
