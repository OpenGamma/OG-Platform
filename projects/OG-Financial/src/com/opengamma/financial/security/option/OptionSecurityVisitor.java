/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

/**
 * Visitor for the {@code OptionSecurity} subclasses.
 * 
 * @param <T> return type of the visit methods
 */
public interface OptionSecurityVisitor<T> {

  T visitBondOptionSecurity(BondOptionSecurity security);

  T visitEquityOptionSecurity(EquityOptionSecurity security);

  T visitFutureOptionSecurity(FutureOptionSecurity security);

  T visitFXOptionSecurity(FXOptionSecurity security);

  T visitOptionOptionSecurity(OptionOptionSecurity security);

  T visitSwaptionSecurity(SwaptionSecurity security);

}
