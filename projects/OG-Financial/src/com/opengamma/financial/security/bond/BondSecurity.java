// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.bond;
public abstract class BondSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public abstract <T> T accept(BondSecurityVisitor<T> visitor);

        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) {
          return visitor.visitBondSecurity(this);
        }
  private static final long serialVersionUID = 8836932213967444406l;
  private String _issuerName;
  public static final String ISSUER_NAME_KEY = "issuerName";
  private String _issuerType;
  public static final String ISSUER_TYPE_KEY = "issuerType";
  private String _issuerDomicile;
  public static final String ISSUER_DOMICILE_KEY = "issuerDomicile";
  private String _market;
  public static final String MARKET_KEY = "market";
  private com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  private com.opengamma.financial.convention.yield.YieldConvention _yieldConvention;
  public static final String YIELD_CONVENTION_KEY = "yieldConvention";
  private String _guaranteeType;
  public static final String GUARANTEE_TYPE_KEY = "guaranteeType";
  private com.opengamma.util.time.Expiry _lastTradeDate;
  public static final String LAST_TRADE_DATE_KEY = "lastTradeDate";
  private String _couponType;
  public static final String COUPON_TYPE_KEY = "couponType";
  private double _couponRate;
  public static final String COUPON_RATE_KEY = "couponRate";
  private com.opengamma.financial.convention.frequency.Frequency _couponFrequency;
  public static final String COUPON_FREQUENCY_KEY = "couponFrequency";
  private com.opengamma.financial.convention.daycount.DayCount _dayCountConvention;
  public static final String DAY_COUNT_CONVENTION_KEY = "dayCountConvention";
  private com.opengamma.financial.convention.businessday.BusinessDayConvention _businessDayConvention;
  public static final String BUSINESS_DAY_CONVENTION_KEY = "businessDayConvention";
  private javax.time.calendar.ZonedDateTime _announcementDate;
  public static final String ANNOUNCEMENT_DATE_KEY = "announcementDate";
  private javax.time.calendar.ZonedDateTime _interestAccrualDate;
  public static final String INTEREST_ACCRUAL_DATE_KEY = "interestAccrualDate";
  private javax.time.calendar.ZonedDateTime _settlementDate;
  public static final String SETTLEMENT_DATE_KEY = "settlementDate";
  private javax.time.calendar.ZonedDateTime _firstCouponDate;
  public static final String FIRST_COUPON_DATE_KEY = "firstCouponDate";
  private double _issuancePrice;
  public static final String ISSUANCE_PRICE_KEY = "issuancePrice";
  private double _totalAmountIssued;
  public static final String TOTAL_AMOUNT_ISSUED_KEY = "totalAmountIssued";
  private double _minimumAmount;
  public static final String MINIMUM_AMOUNT_KEY = "minimumAmount";
  private double _minimumIncrement;
  public static final String MINIMUM_INCREMENT_KEY = "minimumIncrement";
  private double _parAmount;
  public static final String PAR_AMOUNT_KEY = "parAmount";
  private double _redemptionValue;
  public static final String REDEMPTION_VALUE_KEY = "redemptionValue";
  public static final String SECURITY_TYPE = "BOND";
  public BondSecurity (String issuerName, String issuerType, String issuerDomicile, String market, com.opengamma.util.money.Currency currency, com.opengamma.financial.convention.yield.YieldConvention yieldConvention, com.opengamma.util.time.Expiry lastTradeDate, String couponType, double couponRate, com.opengamma.financial.convention.frequency.Frequency couponFrequency, com.opengamma.financial.convention.daycount.DayCount dayCountConvention, javax.time.calendar.ZonedDateTime interestAccrualDate, javax.time.calendar.ZonedDateTime settlementDate, javax.time.calendar.ZonedDateTime firstCouponDate, double issuancePrice, double totalAmountIssued, double minimumAmount, double minimumIncrement, double parAmount, double redemptionValue) {
    super (SECURITY_TYPE);
    if (issuerName == null) throw new NullPointerException ("issuerName' cannot be null");
    _issuerName = issuerName;
    if (issuerType == null) throw new NullPointerException ("issuerType' cannot be null");
    _issuerType = issuerType;
    if (issuerDomicile == null) throw new NullPointerException ("issuerDomicile' cannot be null");
    _issuerDomicile = issuerDomicile;
    if (market == null) throw new NullPointerException ("market' cannot be null");
    _market = market;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    if (yieldConvention == null) throw new NullPointerException ("yieldConvention' cannot be null");
    _yieldConvention = yieldConvention;
    if (lastTradeDate == null) throw new NullPointerException ("'lastTradeDate' cannot be null");
    else {
      _lastTradeDate = lastTradeDate;
    }
    if (couponType == null) throw new NullPointerException ("couponType' cannot be null");
    _couponType = couponType;
    _couponRate = couponRate;
    if (couponFrequency == null) throw new NullPointerException ("couponFrequency' cannot be null");
    _couponFrequency = couponFrequency;
    if (dayCountConvention == null) throw new NullPointerException ("dayCountConvention' cannot be null");
    _dayCountConvention = dayCountConvention;
    if (interestAccrualDate == null) throw new NullPointerException ("'interestAccrualDate' cannot be null");
    else {
      _interestAccrualDate = interestAccrualDate;
    }
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = settlementDate;
    }
    if (firstCouponDate == null) throw new NullPointerException ("'firstCouponDate' cannot be null");
    else {
      _firstCouponDate = firstCouponDate;
    }
    _issuancePrice = issuancePrice;
    _totalAmountIssued = totalAmountIssued;
    _minimumAmount = minimumAmount;
    _minimumIncrement = minimumIncrement;
    _parAmount = parAmount;
    _redemptionValue = redemptionValue;
  }
  protected BondSecurity (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (ISSUER_NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'issuerName' is not present");
    try {
      _issuerName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'issuerName' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (ISSUER_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'issuerType' is not present");
    try {
      _issuerType = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'issuerType' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (ISSUER_DOMICILE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'issuerDomicile' is not present");
    try {
      _issuerDomicile = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'issuerDomicile' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (MARKET_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'market' is not present");
    try {
      _market = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'market' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'currency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (YIELD_CONVENTION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'yieldConvention' is not present");
    try {
      _yieldConvention = fudgeMsg.getFieldValue (com.opengamma.financial.convention.yield.YieldConvention.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'yieldConvention' is not YieldConvention typedef", e);
    }
    fudgeField = fudgeMsg.getByName (LAST_TRADE_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'lastTradeDate' is not present");
    try {
      _lastTradeDate = com.opengamma.util.time.Expiry.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'lastTradeDate' is not Expiry message", e);
    }
    fudgeField = fudgeMsg.getByName (COUPON_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'couponType' is not present");
    try {
      _couponType = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'couponType' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (COUPON_RATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'couponRate' is not present");
    try {
      _couponRate = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'couponRate' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (COUPON_FREQUENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'couponFrequency' is not present");
    try {
      _couponFrequency = fudgeMsg.getFieldValue (com.opengamma.financial.convention.frequency.Frequency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'couponFrequency' is not Frequency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (DAY_COUNT_CONVENTION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'dayCountConvention' is not present");
    try {
      _dayCountConvention = fudgeMsg.getFieldValue (com.opengamma.financial.convention.daycount.DayCount.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'dayCountConvention' is not DayCount typedef", e);
    }
    fudgeField = fudgeMsg.getByName (INTEREST_ACCRUAL_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'interestAccrualDate' is not present");
    try {
      _interestAccrualDate = deserializer.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'interestAccrualDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (SETTLEMENT_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'settlementDate' is not present");
    try {
      _settlementDate = deserializer.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'settlementDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (FIRST_COUPON_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'firstCouponDate' is not present");
    try {
      _firstCouponDate = deserializer.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'firstCouponDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (ISSUANCE_PRICE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'issuancePrice' is not present");
    try {
      _issuancePrice = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'issuancePrice' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (TOTAL_AMOUNT_ISSUED_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'totalAmountIssued' is not present");
    try {
      _totalAmountIssued = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'totalAmountIssued' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (MINIMUM_AMOUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'minimumAmount' is not present");
    try {
      _minimumAmount = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'minimumAmount' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (MINIMUM_INCREMENT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'minimumIncrement' is not present");
    try {
      _minimumIncrement = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'minimumIncrement' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (PAR_AMOUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'parAmount' is not present");
    try {
      _parAmount = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'parAmount' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (REDEMPTION_VALUE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'redemptionValue' is not present");
    try {
      _redemptionValue = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'redemptionValue' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (GUARANTEE_TYPE_KEY);
    if (fudgeField != null)  {
      try {
        setGuaranteeType ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'guaranteeType' is not string", e);
      }
    }
    fudgeField = fudgeMsg.getByName (BUSINESS_DAY_CONVENTION_KEY);
    if (fudgeField != null)  {
      try {
        setBusinessDayConvention (fudgeMsg.getFieldValue (com.opengamma.financial.convention.businessday.BusinessDayConvention.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'businessDayConvention' is not BusinessDayConvention typedef", e);
      }
    }
    fudgeField = fudgeMsg.getByName (ANNOUNCEMENT_DATE_KEY);
    if (fudgeField != null)  {
      try {
        setAnnouncementDate (deserializer.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a BondSecurity - field 'announcementDate' is not ZonedDateTime typedef", e);
      }
    }
  }
  public BondSecurity (com.opengamma.id.UniqueId uniqueId, String name, String securityType, com.opengamma.id.ExternalIdBundle identifiers, String issuerName, String issuerType, String issuerDomicile, String market, com.opengamma.util.money.Currency currency, com.opengamma.financial.convention.yield.YieldConvention yieldConvention, String guaranteeType, com.opengamma.util.time.Expiry lastTradeDate, String couponType, double couponRate, com.opengamma.financial.convention.frequency.Frequency couponFrequency, com.opengamma.financial.convention.daycount.DayCount dayCountConvention, com.opengamma.financial.convention.businessday.BusinessDayConvention businessDayConvention, javax.time.calendar.ZonedDateTime announcementDate, javax.time.calendar.ZonedDateTime interestAccrualDate, javax.time.calendar.ZonedDateTime settlementDate, javax.time.calendar.ZonedDateTime firstCouponDate, double issuancePrice, double totalAmountIssued, double minimumAmount, double minimumIncrement, double parAmount, double redemptionValue) {
    super (uniqueId, name, securityType, identifiers);
    if (issuerName == null) throw new NullPointerException ("issuerName' cannot be null");
    _issuerName = issuerName;
    if (issuerType == null) throw new NullPointerException ("issuerType' cannot be null");
    _issuerType = issuerType;
    if (issuerDomicile == null) throw new NullPointerException ("issuerDomicile' cannot be null");
    _issuerDomicile = issuerDomicile;
    if (market == null) throw new NullPointerException ("market' cannot be null");
    _market = market;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    if (yieldConvention == null) throw new NullPointerException ("yieldConvention' cannot be null");
    _yieldConvention = yieldConvention;
    _guaranteeType = guaranteeType;
    if (lastTradeDate == null) throw new NullPointerException ("'lastTradeDate' cannot be null");
    else {
      _lastTradeDate = lastTradeDate;
    }
    if (couponType == null) throw new NullPointerException ("couponType' cannot be null");
    _couponType = couponType;
    _couponRate = couponRate;
    if (couponFrequency == null) throw new NullPointerException ("couponFrequency' cannot be null");
    _couponFrequency = couponFrequency;
    if (dayCountConvention == null) throw new NullPointerException ("dayCountConvention' cannot be null");
    _dayCountConvention = dayCountConvention;
    _businessDayConvention = businessDayConvention;
    if (announcementDate == null) _announcementDate = null;
    else {
      _announcementDate = announcementDate;
    }
    if (interestAccrualDate == null) throw new NullPointerException ("'interestAccrualDate' cannot be null");
    else {
      _interestAccrualDate = interestAccrualDate;
    }
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = settlementDate;
    }
    if (firstCouponDate == null) throw new NullPointerException ("'firstCouponDate' cannot be null");
    else {
      _firstCouponDate = firstCouponDate;
    }
    _issuancePrice = issuancePrice;
    _totalAmountIssued = totalAmountIssued;
    _minimumAmount = minimumAmount;
    _minimumIncrement = minimumIncrement;
    _parAmount = parAmount;
    _redemptionValue = redemptionValue;
  }
  protected BondSecurity (final BondSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _issuerName = source._issuerName;
    _issuerType = source._issuerType;
    _issuerDomicile = source._issuerDomicile;
    _market = source._market;
    _currency = source._currency;
    _yieldConvention = source._yieldConvention;
    _guaranteeType = source._guaranteeType;
    if (source._lastTradeDate == null) _lastTradeDate = null;
    else {
      _lastTradeDate = source._lastTradeDate;
    }
    _couponType = source._couponType;
    _couponRate = source._couponRate;
    _couponFrequency = source._couponFrequency;
    _dayCountConvention = source._dayCountConvention;
    _businessDayConvention = source._businessDayConvention;
    if (source._announcementDate == null) _announcementDate = null;
    else {
      _announcementDate = source._announcementDate;
    }
    if (source._interestAccrualDate == null) _interestAccrualDate = null;
    else {
      _interestAccrualDate = source._interestAccrualDate;
    }
    if (source._settlementDate == null) _settlementDate = null;
    else {
      _settlementDate = source._settlementDate;
    }
    if (source._firstCouponDate == null) _firstCouponDate = null;
    else {
      _firstCouponDate = source._firstCouponDate;
    }
    _issuancePrice = source._issuancePrice;
    _totalAmountIssued = source._totalAmountIssued;
    _minimumAmount = source._minimumAmount;
    _minimumIncrement = source._minimumIncrement;
    _parAmount = source._parAmount;
    _redemptionValue = source._redemptionValue;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_issuerName != null)  {
      msg.add (ISSUER_NAME_KEY, null, _issuerName);
    }
    if (_issuerType != null)  {
      msg.add (ISSUER_TYPE_KEY, null, _issuerType);
    }
    if (_issuerDomicile != null)  {
      msg.add (ISSUER_DOMICILE_KEY, null, _issuerDomicile);
    }
    if (_market != null)  {
      msg.add (MARKET_KEY, null, _market);
    }
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
    if (_yieldConvention != null)  {
      msg.add (YIELD_CONVENTION_KEY, null, _yieldConvention);
    }
    if (_guaranteeType != null)  {
      msg.add (GUARANTEE_TYPE_KEY, null, _guaranteeType);
    }
    if (_lastTradeDate != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _lastTradeDate.getClass (), com.opengamma.util.time.Expiry.class);
      _lastTradeDate.toFudgeMsg (serializer, fudge1);
      msg.add (LAST_TRADE_DATE_KEY, null, fudge1);
    }
    if (_couponType != null)  {
      msg.add (COUPON_TYPE_KEY, null, _couponType);
    }
    msg.add (COUPON_RATE_KEY, null, _couponRate);
    if (_couponFrequency != null)  {
      msg.add (COUPON_FREQUENCY_KEY, null, _couponFrequency);
    }
    if (_dayCountConvention != null)  {
      msg.add (DAY_COUNT_CONVENTION_KEY, null, _dayCountConvention);
    }
    if (_businessDayConvention != null)  {
      msg.add (BUSINESS_DAY_CONVENTION_KEY, null, _businessDayConvention);
    }
    if (_announcementDate != null)  {
      serializer.addToMessage (msg, ANNOUNCEMENT_DATE_KEY, null, _announcementDate);
    }
    if (_interestAccrualDate != null)  {
      serializer.addToMessage (msg, INTEREST_ACCRUAL_DATE_KEY, null, _interestAccrualDate);
    }
    if (_settlementDate != null)  {
      serializer.addToMessage (msg, SETTLEMENT_DATE_KEY, null, _settlementDate);
    }
    if (_firstCouponDate != null)  {
      serializer.addToMessage (msg, FIRST_COUPON_DATE_KEY, null, _firstCouponDate);
    }
    msg.add (ISSUANCE_PRICE_KEY, null, _issuancePrice);
    msg.add (TOTAL_AMOUNT_ISSUED_KEY, null, _totalAmountIssued);
    msg.add (MINIMUM_AMOUNT_KEY, null, _minimumAmount);
    msg.add (MINIMUM_INCREMENT_KEY, null, _minimumIncrement);
    msg.add (PAR_AMOUNT_KEY, null, _parAmount);
    msg.add (REDEMPTION_VALUE_KEY, null, _redemptionValue);
  }
  public static BondSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.bond.BondSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.bond.BondSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    throw new UnsupportedOperationException ("BondSecurity is an abstract message");
  }
  public String getIssuerName () {
    return _issuerName;
  }
  public void setIssuerName (String issuerName) {
    if (issuerName == null) throw new NullPointerException ("issuerName' cannot be null");
    _issuerName = issuerName;
  }
  public String getIssuerType () {
    return _issuerType;
  }
  public void setIssuerType (String issuerType) {
    if (issuerType == null) throw new NullPointerException ("issuerType' cannot be null");
    _issuerType = issuerType;
  }
  public String getIssuerDomicile () {
    return _issuerDomicile;
  }
  public void setIssuerDomicile (String issuerDomicile) {
    if (issuerDomicile == null) throw new NullPointerException ("issuerDomicile' cannot be null");
    _issuerDomicile = issuerDomicile;
  }
  public String getMarket () {
    return _market;
  }
  public void setMarket (String market) {
    if (market == null) throw new NullPointerException ("market' cannot be null");
    _market = market;
  }
  public com.opengamma.util.money.Currency getCurrency () {
    return _currency;
  }
  public void setCurrency (com.opengamma.util.money.Currency currency) {
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  public com.opengamma.financial.convention.yield.YieldConvention getYieldConvention () {
    return _yieldConvention;
  }
  public void setYieldConvention (com.opengamma.financial.convention.yield.YieldConvention yieldConvention) {
    if (yieldConvention == null) throw new NullPointerException ("yieldConvention' cannot be null");
    _yieldConvention = yieldConvention;
  }
  public String getGuaranteeType () {
    return _guaranteeType;
  }
  public void setGuaranteeType (String guaranteeType) {
    _guaranteeType = guaranteeType;
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
  public String getCouponType () {
    return _couponType;
  }
  public void setCouponType (String couponType) {
    if (couponType == null) throw new NullPointerException ("couponType' cannot be null");
    _couponType = couponType;
  }
  public double getCouponRate () {
    return _couponRate;
  }
  public void setCouponRate (double couponRate) {
    _couponRate = couponRate;
  }
  public com.opengamma.financial.convention.frequency.Frequency getCouponFrequency () {
    return _couponFrequency;
  }
  public void setCouponFrequency (com.opengamma.financial.convention.frequency.Frequency couponFrequency) {
    if (couponFrequency == null) throw new NullPointerException ("couponFrequency' cannot be null");
    _couponFrequency = couponFrequency;
  }
  public com.opengamma.financial.convention.daycount.DayCount getDayCountConvention () {
    return _dayCountConvention;
  }
  public void setDayCountConvention (com.opengamma.financial.convention.daycount.DayCount dayCountConvention) {
    if (dayCountConvention == null) throw new NullPointerException ("dayCountConvention' cannot be null");
    _dayCountConvention = dayCountConvention;
  }
  public com.opengamma.financial.convention.businessday.BusinessDayConvention getBusinessDayConvention () {
    return _businessDayConvention;
  }
  public void setBusinessDayConvention (com.opengamma.financial.convention.businessday.BusinessDayConvention businessDayConvention) {
    _businessDayConvention = businessDayConvention;
  }
  public javax.time.calendar.ZonedDateTime getAnnouncementDate () {
    return _announcementDate;
  }
  public void setAnnouncementDate (javax.time.calendar.ZonedDateTime announcementDate) {
    if (announcementDate == null) _announcementDate = null;
    else {
      _announcementDate = announcementDate;
    }
  }
  public javax.time.calendar.ZonedDateTime getInterestAccrualDate () {
    return _interestAccrualDate;
  }
  public void setInterestAccrualDate (javax.time.calendar.ZonedDateTime interestAccrualDate) {
    if (interestAccrualDate == null) throw new NullPointerException ("'interestAccrualDate' cannot be null");
    else {
      _interestAccrualDate = interestAccrualDate;
    }
  }
  public javax.time.calendar.ZonedDateTime getSettlementDate () {
    return _settlementDate;
  }
  public void setSettlementDate (javax.time.calendar.ZonedDateTime settlementDate) {
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = settlementDate;
    }
  }
  public javax.time.calendar.ZonedDateTime getFirstCouponDate () {
    return _firstCouponDate;
  }
  public void setFirstCouponDate (javax.time.calendar.ZonedDateTime firstCouponDate) {
    if (firstCouponDate == null) throw new NullPointerException ("'firstCouponDate' cannot be null");
    else {
      _firstCouponDate = firstCouponDate;
    }
  }
  public double getIssuancePrice () {
    return _issuancePrice;
  }
  public void setIssuancePrice (double issuancePrice) {
    _issuancePrice = issuancePrice;
  }
  public double getTotalAmountIssued () {
    return _totalAmountIssued;
  }
  public void setTotalAmountIssued (double totalAmountIssued) {
    _totalAmountIssued = totalAmountIssued;
  }
  public double getMinimumAmount () {
    return _minimumAmount;
  }
  public void setMinimumAmount (double minimumAmount) {
    _minimumAmount = minimumAmount;
  }
  public double getMinimumIncrement () {
    return _minimumIncrement;
  }
  public void setMinimumIncrement (double minimumIncrement) {
    _minimumIncrement = minimumIncrement;
  }
  public double getParAmount () {
    return _parAmount;
  }
  public void setParAmount (double parAmount) {
    _parAmount = parAmount;
  }
  public double getRedemptionValue () {
    return _redemptionValue;
  }
  public void setRedemptionValue (double redemptionValue) {
    _redemptionValue = redemptionValue;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof BondSecurity)) return false;
    BondSecurity msg = (BondSecurity)o;
    if (_issuerName != null) {
      if (msg._issuerName != null) {
        if (!_issuerName.equals (msg._issuerName)) return false;
      }
      else return false;
    }
    else if (msg._issuerName != null) return false;
    if (_issuerType != null) {
      if (msg._issuerType != null) {
        if (!_issuerType.equals (msg._issuerType)) return false;
      }
      else return false;
    }
    else if (msg._issuerType != null) return false;
    if (_issuerDomicile != null) {
      if (msg._issuerDomicile != null) {
        if (!_issuerDomicile.equals (msg._issuerDomicile)) return false;
      }
      else return false;
    }
    else if (msg._issuerDomicile != null) return false;
    if (_market != null) {
      if (msg._market != null) {
        if (!_market.equals (msg._market)) return false;
      }
      else return false;
    }
    else if (msg._market != null) return false;
    if (_currency != null) {
      if (msg._currency != null) {
        if (!_currency.equals (msg._currency)) return false;
      }
      else return false;
    }
    else if (msg._currency != null) return false;
    if (_yieldConvention != null) {
      if (msg._yieldConvention != null) {
        if (!_yieldConvention.equals (msg._yieldConvention)) return false;
      }
      else return false;
    }
    else if (msg._yieldConvention != null) return false;
    if (_guaranteeType != null) {
      if (msg._guaranteeType != null) {
        if (!_guaranteeType.equals (msg._guaranteeType)) return false;
      }
      else return false;
    }
    else if (msg._guaranteeType != null) return false;
    if (_lastTradeDate != null) {
      if (msg._lastTradeDate != null) {
        if (!_lastTradeDate.equals (msg._lastTradeDate)) return false;
      }
      else return false;
    }
    else if (msg._lastTradeDate != null) return false;
    if (_couponType != null) {
      if (msg._couponType != null) {
        if (!_couponType.equals (msg._couponType)) return false;
      }
      else return false;
    }
    else if (msg._couponType != null) return false;
    if (_couponRate != msg._couponRate) return false;
    if (_couponFrequency != null) {
      if (msg._couponFrequency != null) {
        if (!_couponFrequency.equals (msg._couponFrequency)) return false;
      }
      else return false;
    }
    else if (msg._couponFrequency != null) return false;
    if (_dayCountConvention != null) {
      if (msg._dayCountConvention != null) {
        if (!_dayCountConvention.equals (msg._dayCountConvention)) return false;
      }
      else return false;
    }
    else if (msg._dayCountConvention != null) return false;
    if (_businessDayConvention != null) {
      if (msg._businessDayConvention != null) {
        if (!_businessDayConvention.equals (msg._businessDayConvention)) return false;
      }
      else return false;
    }
    else if (msg._businessDayConvention != null) return false;
    if (_announcementDate != null) {
      if (msg._announcementDate != null) {
        if (!_announcementDate.equals (msg._announcementDate)) return false;
      }
      else return false;
    }
    else if (msg._announcementDate != null) return false;
    if (_interestAccrualDate != null) {
      if (msg._interestAccrualDate != null) {
        if (!_interestAccrualDate.equals (msg._interestAccrualDate)) return false;
      }
      else return false;
    }
    else if (msg._interestAccrualDate != null) return false;
    if (_settlementDate != null) {
      if (msg._settlementDate != null) {
        if (!_settlementDate.equals (msg._settlementDate)) return false;
      }
      else return false;
    }
    else if (msg._settlementDate != null) return false;
    if (_firstCouponDate != null) {
      if (msg._firstCouponDate != null) {
        if (!_firstCouponDate.equals (msg._firstCouponDate)) return false;
      }
      else return false;
    }
    else if (msg._firstCouponDate != null) return false;
    if (_issuancePrice != msg._issuancePrice) return false;
    if (_totalAmountIssued != msg._totalAmountIssued) return false;
    if (_minimumAmount != msg._minimumAmount) return false;
    if (_minimumIncrement != msg._minimumIncrement) return false;
    if (_parAmount != msg._parAmount) return false;
    if (_redemptionValue != msg._redemptionValue) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_issuerName != null) hc += _issuerName.hashCode ();
    hc *= 31;
    if (_issuerType != null) hc += _issuerType.hashCode ();
    hc *= 31;
    if (_issuerDomicile != null) hc += _issuerDomicile.hashCode ();
    hc *= 31;
    if (_market != null) hc += _market.hashCode ();
    hc *= 31;
    if (_currency != null) hc += _currency.hashCode ();
    hc *= 31;
    if (_yieldConvention != null) hc += _yieldConvention.hashCode ();
    hc *= 31;
    if (_guaranteeType != null) hc += _guaranteeType.hashCode ();
    hc *= 31;
    if (_lastTradeDate != null) hc += _lastTradeDate.hashCode ();
    hc *= 31;
    if (_couponType != null) hc += _couponType.hashCode ();
    hc = (hc * 31) + (int)_couponRate;
    hc *= 31;
    if (_couponFrequency != null) hc += _couponFrequency.hashCode ();
    hc *= 31;
    if (_dayCountConvention != null) hc += _dayCountConvention.hashCode ();
    hc *= 31;
    if (_businessDayConvention != null) hc += _businessDayConvention.hashCode ();
    hc *= 31;
    if (_announcementDate != null) hc += _announcementDate.hashCode ();
    hc *= 31;
    if (_interestAccrualDate != null) hc += _interestAccrualDate.hashCode ();
    hc *= 31;
    if (_settlementDate != null) hc += _settlementDate.hashCode ();
    hc *= 31;
    if (_firstCouponDate != null) hc += _firstCouponDate.hashCode ();
    hc = (hc * 31) + (int)_issuancePrice;
    hc = (hc * 31) + (int)_totalAmountIssued;
    hc = (hc * 31) + (int)_minimumAmount;
    hc = (hc * 31) + (int)_minimumIncrement;
    hc = (hc * 31) + (int)_parAmount;
    hc = (hc * 31) + (int)_redemptionValue;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
