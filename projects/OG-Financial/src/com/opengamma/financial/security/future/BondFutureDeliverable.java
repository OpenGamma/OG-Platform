// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.future;
public class BondFutureDeliverable implements java.io.Serializable {
  private static final long serialVersionUID = 39227779606784l;
  private com.opengamma.id.ExternalIdBundle _identifiers;
  public static final String IDENTIFIERS_KEY = "identifiers";
  private double _conversionFactor;
  public static final String CONVERSION_FACTOR_KEY = "conversionFactor";
  public BondFutureDeliverable (com.opengamma.id.ExternalIdBundle identifiers, double conversionFactor) {
    if (identifiers == null) throw new NullPointerException ("'identifiers' cannot be null");
    else {
      _identifiers = identifiers;
    }
    _conversionFactor = conversionFactor;
  }
  protected BondFutureDeliverable (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (IDENTIFIERS_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondFutureDeliverable - field 'identifiers' is not present");
    try {
      _identifiers = com.opengamma.id.ExternalIdBundle.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondFutureDeliverable - field 'identifiers' is not ExternalIdBundle message", e);
    }
    fudgeField = fudgeMsg.getByName (CONVERSION_FACTOR_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondFutureDeliverable - field 'conversionFactor' is not present");
    try {
      _conversionFactor = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondFutureDeliverable - field 'conversionFactor' is not double", e);
    }
  }
  protected BondFutureDeliverable (final BondFutureDeliverable source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._identifiers == null) _identifiers = null;
    else {
      _identifiers = source._identifiers;
    }
    _conversionFactor = source._conversionFactor;
  }
  public BondFutureDeliverable clone () {
    return new BondFutureDeliverable (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_identifiers != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _identifiers.getClass (), com.opengamma.id.ExternalIdBundle.class);
      _identifiers.toFudgeMsg (serializer, fudge1);
      msg.add (IDENTIFIERS_KEY, null, fudge1);
    }
    msg.add (CONVERSION_FACTOR_KEY, null, _conversionFactor);
  }
  public static BondFutureDeliverable fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.future.BondFutureDeliverable".equals (className)) break;
      try {
        return (com.opengamma.financial.security.future.BondFutureDeliverable)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new BondFutureDeliverable (deserializer, fudgeMsg);
  }
  public com.opengamma.id.ExternalIdBundle getIdentifiers () {
    return _identifiers;
  }
  public void setIdentifiers (com.opengamma.id.ExternalIdBundle identifiers) {
    if (identifiers == null) throw new NullPointerException ("'identifiers' cannot be null");
    else {
      _identifiers = identifiers;
    }
  }
  public double getConversionFactor () {
    return _conversionFactor;
  }
  public void setConversionFactor (double conversionFactor) {
    _conversionFactor = conversionFactor;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof BondFutureDeliverable)) return false;
    BondFutureDeliverable msg = (BondFutureDeliverable)o;
    if (_identifiers != null) {
      if (msg._identifiers != null) {
        if (!_identifiers.equals (msg._identifiers)) return false;
      }
      else return false;
    }
    else if (msg._identifiers != null) return false;
    if (_conversionFactor != msg._conversionFactor) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_identifiers != null) hc += _identifiers.hashCode ();
    hc = (hc * 31) + (int)_conversionFactor;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
