// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com\opengamma\master\msg\Corrected.proto:9(10)
package com.opengamma.master.msg;
public class Corrected extends com.opengamma.master.msg.MasterChangeMessage implements java.io.Serializable {
  public void accept (MasterChangeMessageVisitor visitor) {  visitor.visitCorrectedMessage (this); }
  private static final long serialVersionUID = -42034605915630l;
  private com.opengamma.id.UniqueIdentifier _oldItem;
  public static final String OLD_ITEM_KEY = "oldItem";
  private com.opengamma.id.UniqueIdentifier _newItem;
  public static final String NEW_ITEM_KEY = "newItem";
  public Corrected (com.opengamma.id.UniqueIdentifier oldItem, com.opengamma.id.UniqueIdentifier newItem) {
    if (oldItem == null) throw new NullPointerException ("'oldItem' cannot be null");
    else {
      _oldItem = oldItem;
    }
    if (newItem == null) throw new NullPointerException ("'newItem' cannot be null");
    else {
      _newItem = newItem;
    }
  }
  protected Corrected (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (OLD_ITEM_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Corrected - field 'oldItem' is not present");
    try {
      _oldItem = com.opengamma.id.UniqueIdentifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Corrected - field 'oldItem' is not UniqueIdentifier message", e);
    }
    fudgeField = fudgeMsg.getByName (NEW_ITEM_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Corrected - field 'newItem' is not present");
    try {
      _newItem = com.opengamma.id.UniqueIdentifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Corrected - field 'newItem' is not UniqueIdentifier message", e);
    }
  }
  protected Corrected (final Corrected source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._oldItem == null) _oldItem = null;
    else {
      _oldItem = source._oldItem;
    }
    if (source._newItem == null) _newItem = null;
    else {
      _newItem = source._newItem;
    }
  }
  public Corrected clone () {
    return new Corrected (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_oldItem != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _oldItem.getClass (), com.opengamma.id.UniqueIdentifier.class);
      _oldItem.toFudgeMsg (fudgeContext, fudge1);
      msg.add (OLD_ITEM_KEY, null, fudge1);
    }
    if (_newItem != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _newItem.getClass (), com.opengamma.id.UniqueIdentifier.class);
      _newItem.toFudgeMsg (fudgeContext, fudge1);
      msg.add (NEW_ITEM_KEY, null, fudge1);
    }
  }
  public static Corrected fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.master.msg.Corrected".equals (className)) break;
      try {
        return (com.opengamma.master.msg.Corrected)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Corrected (fudgeMsg);
  }
  public com.opengamma.id.UniqueIdentifier getOldItem () {
    return _oldItem;
  }
  public void setOldItem (com.opengamma.id.UniqueIdentifier oldItem) {
    if (oldItem == null) throw new NullPointerException ("'oldItem' cannot be null");
    else {
      _oldItem = oldItem;
    }
  }
  public com.opengamma.id.UniqueIdentifier getNewItem () {
    return _newItem;
  }
  public void setNewItem (com.opengamma.id.UniqueIdentifier newItem) {
    if (newItem == null) throw new NullPointerException ("'newItem' cannot be null");
    else {
      _newItem = newItem;
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Corrected)) return false;
    Corrected msg = (Corrected)o;
    if (_oldItem != null) {
      if (msg._oldItem != null) {
        if (!_oldItem.equals (msg._oldItem)) return false;
      }
      else return false;
    }
    else if (msg._oldItem != null) return false;
    if (_newItem != null) {
      if (msg._newItem != null) {
        if (!_newItem.equals (msg._newItem)) return false;
      }
      else return false;
    }
    else if (msg._newItem != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_oldItem != null) hc += _oldItem.hashCode ();
    hc *= 31;
    if (_newItem != null) hc += _newItem.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
