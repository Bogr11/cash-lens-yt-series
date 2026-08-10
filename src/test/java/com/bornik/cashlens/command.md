## Text

```
curl.exe -i -X POST http://127.0.0.1:8080/inbound/save/text -H "Content-Type: application/json" -d "@src/test/resources/request.json" -w "`nHTTP %{http_code} - took %{time_total}s`n"
```

## Photo

```
curl.exe -i -X POST http://127.0.0.1:8080/inbound/save/photo -F "externalId=rcpt_001" -F "file=@src/test/resources/receipt.jpg" -w "`nHTTP %{http_code} - took %{time_total}s`n"
```

## Voice

```
curl.exe -i -X POST http://127.0.0.1:8080/inbound/save/voice -F "externalId=voice_001" -F "file=@src/test/resources/voice.m4a" -w "`nHTTP %{http_code} - took %{time_total}s`n"
```

## The bad receipt — watch the confidence

```
curl.exe -i -X POST http://127.0.0.1:8080/inbound/save/photo -F "externalId=rcpt_002" -F "file=@src/test/resources/bad_receipt.jpg" -w "`nHTTP %{http_code} - took %{time_total}s`n"
```

## Same id again — skipped

```
curl.exe -i -X POST http://127.0.0.1:8080/inbound/save/photo -F "externalId=rcpt_001" -F "file=@src/test/resources/receipt.jpg" -w "`nHTTP %{http_code} - took %{time_total}s`n"
```

## Read them back

```
curl.exe -s http://127.0.0.1:8080/expenses | ConvertFrom-Json | Format-Table amount, currency, category, merchant, occurredAt, confidence
```

Raw:

```
curl.exe -s http://127.0.0.1:8080/expenses
```
