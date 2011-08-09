// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class CashOrNothingPayoffStyle extends com.opengamma.financial.security.option.PayoffStyle implements java.io.Serializable {
  public <T> T accept (PayoffStyleVisitor<T> visitor) { return visitor.visitCashOrNothingPayoffStyle(this); }
  private static final long serialVersionUID = -25713078708l;
  private final double _payment;
  public static final String PAYMENT_KEY = "payment";
  public CashOrNothingPayoffStyle (double payment) {
    _payment = payment;
  }
  protected CashOrNothingPayoffStyle (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (PAYMENT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CashOrNothingPayoffStyle - field 'payment' is not present");
    try {
      _payment = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CashOrNothingPayoffStyle - field 'payment' is not double", e);
    }
  }
  protected CashOrNothingPayoffStyle (final CashOrNothingPayoffStyle source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _payment = source._payment;
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    msg.add (PAYMENT_KEY, null, _payment);
  }
  public static CashOrNothingPayoffStyle fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.CashOrNothingPayoffStyle".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.CashOrNothingPayoffStyle)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new CashOrNothingPayoffStyle (deserializer, fudgeMsg);
  }
  public double getPayment () {
    return _payment;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof CashOrNothingPayoffStyle)) return false;
    CashOrNothingPayoffStyle msg = (CashOrNothingPayoffStyle)o;
    if (_payment != msg._payment) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc = (hc * 31) + (int)_payment;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
