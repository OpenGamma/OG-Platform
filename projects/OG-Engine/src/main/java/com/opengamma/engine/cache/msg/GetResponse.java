// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.cache.msg;
public class GetResponse extends com.opengamma.engine.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitGetResponse (this); }
  private static final long serialVersionUID = -490550345l;
  private java.util.List<org.fudgemsg.FudgeMsg> _data;
  public static final String DATA_KEY = "data";
  public GetResponse (java.util.Collection<? extends org.fudgemsg.FudgeMsg> data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      final java.util.List<org.fudgemsg.FudgeMsg> fudge0 = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (data);
      if (data.size () == 0) throw new IllegalArgumentException ("'data' cannot be an empty list");
      for (java.util.ListIterator<org.fudgemsg.FudgeMsg> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        org.fudgemsg.FudgeMsg fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'data' cannot be null");
      }
      _data = fudge0;
    }
  }
  protected GetResponse (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (DATA_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a GetResponse - field 'data' is not present");
    _data = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final org.fudgemsg.FudgeMsg fudge2;
        fudge2 = fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge1);
        _data.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a GetResponse - field 'data' is not anonymous/unknown message", e);
      }
    }
  }
  public GetResponse (Long correlationId, java.util.Collection<? extends org.fudgemsg.FudgeMsg> data) {
    super (correlationId);
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      final java.util.List<org.fudgemsg.FudgeMsg> fudge0 = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (data);
      if (data.size () == 0) throw new IllegalArgumentException ("'data' cannot be an empty list");
      for (java.util.ListIterator<org.fudgemsg.FudgeMsg> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        org.fudgemsg.FudgeMsg fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'data' cannot be null");
      }
      _data = fudge0;
    }
  }
  protected GetResponse (final GetResponse source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._data == null) _data = null;
    else {
      _data = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (source._data);
    }
  }
  public GetResponse clone () {
    return new GetResponse (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_data != null)  {
      for (org.fudgemsg.FudgeMsg fudge1 : _data) {
        msg.add (DATA_KEY, null, (fudge1 instanceof org.fudgemsg.MutableFudgeMsg) ? serializer.newMessage (fudge1) : fudge1);
      }
    }
  }
  public static GetResponse fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.cache.msg.GetResponse".equals (className)) break;
      try {
        return (com.opengamma.engine.cache.msg.GetResponse)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new GetResponse (deserializer, fudgeMsg);
  }
  public java.util.List<org.fudgemsg.FudgeMsg> getData () {
    return java.util.Collections.unmodifiableList (_data);
  }
  public void setData (org.fudgemsg.FudgeMsg data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      _data = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (1);
      addData (data);
    }
  }
  public void setData (java.util.Collection<? extends org.fudgemsg.FudgeMsg> data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      final java.util.List<org.fudgemsg.FudgeMsg> fudge0 = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (data);
      if (data.size () == 0) throw new IllegalArgumentException ("'data' cannot be an empty list");
      for (java.util.ListIterator<org.fudgemsg.FudgeMsg> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        org.fudgemsg.FudgeMsg fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'data' cannot be null");
      }
      _data = fudge0;
    }
  }
  public void addData (org.fudgemsg.FudgeMsg data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    if (_data == null) _data = new java.util.ArrayList<org.fudgemsg.FudgeMsg> ();
    _data.add (data);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
