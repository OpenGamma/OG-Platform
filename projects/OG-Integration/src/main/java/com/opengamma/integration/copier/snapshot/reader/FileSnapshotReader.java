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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

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
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

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

  public FileSnapshotReader(String filename) {
    _sheetReader = new CsvSheetReader(filename);
    iterateSheetRows();
  }

  private void iterateSheetRows() {

    _curves = new HashMap<>();
    _surface = new HashMap<>();
    _yieldCurve = new HashMap<>();

    //Temporary maps for data structures
    HashMap<String, ManageableCurveSnapshot> curvesBuilder = new HashMap<>();
    HashMap<String, Pair<YieldCurveKey, ManageableYieldCurveSnapshot>> yieldCurveBuilder = new HashMap<>();
    HashMap<String, Pair<VolatilitySurfaceKey, ManageableVolatilitySurfaceSnapshot>> surfaceBuilder = new HashMap<>();
    ManageableUnstructuredMarketDataSnapshot globalBuilder = new ManageableUnstructuredMarketDataSnapshot();

    while (true) {
      Map<String, String> currentRow = _sheetReader.loadNextRow();

      // When rows are complete create snapshot elements from temporary structures
      if (currentRow == null) {
        for (Map.Entry<String, ManageableCurveSnapshot> entry : curvesBuilder.entrySet()) {
          _curves.put(new CurveKey(entry.getKey()), entry.getValue());
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

      switch (SnapshotType.from(type)) {
        case NAME:
          _name = currentRow.get(SnapshotColumns.NAME.get());
          break;
        case BASIS_NAME:
          _basisName = currentRow.get(SnapshotColumns.NAME.get());
          break;
        case CURVE:
          buildCurves(curvesBuilder, currentRow);
          break;
        case YIELD_CURVE:
          buildYieldCurves(yieldCurveBuilder, currentRow);
          break;
        case GOBAL_VALUES:
          buildGlobalValues(globalBuilder, currentRow);
          break;
        case VOL_SURFACE:
          buildSurface(surfaceBuilder, currentRow);
          break;
        default:
          s_logger.error("Unknown snapshot element of type {}", type);
          break;
      }
    }
  }

  private void buildGlobalValues(ManageableUnstructuredMarketDataSnapshot globalBuilder , Map<String, String> currentRow) {
    globalBuilder.putValue(createExternalIdBundle(currentRow),
                           currentRow.get(SnapshotColumns.VALUE_NAME.get()),
                           createValueSnapshot(currentRow));
  }

  private void buildSurface(HashMap<String, Pair<VolatilitySurfaceKey, ManageableVolatilitySurfaceSnapshot>> surfaceBuilder,
                            Map<String, String> currentRow) {
    String name = currentRow.get(SnapshotColumns.NAME.get());

    if (!surfaceBuilder.containsKey(name)) {
      ManageableVolatilitySurfaceSnapshot surface = new ManageableVolatilitySurfaceSnapshot();
      VolatilitySurfaceKey key = new VolatilitySurfaceKey(UniqueId.parse(currentRow.get(SnapshotColumns.SURFACE_TARGET.get())),
                                                          currentRow.get(SnapshotColumns.NAME.get()),
                                                          currentRow.get(SnapshotColumns.SURFACE_INSTRUMENT_TYPE.get()),
                                                          currentRow.get(SnapshotColumns.SURFACE_QUOTE_TYPE.get()),
                                                          currentRow.get(SnapshotColumns.SURFACE_QUOTE_UNITS.get()));
      HashMap values = new HashMap<Pair<Object, Object>, ValueSnapshot>();

      values.put(Pair.of(currentRow.get(SnapshotColumns.SURFACE_X.get()),
                         currentRow.get(SnapshotColumns.SURFACE_Y.get())), createValueSnapshot(currentRow));
      surface.setValues(values);
      surfaceBuilder.put(name, Pair.of(key, surface));
    } else {
      surfaceBuilder.get(name).getSecond().getValues().put(createOrdinatePair(currentRow),
                                                           createValueSnapshot(currentRow));
    }

  }

  private void buildYieldCurves(HashMap<String, Pair<YieldCurveKey, ManageableYieldCurveSnapshot>> yieldCurveBuilder,
                                Map<String, String> currentRow) {
    String name = currentRow.get(SnapshotColumns.NAME.get());

    if (!yieldCurveBuilder.containsKey(name)) {
      ManageableYieldCurveSnapshot curve = new ManageableYieldCurveSnapshot();
      ManageableUnstructuredMarketDataSnapshot snapshot = new ManageableUnstructuredMarketDataSnapshot();
      YieldCurveKey key = new YieldCurveKey(Currency.of(currentRow.get(SnapshotColumns.YIELD_CURVE_CURRENCY.get())),
                                            currentRow.get(SnapshotColumns.NAME.get()));

      curve.setValuationTime(Instant.parse(currentRow.get(SnapshotColumns.INSTANT.get())));
      snapshot.putValue(createExternalIdBundle(currentRow),
                        currentRow.get(SnapshotColumns.VALUE_NAME.get()),
                        createValueSnapshot(currentRow));
      curve.setValues(snapshot);
      yieldCurveBuilder.put(name, Pair.of(key, curve));
    } else {
      yieldCurveBuilder.get(name).getSecond().getValues().putValue(createExternalIdBundle(currentRow),
                                                   currentRow.get(SnapshotColumns.VALUE_NAME.get()),
                                                   createValueSnapshot(currentRow));
    }
  }

  private void buildCurves(HashMap<String, ManageableCurveSnapshot> curvesBuilder, Map<String, String> currentRow) {
    String name = currentRow.get(SnapshotColumns.NAME.get());

    if (!curvesBuilder.containsKey(name)) {
      ManageableCurveSnapshot curve = new ManageableCurveSnapshot();
      ManageableUnstructuredMarketDataSnapshot snapshot = new ManageableUnstructuredMarketDataSnapshot();

      curve.setValuationTime(Instant.parse(currentRow.get(SnapshotColumns.INSTANT.get())));
      snapshot.putValue(createExternalIdBundle(currentRow),
                        currentRow.get(SnapshotColumns.VALUE_NAME.get()),
                        createValueSnapshot(currentRow));
      curve.setValues(snapshot);
      curvesBuilder.put(name, curve);
    } else {
      curvesBuilder.get(name).getValues().putValue(createExternalIdBundle(currentRow),
                                                   currentRow.get(SnapshotColumns.VALUE_NAME.get()),
                                                   createValueSnapshot(currentRow));
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

    return Pair.of(surfaceX, surfaceY);
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
    return Pair.of(firstElement, secondElement);
  }

  private ValueSnapshot createValueSnapshot(Map<String, String> currentRow) {
    String market = currentRow.get(SnapshotColumns.MARKET_VALUE.get());
    String override = currentRow.get(SnapshotColumns.OVERRIDE_VALUE.get());
    Object marketValue = null;
    Object overrideValue = null;

    // marketValue can only be Double, LocalDate or empty
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

    return new ValueSnapshot(marketValue, overrideValue);
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
