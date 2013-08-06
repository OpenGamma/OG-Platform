// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.livedata.msg;
public class EntitlementRequest implements java.io.Serializable {
  private static final long serialVersionUID = 702824259518l;
  private com.opengamma.livedata.UserPrincipal _user;
  public static final String USER_KEY = "user";
  private java.util.List<com.opengamma.livedata.LiveDataSpecification> _liveDataSpecifications;
  public static final String LIVE_DATA_SPECIFICATIONS_KEY = "liveDataSpecifications";
  public EntitlementRequest (com.opengamma.livedata.UserPrincipal user, java.util.Collection<? extends com.opengamma.livedata.LiveDataSpecification> liveDataSpecifications) {
    if (user == null) throw new NullPointerException ("'user' cannot be null");
    else {
      _user = user;
    }
    if (liveDataSpecifications == null) throw new NullPointerException ("'liveDataSpecifications' cannot be null");
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecification> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (liveDataSpecifications);
      if (liveDataSpecifications.size () == 0) throw new IllegalArgumentException ("'liveDataSpecifications' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'liveDataSpecifications' cannot be null");
        fudge1.set (fudge2);
      }
      _liveDataSpecifications = fudge0;
    }
  }
  protected EntitlementRequest (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (USER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EntitlementRequest - field 'user' is not present");
    try {
      _user = deserializer.fieldValueToObject (com.opengamma.livedata.UserPrincipal.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EntitlementRequest - field 'user' is not UserPrincipal message", e);
    }
    fudgeFields = fudgeMsg.getAllByName (LIVE_DATA_SPECIFICATIONS_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a EntitlementRequest - field 'liveDataSpecifications' is not present");
    _liveDataSpecifications = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.livedata.LiveDataSpecification fudge2;
        fudge2 = deserializer.fieldValueToObject (com.opengamma.livedata.LiveDataSpecification.class, fudge1);
        _liveDataSpecifications.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a EntitlementRequest - field 'liveDataSpecifications' is not LiveDataSpecification message", e);
      }
    }
  }
  protected EntitlementRequest (final EntitlementRequest source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._user == null) _user = null;
    else {
      _user = source._user;
    }
    if (source._liveDataSpecifications == null) _liveDataSpecifications = null;
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecification> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (source._liveDataSpecifications);
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecification fudge2 = fudge1.next ();
        fudge1.set (fudge2);
      }
      _liveDataSpecifications = fudge0;
    }
  }
  public EntitlementRequest clone () {
    return new EntitlementRequest (this);
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
    if (_liveDataSpecifications != null)  {
      for (com.opengamma.livedata.LiveDataSpecification fudge1 : _liveDataSpecifications) {
        serializer.addToMessageWithClassHeaders (msg, LIVE_DATA_SPECIFICATIONS_KEY, null, fudge1, com.opengamma.livedata.LiveDataSpecification.class);
      }
    }
  }
  public static EntitlementRequest fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.livedata.msg.EntitlementRequest".equals (className)) break;
      try {
        return (com.opengamma.livedata.msg.EntitlementRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new EntitlementRequest (deserializer, fudgeMsg);
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
  public java.util.List<com.opengamma.livedata.LiveDataSpecification> getLiveDataSpecifications () {
    return java.util.Collections.unmodifiableList (_liveDataSpecifications);
  }
  public void setLiveDataSpecifications (com.opengamma.livedata.LiveDataSpecification liveDataSpecifications) {
    if (liveDataSpecifications == null) throw new NullPointerException ("'liveDataSpecifications' cannot be null");
    else {
      _liveDataSpecifications = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (1);
      addLiveDataSpecifications (liveDataSpecifications);
    }
  }
  public void setLiveDataSpecifications (java.util.Collection<? extends com.opengamma.livedata.LiveDataSpecification> liveDataSpecifications) {
    if (liveDataSpecifications == null) throw new NullPointerException ("'liveDataSpecifications' cannot be null");
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecification> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (liveDataSpecifications);
      if (liveDataSpecifications.size () == 0) throw new IllegalArgumentException ("'liveDataSpecifications' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'liveDataSpecifications' cannot be null");
        fudge1.set (fudge2);
      }
      _liveDataSpecifications = fudge0;
    }
  }
  public void addLiveDataSpecifications (com.opengamma.livedata.LiveDataSpecification liveDataSpecifications) {
    if (liveDataSpecifications == null) throw new NullPointerException ("'liveDataSpecifications' cannot be null");
    if (_liveDataSpecifications == null) _liveDataSpecifications = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> ();
    _liveDataSpecifications.add (liveDataSpecifications);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
