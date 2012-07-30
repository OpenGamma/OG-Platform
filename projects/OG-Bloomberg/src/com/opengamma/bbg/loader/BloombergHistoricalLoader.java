/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.ExternalIdResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.MapUtils;
import com.opengamma.util.PlatformConfigUtils;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * Tool to load time-series information from Bloomberg.
 * <p>
 * This loads missing historical time-series data from Bloomberg.
 */
public class BloombergHistoricalLoader {

  /** The spring configuration. */
  public static final String CONTEXT_CONFIGURATION_PATH = "/com/opengamma/bbg/loader/bloomberg-historical-loader-context.xml";
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergHistoricalLoader.class);

  private static final String FIELDS_OPTION = "fields";
  private static final String DATAPROVIDERS_OPTION = "dataproviders";
  private static final String HELP_OPTION = "help";
  private static final String POSITION_MASTER_OPTION = "pm";
  private static final String RELOAD_OPTION = "reload";
  private static final String UPDATE_OPTION = "update";
  private static final String START_OPTION = "start";
  private static final String END_OPTION = "end";
  private static final String UNIQUE_ID_OPTION = "unique";
  private static final String CSV_OPTION = "csv";
  private static final LocalDate DEFAULT_START_DATE = LocalDate.of(1900, MonthOfYear.JANUARY, 1);

  private final HistoricalTimeSeriesMaster _timeSeriesMaster;
  private final HistoricalTimeSeriesSource _bbgHistoricalTimeSeriesSource;
  private PositionMaster _positionMaster;
  private final BloombergHistoricalTimeSeriesLoader _loader;

  private boolean _updateDb;
  private LocalDate _startDate;
  private LocalDate _endDate;
  private Set<String> _files = new HashSet<String>();
  private Set<String> _dataProviders = new HashSet<String>();
  private Set<String> _dataFields = new HashSet<String>();
  private boolean _loadPositionMaster;
  private boolean _reload;
  private boolean _bbgUniqueId;
  private boolean _isCsv;

  private List<ExternalIdBundle> _errors = new ArrayList<ExternalIdBundle>();

  public BloombergHistoricalLoader(final HistoricalTimeSeriesMaster htsMaster, 
      final HistoricalTimeSeriesSource underlyingHtsProvider, 
      final ExternalIdResolver identifierProvider) {
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(underlyingHtsProvider, "underlyingHtsProvider");
    ArgumentChecker.notNull(identifierProvider, "identifierProvider");
    _timeSeriesMaster = htsMaster;
    _bbgHistoricalTimeSeriesSource = underlyingHtsProvider;
    _loader = new BloombergHistoricalTimeSeriesLoader(htsMaster, underlyingHtsProvider, identifierProvider);
  }

  /**
   * Gets the positionMaster field.
   * @return the positionMaster
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * Sets the positionMaster field.
   * @param positionMaster  the positionMaster
   */
  public void setPositionMaster(PositionMaster positionMaster) {
    _positionMaster = positionMaster;
  }

  /**
   * Gets the bbgUniqueId field.
   * @return the bbgUniqueId
   */
  public boolean isBbgUniqueId() {
    return _bbgUniqueId;
  }

  /**
   * Sets the bbgUniqueId field.
   * @param bbgUniqueId  the bbgUniqueId
   */
  public void setBbgUniqueId(boolean bbgUniqueId) {
    _bbgUniqueId = bbgUniqueId;
  }

  private boolean isCsv() {
    return _isCsv;
  }

  /**
   * Sets whether the files are in CSV format including the data provider for each identifier as a second column.
   * 
   * @param isCsv  <code>true</code> if the files are in CSV format
   */
  public void setCsv(boolean isCsv) {
    _isCsv = isCsv;
  }

  /**
   * Sets the dataProviders field.
   * @param dataProviders  the dataProviders
   */
  public void setDataProviders(Collection<String> dataProviders) {
    ArgumentChecker.notNull(dataProviders, "dataProviders");
    _dataProviders = new HashSet<String>(dataProviders);
  }

  /**
   * Gets the dataProviders field.
   * @return the dataProviders
   */
  public Set<String> getDataProviders() {
    return Collections.unmodifiableSet(_dataProviders);
  }

  /**
   * Gets the dataFields field.
   * @return the dataFields
   */
  public Set<String> getDataFields() {
    return Collections.unmodifiableSet(_dataFields);
  }

  /**
   * Sets the dataFields field.
   * @param dataFields  the dataFields
   */
  public void setDataFields(Collection<String> dataFields) {
    ArgumentChecker.notNull(dataFields, "dataFields");
    _dataFields = new HashSet<String>(dataFields);
  }

  /**
   * Sets the files field.
   * @param files  the files
   */
  public void setFiles(Collection<String> files) {
    ArgumentChecker.notNull(files, "files");
    _files = new HashSet<String>(files);
  }
  
  /**
   * Sets the loadPositionMaster field.
   * @param loadPositionMaster  the loadPositionMaster
   */
  public void setLoadPositionMaster(boolean loadPositionMaster) {
    _loadPositionMaster = loadPositionMaster;
  }
  
  /**
   * Sets the updateDb field.
   * @param updateDb  the updateDb
   */
  public void setUpdateDb(boolean updateDb) {
    _updateDb = updateDb;
  }
  
  /**
   * Sets the startDate field.
   * @param startDate  the startDate
   */
  public void setStartDate(LocalDate startDate) {
    _startDate = startDate;
  }
  
  /**
   * Sets the endDate field.
   * @param endDate  the endDate
   */
  public void setEndDate(LocalDate endDate) {
    _endDate = endDate;
  }
  
  /**
   * Sets the reload field.
   * @param reload  the reload
   */
  public void setReload(boolean reload) {
    _reload = reload;
  }

  public void run() {
    _errors.clear();
    //update/reload current timeseries in datastore
    if (_updateDb || _reload) {
      if (_reload) {
        _startDate = DEFAULT_START_DATE;
        _endDate = DateUtils.previousWeekDay();
      }
      updateTimeSeriesInDB();
      return;
    }
    //load timeseries from input files
    if (!_files.isEmpty()) {
      if (isCsv()) {
        processCsvFiles();
      } else {
        processBasicFiles();
      }
      return;
    }
    //load missing data from position master
    if (_loadPositionMaster) {
      processMissingDataInPositionMaster();
      return;
    }
    
  }
  
  private void processMissingDataInPositionMaster() {
    Set<ExternalId> preferredIdentifiers = new HashSet<ExternalId>();
    for (ExternalIdBundle identifierBundle : BloombergDataUtils.getCurrentIdentifiers(_positionMaster)) {
      ExternalId preferredIdentifier = BloombergDomainIdentifierResolver.resolvePreferredIdentifier(identifierBundle);
      if (preferredIdentifier != null) {
        preferredIdentifiers.add(preferredIdentifier);
      } else {
        s_logger.warn("No preferred identifier for {}", identifierBundle);
      }      
    }
    load(preferredIdentifiers);
  }

  private void processBasicFiles() {
    Set<ExternalId> identifiers = readBasicFiles();
    load(identifiers);
  }
  
  private void load(Set<ExternalId> identifiers) {
    LocalDate startDate = _startDate == null ? DEFAULT_START_DATE : _startDate;
    LocalDate endDate = _endDate == null ? DateUtils.previousWeekDay() : _endDate;
    if (_dataProviders.isEmpty()) {
      _dataProviders.add(BloombergDataUtils.UNKNOWN_DATA_PROVIDER);
    }
    for (String dataProvider : _dataProviders) {
      for (String dataField : _dataFields) {
        _loader.addTimeSeries(identifiers, dataProvider, dataField, startDate, endDate);
      }
    }
  }

  private Set<ExternalId> readBasicFiles() {
    Set<String> securities = new HashSet<String>();
    if (_files != null) {
      for (String file : _files) {
        try {
          securities.addAll(FileUtils.readLines(new File(file)));
        } catch (IOException e) {
          s_logger.warn("Problem reading from input file={}", file);
          throw new OpenGammaRuntimeException("Problem reading from " + file, e);
        }
      }
    }
    Set<ExternalId> result = new HashSet<ExternalId>();
    for (String secDes : securities) {
      if (!StringUtils.isBlank(secDes)) {
        if (_bbgUniqueId) {
          result.add(ExternalSchemes.bloombergBuidSecurityId(secDes.trim()));
        } else {
          result.add(ExternalSchemes.bloombergTickerSecurityId(secDes.trim()));
        }
      }
    }
    return result;
  }

  private void processCsvFiles() {
    LocalDate startDate = _startDate == null ? DEFAULT_START_DATE : _startDate;
    LocalDate endDate = _endDate == null ? DateUtils.previousWeekDay() : _endDate;
    Map<Pair<String, String>, Set<ExternalId>> providerFieldRequestsMap = readCsvFiles();
    for (Entry<Pair<String, String>, Set<ExternalId>> providerFieldRequests : providerFieldRequestsMap.entrySet()) {
      String dataProvider = providerFieldRequests.getKey().getFirst();
      String dataField = providerFieldRequests.getKey().getSecond();
      Set<ExternalId> identifiers = providerFieldRequests.getValue();
      _loader.addTimeSeries(identifiers, dataProvider, dataField, startDate, endDate);
    }
  }
  
  private Map<Pair<String, String>, Set<ExternalId>> readCsvFiles() {
    Map<Pair<String, String>, Set<ExternalId>> result = new HashMap<Pair<String, String>, Set<ExternalId>>();
    if (_files != null) {
      int total = 0;
      for (String file : _files) {
        try {
          CSVReader reader = new CSVReader(new FileReader(file));
          String[] line;
          while ((line = reader.readNext()) != null) {
            if (line.length != 4) {
              throw new OpenGammaRuntimeException("Expected 4 columns in CSV file '" + file + "', found a line with " + line.length);
            }
            String provider = line[0];
            String field = line[1];
            String idScheme = line[2];
            String idValue = line[3];
            if (StringUtils.isBlank(provider)) {
              // Perfectly fine - we'll resolve the provider later
              provider = BloombergDataUtils.UNKNOWN_DATA_PROVIDER;
            }
            if (StringUtils.isBlank(field)) {
              s_logger.warn("Blank field value found in CSV file {} for identifier {}. This line will be ignored.", file, idValue);
              continue;
            }
            if (StringUtils.isBlank(idScheme)) {
              idScheme = _bbgUniqueId ? ExternalSchemes.BLOOMBERG_BUID.getName() : ExternalSchemes.BLOOMBERG_TICKER.getName();
            }
            if (StringUtils.isBlank(idValue)) {
              s_logger.warn("Blank identifier value found in CSV file {}. This line will be ignored.", file);
              continue;
            }
            Pair<String, String> providerFieldPair = Pair.of(provider, field);
            Set<ExternalId> providerRequests = result.get(providerFieldPair);
            if (providerRequests == null) {
              providerRequests = new HashSet<ExternalId>();
              result.put(providerFieldPair, providerRequests);
            }
            providerRequests.add(ExternalId.of(idScheme, idValue));
            total++;
          }
          reader.close();
        } catch (Exception e) {
          throw new OpenGammaRuntimeException("Problem reading from input file '" + file + "'", e);
        }
      }
      System.out.println(total);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  private void updateTimeSeriesInDB() {
    // load the info documents for all Bloomberg series that can be updated
    s_logger.info("Loading all time series information...");
    List<HistoricalTimeSeriesInfoDocument> documents = getCurrentTimeSeriesDocuments();
    s_logger.info("Loaded {} time series.", documents.size());
    
    // group Bloomberg request by dates/dataProviders/dataFields
    Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> bbgTSRequest = Maps.newHashMap();
    // store identifier to UID map for timeseries update
    Map<MetaDataKey, ObjectId> metaDataKeyMap = new HashMap<MetaDataKey, ObjectId>();
    if (_startDate != null) {
      bbgTSRequest.put(_startDate, new HashMap<String, Map<String, Set<ExternalIdBundle>>>());
    }
    int i = 0;
    int toUpdate = 0;
    for (HistoricalTimeSeriesInfoDocument doc : documents) {
      if (++i % 100 == 0) {
        s_logger.info("Checking required updates for time series {} of {} ", i, documents.size());
      }
      ManageableHistoricalTimeSeriesInfo info = doc.getInfo();
      ExternalIdBundle idBundle = info.getExternalIdBundle().toBundle();
      
      // select start date
      LocalDate startDate = _startDate;
      if (startDate == null) {
        // lookup start date as one day after the latest point in the series
        UniqueId htsId = doc.getInfo().getUniqueId();
        LocalDate latestDate = getLatestDate(htsId);
        if (isUpToDate(latestDate)) {
          s_logger.debug("Not scheduling update for up to date series {} from {}", htsId, latestDate);
          continue;  // up to date, so do not fetch
        }
        s_logger.debug("Scheduling update for series {} from {}", htsId, latestDate);
        toUpdate++;
        startDate = latestDate.plusDays(1);
      }
      Map<String, Map<String, Set<ExternalIdBundle>>> providerFieldIdentifiers = MapUtils.putIfAbsentGet(bbgTSRequest, startDate, new HashMap<String, Map<String, Set<ExternalIdBundle>>>());
      
      // select data provider
      String dataProvider = info.getDataProvider();
      Map<String, Set<ExternalIdBundle>> fieldIdentifiers = MapUtils.putIfAbsentGet(providerFieldIdentifiers, dataProvider, new HashMap<String, Set<ExternalIdBundle>>());
      
      // select data field
      String dataField = info.getDataField();
      Set<ExternalIdBundle> identifiers = MapUtils.putIfAbsentGet(fieldIdentifiers, dataField, new HashSet<ExternalIdBundle>());
      
      // store external id
      identifiers.add(idBundle);
      
      MetaDataKey metaDataKey = new MetaDataKey(idBundle, dataProvider, dataField);
      ObjectId previous = metaDataKeyMap.put(metaDataKey, doc.getInfo().getTimeSeriesObjectId());
      if (previous != null) {
        // if we don't check here then the master might fail, but it doesn't always 
        throw new OpenGammaRuntimeException("Duplicate time-series " + previous + " " + doc.getInfo().getTimeSeriesObjectId());
      }
    }
    
    // select end date
    LocalDate endDate = (_endDate == null ? DateUtils.previousWeekDay() : _endDate);
    
    s_logger.info("Updating {} time series to {}", toUpdate, endDate);
    // load from Bloomberg and store in database
    getAndUpdateHistoricalData(bbgTSRequest, metaDataKeyMap, endDate);
  }

  private LocalDate getLatestDate(UniqueId htsId) {
    LocalDateDoubleTimeSeries timeSeries = _timeSeriesMaster.getTimeSeries(htsId,
        HistoricalTimeSeriesGetFilter.ofLatestPoint()).getTimeSeries();
    if (timeSeries.isEmpty()) {
      return DEFAULT_START_DATE;
    } else {
      return timeSeries.getLatestTime();
    }
  }

  private boolean isUpToDate(LocalDate latestDate) {
    LocalDate previousWeekDay = DateUtils.previousWeekDay();
    return previousWeekDay.isBefore(latestDate) || previousWeekDay.equals(latestDate);
  }

  //-------------------------------------------------------------------------
  private List<HistoricalTimeSeriesInfoDocument> getCurrentTimeSeriesDocuments() {
    // loads all time-series that were originally loaded from Bloomberg
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setDataSource(BLOOMBERG_DATA_SOURCE_NAME);
    return removeExpiredTimeSeries(_timeSeriesMaster.search(request));
  }

  private List<HistoricalTimeSeriesInfoDocument> removeExpiredTimeSeries(final HistoricalTimeSeriesInfoSearchResult searchResult) {
    List<HistoricalTimeSeriesInfoDocument> result = Lists.newArrayList();
    LocalDate previousWeekDay = DateUtils.previousWeekDay();
    
    for (HistoricalTimeSeriesInfoDocument htsInfoDoc : searchResult.getDocuments()) {
      ManageableHistoricalTimeSeriesInfo tsInfo = htsInfoDoc.getInfo();

      boolean valid = getIsValidOn(previousWeekDay, tsInfo);
      if (valid) {
        result.add(htsInfoDoc);
      } else {
        s_logger.debug("Time series {} is not valid on {}", tsInfo.getUniqueId(), previousWeekDay);
      }
    }
    return result;
  }

  private boolean getIsValidOn(LocalDate previousWeekDay, ManageableHistoricalTimeSeriesInfo tsInfo) {
    boolean anyInvalid = false;
    for (ExternalIdWithDates id : tsInfo.getExternalIdBundle()) {
      if (id.isValidOn(previousWeekDay)) {
        if (id.getValidFrom() != null || id.getValidTo() != null) {
          //[PLAT-1724] If there is a ticker with expiry, which is valid, that's ok
          return true;
        }
      } else {
        anyInvalid = true;
      }
    }
    // Otherwise be very strict, since many things have tickers with no expiry 
    return !anyInvalid;
  }

  //-------------------------------------------------------------------------
  private void getAndUpdateHistoricalData(Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> bbgTSRequest,
      Map<MetaDataKey, ObjectId> metaDataKeyMap, LocalDate endDate) {
    // process the request
    for (Entry<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> entry : bbgTSRequest.entrySet()) {
      s_logger.debug("processing {}", entry);
      LocalDate startDate = entry.getKey();
      
      for (Entry<String, Map<String, Set<ExternalIdBundle>>> providerFieldIdentifiers : entry.getValue().entrySet()) {
        s_logger.debug("processing {}", providerFieldIdentifiers);
        String dataProvider = providerFieldIdentifiers.getKey();
        
        for (Entry<String, Set<ExternalIdBundle>> fieldIdentifiers : providerFieldIdentifiers.getValue().entrySet()) {
          s_logger.debug("processing {}", fieldIdentifiers);
          String dataField = fieldIdentifiers.getKey();
          Set<ExternalIdBundle> identifiers = fieldIdentifiers.getValue();
          
          String bbgDataProvider = BloombergDataUtils.resolveDataProvider(dataProvider);
          Map<ExternalIdBundle, HistoricalTimeSeries> bbgLoadedTS = getTimeSeries(dataField, startDate, endDate, bbgDataProvider, identifiers);
          if (bbgLoadedTS.size() < identifiers.size()) {
            s_logger.error("Failed to load time series for {}", Sets.difference(identifiers, bbgLoadedTS.keySet()));
          }
          storeUpdatedSeriesInDb(bbgLoadedTS, metaDataKeyMap, dataProvider, dataField);
        }
      }
    }
  }

  private void storeUpdatedSeriesInDb(Map<ExternalIdBundle, HistoricalTimeSeries> bbgLoadedTS, Map<MetaDataKey, ObjectId> metaDataKeyMap, String dataProvider, String dataField) {
    for (Entry<ExternalIdBundle, HistoricalTimeSeries> identifierTS : bbgLoadedTS.entrySet()) {
      // ensure data points are after the last stored data point
      LocalDateDoubleTimeSeries timeSeries = identifierTS.getValue().getTimeSeries();
      if (timeSeries.isEmpty()) {
        s_logger.info("No new data for series {} {}", dataField, identifierTS.getKey());
        continue;  // avoids errors in getLatestTime()
      }
      s_logger.info("Got {} new points for series {} {}", new Object[] {timeSeries.size(), dataField, identifierTS.getKey()});
      
      LocalDate latestTime = timeSeries.getLatestTime();
      timeSeries = timeSeries.subSeries(_startDate, true, latestTime, true);
      if (timeSeries != null && timeSeries.isEmpty() == false) {
        // metaDataKeyMap holds the object id of the series to be updated
        ExternalIdBundle idBundle = identifierTS.getKey();
        MetaDataKey metaDataKey = new MetaDataKey(idBundle, dataProvider, dataField);
        ObjectId oid = metaDataKeyMap.get(metaDataKey);
        try {
          if (_reload) {
            _timeSeriesMaster.correctTimeSeriesDataPoints(oid, timeSeries);
          } else {
            _timeSeriesMaster.updateTimeSeriesDataPoints(oid, timeSeries);
          }
        } catch (Exception ex) {
          s_logger.error("Error writing time-series " + oid, ex);
        }
      }
    }
  }

  private String getBloombergDataProvider(String requestDataProvider) {
    return (requestDataProvider == null || requestDataProvider.equals(BloombergDataUtils.UNKNOWN_DATA_PROVIDER)) ? null : requestDataProvider;
  }

  //-------------------------------------------------------------------------
  /**
   * Program main entry point
   * 
   * <pre>
   * usage: java com.opengamma.bbg.loader.BloombergHistoricalLoader [options]... [files]...
   *  -e,--end (yyyymmdd)                            End date
   *  -f,--fields (PX_LAST,VOLUME,LAST_VOLATILITY)   List of bloomberg fields
   *  -h,--help                                      Print this message
   *  -p,--dataproviders (CMPL,CMPT)                 List of data providers
   *  -pm                                            Load missing data from position master
   *  -r,--reload                                    Reload historical data
   *  -s,--start (yyyymmdd)                          Start date
   *  -u,--update                                    Update historical data in database
   *  -unique                                        BLOOMBERG UNIQUE IDS in files otherwise treat as BLOOMBERG_TICKERS
   *  -csv                                           Files are in CSV format (provider,field,id-scheme,id-value)
   * </pre>
   * 
   * @param args the command line arguments
   */
  public static void main(String[] args) {   // CSIGNORE
    PlatformConfigUtils.configureSystemProperties();
    
    Options options = createOptions();
    processCommandLineOptions(args, options);
  }

  /**
   * @param args
   * @param options
   */
  private static void processCommandLineOptions(String[] args, Options options) {
    CommandLineParser parser = new PosixParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      usage(options);
      return;
    }
    if (line.hasOption(HELP_OPTION)) {
      usage(options);
      return;
    }
    ConfigurableApplicationContext applicationContext = null;
    try {
      applicationContext = getApplicationContext();
      BloombergHistoricalLoader dataLoader = getDataHistoricalLoader(applicationContext);
      configureOptions(options, line, dataLoader);
      dataLoader.run();
      List<ExternalIdBundle> errors = dataLoader.getErrors();
      if (!errors.isEmpty()) {
        for (ExternalIdBundle bundle : errors) {
          s_logger.warn("Could not load historical data for {}", bundle);
        }
      }
    } finally {
      if (applicationContext != null) {
        applicationContext.close();
      }
    }
    
  }

  private static void configureOptions(Options options, CommandLine line, BloombergHistoricalLoader dataLoader) {
    //get files from command line if any
    String[] files = line.getArgs();
    dataLoader.setFiles(Arrays.asList(files));
    if (line.hasOption(UPDATE_OPTION)) {
      dataLoader.setUpdateDb(true);
    }
    if (line.hasOption(POSITION_MASTER_OPTION)) {
      dataLoader.setLoadPositionMaster(true);
    }
    if (line.hasOption(RELOAD_OPTION)) {
      dataLoader.setReload(true);
    }
    if (line.hasOption(CSV_OPTION)) {
      dataLoader.setCsv(true);
    }
    if (line.hasOption(DATAPROVIDERS_OPTION)) {
      if (dataLoader.isCsv()) {
        s_logger.warn("Cannot specify data providers with CSV input files, since providers are part of the CSV file.");
        usage(options);
        return;
      }

      String[] dataProviders = splitByComma(line.getOptionValue(DATAPROVIDERS_OPTION));
      dataLoader.setDataProviders(Arrays.asList(dataProviders));
    }
    if (line.hasOption(START_OPTION)) {
      String startOption = line.getOptionValue(START_OPTION);
      try {
        LocalDate startDate = DateUtils.toLocalDate(startOption);
        dataLoader.setStartDate(startDate);
      } catch (Exception ex) {
        s_logger.warn("unable to parse start date {}", startOption);
        usage(options);
        return;
      }
    }
    if (line.hasOption(END_OPTION)) {
      String endOption = line.getOptionValue(END_OPTION);
      try {
        LocalDate endDate = DateUtils.toLocalDate(endOption);
        dataLoader.setEndDate(endDate);
      } catch (Exception ex) {
        s_logger.warn("unable to parse end date {}", endOption);
        usage(options);
        return;
      }
    }
    String[] fields = null;
    if (line.hasOption(FIELDS_OPTION)) {
      if (dataLoader.isCsv()) {
        s_logger.warn("Cannot specify fields with CSV input files, since fields are part of the CSV file.");
        usage(options);
        return;
      }

      fields = splitByComma(line.getOptionValue(FIELDS_OPTION));
      dataLoader.setDataFields(Arrays.asList(fields));
    }

    if (line.hasOption(UNIQUE_ID_OPTION)) {
      dataLoader.setBbgUniqueId(true);
    }

    //check we have right options and input files
    if (files != null && files.length > 0 && !dataLoader.isCsv() && (fields == null || fields.length == 0)) {
      s_logger.warn("DataFields must be specified");
      usage(options);
      return;
    }
  }

  private static String[] splitByComma(String word) {
    return word.split(",\\s*");
  }
 
  private static BloombergHistoricalLoader getDataHistoricalLoader(ApplicationContext context) {    
    BloombergHistoricalLoader loader = (BloombergHistoricalLoader) context.getBean("htsLoader");
    return loader;
  }
  
  private static ConfigurableApplicationContext getApplicationContext() {
    ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(CONTEXT_CONFIGURATION_PATH);
    context.start();
    return context;
  }

  /**
   * @param options
   */
  private static void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + BloombergHistoricalLoader.class.getName() + " [options]... [files]...", options);
  }

  /**
   * @return
   */
  private static Options createOptions() {
    Options options = new Options();
    options.addOption(createHelpOption());
    options.addOption(createDataProviderOption());
    options.addOption(createFieldsOption());
    options.addOption(createReloadOption());
    options.addOption(createLoadPositionMasterOption());
    options.addOption(createUpdateOption());
    options.addOption(createStartOption());
    options.addOption(createEndOption());
    options.addOption(createUniqueOption());
    options.addOption(createCsvOption());
    return options;
  }
  
  private static Option createUniqueOption() {
    return new Option(UNIQUE_ID_OPTION, false, "BLOOMBERG UNIQUE IDS in files otherwise treat as BLOOMBERG_TICKERS");
  }
  
  private static Option createCsvOption() {
    OptionBuilder.withLongOpt(CSV_OPTION);
    OptionBuilder.withDescription("CSV input files");
    return OptionBuilder.create("csv");
  }


  private static Option createEndOption() {
    OptionBuilder.withLongOpt(END_OPTION);
    OptionBuilder.withDescription("End date");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("yyyymmdd");
    return OptionBuilder.create("e");
  }

  private static Option createStartOption() {
    OptionBuilder.withLongOpt(START_OPTION);
    OptionBuilder.withDescription("Start date");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("yyyymmdd");
    return OptionBuilder.create("s");
  }

  private static Option createUpdateOption() {
    return new Option("u", UPDATE_OPTION, false, "Update historical data in database");
  }

  private static Option createReloadOption() {
    return new Option("r", RELOAD_OPTION, false, "Reload historical data");
  }

  private static Option createLoadPositionMasterOption() {
    return new Option(POSITION_MASTER_OPTION, false, "Load missing data from position master");
  }
  
  private static Option createHelpOption() {
    return new Option("h", HELP_OPTION, false, "Print this message");
  }

  private static Option createFieldsOption() {
    OptionBuilder.withLongOpt(FIELDS_OPTION);
    OptionBuilder.withDescription("List of bloomberg fields");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("PX_LAST,VOLUME,LAST_VOLATILITY");
    return OptionBuilder.create("f");
  }
  
  private static Option createDataProviderOption() {
    OptionBuilder.withLongOpt(DATAPROVIDERS_OPTION);
    OptionBuilder.withDescription("List of data providers");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("CMPL,CMPT");
    return OptionBuilder.create("p");
  }
  
  private static class MetaDataKey {
    private ExternalIdBundle _identifiers;
    private String _dataProvider;
    private String _field;
    
    public MetaDataKey(ExternalIdBundle identifiers, String dataProvider, String field) {
      _identifiers = identifiers;
      _dataProvider = dataProvider;
      _field = field;
    }
    
    @Override
    public int hashCode() {
      return _identifiers.hashCode() ^ _field.hashCode();
    }
    
    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof MetaDataKey)) {
        return false;
      }
      MetaDataKey other = (MetaDataKey) obj;
      if (_field == null) {
        if (other._field != null) {
          return false;
        }
      } else if (!_field.equals(other._field)) {
        return false;
      }
      if (_identifiers == null) {
        if (other._identifiers != null) {
          return false;
        }
      } else if (!_identifiers.equals(other._identifiers)) {
        return false;
      }
      if (_dataProvider == null) {
        if (other._dataProvider != null) {
          return false;
        }
      } else if (!_dataProvider.equals(other._dataProvider)) {
        return false;
      }
      return true;
    }
  }

  /**
   * Gets the errors field.
   * @return the errors
   */
  public List<ExternalIdBundle> getErrors() {
    return Collections.unmodifiableList(_errors);
  }

  //-------------------------------------------------------------------------
  private Map<ExternalIdBundle, HistoricalTimeSeries> getTimeSeries(
      final String dataField, final LocalDate startDate, final LocalDate endDate, String bbgDataProvider, Set<ExternalIdBundle> identifierSet) {
    s_logger.debug("Loading time series {} ({}-{}) {}: {}", new Object[] {dataField, startDate, endDate, bbgDataProvider, identifierSet});
    return _bbgHistoricalTimeSeriesSource.getHistoricalTimeSeries(
        identifierSet, BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME, bbgDataProvider, dataField, startDate, true, endDate, true);
  }

}
