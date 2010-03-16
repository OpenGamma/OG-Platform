/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

public interface EquityOptionSecurityVisitor<T> {
  public T visitAmericanVanillaEquityOptionSecurity(AmericanVanillaEquityOptionSecurity security);
  public T visitEuropeanVanillaEquityOptionSecurity(EuropeanVanillaEquityOptionSecurity security);
  public T visitPoweredEquityOptionSecurity(PoweredEquityOptionSecurity security);
}
