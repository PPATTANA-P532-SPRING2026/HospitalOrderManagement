const API_BASE = 'https://hospitalordermanagement-1.onrender.com';
const REFRESH_INTERVAL = 3000;

// ── on page load
window.onload = function () {
    fetchStrategy();
    fetchStaff();
    fetchBadge();
    refreshAll();
    setInterval(refreshAll, REFRESH_INTERVAL);
};

// ── refresh everything
function refreshAll() {
    fetchOrders();
    fetchAuditLog();
    fetchStaff();
    fetchBadge();
}

// ── fetch all orders
function fetchOrders() {
    fetch(`${API_BASE}/api/orders`)
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById('ordersTable');
            tbody.innerHTML = '';

            if (data.length === 0) {
                tbody.innerHTML =
                    '<tr><td colspan="8">No orders yet</td></tr>';
                return;
            }

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
                    <td>${new Date(order.timestamp)
                            .toLocaleTimeString()}</td>
                    <td>${buildActions(order)}</td>
                `;
                tbody.appendChild(row);
            });

            document.getElementById('lastUpdated').textContent =
                'Last updated: '
                + new Date().toLocaleTimeString();
        })
        .catch(err =>
            console.error('Error fetching orders:', err));
}

// ── build action buttons
function buildActions(order) {
    if (order.status === 'PENDING') {
        return `
            <button class="btn-claim"
                onclick="claimOrder('${order.id}')">
                Claim
            </button>
            <button class="btn-cancel"
                onclick="cancelOrder('${order.id}')">
                Cancel
            </button>
        `;
    }
    if (order.status === 'IN_PROGRESS') {
        return `
            <button class="btn-complete"
                onclick="completeOrder('${order.id}')">
                Complete
            </button>
        `;
    }
    return '—';
}

// ── submit order
function submitOrder() {
    const body = {
        type:        document.getElementById('orderType').value,
        patientName: document.getElementById('patientName').value,
        clinician:   document.getElementById('clinician').value,
        description: document.getElementById('description').value,
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

// ── claim order
function claimOrder(orderId) {
    const staffName =
        document.getElementById('staffName').value;
    if (!staffName) {
        showMessage('claimMessage',
            'Please enter staff name before claiming', 'error');
        return;
    }

    fetch(`${API_BASE}/api/orders/${orderId}/claim`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ claimedBy: staffName })
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

// ── complete order
function completeOrder(orderId) {
    const staffName =
        document.getElementById('staffName').value;
    if (!staffName) {
        showMessage('claimMessage',
            'Please enter staff name before completing', 'error');
        return;
    }

    fetch(`${API_BASE}/api/orders/${orderId}/complete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ actor: staffName })
    })
    .then(res => {
        if (res.ok) {
            showMessage('claimMessage',
                'Order completed successfully', 'success');
            refreshAll();
        } else {
            res.text().then(err =>
                showMessage('claimMessage', err, 'error'));
        }
    })
    .catch(err =>
        console.error('Error completing order:', err));
}

// ── cancel order
function cancelOrder(orderId) {
    const staffName =
        document.getElementById('staffName').value || 'System';

    fetch(`${API_BASE}/api/orders/${orderId}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ actor: staffName })
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

// ── fetch audit log
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

// ── undo last command
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
        .catch(err => console.error('Error undoing:', err));
}

// ── replay command
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

// ── fetch strategy
function fetchStrategy() {
    fetch(`${API_BASE}/api/strategy`)
        .then(res => res.json())
        .then(data => {
            const select =
                document.getElementById('strategySelect');
            const name = data.strategy;
            if (name.includes('LoadBalancing'))
                select.value = 'loadBalancing';
            else if (name.includes('Deadline'))
                select.value = 'deadlineFirst';
            else
                select.value = 'priorityFirst';
        })
        .catch(err =>
            console.error('Error fetching strategy:', err));
}

// ── change strategy
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
    })
    .catch(err =>
        console.error('Error changing strategy:', err));
}

// ── update notification channels
function updateChannels() {
    const channels = ['console']; // console always on
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

// ── fetch badge count
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

// ── reset badge
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

// ── register staff
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

// ── fetch staff list
function fetchStaff() {
    fetch(`${API_BASE}/api/staff`)
        .then(res => res.json())
        .then(data => {
            const tbody =
                document.getElementById('staffTable');
            tbody.innerHTML = '';

            if (data.length === 0) {
                tbody.innerHTML =
                    '<tr><td colspan="2">No staff registered</td></tr>';
                return;
            }

            data.forEach(member => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${member.name}</td>
                    <td>${member.role}</td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(err =>
            console.error('Error fetching staff:', err));
}

// ── clear form
function clearForm() {
    document.getElementById('patientName').value = '';
    document.getElementById('clinician').value   = '';
    document.getElementById('description').value = '';
}

// ── show message helper
function showMessage(elementId, message, type) {
    const el = document.getElementById(elementId);
    el.textContent = message;
    el.className   = type;
    setTimeout(() => { el.textContent = ''; }, 4000);
}