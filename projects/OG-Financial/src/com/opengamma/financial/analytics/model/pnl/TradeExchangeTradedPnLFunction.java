/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * 
 */
public class TradeExchangeTradedPnLFunction extends AbstractTradePnLFunction {
  
  /**
   * @param resolutionKey the resolution key, not-null
   * @param markDataField the mark to market data field name, not-null
   * @param costOfCarryField the cost of carry field name, not-null
   */
  public TradeExchangeTradedPnLFunction(String resolutionKey, String markDataField, String costOfCarryField) {
    super(resolutionKey, markDataField, costOfCarryField);
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    Security security = target.getTrade().getSecurity();
    return (target.getType() == ComputationTargetType.TRADE && FinancialSecurityUtils.isExchangedTraded(security));
  }

  @Override
  public String getShortName() {
    return "TradeDailyPnL";
  }


}
