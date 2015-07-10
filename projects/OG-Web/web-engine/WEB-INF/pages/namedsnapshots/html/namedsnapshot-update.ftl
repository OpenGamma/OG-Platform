<#escape x as x?html>
<@page title="Update - ${snapshotDoc.name}" jquery=true aceXmlEditor=true>


<#-- SECTION Update snapshot -->
<@section title="Update snapshot">
  <@form method="PUT" action="${uris.snapshot()}" id="updateForm">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${snapshotDoc.name}" /></@rowin>
    <#if err_snapshotXmlMsg?has_content><div class="err">${err_snapshotXmlMsg}</div></#if>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="snapshotxml" id="snapshot-xml"/>
    <@rowin><input type="submit" value="Update" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="updateForm" inputId="snapshot-xml" xmlValue="${snapshotXml}"></@xmlEditorScript></#noescape>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.snapshots()}">Snapshot home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
