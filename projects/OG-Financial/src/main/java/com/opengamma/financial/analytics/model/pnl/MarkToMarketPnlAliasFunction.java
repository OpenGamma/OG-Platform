/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.value.ValueRequirementAliasFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * Renames ValueRequirementNames.MTM_PNL ("Mark-to-Market P&L") to arbitrary value  
 */
public class MarkToMarketPnlAliasFunction extends ValueRequirementAliasFunction {

  public MarkToMarketPnlAliasFunction(String aliasedValueRequirementName, String tradeTypeConstraint) { 
      
    super(aliasedValueRequirementName,
        ValueRequirementNames.MTM_PNL,
        ValueProperties.with(PnLFunctionUtils.PNL_TRADE_TYPE_CONSTRAINT, tradeTypeConstraint).get(),
        ImmutableSet.of(PnLFunctionUtils.PNL_TRADE_TYPE_CONSTRAINT),
        ComputationTargetType.TRADE.or(ComputationTargetType.POSITION).or(ComputationTargetType.PORTFOLIO_NODE));
    
    ArgumentChecker.notNull(aliasedValueRequirementName, "aliasedValueRequirementName");
    ArgumentChecker.notNull(tradeTypeConstraint, "tradeTypeConstraint");
    if ((!tradeTypeConstraint.equalsIgnoreCase(PnLFunctionUtils.PNL_TRADE_TYPE_ALL))
        && (!tradeTypeConstraint.equalsIgnoreCase(PnLFunctionUtils.PNL_TRADE_TYPE_OPEN))
        && (!tradeTypeConstraint.equalsIgnoreCase(PnLFunctionUtils.PNL_TRADE_TYPE_NEW))) {
      throw new OpenGammaRuntimeException(tradeTypeConstraint + "is not allowed. Looking for one of:" +
          PnLFunctionUtils.PNL_TRADE_TYPE_ALL + "," + PnLFunctionUtils.PNL_TRADE_TYPE_OPEN + "," + PnLFunctionUtils.PNL_TRADE_TYPE_NEW); 
    }
  }

}
