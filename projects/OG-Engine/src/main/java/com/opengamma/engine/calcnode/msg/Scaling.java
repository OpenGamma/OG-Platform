// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.calcnode.msg;
public class Scaling extends com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage implements java.io.Serializable {
  public void accept (RemoteCalcNodeMessageVisitor visitor) { visitor.visitScalingMessage (this); }
  private static final long serialVersionUID = 52573772034l;
  private double _invocation;
  public static final String INVOCATION_KEY = "invocation";
  public Scaling (double invocation) {
    _invocation = invocation;
  }
  protected Scaling (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (INVOCATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Scaling - field 'invocation' is not present");
    try {
      _invocation = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Scaling - field 'invocation' is not double", e);
    }
  }
  protected Scaling (final Scaling source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _invocation = source._invocation;
  }
  public Scaling clone () {
    return new Scaling (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    msg.add (INVOCATION_KEY, null, _invocation);
  }
  public static Scaling fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.calcnode.msg.Scaling".equals (className)) break;
      try {
        return (com.opengamma.engine.calcnode.msg.Scaling)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Scaling (deserializer, fudgeMsg);
  }
  public double getInvocation () {
    return _invocation;
  }
  public void setInvocation (double invocation) {
    _invocation = invocation;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
