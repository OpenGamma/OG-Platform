// automatically created - Mon Feb 01 16:32:20 GMT 2010
// created from /home/andrew/OpenGamma/OG-Build/projects/OG-LiveData/src/com/opengamma/livedata/LiveDataSubscriptionResponse.proto:36(10)
package com.opengamma.livedata;
public class LiveDataSubscriptionResponse implements Cloneable {
  private final String _requestingUserName;
  public static final String REQUESTINGUSERNAME_KEY = "requestingUserName";
  private final com.opengamma.livedata.LiveDataSpecification _requestedSpecification;
  public static final String REQUESTEDSPECIFICATION_KEY = "requestedSpecification";
  private final com.opengamma.livedata.LiveDataSpecification _fullyQualifiedSpecification;
  public static final String FULLYQUALIFIEDSPECIFICATION_KEY = "fullyQualifiedSpecification";
  private final com.opengamma.livedata.LiveDataSubscriptionResult _subscriptionResult;
  public static final String SUBSCRIPTIONRESULT_KEY = "subscriptionResult";
  private final String _userMessage;
  public static final String USERMESSAGE_KEY = "userMessage";
  public LiveDataSubscriptionResponse (String requestingUserName, com.opengamma.livedata.LiveDataSpecification requestedSpecification, com.opengamma.livedata.LiveDataSpecification fullyQualifiedSpecification, com.opengamma.livedata.LiveDataSubscriptionResult subscriptionResult, String userMessage) {
    _requestingUserName = requestingUserName;
    if (requestedSpecification == null) throw new NullPointerException ("'requestedSpecification' cannot be null");
    else {
      _requestedSpecification = requestedSpecification;
    }
    if (fullyQualifiedSpecification == null) throw new NullPointerException ("'fullyQualifiedSpecification' cannot be null");
    else {
      _fullyQualifiedSpecification = fullyQualifiedSpecification;
    }
    _subscriptionResult = subscriptionResult;
    _userMessage = userMessage;
  }
  protected LiveDataSubscriptionResponse (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    Object fudge0;
    fudgeField = fudgeMsg.getByName (REQUESTINGUSERNAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'requestingUserName' is not present");
    _requestingUserName = fudgeField.getValue ().toString ();
    fudgeField = fudgeMsg.getByName (REQUESTEDSPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'requestedSpecification' is not present");
    fudge0 = fudgeField.getValue ();
    if (!(fudge0 instanceof org.fudgemsg.FudgeFieldContainer)) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'requestedSpecification' is not LiveDataSpecification message");
    try {
      _requestedSpecification = fudgeContext.fudgeMsgToObject (com.opengamma.livedata.LiveDataSpecification.class, (org.fudgemsg.FudgeFieldContainer)fudge0);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'requestedSpecification' is not LiveDataSpecification message", e);
    }
    fudgeField = fudgeMsg.getByName (FULLYQUALIFIEDSPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'fullyQualifiedSpecification' is not present");
    fudge0 = fudgeField.getValue ();
    if (!(fudge0 instanceof org.fudgemsg.FudgeFieldContainer)) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'fullyQualifiedSpecification' is not LiveDataSpecification message");
    try {
      _fullyQualifiedSpecification = fudgeContext.fudgeMsgToObject (com.opengamma.livedata.LiveDataSpecification.class, (org.fudgemsg.FudgeFieldContainer)fudge0);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'fullyQualifiedSpecification' is not LiveDataSpecification message", e);
    }
    fudgeField = fudgeMsg.getByName (SUBSCRIPTIONRESULT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'subscriptionResult' is not present");
    final int fudge1;
    fudge0 = fudgeField.getValue ();
    if (fudge0 instanceof Integer) fudge1 = (Integer)fudge0;
    else if (fudge0 instanceof Short) fudge1 = (Short)fudge0;
    else if (fudge0 instanceof Byte) fudge1 = (Byte)fudge0;
    else throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'subscriptionResult' is not LiveDataSubscriptionResult");
    try {
      _subscriptionResult = com.opengamma.livedata.LiveDataSubscriptionResult.fromFudgeEncoding (fudge1);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'subscriptionResult' is not LiveDataSubscriptionResult", e);
    }
    fudgeField = fudgeMsg.getByName (USERMESSAGE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'userMessage' is not present");
    _userMessage = fudgeField.getValue ().toString ();
  }
  protected LiveDataSubscriptionResponse (final LiveDataSubscriptionResponse source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _requestingUserName = source._requestingUserName;
    if (source._requestedSpecification == null) _requestedSpecification = null;
    else {
      _requestedSpecification = source._requestedSpecification;
    }
    if (source._fullyQualifiedSpecification == null) _fullyQualifiedSpecification = null;
    else {
      _fullyQualifiedSpecification = source._fullyQualifiedSpecification;
    }
    _subscriptionResult = source._subscriptionResult;
    _userMessage = source._userMessage;
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
    if (_requestedSpecification != null)  {
      msg.add (REQUESTEDSPECIFICATION_KEY, null, fudgeContext.objectToFudgeMsg (_requestedSpecification));
    }
    if (_fullyQualifiedSpecification != null)  {
      msg.add (FULLYQUALIFIEDSPECIFICATION_KEY, null, fudgeContext.objectToFudgeMsg (_fullyQualifiedSpecification));
    }
    if (_subscriptionResult != null)  {
      msg.add (SUBSCRIPTIONRESULT_KEY, null, _subscriptionResult.getFudgeEncoding ());
    }
    if (_userMessage != null)  {
      msg.add (USERMESSAGE_KEY, null, _userMessage);
    }
  }
  public static LiveDataSubscriptionResponse fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    return new LiveDataSubscriptionResponse (fudgeContext, fudgeMsg);
  }
  public String getRequestingUserName () {
    return _requestingUserName;
  }
  public com.opengamma.livedata.LiveDataSpecification getRequestedSpecification () {
    return _requestedSpecification;
  }
  public com.opengamma.livedata.LiveDataSpecification getFullyQualifiedSpecification () {
    return _fullyQualifiedSpecification;
  }
  public com.opengamma.livedata.LiveDataSubscriptionResult getSubscriptionResult () {
    return _subscriptionResult;
  }
  public String getUserMessage () {
    return _userMessage;
  }
  public boolean equals (final Object o) {
    if (o == null) return false;
    if (!(o instanceof LiveDataSubscriptionResponse)) return false;
    final LiveDataSubscriptionResponse msg = (LiveDataSubscriptionResponse)o;
    if (_requestingUserName != null) if (msg._requestingUserName != null)  {
      if (!_requestingUserName.equals (msg._requestingUserName)) return false;
    }
    else return false;
    else if (msg._requestingUserName != null) return false;
    if (_requestedSpecification != null) if (msg._requestedSpecification != null)  {
      if (!_requestedSpecification.equals (msg._requestedSpecification)) return false;
    }
    else return false;
    else if (msg._requestedSpecification != null) return false;
    if (_fullyQualifiedSpecification != null) if (msg._fullyQualifiedSpecification != null)  {
      if (!_fullyQualifiedSpecification.equals (msg._fullyQualifiedSpecification)) return false;
    }
    else return false;
    else if (msg._fullyQualifiedSpecification != null) return false;
    if (_subscriptionResult != null) if (msg._subscriptionResult != null)  {
      if (!_subscriptionResult.equals (msg._subscriptionResult)) return false;
    }
    else return false;
    else if (msg._subscriptionResult != null) return false;
    if (_userMessage != null) if (msg._userMessage != null)  {
      if (!_userMessage.equals (msg._userMessage)) return false;
    }
    else return false;
    else if (msg._userMessage != null) return false;
    return true;
  }
  public int hashCode () {
    int hc;
    hc = 1;
    hc = hc * 31;
    if (_requestingUserName != null) hc = hc + _requestingUserName.hashCode ();
    hc = hc * 31;
    if (_requestedSpecification != null) hc = hc + _requestedSpecification.hashCode ();
    hc = hc * 31;
    if (_fullyQualifiedSpecification != null) hc = hc + _fullyQualifiedSpecification.hashCode ();
    hc = hc * 31;
    if (_subscriptionResult != null) hc = hc + _subscriptionResult.hashCode ();
    hc = hc * 31;
    if (_userMessage != null) hc = hc + _userMessage.hashCode ();
    return hc;
  }
  public LiveDataSubscriptionResponse clone () {
    return new LiveDataSubscriptionResponse (this);
  }
}
