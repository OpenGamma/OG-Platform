/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;


/**
 * 
 * @param <T> The option definition type
 * @param <U> The option data bundle type
 * 
 */
public abstract class TrinomialOptionModelDefinition<T extends OptionDefinition, U extends StandardOptionDataBundle> {

  public abstract double getDX(T option, U data, int n, int j);

  public abstract double getUpFactor(T option, U data, int n, int j);

  public abstract double getMidFactor(T option, U data, int n, int j);

  public abstract double getDownFactor(T option, U data, int n, int j);

}
