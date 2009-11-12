/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

/**
 * 
 * @author emcleod
 */
public abstract class BinomialOptionModelDefinition<T extends OptionDefinition, U extends StandardOptionDataBundle> {

  public abstract double getUpFactor(T option, U data, double n);

  public abstract double getDownFactor(T option, U data, double n);

  public abstract double getProbability(T option, U data, double n);
}
