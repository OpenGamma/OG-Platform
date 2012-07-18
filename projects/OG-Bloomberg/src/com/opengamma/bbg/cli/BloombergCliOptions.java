/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Creates common command line options for Bloomberg scripts
 */
public final class BloombergCliOptions {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergCliOptions.class);
  /**
   * Fields option name.
   */
  public static final String FIELDS_OPTION = "fields";
  /**
   * Fields file option name.
   */
  public static final String FIELDS_FILE_OPTION = "fieldsFile";
  /**
   * Data providers option name.
   */
  public static final String DATAPROVIDERS_OPTION = "dataproviders";
  /**
   * Help option name.
   */
  public static final String HELP_OPTION = "help";
  /**
   * Position master option name.
   */
  public static final String POSITION_MASTER_OPTION = "pm";
  /**
   * Reload option name.
   */
  public static final String RELOAD_OPTION = "reload";
  /**
   * Update option name.
   */
  public static final String UPDATE_OPTION = "update";
  /**
   * Start date option name.
   */
  public static final String START_OPTION = "start";
  /**
   * End date option name.
   */
  public static final String END_OPTION = "end";
  /**
   * Unique option name.
   */
  public static final String UNIQUE_ID_OPTION = "unique";
  /**
   * CSV option name.
   */
  public static final String CSV_OPTION = "csv";
  /**
   * Identifiers option name.
   */
  public static final String IDENTIFIERS_OPTION = "identifiers";
  /**
   * Output file option name.
   */
  public static final String OUPUT_OPTION = "output";
  /**
   * Host option name.
   */
  public static final String HOST_OPTION = "host";
  /**
   * Port option name.
   */
  public static final String PORT_OPTION = "port";
  
  private boolean _dataFields;
  private boolean _dataProviders;
  private boolean _help;
  private boolean _identifiers;
  private boolean _output;
  private boolean _dataFieldsRequired;
  private boolean _dataProvidersRequired;
  private boolean _identifiersRequired;
  private boolean _outputRequired;
  private boolean _dataFieldsFileRequired;
  private boolean _dataFieldsFile;
  
  private Options _options;
  private boolean _port;
  private boolean _portRequired;
  private boolean _hostRequired;
  private boolean _host;
    
  /**
   * Restricted constructor.
   */
  private BloombergCliOptions() {
  }
    
  /**
   * Checks if options has data fields option.
   * @return true or false
   */
  public boolean hasFields() {
    return _dataFields;
  }
  
  /**
   * Checks if options has data fields file option.
   * @return true or false
   */
  public boolean hasFieldsFile() {
    return _dataFieldsFile;
  }

  /**
   * Checks if options has dataProviders option.
   * @return true or false
   */
  public boolean hasDataProviders() {
    return _dataProviders;
  }

  /**
   * Checks if options has help option.
   * @return true or false
   */
  public boolean hasHelp() {
    return _help;
  }

  /**
   * Checks if options has identifiers option.
   * @return true or false
   */
  public boolean hasIdentifiers() {
    return _identifiers;
  }

  /**
   * Checks if options has output option.
   * @return true or false
   */
  public boolean hasOutput() {
    return _output;
  }
  
  /**
   * Checks if options has host option.
   * @return true or false
   */
  public boolean hasHost() {
    return _host;
  }
  
  /**
   * Checks if options has port option.
   * @return true or false
   */
  public boolean hasPort() {
    return _port;
  }

  private void createOptions() {
    _options = new Options();
    if (hasFields()) {
      _options.addOption(createFieldsOption());
    }
    if (hasFieldsFile()) {
      _options.addOption(createDataFieldsFileOption());
    }
    if (hasDataProviders()) {
      _options.addOption(createDataProviderOption());
    }
    if (hasHelp()) {
      _options.addOption(createHelpOption());
    }
    if (hasIdentifiers()) {
      _options.addOption(createIdentifiersOption());
    }
    if (hasOutput()) {
      _options.addOption(createOutputOption());
    }
    if (hasHost()) {
      _options.addOption(createHostOption());
    }
    if (hasPort()) {
      _options.addOption(createPortOption());
    }
//    options.addOption(createReloadOption());
//    options.addOption(createLoadPositionMasterOption());
//    options.addOption(createUpdateOption());
//    options.addOption(createStartOption());
//    options.addOption(createEndOption());
//    options.addOption(createUniqueOption());
//    options.addOption(createCsvOption());
  }
  
  public Options getOptions() {
    return _options;
  }
  
  public void printUsage(Class<?> clazz) {
    ArgumentChecker.notNull(clazz, "class");
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + clazz.getSimpleName() + " [options]... [files]...", _options);
  }
  
  private Option createOutputOption() {
    Option option = new Option("o", OUPUT_OPTION, true, "output file");
    option.setRequired(_outputRequired);
    return option;
  }

  private Option createFieldsOption() {
    Option option = new Option("F", FIELDS_OPTION, true, "List of bloomberg fields");
    option.setArgName("PX_LAST,VOLUME,LAST_VOLATILITY");
    option.setRequired(_dataFieldsRequired);
    return option;
  }
  
  private Option createDataFieldsFileOption() {
    Option option = new Option("f", FIELDS_FILE_OPTION, true, "data fields file");
    option.setRequired(_dataFieldsFileRequired);
    return option;
  }

  private Option createDataProviderOption() {
    Option option = new Option("p", DATAPROVIDERS_OPTION, true, "List of data providers");
    option.setArgName("CMPL,CMPT");
    option.setRequired(_dataProvidersRequired);
    return option;
  }
  
  private Option createIdentifiersOption() {
    Option option = new Option("i", IDENTIFIERS_OPTION, true, "identifiers file");
    option.setRequired(_identifiersRequired);
    return option;
  }

  private Option createHelpOption() {
    return new Option(HELP_OPTION, false, "Print this message");
  }
  
  private Option createHostOption() {
    Option option = new Option("h", HOST_OPTION, true, "bloomberg server host");
    option.setRequired(_hostRequired);
    return option;
  }
  
  private Option createPortOption() {
    Option option = new Option("p", PORT_OPTION, true, "bloomberg server port");
    option.setRequired(_portRequired);
    return option;
  }

  private void setDataFields(boolean field) {
    _dataFields = field;
  }
  
  /**
   * @param dataFieldsRequired
   */
  private void setDataFieldsRequired(boolean dataFieldsRequired) {
    _dataFieldsRequired = dataFieldsRequired;
  }
  
  private void setDataProviders(boolean dataProviders) {
    _dataProviders = dataProviders;
  }
  
  /**
   * @param dataProvidersRequired
   */
  private void setDataProvidersRequired(boolean dataProvidersRequired) {
    _dataProvidersRequired = dataProvidersRequired;
  }
  
  private void setHelp(boolean help) {
    _help = help;
  }
  
  private void setIdentifiers(boolean identifer) {
    _identifiers = identifer;
  }
  
  /**
   * @param identifiersRequired
   */
  private void setIdentifiersRequired(boolean identifiersRequired) {
    _identifiersRequired = identifiersRequired;
  }
  
  private void setOutput(boolean output) {
    _output = output;
  }
  
  /**
   * @param outputRequired
   */
  private void setOutputRequired(boolean outputRequired) {
    _outputRequired = outputRequired;
  }

  /**
   * @param dataFieldsFileRequired
   */
  private void setDataFieldsFileRequired(boolean dataFieldsFileRequired) {
    _dataFieldsFileRequired = dataFieldsFileRequired;
  }

  /**
   * @param dataFieldsFile
   */
  private void setDataFieldsFile(boolean dataFieldsFile) {
    _dataFieldsFile = dataFieldsFile;
  }
  
  /**
   * @param port
   */
  private void setPort(boolean port) {
    _port = port;
  }
  
  /**
   * @param portRequired
   */
  private void setPortRequired(boolean portRequired) {
    _portRequired = portRequired;
  }

  /**
   * @param hostRequired
   */
  private void setHostRequired(boolean hostRequired) {
    _hostRequired = hostRequired;
  }

  /**
   * @param host
   */
  private void setHost(boolean host) {
    _host = host;
  }
  
  /**
   * Parses the command line arguments with created options
   * 
   * @param args the command line
   * @return the commandline or null if it can not be parsed
   */
  public CommandLine parse(String[] args) {
    CommandLineParser parser = new PosixParser();
    CommandLine result = null;
    try {
      result = parser.parse(_options, args);
    } catch (ParseException e) {
      s_logger.warn("error parsing command line arguments {}", new Object[]{args});
    }
    return result;
  }

  /**
   * Builds command line options based on requested parameters
   */
  public static class Builder {
    
    private boolean _dataFields;
    private boolean _dataFieldsRequired;
    private boolean _dataFieldsFile;
    private boolean _dataFieldsFileRequired;
    private boolean _dataProviders;
    private boolean _dataProvidersRequired;
    private boolean _help;
    private boolean _identifiers;
    private boolean _identifiersRequired;
    private boolean _output;
    private boolean _outputRequired;
    private boolean _host;
    private boolean _hostRequired;
    private boolean _port;
    private boolean _portRequired;

    public Builder() {
      _help = true;
    }
    
    /**
     * Builds options with data fields
     * 
     * @param required true if required
     * @return the builder
     */
    public Builder withDataFields(boolean required) {
      _dataFields = true;
      _dataFieldsRequired = required;
      return this;
    }
    
    public Builder withDataFieldsFile(boolean required) {
      _dataFieldsFile = true;
      _dataFieldsFileRequired = required;
      return this;
    }
    
    /**
     * Builds options with data provider
     * @param required true if required
     * @return the builder
     */
    public Builder withDataProviders(boolean required) {
      _dataProviders = true;
      _dataProvidersRequired = required;
      return this;
    }
    
    public Builder withHelp() {
      _help = true;
      return this;
    }
    
    public Builder withIdentifiers(boolean required) {
      _identifiers = true;
      _identifiersRequired = required;
      return this;
    }
    
    public Builder withOutput(boolean required) {
      _output = true;
      _outputRequired = required;
      return this;
    }
    
    public Builder withHost(boolean required) {
      _host = true;
      _hostRequired = required;
      return this;
    }
    
    public Builder withPort(boolean required) {
      _port = true;
      _portRequired = required;
      return this;
    }
    
    public BloombergCliOptions build() {
      BloombergCliOptions builder = new BloombergCliOptions();
      
      builder.setDataFields(_dataFields);
      builder.setDataFieldsRequired(_dataFieldsRequired);
      
      builder.setDataFieldsFile(_dataFieldsFile);
      builder.setDataFieldsFileRequired(_dataFieldsFileRequired);
      
      builder.setDataProviders(_dataProviders);
      builder.setDataProvidersRequired(_dataProvidersRequired);
      
      builder.setHelp(_help);
      
      builder.setIdentifiers(_identifiers);
      builder.setIdentifiersRequired(_identifiersRequired);
      
      builder.setOutput(_output);
      builder.setOutputRequired(_outputRequired);
      
      builder.setHost(_host);
      builder.setHostRequired(_hostRequired);
      
      builder.setPort(_port);
      builder.setPortRequired(_portRequired);
      
      builder.createOptions();
      return builder;
    }

  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  
}
