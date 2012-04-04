/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.id.ObjectId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;

/**
 * The portfolio loader tool
 */
public class SecurityDeleteTool extends AbstractTool {

  private static final Logger s_logger = LoggerFactory.getLogger(SecurityDeleteTool.class);

  /** Delete securities option flag */
  private static final String SECURITY_NAMES_OPT = "n";
  /** Delete security ids option flag */
  private static final String SECURITY_IDS_OPT = "i";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Verbose option flag */
  private static final String VERBOSE_OPT = "v";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new SecurityDeleteTool().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio into the position master.
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

    SecuritySearchResult securitySearchResult = getToolContext().getSecurityMaster().search(securitySearchRequest);

    for (SecurityDocument securityDocument : securitySearchResult.getDocuments()) {
      if (getCommandLine().hasOption(WRITE_OPT)) {
        getToolContext().getPositionMaster().remove(securityDocument.getUniqueId());
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
    
    Option securityIdsOption = new Option(
        SECURITY_IDS_OPT, "securityid", true, "Security IDs to match");
    
    OptionGroup group = new OptionGroup();
    group.addOption(securityIdsOption);
    group.addOption(securityNamesOption);
    group.setRequired(true);
    
    options.addOptionGroup(group);

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
