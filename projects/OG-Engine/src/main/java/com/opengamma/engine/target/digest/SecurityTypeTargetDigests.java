/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.digest;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.util.tuple.Pair;

/**
 * Basic implementation that returns the security type as the digest.
 * <p>
 * This is supplied mainly for use as an example implementation. The quality of these digests will depend on the function repository and security modeling being used. An implementation that has more
 * detailed knowledge of the analytic functions or targets in use might be necessary to benefit from the target digest algorithm.
 */
public class SecurityTypeTargetDigests extends AbstractTargetDigests {

  public SecurityTypeTargetDigests() {
    addHandler(ComputationTargetType.POSITION, new TargetDigests() {
      @Override
      public Object getDigest(FunctionCompilationContext context, ComputationTargetSpecification targetSpec) {
        final ComputationTarget target = context.getComputationTargetResolver().resolve(targetSpec);
        if (target != null) {
          return getPositionDigest(target.getPosition());
        } else {
          return null;
        }
      }
    });
    addHandler(ComputationTargetType.TRADE, new TargetDigests() {
      @Override
      public Object getDigest(FunctionCompilationContext context, ComputationTargetSpecification targetSpec) {
        final ComputationTarget target = context.getComputationTargetResolver().resolve(targetSpec);
        if (target != null) {
          return getTradeDigest(target.getTrade());
        } else {
          return null;
        }
      }
    });
    addHandler(ComputationTargetType.SECURITY, new TargetDigests() {
      @Override
      public Object getDigest(FunctionCompilationContext context, ComputationTargetSpecification targetSpec) {
        final ComputationTarget target = context.getComputationTargetResolver().resolve(targetSpec);
        if (target != null) {
          return getSecurityDigest(target.getSecurity());
        } else {
          return null;
        }
      }
    });
  }

  protected Object getPositionDigest(Position position) {
    return Pair.of("POSITION", getSecurityDigest(position.getSecurity()));
  }

  protected Object getTradeDigest(Trade trade) {
    return Pair.of("TRADE", getSecurityDigest(trade.getSecurity()));
  }

  protected Object getSecurityDigest(Security security) {
    return security.getSecurityType();
  }

}
