const API_BASE = 'https://hospitalordermanagement-1.onrender.com';
const REFRESH_INTERVAL = 3000;

// ── on page load ──────────────────────────────────────────────────────
window.onload = function () {
    fetchStrategy();
    fetchStaff();
    fetchClinicians();
    fetchBadge();
    refreshAll();
    setInterval(refreshAll, REFRESH_INTERVAL);
};

// ── refresh everything ────────────────────────────────────────────────
function refreshAll() {
    fetchOrders();
    fetchAuditLog();
    fetchStaff();
    fetchClinicians();
    fetchBadge();
}

// ── fetch all orders ──────────────────────────────────────────────────
function fetchOrders() {
    fetch(`${API_BASE}/api/orders`)
        .then(res => res.json())
        .then(data => {
            const tbody =
                document.getElementById('ordersTable');
            tbody.innerHTML = '';

            if (data.length === 0) {
                tbody.innerHTML =
                    '<tr><td colspan="9">No orders yet</td></tr>';
                return;
            }

            // DO NOT sort client side
            // trust the order returned by the backend
            // which is already sorted by triage strategy
            data.forEach(order => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${order.type}</td>
                    <td>${order.patientName}</td>
                    <td>${order.clinician}</td>
                    <td>${order.description}</td>
                    <td class="priority-${order.priority
                            .toLowerCase()}">
                        ${order.priority}
                    </td>
                    <td class="status-${order.status
                            .toLowerCase().replace('_', '')}">
                        ${order.status.replace('_', ' ')}
                    </td>
                    <td>${order.claimedBy || '—'}</td>
                    <td>${new Date(order.timestamp)
                            .toLocaleTimeString()}</td>
                    <td>${buildActions(order)}</td>
                `;
                tbody.appendChild(row);
            });

            document.getElementById('lastUpdated')
                    .textContent = 'Last updated: '
                    + new Date().toLocaleTimeString();
        })
        .catch(err =>
            console.error('Error fetching orders:', err));
}

// ── build action buttons ──────────────────────────────────────────────
function buildActions(order) {
    if (order.status === 'PENDING') {
        return `
            <button class="btn-claim"
                onclick="claimOrder('${order.id}')">
                Claim
            </button>
            <button class="btn-cancel"
                onclick="cancelOrder('${order.id}',
                    '${order.clinician}')">
                Cancel
            </button>
        `;
    }
    if (order.status === 'IN_PROGRESS') {
        return `
            <button class="btn-complete"
                onclick="completeOrder('${order.id}',
                    '${order.claimedBy}')">
                Complete
            </button>
        `;
    }
    if (order.status === 'COMPLETED') {
        return `<span style="color:#1E7145;">
                    Done
                </span>`;
    }
    if (order.status === 'CANCELLED') {
        return `<span style="color:#888;">Cancelled</span>`;
    }
    return '—';
}

// ── submit order ──────────────────────────────────────────────────────
function submitOrder() {
    const clinician =
        document.getElementById('clinician').value;

    if (!clinician) {
        showMessage('submitMessage',
            'Please select a clinician', 'error');
        return;
    }

    const patientName =
        document.getElementById('patientName').value;
    const description =
        document.getElementById('description').value;

    if (!patientName) {
        showMessage('submitMessage',
            'Please enter patient name', 'error');
        return;
    }

    if (!description) {
        showMessage('submitMessage',
            'Please enter description', 'error');
        return;
    }

    const body = {
        type:        document.getElementById('orderType').value,
        patientName: patientName,
        clinician:   clinician,
        description: description,
        priority:    document.getElementById('priority').value
    };

    fetch(`${API_BASE}/api/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    .then(res => {
        if (res.ok) {
            showMessage('submitMessage',
                'Order submitted successfully', 'success');
            clearForm();
            refreshAll();
        } else {
            res.text().then(err =>
                showMessage('submitMessage', err, 'error'));
        }
    })
    .catch(err =>
        console.error('Error submitting order:', err));
}

// ── claim order ───────────────────────────────────────────────────────
function claimOrder(orderId) {
    const strategy =
        document.getElementById('strategySelect').value;

    let claimedBy = '';

    if (strategy === 'loadBalancing') {
        // backend assigns automatically
        claimedBy = 'AUTO';
    } else {
        claimedBy =
            document.getElementById('staffName').value;
        if (!claimedBy) {
            showMessage('claimMessage',
                'Please select a staff member', 'error');
            return;
        }
    }

    fetch(`${API_BASE}/api/orders/${orderId}/claim`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ claimedBy })
    })
    .then(res => {
        if (res.ok) {
            showMessage('claimMessage',
                'Order claimed successfully', 'success');
            refreshAll();
        } else {
            res.text().then(err =>
                showMessage('claimMessage', err, 'error'));
        }
    })
    .catch(err =>
        console.error('Error claiming order:', err));
}

