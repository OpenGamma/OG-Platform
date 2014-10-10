/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BillSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BillTransactionDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Class use to generate Bill transactions.
 */
public class GeneratorBill extends GeneratorInstrument<GeneratorAttributeET> {

  /**
   * The underlying bill security.
   */
  private final BillSecurityDefinition _security;

  /**
   * Constructor.
   * @param name Generator name.
   * @param security The underlying bill security.
   */
  public GeneratorBill(final String name, final BillSecurityDefinition security) {
    super(name);
    ArgumentChecker.notNull(security, "Bill security");
    _security = security;
  }

  /**
   * {@inheritDoc}
   * Generate a bill transaction from the bill (market quote) yield.
   */
  @Override
  public BillTransactionDefinition generateInstrument(final ZonedDateTime date, final double marketQuote, 
      final double notional, final GeneratorAttributeET attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    final int quantity = (int) Math.round(notional / _security.getNotional());
    final ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(date, _security.getSettlementDays(), _security.getCalendar());
    return BillTransactionDefinition.fromYield(_security, quantity, settleDate, marketQuote, _security.getCalendar());
  }

}
