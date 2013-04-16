/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.BuySell;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SettlementType;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SwapLeg;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SwapTrade;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SwaptionTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.time.Expiry;

/**
 * Security extractor for swaption trades.
 */
public class SwaptionTradeSecurityExtractor extends TradeSecurityExtractor<SwaptionTrade> {

  /**
   * Create a security extractor for the supplied trade.
   *
   * @param trade the trade to perform extraction on
   */
  public SwaptionTradeSecurityExtractor(SwaptionTrade trade) {
    super(trade);
  }

  @Override
  public ManageableSecurity[] extractSecurities() {

    SwapTrade swapTrade = _trade.getUnderlyingSwapTrade();
    ManageableSecurity underlying = swapTrade.getSecurityExtractor().extractSecurities()[0];

    ExternalId underlyingId = underlying.getExternalIdBundle().getExternalId(ExternalScheme.of("XML_LOADER"));

    boolean isPayer = swapTrade.getFixedLeg().getDirection() == SwapLeg.Direction.PAY;
    Expiry expiry = new Expiry(convertLocalDate(_trade.getExpirationDate()));

    ManageableSecurity security = new SwaptionSecurity(isPayer, underlyingId, _trade.getBuySell() == BuySell.BUY,
                                                       expiry, _trade.getSettlementType() == SettlementType.CASH_SETTLED,
                                                       swapTrade.getFixedLeg().getCurrency(),
                                                       null, _trade.getExerciseType().convert(),
                                                       convertLocalDate(_trade.getCashSettlementPaymentDate()));

    return securityArray(addIdentifier(security), underlying);
  }
}
