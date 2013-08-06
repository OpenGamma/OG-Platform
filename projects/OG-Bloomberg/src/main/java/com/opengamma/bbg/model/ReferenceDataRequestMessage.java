// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.bbg.model;
import java.util.Set;
import java.util.TreeSet;
public class ReferenceDataRequestMessage implements java.io.Serializable {
          public Set<String> getSecurities () {
            return new TreeSet<String> (getSecurity ());
          }
          public Set<String> getFields () {
            return new TreeSet<String> (getField ()); 
          }
  private static final long serialVersionUID = 27268955680917l;
  private java.util.List<String> _security;
  public static final String SECURITY_KEY = "security";
  private java.util.List<String> _field;
  public static final String FIELD_KEY = "field";
  public ReferenceDataRequestMessage () {
  }
  protected ReferenceDataRequestMessage (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (SECURITY_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<String> fudge1;
      fudge1 = new java.util.ArrayList<String> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          fudge1.add (fudge2.getValue ().toString ());
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a ReferenceDataRequestMessage - field 'security' is not string", e);
        }
      }
      setSecurity (fudge1);
    }
    fudgeFields = fudgeMsg.getAllByName (FIELD_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<String> fudge1;
      fudge1 = new java.util.ArrayList<String> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          fudge1.add (fudge2.getValue ().toString ());
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a ReferenceDataRequestMessage - field 'field' is not string", e);
        }
      }
      setField (fudge1);
    }
  }
  public ReferenceDataRequestMessage (java.util.Collection<? extends String> security, java.util.Collection<? extends String> field) {
    if (security == null) _security = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (security);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'security' cannot be null");
      }
      _security = fudge0;
    }
    if (field == null) _field = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (field);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'field' cannot be null");
      }
      _field = fudge0;
    }
  }
  protected ReferenceDataRequestMessage (final ReferenceDataRequestMessage source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._security == null) _security = null;
    else {
      _security = new java.util.ArrayList<String> (source._security);
    }
    if (source._field == null) _field = null;
    else {
      _field = new java.util.ArrayList<String> (source._field);
    }
  }
  public ReferenceDataRequestMessage clone () {
    return new ReferenceDataRequestMessage (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_security != null)  {
      for (String fudge1 : _security) {
        msg.add (SECURITY_KEY, null, fudge1);
      }
    }
    if (_field != null)  {
      for (String fudge1 : _field) {
        msg.add (FIELD_KEY, null, fudge1);
      }
    }
  }
  public static ReferenceDataRequestMessage fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.bbg.model.ReferenceDataRequestMessage".equals (className)) break;
      try {
        return (com.opengamma.bbg.model.ReferenceDataRequestMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ReferenceDataRequestMessage (deserializer, fudgeMsg);
  }
  public java.util.List<String> getSecurity () {
    if (_security != null) {
      return java.util.Collections.unmodifiableList (_security);
    }
    else return null;
  }
  public void setSecurity (String security) {
    if (security == null) _security = null;
    else {
      _security = new java.util.ArrayList<String> (1);
      addSecurity (security);
    }
  }
  public void setSecurity (java.util.Collection<? extends String> security) {
    if (security == null) _security = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (security);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'security' cannot be null");
      }
      _security = fudge0;
    }
  }
  public void addSecurity (String security) {
    if (security == null) throw new NullPointerException ("'security' cannot be null");
    if (_security == null) _security = new java.util.ArrayList<String> ();
    _security.add (security);
  }
  public java.util.List<String> getField () {
    if (_field != null) {
      return java.util.Collections.unmodifiableList (_field);
    }
    else return null;
  }
  public void setField (String field) {
    if (field == null) _field = null;
    else {
      _field = new java.util.ArrayList<String> (1);
      addField (field);
    }
  }
  public void setField (java.util.Collection<? extends String> field) {
    if (field == null) _field = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (field);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'field' cannot be null");
      }
      _field = fudge0;
    }
  }
  public void addField (String field) {
    if (field == null) throw new NullPointerException ("'field' cannot be null");
    if (_field == null) _field = new java.util.ArrayList<String> ();
    _field.add (field);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
