'**************************************************************************
' ELEMENTS DE CONNEXION POUR L'API HEVA
'**************************************************************************

Private Declare PtrSafe Sub GetSystemTime Lib "Kernel32" (ByRef lpSystemTime As SYSTEMTIME)

Private Type SYSTEMTIME
  wYear As Integer
  wMonth As Integer
  wDayOfWeek As Integer
  wDay As Integer
  wHour As Integer
  wMinute As Integer
  wSecond As Integer
  wMilliseconds As Integer
End Type

Private Function getNonce(i As Integer) As String
    Dim chars As String, j As Integer
    
    getNonce = ""
    chars = "123456789abcdef"
    Randomize
    For j = 1 To i
        getNonce = getNonce & Mid(chars, Int(15 * Rnd) + 1, 1)
    Next j
End Function

Public Function SHA1Base64(ByVal sText As String)
    Dim asc As Object, enc As Object, bText() As Byte, bytes() As Byte
    
    Set asc = CreateObject("System.Text.UTF8Encoding")
    Set enc = CreateObject("System.Security.Cryptography.SHA1CryptoServiceProvider")
    
    bText = asc.Getbytes_4(sText)
    bytes = enc.ComputeHash_2(bText)
    SHA1Base64 = EncodeBase64(bytes)
    
    Set asc = Nothing
    Set enc = Nothing

End Function

Private Function EncodeBase64(ByRef arrData() As Byte) As String

    Dim objXML As MSXML2.DOMDocument
    Dim objNode As MSXML2.IXMLDOMElement

    Set objXML = New MSXML2.DOMDocument
    Set objNode = objXML.createElement("b64")
    
    objNode.DataType = "bin.base64"
    objNode.nodeTypedValue = arrData
    EncodeBase64 = objNode.Text

    Set objNode = Nothing
    Set objXML = Nothing

End Function

Private Function getUTCdate() As Date
    Dim nowUtc As SYSTEMTIME
    Call GetSystemTime(nowUtc)
    With nowUtc
        getUTCdate = DateSerial(.wYear, .wMonth, .wDay) + _
                TimeSerial(.wHour, .wMinute, .wSecond)
    End With
End Function

Private Function getWSSEHeader(sUser As String, sPwd As String) As String
    
    Dim s As String, sNonce As String, sCreated As String
    
    sNonce = getNonce(32)
    sCreated = Format(getUTCdate(), "yyyy-mm-dd""T""hh:mm:ss""Z""")
    
    s = "UsernameToken Username=""" & sUser & """"
    s = s & ", PasswordDigest=""" & SHA1Base64(sNonce + sCreated + sPwd) & """"
    s = s & ", Nonce=""" & sNonce & """"
    s = s & ", Created=""" & sCreated & """"
    getWSSEHeader = s
    
End Function

Function getHEVA(sUser As String, sPassword As String, sURL As String) As String
    Dim http As MSXML2.ServerXMLHTTP
    
    Set http = New MSXML2.ServerXMLHTTP
    With http
        .Open "GET", sURL, False
        .setTimeouts 5000, 5000, 5000, 5000
        .setRequestHeader "Content-Type", "application/json"
        .setRequestHeader "Authorization", "WSSE profile=""UsernameToken"""
        .setRequestHeader "X-WSSE", getWSSEHeader(sUser, sPassword)
        .Send ""
        If (.Status <> 200) Then
            Debug.Print "statut = " & .Status
            Debug.Print getWSSEHeader(sUser, sPassword)
            getHEVA = ""
            MsgBox sURL & vbCrLf & vbCrLf & "statut de connexion : " & .Status, vbExclamation, "Problème de connexion"
        Else
            getHEVA = .responseText
        End If
    End With
    Set http = Nothing

End Function

Sub testHeva()
 Debug.Print (getHEVA("myWsseLogin", "myWssePassword", "http://api.licences.ffvv.stadline.com/persons"))
End Sub

