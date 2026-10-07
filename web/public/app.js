'use strict'; // turns silent JavaScript mistakes (like a misspelled variable) into errors

/*
 * Room Booking System - browser front end.
 *
 * Plain JavaScript, no framework and no build step. The page keeps what it
 * knows in one `state` object and redraws itself from it after every action.
 * All text from the server is put on the page with textContent (never
 * innerHTML), so a room name can never be run as HTML or script.
 */

const app = document.getElementById('app');           // where each screen is drawn
const masthead = document.getElementById('masthead'); // the blue bar at the top

const state = {
  config: null,     // answer of GET /api/config: demo flag, server time, demo accounts
  me: null,         // answer of GET /api/me: who is signed in
  notice: null,     // {kind: 'ok' | 'error' | 'info', text} - the message shown at the top
  search: null,     // {start, end, rooms} - the last room search
  form: { start: '', hours: 2 },                 // what is typed in the search form
  pay: { method: 'CREDIT_CARD', fields: {} },    // payment method and details typed on the payment screen
  paying: null,     // set while the payment screen is open: {kind: 'deposit' | 'balance', ...}
  showRegister: false, // whether the "create an account" form is open on the first page
  bookings: [],     // the signed-in user's bookings
  editing: null,    // bookingId whose "change time" form is open
  rooms: []         // every room (administrator screen)
};


function el(tag, props, ...children) {
  const node = document.createElement(tag);
  for (const [key, value] of Object.entries(props || {})) {
    if (value === null || value === undefined || value === false) continue; // skip switched-off properties
    if (key === 'class') node.className = value;
    else if (key === 'text') node.textContent = value;                      // safe: never parsed as HTML
    else if (key.startsWith('on')) node.addEventListener(key.slice(2), value); // onclick -> 'click'
    else if (key === 'value') node.value = value;
    else node.setAttribute(key, value === true ? '' : value);
  }
  for (const child of children.flat(Infinity)) {       // children may be given as nested lists
    if (child) node.append(child);                                          // strings are added as text
  }
  return node;
}


async function api(method, path, body) {
  const options = { method, headers: {}, credentials: 'same-origin' };      // same-origin: send the session cookie
  if (method === 'POST') {
    options.headers['Content-Type'] = 'application/json';                   // the server refuses POSTs without this
    options.body = JSON.stringify(body || {});
  }
  const response = await fetch('/api/' + path, options);
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    throw new Error((data && data.error) || 'The request failed (' + response.status + ').');
  }
  return data;
}



function toDate(text) {
  const m = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(text);
  return m ? new Date(Date.UTC(+m[1], +m[2] - 1, +m[3], +m[4], +m[5])) : null;
}

function toText(date) {
  return date.toISOString().slice(0, 16);
}

function addMinutes(text, minutes) {
  return toText(new Date(toDate(text).getTime() + minutes * 60000));
}

function sayDay(text) {
  return toDate(text).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', timeZone: 'UTC' });
}

function sayTime(text) {
  return toDate(text).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', timeZone: 'UTC' });
}

function sayRange(start, end) {
  const sameDay = start.slice(0, 10) === end.slice(0, 10);
  return sayDay(start) + ', ' + sayTime(start) + ' to ' + (sameDay ? '' : sayDay(end) + ', ') + sayTime(end);
}

function money(amount) {                      // 20 -> "$20.00"
  return '$' + Number(amount).toFixed(2);
}

const METHOD_NAMES = {
  CREDIT_CARD: 'Credit card',
  DEBIT_CARD: 'Debit card',
  INSTITUTIONAL_BILLING: 'Institutional billing'
};

const ROOM_STATUS_NAMES = {
  AVAILABLE: 'In service',
  DISABLED: 'Switched off',
  MAINTENANCE: 'Closed for maintenance',
  OCCUPIED: 'Occupied'
};

function say(kind, text) {                    // sets the message shown at the top of the page
  state.notice = { kind, text };
}

