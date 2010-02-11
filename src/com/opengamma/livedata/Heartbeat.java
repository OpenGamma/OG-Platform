// Automatically created - do not modify
// Created from com\opengamma\livedata\Heartbeat.proto:9(10)
package com.opengamma.livedata;
public class Heartbeat implements java.io.Serializable {
  private final java.util.List<com.opengamma.livedata.LiveDataSpecificationImpl> _liveDataSpecifications;
  public static final String LIVEDATASPECIFICATIONS_KEY = "liveDataSpecifications";
  public Heartbeat (java.util.Collection<? extends com.opengamma.livedata.LiveDataSpecificationImpl> liveDataSpecifications) {
    if (liveDataSpecifications == null) throw new NullPointerException ("'liveDataSpecifications' cannot be null");
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecificationImpl> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecificationImpl> (liveDataSpecifications);
      if (liveDataSpecifications.size () == 0) throw new IllegalArgumentException ("'liveDataSpecifications' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecificationImpl> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecificationImpl fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'liveDataSpecifications' cannot be null");
        fudge1.set (fudge2);
      }
      _liveDataSpecifications = fudge0;
    }
  }
  protected Heartbeat (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (LIVEDATASPECIFICATIONS_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a Heartbeat - field 'liveDataSpecifications' is not present");
    _liveDataSpecifications = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecificationImpl> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.livedata.LiveDataSpecificationImpl fudge2;
        fudge2 = fudgeContext.fudgeMsgToObject (com.opengamma.livedata.LiveDataSpecificationImpl.class, fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudge1));
        _liveDataSpecifications.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Heartbeat - field 'liveDataSpecifications' is not LiveDataSpecificationImpl message", e);
      }
    }
  }
  protected Heartbeat (final Heartbeat source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._liveDataSpecifications == null) _liveDataSpecifications = null;
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecificationImpl> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecificationImpl> (source._liveDataSpecifications);
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecificationImpl> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecificationImpl fudge2 = fudge1.next ();
        fudge1.set (fudge2);
      }
      _liveDataSpecifications = fudge0;
    }
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    if (_liveDataSpecifications != null)  {
      for (com.opengamma.livedata.LiveDataSpecificationImpl fudge1 : _liveDataSpecifications) {
        msg.add (LIVEDATASPECIFICATIONS_KEY, null, fudgeContext.objectToFudgeMsg (fudge1));
      }
    }
  }
  public static Heartbeat fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    return new Heartbeat (fudgeContext, fudgeMsg);
  }
  public com.opengamma.livedata.LiveDataSpecificationImpl getLiveDataSpecifications () {
    return getLiveDataSpecifications (0);
  }
  public int getLiveDataSpecificationsCount () {
    return (_liveDataSpecifications != null) ? _liveDataSpecifications.size () : 0;
  }
  public com.opengamma.livedata.LiveDataSpecificationImpl getLiveDataSpecifications (final int n) {
    if (_liveDataSpecifications == null)  {
      if (n == 0) return null;
      throw new IndexOutOfBoundsException ("n=" + n);
    }
    return _liveDataSpecifications.get (n);
  }
  public java.util.List<com.opengamma.livedata.LiveDataSpecificationImpl> getLiveDataSpecificationsList () {
    return java.util.Collections.unmodifiableList (_liveDataSpecifications);
  }
}
