// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.livedata.msg;
public class EntitlementResponseMsg implements java.io.Serializable {
  private static final long serialVersionUID = -61750678368l;
  private java.util.List<com.opengamma.livedata.msg.EntitlementResponse> _responses;
  public static final String RESPONSES_KEY = "responses";
  public EntitlementResponseMsg (java.util.Collection<? extends com.opengamma.livedata.msg.EntitlementResponse> responses) {
    if (responses == null) throw new NullPointerException ("'responses' cannot be null");
    else {
      final java.util.List<com.opengamma.livedata.msg.EntitlementResponse> fudge0 = new java.util.ArrayList<com.opengamma.livedata.msg.EntitlementResponse> (responses);
      if (responses.size () == 0) throw new IllegalArgumentException ("'responses' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.livedata.msg.EntitlementResponse> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.msg.EntitlementResponse fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'responses' cannot be null");
        fudge1.set ((com.opengamma.livedata.msg.EntitlementResponse)fudge2.clone ());
      }
      _responses = fudge0;
    }
  }
  protected EntitlementResponseMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (RESPONSES_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a EntitlementResponseMsg - field 'responses' is not present");
    _responses = new java.util.ArrayList<com.opengamma.livedata.msg.EntitlementResponse> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.livedata.msg.EntitlementResponse fudge2;
        fudge2 = com.opengamma.livedata.msg.EntitlementResponse.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge1));
        _responses.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a EntitlementResponseMsg - field 'responses' is not EntitlementResponse message", e);
      }
    }
  }
  protected EntitlementResponseMsg (final EntitlementResponseMsg source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._responses == null) _responses = null;
    else {
      final java.util.List<com.opengamma.livedata.msg.EntitlementResponse> fudge0 = new java.util.ArrayList<com.opengamma.livedata.msg.EntitlementResponse> (source._responses);
      for (java.util.ListIterator<com.opengamma.livedata.msg.EntitlementResponse> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.msg.EntitlementResponse fudge2 = fudge1.next ();
        fudge1.set ((com.opengamma.livedata.msg.EntitlementResponse)fudge2.clone ());
      }
      _responses = fudge0;
    }
  }
  public EntitlementResponseMsg clone () {
    return new EntitlementResponseMsg (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_responses != null)  {
      for (com.opengamma.livedata.msg.EntitlementResponse fudge1 : _responses) {
        final org.fudgemsg.MutableFudgeMsg fudge2 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), fudge1.getClass (), com.opengamma.livedata.msg.EntitlementResponse.class);
        fudge1.toFudgeMsg (serializer, fudge2);
        msg.add (RESPONSES_KEY, null, fudge2);
      }
    }
  }
  public static EntitlementResponseMsg fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.livedata.msg.EntitlementResponseMsg".equals (className)) break;
      try {
        return (com.opengamma.livedata.msg.EntitlementResponseMsg)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new EntitlementResponseMsg (deserializer, fudgeMsg);
  }
  public java.util.List<com.opengamma.livedata.msg.EntitlementResponse> getResponses () {
    return java.util.Collections.unmodifiableList (_responses);
  }
  public void setResponses (com.opengamma.livedata.msg.EntitlementResponse responses) {
    if (responses == null) throw new NullPointerException ("'responses' cannot be null");
    else {
      _responses = new java.util.ArrayList<com.opengamma.livedata.msg.EntitlementResponse> (1);
      addResponses (responses);
    }
  }
  public void setResponses (java.util.Collection<? extends com.opengamma.livedata.msg.EntitlementResponse> responses) {
    if (responses == null) throw new NullPointerException ("'responses' cannot be null");
    else {
      final java.util.List<com.opengamma.livedata.msg.EntitlementResponse> fudge0 = new java.util.ArrayList<com.opengamma.livedata.msg.EntitlementResponse> (responses);
      if (responses.size () == 0) throw new IllegalArgumentException ("'responses' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.livedata.msg.EntitlementResponse> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.livedata.msg.EntitlementResponse fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'responses' cannot be null");
        fudge1.set ((com.opengamma.livedata.msg.EntitlementResponse)fudge2.clone ());
      }
      _responses = fudge0;
    }
  }
  public void addResponses (com.opengamma.livedata.msg.EntitlementResponse responses) {
    if (responses == null) throw new NullPointerException ("'responses' cannot be null");
    if (_responses == null) _responses = new java.util.ArrayList<com.opengamma.livedata.msg.EntitlementResponse> ();
    _responses.add ((com.opengamma.livedata.msg.EntitlementResponse)responses.clone ());
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