// Runs one user action: clears the old message, shows any error, then reloads and redraws.
async function act(work) {
  state.notice = null;
  try {
    await work();
  } catch (error) {
    say('error', error.message);
  }
  await refresh();
}

// ---------------------------------------------------------------- loading

// Re-reads everything the current screen shows, then redraws it.
async function refresh() {
  try {
    state.config = await api('GET', 'config');          // also gives the server's current time
    state.me = await api('GET', 'me');
    if (state.me.authenticated && state.me.role === 'user') {
      state.bookings = await api('GET', 'bookings');
      if (!state.form.start) state.form.start = state.config.now; // first visit: search from "now"
      if (state.search) {
        try {
          await runSearch(false);                         // keep the room list in step with what was just booked
        } catch (stale) {
          state.search = null;                            // e.g. the searched time is now too far in the past
        }
      }
    }
    if (state.me.authenticated && state.me.role === 'admin') {
      state.rooms = await api('GET', 'admin/rooms');
    }
  } catch (error) {
    say('error', error.message);
  }
  draw();
}

async function runSearch(announce) {
  const start = state.form.start;
  const end = addMinutes(start, state.form.hours * 60);
  const rooms = await api('GET', 'rooms/available?start=' + encodeURIComponent(start) + '&end=' + encodeURIComponent(end));
  state.search = { start, end, rooms };
  if (announce && rooms.length === 0) {
    say('info', 'No room is free for that time. Try another start or a shorter booking.');
  }
}

// ---------------------------------------------------------------- drawing

function draw() {
  masthead.replaceChildren(...drawMasthead());
  const parts = [];
  if (state.notice) {
    parts.push(el('p', { class: 'notice ' + state.notice.kind, role: state.notice.kind === 'error' ? 'alert' : 'status', text: state.notice.text }));
  }
  if (!state.config || !state.me) {
    parts.push(el('p', { class: 'empty', text: 'The server did not answer. Reload the page to try again.' }));
  } else if (!state.me.authenticated) {
    parts.push(drawWelcome());
  } else if (state.me.role === 'admin') {
    parts.push(drawAdmin());
  } else {
    parts.push(drawUser());
  }
  app.replaceChildren(el('div', { class: 'stack' }, parts));
}

function drawMasthead() {
  const parts = [el('span', { class: 'brand', text: 'Room Booking System' })];
  const me = state.me;
  if (me && me.authenticated) {
    const detail = me.role === 'admin' ? 'administrator' : me.accountType.toLowerCase() + ' rate ' + money(me.hourlyRate) + ' an hour';
    parts.push(el('span', { class: 'who' }, el('strong', { text: me.name }), ', ' + detail));
    parts.push(el('button', {
      class: 'btn plain small', type: 'button', text: 'Log out',
      onclick: () => act(async () => {
        await api('POST', 'logout');
        state.search = null;                             // forget the previous person's search
        state.form.start = '';
        state.editing = null;
        state.paying = null;                             // close the payment screen if it was open
        state.pay = { method: 'CREDIT_CARD', fields: {} }; // and forget the payment method and details that were typed
      })
    }));
  }
  return parts;
}

// ---------- first page (nobody logged in)

// Two choices side by side: try the demo, or log in. The demo comes first
// because it is the quickest way for a visitor to see the system working.
function drawWelcome() {
  const demo = state.config.demo;
  const box = el('section', { class: 'welcome' },
    el('h1', { text: 'Book a room on campus' }),
    el('p', {
      class: 'lead',
      text: demo
        ? 'A working room booking system. To look around, use "Try the demo": it needs no account and takes about a minute.'
        : 'Log in to search for a free room, book it, and check in when you arrive.'
    }));

  const choices = el('div', { class: 'choices' });
  if (demo) choices.append(drawDemoChoice());
  choices.append(drawLoginChoice());
  box.append(choices);
  if (demo) box.append(drawHowTo());
  return box;
}

