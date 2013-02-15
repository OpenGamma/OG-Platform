/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.io.Serializable;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Wrapper for a "primitive" value. This is to allow the {@link ComputationTargetType#PRIMITIVE} to be defined as something more specific than {@link UniqueIdentifiable} which causes function
 * resolution issues.
 */
public class Primitive implements UniqueIdentifiable, Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Implementation of {@link Primitive} that is based on an external identifier
   */
  public static final class ExternalIdentifiablePrimitive extends Primitive implements ExternalIdentifiable {

    private static final long serialVersionUID = 1L;

    private final ExternalId _eid;

    public ExternalIdentifiablePrimitive(final UniqueId uid, final ExternalId eid) {
      super(uid);
      assert eid != null;
      _eid = eid;
    }

    private ExternalIdentifiablePrimitive(final FudgeDeserializer deserializer, final FudgeMsg msg) {
      super(deserializer, msg);
      _eid = deserializer.fieldValueToObject(ExternalId.class, msg.getByName("identifier"));
    }

    @Override
    public ExternalId getExternalId() {
      return _eid;
    }

    @Override
    public void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg msg) {
      super.toFudgeMsg(serializer, msg);
      serializer.addToMessage(msg, "identifier", null, getExternalId());
    }

    public static ExternalIdentifiablePrimitive fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
      return new ExternalIdentifiablePrimitive(deserializer, msg);
    }

    @Override
    public String toString() {
      return getExternalId().toString();
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!o.getClass().equals(ExternalIdentifiablePrimitive.class)) {
        return false;
      }
      final ExternalIdentifiablePrimitive other = (ExternalIdentifiablePrimitive) o;
      return getUniqueId().equals(other.getUniqueId()) && getExternalId().equals(other.getExternalId());
    }

    @Override
    public int hashCode() {
      return getUniqueId().hashCode() * 31 + getExternalId().hashCode();
    }

  }

  /**
   * Implementation of {@link Primitive} that is based on an external identifier bundle.
   */
  public static final class ExternalBundleIdentifiablePrimitive extends Primitive implements ExternalBundleIdentifiable {

    private static final long serialVersionUID = 1L;

    private final ExternalIdBundle _eids;

    public ExternalBundleIdentifiablePrimitive(final UniqueId uid, final ExternalIdBundle eids) {
      super(uid);
      assert eids != null;
      _eids = eids;
    }

    private ExternalBundleIdentifiablePrimitive(final FudgeDeserializer deserializer, final FudgeMsg msg) {
      super(deserializer, msg);
      _eids = deserializer.fieldValueToObject(ExternalIdBundle.class, msg.getByName("identifiers"));
    }

    @Override
    public ExternalIdBundle getExternalIdBundle() {
      return _eids;
    }

    @Override
    public void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg msg) {
      super.toFudgeMsg(serializer, msg);
      serializer.addToMessage(msg, "identifiers", null, getExternalIdBundle());
    }

    public static ExternalBundleIdentifiablePrimitive fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
      return new ExternalBundleIdentifiablePrimitive(deserializer, msg);
    }

    @Override
    public String toString() {
      return getExternalIdBundle().toString();
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!o.getClass().equals(ExternalBundleIdentifiablePrimitive.class)) {
        return false;
      }
      final ExternalBundleIdentifiablePrimitive other = (ExternalBundleIdentifiablePrimitive) o;
      return getUniqueId().equals(other.getUniqueId()) && getExternalIdBundle().equals(other.getExternalIdBundle());
    }

    @Override
    public int hashCode() {
      return getUniqueId().hashCode() * 31 + getExternalIdBundle().hashCode();
    }

  }

  private final UniqueId _uid;

  public Primitive(final UniqueId uid) {
    assert uid != null;
    _uid = uid;
  }

  protected Primitive(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    this(deserializer.fieldValueToObject(UniqueId.class, msg.getByName("uniqueId")));
  }

  @Override
  public UniqueId getUniqueId() {
    return _uid;
  }

  public void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg msg) {
    serializer.addToMessage(msg, "uniqueId", null, getUniqueId());
  }

  public static Primitive fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return new Primitive(deserializer, msg);
  }

  @Override
  public String toString() {
    return getUniqueId().toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!o.getClass().equals(Primitive.class)) {
      return false;
    }
    final Primitive other = (Primitive) o;
    return getUniqueId().equals(other.getUniqueId());
  }

  @Override
  public int hashCode() {
    return getUniqueId().hashCode();
  }

}
