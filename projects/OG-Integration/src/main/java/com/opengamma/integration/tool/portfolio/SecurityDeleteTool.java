/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ObjectId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.impl.SecuritySearchIterator;
import com.opengamma.scripts.Scriptable;

/**
 * The portfolio loader tool
 */
@Scriptable
public class SecurityDeleteTool extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SecurityDeleteTool.class);

  /** Delete securities option flag */
  private static final String SECURITY_NAMES_OPT = "n";
  /** Delete security ids option flag */
  private static final String SECURITY_IDS_OPT = "i";
  /** External Id scheme wildcard search flag */
  private static final String EXTERNAL_ID_SCHEMES_OPT = "es";
  /** External Id value wildcard search flag */
  private static final String EXTERNAL_ID_VALUES_OPT = "ev";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Verbose option flag */
  private static final String VERBOSE_OPT = "v";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new SecurityDeleteTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Deletes securities from the specified sec master.
   */
  @Override
  protected void doRun() {    
    SecuritySearchRequest securitySearchRequest = new SecuritySearchRequest();

    if (getCommandLine().hasOption(SECURITY_NAMES_OPT)) {
      securitySearchRequest.setName(
          getCommandLine().getOptionValue(SECURITY_NAMES_OPT));
    }
    if (getCommandLine().hasOption(SECURITY_IDS_OPT)) {
      List<ObjectId> ids = new ArrayList<ObjectId>();
      for (String s : getCommandLine().getOptionValues(SECURITY_IDS_OPT)) {
        ids.add(ObjectId.parse(s));
      }
      securitySearchRequest.setObjectIds(ids);
    }
    if (getCommandLine().hasOption(EXTERNAL_ID_SCHEMES_OPT)) {
      securitySearchRequest.setExternalIdScheme(
          getCommandLine().getOptionValue(EXTERNAL_ID_SCHEMES_OPT));
    }
    if (getCommandLine().hasOption(EXTERNAL_ID_VALUES_OPT)) {
      securitySearchRequest.setExternalIdValue(
          getCommandLine().getOptionValue(EXTERNAL_ID_VALUES_OPT));
    }
    
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    for (SecurityDocument securityDocument : SecuritySearchIterator.iterable(securityMaster, securitySearchRequest)) {
      if (getCommandLine().hasOption(WRITE_OPT)) {
        securityMaster.remove(securityDocument.getUniqueId());
        s_logger.warn("Deleted " + securityDocument.getSecurity().getUniqueId() + 
            " (" + securityDocument.getSecurity().getName() + ")");
      } else {
        s_logger.warn("Matched " + securityDocument.getSecurity().getUniqueId() + 
            " (" + securityDocument.getSecurity().getName() + ")");
      }

    }
  }

  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
    Option securityNamesOption = new Option(
        SECURITY_NAMES_OPT, "name", true, "Regular expression to match security names");
    options.addOption(securityNamesOption);

    Option securityIdsOption = new Option(
        SECURITY_IDS_OPT, "securityid", true, "Security IDs to match");    
    options.addOption(securityIdsOption);

//    OptionGroup group = new OptionGroup();
//    group.addOption(securityIdsOption);
//    group.addOption(securityNamesOption);
//    group.setRequired(true);    
//    options.addOptionGroup(group);

    Option externalIdSchemesOption = new Option(
        EXTERNAL_ID_SCHEMES_OPT, "extscheme", true, 
        "Regular expression to match external ID schemes");
    options.addOption(externalIdSchemesOption);

    Option externalIdValuesOption = new Option(
        EXTERNAL_ID_VALUES_OPT, "extvalue", true, 
        "Regular expression to match external ID values");
    options.addOption(externalIdValuesOption);

    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persist the deletions");
    options.addOption(writeOption);

    Option verboseOption = new Option(
        VERBOSE_OPT, "verbose", false, 
        "Displays progress messages on the terminal");
    options.addOption(verboseOption);

    return options;
  }

}
