// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/engine/function/config/ParameterizedFunctionConfiguration.proto:9(10)
package com.opengamma.engine.function.config;
public class ParameterizedFunctionConfiguration extends com.opengamma.engine.function.config.StaticFunctionConfiguration implements java.io.Serializable {
  private static final long serialVersionUID = 59539022111l;
  private java.util.List<String> _parameter;
  public static final String PARAMETER_KEY = "parameter";
  public ParameterizedFunctionConfiguration () {
  }
  protected ParameterizedFunctionConfiguration (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (PARAMETER_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<String> fudge1;
      fudge1 = new java.util.ArrayList<String> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          fudge1.add (fudge2.getValue ().toString ());
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a ParameterizedFunctionConfiguration - field 'parameter' is not string", e);
        }
      }
      setParameter (fudge1);
    }
  }
  public ParameterizedFunctionConfiguration (String definitionClassName, java.util.Collection<? extends String> parameter) {
    super (definitionClassName);
    if (parameter == null) _parameter = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (parameter);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'parameter' cannot be null");
      }
      _parameter = fudge0;
    }
  }
  protected ParameterizedFunctionConfiguration (final ParameterizedFunctionConfiguration source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._parameter == null) _parameter = null;
    else {
      _parameter = new java.util.ArrayList<String> (source._parameter);
    }
  }
  public ParameterizedFunctionConfiguration clone () {
    return new ParameterizedFunctionConfiguration (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_parameter != null)  {
      for (String fudge1 : _parameter) {
        msg.add (PARAMETER_KEY, null, fudge1);
      }
    }
  }
  public static ParameterizedFunctionConfiguration fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.function.config.ParameterizedFunctionConfiguration".equals (className)) break;
      try {
        return (com.opengamma.engine.function.config.ParameterizedFunctionConfiguration)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ParameterizedFunctionConfiguration (fudgeMsg);
  }
  public java.util.List<String> getParameter () {
    if (_parameter != null) {
      return java.util.Collections.unmodifiableList (_parameter);
    }
    else return null;
  }
  public void setParameter (String parameter) {
    if (parameter == null) _parameter = null;
    else {
      _parameter = new java.util.ArrayList<String> (1);
      addParameter (parameter);
    }
  }
  public void setParameter (java.util.Collection<? extends String> parameter) {
    if (parameter == null) _parameter = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (parameter);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'parameter' cannot be null");
      }
      _parameter = fudge0;
    }
  }
  public void addParameter (String parameter) {
    if (parameter == null) throw new NullPointerException ("'parameter' cannot be null");
    if (_parameter == null) _parameter = new java.util.ArrayList<String> ();
    _parameter.add (parameter);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
