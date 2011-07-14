/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.CurrencyAmount;

/**
 * General converter for doubles that applies rounding rules.
 */
public class DoubleConverter implements ResultConverter<Object> {

  private static final boolean DISPLAY_UNKNOWN_CCY = false;

  // NOTE jonathan 2011-05-05 --
  // The following is actually quite generic, but it was needed in a short timescale for the web client, so is
  // currently located here. This kind of formatting logic should be moved to a more central place eventually, where
  // user configs can be taken into account, and the entire set of formatting rules can be shared between different
  // types of client.

  private static enum PrecisionType {
    SIGNIFICANT_FIGURES, DECIMAL_PLACES
  }

  private static class DoubleValueConversionSettings {

    public static final DoubleValueConversionSettings NON_CCY_5SF = DoubleValueConversionSettings.of(5,
        PrecisionType.SIGNIFICANT_FIGURES, false);
    public static final DoubleValueConversionSettings NON_CCY_2DP = DoubleValueConversionSettings.of(2,
        PrecisionType.DECIMAL_PLACES, false);
    public static final DoubleValueConversionSettings NON_CCY_4DP = DoubleValueConversionSettings.of(4,
        PrecisionType.DECIMAL_PLACES, false);
    public static final DoubleValueConversionSettings NON_CCY_6DP = DoubleValueConversionSettings.of(6,
        PrecisionType.DECIMAL_PLACES, false);
    public static final DoubleValueConversionSettings CCY_2DP = DoubleValueConversionSettings.of(2,
        PrecisionType.DECIMAL_PLACES, true);
    public static final DoubleValueConversionSettings CCY_4DP = DoubleValueConversionSettings.of(4,
        PrecisionType.DECIMAL_PLACES, true);
    public static final DoubleValueConversionSettings CCY_6DP = DoubleValueConversionSettings.of(6,
        PrecisionType.DECIMAL_PLACES, true);

    private final int _precision;
    private final PrecisionType _precisionType;
    private final boolean _isCurrencyAmount;

    public DoubleValueConversionSettings(int precision, PrecisionType precisionType, boolean isCurrencyAmount) {
      _precision = precision;
      _precisionType = precisionType;
      _isCurrencyAmount = isCurrencyAmount;
    }

    public static DoubleValueConversionSettings of(int precision, PrecisionType precisionType, boolean isCurrencyAmount) {
      return new DoubleValueConversionSettings(precision, precisionType, isCurrencyAmount);
    }

    public int getPrecision() {
      return _precision;
    }

    public PrecisionType getPrecisionType() {
      return _precisionType;
    }

    public boolean isCurrencyAmount() {
      return _isCurrencyAmount;
    }

  }

  private static final DoubleValueConversionSettings DEFAULT_CONVERSION = DoubleValueConversionSettings.NON_CCY_5SF;
  private static final Map<String, DoubleValueConversionSettings> VALUE_CONVERSION_MAP = new HashMap<String, DoubleConverter.DoubleValueConversionSettings>();

