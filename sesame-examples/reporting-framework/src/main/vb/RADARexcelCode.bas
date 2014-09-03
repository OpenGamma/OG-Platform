Attribute VB_Name = "Module2"
Public Function getPvSqlCall(ByVal externalId As String) As String
    quo = """"
    getPvSqlCall = "SELECT * FROM test.TradeResults TradeResults WHERE (TradeResults.External_ID='" & externalId & "')"
    'MsgBox (getPvSqlCall)
    
End Function

Function myJoin(rng As Range, delim As String) As String
    Dim r As Range
    For Each r In rng
        If Len(Trim(r.Value)) Then
            myJoin = myJoin & delim & r.Value
        End If
    Next
    myJoin = Mid$(myJoin, Len(delim) + 1)
End Function

Public Function getTradeSqlCall(ByVal collectionName As String, ByVal externalId As String) As String
    quo = """"
    getTradeSqlCall = "SELECT * FROM test." & collectionName & " " & collectionName & " WHERE (" & collectionName & ".External_ID='" & externalId & "')"
    'MsgBox (SqlCall)
    
End Function

Sub Price()

filepath = "c:\Temp\securitydata.csv"

Open filepath For Output As #1

d = Range("D5").Text
e = Range("D5").Text
uid = Format(Now(), "yyMMddhhmmss")

tempID = "Pre" + d + uid

Range("D5") = tempID
'Cells(3, 6) = tempID
x = myJoin(Range("A4:AH4"), ",")
Z = myJoin(Range("A5:AH5"), ",")

Range("D5") = e

Print #1, x
Print #1, Z

Close #1

h = sql(tempID)

MsgBox (h)

End Sub

Sub Load()
Dim con, rst, RowCount

Set con = CreateObject("ADODB.Connection")
con.Open "DSN=radarDB;"
Set rst = CreateObject("ADODB.Recordset")

'collName = Range("B1").Value
collName = "FullSecurityDataCsv"
extId = Range("B2").Value

g = getTradeSqlCall(collName, extId)
rst.Open g, con
'MsgBox (rst!External_ID)
Dim nFieldIndex As Long
For nFieldIndex = 0 To rst.Fields.Count - 1
Cells(4, 1 + nFieldIndex).Value = rst.Fields(nFieldIndex).Name
Next

Sheets("Sheet1").Select
Range("A5").Select

ActiveCell.CopyFromRecordset rst


rst.Close

con.Close

End Sub

Public Function sql(ByVal tradeId As String)

d = tradeId

g = getPvSqlCall(d)

Set con5 = CreateObject("ADODB.Connection")
con5.Open "DSN=radarDB;"
'MsgBox (con4.isolationlevel)
Set prst5 = CreateObject("ADODB.Recordset")

prst5.Open g, con5

For i = 1 To 7
'MsgBox (g)
    If (prst5.EOF = True) Then
        Application.Wait (Now + TimeValue("00:00:10"))
        prst5.Close
        prst5.Open g, con5
    ElseIf (prst5.EOF = False) Then
        Z = prst5!Present_Value
        'MsgBox (Z)
        Exit For
    End If
    'MsgBox (i)
Next i
prst5.Close

con5.Close

sql = Z

End Function
Sub SubmitTrade()

filepath = "c:\Temp\securitydata.csv"

Open filepath For Output As #1

d = Range("D5").Text

x = myJoin(Range("A4:AH4"), ",")
Z = myJoin(Range("A5:AH5"), ",")
'MsgBox (Z)
Print #1, x
Print #1, Z

Close #1

'a = sql(Range("D5").Value)

'MsgBox (a)

End Sub
