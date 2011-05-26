// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.future;
public abstract class EquityFutureSecurity extends com.opengamma.financial.security.future.FutureSecurity implements java.io.Serializable {
  private static final long serialVersionUID = -8459661175054373781l;
  private String _UnderlyingType;
  public static final String UNDERLYING_TYPE_KEY = "UnderlyingType";
  private String _SettlementType;
  public static final String SETTLEMENT_TYPE_KEY = "SettlementType";
  private double _TickSize;
  public static final String TICK_SIZE_KEY = "TickSize";
  private double _CcyPerPt;
  public static final String CCY_PER_PT_KEY = "CcyPerPt";
  private Double _unitNumber;
  public static final String UNIT_NUMBER_KEY = "unitNumber";
  private String _unitName;
  public static final String UNIT_NAME_KEY = "unitName";
  public EquityFutureSecurity (com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, String UnderlyingType, String SettlementType, double TickSize, double CcyPerPt) {
    super (expiry, tradingExchange, settlementExchange, currency);
    if (UnderlyingType == null) throw new NullPointerException ("UnderlyingType' cannot be null");
    _UnderlyingType = UnderlyingType;
    if (SettlementType == null) throw new NullPointerException ("SettlementType' cannot be null");
    _SettlementType = SettlementType;
    _TickSize = TickSize;
    _CcyPerPt = CcyPerPt;
  }
  protected EquityFutureSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (UNDERLYING_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'UnderlyingType' is not present");
    try {
      _UnderlyingType = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'UnderlyingType' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (SETTLEMENT_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'SettlementType' is not present");
    try {
      _SettlementType = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'SettlementType' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (TICK_SIZE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'TickSize' is not present");
    try {
      _TickSize = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'TickSize' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (CCY_PER_PT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'CcyPerPt' is not present");
    try {
      _CcyPerPt = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'CcyPerPt' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (UNIT_NUMBER_KEY);
    if (fudgeField != null)  {
      try {
        setUnitNumber (fudgeMsg.getFieldValue (Double.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'unitNumber' is not double", e);
      }
    }
    fudgeField = fudgeMsg.getByName (UNIT_NAME_KEY);
    if (fudgeField != null)  {
      try {
        setUnitName ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'unitName' is not string", e);
      }
    }
  }
  public EquityFutureSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, String UnderlyingType, String SettlementType, double TickSize, double CcyPerPt, Double unitNumber, String unitName) {
    super (uniqueId, name, securityType, identifiers, expiry, tradingExchange, settlementExchange, currency);
    if (UnderlyingType == null) throw new NullPointerException ("UnderlyingType' cannot be null");
    _UnderlyingType = UnderlyingType;
    if (SettlementType == null) throw new NullPointerException ("SettlementType' cannot be null");
    _SettlementType = SettlementType;
    _TickSize = TickSize;
    _CcyPerPt = CcyPerPt;
    _unitNumber = unitNumber;
    _unitName = unitName;
  }
  protected EquityFutureSecurity (final EquityFutureSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _UnderlyingType = source._UnderlyingType;
    _SettlementType = source._SettlementType;
    _TickSize = source._TickSize;
    _CcyPerPt = source._CcyPerPt;
    _unitNumber = source._unitNumber;
    _unitName = source._unitName;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_UnderlyingType != null)  {
      msg.add (UNDERLYING_TYPE_KEY, null, _UnderlyingType);
    }
    if (_SettlementType != null)  {
      msg.add (SETTLEMENT_TYPE_KEY, null, _SettlementType);
    }
    msg.add (TICK_SIZE_KEY, null, _TickSize);
    msg.add (CCY_PER_PT_KEY, null, _CcyPerPt);
    if (_unitNumber != null)  {
      msg.add (UNIT_NUMBER_KEY, null, _unitNumber);
    }
    if (_unitName != null)  {
      msg.add (UNIT_NAME_KEY, null, _unitName);
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
    throw new UnsupportedOperationException ("EquityFutureSecurity is an abstract message");
  }
  public String getUnderlyingType () {
    return _UnderlyingType;
  }
  public void setUnderlyingType (String UnderlyingType) {
    if (UnderlyingType == null) throw new NullPointerException ("UnderlyingType' cannot be null");
    _UnderlyingType = UnderlyingType;
  }
  public String getSettlementType () {
    return _SettlementType;
  }
  public void setSettlementType (String SettlementType) {
    if (SettlementType == null) throw new NullPointerException ("SettlementType' cannot be null");
    _SettlementType = SettlementType;
  }
  public double getTickSize () {
    return _TickSize;
  }
  public void setTickSize (double TickSize) {
    _TickSize = TickSize;
  }
  public double getCcyPerPt () {
    return _CcyPerPt;
  }
  public void setCcyPerPt (double CcyPerPt) {
    _CcyPerPt = CcyPerPt;
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
    if (!(o instanceof EquityFutureSecurity)) return false;
    EquityFutureSecurity msg = (EquityFutureSecurity)o;
    if (_UnderlyingType != null) {
      if (msg._UnderlyingType != null) {
        if (!_UnderlyingType.equals (msg._UnderlyingType)) return false;
      }
      else return false;
    }
    else if (msg._UnderlyingType != null) return false;
    if (_SettlementType != null) {
      if (msg._SettlementType != null) {
        if (!_SettlementType.equals (msg._SettlementType)) return false;
      }
      else return false;
    }
    else if (msg._SettlementType != null) return false;
    if (_TickSize != msg._TickSize) return false;
    if (_CcyPerPt != msg._CcyPerPt) return false;
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
    if (_UnderlyingType != null) hc += _UnderlyingType.hashCode ();
    hc *= 31;
    if (_SettlementType != null) hc += _SettlementType.hashCode ();
    hc = (hc * 31) + (int)_TickSize;
    hc = (hc * 31) + (int)_CcyPerPt;
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
