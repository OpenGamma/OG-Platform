package com.opengamma.financial;

/**
 * Representation of a GICS code.
 * 
 * @author Andrew
 */
public class GICSCode {
  
  private final int _code;
  
  private GICSCode (final int code) {
    _code = code;
  }
  
  public static GICSCode getInstance (final int code) {
    if ((code < 1) || (code > 99999999)) throw new IllegalArgumentException ("code out of range " + code);
    int c = code;
    while (c >= 100) {
      if ((c % 100) == 0) throw new IllegalArgumentException ("invalid code " + code);
      c /= 100;
    }
    return new GICSCode (code);
  }
  
  public static GICSCode getInstance (final String code) {
    try {
      return getInstance (Integer.parseInt (code));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException ("code is not valid", e);
    }
  }
  
  public int getCode () {
    return _code;
  }
  
  public int getSectorCode () {
    int c = getCode ();
    while (c >= 100) {
      c /= 100;
    }
    return c;
  }
  
  public int getIndustryGroupCode () {
    int c = getCode ();
    if (c < 100) return -1;
    while (c >= 10000) {
      c /= 100;
    }
    return c % 100;
  }
  
  public int getIndustryCode () {
    int c = getCode ();
    if (c < 10000) return -1;
    while (c >= 1000000) {
      c /= 100;
    }
    return c % 100;
  }
  
  public int getSubIndustryCode () {
    int c = getCode ();
    if (c < 1000000) return -1;
    return c % 100;
  }
  
  @Override
  public String toString () {
    return "" + getCode ();
  }
  
  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (!(o instanceof GICSCode)) return false;
    return ((GICSCode)o).getCode () == getCode ();
  }
  
  @Override
  public int hashCode () {
    return getCode ();
  }
  
}