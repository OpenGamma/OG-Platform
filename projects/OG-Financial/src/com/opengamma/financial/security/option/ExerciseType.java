// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public abstract class ExerciseType implements java.io.Serializable {
  public abstract <T> T accept (ExerciseTypeVisitor<T> visitor);
  private static final long serialVersionUID = 1l;
  public ExerciseType () {
  }
  protected ExerciseType (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
  }
  protected ExerciseType (final ExerciseType source) {
  }
  public abstract org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext);
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
  }
  public static ExerciseType fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.ExerciseType".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.ExerciseType)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    throw new UnsupportedOperationException ("ExerciseType is an abstract message");
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof ExerciseType)) return false;
    ExerciseType msg = (ExerciseType)o;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
