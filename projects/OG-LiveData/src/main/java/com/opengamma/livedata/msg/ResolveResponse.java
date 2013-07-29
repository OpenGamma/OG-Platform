// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.livedata.msg;
public class ResolveResponse implements java.io.Serializable {
  private static final long serialVersionUID = -26273330199l;
  private com.opengamma.livedata.LiveDataSpecification _resolvedSpecification;
  public static final String RESOLVED_SPECIFICATION_KEY = "resolvedSpecification";
  public ResolveResponse (com.opengamma.livedata.LiveDataSpecification resolvedSpecification) {
    if (resolvedSpecification == null) throw new NullPointerException ("'resolvedSpecification' cannot be null");
    else {
      _resolvedSpecification = resolvedSpecification;
    }
  }
  protected ResolveResponse (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (RESOLVED_SPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ResolveResponse - field 'resolvedSpecification' is not present");
    try {
      _resolvedSpecification = deserializer.fieldValueToObject (com.opengamma.livedata.LiveDataSpecification.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ResolveResponse - field 'resolvedSpecification' is not LiveDataSpecification message", e);
    }
  }
  protected ResolveResponse (final ResolveResponse source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._resolvedSpecification == null) _resolvedSpecification = null;
    else {
      _resolvedSpecification = source._resolvedSpecification;
    }
  }
  public ResolveResponse clone () {
    return new ResolveResponse (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_resolvedSpecification != null)  {
      serializer.addToMessageWithClassHeaders (msg, RESOLVED_SPECIFICATION_KEY, null, _resolvedSpecification, com.opengamma.livedata.LiveDataSpecification.class);
    }
  }
  public static ResolveResponse fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.livedata.msg.ResolveResponse".equals (className)) break;
      try {
        return (com.opengamma.livedata.msg.ResolveResponse)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ResolveResponse (deserializer, fudgeMsg);
  }
  public com.opengamma.livedata.LiveDataSpecification getResolvedSpecification () {
    return _resolvedSpecification;
  }
  public void setResolvedSpecification (com.opengamma.livedata.LiveDataSpecification resolvedSpecification) {
    if (resolvedSpecification == null) throw new NullPointerException ("'resolvedSpecification' cannot be null");
    else {
      _resolvedSpecification = resolvedSpecification;
    }
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
