const API_BASE = 'http://localhost:8080/api';
let medications = [];
let pollingInterval = null;
let isAuthenticated = false; // New state for login status

// Make functions globally available for onclick handlers
window.takeMedication = takeMedication;
window.snoozeMedication = snoozeMedication;
window.openEditModal = openEditModal;
window.closeEditModal = closeEditModal;
window.deleteMedication = deleteMedication;

document.addEventListener('DOMContentLoaded', () => {
    setupAuthListeners();
    // Check local storage or session for existing auth status
    const savedAuth = localStorage.getItem('medtrackAuth');
    if (savedAuth === 'true') {
        handleLoginSuccess();
    }
});

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

function handleLoginSuccess() {
    isAuthenticated = true;
    localStorage.setItem('medtrackAuth', 'true');
    const authView = document.getElementById('authView');
    const appView = document.getElementById('appView');

    if(authView) authView.style.display = 'none';
    if(appView) appView.style.display = 'grid';

    loadMedications();
    startPolling();
}

function handleLogout() {
    isAuthenticated = false;
    localStorage.removeItem('medtrackAuth');
    const authView = document.getElementById('authView');
    const appView = document.getElementById('appView');

    if(authView) authView.style.display = 'grid';
    if(appView) appView.style.display = 'none';

    if (pollingInterval) clearInterval(pollingInterval);
    showAlert('Logged out successfully.', 'success');
}

// --- AUTHENTICATION API CALLS ---
async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById('regUsername').value;
    const password = document.getElementById('regPassword').value;

    try {
        const response = await fetch(`${API_BASE}/auth/register?username=${username}&password=${password}`, { method: 'POST' });
        const result = await response.text();

        if (response.ok) {
            showAlert('Registration successful! Please sign in.', 'success');
            document.getElementById('showLogin').click(); // Switch to login form
        } else {
            showAlert(`Registration failed: ${result}`, 'error');
        }
    } catch (error) {
        showAlert('Connection error during registration.', 'error');
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const response = await fetch(`${API_BASE}/auth/login?username=${username}&password=${password}`, { method: 'POST' });
        const result = await response.text();

        if (response.ok) {
            showAlert(`Welcome, ${username}!`, 'success');
            handleLoginSuccess();
        } else {
            showAlert(`Login failed: ${result}`, 'error');
        }
    } catch (error) {
        showAlert('Connection error during login.', 'error');
    }
}
// --- END AUTHENTICATION API CALLS ---

function startPolling() {
    if (!pollingInterval) {
        pollingInterval = setInterval(() => {
            if (isAuthenticated) loadMedications();
        }, 5000);
    }
}

async function loadMedications() {
    if (!isAuthenticated) return;
    try {
        // showConnectionStatus('Attempting connection...', 'warning');
        const response = await fetch(`${API_BASE}/medications`);
        if (!response.ok) throw new Error('Failed to fetch medications');

        medications = await response.json();
        renderMedications();
        showConnectionStatus('Connected', 'success');
    } catch (error) {
        console.error('Error loading medications:', error);
        showConnectionStatus('Disconnected: Server not reachable (Check console)', 'error');
        renderError();
    }
}

