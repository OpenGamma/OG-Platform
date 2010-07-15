/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.bond;


public interface BondSecurityVisitor<T> {
  T visitCorporateBondSecurity(CorporateBondSecurity security);
  T visitGovernmentBondSecurity(GovernmentBondSecurity security);
  T visitMunicipalBondSecurity(MunicipalBondSecurity security);
}
