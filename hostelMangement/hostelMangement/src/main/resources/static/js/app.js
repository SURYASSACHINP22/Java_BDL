'use strict';

/* =========================================================
   Helpers
   ========================================================= */

const $ = (sel, root = document) => root.querySelector(sel);

function esc(value) {
    if (value === null || value === undefined) return '';
    return String(value).replace(/[&<>"']/g, c => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
}

const BLOOD_GROUPS = {
    A_POSITIVE: 'A+', A_NEGATIVE: 'A-',
    B_POSITIVE: 'B+', B_NEGATIVE: 'B-',
    AB_POSITIVE: 'AB+', AB_NEGATIVE: 'AB-',
    O_POSITIVE: 'O+', O_NEGATIVE: 'O-'
};
const bloodLabel = value => value ? BLOOD_GROUPS[value] : 'Unknown';

// the backend sends LocalDate / LocalDateTime without a zone, treat them as local time
function parseDate(iso) {
    if (!iso) return null;
    if (iso.length === 10) {
        const [y, m, d] = iso.split('-').map(Number);
        return new Date(y, m - 1, d);
    }
    return new Date(iso.slice(0, 19));
}

function fmtDate(iso) {
    const d = parseDate(iso);
    return d ? d.toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' }) : '—';
}

function fmtDateTime(iso) {
    const d = parseDate(iso);
    return d ? d.toLocaleString(undefined, {
        day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
    }) : '—';
}

// value for <input type="date"> / <input type="datetime-local">
function toInputDate(date) {
    const pad = n => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}
function toInputDateTime(date) {
    const pad = n => String(n).padStart(2, '0');
    return `${toInputDate(date)}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

const isSameDay = (a, b) => a.toDateString() === b.toDateString();

/* =========================================================
   API client
   ========================================================= */

const api = {
    async request(method, url, body) {
        const options = { method, headers: {} };
        if (body !== undefined) {
            options.headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(body);
        }

        const res = await fetch(url, options);
        if (res.status === 204) return null;

        const text = await res.text();
        let data = null;
        try { data = text ? JSON.parse(text) : null; } catch { /* not JSON */ }

        if (!res.ok) {
            // backend errors are ProblemDetail: { title, status, detail }
            throw new Error(data?.detail || data?.title || `Request failed (${res.status})`);
        }
        return data;
    },
    get: url => api.request('GET', url),
    post: (url, body) => api.request('POST', url, body),
    put: (url, body) => api.request('PUT', url, body),
    patch: url => api.request('PATCH', url),
    del: url => api.request('DELETE', url)
};

const loadAllPatients = () => api.get('/patients?size=1000').then(page => page.content);
const loadDoctors = () => api.get('/doctors');

/* =========================================================
   UI primitives: toast, modal, form, confirm
   ========================================================= */

function toast(message, type = 'ok') {
    const el = document.createElement('div');
    el.className = 'toast' + (type === 'error' ? ' error' : '');
    el.textContent = message;
    $('#toasts').append(el);
    setTimeout(() => el.remove(), 3800);
}

const modal = $('#modal');
$('#modal-close').addEventListener('click', () => modal.close());

function openModal(title, html, { wide = false } = {}) {
    $('#modal-title').textContent = title;
    $('#modal-body').innerHTML = html;
    modal.classList.toggle('wide', wide);
    if (!modal.open) modal.showModal();
    return $('#modal-body');
}

function closeModal() {
    if (modal.open) modal.close();
}

function fieldHtml(field, value) {
    const id = `f-${field.name}`;
    const required = field.required ? 'required' : '';
    const current = value === null || value === undefined ? '' : String(value);
    const label = `<label for="${id}">${esc(field.label)}${field.required ? ' <span class="req">*</span>' : ''}</label>`;

    let input;
    if (field.type === 'select') {
        const placeholder = field.placeholder !== undefined
            ? `<option value="">${esc(field.placeholder)}</option>` : '';
        const options = field.options.map(o =>
            `<option value="${esc(o.value)}" ${String(o.value) === current ? 'selected' : ''}>${esc(o.label)}</option>`
        ).join('');
        input = `<select id="${id}" name="${field.name}" ${required}>${placeholder}${options}</select>`;
    } else if (field.type === 'textarea') {
        input = `<textarea id="${id}" name="${field.name}" rows="3" ${required}
                    ${field.maxlength ? `maxlength="${field.maxlength}"` : ''}>${esc(current)}</textarea>`;
    } else {
        input = `<input id="${id}" name="${field.name}" type="${field.type || 'text'}" value="${esc(current)}" ${required}
                    ${field.min ? `min="${field.min}"` : ''} ${field.max ? `max="${field.max}"` : ''}
                    ${field.maxlength ? `maxlength="${field.maxlength}"` : ''}
                    ${field.placeholder ? `placeholder="${esc(field.placeholder)}"` : ''}>`;
    }
    return `<div class="field ${field.full ? 'full' : ''}">${label}${input}</div>`;
}

function readForm(form, fields) {
    const data = {};
    for (const field of fields) {
        const raw = form.elements[field.name].value.trim();
        data[field.name] = raw === '' ? null : (field.number ? Number(raw) : raw);
    }
    return data;
}

// wires submit handling (validation, busy state, inline error) onto a <form>
function bindForm(form, fields, onSubmit) {
    form.addEventListener('submit', async e => {
        e.preventDefault();
        if (!form.reportValidity()) return;

        const button = form.querySelector('[type=submit]');
        form.querySelector('.form-error')?.remove();
        button.disabled = true;
        try {
            await onSubmit(readForm(form, fields));
        } catch (err) {
            form.insertAdjacentHTML('afterbegin', `<div class="form-error">${esc(err.message)}</div>`);
        } finally {
            button.disabled = false;
        }
    });
}

function openForm({ title, fields, values = {}, submitLabel = 'Save', note = '', onSubmit }) {
    const body = openModal(title, `
        ${note ? `<p class="muted" style="margin-top:0">${note}</p>` : ''}
        <form class="form" novalidate>
            ${fields.map(f => fieldHtml(f, values[f.name])).join('')}
            <div class="form-actions">
                <button type="button" class="btn" data-cancel>Cancel</button>
                <button type="submit" class="btn btn-primary">${esc(submitLabel)}</button>
            </div>
        </form>`);

    const form = $('form', body);
    form.querySelector('[data-cancel]').addEventListener('click', closeModal);
    bindForm(form, fields, async data => {
        await onSubmit(data);
        closeModal();
    });
    form.elements[fields[0].name]?.focus();
}

function confirmAction(title, message, confirmLabel = 'Delete') {
    return new Promise(resolve => {
        const body = openModal(title, `
            <p style="margin-top:0">${esc(message)}</p>
            <div class="form-actions">
                <button type="button" class="btn" data-no>Keep</button>
                <button type="button" class="btn btn-danger-solid" data-yes>${esc(confirmLabel)}</button>
            </div>`);

        let settled = false;
        const finish = answer => {
            if (settled) return;
            settled = true;
            modal.removeEventListener('close', onClose);
            closeModal();
            resolve(answer);
        };
        const onClose = () => finish(false);
        modal.addEventListener('close', onClose);
        body.querySelector('[data-no]').addEventListener('click', () => finish(false));
        body.querySelector('[data-yes]').addEventListener('click', () => finish(true));
    });
}

// runs a mutation, reports the result and re-renders the current page
async function run(action, successMessage) {
    try {
        await action();
        if (successMessage) toast(successMessage);
        await refresh();
    } catch (err) {
        toast(err.message, 'error');
    }
}

function setActions(actions) {
    const bar = $('#page-actions');
    bar.innerHTML = '';
    for (const a of actions) {
        const btn = document.createElement('button');
        btn.className = 'btn' + (a.primary ? ' btn-primary' : '');
        btn.textContent = a.label;
        btn.addEventListener('click', a.onClick);
        bar.append(btn);
    }
}

const statusBadge = status => `<span class="badge badge-${status}">${esc(status.charAt(0) + status.slice(1).toLowerCase())}</span>`;
const emptyRow = (cols, text) => `<tr><td colspan="${cols}" class="empty">${esc(text)}</td></tr>`;

/* =========================================================
   Shared forms (used from several pages)
   ========================================================= */

async function openPatientForm(patient) {
    const fields = [
        { name: 'name', label: 'Full name', required: true, maxlength: 20 },
        { name: 'email', label: 'Email', type: 'email', required: true },
        {
            name: 'gender', label: 'Gender', type: 'select', placeholder: 'Select…',
            options: ['Male', 'Female', 'Other'].map(g => ({ value: g, label: g }))
        },
        { name: 'birthDate', label: 'Birth date', type: 'date', max: toInputDate(new Date()) },
        {
            name: 'bloodGroup', label: 'Blood group', type: 'select', placeholder: 'Unknown', full: true,
            options: Object.entries(BLOOD_GROUPS).map(([value, label]) => ({ value, label }))
        }
    ];

    openForm({
        title: patient ? 'Edit patient' : 'Register patient',
        fields,
        values: patient || {},
        submitLabel: patient ? 'Save changes' : 'Register',
        onSubmit: async data => {
            if (patient) await api.put(`/patients/${patient.id}`, data);
            else await api.post('/patients', data);
            toast(patient ? 'Patient updated' : 'Patient registered');
            await refresh();
        }
    });
}

async function openDoctorForm(doctor) {
    openForm({
        title: doctor ? 'Edit doctor' : 'Add doctor',
        fields: [
            { name: 'name', label: 'Full name', required: true, maxlength: 100, placeholder: 'Dr. …' },
            { name: 'specialization', label: 'Specialization', maxlength: 100 },
            { name: 'email', label: 'Email', type: 'email', required: true, full: true }
        ],
        values: doctor || {},
        submitLabel: doctor ? 'Save changes' : 'Add doctor',
        onSubmit: async data => {
            if (doctor) await api.put(`/doctors/${doctor.id}`, data);
            else await api.post('/doctors', data);
            toast(doctor ? 'Doctor updated' : 'Doctor added');
            await refresh();
        }
    });
}

async function openAppointmentForm(prefill = {}) {
    let patients, doctors;
    try {
        [patients, doctors] = await Promise.all([loadAllPatients(), loadDoctors()]);
    } catch (err) {
        toast(err.message, 'error');
        return;
    }

    const soon = new Date();
    soon.setDate(soon.getDate() + 1);
    soon.setHours(10, 0, 0, 0);

    openForm({
        title: 'Book appointment',
        note: 'A doctor can\'t have two scheduled appointments within 30 minutes of each other.',
        fields: [
            {
                name: 'patientId', label: 'Patient', type: 'select', required: true, number: true, full: true,
                placeholder: 'Select patient…',
                options: patients.map(p => ({ value: p.id, label: `${p.name} — ${p.email}` }))
            },
            {
                name: 'doctorId', label: 'Doctor', type: 'select', required: true, number: true, full: true,
                placeholder: 'Select doctor…',
                options: doctors.map(d => ({ value: d.id, label: `${d.name}${d.specialization ? ' — ' + d.specialization : ''}` }))
            },
            {
                name: 'appointmentTime', label: 'Date & time', type: 'datetime-local', required: true, full: true,
                min: toInputDateTime(new Date())
            },
            { name: 'reason', label: 'Reason', type: 'textarea', maxlength: 500, full: true }
        ],
        values: { appointmentTime: toInputDateTime(soon), ...prefill },
        submitLabel: 'Book',
        onSubmit: async data => {
            await api.post('/appointments', data);
            toast('Appointment booked');
            await refresh();
        }
    });
}

async function reassignAppointment(appointment) {
    const doctors = (await loadDoctors()).filter(d => d.id !== appointment.doctorId);
    openForm({
        title: 'Reassign appointment',
        note: `Currently with <strong>${esc(appointment.doctorName)}</strong> on ${esc(fmtDateTime(appointment.appointmentTime))}.`,
        fields: [{
            name: 'doctorId', label: 'New doctor', type: 'select', required: true, number: true, full: true,
            placeholder: 'Select doctor…',
            options: doctors.map(d => ({ value: d.id, label: `${d.name}${d.specialization ? ' — ' + d.specialization : ''}` }))
        }],
        submitLabel: 'Reassign',
        onSubmit: async data => {
            await api.put(`/appointments/${appointment.id}/doctor/${data.doctorId}`);
            toast('Appointment reassigned');
            await refresh();
        }
    });
}

function appointmentActions(a) {
    if (a.status !== 'SCHEDULED') return '';
    return `
        <button class="btn btn-sm" data-act="reassign" data-id="${a.id}">Reassign</button>
        <button class="btn btn-sm" data-act="complete" data-id="${a.id}">Complete</button>
        <button class="btn btn-sm btn-danger" data-act="cancel" data-id="${a.id}">Cancel</button>`;
}

// handles the Reassign / Complete / Cancel buttons rendered by appointmentActions()
async function handleAppointmentAction(button, appointments) {
    const appointment = appointments.find(a => a.id === Number(button.dataset.id));
    if (!appointment) return;

    switch (button.dataset.act) {
        case 'reassign':
            await reassignAppointment(appointment);
            break;
        case 'complete':
            await run(() => api.patch(`/appointments/${appointment.id}/complete`), 'Appointment marked as completed');
            closeModal();
            break;
        case 'cancel':
            if (await confirmAction('Cancel appointment',
                `Cancel ${appointment.patientName}'s appointment with ${appointment.doctorName} on ${fmtDateTime(appointment.appointmentTime)}?`,
                'Cancel appointment')) {
                await run(() => api.patch(`/appointments/${appointment.id}/cancel`), 'Appointment cancelled');
            }
            break;
    }
}

