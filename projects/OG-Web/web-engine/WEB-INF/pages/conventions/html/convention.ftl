<#escape x as x?html>
<@page title="Convention - ${conventionDoc.name}" jquery=true aceXmlEditor=true>

<@section css="info" if=deleted>
  <p>This convention has been deleted</p>
</@section>


<#-- SECTION Convention output -->
<@section title="Convention">
  <p>
    <@rowout label="Name">${conventionDoc.name}</@rowout>
    <@rowout label="Type">${conventionDescription}</@rowout>
    <@rowout label="Reference">${conventionDoc.uniqueId.value}, version ${conventionDoc.uniqueId.version}, <a href="${uris.conventionVersions()}">view history</a>
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail" if=deleted>
    <@rowout label="Data"><textarea readonly style="width:650px;height:300px;">${conventionXml}</textarea></@rowout>
</@subsection>
</@section>


<#-- SECTION Update convention -->
<@section title="Update convention" if=!deleted>
  <@form method="PUT" action="${uris.convention()}" id="updateForm">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${conventionDoc.name}" /></@rowin>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="conventionxml" id="convention-xml"/>
    <@rowin><input type="submit" value="Update" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="updateForm" inputId="convention-xml" xmlValue="${conventionXml}"></@xmlEditorScript></#noescape>
  </p>
  </@form>
</@section>


<#-- SECTION Delete convention -->
<@section title="Delete convention" if=!deleted>
  <@form method="DELETE" action="${uris.convention()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.conventionVersions()}">History of this convention</a><br />
    <a href="${uris.conventions()}">Convention home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
