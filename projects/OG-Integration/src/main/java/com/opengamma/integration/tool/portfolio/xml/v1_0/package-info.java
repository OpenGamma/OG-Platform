@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class, type = LocalDate.class)})
package com.opengamma.integration.tool.portfolio.xml.v1_0;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.threeten.bp.LocalDate;