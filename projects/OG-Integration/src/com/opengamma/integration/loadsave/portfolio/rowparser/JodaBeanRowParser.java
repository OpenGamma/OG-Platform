/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.loadsave.portfolio.rowparser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyReadWrite;
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
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.ArgumentChecker;

/**
 * A generic row parser for Joda beans that automatically identifies fields to be persisted to rows/populated from rows
 */
public class JodaBeanRowParser extends RowParser {

  private static final Logger s_logger = LoggerFactory.getLogger(JodaBeanRowParser.class);

  /**
   * Types of swap leg that might be encountered, and for which additional fields are generated
   */
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
  
  /**
   * The packages where security classes are to be found
   */
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

  /**
   * Security properties to ignore when scanning
   */
  private static final String[] IGNORE_METAPROPERTIES = {
    "attributes",
    "uniqueid",
    "objectid",
    "securitylink",
    "trades",
    "attributes",
    "gicscode"
  };
  
  /**
   * Every security class name ends with this
   */
  private static final String CLASS_POSTFIX = "Security";

  /**
   *  The security class that this parser is adapted to
   */
  private Class<DirectBean> _securityClass;
  
  /**
   *  Map from column name to the field's Java type
   */
  private SortedMap<String, Class<?>> _columns = new TreeMap<String, Class<?>>();

