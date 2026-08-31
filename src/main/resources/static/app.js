const ACCOUNT_KEY = 'cashlens.accountId';
const ACCOUNT_HEADER = 'X-Account-Id';
const LOW_CONFIDENCE = 0.6;
const MAX_RECORDING_SECONDS = 120;

const state = {
    accountId: localStorage.getItem(ACCOUNT_KEY) || 'demo',
    expenses: [],
    pending: [],
    period: '30',
    category: '',
    currency: '',
    inputType: 'text',
    submitting: false,
    voiceFile: null,
    voiceChunks: [],
    mediaRecorder: null,
    mediaStream: null,
    recordingStartedAt: 0,
    recordingTimerId: null,
    discardRecording: false,
    requestingMicrophone: false
};

const elements = {
    pendingList: document.querySelector('#pendingList'),
    accountButton: document.querySelector('#accountButton'),
    accountLabel: document.querySelector('#accountLabel'),
    accountDialog: document.querySelector('#accountDialog'),
    accountForm: document.querySelector('#accountForm'),
    accountInput: document.querySelector('#accountInput'),
    addExpenseButton: document.querySelector('#addExpenseButton'),
    expenseDialog: document.querySelector('#expenseDialog'),
    expenseForm: document.querySelector('#expenseForm'),
    submitExpenseButton: document.querySelector('#submitExpenseButton'),
    expenseText: document.querySelector('#expenseText'),
    receiptFile: document.querySelector('#receiptFile'),
    receiptFileName: document.querySelector('#receiptFileName'),
    voiceRecorder: document.querySelector('#voiceRecorder'),
    recordButton: document.querySelector('#recordButton'),
    recordingLabel: document.querySelector('#recordingLabel'),
    recordingTimer: document.querySelector('#recordingTimer'),
    formStatus: document.querySelector('#formStatus'),
    refreshButton: document.querySelector('#refreshButton'),
    categoryFilter: document.querySelector('#categoryFilter'),
    currencyFilter: document.querySelector('#currencyFilter'),
    loadingState: document.querySelector('#loadingState'),
    emptyState: document.querySelector('#emptyState'),
    expenseList: document.querySelector('#expenseList'),
    categoryBreakdown: document.querySelector('#categoryBreakdown'),
    currencyBreakdown: document.querySelector('#currencyBreakdown'),
    grandTotal: document.querySelector('#grandTotal'),
    expenseCount: document.querySelector('#expenseCount'),
    statCount: document.querySelector('#statCount'),
    lowConfidence: document.querySelector('#lowConfidence'),
    perDay: document.querySelector('#perDay'),
    periodLabel: document.querySelector('#periodLabel')
};

document.addEventListener('DOMContentLoaded', initialize);

function initialize() {
    updateAccountLabel();
    bindEvents();
    loadExpenses();
}

function bindEvents() {
    elements.addExpenseButton.addEventListener('click', openExpenseDialog);
    document.querySelectorAll('[data-open-expense]').forEach(button => button.addEventListener('click', openExpenseDialog));
    document.querySelectorAll('[data-close-dialog]').forEach(button => button.addEventListener('click', () => elements.expenseDialog.close()));
    document.querySelectorAll('[data-close-account]').forEach(button => button.addEventListener('click', () => elements.accountDialog.close()));

    elements.accountButton.addEventListener('click', () => {
        elements.accountInput.value = state.accountId;
        elements.accountDialog.showModal();
        elements.accountInput.focus();
        elements.accountInput.select();
    });

    elements.accountForm.addEventListener('submit', event => {
        event.preventDefault();
        const nextAccount = elements.accountInput.value.trim();
        if (!nextAccount) return;
        state.accountId = nextAccount;
        localStorage.setItem(ACCOUNT_KEY, nextAccount);
        updateAccountLabel();
        elements.accountDialog.close();
        loadExpenses();
    });

    elements.refreshButton.addEventListener('click', loadExpenses);
    elements.expenseForm.addEventListener('submit', submitExpense);

    document.querySelectorAll('[data-period]').forEach(button => {
        button.addEventListener('click', () => {
            state.period = button.dataset.period;
            document.querySelectorAll('[data-period]').forEach(candidate => candidate.classList.toggle('active', candidate === button));
            render();
        });
    });

    elements.categoryFilter.addEventListener('change', () => {
        state.category = elements.categoryFilter.value;
        render();
    });

    elements.currencyFilter.addEventListener('change', () => {
        state.currency = elements.currencyFilter.value;
        render();
    });

    document.querySelectorAll('[data-input]').forEach(tab => {
        tab.addEventListener('click', () => setInputType(tab.dataset.input));
    });

    elements.receiptFile.addEventListener('change', () => showSelectedFile(elements.receiptFile, elements.receiptFileName));
    elements.recordButton.addEventListener('click', toggleVoiceRecording);
    elements.expenseDialog.addEventListener('close', resetVoiceRecorder);

    [elements.expenseDialog, elements.accountDialog].forEach(dialog => {
        dialog.addEventListener('click', event => {
            if (event.target === dialog) dialog.close();
        });
    });
}

