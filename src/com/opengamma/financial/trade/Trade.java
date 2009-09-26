/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.trade;

import com.opengamma.financial.securities.descriptions.Description;

/**
 * Best trade class ever
 * @author jim & elaine
 */
public class Trade<T> {
  private final double _amount;
  private final Description<T> _description;
  public Trade(Description<T> description, double amount) {
    _description = description;
    _amount = amount;   
  }
  
  public double getAmount() {
    return _amount;
  }
  
  public Description<T> getDescription() {
    return _description;
  }
  
  public String toString() {
    return "Trade["+getDescription()+", "+getAmount()+"]";
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result
        + ((_description == null) ? 0 : _description.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Trade other = (Trade) obj;
    if (Double.doubleToLongBits(_amount) != Double
        .doubleToLongBits(other._amount))
      return false;
    if (_description == null) {
      if (other._description != null)
        return false;
    } else if (!_description.equals(other._description))
      return false;
    return true;
  }
}
