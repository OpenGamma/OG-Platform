// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.config;
public class ViewCalculationRate extends com.opengamma.language.config.ConfigurationItem implements java.io.Serializable {
          @Override 
          public <T> T accept (final ConfigurationItemVisitor<T> visitor) { return visitor.visitViewCalculationRate (this); }
  private static final long serialVersionUID = -1430661887897626589l;
  private long _minDelta;
  public static final String MIN_DELTA_KEY = "minDelta";
  private long _maxDelta;
  public static final String MAX_DELTA_KEY = "maxDelta";
  private long _minFull;
  public static final String MIN_FULL_KEY = "minFull";
  private long _maxFull;
  public static final String MAX_FULL_KEY = "maxFull";
  public ViewCalculationRate (long minDelta, long maxDelta, long minFull, long maxFull) {
    _minDelta = minDelta;
    _maxDelta = maxDelta;
    _minFull = minFull;
    _maxFull = maxFull;
  }
  protected ViewCalculationRate (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (MIN_DELTA_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ViewCalculationRate - field 'minDelta' is not present");
    try {
      _minDelta = fudgeMsg.getFieldValue (Long.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ViewCalculationRate - field 'minDelta' is not long", e);
    }
    fudgeField = fudgeMsg.getByName (MAX_DELTA_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ViewCalculationRate - field 'maxDelta' is not present");
    try {
      _maxDelta = fudgeMsg.getFieldValue (Long.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ViewCalculationRate - field 'maxDelta' is not long", e);
    }
    fudgeField = fudgeMsg.getByName (MIN_FULL_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ViewCalculationRate - field 'minFull' is not present");
    try {
      _minFull = fudgeMsg.getFieldValue (Long.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ViewCalculationRate - field 'minFull' is not long", e);
    }
    fudgeField = fudgeMsg.getByName (MAX_FULL_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ViewCalculationRate - field 'maxFull' is not present");
    try {
      _maxFull = fudgeMsg.getFieldValue (Long.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ViewCalculationRate - field 'maxFull' is not long", e);
    }
  }
  protected ViewCalculationRate (final ViewCalculationRate source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _minDelta = source._minDelta;
    _maxDelta = source._maxDelta;
    _minFull = source._minFull;
    _maxFull = source._maxFull;
  }
  public ViewCalculationRate clone () {
    return new ViewCalculationRate (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    msg.add (MIN_DELTA_KEY, null, _minDelta);
    msg.add (MAX_DELTA_KEY, null, _maxDelta);
    msg.add (MIN_FULL_KEY, null, _minFull);
    msg.add (MAX_FULL_KEY, null, _maxFull);
  }
  public static ViewCalculationRate fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.config.ViewCalculationRate".equals (className)) break;
      try {
        return (com.opengamma.language.config.ViewCalculationRate)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ViewCalculationRate (deserializer, fudgeMsg);
  }
  public long getMinDelta () {
    return _minDelta;
  }
  public void setMinDelta (long minDelta) {
    _minDelta = minDelta;
  }
  public long getMaxDelta () {
    return _maxDelta;
  }
  public void setMaxDelta (long maxDelta) {
    _maxDelta = maxDelta;
  }
  public long getMinFull () {
    return _minFull;
  }
  public void setMinFull (long minFull) {
    _minFull = minFull;
  }
  public long getMaxFull () {
    return _maxFull;
  }
  public void setMaxFull (long maxFull) {
    _maxFull = maxFull;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof ViewCalculationRate)) return false;
    ViewCalculationRate msg = (ViewCalculationRate)o;
    if (_minDelta != msg._minDelta) return false;
    if (_maxDelta != msg._maxDelta) return false;
    if (_minFull != msg._minFull) return false;
    if (_maxFull != msg._maxFull) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc = (hc * 31) + (int)_minDelta;
    hc = (hc * 31) + (int)_maxDelta;
    hc = (hc * 31) + (int)_minFull;
    hc = (hc * 31) + (int)_maxFull;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
