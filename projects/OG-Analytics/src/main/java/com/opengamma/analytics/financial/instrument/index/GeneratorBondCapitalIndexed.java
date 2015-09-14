/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Generator for capital indexed bonds.
 */
public class GeneratorBondCapitalIndexed extends GeneratorInstrument<GeneratorAttributeET> {

  /**
   * The underlying capital indexed bond security.
   */
  private final BondCapitalIndexedSecurityDefinition<? extends CouponInflationDefinition> _security;

  /**
   * Constructor.
   * @param name Generator name.
   * @param security The underlying fixed bond security.
   */
  public GeneratorBondCapitalIndexed(final String name, 
      final BondCapitalIndexedSecurityDefinition<? extends CouponInflationDefinition> security) {
    super(name);
    ArgumentChecker.notNull(security, "Bond security");
    _security = security;
  }

  @Override
  /**
   * Generate a capital indexed bond transaction from the bond marquetQuote (clean real price).
   */
  public BondCapitalIndexedTransactionDefinition<? extends CouponInflationDefinition> generateInstrument(
      final ZonedDateTime date, final double marketQuote, final double notional, final GeneratorAttributeET attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.isTrue(attribute.isPrice(), "Only (clean real) price supported for Capital Indexed Bonds");
    int quantity = (int) Math.round(notional / _security.getNominal().getNthPayment(0).getReferenceAmount());
    ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(date, _security.getSettlementDays(), _security.getCalendar());
    return new BondCapitalIndexedTransactionDefinition<>(_security, quantity, settleDate, marketQuote);
  }

}
