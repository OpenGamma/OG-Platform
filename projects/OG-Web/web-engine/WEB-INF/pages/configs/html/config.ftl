<#escape x as x?html>
<@page title="Configuration - ${configDoc.name}" jquery=true aceXmlEditor=true>

<@section css="info" if=deleted>
  <p>This configuration has been deleted</p>
</@section>


<#-- SECTION Configuration output -->
<@section title="Configuration">
  <p>
    <@rowout label="Name">${configDoc.name}</@rowout>
    <@rowout label="Type">${configDescription}</@rowout>
    <@rowout label="Reference">${configDoc.uniqueId.value}, version ${configDoc.uniqueId.version}, <a href="${uris.configVersions()}">view history</a>
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail" if=deleted>
    <@rowout label="Data"><textarea readonly style="width:650px;height:300px;">${configXml}</textarea></@rowout>
</@subsection>
</@section>


<#-- SECTION Update config -->
<@section title="Update configuration" if=!deleted>
  <@form method="PUT" action="${uris.config()}" id="updateConfigForm">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${configDoc.name}"/></@rowin>
    <@rowin label="Configuration (XML)">
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="configXML" id="config-xml"/>
    <@rowin><input type="submit" value="Update" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="updateConfigForm" inputId="config-xml" xmlValue="${configXML}"></@xmlEditorScript></#noescape>
  </p>
  </@form>
</@section>


<#-- SECTION Delete config -->
<@section title="Delete configuration" if=!deleted>
  <@form method="DELETE" action="${uris.config()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.configVersions()}">History of this config</a><br />
    <a href="${uris.configs()}">Configuration home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
