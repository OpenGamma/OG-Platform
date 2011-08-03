// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.master.security;
public class ManageableSecurity implements java.io.Serializable, com.opengamma.core.security.Security, com.opengamma.id.MutableUniqueIdentifiable {
          /**
         * Dynamically determines a 'default' display name if one hasn't been explicitly set.
         * This implementation constructs one from the identity key or identifiers.
         * 
         * @return a default display name
         */
        protected String buildDefaultDisplayName() {
          final com.opengamma.id.UniqueId identifier = getUniqueId(); // assign for thread-safety
          if (identifier != null) {
            return identifier.toString();
          }
          final com.opengamma.id.IdentifierBundle bundle = getIdentifiers(); // assign for thread-safety
          final com.opengamma.id.Identifier first = (bundle.size() == 0 ? null : bundle.getIdentifiers().iterator().next());
          return org.apache.commons.lang.ObjectUtils.toString(first);
        }
        
        /**
         * Add an identifier to the bundle.
         */
        public void addIdentifier (final com.opengamma.id.Identifier identifier) {
          setIdentifiers (getIdentifiers ().withIdentifier (identifier));
        }
  private static final long serialVersionUID = -9060937763900388887l;
  private com.opengamma.id.UniqueId _uniqueId;
  public static final String UNIQUE_ID_KEY = "uniqueId";
  private String _name;
  public static final String NAME_KEY = "name";
  private String _securityType;
  public static final String SECURITY_TYPE_KEY = "securityType";
  private com.opengamma.id.IdentifierBundle _identifiers;
  public static final String IDENTIFIERS_KEY = "identifiers";
  public static final String NAME = "";
  public static final com.opengamma.id.IdentifierBundle IDENTIFIERS = new com.opengamma.id.IdentifierBundle ();
  public ManageableSecurity (String securityType) {
    if (securityType == null) throw new NullPointerException ("securityType' cannot be null");
    _securityType = securityType;
    setName (NAME);
    setIdentifiers (IDENTIFIERS);
  }
  protected ManageableSecurity (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ManageableSecurity - field 'name' is not present");
    try {
      _name = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ManageableSecurity - field 'name' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (SECURITY_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ManageableSecurity - field 'securityType' is not present");
    try {
      _securityType = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ManageableSecurity - field 'securityType' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (IDENTIFIERS_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ManageableSecurity - field 'identifiers' is not present");
    try {
      _identifiers = com.opengamma.id.IdentifierBundle.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ManageableSecurity - field 'identifiers' is not IdentifierBundle message", e);
    }
    fudgeField = fudgeMsg.getByName (UNIQUE_ID_KEY);
    if (fudgeField != null)  {
      try {
        final com.opengamma.id.UniqueId fudge1;
        fudge1 = com.opengamma.id.UniqueId.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
        setUniqueId (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a ManageableSecurity - field 'uniqueId' is not UniqueId message", e);
      }
    }
  }
  public ManageableSecurity (com.opengamma.id.UniqueId uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers) {
    if (uniqueId == null) _uniqueId = null;
    else {
      _uniqueId = uniqueId;
    }
    if (name == null) throw new NullPointerException ("name' cannot be null");
    _name = name;
    if (securityType == null) throw new NullPointerException ("securityType' cannot be null");
    _securityType = securityType;
    if (identifiers == null) throw new NullPointerException ("'identifiers' cannot be null");
    else {
      _identifiers = identifiers;
    }
  }
  protected ManageableSecurity (final ManageableSecurity source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._uniqueId == null) _uniqueId = null;
    else {
      _uniqueId = source._uniqueId;
    }
    _securityType = source._securityType;
    _name = source._name;
    if (source._identifiers == null) _identifiers = null;
    else {
      _identifiers = source._identifiers;
    }
  }
  public ManageableSecurity clone () {
    return new ManageableSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_uniqueId != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _uniqueId.getClass (), com.opengamma.id.UniqueId.class);
      _uniqueId.toFudgeMsg (fudgeContext, fudge1);
      msg.add (UNIQUE_ID_KEY, null, fudge1);
    }
    if (_name != null)  {
      msg.add (NAME_KEY, null, _name);
    }
    if (_securityType != null)  {
      msg.add (SECURITY_TYPE_KEY, null, _securityType);
    }
    if (_identifiers != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _identifiers.getClass (), com.opengamma.id.IdentifierBundle.class);
      _identifiers.toFudgeMsg (fudgeContext, fudge1);
      msg.add (IDENTIFIERS_KEY, null, fudge1);
    }
  }
  public static ManageableSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.master.security.ManageableSecurity".equals (className)) break;
      try {
        return (com.opengamma.master.security.ManageableSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ManageableSecurity (fudgeContext, fudgeMsg);
  }
  public com.opengamma.id.UniqueId getUniqueId () {
    return _uniqueId;
  }
  public void setUniqueId (com.opengamma.id.UniqueId uniqueId) {
    if (uniqueId == null) _uniqueId = null;
    else {
      _uniqueId = uniqueId;
    }
  }
  public String getName () {
    return _name;
  }
  public void setName (String name) {
    if (name == null) throw new NullPointerException ("name' cannot be null");
    _name = name;
  }
  public String getSecurityType () {
    return _securityType;
  }
  public void setSecurityType (String securityType) {
    if (securityType == null) throw new NullPointerException ("securityType' cannot be null");
    _securityType = securityType;
  }
  public com.opengamma.id.IdentifierBundle getIdentifiers () {
    return _identifiers;
  }
  public void setIdentifiers (com.opengamma.id.IdentifierBundle identifiers) {
    if (identifiers == null) throw new NullPointerException ("'identifiers' cannot be null");
    else {
      _identifiers = identifiers;
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof ManageableSecurity)) return false;
    ManageableSecurity msg = (ManageableSecurity)o;
    if (_uniqueId != null) {
      if (msg._uniqueId != null) {
        if (!_uniqueId.equals (msg._uniqueId)) return false;
      }
      else return false;
    }
    else if (msg._uniqueId != null) return false;
    if (_name != null) {
      if (msg._name != null) {
        if (!_name.equals (msg._name)) return false;
      }
      else return false;
    }
    else if (msg._name != null) return false;
    if (_securityType != null) {
      if (msg._securityType != null) {
        if (!_securityType.equals (msg._securityType)) return false;
      }
      else return false;
    }
    else if (msg._securityType != null) return false;
    if (_identifiers != null) {
      if (msg._identifiers != null) {
        if (!_identifiers.equals (msg._identifiers)) return false;
      }
      else return false;
    }
    else if (msg._identifiers != null) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_uniqueId != null) hc += _uniqueId.hashCode ();
    hc *= 31;
    if (_name != null) hc += _name.hashCode ();
    hc *= 31;
    if (_securityType != null) hc += _securityType.hashCode ();
    hc *= 31;
    if (_identifiers != null) hc += _identifiers.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