// ── complete order ────────────────────────────────────────────────────
// claimedBy passed from buildActions — enforces same staff completes
function completeOrder(orderId, claimedBy) {
    fetch(`${API_BASE}/api/orders/${orderId}/complete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ actor: claimedBy })
    })
    .then(res => {
        if (res.ok) {
            showMessage('claimMessage',
                'Order completed by ' + claimedBy, 'success');
            refreshAll();
        } else {
            res.text().then(err =>
                showMessage('claimMessage', err, 'error'));
        }
    })
    .catch(err =>
        console.error('Error completing order:', err));
}

// ── cancel order ──────────────────────────────────────────────────────
// clinician passed from buildActions — only the submitting clinician
function cancelOrder(orderId, clinician) {
    if (!clinician) {
        showMessage('claimMessage',
            'No clinician found for this order', 'error');
        return;
    }

    fetch(`${API_BASE}/api/orders/${orderId}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ actor: clinician })
    })
    .then(res => {
        if (res.ok) {
            showMessage('claimMessage',
                'Order cancelled successfully', 'success');
            refreshAll();
        } else {
            res.text().then(err =>
                showMessage('claimMessage', err, 'error'));
        }
    })
    .catch(err =>
        console.error('Error cancelling order:', err));
}

// ── fetch audit log ───────────────────────────────────────────────────
function fetchAuditLog() {
    fetch(`${API_BASE}/api/audit`)
        .then(res => res.json())
        .then(data => {
            const tbody =
                document.getElementById('auditTable');
            tbody.innerHTML = '';

            if (data.length === 0) {
                tbody.innerHTML =
                    '<tr><td colspan="5">No actions yet</td></tr>';
                return;
            }

            [...data].reverse().forEach(entry => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${new Date(entry.timestamp)
                            .toLocaleTimeString()}</td>
                    <td>${entry.commandType}</td>
                    <td>${entry.orderId
                            .substring(0, 8)}...</td>
                    <td>${entry.actor}</td>
                    <td>
                        <button class="btn-claim"
                            onclick="replayCommand(
                                '${entry.id}')">
                            Replay
                        </button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(err =>
            console.error('Error fetching audit log:', err));
}

// ── undo last command ─────────────────────────────────────────────────
function undoLast() {
    fetch(`${API_BASE}/api/audit/undo`, { method: 'POST' })
        .then(res => {
            if (res.ok) {
                showMessage('undoMessage',
                    'Last command undone', 'success');
                refreshAll();
            } else {
                res.text().then(err =>
                    showMessage('undoMessage', err, 'error'));
            }
        })
        .catch(err =>
            console.error('Error undoing:', err));
}

// ── replay command ────────────────────────────────────────────────────
function replayCommand(entryId) {
    fetch(`${API_BASE}/api/audit/replay/${entryId}`,
          { method: 'POST' })
        .then(res => {
            if (res.ok) {
                showMessage('undoMessage',
                    'Command replayed successfully', 'success');
                refreshAll();
            } else {
                res.text().then(err =>
                    showMessage('undoMessage', err, 'error'));
            }
        })
        .catch(err =>
            console.error('Error replaying:', err));
}

function changeStrategy() {
    const strategy =
        document.getElementById('strategySelect').value;

    fetch(`${API_BASE}/api/strategy`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ strategy })
    })
    .then(res => res.json())
    .then(() => {
        showMessage('strategyMessage',
            'Strategy changed to ' + strategy, 'success');
        updateStaffDropdownVisibility(strategy);
    })
    .catch(err =>
        console.error('Error changing strategy:', err));
}

function fetchStrategy() {
    fetch(`${API_BASE}/api/strategy`)
        .then(res => res.json())
        .then(data => {
            const select =
                document.getElementById('strategySelect');
            const name = data.strategy;
            let value = 'priorityFirst';
            if (name.includes('LoadBalancing'))
                value = 'loadBalancing';
            else if (name.includes('Deadline'))
                value = 'deadlineFirst';
            select.value = value;
            updateStaffDropdownVisibility(value);
        })
        .catch(err =>
            console.error('Error fetching strategy:', err));
}

function updateStaffDropdownVisibility(strategy) {
    const staffSection =
        document.getElementById('staffSelectSection');
    if (strategy === 'loadBalancing') {
        staffSection.style.display = 'none';
    } else {
        staffSection.style.display = 'block';
    }
}

// ── update notification channels ──────────────────────────────────────
function updateChannels() {
    const channels = ['console'];
    if (document.getElementById('chInApp').checked)
        channels.push('inapp');
    if (document.getElementById('chEmail').checked)
        channels.push('email');

    fetch(`${API_BASE}/api/notifications/channels`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ channels })
    })
    .then(res => res.json())
    .then(() => {
        showMessage('notificationMessage',
            'Channels updated', 'success');
    })
    .catch(err =>
        console.error('Error updating channels:', err));
}

