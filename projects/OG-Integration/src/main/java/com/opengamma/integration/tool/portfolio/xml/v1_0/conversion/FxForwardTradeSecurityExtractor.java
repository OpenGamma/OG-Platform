/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FxForwardTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;

/**
 * Security extractor for fx forward trades.
 */
public class FxForwardTradeSecurityExtractor extends TradeSecurityExtractor<FxForwardTrade> {

  /**
   * Create a security extractor for the supplied trade.
   *
   * @param trade the trade to perform extraction on
   */
  public FxForwardTradeSecurityExtractor(FxForwardTrade trade) {
    super(trade);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableSecurity[] extractSecurities() {
    FxForwardTrade trade = getTrade();
    ExternalId region = extractRegion(trade.getPaymentCalendars());
    boolean nonDeliverable = checkNonDeliverable(trade);

    Currency payCurrency = trade.getPayCurrency();
    double payAmount = trade.getPayAmount().doubleValue();
    Currency receiveCurrency = trade.getReceiveCurrency();
    double receiveAmount = trade.getReceiveAmount().doubleValue();
    ZonedDateTime forwardDate = convertLocalDate(trade.getMaturityDate());

    ManageableSecurity security = nonDeliverable ?
        // todo - expiry should be used in construction of NonDeliverableFXForwardSecurity
        new NonDeliverableFXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate,
                                            region, trade.getSettlementCurrency().equals(trade.getReceiveCurrency())) :
        new FXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate, region);

    return securityArray(addIdentifier(security));
  }

  private boolean checkNonDeliverable(FxForwardTrade trade) {

    if (trade.getSettlementCurrency() != null && trade.getFxExpiry() != null) {
      return true;
    } else if (trade.getSettlementCurrency() == null && trade.getFxExpiry() == null) {
      return false;
    } else {
      throw new PortfolioParsingException(
          "Either both settlementCurrency and fxExpiry elements must be present, or neither");
    }
  }

}
