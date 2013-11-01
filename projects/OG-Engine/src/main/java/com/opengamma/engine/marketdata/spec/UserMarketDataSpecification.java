// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.marketdata.spec;

import com.opengamma.id.UniqueId;

public class UserMarketDataSpecification extends com.opengamma.engine.marketdata.spec.MarketDataSpecification implements java.io.Serializable {
  private static final long serialVersionUID = -12218260552l;
  private com.opengamma.id.UniqueId _userSnapshotId;
  public static final String USER_SNAPSHOT_ID_KEY = "userSnapshotId";
  public UserMarketDataSpecification (com.opengamma.id.UniqueId userSnapshotId) {
    if (userSnapshotId == null) throw new NullPointerException ("'userSnapshotId' cannot be null");
    else {
      _userSnapshotId = userSnapshotId;
    }
  }
  protected UserMarketDataSpecification (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (USER_SNAPSHOT_ID_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a UserMarketDataSpecification - field 'userSnapshotId' is not present");
    if (fudgeField.getType().getJavaType().equals(String.class)) {
      _userSnapshotId = UniqueId.parse(deserializer.fieldValueToObject(String.class, fudgeField));
    } else {
      try {
        _userSnapshotId = com.opengamma.id.UniqueId.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a UserMarketDataSpecification - field 'userSnapshotId' is not UniqueId message", e);
      }
    }
  }
  protected UserMarketDataSpecification (final UserMarketDataSpecification source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._userSnapshotId == null) _userSnapshotId = null;
    else {
      _userSnapshotId = source._userSnapshotId;
    }
  }
  public UserMarketDataSpecification clone () {
    return new UserMarketDataSpecification (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_userSnapshotId != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _userSnapshotId.getClass (), com.opengamma.id.UniqueId.class);
      _userSnapshotId.toFudgeMsg (serializer, fudge1);
      msg.add (USER_SNAPSHOT_ID_KEY, null, fudge1);
    }
  }
  public static UserMarketDataSpecification fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.marketdata.spec.UserMarketDataSpecification".equals (className)) break;
      try {
        return (com.opengamma.engine.marketdata.spec.UserMarketDataSpecification)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new UserMarketDataSpecification (deserializer, fudgeMsg);
  }
  public com.opengamma.id.UniqueId getUserSnapshotId () {
    return _userSnapshotId;
  }
  public void setUserSnapshotId (com.opengamma.id.UniqueId userSnapshotId) {
    if (userSnapshotId == null) throw new NullPointerException ("'userSnapshotId' cannot be null");
    else {
      _userSnapshotId = userSnapshotId;
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof UserMarketDataSpecification)) return false;
    UserMarketDataSpecification msg = (UserMarketDataSpecification)o;
    if (_userSnapshotId != null) {
      if (msg._userSnapshotId != null) {
        if (!_userSnapshotId.equals (msg._userSnapshotId)) return false;
      }
      else return false;
    }
    else if (msg._userSnapshotId != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_userSnapshotId != null) hc += _userSnapshotId.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
