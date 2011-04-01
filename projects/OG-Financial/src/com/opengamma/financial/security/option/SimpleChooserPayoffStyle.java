// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class SimpleChooserPayoffStyle extends com.opengamma.financial.security.option.PayoffStyle implements java.io.Serializable {
  public <T> T accept (PayoffStyleVisitor<T> visitor) { return visitor.visitSimpleChooserPayoffStyle(this); }
  private static final long serialVersionUID = 59099522115609580l;
  private final com.opengamma.financial.security.DateTimeWithZone _chooseDate;
  public static final String CHOOSE_DATE_KEY = "chooseDate";
  private final double _underlyingStrike;
  public static final String UNDERLYING_STRIKE_KEY = "underlyingStrike";
  private final com.opengamma.util.time.Expiry _underlyingExpiry;
  public static final String UNDERLYING_EXPIRY_KEY = "underlyingExpiry";
  public SimpleChooserPayoffStyle (com.opengamma.financial.security.DateTimeWithZone chooseDate, double underlyingStrike, com.opengamma.util.time.Expiry underlyingExpiry) {
    if (chooseDate == null) throw new NullPointerException ("'chooseDate' cannot be null");
    else {
      _chooseDate = (com.opengamma.financial.security.DateTimeWithZone)chooseDate.clone ();
    }
    _underlyingStrike = underlyingStrike;
    if (underlyingExpiry == null) throw new NullPointerException ("'underlyingExpiry' cannot be null");
    else {
      _underlyingExpiry = underlyingExpiry;
    }
  }
  protected SimpleChooserPayoffStyle (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (CHOOSE_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SimpleChooserPayoffStyle - field 'chooseDate' is not present");
    try {
      _chooseDate = com.opengamma.financial.security.DateTimeWithZone.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SimpleChooserPayoffStyle - field 'chooseDate' is not DateTimeWithZone message", e);
    }
    fudgeField = fudgeMsg.getByName (UNDERLYING_STRIKE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SimpleChooserPayoffStyle - field 'underlyingStrike' is not present");
    try {
      _underlyingStrike = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SimpleChooserPayoffStyle - field 'underlyingStrike' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (UNDERLYING_EXPIRY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SimpleChooserPayoffStyle - field 'underlyingExpiry' is not present");
    try {
      _underlyingExpiry = com.opengamma.util.time.Expiry.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SimpleChooserPayoffStyle - field 'underlyingExpiry' is not Expiry message", e);
    }
  }
  protected SimpleChooserPayoffStyle (final SimpleChooserPayoffStyle source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._chooseDate == null) _chooseDate = null;
    else {
      _chooseDate = (com.opengamma.financial.security.DateTimeWithZone)source._chooseDate.clone ();
    }
    _underlyingStrike = source._underlyingStrike;
    if (source._underlyingExpiry == null) _underlyingExpiry = null;
    else {
      _underlyingExpiry = source._underlyingExpiry;
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
    if (_chooseDate != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _chooseDate.getClass (), com.opengamma.financial.security.DateTimeWithZone.class);
      _chooseDate.toFudgeMsg (fudgeContext, fudge1);
      msg.add (CHOOSE_DATE_KEY, null, fudge1);
    }
    msg.add (UNDERLYING_STRIKE_KEY, null, _underlyingStrike);
    if (_underlyingExpiry != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _underlyingExpiry.getClass (), com.opengamma.util.time.Expiry.class);
      _underlyingExpiry.toFudgeMsg (fudgeContext, fudge1);
      msg.add (UNDERLYING_EXPIRY_KEY, null, fudge1);
    }
  }
  public static SimpleChooserPayoffStyle fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.SimpleChooserPayoffStyle".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.SimpleChooserPayoffStyle)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new SimpleChooserPayoffStyle (fudgeMsg);
  }
  public com.opengamma.financial.security.DateTimeWithZone getChooseDate () {
    return _chooseDate;
  }
  public double getUnderlyingStrike () {
    return _underlyingStrike;
  }
  public com.opengamma.util.time.Expiry getUnderlyingExpiry () {
    return _underlyingExpiry;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof SimpleChooserPayoffStyle)) return false;
    SimpleChooserPayoffStyle msg = (SimpleChooserPayoffStyle)o;
    if (_chooseDate != null) {
      if (msg._chooseDate != null) {
        if (!_chooseDate.equals (msg._chooseDate)) return false;
      }
      else return false;
    }
    else if (msg._chooseDate != null) return false;
    if (_underlyingStrike != msg._underlyingStrike) return false;
    if (_underlyingExpiry != null) {
      if (msg._underlyingExpiry != null) {
        if (!_underlyingExpiry.equals (msg._underlyingExpiry)) return false;
      }
      else return false;
    }
    else if (msg._underlyingExpiry != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_chooseDate != null) hc += _chooseDate.hashCode ();
    hc = (hc * 31) + (int)_underlyingStrike;
    hc *= 31;
    if (_underlyingExpiry != null) hc += _underlyingExpiry.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
