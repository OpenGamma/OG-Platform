/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.copier.sheet.reader.XlsSheetReader;
import com.opengamma.integration.copier.snapshot.SnapshotColumns;
import com.opengamma.integration.copier.snapshot.SnapshotType;
import com.opengamma.integration.tool.marketdata.MarketDataSnapshotToolUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Reads a snapshot from an imported file
 */
public class XlsSnapshotReader implements SnapshotReader {


  private Map<CurveKey, CurveSnapshot> _curves;
  private UnstructuredMarketDataSnapshot _global;
  private Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> _surface;
  private Map<YieldCurveKey, YieldCurveSnapshot> _yieldCurve;
  private String _name;
  private String _basisName;
  private XlsSheetReader _nameSheet;
  private XlsSheetReader _globalsSheet;
  private XlsSheetReader _yieldCurveSheet;
  private XlsSheetReader _curveSheet;
  private XlsSheetReader _surfaceSheet;
  private Workbook _workbook;
  private InputStream _fileInputStream;
  private final String _valueObject = "Market_Value";

  public XlsSnapshotReader(String filename) {
    _fileInputStream = openFile(filename);
    _workbook = getWorkbook(_fileInputStream);
    _curves = new HashMap<>();
    _surface = new HashMap<>();
    _yieldCurve = new HashMap<>();
    buildNameData();
    buildGlobalData();
    buildYieldCurveData();
    buildCurveData();
    buildSurfaceData();
  }

  /**
   * Build surface data from xls data, made up of a key/value block of details, a matrix block of market values and
   * a matrix block of override values, each block separated by a empty row
   */
  private void buildSurfaceData() {
    _surfaceSheet = new XlsSheetReader(_workbook, SnapshotType.VOL_SURFACE.get());

    //continue reading in each surface object until current row incrementing returns no data
    while (true) {
      Map<String, String> details = _surfaceSheet.readKeyValueBlock(_surfaceSheet.getCurrentRowIndex(), 0);
      if (details.isEmpty() || details == null) {
        break;
      }

      Map<Pair<String, String>, String> marketValues = _surfaceSheet.readMatrix(_surfaceSheet.getCurrentRowIndex(), 0);
      Map<Pair<String, String>, String> overrideValues = _surfaceSheet.readMatrix(_surfaceSheet.getCurrentRowIndex(), 0);
      ManageableVolatilitySurfaceSnapshot surface = new ManageableVolatilitySurfaceSnapshot();
      VolatilitySurfaceKey key = VolatilitySurfaceKey.of(UniqueId.parse(details.get(SnapshotColumns.SURFACE_TARGET.get())),
                                                         details.get(SnapshotColumns.NAME.get()),
                                                         details.get(SnapshotColumns.SURFACE_INSTRUMENT_TYPE.get()),
                                                         details.get(SnapshotColumns.SURFACE_QUOTE_TYPE.get()),
                                                         details.get(SnapshotColumns.SURFACE_QUOTE_UNITS.get()));
      HashMap<Pair<Object, Object>, ValueSnapshot> values = new HashMap<>();

      // use the market value map to add the value and 'lookup' the corresponding override value
      for (Map.Entry<Pair<String, String>, String> entry : marketValues.entrySet()) {
        Pair rawOrdinals = entry.getKey();
        Pair ordinals = MarketDataSnapshotToolUtils.createOrdinatePair(rawOrdinals.getFirst().toString(),
                                                                       rawOrdinals.getSecond().toString());
        String override = overrideValues.get(rawOrdinals);
        String market = entry.getValue();
        values.put(ordinals, MarketDataSnapshotToolUtils.createValueSnapshot(market, override));
      }

      surface.setValues(values);
      _surface.put(key, surface);
    }
  }

  private Workbook getWorkbook(InputStream inputStream) {
    try {
      return new HSSFWorkbook(inputStream);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Error opening Excel workbook: " + ex.getMessage());
    }
  }

  private ExternalIdBundle createExternalIdBundle(String idBundle) {
    Iterable<String> iterable = Arrays.asList(idBundle.split("\\|"));
    return ExternalIdBundle.parse(iterable);
  }

