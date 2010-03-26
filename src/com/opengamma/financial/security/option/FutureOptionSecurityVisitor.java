/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

/**
 * 
 *
 * @author emcleod
 */
public interface FutureOptionSecurityVisitor<T> {

  public T visitAmericanVanillaFutureOptionSecurity(AmericanVanillaFutureOptionSecurity security);

  public T visitEuropeanVanillaFutureOptionSecurity(EuropeanVanillaFutureOptionSecurity security);

}
