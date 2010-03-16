/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

public interface BondSecurityVisitor<T> {
  public T visitCorporateBondSecurity (CorporateBondSecurity security);
  public T visitGovernmentBondSecurity (GovernmentBondSecurity security);
  public T visitMunicipalBondSecurity (MunicipalBondSecurity security);
}
