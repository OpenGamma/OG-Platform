/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.writer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.sheet.writer.CsvSheetWriter;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.tuple.Pair;

/**
 * Writes a snapshot from an to exported file
 */
public class FileSnapshotWriter implements SnapshotWriter {

  private CsvSheetWriter _sheetWriter;

  private static final Logger s_logger = LoggerFactory.getLogger(FileSnapshotWriter.class);

  private static final String TYPE = "Type";
  private static final String NAME = "Name";
  private static final String INSTANT = "Instant";
  private static final String SURFACE_TARGET = "Surface Target";
  private static final String SURFACE_INSTRUMENT_TYPE = "Surface Instrument Type";
  private static final String SURFACE_QUOTE_TYPE = "Surface Quote Type";
  private static final String SURFACE_QUOTE_UNITS = "Surface Quote Units";
  private static final String ID_BUNDLE = "External ID Bundle";
  private static final String SURFACE_X = "Surface X";
  private static final String SURFACE_Y = "Surface Y";

  private static final String VALUE_NAME = "Value Name";
  private static final String MARKET_VALUE = "Market Value";
  private static final String OVERRIDE_VALUE = "Override Value";
  private static final String[] COLUMNS =
      {TYPE, NAME, INSTANT, SURFACE_TARGET, SURFACE_INSTRUMENT_TYPE, SURFACE_QUOTE_TYPE, SURFACE_QUOTE_UNITS, ID_BUNDLE, VALUE_NAME, MARKET_VALUE, OVERRIDE_VALUE, SURFACE_X, SURFACE_Y};

  private static final String TYPE_NAME = "name";
  private static final String TYPE_BASIS_NAME = "basis name";
  private static final String TYPE_CURVE = "curve";
  private static final String TYPE_YIELD_CURVE = "yield curve";
  private static final String TYPE_GOBAL_VALUES = "global values";
  private static final String TYPE_VOL_SURFACE = "volatility surface";



  public FileSnapshotWriter(String filename, MarketDataSnapshotMaster marketDataSnapshotMaster) {

    if (filename == null) {
      throw new OpenGammaRuntimeException("File name omitted, cannot export to file");
    }

    if (SheetFormat.of(filename) == SheetFormat.CSV || SheetFormat.of(filename) == SheetFormat.XLS) {
      _sheetWriter = new CsvSheetWriter(filename, COLUMNS);
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .XLS");
    }
  }

  private void writeOrdinatedValueSnapshot(Map<String, String> prefixes,
                                           Map<Pair<Object, Object>, ValueSnapshot> valueSnapshots) {

    for (Map.Entry<Pair<Object, Object>, ValueSnapshot> entry : valueSnapshots.entrySet()) {
      Map<String, String> tempRow = new HashMap<>();
      ValueSnapshot valueSnapshot = entry.getValue();
      tempRow.putAll(prefixes);
      tempRow.put(SURFACE_X, entry.getKey().getFirst().toString());
      tempRow.put(SURFACE_Y, entry.getKey().getSecond().toString());
      if (valueSnapshot.getMarketValue() != null) {
        tempRow.put(MARKET_VALUE, valueSnapshot.getMarketValue().toString());
      }
      if (valueSnapshot.getOverrideValue() != null) {
        tempRow.put(OVERRIDE_VALUE, valueSnapshot.getOverrideValue().toString());
      }
      _sheetWriter.writeNextRow(tempRow);
    }
  }

  private void writeValueSnapshot(Map<String, String> prefixes, Map<String, ValueSnapshot> valueSnapshots) {

    for (Map.Entry<String, ValueSnapshot> entry : valueSnapshots.entrySet()) {
      Map<String, String> tempRow = new HashMap<>();
      ValueSnapshot valueSnapshot = entry.getValue();
      tempRow.putAll(prefixes);
      tempRow.put(VALUE_NAME, entry.getKey());
      if (valueSnapshot.getMarketValue() != null) {
        tempRow.put(MARKET_VALUE, valueSnapshot.getMarketValue().toString());
      }
      if (valueSnapshot.getOverrideValue() != null) {
        tempRow.put(OVERRIDE_VALUE, valueSnapshot.getOverrideValue().toString());
      }
      _sheetWriter.writeNextRow(tempRow);
    }
  }

