/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.auth;

import org.threeten.bp.Instant;
import com.opengamma.core.user.ResourceAccess;


public class Entitlement<IDENTIFIER> {
  private final IDENTIFIER _identifier; // identifies the resource this entitlement entitles to.
  private final Instant _expiry;
  private final ResourceAccess _access;

  public Entitlement(IDENTIFIER identifier, Instant expiry, ResourceAccess access) {
    _identifier = identifier;
    _expiry = expiry;
    _access = access;
  }

  /**
   * Creates never expiring entitlement
   *
   * @param identifier
   * @param access
   */
  public Entitlement(IDENTIFIER identifier, ResourceAccess access) {
    this(identifier, null, access);
  }

  public IDENTIFIER getIdentifier() {
    return _identifier;
  }

  public Instant getExpiry() {
    return _expiry;
  }

  public ResourceAccess getAccess() {
    return _access;
  }

  @Override
  public String toString() {
    return "PortfolioEntitlement{" +
        "_identifier=" + _identifier +
        ", _expiry=" + _expiry +
        ", _access=" + _access +
        '}';
  }

//  @FudgeBuilderFor(Entitlement.class)
//  public static class FudgeBuilder implements org.fudgemsg.mapping.FudgeBuilder<Entitlement> {
//
//    private static final String IDENTI = "identifier";
//    private static final String ACCESS = "access";
//    private static final String EXPIRY = "expiry";
//
//
//    @Override
//    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Entitlement object) {
//      MutableFudgeMsg rootMsg = serializer.newMessage();
//      serializer.addToMessage(rootMsg, IDENTI, null, object.getIdentifier());
//      serializer.addToMessage(rootMsg, EXPIRY, null, object.getExpiry());
//      serializer.addToMessage(rootMsg, ACCESS, null, object.getAccess());
//      return rootMsg;
//    }
//
//    @Override
//    public Entitlement buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
//      Object identifier = deserializer.fieldValueToObject(Object.class, message.getByName(IDENTI));
//      Instant expiry = deserializer.fieldValueToObject(Instant.class, message.getByName(EXPIRY));
//      ResourceAccess access = deserializer.fieldValueToObject(ResourceAccess.class, message.getByName(ACCESS));
//      return new Entitlement(identifier, expiry, access);
//    }
//  }
}
