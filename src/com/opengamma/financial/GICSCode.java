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
  
  public GICSCode getInstance (final int code) {
    if ((code < 10000000) || (code > 99999999)) throw new IllegalArgumentException ("code out of range");
    if (((code / 10000) % 100) == 0) {
      // no industry group code, so check for a sector code only
      if ((code % 10000) != 0) throw new IllegalArgumentException ("code out of range");
    } else if (((code / 100) % 100) == 0) {
      // no industry code, so check for industry group code only
      if ((code % 100) != 0) throw new IllegalArgumentException ("code out of range");
    }
    return new GICSCode (code);
  }
  
  protected int getCode () {
    return _code;
  }
  
  public int getSectorCode () {
    return getCode () / 1000000;
  }
  
  public int getIndustryGroupCode () {
    return getCode () / 10000; 
  }
  
  public int getIndustryCode () {
    return getCode () / 100;
  }
  
  public int getSubIndustryCode () {
    return getCode ();
  }
  
  public String toString () {
    return "" + getCode ();
  }
  
}