/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.Pair;

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

  private static final DoubleValueFormatter DEFAULT_CONVERSION = DoubleValueSignificantFiguresFormatter.NON_CCY_5SF;
  private static final Map<String, DoubleValueFormatter> VALUE_CONVERSION_MAP = new HashMap<String, DoubleValueFormatter>();

  static {   
    // General
    addConversion(ValueRequirementNames.DISCOUNT_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.YIELD_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VOLATILITY_SURFACE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VOLATILITY_SURFACE_DATA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.COST_OF_CARRY, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Pricing
    addConversion(ValueRequirementNames.PRESENT_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    addConversion(ValueRequirementNames.PV01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    addConversion(ValueRequirementNames.PAR_RATE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    addConversion(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    addConversion(ValueRequirementNames.FAIR_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    addConversion(ValueRequirementNames.POSITION_FAIR_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    addConversion(ValueRequirementNames.VALUE_FAIR_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    addConversion(ValueRequirementNames.SECURITY_MARKET_PRICE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    
    // PnL
    addConversion(ValueRequirementNames.PNL, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    addConversion(ValueRequirementNames.DAILY_PNL, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Greeks
    addConversion(ValueRequirementNames.DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.DELTA_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.STRIKE_DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.GAMMA_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.STRIKE_GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.GAMMA_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.GAMMA_P_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VEGA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VEGA_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VARIANCE_VEGA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VEGA_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.THETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.RHO, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CARRY_RHO, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.YIELD_CURVE_JACOBIAN, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.ULTIMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VARIANCE_ULTIMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.SPEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.SPEED_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VANNA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VARIANCE_VANNA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.DVANNA_DVOL, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VOMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VOMMA_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VARIANCE_VOMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // Position/value greeks
    addBulkConversion("(POSITION_|VALUE_).*", DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Series analysis
    addConversion(ValueRequirementNames.SKEW, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.FISHER_KURTOSIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.PEARSON_KURTOSIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);

    // VaR
    addConversion(ValueRequirementNames.HISTORICAL_VAR, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    addConversion(ValueRequirementNames.PARAMETRIC_VAR, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Capital Asset Pricing
    addConversion(ValueRequirementNames.CAPM_BETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_ALPHA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_BETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // Traditional Risk-Reward
    addConversion(ValueRequirementNames.SHARPE_RATIO, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.TREYNOR_RATIO, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.JENSENS_ALPHA, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.TOTAL_RISK_ALPHA, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.WEIGHT, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);

    // Bonds
    addConversion(ValueRequirementNames.CLEAN_PRICE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    addConversion(ValueRequirementNames.DIRTY_PRICE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    addConversion(ValueRequirementNames.YTM, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.MARKET_YTM, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.MARKET_DIRTY_PRICE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    addConversion(ValueRequirementNames.MACAULAY_DURATION, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.CONVEXITY, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.Z_SPREAD, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.CONVERTION_FACTOR, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.IMPLIED_REPO, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.GROSS_BASIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.NET_BASIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.BOND_TENOR, DoubleValueDecimalPlaceFormatter.NON_CCY_2DP);
    addConversion(ValueRequirementNames.NS_BOND_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.NSS_BOND_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    
    // FX
    addConversion(ValueRequirementNames.FX_PRESENT_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
  }

  private static void addBulkConversion(String valueRequirementFieldNamePattern,
      DoubleValueFormatter conversionSettings) {
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

  private static void addConversion(String valueName, DoubleValueFormatter conversionSettings) {
    VALUE_CONVERSION_MAP.put(valueName, conversionSettings);
  }

  //-------------------------------------------------------------------------

  //TODO putting the conversion for CurrencyAmount into here right now, but this is probably not the place for it.
  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, Object value, ConversionMode mode) {
    String displayValue = null;
    
    Pair<Double, BigDecimal> processedValue = processValue(value);
    Double doubleValue = processedValue.getFirst();
    BigDecimal bigDecimalValue = processedValue.getSecond();

    if (doubleValue != null) {
      if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
        displayValue = Double.toString(doubleValue);
      } else {
        bigDecimalValue = BigDecimal.valueOf(doubleValue);
      }
    }
    
    DoubleValueFormatter formatter = getFormatter(valueSpec);
    
    if (displayValue == null) {
      assert bigDecimalValue != null;
      displayValue = formatter.format(bigDecimalValue);
    }
    
    if (formatter.isCurrencyAmount()) {
      String ccy;
      if (value instanceof CurrencyAmount) {
        ccy = ((CurrencyAmount) value).getCurrency().getCode();
      } else {
        Set<String> currencyValues = valueSpec.getProperties().getValues(ValuePropertyNames.CURRENCY);
        if (currencyValues == null) {
          ccy = DISPLAY_UNKNOWN_CCY ? "?" : "";
        } else if (currencyValues.isEmpty()) {
          ccy = DISPLAY_UNKNOWN_CCY ? "*" : "";
        } else {
          ccy = currencyValues.iterator().next();
        }
      }
      displayValue = ccy + " " + displayValue;
    }
    return displayValue;
  }

  //TODO putting the conversion for CurrencyAmount into here right now, but this is probably not the place for it.
  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, Object value) {
    Pair<Double, BigDecimal> processedValue = processValue(value);
    Double doubleValue = processedValue.getFirst();
    BigDecimal bigDecimalValue = processedValue.getSecond();
    
    if (doubleValue != null) {
      if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
        //REVIEW emcleod 7-6-2011 This is awful - 0 is a legitimate value to return, whereas NaN or infinity show an error in the calculation
        bigDecimalValue = BigDecimal.ZERO; 
      } else {
        bigDecimalValue = BigDecimal.valueOf(doubleValue);
      }
    }

    DoubleValueFormatter formatter = getFormatter(valueSpec);
    return formatter.getRoundedValue(bigDecimalValue);
  }
  
  private Pair<Double, BigDecimal> processValue(Object value) {
    if (value instanceof Double) {
      return Pair.of((Double) value, null);
    }
    if (value instanceof CurrencyAmount) {
      CurrencyAmount currencyAmount = (CurrencyAmount) value;
      return Pair.of((Double) currencyAmount.getAmount(), null);
    }
    if (value instanceof BigDecimal) {
      return Pair.of(null, ((BigDecimal) value));
    }
    throw new OpenGammaRuntimeException(getClass().getSimpleName() + " is unable to process value of type " + value.getClass());
  }

  private DoubleValueFormatter getFormatter(ValueSpecification valueSpec) {
    DoubleValueFormatter conversion = null;
    if (valueSpec != null) {
      conversion = VALUE_CONVERSION_MAP.get(valueSpec.getValueName());
    }
    if (conversion == null) {
      conversion = DEFAULT_CONVERSION;
    }
    return conversion;
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
