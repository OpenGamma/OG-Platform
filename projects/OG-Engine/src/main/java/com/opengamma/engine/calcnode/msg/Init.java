// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.calcnode.msg;
public class Init extends com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage implements java.io.Serializable {
  public void accept (RemoteCalcNodeMessageVisitor visitor) { visitor.visitInitMessage (this); }
  private static final long serialVersionUID = -43293390246l;
  private long _functionInitId;
  public static final String FUNCTION_INIT_ID_KEY = "functionInitId";
  public Init (long functionInitId) {
    _functionInitId = functionInitId;
  }
  protected Init (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (FUNCTION_INIT_ID_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Init - field 'functionInitId' is not present");
    try {
      _functionInitId = fudgeMsg.getFieldValue (Long.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Init - field 'functionInitId' is not long", e);
    }
  }
  protected Init (final Init source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _functionInitId = source._functionInitId;
  }
  public Init clone () {
    return new Init (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    msg.add (FUNCTION_INIT_ID_KEY, null, _functionInitId);
  }
  public static Init fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.calcnode.msg.Init".equals (className)) break;
      try {
        return (com.opengamma.engine.calcnode.msg.Init)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Init (deserializer, fudgeMsg);
  }
  public long getFunctionInitId () {
    return _functionInitId;
  }
  public void setFunctionInitId (long functionInitId) {
    _functionInitId = functionInitId;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
