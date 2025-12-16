const API_BASE = 'http://localhost:8080/api';
let medications = [];
let pollingInterval = null;
let timerInterval = null;
let isAuthenticated = false;

window.takeMedication = takeMedication;
window.snoozeMedication = snoozeMedication;
window.openEditModal = openEditModal;
window.closeEditModal = closeEditModal;
window.deleteMedication = deleteMedication;

document.addEventListener('DOMContentLoaded', () => {
    setupAuthListeners();
    const savedAuth = localStorage.getItem('medtrackAuth');
    if (savedAuth === 'true') {
        handleLoginSuccess();
    }
    document.getElementById('enableNotifications').addEventListener('click', requestNotificationPermission);
    startLocalTimer();
});

function requestNotificationPermission() {
    if (!("Notification" in window)) {
        alert("This browser does not support desktop notification");
    } else {
        Notification.requestPermission().then(permission => {
            if (permission === "granted") {
                showAlert("✅ Notifications enabled!", "success");
                document.getElementById('enableNotifications').style.display = 'none';
            }
        });
    }
}

function checkAndNotifyDueMeds(meds) {
    const now = new Date();
    meds.forEach(med => {
        const dueTime = new Date(med.nextDueTime);
        if (dueTime <= now) {
            const lastNotified = localStorage.getItem(`notified_${med.id}`);
            if (lastNotified !== med.nextDueTime) {
                sendBrowserNotification(med);
                localStorage.setItem(`notified_${med.id}`, med.nextDueTime);
            }
        }
    });
}

function sendBrowserNotification(med) {
    if (Notification.permission === "granted") {
        const audio = new Audio('https://codeskulptor-demos.commondatastorage.googleapis.com/pang/pop.mp3');
        audio.play().catch(e => console.log(e));
        const notification = new Notification("Medication Due!", {
            body: `It's time to take your ${med.name}`,
            requireInteraction: true
        });
        notification.onclick = () => { window.focus(); notification.close(); };
    }
}

function startLocalTimer() {
    if (timerInterval) clearInterval(timerInterval);
    timerInterval = setInterval(() => {
        if (medications.length > 0 && isAuthenticated) {
            renderMedications(false);
        }
    }, 1000);
}

function formatTimeUntilDetailed(date) {
    const now = new Date();
    const diff = date - now;
    if (diff < 0) return 'Overdue';
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);
    let result = '';
    if (hours > 0) result += `${hours}h `;
    if (minutes > 0 || hours > 0) result += `${minutes}m `;
    result += `${seconds}s`;
    return result;
}

function setupAuthListeners() {
    const showRegister = document.getElementById('showRegister');
    const showLogin = document.getElementById('showLogin');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const logoutButton = document.getElementById('logoutButton');
    const addMedicationForm = document.getElementById('addMedicationForm');
    const editMedicationForm = document.getElementById('editMedicationForm');

    if(showRegister) showRegister.addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('loginFormContainer').style.display = 'none';
        document.getElementById('registerFormContainer').style.display = 'block';
    });
    if(showLogin) showLogin.addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('registerFormContainer').style.display = 'none';
        document.getElementById('loginFormContainer').style.display = 'block';
    });
    if(loginForm) loginForm.addEventListener('submit', handleLogin);
    if(registerForm) registerForm.addEventListener('submit', handleRegister);
    if(logoutButton) logoutButton.addEventListener('click', handleLogout);
    if(addMedicationForm) addMedicationForm.addEventListener('submit', handleAddMedication);
    if(editMedicationForm) editMedicationForm.addEventListener('submit', handleEditMedication);
}

async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    try {
        const response = await fetch(`${API_BASE}/auth/login?username=${username}&password=${password}`, { method: 'POST' });
        const result = await response.text();
        if (response.ok) {
            localStorage.setItem('medtrackUser', username);
            showAlert(`Welcome, ${username}!`, 'success');
            handleLoginSuccess();
        } else {
            showAlert(`Login failed: ${result}`, 'error');
        }
    } catch (error) { showAlert('Connection error.', 'error'); }
}

function handleLoginSuccess() {
    isAuthenticated = true;
    localStorage.setItem('medtrackAuth', 'true');
    const authView = document.getElementById('authView');
    const appView = document.getElementById('appView');
    if(authView) authView.style.display = 'none';
    if(appView) appView.style.display = 'grid';
    loadMedications();
    startPolling();
    if (Notification.permission === "granted") document.getElementById('enableNotifications').style.display = 'none';
}