// Choice 1: one-click demo accounts.
function drawDemoChoice() {
  const passes = el('div', { class: 'passes' });
  for (const account of state.config.demoAccounts) {
    passes.append(drawPass(account.key, account.name.replace('Demo ', ''), money(account.hourlyRate) + ' an hour', false));
  }
  passes.append(drawPass('admin', 'Administrator', 'Add rooms and take them out of service', true));
  return el('section', { class: 'panel choice primary' },
    el('h2', { text: 'Try the demo' }),
    el('p', { text: 'No account needed. Pick a role and you are in.' }),
    passes,
    el('p', { class: 'hint', text: 'Payments are simulated, and demo data is cleared when the server restarts.' }));
}

function drawPass(key, role, detail, isAdmin) {
  return el('button', {
    class: 'pass' + (isAdmin ? ' admin' : ''), type: 'button',
    onclick: () => act(() => api('POST', 'demo-login', { account: key }))
  }, el('span', { class: 'role', text: role }), el('span', { class: 'detail', text: detail }));
}

// A labelled input. `store` is the object the typed value is saved into, under `key`.
function field(label, type, store, key, extra) {
  const id = 'f-' + label.toLowerCase().replace(/[^a-z0-9]+/g, '-');
  const input = el('input', Object.assign({
    id, type, value: store[key] || '',
    oninput: (event) => { store[key] = event.target.value; }
  }, extra || {}));
  return el('div', null, el('label', { for: id, text: label }), input);
}

const loginForm = {};    // values typed into the log-in form
const registerForm = { accountType: 'STUDENT' };

// Choice 2: log in with your own account, or open the form to create one.
function drawLoginChoice() {
  const panel = el('section', { class: 'panel choice' },
    el('h2', { text: 'Log in' }),
    el('p', { text: 'For people who have created their own account.' }),
    el('form', {
      class: 'stack',
      onsubmit: (event) => {
        event.preventDefault();                          // stay on the page instead of reloading it
        act(() => api('POST', 'login', { email: loginForm.email || '', password: loginForm.password || '' }));
      }
    },
      el('div', { class: 'fields' },
        field('Email', 'email', loginForm, 'email', { autocomplete: 'username', required: true }),
        field('Password', 'password', loginForm, 'password', { autocomplete: 'current-password', required: true })),
      el('button', { class: 'btn', type: 'submit', text: 'Log in' })),
    el('button', {
      class: 'btn quiet small', type: 'button',
      text: state.showRegister ? 'Close the new-account form' : 'Create an account',
      'aria-expanded': state.showRegister ? 'true' : 'false',
      onclick: () => { state.showRegister = !state.showRegister; draw(); }
    }));
  if (state.showRegister) panel.append(drawRegister());
  return panel;
}

function drawRegister() {
  const typeSelect = el('select', { id: 'f-account-type', onchange: (event) => { registerForm.accountType = event.target.value; } },
    ['STUDENT', 'FACULTY', 'STAFF', 'PARTNER'].map((type) =>
      el('option', { value: type, selected: registerForm.accountType === type, text: type.charAt(0) + type.slice(1).toLowerCase() })));
  return el('form', {
    class: 'stack register',
    onsubmit: (event) => {
      event.preventDefault();
      act(async () => {
        await api('POST', 'register', {
          email: registerForm.email || '', password: registerForm.password || '', userName: registerForm.userName || '',
          accountType: registerForm.accountType, organizationId: registerForm.organizationId || ''
        });
        state.showRegister = false;                      // signed up and logged in: close the form
        registerForm.password = '';                      // do not keep the password in the page
      });
    }
  },
    el('h3', { text: 'Create an account' }),
    el('div', { class: 'fields' },
      field('Name', 'text', registerForm, 'userName', { autocomplete: 'name', required: true }),
      field('Email address', 'email', registerForm, 'email', { autocomplete: 'email', required: true }),
      field('New password', 'password', registerForm, 'password', { autocomplete: 'new-password', required: true }),
      el('div', null, el('label', { for: 'f-account-type', text: 'Account type' }), typeSelect),
      field('Organization ID', 'text', registerForm, 'organizationId', { inputmode: 'numeric', required: true })),
    el('p', { class: 'hint', text: 'Student, faculty and staff accounts need a @yorku.ca address; a partner account works with any address. The organization ID has 9 digits. Passwords need 8 characters with upper case, lower case, a digit and a symbol.' }),
    state.config.demo ? el('p', { class: 'hint', text: 'This is a demonstration site: use a made-up password. Accounts are erased when the server restarts.' }) : null,
    el('button', { class: 'btn', type: 'submit', text: 'Create account' }));
}