async function loadExpenses() {
    elements.loadingState.hidden = false;
    elements.emptyState.hidden = true;

    try {
        const response = await fetch('/expenses', { headers: accountHeaders() });
        if (!response.ok) throw new Error(await responseMessage(response, 'Could not read expenses'));
        state.expenses = await response.json();
        rebuildFilterOptions();
        render();
    } catch (error) {
        state.expenses = [];
        render();
        elements.emptyState.hidden = false;
        elements.emptyState.querySelector('p').textContent = error.message;
    } finally {
        elements.loadingState.hidden = true;
    }
}

function render() {
    const expenses = visibleExpenses();
    const primaryCurrency = findPrimaryCurrency(expenses);
    const primaryExpenses = primaryCurrency
        ? expenses.filter(expense => expense.currency === primaryCurrency)
        : [];
    const primaryTotal = sum(primaryExpenses.map(expense => expense.amount));

    elements.periodLabel.textContent = periodTitle();
    elements.grandTotal.textContent = primaryCurrency ? formatMoney(primaryTotal, primaryCurrency) : '—';
    elements.expenseCount.textContent = expenses.length;
    elements.statCount.textContent = expenses.length;
    elements.lowConfidence.textContent = expenses.filter(expense => Number(expense.confidence) < LOW_CONFIDENCE).length;

    const activeDays = new Set(primaryExpenses.map(expense => dateKey(expense.createdDate))).size;
    elements.perDay.textContent = primaryCurrency && activeDays
        ? formatMoney(primaryTotal / activeDays, primaryCurrency)
        : '—';

    renderLedger(expenses);
    renderCategoryBreakdown(primaryExpenses, primaryCurrency);
    renderCurrencyBreakdown(expenses);
}

function visibleExpenses() {
    const now = new Date();
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
    const thirtyDaysAgo = new Date(now);
    thirtyDaysAgo.setDate(now.getDate() - 30);

    return [...state.expenses]
        .filter(expense => {
            const created = new Date(expense.createdDate);
            const inPeriod = state.period === 'all'
                || (state.period === 'month' && created >= monthStart)
                || (state.period === '30' && created >= thirtyDaysAgo);
            return inPeriod
                && (!state.category || expense.category === state.category)
                && (!state.currency || expense.currency === state.currency);
        })
        .sort((left, right) => new Date(right.createdDate) - new Date(left.createdDate));
}

function renderLedger(expenses) {
    elements.expenseList.replaceChildren();
    elements.emptyState.hidden = expenses.length !== 0 || state.pending.length > 0;
    if (!expenses.length) return;

    const grouped = groupExpensesByDate(expenses);
    for (const [day, dayExpenses] of grouped) {
        const section = document.createElement('section');
        section.className = 'date-group';

        const heading = document.createElement('h2');
        heading.className = 'date-heading';
        const time = document.createElement('time');
        time.dateTime = day;
        time.innerHTML = `${dateTitle(day)} <span>— ${weekday(day)}</span>`;

        const dayTotals = totalsBy(dayExpenses, 'currency', 'amount');
        const total = document.createElement('span');
        total.className = 'group-total';
        total.textContent = [...dayTotals.entries()]
            .map(([currency, amount]) => formatMoney(amount, currency))
            .join(' · ');

        heading.append(time, total);
        section.append(heading);

        dayExpenses.forEach(expense => section.append(createExpenseRow(expense)));
        elements.expenseList.append(section);
    }
}

