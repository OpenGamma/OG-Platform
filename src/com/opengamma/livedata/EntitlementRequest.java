// Automatically created - do not modify
// Created from com\opengamma\livedata\EntitlementRequest.proto:9(10)
package com.opengamma.livedata;
public class EntitlementRequest implements java.io.Serializable {
  private final String _userName;
  public static final String USERNAME_KEY = "userName";
  private final com.opengamma.livedata.LiveDataSpecificationImpl _liveDataSpecification;
  public static final String LIVEDATASPECIFICATION_KEY = "liveDataSpecification";
  public EntitlementRequest (String userName, com.opengamma.livedata.LiveDataSpecificationImpl liveDataSpecification) {
    if (userName == null) throw new NullPointerException ("userName' cannot be null");
    _userName = userName;
    if (liveDataSpecification == null) throw new NullPointerException ("'liveDataSpecification' cannot be null");
    else {
      _liveDataSpecification = liveDataSpecification;
    }
  }
  protected EntitlementRequest (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (USERNAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EntitlementRequest - field 'userName' is not present");
    try {
      _userName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EntitlementRequest - field 'userName' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (LIVEDATASPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EntitlementRequest - field 'liveDataSpecification' is not present");
    try {
      _liveDataSpecification = fudgeContext.fudgeMsgToObject (com.opengamma.livedata.LiveDataSpecificationImpl.class, fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EntitlementRequest - field 'liveDataSpecification' is not LiveDataSpecificationImpl message", e);
    }
  }
  protected EntitlementRequest (final EntitlementRequest source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _userName = source._userName;
    if (source._liveDataSpecification == null) _liveDataSpecification = null;
    else {
      _liveDataSpecification = source._liveDataSpecification;
    }
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    if (_userName != null)  {
      msg.add (USERNAME_KEY, null, _userName);
    }
    if (_liveDataSpecification != null)  {
      msg.add (LIVEDATASPECIFICATION_KEY, null, fudgeContext.objectToFudgeMsg (_liveDataSpecification));
    }
  }
  public static EntitlementRequest fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    return new EntitlementRequest (fudgeContext, fudgeMsg);
  }
  public String getUserName () {
    return _userName;
  }
  public com.opengamma.livedata.LiveDataSpecificationImpl getLiveDataSpecification () {
    return _liveDataSpecification;
  }
}
