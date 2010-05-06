/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.pnl;

public enum Underlying {
  // TODO YIELD_CURVE will need to change - need information about the interest rates that compose it
  SPOT_PRICE, SPOT_VOLATILITY, IMPLIED_VOLATILITY, INTEREST_RATE, COST_OF_CARRY, STRIKE, TIME, IMPLIED_VARIANCE, YIELD, YIELD_CURVE, BOND_YIELD
}
