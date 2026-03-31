const API_BASE = 'https://hospitalordermanagement.onrender.com';
const REFRESH_INTERVAL = 3000;

// ── on page load ──────────────────────────────────────────────────────
window.onload = function () {
    refreshAll();
    setInterval(refreshAll, REFRESH_INTERVAL);
};

// ── refresh everything ────────────────────────────────────────────────
function refreshAll() {
    fetchOrders();
    fetchAuditLog();
}

// ── fetch all orders ──────────────────────────────────────────────────
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

            // sort by priority then timestamp
            const sorted = data.sort((a, b) => {
                const pOrder = { STAT: 2, URGENT: 1, ROUTINE: 0 };
                const pDiff  = pOrder[b.priority] - pOrder[a.priority];
                if (pDiff !== 0) return pDiff;
                return new Date(a.timestamp) - new Date(b.timestamp);
            });

            sorted.forEach(order => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${order.type}</td>
                    <td>${order.patientName}</td>
                    <td>${order.clinician}</td>
                    <td>${order.description}</td>
                    <td class="priority-${order.priority.toLowerCase()}">
                        ${order.priority}
                    </td>
                    <td class="status-${order.status.toLowerCase()}">
                        ${order.status.replace('_', ' ')}
                    </td>
                    <td>${new Date(order.timestamp)
                            .toLocaleTimeString()}</td>
                    <td>${buildActions(order)}</td>
                `;
                tbody.appendChild(row);
            });

            document.getElementById('lastUpdated').textContent =
                'Last updated: ' + new Date().toLocaleTimeString();
        })
        .catch(err => console.error('Error fetching orders:', err));
}

// ── build action buttons per order status ─────────────────────────────
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

// ── submit order ──────────────────────────────────────────────────────
function submitOrder() {
    const type        = document.getElementById('orderType').value;
    const patientName = document.getElementById('patientName').value;
    const clinician   = document.getElementById('clinician').value;
    const description = document.getElementById('description').value;
    const priority    = document.getElementById('priority').value;

    fetch(`${API_BASE}/api/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            type, patientName, clinician, description, priority
        })
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
    .catch(err => console.error('Error submitting order:', err));
}

// ── claim order ───────────────────────────────────────────────────────
function claimOrder(orderId) {
    const staffName = document.getElementById('staffName').value;
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
    .catch(err => console.error('Error claiming order:', err));
}

// ── complete order ────────────────────────────────────────────────────
function completeOrder(orderId) {
    const staffName = document.getElementById('staffName').value;
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
    .catch(err => console.error('Error completing order:', err));
}

// ── cancel order ──────────────────────────────────────────────────────
function cancelOrder(orderId) {
    const staffName = document.getElementById('staffName').value
                   || 'System';

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
    .catch(err => console.error('Error cancelling order:', err));
}

// ── fetch audit log ───────────────────────────────────────────────────
function fetchAuditLog() {
    fetch(`${API_BASE}/api/audit`)
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById('auditTable');
            tbody.innerHTML = '';

            if (data.length === 0) {
                tbody.innerHTML =
                    '<tr><td colspan="4">No actions yet</td></tr>';
                return;
            }

            // show most recent first
            [...data].reverse().forEach(entry => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${new Date(entry.timestamp)
                            .toLocaleTimeString()}</td>
                    <td>${entry.commandType}</td>
                    <td>${entry.orderId.substring(0, 8)}...</td>
                    <td>${entry.actor}</td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(err => console.error('Error fetching audit log:', err));
}

// ── clear form ────────────────────────────────────────────────────────
function clearForm() {
    document.getElementById('patientName').value  = '';
    document.getElementById('clinician').value    = '';
    document.getElementById('description').value  = '';
}

// ── show message helper ───────────────────────────────────────────────
function showMessage(elementId, message, type) {
    const el = document.getElementById(elementId);
    el.textContent = message;
    el.className   = type;
    setTimeout(() => { el.textContent = ''; }, 4000);
}
