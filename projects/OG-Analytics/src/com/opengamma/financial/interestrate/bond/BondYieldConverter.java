/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.instrument.bond.BondConvention;
import com.opengamma.financial.instrument.bond.BondDefinition;

/**
 * 
 */
public class BondYieldConverter {
  //TODO doesn't need to be a separate converter - should be part of the yield calculator
  public double convertYield(final BondDefinition definition, final LocalDate date, final double continuouslyCompoundedYield) {
    final BondConvention convention = definition.getConvention();
    return getYield(SimpleYieldConvention.US_TREASURY_EQUIVALANT, definition, continuouslyCompoundedYield);
  }

  private double getYield(final YieldConvention convention, final BondDefinition definition, final double continuouslyCompoundedYield) {
    if (convention.equals(SimpleYieldConvention.US_TREASURY_EQUIVALANT)) {
      return getUSDTreasuryEquivalentYield(definition, continuouslyCompoundedYield);
    }
    throw new IllegalArgumentException("Can only handle " + SimpleYieldConvention.US_TREASURY_EQUIVALANT);
  }

  private double getUSDTreasuryEquivalentYield(final BondDefinition definition, final double continuouslyCompoundedYield) {
    final double paymentsPerYear = definition.getCouponsPerYear();
    return paymentsPerYear * (Math.exp(continuouslyCompoundedYield / paymentsPerYear) - 1);
  }

  private double getMoneyMarketYield(final BondDefinition definition, final double continuouslyCompoundedYield) {
    return 0;
  }

}
