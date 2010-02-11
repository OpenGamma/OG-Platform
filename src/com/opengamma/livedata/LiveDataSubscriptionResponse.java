// Automatically created - do not modify
// Created from com\opengamma\livedata\LiveDataSubscriptionResponse.proto:45(10)
package com.opengamma.livedata;
public class LiveDataSubscriptionResponse implements java.io.Serializable {
  private final com.opengamma.livedata.LiveDataSpecificationImpl _requestedSpecification;
  public static final String REQUESTEDSPECIFICATION_KEY = "requestedSpecification";
  private final com.opengamma.livedata.LiveDataSpecificationImpl _fullyQualifiedSpecification;
  public static final String FULLYQUALIFIEDSPECIFICATION_KEY = "fullyQualifiedSpecification";
  private final com.opengamma.livedata.LiveDataSubscriptionResult _subscriptionResult;
  public static final String SUBSCRIPTIONRESULT_KEY = "subscriptionResult";
  private final String _userMessage;
  public static final String USERMESSAGE_KEY = "userMessage";
  private final String _tickDistributionSpecification;
  public static final String TICKDISTRIBUTIONSPECIFICATION_KEY = "tickDistributionSpecification";
  public static class Builder {
    private final com.opengamma.livedata.LiveDataSpecificationImpl _requestedSpecification;
    private com.opengamma.livedata.LiveDataSpecificationImpl _fullyQualifiedSpecification;
    private final com.opengamma.livedata.LiveDataSubscriptionResult _subscriptionResult;
    private String _userMessage;
    private String _tickDistributionSpecification;
    public Builder (com.opengamma.livedata.LiveDataSpecificationImpl requestedSpecification, com.opengamma.livedata.LiveDataSubscriptionResult subscriptionResult) {
      if (requestedSpecification == null) throw new NullPointerException ("'requestedSpecification' cannot be null");
      else {
        _requestedSpecification = requestedSpecification;
      }
      if (subscriptionResult == null) throw new NullPointerException ("subscriptionResult' cannot be null");
      _subscriptionResult = subscriptionResult;
    }
    protected Builder (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
      org.fudgemsg.FudgeField fudgeField;
      fudgeField = fudgeMsg.getByName (REQUESTEDSPECIFICATION_KEY);
      if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'requestedSpecification' is not present");
      try {
        _requestedSpecification = fudgeContext.fudgeMsgToObject (com.opengamma.livedata.LiveDataSpecificationImpl.class, fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'requestedSpecification' is not LiveDataSpecificationImpl message", e);
      }
      fudgeField = fudgeMsg.getByName (SUBSCRIPTIONRESULT_KEY);
      if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'subscriptionResult' is not present");
      try {
        _subscriptionResult = com.opengamma.livedata.LiveDataSubscriptionResult.fromFudgeEncoding (fudgeMsg.getFieldValue (Integer.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'subscriptionResult' is not LiveDataSubscriptionResult enum", e);
      }
      fudgeField = fudgeMsg.getByName (FULLYQUALIFIEDSPECIFICATION_KEY);
      if (fudgeField != null)  {
        try {
          final com.opengamma.livedata.LiveDataSpecificationImpl fudge1;
          fudge1 = fudgeContext.fudgeMsgToObject (com.opengamma.livedata.LiveDataSpecificationImpl.class, fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
          fullyQualifiedSpecification (fudge1);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'fullyQualifiedSpecification' is not LiveDataSpecificationImpl message", e);
        }
      }
      fudgeField = fudgeMsg.getByName (USERMESSAGE_KEY);
      if (fudgeField != null)  {
        try {
          userMessage (fudgeField.getValue ().toString ());
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'userMessage' is not string", e);
        }
      }
      fudgeField = fudgeMsg.getByName (TICKDISTRIBUTIONSPECIFICATION_KEY);
      if (fudgeField != null)  {
        try {
          tickDistributionSpecification (fudgeField.getValue ().toString ());
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'tickDistributionSpecification' is not string", e);
        }
      }
    }
    public Builder fullyQualifiedSpecification (com.opengamma.livedata.LiveDataSpecificationImpl fullyQualifiedSpecification) {
      if (fullyQualifiedSpecification == null) _fullyQualifiedSpecification = null;
      else {
        _fullyQualifiedSpecification = fullyQualifiedSpecification;
      }
      return this;
    }
    public Builder userMessage (String userMessage) {
      _userMessage = userMessage;
      return this;
    }
    public Builder tickDistributionSpecification (String tickDistributionSpecification) {
      _tickDistributionSpecification = tickDistributionSpecification;
      return this;
    }
    public LiveDataSubscriptionResponse build () {
      return new LiveDataSubscriptionResponse (this);
    }
  }
  protected LiveDataSubscriptionResponse (final Builder builder) {
    if (builder._requestedSpecification == null) _requestedSpecification = null;
    else {
      _requestedSpecification = builder._requestedSpecification;
    }
    if (builder._fullyQualifiedSpecification == null) _fullyQualifiedSpecification = null;
    else {
      _fullyQualifiedSpecification = builder._fullyQualifiedSpecification;
    }
    _subscriptionResult = builder._subscriptionResult;
    _userMessage = builder._userMessage;
    _tickDistributionSpecification = builder._tickDistributionSpecification;
  }
  public LiveDataSubscriptionResponse (com.opengamma.livedata.LiveDataSpecificationImpl requestedSpecification, com.opengamma.livedata.LiveDataSpecificationImpl fullyQualifiedSpecification, com.opengamma.livedata.LiveDataSubscriptionResult subscriptionResult, String userMessage, String tickDistributionSpecification) {
    if (requestedSpecification == null) throw new NullPointerException ("'requestedSpecification' cannot be null");
    else {
      _requestedSpecification = requestedSpecification;
    }
    if (fullyQualifiedSpecification == null) _fullyQualifiedSpecification = null;
    else {
      _fullyQualifiedSpecification = fullyQualifiedSpecification;
    }
    if (subscriptionResult == null) throw new NullPointerException ("subscriptionResult' cannot be null");
    _subscriptionResult = subscriptionResult;
    _userMessage = userMessage;
    _tickDistributionSpecification = tickDistributionSpecification;
  }
  protected LiveDataSubscriptionResponse (final LiveDataSubscriptionResponse source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
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
    _tickDistributionSpecification = source._tickDistributionSpecification;
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
    if (_fullyQualifiedSpecification != null)  {
      msg.add (FULLYQUALIFIEDSPECIFICATION_KEY, null, fudgeContext.objectToFudgeMsg (_fullyQualifiedSpecification));
    }
    if (_subscriptionResult != null)  {
      msg.add (SUBSCRIPTIONRESULT_KEY, null, _subscriptionResult.getFudgeEncoding ());
    }
    if (_userMessage != null)  {
      msg.add (USERMESSAGE_KEY, null, _userMessage);
    }
    if (_tickDistributionSpecification != null)  {
      msg.add (TICKDISTRIBUTIONSPECIFICATION_KEY, null, _tickDistributionSpecification);
    }
  }
  public static LiveDataSubscriptionResponse fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    return new Builder (fudgeContext, fudgeMsg).build ();
  }
  public com.opengamma.livedata.LiveDataSpecificationImpl getRequestedSpecification () {
    return _requestedSpecification;
  }
  public com.opengamma.livedata.LiveDataSpecificationImpl getFullyQualifiedSpecification () {
    return _fullyQualifiedSpecification;
  }
  public com.opengamma.livedata.LiveDataSubscriptionResult getSubscriptionResult () {
    return _subscriptionResult;
  }
  public String getUserMessage () {
    return _userMessage;
  }
  public String getTickDistributionSpecification () {
    return _tickDistributionSpecification;
  }
}
