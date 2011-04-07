// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/financial/security/FinancialSecurity.proto:12(19)
package com.opengamma.financial.security;
public abstract class FinancialSecurity extends com.opengamma.master.security.ManageableSecurity implements java.io.Serializable {
          public abstract <T> T accept (FinancialSecurityVisitor<T> visitor);
  private static final long serialVersionUID = 1l;
  public FinancialSecurity (String securityType) {
    super (securityType);
  }
  protected FinancialSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
  }
  public FinancialSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers) {
    super (uniqueId, name, securityType, identifiers);
  }
  protected FinancialSecurity (final FinancialSecurity source) {
    super (source);
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
  }
  public static FinancialSecurity fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.FinancialSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.FinancialSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    throw new UnsupportedOperationException ("FinancialSecurity is an abstract message");
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FinancialSecurity)) return false;
    FinancialSecurity msg = (FinancialSecurity)o;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