function createExpenseRow(expense) {
    const row = document.createElement('article');
    row.className = 'expense-row';

    const name = document.createElement('div');
    name.className = 'expense-name';
    name.textContent = expense.merchant || expense.description || 'Expense';
    if (expense.description && expense.merchant && expense.description.toLowerCase() !== expense.merchant.toLowerCase()) {
        const description = document.createElement('em');
        description.textContent = expense.description;
        name.append(description);
    }

    const meta = document.createElement('div');
    meta.className = 'expense-meta';
    const confidence = Math.round((Number(expense.confidence) || 0) * 100);
    meta.textContent = `${humanize(expense.category)} · ${confidence}% CONF.`;

    const amount = document.createElement('div');
    amount.className = 'expense-amount';
    amount.textContent = formatMoney(Number(expense.amount), expense.currency);

    row.append(name, meta, amount);
    return row;
}

function renderCategoryBreakdown(expenses, currency) {
    elements.categoryBreakdown.replaceChildren();
    if (!expenses.length || !currency) {
        elements.categoryBreakdown.append(makeMutedMessage('No category data'));
        return;
    }

    const categories = [...totalsBy(expenses, 'category', 'amount').entries()]
        .sort((left, right) => right[1] - left[1]);
    const maximum = categories[0][1];

    categories.forEach(([category, amount]) => {
        const item = document.createElement('div');
        item.className = 'bar-item';
        item.innerHTML = `
            <div class="bar-label"><span>${escapeHtml(humanize(category))}</span><strong>${escapeHtml(formatMoney(amount, currency))}</strong></div>
            <div class="bar-track"><div class="bar-fill" style="width:${Math.max(2, (amount / maximum) * 100)}%"></div></div>`;
        elements.categoryBreakdown.append(item);
    });
}

function renderCurrencyBreakdown(expenses) {
    elements.currencyBreakdown.replaceChildren();
    if (!expenses.length) {
        elements.currencyBreakdown.append(makeMutedMessage('No wallet data'));
        return;
    }

    const currencies = totalsBy(expenses, 'currency', 'amount');
    [...currencies.entries()]
        .sort((left, right) => right[1] - left[1])
        .forEach(([currency, amount]) => {
            const count = expenses.filter(expense => expense.currency === currency).length;
            const item = document.createElement('div');
            item.className = 'wallet-item';
            item.innerHTML = `
                <div class="wallet-main"><span>${escapeHtml(currencyName(currency))}</span><strong>${escapeHtml(formatMoney(amount, currency))}</strong></div>
                <p class="wallet-meta">${count} ${count === 1 ? 'expense' : 'expenses'}</p>`;
            elements.currencyBreakdown.append(item);
        });
}

function rebuildFilterOptions() {
    replaceOptions(
        elements.categoryFilter,
        'ALL CATEGORIES',
        [...new Set(state.expenses.map(expense => expense.category).filter(Boolean))].sort(),
        humanize,
        state.category
    );
    replaceOptions(
        elements.currencyFilter,
        'ALL CURRENCIES',
        [...new Set(state.expenses.map(expense => expense.currency).filter(Boolean))].sort(),
        value => value,
        state.currency
    );
}

function replaceOptions(select, firstLabel, values, labelMaker, selected) {
    select.replaceChildren(new Option(firstLabel, ''));
    values.forEach(value => select.add(new Option(labelMaker(value).toUpperCase(), value)));
    select.value = values.includes(selected) ? selected : '';
    if (select.value !== selected) {
        if (select === elements.categoryFilter) state.category = '';
        if (select === elements.currencyFilter) state.currency = '';
    }
}

function openExpenseDialog() {
    elements.expenseForm.reset();
    elements.receiptFileName.textContent = 'No file selected';
    resetVoiceRecorder();
    setFormStatus('');
    setInputType('text');
    elements.expenseDialog.showModal();
    elements.expenseText.focus();
}

