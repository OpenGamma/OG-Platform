// Automatically created - do not modify
// Created from com\opengamma\livedata\EntitlementResponse.proto:9(10)
package com.opengamma.livedata;
public class EntitlementResponse implements java.io.Serializable {
  private final boolean _isEntitled;
  public static final String ISENTITLED_KEY = "isEntitled";
  public EntitlementResponse (boolean isEntitled) {
    _isEntitled = isEntitled;
  }
  protected EntitlementResponse (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (ISENTITLED_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EntitlementResponse - field 'isEntitled' is not present");
    try {
      _isEntitled = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EntitlementResponse - field 'isEntitled' is not boolean", e);
    }
  }
  protected EntitlementResponse (final EntitlementResponse source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _isEntitled = source._isEntitled;
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    msg.add (ISENTITLED_KEY, null, _isEntitled);
  }
  public static EntitlementResponse fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    return new EntitlementResponse (fudgeMsg);
  }
  public boolean getIsEntitled () {
    return _isEntitled;
  }
}