// Numbered because it really is a sequence: each step sets up the next.
function drawHowTo() {
  return el('section', { class: 'howto' },
    el('h2', { text: 'How to try it' }),
    el('ol', null,
      el('li', { text: 'Under "Try the demo", choose Student.' }),
      el('li', { text: 'Press "Starting now", then "Book" on any room, and confirm the simulated payment.' }),
      el('li', { text: 'Press "Check in" on the new booking, then "Pay" to complete it.' }),
      el('li', { text: 'Log out and choose Administrator. Switch a room off, then log out and choose Student: that room has left the search.' })));
}

// ---------- logged in as a user

function drawUser() {
  if (state.paying) {
    return drawPayment();                                // the payment screen replaces rooms and bookings
  }
  const parts = [];
  if (state.me.demoSession) {
    parts.push(el('p', { class: 'notice info', text: 'This demo account is shared, so other visitors may be using it too. Payments are simulated.' }));
  }
  parts.push(el('div', { class: 'columns' }, drawFindRoom(), drawBookings()));
  return el('div', { class: 'stack' }, parts);
}

// Opens the payment screen. `payment` says what is being paid for (see drawPayment).
function openPayment(payment) {
  state.paying = payment;
  state.notice = null;
  draw();
  window.scrollTo(0, 0);                                 // the payment screen starts at the top of the page
}

function drawFindRoom() {
  const form = state.form;
  const hoursSelect = el('select', { id: 'f-length', onchange: (event) => { form.hours = Number(event.target.value); } },
    [1, 2, 3, 4].map((hours) => el('option', { value: hours, selected: form.hours === hours, text: hours + (hours === 1 ? ' hour' : ' hours') })));

  const searchForm = el('form', {
    class: 'panel stack',
    onsubmit: (event) => {
      event.preventDefault();
      act(() => runSearch(true));
    }
  },
    el('div', { class: 'fields' },
      field('Start', 'datetime-local', form, 'start', { required: true }),
      el('div', null, el('label', { for: 'f-length', text: 'Length' }), hoursSelect)),
    el('div', { class: 'row' },
      el('button', {
        class: 'btn quiet small', type: 'button', text: 'Starting now',
        onclick: () => act(async () => {
          form.start = (await api('GET', 'config')).now;  // the server's clock decides what "now" is
          await runSearch(true);
        })
      }),
      el('button', {
        class: 'btn quiet small', type: 'button', text: 'Tomorrow at 10:00',
        onclick: () => act(async () => {
          form.start = addMinutes(state.config.now.slice(0, 10) + 'T10:00', 24 * 60);
          await runSearch(true);
        })
      })),
    el('p', { class: 'hint', text: 'Times are campus time (' + state.config.zone + '). To try check-in straight away, book a room starting now.' }),
    el('button', { class: 'btn', type: 'submit', text: 'Search rooms' }));

  const section = el('section', null,
    el('div', { class: 'section-head' }, el('h2', { text: 'Find a room' })),
    searchForm);

  if (state.search) {
    const { start, end, rooms } = state.search;
    const hours = (toDate(end) - toDate(start)) / 3600000;
    section.append(el('div', { class: 'section-head results-head' },
      el('h3', { text: rooms.length + (rooms.length === 1 ? ' free room' : ' free rooms') }),
      el('p', { text: sayRange(start, end) })));
    if (rooms.length > 0) {
      section.append(el('p', { class: 'hint', text: 'Booking charges a deposit of ' + money(state.me.hourlyRate) + ' (one hour). You pay it on the next screen.' }));
      section.append(el('ul', { class: 'rooms' }, rooms.map((room) => el('li', { class: 'room' },
        el('span', { class: 'number', text: room.roomNumber }),
        el('span', null,
          el('span', { class: 'where', text: room.building }),
          el('br'),
          el('span', { class: 'seats', text: 'Seats ' + room.capacity + ', room ID ' + room.roomId })),
        el('button', {
          class: 'btn', type: 'button', text: 'Book', 'aria-label': 'Book ' + room.building + ' ' + room.roomNumber,
          onclick: () => openPayment({ kind: 'deposit', room, start, end, hours, amount: state.me.hourlyRate })
        })))));
    }
  }
  return section;
}

