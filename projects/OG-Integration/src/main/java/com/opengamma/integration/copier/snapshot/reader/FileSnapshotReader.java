/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.reader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableYieldCurveSnapshot;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.copier.sheet.reader.CsvSheetReader;
import com.opengamma.integration.copier.snapshot.SnapshotColumns;
import com.opengamma.integration.copier.snapshot.SnapshotType;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Reads a snapshot from an imported file
 */
public class FileSnapshotReader implements SnapshotReader {

  private static final Logger s_logger = LoggerFactory.getLogger(FileSnapshotReader.class);

  private CsvSheetReader _sheetReader;
  private Map<CurveKey, CurveSnapshot> _curves;
  private UnstructuredMarketDataSnapshot _global;
  private Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> _surface;
  private Map<YieldCurveKey, YieldCurveSnapshot> _yieldCurve;
  private String _name;
  private String _basisName;
  private static final String MARKET_ALL = "market_all";
  private final FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();

  public FileSnapshotReader(String filename) {
    _sheetReader = new CsvSheetReader(filename);
    iterateSheetRows();
  }

  private void iterateSheetRows() {

    _curves = new HashMap<>();
    _surface = new HashMap<>();
    _yieldCurve = new HashMap<>();

    //Temporary maps for data structures
    HashMap<String, ManageableCurveSnapshot> curveBuilder = new HashMap<>();
    HashMap<String, Pair<YieldCurveKey, ManageableYieldCurveSnapshot>> yieldCurveBuilder = new HashMap<>();
    HashMap<String, Pair<VolatilitySurfaceKey, ManageableVolatilitySurfaceSnapshot>> surfaceBuilder = new HashMap<>();
    ManageableUnstructuredMarketDataSnapshot globalBuilder = new ManageableUnstructuredMarketDataSnapshot();

    while (true) {
      Map<String, String> currentRow = _sheetReader.loadNextRow();

      // When rows are complete create snapshot elements from temporary structures
      if (currentRow == null) {
        for (Map.Entry<String, ManageableCurveSnapshot> entry : curveBuilder.entrySet()) {
          _curves.put(CurveKey.of(entry.getKey()), entry.getValue());
        }
        for (Map.Entry<String, Pair<YieldCurveKey, ManageableYieldCurveSnapshot>> entry : yieldCurveBuilder.entrySet()) {
          _yieldCurve.put(entry.getValue().getFirst(), entry.getValue().getSecond());
        }
        for (Map.Entry<String, Pair<VolatilitySurfaceKey, ManageableVolatilitySurfaceSnapshot>> entry : surfaceBuilder.entrySet()) {
          _surface.put(entry.getValue().getFirst(), entry.getValue().getSecond());
        }
        _global = globalBuilder;
        return;
      }

      String type = currentRow.get(SnapshotColumns.TYPE.get());
      SnapshotType snapshotType = SnapshotType.from(type);

      if (snapshotType != null) {
        switch (snapshotType) {
          case NAME:
            _name = currentRow.get(SnapshotColumns.NAME.get());
            break;
          case BASIS_NAME:
            _basisName = currentRow.get(SnapshotColumns.NAME.get());
            break;
          case CURVE:
            buildCurves(curveBuilder, currentRow);
            break;
          case YIELD_CURVE:
            buildYieldCurves(yieldCurveBuilder, currentRow);
            break;
          case GLOBAL_VALUES:
            buildMarketDataSnapshot(globalBuilder, currentRow);
            break;
          case VOL_SURFACE:
            buildSurface(surfaceBuilder, currentRow);
            break;
        }
      } else {
        s_logger.error("Unknown snapshot element of type {}", type);
      }
    }
  }

  /**
   * Utility method to collect Market_All data, stored as the MarketValue of the
   * ManageableUnstructuredMarketDataSnapshot, either by creating a FudgeMsg or adding to current FudgeMsg
   */
  private void buildMarketAll(ManageableUnstructuredMarketDataSnapshot snapshot, Map<String, String> currentRow) {
    ValueSnapshot valueSnapshot =  snapshot.getValue(createExternalIdBundle(currentRow),
                                                          currentRow.get(SnapshotColumns.VALUE_NAME.get()));
    String valueObject = currentRow.get(SnapshotColumns.VALUE_OBJECT.get());

    if (valueSnapshot == null) {
      if (valueObject.equalsIgnoreCase("null")) {
        snapshot.putValue(createExternalIdBundle(currentRow),
                          currentRow.get(SnapshotColumns.VALUE_NAME.get()),
                          null);
      } else {
        MutableFudgeMsg msg = _fudgeContext.newMessage();
        msg.add(valueObject, marketAllTypeForFudgeMessage(currentRow));
        snapshot.putValue(createExternalIdBundle(currentRow),
                          currentRow.get(SnapshotColumns.VALUE_NAME.get()),
                          ValueSnapshot.of(msg));
      }

    } else {
      MutableFudgeMsg msg = (MutableFudgeMsg) valueSnapshot.getMarketValue();
      msg.add(valueObject, marketAllTypeForFudgeMessage(currentRow));
    }
  }

