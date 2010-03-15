/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

/**
 * @author emcleod
 * 
 */
public interface OrderVisitor<T> {

  public T visitZerothOrder();

  public T visitFirstOrder();

  public T visitMixedSecondOrder();

  public T visitSecondOrder();

  public T visitMixedThirdOrder();

  public T visitThirdOrder();
}
