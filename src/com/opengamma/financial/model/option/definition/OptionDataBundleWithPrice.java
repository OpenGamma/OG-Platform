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
public class OptionDataBundleWithPrice<T extends StandardOptionDataBundle> {
  private final T _bundle;
  private final double _price;

  public OptionDataBundleWithPrice(T bundle, double price) {
    _bundle = bundle;
    _price = price;
  }

  public T getDataBundle() {
    return _bundle;
  }

  public double getOptionPrice() {
    return _price;
  }
}
