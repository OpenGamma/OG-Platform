// Automatically created - do not modify
// Created from com\opengamma\livedata\ResolveResponse.proto:9(10)
package com.opengamma.livedata;
public class ResolveResponse implements java.io.Serializable {
  private final com.opengamma.livedata.LiveDataSpecificationImpl _resolvedSpecification;
  public static final String RESOLVEDSPECIFICATION_KEY = "resolvedSpecification";
  public ResolveResponse (com.opengamma.livedata.LiveDataSpecificationImpl resolvedSpecification) {
    if (resolvedSpecification == null) throw new NullPointerException ("'resolvedSpecification' cannot be null");
    else {
      _resolvedSpecification = resolvedSpecification;
    }
  }
  protected ResolveResponse (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (RESOLVEDSPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ResolveResponse - field 'resolvedSpecification' is not present");
    try {
      _resolvedSpecification = fudgeContext.fudgeMsgToObject (com.opengamma.livedata.LiveDataSpecificationImpl.class, fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ResolveResponse - field 'resolvedSpecification' is not LiveDataSpecificationImpl message", e);
    }
  }
  protected ResolveResponse (final ResolveResponse source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._resolvedSpecification == null) _resolvedSpecification = null;
    else {
      _resolvedSpecification = source._resolvedSpecification;
    }
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    if (_resolvedSpecification != null)  {
      msg.add (RESOLVEDSPECIFICATION_KEY, null, fudgeContext.objectToFudgeMsg (_resolvedSpecification));
    }
  }
  public static ResolveResponse fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    return new ResolveResponse (fudgeContext, fudgeMsg);
  }
  public com.opengamma.livedata.LiveDataSpecificationImpl getResolvedSpecification () {
    return _resolvedSpecification;
  }
}
