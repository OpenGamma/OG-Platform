<#escape x as x?html>
<@page title="Configuration - ${configDoc.name}">

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
    <div id="config-xml-editor">${configXML}</div>
</@subsection>

<script type="text/javascript">
var editor = ace.edit("config-xml-editor")
editor.getSession().setMode('ace/mode/xml')
$("#config-xml-editor").show()
</script>

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
