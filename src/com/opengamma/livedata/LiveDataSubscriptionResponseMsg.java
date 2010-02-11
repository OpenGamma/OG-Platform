// Automatically created - do not modify
// Created from com\opengamma\livedata\LiveDataSubscriptionResponse.proto:40(10)
package com.opengamma.livedata;
public class LiveDataSubscriptionResponseMsg implements java.io.Serializable {
  private final String _requestingUserName;
  public static final String REQUESTINGUSERNAME_KEY = "requestingUserName";
  private final java.util.List<com.opengamma.livedata.LiveDataSubscriptionResponse> _responses;
  public static final String RESPONSES_KEY = "responses";
  public LiveDataSubscriptionResponseMsg (String requestingUserName, java.util.Collection<? extends com.opengamma.livedata.LiveDataSubscriptionResponse> responses) {
    if (requestingUserName == null) throw new NullPointerException ("requestingUserName' cannot be null");
    _requestingUserName = requestingUserName;
    if (responses == null) throw new NullPointerException ("'responses' cannot be null");
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSubscriptionResponse> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSubscriptionResponse> (responses);
      if (responses.size () == 0) throw new IllegalArgumentException ("'responses' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSubscriptionResponse> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSubscriptionResponse fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'responses' cannot be null");
        fudge1.set (fudge2);
      }
      _responses = fudge0;
    }
  }
  protected LiveDataSubscriptionResponseMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (REQUESTINGUSERNAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponseMsg - field 'requestingUserName' is not present");
    try {
      _requestingUserName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponseMsg - field 'requestingUserName' is not string", e);
    }
    fudgeFields = fudgeMsg.getAllByName (RESPONSES_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponseMsg - field 'responses' is not present");
    _responses = new java.util.ArrayList<com.opengamma.livedata.LiveDataSubscriptionResponse> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.livedata.LiveDataSubscriptionResponse fudge2;
        fudge2 = com.opengamma.livedata.LiveDataSubscriptionResponse.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudge1));
        _responses.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponseMsg - field 'responses' is not LiveDataSubscriptionResponse message", e);
      }
    }
  }
  protected LiveDataSubscriptionResponseMsg (final LiveDataSubscriptionResponseMsg source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _requestingUserName = source._requestingUserName;
    if (source._responses == null) _responses = null;
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSubscriptionResponse> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSubscriptionResponse> (source._responses);
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSubscriptionResponse> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSubscriptionResponse fudge2 = fudge1.next ();
        fudge1.set (fudge2);
      }
      _responses = fudge0;
    }
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    if (_requestingUserName != null)  {
      msg.add (REQUESTINGUSERNAME_KEY, null, _requestingUserName);
    }
    if (_responses != null)  {
      for (com.opengamma.livedata.LiveDataSubscriptionResponse fudge1 : _responses) {
        msg.add (RESPONSES_KEY, null, fudgeContext.objectToFudgeMsg (fudge1));
      }
    }
  }
  public static LiveDataSubscriptionResponseMsg fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    return new LiveDataSubscriptionResponseMsg (fudgeContext, fudgeMsg);
  }
  public String getRequestingUserName () {
    return _requestingUserName;
  }
  public com.opengamma.livedata.LiveDataSubscriptionResponse getResponses () {
    return getResponses (0);
  }
  public int getResponsesCount () {
    return (_responses != null) ? _responses.size () : 0;
  }
  public com.opengamma.livedata.LiveDataSubscriptionResponse getResponses (final int n) {
    if (_responses == null)  {
      if (n == 0) return null;
      throw new IndexOutOfBoundsException ("n=" + n);
    }
    return _responses.get (n);
  }
  public java.util.List<com.opengamma.livedata.LiveDataSubscriptionResponse> getResponsesList () {
    return java.util.Collections.unmodifiableList (_responses);
  }
}
