// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language;
public class Data implements java.io.Serializable {
  private static final long serialVersionUID = -25046238613169372l;
  private com.opengamma.language.Value _single;
  public static final int SINGLE_ORDINAL = 1;
  private com.opengamma.language.Value[] _linear;
  public static final int LINEAR_ORDINAL = 2;
  private com.opengamma.language.Value[][] _matrix;
  public static final int MATRIX_ORDINAL = 3;
  public Data () {
  }
  protected Data (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByOrdinal (SINGLE_ORDINAL);
    if (fudgeField != null)  {
      try {
        final com.opengamma.language.Value fudge1;
        fudge1 = com.opengamma.language.Value.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
        setSingle (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Data - field 'single' is not Value message", e);
      }
    }
    fudgeField = fudgeMsg.getByOrdinal (LINEAR_ORDINAL);
    if (fudgeField != null)  {
      try {
        final org.fudgemsg.FudgeMsg fudge1 = fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField);
        final java.util.List<com.opengamma.language.Value> fudge2 = new java.util.ArrayList<com.opengamma.language.Value> ();
        for (org.fudgemsg.FudgeField fudge3 : fudge1)if (fudge3.getType() != org.fudgemsg.wire.types.FudgeWireType.INDICATOR)  {
          try {
            final com.opengamma.language.Value fudge4;
            fudge4 = com.opengamma.language.Value.fromFudgeMsg (deserializer, fudge1.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge3));
            fudge2.add (fudge4);
          }
          catch (IllegalArgumentException e) {
            throw new IllegalArgumentException ("Fudge message is not a Data - field 'linear[]' is not Value message", e);
          }
        }
        else fudge2.add (null);
        setLinear (fudge2.toArray (new com.opengamma.language.Value[fudge2.size ()]));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Data - field 'linear' is not Value message[]", e);
      }
    }
    fudgeField = fudgeMsg.getByOrdinal (MATRIX_ORDINAL);
    if (fudgeField != null)  {
      try {
        final org.fudgemsg.FudgeMsg fudge1 = fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField);
        final java.util.List<com.opengamma.language.Value[]> fudge2 = new java.util.ArrayList<com.opengamma.language.Value[]> ();
        for (org.fudgemsg.FudgeField fudge3 : fudge1)if (fudge3.getType() != org.fudgemsg.wire.types.FudgeWireType.INDICATOR)  {
          try {
            final org.fudgemsg.FudgeMsg fudge4 = fudge1.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge3);
            final java.util.List<com.opengamma.language.Value> fudge5 = new java.util.ArrayList<com.opengamma.language.Value> ();
            for (org.fudgemsg.FudgeField fudge6 : fudge4)if (fudge6.getType() != org.fudgemsg.wire.types.FudgeWireType.INDICATOR)  {
              try {
                final com.opengamma.language.Value fudge7;
                fudge7 = com.opengamma.language.Value.fromFudgeMsg (deserializer, fudge4.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge6));
                fudge5.add (fudge7);
              }
              catch (IllegalArgumentException e) {
                throw new IllegalArgumentException ("Fudge message is not a Data - field 'matrix[][]' is not Value message", e);
              }
            }
            else fudge5.add (null);
            fudge2.add (fudge5.toArray (new com.opengamma.language.Value[fudge5.size ()]));
          }
          catch (IllegalArgumentException e) {
            throw new IllegalArgumentException ("Fudge message is not a Data - field 'matrix[]' is not Value message[]", e);
          }
        }
        else fudge2.add (null);
        setMatrix (fudge2.toArray (new com.opengamma.language.Value[fudge2.size ()][]));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Data - field 'matrix' is not Value message[][]", e);
      }
    }
  }
  public Data (com.opengamma.language.Value single, com.opengamma.language.Value[] linear, com.opengamma.language.Value[][] matrix) {
    if (single == null) _single = null;
    else {
      _single = (com.opengamma.language.Value)single.clone ();
    }
    if (linear == null) _linear = null;
    else {
      linear = java.util.Arrays.copyOf (linear, linear.length);
      for (int fudge0 = 0; fudge0 < linear.length; fudge0++) {
        if (linear[fudge0] != null) {
          linear[fudge0] = (com.opengamma.language.Value)linear[fudge0].clone ();
        }
      }
      _linear = linear;
    }
    if (matrix == null) _matrix = null;
    else {
      matrix = java.util.Arrays.copyOf (matrix, matrix.length);
      for (int fudge0 = 0; fudge0 < matrix.length; fudge0++) {
        if (matrix[fudge0] != null) {
          matrix[fudge0] = java.util.Arrays.copyOf (matrix[fudge0], matrix[fudge0].length);
          for (int fudge1 = 0; fudge1 < matrix[fudge0].length; fudge1++) {
            if (matrix[fudge0][fudge1] != null) {
              matrix[fudge0][fudge1] = (com.opengamma.language.Value)matrix[fudge0][fudge1].clone ();
            }
          }
        }
      }
      _matrix = matrix;
    }
  }
  protected Data (final Data source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._single == null) _single = null;
    else {
      _single = (com.opengamma.language.Value)source._single.clone ();
    }
    if (source._linear == null) _linear = null;
    else {
      final com.opengamma.language.Value[] fudge0 = java.util.Arrays.copyOf (source._linear, source._linear.length);
      for (int fudge1 = 0; fudge1 < fudge0.length; fudge1++) {
        if (fudge0[fudge1] != null) {
          fudge0[fudge1] = (com.opengamma.language.Value)fudge0[fudge1].clone ();
        }
      }
      _linear = fudge0;
    }
    if (source._matrix == null) _matrix = null;
    else {
      final com.opengamma.language.Value[][] fudge0 = java.util.Arrays.copyOf (source._matrix, source._matrix.length);
      for (int fudge1 = 0; fudge1 < fudge0.length; fudge1++) {
        if (fudge0[fudge1] != null) {
          fudge0[fudge1] = java.util.Arrays.copyOf (fudge0[fudge1], fudge0[fudge1].length);
          for (int fudge2 = 0; fudge2 < fudge0[fudge1].length; fudge2++) {
            if (fudge0[fudge1][fudge2] != null) {
              fudge0[fudge1][fudge2] = (com.opengamma.language.Value)fudge0[fudge1][fudge2].clone ();
            }
          }
        }
      }
      _matrix = fudge0;
    }
  }
  public Data clone () {
    return new Data (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_single != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _single.getClass (), com.opengamma.language.Value.class);
      _single.toFudgeMsg (serializer, fudge1);
      msg.add (null, SINGLE_ORDINAL, fudge1);
    }
    if (_linear != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = serializer.newMessage ();
      for (com.opengamma.language.Value fudge2 : _linear) {
        if (fudge2 != null)  {
          final org.fudgemsg.MutableFudgeMsg fudge3 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), fudge2.getClass (), com.opengamma.language.Value.class);
          fudge2.toFudgeMsg (serializer, fudge3);
          fudge1.add (null, null, fudge3);
        }
        else {
          fudge1.add (null, null, org.fudgemsg.types.IndicatorType.INSTANCE);
        }
      }
      msg.add (null, LINEAR_ORDINAL, fudge1);
    }
    if (_matrix != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = serializer.newMessage ();
      for (com.opengamma.language.Value[] fudge2 : _matrix) {
        if (fudge2 != null)  {
          final org.fudgemsg.MutableFudgeMsg fudge3 = serializer.newMessage ();
          for (com.opengamma.language.Value fudge4 : fudge2) {
            if (fudge4 != null)  {
              final org.fudgemsg.MutableFudgeMsg fudge5 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), fudge4.getClass (), com.opengamma.language.Value.class);
              fudge4.toFudgeMsg (serializer, fudge5);
              fudge3.add (null, null, fudge5);
            }
            else {
              fudge3.add (null, null, org.fudgemsg.types.IndicatorType.INSTANCE);
            }
          }
          fudge1.add (null, null, fudge3);
        }
        else {
          fudge1.add (null, null, org.fudgemsg.types.IndicatorType.INSTANCE);
        }
      }
      msg.add (null, MATRIX_ORDINAL, fudge1);
    }
  }
  public static Data fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.Data".equals (className)) break;
      try {
        return (com.opengamma.language.Data)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Data (deserializer, fudgeMsg);
  }
  public com.opengamma.language.Value getSingle () {
    return _single;
  }
  public void setSingle (com.opengamma.language.Value single) {
    if (single == null) _single = null;
    else {
      _single = (com.opengamma.language.Value)single.clone ();
    }
  }
  public com.opengamma.language.Value[] getLinear () {
    return _linear;
  }
  public void setLinear (com.opengamma.language.Value[] linear) {
    if (linear == null) _linear = null;
    else {
      linear = java.util.Arrays.copyOf (linear, linear.length);
      for (int fudge0 = 0; fudge0 < linear.length; fudge0++) {
        if (linear[fudge0] != null) {
          linear[fudge0] = (com.opengamma.language.Value)linear[fudge0].clone ();
        }
      }
      _linear = linear;
    }
  }
  public com.opengamma.language.Value[][] getMatrix () {
    return _matrix;
  }
  public void setMatrix (com.opengamma.language.Value[][] matrix) {
    if (matrix == null) _matrix = null;
    else {
      matrix = java.util.Arrays.copyOf (matrix, matrix.length);
      for (int fudge0 = 0; fudge0 < matrix.length; fudge0++) {
        if (matrix[fudge0] != null) {
          matrix[fudge0] = java.util.Arrays.copyOf (matrix[fudge0], matrix[fudge0].length);
          for (int fudge1 = 0; fudge1 < matrix[fudge0].length; fudge1++) {
            if (matrix[fudge0][fudge1] != null) {
              matrix[fudge0][fudge1] = (com.opengamma.language.Value)matrix[fudge0][fudge1].clone ();
            }
          }
        }
      }
      _matrix = matrix;
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Data)) return false;
    Data msg = (Data)o;
    if (_single != null) {
      if (msg._single != null) {
        if (!_single.equals (msg._single)) return false;
      }
      else return false;
    }
    else if (msg._single != null) return false;
    if (!java.util.Arrays.deepEquals (_linear, msg._linear)) return false;
    if (!java.util.Arrays.deepEquals (_matrix, msg._matrix)) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_single != null) hc += _single.hashCode ();
    hc *= 31;
    if (_linear != null)hc += java.util.Arrays.deepHashCode (_linear);
    hc *= 31;
    if (_matrix != null)hc += java.util.Arrays.deepHashCode (_matrix);
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
