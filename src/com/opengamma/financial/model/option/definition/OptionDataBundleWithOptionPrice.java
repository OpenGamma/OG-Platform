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
public class OptionDataBundleWithOptionPrice {
  private final StandardOptionDataBundle _data;
  private final double _optionPrice;

  public OptionDataBundleWithOptionPrice(final StandardOptionDataBundle data, final double optionPrice) {
    _data = data;
    _optionPrice = optionPrice;
  }

  public double getOptionPrice() {
    return _optionPrice;
  }

  public StandardOptionDataBundle getDataBundle() {
    return _data;
  }

  public OptionDataBundleWithOptionPrice withPrice(final double price) {
    return new OptionDataBundleWithOptionPrice(getDataBundle(), price);
  }

  public OptionDataBundleWithOptionPrice withData(final StandardOptionDataBundle data) {
    return new OptionDataBundleWithOptionPrice(data, getOptionPrice());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_data == null ? 0 : _data.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_optionPrice);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    final OptionDataBundleWithOptionPrice other = (OptionDataBundleWithOptionPrice) obj;
    if (_data == null) {
      if (other._data != null)
        return false;
    } else if (!_data.equals(other._data))
      return false;
    if (Double.doubleToLongBits(_optionPrice) != Double.doubleToLongBits(other._optionPrice))
      return false;
    return true;
  }
}
