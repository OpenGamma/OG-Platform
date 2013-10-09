// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.livedata.msg;
public class LiveDataSubscriptionRequest implements java.io.Serializable {
        @Override
        public String toString() {
          return new StringBuilder()
          .append("LiveDataSubscriptionRequest[")
          .append(_specifications.size())
          .append(" specifications]")
          .toString();
        }
  private static final long serialVersionUID = 666128997025752l;
  private com.opengamma.livedata.UserPrincipal _user;
  public static final String USER_KEY = "user";
  private com.opengamma.livedata.msg.SubscriptionType _type;
  public static final String TYPE_KEY = "type";
  private java.util.List<com.opengamma.livedata.LiveDataSpecification> _specifications;
  public static final String SPECIFICATIONS_KEY = "specifications";
  public LiveDataSubscriptionRequest (com.opengamma.livedata.UserPrincipal user, com.opengamma.livedata.msg.SubscriptionType type, java.util.Collection<? extends com.opengamma.livedata.LiveDataSpecification> specifications) {
    if (user == null) throw new NullPointerException ("'user' cannot be null");
    else {
      _user = user;
    }
    if (type == null) throw new NullPointerException ("type' cannot be null");
    _type = type;
    if (specifications == null) throw new NullPointerException ("'specifications' cannot be null");
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecification> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (specifications);
      if (specifications.size () == 0) throw new IllegalArgumentException ("'specifications' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'specifications' cannot be null");
        fudge1.set (fudge2);
      }
      _specifications = fudge0;
    }
  }
  protected LiveDataSubscriptionRequest (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (USER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionRequest - field 'user' is not present");
    try {
      _user = deserializer.fieldValueToObject (com.opengamma.livedata.UserPrincipal.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionRequest - field 'user' is not UserPrincipal message", e);
    }
    fudgeField = fudgeMsg.getByName (TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionRequest - field 'type' is not present");
    try {
      _type = fudgeMsg.getFieldValue (com.opengamma.livedata.msg.SubscriptionType.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionRequest - field 'type' is not SubscriptionType enum", e);
    }
    fudgeFields = fudgeMsg.getAllByName (SPECIFICATIONS_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionRequest - field 'specifications' is not present");
    _specifications = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.livedata.LiveDataSpecification fudge2;
        fudge2 = deserializer.fieldValueToObject (com.opengamma.livedata.LiveDataSpecification.class, fudge1);
        _specifications.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionRequest - field 'specifications' is not LiveDataSpecification message", e);
      }
    }
  }
  protected LiveDataSubscriptionRequest (final LiveDataSubscriptionRequest source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._user == null) _user = null;
    else {
      _user = source._user;
    }
    _type = source._type;
    if (source._specifications == null) _specifications = null;
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecification> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (source._specifications);
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecification fudge2 = fudge1.next ();
        fudge1.set (fudge2);
      }
      _specifications = fudge0;
    }
  }
  public LiveDataSubscriptionRequest clone () {
    return new LiveDataSubscriptionRequest (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_user != null)  {
      serializer.addToMessageWithClassHeaders (msg, USER_KEY, null, _user, com.opengamma.livedata.UserPrincipal.class);
    }
    if (_type != null)  {
      msg.add (TYPE_KEY, null, _type.name ());
    }
    if (_specifications != null)  {
      for (com.opengamma.livedata.LiveDataSpecification fudge1 : _specifications) {
        serializer.addToMessageWithClassHeaders (msg, SPECIFICATIONS_KEY, null, fudge1, com.opengamma.livedata.LiveDataSpecification.class);
      }
    }
  }
  public static LiveDataSubscriptionRequest fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.livedata.msg.LiveDataSubscriptionRequest".equals (className)) break;
      try {
        return (com.opengamma.livedata.msg.LiveDataSubscriptionRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new LiveDataSubscriptionRequest (deserializer, fudgeMsg);
  }
  public com.opengamma.livedata.UserPrincipal getUser () {
    return _user;
  }
  public void setUser (com.opengamma.livedata.UserPrincipal user) {
    if (user == null) throw new NullPointerException ("'user' cannot be null");
    else {
      _user = user;
    }
  }
  public com.opengamma.livedata.msg.SubscriptionType getType () {
    return _type;
  }
  public void setType (com.opengamma.livedata.msg.SubscriptionType type) {
    if (type == null) throw new NullPointerException ("type' cannot be null");
    _type = type;
  }
  public java.util.List<com.opengamma.livedata.LiveDataSpecification> getSpecifications () {
    return java.util.Collections.unmodifiableList (_specifications);
  }
  public void setSpecifications (com.opengamma.livedata.LiveDataSpecification specifications) {
    if (specifications == null) throw new NullPointerException ("'specifications' cannot be null");
    else {
      _specifications = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (1);
      addSpecifications (specifications);
    }
  }
  public void setSpecifications (java.util.Collection<? extends com.opengamma.livedata.LiveDataSpecification> specifications) {
    if (specifications == null) throw new NullPointerException ("'specifications' cannot be null");
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecification> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (specifications);
      if (specifications.size () == 0) throw new IllegalArgumentException ("'specifications' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'specifications' cannot be null");
        fudge1.set (fudge2);
      }
      _specifications = fudge0;
    }
  }
  public void addSpecifications (com.opengamma.livedata.LiveDataSpecification specifications) {
    if (specifications == null) throw new NullPointerException ("'specifications' cannot be null");
    if (_specifications == null) _specifications = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> ();
    _specifications.add (specifications);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof LiveDataSubscriptionRequest)) return false;
    LiveDataSubscriptionRequest msg = (LiveDataSubscriptionRequest)o;
    if (_user != null) {
      if (msg._user != null) {
        if (!_user.equals (msg._user)) return false;
      }
      else return false;
    }
    else if (msg._user != null) return false;
    if (_type != null) {
      if (msg._type != null) {
        if (!_type.equals (msg._type)) return false;
      }
      else return false;
    }
    else if (msg._type != null) return false;
    if (_specifications != null) {
      if (msg._specifications != null) {
        if (!_specifications.equals (msg._specifications)) return false;
      }
      else return false;
    }
    else if (msg._specifications != null) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_user != null) hc += _user.hashCode ();
    hc *= 31;
    if (_type != null) hc += _type.hashCode ();
    hc *= 31;
    if (_specifications != null) hc += _specifications.hashCode ();
    return hc;
  }
}
///CLOVER:ON - CSON
