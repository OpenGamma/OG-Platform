package com.opengamma.integration.swing;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.jidesoft.hints.ListDataIntelliHints;
import com.opengamma.util.time.Tenor;

/**
 * Class for implementing a Tenor component
 */
public class TenorField extends JTextField {

  private static final Logger s_logger = LoggerFactory.getLogger(TenorField.class);
  public TenorField() {
    super();
    //ListDataIntelliHints<Tenor> intelliHints = new ListDataIntelliHints<>(this, getAllTenors());
  }

  private List<Tenor> getAllTenors() {
    List<Tenor> tenors = new ArrayList<>();
    Field[] fields = Tenor.class.getFields();
    for (Field field : fields) {
      if (field.isAccessible() && field.getType().isAssignableFrom(Tenor.class)) {
        try {
          tenors.add((Tenor) field.get(field));
        } catch (IllegalArgumentException | IllegalAccessException ex) {
          // TODO Auto-generated catch block
          s_logger.debug("problem accessing Tenor field {}", field);
        }
      }
    }
    return tenors;
  }
  
}
