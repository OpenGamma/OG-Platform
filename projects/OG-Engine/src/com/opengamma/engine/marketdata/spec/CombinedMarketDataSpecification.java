// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.marketdata.spec;
public class CombinedMarketDataSpecification extends com.opengamma.engine.marketdata.spec.MarketDataSpecification implements java.io.Serializable {
  private static final long serialVersionUID = -56192686619500l;
  private com.opengamma.engine.marketdata.spec.MarketDataSpecification _prefferedSpecification;
  public static final String PREFFERED_SPECIFICATION_KEY = "prefferedSpecification";
  private com.opengamma.engine.marketdata.spec.MarketDataSpecification _fallbackSpecification;
  public static final String FALLBACK_SPECIFICATION_KEY = "fallbackSpecification";
  public CombinedMarketDataSpecification (com.opengamma.engine.marketdata.spec.MarketDataSpecification prefferedSpecification, com.opengamma.engine.marketdata.spec.MarketDataSpecification fallbackSpecification) {
    if (prefferedSpecification == null) throw new NullPointerException ("'prefferedSpecification' cannot be null");
    else {
      _prefferedSpecification = prefferedSpecification;
    }
    if (fallbackSpecification == null) throw new NullPointerException ("'fallbackSpecification' cannot be null");
    else {
      _fallbackSpecification = fallbackSpecification;
    }
  }
  protected CombinedMarketDataSpecification (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (PREFFERED_SPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CombinedMarketDataSpecification - field 'prefferedSpecification' is not present");
    try {
      _prefferedSpecification = com.opengamma.engine.marketdata.spec.MarketDataSpecification.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CombinedMarketDataSpecification - field 'prefferedSpecification' is not MarketDataSpecification message", e);
    }
    fudgeField = fudgeMsg.getByName (FALLBACK_SPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CombinedMarketDataSpecification - field 'fallbackSpecification' is not present");
    try {
      _fallbackSpecification = com.opengamma.engine.marketdata.spec.MarketDataSpecification.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CombinedMarketDataSpecification - field 'fallbackSpecification' is not MarketDataSpecification message", e);
    }
  }
  protected CombinedMarketDataSpecification (final CombinedMarketDataSpecification source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._prefferedSpecification == null) _prefferedSpecification = null;
    else {
      _prefferedSpecification = source._prefferedSpecification;
    }
    if (source._fallbackSpecification == null) _fallbackSpecification = null;
    else {
      _fallbackSpecification = source._fallbackSpecification;
    }
  }
  public CombinedMarketDataSpecification clone () {
    return new CombinedMarketDataSpecification (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_prefferedSpecification != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _prefferedSpecification.getClass (), com.opengamma.engine.marketdata.spec.MarketDataSpecification.class);
      _prefferedSpecification.toFudgeMsg (fudgeContext, fudge1);
      msg.add (PREFFERED_SPECIFICATION_KEY, null, fudge1);
    }
    if (_fallbackSpecification != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _fallbackSpecification.getClass (), com.opengamma.engine.marketdata.spec.MarketDataSpecification.class);
      _fallbackSpecification.toFudgeMsg (fudgeContext, fudge1);
      msg.add (FALLBACK_SPECIFICATION_KEY, null, fudge1);
    }
  }
  public static CombinedMarketDataSpecification fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.marketdata.spec.CombinedMarketDataSpecification".equals (className)) break;
      try {
        return (com.opengamma.engine.marketdata.spec.CombinedMarketDataSpecification)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new CombinedMarketDataSpecification (fudgeContext, fudgeMsg);
  }
  public com.opengamma.engine.marketdata.spec.MarketDataSpecification getPrefferedSpecification () {
    return _prefferedSpecification;
  }
  public void setPrefferedSpecification (com.opengamma.engine.marketdata.spec.MarketDataSpecification prefferedSpecification) {
    if (prefferedSpecification == null) throw new NullPointerException ("'prefferedSpecification' cannot be null");
    else {
      _prefferedSpecification = prefferedSpecification;
    }
  }
  public com.opengamma.engine.marketdata.spec.MarketDataSpecification getFallbackSpecification () {
    return _fallbackSpecification;
  }
  public void setFallbackSpecification (com.opengamma.engine.marketdata.spec.MarketDataSpecification fallbackSpecification) {
    if (fallbackSpecification == null) throw new NullPointerException ("'fallbackSpecification' cannot be null");
    else {
      _fallbackSpecification = fallbackSpecification;
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof CombinedMarketDataSpecification)) return false;
    CombinedMarketDataSpecification msg = (CombinedMarketDataSpecification)o;
    if (_prefferedSpecification != null) {
      if (msg._prefferedSpecification != null) {
        if (!_prefferedSpecification.equals (msg._prefferedSpecification)) return false;
      }
      else return false;
    }
    else if (msg._prefferedSpecification != null) return false;
    if (_fallbackSpecification != null) {
      if (msg._fallbackSpecification != null) {
        if (!_fallbackSpecification.equals (msg._fallbackSpecification)) return false;
      }
      else return false;
    }
    else if (msg._fallbackSpecification != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_prefferedSpecification != null) hc += _prefferedSpecification.hashCode ();
    hc *= 31;
    if (_fallbackSpecification != null) hc += _fallbackSpecification.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
