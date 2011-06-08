// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.future;
public abstract class CommodityFutureSecurity extends com.opengamma.financial.security.future.FutureSecurity implements java.io.Serializable {
  private static final long serialVersionUID = 40741415860027857l;
  private String _commodityType;
  public static final String COMMODITY_TYPE_KEY = "commodityType";
  private Double _unitNumber;
  public static final String UNIT_NUMBER_KEY = "unitNumber";
  private String _unitName;
  public static final String UNIT_NAME_KEY = "unitName";
  public CommodityFutureSecurity (com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, String commodityType) {
    super (expiry, tradingExchange, settlementExchange, currency);
    if (commodityType == null) throw new NullPointerException ("commodityType' cannot be null");
    _commodityType = commodityType;
  }
  protected CommodityFutureSecurity (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (COMMODITY_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CommodityFutureSecurity - field 'commodityType' is not present");
    try {
      _commodityType = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CommodityFutureSecurity - field 'commodityType' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (UNIT_NUMBER_KEY);
    if (fudgeField != null)  {
      try {
        setUnitNumber (fudgeMsg.getFieldValue (Double.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a CommodityFutureSecurity - field 'unitNumber' is not double", e);
      }
    }
    fudgeField = fudgeMsg.getByName (UNIT_NAME_KEY);
    if (fudgeField != null)  {
      try {
        setUnitName ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a CommodityFutureSecurity - field 'unitName' is not string", e);
      }
    }
  }
  public CommodityFutureSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, String commodityType, Double unitNumber, String unitName) {
    super (uniqueId, name, securityType, identifiers, expiry, tradingExchange, settlementExchange, currency);
    if (commodityType == null) throw new NullPointerException ("commodityType' cannot be null");
    _commodityType = commodityType;
    _unitNumber = unitNumber;
    _unitName = unitName;
  }
  protected CommodityFutureSecurity (final CommodityFutureSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _commodityType = source._commodityType;
    _unitNumber = source._unitNumber;
    _unitName = source._unitName;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_commodityType != null)  {
      msg.add (COMMODITY_TYPE_KEY, null, _commodityType);
    }
    if (_unitNumber != null)  {
      msg.add (UNIT_NUMBER_KEY, null, _unitNumber);
    }
    if (_unitName != null)  {
      msg.add (UNIT_NAME_KEY, null, _unitName);
    }
  }
  public static CommodityFutureSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.future.CommodityFutureSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.future.CommodityFutureSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    throw new UnsupportedOperationException ("CommodityFutureSecurity is an abstract message");
  }
  public String getCommodityType () {
    return _commodityType;
  }
  public void setCommodityType (String commodityType) {
    if (commodityType == null) throw new NullPointerException ("commodityType' cannot be null");
    _commodityType = commodityType;
  }
  public Double getUnitNumber () {
    return _unitNumber;
  }
  public void setUnitNumber (Double unitNumber) {
    _unitNumber = unitNumber;
  }
  public String getUnitName () {
    return _unitName;
  }
  public void setUnitName (String unitName) {
    _unitName = unitName;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof CommodityFutureSecurity)) return false;
    CommodityFutureSecurity msg = (CommodityFutureSecurity)o;
    if (_commodityType != null) {
      if (msg._commodityType != null) {
        if (!_commodityType.equals (msg._commodityType)) return false;
      }
      else return false;
    }
    else if (msg._commodityType != null) return false;
    if (_unitNumber != null) {
      if (msg._unitNumber != null) {
        if (!_unitNumber.equals (msg._unitNumber)) return false;
      }
      else return false;
    }
    else if (msg._unitNumber != null) return false;
    if (_unitName != null) {
      if (msg._unitName != null) {
        if (!_unitName.equals (msg._unitName)) return false;
      }
      else return false;
    }
    else if (msg._unitName != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_commodityType != null) hc += _commodityType.hashCode ();
    hc *= 31;
    if (_unitNumber != null) hc += _unitNumber.hashCode ();
    hc *= 31;
    if (_unitName != null) hc += _unitName.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