/* =========================================================
   Detail views
   ========================================================= */

async function openPatientDetail(id) {
    let patient, appointments;
    try {
        [patient, appointments] = await Promise.all([
            api.get(`/patients/${id}`), api.get(`/patients/${id}/appointments`)
        ]);
    } catch (err) {
        toast(err.message, 'error');
        return;
    }

    const ins = patient.insurance;
    const expired = ins && parseDate(ins.validUntil) < new Date(new Date().toDateString());
    const insuranceFields = [
        { name: 'policyNumber', label: 'Policy number', required: true, maxlength: 50 },
        { name: 'provider', label: 'Provider', required: true, maxlength: 100 },
        { name: 'validUntil', label: 'Valid until', type: 'date', required: true, min: toInputDate(new Date()), full: true }
    ];

    const body = openModal(patient.name, `
        <dl class="details">
            <div><dt>Email</dt><dd>${esc(patient.email)}</dd></div>
            <div><dt>Gender</dt><dd>${esc(patient.gender || '—')}</dd></div>
            <div><dt>Birth date</dt><dd>${fmtDate(patient.birthDate)}</dd></div>
            <div><dt>Blood group</dt><dd>${patient.bloodGroup ? `<span class="badge badge-blood">${bloodLabel(patient.bloodGroup)}</span>` : 'Unknown'}</dd></div>
            <div><dt>Registered</dt><dd>${fmtDate(patient.createdAt?.slice(0, 10))}</dd></div>
        </dl>

        <div class="section-title">Insurance</div>
        <div class="subcard">
            ${ins ? `
                <dl class="details" style="margin-bottom:14px">
                    <div><dt>Policy</dt><dd>${esc(ins.policyNumber)}</dd></div>
                    <div><dt>Provider</dt><dd>${esc(ins.provider)}</dd></div>
                    <div><dt>Valid until</dt><dd>${fmtDate(ins.validUntil)}
                        ${expired ? '<span class="badge badge-CANCELLED">Expired</span>' : '<span class="badge badge-ok">Active</span>'}</dd></div>
                </dl>` : '<p class="muted" style="margin-top:0">No insurance on file.</p>'}
            <form class="form" id="insurance-form" novalidate>
                ${insuranceFields.map(f => fieldHtml(f, ins?.[f.name])).join('')}
                <div class="form-actions">
                    ${ins ? '<button type="button" class="btn btn-danger" data-remove-insurance>Remove insurance</button>' : ''}
                    <button type="submit" class="btn btn-primary">${ins ? 'Update insurance' : 'Add insurance'}</button>
                </div>
            </form>
        </div>

        <div class="section-title">
            Appointments
            <button class="btn btn-sm btn-primary" data-book>+ Book appointment</button>
        </div>
        <div class="card table-wrap">
            <table>
                <thead><tr><th>Date &amp; time</th><th>Doctor</th><th>Reason</th><th>Status</th><th></th></tr></thead>
                <tbody>
                    ${appointments.length ? appointments.map(a => `
                        <tr>
                            <td>${fmtDateTime(a.appointmentTime)}</td>
                            <td>${esc(a.doctorName)}</td>
                            <td>${esc(a.reason || '—')}</td>
                            <td>${statusBadge(a.status)}</td>
                            <td class="actions">${appointmentActions(a)}</td>
                        </tr>`).join('') : emptyRow(5, 'No appointments yet.')}
                </tbody>
            </table>
        </div>`, { wide: true });

    bindForm($('#insurance-form', body), insuranceFields, async data => {
        await api.put(`/patients/${id}/insurance`, data);
        toast(ins ? 'Insurance updated' : 'Insurance added');
        await refresh();
        await openPatientDetail(id);
    });

    body.querySelector('[data-remove-insurance]')?.addEventListener('click', async () => {
        if (await confirmAction('Remove insurance', `Remove policy ${ins.policyNumber} from ${patient.name}?`, 'Remove')) {
            await run(() => api.del(`/patients/${id}/insurance`), 'Insurance removed');
        }
        await openPatientDetail(id);
    });

    body.querySelector('[data-book]').addEventListener('click', () => openAppointmentForm({ patientId: id }));
    body.querySelector('tbody').addEventListener('click', e => {
        const button = e.target.closest('[data-act]');
        if (button) handleAppointmentAction(button, appointments);
    });
}

