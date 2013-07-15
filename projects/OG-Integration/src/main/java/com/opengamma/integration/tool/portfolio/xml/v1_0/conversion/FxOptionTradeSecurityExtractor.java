/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FxOptionTrade;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.SettlementType;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Security extractor for fx option trades.
 */
public class FxOptionTradeSecurityExtractor extends TradeSecurityExtractor<FxOptionTrade> {

  /**
   * Create a security extractor for the supplied trade.
   *
   * @param trade the trade to perform extraction on
   */
  public FxOptionTradeSecurityExtractor(FxOptionTrade trade) {
    super(trade);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableSecurity[] extractSecurities() {
    FxOptionTrade trade = getTrade();
    FxOptionCalculator calculator = new FxOptionCalculator(trade, trade.getNotional(), trade.getNotionalCurrency());
    ExerciseType exerciseType = trade.getExerciseType().convert();
    ManageableSecurity security = trade.getSettlementType() == SettlementType.PHYSICAL ?
        new FXOptionSecurity(
            calculator.getPutCurrency(),
            calculator.getCallCurrency(),
            calculator.getPutAmount(),
            calculator.getCallAmount(),
            calculator.getExpiry(),
            calculator.getSettlementDate(),
            calculator.isLong(),
            exerciseType) :
        new NonDeliverableFXOptionSecurity(
            calculator.getPutCurrency(),
            calculator.getCallCurrency(),
            calculator.getPutAmount(),
            calculator.getCallAmount(),
            calculator.getExpiry(),
            calculator.getSettlementDate(),
            calculator.isLong(),
            exerciseType,
            trade.getSettlementCurrency().equals(calculator.getCallCurrency().getCode()));
    return securityArray(addIdentifier(security));
  }

}
