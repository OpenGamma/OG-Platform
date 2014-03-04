<#escape x as x?html>
<@page title="Update position - ${position.name}" jquery=true aceXmlEditor=true>

<#-- SECTION Position output -->
<@section title="Portfolio node">
  <p>
    <@rowout label="Name">${position.name}</@rowout>
    <@rowout label="Reference">${position.uniqueId.value}, version ${position.uniqueId.version}</@rowout>
  </p>
</@section>
<#-- SECTION Update position -->
<@section title="Update position">
  <@form method="PUT" action="${uris.position()}" id="updatePositionForm">
  <p>
    <#if err_xmlMissing??><div class="err">The position XML must be entered</div></#if>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="positionXml" id="position-xml"/>
    <@rowin><input type="submit" value="Update" /></@rowin>
<#noescape><@xmlEditorScript formId="updatePositionForm" inputId="position-xml" xmlValue="${positionXml}"></@xmlEditorScript></#noescape>
  </p>
  </@form>
</@section>

<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.position()}">Position - ${position.name}</a><br />
    <a href="${uris.positions()}">Position search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
