// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/financial/security/swap/ForwardSwapSecurity.proto:9(10)
package com.opengamma.financial.security.swap;
public class ForwardSwapSecurity extends com.opengamma.financial.security.swap.SwapSecurity implements java.io.Serializable {
  public <T> T accept (SwapSecurityVisitor<T> visitor) { return visitor.visitForwardSwapSecurity (this); }
  private static final long serialVersionUID = -11591641618l;
  private com.opengamma.financial.security.DateTimeWithZone _forwardStartDate;
  public static final String FORWARD_START_DATE_KEY = "forwardStartDate";
  public ForwardSwapSecurity (com.opengamma.financial.security.DateTimeWithZone tradeDate, com.opengamma.financial.security.DateTimeWithZone effectiveDate, com.opengamma.financial.security.DateTimeWithZone maturityDate, String counterparty, com.opengamma.financial.security.swap.SwapLeg payLeg, com.opengamma.financial.security.swap.SwapLeg receiveLeg, com.opengamma.financial.security.DateTimeWithZone forwardStartDate) {
    super (tradeDate, effectiveDate, maturityDate, counterparty, payLeg, receiveLeg);
    if (forwardStartDate == null) throw new NullPointerException ("'forwardStartDate' cannot be null");
    else {
      _forwardStartDate = (com.opengamma.financial.security.DateTimeWithZone)forwardStartDate.clone ();
    }
  }
  protected ForwardSwapSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (FORWARD_START_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ForwardSwapSecurity - field 'forwardStartDate' is not present");
    try {
      _forwardStartDate = com.opengamma.financial.security.DateTimeWithZone.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ForwardSwapSecurity - field 'forwardStartDate' is not DateTimeWithZone message", e);
    }
  }
  public ForwardSwapSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.financial.security.DateTimeWithZone tradeDate, com.opengamma.financial.security.DateTimeWithZone effectiveDate, com.opengamma.financial.security.DateTimeWithZone maturityDate, String counterparty, com.opengamma.financial.security.swap.SwapLeg payLeg, com.opengamma.financial.security.swap.SwapLeg receiveLeg, com.opengamma.financial.security.DateTimeWithZone forwardStartDate) {
    super (uniqueId, name, securityType, identifiers, tradeDate, effectiveDate, maturityDate, counterparty, payLeg, receiveLeg);
    if (forwardStartDate == null) throw new NullPointerException ("'forwardStartDate' cannot be null");
    else {
      _forwardStartDate = (com.opengamma.financial.security.DateTimeWithZone)forwardStartDate.clone ();
    }
  }
  protected ForwardSwapSecurity (final ForwardSwapSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._forwardStartDate == null) _forwardStartDate = null;
    else {
      _forwardStartDate = (com.opengamma.financial.security.DateTimeWithZone)source._forwardStartDate.clone ();
    }
  }
  public ForwardSwapSecurity clone () {
    return new ForwardSwapSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_forwardStartDate != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _forwardStartDate.getClass (), com.opengamma.financial.security.DateTimeWithZone.class);
      _forwardStartDate.toFudgeMsg (fudgeContext, fudge1);
      msg.add (FORWARD_START_DATE_KEY, null, fudge1);
    }
  }
  public static ForwardSwapSecurity fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.ForwardSwapSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.ForwardSwapSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ForwardSwapSecurity (fudgeMsg);
  }
  public com.opengamma.financial.security.DateTimeWithZone getForwardStartDate () {
    return _forwardStartDate;
  }
  public void setForwardStartDate (com.opengamma.financial.security.DateTimeWithZone forwardStartDate) {
    if (forwardStartDate == null) throw new NullPointerException ("'forwardStartDate' cannot be null");
    else {
      _forwardStartDate = (com.opengamma.financial.security.DateTimeWithZone)forwardStartDate.clone ();
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
