package com.opengamma.financial.securities;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.util.CompareUtils;

// REVIEW kirk 2009-09-15 -- This REALLY needs to be renamed.

// REVIEW kirk 2009-09-16 -- This needs to be worked out better for serialization.
// It's really not serialization clean, and everything at this level needs to be
// serialization friendly.

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
    // REVIEW kirk 2009-09-16 -- This is really not matching good practice:
    // - Will allow lower-case ISO codes
    // - Will allow ISO codes outside normal rules (e.g. 3-letter)
    // - Isn't even concurrency safe
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
    return (this == o);
  }
  
  public int hashCode() {
    return _isoCode.hashCode();
  }
  
  public String toString() {
    return _isoCode;
  }
}
