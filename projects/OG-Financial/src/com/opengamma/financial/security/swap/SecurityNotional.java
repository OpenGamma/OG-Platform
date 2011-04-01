// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.swap;
public class SecurityNotional extends com.opengamma.financial.security.swap.Notional implements java.io.Serializable {
  public <T> T accept (NotionalVisitor<T> visitor) { return visitor.visitSecurityNotional (this); }
  private static final long serialVersionUID = -35332849337l;
  private final com.opengamma.id.UniqueIdentifier _notionalIdentifier;
  public static final String NOTIONAL_IDENTIFIER_KEY = "notionalIdentifier";
  public SecurityNotional (com.opengamma.id.UniqueIdentifier notionalIdentifier) {
    if (notionalIdentifier == null) throw new NullPointerException ("'notionalIdentifier' cannot be null");
    else {
      _notionalIdentifier = notionalIdentifier;
    }
  }
  protected SecurityNotional (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (NOTIONAL_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SecurityNotional - field 'notionalIdentifier' is not present");
    try {
      _notionalIdentifier = com.opengamma.id.UniqueIdentifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SecurityNotional - field 'notionalIdentifier' is not UniqueIdentifier message", e);
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
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_notionalIdentifier != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _notionalIdentifier.getClass (), com.opengamma.id.UniqueIdentifier.class);
      _notionalIdentifier.toFudgeMsg (fudgeContext, fudge1);
      msg.add (NOTIONAL_IDENTIFIER_KEY, null, fudge1);
    }
  }
  public static SecurityNotional fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.SecurityNotional".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.SecurityNotional)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new SecurityNotional (fudgeMsg);
  }
  public com.opengamma.id.UniqueIdentifier getNotionalIdentifier () {
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