// ── fetch badge count ─────────────────────────────────────────────────
function fetchBadge() {
    fetch(`${API_BASE}/api/notifications/badge`)
        .then(res => res.json())
        .then(data => {
            const badge =
                document.getElementById('badgeBubble');
            if (data.badgeCount > 0) {
                badge.textContent = data.badgeCount;
                badge.style.display = 'inline';
            } else {
                badge.style.display = 'none';
            }
        })
        .catch(err =>
            console.error('Error fetching badge:', err));
}

// ── reset badge ───────────────────────────────────────────────────────
function resetBadge() {
    fetch(`${API_BASE}/api/notifications/badge/reset`,
          { method: 'POST' })
        .then(res => res.json())
        .then(() => {
            document.getElementById('badgeBubble')
                    .style.display = 'none';
            showMessage('notificationMessage',
                'Badge reset', 'success');
        })
        .catch(err =>
            console.error('Error resetting badge:', err));
}

// ── register clinician ────────────────────────────────────────────────
function registerClinician() {
    const name =
        document.getElementById('clinicianNameReg').value;
    const department =
        document.getElementById('clinicianDept').value;

    if (!name) {
        showMessage('clinicianMessage',
            'Please enter clinician name', 'error');
        return;
    }

    fetch(`${API_BASE}/api/clinicians`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, department })
    })
    .then(res => {
        if (res.ok) {
            showMessage('clinicianMessage',
                name + ' registered successfully', 'success');
            document.getElementById('clinicianNameReg')
                    .value = '';
            fetchClinicians();
        } else {
            res.text().then(err =>
                showMessage('clinicianMessage', err, 'error'));
        }
    })
    .catch(err =>
        console.error('Error registering clinician:', err));
}

// ── fetch clinicians ──────────────────────────────────────────────────
function fetchClinicians() {
    fetch(`${API_BASE}/api/clinicians`)
        .then(res => res.json())
        .then(data => {
            // update table
            const tbody =
                document.getElementById('clinicianTable');
            tbody.innerHTML = '';

            if (data.length === 0) {
                tbody.innerHTML =
                    '<tr><td colspan="2">'
                    + 'No clinicians registered</td></tr>';
            } else {
                data.forEach(c => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${c.name}</td>
                        <td>${c.department}</td>
                    `;
                    tbody.appendChild(row);
                });
            }

            // update clinician dropdown for submit
            const clinicianSelect =
                document.getElementById('clinician');
            const currentVal = clinicianSelect.value;
            clinicianSelect.innerHTML =
                '<option value="">'
                + '-- select clinician --</option>';

            data.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.name;
                opt.textContent =
                    c.name + ' (' + c.department + ')';
                clinicianSelect.appendChild(opt);
            });

            if (currentVal) clinicianSelect.value = currentVal;
        })
        .catch(err =>
            console.error('Error fetching clinicians:', err));
}

// ── register staff ────────────────────────────────────────────────────
function registerStaff() {
    const name =
        document.getElementById('staffNameReg').value;
    const role =
        document.getElementById('staffRole').value;

    if (!name) {
        showMessage('staffMessage',
            'Please enter staff name', 'error');
        return;
    }

    fetch(`${API_BASE}/api/staff`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, role })
    })
    .then(res => {
        if (res.ok) {
            showMessage('staffMessage',
                name + ' registered successfully', 'success');
            document.getElementById('staffNameReg').value = '';
            fetchStaff();
        } else {
            res.text().then(err =>
                showMessage('staffMessage', err, 'error'));
        }
    })
    .catch(err =>
        console.error('Error registering staff:', err));
}

// ── fetch staff ───────────────────────────────────────────────────────
function fetchStaff() {
    fetch(`${API_BASE}/api/staff`)
        .then(res => res.json())
        .then(data => {
            // update table
            const tbody =
                document.getElementById('staffTable');
            tbody.innerHTML = '';

            if (data.length === 0) {
                tbody.innerHTML =
                    '<tr><td colspan="2">'
                    + 'No staff registered</td></tr>';
            } else {
                data.forEach(s => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${s.name}</td>
                        <td>${s.role}</td>
                    `;
                    tbody.appendChild(row);
                });
            }

            // update staff dropdown
            const staffSelect =
                document.getElementById('staffName');
            const currentVal = staffSelect.value;
            staffSelect.innerHTML =
                '<option value="">'
                + '-- select staff --</option>';

            data.forEach(s => {
                const opt = document.createElement('option');
                opt.value = s.name;
                opt.textContent = s.name + ' (' + s.role + ')';
                staffSelect.appendChild(opt);
            });

            if (currentVal) staffSelect.value = currentVal;
        })
        .catch(err =>
            console.error('Error fetching staff:', err));
}

// ── clear form ────────────────────────────────────────────────────────
function clearForm() {
    document.getElementById('patientName').value = '';
    document.getElementById('description').value = '';
}

// ── show message helper ───────────────────────────────────────────────
function showMessage(elementId, message, type) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.textContent = message;
    el.className   = type;
    setTimeout(() => { el.textContent = ''; }, 4000);
}

