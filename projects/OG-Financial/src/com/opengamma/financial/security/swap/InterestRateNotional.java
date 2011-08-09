// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.swap;
public class InterestRateNotional extends com.opengamma.financial.security.swap.Notional implements java.io.Serializable {
  public <T> T accept (NotionalVisitor<T> visitor) { return visitor.visitInterestRateNotional (this); }
  private static final long serialVersionUID = 15865472633494l;
  private final com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  private final double _amount;
  public static final String AMOUNT_KEY = "amount";
  public InterestRateNotional (com.opengamma.util.money.Currency currency, double amount) {
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    _amount = amount;
  }
  protected InterestRateNotional (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a InterestRateNotional - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a InterestRateNotional - field 'currency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (AMOUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a InterestRateNotional - field 'amount' is not present");
    try {
      _amount = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a InterestRateNotional - field 'amount' is not double", e);
    }
  }
  protected InterestRateNotional (final InterestRateNotional source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _currency = source._currency;
    _amount = source._amount;
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
    msg.add (AMOUNT_KEY, null, _amount);
  }
  public static InterestRateNotional fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.InterestRateNotional".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.InterestRateNotional)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new InterestRateNotional (deserializer, fudgeMsg);
  }
  public com.opengamma.util.money.Currency getCurrency () {
    return _currency;
  }
  public double getAmount () {
    return _amount;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof InterestRateNotional)) return false;
    InterestRateNotional msg = (InterestRateNotional)o;
    if (_currency != null) {
      if (msg._currency != null) {
        if (!_currency.equals (msg._currency)) return false;
      }
      else return false;
    }
    else if (msg._currency != null) return false;
    if (_amount != msg._amount) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_currency != null) hc += _currency.hashCode ();
    hc = (hc * 31) + (int)_amount;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
