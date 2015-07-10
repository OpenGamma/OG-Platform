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
    <@rowout label="Reference">${configDoc.uniqueId.value}, version ${configDoc.uniqueId.version}
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
    <div id="ace-xml-editor"></div>
</@subsection>

<#noescape><@xmlEditorScript  xmlValue="${configXML}" readOnly=true></@xmlEditorScript></#noescape>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.configVersions()}">All versions</a><br />
    <a href="${uris.config()}">Latest version - ${latestConfigDoc.uniqueId.version}</a><br />
    <a href="${uris.configs()}">Configuration home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