  /**
   * Build yield curve data from xls data, made up of a key/value block of details and a key/value-pair block of values
   * built by getManageableUnstructuredMarketDataSnapshot, each block separated by a empty row
   */
  private void buildYieldCurveData() {
    _yieldCurveSheet = new XlsSheetReader(_workbook, SnapshotType.YIELD_CURVE.get());
    while (true) {
      Map<String, String> details = _yieldCurveSheet.readKeyValueBlock(_yieldCurveSheet.getCurrentRowIndex(), 0);
      if (details.isEmpty() || details == null) {
        break;
      }
      YieldCurveKey key = YieldCurveKey.of(Currency.of(details.get(SnapshotColumns.YIELD_CURVE_CURRENCY.get())),
                                           details.get(SnapshotColumns.NAME.get()));
      Instant instant = Instant.parse(details.get(SnapshotColumns.INSTANT.get()));
      ManageableUnstructuredMarketDataSnapshot snapshot = getManageableUnstructuredMarketDataSnapshot(_yieldCurveSheet);
      ManageableYieldCurveSnapshot curve = ManageableYieldCurveSnapshot.of(instant, snapshot);
      _yieldCurve.put(key, curve);
    }
  }
  /**
   * Build curve data from xls data, made up of a key/value block of details and a key/value-pair block of values
   * built by getManageableUnstructuredMarketDataSnapshot, each block separated by a empty row
   */
  private void buildCurveData() {
    _curveSheet = new XlsSheetReader(_workbook, SnapshotType.CURVE.get());
    while (true) {
      Map<String, String> details = _curveSheet.readKeyValueBlock(_curveSheet.getCurrentRowIndex(), 0);
      if (details.isEmpty() || details == null) {
        break;
      }
      ManageableCurveSnapshot curve = new ManageableCurveSnapshot();
      ManageableUnstructuredMarketDataSnapshot snapshot =  getManageableUnstructuredMarketDataSnapshot(_curveSheet);
      Instant instant = Instant.parse(details.get(SnapshotColumns.INSTANT.get()));
      curve.setValuationTime(instant);
      curve.setValues(snapshot);
      _curves.put(CurveKey.of(details.get(SnapshotColumns.NAME.get())), curve);
    }
  }

  private ManageableUnstructuredMarketDataSnapshot getManageableUnstructuredMarketDataSnapshot(XlsSheetReader sheet) {
    //Skip the header row
    Map<String, ObjectsPair<String, String>> map = sheet.readKeyPairBlock(sheet.getCurrentRowIndex() + 1, 0);
    ManageableUnstructuredMarketDataSnapshot builder = new ManageableUnstructuredMarketDataSnapshot();
    for (Map.Entry<String, ObjectsPair<String, String>> entry : map.entrySet()) {
      builder.putValue(createExternalIdBundle(entry.getKey()),
                       _valueObject,
                             MarketDataSnapshotToolUtils.createValueSnapshot(entry.getValue().getFirst(),
                                                                             entry.getValue().getSecond()));
    }
    return builder;
  }
  /**
   * Build global data from xls data, made up of a key/value-pair block of values
   * built by getManageableUnstructuredMarketDataSnapshot, should only consist of one block
   */
  private void buildGlobalData() {
    _globalsSheet = new XlsSheetReader(_workbook, SnapshotType.GLOBAL_VALUES.get());
    _global = getManageableUnstructuredMarketDataSnapshot(_globalsSheet);
  }

  /**
   * Name data is made up of two separate blocks (name and basis name) which are special cased on writing the data
   * so that they can be read in as on key value block
   */
  private void buildNameData() {
    _nameSheet = new XlsSheetReader(_workbook, SnapshotType.NAME.get());
    Map<String, String> nameMap = _nameSheet.readKeyValueBlock(_nameSheet.getCurrentRowIndex(), 0);
    _name = nameMap.get(SnapshotType.NAME.get());
    _basisName = nameMap.get(SnapshotType.BASIS_NAME.get());
  }

  protected static InputStream openFile(String filename) {
    // Open input file for reading
    FileInputStream fileInputStream;
    try {
      fileInputStream = new FileInputStream(filename);
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("Could not open file " + filename + " for reading, exiting immediately.");
    }
    return fileInputStream;
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
    //XlsSheetReader takes care of closing the input stream
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
