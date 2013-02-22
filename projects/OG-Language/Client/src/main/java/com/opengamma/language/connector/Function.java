// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.connector;
public abstract class Function extends com.opengamma.language.connector.UserMessagePayload implements java.io.Serializable {
          public <T1,T2> T1 accept (final UserMessagePayloadVisitor<T1,T2> visitor, final T2 data) throws com.opengamma.util.async.AsynchronousExecution { return visitor.visitFunction (this, data); }
          public <T1,T2> T1 accept (final com.opengamma.language.function.FunctionVisitor<T1,T2> visitor, final T2 data) throws com.opengamma.util.async.AsynchronousExecution { return visitor.visitUnexpected (this, data); }
  private static final long serialVersionUID = 1l;
  public Function () {
  }
  protected Function (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
  }
  protected Function (final Function source) {
    super (source);
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
  }
  public static Function fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.connector.Function".equals (className)) break;
      try {
        return (com.opengamma.language.connector.Function)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    throw new UnsupportedOperationException ("Function is an abstract message");
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Function)) return false;
    Function msg = (Function)o;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
