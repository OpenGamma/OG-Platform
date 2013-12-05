/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.writer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
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
import com.opengamma.integration.copier.sheet.writer.XlsSheetWriter;
import com.opengamma.integration.copier.sheet.writer.XlsWriter;
import com.opengamma.integration.copier.snapshot.SnapshotColumns;
import com.opengamma.integration.copier.snapshot.SnapshotType;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Writes a snapshot to exported file
 */
public class XlsSnapshotWriter implements SnapshotWriter {

  private XlsWriter _xlsWriter;
  private XlsSheetWriter _nameSheet;
  private XlsSheetWriter _curveSheet;
  private XlsSheetWriter _yieldCurveSheet;
  private XlsSheetWriter _globalsSheet;
  private XlsSheetWriter _surfaceSheet;


  private static final Logger s_logger = LoggerFactory.getLogger(XlsSnapshotWriter.class);

  public XlsSnapshotWriter(String filename) {

    if (filename == null) {
      throw new OpenGammaRuntimeException("File name omitted, cannot export to file");
    }
    _xlsWriter = new XlsWriter(filename);
    _nameSheet = new XlsSheetWriter(_xlsWriter.getWorkbook(), "name");
    _curveSheet = new XlsSheetWriter(_xlsWriter.getWorkbook(), SnapshotType.CURVE.get());
    _yieldCurveSheet = new XlsSheetWriter(_xlsWriter.getWorkbook(), SnapshotType.YIELD_CURVE.get());
    _globalsSheet = new XlsSheetWriter(_xlsWriter.getWorkbook(), SnapshotType.GLOBAL_VALUES.get());
    _surfaceSheet = new XlsSheetWriter(_xlsWriter.getWorkbook(), SnapshotType.VOL_SURFACE.get());


  }

  /** Ordinated ValueSnapshots, needed for Volatility Surfaces */
  private void writeOrdinatedValueSnapshot(Map<String, String> prefixes,
                                           Map<Pair<Object, Object>, ValueSnapshot> valueSnapshots) {



    for (Map.Entry<Pair<Object, Object>, ValueSnapshot> entry : valueSnapshots.entrySet()) {
      Map<String, String> tempRow = new HashMap<>();
      ValueSnapshot valueSnapshot = entry.getValue();
      tempRow.putAll(prefixes);

      String surfaceX;
      if (entry.getKey().getFirst() instanceof Tenor) {
        surfaceX = ((Tenor) entry.getKey().getFirst()).toFormattedString();
      } else {
        surfaceX = entry.getKey().getFirst().toString();
      }

      String surfaceY;
      if (entry.getKey().getSecond() instanceof Pair) {
        surfaceY = ((Pair) entry.getKey().getSecond()).getFirst() + "|" + ((Pair) entry.getKey().getSecond()).getSecond();
      } else {
        surfaceY = entry.getKey().getSecond().toString();
      }

      tempRow.put(SnapshotColumns.SURFACE_X.get(), surfaceX);
      tempRow.put(SnapshotColumns.SURFACE_Y.get(), surfaceY);
      if (valueSnapshot == null) {
        tempRow.put(SnapshotColumns.MARKET_VALUE.get(), "null");
      } else {
        if (valueSnapshot.getMarketValue() != null) {
          tempRow.put(SnapshotColumns.MARKET_VALUE.get(), valueSnapshot.getMarketValue().toString());
        }
        if (valueSnapshot.getOverrideValue() != null) {
          tempRow.put(SnapshotColumns.OVERRIDE_VALUE.get(), valueSnapshot.getOverrideValue().toString());
        }
      }
      //TODO _sheetWriter.writeNextRow(tempRow);
    }
  }



  private void writeFudgeMsg(Map<String, String> prefixes, FudgeMsg message) {
    for (FudgeField field : message.getAllFields()) {
      Map<String, String> tempRow = new HashMap<>();
      tempRow.putAll(prefixes);
      tempRow.put(SnapshotColumns.VALUE_OBJECT.get(), field.getName());
      // assuming that the the value of the field is not another data structure
      tempRow.put(SnapshotColumns.MARKET_VALUE.get(), field.getValue().toString());
      //TODO _sheetWriter.writeNextRow(tempRow);
    }
  }


  @Override
  public void flush() {
    //TODO _sheetWriter.flush();
  }


















  private Map<String, ObjectsPair<String, String>> buildUnstructuredMarketDataSnapshotMap(UnstructuredMarketDataSnapshot snapshot) {
    Map<String, ObjectsPair<String, String>> values = new LinkedHashMap<>();
    values.put(SnapshotColumns.ID_BUNDLE.get(),
               ObjectsPair.of(SnapshotColumns.MARKET_VALUE.get(), SnapshotColumns.OVERRIDE_VALUE.get()));

    for (ExternalIdBundle eib : snapshot.getTargets()) {
      Map<String, ValueSnapshot> valueSnapshots =  snapshot.getTargetValues(eib);
      if (valueSnapshots.size() > 1) {
        throw new OpenGammaRuntimeException("XML export only supports a single value snapshot for UnstructuredMarketDataSnapshot. " +
                                                eib.toString() + " contains " + valueSnapshots.size() + " ValueSnapshots. " +
                                                "Export to CSV in this instance.");
      }
      ValueSnapshot valueSnapshot = valueSnapshots.entrySet().iterator().next().getValue();
      String market = (valueSnapshot.getMarketValue() == null) ? "" : valueSnapshot.getMarketValue().toString();
      String override = (valueSnapshot.getOverrideValue() == null) ? "" : valueSnapshot.getOverrideValue().toString();
      values.put(StringUtils.join(eib.getExternalIds(), '|'), ObjectsPair.of(market, override));

    }
    return values;
  }

