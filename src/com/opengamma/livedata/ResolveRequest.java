// Automatically created - do not modify
// Created from com\opengamma\livedata\ResolveRequest.proto:9(10)
package com.opengamma.livedata;
public class ResolveRequest implements java.io.Serializable {
  private final com.opengamma.livedata.LiveDataSpecificationImpl _requestedSpecification;
  public static final String REQUESTEDSPECIFICATION_KEY = "requestedSpecification";
  public ResolveRequest (com.opengamma.livedata.LiveDataSpecificationImpl requestedSpecification) {
    if (requestedSpecification == null) throw new NullPointerException ("'requestedSpecification' cannot be null");
    else {
      _requestedSpecification = requestedSpecification;
    }
  }
  protected ResolveRequest (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (REQUESTEDSPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ResolveRequest - field 'requestedSpecification' is not present");
    try {
      _requestedSpecification = fudgeContext.fudgeMsgToObject (com.opengamma.livedata.LiveDataSpecificationImpl.class, fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ResolveRequest - field 'requestedSpecification' is not LiveDataSpecificationImpl message", e);
    }
  }
  protected ResolveRequest (final ResolveRequest source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._requestedSpecification == null) _requestedSpecification = null;
    else {
      _requestedSpecification = source._requestedSpecification;
    }
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    if (_requestedSpecification != null)  {
      msg.add (REQUESTEDSPECIFICATION_KEY, null, fudgeContext.objectToFudgeMsg (_requestedSpecification));
    }
  }
  public static ResolveRequest fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    return new ResolveRequest (fudgeContext, fudgeMsg);
  }
  public com.opengamma.livedata.LiveDataSpecificationImpl getRequestedSpecification () {
    return _requestedSpecification;
  }
}
