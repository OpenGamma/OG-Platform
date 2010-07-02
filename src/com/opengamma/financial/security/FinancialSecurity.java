/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.security.DefaultSecurity;

/**
 * An implementation of {@code Security} that implements the visitor pattern.
 */
public abstract class FinancialSecurity extends DefaultSecurity {

  /**
   * Creates a new security.
   * @param securityType  the type, not null
   */
  public FinancialSecurity(String securityType) {
    super(securityType);
  }

  /**
   * Accepts and processes the visitor.
   * 
   * @param <T>  the visitor result type
   * @param visitor  the visitor, not null
   * @return the result
   */
  public abstract <T> T accept(FinancialSecurityVisitor<T> visitor);

  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    // No additional fields
  }

  protected void fromFudgeMsgImpl(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    super.fromFudgeMsgImpl(context, message);
    // No additional fields
  }

}
