// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/engine/view/calcnode/RemoteCalcNodeInitMessage.proto:13(12)
package com.opengamma.engine.view.calcnode;
public class RemoteCalcNodeInitMessage extends com.opengamma.engine.view.calcnode.RemoteCalcNodeMessage implements java.io.Serializable {
  private static final long serialVersionUID = -6340712368l;
  private com.opengamma.engine.function.FunctionRepository _functions;
  public static final String FUNCTIONS_KEY = "functions";
  public RemoteCalcNodeInitMessage (com.opengamma.engine.function.FunctionRepository functions) {
    if (functions == null) throw new NullPointerException ("'functions' cannot be null");
    else {
      _functions = functions;
    }
  }
  protected RemoteCalcNodeInitMessage (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (FUNCTIONS_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a RemoteCalcNodeInitMessage - field 'functions' is not present");
    try {
      _functions = fudgeContext.fieldValueToObject (com.opengamma.engine.function.FunctionRepository.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a RemoteCalcNodeInitMessage - field 'functions' is not FunctionRepository message", e);
    }
  }
  protected RemoteCalcNodeInitMessage (final RemoteCalcNodeInitMessage source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._functions == null) _functions = null;
    else {
      _functions = source._functions;
    }
  }
  public RemoteCalcNodeInitMessage clone () {
    return new RemoteCalcNodeInitMessage (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_functions != null)  {
      fudgeContext.objectToFudgeMsgWithClassHeaders (msg, FUNCTIONS_KEY, null, _functions, com.opengamma.engine.function.FunctionRepository.class);
    }
  }
  public static RemoteCalcNodeInitMessage fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.calcnode.RemoteCalcNodeInitMessage".equals (className)) break;
      try {
        return (com.opengamma.engine.view.calcnode.RemoteCalcNodeInitMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new RemoteCalcNodeInitMessage (fudgeContext, fudgeMsg);
  }
  public com.opengamma.engine.function.FunctionRepository getFunctions () {
    return _functions;
  }
  public void setFunctions (com.opengamma.engine.function.FunctionRepository functions) {
    if (functions == null) throw new NullPointerException ("'functions' cannot be null");
    else {
      _functions = functions;
    }
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
