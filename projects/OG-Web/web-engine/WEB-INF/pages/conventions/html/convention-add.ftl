<#escape x as x?html>
<@page title="Add convention" jquery=true aceXmlEditor=true>


<#-- SECTION Add convention -->
<@section title="Add convention">
  <@form method="POST" action="${uris.conventions()}" id="addForm">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${name}" /></@rowin>
    
    <#if err_typeMissing??><div class="err">The type must be entered</div></#if>
    <#if err_typeInvalid??><div class="err">The type is invalid</div></#if>
    <@rowin label="Type">
      <select name="type">
        <option value="" <#if type = ''>selected</#if>></option>
        <#list conventionDescriptionMap?keys as key><option value="${key}"<#if type = '${key}'> selected</#if>>${conventionDescriptionMap[key]}</option></#list>
      </select>
    </@rowin>
    
    <#if err_xmlMissing??><div class="err">The data must be entered</div></#if>
    <#if err_conventionXmlMsg?has_content><div class="err">${err_conventionXmlMsg}</div></#if>
    <@rowin>
      <div id="ace-xml-editor">${conventionXml}</div>
    </@rowin>
    <input type="hidden" name="conventionxml" id="convention-xml"/>
    <@rowin><input type="submit" value="Add" /></@rowin>
<script type="text/javascript">
var editor = ace.edit("ace-xml-editor")
editor.getSession().setMode('ace/mode/xml')
$("#ace-xml-editor").show()

$("#addForm").submit( function(eventObj) {
  $("#convention-xml").val(editor.getSession().getValue())
  return true
})
</script>
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
