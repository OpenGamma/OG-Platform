// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.cache.msg;
public class GetResponse extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitGetResponse (this); }
  private static final long serialVersionUID = -381391323l;
  private java.util.List<byte[]> _data;
  public static final String DATA_KEY = "data";
  public GetResponse (java.util.Collection<? extends byte[]> data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      final java.util.List<byte[]> fudge0 = new java.util.ArrayList<byte[]> (data);
      if (data.size () == 0) throw new IllegalArgumentException ("'data' cannot be an empty list");
      for (java.util.ListIterator<byte[]> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        byte[] fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'data' cannot be null");
        fudge1.set (java.util.Arrays.copyOf (fudge2, fudge2.length));
      }
      _data = fudge0;
    }
  }
  protected GetResponse (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (DATA_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a GetResponse - field 'data' is not present");
    _data = new java.util.ArrayList<byte[]> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        _data.add (fudgeMsg.getFieldValue (byte[].class, fudge1));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a GetResponse - field 'data' is not byte[]", e);
      }
    }
  }
  public GetResponse (Long correlationId, java.util.Collection<? extends byte[]> data) {
    super (correlationId);
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      final java.util.List<byte[]> fudge0 = new java.util.ArrayList<byte[]> (data);
      if (data.size () == 0) throw new IllegalArgumentException ("'data' cannot be an empty list");
      for (java.util.ListIterator<byte[]> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        byte[] fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'data' cannot be null");
        fudge1.set (java.util.Arrays.copyOf (fudge2, fudge2.length));
      }
      _data = fudge0;
    }
  }
  protected GetResponse (final GetResponse source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._data == null) _data = null;
    else {
      final java.util.List<byte[]> fudge0 = new java.util.ArrayList<byte[]> (source._data);
      for (java.util.ListIterator<byte[]> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        byte[] fudge2 = fudge1.next ();
        fudge1.set (java.util.Arrays.copyOf (fudge2, fudge2.length));
      }
      _data = fudge0;
    }
  }
  public GetResponse clone () {
    return new GetResponse (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_data != null)  {
      for (byte[] fudge1 : _data) {
        msg.add (DATA_KEY, null, fudge1);
      }
    }
  }
  public static GetResponse fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.GetResponse".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.GetResponse)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new GetResponse (fudgeMsg);
  }
  public java.util.List<byte[]> getData () {
    return java.util.Collections.unmodifiableList (_data);
  }
  public void setData (byte[] data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      _data = new java.util.ArrayList<byte[]> (1);
      addData (data);
    }
  }
  public void setData (java.util.Collection<? extends byte[]> data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      final java.util.List<byte[]> fudge0 = new java.util.ArrayList<byte[]> (data);
      if (data.size () == 0) throw new IllegalArgumentException ("'data' cannot be an empty list");
      for (java.util.ListIterator<byte[]> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        byte[] fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'data' cannot be null");
        fudge1.set (java.util.Arrays.copyOf (fudge2, fudge2.length));
      }
      _data = fudge0;
    }
  }
  public void addData (byte[] data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    if (_data == null) _data = new java.util.ArrayList<byte[]> ();
    _data.add (java.util.Arrays.copyOf (data, data.length));
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