  @Override
  public void writeVolatilitySurface(Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> volatilitySurface) {

    if (volatilitySurface == null || volatilitySurface.isEmpty()) {
      s_logger.warn("Snapshot does not contain any Volatility Surfaces.");
      return;
    }

    for (Map.Entry<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> entry : volatilitySurface.entrySet()) {
      VolatilitySurfaceSnapshot surface = entry.getValue();
      Map<String, String> details = new HashMap<>();
      details.put(SnapshotColumns.TYPE.get(), SnapshotType.VOL_SURFACE.get());
      details.put(SnapshotColumns.NAME.get(), entry.getKey().getName());
      details.put(SnapshotColumns.SURFACE_TARGET.get(), entry.getKey().getTarget().toString());
      details.put(SnapshotColumns.SURFACE_INSTRUMENT_TYPE.get(), entry.getKey().getInstrumentType());
      details.put(SnapshotColumns.SURFACE_QUOTE_TYPE.get(), entry.getKey().getQuoteType());
      details.put(SnapshotColumns.SURFACE_QUOTE_UNITS.get(), entry.getKey().getQuoteUnits());

      Map<Pair<String, String>, String> marketValueMap = new LinkedHashMap<>();
      Map<Pair<String, String>, String> overrideValueMap = new LinkedHashMap<>();


      Set<String> xMap = new HashSet<>();
      Set<String> yMap = new HashSet<>();

      for (Map.Entry<Pair<Object, Object>, ValueSnapshot> value : surface.getValues().entrySet()) {
        Pair<String, String> ordinal = ObjectsPair.of(value.getKey().getFirst().toString(), value.getKey().getSecond().toString());

        xMap.add(ordinal.getFirst());
        yMap.add(ordinal.getSecond());

        ValueSnapshot valueSnapshot = value.getValue();
        String market = (valueSnapshot.getMarketValue() == null) ? "" : valueSnapshot.getMarketValue().toString();
        String override = (valueSnapshot.getOverrideValue() == null) ? "" : valueSnapshot.getOverrideValue().toString();

        marketValueMap.put(ordinal, market);
        overrideValueMap.put(ordinal, override);
      }

      _surfaceSheet.writeKeyValueBlock(details);
      _surfaceSheet.writeMatrix(xMap, yMap, SnapshotColumns.MARKET_VALUE.get(), marketValueMap);
      _surfaceSheet.writeMatrix(xMap, yMap, SnapshotColumns.OVERRIDE_VALUE.get(), overrideValueMap);

    }
  }

  @Override
  public void writeGlobalValues(UnstructuredMarketDataSnapshot globalValues) {

    if (globalValues == null || globalValues.isEmpty()) {
      s_logger.warn("Snapshot does not contain any Global Values.");
      return;
    }
    _globalsSheet.writePairBlock(buildUnstructuredMarketDataSnapshotMap(globalValues));
  }


  @Override
  public void writeYieldCurves(Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves) {

    if (yieldCurves == null || yieldCurves.isEmpty()) {
      s_logger.warn("Snapshot does not contain any Yield Curve Snapshots.");
      return;
    }

    for (Map.Entry<YieldCurveKey, YieldCurveSnapshot> entry : yieldCurves.entrySet()) {
      YieldCurveSnapshot curve = entry.getValue();
      Map<String, String> details = new LinkedHashMap<>();
      details.put(SnapshotColumns.NAME.get(), entry.getKey().getName());
      details.put(SnapshotColumns.YIELD_CURVE_CURRENCY.get(), entry.getKey().getCurrency().toString());
      details.put(SnapshotColumns.INSTANT.get(), curve.getValuationTime().toString());
      _yieldCurveSheet.writeKeyValueBlock(details);

      UnstructuredMarketDataSnapshot snapshot = curve.getValues();
      _yieldCurveSheet.writePairBlock(buildUnstructuredMarketDataSnapshotMap(snapshot));
    }

  }

  @Override
  public void writeCurves(Map<CurveKey, CurveSnapshot> curves) {

    if (curves == null || curves.isEmpty()) {
      s_logger.warn("Snapshot does not contain any Curve Snapshots.");
      return;
    }

    for (Map.Entry<CurveKey, CurveSnapshot> entry : curves.entrySet()) {
      CurveSnapshot curve = entry.getValue();
      Map<String, String> details = new LinkedHashMap<>();
      details.put(SnapshotColumns.NAME.get(), entry.getKey().getName());
      details.put(SnapshotColumns.INSTANT.get(), curve.getValuationTime().toString());
      _curveSheet.writeKeyValueBlock(details);
      UnstructuredMarketDataSnapshot snapshot = curve.getValues();
      _curveSheet.writePairBlock(buildUnstructuredMarketDataSnapshotMap(snapshot));
    }
  }

  @Override
  public void writeName(String name) {
    Map<String, String> detail = new HashMap<>();
    detail.put(SnapshotType.NAME.get(), name);

    _nameSheet.writeKeyValueBlock(detail);
  }

  @Override
  public void writeBasisViewName(String basisName) {
    Map<String, String> detail = new HashMap<>();
    detail.put(SnapshotType.BASIS_NAME.get(), basisName);
    _nameSheet.writeKeyValueBlock(detail);
  }

  @Override
  public void close() {
    flush();
    _xlsWriter.close();
  }
}
