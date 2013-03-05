// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.function;
public class Invoke extends com.opengamma.language.connector.Function implements java.io.Serializable {
  public <T1,T2> T1 accept (final FunctionVisitor<T1,T2> visitor, final T2 data) throws com.opengamma.util.async.AsynchronousExecution { return visitor.visitInvoke (this, data); }
  private static final long serialVersionUID = -48154412771264l;
  private int _identifier;
  public static final String IDENTIFIER_KEY = "identifier";
  private java.util.List<com.opengamma.language.Data> _parameter;
  public static final String PARAMETER_KEY = "parameter";
  public Invoke (int identifier) {
    _identifier = identifier;
  }
  protected Invoke (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Invoke - field 'identifier' is not present");
    try {
      _identifier = fudgeMsg.getFieldValue (Integer.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Invoke - field 'identifier' is not integer", e);
    }
    fudgeFields = fudgeMsg.getAllByName (PARAMETER_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<com.opengamma.language.Data> fudge1;
      fudge1 = new java.util.ArrayList<com.opengamma.language.Data> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          final com.opengamma.language.Data fudge3;
          fudge3 = com.opengamma.language.Data.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge2));
          fudge1.add (fudge3);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a Invoke - field 'parameter' is not Data message", e);
        }
      }
      setParameter (fudge1);
    }
  }
  public Invoke (int identifier, java.util.Collection<? extends com.opengamma.language.Data> parameter) {
    _identifier = identifier;
    if (parameter == null) _parameter = null;
    else {
      final java.util.List<com.opengamma.language.Data> fudge0 = new java.util.ArrayList<com.opengamma.language.Data> (parameter);
      for (java.util.ListIterator<com.opengamma.language.Data> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.language.Data fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'parameter' cannot be null");
        fudge1.set ((com.opengamma.language.Data)fudge2.clone ());
      }
      _parameter = fudge0;
    }
  }
  protected Invoke (final Invoke source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _identifier = source._identifier;
    if (source._parameter == null) _parameter = null;
    else {
      final java.util.List<com.opengamma.language.Data> fudge0 = new java.util.ArrayList<com.opengamma.language.Data> (source._parameter);
      for (java.util.ListIterator<com.opengamma.language.Data> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.language.Data fudge2 = fudge1.next ();
        fudge1.set ((com.opengamma.language.Data)fudge2.clone ());
      }
      _parameter = fudge0;
    }
  }
  public Invoke clone () {
    return new Invoke (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    msg.add (IDENTIFIER_KEY, null, _identifier);
    if (_parameter != null)  {
      for (com.opengamma.language.Data fudge1 : _parameter) {
        final org.fudgemsg.MutableFudgeMsg fudge2 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), fudge1.getClass (), com.opengamma.language.Data.class);
        fudge1.toFudgeMsg (serializer, fudge2);
        msg.add (PARAMETER_KEY, null, fudge2);
      }
    }
  }
  public static Invoke fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.function.Invoke".equals (className)) break;
      try {
        return (com.opengamma.language.function.Invoke)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Invoke (deserializer, fudgeMsg);
  }
  public int getIdentifier () {
    return _identifier;
  }
  public void setIdentifier (int identifier) {
    _identifier = identifier;
  }
  public java.util.List<com.opengamma.language.Data> getParameter () {
    if (_parameter != null) {
      return java.util.Collections.unmodifiableList (_parameter);
    }
    else return null;
  }
  public void setParameter (com.opengamma.language.Data parameter) {
    if (parameter == null) _parameter = null;
    else {
      _parameter = new java.util.ArrayList<com.opengamma.language.Data> (1);
      addParameter (parameter);
    }
  }
  public void setParameter (java.util.Collection<? extends com.opengamma.language.Data> parameter) {
    if (parameter == null) _parameter = null;
    else {
      final java.util.List<com.opengamma.language.Data> fudge0 = new java.util.ArrayList<com.opengamma.language.Data> (parameter);
      for (java.util.ListIterator<com.opengamma.language.Data> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.language.Data fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'parameter' cannot be null");
        fudge1.set ((com.opengamma.language.Data)fudge2.clone ());
      }
      _parameter = fudge0;
    }
  }
  public void addParameter (com.opengamma.language.Data parameter) {
    if (parameter == null) throw new NullPointerException ("'parameter' cannot be null");
    if (_parameter == null) _parameter = new java.util.ArrayList<com.opengamma.language.Data> ();
    _parameter.add ((com.opengamma.language.Data)parameter.clone ());
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Invoke)) return false;
    Invoke msg = (Invoke)o;
    if (_identifier != msg._identifier) return false;
    if (_parameter != null) {
      if (msg._parameter != null) {
        if (!_parameter.equals (msg._parameter)) return false;
      }
      else return false;
    }
    else if (msg._parameter != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc = (hc * 31) + (int)_identifier;
    hc *= 31;
    if (_parameter != null) hc += _parameter.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
