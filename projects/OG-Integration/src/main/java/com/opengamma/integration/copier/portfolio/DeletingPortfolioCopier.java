/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.copier.portfolio.reader.PositionReader;
import com.opengamma.integration.copier.portfolio.writer.PositionWriter;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * A portfolio copier that copies positions/securities from the reader to the writer while deleting them
 * from the specified masters.
 */
public class DeletingPortfolioCopier implements PortfolioCopier {

  private static final Logger s_logger = LoggerFactory.getLogger(DeletingPortfolioCopier.class);

  private SecurityMaster _securityMaster;
  private PositionMaster _positionMaster;
  private boolean _write;
  
  public DeletingPortfolioCopier(SecurityMaster securityMaster, PositionMaster positionMaster, boolean write) {
    
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");

    _securityMaster = securityMaster;
    _positionMaster = positionMaster;
    _write = write;
  }

  @Override
  public void copy(PositionReader positionReader, PositionWriter positionWriter) {
    copy(positionReader, positionWriter, null);
  }

  public void copy(PositionReader positionReader, PositionWriter positionWriter,
      boolean deletePositions, boolean deleteSecurities) {
    copy(positionReader, positionWriter, null, deletePositions, deleteSecurities);
  }

  public void copy(PositionReader positionReader, PositionWriter positionWriter, PortfolioCopierVisitor visitor) {
    copy(positionReader, positionWriter, visitor, true, true);
  }

  public void copy(PositionReader positionReader, PositionWriter positionWriter, PortfolioCopierVisitor visitor,
      boolean deletePositions, boolean deleteSecurities) {

    ArgumentChecker.notNull(positionWriter, "positionWriter");
    ArgumentChecker.notNull(positionReader, "positionReader");
    
    ObjectsPair<ManageablePosition, ManageableSecurity[]> next;

    // Read in next row, checking for EOF
    while ((next = positionReader.readNext()) != null) {
            
      // Is position and security data is available for the current row?
      if (next.getFirst() != null && next.getSecond() != null) {
        
        // Delete positions
        if (deletePositions) {
          if (_write) {
            try {
              _positionMaster.remove(next.getFirst().getUniqueId());
              s_logger.warn("Deleted " + next.getFirst().getUniqueId() + " (" + next.getFirst().getName() + ")");
            } catch (Throwable e) {
              throw new OpenGammaRuntimeException("Could not remove position " + 
                  next.getFirst().getName() + " (" + next.getFirst().getUniqueId().toString() + ")");          
            }
          } else {
            s_logger.warn("Matched " + next.getFirst().getUniqueId() + " (" + next.getFirst().getName() + ")");
          }
        }
        
        // Delete securities
        if (deleteSecurities) {
          for (ManageableSecurity security : next.getSecond()) {
            if (_write) {
              try {
                _securityMaster.remove(security.getUniqueId());
                s_logger.warn("Deleted " + security.getUniqueId() + " (" + security.getName() + ")");
              } catch (Throwable e) {
                throw new OpenGammaRuntimeException("Could not remove security " + 
                    security.getName() + " (" + security.getUniqueId().toString() + ")");          
              }
            } else {
              s_logger.warn("Matched " + security.getUniqueId() + " (" + security.getName() + ")");            
            }
          }
        }
        
        // Set current path
        String[] path = positionReader.getCurrentPath();
        positionWriter.setPath(path);
        
        // Write position and security data
        ObjectsPair<ManageablePosition, ManageableSecurity[]> written = 
            positionWriter.writePosition(next.getFirst(), next.getSecond());
        
        if (visitor != null) {
          visitor.info(StringUtils.arrayToDelimitedString(path, "/"), written.getFirst(), written.getSecond());
        }
      } else {
        if (visitor != null) {
          visitor.error("Could not load" + (next.getFirst() == null ? " position" : "") + (next.getSecond() == null ? " security" : ""));
        }
      }
    }
    
    // Flush changes to portfolio master
    positionWriter.flush();
  }
}
