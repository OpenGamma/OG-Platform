<#escape x as x?html>
<@page title="Update - ${configDoc.name}" jquery=true aceXmlEditor=true>


<#-- SECTION Update configuration -->
<@section title="Update configuration">
  <@form method="PUT" action="${uris.config()}" id="updateConfigForm">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${configDoc.name}" /></@rowin>
    <#if err_xmlMissing??><div class="err">The configuration xml must be entered</div></#if>
    <@rowin label="Configuration (XML)">
      <div id="ace-xml-editor"></div>
    </@rowin>
    <@rowin><input type="hidden" name="configXML" id="config-xml"/></@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>

  <#noescape><@xmlEditorScript formId="updateConfigForm" inputId="config-xml" xmlValue="${configXML}"></@xmlEditorScript></#noescape>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.configs()}">Configuration home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
