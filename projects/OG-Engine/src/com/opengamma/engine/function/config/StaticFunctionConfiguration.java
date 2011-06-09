// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.function.config;
public class StaticFunctionConfiguration extends com.opengamma.engine.function.config.FunctionConfiguration implements java.io.Serializable {
  private static final long serialVersionUID = 1144487394l;
  private String _definitionClassName;
  public static final String DEFINITION_CLASS_NAME_KEY = "definitionClassName";
  public StaticFunctionConfiguration () {
  }
  protected StaticFunctionConfiguration (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (DEFINITION_CLASS_NAME_KEY);
    if (fudgeField != null)  {
      try {
        setDefinitionClassName ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a StaticFunctionConfiguration - field 'definitionClassName' is not string", e);
      }
    }
  }
  public StaticFunctionConfiguration (String definitionClassName) {
    _definitionClassName = definitionClassName;
  }
  protected StaticFunctionConfiguration (final StaticFunctionConfiguration source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _definitionClassName = source._definitionClassName;
  }
  public StaticFunctionConfiguration clone () {
    return new StaticFunctionConfiguration (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_definitionClassName != null)  {
      msg.add (DEFINITION_CLASS_NAME_KEY, null, _definitionClassName);
    }
  }
  public static StaticFunctionConfiguration fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.function.config.StaticFunctionConfiguration".equals (className)) break;
      try {
        return (com.opengamma.engine.function.config.StaticFunctionConfiguration)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new StaticFunctionConfiguration (fudgeContext, fudgeMsg);
  }
  public String getDefinitionClassName () {
    return _definitionClassName;
  }
  public void setDefinitionClassName (String definitionClassName) {
    _definitionClassName = definitionClassName;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
