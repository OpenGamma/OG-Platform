<#escape x as x?html>
{
    "templateData": {
      "name": "${holidayDoc.name}",
      "objectId":"${holidayDoc.uniqueId.objectId}",
      "versionId":"${holidayDoc.uniqueId.version}",
<#if deleted>
      "deleted":"${holidayDoc.versionToInstant}",
</#if>
      "type":"${holiday.type}"
    },
	"dates": { 
	<#list holidayDatesByYear as item>
		"${item.first.value?c}": [<#list item.second as date> "${date?replace("${item.first.value?c}-","")}" <#if date_has_next>,</#if></#list>] <#if item_has_next>,</#if>
	</#list>
	}
}
</#escape>