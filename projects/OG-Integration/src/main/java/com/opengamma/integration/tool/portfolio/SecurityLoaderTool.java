/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.loader.BloombergBulkSecurityLoader;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMasterUtils;


/**
 * Tool to load a list of Securities defined by ExternalId (eg Bloomberg Ticker, ISIN)
 */
public class SecurityLoaderTool extends AbstractTool<IntegrationToolContext> {
  
  /** Portfolio name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Verbose option flag */
  private static final String VERBOSE_OPT = "v";
  
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new SecurityLoaderTool().invokeAndTerminate(args);
  }
  
  /**
   * Loads the list of securities into the security master.
   */
  @Override
  protected void doRun() throws Exception {
    
    // build list of security ids
    Set<ExternalId> externalIds = new HashSet<ExternalId>();
    final String filename = getCommandLine().getOptionValue(FILE_NAME_OPT);
    final InputStream fileInputStream = openFile(filename);
    final CSVReader reader = new CSVReader(new InputStreamReader(fileInputStream));
    String[] headers = reader.readNext();
    if (headers.length > 1) {
      reader.close();
      throw new OpenGammaRuntimeException(filename + " must contain a single column with a header row.");
    }
    String id = headers[0];

    while (true) {
      final String[] row = reader.readNext();
      if (row == null) {
        break;
      }
      externalIds.add(ExternalId.of(id, row[0]));
    }
    reader.close();
    
    // filter out ids that are already loaded into the sec master
    final Set<ExternalId> unloadedIds = filterPresentIds(externalIds);
    
    // Load the new securities
    loadSecurityData(getCommandLine().hasOption(WRITE_OPT), unloadedIds);
  }
  
  private Set<ExternalId> filterPresentIds(Set<ExternalId> externalIds) {
    Set<ExternalId> filtered = new HashSet<>();
    SecuritySource securitySource = getToolContext().getSecuritySource();
    for (ExternalId externalId : externalIds) {
      Collection<Security> securitySet = securitySource.get(externalId.toBundle());
      if (securitySet.isEmpty()) {
        if (isVerbose()) {
          System.out.println(externalId.getValue() + ": will be added");
        }
        filtered.add(externalId);
      } else {
        if (isVerbose()) {
          System.out.println(externalId.getValue() + ": exists");
        }
      }
    }
    if (isVerbose()) {
      System.out.println("Of " + externalIds.size() + " ids, " + filtered.size() + " were not present in the security master");
    }
    return filtered;
  }
  
  private void loadSecurityData(final boolean write, final Set<ExternalId> externalIds) {
    BloombergBulkSecurityLoader bulkSecurityLoader = new BloombergBulkSecurityLoader(getToolContext().getBloombergReferenceDataProvider(), DefaultExchangeDataProvider.getInstance());
    SecurityMaster secMaster = getToolContext().getSecurityMaster();
    Set<ExternalIdBundle> externalIdBundles = new HashSet<>();
    for (ExternalId externalId : externalIds) {
      externalIdBundles.add(externalId.toBundle());
    }
    Map<ExternalIdBundle, ManageableSecurity> loadedSecurities = bulkSecurityLoader.loadSecurity(externalIdBundles);
    for (ManageableSecurity entry : loadedSecurities.values()) {
      if (write) {
        ManageableSecurity security = SecurityMasterUtils.addOrUpdateSecurity(secMaster, entry);
        if (isVerbose()) {
          System.out.println("Loaded security " + security.getExternalIdBundle().toString());
          System.out.println("New UniqueId =  " + security.getUniqueId());
        }
      } else {
        System.out.println("Created but did not add security " + entry.toString());
      }

    }
  }
  
  private boolean isVerbose() {
    return getCommandLine().hasOption(VERBOSE_OPT);
  }
  
  @Override
  protected Options createOptions(final boolean contextProvided) {

    final Options options = super.createOptions(contextProvided);

    final Option fileNameOption = new Option(FILE_NAME_OPT, "filename", true, "The name of the file containing a column of ExternalIds." +
        " The header must contain the Scheme, remaining rows the Schemes value");
    fileNameOption.setRequired(true);
    options.addOption(fileNameOption);

    final Option writeOption = new Option(WRITE_OPT, "write", false, "Actually persists the time series to the database if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);

    final Option verboseOption = new Option(VERBOSE_OPT, "verbose", false, "Displays progress messages on the terminal");
    options.addOption(verboseOption);

    return options;
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
}