// The details each payment method asks for. `key` is the name the server expects.
const PAYMENT_FIELDS = {
  CREDIT_CARD: [
    { key: 'cardNumber', label: 'Card number', type: 'text', extra: { inputmode: 'numeric', autocomplete: 'off', placeholder: '4242 4242 4242 4242' } },
    { key: 'expiryDate', label: 'Expiry date', type: 'text', extra: { autocomplete: 'off', placeholder: 'MM/YY' } },
    { key: 'cvv', label: 'Security code', type: 'password', extra: { inputmode: 'numeric', autocomplete: 'off' } }],
  DEBIT_CARD: [
    { key: 'cardNumber', label: 'Card number', type: 'text', extra: { inputmode: 'numeric', autocomplete: 'off', placeholder: '4242 4242 4242 4242' } },
    { key: 'pin', label: 'PIN', type: 'password', extra: { inputmode: 'numeric', autocomplete: 'off' } }],
  INSTITUTIONAL_BILLING: [
    { key: 'organizationId', label: 'Organization ID', type: 'text', extra: { inputmode: 'numeric' } },
    { key: 'billingAccount', label: 'Billing account', type: 'text', extra: {} }]
};

// The payment screen. state.paying is one of:
//   {kind: 'deposit', room, start, end, hours, amount}  - opened by "Book": pays the deposit and creates the booking
//   {kind: 'balance', booking, amount}                  - opened by "Pay": pays what is left after check-in
function drawPayment() {
  const paying = state.paying;
  const pay = state.pay;
  const isDeposit = paying.kind === 'deposit';
  const demoSession = state.me.demoSession;
  const place = isDeposit
    ? paying.room.building + ' ' + paying.room.roomNumber
    : (paying.booking.building || 'Room') + ' ' + (paying.booking.roomNumber || paying.booking.roomId);
  const start = isDeposit ? paying.start : paying.booking.start;
  const end = isDeposit ? paying.end : paying.booking.end;

  // What is being paid for, as label / value rows.
  const rows = [['Room', place], ['Time', sayRange(start, end)]];
  if (isDeposit) {
    rows.push(['Full cost', money(state.me.hourlyRate * paying.hours) + ' for ' + paying.hours + (paying.hours === 1 ? ' hour' : ' hours')]);
  } else {
    rows.push(['Full cost', money(paying.booking.totalCost)]);
    rows.push(['Deposit already paid', money(paying.booking.deposit)]);
  }
  const summary = el('dl', { class: 'summary' },
    rows.map(([label, value]) => [el('dt', { text: label }), el('dd', { text: value })]),
    el('dt', { text: isDeposit ? 'Deposit due now' : 'Balance due now' }),
    el('dd', { class: 'due', text: money(paying.amount) }));

  // A demo account shows the made-up details the server will use, and they cannot be edited.
  // A person's own account types its details into pay.fields.
  const store = demoSession ? Object.assign({}, state.me.demoPayment[pay.method]) : pay.fields;
  const inputs = PAYMENT_FIELDS[pay.method].map((f) =>
    field(f.label, demoSession ? 'text' : f.type, store, f.key, Object.assign({ required: true }, f.extra, demoSession ? { readonly: true } : {})));

  const methodSelect = el('select', {
    id: 'f-method',
    onchange: (event) => { pay.method = event.target.value; draw(); }   // redraw: each method has different fields
  }, state.config.paymentMethods.map((method) => el('option', { value: method, selected: pay.method === method, text: METHOD_NAMES[method] || method })));

  let warning;
  if (demoSession) {
    warning = 'Demo payment: test details are filled in for you, and nothing is charged.';
  } else if (state.config.demo) {
    warning = 'Payments on this site are simulated. Do not enter a real card: use 4242 4242 4242 4242, any expiry date and any 3 digits. What you type is checked and then discarded.';
  } else {
    warning = 'Payments are simulated: nothing is charged.';
  }

  const close = () => { state.paying = null; state.notice = null; draw(); };

  return el('section', { class: 'payment stack' },
    el('div', { class: 'section-head' },
      el('h2', { text: isDeposit ? 'Pay the deposit' : 'Pay the balance' }),
      el('p', { text: isDeposit ? 'One hour is charged now. It counts toward the full cost once you check in.' : 'The deposit has been taken off the full cost.' })),
    el('div', { class: 'panel' }, summary),
    el('form', {
      class: 'panel stack',
      onsubmit: (event) => {
        event.preventDefault();
        act(async () => {
          const details = demoSession ? {} : pay.fields;      // a demo account sends no payment details at all
          const methodName = METHOD_NAMES[pay.method].toLowerCase();
          if (isDeposit) {
            const booking = await api('POST', 'bookings', Object.assign(
              { roomId: paying.room.roomId, start: paying.start, end: paying.end, paymentMethod: pay.method }, details));
            say('ok', 'Booked ' + place + '. Deposit of ' + money(booking.deposit) + ' paid by ' + methodName + '.');
          } else {
            const paid = await api('POST', 'bookings/' + encodeURIComponent(paying.booking.bookingId) + '/pay',
              Object.assign({ paymentMethod: pay.method }, details));
            say('ok', 'Paid ' + money(paid.amountPaid) + ' by ' + methodName + '. Booking completed.');
          }
          state.paying = null;                                // paid: go back to rooms and bookings
          pay.fields = {};                                    // and forget what was typed
        });
      }
    },
      el('div', { class: 'fields' },
        el('div', null, el('label', { for: 'f-method', text: 'Pay with' }), methodSelect)),
      el('div', { class: 'fields' }, inputs),
      el('p', { class: 'notice info', text: warning }),
      el('div', { class: 'row' },
        el('button', { class: 'btn', type: 'submit', text: isDeposit ? 'Pay ' + money(paying.amount) + ' and book' : 'Pay ' + money(paying.amount) }),
        el('button', { class: 'btn quiet', type: 'button', text: 'Back', onclick: close }))));
}

