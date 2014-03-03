/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import static com.opengamma.web.analytics.formatting.ResultsFormatter.CurrencyDisplay.DISPLAY_CURRENCY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.calcnode.MissingValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ClassMap;
import com.opengamma.web.analytics.ValueTypes;

/**
 * Formats analytics data for display in a grid in the user interface or for transmitting to the client as history.
 * Data structures bigger than a single value are encoded as JSON.
 */
@SuppressWarnings("rawtypes")
public class ResultsFormatter {

  /**
   * Enum indicating whether the currency code should be included in the output
   * when values are formatted.
   */
  public enum CurrencyDisplay {
    /**
     * Include the currency code in formatted outputs (if one is available).
     */
    DISPLAY_CURRENCY,
    /**
     * Do not include the currency code in formatted outputs.
     */
    SUPPRESS_CURRENCY
  }

  /**
   * Marker value returned to indicate there is no formatted value available for a combination of value, formatter
   * and inline key. This can happen for cells displaying inline values where the underlying value has no entry
   * for the column's key. It is also possible for complex values (e.g. vectors) where the history is stored
   * but can only be formatted when displayed inline as single values.
   */
  public static final Object VALUE_UNAVAILABLE = new Object() {

    /**
     * @return An empty string - this means the grid shows an empty cell for an unavailable value.
     */
    @Override
    public String toString() {
      return "";
    }
  };

  private static final Logger s_logger = LoggerFactory.getLogger(ResultsFormatter.class);

  /** For formatting null values. */
  private final TypeFormatter _nullFormatter = new NullFormatter();
  /** For formatting values with no specific formatter. */
  private final TypeFormatter _defaultFormatter = new DefaultFormatter();
  /** Formatters keyed on the type of value they can format. */
  private final ClassMap<TypeFormatter<?>> _formatters = new ClassMap<>();
  /** Formatter for values whose type isn't know in advance or whose type can changes between calculation cycles. */
  private final UnknownTypeFormatter _unknownTypeFormatter = new UnknownTypeFormatter();

  public ResultsFormatter() {
    this(DISPLAY_CURRENCY);
  }

  public ResultsFormatter(CurrencyDisplay currencyDisplay) {
    BigDecimalFormatter bigDecimalFormatter = new BigDecimalFormatter(currencyDisplay);
    DoubleFormatter doubleFormatter = new DoubleFormatter(bigDecimalFormatter);
    CurrencyAmountFormatter currencyAmountFormatter = new CurrencyAmountFormatter(currencyDisplay, bigDecimalFormatter);
    ZonedDateTimeFormatter zonedDateTimeFormatter = new ZonedDateTimeFormatter();
    LocalDateDoubleTimeSeriesFormatter localDateDoubleTimeSeriesFormatter = new LocalDateDoubleTimeSeriesFormatter();
    RateFormatter rateFormatter = new RateFormatter();
    BasisPointsFormatter basisPointFormatter = new BasisPointsFormatter();
    addFormatters(doubleFormatter,
                  bigDecimalFormatter,
                  currencyAmountFormatter,
                  zonedDateTimeFormatter,
                  localDateDoubleTimeSeriesFormatter,
                  new YieldCurveFormatter(),
                  new PriceIndexCurveFormatter(),
                  new ISDACompliantYieldCurveFormatter(),
                  new ISDACompliantCurveFormatter(),
                  new NodalObjectsCurveFormatter(), //TODO is not a general formatter - used only for (Tenor, Double) curves
                  new VolatilityCubeDataFormatter(),
                  new VolatilitySurfaceDataFormatter(),
                  new VolatilitySurfaceFormatter(),
                  new LabelledMatrix1DFormatter(doubleFormatter),
                  new LocalDateLabelledMatrix1DFormatter(doubleFormatter),
                  new LabelledMatrix2DFormatter(doubleFormatter),
                  new LabelledMatrix3DFormatter(),
                  new TenorLabelledLocalDateDoubleTimeSeriesMatrix1DFormatter(localDateDoubleTimeSeriesFormatter),
                  new TenorFormatter(),
                  new MultipleCurrencyAmountFormatter(doubleFormatter),
                  new MissingInputFormatter(),
                  new MissingOutputFormatter(),
                  new ForwardCurveFormatter(),
                  new BlackVolatilitySurfaceMoneynessFormatter(),
                  new LocalVolatilitySurfaceMoneynessFormatter(),
                  new BucketedGreekResultCollectionFormatter(),
                  new DoublesCurveFormatter(),
                  new HistoricalTimeSeriesFormatter(),
                  new DoubleArrayFormatter(),
                  new DoubleObjectArrayFormatter(),
                  new FudgeMsgFormatter(),
                  new ListDoubleArrayFormatter(),
                  new PresentValueForexBlackVolatilitySensitivityFormatter(),
                  new SnapshotDataBundleFormatter(doubleFormatter),
                  new InterpolatedYieldCurveSpecificationWithSecuritiesFormatter(),
                  new HistoricalTimeSeriesBundleFormatter(),
                  new VolatilitySurfaceSpecificationFormatter(),
                  new CurrencyPairsFormatter(),
                  new NodeTargetFormatter(),
                  new PositionTargetFormatter(),
                  new FungibleTradeTargetFormatter(),
                  new OtcTradeTargetFormatter(),
                  new BlackVolatilitySurfaceMoneynessFcnBackedByGridFormatter(),
                  new FrequencyFormatter(),
                  new FXAmountsFormatter(doubleFormatter),
                  new ExpiryFormatter(zonedDateTimeFormatter),
                  new ValuePropertiesFormatter(),
                  new FixedPaymentMatrixFormatter(currencyAmountFormatter),
                  new FloatingPaymentMatrixFormatter(currencyAmountFormatter),
                  new FixedSwapLegDetailsFormatter(new CurrencyAmountFormatter(CurrencyDisplay.SUPPRESS_CURRENCY, bigDecimalFormatter), rateFormatter),
                  new FloatingSwapLegDetailsFormatter(new CurrencyAmountFormatter(CurrencyDisplay.SUPPRESS_CURRENCY, bigDecimalFormatter), rateFormatter, basisPointFormatter),
                  new FXMatrixFormatter(),
                  new YieldCurveDataFormatter(doubleFormatter));
  }

