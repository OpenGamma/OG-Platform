/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.threeten.bp.ZoneOffset;

import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.BuySell;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.ExerciseType;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SettlementType;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SwapLeg;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SwapTrade;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SwaptionTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

public class SwaptionTradeSecurityExtractor extends TradeSecurityExtractor<SwaptionTrade> {

  @Override
  public ManageableSecurity[] extractSecurity(SwaptionTrade trade) {

    SwapTrade swapTrade = trade.getUnderlyingSwapTrade();
    ManageableSecurity underlying = new SwapTradeSecurityExtractor().extractSecurity(swapTrade)[0];

    ExternalId underlyingId = underlying.getExternalIdBundle().getExternalId(ExternalScheme.of("XML_LOADER"));

    boolean isPayer = swapTrade.getFixedLeg().getDirection() == SwapLeg.Direction.PAY;
    Expiry expiry = new Expiry(trade.getExpirationDate().atStartOfDay(ZoneOffset.UTC));

    Currency fixedLegCurrency = swapTrade.getFixedLeg().getCurrency();

    ExerciseType exerciseType = trade.getExerciseType();

    ManageableSecurity security = new SwaptionSecurity(isPayer, underlyingId, trade.getBuySell() == BuySell.BUY,
                                                       expiry, trade.getSettlementType() == SettlementType.CASH_SETTLED,
                                                       fixedLegCurrency,
                                                       null, exerciseType.convert(),
                                                       convertLocalDate(trade.getCashSettlementPaymentDate()));

    security.addExternalId(ExternalId.of("XML_LOADER", Integer.toHexString(
        new HashCodeBuilder()
            .append(security.getClass())
            .append(isPayer)
            .append(underlyingId)
            .append(trade.getBuySell())
            .append(expiry)
            .append(trade.getSettlementType())
            .append(fixedLegCurrency)
            .append(trade.getExerciseType())
            .append(trade.getCashSettlementPaymentDate()).toHashCode()
    )));

    return securityArray(security, underlying);
  }
}
