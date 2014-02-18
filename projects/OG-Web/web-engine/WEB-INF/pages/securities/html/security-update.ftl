<#escape x as x?html>
<@page title="Update - ${security.name}" jquery=true aceXmlEditor=true>


<#-- SECTION Update security -->
<@section title="Update security">
  <@form method="PUT" action="${uris.security()}" id="updateSecurityForm">
  <p>
    <#if err_securityXml??>
      <div class="err">${err_securityXmlMsg}</div>
    </#if>
      <@rowin><div id="ace-xml-editor"></div></@rowin>
      <@rowin><input type="hidden" name="securityXml" id="security-xml"/></@rowin>
    <input type="hidden" name="type" value="xml"/>
    <@rowin><input type="submit" value="Update" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="updateSecurityForm" inputId="security-xml" xmlValue="${securityXml}"></@xmlEditorScript></#noescape>
  </p>
  </@form>
</@section>

<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.security()}">Security - ${security.name}</a><br />
    <a href="${uris.securities()}">Security search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
