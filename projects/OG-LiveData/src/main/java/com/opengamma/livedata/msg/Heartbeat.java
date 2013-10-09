// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.livedata.msg;
public class Heartbeat implements java.io.Serializable {
  private static final long serialVersionUID = 8068988544l;
  private java.util.List<com.opengamma.livedata.LiveDataSpecification> _liveDataSpecifications;
  public static final String LIVE_DATA_SPECIFICATIONS_KEY = "liveDataSpecifications";
  public Heartbeat (java.util.Collection<? extends com.opengamma.livedata.LiveDataSpecification> liveDataSpecifications) {
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
  protected Heartbeat (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (LIVE_DATA_SPECIFICATIONS_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a Heartbeat - field 'liveDataSpecifications' is not present");
    _liveDataSpecifications = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.livedata.LiveDataSpecification fudge2;
        fudge2 = deserializer.fieldValueToObject (com.opengamma.livedata.LiveDataSpecification.class, fudge1);
        _liveDataSpecifications.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Heartbeat - field 'liveDataSpecifications' is not LiveDataSpecification message", e);
      }
    }
  }
  protected Heartbeat (final Heartbeat source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
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
  public Heartbeat clone () {
    return new Heartbeat (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_liveDataSpecifications != null)  {
      for (com.opengamma.livedata.LiveDataSpecification fudge1 : _liveDataSpecifications) {
        serializer.addToMessageWithClassHeaders (msg, LIVE_DATA_SPECIFICATIONS_KEY, null, fudge1, com.opengamma.livedata.LiveDataSpecification.class);
      }
    }
  }
  public static Heartbeat fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.livedata.msg.Heartbeat".equals (className)) break;
      try {
        return (com.opengamma.livedata.msg.Heartbeat)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Heartbeat (deserializer, fudgeMsg);
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
