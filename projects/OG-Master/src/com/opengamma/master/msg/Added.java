// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com\opengamma\master\msg\Added.proto:9(10)
package com.opengamma.master.msg;
public class Added extends com.opengamma.master.msg.MasterChangeMessage implements java.io.Serializable {
  public void accept (MasterChangeMessageVisitor visitor) {  visitor.visitAddedMessage (this); }
  private static final long serialVersionUID = 14478217821l;
  private com.opengamma.id.UniqueIdentifier _addedItem;
  public static final String ADDED_ITEM_KEY = "addedItem";
  public Added (com.opengamma.id.UniqueIdentifier addedItem) {
    if (addedItem == null) throw new NullPointerException ("'addedItem' cannot be null");
    else {
      _addedItem = addedItem;
    }
  }
  protected Added (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (ADDED_ITEM_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Added - field 'addedItem' is not present");
    try {
      _addedItem = com.opengamma.id.UniqueIdentifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Added - field 'addedItem' is not UniqueIdentifier message", e);
    }
  }
  protected Added (final Added source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._addedItem == null) _addedItem = null;
    else {
      _addedItem = source._addedItem;
    }
  }
  public Added clone () {
    return new Added (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_addedItem != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _addedItem.getClass (), com.opengamma.id.UniqueIdentifier.class);
      _addedItem.toFudgeMsg (fudgeContext, fudge1);
      msg.add (ADDED_ITEM_KEY, null, fudge1);
    }
  }
  public static Added fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.master.msg.Added".equals (className)) break;
      try {
        return (com.opengamma.master.msg.Added)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Added (fudgeMsg);
  }
  public com.opengamma.id.UniqueIdentifier getAddedItem () {
    return _addedItem;
  }
  public void setAddedItem (com.opengamma.id.UniqueIdentifier addedItem) {
    if (addedItem == null) throw new NullPointerException ("'addedItem' cannot be null");
    else {
      _addedItem = addedItem;
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Added)) return false;
    Added msg = (Added)o;
    if (_addedItem != null) {
      if (msg._addedItem != null) {
        if (!_addedItem.equals (msg._addedItem)) return false;
      }
      else return false;
    }
    else if (msg._addedItem != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_addedItem != null) hc += _addedItem.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