function setInputType(type) {
    if (state.inputType === 'voice' && type !== 'voice' && isRecording()) {
        stopVoiceRecording();
    }
    state.inputType = type;
    document.querySelectorAll('[data-input]').forEach(tab => {
        const active = tab.dataset.input === type;
        tab.classList.toggle('active', active);
        tab.setAttribute('aria-selected', String(active));
    });
    document.querySelectorAll('[data-panel]').forEach(panel => {
        panel.hidden = panel.dataset.panel !== type;
    });
}

// The server answers 202 straight away, so the dialog closes straight away.
// Waiting for PROCESSED inside the submit handler turned an asynchronous API
// back into a blocking one: the user sat in front of a frozen modal while the
// whole point of 202 was that they would not have to.
async function submitExpense(event) {
    event.preventDefault();
    const externalId = `web_${crypto.randomUUID()}`;

    try {
        setSubmitting(true);
        setFormStatus('SENDING TO CASHLENS…');
        const label = pendingLabel();
        const response = await sendExpense(externalId);
        if (!response.ok) throw new Error(await responseMessage(response, 'Could not submit expense'));

        // 202 — the server took it and started working.
        // 200 — it already had this one, and nothing new will happen.
        const statusUrl = response.headers.get('Location');
        logLine(`POST → ${response.status}  ${statusUrl}`);

        elements.expenseDialog.close();
        resetExpenseForm();

        if (response.status === 200) {
            await loadExpenses();
            return;
        }

        trackPending(externalId, label, statusUrl);
    } catch (error) {
        setFormStatus(error.message, 'error');
    } finally {
        setSubmitting(false);
    }
}

// Один запис у списку очікування на кожну відправку. Їх може бути кілька
// одночасно — кожен опитує свій statusUrl і сам себе прибирає.
function trackPending(externalId, label, statusUrl) {
    const item = { externalId, label, status: 'RECEIVED', error: null };
    state.pending.push(item);
    renderPending();

    waitForProcessing(statusUrl, (status) => {
        item.status = status;
        renderPending();
    })
        .then(() => {
            state.pending = state.pending.filter(p => p !== item);
            renderPending();
            return loadExpenses();
        })
        .catch((error) => {
            item.status = 'FAILED';
            item.error = error.message;
            renderPending();
        });
}

function renderPending() {
    elements.pendingList.replaceChildren();
    for (const item of state.pending) {
        const row = document.createElement('div');
        row.className = item.status === 'FAILED' ? 'pending-row failed' : 'pending-row';

        const name = document.createElement('div');
        name.className = 'pending-label';
        name.textContent = item.label;

        const state_ = document.createElement('div');
        state_.className = 'pending-status';
        state_.textContent = item.error ? item.error : `${item.status}…`;

        row.append(name, state_);
        if (item.status === 'FAILED') {
            const dismiss = document.createElement('button');
            dismiss.className = 'text-button';
            dismiss.type = 'button';
            dismiss.textContent = 'DISMISS';
            dismiss.onclick = () => {
                state.pending = state.pending.filter(p => p !== item);
                renderPending();
            };
            row.append(dismiss);
        }
        elements.pendingList.append(row);
    }
}

// Поки модель не розібрала витрату, показувати нічого — крім того,
// що користувач сам щойно надіслав.
function pendingLabel() {
    if (state.inputType === 'text') return elements.expenseText.value.trim() || 'Text note';
    if (state.inputType === 'photo') return elements.receiptFile.files[0]?.name || 'Receipt';
    return 'Voice note';
}

function resetExpenseForm() {
    elements.expenseText.value = '';
    elements.receiptFile.value = '';
    showSelectedFile(elements.receiptFile, elements.receiptFileName);
    resetVoiceRecorder();
    setFormStatus('');
}

