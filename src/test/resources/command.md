## Account A — text

```
curl.exe -i -X POST http://127.0.0.1:8080/inbound/save/text -H "Content-Type: application/json" -H "X-Account-Id: acc_a" -d "@src/test/resources/request.json" -w "`nHTTP %{http_code}`n"
```

## Account B — SAME externalId

```
curl.exe -i -X POST http://127.0.0.1:8080/inbound/save/text -H "Content-Type: application/json" -H "X-Account-Id: acc_b" -d "@src/test/resources/request.json" -w "`nHTTP %{http_code}`n"
```

## Status — each account asks for the same id

```
curl.exe -s http://127.0.0.1:8080/inbound/msg_006 -H "X-Account-Id: acc_a" | ConvertFrom-Json | Format-List
```

```
curl.exe -s http://127.0.0.1:8080/inbound/msg_006 -H "X-Account-Id: acc_b" | ConvertFrom-Json | Format-List
```

## Photo / voice

```
curl.exe -i -X POST http://127.0.0.1:8080/inbound/save/photo -H "X-Account-Id: acc_a" -F "externalId=rcpt_001" -F "file=@src/test/resources/receipt.jpg" -w "`nHTTP %{http_code}`n"
```

```
curl.exe -i -X POST http://127.0.0.1:8080/inbound/save/voice -H "X-Account-Id: acc_b" -F "externalId=voice_002" -F "file=@src/test/resources/voice.m4a" -w "`nHTTP %{http_code}`n"
```

## Read them back — each account sees only its own

```
curl.exe -s http://127.0.0.1:8080/expenses -H "X-Account-Id: acc_a" | ConvertFrom-Json | Format-Table amount, currency, category, merchant, confidence
```

```
curl.exe -s http://127.0.0.1:8080/expenses -H "X-Account-Id: demo_acc_sdfasdf+--009" | ConvertFrom-Json | Format-Table amount, currency, category, merchant, confidence
```

## No header at all

```
curl.exe -i -s http://127.0.0.1:8080/expenses -w "`nHTTP %{http_code}`n"
```