function drawBookings() {
  const section = el('section', null,
    el('div', { class: 'section-head' }, el('h2', { text: 'Your bookings' })));
  if (state.bookings.length === 0) {
    section.append(el('p', { class: 'empty', text: 'No bookings yet. Search for a room on this page and book it.' }));
  } else {
    section.append(el('ul', { class: 'bookings' }, state.bookings.map(drawBooking)));
  }
  return section;
}

function drawBooking(booking) {
  const now = state.config.now;                          // server time, same format as the booking times
  const started = booking.start <= now;                  // ISO date strings compare correctly as text
  const ended = booking.end <= now;
  const id = booking.bookingId;
  const place = (booking.building || 'Room') + ' ' + (booking.roomNumber || booking.roomId);
  const post = (action, body) => api('POST', 'bookings/' + encodeURIComponent(id) + '/' + action, body || {});

  const actions = [];
  if (booking.status === 'CONFIRMED' && started) {
    actions.push(el('button', { class: 'btn small', type: 'button', text: 'Check in', onclick: () => act(async () => {
      await post('check-in');
      say('ok', 'Checked in to ' + place + '. The deposit now counts toward the total.');
    }) }));
  }
  if (booking.status === 'CHECKED_IN' && booking.remainingBalance > 0) {
    actions.push(el('button', {
      class: 'btn small', type: 'button', text: 'Pay ' + money(booking.remainingBalance),
      onclick: () => openPayment({ kind: 'balance', booking, amount: booking.remainingBalance })   // payment has its own screen
    }));
  }
  if ((booking.status === 'CONFIRMED' || booking.status === 'CHECKED_IN') && !ended) {
    actions.push(el('button', { class: 'btn quiet small', type: 'button', text: 'Extend 30 minutes', onclick: () => act(async () => {
      await post('extend', { until: addMinutes(booking.end, 30) });
      say('ok', place + ' is now booked until ' + sayTime(addMinutes(booking.end, 30)) + '.');
    }) }));
  }
  if (booking.status === 'CONFIRMED' && !started) {
    actions.push(el('button', { class: 'btn quiet small', type: 'button', text: state.editing === id ? 'Close' : 'Change time', onclick: () => {
      state.editing = state.editing === id ? null : id;
      draw();
    } }));
    actions.push(el('button', { class: 'btn danger small', type: 'button', text: 'Cancel booking', onclick: () => act(async () => {
      await post('cancel');
      say('ok', 'Cancelled ' + place + '. The deposit was refunded.');
    }) }));
  }

  const item = el('li', { class: 'booking' },
    el('div', { class: 'top' },
      el('span', { class: 'number', text: booking.roomNumber || booking.roomId }),
      el('span', { class: 'when', text: sayRange(booking.start, booking.end) })),
    el('div', { class: 'where', text: (booking.building || '') + ', room ID ' + booking.roomId }),
    drawRail(booking),
    el('p', { class: 'money', text: moneyLine(booking) }),
    actions.length ? el('div', { class: 'row actions' }, actions) : null);

  if (state.editing === id && booking.status === 'CONFIRMED' && !started) {
    const edit = { start: booking.start.slice(0, 16), end: booking.end.slice(0, 16) };
    item.append(el('form', {
      class: 'stack',
      onsubmit: (event) => {
        event.preventDefault();
        act(async () => {
          await post('edit', { start: edit.start, end: edit.end });
          state.editing = null;
          say('ok', place + ' moved to ' + sayRange(edit.start, edit.end) + '.');
        });
      }
    },
      el('div', { class: 'fields' },
        field('New start', 'datetime-local', edit, 'start', { required: true }),
        field('New end', 'datetime-local', edit, 'end', { required: true })),
      el('button', { class: 'btn small', type: 'submit', text: 'Save new time' })));
  }
  return item;
}

