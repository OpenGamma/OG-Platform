/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.security.Security;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.copier.portfolio.PortfolioCopier;
import com.opengamma.integration.copier.portfolio.PortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.QuietPortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.SimplePortfolioCopier;
import com.opengamma.integration.copier.portfolio.VerbosePortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.reader.PositionReader;
import com.opengamma.integration.copier.portfolio.writer.PositionWriter;
import com.opengamma.integration.copier.portfolio.writer.PrettyPrintingPositionWriter;
import com.opengamma.integration.copier.portfolio.writer.ZippedPositionWriter;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Tool to generate a template for doing field mapping tasks
 */
@Scriptable
public class PortfolioZipFormatExamplesGenerator extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioZipFormatExamplesGenerator.class);
  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Verbose option flag */
  private static final String VERBOSE_OPT = "v";
  /** Include trades flag */
  private static final String INCLUDE_TRADES_OPT = "t";

  @Override
  protected void doRun() throws Exception {

    List<ManageablePosition> positions = loadSomePositions(getCommandLine().hasOption(INCLUDE_TRADES_OPT));
    // Construct portfolio reader
    PositionReader positionReader = new MyPositionReader(positions);

    // Create portfolio writer
    PositionWriter positionWriter = constructPortfolioWriter(
        getCommandLine().getOptionValue(FILE_NAME_OPT),
        getCommandLine().hasOption(WRITE_OPT),
        getCommandLine().hasOption(INCLUDE_TRADES_OPT));

    // Construct portfolio copier
    PortfolioCopier portfolioCopier = new SimplePortfolioCopier();

    // Create visitor for verbose/quiet mode
    PortfolioCopierVisitor portfolioCopierVisitor; 
    if (getCommandLine().hasOption(VERBOSE_OPT)) {
      portfolioCopierVisitor = new VerbosePortfolioCopierVisitor();
    } else {
      portfolioCopierVisitor = new QuietPortfolioCopierVisitor();
    }

    // Call the portfolio loader with the supplied arguments
    portfolioCopier.copy(positionReader, positionWriter, portfolioCopierVisitor);

    // close stuff
    positionReader.close();
    positionWriter.close();
  }
  
  private static final Set<String> UNSUPPORTED_SECURITY_TYPES = Sets.newHashSet("CDS_INDEX", "CDS_INDEX_DEFINITION", "CDS", "RAW", "XXX", "MANAGEABLE", 
                                                                                "EXTERNAL_SENSITIVITIES_SECURITY", "EXTERNAL_SENSITIVITY_RISK_FACTORS"); 
                                                                                // not enough string conversion stuff there for these yet

  private List<ManageablePosition> loadSomePositions(boolean includeTrades) {
    List<ManageablePosition> positions = new ArrayList<ManageablePosition>();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    SecurityMetaDataRequest metaRequest = new SecurityMetaDataRequest();
    SecurityMetaDataResult metaData = securityMaster.metaData(metaRequest);
    for (String securityType : metaData.getSecurityTypes()) {
      if (UNSUPPORTED_SECURITY_TYPES.contains(securityType)) {
        continue;
      }
      s_logger.info("Processing security type " + securityType);
      SecuritySearchRequest searchRequest = new SecuritySearchRequest();
      searchRequest.setName("*");
      searchRequest.setSecurityType(securityType);
      searchRequest.setPagingRequest(PagingRequest.FIRST_PAGE);
      SecuritySearchResult search = securityMaster.search(searchRequest);
      s_logger.info("Search returned " + search.getPaging().getTotalItems() + " securities");
      List<ManageableSecurity> securities = search.getSecurities();
      int count = 0;
      for (ManageableSecurity security : securities) {
        if (security == null) {
          s_logger.error("null security of type " + securityType);
          continue;
        }
        count++;
        positions.add(createPosition(security, includeTrades));
        if (count == 3) {
          s_logger.info("Reached count of 3");
          break;
        }
      }
    }
    return positions;
  }

  private class MyPositionReader implements PositionReader {

    private List<ManageablePosition> _positions;
    private Iterator<ManageablePosition> _iterator;

    public MyPositionReader(List<ManageablePosition> positions) {
      _positions = positions;
      _iterator = _positions.iterator();
    }

    @Override
    public ObjectsPair<ManageablePosition, ManageableSecurity[]> readNext() {
      if (!_iterator.hasNext()) {
        return null;
      }
      ManageablePosition position = _iterator.next();
      
      // Write the related security(ies)
      ManageableSecurityLink sLink = position.getSecurityLink();
      Security security = sLink.resolveQuiet(getToolContext().getSecuritySource());
      if ((security != null) && (security instanceof ManageableSecurity)) {

        // Find underlying security
        // TODO support multiple underlyings; unfortunately the system does not provide a standard way
        // to retrieve underlyings
        if (((ManageableSecurity) security).propertyNames().contains("underlyingId")) {
          ExternalId id = (ExternalId) ((ManageableSecurity) security).property("underlyingId").get();

          Security underlying;
          try {
            underlying = getToolContext().getSecuritySource().getSingle(id.toBundle());
            if (underlying != null) {
              return ObjectsPair.of(position,
                  new ManageableSecurity[] {(ManageableSecurity) security, (ManageableSecurity) underlying });
            } else {
              s_logger.warn("Could not resolve underlying " + id + " for security " + security.getName());
            }
          } catch (Throwable e) {
            // Underlying not found
            s_logger.warn("Error trying to resolve underlying " + id + " for security " + security.getName());
          }
        }
        return ObjectsPair.of(position,
            new ManageableSecurity[] {(ManageableSecurity) security });

      } else {
        s_logger.warn("Could not resolve security relating to position " + position.getName());
        return ObjectsPair.of(null, null);
      }
    }

    @Override
    public String[] getCurrentPath() {
      return new String[] {};
    }

    @Override
    public void close() {
    }

    @Override
    public String getPortfolioName() {
      return "Example";
    }
  }

  private ManageablePosition createPosition(ManageableSecurity security, boolean includeTrade) {
    ManageablePosition position = new ManageablePosition(BigDecimal.ONE, security.getExternalIdBundle());
    if (includeTrade) {
      ManageableTrade trade = new ManageableTrade(BigDecimal.ONE, security.getExternalIdBundle(), LocalDate.now().minusDays(3), OffsetTime.now(), ExternalId.of("Cpty", "GOLDMAN"));
      position.addTrade(trade);
    }
    return position;
  }

  private static PositionWriter constructPortfolioWriter(String filename, boolean write,
      boolean includeTrades) {
    if (write) {
      // Check that the file name was specified on the command line
      if (filename == null) {
        throw new OpenGammaRuntimeException("File name omitted, cannot export to file");
      }

      if (SheetFormat.of(filename) == SheetFormat.ZIP) {
        return new ZippedPositionWriter(filename, includeTrades);
      } else {
        throw new OpenGammaRuntimeException("Input filename should end in .ZIP");
      }

    } else {
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new PrettyPrintingPositionWriter(true);
    }
  }

  @Override
  protected Options createOptions(boolean contextProvided) {

    Options options = super.createOptions(contextProvided);

    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file to create and export to (CSV, XLS or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);

    Option writeOption = new Option(
        WRITE_OPT, "write", false,
        "Actually persists the portfolio to the file if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);

    Option verboseOption = new Option(
        VERBOSE_OPT, "verbose", false,
        "Displays progress messages on the terminal");
    options.addOption(verboseOption);

    Option includeTradesOption = new Option(
        INCLUDE_TRADES_OPT, "trades", false,
        "Generate a separate row for each trade instead of one row per position");
    options.addOption(includeTradesOption);

    return options;
  }

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new PortfolioZipFormatExamplesGenerator().invokeAndTerminate(args);
  }

}
