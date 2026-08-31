# The prompt behind the UI

The frontend in `src/main/resources/static/` was generated, not hand written.
I am a backend developer and I do not do frontend, so instead of pretending
otherwise, here is what I asked for.

Three files came out of it: `index.html`, `app.js`, `styles.css`. Spring Boot
serves them from `/` on its own — a `static` folder under `resources` is the
whole configuration.

---

```
Build a single page frontend for an expense tracker. Plain HTML, CSS and
vanilla JavaScript — no framework, no build step, no dependencies. Three
files only: index.html, app.js, styles.css. It will be served as static
resources by Spring Boot from the same origin as the API, so no CORS
handling is needed.

THE API IT TALKS TO

Every request carries an account header:

    X-Account-Id: <string>

Endpoints:

    POST /inbound/save/text    {"externalId": "...", "payload": "..."}
    POST /inbound/save/photo   multipart: externalId, file
    POST /inbound/save/voice   multipart: externalId, file
    GET  /inbound/{externalId} -> { externalId, source, status,
                                    receivedAt, failureReason? }
    GET  /expenses             -> [ { id, amount, currency, category,
                                      description, merchant, confidence,
                                      createdDate } ]

The three POST endpoints answer 202 Accepted with a Location header
pointing at the status endpoint, and process the message in the
background. A repeated externalId answers 200 instead of 202 and does
no new work. Status goes RECEIVED -> PROCESSED or FAILED, and a failure
carries failureReason.

HOW SUBMITTING MUST BEHAVE

This is the part I care about most. The API is asynchronous on purpose,
so the page must not wait.

- Generate externalId on the client with crypto.randomUUID().
- On 202, read the Location header and follow it. Do not build that path
  yourself — the client should not know the id format or that the status
  endpoint lives under /inbound.
- Close the dialog immediately after the 202. Do not block the user.
- Show the submission as a pending row above the ledger, labelled with
  whatever the user gave: the text they typed, the file name, or
  "Voice note". Update it as the status changes.
- Poll the status URL once a second (first check sooner, around 400ms).
  Tolerate a 404 on the first checks — the row may not be visible yet.
- On PROCESSED, drop the pending row and reload the expense list.
- On FAILED, keep the row, show failureReason, and give it a dismiss
  button.
- Several submissions can be in flight at once, each polling its own
  status URL.
- On 200 instead of 202, say it was already captured and do not poll.

WHAT THE PAGE SHOWS

- Header: a wordmark, the current account id as a button that opens a
  dialog to change it (persist it in localStorage), and a refresh button.
- Summary: total for the selected period in the dominant currency,
  expense count, average per active day, and a count of expenses whose
  confidence is below 0.6.
- Filters: period (last 30 days / this month / all time), category,
  currency. Filtering happens on the client over the loaded list.
- Ledger: expenses grouped by day, newest first. Each row shows merchant
  or description, category, confidence as a percentage, and the amount.
- Two breakdowns beside the ledger: totals by category as horizontal
  bars, and totals by currency.
- Empty and loading states, and an error state that shows the message
  from the failed request.

ADDING AN EXPENSE

A dialog with three tabs:

- Text: a textarea. Placeholder should suggest natural language, like
  "Coffee and a croissant, 8.50 at Federal".
- Receipt: a file input for JPEG, PNG or WEBP, showing the chosen file
  name.
- Voice: a record button that uses MediaRecorder, with a running timer
  and a two minute cap. Pick the first supported mime type from
  audio/webm;codecs=opus, audio/ogg;codecs=opus, audio/mp4. Release the
  microphone when recording stops.

LOGGING

Log one line per step to the browser console, prefixed and timestamped
so it can be read next to the server log:

    [cashlens 14:32:05.120] POST → 202  /inbound/web_3f2a...
    [cashlens 14:32:05.523] check 1  +0.4s → RECEIVED
    [cashlens 14:32:11.560] check 7  +6.4s → PROCESSED

STYLE

Quiet and printed, not a dashboard. Warm paper background, near black
ink, a serif for numbers and headings, monospace for metadata and
labels. Generous whitespace, thin rules instead of boxes, no shadows,
no rounded cards, no colour except where it carries meaning. It should
read like a ledger, not like an admin panel.
```