async function openDoctorDetail(id) {
    let doctor, departments, appointments;
    try {
        [doctor, departments, appointments] = await Promise.all([
            api.get(`/doctors/${id}`), api.get(`/doctors/${id}/departments`), api.get(`/doctors/${id}/appointments`)
        ]);
    } catch (err) {
        toast(err.message, 'error');
        return;
    }

    const body = openModal(doctor.name, `
        <dl class="details">
            <div><dt>Specialization</dt><dd>${esc(doctor.specialization || '—')}</dd></div>
            <div><dt>Email</dt><dd>${esc(doctor.email)}</dd></div>
            <div><dt>Joined</dt><dd>${fmtDate(doctor.createdAt?.slice(0, 10))}</dd></div>
        </dl>

        <div class="section-title">Departments</div>
        <div class="chips">
            ${departments.length ? departments.map(d => `
                <span class="badge ${d.headDoctor.id === id ? 'badge-head' : ''}">
                    ${esc(d.name)}${d.headDoctor.id === id ? ' · Head' : ''}
                </span>`).join('') : '<span class="muted">Not assigned to any department.</span>'}
        </div>

        <div class="section-title">
            Appointments
            <button class="btn btn-sm btn-primary" data-book>+ Book appointment</button>
        </div>
        <div class="card table-wrap">
            <table>
                <thead><tr><th>Date &amp; time</th><th>Patient</th><th>Reason</th><th>Status</th><th></th></tr></thead>
                <tbody>
                    ${appointments.length ? appointments.map(a => `
                        <tr>
                            <td>${fmtDateTime(a.appointmentTime)}</td>
                            <td>${esc(a.patientName)}</td>
                            <td>${esc(a.reason || '—')}</td>
                            <td>${statusBadge(a.status)}</td>
                            <td class="actions">${appointmentActions(a)}</td>
                        </tr>`).join('') : emptyRow(5, 'No appointments yet.')}
                </tbody>
            </table>
        </div>`, { wide: true });

    body.querySelector('[data-book]').addEventListener('click', () => openAppointmentForm({ doctorId: id }));
    body.querySelector('tbody').addEventListener('click', e => {
        const button = e.target.closest('[data-act]');
        if (button) handleAppointmentAction(button, appointments);
    });
}

