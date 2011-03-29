// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security;
public class DateTimeWithZone implements java.io.Serializable {
          public javax.time.calendar.ZonedDateTime toZonedDateTime () {
          return getDate ().toLocalDateTime ().atZone (javax.time.calendar.TimeZone.of (getZone ()));
        }
        public static DateTimeWithZone fromZonedDateTime (javax.time.calendar.ZonedDateTime zonedDateTime) {
          return new DateTimeWithZone (zonedDateTime, zonedDateTime.getZone ().getID ());
        }
  private static final long serialVersionUID = 1807845140555l;
  private javax.time.calendar.LocalDateTime _date;
  public static final String DATE_KEY = "date";
  private String _zone;
  public static final String ZONE_KEY = "zone";
  public static final String ZONE = "UTC";
  public DateTimeWithZone (javax.time.calendar.DateTimeProvider date) {
    if (date == null) throw new NullPointerException ("'date' cannot be null");
    else {
      _date = date.toLocalDateTime ();
    }
    setZone (ZONE);
  }
  protected DateTimeWithZone (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a DateTimeWithZone - field 'date' is not present");
    try {
      _date = fudgeMsg.getFieldValue (javax.time.calendar.DateTimeProvider.class, fudgeField).toLocalDateTime ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a DateTimeWithZone - field 'date' is not datetime", e);
    }
    fudgeField = fudgeMsg.getByName (ZONE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a DateTimeWithZone - field 'zone' is not present");
    try {
      _zone = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a DateTimeWithZone - field 'zone' is not string", e);
    }
  }
  public DateTimeWithZone (javax.time.calendar.DateTimeProvider date, String zone) {
    if (date == null) throw new NullPointerException ("'date' cannot be null");
    else {
      _date = date.toLocalDateTime ();
    }
    if (zone == null) throw new NullPointerException ("zone' cannot be null");
    _zone = zone;
  }
  protected DateTimeWithZone (final DateTimeWithZone source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._date == null) _date = null;
    else {
      _date = source._date.toLocalDateTime ();
    }
    _zone = source._zone;
  }
  public DateTimeWithZone clone () {
    return new DateTimeWithZone (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    if (_date != null)  {
      msg.add (DATE_KEY, null, _date);
    }
    if (_zone != null)  {
      msg.add (ZONE_KEY, null, _zone);
    }
  }
  public static DateTimeWithZone fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.DateTimeWithZone".equals (className)) break;
      try {
        return (com.opengamma.financial.security.DateTimeWithZone)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new DateTimeWithZone (fudgeMsg);
  }
  public javax.time.calendar.LocalDateTime getDate () {
    return _date;
  }
  public void setDate (javax.time.calendar.DateTimeProvider date) {
    if (date == null) throw new NullPointerException ("'date' cannot be null");
    else {
      _date = date.toLocalDateTime ();
    }
  }
  public String getZone () {
    return _zone;
  }
  public void setZone (String zone) {
    if (zone == null) throw new NullPointerException ("zone' cannot be null");
    _zone = zone;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof DateTimeWithZone)) return false;
    DateTimeWithZone msg = (DateTimeWithZone)o;
    if (_date != null) {
      if (msg._date != null) {
        if (!_date.equals (msg._date)) return false;
      }
      else return false;
    }
    else if (msg._date != null) return false;
    if (_zone != null) {
      if (msg._zone != null) {
        if (!_zone.equals (msg._zone)) return false;
      }
      else return false;
    }
    else if (msg._zone != null) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_date != null) hc += _date.hashCode ();
    hc *= 31;
    if (_zone != null) hc += _zone.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
