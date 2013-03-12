/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.expression.CommonSynthetics;
import com.opengamma.financial.expression.ELExpressionParser;
import com.opengamma.financial.expression.UserExpression;
import com.opengamma.financial.expression.UserExpressionParser;
import com.opengamma.id.ExternalIdBundle;

/**
 * Implementation of {@link OverrideOperationCompiler} that allows market data overrides to be expressed as EL expressions.
 */
public class MarketDataELCompiler implements OverrideOperationCompiler {

  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataELCompiler.class);

  private final class Evaluator implements OverrideOperation {

    private final UserExpression _expr;

    private final ComputationTargetResolver.AtVersionCorrection _resolver;

    public Evaluator(final UserExpression expr, final ComputationTargetResolver.AtVersionCorrection resolver) {
      _expr = expr;
      _resolver = resolver;
    }

    private UserExpression getExpr() {
      return _expr;
    }

    @Override
    public Object apply(final ValueRequirement requirement, final Object original) {
      UserExpressionParser.setResolver(_resolver);
      s_logger.debug("Applying {} to {}", _expr, requirement);
      final UserExpression.Evaluator eval = getExpr().evaluator();
      eval.setVariable("x", original);
      if (requirement.getTargetReference().getType().isTargetType(ComputationTargetType.SECURITY)) {
        requirement.getTargetReference().accept(new ComputationTargetReferenceVisitor<Void>() {

          @Override
          public Void visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
            eval.setVariable("security", UserExpressionParser.resolve(ComputationTargetType.SECURITY, requirement.getIdentifiers()));
            return null;
          }

          @Override
          public Void visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
            eval.setVariable("security", UserExpressionParser.resolve(ComputationTargetType.SECURITY, specification.getUniqueId()));
            return null;
          }

        });
      } else if (requirement.getTargetReference().getType().isTargetType(ComputationTargetType.PRIMITIVE)) {
        requirement.getTargetReference().accept(new ComputationTargetReferenceVisitor<Void>() {

          @Override
          public Void visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
            final ExternalIdBundle bundle = requirement.getIdentifiers();
            eval.setVariable("externalIds", bundle);
            if (bundle.size() == 1) {
              eval.setVariable("externalId", bundle.iterator().next());
            }
            return null;
          }

          @Override
          public Void visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
            eval.setVariable("uniqueId", specification.getUniqueId());
            return null;
          }

        });
      }
      eval.setVariable("value", requirement.getValueName());
      final Object result = eval.evaluate();
      UserExpressionParser.setResolver(null);
      if (result == UserExpression.NA) {
        s_logger.debug("Evaluation failed - using original {}", original);
        return original;
      } else {
        s_logger.debug("Evaluation of {} to {}", original, result);
        return result;
      }
    }

  }

  private final UserExpressionParser _parser;

  public MarketDataELCompiler() {
    _parser = new ELExpressionParser();
    try {
      _parser.setFunction("Curve", "parallelShift", MarketDataELFunctions.class.getMethod("parallelShiftCurve", Object.class, Double.TYPE));
      _parser.setFunction("Curve", "pointShift", MarketDataELFunctions.class.getMethod("pointShiftCurve", Object.class, Double.TYPE, Double.TYPE));
      _parser.setFunction("Security", "get", MarketDataELFunctions.class.getMethod("getSecurity", Object.class));
      _parser.setFunction("FX", "isRate", MarketDataELFunctions.class.getMethod("isFXRate", Object.class));
      _parser.setFunction("FX", "multiplier", MarketDataELFunctions.class.getMethod("getFXMultiplier", Object.class, Double.TYPE));
      CommonSynthetics.configureParser(_parser);
    } catch (final Exception ex) {
      throw new OpenGammaRuntimeException("Caught", ex);
    }
  }

  @Override
  public OverrideOperation compile(final String operation, final ComputationTargetResolver.AtVersionCorrection resolver) {
    final UserExpression expr = getParser().parse(operation);
    return new Evaluator(expr, resolver);
  }

  private UserExpressionParser getParser() {
    return _parser;
  }

}
