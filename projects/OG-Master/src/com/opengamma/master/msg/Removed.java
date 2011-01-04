// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/master/msg/Removed.proto:9(10)
package com.opengamma.master.msg;
public class Removed extends com.opengamma.master.msg.MasterChangeMessage implements java.io.Serializable {
  public void accept (MasterChangeMessageVisitor visitor) {  visitor.visitRemovedMessage (this); }
  private static final long serialVersionUID = -10024028419l;
  private com.opengamma.id.UniqueIdentifier _removedItem;
  public static final String REMOVED_ITEM_KEY = "removedItem";
  public Removed (com.opengamma.id.UniqueIdentifier removedItem) {
    if (removedItem == null) throw new NullPointerException ("'removedItem' cannot be null");
    else {
      _removedItem = removedItem;
    }
  }
  protected Removed (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (REMOVED_ITEM_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Removed - field 'removedItem' is not present");
    try {
      _removedItem = com.opengamma.id.UniqueIdentifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Removed - field 'removedItem' is not UniqueIdentifier message", e);
    }
  }
  protected Removed (final Removed source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._removedItem == null) _removedItem = null;
    else {
      _removedItem = source._removedItem;
    }
  }
  public Removed clone () {
    return new Removed (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_removedItem != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _removedItem.getClass (), com.opengamma.id.UniqueIdentifier.class);
      _removedItem.toFudgeMsg (fudgeContext, fudge1);
      msg.add (REMOVED_ITEM_KEY, null, fudge1);
    }
  }
  public static Removed fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.master.msg.Removed".equals (className)) break;
      try {
        return (com.opengamma.master.msg.Removed)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Removed (fudgeMsg);
  }
  public com.opengamma.id.UniqueIdentifier getRemovedItem () {
    return _removedItem;
  }
  public void setRemovedItem (com.opengamma.id.UniqueIdentifier removedItem) {
    if (removedItem == null) throw new NullPointerException ("'removedItem' cannot be null");
    else {
      _removedItem = removedItem;
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Removed)) return false;
    Removed msg = (Removed)o;
    if (_removedItem != null) {
      if (msg._removedItem != null) {
        if (!_removedItem.equals (msg._removedItem)) return false;
      }
      else return false;
    }
    else if (msg._removedItem != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_removedItem != null) hc += _removedItem.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
