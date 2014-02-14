<#escape x as x?html>
<@page title="Add configuration" jquery=true aceXmlEditor=true>


<#-- SECTION Add configuration -->
<@section title="Add configuration">
  <@form method="POST" action="${uris.configs()}" id="addConfigForm">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${name}" /></@rowin>
    
    <#if err_typeMissing??><div class="err">The type must be entered</div></#if>
    <#if err_typeInvalid??><div class="err">The type is invalid</div></#if>
    <@rowin label="Type">
      <select name="type">
        <option value="" <#if type = ''>selected</#if>></option>
        <#list configDescriptionMap?keys as key><option value="${key}"<#if type = '${key}'> selected</#if>>${configDescriptionMap[key]}</option></#list>
      </select>
    </@rowin>
    
    <#if err_xmlMissing??><div class="err">The data must be entered</div></#if>
    <@rowin label="Configuration (XML)">
      <div id="config-xml-editor">${configXML}</div>
    </@rowin>
    <@rowin><input type="hidden" name="configXML" id="config-xml"/></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
<script type="text/javascript">
var editor = ace.edit("config-xml-editor")
editor.getSession().setMode('ace/mode/xml')
$("#config-xml-editor").show()

$("#addConfigForm").submit( function(eventObj) {
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
