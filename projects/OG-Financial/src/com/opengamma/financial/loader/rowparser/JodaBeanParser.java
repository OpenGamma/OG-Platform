/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader.rowparser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.VarianceSwapLeg;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;

/**
 * A generic row parser for Joda beans that automatically identifies fields to be persisted to rows/populated from rows
 */
public class JodaBeanParser extends RowParser {

  private static final Logger s_logger = LoggerFactory.getLogger(JodaBeanParser.class);

  private static final Class<?>[] SWAP_LEG_CLASSES = {
    SwapLeg.class,
    InterestRateLeg.class,
    FixedInterestRateLeg.class,
    FloatingInterestRateLeg.class,
    FloatingGearingIRLeg.class,
    FloatingSpreadIRLeg.class,
    VarianceSwapLeg.class,
    FixedVarianceSwapLeg.class,
    FloatingVarianceSwapLeg.class
  };
  
  private static final String[] CLASS_PACKAGES = {
    "com.opengamma.financial.security.bond",
    "com.opengamma.financial.security.capfloor",
    "com.opengamma.financial.security.cash",
    "com.opengamma.financial.security.equity",
    "com.opengamma.financial.security.fra",
    "com.opengamma.financial.security.future",
    "com.opengamma.financial.security.fx",
    "com.opengamma.financial.security.option",
    "com.opengamma.financial.security.swap",
  };

  private static final String[] IGNORE_METAPROPERTIES = {
    "attributes",
    "uniqueid"
  };
  
  private static final String CLASS_POSTFIX = "Security";

  // The security class that this parser is adapted to
  private Class<DirectBean> _securityClass;
  
  // Map from column name to the field's Java type
  private Map<String, Class<Object>> _columns = new HashMap<String, Class<Object>>();

  static {
    // Register the automatic string converters with Joda Beans
    JodaBeanConverters.getInstance();

    // Force registration of various meta beans that might not have been loaded yet
    Notional.meta();
    SwapLeg.meta();
    InterestRateLeg.meta();
    FixedInterestRateLeg.meta();
    FloatingInterestRateLeg.meta();
    FloatingGearingIRLeg.meta();
    FloatingSpreadIRLeg.meta();
    VarianceSwapLeg.meta();
    FixedVarianceSwapLeg.meta();
    FloatingVarianceSwapLeg.meta();
  }
  
  public JodaBeanParser(String securityName, ToolContext toolContext) throws OpenGammaRuntimeException {
    super(toolContext);

    
    // Find the corresponding security class
    _securityClass = getClass(securityName + CLASS_POSTFIX);

    // Set column map
    _columns = recursiveGetColumnMap(_securityClass, "");
    
    s_logger.info(securityName + " properties: " + _columns);

  }

