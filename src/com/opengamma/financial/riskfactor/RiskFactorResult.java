/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

/**
 * @author emcleod
 * 
 */
public interface RiskFactorResult<T> {

  public boolean isMultiValued();

  public T getResult();
}
