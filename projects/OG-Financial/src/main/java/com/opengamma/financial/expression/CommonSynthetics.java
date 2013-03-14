/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.expression;

import com.google.common.base.Function;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;

/**
 * Common synthetic properties for expression evaluation.
 */
public final class CommonSynthetics {

  private static final ThreadLocal<ComputationTargetResolver.AtVersionCorrection> s_resolver = new ThreadLocal<ComputationTargetResolver.AtVersionCorrection>();

  private CommonSynthetics() {
  }

  protected static ComputationTargetResolver.AtVersionCorrection getResolver() {
    return s_resolver.get();
  }

  protected static void setResolver(final ComputationTargetResolver.AtVersionCorrection resolver) {
    s_resolver.set(resolver);
  }

  public static Function<Security, Currency> securityCurrency() {
    return new Function<Security, Currency>() {

      @Override
      public Currency apply(final Security security) {
        return FinancialSecurityUtils.getCurrency(security);
      }

    };
  }

  public static Function<Security, String> securityType() {
    return new Function<Security, String>() {

      @Override
      public String apply(final Security security) {
        return security.getSecurityType();
      }

    };
  }

  public static Function<ManageableSecurity, Security> securityUnderlying() {
    return new Function<ManageableSecurity, Security>() {

      @Override
      public Security apply(final ManageableSecurity security) {
        final Object underlying = security.property("underlyingId").get();
        return UserExpressionParser.resolve(ComputationTargetType.SECURITY, underlying);
      }

    };
  }

  public static void configureParser(final UserExpressionParser parser) {
    parser.setSynthetic(Security.class, Currency.class, "currency", securityCurrency());
    parser.setSynthetic(Security.class, String.class, "type", securityType());
    parser.setSynthetic(ManageableSecurity.class, Security.class, "underlying", securityUnderlying());
  }

}
