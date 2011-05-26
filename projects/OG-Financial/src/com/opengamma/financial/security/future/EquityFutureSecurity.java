// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.future;
public class EquityFutureSecurity extends com.opengamma.financial.security.future.FutureSecurity implements java.io.Serializable {
  public <T> T accept (FutureSecurityVisitor<T> visitor) { return visitor.visitEquityFutureSecurity (this); }
  private static final long serialVersionUID = 6236526185413771507l;
  private com.opengamma.util.time.Expiry _lastTradeDate;
  public static final String LAST_TRADE_DATE_KEY = "lastTradeDate";
  private com.opengamma.financial.security.DateTimeWithZone _settlementDate;
  public static final String SETTLEMENT_DATE_KEY = "settlementDate";
  private com.opengamma.id.Identifier _assetIdentifier;
  public static final String ASSET_IDENTIFIER_KEY = "assetIdentifier";
  private com.opengamma.id.Identifier _underlyingIdentifier;
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";
  private String _underlyingType;
  public static final String UNDERLYING_TYPE_KEY = "underlyingType";
  private Double _pointValue;
  public static final String POINT_VALUE_KEY = "pointValue";
  public EquityFutureSecurity (com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, com.opengamma.util.time.Expiry lastTradeDate, com.opengamma.financial.security.DateTimeWithZone settlementDate, com.opengamma.id.Identifier assetIdentifier) {
    super (expiry, tradingExchange, settlementExchange, currency);
    if (lastTradeDate == null) throw new NullPointerException ("'lastTradeDate' cannot be null");
    else {
      _lastTradeDate = lastTradeDate;
    }
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = (com.opengamma.financial.security.DateTimeWithZone)settlementDate.clone ();
    }
    if (assetIdentifier == null) throw new NullPointerException ("'assetIdentifier' cannot be null");
    else {
      _assetIdentifier = assetIdentifier;
    }
  }
  protected EquityFutureSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (LAST_TRADE_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'lastTradeDate' is not present");
    try {
      _lastTradeDate = com.opengamma.util.time.Expiry.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'lastTradeDate' is not Expiry message", e);
    }
    fudgeField = fudgeMsg.getByName (SETTLEMENT_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'settlementDate' is not present");
    try {
      _settlementDate = com.opengamma.financial.security.DateTimeWithZone.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'settlementDate' is not DateTimeWithZone message", e);
    }
    fudgeField = fudgeMsg.getByName (ASSET_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'assetIdentifier' is not present");
    try {
      _assetIdentifier = com.opengamma.id.Identifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'assetIdentifier' is not Identifier message", e);
    }
    fudgeField = fudgeMsg.getByName (UNDERLYING_IDENTIFIER_KEY);
    if (fudgeField != null)  {
      try {
        final com.opengamma.id.Identifier fudge1;
        fudge1 = com.opengamma.id.Identifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
        setUnderlyingIdentifier (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'underlyingIdentifier' is not Identifier message", e);
      }
    }
    fudgeField = fudgeMsg.getByName (UNDERLYING_TYPE_KEY);
    if (fudgeField != null)  {
      try {
        setUnderlyingType ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'underlyingType' is not string", e);
      }
    }
    fudgeField = fudgeMsg.getByName (POINT_VALUE_KEY);
    if (fudgeField != null)  {
      try {
        setPointValue (fudgeMsg.getFieldValue (Double.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'pointValue' is not double", e);
      }
    }
  }
  public EquityFutureSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, com.opengamma.util.time.Expiry lastTradeDate, com.opengamma.financial.security.DateTimeWithZone settlementDate, com.opengamma.id.Identifier assetIdentifier, com.opengamma.id.Identifier underlyingIdentifier, String underlyingType, Double pointValue) {
    super (uniqueId, name, securityType, identifiers, expiry, tradingExchange, settlementExchange, currency);
    if (lastTradeDate == null) throw new NullPointerException ("'lastTradeDate' cannot be null");
    else {
      _lastTradeDate = lastTradeDate;
    }
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = (com.opengamma.financial.security.DateTimeWithZone)settlementDate.clone ();
    }
    if (assetIdentifier == null) throw new NullPointerException ("'assetIdentifier' cannot be null");
    else {
      _assetIdentifier = assetIdentifier;
    }
    if (underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    _underlyingType = underlyingType;
    _pointValue = pointValue;
  }
  protected EquityFutureSecurity (final EquityFutureSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._lastTradeDate == null) _lastTradeDate = null;
    else {
      _lastTradeDate = source._lastTradeDate;
    }
    if (source._settlementDate == null) _settlementDate = null;
    else {
      _settlementDate = (com.opengamma.financial.security.DateTimeWithZone)source._settlementDate.clone ();
    }
    if (source._assetIdentifier == null) _assetIdentifier = null;
    else {
      _assetIdentifier = source._assetIdentifier;
    }
    if (source._underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = source._underlyingIdentifier;
    }
    _underlyingType = source._underlyingType;
    _pointValue = source._pointValue;
  }
  public EquityFutureSecurity clone () {
    return new EquityFutureSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_lastTradeDate != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _lastTradeDate.getClass (), com.opengamma.util.time.Expiry.class);
      _lastTradeDate.toFudgeMsg (fudgeContext, fudge1);
      msg.add (LAST_TRADE_DATE_KEY, null, fudge1);
    }
    if (_settlementDate != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _settlementDate.getClass (), com.opengamma.financial.security.DateTimeWithZone.class);
      _settlementDate.toFudgeMsg (fudgeContext, fudge1);
      msg.add (SETTLEMENT_DATE_KEY, null, fudge1);
    }
    if (_assetIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _assetIdentifier.getClass (), com.opengamma.id.Identifier.class);
      _assetIdentifier.toFudgeMsg (fudgeContext, fudge1);
      msg.add (ASSET_IDENTIFIER_KEY, null, fudge1);
    }
    if (_underlyingIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _underlyingIdentifier.getClass (), com.opengamma.id.Identifier.class);
      _underlyingIdentifier.toFudgeMsg (fudgeContext, fudge1);
      msg.add (UNDERLYING_IDENTIFIER_KEY, null, fudge1);
    }
    if (_underlyingType != null)  {
      msg.add (UNDERLYING_TYPE_KEY, null, _underlyingType);
    }
    if (_pointValue != null)  {
      msg.add (POINT_VALUE_KEY, null, _pointValue);
    }
  }
  public static EquityFutureSecurity fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.future.EquityFutureSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.future.EquityFutureSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new EquityFutureSecurity (fudgeMsg);
  }
  public com.opengamma.util.time.Expiry getLastTradeDate () {
    return _lastTradeDate;
  }
  public void setLastTradeDate (com.opengamma.util.time.Expiry lastTradeDate) {
    if (lastTradeDate == null) throw new NullPointerException ("'lastTradeDate' cannot be null");
    else {
      _lastTradeDate = lastTradeDate;
    }
  }
  public com.opengamma.financial.security.DateTimeWithZone getSettlementDate () {
    return _settlementDate;
  }
  public void setSettlementDate (com.opengamma.financial.security.DateTimeWithZone settlementDate) {
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = (com.opengamma.financial.security.DateTimeWithZone)settlementDate.clone ();
    }
  }
  public com.opengamma.id.Identifier getAssetIdentifier () {
    return _assetIdentifier;
  }
  public void setAssetIdentifier (com.opengamma.id.Identifier assetIdentifier) {
    if (assetIdentifier == null) throw new NullPointerException ("'assetIdentifier' cannot be null");
    else {
      _assetIdentifier = assetIdentifier;
    }
  }
  public com.opengamma.id.Identifier getUnderlyingIdentifier () {
    return _underlyingIdentifier;
  }
  public void setUnderlyingIdentifier (com.opengamma.id.Identifier underlyingIdentifier) {
    if (underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
  }
  public String getUnderlyingType () {
    return _underlyingType;
  }
  public void setUnderlyingType (String underlyingType) {
    _underlyingType = underlyingType;
  }
  public Double getPointValue () {
    return _pointValue;
  }
  public void setPointValue (Double pointValue) {
    _pointValue = pointValue;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof EquityFutureSecurity)) return false;
    EquityFutureSecurity msg = (EquityFutureSecurity)o;
    if (_lastTradeDate != null) {
      if (msg._lastTradeDate != null) {
        if (!_lastTradeDate.equals (msg._lastTradeDate)) return false;
      }
      else return false;
    }
    else if (msg._lastTradeDate != null) return false;
    if (_settlementDate != null) {
      if (msg._settlementDate != null) {
        if (!_settlementDate.equals (msg._settlementDate)) return false;
      }
      else return false;
    }
    else if (msg._settlementDate != null) return false;
    if (_assetIdentifier != null) {
      if (msg._assetIdentifier != null) {
        if (!_assetIdentifier.equals (msg._assetIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._assetIdentifier != null) return false;
    if (_underlyingIdentifier != null) {
      if (msg._underlyingIdentifier != null) {
        if (!_underlyingIdentifier.equals (msg._underlyingIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._underlyingIdentifier != null) return false;
    if (_underlyingType != null) {
      if (msg._underlyingType != null) {
        if (!_underlyingType.equals (msg._underlyingType)) return false;
      }
      else return false;
    }
    else if (msg._underlyingType != null) return false;
    if (_pointValue != null) {
      if (msg._pointValue != null) {
        if (!_pointValue.equals (msg._pointValue)) return false;
      }
      else return false;
    }
    else if (msg._pointValue != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_lastTradeDate != null) hc += _lastTradeDate.hashCode ();
    hc *= 31;
    if (_settlementDate != null) hc += _settlementDate.hashCode ();
    hc *= 31;
    if (_assetIdentifier != null) hc += _assetIdentifier.hashCode ();
    hc *= 31;
    if (_underlyingIdentifier != null) hc += _underlyingIdentifier.hashCode ();
    hc *= 31;
    if (_underlyingType != null) hc += _underlyingType.hashCode ();
    hc *= 31;
    if (_pointValue != null) hc += _pointValue.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
