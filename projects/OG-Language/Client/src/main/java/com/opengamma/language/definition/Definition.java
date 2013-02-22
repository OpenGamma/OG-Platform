// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.definition;
public class Definition implements java.io.Serializable {
  private static final long serialVersionUID = -7791427280735659141l;
  private String _name;
  public static final String NAME_KEY = "name";
  private String _description;
  public static final String DESCRIPTION_KEY = "description";
  private java.util.List<String> _alias;
  public static final String ALIAS_KEY = "alias";
  private String _category;
  public static final String CATEGORY_KEY = "category";
  private java.util.List<com.opengamma.language.definition.Parameter> _parameter;
  public static final String PARAMETER_KEY = "parameter";
  public Definition (String name) {
    if (name == null) throw new NullPointerException ("name' cannot be null");
    _name = name;
  }
  protected Definition (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Definition - field 'name' is not present");
    try {
      _name = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Definition - field 'name' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (DESCRIPTION_KEY);
    if (fudgeField != null)  {
      try {
        setDescription ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Definition - field 'description' is not string", e);
      }
    }
    fudgeFields = fudgeMsg.getAllByName (ALIAS_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<String> fudge1;
      fudge1 = new java.util.ArrayList<String> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          fudge1.add (fudge2.getValue ().toString ());
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a Definition - field 'alias' is not string", e);
        }
      }
      setAlias (fudge1);
    }
    fudgeField = fudgeMsg.getByName (CATEGORY_KEY);
    if (fudgeField != null)  {
      try {
        setCategory ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Definition - field 'category' is not string", e);
      }
    }
    fudgeFields = fudgeMsg.getAllByName (PARAMETER_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<com.opengamma.language.definition.Parameter> fudge1;
      fudge1 = new java.util.ArrayList<com.opengamma.language.definition.Parameter> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          final com.opengamma.language.definition.Parameter fudge3;
          fudge3 = com.opengamma.language.definition.Parameter.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge2));
          fudge1.add (fudge3);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a Definition - field 'parameter' is not Parameter message", e);
        }
      }
      setParameter (fudge1);
    }
  }
  public Definition (String name, String description, java.util.Collection<? extends String> alias, String category, java.util.Collection<? extends com.opengamma.language.definition.Parameter> parameter) {
    if (name == null) throw new NullPointerException ("name' cannot be null");
    _name = name;
    _description = description;
    if (alias == null) _alias = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (alias);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'alias' cannot be null");
      }
      _alias = fudge0;
    }
    _category = category;
    if (parameter == null) _parameter = null;
    else {
      final java.util.List<com.opengamma.language.definition.Parameter> fudge0 = new java.util.ArrayList<com.opengamma.language.definition.Parameter> (parameter);
      for (java.util.ListIterator<com.opengamma.language.definition.Parameter> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.language.definition.Parameter fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'parameter' cannot be null");
        fudge1.set ((com.opengamma.language.definition.Parameter)fudge2.clone ());
      }
      _parameter = fudge0;
    }
  }
  protected Definition (final Definition source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _name = source._name;
    _description = source._description;
    if (source._alias == null) _alias = null;
    else {
      _alias = new java.util.ArrayList<String> (source._alias);
    }
    _category = source._category;
    if (source._parameter == null) _parameter = null;
    else {
      final java.util.List<com.opengamma.language.definition.Parameter> fudge0 = new java.util.ArrayList<com.opengamma.language.definition.Parameter> (source._parameter);
      for (java.util.ListIterator<com.opengamma.language.definition.Parameter> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.language.definition.Parameter fudge2 = fudge1.next ();
        fudge1.set ((com.opengamma.language.definition.Parameter)fudge2.clone ());
      }
      _parameter = fudge0;
    }
  }
  public Definition clone () {
    return new Definition (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_name != null)  {
      msg.add (NAME_KEY, null, _name);
    }
    if (_description != null)  {
      msg.add (DESCRIPTION_KEY, null, _description);
    }
    if (_alias != null)  {
      for (String fudge1 : _alias) {
        msg.add (ALIAS_KEY, null, fudge1);
      }
    }
    if (_category != null)  {
      msg.add (CATEGORY_KEY, null, _category);
    }
    if (_parameter != null)  {
      for (com.opengamma.language.definition.Parameter fudge1 : _parameter) {
        final org.fudgemsg.MutableFudgeMsg fudge2 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), fudge1.getClass (), com.opengamma.language.definition.Parameter.class);
        fudge1.toFudgeMsg (serializer, fudge2);
        msg.add (PARAMETER_KEY, null, fudge2);
      }
    }
  }
  public static Definition fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.definition.Definition".equals (className)) break;
      try {
        return (com.opengamma.language.definition.Definition)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Definition (deserializer, fudgeMsg);
  }
  public String getName () {
    return _name;
  }
  public void setName (String name) {
    if (name == null) throw new NullPointerException ("name' cannot be null");
    _name = name;
  }
  public String getDescription () {
    return _description;
  }
  public void setDescription (String description) {
    _description = description;
  }
  public java.util.List<String> getAlias () {
    if (_alias != null) {
      return java.util.Collections.unmodifiableList (_alias);
    }
    else return null;
  }
  public void setAlias (String alias) {
    if (alias == null) _alias = null;
    else {
      _alias = new java.util.ArrayList<String> (1);
      addAlias (alias);
    }
  }
  public void setAlias (java.util.Collection<? extends String> alias) {
    if (alias == null) _alias = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (alias);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'alias' cannot be null");
      }
      _alias = fudge0;
    }
  }
  public void addAlias (String alias) {
    if (alias == null) throw new NullPointerException ("'alias' cannot be null");
    if (_alias == null) _alias = new java.util.ArrayList<String> ();
    _alias.add (alias);
  }
  public String getCategory () {
    return _category;
  }
  public void setCategory (String category) {
    _category = category;
  }
  public java.util.List<com.opengamma.language.definition.Parameter> getParameter () {
    if (_parameter != null) {
      return java.util.Collections.unmodifiableList (_parameter);
    }
    else return null;
  }
  public void setParameter (com.opengamma.language.definition.Parameter parameter) {
    if (parameter == null) _parameter = null;
    else {
      _parameter = new java.util.ArrayList<com.opengamma.language.definition.Parameter> (1);
      addParameter (parameter);
    }
  }
  public void setParameter (java.util.Collection<? extends com.opengamma.language.definition.Parameter> parameter) {
    if (parameter == null) _parameter = null;
    else {
      final java.util.List<com.opengamma.language.definition.Parameter> fudge0 = new java.util.ArrayList<com.opengamma.language.definition.Parameter> (parameter);
      for (java.util.ListIterator<com.opengamma.language.definition.Parameter> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.language.definition.Parameter fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'parameter' cannot be null");
        fudge1.set ((com.opengamma.language.definition.Parameter)fudge2.clone ());
      }
      _parameter = fudge0;
    }
  }
  public void addParameter (com.opengamma.language.definition.Parameter parameter) {
    if (parameter == null) throw new NullPointerException ("'parameter' cannot be null");
    if (_parameter == null) _parameter = new java.util.ArrayList<com.opengamma.language.definition.Parameter> ();
    _parameter.add ((com.opengamma.language.definition.Parameter)parameter.clone ());
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Definition)) return false;
    Definition msg = (Definition)o;
    if (_name != null) {
      if (msg._name != null) {
        if (!_name.equals (msg._name)) return false;
      }
      else return false;
    }
    else if (msg._name != null) return false;
    if (_description != null) {
      if (msg._description != null) {
        if (!_description.equals (msg._description)) return false;
      }
      else return false;
    }
    else if (msg._description != null) return false;
    if (_alias != null) {
      if (msg._alias != null) {
        if (!_alias.equals (msg._alias)) return false;
      }
      else return false;
    }
    else if (msg._alias != null) return false;
    if (_category != null) {
      if (msg._category != null) {
        if (!_category.equals (msg._category)) return false;
      }
      else return false;
    }
    else if (msg._category != null) return false;
    if (_parameter != null) {
      if (msg._parameter != null) {
        if (!_parameter.equals (msg._parameter)) return false;
      }
      else return false;
    }
    else if (msg._parameter != null) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_name != null) hc += _name.hashCode ();
    hc *= 31;
    if (_description != null) hc += _description.hashCode ();
    hc *= 31;
    if (_alias != null) hc += _alias.hashCode ();
    hc *= 31;
    if (_category != null) hc += _category.hashCode ();
    hc *= 31;
    if (_parameter != null) hc += _parameter.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
