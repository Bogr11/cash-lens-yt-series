# Test fixtures

`CarrierExtractionTest` runs against the real model and needs two files here:

- `receipt.jpg` — a photo of a real receipt
- `voice.m4a` — a voice note saying what you spent

Each test skips itself when its file is missing, so the suite stays green without them.

Both end up in a public repo — pick a receipt with no card number, address or name
on it, and say nothing personal in the voice note.