// One plain sentence about the money side of a booking.
function moneyLine(booking) {
  const total = 'Total ' + money(booking.totalCost) + '.';
  switch (booking.status) {
    case 'CONFIRMED':
      return total + ' Deposit of ' + money(booking.deposit) + ' paid; it counts toward the total once you check in.';
    case 'CHECKED_IN':
      return booking.remainingBalance > 0
        ? total + ' Deposit of ' + money(booking.deposit) + ' applied, ' + money(booking.remainingBalance) + ' left to pay.'
        : total + ' The deposit covered it, so nothing is left to pay.';
    case 'COMPLETED':
      return total + ' Paid in full.';
    case 'CANCELLED':
      return booking.depositStatus === 'REFUNDED' ? 'Deposit of ' + money(booking.deposit) + ' refunded.' : 'Cancelled.';
    case 'EXPIRED':
      return 'Not checked in within 30 minutes, so the deposit of ' + money(booking.deposit) + ' was kept.';
    default:
      return total;
  }
}

// The lifecycle rail: where the booking is among its states (the State pattern, drawn).
function drawRail(booking) {
  const stop = (label, cls) => el('li', { class: cls }, el('span', { text: label }));
  let stops;
  switch (booking.status) {
    case 'CONFIRMED':
      stops = [stop('Booked', 'now'), stop('Checked in', ''), stop('Completed', '')];
      break;
    case 'CHECKED_IN':
      stops = [stop('Booked', 'done through'), stop('Checked in', 'now'), stop('Completed', '')];
      break;
    case 'COMPLETED':
      stops = [stop('Booked', 'done through'), stop('Checked in', 'done through'), stop('Completed', 'done')];
      break;
    case 'CANCELLED':
      stops = [stop('Booked', 'done'), stop('Cancelled', 'ended')];
      break;
    default: // EXPIRED
      stops = [stop('Booked', 'done'), stop('Expired', 'ended kept')];
  }
  return el('ol', { class: 'rail', 'aria-label': 'Booking progress' }, stops);
}