  private Object marketAllTypeForFudgeMessage(Map<String, String> currentRow) {
    Object output = null;
    String input = currentRow.get(SnapshotColumns.MARKET_VALUE.get());

    if (input != null) {
      if (NumberUtils.isNumber(input)) {
        output = NumberUtils.createDouble(input);
      } else if (isValidInstant(input)) {
        output = Instant.parse(input);
      } else if (isValidLocalDate(input)) {
        output = LocalDate.parse(input);
      } else {
        output = input;
      }
    }
    return output;
  }

  private Boolean isValidInstant(String input) {
    try {
      Instant.parse(input);
    } catch (DateTimeParseException e) {
      return false;
    }
    return true;
  }

  private Boolean isValidLocalDate(String input) {
    try {
      LocalDate.parse(input);
    } catch (DateTimeParseException e) {
      return false;
    }
    return true;
  }


  /**
   * Utility method to collect ManageableUnstructuredMarketDataSnapshot data, used by
   * GLOBAL_VALUES, YIELD_CURVE and CURVE
   */
  private void buildMarketDataSnapshot(ManageableUnstructuredMarketDataSnapshot snapshot,
                                       Map<String, String> currentRow) {

    // Special case for Market_All
    if (currentRow.get(SnapshotColumns.VALUE_NAME.get()).equalsIgnoreCase(MARKET_ALL)) {
      buildMarketAll(snapshot, currentRow);
    } else {
      snapshot.putValue(createExternalIdBundle(currentRow),
                             currentRow.get(SnapshotColumns.VALUE_NAME.get()),
                             createValueSnapshot(currentRow));
    }
  }

  private void buildSurface(HashMap<String, Pair<VolatilitySurfaceKey, ManageableVolatilitySurfaceSnapshot>> surfaceBuilder,
                            Map<String, String> currentRow) {
    String target = currentRow.get(SnapshotColumns.SURFACE_TARGET.get());

    if (!surfaceBuilder.containsKey(target)) {
      ManageableVolatilitySurfaceSnapshot surface = new ManageableVolatilitySurfaceSnapshot();
      VolatilitySurfaceKey key = VolatilitySurfaceKey.of(UniqueId.parse(currentRow.get(SnapshotColumns.SURFACE_TARGET.get())),
                                                          currentRow.get(SnapshotColumns.NAME.get()),
                                                          currentRow.get(SnapshotColumns.SURFACE_INSTRUMENT_TYPE.get()),
                                                          currentRow.get(SnapshotColumns.SURFACE_QUOTE_TYPE.get()),
                                                          currentRow.get(SnapshotColumns.SURFACE_QUOTE_UNITS.get()));
      HashMap<Pair<Object, Object>, ValueSnapshot> values = new HashMap<Pair<Object, Object>, ValueSnapshot>();

      values.put(createOrdinatePair(currentRow), createValueSnapshot(currentRow));
      surface.setValues(values);
      surfaceBuilder.put(target, Pairs.of(key, surface));
    } else {
      surfaceBuilder.get(target).getSecond().getValues().put(createOrdinatePair(currentRow),
                                                           createValueSnapshot(currentRow));
    }

  }

  private void buildYieldCurves(HashMap<String, Pair<YieldCurveKey, ManageableYieldCurveSnapshot>> yieldCurveBuilder,
                                Map<String, String> currentRow) {
    String name = currentRow.get(SnapshotColumns.NAME.get());

    if (!yieldCurveBuilder.containsKey(name)) {
      
      ManageableUnstructuredMarketDataSnapshot snapshot = new ManageableUnstructuredMarketDataSnapshot();
      YieldCurveKey key = YieldCurveKey.of(Currency.of(currentRow.get(SnapshotColumns.YIELD_CURVE_CURRENCY.get())),
                                            currentRow.get(SnapshotColumns.NAME.get()));

      buildMarketDataSnapshot(snapshot, currentRow);
      ManageableYieldCurveSnapshot curve = ManageableYieldCurveSnapshot.of(Instant.parse(currentRow.get(SnapshotColumns.INSTANT.get())), snapshot);
      yieldCurveBuilder.put(name, Pairs.of(key, curve));
    } else {
      ManageableUnstructuredMarketDataSnapshot existingSnapshot = yieldCurveBuilder.get(name).getSecond().getValues();
      buildMarketDataSnapshot(existingSnapshot, currentRow);
    }
  }

