/*
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Defines static helpers for yield curve functions.
 */
public final class YieldCurveFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveFunction.class);

  /**
   * Identifies the name of the forward curve used for a value. A value dependent on just one
   * curve should use the default "CURVE" name and not this prefixed name.
   * @deprecated This is used in old yield curve code - do not use
   */
  @Deprecated
  public static final String PROPERTY_FORWARD_CURVE = "Forward" + ValuePropertyNames.CURVE;

  /**
   * Identifies the name of the funding curve used for a value. A value dependent on just one
   * curve should use the default "CURVE" name and not this prefixed name.
   * @deprecated This is used in old yield curve code - do not use
   */
  @Deprecated
  public static final String PROPERTY_FUNDING_CURVE = "Funding" + ValuePropertyNames.CURVE;

  /**
   * Identifies the interpolator to be used
   */
  public static final String PROPERTY_INTERPOLATOR = "Interpolator";

  /**
   * Identifies the left extrapolator to be used
   */
  public static final String PROPERTY_LEFT_EXTRAPOLATOR = "LeftExtrapolator";

  /**
   * Identifies the right extrapolator to be used
   */
  public static final String PROPERTY_RIGHT_EXTRAPOLATOR = "RightExtrapolator";

  private YieldCurveFunction() {
  }

  // TODO: these should be somewhere else
  public static String getPropertyValue(final String propertyName, final ValueRequirement requirement) {
    final Set<String> curveNames = requirement.getConstraints().getValues(propertyName);
    if ((curveNames == null) || (curveNames.size() != 1)) {
      return null;
    } else {
      return curveNames.iterator().next();
    }
  }

  // TODO: these should be somewhere else
  public static String getPropertyValue(final String propertyName, final FunctionCompilationContext context, final ValueRequirement requirement) {
    s_logger.debug("propertyName={} requirement={}", propertyName, requirement);
    final Set<String> curveNames = requirement.getConstraints().getValues(propertyName);
    final Set<String> defaultCurves;
    switch ((curveNames != null) ? curveNames.size() : 0) {
      case 0:
        // Handles both the wildcard case and the unspecified case
        s_logger.debug("wildcard or unspecified requirement");
        defaultCurves = context.getViewCalculationConfiguration().getDefaultProperties().getValues(propertyName);
        if (defaultCurves == null) {
          s_logger.info("No default {} defined", propertyName);
          throw new IllegalStateException("No default " + propertyName + " defined");
        } else if (defaultCurves.size() != 1) {
          s_logger.info("Invalid default {} - {}", propertyName, defaultCurves);
          throw new IllegalStateException("Invalid default " + propertyName + " - " + defaultCurves);
        } else {
          final String value = defaultCurves.iterator().next();
          s_logger.info("Default {} is {}", propertyName, value);
          return value;
        }
      case 1:
        final String value = curveNames.iterator().next();
        s_logger.info("Value for {} is {}", propertyName, value);
        return value;
      default:
        defaultCurves = context.getViewCalculationConfiguration().getDefaultProperties().getValues(propertyName);
        String foundCurve = null;
        for (final String curveName : curveNames) {
          if (defaultCurves.contains(curveName)) {
            if (foundCurve == null) {
              foundCurve = curveName;
            } else {
              s_logger.info("Default {} contains more than one of {}", propertyName, curveNames);
              throw new IllegalStateException("Both " + curveName + " and " + foundCurve + " declared as default " + propertyName);
            }
          }
        }
        if (foundCurve != null) {
          s_logger.info("Default {} is {}", propertyName, foundCurve);
          return foundCurve;
        } else {
          s_logger.info("None of {} declared as defaults for {}", curveNames, propertyName);
          throw new IllegalStateException("Can't select " + propertyName + " from " + curveNames + " - none declared as default");
        }
    }
  }

  /**
   * Returns the curve name specified as a requirement constraint, null if there is no constraint
   * or a wildcard.
   *
   * @param requirement the requirement
   * @return the curve name, may be null
   */
  public static String getCurveName(final ValueRequirement requirement) {
    return getPropertyValue(ValuePropertyNames.CURVE, requirement);
  }

  /**
   * Returns the curve name specified as a requirement constraint or the view's default curve name if there is
   * no constraint or a wildcard.
   *
   * @param context the function compilation context
   * @param requirement the requirement
   * @return the curve name, not null
   */
  public static String getCurveName(final FunctionCompilationContext context, final ValueRequirement requirement) {
    return getPropertyValue(ValuePropertyNames.CURVE, context, requirement);
  }

  public static String getForwardCurveName(final ValueRequirement requirement) {
    return getPropertyValue(PROPERTY_FORWARD_CURVE, requirement);
  }

  public static String getForwardCurveName(final FunctionCompilationContext context, final ValueRequirement requirement) {
    return getPropertyValue(PROPERTY_FORWARD_CURVE, context, requirement);
  }

  public static String getFundingCurveName(final ValueRequirement requirement) {
    return getPropertyValue(PROPERTY_FUNDING_CURVE, requirement);
  }

  public static String getFundingCurveName(final FunctionCompilationContext context, final ValueRequirement requirement) {
    return getPropertyValue(PROPERTY_FUNDING_CURVE, context, requirement);
  }

  public static ValueRequirement getCurveRequirement(final Currency currency, final String curveName, final String advisoryForward, final String advisoryFunding) {
    final ValueProperties.Builder props = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    if (advisoryForward != null) {
      props.with(PROPERTY_FORWARD_CURVE, advisoryForward).withOptional(PROPERTY_FORWARD_CURVE);
    }
    if (advisoryFunding != null) {
      props.with(PROPERTY_FUNDING_CURVE, advisoryFunding).withOptional(PROPERTY_FUNDING_CURVE);
    }
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), props.get());
  }

  public static ValueRequirement getCurveRequirement(final Currency currency, final String curveName, final String advisoryForward, final String advisoryFunding, final String calculationMethod) {
    final ValueProperties.Builder props = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    if (advisoryForward != null) {
      props.with(PROPERTY_FORWARD_CURVE, advisoryForward).withOptional(PROPERTY_FORWARD_CURVE);
    }
    if (advisoryFunding != null) {
      props.with(PROPERTY_FUNDING_CURVE, advisoryFunding).withOptional(PROPERTY_FUNDING_CURVE);
    }
    props.with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), props.get());
  }

  public static ValueRequirement getJacobianRequirement(final Currency currency, final String forwardCurveName, final String fundingCurveName) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.of(currency),
        ValueProperties.with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .get());
  }

  public static ValueRequirement getJacobianRequirement(final Currency currency, final String forwardCurveName, final String fundingCurveName, final String calculationMethod) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.of(currency),
        ValueProperties.with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod)
        .get());
  }

  public static ValueRequirement getCouponSensitivityRequirement(final Currency currency, final String forwardCurveName, final String fundingCurveName) {
    return new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, ComputationTargetSpecification.of(currency),
        ValueProperties.builder()
        .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, forwardCurveName)
        .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, fundingCurveName).get());
  }

  public static ValueRequirement getCouponSensitivityRequirement(final Currency currency) {
    return new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, ComputationTargetSpecification.of(currency),
        ValueProperties.withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .get());
  }

  public static ValueRequirement getJacobianRequirement(final Currency currency, final String calculationMethod) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, ComputationTargetSpecification.of(currency),
        ValueProperties.withAny(YieldCurveFunction.PROPERTY_FORWARD_CURVE)
        .withAny(YieldCurveFunction.PROPERTY_FUNDING_CURVE)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, calculationMethod)
        .get());
  }

  public static Pair<String, String> getDesiredValueCurveNames(final Set<ValueRequirement> desiredValues) {
    String forwardCurveName = null;
    String fundingCurveName = null;
    for (final ValueRequirement desiredValue : desiredValues) {
      if (forwardCurveName == null) {
        final String curveName = getForwardCurveName(desiredValue);
        if (curveName != null) {
          forwardCurveName = curveName;
          if (fundingCurveName != null) {
            break;
          }
        }
      }
      if (fundingCurveName == null) {
        final String curveName = getFundingCurveName(desiredValue);
        if (curveName != null) {
          fundingCurveName = curveName;
          if (forwardCurveName != null) {
            break;
          }
        }
      }
    }
    ArgumentChecker.notNull(fundingCurveName, "fundingCurveName");
    ArgumentChecker.notNull(forwardCurveName, "forwardCurveName");
    return Pairs.of(forwardCurveName, fundingCurveName);
  }

  public static Pair<String, String> getDesiredValueCurveNames(final FunctionCompilationContext context, final ValueRequirement desiredValue) {
    final String desiredCurveName = getCurveName(context, desiredValue);
    String forwardCurveName = getForwardCurveName(desiredValue);
    String fundingCurveName = getFundingCurveName(desiredValue);
    if (forwardCurveName == null) {
      if (fundingCurveName == null) {
        forwardCurveName = desiredCurveName;
        fundingCurveName = desiredCurveName;
      } else {
        throw new IllegalArgumentException("forwardCurveName");
      }
    } else {
      if (fundingCurveName == null) {
        throw new IllegalArgumentException("fundingCurveName");
      } else {
        if (!desiredCurveName.equals(forwardCurveName) && !desiredCurveName.equals(fundingCurveName)) {
          //TODO put a Jira in about this stupidity
          throw new IllegalArgumentException("curveName " + desiredCurveName + " not one of forwardCurveName=" + forwardCurveName + " or fundingCurveName=" + fundingCurveName);
        }
      }
    }
    return Pairs.of(forwardCurveName, fundingCurveName);
  }

  public static Pair<String, String> getInputCurveNames(final Map<ValueSpecification, ValueRequirement> inputs) {
    String forwardCurveName = null;
    String fundingCurveName = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (ValueRequirementNames.YIELD_CURVE.equals(input.getKey().getValueName())) {
        final String curveName = input.getKey().getProperty(ValuePropertyNames.CURVE);
        if (curveName.equals(input.getValue().getConstraint(PROPERTY_FORWARD_CURVE))) {
          s_logger.debug("Using {} from advisory forward", curveName);
          forwardCurveName = curveName;
        } else if (curveName.equals(input.getValue().getConstraint(PROPERTY_FUNDING_CURVE))) {
          s_logger.debug("Using {} from advisory funding", curveName);
          fundingCurveName = curveName;
        } else {
          if ((forwardCurveName == null) && (fundingCurveName == null)) {
            s_logger.debug("Using {} for both curve names", curveName);
            forwardCurveName = curveName;
            fundingCurveName = curveName;
          } else {
            throw new IllegalArgumentException("Curves already set - inputs=" + inputs);
          }
        }
      }
    }
    ArgumentChecker.notNull(fundingCurveName, "fundingCurveName");
    ArgumentChecker.notNull(forwardCurveName, "forwardCurveName");
    return Pairs.of(forwardCurveName, fundingCurveName);
  }

}
