// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.calcnode.msg;
public class Ready extends com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage implements java.io.Serializable {
  public void accept (RemoteCalcNodeMessageVisitor visitor) { visitor.visitReadyMessage (this); }
  private static final long serialVersionUID = -2059821179804l;
  private int _capacity;
  public static final String CAPACITY_KEY = "capacity";
  private String _hostId;
  public static final String HOST_ID_KEY = "hostId";
  public Ready (int capacity, String hostId) {
    _capacity = capacity;
    if (hostId == null) throw new NullPointerException ("hostId' cannot be null");
    _hostId = hostId;
  }
  protected Ready (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (CAPACITY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Ready - field 'capacity' is not present");
    try {
      _capacity = fudgeMsg.getFieldValue (Integer.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Ready - field 'capacity' is not integer", e);
    }
    fudgeField = fudgeMsg.getByName (HOST_ID_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Ready - field 'hostId' is not present");
    try {
      _hostId = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Ready - field 'hostId' is not string", e);
    }
  }
  protected Ready (final Ready source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _capacity = source._capacity;
    _hostId = source._hostId;
  }
  public Ready clone () {
    return new Ready (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    msg.add (CAPACITY_KEY, null, _capacity);
    if (_hostId != null)  {
      msg.add (HOST_ID_KEY, null, _hostId);
    }
  }
  public static Ready fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.calcnode.msg.Ready".equals (className)) break;
      try {
        return (com.opengamma.engine.calcnode.msg.Ready)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Ready (deserializer, fudgeMsg);
  }
  public int getCapacity () {
    return _capacity;
  }
  public void setCapacity (int capacity) {
    _capacity = capacity;
  }
  public String getHostId () {
    return _hostId;
  }
  public void setHostId (String hostId) {
    if (hostId == null) throw new NullPointerException ("hostId' cannot be null");
    _hostId = hostId;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
