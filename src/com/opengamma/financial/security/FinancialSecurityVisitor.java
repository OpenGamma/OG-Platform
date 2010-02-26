/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

/**
 * General visitor for securities.  This is specifically divorced from the Security definition in the engine.
 * Note how we've just got the concrete classes here.
 * @author jim
 */
public interface FinancialSecurityVisitor<T> {
  public T visitAmericanVanillaEquityOptionSecurity(AmericanVanillaEquityOptionSecurity security);
  public T visitBondFutureSecurity(BondFutureSecurity security);
  public T visitCorporateBondSecurity(CorporateBondSecurity security);
  public T visitEquitySecurity(EquitySecurity security);
  public T visitEuropeanVanillaEquityOptionSecurity(EuropeanVanillaEquityOptionSecurity security);
  public T visitForwardExchangeFutureSecurity(FXFutureSecurity security);
  public T visitGovernmentBondSecurity(GovernmentBondSecurity security);
  public T visitMunicipalBondSecurity(MunicipalBondSecurity security);
  public T visitPoweredEquityOptionSecurity(PoweredEquityOptionSecurity security);
  public T visitVanillaFutureSecurity(VanillaFutureSecurity security);
}
