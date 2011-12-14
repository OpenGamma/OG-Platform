// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.livedata.msg;
public class NormalizationRequest implements java.io.Serializable {
  private static final long serialVersionUID = -21852835570742026l;
  private long _correlationId;
  public static final String CORRELATION_ID_KEY = "correlationId";
  private java.util.List<com.opengamma.livedata.LiveDataSpecification> _liveDataSpecification;
  public static final String LIVE_DATA_SPECIFICATION_KEY = "liveDataSpecification";
  private java.util.List<org.fudgemsg.FudgeMsg> _values;
  public static final String VALUES_KEY = "values";
  public NormalizationRequest (long correlationId, java.util.Collection<? extends com.opengamma.livedata.LiveDataSpecification> liveDataSpecification, java.util.Collection<? extends org.fudgemsg.FudgeMsg> values) {
    _correlationId = correlationId;
    if (liveDataSpecification == null) throw new NullPointerException ("'liveDataSpecification' cannot be null");
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecification> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (liveDataSpecification);
      if (liveDataSpecification.size () == 0) throw new IllegalArgumentException ("'liveDataSpecification' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'liveDataSpecification' cannot be null");
        fudge1.set (fudge2);
      }
      _liveDataSpecification = fudge0;
    }
    if (values == null) throw new NullPointerException ("'values' cannot be null");
    else {
      final java.util.List<org.fudgemsg.FudgeMsg> fudge0 = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (values);
      if (values.size () == 0) throw new IllegalArgumentException ("'values' cannot be an empty list");
      for (java.util.ListIterator<org.fudgemsg.FudgeMsg> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        org.fudgemsg.FudgeMsg fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'values' cannot be null");
      }
      _values = fudge0;
    }
  }
  protected NormalizationRequest (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (CORRELATION_ID_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a NormalizationRequest - field 'correlationId' is not present");
    try {
      _correlationId = fudgeMsg.getFieldValue (Long.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a NormalizationRequest - field 'correlationId' is not long", e);
    }
    fudgeFields = fudgeMsg.getAllByName (LIVE_DATA_SPECIFICATION_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a NormalizationRequest - field 'liveDataSpecification' is not present");
    _liveDataSpecification = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.livedata.LiveDataSpecification fudge2;
        fudge2 = deserializer.fieldValueToObject (com.opengamma.livedata.LiveDataSpecification.class, fudge1);
        _liveDataSpecification.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a NormalizationRequest - field 'liveDataSpecification' is not LiveDataSpecification message", e);
      }
    }
    fudgeFields = fudgeMsg.getAllByName (VALUES_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a NormalizationRequest - field 'values' is not present");
    _values = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
      try {
        final org.fudgemsg.FudgeMsg fudge3;
        fudge3 = fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge2);
        _values.add (fudge3);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a NormalizationRequest - field 'values' is not anonymous/unknown message", e);
      }
    }
  }
  protected NormalizationRequest (final NormalizationRequest source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _correlationId = source._correlationId;
    if (source._liveDataSpecification == null) _liveDataSpecification = null;
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecification> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (source._liveDataSpecification);
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecification fudge2 = fudge1.next ();
        fudge1.set (fudge2);
      }
      _liveDataSpecification = fudge0;
    }
    if (source._values == null) _values = null;
    else {
      _values = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (source._values);
    }
  }
  public NormalizationRequest clone () {
    return new NormalizationRequest (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    msg.add (CORRELATION_ID_KEY, null, _correlationId);
    if (_liveDataSpecification != null)  {
      for (com.opengamma.livedata.LiveDataSpecification fudge1 : _liveDataSpecification) {
        serializer.addToMessageWithClassHeaders (msg, LIVE_DATA_SPECIFICATION_KEY, null, fudge1, com.opengamma.livedata.LiveDataSpecification.class);
      }
    }
    if (_values != null)  {
      for (org.fudgemsg.FudgeMsg fudge1 : _values) {
        msg.add (VALUES_KEY, null, (fudge1 instanceof org.fudgemsg.MutableFudgeMsg) ? serializer.newMessage (fudge1) : fudge1);
      }
    }
  }
  public static NormalizationRequest fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.livedata.msg.NormalizationRequest".equals (className)) break;
      try {
        return (com.opengamma.livedata.msg.NormalizationRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new NormalizationRequest (deserializer, fudgeMsg);
  }
  public long getCorrelationId () {
    return _correlationId;
  }
  public void setCorrelationId (long correlationId) {
    _correlationId = correlationId;
  }
  public java.util.List<com.opengamma.livedata.LiveDataSpecification> getLiveDataSpecification () {
    return java.util.Collections.unmodifiableList (_liveDataSpecification);
  }
  public void setLiveDataSpecification (com.opengamma.livedata.LiveDataSpecification liveDataSpecification) {
    if (liveDataSpecification == null) throw new NullPointerException ("'liveDataSpecification' cannot be null");
    else {
      _liveDataSpecification = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (1);
      addLiveDataSpecification (liveDataSpecification);
    }
  }
  public void setLiveDataSpecification (java.util.Collection<? extends com.opengamma.livedata.LiveDataSpecification> liveDataSpecification) {
    if (liveDataSpecification == null) throw new NullPointerException ("'liveDataSpecification' cannot be null");
    else {
      final java.util.List<com.opengamma.livedata.LiveDataSpecification> fudge0 = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> (liveDataSpecification);
      if (liveDataSpecification.size () == 0) throw new IllegalArgumentException ("'liveDataSpecification' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.livedata.LiveDataSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.LiveDataSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'liveDataSpecification' cannot be null");
        fudge1.set (fudge2);
      }
      _liveDataSpecification = fudge0;
    }
  }
  public void addLiveDataSpecification (com.opengamma.livedata.LiveDataSpecification liveDataSpecification) {
    if (liveDataSpecification == null) throw new NullPointerException ("'liveDataSpecification' cannot be null");
    if (_liveDataSpecification == null) _liveDataSpecification = new java.util.ArrayList<com.opengamma.livedata.LiveDataSpecification> ();
    _liveDataSpecification.add (liveDataSpecification);
  }
  public java.util.List<org.fudgemsg.FudgeMsg> getValues () {
    return java.util.Collections.unmodifiableList (_values);
  }
  public void setValues (org.fudgemsg.FudgeMsg values) {
    if (values == null) throw new NullPointerException ("'values' cannot be null");
    else {
      _values = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (1);
      addValues (values);
    }
  }
  public void setValues (java.util.Collection<? extends org.fudgemsg.FudgeMsg> values) {
    if (values == null) throw new NullPointerException ("'values' cannot be null");
    else {
      final java.util.List<org.fudgemsg.FudgeMsg> fudge0 = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (values);
      if (values.size () == 0) throw new IllegalArgumentException ("'values' cannot be an empty list");
      for (java.util.ListIterator<org.fudgemsg.FudgeMsg> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        org.fudgemsg.FudgeMsg fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'values' cannot be null");
      }
      _values = fudge0;
    }
  }
  public void addValues (org.fudgemsg.FudgeMsg values) {
    if (values == null) throw new NullPointerException ("'values' cannot be null");
    if (_values == null) _values = new java.util.ArrayList<org.fudgemsg.FudgeMsg> ();
    _values.add (values);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
