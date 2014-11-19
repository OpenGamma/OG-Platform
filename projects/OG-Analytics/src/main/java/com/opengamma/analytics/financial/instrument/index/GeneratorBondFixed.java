/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class GeneratorBondFixed extends GeneratorInstrument<GeneratorAttributeET> {

  /**
   * The underlying fixed bond security.
   */
  private final BondFixedSecurityDefinition _security;

  /**
   * Constructor.
   * @param name Generator name.
   * @param security The underlying fixed bond security.
   */
  public GeneratorBondFixed(final String name, final BondFixedSecurityDefinition security) {
    super(name);
    ArgumentChecker.notNull(security, "Bond security");
    _security = security;
  }

  @Override
  /**
   * Generate a fixed bond transaction from the fixed bond marquetQuote  which is clean price .
   */
  public BondFixedTransactionDefinition generateInstrument(final ZonedDateTime date, final double marketQuote, 
      final double notional, final GeneratorAttributeET attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    int quantity = (int) Math.round(notional / _security.getNominal().getNthPayment(0).getReferenceAmount());
    ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(date, _security.getSettlementDays(), _security.getCalendar());
    if (attribute.isPrice()) {
      return new BondFixedTransactionDefinition(_security, quantity, settleDate, marketQuote);
    }
    return BondFixedTransactionDefinition.fromYield(_security, quantity, settleDate, marketQuote);    
  }

}