function handleLogout() {
    isAuthenticated = false;
    localStorage.removeItem('medtrackAuth');
    localStorage.removeItem('medtrackUser');
    const authView = document.getElementById('authView');
    const appView = document.getElementById('appView');
    if(authView) authView.style.display = 'grid';
    if(appView) appView.style.display = 'none';
    if (pollingInterval) clearInterval(pollingInterval);
    showAlert('Logged out successfully.', 'success');
}

async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById('regUsername').value;
    const password = document.getElementById('regPassword').value;
    try {
        const response = await fetch(`${API_BASE}/auth/register?username=${username}&password=${password}`, { method: 'POST' });
        if (response.ok) {
            showAlert('Registration successful! Please sign in.', 'success');
            document.getElementById('showLogin').click();
        } else {
            const result = await response.text();
            showAlert(`Registration failed: ${result}`, 'error');
        }
    } catch (error) { showAlert('Connection error.', 'error'); }
}

function startPolling() {
    if (!pollingInterval) {
        pollingInterval = setInterval(() => {
            if (isAuthenticated) loadMedications();
        }, 5000);
    }
}

async function loadMedications() {
    if (!isAuthenticated) return;
    const username = localStorage.getItem('medtrackUser');
    try {
        const response = await fetch(`${API_BASE}/medications?username=${username}`);
        if (!response.ok) throw new Error('Failed');
        medications = await response.json();
        checkAndNotifyDueMeds(medications);
        renderMedications();
        showConnectionStatus('Connected', 'success');
    } catch (error) {
        showConnectionStatus('Disconnected', 'error');
        renderError();
    }
}

async function handleAddMedication(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const name = formData.get('name');
    const dosageForm = formData.get('dosageForm');
    const dosagesPerDay = formData.get('dosagesPerDay');
    const startTime = formData.get('startTime'); // Get local datetime string
    const foodSensitive = document.getElementById('foodSensitive').checked;
    const username = localStorage.getItem('medtrackUser');

    // Build URL Params
    let url = `${API_BASE}/medications?name=${encodeURIComponent(name)}&dosageForm=${encodeURIComponent(dosageForm)}&dosagesPerDay=${dosagesPerDay}&foodSensitive=${foodSensitive}&username=${username}`;

    // Append startTime if user selected one
    if (startTime) {
        url += `&startTime=${startTime}`;
    }

    try {
        const response = await fetch(url, { method: 'POST' });
        if (!response.ok) throw new Error('Failed');
        const newMed = await response.json();

        const serverSideInteraction = checkClientInteractions(newMed);
        if (serverSideInteraction) showAlert(serverSideInteraction, 'error');
        else showAlert('Medication added successfully!', 'success');

        e.target.reset();
        await loadMedications();
    } catch (error) { showAlert('Failed to add medication.', 'error'); }
}

async function takeMedication(id, name) {
    try {
        const response = await fetch(`${API_BASE}/medications/${id}/take`, { method: 'POST' });
        if (!response.ok) throw new Error('Failed');
        showAlert(`✅ ${name} marked as taken!`, 'success');
        await loadMedications();
    } catch (error) { showAlert('Failed.', 'error'); }
}

async function snoozeMedication(id, name) {
    try {
        const response = await fetch(`${API_BASE}/medications/${id}/snooze`, { method: 'POST' });
        if (!response.ok) throw new Error('Failed');
        showAlert(`⏰ ${name} snoozed.`, 'warning');
        await loadMedications();
    } catch (error) { showAlert('Failed.', 'error'); }
}

function openEditModal(medId, name, dosageForm, dosagesPerDay) {
    document.getElementById('editMedId').value = medId;
    document.getElementById('editMedName').value = name;
    document.getElementById('editDosageForm').value = dosageForm;
    document.getElementById('editDosagesPerDay').value = dosagesPerDay || 1;
    const overlay = document.getElementById('editModalOverlay');
    const content = document.getElementById('editModalContent');
    overlay.style.display = 'flex';
    setTimeout(() => content.classList.add('show'), 10);
}

function closeEditModal() {
    const overlay = document.getElementById('editModalOverlay');
    const content = document.getElementById('editModalContent');
    content.classList.remove('show');
    setTimeout(() => overlay.style.display = 'none', 300);
}

