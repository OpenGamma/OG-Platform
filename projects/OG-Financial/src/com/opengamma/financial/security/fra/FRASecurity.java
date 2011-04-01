// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.fra;
public class FRASecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public <T> T accept(FRASecurityVisitor<T> visitor) { return visitor.visitFRASecurity(this); }
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitFRASecurity(this); }
  private static final long serialVersionUID = 3599266378640254495l;
  private com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  private com.opengamma.id.Identifier _region;
  public static final String REGION_KEY = "region";
  private com.opengamma.financial.security.DateTimeWithZone _startDate;
  public static final String START_DATE_KEY = "startDate";
  private com.opengamma.financial.security.DateTimeWithZone _endDate;
  public static final String END_DATE_KEY = "endDate";
  private double _rate;
  public static final String RATE_KEY = "rate";
  private double _amount;
  public static final String AMOUNT_KEY = "amount";
  public static final String SECURITY_TYPE = "FRA";
  public FRASecurity (com.opengamma.util.money.Currency currency, com.opengamma.id.Identifier region, com.opengamma.financial.security.DateTimeWithZone startDate, com.opengamma.financial.security.DateTimeWithZone endDate, double rate, double amount) {
    super (SECURITY_TYPE);
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
    if (startDate == null) throw new NullPointerException ("'startDate' cannot be null");
    else {
      _startDate = (com.opengamma.financial.security.DateTimeWithZone)startDate.clone ();
    }
    if (endDate == null) throw new NullPointerException ("'endDate' cannot be null");
    else {
      _endDate = (com.opengamma.financial.security.DateTimeWithZone)endDate.clone ();
    }
    _rate = rate;
    _amount = amount;
  }
  protected FRASecurity (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'currency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (REGION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'region' is not present");
    try {
      _region = com.opengamma.id.Identifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'region' is not Identifier message", e);
    }
    fudgeField = fudgeMsg.getByName (START_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'startDate' is not present");
    try {
      _startDate = com.opengamma.financial.security.DateTimeWithZone.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'startDate' is not DateTimeWithZone message", e);
    }
    fudgeField = fudgeMsg.getByName (END_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'endDate' is not present");
    try {
      _endDate = com.opengamma.financial.security.DateTimeWithZone.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'endDate' is not DateTimeWithZone message", e);
    }
    fudgeField = fudgeMsg.getByName (RATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'rate' is not present");
    try {
      _rate = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'rate' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (AMOUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'amount' is not present");
    try {
      _amount = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'amount' is not double", e);
    }
  }
  public FRASecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.util.money.Currency currency, com.opengamma.id.Identifier region, com.opengamma.financial.security.DateTimeWithZone startDate, com.opengamma.financial.security.DateTimeWithZone endDate, double rate, double amount) {
    super (uniqueId, name, securityType, identifiers);
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
    if (startDate == null) throw new NullPointerException ("'startDate' cannot be null");
    else {
      _startDate = (com.opengamma.financial.security.DateTimeWithZone)startDate.clone ();
    }
    if (endDate == null) throw new NullPointerException ("'endDate' cannot be null");
    else {
      _endDate = (com.opengamma.financial.security.DateTimeWithZone)endDate.clone ();
    }
    _rate = rate;
    _amount = amount;
  }
  protected FRASecurity (final FRASecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _currency = source._currency;
    if (source._region == null) _region = null;
    else {
      _region = source._region;
    }
    if (source._startDate == null) _startDate = null;
    else {
      _startDate = (com.opengamma.financial.security.DateTimeWithZone)source._startDate.clone ();
    }
    if (source._endDate == null) _endDate = null;
    else {
      _endDate = (com.opengamma.financial.security.DateTimeWithZone)source._endDate.clone ();
    }
    _rate = source._rate;
    _amount = source._amount;
  }
  public FRASecurity clone () {
    return new FRASecurity (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
    if (_region != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _region.getClass (), com.opengamma.id.Identifier.class);
      _region.toFudgeMsg (fudgeContext, fudge1);
      msg.add (REGION_KEY, null, fudge1);
    }
    if (_startDate != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _startDate.getClass (), com.opengamma.financial.security.DateTimeWithZone.class);
      _startDate.toFudgeMsg (fudgeContext, fudge1);
      msg.add (START_DATE_KEY, null, fudge1);
    }
    if (_endDate != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _endDate.getClass (), com.opengamma.financial.security.DateTimeWithZone.class);
      _endDate.toFudgeMsg (fudgeContext, fudge1);
      msg.add (END_DATE_KEY, null, fudge1);
    }
    msg.add (RATE_KEY, null, _rate);
    msg.add (AMOUNT_KEY, null, _amount);
  }
  public static FRASecurity fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.fra.FRASecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.fra.FRASecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FRASecurity (fudgeMsg);
  }
  public com.opengamma.util.money.Currency getCurrency () {
    return _currency;
  }
  public void setCurrency (com.opengamma.util.money.Currency currency) {
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  public com.opengamma.id.Identifier getRegion () {
    return _region;
  }
  public void setRegion (com.opengamma.id.Identifier region) {
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
  }
  public com.opengamma.financial.security.DateTimeWithZone getStartDate () {
    return _startDate;
  }
  public void setStartDate (com.opengamma.financial.security.DateTimeWithZone startDate) {
    if (startDate == null) throw new NullPointerException ("'startDate' cannot be null");
    else {
      _startDate = (com.opengamma.financial.security.DateTimeWithZone)startDate.clone ();
    }
  }
  public com.opengamma.financial.security.DateTimeWithZone getEndDate () {
    return _endDate;
  }
  public void setEndDate (com.opengamma.financial.security.DateTimeWithZone endDate) {
    if (endDate == null) throw new NullPointerException ("'endDate' cannot be null");
    else {
      _endDate = (com.opengamma.financial.security.DateTimeWithZone)endDate.clone ();
    }
  }
  public double getRate () {
    return _rate;
  }
  public void setRate (double rate) {
    _rate = rate;
  }
  public double getAmount () {
    return _amount;
  }
  public void setAmount (double amount) {
    _amount = amount;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FRASecurity)) return false;
    FRASecurity msg = (FRASecurity)o;
    if (_currency != null) {
      if (msg._currency != null) {
        if (!_currency.equals (msg._currency)) return false;
      }
      else return false;
    }
    else if (msg._currency != null) return false;
    if (_region != null) {
      if (msg._region != null) {
        if (!_region.equals (msg._region)) return false;
      }
      else return false;
    }
    else if (msg._region != null) return false;
    if (_startDate != null) {
      if (msg._startDate != null) {
        if (!_startDate.equals (msg._startDate)) return false;
      }
      else return false;
    }
    else if (msg._startDate != null) return false;
    if (_endDate != null) {
      if (msg._endDate != null) {
        if (!_endDate.equals (msg._endDate)) return false;
      }
      else return false;
    }
    else if (msg._endDate != null) return false;
    if (_rate != msg._rate) return false;
    if (_amount != msg._amount) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_currency != null) hc += _currency.hashCode ();
    hc *= 31;
    if (_region != null) hc += _region.hashCode ();
    hc *= 31;
    if (_startDate != null) hc += _startDate.hashCode ();
    hc *= 31;
    if (_endDate != null) hc += _endDate.hashCode ();
    hc = (hc * 31) + (int)_rate;
    hc = (hc * 31) + (int)_amount;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
