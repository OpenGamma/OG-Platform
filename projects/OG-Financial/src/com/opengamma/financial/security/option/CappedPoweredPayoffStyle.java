// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class CappedPoweredPayoffStyle extends com.opengamma.financial.security.option.PayoffStyle implements java.io.Serializable {
  public <T> T accept (PayoffStyleVisitor<T> visitor) { return visitor.visitCappedPoweredPayoffStyle(this); }
  private static final long serialVersionUID = 1907861419564l;
  private final double _power;
  public static final String POWER_KEY = "power";
  private final double _cap;
  public static final String CAP_KEY = "cap";
  public CappedPoweredPayoffStyle (double power, double cap) {
    _power = power;
    _cap = cap;
  }
  protected CappedPoweredPayoffStyle (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (POWER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CappedPoweredPayoffStyle - field 'power' is not present");
    try {
      _power = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CappedPoweredPayoffStyle - field 'power' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (CAP_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CappedPoweredPayoffStyle - field 'cap' is not present");
    try {
      _cap = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CappedPoweredPayoffStyle - field 'cap' is not double", e);
    }
  }
  protected CappedPoweredPayoffStyle (final CappedPoweredPayoffStyle source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _power = source._power;
    _cap = source._cap;
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    msg.add (POWER_KEY, null, _power);
    msg.add (CAP_KEY, null, _cap);
  }
  public static CappedPoweredPayoffStyle fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.CappedPoweredPayoffStyle".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.CappedPoweredPayoffStyle)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new CappedPoweredPayoffStyle (fudgeMsg);
  }
  public double getPower () {
    return _power;
  }
  public double getCap () {
    return _cap;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof CappedPoweredPayoffStyle)) return false;
    CappedPoweredPayoffStyle msg = (CappedPoweredPayoffStyle)o;
    if (_power != msg._power) return false;
    if (_cap != msg._cap) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc = (hc * 31) + (int)_power;
    hc = (hc * 31) + (int)_cap;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