  private void addFormatters(TypeFormatter<?>... formatters) {
    for (TypeFormatter<?> formatter : formatters) {
      _formatters.put(formatter.getType(), formatter);
    }
  }

  private TypeFormatter getFormatter(Object value, ValueSpecification valueSpec) {
    if (value == null) {
      return _nullFormatter;
    } else if (isError(value) || valueSpec == null) {
      return getFormatterForType(value.getClass());
    } else {
      Class<?> type = ValueTypes.getTypeForValueName(valueSpec.getValueName());
      if (type != null) {
        if (type.isInstance(value)) {
          return getFormatterForType(type);
        } else {
          // this happens if ValueTypes has a type for a value name but the actual value produced has a different type.
          // there are several possible causes:
          //   1) the type produced for a value has been changed (e.g. the function that produces it has been modified)
          //      but the ValueTypes config hasn't been updated to match
          //   2) the type produced for the value can change from cycle to cycle. to fix this the ValueTypes config
          //      should be modified to specify a supertype of all possible types. if there isn't a common supertype
          //      with a formatter that works for all possible values then the value name can be removed from the
          //      ValueTypes config. this will give the value name a type of UNKNOWN and the type and formatting will
          //      be decided from the value after every cycle
          //   3) the type produced for the value is always the same but the value is converted to a different type
          //      by Fudge depending on the value. e.g. an integer will be encoded as a byte if it is small enough
          //      but will be encoded as an integer if it won't fit into a byte. the fix for this scenario is the same
          //      as #2 above
          s_logger.warn("Unexpected type for value. Value name: '{}', expected type: {}, actual type: {}, value: {}",
                        new Object[]{valueSpec.getValueName(), type.getName(), value.getClass().getName(), value});
        }
      }
      return getFormatterForType(value.getClass());
    }
  }

  private TypeFormatter getFormatterForType(Class<?> type) {
    if (type == null) {
      return _unknownTypeFormatter;
    }
    TypeFormatter formatter = _formatters.get(type);
    if (formatter == null) {
      return _defaultFormatter;
    } else {
      return formatter;
    }
  }

  private static boolean isError(Object value) {
    return value instanceof MissingValue;
  }

  /**
   * Formats a value for conversion to JSON and sending to the client.
   * @param value The value to be formatted, possibly null
   * @param valueSpec The specification of the value, null if the value wasn't calculated by the engine
   * @param format The type of formatting
   * @param inlineKey The key for extracting a single value from the value, possibly null. This is used for values
   * that can be displayed inline across multiple cells, e.g. vectors of doubles that are displayed across multiple
   * columns of double values.
   * @return The formatted value. Can be null (indicating an error) or {@link #VALUE_UNAVAILABLE} if the object
   * can't be formatted as requested or if the value doesn't have an entry for the specified key.
   */
  @SuppressWarnings("unchecked")
  public Object format(Object value, ValueSpecification valueSpec, TypeFormatter.Format format, Object inlineKey) {
    TypeFormatter formatter = getFormatter(value, valueSpec);
    return formatter.format(value, valueSpec, format, inlineKey);
  }

  /**
   * Returns the format type for a value type.
   * @param type The value type
   * @return The formatter used for formatting the type
   */
  public DataType getDataType(Class<?> type) {
    return getFormatterForType(type).getDataType();
  }

  /**
   * Returns the format type for a value.
   * @param value The value, possibly null
   * @param valueSpec The value's specification, possibly null
   * @return The format type for the value, not null
   */
  @SuppressWarnings("unchecked")
  public DataType getDataTypeForValue(Object value, ValueSpecification valueSpec) {
    return getFormatter(value, valueSpec).getDataTypeForValue(value);
  }
}
