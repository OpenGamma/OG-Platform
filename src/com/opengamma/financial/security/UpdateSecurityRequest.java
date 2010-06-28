/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.security.Security;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A request to update a security.
 */
public final class UpdateSecurityRequest {

  /**
   * The security uid.
   */
  private UniqueIdentifier _uid;
  /**
   * The security.
   */
  private Security _security;

  /**
   * Creates an instance.
   */
  public UpdateSecurityRequest() {
  }

  /**
   * Creates an instance.
   * @param security  the security, not null
   */
  public UpdateSecurityRequest(Security security) {
    setSecurity(security);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uid;
  }

  /**
   * Sets the security unique identifier.
   * @param uid  the unique identifier, not null
   */
  public void setUniqueIdentifier(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "UniqueIdentifier");
    _uid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security field.
   * @return the security
   */
  public Security getSecurity() {
    return _security;
  }

  /**
   * Sets the security field.
   * @param security  the security
   */
  public void setSecurity(Security security) {
    _security = security;
  }

  //-------------------------------------------------------------------------
  /**
   * Validates this request throwing an exception if not.
   */
  public void checkValid() {
    Validate.notNull(getUniqueIdentifier(), "UniqueIdentifier must not be null");
//    Validate.isTrue(getUniqueIdentifier().isVersioned(), "UniqueIdentifier must be versioned");
    Validate.notNull(getSecurity(), "Security must not be null");
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String UID_FIELD_NAME = "uid";
  /** Field name. */
  private static final String SECURITY_FIELD_NAME = "security";

  /**
   * Serializes to a Fudge message.
   * @param context  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    MutableFudgeFieldContainer msg = context.newMessage();
    if (_uid != null) {
      msg.add(UID_FIELD_NAME, _uid.toFudgeMsg(context));
    }
    if (_security != null) {
      context.objectToFudgeMsg(msg, SECURITY_FIELD_NAME, null, _security);
    }
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static UpdateSecurityRequest fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    UpdateSecurityRequest req = new UpdateSecurityRequest();
    if (msg.hasField(UID_FIELD_NAME)) {
      req.setUniqueIdentifier(UniqueIdentifier.fromFudgeMsg(msg.getMessage(UID_FIELD_NAME)));
    }
    if (msg.hasField(SECURITY_FIELD_NAME)) {
      req.setSecurity(context.fieldValueToObject(Security.class, msg.getByName(SECURITY_FIELD_NAME)));
    }
    return req;
  }

}
