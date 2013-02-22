/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FxDigitalOptionTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;

public class FxDigitalOptionTradeSecurityExtractor extends TradeSecurityExtractor<FxDigitalOptionTrade> {

  @Override
  public ManageableSecurity extractSecurity(FxDigitalOptionTrade trade) {

    Currency payoutCurrency = Currency.of(trade.getPayoutCurrency());
    FxOptionCalculator calculator = new FxOptionCalculator(trade, trade.getPayout(), payoutCurrency);

    ManageableSecurity security = new FXDigitalOptionSecurity(calculator.getPutCurrency(),
                                                              calculator.getCallCurrency(),
                                                              calculator.getPutAmount(),
                                                              calculator.getCallAmount(),
                                                              payoutCurrency,
                                                              calculator.getExpiry(),
                                                              calculator.getSettlementDate(),
                                                              calculator.isLong());

    // Generate the loader SECURITY_ID (should be uniquely identifying)
    security.addExternalId(ExternalId.of("XML_LOADER", Integer.toHexString(
        new HashCodeBuilder()
            .append(security.getClass())
            .append(calculator.isLong())
            .append(calculator.getCallCurrency())
            .append(calculator.getCallAmount())
            .append(calculator.getPutCurrency())
            .append(calculator.getPutAmount())
            .append(payoutCurrency)
            .append(calculator.getExpiry())
            .append(calculator.getSettlementDate()).toHashCode()
    )));

    return security;

  }

}
