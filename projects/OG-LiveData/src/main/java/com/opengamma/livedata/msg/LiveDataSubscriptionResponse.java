// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.livedata.msg;
public class LiveDataSubscriptionResponse implements java.io.Serializable {
  private static final long serialVersionUID = 7462196183992596952l;
  private com.opengamma.livedata.LiveDataSpecification _requestedSpecification;
  public static final String REQUESTED_SPECIFICATION_KEY = "requestedSpecification";
  private com.opengamma.livedata.msg.LiveDataSubscriptionResult _subscriptionResult;
  public static final String SUBSCRIPTION_RESULT_KEY = "subscriptionResult";
  private String _userMessage;
  public static final String USER_MESSAGE_KEY = "userMessage";
  private com.opengamma.livedata.LiveDataSpecification _fullyQualifiedSpecification;
  public static final String FULLY_QUALIFIED_SPECIFICATION_KEY = "fullyQualifiedSpecification";
  private String _tickDistributionSpecification;
  public static final String TICK_DISTRIBUTION_SPECIFICATION_KEY = "tickDistributionSpecification";
  private com.opengamma.livedata.LiveDataValueUpdateBean _snapshot;
  public static final String SNAPSHOT_KEY = "snapshot";
  public LiveDataSubscriptionResponse (com.opengamma.livedata.LiveDataSpecification requestedSpecification, com.opengamma.livedata.msg.LiveDataSubscriptionResult subscriptionResult) {
    if (requestedSpecification == null) throw new NullPointerException ("'requestedSpecification' cannot be null");
    else {
      _requestedSpecification = requestedSpecification;
    }
    if (subscriptionResult == null) throw new NullPointerException ("subscriptionResult' cannot be null");
    _subscriptionResult = subscriptionResult;
  }
  protected LiveDataSubscriptionResponse (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (REQUESTED_SPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'requestedSpecification' is not present");
    try {
      _requestedSpecification = deserializer.fieldValueToObject (com.opengamma.livedata.LiveDataSpecification.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'requestedSpecification' is not LiveDataSpecification message", e);
    }
    fudgeField = fudgeMsg.getByName (SUBSCRIPTION_RESULT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'subscriptionResult' is not present");
    try {
      _subscriptionResult = fudgeMsg.getFieldValue (com.opengamma.livedata.msg.LiveDataSubscriptionResult.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'subscriptionResult' is not LiveDataSubscriptionResult enum", e);
    }
    fudgeField = fudgeMsg.getByName (USER_MESSAGE_KEY);
    if (fudgeField != null)  {
      try {
        setUserMessage ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'userMessage' is not string", e);
      }
    }
    fudgeField = fudgeMsg.getByName (FULLY_QUALIFIED_SPECIFICATION_KEY);
    if (fudgeField != null)  {
      try {
        final com.opengamma.livedata.LiveDataSpecification fudge1;
        fudge1 = deserializer.fieldValueToObject (com.opengamma.livedata.LiveDataSpecification.class, fudgeField);
        setFullyQualifiedSpecification (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'fullyQualifiedSpecification' is not LiveDataSpecification message", e);
      }
    }
    fudgeField = fudgeMsg.getByName (TICK_DISTRIBUTION_SPECIFICATION_KEY);
    if (fudgeField != null)  {
      try {
        setTickDistributionSpecification ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'tickDistributionSpecification' is not string", e);
      }
    }
    fudgeField = fudgeMsg.getByName (SNAPSHOT_KEY);
    if (fudgeField != null)  {
      try {
        final com.opengamma.livedata.LiveDataValueUpdateBean fudge1;
        fudge1 = deserializer.fieldValueToObject (com.opengamma.livedata.LiveDataValueUpdateBean.class, fudgeField);
        setSnapshot (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionResponse - field 'snapshot' is not LiveDataValueUpdateBean message", e);
      }
    }
  }
  public LiveDataSubscriptionResponse (com.opengamma.livedata.LiveDataSpecification requestedSpecification, com.opengamma.livedata.msg.LiveDataSubscriptionResult subscriptionResult, String userMessage, com.opengamma.livedata.LiveDataSpecification fullyQualifiedSpecification, String tickDistributionSpecification, com.opengamma.livedata.LiveDataValueUpdateBean snapshot) {
    if (requestedSpecification == null) throw new NullPointerException ("'requestedSpecification' cannot be null");
    else {
      _requestedSpecification = requestedSpecification;
    }
    if (subscriptionResult == null) throw new NullPointerException ("subscriptionResult' cannot be null");
    _subscriptionResult = subscriptionResult;
    _userMessage = userMessage;
    if (fullyQualifiedSpecification == null) _fullyQualifiedSpecification = null;
    else {
      _fullyQualifiedSpecification = fullyQualifiedSpecification;
    }
    _tickDistributionSpecification = tickDistributionSpecification;
    if (snapshot == null) _snapshot = null;
    else {
      _snapshot = snapshot;
    }
  }
  protected LiveDataSubscriptionResponse (final LiveDataSubscriptionResponse source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._requestedSpecification == null) _requestedSpecification = null;
    else {
      _requestedSpecification = source._requestedSpecification;
    }
    _subscriptionResult = source._subscriptionResult;
    _userMessage = source._userMessage;
    if (source._fullyQualifiedSpecification == null) _fullyQualifiedSpecification = null;
    else {
      _fullyQualifiedSpecification = source._fullyQualifiedSpecification;
    }
    _tickDistributionSpecification = source._tickDistributionSpecification;
    if (source._snapshot == null) _snapshot = null;
    else {
      _snapshot = source._snapshot;
    }
  }
  public LiveDataSubscriptionResponse clone () {
    return new LiveDataSubscriptionResponse (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_requestedSpecification != null)  {
      serializer.addToMessageWithClassHeaders (msg, REQUESTED_SPECIFICATION_KEY, null, _requestedSpecification, com.opengamma.livedata.LiveDataSpecification.class);
    }
    if (_subscriptionResult != null)  {
      msg.add (SUBSCRIPTION_RESULT_KEY, null, _subscriptionResult.name ());
    }
    if (_userMessage != null)  {
      msg.add (USER_MESSAGE_KEY, null, _userMessage);
    }
    if (_fullyQualifiedSpecification != null)  {
      serializer.addToMessageWithClassHeaders (msg, FULLY_QUALIFIED_SPECIFICATION_KEY, null, _fullyQualifiedSpecification, com.opengamma.livedata.LiveDataSpecification.class);
    }
    if (_tickDistributionSpecification != null)  {
      msg.add (TICK_DISTRIBUTION_SPECIFICATION_KEY, null, _tickDistributionSpecification);
    }
    if (_snapshot != null)  {
      serializer.addToMessageWithClassHeaders (msg, SNAPSHOT_KEY, null, _snapshot, com.opengamma.livedata.LiveDataValueUpdateBean.class);
    }
  }
  public static LiveDataSubscriptionResponse fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.livedata.msg.LiveDataSubscriptionResponse".equals (className)) break;
      try {
        return (com.opengamma.livedata.msg.LiveDataSubscriptionResponse)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new LiveDataSubscriptionResponse (deserializer, fudgeMsg);
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
  public com.opengamma.livedata.msg.LiveDataSubscriptionResult getSubscriptionResult () {
    return _subscriptionResult;
  }
  public void setSubscriptionResult (com.opengamma.livedata.msg.LiveDataSubscriptionResult subscriptionResult) {
    if (subscriptionResult == null) throw new NullPointerException ("subscriptionResult' cannot be null");
    _subscriptionResult = subscriptionResult;
  }
  public String getUserMessage () {
    return _userMessage;
  }
  public void setUserMessage (String userMessage) {
    _userMessage = userMessage;
  }
  public com.opengamma.livedata.LiveDataSpecification getFullyQualifiedSpecification () {
    return _fullyQualifiedSpecification;
  }
  public void setFullyQualifiedSpecification (com.opengamma.livedata.LiveDataSpecification fullyQualifiedSpecification) {
    if (fullyQualifiedSpecification == null) _fullyQualifiedSpecification = null;
    else {
      _fullyQualifiedSpecification = fullyQualifiedSpecification;
    }
  }
  public String getTickDistributionSpecification () {
    return _tickDistributionSpecification;
  }
  public void setTickDistributionSpecification (String tickDistributionSpecification) {
    _tickDistributionSpecification = tickDistributionSpecification;
  }
  public com.opengamma.livedata.LiveDataValueUpdateBean getSnapshot () {
    return _snapshot;
  }
  public void setSnapshot (com.opengamma.livedata.LiveDataValueUpdateBean snapshot) {
    if (snapshot == null) _snapshot = null;
    else {
      _snapshot = snapshot;
    }
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