function sendExpense(externalId) {
    if (state.inputType === 'text') {
        const payload = elements.expenseText.value.trim();
        if (!payload) throw new Error('Describe the expense first.');
        return fetch('/inbound/save/text', {
            method: 'POST',
            headers: { ...accountHeaders(), 'Content-Type': 'application/json' },
            body: JSON.stringify({ externalId, payload })
        });
    }

    const file = state.inputType === 'photo' ? elements.receiptFile.files[0] : state.voiceFile;
    if (!file) throw new Error(state.inputType === 'photo' ? 'Choose a receipt first.' : 'Record a voice note first.');

    const data = new FormData();
    data.append('externalId', externalId);
    data.append('file', file);
    return fetch(`/inbound/save/${state.inputType}`, {
        method: 'POST',
        headers: accountHeaders(),
        body: data
    });
}

// The server said where to look, so we go there. The client does not build
// this path: it does not know the id format, and does not need to know that
// the status endpoint happens to live under /inbound.
async function waitForProcessing(statusUrl, onStatus = () => {}) {
    const startedAt = Date.now();
    for (let attempt = 0; attempt < 120; attempt += 1) {
        await delay(attempt === 0 ? 400 : 1000);
        const response = await fetch(statusUrl, { headers: accountHeaders() });
        if (!response.ok) {
            if (response.status === 404) continue;
            throw new Error(await responseMessage(response, 'Could not read processing status'));
        }
        const result = await response.json();
        const elapsed = ((Date.now() - startedAt) / 1000).toFixed(1);
        logLine(`check ${attempt + 1}  +${elapsed}s → ${result.status}`);
        onStatus(result.status);
        if (result.status === 'PROCESSED') return;
        if (result.status === 'FAILED') throw new Error(result.failureReason || 'CashLens could not process this expense.');
    }
    throw new Error('CashLens is taking longer than expected. Refresh the ledger in a moment.');
}

async function toggleVoiceRecording() {
    if (state.requestingMicrophone) return;
    if (isRecording()) {
        stopVoiceRecording();
        return;
    }
    await startVoiceRecording();
}

async function startVoiceRecording() {
    if (!navigator.mediaDevices?.getUserMedia || typeof MediaRecorder === 'undefined') {
        setFormStatus('Voice recording is not supported in this browser.', 'error');
        return;
    }

    resetVoiceRecorder();
    state.requestingMicrophone = true;
    elements.recordButton.disabled = true;
    elements.recordingLabel.textContent = 'WAITING FOR MICROPHONE…';
    updateSubmitButton();

    try {
        const stream = await navigator.mediaDevices.getUserMedia({
            audio: {
                echoCancellation: true,
                noiseSuppression: true,
                autoGainControl: true
            }
        });
        state.mediaStream = stream;
        if (!elements.expenseDialog.open || state.inputType !== 'voice') {
            releaseMediaStream();
            setRecorderUi('idle', 'PRESS TO RECORD', 0);
            return;
        }
        const mimeType = preferredAudioMimeType();
        const recorder = mimeType
            ? new MediaRecorder(stream, { mimeType })
            : new MediaRecorder(stream);

        state.mediaRecorder = recorder;
        state.voiceChunks = [];
        state.voiceFile = null;
        state.discardRecording = false;

        recorder.addEventListener('dataavailable', event => {
            if (event.data.size > 0) state.voiceChunks.push(event.data);
        });
        recorder.addEventListener('stop', finalizeVoiceRecording, { once: true });
        recorder.addEventListener('error', () => {
            setFormStatus('The microphone recording failed. Please try again.', 'error');
            resetVoiceRecorder();
        }, { once: true });

        recorder.start(250);
        state.recordingStartedAt = Date.now();
        startRecordingTimer();
        setRecorderUi('recording', 'LISTENING — PRESS TO STOP', 0);
        setFormStatus('');
    } catch (error) {
        releaseMediaStream();
        const denied = error?.name === 'NotAllowedError' || error?.name === 'SecurityError';
        setFormStatus(
            denied ? 'Microphone access was blocked. Allow it in the browser and try again.' : 'Could not start the microphone.',
            'error'
        );
        setRecorderUi('idle', 'PRESS TO RECORD', 0);
    } finally {
        state.requestingMicrophone = false;
        elements.recordButton.disabled = false;
        updateSubmitButton();
    }
}

