// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/livedata/msg/ResolveRequest.proto:9(10)
package com.opengamma.livedata.msg;
public class ResolveRequest implements java.io.Serializable {
  private static final long serialVersionUID = 39522492639l;
  private com.opengamma.livedata.LiveDataSpecification _requestedSpecification;
  public static final String REQUESTED_SPECIFICATION_KEY = "requestedSpecification";
  public ResolveRequest (com.opengamma.livedata.LiveDataSpecification requestedSpecification) {
    if (requestedSpecification == null) throw new NullPointerException ("'requestedSpecification' cannot be null");
    else {
      _requestedSpecification = requestedSpecification;
    }
  }
  protected ResolveRequest (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (REQUESTED_SPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ResolveRequest - field 'requestedSpecification' is not present");
    try {
      _requestedSpecification = fudgeContext.fieldValueToObject (com.opengamma.livedata.LiveDataSpecification.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ResolveRequest - field 'requestedSpecification' is not LiveDataSpecification message", e);
    }
  }
  protected ResolveRequest (final ResolveRequest source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._requestedSpecification == null) _requestedSpecification = null;
    else {
      _requestedSpecification = source._requestedSpecification;
    }
  }
  public ResolveRequest clone () {
    return new ResolveRequest (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_requestedSpecification != null)  {
      fudgeContext.objectToFudgeMsgWithClassHeaders (msg, REQUESTED_SPECIFICATION_KEY, null, _requestedSpecification, com.opengamma.livedata.LiveDataSpecification.class);
    }
  }
  public static ResolveRequest fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.livedata.msg.ResolveRequest".equals (className)) break;
      try {
        return (com.opengamma.livedata.msg.ResolveRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ResolveRequest (fudgeContext, fudgeMsg);
  }
  public com.opengamma.livedata.LiveDataSpecification getRequestedSpecification () {
    return _requestedSpecification;
  }
  public void setRequestedSpecification (com.opengamma.livedata.LiveDataSpecification requestedSpecification) {
    if (requestedSpecification == null) throw new NullPointerException ("'requestedSpecification' cannot be null");
    else {
      _requestedSpecification = requestedSpecification;
    }
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