/* =========================================================
   Pages
   ========================================================= */

const state = {
    patients: { page: 0, size: 10, search: '' },
    doctors: { search: '', specialization: '' },
    appointments: { status: '', search: '' }
};

function statCard(label, value, note) {
    return `
        <div class="card card-pad">
            <div class="stat-label">${esc(label)}</div>
            <div class="stat-value">${esc(value)}</div>
            <div class="stat-note">${esc(note)}</div>
        </div>`;
}

const views = {
    dashboard: {
        title: 'Dashboard',
        subtitle: 'Overview of the hospital today',
        async render(el) {
            const [patients, doctors, departments, appointments, bloodGroups] = await Promise.all([
                api.get('/patients?size=1'), loadDoctors(), api.get('/departments'),
                api.get('/appointments?status=SCHEDULED'), api.get('/patients/blood-groups')
            ]);

            const now = new Date();
            const upcoming = appointments.filter(a => parseDate(a.appointmentTime) >= now);
            const today = upcoming.filter(a => isSameDay(parseDate(a.appointmentTime), now)).length;
            const specializations = new Set(doctors.map(d => d.specialization).filter(Boolean)).size;
            const maxCount = Math.max(1, ...bloodGroups.map(b => b.count));

            el.innerHTML = `
                <div class="grid grid-stats">
                    ${statCard('Patients', patients.page.totalElements, 'registered in total')}
                    ${statCard('Doctors', doctors.length, `${specializations} specializations`)}
                    ${statCard('Departments', departments.length, `${departments.reduce((n, d) => n + d.doctors.length, 0)} doctor assignments`)}
                    ${statCard('Upcoming appointments', upcoming.length, `${today} today`)}
                </div>

                <div class="grid grid-2" style="margin-top:16px">
                    <div class="card card-pad">
                        <div class="card-title">Next appointments</div>
                        ${upcoming.length ? `<ul class="list">${upcoming.slice(0, 6).map(a => `
                            <li>
                                <div>
                                    <div style="font-weight:600">${esc(a.patientName)}</div>
                                    <div class="muted" style="font-size:13px">${esc(a.doctorName)}${a.reason ? ' · ' + esc(a.reason) : ''}</div>
                                </div>
                                <div style="text-align:right; white-space:nowrap">${fmtDateTime(a.appointmentTime)}</div>
                            </li>`).join('')}</ul>`
                            : '<div class="empty">No upcoming appointments.</div>'}
                    </div>

                    <div class="card card-pad">
                        <div class="card-title">Patients by blood group</div>
                        ${[...bloodGroups].sort((a, b) => b.count - a.count).map(b => `
                            <div class="bar-row">
                                <span>${b.bloodGroup ? `<span class="badge badge-blood">${bloodLabel(b.bloodGroup)}</span>` : '<span class="muted">Unknown</span>'}</span>
                                <div class="bar-track"><div class="bar-fill" style="width:${(b.count / maxCount) * 100}%"></div></div>
                                <span class="bar-count">${b.count}</span>
                            </div>`).join('') || '<div class="empty">No patients yet.</div>'}
                    </div>
                </div>`;

            setActions([{ label: '+ Book appointment', primary: true, onClick: () => openAppointmentForm() }]);
        }
    },

    patients: {
        title: 'Patients',
        subtitle: 'Register patients, manage their insurance and appointments',
        async render(el) {
            const patients = await loadAllPatients();
            const s = state.patients;

            el.innerHTML = `
                <div class="toolbar">
                    <input type="search" id="patient-search" placeholder="Search by name or email…" value="${esc(s.search)}">
                </div>
                <div class="card">
                    <div class="table-wrap">
                        <table>
                            <thead><tr>
                                <th>Name</th><th>Gender</th><th>Birth date</th><th>Email</th>
                                <th>Blood</th><th>Insurance</th><th></th>
                            </tr></thead>
                            <tbody id="patient-rows"></tbody>
                        </table>
                    </div>
                    <div class="pager" id="patient-pager"></div>
                </div>`;

            const draw = () => {
                const term = s.search.toLowerCase();
                const filtered = patients.filter(p =>
                    p.name.toLowerCase().includes(term) || p.email.toLowerCase().includes(term));
                const totalPages = Math.max(1, Math.ceil(filtered.length / s.size));
                s.page = Math.min(s.page, totalPages - 1);
                const rows = filtered.slice(s.page * s.size, (s.page + 1) * s.size);

                $('#patient-rows').innerHTML = rows.length ? rows.map(p => `
                    <tr>
                        <td><button class="link" data-act="view" data-id="${p.id}">${esc(p.name)}</button></td>
                        <td>${esc(p.gender || '—')}</td>
                        <td>${fmtDate(p.birthDate)}</td>
                        <td>${esc(p.email)}</td>
                        <td>${p.bloodGroup ? `<span class="badge badge-blood">${bloodLabel(p.bloodGroup)}</span>` : '<span class="muted">—</span>'}</td>
                        <td>${p.insurance ? esc(p.insurance.provider) : '<span class="muted">None</span>'}</td>
                        <td class="actions">
                            <button class="btn btn-sm" data-act="edit" data-id="${p.id}">Edit</button>
                            <button class="btn btn-sm btn-danger" data-act="delete" data-id="${p.id}">Delete</button>
                        </td>
                    </tr>`).join('') : emptyRow(7, term ? 'No patients match your search.' : 'No patients registered yet.');

                $('#patient-pager').innerHTML = `
                    <span class="muted">${filtered.length} patient${filtered.length === 1 ? '' : 's'}</span>
                    <div style="display:flex; gap:8px; align-items:center">
                        <button class="btn btn-sm" data-page="-1" ${s.page === 0 ? 'disabled' : ''}>Previous</button>
                        <span>Page ${s.page + 1} of ${totalPages}</span>
                        <button class="btn btn-sm" data-page="1" ${s.page >= totalPages - 1 ? 'disabled' : ''}>Next</button>
                    </div>`;
            };
            draw();

            $('#patient-search').addEventListener('input', e => {
                s.search = e.target.value;
                s.page = 0;
                draw();
            });
            $('#patient-pager').addEventListener('click', e => {
                const button = e.target.closest('[data-page]');
                if (!button) return;
                s.page += Number(button.dataset.page);
                draw();
            });
            $('#patient-rows').addEventListener('click', async e => {
                const button = e.target.closest('[data-act]');
                if (!button) return;
                const patient = patients.find(p => p.id === Number(button.dataset.id));

                if (button.dataset.act === 'view') openPatientDetail(patient.id);
                if (button.dataset.act === 'edit') openPatientForm(patient);
                if (button.dataset.act === 'delete' && await confirmAction('Delete patient',
                    `Delete ${patient.name}? Their insurance and all their appointments will be deleted too.`)) {
                    await run(() => api.del(`/patients/${patient.id}`), 'Patient deleted');
                }
            });

            setActions([{ label: '+ Register patient', primary: true, onClick: () => openPatientForm() }]);
        }
    },

    doctors: {
        title: 'Doctors',
        subtitle: 'Medical staff, their departments and schedules',
        async render(el) {
            const doctors = await loadDoctors();
            const s = state.doctors;
            const specializations = [...new Set(doctors.map(d => d.specialization).filter(Boolean))].sort();

            el.innerHTML = `
                <div class="toolbar">
                    <input type="search" id="doctor-search" placeholder="Search by name or email…" value="${esc(s.search)}">
                    <select id="doctor-spec">
                        <option value="">All specializations</option>
                        ${specializations.map(sp => `<option ${sp === s.specialization ? 'selected' : ''}>${esc(sp)}</option>`).join('')}
                    </select>
                </div>
                <div class="card table-wrap">
                    <table>
                        <thead><tr><th>Name</th><th>Specialization</th><th>Email</th><th>Joined</th><th></th></tr></thead>
                        <tbody id="doctor-rows"></tbody>
                    </table>
                </div>`;

            const draw = () => {
                const term = s.search.toLowerCase();
                const rows = doctors.filter(d =>
                    (d.name.toLowerCase().includes(term) || d.email.toLowerCase().includes(term)) &&
                    (!s.specialization || d.specialization === s.specialization));

                $('#doctor-rows').innerHTML = rows.length ? rows.map(d => `
                    <tr>
                        <td><button class="link" data-act="view" data-id="${d.id}">${esc(d.name)}</button></td>
                        <td>${esc(d.specialization || '—')}</td>
                        <td>${esc(d.email)}</td>
                        <td>${fmtDate(d.createdAt?.slice(0, 10))}</td>
                        <td class="actions">
                            <button class="btn btn-sm" data-act="edit" data-id="${d.id}">Edit</button>
                            <button class="btn btn-sm btn-danger" data-act="delete" data-id="${d.id}">Delete</button>
                        </td>
                    </tr>`).join('') : emptyRow(5, 'No doctors found.');
            };
            draw();

            $('#doctor-search').addEventListener('input', e => { s.search = e.target.value; draw(); });
            $('#doctor-spec').addEventListener('change', e => { s.specialization = e.target.value; draw(); });
            $('#doctor-rows').addEventListener('click', async e => {
                const button = e.target.closest('[data-act]');
                if (!button) return;
                const doctor = doctors.find(d => d.id === Number(button.dataset.id));

                if (button.dataset.act === 'view') openDoctorDetail(doctor.id);
                if (button.dataset.act === 'edit') openDoctorForm(doctor);
                if (button.dataset.act === 'delete' && await confirmAction('Delete doctor', `Delete ${doctor.name}?`)) {
                    await run(() => api.del(`/doctors/${doctor.id}`), 'Doctor deleted');
                }
            });

            setActions([{ label: '+ Add doctor', primary: true, onClick: () => openDoctorForm() }]);
        }
    },

    departments: {
        title: 'Departments',
        subtitle: 'Department heads and assigned doctors',
        async render(el) {
            const [departments, doctors] = await Promise.all([api.get('/departments'), loadDoctors()]);
            const headIds = new Set(departments.map(d => d.headDoctor.id));

            el.innerHTML = departments.length ? `<div class="grid grid-cards">${departments.map(dep => {
                const memberIds = new Set(dep.doctors.map(d => d.id));
                const available = doctors.filter(d => !memberIds.has(d.id));
                return `
                    <div class="card dept-card">
                        <div class="card-pad">
                            <div class="dept-head">
                                <div>
                                    <h3>${esc(dep.name)}</h3>
                                    <div class="muted" style="font-size:13px">${dep.doctors.length} doctor${dep.doctors.length === 1 ? '' : 's'}</div>
                                </div>
                                <button class="btn btn-sm btn-danger" data-act="delete" data-dept="${dep.id}">Delete</button>
                            </div>
                            ${dep.doctors.map(doc => `
                                <div class="member">
                                    <div>
                                        <div class="member-name">${esc(doc.name)}</div>
                                        <div class="member-meta">${esc(doc.specialization || '—')}</div>
                                    </div>
                                    ${doc.id === dep.headDoctor.id
                                        ? '<span class="badge badge-head">Head</span>'
                                        : `<div style="white-space:nowrap">
                                               <button class="btn btn-sm" data-act="head" data-dept="${dep.id}" data-doc="${doc.id}"
                                                   ${headIds.has(doc.id) ? 'disabled title="Already heads another department"' : ''}>Make head</button>
                                               <button class="btn btn-sm btn-danger" data-act="remove" data-dept="${dep.id}" data-doc="${doc.id}" aria-label="Remove">&times;</button>
                                           </div>`}
                                </div>`).join('')}
                        </div>
                        <div class="dept-footer">
                            <select data-add-select="${dep.id}" ${available.length ? '' : 'disabled'}>
                                ${available.length
                                    ? `<option value="">Assign a doctor…</option>${available.map(d => `<option value="${d.id}">${esc(d.name)}</option>`).join('')}`
                                    : '<option>All doctors assigned</option>'}
                            </select>
                            <button class="btn btn-sm btn-primary" data-act="add" data-dept="${dep.id}" ${available.length ? '' : 'disabled'}>Assign</button>
                        </div>
                    </div>`;
            }).join('')}</div>` : '<div class="card empty">No departments yet. Create one to get started.</div>';

            el.onclick = async e => {
                const button = e.target.closest('[data-act]');
                if (!button) return;
                const dep = departments.find(d => d.id === Number(button.dataset.dept));
                const doctorId = Number(button.dataset.doc);

                switch (button.dataset.act) {
                    case 'add': {
                        const selected = el.querySelector(`[data-add-select="${dep.id}"]`).value;
                        if (!selected) { toast('Choose a doctor to assign', 'error'); return; }
                        await run(() => api.post(`/departments/${dep.id}/doctors/${selected}`), 'Doctor assigned');
                        break;
                    }
                    case 'remove':
                        await run(() => api.del(`/departments/${dep.id}/doctors/${doctorId}`), 'Doctor removed from department');
                        break;
                    case 'head':
                        await run(() => api.put(`/departments/${dep.id}/head/${doctorId}`), 'Department head changed');
                        break;
                    case 'delete':
                        if (await confirmAction('Delete department', `Delete the ${dep.name} department? Doctors will stay in the system.`)) {
                            await run(() => api.del(`/departments/${dep.id}`), 'Department deleted');
                        }
                        break;
                }
            };

            setActions([{
                label: '+ New department', primary: true, onClick: () => {
                    const candidates = doctors.filter(d => !headIds.has(d.id));
                    if (!candidates.length) {
                        toast('Every doctor already heads a department. Add a new doctor first.', 'error');
                        return;
                    }
                    openForm({
                        title: 'New department',
                        note: 'Each department needs a head. A doctor can head only one department.',
                        fields: [
                            { name: 'name', label: 'Department name', required: true, maxlength: 100, full: true },
                            {
                                name: 'headDoctorId', label: 'Head doctor', type: 'select', required: true, number: true, full: true,
                                placeholder: 'Select doctor…',
                                options: candidates.map(d => ({ value: d.id, label: `${d.name}${d.specialization ? ' — ' + d.specialization : ''}` }))
                            }
                        ],
                        submitLabel: 'Create',
                        onSubmit: async data => {
                            await api.post('/departments', data);
                            toast('Department created');
                            await refresh();
                        }
                    });
                }
            }]);
        }
    },

    appointments: {
        title: 'Appointments',
        subtitle: 'Book, reassign, complete or cancel appointments',
        async render(el) {
            const s = state.appointments;
            const appointments = await api.get('/appointments' + (s.status ? `?status=${s.status}` : ''));
            const filters = [['', 'All'], ['SCHEDULED', 'Scheduled'], ['COMPLETED', 'Completed'], ['CANCELLED', 'Cancelled']];

            el.innerHTML = `
                <div class="toolbar">
                    <div class="chips">
                        ${filters.map(([value, label]) =>
                            `<button class="chip ${s.status === value ? 'active' : ''}" data-status="${value}">${label}</button>`).join('')}
                    </div>
                    <span class="spacer"></span>
                    <input type="search" id="appt-search" placeholder="Search patient or doctor…" value="${esc(s.search)}">
                </div>
                <div class="card table-wrap">
                    <table>
                        <thead><tr><th>Date &amp; time</th><th>Patient</th><th>Doctor</th><th>Reason</th><th>Status</th><th></th></tr></thead>
                        <tbody id="appt-rows"></tbody>
                    </table>
                </div>`;

            const draw = () => {
                const term = s.search.toLowerCase();
                const rows = appointments.filter(a =>
                    a.patientName.toLowerCase().includes(term) || a.doctorName.toLowerCase().includes(term));

                $('#appt-rows').innerHTML = rows.length ? rows.map(a => `
                    <tr>
                        <td style="white-space:nowrap">${fmtDateTime(a.appointmentTime)}</td>
                        <td><button class="link" data-act="patient" data-id="${a.patientId}">${esc(a.patientName)}</button></td>
                        <td><button class="link" data-act="doctor" data-id="${a.doctorId}">${esc(a.doctorName)}</button></td>
                        <td>${esc(a.reason || '—')}</td>
                        <td>${statusBadge(a.status)}</td>
                        <td class="actions">${appointmentActions(a)}</td>
                    </tr>`).join('') : emptyRow(6, 'No appointments found.');
            };
            draw();

            el.querySelector('.chips').addEventListener('click', e => {
                const chip = e.target.closest('[data-status]');
                if (!chip) return;
                s.status = chip.dataset.status;
                refresh();
            });
            $('#appt-search').addEventListener('input', e => { s.search = e.target.value; draw(); });
            $('#appt-rows').addEventListener('click', e => {
                const button = e.target.closest('[data-act]');
                if (!button) return;
                if (button.dataset.act === 'patient') openPatientDetail(Number(button.dataset.id));
                else if (button.dataset.act === 'doctor') openDoctorDetail(Number(button.dataset.id));
                else handleAppointmentAction(button, appointments);
            });

            setActions([{ label: '+ Book appointment', primary: true, onClick: () => openAppointmentForm() }]);
        }
    }
};

/* =========================================================
   Router
   ========================================================= */

let currentView = 'dashboard';

async function refresh() {
    const view = views[currentView];
    $('#page-title').textContent = view.title;
    $('#page-subtitle').textContent = view.subtitle;
    document.title = `${view.title} · CarePoint`;

    const el = $('#view');
    el.onclick = null;
    try {
        await view.render(el);
    } catch (err) {
        el.innerHTML = `<div class="card empty">Could not load data: ${esc(err.message)}</div>`;
    }
}

function route() {
    const name = location.hash.replace('#/', '');
    currentView = views[name] ? name : 'dashboard';
    document.querySelectorAll('.nav a').forEach(a =>
        a.classList.toggle('active', a.dataset.view === currentView));
    $('#view').innerHTML = '<div class="loading">Loading…</div>';
    $('#page-actions').innerHTML = '';
    refresh();
}

window.addEventListener('hashchange', route);
route();
