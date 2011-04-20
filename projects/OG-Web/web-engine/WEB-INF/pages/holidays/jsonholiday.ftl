<#escape x as x?html>
{
	"templateData": { "name": "${holidayDoc.name}", "type":"${holiday.type}"},
	"dates": { 
	<#list holidayDatesByYear as item>
		"${item.first.value?c}": [<#list item.second as date> "${date?replace("${item.first.value?c}-","")}" <#if date_has_next>,</#if></#list>] <#if item_has_next>,</#if>
	</#list>
	}
}
</#escape>