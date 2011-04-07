// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/engine/view/cache/msg/SlaveChannelMessage.proto:15(10)
package com.opengamma.engine.view.cache.msg;
public class SlaveChannelMessage extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitSlaveChannelMessage (this); }
  private static final long serialVersionUID = 1l;
  public SlaveChannelMessage () {
  }
  protected SlaveChannelMessage (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
  }
  public SlaveChannelMessage (Long correlationId) {
    super (correlationId);
  }
  protected SlaveChannelMessage (final SlaveChannelMessage source) {
    super (source);
  }
  public SlaveChannelMessage clone () {
    return new SlaveChannelMessage (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
  }
  public static SlaveChannelMessage fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.SlaveChannelMessage".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.SlaveChannelMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new SlaveChannelMessage (fudgeMsg);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
