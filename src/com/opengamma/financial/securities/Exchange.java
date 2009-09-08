package com.opengamma.financial.securities;

import com.opengamma.util.CompareUtils;

public class Exchange {

  private String _shortCode;
  private String _fullName;
  private ExchangeType _type;
  
  public Exchange(String shortCode, String fullName) {
    _shortCode = shortCode;
    _fullName = fullName;
    _type = ExchangeType.getType(_shortCode);
  }

  public String getShortCode() {
    return _shortCode;
  }

  public String getFullName() {
    return _fullName;
  }
  
  public String toString() {
    return "Exchange[shortCode="+getShortCode()+", "+getFullName()+"]";
  }
  
  public ExchangeType getExchangeType() {
    return _type;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof Exchange)) {
      return false;
    }
    Exchange other = (Exchange) o;
    if (!CompareUtils.equalsWithNull(other.getShortCode(), getShortCode())) {
      return false;
    }
    return CompareUtils.equalsWithNull(other.getFullName(), getFullName());
  }
  
  public int hashCode() {
    return _shortCode.hashCode();
  }
}
