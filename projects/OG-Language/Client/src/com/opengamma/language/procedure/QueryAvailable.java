// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/language/procedure/QueryAvailable.proto:12(10)
package com.opengamma.language.procedure;
public class QueryAvailable extends com.opengamma.language.connector.Procedure implements java.io.Serializable {
  public <T1,T2> T1 accept (final ProcedureVisitor<T1,T2> visitor, final T2 data) { return visitor.visitQueryAvailable (this, data); }
  private static final long serialVersionUID = 1l;
  public QueryAvailable () {
  }
  protected QueryAvailable (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
  }
  protected QueryAvailable (final QueryAvailable source) {
    super (source);
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
  public static QueryAvailable fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.procedure.QueryAvailable".equals (className)) break;
      try {
        return (com.opengamma.language.procedure.QueryAvailable)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new QueryAvailable (fudgeMsg);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof QueryAvailable)) return false;
    QueryAvailable msg = (QueryAvailable)o;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
