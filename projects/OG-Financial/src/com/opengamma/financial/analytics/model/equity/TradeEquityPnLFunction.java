/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.pnl.AbstractTradePnLFunction;
import com.opengamma.financial.security.equity.EquitySecurity;

/**
 * 
 */
public class TradeEquityPnLFunction extends AbstractTradePnLFunction {
  
  /**
   * @param markDataSource the mark to market data source name, not-null
   * @param markDataProvider the mark to market data provider name
   * @param markDataField the mark to market data field name, not-null
   * @param costOfCarryField the cost of carry field name, not-null
   */
  public TradeEquityPnLFunction(String markDataSource, String markDataProvider, String markDataField, String costOfCarryField) {
    super(markDataSource, markDataProvider, markDataField, costOfCarryField);
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getType() == ComputationTargetType.TRADE && target.getTrade().getSecurity() instanceof EquitySecurity;
  }
  
  @Override
  public String getShortName() {
    return "TradeDailyEquityPnL";
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.FAIR_VALUE;
  }

}
