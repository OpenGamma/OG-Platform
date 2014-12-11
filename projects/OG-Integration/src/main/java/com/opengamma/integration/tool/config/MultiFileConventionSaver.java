/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.joda.beans.ser.xml.JodaBeanXmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.convention.ConventionType;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionMetaDataRequest;
import com.opengamma.master.convention.ConventionMetaDataResult;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.JodaBeanSerialization;

/**
 * Class that will save the entire current contents of a configuration store
 * to a set of XML encoded files on disk.
 * This differs from {@link ConfigSaver} primarily in that it saves all items
 * as individual files.
 */
@BeanDefinition
public class MultiFileConventionSaver extends DirectBean {
  private static final Logger s_logger = LoggerFactory.getLogger(MultiFileConventionSaver.class);
  private static final boolean PRETTY = true;
  @PropertyDefinition
  private File _zipFileName;
  @PropertyDefinition
  private ConventionMaster _conventionMaster;
  
  public void setZipFileName(String directory) {
    setZipFileName(new File(directory));
  }
  
  public void run() throws IOException {
    ArgumentChecker.notNullInjected(getZipFileName(), "destinationZip");
    ArgumentChecker.notNullInjected(getConventionMaster(), "conventionMaster");
    
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(getZipFileName()));
    ConventionMetaDataRequest request = new ConventionMetaDataRequest();
    ConventionMetaDataResult result = getConventionMaster().metaData(request);
    
    for (ConventionType conventionType : result.getConventionTypes()) {
      outputFilesForConventionType(conventionType, out);
    }
    out.close();
  }
  
  protected void outputFilesForConventionType(ConventionType conventionType, ZipOutputStream out) throws IOException {
    s_logger.info("Outputting files for {}", conventionType);
    ConventionSearchRequest searchRequest = new ConventionSearchRequest();
    searchRequest.setConventionType(conventionType);
    ConventionSearchResult searchResult = getConventionMaster().search(searchRequest);
    Set<ConventionDocument> latest = new HashSet<>();
    for (ConventionDocument document : searchResult.getDocuments()) {
      latest.add(getConventionMaster().get(document.getObjectId(), VersionCorrection.LATEST));
    }

    Set<String> fileNames = new HashSet<>();
    for (ConventionDocument document : latest) {
      ManageableConvention convention = document.getConvention();
      String fileName = escapeFileName(convention.getName());
      if (!convention.getName().equals(document.getName())) {
        s_logger.warn("Convention document {} contains convention with differing name {}", document.getName(), convention.getName());
      }
      if (fileNames.contains(fileName)) {
        int count = 1;
        String duplicateFileName;
        do {
          duplicateFileName = fileName + " (" + count + ")";
        } while (fileNames.contains(duplicateFileName));
        s_logger.warn("Found duplicate convention {}, exporting as {}", fileName, duplicateFileName);
        fileName = duplicateFileName;
      }
      fileNames.add(fileName);
      ZipEntry entry = new ZipEntry(conventionType.getName() + "/" + fileName + ".xml");
      out.putNextEntry(entry);
      JodaBeanXmlWriter xmlWriter = JodaBeanSerialization.serializer(PRETTY).xmlWriter();
      StringBuilder sb = xmlWriter.writeToBuilder(convention, PRETTY);
      out.write(sb.toString().getBytes(Charset.forName("UTF-8")));
      out.closeEntry();
    }
  }
  
  private String escapeFileName(String name) {
    String escapedForwardSlashes = name.replaceAll("/", " SLASH ");
    return escapedForwardSlashes.replaceAll("~", " TILDE ").replaceAll("\\\\", " BACKSLASH ");
  }
  

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MultiFileConventionSaver}.
   * @return the meta-bean, not null
   */
  public static MultiFileConventionSaver.Meta meta() {
    return MultiFileConventionSaver.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MultiFileConventionSaver.Meta.INSTANCE);
  }

  @Override
  public MultiFileConventionSaver.Meta metaBean() {
    return MultiFileConventionSaver.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the zipFileName.
   * @return the value of the property
   */
  public File getZipFileName() {
    return _zipFileName;
  }

  /**
   * Sets the zipFileName.
   * @param zipFileName  the new value of the property
   */
  public void setZipFileName(File zipFileName) {
    this._zipFileName = zipFileName;
  }

  /**
   * Gets the the {@code zipFileName} property.
   * @return the property, not null
   */
  public final Property<File> zipFileName() {
    return metaBean().zipFileName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the conventionMaster.
   * @return the value of the property
   */
  public ConventionMaster getConventionMaster() {
    return _conventionMaster;
  }

  /**
   * Sets the conventionMaster.
   * @param conventionMaster  the new value of the property
   */
  public void setConventionMaster(ConventionMaster conventionMaster) {
    this._conventionMaster = conventionMaster;
  }

  /**
   * Gets the the {@code conventionMaster} property.
   * @return the property, not null
   */
  public final Property<ConventionMaster> conventionMaster() {
    return metaBean().conventionMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public MultiFileConventionSaver clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MultiFileConventionSaver other = (MultiFileConventionSaver) obj;
      return JodaBeanUtils.equal(getZipFileName(), other.getZipFileName()) &&
          JodaBeanUtils.equal(getConventionMaster(), other.getConventionMaster());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getZipFileName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConventionMaster());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("MultiFileConventionSaver{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("zipFileName").append('=').append(JodaBeanUtils.toString(getZipFileName())).append(',').append(' ');
    buf.append("conventionMaster").append('=').append(JodaBeanUtils.toString(getConventionMaster())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MultiFileConventionSaver}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code zipFileName} property.
     */
    private final MetaProperty<File> _zipFileName = DirectMetaProperty.ofReadWrite(
        this, "zipFileName", MultiFileConventionSaver.class, File.class);
    /**
     * The meta-property for the {@code conventionMaster} property.
     */
    private final MetaProperty<ConventionMaster> _conventionMaster = DirectMetaProperty.ofReadWrite(
        this, "conventionMaster", MultiFileConventionSaver.class, ConventionMaster.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "zipFileName",
        "conventionMaster");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -586330008:  // zipFileName
          return _zipFileName;
        case 41113907:  // conventionMaster
          return _conventionMaster;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends MultiFileConventionSaver> builder() {
      return new DirectBeanBuilder<MultiFileConventionSaver>(new MultiFileConventionSaver());
    }

    @Override
    public Class<? extends MultiFileConventionSaver> beanType() {
      return MultiFileConventionSaver.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code zipFileName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<File> zipFileName() {
      return _zipFileName;
    }

    /**
     * The meta-property for the {@code conventionMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConventionMaster> conventionMaster() {
      return _conventionMaster;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -586330008:  // zipFileName
          return ((MultiFileConventionSaver) bean).getZipFileName();
        case 41113907:  // conventionMaster
          return ((MultiFileConventionSaver) bean).getConventionMaster();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -586330008:  // zipFileName
          ((MultiFileConventionSaver) bean).setZipFileName((File) newValue);
          return;
        case 41113907:  // conventionMaster
          ((MultiFileConventionSaver) bean).setConventionMaster((ConventionMaster) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