function stopVoiceRecording(discard = false) {
    state.discardRecording = discard;
    if (isRecording()) {
        elements.recordButton.disabled = true;
        elements.recordingLabel.textContent = 'PREPARING RECORDING…';
        state.mediaRecorder.stop();
    }
}

function finalizeVoiceRecording() {
    const recorder = state.mediaRecorder;
    const seconds = recordingSeconds();
    stopRecordingTimer();
    releaseMediaStream();
    state.mediaRecorder = null;
    elements.recordButton.disabled = false;

    if (state.discardRecording) {
        state.voiceFile = null;
        state.voiceChunks = [];
        state.discardRecording = false;
        setRecorderUi('idle', 'PRESS TO RECORD', 0);
        return;
    }

    const recordedMimeType = recorder?.mimeType || state.voiceChunks[0]?.type || 'audio/webm';
    const mimeType = recordedMimeType.split(';')[0];
    const blob = new Blob(state.voiceChunks, { type: mimeType });
    state.voiceChunks = [];

    if (!blob.size) {
        state.voiceFile = null;
        setRecorderUi('idle', 'PRESS TO RECORD', 0);
        setFormStatus('The recording was empty. Please try again.', 'error');
        return;
    }

    state.voiceFile = new File(
        [blob],
        `cashlens-voice-${Date.now()}.${audioExtension(mimeType)}`,
        { type: mimeType }
    );
    setRecorderUi('ready', 'RECORDING READY — PRESS TO RE-RECORD', seconds);
    setFormStatus('Voice note is ready to process.', 'success');
}

function resetVoiceRecorder() {
    stopRecordingTimer();
    if (isRecording()) {
        stopVoiceRecording(true);
        return;
    }
    releaseMediaStream();
    state.mediaRecorder = null;
    state.voiceFile = null;
    state.voiceChunks = [];
    state.recordingStartedAt = 0;
    state.discardRecording = false;
    setRecorderUi('idle', 'PRESS TO RECORD', 0);
}

function setRecorderUi(mode, label, seconds) {
    elements.voiceRecorder.classList.toggle('is-recording', mode === 'recording');
    elements.voiceRecorder.classList.toggle('is-ready', mode === 'ready');
    elements.recordingLabel.textContent = label;
    elements.recordingTimer.textContent = formatDuration(seconds);
    elements.recordButton.setAttribute('aria-label', mode === 'recording' ? 'Stop voice recording' : 'Start voice recording');
    updateSubmitButton();
}

function startRecordingTimer() {
    stopRecordingTimer();
    state.recordingTimerId = window.setInterval(() => {
        const seconds = recordingSeconds();
        elements.recordingTimer.textContent = formatDuration(seconds);
        if (seconds >= MAX_RECORDING_SECONDS) stopVoiceRecording();
    }, 250);
}

function stopRecordingTimer() {
    if (state.recordingTimerId !== null) {
        window.clearInterval(state.recordingTimerId);
        state.recordingTimerId = null;
    }
}

function recordingSeconds() {
    if (!state.recordingStartedAt) return 0;
    return Math.min(MAX_RECORDING_SECONDS, Math.floor((Date.now() - state.recordingStartedAt) / 1000));
}

function isRecording() {
    return state.mediaRecorder?.state === 'recording';
}

function releaseMediaStream() {
    state.mediaStream?.getTracks().forEach(track => track.stop());
    state.mediaStream = null;
}

function preferredAudioMimeType() {
    const candidates = [
        'audio/webm;codecs=opus',
        'audio/ogg;codecs=opus',
        'audio/mp4'
    ];
    return candidates.find(type => MediaRecorder.isTypeSupported(type)) || '';
}

function audioExtension(mimeType) {
    if (mimeType.includes('ogg')) return 'ogg';
    if (mimeType.includes('mp4')) return 'm4a';
    return 'webm';
}

function formatDuration(seconds) {
    const minutes = Math.floor(seconds / 60);
    const remainder = seconds % 60;
    return `${String(minutes).padStart(2, '0')}:${String(remainder).padStart(2, '0')}`;
}

