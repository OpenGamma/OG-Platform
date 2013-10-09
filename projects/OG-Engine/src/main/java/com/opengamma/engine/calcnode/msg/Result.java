// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.calcnode.msg;
public class Result extends com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage implements java.io.Serializable {
  public void accept (RemoteCalcNodeMessageVisitor visitor) { visitor.visitResultMessage (this); }
  private static final long serialVersionUID = -27076885070712l;
  private com.opengamma.engine.calcnode.CalculationJobResult _result;
  public static final String RESULT_KEY = "result";
  private com.opengamma.engine.calcnode.msg.Ready _ready;
  public static final String READY_KEY = "ready";
  public Result (com.opengamma.engine.calcnode.CalculationJobResult result) {
    if (result == null) throw new NullPointerException ("'result' cannot be null");
    else {
      _result = result;
    }
  }
  protected Result (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (RESULT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Result - field 'result' is not present");
    try {
      _result = deserializer.fieldValueToObject (com.opengamma.engine.calcnode.CalculationJobResult.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Result - field 'result' is not CalculationJobResult message", e);
    }
    fudgeField = fudgeMsg.getByName (READY_KEY);
    if (fudgeField != null)  {
      try {
        final com.opengamma.engine.calcnode.msg.Ready fudge1;
        fudge1 = com.opengamma.engine.calcnode.msg.Ready.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
        setReady (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Result - field 'ready' is not Ready message", e);
      }
    }
  }
  public Result (com.opengamma.engine.calcnode.CalculationJobResult result, com.opengamma.engine.calcnode.msg.Ready ready) {
    if (result == null) throw new NullPointerException ("'result' cannot be null");
    else {
      _result = result;
    }
    if (ready == null) _ready = null;
    else {
      _ready = (com.opengamma.engine.calcnode.msg.Ready)ready.clone ();
    }
  }
  protected Result (final Result source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._result == null) _result = null;
    else {
      _result = source._result;
    }
    if (source._ready == null) _ready = null;
    else {
      _ready = (com.opengamma.engine.calcnode.msg.Ready)source._ready.clone ();
    }
  }
  public Result clone () {
    return new Result (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_result != null)  {
      serializer.addToMessageWithClassHeaders (msg, RESULT_KEY, null, _result, com.opengamma.engine.calcnode.CalculationJobResult.class);
    }
    if (_ready != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _ready.getClass (), com.opengamma.engine.calcnode.msg.Ready.class);
      _ready.toFudgeMsg (serializer, fudge1);
      msg.add (READY_KEY, null, fudge1);
    }
  }
  public static Result fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.calcnode.msg.Result".equals (className)) break;
      try {
        return (com.opengamma.engine.calcnode.msg.Result)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Result (deserializer, fudgeMsg);
  }
  public com.opengamma.engine.calcnode.CalculationJobResult getResult () {
    return _result;
  }
  public void setResult (com.opengamma.engine.calcnode.CalculationJobResult result) {
    if (result == null) throw new NullPointerException ("'result' cannot be null");
    else {
      _result = result;
    }
  }
  public com.opengamma.engine.calcnode.msg.Ready getReady () {
    return _ready;
  }
  public void setReady (com.opengamma.engine.calcnode.msg.Ready ready) {
    if (ready == null) _ready = null;
    else {
      _ready = (com.opengamma.engine.calcnode.msg.Ready)ready.clone ();
    }
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
