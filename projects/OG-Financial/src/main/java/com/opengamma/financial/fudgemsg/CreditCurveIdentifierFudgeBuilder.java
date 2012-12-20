/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.credit.CreditCurveIdentifier;

/**
 * Builder for converting {@link CreditCurveIdentifier} instances to / from Fudge messages.
 */
@FudgeBuilderFor(CreditCurveIdentifier.class)
public class CreditCurveIdentifierFudgeBuilder implements FudgeBuilder<CreditCurveIdentifier> {
  private static final String ISSUER_ID_VALUE = "issuerIdValue";
  private static final String SENIORITY = "seniority";
  private static final String RESTRUCTURING_CLAUSE = "restructuringClause";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CreditCurveIdentifier object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, CreditCurveIdentifier.class);
    message.add(ISSUER_ID_VALUE, object.getIssuer());
    message.add(SENIORITY, object.getSeniority());
    message.add(RESTRUCTURING_CLAUSE, object.getRestructuringClause());
    return message;
  }

  @Override
  public CreditCurveIdentifier buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String issuerIdValue = message.getString(ISSUER_ID_VALUE);
    final String seniority = message.getString(SENIORITY);
    final String restructuringClause = message.getString(RESTRUCTURING_CLAUSE);
    return CreditCurveIdentifier.of(issuerIdValue, seniority, restructuringClause);
  }

}