  private void writeUnstructuredMarketDataSnapshot(Map<String, String> prefixes,
                                                   UnstructuredMarketDataSnapshot snapshot) {

    for (ExternalIdBundle eib : snapshot.getTargets()) {
      Map<String, String> tempRow = new HashMap<>();
      tempRow.putAll(prefixes);
      tempRow.put(ID_BUNDLE, StringUtils.join(eib.getExternalIds(), '|'));
      writeValueSnapshot(tempRow, snapshot.getTargetValues(eib));
    }
  }

  @Override
  public void flush() {
    _sheetWriter.flush();
  }

  @Override
  public void writeCurves(Map<CurveKey, CurveSnapshot> curves) {

    if (curves == null || curves.isEmpty()) {
      s_logger.warn("Snapshot does not contain any Curve Snapshots.");
      return;
    }

    for (Map.Entry<CurveKey, CurveSnapshot> entry : curves.entrySet()) {
      CurveSnapshot curve = entry.getValue();
      Map<String, String> tempRow = new HashMap<>();
      tempRow.put(TYPE, TYPE_CURVE);
      tempRow.put(NAME, entry.getKey().getName());
      tempRow.put(INSTANT, curve.getValuationTime().toString());
      writeUnstructuredMarketDataSnapshot(tempRow, curve.getValues());
      //TODO
    }

  }

  @Override
  public void writeGlobalValues(UnstructuredMarketDataSnapshot globalValues) {

    if (globalValues == null || globalValues.isEmpty()) {
      s_logger.warn("Snapshot does not contain any Global Values.");
      return;
    }

    Map<String, String> tempRow = new HashMap<>();
    tempRow.put(TYPE, TYPE_GOBAL_VALUES);
    writeUnstructuredMarketDataSnapshot(tempRow, globalValues);
  }

  @Override
  public void writeVoliatilitySurface(Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> volatilitySurface) {

    if (volatilitySurface == null || volatilitySurface.isEmpty()) {
      s_logger.warn("Snapshot does not contain any Volatility Surfaces.");
      return;
    }

    for (Map.Entry<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> entry : volatilitySurface.entrySet()) {
      VolatilitySurfaceSnapshot surface = entry.getValue();
      Map<String, String> tempRow = new HashMap<>();
      tempRow.put(TYPE, TYPE_VOL_SURFACE);
      tempRow.put(NAME, entry.getKey().getName());
      tempRow.put(SURFACE_TARGET, entry.getKey().getTarget().toString());
      tempRow.put(SURFACE_INSTRUMENT_TYPE, entry.getKey().getInstrumentType());
      tempRow.put(SURFACE_QUOTE_TYPE, entry.getKey().getQuoteType());
      tempRow.put(SURFACE_QUOTE_UNITS, entry.getKey().getQuoteUnits());
      writeOrdinatedValueSnapshot(tempRow, surface.getValues());
    }
  }

  @Override
  public void writeYieldCurves(Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves) {

    if (yieldCurves == null || yieldCurves.isEmpty()) {
      s_logger.warn("Snapshot does not contain any Yield Curve Snapshots.");
      return;
    }

    for (Map.Entry<YieldCurveKey, YieldCurveSnapshot> entry : yieldCurves.entrySet()) {
      YieldCurveSnapshot curve = entry.getValue();
      Map<String, String> tempRow = new HashMap<>();
      tempRow.put(TYPE, TYPE_YIELD_CURVE);
      tempRow.put(NAME, entry.getKey().getName());
      tempRow.put(INSTANT, curve.getValuationTime().toString());
      writeUnstructuredMarketDataSnapshot(tempRow, curve.getValues());
    }
  }

  @Override
  public void writeName(String name) {

    if (name == null || name.isEmpty()) {
      s_logger.warn("Snapshot does not contain name.");
      return;
    }

    Map<String, String> tempRow = new HashMap<>();
    tempRow.put(TYPE, TYPE_NAME);
    tempRow.put(NAME, name);
    _sheetWriter.writeNextRow(tempRow);
  }

  @Override
  public void writeBasisViewName(String basisName) {

    if (basisName == null || basisName.isEmpty()) {
      s_logger.warn("Snapshot does not contain basis name.");
      return;
    }

    Map<String, String> tempRow = new HashMap<>();
    tempRow.put(TYPE, TYPE_BASIS_NAME);
    tempRow.put(NAME, basisName);
    _sheetWriter.writeNextRow(tempRow);
  }

  @Override
  public void close() {
    flush();
    _sheetWriter.close();
  }
}
