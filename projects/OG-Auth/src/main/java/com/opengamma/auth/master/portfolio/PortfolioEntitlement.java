/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.auth.master.portfolio;

import com.opengamma.auth.Either;
import com.opengamma.auth.Entitlement;
import com.opengamma.core.user.ResourceAccess;
import com.opengamma.id.ObjectId;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class PortfolioEntitlement extends Entitlement<Either<ObjectId, WholePortfolio>> {

  public static PortfolioEntitlement globalPortfolioEntitlement(Instant expiry, ResourceAccess access) {
    return new PortfolioEntitlement(Either.<ObjectId, WholePortfolio>right(WholePortfolio.INSTANCE), expiry, access);
  }

  public static PortfolioEntitlement singlePortfolioEntitlement(ObjectId portfolioId, Instant expiry, ResourceAccess access) {
    return new PortfolioEntitlement(Either.<ObjectId, WholePortfolio>left(portfolioId), expiry, access);
  }

  public PortfolioEntitlement(Either<ObjectId, WholePortfolio> objectIdWholePortfolioEither, Instant expiry, ResourceAccess access) {
    super(objectIdWholePortfolioEither, expiry, access);
  }

  @FudgeBuilderFor(PortfolioEntitlement.class)
  public static class FudgeBuilder implements org.fudgemsg.mapping.FudgeBuilder<Entitlement> {

    private static final String IDENTI = "identifier";
    private static final String ACCESS = "access";
    private static final String EXPIRY = "expiry";


    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Entitlement object) {
      MutableFudgeMsg rootMsg = serializer.newMessage();
      serializer.addToMessage(rootMsg, IDENTI, null, object.getIdentifier());
      serializer.addToMessage(rootMsg, EXPIRY, null, object.getExpiry());
      serializer.addToMessage(rootMsg, ACCESS, null, object.getAccess());
      return rootMsg;
    }

    @Override
    public Entitlement buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      Either identifier = deserializer.fieldValueToObject(Either.class, message.getByName(IDENTI));
      Instant expiry = deserializer.fieldValueToObject(Instant.class, message.getByName(EXPIRY));
      ResourceAccess access = deserializer.fieldValueToObject(ResourceAccess.class, message.getByName(ACCESS));
      return new PortfolioEntitlement(identifier, expiry, access);
    }
  }

}

