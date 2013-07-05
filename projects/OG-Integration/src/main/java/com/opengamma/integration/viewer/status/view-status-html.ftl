<!DOCTYPE html>
<html>
<head>
<title>View Status Report</title>
</head>

<body>

<table border="1">

  <#assign colCount = viewStatus.getColumnCount() - 1>
  <#assign hRowCount = viewStatus.getHeaderRowCount() -1>
   
  <#list 0..hRowCount as hRowIndex>
    <tr>
        <#list 0..colCount as colIndex>
          <#assign column = viewStatus.getColumnNameAt(hRowIndex, colIndex)>
          <th>${column}</th>
        </#list>
    </tr>
  </#list>  
  
  <#assign rowCount = viewStatus.getRowCount() -1>
  <#list 0..rowCount as rowIndex>
    <tr>
        <#list 0..colCount as colIndex>
          <#assign column = viewStatus.getRowValueAt(rowIndex, colIndex)>
          <td>${column?string}</td>
        </#list>
    </tr>
  </#list>  
</table>


</body>
</html>