async function handleAddMedication(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const name = formData.get('name');
    const dosageForm = formData.get('dosageForm');
    const foodSensitive = document.getElementById('foodSensitive').checked;

    try {
        const response = await fetch(`${API_BASE}/medications?name=${encodeURIComponent(name)}&dosageForm=${encodeURIComponent(dosageForm)}&foodSensitive=${foodSensitive}`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error('Failed to add medication');

        const newMed = await response.json();
        const serverSideInteraction = checkClientInteractions(newMed);

        if (serverSideInteraction) {
            showAlert(serverSideInteraction, 'error');
        } else {
            showAlert('Medication added successfully!', 'success');
        }

        e.target.reset();
        await loadMedications();
    } catch (error) {
        console.error('Error adding medication:', error);
        showAlert('Failed to add medication. Please ensure the backend is running.', 'error');
    }
}

// --- COMMAND PATTERN EXECUTION ---

async function takeMedication(id, name) {
    console.log(`Executing Take Command for ID: ${id}`);
    try {
        const response = await fetch(`${API_BASE}/medications/${id}/take`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error('Failed to execute TAKE command');

        // Command executes on backend, updating schedule. Polling refreshes UI.
        showAlert(`✅ ${name} marked as taken! Schedule advanced (Command Pattern).`, 'success');
        await loadMedications();
    } catch (error) {
        console.error('Error taking medication:', error);
        showAlert('Failed to record medication. Please check connection.', 'error');
    }
}

async function snoozeMedication(id, name) {
    console.log(`Executing Snooze Command for ID: ${id}`);
    try {
        const response = await fetch(`${API_BASE}/medications/${id}/snooze`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error('Failed to execute SNOOZE command');

        // Command executes on backend, updating schedule. Polling refreshes UI.
        showAlert(`⏰ ${name} snoozed for 15 minutes (Command Pattern).`, 'warning');
        await loadMedications();
    } catch (error) {
        console.error('Error snoozing medication:', error);
        showAlert('Failed to snooze medication. Please check connection.', 'error');
    }
}

// --- NEW: EDIT FUNCTIONALITY ---

function openEditModal(medId, name, dosageForm) {
    document.getElementById('editMedId').value = medId;
    document.getElementById('editMedName').value = name;
    document.getElementById('editDosageForm').value = dosageForm;

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

    try {
        const response = await fetch(`${API_BASE}/medications/${id}?name=${encodeURIComponent(name)}&dosageForm=${encodeURIComponent(dosageForm)}`, {
            method: 'PUT'
        });

        if (!response.ok) throw new Error('Failed to update medication');

        showAlert(`💊 ${name} updated successfully.`, 'success');
        closeEditModal();
        await loadMedications();
    } catch (error) {
        console.error('Error updating medication:', error);
        showAlert('Failed to update medication.', 'error');
    }
}

// --- NEW: DELETE FUNCTIONALITY ---

async function deleteMedication(id, name) {
    if (!confirm(`Are you sure you want to delete ${name}?`)) return;

    try {
        const response = await fetch(`${API_BASE}/medications/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Failed to delete medication');

        showAlert(`🗑️ ${name} deleted successfully.`, 'success');
        await loadMedications();
    } catch (error) {
        console.error('Error deleting medication:', error);
        showAlert('Failed to delete medication.', 'error');
    }
}

// --- RENDERING & UTILITIES ---

function renderMedications() {
    const container = document.getElementById('medicationsList');
    if (medications.length === 0) {
        container.innerHTML = `
                <div class="empty-state">
                    <div class="icon">💊</div>
                    <p>No medications added yet</p>
                    <p style="font-size: 0.9em; margin-top: 8px;">Add your first medication to get started</p>
                </div>
            `;
        return;
    }

    const now = new Date();

    container.innerHTML = medications.map(med => {
        const dueTime = new Date(med.nextDueTime);
        const isDue = dueTime <= now;
        const timeStr = formatTimeUntil(dueTime);

        // IMPORTANT: Using window.takeMedication explicitly
        return `
                <div class="med-item">
                    <div class="med-header">
                        <div>
                            <div class="med-name">
                                <span class="status-indicator ${isDue ? 'status-due' : 'status-upcoming'}"></span>
                                ${med.name}
                            </div>
                            <div class="med-form">${med.dosageForm}</div>
                        </div>
                        <div class="med-actions">
                            <button class="action-btn edit-btn" title="Edit" onclick="window.openEditModal(${med.id}, '${med.name}', '${med.dosageForm}')">
                                ⚙️
                            </button>
                            <button class="action-btn delete-btn" title="Delete" onclick="window.deleteMedication(${med.id}, '${med.name}')">
                                🗑️
                            </button>
                        </div>
                    </div>
                    ${med.attributes && med.attributes !== 'Standard' ? `<div class="med-attributes">${med.attributes}</div>` : ''}
                    <div class="med-time">
                        ${isDue ? '🔔 DUE NOW (Observer Event)' : `⏱️ Next dose: ${timeStr}`}
                    </div>
                    <div class="med-actions" style="margin-top: 5px; gap: 12px; justify-content: start;">
                        <button class="action-btn take-btn" onclick="window.takeMedication(${med.id}, '${med.name}')">
                            ✓ Take
                        </button>
                        <button class="action-btn snooze-btn" onclick="window.snoozeMedication(${med.id}, '${med.name}')">
                            ⏰ Snooze
                        </button>
                    </div>
                </div>
            `;
    }).join('');
}

// Client-side simulation of the Strategy Pattern (for visual feedback) - (existing code)
function checkClientInteractions(newMed) {
    const newName = newMed.name.toLowerCase();

    for (const existing of medications) {
        const existingName = existing.name.toLowerCase();

        // Rule 1: Aspirin and Warfarin
        if ((newName.includes('aspirin') && existingName.includes('warfarin')) ||
            (newName.includes('warfarin') && existingName.includes('aspirin'))) {
            return `⚠️ CRITICAL INTERACTION: ${newMed.name} and ${existing.name} may cause bleeding risks! (Strategy Check)`;
        }

        // Rule 2: Cipro and Food Sensitive
        if (newName.includes('cipro') && existing.attributes?.includes('Food Sensitive')) {
            return `⚠️ Interaction Warning: ${newMed.name} might interact with food-sensitive med ${existing.name}. (Strategy Check)`;
        }
    }
    return null;
}


function renderError() {
    const container = document.getElementById('medicationsList');
    container.innerHTML = `
            <div class="empty-state">
                <div class="icon">⚠️</div>
                <p>Unable to load medications</p>
                <p style="font-size: 0.9em; margin-top: 8px;">Please check your backend connection (http://localhost:8080)</p>
            </div>
        `;
}

function formatTimeUntil(date) {
    const now = new Date();
    const diff = date - now;

    if (diff < 0) return 'Overdue';

    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

    if (hours > 24) {
        const days = Math.floor(hours / 24);
        return `in ${days} day${days !== 1 ? 's' : ''}`;
    }

    if (hours > 0) {
        return `in ${hours}h ${minutes}m`;
    }

    return `in ${minutes} minute${minutes !== 1 ? 's' : ''}`;
}

function showAlert(message, type = 'info') {
    const container = document.getElementById('alertContainer');
    const alertId = 'alert-' + Date.now();

    const alert = document.createElement('div');
    alert.id = alertId;
    alert.className = `alert ${type}`;
    alert.innerHTML = message;

    container.prepend(alert);

    setTimeout(() => alert.classList.add('show'), 10);

    setTimeout(() => {
        alert.classList.remove('show');
        setTimeout(() => alert.remove(), 300);
    }, 6000);
}

function showConnectionStatus(message, type) {
    const status = document.getElementById('connectionStatus');
    const indicator = status.querySelector('.status-indicator');
    const messageSpan = document.getElementById('connectionMessage');

    status.classList.remove('success', 'warning', 'error');
    indicator.className = 'status-indicator';

    // Add visual cues to the status box
    if (type === 'success') {
        status.style.backgroundColor = '#d4edda';
        messageSpan.style.color = '#155724';
        indicator.style.backgroundColor = '#28a745';
    } else if (type === 'error') {
        status.style.backgroundColor = '#f8d7da';
        messageSpan.style.color = '#721c24';
        indicator.style.backgroundColor = '#dc3545';
    } else {
        status.style.backgroundColor = '#fff3cd';
        messageSpan.style.color = '#856404';
        indicator.style.backgroundColor = '#ffc107';
    }

    messageSpan.textContent = message;
}

// Clean up on page unload
window.addEventListener('beforeunload', () => {
    if (pollingInterval) {
        clearInterval(pollingInterval);
    }
});