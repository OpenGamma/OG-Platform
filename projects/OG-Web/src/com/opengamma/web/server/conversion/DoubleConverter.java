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
import java.text.DecimalFormatSymbols;
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
  
  private abstract static class DoubleValueFormatter {
    
    private static final int GROUP_SIZE = 3;
    
    private static final char s_decimalSeparator;
    private static final char s_groupingSeparator;
    
    private final boolean _isCurrencyAmount;
    
    static {
      DecimalFormatSymbols formatSymbols = DecimalFormatSymbols.getInstance();
      s_decimalSeparator = formatSymbols.getDecimalSeparator();
      s_groupingSeparator = formatSymbols.getGroupingSeparator();
    }

    public DoubleValueFormatter(boolean isCurrencyAmount) {
      _isCurrencyAmount = isCurrencyAmount;
    }
    
    public boolean isCurrencyAmount() {
      return _isCurrencyAmount;
    }
    
    protected abstract String formatPlainString(double value);
    
    public String format(double value) {
      String plainString = formatPlainString(value);
      return addGroupings(plainString);
    }
    
    private String addGroupings(String plainNumber) {
      int decimalIdx = plainNumber.indexOf(s_decimalSeparator);
      String integerPart = decimalIdx > -1 ? plainNumber.substring(0, decimalIdx) : plainNumber;
      int minusOffset = integerPart.charAt(0) == '-' ? 1 : 0;
      int integerPartLength = integerPart.length() - minusOffset;
      StringBuilder sb = new StringBuilder();
      int firstGroupEndIdx = integerPartLength % GROUP_SIZE;
      if (firstGroupEndIdx == 0) {
        firstGroupEndIdx = GROUP_SIZE;
      }
      firstGroupEndIdx += minusOffset;
      if (firstGroupEndIdx == integerPartLength) {
        // No groups needed
        return plainNumber;
      }
      sb.append(integerPart.substring(0, firstGroupEndIdx));
      for (int i = firstGroupEndIdx; i < integerPartLength + minusOffset; i += GROUP_SIZE) {
        sb.append(s_groupingSeparator).append(integerPart.substring(i, i + 3));
      }
      if (decimalIdx > -1) {
        sb.append(s_decimalSeparator);
        sb.append(plainNumber.substring(decimalIdx + 1));        
      }
      return sb.toString();
    }
    
  }

  private static class DoubleValueDecimalPlaceFormatter extends DoubleValueFormatter {

    public static final DoubleValueDecimalPlaceFormatter NON_CCY_2DP = DoubleValueDecimalPlaceFormatter.of(2, false);
    public static final DoubleValueDecimalPlaceFormatter NON_CCY_4DP = DoubleValueDecimalPlaceFormatter.of(4, false);
    public static final DoubleValueDecimalPlaceFormatter NON_CCY_6DP = DoubleValueDecimalPlaceFormatter.of(6, false);
    public static final DoubleValueDecimalPlaceFormatter CCY_2DP = DoubleValueDecimalPlaceFormatter.of(2, true);
    public static final DoubleValueDecimalPlaceFormatter CCY_4DP = DoubleValueDecimalPlaceFormatter.of(4, true);
    public static final DoubleValueDecimalPlaceFormatter CCY_6DP = DoubleValueDecimalPlaceFormatter.of(6, true);

    private final int _decimalPlaces;
    
    public DoubleValueDecimalPlaceFormatter(int decimalPlaces, boolean isCurrencyAmount) {
      super(isCurrencyAmount);
      _decimalPlaces = decimalPlaces;
    }

    public static DoubleValueDecimalPlaceFormatter of(int decimalPlaces, boolean isCurrencyAmount) {
      return new DoubleValueDecimalPlaceFormatter(decimalPlaces, isCurrencyAmount);
    }
    
    @Override
    public String formatPlainString(double value) {
      BigDecimal bd = BigDecimal.valueOf(value);
      bd = bd.setScale(_decimalPlaces, RoundingMode.HALF_UP);
      return bd.toPlainString();
    }

  }
  
  private static class DoubleValueSignificantFiguresFormatter extends DoubleValueFormatter {
    
    public static final DoubleValueSignificantFiguresFormatter NON_CCY_5SF = DoubleValueSignificantFiguresFormatter.of(5, false);
    
    private final long _maxValueForSigFig;
    private final MathContext _sigFigMathContext;
    
    public DoubleValueSignificantFiguresFormatter(int significantFigures, boolean isCurrencyAmount) {
      super(isCurrencyAmount);
      _maxValueForSigFig = (long) Math.pow(10, significantFigures - 1);
      _sigFigMathContext = new MathContext(significantFigures, RoundingMode.HALF_UP);
    }
    
    public static DoubleValueSignificantFiguresFormatter of(int significantFigures, boolean isCurrencyAmount) {
      return new DoubleValueSignificantFiguresFormatter(significantFigures, isCurrencyAmount);
    }
    
    @Override
    public String formatPlainString(double value) {
      if (value > _maxValueForSigFig) {
        return Long.toString(Math.round(value));
      } else {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.round(_sigFigMathContext);
        return bd.toPlainString();
      }
    }
    
  }

  private static final DoubleValueFormatter DEFAULT_CONVERSION = DoubleValueSignificantFiguresFormatter.NON_CCY_5SF;
  private static final Map<String, DoubleValueFormatter> VALUE_CONVERSION_MAP = new HashMap<String, DoubleConverter.DoubleValueFormatter>();

  static {   
    // General
    addConversion(ValueRequirementNames.DISCOUNT_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.YIELD_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VOLATILITY_SURFACE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.VOLATILITY_SURFACE_DATA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.COST_OF_CARRY, DoubleValueDecimalPlaceFormatter.CCY_2DP);

    // Pricing
    addConversion(ValueRequirementNames.PRESENT_VALUE, DoubleValueDecimalPlaceFormatter.CCY_2DP);
    addConversion(ValueRequirementNames.PV01, DoubleValueDecimalPlaceFormatter.CCY_2DP);
    addConversion(ValueRequirementNames.PAR_RATE, DoubleValueDecimalPlaceFormatter.CCY_6DP);
    addConversion(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT, DoubleValueDecimalPlaceFormatter.CCY_6DP);
    addConversion(ValueRequirementNames.FAIR_VALUE, DoubleValueDecimalPlaceFormatter.CCY_4DP);
    addConversion(ValueRequirementNames.POSITION_FAIR_VALUE, DoubleValueDecimalPlaceFormatter.CCY_4DP);
    addConversion(ValueRequirementNames.VALUE_FAIR_VALUE, DoubleValueDecimalPlaceFormatter.CCY_2DP);

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
    addBulkConversion("(POSITION_|VALUE_).*", DoubleValueDecimalPlaceFormatter.CCY_2DP);

    // Series analysis
    addConversion(ValueRequirementNames.SKEW, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.FISHER_KURTOSIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    addConversion(ValueRequirementNames.PEARSON_KURTOSIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);

    // VaR
    addConversion(ValueRequirementNames.HISTORICAL_VAR, DoubleValueDecimalPlaceFormatter.CCY_2DP);
    addConversion(ValueRequirementNames.PARAMETRIC_VAR, DoubleValueDecimalPlaceFormatter.CCY_2DP);

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
    addConversion(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA,
        DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    addConversion(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA,
        DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

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
    addConversion(ValueRequirementNames.FX_PRESENT_VALUE, DoubleValueDecimalPlaceFormatter.CCY_2DP);
  }

  private static void addBulkConversion(String valueRequirementFieldNamePattern,
      DoubleValueDecimalPlaceFormatter conversionSettings) {
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
    DoubleValueFormatter formatter = getFormatter(valueSpec);

    if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
      displayValue = Double.toString(doubleValue);
    } else {
      displayValue = formatter.format(doubleValue);
    }

    if (formatter.isCurrencyAmount()) {
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
