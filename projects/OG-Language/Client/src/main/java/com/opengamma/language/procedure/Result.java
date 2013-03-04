// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.procedure;
public class Result extends com.opengamma.language.connector.Procedure implements java.io.Serializable {
  private static final long serialVersionUID = -29336371290l;
  private java.util.List<com.opengamma.language.Data> _result;
  public static final String RESULT_KEY = "result";
  public Result () {
  }
  protected Result (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (RESULT_KEY);
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
          throw new IllegalArgumentException ("Fudge message is not a Result - field 'result' is not Data message", e);
        }
      }
      setResult (fudge1);
    }
  }
  public Result (java.util.Collection<? extends com.opengamma.language.Data> result) {
    if (result == null) _result = null;
    else {
      final java.util.List<com.opengamma.language.Data> fudge0 = new java.util.ArrayList<com.opengamma.language.Data> (result);
      for (java.util.ListIterator<com.opengamma.language.Data> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.language.Data fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'result' cannot be null");
        fudge1.set ((com.opengamma.language.Data)fudge2.clone ());
      }
      _result = fudge0;
    }
  }
  protected Result (final Result source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._result == null) _result = null;
    else {
      final java.util.List<com.opengamma.language.Data> fudge0 = new java.util.ArrayList<com.opengamma.language.Data> (source._result);
      for (java.util.ListIterator<com.opengamma.language.Data> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.language.Data fudge2 = fudge1.next ();
        fudge1.set ((com.opengamma.language.Data)fudge2.clone ());
      }
      _result = fudge0;
    }
  }
  public Result clone () {
    return new Result (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_result != null)  {
      for (com.opengamma.language.Data fudge1 : _result) {
        final org.fudgemsg.MutableFudgeMsg fudge2 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), fudge1.getClass (), com.opengamma.language.Data.class);
        fudge1.toFudgeMsg (serializer, fudge2);
        msg.add (RESULT_KEY, null, fudge2);
      }
    }
  }
  public static Result fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.procedure.Result".equals (className)) break;
      try {
        return (com.opengamma.language.procedure.Result)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Result (deserializer, fudgeMsg);
  }
  public java.util.List<com.opengamma.language.Data> getResult () {
    if (_result != null) {
      return java.util.Collections.unmodifiableList (_result);
    }
    else return null;
  }
  public void setResult (com.opengamma.language.Data result) {
    if (result == null) _result = null;
    else {
      _result = new java.util.ArrayList<com.opengamma.language.Data> (1);
      addResult (result);
    }
  }
  public void setResult (java.util.Collection<? extends com.opengamma.language.Data> result) {
    if (result == null) _result = null;
    else {
      final java.util.List<com.opengamma.language.Data> fudge0 = new java.util.ArrayList<com.opengamma.language.Data> (result);
      for (java.util.ListIterator<com.opengamma.language.Data> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.language.Data fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'result' cannot be null");
        fudge1.set ((com.opengamma.language.Data)fudge2.clone ());
      }
      _result = fudge0;
    }
  }
  public void addResult (com.opengamma.language.Data result) {
    if (result == null) throw new NullPointerException ("'result' cannot be null");
    if (_result == null) _result = new java.util.ArrayList<com.opengamma.language.Data> ();
    _result.add ((com.opengamma.language.Data)result.clone ());
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Result)) return false;
    Result msg = (Result)o;
    if (_result != null) {
      if (msg._result != null) {
        if (!_result.equals (msg._result)) return false;
      }
      else return false;
    }
    else if (msg._result != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_result != null) hc += _result.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
