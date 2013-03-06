/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import org.threeten.bp.ZoneOffset;

import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.OtcEquityIndexOptionTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.time.Expiry;

public class OtcEquityIndexOptionTradeSecurityExtractor extends TradeSecurityExtractor<OtcEquityIndexOptionTrade> {

  @Override
  public ManageableSecurity[] extractSecurity(OtcEquityIndexOptionTrade trade) {

    trade.getUnderlyingId().toExternalId().toBundle();
    return securityArray(new EquityIndexOptionSecurity(trade.getOptionType(), trade.getStrike().doubleValue(),
                                                       trade.getNotionalCurrency(),
                                         trade.getUnderlyingId().toExternalId(), trade.getExerciseType().convert(),
                                         new Expiry(trade.getExpiryDate().atStartOfDay(ZoneOffset.UTC)), 1, "OTC"));
  }
}