  static {
    // Register the automatic string converters with Joda Beans
    JodaBeanConverters.getInstance();

    // Force registration of various meta beans that might not have been loaded yet
    ManageablePosition.meta();
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
  
  public JodaBeanRowParser(String securityName) throws OpenGammaRuntimeException {
    
    ArgumentChecker.notEmpty(securityName, "securityName");
    
    // Find the corresponding security class
    _securityClass = getClass(securityName + CLASS_POSTFIX);

    // Set column map
    _columns = recursiveGetColumnMap(_securityClass, "");
    _columns.putAll(recursiveGetColumnMap(ManageablePosition.class, "position:"));
    
    s_logger.info(securityName + " properties: " + _columns);

  }


  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Import routines: construct security(ies), position, trade
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public ManageableSecurity[] constructSecurity(Map<String, String> row) {
    
    ArgumentChecker.notNull(row, "row");
    
    ArrayList<ManageableSecurity> securities = new ArrayList<ManageableSecurity>();
    securities.add((ManageableSecurity) recursiveConstructBean(row, _securityClass, ""));
    return securities.toArray(new ManageableSecurity[securities.size()]);
  }
  
  @Override
  public ManageablePosition constructPosition(Map<String, String> row, ManageableSecurity security) {
    
    ArgumentChecker.notNull(row, "row");
    ArgumentChecker.notNull(security, "security");
    
    ManageablePosition result = (ManageablePosition) recursiveConstructBean(row, ManageablePosition.class, "position:");
    result.setSecurityLink(new ManageableSecurityLink(security.getExternalIdBundle()));
    return result;
  }

  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Export routines: construct row from security, position, trade
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   
  @Override
  public Map<String, String> constructRow(ManageableSecurity security) {
    ArgumentChecker.notNull(security, "security");
    return recursiveConstructRow(security, "");
  }
  
  @Override
  public Map<String, String> constructRow(ManageablePosition position) {
    ArgumentChecker.notNull(position, "position");
    return recursiveConstructRow(position, "position:");
  }

  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Utility routines
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public String[] getColumns() {
    return _columns.keySet().toArray(new String[_columns.size()]);
  }

  @Override
  public int getSecurityHashCode() {
    HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
    for (Entry<String, Class<?>> entry : _columns.entrySet()) {      
      hashCodeBuilder.append(entry.getKey());
      hashCodeBuilder.append(entry.getValue().getCanonicalName());
    }
    return hashCodeBuilder.toHashCode();
  }

  /**
   * Extract a map of column (field) names and types from the properties of the specified direct bean class.
   * Appropriate member classes (such as swap legs) are recursively traversed and their columns also extracted 
   * and added to the map.
   * @param clazz   The bean type from which to extract properties
   * @param prefix  The class membership path traced from the top-level bean class to the current class
   * @return        A map of the column names and their types
   */
  @SuppressWarnings("unchecked")
  private SortedMap<String, Class<?>> recursiveGetColumnMap(Class<?> clazz, String prefix) {
 
    // Scan through and capture the list of relevant properties and their types
    SortedMap<String, Class<?>> columns = new TreeMap<String, Class<?>>();
    
    for (MetaProperty<?> metaProperty : JodaBeanUtils.metaBean(clazz).metaPropertyIterable()) {
        
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
  
  /**
   * Build a bean of the specified type by extracting property values from the supplied map of field names to 
   * values, using recursion to construct the member beans in the same manner. 
   * @param row     The map from property (or column, or field) names to values
   * @param clazz   The bean type of which to construct an instance
   * @param prefix  The class membership path traced from the top-level bean class to the current class
   * @return        The constructed security bean
   */
  private DirectBean recursiveConstructBean(Map<String, String> row, Class<?> clazz, String prefix) {
    try {
      // Get a reference to the meta-bean
      Method metaMethod = clazz.getMethod("meta", (Class<?>[]) null);
      DirectMetaBean metaBean = (DirectMetaBean) metaMethod.invoke(null, (Object[]) null);

      // Get a new builder from the meta-bean
      @SuppressWarnings("unchecked")
      BeanBuilder<? extends DirectBean> builder = (BeanBuilder<? extends DirectBean>) metaBean.builder();

      // Populate the bean from the supplied row using the builder
      for (MetaProperty<?> metaProperty : JodaBeanUtils.metaBean(clazz).metaPropertyIterable()) {

        // Skip any undesired properties, process the rest
        if (!ignoreMetaProperty(metaProperty)) {

          // If this property is itself a bean without a converter, recurse to populate relevant fields
          if (isBean(metaProperty.propertyType()) && !isConvertible(metaProperty.propertyType())) {
  
            // Get the actual type of this bean from the relevant column
            String className = row.get((prefix + metaProperty.name()).trim().toLowerCase());
            Class<DirectBean> beanClass = getClass(className);
            
            // Recursively set properties
            builder.set(metaProperty.name(),
                recursiveConstructBean(row, beanClass, prefix + metaProperty.name() + ":"));
  
          // If not a bean, or it is a bean for which a converter exists, just set value in builder using joda convert
          } else {
            // Convert raw value in row to the target property's type
            String rawValue = row.get((prefix + metaProperty.name()).trim().toLowerCase());
            
            // Set property value
            if (rawValue != null && !rawValue.equals("")) {
              builder.set(metaProperty.name(), 
                  JodaBeanUtils.stringConverter().convertFromString(metaProperty.propertyType(), rawValue));
            } else {
              s_logger.warn("Skipping empty or null value for " + prefix + metaProperty.name());
            }
          }
        }
      }
      
      // Actually build the bean
      return builder.build();
  
    } catch (Throwable ex) {
      throw new OpenGammaRuntimeException("Could not create a " + clazz.getSimpleName() + ": " + ex.getMessage());
    }
  }
  
  /**
   * Extracts a map of column names to values from a supplied security bean's properties, using recursion to 
   * extract properties from any member beans. 
   * @param bean    The bean instance from which to extract property values
   * @param prefix  The class membership path traced from the top-level bean class to the current class
   * @return        A map of extracted column names and values
   */
  private Map<String, String> recursiveConstructRow(DirectBean bean, String prefix) {
    Map<String, String> result = new HashMap<String, String>();
    
    // Populate the row from the bean's properties
    for (MetaProperty<?> metaProperty : bean.metaBean().metaPropertyIterable()) {
      
      // Skip any undesired properties, process the rest
      if (!ignoreMetaProperty(metaProperty)) {
        // If this property is itself a bean without a converter, recurse to populate relevant columns
        if (isBean(metaProperty.propertyType()) && !isConvertible(metaProperty.propertyType())) {
          
          // Store the class name in a separate column (to help identify the correct subclass during loading)
          result.put(prefix + metaProperty.name(), metaProperty.get(bean).getClass().getSimpleName());
          
          // Recursively extract bean's columns        
          result.putAll(recursiveConstructRow((DirectBean) metaProperty.get(bean), prefix + metaProperty.name() + ":"));
          
        // If not a bean, or it is a bean for which a converter exists, just extract its value using joda convert
        } else {
          // Set the column
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
  
  /**
   * Given a class name, look for the class in the list of packages specified by CLASS_PACKAGES and return it
   * or throw exception if not found  
   * @param className   the class name to seek
   * @return            the corresponding class 
   */
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
  
  /**
   * Given a bean class, find its subclasses; this is current hard coded as Java can neither identify the 
   * classes within a package, nor identify a class's subclasses. Currently identifies swap legs.
   * @param beanClass
   * @return
   */
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
  
  /**
   * Checks whether the supplied class has a registered Joda string converter
   * @param clazz   the class to check
   * @return        the answer
   */
  private boolean isConvertible(Class<?> clazz) {
    try {
      JodaBeanUtils.stringConverter().findConverter(clazz);
      return true;
    } catch (Throwable ex) {
      return false;
    }
  }
  
  /**
   * Determines whether the supplied class is a direct bean
   * @param clazz the class in question
   * @return      the answer
   */
  private boolean isBean(Class<?> clazz) {
    return DirectBean.class.isAssignableFrom(clazz) ? true : false; 
  }

  /**
   * Checks whether the specified metaproperty is to be ignored when extracting fields
   * @param mp  the metaproperty in question
   * @return    the answer
   */
  private boolean ignoreMetaProperty(MetaProperty<?> mp) {
    if (mp.readWrite() != PropertyReadWrite.READ_WRITE) {
      return true;
    }
    String s = mp.name().trim().toLowerCase(); 
    for (String t : IGNORE_METAPROPERTIES) {
      if (s.equals(t.trim().toLowerCase())) {
        return true;
      }
    }
    return false;
  }
  
}
