// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class FadeInPayoffStyle extends com.opengamma.financial.security.option.PayoffStyle implements java.io.Serializable {
  public <T> T accept (PayoffStyleVisitor<T> visitor) { return visitor.visitFadeInPayoffStyle(this); }
  private static final long serialVersionUID = 34423739810890l;
  private final double _lowerBound;
  public static final String LOWER_BOUND_KEY = "lowerBound";
  private final double _upperBound;
  public static final String UPPER_BOUND_KEY = "upperBound";
  public FadeInPayoffStyle (double lowerBound, double upperBound) {
    _lowerBound = lowerBound;
    _upperBound = upperBound;
  }
  protected FadeInPayoffStyle (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (LOWER_BOUND_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FadeInPayoffStyle - field 'lowerBound' is not present");
    try {
      _lowerBound = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FadeInPayoffStyle - field 'lowerBound' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (UPPER_BOUND_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FadeInPayoffStyle - field 'upperBound' is not present");
    try {
      _upperBound = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FadeInPayoffStyle - field 'upperBound' is not double", e);
    }
  }
  protected FadeInPayoffStyle (final FadeInPayoffStyle source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _lowerBound = source._lowerBound;
    _upperBound = source._upperBound;
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    msg.add (LOWER_BOUND_KEY, null, _lowerBound);
    msg.add (UPPER_BOUND_KEY, null, _upperBound);
  }
  public static FadeInPayoffStyle fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.FadeInPayoffStyle".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.FadeInPayoffStyle)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FadeInPayoffStyle (fudgeMsg);
  }
  public double getLowerBound () {
    return _lowerBound;
  }
  public double getUpperBound () {
    return _upperBound;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FadeInPayoffStyle)) return false;
    FadeInPayoffStyle msg = (FadeInPayoffStyle)o;
    if (_lowerBound != msg._lowerBound) return false;
    if (_upperBound != msg._upperBound) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc = (hc * 31) + (int)_lowerBound;
    hc = (hc * 31) + (int)_upperBound;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
