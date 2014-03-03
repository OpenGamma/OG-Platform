<#escape x as x?html>
<@page title="Update - ${conventionDoc.name}" jquery=true aceXmlEditor=true>


<#-- SECTION Update convention -->
<@section title="Update convention">
  <@form method="PUT" action="${uris.convention()}" id="updateForm">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${conventionDoc.name}" /></@rowin>
    <#if err_conventionXmlMsg?has_content><div class="err">${err_conventionXmlMsg}</div></#if>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="conventionxml" id="convention-xml"/>
    <@rowin><input type="submit" value="Update" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="updateForm" inputId="convention-xml" xmlValue="${conventionXml}"></@xmlEditorScript></#noescape>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.conventions()}">Convention home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
