# Demo commands

PowerShell. Run from this folder.

## Text

```
curl.exe -i -X POST http://127.0.0.1:8080/inbound/save/text -H "Content-Type: application/json" -d "@request.json" -w "`nHTTP %{http_code} - took %{time_total}s`n"
```

## Photo

`externalId` and `file` are separate parts. The image is stored in the inbound
message row and dropped once the message reaches PROCESSED — so keep the file
somewhere outside the repo and off camera if it has your card number on it.

```
curl.exe -i -X POST http://127.0.0.1:8080/inbound/save/photo -F "externalId=rcpt_001" -F "file=@C:/Users/borys/Desktop/receipt.jpg" -w "`nHTTP %{http_code} - took %{time_total}s`n"
```

Same id twice — second one is skipped, same as with text:

```
curl.exe -i -X POST http://127.0.0.1:8080/inbound/save/photo -F "externalId=rcpt_001" -F "file=@C:/Users/borys/Desktop/receipt.jpg" -w "`nHTTP %{http_code} - took %{time_total}s`n"
```

## Read them back

```
curl.exe -s http://127.0.0.1:8080/expenses | ConvertFrom-Json | Format-Table amount, currency, category, merchant, occurredAt, confidence
```

Raw, if the table hides something:

```
curl.exe -s http://127.0.0.1:8080/expenses
```
