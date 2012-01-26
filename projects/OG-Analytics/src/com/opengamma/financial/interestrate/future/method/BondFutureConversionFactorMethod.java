/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.method.BondSecurityDiscountingMethod;

/**
 * Methods for the computation of conversion factors of bonds in bond futures basket.
 */
public class BondFutureConversionFactorMethod {

  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  /**
   * Computes the conversion factor for Liffe traded Gilt futures.
   * <P> Reference: EXCHANGE CONTRACT NO. 144 IN RESPECT OF GILT CONTRACTS, NYSE Euronext Global Derivatives, 2011.
   * @param bondDefinition The bond.
   * @param deliveryDate The future delivery date (first day of the delivery month).
   * @param notionalCoupon The notional coupon (as specified in the contract details)
   * @return The factor.
   */
  public double conversionFactorLiffe(final BondFixedSecurityDefinition bondDefinition, final ZonedDateTime deliveryDate, final double notionalCoupon) {
    Validate.notNull(bondDefinition, "Bond definition");
    Validate.notNull(deliveryDate, "Delivery date");
    BondFixedSecurity bond = bondDefinition.toDerivative(deliveryDate, deliveryDate, new String[] {"Not used", "Not used"}); // The curves are not used for price from yield computation.
    double cleanPrice = METHOD_BOND_SECURITY.cleanPriceFromYield(bond, notionalCoupon);
    return cleanPrice;
  }

  public double conversionFactorEuronext(final BondFixedSecurityDefinition bondDefinition, final ZonedDateTime deliveryDate, final double notionalCoupon) {
    Validate.notNull(bondDefinition, "Bond definition");
    Validate.notNull(deliveryDate, "Delivery date");
    BondFixedSecurity bond = bondDefinition.toDerivative(deliveryDate, deliveryDate, new String[] {"Not used", "Not used"}); // The curves are not used for price from yield computation.
    double cleanPrice = METHOD_BOND_SECURITY.cleanPriceFromYield(bond, notionalCoupon);
    return cleanPrice;
  }

}
