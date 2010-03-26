/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;


/**
 * 
 *
 * @author jim
 */
public interface OptionVisitor<T> {
  public T visitEuropeanVanillaOption(EuropeanVanillaOption option);
  public T visitAmericanVanillaOption(AmericanVanillaOption option);
  public T visitPoweredOption(PoweredOption option);
}
