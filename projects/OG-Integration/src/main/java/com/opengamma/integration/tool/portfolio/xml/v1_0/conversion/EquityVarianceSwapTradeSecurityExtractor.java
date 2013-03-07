/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.EquityVarianceSwapTrade;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Security extractor for equity variance swap trades.
 */
public class EquityVarianceSwapTradeSecurityExtractor extends TradeSecurityExtractor<EquityVarianceSwapTrade> {

  /**
   * Create a security extractor for the supplied trade.
   *
   * @param trade the trade to perform extraction on
   */
  public EquityVarianceSwapTradeSecurityExtractor(EquityVarianceSwapTrade trade) {
    super(trade);
  }

  @Override
  public ManageableSecurity[] extractSecurities() {

    ExternalId region = null;
    boolean parameterizedAsVariance = false; // distinguishes vega or variance strike/notional
    EquityVarianceSwapSecurity security = new EquityVarianceSwapSecurity(_trade.getUnderlying().toExternalId(),
                                                                         _trade.getCurrency(),
                                                                         _trade.getStrike().doubleValue(),
                                                                         _trade.getVegaAmount().doubleValue(),
                                                                         parameterizedAsVariance,
                                                                         _trade.getAnnualizationFactor(),
                                                                         convertLocalDate(
                                                                             _trade.getObservationStartDate()),
                                                                         convertLocalDate(_trade.getObservationEndDate()),
                                                                         /*convertLocalDate(trade.getPremiumSettlementDate())*/
                                                                         null,
                                                                         region,
                                                                         SimpleFrequencyFactory.INSTANCE.getFrequency(_trade.getObservationfrequency()));
    return securityArray(addIdentifier(security));
  }

}
