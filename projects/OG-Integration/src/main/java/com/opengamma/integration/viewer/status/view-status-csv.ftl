<#assign colCount = viewStatus.getColumnCount() - 1>
<#assign hRowCount = viewStatus.getHeaderRowCount() -1>
   
<#list 0..hRowCount as hRowIndex>
  <#list 0..colCount as colIndex><#assign column = viewStatus.getColumnNameAt(hRowIndex, colIndex)>${column}<#if colIndex_has_next>,</#if></#list>
</#list>  
<#assign rowCount = viewStatus.getRowCount() -1>
<#list 0..rowCount as rowIndex>
  <#list 0..colCount as colIndex><#assign column = viewStatus.getRowValueAt(rowIndex, colIndex)>${column?string}<#if colIndex_has_next>,</#if></#list>
</#list>  
