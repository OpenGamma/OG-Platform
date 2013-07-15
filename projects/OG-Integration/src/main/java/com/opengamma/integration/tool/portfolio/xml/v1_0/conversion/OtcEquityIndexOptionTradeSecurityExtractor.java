/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.OtcEquityIndexOptionTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.time.Expiry;

/**
 * Security extractor for OTC quity index option trades.
 */
public class OtcEquityIndexOptionTradeSecurityExtractor extends TradeSecurityExtractor<OtcEquityIndexOptionTrade> {

  /**
   * Create a security extractor for the supplied trade.
   *
   * @param trade the trade to perform extraction on
   */
  public OtcEquityIndexOptionTradeSecurityExtractor(OtcEquityIndexOptionTrade trade) {
    super(trade);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableSecurity[] extractSecurities() {
    OtcEquityIndexOptionTrade trade = getTrade();
    EquityIndexOptionSecurity security = new EquityIndexOptionSecurity(
        trade.getOptionType(),
        trade.getStrike().doubleValue(),
        trade.getNotionalCurrency(),
        trade.getUnderlyingId().toExternalId(),
        trade.getExerciseType().convert(),
        new Expiry(convertLocalDate(trade.getExpiryDate())),
        1,
        "OTC");
    return securityArray(addIdentifier(security));
  }

}
