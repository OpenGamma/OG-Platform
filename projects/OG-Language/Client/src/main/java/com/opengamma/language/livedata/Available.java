// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.livedata;
public class Available extends com.opengamma.language.connector.LiveData implements java.io.Serializable {
  private static final long serialVersionUID = 43233298658l;
  public static class Entry implements java.io.Serializable {
    private static final long serialVersionUID = -48244976843417l;
    private int _identifier;
    public static final String IDENTIFIER_KEY = "identifier";
    private com.opengamma.language.livedata.Definition _definition;
    public static final String DEFINITION_KEY = "definition";
    public Entry (int identifier, com.opengamma.language.livedata.Definition definition) {
      _identifier = identifier;
      if (definition == null) throw new NullPointerException ("'definition' cannot be null");
      else {
        _definition = (com.opengamma.language.livedata.Definition)definition.clone ();
      }
    }
    protected Entry (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
      org.fudgemsg.FudgeField fudgeField;
      fudgeField = fudgeMsg.getByName (IDENTIFIER_KEY);
      if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Entry - field 'identifier' is not present");
      try {
        _identifier = fudgeMsg.getFieldValue (Integer.class, fudgeField);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Entry - field 'identifier' is not integer", e);
      }
      fudgeField = fudgeMsg.getByName (DEFINITION_KEY);
      if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Entry - field 'definition' is not present");
      try {
        _definition = com.opengamma.language.livedata.Definition.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Entry - field 'definition' is not Definition message", e);
      }
    }
    protected Entry (final Entry source) {
      if (source == null) throw new NullPointerException ("'source' must not be null");
      _identifier = source._identifier;
      if (source._definition == null) _definition = null;
      else {
        _definition = (com.opengamma.language.livedata.Definition)source._definition.clone ();
      }
    }
    public Entry clone () {
      return new Entry (this);
    }
    public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
      if (serializer == null) throw new NullPointerException ("serializer must not be null");
      final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
      toFudgeMsg (serializer, msg);
      return msg;
    }
    public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
      msg.add (IDENTIFIER_KEY, null, _identifier);
      if (_definition != null)  {
        final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _definition.getClass (), com.opengamma.language.livedata.Definition.class);
        _definition.toFudgeMsg (serializer, fudge1);
        msg.add (DEFINITION_KEY, null, fudge1);
      }
    }
    public static Entry fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
      final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
      for (org.fudgemsg.FudgeField field : types) {
        final String className = (String)field.getValue ();
        if ("com.opengamma.language.livedata.Available.Entry".equals (className)) break;
        try {
          return (com.opengamma.language.livedata.Available.Entry)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
        }
        catch (Throwable t) {
          // no-action
        }
      }
      return new Entry (deserializer, fudgeMsg);
    }
    public int getIdentifier () {
      return _identifier;
    }
    public void setIdentifier (int identifier) {
      _identifier = identifier;
    }
    public com.opengamma.language.livedata.Definition getDefinition () {
      return _definition;
    }
    public void setDefinition (com.opengamma.language.livedata.Definition definition) {
      if (definition == null) throw new NullPointerException ("'definition' cannot be null");
      else {
        _definition = (com.opengamma.language.livedata.Definition)definition.clone ();
      }
    }
    public boolean equals (final Object o) {
      if (o == this) return true;
      if (!(o instanceof Entry)) return false;
      Entry msg = (Entry)o;
      if (_identifier != msg._identifier) return false;
      if (_definition != null) {
        if (msg._definition != null) {
          if (!_definition.equals (msg._definition)) return false;
        }
        else return false;
      }
      else if (msg._definition != null) return false;
      return true;
    }
    public int hashCode () {
      int hc = 1;
      hc = (hc * 31) + (int)_identifier;
      hc *= 31;
      if (_definition != null) hc += _definition.hashCode ();
      return hc;
    }
    public String toString () {
      return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }
  private java.util.List<com.opengamma.language.livedata.Available.Entry> _liveData;
  public static final String LIVE_DATA_KEY = "liveData";
  public Available () {
  }
  protected Available (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (LIVE_DATA_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<com.opengamma.language.livedata.Available.Entry> fudge1;
      fudge1 = new java.util.ArrayList<com.opengamma.language.livedata.Available.Entry> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          final com.opengamma.language.livedata.Available.Entry fudge3;
          fudge3 = com.opengamma.language.livedata.Available.Entry.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge2));
          fudge1.add (fudge3);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a Available - field 'liveData' is not Entry message", e);
        }
      }
      setLiveData (fudge1);
    }
  }
  public Available (java.util.Collection<? extends com.opengamma.language.livedata.Available.Entry> liveData) {
    if (liveData == null) _liveData = null;
    else {
      final java.util.List<com.opengamma.language.livedata.Available.Entry> fudge0 = new java.util.ArrayList<com.opengamma.language.livedata.Available.Entry> (liveData);
      for (java.util.ListIterator<com.opengamma.language.livedata.Available.Entry> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.language.livedata.Available.Entry fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'liveData' cannot be null");
        fudge1.set ((com.opengamma.language.livedata.Available.Entry)fudge2.clone ());
      }
      _liveData = fudge0;
    }
  }
  protected Available (final Available source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._liveData == null) _liveData = null;
    else {
      final java.util.List<com.opengamma.language.livedata.Available.Entry> fudge0 = new java.util.ArrayList<com.opengamma.language.livedata.Available.Entry> (source._liveData);
      for (java.util.ListIterator<com.opengamma.language.livedata.Available.Entry> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.language.livedata.Available.Entry fudge2 = fudge1.next ();
        fudge1.set ((com.opengamma.language.livedata.Available.Entry)fudge2.clone ());
      }
      _liveData = fudge0;
    }
  }
  public Available clone () {
    return new Available (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_liveData != null)  {
      for (com.opengamma.language.livedata.Available.Entry fudge1 : _liveData) {
        final org.fudgemsg.MutableFudgeMsg fudge2 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), fudge1.getClass (), com.opengamma.language.livedata.Available.Entry.class);
        fudge1.toFudgeMsg (serializer, fudge2);
        msg.add (LIVE_DATA_KEY, null, fudge2);
      }
    }
  }
  public static Available fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.livedata.Available".equals (className)) break;
      try {
        return (com.opengamma.language.livedata.Available)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Available (deserializer, fudgeMsg);
  }
  public java.util.List<com.opengamma.language.livedata.Available.Entry> getLiveData () {
    if (_liveData != null) {
      return java.util.Collections.unmodifiableList (_liveData);
    }
    else return null;
  }
  public void setLiveData (com.opengamma.language.livedata.Available.Entry liveData) {
    if (liveData == null) _liveData = null;
    else {
      _liveData = new java.util.ArrayList<com.opengamma.language.livedata.Available.Entry> (1);
      addLiveData (liveData);
    }
  }
  public void setLiveData (java.util.Collection<? extends com.opengamma.language.livedata.Available.Entry> liveData) {
    if (liveData == null) _liveData = null;
    else {
      final java.util.List<com.opengamma.language.livedata.Available.Entry> fudge0 = new java.util.ArrayList<com.opengamma.language.livedata.Available.Entry> (liveData);
      for (java.util.ListIterator<com.opengamma.language.livedata.Available.Entry> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.language.livedata.Available.Entry fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'liveData' cannot be null");
        fudge1.set ((com.opengamma.language.livedata.Available.Entry)fudge2.clone ());
      }
      _liveData = fudge0;
    }
  }
  public void addLiveData (com.opengamma.language.livedata.Available.Entry liveData) {
    if (liveData == null) throw new NullPointerException ("'liveData' cannot be null");
    if (_liveData == null) _liveData = new java.util.ArrayList<com.opengamma.language.livedata.Available.Entry> ();
    _liveData.add ((com.opengamma.language.livedata.Available.Entry)liveData.clone ());
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Available)) return false;
    Available msg = (Available)o;
    if (_liveData != null) {
      if (msg._liveData != null) {
        if (!_liveData.equals (msg._liveData)) return false;
      }
      else return false;
    }
    else if (msg._liveData != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_liveData != null) hc += _liveData.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