// ---------- logged in as an administrator

const roomForm = {};   // values typed into the add-room form

function drawAdmin() {
  const roomAction = (room, action, done) => act(async () => {
    await api('POST', 'admin/rooms/' + encodeURIComponent(room.roomId) + '/' + action);
    say('ok', room.building + ' ' + room.roomNumber + ' ' + done);
  });

  const rows = state.rooms.map((room) => el('tr', null,
    el('td', { class: 'id', text: room.roomId }),
    el('td', { text: room.building + ' ' + room.roomNumber }),
    el('td', { text: String(room.capacity) }),
    el('td', null, el('span', { class: 'light ' + room.status, text: ROOM_STATUS_NAMES[room.status] || room.status })),
    el('td', null, el('div', { class: 'row' },
      room.status !== 'AVAILABLE' ? el('button', { class: 'btn small', type: 'button', text: 'Put in service', onclick: () => roomAction(room, 'enable', 'is back in service.') }) : null,
      room.status !== 'DISABLED' ? el('button', { class: 'btn quiet small', type: 'button', text: 'Switch off', onclick: () => roomAction(room, 'disable', 'is switched off and hidden from search.') }) : null,
      room.status !== 'MAINTENANCE' ? el('button', { class: 'btn quiet small', type: 'button', text: 'Close for maintenance', onclick: () => roomAction(room, 'close', 'is closed for maintenance.') }) : null))));

  const table = el('div', { class: 'panel table-wrap' },
    el('table', null,
      el('thead', null, el('tr', null,
        el('th', { scope: 'col', text: 'Room ID' }), el('th', { scope: 'col', text: 'Location' }),
        el('th', { scope: 'col', text: 'Seats' }), el('th', { scope: 'col', text: 'Status' }), el('th', { scope: 'col', text: 'Change status' }))),
      el('tbody', null, rows)));

  const addForm = el('form', {
    class: 'panel stack',
    onsubmit: (event) => {
      event.preventDefault();
      act(async () => {
        const room = await api('POST', 'admin/rooms', {
          roomId: roomForm.roomId || '', building: roomForm.building || '',
          roomNumber: roomForm.roomNumber || '', capacity: roomForm.capacity || ''
        });
        say('ok', 'Added ' + room.building + ' ' + room.roomNumber + '. It is in service and can be booked.');
        for (const key of Object.keys(roomForm)) delete roomForm[key];   // empty the form for the next room
      });
    }
  },
    el('h3', { text: 'Add a room' }),
    el('div', { class: 'fields' },
      field('Room ID', 'text', roomForm, 'roomId', { required: true, maxlength: 20, placeholder: 'VH-1005' }),
      field('Building', 'text', roomForm, 'building', { required: true, maxlength: 40, placeholder: 'Vari Hall' }),
      field('Room number', 'text', roomForm, 'roomNumber', { required: true, maxlength: 40, placeholder: '1005' }),
      field('Seats', 'number', roomForm, 'capacity', { required: true, min: 1, max: 1000 })),
    el('button', { class: 'btn', type: 'submit', text: 'Add room' }));

  return el('section', { class: 'stack' },
    el('div', { class: 'section-head' },
      el('h2', { text: 'Rooms' }),
      el('p', { text: 'Only rooms in service appear in search. Log out and try the Student demo to see the effect.' })),
    state.config.demo ? el('p', { class: 'notice info', text: 'A sample room you take out of service comes back by itself after 10 minutes, so the demo is never left empty.' }) : null,
    table,
    addForm);
}

// ---------------------------------------------------------------- start

refresh();   // ask the server who is signed in, then draw the first screen
