// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class PoweredPayoffStyle extends com.opengamma.financial.security.option.PayoffStyle implements java.io.Serializable {
  public <T> T accept (PayoffStyleVisitor<T> visitor) { return visitor.visitPoweredPayoffStyle(this); }
  private static final long serialVersionUID = 1986664237l;
  private final double _power;
  public static final String POWER_KEY = "power";
  public PoweredPayoffStyle (double power) {
    _power = power;
  }
  protected PoweredPayoffStyle (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (POWER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a PoweredPayoffStyle - field 'power' is not present");
    try {
      _power = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a PoweredPayoffStyle - field 'power' is not double", e);
    }
  }
  protected PoweredPayoffStyle (final PoweredPayoffStyle source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _power = source._power;
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    msg.add (POWER_KEY, null, _power);
  }
  public static PoweredPayoffStyle fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.PoweredPayoffStyle".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.PoweredPayoffStyle)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new PoweredPayoffStyle (fudgeMsg);
  }
  public double getPower () {
    return _power;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof PoweredPayoffStyle)) return false;
    PoweredPayoffStyle msg = (PoweredPayoffStyle)o;
    if (_power != msg._power) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc = (hc * 31) + (int)_power;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
