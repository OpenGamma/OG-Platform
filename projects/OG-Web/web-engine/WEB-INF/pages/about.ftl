<#escape x as x?html>
<@page title="Home">
<#-- SECTION Main options -->
<@section title="About">
  <p>
    Information about this OpenGamma instance.
  </p>
  <p>
    Basics
    <table>
      <tr><td>OpenGamma version</td><td>${about.openGammaVersion}</td></tr>
      <tr><td>OpenGamma build</td><td>${about.openGammaBuild}</td></tr>
      <tr><td>OpenGamma build ID</td><td>${about.openGammaBuildId}</td></tr>
      <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
      <tr><td>Time</td><td>${now.toInstant()} (UTC)</td></tr>
      <tr><td>Zoned time</td><td>${now}</td></tr>
      <tr><td>User name</td><td>${about.systemUtils.USER_NAME}</td></tr>
      <tr><td>Locale</td><td>${about.defaultLocale}</td></tr>
      <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
      <tr><td>Web base URI</td><td>${baseUri}</td></tr>
      <tr><td>Servlet spec</td><td>${about.servletContext.majorVersion}.${about.servletContext.minorVersion}</td></tr>
      <tr><td>Server info</td><td>${about.servletContext.serverInfo}</td></tr>
      <tr><td>Context path</td><td>${about.servletContext.contextPath}</td></tr>
      <tr><td>Web root</td><td>${about.servletContext.getRealPath('/')}</td></tr>
      <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
      <#--tr><td>Java spec name</td><td>${about.systemUtils.JAVA_SPECIFICATION_NAME}</td></tr-->
      <tr><td>Java spec version</td><td>${about.systemUtils.JAVA_SPECIFICATION_VERSION}</td></tr>
      <#--tr><td>Java spec vendor</td><td>${about.systemUtils.JAVA_SPECIFICATION_VENDOR}</td></tr-->
      <tr><td>Java version</td><td>${about.systemUtils.JAVA_VERSION}</td></tr>
      <#--tr><td>Java vendor</td><td>${about.systemUtils.JAVA_VENDOR}</td></tr-->
      <tr><td>JVM name</td><td>${about.systemUtils.JAVA_VM_NAME}</td></tr>
      <tr><td>JVM version</td><td>${about.systemUtils.JAVA_VM_VERSION}</td></tr>
      <tr><td>JVM vendor</td><td>${about.systemUtils.JAVA_VM_VENDOR}</td></tr>
      <#--tr><td>JVM info</td><td>${about.systemUtils.JAVA_VM_INFO}</td></tr-->
      <#--tr><td>Runtime name</td><td>${about.systemUtils.JAVA_RUNTIME_NAME}</td></tr-->
      <tr><td>Runtime version</td><td>${about.systemUtils.JAVA_RUNTIME_VERSION}</td></tr>
      <#--tr><td>JVM class version</td><td>${about.systemUtils.JAVA_CLASS_VERSION}</td></tr-->
      <tr><td>OS name</td><td>${about.systemUtils.OS_NAME}</td></tr>
      <tr><td>OS arch</td><td>${about.systemUtils.OS_ARCH}</td></tr>
      <#--tr><td>OS version</td><td>${about.systemUtils.OS_VERSION}</td></tr-->
      <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
      <tr><td>Java home</td><td>${about.systemUtils.JAVA_HOME}</td></tr>
      <tr><td>Working dir</td><td>${about.systemUtils.USER_DIR}</td></tr>
      <tr><td>IO tempdir</td><td>${about.systemUtils.JAVA_IO_TMPDIR}</td></tr>
      <tr><td>User home</td><td>${about.systemUtils.USER_HOME}</td></tr>
      <#--tr><td>Classpath</td><td>${about.systemUtils.JAVA_CLASS_PATH}</td></tr-->
      <#--tr><td>Library path</td><td>${about.systemUtils.JAVA_LIBRARY_PATH}</td></tr-->
      <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
      <tr><td>Uptime</td><td>${about.jvmUptime}</td></tr>
      <tr><td>Thread count</td><td>${about.threadJmx.threadCount} (peaked at ${about.threadJmx.peakThreadCount})</td></tr>
      <tr><td>Class loaded</td><td>${about.classLoadingJmx.loadedClassCount} (unloaded ${about.classLoadingJmx.unloadedClassCount})</td></tr>
      <tr><td>Heap memory</td><td>${about.memoryJmx.heapMemoryUsage}</td></tr>
      <tr><td>Non heap memory</td><td>${about.memoryJmx.nonHeapMemoryUsage}</td></tr>
    </table>
  </p>
  <p>
    Input arguments:
    <ul>
<#list about.jvmArguments as entry>
      <li>${entry}</li>
</#list>
    </ul>
  </p>
  <p>
    Classpath:
    <ul>
<#list about.classpath as entry>
      <li>
<#if entry.infoParsed>
      <b>${entry.artifactId} ${entry.version}</b> <#if entry.groupParsed>(${entry.groupId}) </#if>- <i>${entry.url}</i>
<#else>
      ${entry.url}
</#if>
      </li>
</#list>
    </ul>
  </p>
</@section>
<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.home()}">Return home</a><br />
<#if userSecurity.isPermitted('WebComponents:view')>
    <a href="${uris.components()}">View the components</a><br />
</#if>
  </p>
</@section>
<p>
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
</p>
</@page>
</#escape>
