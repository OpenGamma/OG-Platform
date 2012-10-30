// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.marketdata.spec;
public class CombinedMarketDataSpecification extends com.opengamma.engine.marketdata.spec.MarketDataSpecification implements java.io.Serializable {
  private static final long serialVersionUID = -14180547628570l;
  private com.opengamma.engine.marketdata.spec.MarketDataSpecification _preferredSpecification;
  public static final String PREFERRED_SPECIFICATION_KEY = "preferredSpecification";
  private com.opengamma.engine.marketdata.spec.MarketDataSpecification _fallbackSpecification;
  public static final String FALLBACK_SPECIFICATION_KEY = "fallbackSpecification";
  public CombinedMarketDataSpecification (com.opengamma.engine.marketdata.spec.MarketDataSpecification preferredSpecification, com.opengamma.engine.marketdata.spec.MarketDataSpecification fallbackSpecification) {
    if (preferredSpecification == null) throw new NullPointerException ("'preferredSpecification' cannot be null");
    else {
      _preferredSpecification = preferredSpecification;
    }
    if (fallbackSpecification == null) throw new NullPointerException ("'fallbackSpecification' cannot be null");
    else {
      _fallbackSpecification = fallbackSpecification;
    }
  }
  protected CombinedMarketDataSpecification (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (PREFERRED_SPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CombinedMarketDataSpecification - field 'preferredSpecification' is not present");
    try {
      _preferredSpecification = com.opengamma.engine.marketdata.spec.MarketDataSpecification.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CombinedMarketDataSpecification - field 'preferredSpecification' is not MarketDataSpecification message", e);
    }
    fudgeField = fudgeMsg.getByName (FALLBACK_SPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CombinedMarketDataSpecification - field 'fallbackSpecification' is not present");
    try {
      _fallbackSpecification = com.opengamma.engine.marketdata.spec.MarketDataSpecification.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CombinedMarketDataSpecification - field 'fallbackSpecification' is not MarketDataSpecification message", e);
    }
  }
  protected CombinedMarketDataSpecification (final CombinedMarketDataSpecification source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._preferredSpecification == null) _preferredSpecification = null;
    else {
      _preferredSpecification = source._preferredSpecification;
    }
    if (source._fallbackSpecification == null) _fallbackSpecification = null;
    else {
      _fallbackSpecification = source._fallbackSpecification;
    }
  }
  public CombinedMarketDataSpecification clone () {
    return new CombinedMarketDataSpecification (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_preferredSpecification != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _preferredSpecification.getClass (), com.opengamma.engine.marketdata.spec.MarketDataSpecification.class);
      _preferredSpecification.toFudgeMsg (serializer, fudge1);
      msg.add (PREFERRED_SPECIFICATION_KEY, null, fudge1);
    }
    if (_fallbackSpecification != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _fallbackSpecification.getClass (), com.opengamma.engine.marketdata.spec.MarketDataSpecification.class);
      _fallbackSpecification.toFudgeMsg (serializer, fudge1);
      msg.add (FALLBACK_SPECIFICATION_KEY, null, fudge1);
    }
  }
  public static CombinedMarketDataSpecification fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.marketdata.spec.CombinedMarketDataSpecification".equals (className)) break;
      try {
        return (com.opengamma.engine.marketdata.spec.CombinedMarketDataSpecification)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new CombinedMarketDataSpecification (deserializer, fudgeMsg);
  }
  public com.opengamma.engine.marketdata.spec.MarketDataSpecification getPreferredSpecification () {
    return _preferredSpecification;
  }
  public void setPreferredSpecification (com.opengamma.engine.marketdata.spec.MarketDataSpecification preferredSpecification) {
    if (preferredSpecification == null) throw new NullPointerException ("'preferredSpecification' cannot be null");
    else {
      _preferredSpecification = preferredSpecification;
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
    if (_preferredSpecification != null) {
      if (msg._preferredSpecification != null) {
        if (!_preferredSpecification.equals (msg._preferredSpecification)) return false;
      }
      else return false;
    }
    else if (msg._preferredSpecification != null) return false;
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
    if (_preferredSpecification != null) hc += _preferredSpecification.hashCode ();
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
