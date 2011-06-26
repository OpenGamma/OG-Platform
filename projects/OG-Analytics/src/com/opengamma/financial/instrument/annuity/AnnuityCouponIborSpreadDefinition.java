/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponIborSpreadDefinition;

/**
 * A wrapper class for an AnnuityDefinition containing CouponIborSpreadDefinition.
 */
public class AnnuityCouponIborSpreadDefinition extends AnnuityDefinition<CouponIborSpreadDefinition> {

  /**
   * Constructor from a list of Ibor-like coupons.
   * @param payments The Ibor coupons.
   */
  public AnnuityCouponIborSpreadDefinition(final CouponIborSpreadDefinition[] payments) {
    super(payments);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param tenor The tenor.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param spread The common spread.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborSpreadDefinition from(final ZonedDateTime settlementDate, final Period tenor, final double notional, final IborIndex index, final double spread, 
      final boolean isPayer) {
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(index, "index");
    Validate.notNull(tenor, "tenor");
    final AnnuityCouponIborDefinition iborAnnuity = AnnuityCouponIborDefinition.from(settlementDate, tenor, notional, index, isPayer);
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[iborAnnuity.getPayments().length];
    for (int loopcpn = 0; loopcpn < iborAnnuity.getPayments().length; loopcpn++) {
      coupons[loopcpn] = CouponIborSpreadDefinition.from(iborAnnuity.getNthPayment(loopcpn), spread);
    }
    return new AnnuityCouponIborSpreadDefinition(coupons);
  }

  /**
   * Annuity builder from the conventions and common characteristics.
   * @param settlementDate The settlement date.
   * @param maturityDate The annuity maturity date.
   * @param notional The notional.
   * @param index The Ibor index.
   * @param isPayer The payer flag.
   * @param spread The common spread.
   * @return The Ibor annuity.
   */
  public static AnnuityCouponIborSpreadDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double notional, final IborIndex index, final double spread,
      final boolean isPayer) {
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(index, "index");
    Validate.notNull(maturityDate, "maturity date");
    final AnnuityCouponIborDefinition iborAnnuity = AnnuityCouponIborDefinition.from(settlementDate, maturityDate, notional, index, isPayer);
    final CouponIborSpreadDefinition[] coupons = new CouponIborSpreadDefinition[iborAnnuity.getPayments().length];
    for (int loopcpn = 0; loopcpn < iborAnnuity.getPayments().length; loopcpn++) {
      coupons[loopcpn] = CouponIborSpreadDefinition.from(iborAnnuity.getNthPayment(loopcpn), spread);
    }
    return new AnnuityCouponIborSpreadDefinition(coupons);
  }
  
}
