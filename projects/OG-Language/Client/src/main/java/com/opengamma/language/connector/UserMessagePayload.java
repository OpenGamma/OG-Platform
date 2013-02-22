// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.connector;
public class UserMessagePayload implements java.io.Serializable {
          public static final UserMessagePayload EMPTY_PAYLOAD = new UserMessagePayload ();
          
          public <T1,T2> T1 accept (final UserMessagePayloadVisitor<T1,T2> visitor, final T2 data) throws com.opengamma.util.async.AsynchronousExecution {
            return visitor.visitUserMessagePayload (this, data);
          }
  private static final long serialVersionUID = 1l;
  public UserMessagePayload () {
  }
  protected UserMessagePayload (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
  }
  protected UserMessagePayload (final UserMessagePayload source) {
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
  }
  public static UserMessagePayload fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.connector.UserMessagePayload".equals (className)) break;
      try {
        return (com.opengamma.language.connector.UserMessagePayload)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new UserMessagePayload (deserializer, fudgeMsg);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof UserMessagePayload)) return false;
    UserMessagePayload msg = (UserMessagePayload)o;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
