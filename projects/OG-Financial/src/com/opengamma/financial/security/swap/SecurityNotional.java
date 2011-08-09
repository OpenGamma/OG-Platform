// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.swap;
public class SecurityNotional extends com.opengamma.financial.security.swap.Notional implements java.io.Serializable {
  public <T> T accept (NotionalVisitor<T> visitor) { return visitor.visitSecurityNotional (this); }
  private static final long serialVersionUID = -35384588359l;
  private final com.opengamma.id.UniqueId _notionalIdentifier;
  public static final String NOTIONAL_IDENTIFIER_KEY = "notionalIdentifier";
  public SecurityNotional (com.opengamma.id.UniqueId notionalIdentifier) {
    if (notionalIdentifier == null) throw new NullPointerException ("'notionalIdentifier' cannot be null");
    else {
      _notionalIdentifier = notionalIdentifier;
    }
  }
  protected SecurityNotional (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (NOTIONAL_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SecurityNotional - field 'notionalIdentifier' is not present");
    try {
      _notionalIdentifier = com.opengamma.id.UniqueId.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SecurityNotional - field 'notionalIdentifier' is not UniqueId message", e);
    }
  }
  protected SecurityNotional (final SecurityNotional source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._notionalIdentifier == null) _notionalIdentifier = null;
    else {
      _notionalIdentifier = source._notionalIdentifier;
    }
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_notionalIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _notionalIdentifier.getClass (), com.opengamma.id.UniqueId.class);
      _notionalIdentifier.toFudgeMsg (serializer, fudge1);
      msg.add (NOTIONAL_IDENTIFIER_KEY, null, fudge1);
    }
  }
  public static SecurityNotional fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.SecurityNotional".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.SecurityNotional)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new SecurityNotional (deserializer, fudgeMsg);
  }
  public com.opengamma.id.UniqueId getNotionalIdentifier () {
    return _notionalIdentifier;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof SecurityNotional)) return false;
    SecurityNotional msg = (SecurityNotional)o;
    if (_notionalIdentifier != null) {
      if (msg._notionalIdentifier != null) {
        if (!_notionalIdentifier.equals (msg._notionalIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._notionalIdentifier != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_notionalIdentifier != null) hc += _notionalIdentifier.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
