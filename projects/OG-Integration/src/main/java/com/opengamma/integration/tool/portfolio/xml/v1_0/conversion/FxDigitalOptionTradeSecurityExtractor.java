/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FxDigitalOptionTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;

/**
 * Security extractor for fx digital option trades.
 */
public class FxDigitalOptionTradeSecurityExtractor extends TradeSecurityExtractor<FxDigitalOptionTrade> {

  /**
   * Create a security extractor for the supplied trade.
   *
   * @param trade the trade to perform extraction on
   */
  public FxDigitalOptionTradeSecurityExtractor(FxDigitalOptionTrade trade) {
    super(trade);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableSecurity[] extractSecurities() {
    FxDigitalOptionTrade trade = getTrade();
    Currency payoutCurrency = trade.getPayoutCurrency();
    FxOptionCalculator calculator = new FxOptionCalculator(trade, trade.getPayout(), payoutCurrency);

    ManageableSecurity security = new FXDigitalOptionSecurity(
        calculator.getPutCurrency(),
        calculator.getCallCurrency(),
        calculator.getPutAmount(),
        calculator.getCallAmount(),
        payoutCurrency,
        calculator.getExpiry(),
        calculator.getSettlementDate(),
        calculator.isLong());
    return securityArray(addIdentifier(security));
  }

}
