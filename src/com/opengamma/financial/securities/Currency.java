package com.opengamma.financial.securities;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.util.CompareUtils;

public class Currency {
  private String _isoCode;

  private Currency(String isoCode) {
    _isoCode = isoCode;
  }
  
  public String getISOCode() {
    return _isoCode;
  }
  
  public static Map<String, Currency> s_instanceMap = new HashMap<String, Currency>();
  // is this even necessary or a good idea?  Probably should 
  public static Currency getInstance(String isoCode) {
    CompareUtils.checkForNull(isoCode);
    if (s_instanceMap.containsKey(isoCode)) {
      return s_instanceMap.get(isoCode);
    } else {
      Currency curr = new Currency(isoCode);
      s_instanceMap.put(isoCode, curr);
      return curr;
    }
  }
  
  // NOTE: This relies on the above getInstance pattern being used.  If that is ditched, get rid.
  public boolean equals(Object o) {
    if (!(o instanceof Currency)) {
      return false;
    }
    Currency curr = (Currency) o;
    return (curr == this); 
  }
  
  public int hashCode() {
    return _isoCode.hashCode();
  }
  
  public String toString() {
    return _isoCode;
  }
}
