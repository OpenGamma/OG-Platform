// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class SwaptionSecurity extends com.opengamma.financial.security.option.OptionSecurity implements java.io.Serializable {
  public <T> T accept (OptionSecurityVisitor<T> visitor) { return visitor.visitSwaptionSecurity (this); }
  private static final long serialVersionUID = 23247505560l;
  public static final String SECURITY_TYPE = "SWAPTION";
  public SwaptionSecurity (com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.financial.security.option.PayoffStyle payoffStyle, com.opengamma.financial.security.option.OptionType optionType, double strike, com.opengamma.util.time.Expiry expiry, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.util.money.Currency currency) {
    super (SECURITY_TYPE, exerciseType, payoffStyle, optionType, strike, expiry, underlyingIdentifier, currency);
  }
  protected SwaptionSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
  }
  public SwaptionSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.financial.security.option.PayoffStyle payoffStyle, com.opengamma.financial.security.option.OptionType optionType, double strike, com.opengamma.util.time.Expiry expiry, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.util.money.Currency currency) {
    super (uniqueId, name, securityType, identifiers, exerciseType, payoffStyle, optionType, strike, expiry, underlyingIdentifier, currency);
  }
  protected SwaptionSecurity (final SwaptionSecurity source) {
    super (source);
    if (source != null) {
    }
    else {
      setSecurityType (SECURITY_TYPE);
    }
  }
  public SwaptionSecurity clone () {
    return new SwaptionSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
  }
  public static SwaptionSecurity fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.SwaptionSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.SwaptionSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new SwaptionSecurity (fudgeMsg);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof SwaptionSecurity)) return false;
    SwaptionSecurity msg = (SwaptionSecurity)o;
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
