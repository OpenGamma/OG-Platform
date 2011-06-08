// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class SwaptionSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public <T> T accept (SwaptionSecurityVisitor<T> visitor) { return visitor.visitSwaptionSecurity(this); }
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitSwaptionSecurity(this); }
  private static final long serialVersionUID = 2195944140107062575l;
  private com.opengamma.id.Identifier _underlyingIdentifier;
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";
  private com.opengamma.util.time.Expiry _expiry;
  public static final String EXPIRY_KEY = "expiry";
  private boolean _isCashSettled;
  public static final String IS_CASH_SETTLED_KEY = "isCashSettled";
  public static final String SECURITY_TYPE = "SWAPTION";
  public SwaptionSecurity (com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.util.time.Expiry expiry, boolean isCashSettled) {
    super (SECURITY_TYPE);
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    _isCashSettled = isCashSettled;
  }
  protected SwaptionSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (UNDERLYING_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'underlyingIdentifier' is not present");
    try {
      _underlyingIdentifier = com.opengamma.id.Identifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'underlyingIdentifier' is not Identifier message", e);
    }
    fudgeField = fudgeMsg.getByName (EXPIRY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'expiry' is not present");
    try {
      _expiry = com.opengamma.util.time.Expiry.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'expiry' is not Expiry message", e);
    }
    fudgeField = fudgeMsg.getByName (IS_CASH_SETTLED_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'isCashSettled' is not present");
    try {
      _isCashSettled = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'isCashSettled' is not boolean", e);
    }
  }
  public SwaptionSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.util.time.Expiry expiry, boolean isCashSettled) {
    super (uniqueId, name, securityType, identifiers);
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    _isCashSettled = isCashSettled;
  }
  protected SwaptionSecurity (final SwaptionSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = source._underlyingIdentifier;
    }
    if (source._expiry == null) _expiry = null;
    else {
      _expiry = source._expiry;
    }
    _isCashSettled = source._isCashSettled;
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
    if (_underlyingIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _underlyingIdentifier.getClass (), com.opengamma.id.Identifier.class);
      _underlyingIdentifier.toFudgeMsg (fudgeContext, fudge1);
      msg.add (UNDERLYING_IDENTIFIER_KEY, null, fudge1);
    }
    if (_expiry != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _expiry.getClass (), com.opengamma.util.time.Expiry.class);
      _expiry.toFudgeMsg (fudgeContext, fudge1);
      msg.add (EXPIRY_KEY, null, fudge1);
    }
    msg.add (IS_CASH_SETTLED_KEY, null, _isCashSettled);
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
  public com.opengamma.id.Identifier getUnderlyingIdentifier () {
    return _underlyingIdentifier;
  }
  public void setUnderlyingIdentifier (com.opengamma.id.Identifier underlyingIdentifier) {
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
  }
  public com.opengamma.util.time.Expiry getExpiry () {
    return _expiry;
  }
  public void setExpiry (com.opengamma.util.time.Expiry expiry) {
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
  }
  public boolean getIsCashSettled () {
    return _isCashSettled;
  }
  public void setIsCashSettled (boolean isCashSettled) {
    _isCashSettled = isCashSettled;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof SwaptionSecurity)) return false;
    SwaptionSecurity msg = (SwaptionSecurity)o;
    if (_underlyingIdentifier != null) {
      if (msg._underlyingIdentifier != null) {
        if (!_underlyingIdentifier.equals (msg._underlyingIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._underlyingIdentifier != null) return false;
    if (_expiry != null) {
      if (msg._expiry != null) {
        if (!_expiry.equals (msg._expiry)) return false;
      }
      else return false;
    }
    else if (msg._expiry != null) return false;
    if (_isCashSettled != msg._isCashSettled) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_underlyingIdentifier != null) hc += _underlyingIdentifier.hashCode ();
    hc *= 31;
    if (_expiry != null) hc += _expiry.hashCode ();
    hc *= 31;
    if (_isCashSettled) hc++;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