  @SuppressWarnings("unchecked")
  private Map<String, Class<Object>> recursiveGetColumnMap(Class<DirectBean> clazz, String prefix) {
 
    // Scan through and capture the list of relevant properties and their types
    // TODO identify and traverse underlying securities/legs, ignore uniqueIds
    Map<String, Class<Object>> columns = new HashMap<String, Class<Object>>();
    
    for (MetaProperty<Object> metaProperty : JodaBeanUtils.metaBean(clazz).metaPropertyIterable()) {
      
//      // Traverse underlying security
//      if (metaProperty.name().toLowerCase().equals("underlyingid")) {
//        
//        // HOW THE FUCK DO YOU WORK OUT WHAT TYPE OF SECURITY THE UNDERLYING ID IS GOING TO REFER TO
//        // PROBABLY HARD CODED... :(
//      } else 
        
      // Skip any undesired properties, process the rest
      if (!ignoreMetaProperty(metaProperty)) {
        
        // Add a column for the property (used either for the actual value
        // or for the class name in the case of a non-convertible bean
        columns.put(prefix + metaProperty.name(), metaProperty.propertyType());

        // If this is a bean without a converter recursively extract all 
        // columns for the metabean and all its subclasses
        if (isBean(metaProperty.propertyType()) && !isConvertible(metaProperty.propertyType())) {
          
          // This is the bean (might be an abstract class/subclassed)
          Class<DirectBean> beanClass = (Class<DirectBean>) metaProperty.propertyType().asSubclass(DirectBean.class);
          
          // Recursively extract this bean's properties
          columns.putAll(recursiveGetColumnMap(beanClass, prefix + metaProperty.name() + ":"));

          // Identify ALL subclasses of this bean and extract all their properties
          for (Class<?> subClass : getSubClasses(beanClass)) {
            columns.putAll(recursiveGetColumnMap((Class<DirectBean>) subClass, prefix + metaProperty.name() + ":"));            
          }
        }
      }
    }
    return columns;
  }
  
  
  /*
   * Import routines: construct security(ies), position, trade
   */
  
  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> row) {
    ArrayList<ManageableSecurity> securities = new ArrayList<ManageableSecurity>();    
    securities.add((ManageableSecurity) recursiveConstructSecurity(row, _securityClass, ""));
    return securities.toArray(new ManageableSecurity[securities.size()]);
  }
  
  private DirectBean recursiveConstructSecurity(Map<String, String> row, Class<DirectBean> clazz, String prefix) {
    try {
      // Get a reference to the meta-bean
      Method metaMethod = clazz.getMethod("meta", (Class<?>[]) null);
      DirectMetaBean metaBean = (DirectMetaBean) metaMethod.invoke(null, (Object[]) null);

      // Get a new builder from the meta-bean
      @SuppressWarnings("unchecked")
      BeanBuilder<? extends DirectBean> builder = (BeanBuilder<? extends DirectBean>) metaBean.builder();

      // Populate the bean from the supplied row using the builder
      for (MetaProperty<Object> metaProperty : JodaBeanUtils.metaBean(clazz).metaPropertyIterable()) {

        // If this property is itself a bean without a converter, recurse to populate relevant fields
        if (isBean(metaProperty.propertyType()) && !isConvertible(metaProperty.propertyType())) {

          // Get the actual type of this bean from the relevant column
          String className = row.get((prefix + metaProperty.name()).trim().toLowerCase());
          Class<DirectBean> beanClass = getClass(className);
          
          // Recursively set properties
          builder.set(metaProperty.name(),
              recursiveConstructSecurity(row, beanClass, prefix + metaProperty.name() + ":"));

        // If not a bean, or it is a bean for which a converter exists, just set value in builder using joda convert
        } else {
          // Convert raw value in row to the target property's type
          String rawValue = row.get((prefix + metaProperty.name()).trim().toLowerCase());
          
          // Set property value
          if (!ignoreMetaProperty(metaProperty)) {
            if (rawValue != null && !rawValue.equals("")) {
              // builder.setString(metaProperty.name(), rawValue);
              builder.set(metaProperty.name(), 
                  JodaBeanUtils.stringConverter().convertFromString(metaProperty.propertyType(), rawValue));
            } else {
              s_logger.warn("Skipping empty or null value for " + prefix + metaProperty.name());
            }
          }
        }    
      }
      
      // Actually build the security
      return builder.build();
  
    } catch (Throwable ex) {
      throw new OpenGammaRuntimeException("Could not create a " + clazz.getSimpleName() + ": " + ex.getMessage());
    }
  }
  
  @Override
  public ManageablePosition constructPosition(Map<String, String> row, ManageableSecurity security) {
    
    // TODO work out how to determine if fungible
    ManageablePosition position = new ManageablePosition();
    return position;
  }
  

  @Override
  public ManageableTrade constructTrade(Map<String, String> row, ManageableSecurity security, ManageablePosition position) {
    ManageableTrade trade = new ManageableTrade();
    return trade;
  }

  
  /*
   * Export routines: construct row from security, position, trade
   */
  
  @Override
  public Map<String, String> constructRow(ManageableSecurity security) {
    return recursiveConstructRow(security, "");
  }
  
  private Map<String, String> recursiveConstructRow(DirectBean bean, String prefix) {
    Map<String, String> result = new HashMap<String, String>();
    
    // Populate the row from the bean's properties
    for (MetaProperty<Object> metaProperty : bean.metaBean().metaPropertyIterable()) {
      
      // If this property is itself a bean without a converter, recurse to populate relevant columns
      if (isBean(metaProperty.propertyType()) && !isConvertible(metaProperty.propertyType())) {
        
        // Store the class name in a separate column (to help identify the correct subclass during loading)
        result.put(prefix + metaProperty.name(), metaProperty.get(bean).getClass().getSimpleName());
        
        // Recursively extract bean's columns        
        result.putAll(recursiveConstructRow((DirectBean) metaProperty.get(bean), prefix + metaProperty.name() + ":"));
        
      // If not a bean, or it is a bean for which a converter exists, just extract its value using joda convert
      } else {
        // Set the column
        if (!ignoreMetaProperty(metaProperty)) {       
          if (_columns.containsKey(prefix + metaProperty.name())) {
            result.put(prefix + metaProperty.name(), metaProperty.getString(bean));
          } else {
            s_logger.warn("No matching column found for property " + prefix + metaProperty.name());
          }
        }
      }
    }
    return result;
  }
  
  @Override
  public Map<String, String> constructRow(ManageablePosition position) {
    Map<String, String> result = new HashMap<String, String>();

    // TODO extract standard position fields
    return result;
  }
  

  @Override
  public Map<String, String> constructRow(ManageableTrade trade) {
    Map<String, String> result = new HashMap<String, String>();

    // TODO extract standard trade fields
    return result;
  }

  
  @SuppressWarnings("unchecked")
  private Class<DirectBean> getClass(String className) {
    Class<DirectBean> theClass = null;
    for (String prefix : CLASS_PACKAGES) {
      try {
        String fullName = prefix + "." + className;
        theClass = (Class<DirectBean>) Class.forName(fullName);
        break;
      } catch (Throwable ex) { }
    }
    if (theClass == null) {
      throw new OpenGammaRuntimeException("Could not load class " + className);
    }
    return theClass;
  }
  
  private Collection<Class<?>> getSubClasses(Class<?> beanClass) {
    Collection<Class<?>> subClasses = new ArrayList<Class<?>>();
    
    // This has to be hard-coded since Java can neither identify the classes within a package, nor identify a class's subclasses
    if (SwapLeg.class.isAssignableFrom(beanClass)) {
      for (Class<?> c : SWAP_LEG_CLASSES) {
        subClasses.add(c);
      }
    }  
    return (Collection<Class<?>>) subClasses;
  }
  
  private boolean isConvertible(Class<Object> clazz) {
    try {
      JodaBeanUtils.stringConverter().findConverter(clazz);
      return true;
    } catch (Throwable ex) {
      return false;
    }
  }
  
  private boolean isBean(Class<Object> clazz) {
    return DirectBean.class.isAssignableFrom(clazz) ? true : false; 
  }

  private boolean ignoreMetaProperty(MetaProperty<Object> mp) {
    String s = mp.name().trim().toLowerCase(); 
    for (String t : IGNORE_METAPROPERTIES) {
      if (s.equals(t)) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public String[] getColumns() {
    return _columns.keySet().toArray(new String[_columns.size()]);
  }

}