  static {
    // General
    addConversion(ValueRequirementNames.DISCOUNT_CURVE, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.YIELD_CURVE, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VOLATILITY_SURFACE, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VOLATILITY_SURFACE_DATA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.COST_OF_CARRY, DoubleValueConversionSettings.CCY_2DP);

    // Pricing
    addConversion(ValueRequirementNames.PRESENT_VALUE, DoubleValueConversionSettings.CCY_2DP);
    addConversion(ValueRequirementNames.PV01, DoubleValueConversionSettings.CCY_2DP);
    addConversion(ValueRequirementNames.PAR_RATE, DoubleValueConversionSettings.CCY_6DP);
    addConversion(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT, DoubleValueConversionSettings.CCY_6DP);
    addConversion(ValueRequirementNames.FAIR_VALUE, DoubleValueConversionSettings.CCY_4DP);
    addConversion(ValueRequirementNames.POSITION_FAIR_VALUE, DoubleValueConversionSettings.CCY_4DP);
    addConversion(ValueRequirementNames.VALUE_FAIR_VALUE, DoubleValueConversionSettings.CCY_2DP);

    // Greeks
    addConversion(ValueRequirementNames.DELTA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.DELTA_BLEED, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.STRIKE_DELTA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.GAMMA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.GAMMA_P, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.STRIKE_GAMMA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.GAMMA_BLEED, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.GAMMA_P_BLEED, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VEGA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VEGA_P, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VARIANCE_VEGA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VEGA_BLEED, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.THETA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.RHO, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CARRY_RHO, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.YIELD_CURVE_JACOBIAN, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.ULTIMA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VARIANCE_ULTIMA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.SPEED, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.SPEED_P, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VANNA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VARIANCE_VANNA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.DVANNA_DVOL, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VOMMA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VOMMA_P, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VARIANCE_VOMMA, DoubleValueConversionSettings.NON_CCY_5SF);

    // Position/value greeks
    addBulkConversion("(POSITION_|VALUE_).*", DoubleValueConversionSettings.CCY_2DP);

    // Series analysis
    addConversion(ValueRequirementNames.SKEW, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.FISHER_KURTOSIS, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.PEARSON_KURTOSIS, DoubleValueConversionSettings.NON_CCY_4DP);

    // VaR
    addConversion(ValueRequirementNames.HISTORICAL_VAR, DoubleValueConversionSettings.CCY_2DP);
    addConversion(ValueRequirementNames.PARAMETRIC_VAR, DoubleValueConversionSettings.CCY_2DP);

    // Capital Asset Pricing
    addConversion(ValueRequirementNames.CAPM_BETA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_ALPHA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_BETA, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA,
        DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA,
        DoubleValueConversionSettings.NON_CCY_5SF);

    // Traditional Risk-Reward
    addConversion(ValueRequirementNames.SHARPE_RATIO, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.TREYNOR_RATIO, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.JENSENS_ALPHA, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.TOTAL_RISK_ALPHA, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.WEIGHT, DoubleValueConversionSettings.NON_CCY_4DP);

    // Bonds
    addConversion(ValueRequirementNames.CLEAN_PRICE, DoubleValueConversionSettings.NON_CCY_6DP);
    addConversion(ValueRequirementNames.DIRTY_PRICE, DoubleValueConversionSettings.NON_CCY_6DP);
    addConversion(ValueRequirementNames.YTM, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.MARKET_YTM, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.MARKET_DIRTY_PRICE, DoubleValueConversionSettings.NON_CCY_6DP);
    addConversion(ValueRequirementNames.MACAULAY_DURATION, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.CONVEXITY, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.Z_SPREAD, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.CONVERTION_FACTOR, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.IMPLIED_REPO, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.GROSS_BASIS, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.NET_BASIS, DoubleValueConversionSettings.NON_CCY_4DP);
    addConversion(ValueRequirementNames.BOND_TENOR, DoubleValueConversionSettings.NON_CCY_2DP);
    addConversion(ValueRequirementNames.NS_BOND_CURVE, DoubleValueConversionSettings.NON_CCY_5SF);
    addConversion(ValueRequirementNames.NSS_BOND_CURVE, DoubleValueConversionSettings.NON_CCY_5SF);
    
    // FX
    addConversion(ValueRequirementNames.FX_PRESENT_VALUE, DoubleValueConversionSettings.CCY_2DP);
  }

  private static void addBulkConversion(String valueRequirementFieldNamePattern,
      DoubleValueConversionSettings conversionSettings) {
    Pattern pattern = Pattern.compile(valueRequirementFieldNamePattern);
    for (Field field : ValueRequirementNames.class.getFields()) {
      if ((field.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) == (Modifier.STATIC | Modifier.PUBLIC)
          && String.class.equals(field.getType()) && pattern.matcher(field.getName()).matches()) {
        String fieldValue;
        try {
          fieldValue = (String) field.get(null);
          addConversion(fieldValue, conversionSettings);
        } catch (Exception e) {
          // Ignore
        }
      }
    }
  }

