// automatically created - Mon Feb 01 16:44:12 GMT 2010
// created from /home/andrew/OpenGamma/OG-Build/projects/OG-Engine/src/com/opengamma/engine/value/ValueSpecification.proto:9(10)
package com.opengamma.engine.value;
public class ValueSpecification implements Cloneable {
  private final com.opengamma.engine.value.ValueRequirement _requirementSpecification;
  public static final String REQUIREMENTSPECIFICATION_KEY = "requirementSpecification";
  public ValueSpecification (com.opengamma.engine.value.ValueRequirement requirementSpecification) {
    if (requirementSpecification == null) throw new NullPointerException ("'requirementSpecification' cannot be null");
    else {
      _requirementSpecification = requirementSpecification;
    }
  }
  protected ValueSpecification (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    Object fudge0;
    fudgeField = fudgeMsg.getByName (REQUIREMENTSPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ValueSpecification - field 'requirementSpecification' is not present");
    fudge0 = fudgeField.getValue ();
    if (!(fudge0 instanceof org.fudgemsg.FudgeFieldContainer)) throw new IllegalArgumentException ("Fudge message is not a ValueSpecification - field 'requirementSpecification' is not ValueRequirement message");
    try {
      _requirementSpecification = fudgeContext.fudgeMsgToObject (com.opengamma.engine.value.ValueRequirement.class, (org.fudgemsg.FudgeFieldContainer)fudge0);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ValueSpecification - field 'requirementSpecification' is not ValueRequirement message", e);
    }
  }
  protected ValueSpecification (final ValueSpecification source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._requirementSpecification == null) _requirementSpecification = null;
    else {
      _requirementSpecification = source._requirementSpecification;
    }
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    if (_requirementSpecification != null)  {
      msg.add (REQUIREMENTSPECIFICATION_KEY, null, fudgeContext.objectToFudgeMsg (_requirementSpecification));
    }
  }
  public static ValueSpecification fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    return new ValueSpecification (fudgeContext, fudgeMsg);
  }
  public com.opengamma.engine.value.ValueRequirement getRequirementSpecification () {
    return _requirementSpecification;
  }
  public boolean equals (final Object o) {
    if (o == null) return false;
    if (!(o instanceof ValueSpecification)) return false;
    final ValueSpecification msg = (ValueSpecification)o;
    if (_requirementSpecification != null) if (msg._requirementSpecification != null)  {
      if (!_requirementSpecification.equals (msg._requirementSpecification)) return false;
    }
    else return false;
    else if (msg._requirementSpecification != null) return false;
    return true;
  }
  public int hashCode () {
    int hc;
    hc = 1;
    hc = hc * 31;
    if (_requirementSpecification != null) hc = hc + _requirementSpecification.hashCode ();
    return hc;
  }
  public ValueSpecification clone () {
    return new ValueSpecification (this);
  }
}
