// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.swap;
public class ForwardSwapSecurity extends com.opengamma.financial.security.swap.SwapSecurity implements java.io.Serializable {
  public <T> T accept (SwapSecurityVisitor<T> visitor) { return visitor.visitForwardSwapSecurity (this); }
  private static final long serialVersionUID = -14571438270l;
  private javax.time.calendar.ZonedDateTime _forwardStartDate;
  public static final String FORWARD_START_DATE_KEY = "forwardStartDate";
  public ForwardSwapSecurity (javax.time.calendar.ZonedDateTime tradeDate, javax.time.calendar.ZonedDateTime effectiveDate, javax.time.calendar.ZonedDateTime maturityDate, String counterparty, com.opengamma.financial.security.swap.SwapLeg payLeg, com.opengamma.financial.security.swap.SwapLeg receiveLeg, javax.time.calendar.ZonedDateTime forwardStartDate) {
    super (tradeDate, effectiveDate, maturityDate, counterparty, payLeg, receiveLeg);
    if (forwardStartDate == null) throw new NullPointerException ("'forwardStartDate' cannot be null");
    else {
      _forwardStartDate = forwardStartDate;
    }
  }
  protected ForwardSwapSecurity (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (FORWARD_START_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ForwardSwapSecurity - field 'forwardStartDate' is not present");
    try {
      _forwardStartDate = deserializer.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ForwardSwapSecurity - field 'forwardStartDate' is not ZonedDateTime typedef", e);
    }
  }
  public ForwardSwapSecurity (com.opengamma.id.UniqueId uniqueId, String name, String securityType, com.opengamma.id.ExternalIdBundle identifiers, javax.time.calendar.ZonedDateTime tradeDate, javax.time.calendar.ZonedDateTime effectiveDate, javax.time.calendar.ZonedDateTime maturityDate, String counterparty, com.opengamma.financial.security.swap.SwapLeg payLeg, com.opengamma.financial.security.swap.SwapLeg receiveLeg, javax.time.calendar.ZonedDateTime forwardStartDate) {
    super (uniqueId, name, securityType, identifiers, tradeDate, effectiveDate, maturityDate, counterparty, payLeg, receiveLeg);
    if (forwardStartDate == null) throw new NullPointerException ("'forwardStartDate' cannot be null");
    else {
      _forwardStartDate = forwardStartDate;
    }
  }
  protected ForwardSwapSecurity (final ForwardSwapSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._forwardStartDate == null) _forwardStartDate = null;
    else {
      _forwardStartDate = source._forwardStartDate;
    }
  }
  public ForwardSwapSecurity clone () {
    return new ForwardSwapSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_forwardStartDate != null)  {
      serializer.addToMessage (msg, FORWARD_START_DATE_KEY, null, _forwardStartDate);
    }
  }
  public static ForwardSwapSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.ForwardSwapSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.ForwardSwapSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ForwardSwapSecurity (deserializer, fudgeMsg);
  }
  public javax.time.calendar.ZonedDateTime getForwardStartDate () {
    return _forwardStartDate;
  }
  public void setForwardStartDate (javax.time.calendar.ZonedDateTime forwardStartDate) {
    if (forwardStartDate == null) throw new NullPointerException ("'forwardStartDate' cannot be null");
    else {
      _forwardStartDate = forwardStartDate;
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof ForwardSwapSecurity)) return false;
    ForwardSwapSecurity msg = (ForwardSwapSecurity)o;
    if (_forwardStartDate != null) {
      if (msg._forwardStartDate != null) {
        if (!_forwardStartDate.equals (msg._forwardStartDate)) return false;
      }
      else return false;
    }
    else if (msg._forwardStartDate != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_forwardStartDate != null) hc += _forwardStartDate.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