async function handleEditMedication(e) {
    e.preventDefault();
    const id = document.getElementById('editMedId').value;
    const name = document.getElementById('editMedName').value;
    const dosageForm = document.getElementById('editDosageForm').value;
    const dosagesPerDay = document.getElementById('editDosagesPerDay').value;
    try {
        const response = await fetch(`${API_BASE}/medications/${id}?name=${encodeURIComponent(name)}&dosageForm=${encodeURIComponent(dosageForm)}&dosagesPerDay=${dosagesPerDay}`, { method: 'PUT' });
        if (!response.ok) throw new Error('Failed');
        showAlert(`💊 ${name} updated.`, 'success');
        closeEditModal();
        await loadMedications();
    } catch (error) { showAlert('Failed.', 'error'); }
}

async function deleteMedication(id, name) {
    if (!confirm(`Delete ${name}?`)) return;
    try {
        const response = await fetch(`${API_BASE}/medications/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed');
        showAlert(`🗑️ ${name} deleted.`, 'success');
        await loadMedications();
    } catch (error) { showAlert('Failed.', 'error'); }
}

function renderMedications() {
    const container = document.getElementById('medicationsList');
    if (medications.length === 0) {
        container.innerHTML = `<div class="empty-state"><div class="icon">💊</div><p>No medications added yet</p></div>`;
        return;
    }
    const now = new Date();
    container.innerHTML = medications.map(med => {
        const dueTime = new Date(med.nextDueTime);
        const isDue = dueTime <= now;
        const timeStr = formatTimeUntilDetailed(dueTime);

        let attributeBadges = '';
        if (med.attributes && med.attributes.includes('Food Sensitive')) {
            attributeBadges += `<span class="med-attributes" style="background-color: #ffeeba; color: #856404; border: 1px solid #ffeeba;">🍽️ Take with Food</span>`;
        } else if (med.attributes && med.attributes !== 'Standard') {
            attributeBadges += `<div class="med-attributes">${med.attributes}</div>`;
        }

        return `
            <div class="med-item">
                <div class="med-header">
                    <div>
                        <div class="med-name">
                            <span class="status-indicator ${isDue ? 'status-due' : 'status-upcoming'}"></span>
                            ${med.name}
                        </div>
                        <div class="med-form">${med.dosageForm} • ${med.dosagesPerDay || 1}x daily</div>
                    </div>
                    <div class="med-actions">
                        <button class="action-btn edit-btn" onclick="window.openEditModal(${med.id}, '${med.name}', '${med.dosageForm}', ${med.dosagesPerDay})">⚙️</button>
                        <button class="action-btn delete-btn" onclick="window.deleteMedication(${med.id}, '${med.name}')">🗑️</button>
                    </div>
                </div>
                <div style="margin-bottom: 10px;">${attributeBadges}</div>
                <div class="med-time" style="font-weight: bold; color: ${isDue ? '#dc3545' : '#28a745'}">
                    ${isDue ? '🔔 DUE NOW' : `⏱️ Next dose: ${timeStr}`}
                </div>
                <div class="med-actions" style="margin-top: 5px; gap: 12px; justify-content: start;">
                    <button class="action-btn take-btn" onclick="window.takeMedication(${med.id}, '${med.name}')">✓ Take</button>
                    <button class="action-btn snooze-btn" onclick="window.snoozeMedication(${med.id}, '${med.name}')">⏰ Snooze</button>
                </div>
            </div>`;
    }).join('');
}

function checkClientInteractions(newMed) {
    const newName = newMed.name.toLowerCase();
    for (const existing of medications) {
        const existingName = existing.name.toLowerCase();
        if ((newName.includes('aspirin') && existingName.includes('warfarin')) ||
            (newName.includes('warfarin') && existingName.includes('aspirin'))) {
            return `⚠️ CRITICAL INTERACTION: ${newMed.name} and ${existing.name} may cause bleeding risks!`;
        }
    }
    return null;
}
function renderError() { document.getElementById('medicationsList').innerHTML = `<p style="text-align:center">Unable to load medications</p>`; }
function showAlert(message, type = 'info') {
    const container = document.getElementById('alertContainer');
    const alert = document.createElement('div');
    alert.className = `alert ${type}`;
    alert.innerHTML = message;
    container.prepend(alert);
    setTimeout(() => alert.classList.add('show'), 10);
    setTimeout(() => { alert.classList.remove('show'); setTimeout(() => alert.remove(), 300); }, 6000);
}
function showConnectionStatus(message, type) {
    const status = document.getElementById('connectionStatus');
    const indicator = status.querySelector('.status-indicator');
    const messageSpan = document.getElementById('connectionMessage');
    status.className = `connection-status ${type}`;
    messageSpan.textContent = message;
    indicator.style.backgroundColor = type === 'success' ? '#28a745' : '#dc3545';
}