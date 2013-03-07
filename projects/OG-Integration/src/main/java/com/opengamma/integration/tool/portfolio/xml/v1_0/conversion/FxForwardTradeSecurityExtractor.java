/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
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

  @Override
  public ManageableSecurity[] extractSecurities() {

    ExternalId region = extractRegion(_trade.getPaymentCalendars());
    boolean nonDeliverable = checkNonDeliverable(_trade);

    Currency payCurrency = _trade.getPayCurrency();
    double payAmount = _trade.getPayAmount().doubleValue();
    Currency receiveCurrency = _trade.getReceiveCurrency();
    double receiveAmount = _trade.getReceiveAmount().doubleValue();
    ZonedDateTime forwardDate = convertLocalDate(_trade.getMaturityDate());

    ManageableSecurity security = nonDeliverable ?
        // todo - expiry should be used in construction of NonDeliverableFXForwardSecurity
        new NonDeliverableFXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate,
                                            region, _trade.getSettlementCurrency().equals(_trade.getReceiveCurrency())) :
        new FXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate, region);

    return securityArray(addIdentifier(security));
  }

  private boolean checkNonDeliverable(FxForwardTrade trade) {

    if (trade.getSettlementCurrency() != null && trade.getFxExpiry() != null) {
      return true;
    } else if (trade.getSettlementCurrency() == null && trade.getFxExpiry() == null) {
      return false;
    } else {
      throw new OpenGammaRuntimeException(
          "Either both settlementCurrency and fxExpiry elements must be present, or neither");
    }
  }
}
