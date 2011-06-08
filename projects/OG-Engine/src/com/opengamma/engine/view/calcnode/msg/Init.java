// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.calcnode.msg;
public class Init extends com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage implements java.io.Serializable {
  public void accept (RemoteCalcNodeMessageVisitor visitor) { visitor.visitInitMessage (this); }
  private static final long serialVersionUID = -43293390246l;
  private long _functionInitId;
  public static final String FUNCTION_INIT_ID_KEY = "functionInitId";
  public Init (long functionInitId) {
    _functionInitId = functionInitId;
  }
  protected Init (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
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
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    msg.add (FUNCTION_INIT_ID_KEY, null, _functionInitId);
  }
  public static Init fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.calcnode.msg.Init".equals (className)) break;
      try {
        return (com.opengamma.engine.view.calcnode.msg.Init)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Init (fudgeContext, fudgeMsg);
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
///CLOVER:ON
// CSON: Generated File
