/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.BuySell;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FixedLeg;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SettlementType;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SwapLeg.Direction;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SwapTrade;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SwaptionTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
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

  //-------------------------------------------------------------------------
  @Override
  public ManageableSecurity[] extractSecurities() {
    SwaptionTrade trade = getTrade();
    SwapTrade swapTrade = trade.getUnderlyingSwapTrade();
    ManageableSecurity underlying = swapTrade.getSecurityExtractor().extractSecurities()[0];

    ExternalId underlyingId = underlying.getExternalIdBundle().getExternalId(ExternalScheme.of("XML_LOADER"));

    List<FixedLeg> fixedLegs = Lists.newArrayList(swapTrade.getFixedLegs());

    //note - these fields are resolved on a best effort basis
    //since they aren't actually used by the analytics.
    //see PLAT-1924
    Currency currency = null;
    boolean isPayer = false;

    if (!fixedLegs.isEmpty()) {
      currency = fixedLegs.get(0).getCurrency();
      isPayer = fixedLegs.get(0).getDirection() == Direction.PAY;
    }

    Expiry expiry = new Expiry(convertLocalDate(trade.getExpirationDate()));

    ManageableSecurity security = new SwaptionSecurity(
        isPayer, underlyingId, trade.getBuySell() == BuySell.BUY,
        expiry, trade.getSettlementType() == SettlementType.CASH_SETTLED,
        currency,
        null, trade.getExerciseType().convert(),
        convertLocalDate(trade.getCashSettlementPaymentDate()));

    return securityArray(addIdentifier(security), underlying);
  }
}