function setSubmitting(submitting) {
    state.submitting = submitting;
    updateSubmitButton();
}

function updateSubmitButton() {
    elements.submitExpenseButton.disabled = state.submitting || isRecording() || state.requestingMicrophone;
    elements.submitExpenseButton.textContent = state.submitting ? 'PROCESSING…' : 'PROCESS EXPENSE';
}

function setFormStatus(message, type = '') {
    elements.formStatus.textContent = message;
    elements.formStatus.className = `form-status${type ? ` ${type}` : ''}`;
}

function showSelectedFile(input, label) {
    label.textContent = input.files[0]?.name || 'No file selected';
}

function updateAccountLabel() {
    elements.accountLabel.textContent = state.accountId.toUpperCase();
}

// Час у форматі того ж вигляду, що й у логах сервера, — щоб можна було
// покласти консоль браузера поруч із консоллю застосунку і читати один
// потік у двох місцях.
function logLine(message) {
    const now = new Date();
    const stamp = now.toTimeString().slice(0, 8)
        + '.' + String(now.getMilliseconds()).padStart(3, '0');
    console.log(`[cashlens ${stamp}] ${message}`);
}

function accountHeaders() {
    return { [ACCOUNT_HEADER]: state.accountId };
}

function totalsBy(expenses, key, numericKey) {
    const totals = new Map();
    expenses.forEach(expense => {
        const label = expense[key] || 'OTHER';
        totals.set(label, (totals.get(label) || 0) + Number(expense[numericKey] || 0));
    });
    return totals;
}

function groupExpensesByDate(expenses) {
    const grouped = new Map();
    expenses.forEach(expense => {
        const day = dateKey(expense.createdDate);
        if (!grouped.has(day)) grouped.set(day, []);
        grouped.get(day).push(expense);
    });
    return grouped;
}

function findPrimaryCurrency(expenses) {
    const counts = new Map();
    expenses.forEach(expense => counts.set(expense.currency, (counts.get(expense.currency) || 0) + 1));
    return [...counts.entries()].sort((left, right) => right[1] - left[1])[0]?.[0] || null;
}

function sum(values) {
    return values.reduce((total, value) => total + Number(value || 0), 0);
}

function formatMoney(value, currency) {
    try {
        return new Intl.NumberFormat(undefined, {
            style: 'currency',
            currency,
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(value);
    } catch {
        return `${currency || ''} ${Number(value || 0).toFixed(2)}`.trim();
    }
}

function dateKey(value) {
    const date = new Date(value);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function dateTitle(day) {
    const date = localDate(day);
    const today = new Date();
    const yesterday = new Date();
    yesterday.setDate(today.getDate() - 1);
    if (dateKey(date) === dateKey(today)) return 'Today';
    if (dateKey(date) === dateKey(yesterday)) return 'Yesterday';
    return new Intl.DateTimeFormat(undefined, { month: 'short', day: 'numeric' }).format(date);
}

function weekday(day) {
    return new Intl.DateTimeFormat(undefined, { weekday: 'short' }).format(localDate(day));
}

function localDate(day) {
    const [year, month, date] = day.split('-').map(Number);
    return new Date(year, month - 1, date);
}

function periodTitle() {
    if (state.period === 'month') return 'THIS MONTH';
    if (state.period === 'all') return 'ALL TIME';
    return 'LAST 30 DAYS';
}

function humanize(value) {
    if (!value) return 'Other';
    return value.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, letter => letter.toUpperCase());
}

function currencyName(currency) {
    try {
        return new Intl.DisplayNames([navigator.language], { type: 'currency' }).of(currency) || currency;
    } catch {
        return currency;
    }
}

function makeMutedMessage(message) {
    const paragraph = document.createElement('p');
    paragraph.className = 'wallet-meta';
    paragraph.textContent = message;
    return paragraph;
}

function delay(milliseconds) {
    return new Promise(resolve => window.setTimeout(resolve, milliseconds));
}

async function responseMessage(response, fallback) {
    try {
        const body = await response.json();
        return body.message || body.detail || fallback;
    } catch {
        return fallback;
    }
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
