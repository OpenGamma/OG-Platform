// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.calcnode.msg;
public class ViewProcessorQueryMessage implements java.io.Serializable {
  private static final long serialVersionUID = -23711167584l;
  private long _correlationId;
  public static final String CORRELATION_ID_KEY = "correlationId";
  public ViewProcessorQueryMessage (long correlationId) {
    _correlationId = correlationId;
  }
  protected ViewProcessorQueryMessage (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (CORRELATION_ID_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ViewProcessorQueryMessage - field 'correlationId' is not present");
    try {
      _correlationId = fudgeMsg.getFieldValue (Long.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ViewProcessorQueryMessage - field 'correlationId' is not long", e);
    }
  }
  protected ViewProcessorQueryMessage (final ViewProcessorQueryMessage source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _correlationId = source._correlationId;
  }
  public ViewProcessorQueryMessage clone () {
    return new ViewProcessorQueryMessage (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    msg.add (CORRELATION_ID_KEY, null, _correlationId);
  }
  public static ViewProcessorQueryMessage fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.calcnode.msg.ViewProcessorQueryMessage".equals (className)) break;
      try {
        return (com.opengamma.engine.view.calcnode.msg.ViewProcessorQueryMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ViewProcessorQueryMessage (deserializer, fudgeMsg);
  }
  public long getCorrelationId () {
    return _correlationId;
  }
  public void setCorrelationId (long correlationId) {
    _correlationId = correlationId;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
