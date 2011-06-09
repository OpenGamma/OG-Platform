// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.future;
public class BondFutureSecurity extends com.opengamma.financial.security.future.FutureSecurity implements java.io.Serializable {
  public <T> T accept (FutureSecurityVisitor<T> visitor) { return visitor.visitBondFutureSecurity (this); }
  private static final long serialVersionUID = -1295014613220177536l;
  private java.util.List<com.opengamma.financial.security.future.BondFutureDeliverable> _basket;
  public static final String BASKET_KEY = "basket";
  private String _bondType;
  public static final String BOND_TYPE_KEY = "bondType";
  private com.opengamma.financial.security.DateTimeWithZone _firstDeliveryDate;
  public static final String FIRST_DELIVERY_DATE_KEY = "firstDeliveryDate";
  private com.opengamma.financial.security.DateTimeWithZone _lastDeliveryDate;
  public static final String LAST_DELIVERY_DATE_KEY = "lastDeliveryDate";
  public BondFutureSecurity (com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, double unitAmount, java.util.Collection<? extends com.opengamma.financial.security.future.BondFutureDeliverable> basket, String bondType, com.opengamma.financial.security.DateTimeWithZone firstDeliveryDate, com.opengamma.financial.security.DateTimeWithZone lastDeliveryDate) {
    super (expiry, tradingExchange, settlementExchange, currency, unitAmount);
    if (basket == null) throw new NullPointerException ("'basket' cannot be null");
    else {
      final java.util.List<com.opengamma.financial.security.future.BondFutureDeliverable> fudge0 = new java.util.ArrayList<com.opengamma.financial.security.future.BondFutureDeliverable> (basket);
      if (basket.size () == 0) throw new IllegalArgumentException ("'basket' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.financial.security.future.BondFutureDeliverable> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.financial.security.future.BondFutureDeliverable fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'basket' cannot be null");
        fudge1.set ((com.opengamma.financial.security.future.BondFutureDeliverable)fudge2.clone ());
      }
      _basket = fudge0;
    }
    if (bondType == null) throw new NullPointerException ("bondType' cannot be null");
    _bondType = bondType;
    if (firstDeliveryDate == null) throw new NullPointerException ("'firstDeliveryDate' cannot be null");
    else {
      _firstDeliveryDate = (com.opengamma.financial.security.DateTimeWithZone)firstDeliveryDate.clone ();
    }
    if (lastDeliveryDate == null) throw new NullPointerException ("'lastDeliveryDate' cannot be null");
    else {
      _lastDeliveryDate = (com.opengamma.financial.security.DateTimeWithZone)lastDeliveryDate.clone ();
    }
  }
  protected BondFutureSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    org.fudgemsg.FudgeField fudgeField;
    fudgeFields = fudgeMsg.getAllByName (BASKET_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a BondFutureSecurity - field 'basket' is not present");
    _basket = new java.util.ArrayList<com.opengamma.financial.security.future.BondFutureDeliverable> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.financial.security.future.BondFutureDeliverable fudge2;
        fudge2 = com.opengamma.financial.security.future.BondFutureDeliverable.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge1));
        _basket.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a BondFutureSecurity - field 'basket' is not BondFutureDeliverable message", e);
      }
    }
    fudgeField = fudgeMsg.getByName (BOND_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondFutureSecurity - field 'bondType' is not present");
    try {
      _bondType = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondFutureSecurity - field 'bondType' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (FIRST_DELIVERY_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondFutureSecurity - field 'firstDeliveryDate' is not present");
    try {
      _firstDeliveryDate = com.opengamma.financial.security.DateTimeWithZone.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondFutureSecurity - field 'firstDeliveryDate' is not DateTimeWithZone message", e);
    }
    fudgeField = fudgeMsg.getByName (LAST_DELIVERY_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondFutureSecurity - field 'lastDeliveryDate' is not present");
    try {
      _lastDeliveryDate = com.opengamma.financial.security.DateTimeWithZone.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondFutureSecurity - field 'lastDeliveryDate' is not DateTimeWithZone message", e);
    }
  }
  public BondFutureSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, double unitAmount, java.util.Collection<? extends com.opengamma.financial.security.future.BondFutureDeliverable> basket, String bondType, com.opengamma.financial.security.DateTimeWithZone firstDeliveryDate, com.opengamma.financial.security.DateTimeWithZone lastDeliveryDate) {
    super (uniqueId, name, securityType, identifiers, expiry, tradingExchange, settlementExchange, currency, unitAmount);
    if (basket == null) throw new NullPointerException ("'basket' cannot be null");
    else {
      final java.util.List<com.opengamma.financial.security.future.BondFutureDeliverable> fudge0 = new java.util.ArrayList<com.opengamma.financial.security.future.BondFutureDeliverable> (basket);
      if (basket.size () == 0) throw new IllegalArgumentException ("'basket' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.financial.security.future.BondFutureDeliverable> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.financial.security.future.BondFutureDeliverable fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'basket' cannot be null");
        fudge1.set ((com.opengamma.financial.security.future.BondFutureDeliverable)fudge2.clone ());
      }
      _basket = fudge0;
    }
    if (bondType == null) throw new NullPointerException ("bondType' cannot be null");
    _bondType = bondType;
    if (firstDeliveryDate == null) throw new NullPointerException ("'firstDeliveryDate' cannot be null");
    else {
      _firstDeliveryDate = (com.opengamma.financial.security.DateTimeWithZone)firstDeliveryDate.clone ();
    }
    if (lastDeliveryDate == null) throw new NullPointerException ("'lastDeliveryDate' cannot be null");
    else {
      _lastDeliveryDate = (com.opengamma.financial.security.DateTimeWithZone)lastDeliveryDate.clone ();
    }
  }
  protected BondFutureSecurity (final BondFutureSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._basket == null) _basket = null;
    else {
      final java.util.List<com.opengamma.financial.security.future.BondFutureDeliverable> fudge0 = new java.util.ArrayList<com.opengamma.financial.security.future.BondFutureDeliverable> (source._basket);
      for (java.util.ListIterator<com.opengamma.financial.security.future.BondFutureDeliverable> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.financial.security.future.BondFutureDeliverable fudge2 = fudge1.next ();
        fudge1.set ((com.opengamma.financial.security.future.BondFutureDeliverable)fudge2.clone ());
      }
      _basket = fudge0;
    }
    _bondType = source._bondType;
    if (source._firstDeliveryDate == null) _firstDeliveryDate = null;
    else {
      _firstDeliveryDate = (com.opengamma.financial.security.DateTimeWithZone)source._firstDeliveryDate.clone ();
    }
    if (source._lastDeliveryDate == null) _lastDeliveryDate = null;
    else {
      _lastDeliveryDate = (com.opengamma.financial.security.DateTimeWithZone)source._lastDeliveryDate.clone ();
    }
  }
  public BondFutureSecurity clone () {
    return new BondFutureSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_basket != null)  {
      for (com.opengamma.financial.security.future.BondFutureDeliverable fudge1 : _basket) {
        final org.fudgemsg.MutableFudgeMsg fudge2 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), fudge1.getClass (), com.opengamma.financial.security.future.BondFutureDeliverable.class);
        fudge1.toFudgeMsg (fudgeContext, fudge2);
        msg.add (BASKET_KEY, null, fudge2);
      }
    }
    if (_bondType != null)  {
      msg.add (BOND_TYPE_KEY, null, _bondType);
    }
    if (_firstDeliveryDate != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _firstDeliveryDate.getClass (), com.opengamma.financial.security.DateTimeWithZone.class);
      _firstDeliveryDate.toFudgeMsg (fudgeContext, fudge1);
      msg.add (FIRST_DELIVERY_DATE_KEY, null, fudge1);
    }
    if (_lastDeliveryDate != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _lastDeliveryDate.getClass (), com.opengamma.financial.security.DateTimeWithZone.class);
      _lastDeliveryDate.toFudgeMsg (fudgeContext, fudge1);
      msg.add (LAST_DELIVERY_DATE_KEY, null, fudge1);
    }
  }
  public static BondFutureSecurity fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.future.BondFutureSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.future.BondFutureSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new BondFutureSecurity (fudgeMsg);
  }
  public java.util.List<com.opengamma.financial.security.future.BondFutureDeliverable> getBasket () {
    return java.util.Collections.unmodifiableList (_basket);
  }
  public void setBasket (com.opengamma.financial.security.future.BondFutureDeliverable basket) {
    if (basket == null) throw new NullPointerException ("'basket' cannot be null");
    else {
      _basket = new java.util.ArrayList<com.opengamma.financial.security.future.BondFutureDeliverable> (1);
      addBasket (basket);
    }
  }
  public void setBasket (java.util.Collection<? extends com.opengamma.financial.security.future.BondFutureDeliverable> basket) {
    if (basket == null) throw new NullPointerException ("'basket' cannot be null");
    else {
      final java.util.List<com.opengamma.financial.security.future.BondFutureDeliverable> fudge0 = new java.util.ArrayList<com.opengamma.financial.security.future.BondFutureDeliverable> (basket);
      if (basket.size () == 0) throw new IllegalArgumentException ("'basket' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.financial.security.future.BondFutureDeliverable> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.financial.security.future.BondFutureDeliverable fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'basket' cannot be null");
        fudge1.set ((com.opengamma.financial.security.future.BondFutureDeliverable)fudge2.clone ());
      }
      _basket = fudge0;
    }
  }
  public void addBasket (com.opengamma.financial.security.future.BondFutureDeliverable basket) {
    if (basket == null) throw new NullPointerException ("'basket' cannot be null");
    if (_basket == null) _basket = new java.util.ArrayList<com.opengamma.financial.security.future.BondFutureDeliverable> ();
    _basket.add ((com.opengamma.financial.security.future.BondFutureDeliverable)basket.clone ());
  }
  public String getBondType () {
    return _bondType;
  }
  public void setBondType (String bondType) {
    if (bondType == null) throw new NullPointerException ("bondType' cannot be null");
    _bondType = bondType;
  }
  public com.opengamma.financial.security.DateTimeWithZone getFirstDeliveryDate () {
    return _firstDeliveryDate;
  }
  public void setFirstDeliveryDate (com.opengamma.financial.security.DateTimeWithZone firstDeliveryDate) {
    if (firstDeliveryDate == null) throw new NullPointerException ("'firstDeliveryDate' cannot be null");
    else {
      _firstDeliveryDate = (com.opengamma.financial.security.DateTimeWithZone)firstDeliveryDate.clone ();
    }
  }
  public com.opengamma.financial.security.DateTimeWithZone getLastDeliveryDate () {
    return _lastDeliveryDate;
  }
  public void setLastDeliveryDate (com.opengamma.financial.security.DateTimeWithZone lastDeliveryDate) {
    if (lastDeliveryDate == null) throw new NullPointerException ("'lastDeliveryDate' cannot be null");
    else {
      _lastDeliveryDate = (com.opengamma.financial.security.DateTimeWithZone)lastDeliveryDate.clone ();
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof BondFutureSecurity)) return false;
    BondFutureSecurity msg = (BondFutureSecurity)o;
    if (_basket != null) {
      if (msg._basket != null) {
        if (!_basket.equals (msg._basket)) return false;
      }
      else return false;
    }
    else if (msg._basket != null) return false;
    if (_bondType != null) {
      if (msg._bondType != null) {
        if (!_bondType.equals (msg._bondType)) return false;
      }
      else return false;
    }
    else if (msg._bondType != null) return false;
    if (_firstDeliveryDate != null) {
      if (msg._firstDeliveryDate != null) {
        if (!_firstDeliveryDate.equals (msg._firstDeliveryDate)) return false;
      }
      else return false;
    }
    else if (msg._firstDeliveryDate != null) return false;
    if (_lastDeliveryDate != null) {
      if (msg._lastDeliveryDate != null) {
        if (!_lastDeliveryDate.equals (msg._lastDeliveryDate)) return false;
      }
      else return false;
    }
    else if (msg._lastDeliveryDate != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_basket != null) hc += _basket.hashCode ();
    hc *= 31;
    if (_bondType != null) hc += _bondType.hashCode ();
    hc *= 31;
    if (_firstDeliveryDate != null) hc += _firstDeliveryDate.hashCode ();
    hc *= 31;
    if (_lastDeliveryDate != null) hc += _lastDeliveryDate.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