  private static void addConversion(String valueName, DoubleValueConversionSettings conversionSettings) {
    VALUE_CONVERSION_MAP.put(valueName, conversionSettings);
  }

  //-------------------------------------------------------------------------

  //TODO putting the conversion for CurrencyAmount into here right now, but this is probably not the place for it.
  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, Object value,
      ConversionMode mode) {
    double doubleValue;
    boolean useCurrencyFromProperties;
    if (value instanceof Double) {
      doubleValue = (Double) value;
      useCurrencyFromProperties = true;
    } else {
      doubleValue = ((CurrencyAmount) value).getAmount();
      useCurrencyFromProperties = false;
    }
    String displayValue;
    DoubleValueConversionSettings conversion = getConversion(valueSpec);

    if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
      displayValue = Double.toString(doubleValue);
    } else {
      switch (conversion.getPrecisionType()) {
        case DECIMAL_PLACES:
          BigDecimal decimalValue = new BigDecimal(doubleValue);
          displayValue = decimalValue.setScale(conversion.getPrecision(), RoundingMode.HALF_UP).toString();
          break;
        case SIGNIFICANT_FIGURES:
          long maxValueForSigFig = (long) Math.pow(10, conversion.getPrecision() - 1);
          if (doubleValue > maxValueForSigFig) {
            displayValue = Long.toString(Math.round(doubleValue));
          } else {
            MathContext mathContext = new MathContext(conversion.getPrecision(), RoundingMode.HALF_UP);
            displayValue = new BigDecimal(doubleValue, mathContext).toString();
          }
          break;
        default:
          throw new IllegalArgumentException("Unsupported precision type: " + conversion.getPrecisionType());
      }
    }

    if (conversion.isCurrencyAmount()) {
      if (useCurrencyFromProperties) {
        Set<String> currencyValues = valueSpec.getProperties().getValues(ValuePropertyNames.CURRENCY);
        String ccy;
        if (currencyValues == null) {
          ccy = DISPLAY_UNKNOWN_CCY ? "?" : "";
        } else if (currencyValues.isEmpty()) {
          ccy = DISPLAY_UNKNOWN_CCY ? "*" : "";
        } else {
          ccy = currencyValues.iterator().next();
        }
        displayValue = ccy + " " + displayValue;
      } else {
        String ccy = ((CurrencyAmount) value).getCurrency().getCode();
        displayValue = ccy + " " + displayValue;
      }
    }
    return displayValue;
  }

  private DoubleValueConversionSettings getConversion(ValueSpecification valueSpec) {
    DoubleValueConversionSettings conversion = null;
    if (valueSpec != null) {
      conversion = VALUE_CONVERSION_MAP.get(valueSpec.getValueName());
    }
    if (conversion == null) {
      conversion = DEFAULT_CONVERSION;
    }
    return conversion;
  }

  //TODO putting the conversion for CurrencyAmount into here right now, but this is probably not the place for it.
  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, Object value) {
    double doubleValue;
    if (value instanceof Double) {
      doubleValue = (Double) value;
    } else if (value instanceof CurrencyAmount) {
      doubleValue = ((CurrencyAmount) value).getAmount();
    } else {
      throw new OpenGammaRuntimeException("Cannot convert objects of type " + value.getClass());
    }
    //REVIEW emcleod 7-6-2011 This is awful - 0 is a legitimate value to return, whereas NaN or infinity show an error in the calculation
    if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
      doubleValue = 0; 
    }
    return doubleValue;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, Object value) {
    // Full value
    return value.toString();
  }
  
  @Override
  public String getFormatterName() {
    return "DOUBLE";
  }

}
