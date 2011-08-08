// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class SupersharePayoffStyle extends com.opengamma.financial.security.option.PayoffStyle implements java.io.Serializable {
  public <T> T accept (PayoffStyleVisitor<T> visitor) { return visitor.visitSupersharePayoffStyle(this); }
  private static final long serialVersionUID = 34423739810890l;
  private final double _lowerBound;
  public static final String LOWER_BOUND_KEY = "lowerBound";
  private final double _upperBound;
  public static final String UPPER_BOUND_KEY = "upperBound";
  public SupersharePayoffStyle (double lowerBound, double upperBound) {
    _lowerBound = lowerBound;
    _upperBound = upperBound;
  }
  protected SupersharePayoffStyle (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (LOWER_BOUND_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SupersharePayoffStyle - field 'lowerBound' is not present");
    try {
      _lowerBound = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SupersharePayoffStyle - field 'lowerBound' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (UPPER_BOUND_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SupersharePayoffStyle - field 'upperBound' is not present");
    try {
      _upperBound = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SupersharePayoffStyle - field 'upperBound' is not double", e);
    }
  }
  protected SupersharePayoffStyle (final SupersharePayoffStyle source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _lowerBound = source._lowerBound;
    _upperBound = source._upperBound;
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    msg.add (LOWER_BOUND_KEY, null, _lowerBound);
    msg.add (UPPER_BOUND_KEY, null, _upperBound);
  }
  public static SupersharePayoffStyle fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.SupersharePayoffStyle".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.SupersharePayoffStyle)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new SupersharePayoffStyle (deserializer, fudgeMsg);
  }
  public double getLowerBound () {
    return _lowerBound;
  }
  public double getUpperBound () {
    return _upperBound;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof SupersharePayoffStyle)) return false;
    SupersharePayoffStyle msg = (SupersharePayoffStyle)o;
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