  private void buildCurves(HashMap<String, ManageableCurveSnapshot> curvesBuilder, Map<String, String> currentRow) {
    String name = currentRow.get(SnapshotColumns.NAME.get());

    if (!curvesBuilder.containsKey(name)) {
      ManageableCurveSnapshot curve = new ManageableCurveSnapshot();
      ManageableUnstructuredMarketDataSnapshot snapshot = new ManageableUnstructuredMarketDataSnapshot();

      curve.setValuationTime(Instant.parse(currentRow.get(SnapshotColumns.INSTANT.get())));
      buildMarketDataSnapshot(snapshot, currentRow);
      curve.setValues(snapshot);
      curvesBuilder.put(name, curve);
    } else {
      ManageableUnstructuredMarketDataSnapshot existingSnapshot = curvesBuilder.get(name).getValues();
      buildMarketDataSnapshot(existingSnapshot, currentRow);
    }

  }

  private Pair<Object, Object> createOrdinatePair(Map<String, String> currentRow) {
    String x = currentRow.get(SnapshotColumns.SURFACE_X.get());
    String[] y = currentRow.get(SnapshotColumns.SURFACE_Y.get()).split("\\|");
    Object surfaceX = null;
    Object surfaceY = null;

    if (x != null) {
      if (NumberUtils.isNumber(x)) {
        surfaceX = NumberUtils.createDouble(x);
      } else {
        try {
          surfaceX = Tenor.parse(x);
        } catch (IllegalArgumentException e)  {
          s_logger.error("Volatility surface X ordinate {} should be a Double, Tenor or empty.", x);
        }
      }
    }

    if (y != null) {
      if (y.length == 1 && NumberUtils.isNumber(y[0])) {
        surfaceY = NumberUtils.createDouble(y[0]);
      } else {
        try {
          surfaceY = createYOrdinatePair(y);
        } catch (IllegalArgumentException e)  {
          s_logger.error("Volatility surface Y ordinate {} should be a Double, Pair<Number, FXVolQuoteType> or empty.", x);
        }
      }
    }

    return Pairs.of(surfaceX, surfaceY);
  }

  // Bloomberg FX option volatility surface codes given a tenor, quote type (ATM, butterfly, risk reversal) and distance from ATM.
  private Pair<Number, BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType> createYOrdinatePair(String[] yPair) {
    Number firstElement = null;
    BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType secondElement = null;
    if (NumberUtils.isNumber(yPair[0])) {
      firstElement = NumberUtils.createDouble(yPair[0]);
    }
    switch (yPair[1]) {
      case "ATM":
        secondElement = BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType.ATM;
        break;
      case "RISK_REVERSAL":
        secondElement = BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType.RISK_REVERSAL;
        break;
      case "BUTTERFLY":
        secondElement = BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType.BUTTERFLY;
        break;
    }
    return Pairs.of(firstElement, secondElement);
  }

  private ValueSnapshot createValueSnapshot(Map<String, String> currentRow) {
    String market = currentRow.get(SnapshotColumns.MARKET_VALUE.get());
    String override = currentRow.get(SnapshotColumns.OVERRIDE_VALUE.get());
    String valueObject = currentRow.get(SnapshotColumns.VALUE_OBJECT.get());
    Object marketValue = null;
    Object overrideValue = null;

    //preserve null valueSnapshots
    if (valueObject != null && valueObject.equalsIgnoreCase("null")) {
      return null;
    }

    // marketValue can only be Double, LocalDate, empty or (FudgeMsg which is special cased for Market_All)
    if (market != null) {
      if (NumberUtils.isNumber(market)) {
        marketValue = NumberUtils.createDouble(market);
      } else {
        try {
          marketValue = LocalDate.parse(market);
        } catch (IllegalArgumentException e)  {
          s_logger.error("Market value {} should be a Double, LocalDate or empty.", market);
        }
      }
    }

    //overrideValue can only be Double, LocalDate or empty
    if (override != null) {
      if (NumberUtils.isNumber(override)) {
        overrideValue = NumberUtils.createDouble(override);
      } else {
        try {
          overrideValue = LocalDate.parse(override);
        } catch (IllegalArgumentException e)  {
          s_logger.error("Override value {} should be a Double, LocalDate or empty.", override);
        }
      }
    }

    return ValueSnapshot.of(marketValue, overrideValue);
  }

  private ExternalIdBundle createExternalIdBundle(Map<String, String> currentRow) {
    Iterable<String> iterable = Arrays.asList(currentRow.get(SnapshotColumns.ID_BUNDLE.get()).split("\\|"));
    return ExternalIdBundle.parse(iterable);
  }

  @Override
  public Map<CurveKey, CurveSnapshot> readCurves() {
    return _curves;
  }

  @Override
  public UnstructuredMarketDataSnapshot readGlobalValues() {
    return _global;
  }

  @Override
  public Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> readVolatilitySurfaces() {
    return _surface;
  }

  @Override
  public Map<YieldCurveKey, YieldCurveSnapshot> readYieldCurves() {
    return _yieldCurve;
  }

  @Override
  public void close() {
    _sheetReader.close();
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String getBasisViewName() {
    return _basisName;
  }
}
