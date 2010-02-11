// Automatically created - do not modify
// Created from com\opengamma\livedata\LiveDataSubscriptionRequest.proto:9(10)
package com.opengamma.livedata;
public class LiveDataSubscriptionRequest implements java.io.Serializable {
  private final String _userName;
  public static final String USERNAME_KEY = "userName";
  private final java.util.List<com.opengamma.livedata.LiveDataSpecificationImpl> _specifications;
  public static final String SPECIFICATIONS_KEY = "specifications";
  public LiveDataSubscriptionRequest (String userName, java.util.Collection<? extends com.opengamma.livedata.LiveDataSpecificationImpl> specifications) {
    if (userName == null) throw new NullPointerException ("userName' cannot be null");
    _userName = userName;
    if (specifications == null) throw new NullPointerException ("'specifications' cannot be null");
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecificationImpl> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecificationImpl> (specifications);
      if (specifications.size () == 0) throw new IllegalArgumentException ("'specifications' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecificationImpl> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecificationImpl fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'specifications' cannot be null");
        fudge1.set (fudge2);
      }
      _specifications = fudge0;
    }
  }
  protected LiveDataSubscriptionRequest (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (USERNAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionRequest - field 'userName' is not present");
    try {
      _userName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionRequest - field 'userName' is not string", e);
    }
    fudgeFields = fudgeMsg.getAllByName (SPECIFICATIONS_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionRequest - field 'specifications' is not present");
    _specifications = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecificationImpl> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.livedata.LiveDataSpecificationImpl fudge2;
        fudge2 = fudgeContext.fudgeMsgToObject (com.opengamma.livedata.LiveDataSpecificationImpl.class, fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudge1));
        _specifications.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveDataSubscriptionRequest - field 'specifications' is not LiveDataSpecificationImpl message", e);
      }
    }
  }
  protected LiveDataSubscriptionRequest (final LiveDataSubscriptionRequest source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _userName = source._userName;
    if (source._specifications == null) _specifications = null;
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecificationImpl> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecificationImpl> (source._specifications);
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecificationImpl> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecificationImpl fudge2 = fudge1.next ();
        fudge1.set (fudge2);
      }
      _specifications = fudge0;
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
    if (_specifications != null)  {
      for (com.opengamma.livedata.LiveDataSpecificationImpl fudge1 : _specifications) {
        msg.add (SPECIFICATIONS_KEY, null, fudgeContext.objectToFudgeMsg (fudge1));
      }
    }
  }
  public static LiveDataSubscriptionRequest fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    return new LiveDataSubscriptionRequest (fudgeContext, fudgeMsg);
  }
  public String getUserName () {
    return _userName;
  }
  public com.opengamma.livedata.LiveDataSpecificationImpl getSpecifications () {
    return getSpecifications (0);
  }
  public int getSpecificationsCount () {
    return (_specifications != null) ? _specifications.size () : 0;
  }
  public com.opengamma.livedata.LiveDataSpecificationImpl getSpecifications (final int n) {
    if (_specifications == null)  {
      if (n == 0) return null;
      throw new IndexOutOfBoundsException ("n=" + n);
    }
    return _specifications.get (n);
  }
  public java.util.List<com.opengamma.livedata.LiveDataSpecificationImpl> getSpecificationsList () {
    return java.util.Collections.unmodifiableList (_specifications);
  }
  public boolean equals (final Object o) {
    if (o == null) return false;
    if (!(o instanceof LiveDataSubscriptionRequest)) return false;
    LiveDataSubscriptionRequest msg = (LiveDataSubscriptionRequest)o;
    if (_userName != null) {
      if (msg._userName != null) {
        if (!_userName.equals (msg._userName)) return false;
      }
      else return false;
    }
    else if (msg._userName != null) return false;
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
    if (_userName != null) hc += _userName.hashCode ();
    hc *= 31;
    if (_specifications != null) hc += _specifications.hashCode ();
    return hc;
  }
}
