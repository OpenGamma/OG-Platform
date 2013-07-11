/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import com.opengamma.id.ObjectId;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import java.io.Serializable;

import static com.opengamma.util.ArgumentChecker.notNull;


/**
 * Simple implementation of {@code OGRole}.
 * <p/>
 * This is the simplest possible implementation of the {@link com.opengamma.core.user.OGRole} interface.
 * <p/>
 * This class is mutable and not thread-safe.
 * It is intended to be used in the engine via the read-only {@code OGRole} interface.
 */
public class OGEntitlement implements Serializable {

  /**
   * Serialization version.
   */
  private static final long serialVersionUID = 1L;

  public OGEntitlement(String resourceId, String type, ResourceAccess accesses) {
    this(null, resourceId, type, accesses);
  }

  public OGEntitlement(ObjectId objectId, String resourceId, String type, ResourceAccess access) {
    this._objectId = objectId;
    this._resourceId = resourceId;
    this._type = type;
    this._acces = access;
  }


  final private ObjectId _objectId;
  /**
   * The external id of the resource this entitlement represents;
   */
  final private String _resourceId;

  /**
   * Type for the entitled resource. E.g 'portfolio'.
   * Used for classification purposes.
   */
  final private String _type;

  /**
   * Access type for the entitled resource. E.g 'r' for read etc.
   */
  final private ResourceAccess _acces;

  public ObjectId getObjectId() {
    return _objectId;
  }

  public String getResourceId() {
    return _resourceId;
  }

  public String getType() {
    return _type;
  }

  public ResourceAccess getAccess() {
    return _acces;
  }

  public OGEntitlement setType(String type) {
    notNull(type, "ResourceType");
    return new OGEntitlement(getObjectId(), getResourceId(), type, getAccess());
  }

  public OGEntitlement setAccess(ResourceAccess access) {
    notNull(access, "ResourceAccess");
    return new OGEntitlement(getObjectId(), getResourceId(), getType(), access);
  }

  public OGEntitlement setExternalId(String resourceId) {
    notNull(resourceId, "resourceId");
    return new OGEntitlement(getObjectId(), resourceId, getType(), getAccess());
  }

  public OGEntitlement setObjectId(ObjectId id) {
    return new OGEntitlement(id, getResourceId(), getType(), getAccess());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OGEntitlement that = (OGEntitlement) o;

    if (_acces != null ? !_acces.equals(that._acces) : that._acces != null) return false;
    if (_resourceId != null ? !_resourceId.equals(that._resourceId) : that._resourceId != null) return false;
    if (_objectId != null ? !_objectId.equals(that._objectId) : that._objectId != null) return false;
    if (_type != null ? !_type.equals(that._type) : that._type != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = _objectId != null ? _objectId.hashCode() : 0;
    result = 31 * result + (_resourceId != null ? _resourceId.hashCode() : 0);
    result = 31 * result + (_type != null ? _type.hashCode() : 0);
    result = 31 * result + (_acces != null ? _acces.hashCode() : 0);
    return result;
  }

  @FudgeBuilderFor(OGEntitlement.class)
  public static class OGEntitlementFudgeBuilder implements FudgeBuilder<OGEntitlement> {

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer fudgeSerializer, OGEntitlement object) {
      final MutableFudgeMsg message = fudgeSerializer.newMessage();
      FudgeSerializer.addClassHeader(message, OGEntitlement.class);
      message.add("oid", object.getObjectId());
      message.add("exid", object.getResourceId());
      message.add("type", object.getType());
      message.add("access", object.getAccess());
      return message;
    }

    @Override
    public OGEntitlement buildObject(FudgeDeserializer fudgeDeserializer, FudgeMsg fudgeFields) {
      return new OGEntitlement(fudgeFields.getValue(ObjectId.class, "oid"),
          fudgeFields.getString("exid"),
          fudgeFields.getString("type"),
          fudgeFields.getValue(ResourceAccess.class, "access"));
    }
  }
}

