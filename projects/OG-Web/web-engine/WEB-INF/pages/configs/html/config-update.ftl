<#escape x as x?html>
<@page title="Update - ${configDoc.name}">


<#-- SECTION Update configuration -->
<@section title="Update configuration">
  <@form method="PUT" action="${uris.config()}" id="updateConfigForm">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${configDoc.name}" /></@rowin>
    <@rowin label="Configuration (XML)">
      <div id="config-xml-editor">${configXML}</div>
    </@rowin>
    <@rowin><input type="hidden" name="configXML" id="config-xml"/></@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
<script type="text/javascript">
var editor = ace.edit("config-xml-editor")
editor.getSession().setMode('ace/mode/xml')
$("#config-xml-editor").show()


$("#updateConfigForm").submit( function(eventObj) {
  $("#config-xml").val(editor.getSession().getValue())
  return true
})
</script>
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
