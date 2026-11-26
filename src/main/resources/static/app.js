// Estado Global
const API_BASE = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')
  ? ''
  : 'https://helpdesk-mor9.onrender.com';
let currentUser = null;
let currentToken = null;
let allTickets = [];
let activeFilter = 'ALL';
let activeTicketForComments = null;

const STATUS_LABELS = {
  OPEN: 'Aberto',
  IN_PROGRESS: 'Em Andamento',
  CLOSED: 'Finalizado'
};

const STATUS_BADGE_CLASSES = {
  OPEN: 'badge-open',
  IN_PROGRESS: 'badge-in-progress',
  CLOSED: 'badge-closed'
};

const STATUS_ICONS = {
  OPEN: 'circle',
  IN_PROGRESS: 'hourglass-high',
  CLOSED: 'check'
};

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
  pingServerOnLoad();
  restoreSession();
});

// ======================= AUTENTICAÇÃO =======================
function restoreSession() {
  const token = localStorage.getItem('helpdesk_token');
  const user = localStorage.getItem('helpdesk_user');

  if (token && user) {
    try {
      currentToken = token;
      currentUser = JSON.parse(user);
      renderDashboard();
    } catch (e) {
      logout();
    }
  } else {
    showAuthScreen();
  }
}

function switchAuthTab(tab) {
  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');
  const formLogin = document.getElementById('form-login');
  const formRegister = document.getElementById('form-register');

  if (tab === 'login') {
    tabLogin.classList.add('active');
    tabRegister.classList.remove('active');
    formLogin.classList.remove('hidden');
    formRegister.classList.add('hidden');
  } else {
    tabRegister.classList.add('active');
    tabLogin.classList.remove('active');
    formRegister.classList.remove('hidden');
    formLogin.classList.add('hidden');
  }
}

async function submitAuth({ endpoint, payload, button, loadingHtml, idleHtml, successMessage, fallbackError }) {
  button.disabled = true;
  button.innerHTML = loadingHtml;

  try {
    const res = await apiFetch(`${API_BASE}${endpoint}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      const err = await parseError(res);
      throw new Error(err || fallbackError);
    }

    const data = await res.json();
    saveSession(data.token, data.user);
    showToast(successMessage, 'success');
    renderDashboard();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    button.disabled = false;
    button.innerHTML = idleHtml;
  }
}

async function handleLogin(event) {
  event.preventDefault();
  await submitAuth({
    endpoint: '/auth/login',
    payload: {
      email: document.getElementById('login-email').value.trim(),
      password: document.getElementById('login-password').value
    },
    button: document.getElementById('btn-login'),
    loadingHtml: '<i class="ph ph-spinner ph-spin"></i> Entrando...',
    idleHtml: '<span>Acessar Painel</span> <i class="ph ph-arrow-right"></i>',
    successMessage: 'Login realizado com sucesso!',
    fallbackError: 'Falha ao autenticar'
  });
}

async function handleRegister(event) {
  event.preventDefault();
  await submitAuth({
    endpoint: '/auth/register',
    payload: {
      name: document.getElementById('reg-name').value.trim(),
      email: document.getElementById('reg-email').value.trim(),
      password: document.getElementById('reg-password').value
    },
    button: document.getElementById('btn-register'),
    loadingHtml: '<i class="ph ph-spinner ph-spin"></i> Cadastrando...',
    idleHtml: '<span>Cadastrar e Entrar</span> <i class="ph ph-check"></i>',
    successMessage: 'Conta criada com sucesso!',
    fallbackError: 'Falha ao registrar'
  });
}

function saveSession(token, user) {
  currentToken = token;
  currentUser = user;
  localStorage.setItem('helpdesk_token', token);
  localStorage.setItem('helpdesk_user', JSON.stringify(user));
}

function logout() {
  localStorage.removeItem('helpdesk_token');
  localStorage.removeItem('helpdesk_user');
  currentToken = null;
  currentUser = null;
  showAuthScreen();
  showToast('Você saiu da sua conta.', 'info');
}

function showAuthScreen() {
  document.getElementById('auth-screen').classList.remove('hidden');
  document.getElementById('dashboard-screen').classList.add('hidden');
}

// ======================= DASHBOARD =======================
function renderDashboard() {
  document.getElementById('auth-screen').classList.add('hidden');
  document.getElementById('dashboard-screen').classList.remove('hidden');

  // Atualizar cabeçalho do usuário
  document.getElementById('header-user-name').textContent = currentUser.name;
  document.getElementById('header-user-role').textContent = currentUser.role || 'CLIENT';
  document.getElementById('header-avatar').textContent = (currentUser.name || 'U').charAt(0).toUpperCase();

  loadTickets();
}

async function loadTickets() {
  const container = document.getElementById('tickets-container');
  container.innerHTML = `
    <div class="loading-state">
      <div class="spinner"></div>
      <span>Carregando chamados...</span>
    </div>
  `;

  try {
    const res = await authFetch(`${API_BASE}/tickets`);
    if (!res.ok) throw new Error('Não foi possível carregar os chamados.');

    allTickets = await res.json();
    updateStats(allTickets);
    renderTickets();
  } catch (err) {
    container.innerHTML = `<div class="empty-state"><i class="ph ph-warning-circle"></i> ${err.message}</div>`;
    showToast(err.message, 'error');
  }
}

function updateStats(tickets) {
  const total = tickets.length;
  const pending = tickets.filter(t => t.status === 'OPEN' || t.status === 'IN_PROGRESS').length;
  const resolved = tickets.filter(t => t.status === 'CLOSED').length;

  document.getElementById('stat-total').textContent = total;
  document.getElementById('stat-pending').textContent = pending;
  document.getElementById('stat-resolved').textContent = resolved;
}

function filterByStatus(status, btnElement) {
  activeFilter = status;
  document.querySelectorAll('.filter-group .pill').forEach(btn => btn.classList.remove('active'));
  if (btnElement) btnElement.classList.add('active');
  renderTickets();
}

function renderTickets() {
  const container = document.getElementById('tickets-container');
  let list = allTickets;

  if (activeFilter !== 'ALL') {
    list = allTickets.filter(t => t.status === activeFilter);
  }

  if (list.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <i class="ph ph-folder-notch-open" style="font-size: 2.5rem; margin-bottom: 8px;"></i>
        <p>Nenhum chamado encontrado para este filtro.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = list.map(ticket => {
    const isTechOrAdmin = currentUser && (currentUser.role === 'ADMIN' || currentUser.role === 'TECHNICIAN');
    const createdDate = ticket.createdAt ? new Date(ticket.createdAt).toLocaleDateString('pt-BR') : 'Hoje';

    return `
      <div class="ticket-card" id="ticket-${ticket.id}">
        <div>
          <div class="ticket-header">
            <h4 class="ticket-title">${escapeHtml(ticket.title)}</h4>
            ${renderStatusBadge(ticket.status)}
          </div>
          <p class="ticket-body">${ticket.description ? escapeHtml(ticket.description) : '<em>Sem descrição informada.</em>'}</p>
        </div>

        <div class="ticket-footer">
          <div class="ticket-author">
            <i class="ph ph-user"></i>
            <span>${ticket.user ? escapeHtml(ticket.user.name) : 'Anônimo'} • ${createdDate}</span>
          </div>
          <div class="ticket-controls">
            ${isTechOrAdmin ? `
              <select class="status-select" onchange="handleChangeStatus(${ticket.id}, this.value)">
                ${renderStatusOptions(ticket.status)}
              </select>
            ` : ''}
            <button class="icon-btn" onclick="openCommentsModal(${ticket.id})" title="Ver Comentários">
              <i class="ph ph-chat-circle-dots"></i>
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function renderStatusBadge(status) {
  const label = STATUS_LABELS[status];
  if (!label) {
    return `<span class="status-badge">${status || 'N/A'}</span>`;
  }
  return `<span class="status-badge ${STATUS_BADGE_CLASSES[status]}"><i class="ph ph-${STATUS_ICONS[status]}"></i> ${label}</span>`;
}

function renderStatusOptions(currentStatus) {
  return Object.entries(STATUS_LABELS)
    .map(([value, label]) => `<option value="${value}" ${currentStatus === value ? 'selected' : ''}>${label}</option>`)
    .join('');
}

function toggleNewTicketPanel() {
  const panel = document.getElementById('new-ticket-panel');
  panel.classList.toggle('hidden');
  if (!panel.classList.contains('hidden')) {
    document.getElementById('ticket-title').focus();
  }
}

async function handleCreateTicket(event) {
  event.preventDefault();
  const title = document.getElementById('ticket-title').value.trim();
  const description = document.getElementById('ticket-desc').value.trim();
  const btn = document.getElementById('btn-create-ticket');

  btn.disabled = true;
  btn.innerHTML = '<i class="ph ph-spinner ph-spin"></i> Registrando...';

  try {
    const res = await authFetch(`${API_BASE}/tickets`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, description })
    });

    if (!res.ok) {
      const err = await parseError(res);
      throw new Error(err || 'Erro ao registrar chamado.');
    }

    showToast('Chamado aberto com sucesso!', 'success');
    document.getElementById('ticket-title').value = '';
    document.getElementById('ticket-desc').value = '';
    toggleNewTicketPanel();
    loadTickets();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<i class="ph ph-paper-plane-right"></i> Registrar Chamado';
  }
}

async function handleChangeStatus(ticketId, newStatus) {
  try {
    const res = await authFetch(`${API_BASE}/tickets/${ticketId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: newStatus })
    });

    if (!res.ok) {
      throw new Error('Falha ao atualizar o status do chamado.');
    }

    showToast('Status do chamado atualizado!', 'success');
    loadTickets();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ======================= MODAL COMENTÁRIOS =======================
async function openCommentsModal(ticketId) {
  activeTicketForComments = allTickets.find(t => t.id === ticketId);
  const modal = document.getElementById('comments-modal');
  const badge = document.getElementById('modal-ticket-badge');
  const title = document.getElementById('modal-ticket-title');
  const desc = document.getElementById('modal-ticket-desc');
  const list = document.getElementById('comments-list');

  if (activeTicketForComments) {
    badge.textContent = `#${activeTicketForComments.id}`;
    title.textContent = activeTicketForComments.title;
    desc.textContent = activeTicketForComments.description || 'Sem descrição adicional.';
  }

  modal.classList.remove('hidden');
  list.innerHTML = `<div class="loading-state"><div class="spinner"></div>Carregando histórico...</div>`;

  try {
    const res = await authFetch(`${API_BASE}/comments/tickets/${ticketId}/comments`);
    if (!res.ok) throw new Error('Não foi possível carregar os comentários.');

    const comments = await res.json();
    renderComments(comments);
  } catch (err) {
    list.innerHTML = `<div class="empty-state">${err.message}</div>`;
  }
}

function renderComments(comments) {
  const list = document.getElementById('comments-list');
  if (!comments || comments.length === 0) {
    list.innerHTML = `<div class="empty-state">Nenhum comentário registrado ainda.</div>`;
    return;
  }

  list.innerHTML = comments.map(c => `
    <div class="comment-item">
      <div class="comment-meta">
        <span><i class="ph ph-user"></i> ${c.user ? escapeHtml(c.user.name) : 'Anônimo'}</span>
        <span><i class="ph ph-clock"></i> ${c.createdAt ? new Date(c.createdAt).toLocaleString('pt-BR') : 'Agora'}</span>
      </div>
      <div class="comment-text">${escapeHtml(c.message)}</div>
    </div>
  `).join('');
  list.scrollTop = list.scrollHeight;
}

async function handleSendComment(event) {
  event.preventDefault();
  if (!activeTicketForComments) return;

  const input = document.getElementById('new-comment-text');
  const message = input.value.trim();
  if (!message) return;

  try {
    const res = await authFetch(`${API_BASE}/comments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ticketId: activeTicketForComments.id,
        message: message
      })
    });

    if (!res.ok) throw new Error('Erro ao enviar resposta.');

    input.value = '';
    // Recarrega comentários
    openCommentsModal(activeTicketForComments.id);
    showToast('Comentário enviado!', 'success');
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function closeCommentsModal() {
  document.getElementById('comments-modal').classList.add('hidden');
  activeTicketForComments = null;
}

// ======================= STATUS DO SERVIDOR (COLD START) =======================
const API_TIMEOUT_MS = 70000;       // folga para o backend terminar de subir
const SLOW_REQUEST_MS = 3000;        // tempo para exibir o aviso de conexão
const PING_DELAY_MS = 2500;          // tempo para exibir aviso no ping inicial
const CONNECTED_FEEDBACK_MS = 2000;  // tempo do "Servidor conectado!"

let serverAwake = false;
let pendingSlowRequests = 0;
let bannerTimer = null;

function showServerBanner(message, type = 'info') {
  const banner = document.getElementById('server-banner');
  const icon = document.getElementById('server-banner-icon');

  clearTimeout(bannerTimer);
  document.getElementById('server-banner-text').textContent = message;

  banner.classList.remove('hidden', 'success', 'error', 'info');
  banner.classList.add(type);

  const iconClass = type === 'success' ? 'ph-check-circle'
    : type === 'error' ? 'ph-warning-circle'
    : 'ph-spinner ph-spin';
  icon.className = `ph ${iconClass}`;
}

function hideServerBanner() {
  clearTimeout(bannerTimer);
  document.getElementById('server-banner').classList.add('hidden');
}

function scheduleBannerHide(ms) {
  clearTimeout(bannerTimer);
  bannerTimer = setTimeout(hideServerBanner, ms);
}

async function apiFetch(url, options = {}) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), API_TIMEOUT_MS);

  let slowNotified = false;
  const slowTimer = setTimeout(() => {
    slowNotified = true;
    pendingSlowRequests++;
    showServerBanner('Conectando ao servidor...', 'info');
  }, SLOW_REQUEST_MS);

  try {
    const res = await fetch(url, { ...options, signal: controller.signal });
    serverAwake = true;
    if (res.status === 401) {
      handleUnauthorized();
    }
    if (res.status > 0) {
      hideServerBanner();
    }
    return res;
  } catch (err) {
    if (err.name === 'AbortError') {
      throw new Error('O servidor demorou muito para responder. Tente novamente em instantes.');
    }
    throw err;
  } finally {
    clearTimeout(timeoutId);
    clearTimeout(slowTimer);
    if (slowNotified) {
      pendingSlowRequests = Math.max(0, pendingSlowRequests - 1);
      if (pendingSlowRequests === 0) hideServerBanner();
    }
    hideServerBanner();
  }
}

async function pingServerOnLoad() {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), API_TIMEOUT_MS);
  let notified = false;

  const notifyTimer = setTimeout(() => {
    notified = true;
    showServerBanner('Iniciando o servidor na nuvem... O primeiro acesso pode levar até 50 segundos.', 'info');
  }, PING_DELAY_MS);

  try {
    const res = await fetch(`${API_BASE}/`, { method: 'HEAD', cache: 'no-store', signal: controller.signal });
    serverAwake = true;
    clearTimeout(notifyTimer);
    if (res.status > 0) {
      hideServerBanner();
    }
  } catch (err) {
    clearTimeout(notifyTimer);
    if (notified) {
      showServerBanner('Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.', 'error');
      scheduleBannerHide(6000);
    }
  } finally {
    clearTimeout(timeoutId);
    if (!notified) {
      hideServerBanner();
    }
  }
}

// ======================= HELPERS =======================
function handleUnauthorized() {
  localStorage.removeItem('helpdesk_token');
  localStorage.removeItem('helpdesk_user');
  currentToken = null;
  currentUser = null;
  showAuthScreen();
}

async function authFetch(url, options = {}) {
  const headers = options.headers || {};
  if (currentToken) {
    headers['Authorization'] = `Bearer ${currentToken}`;
  }
  const res = await apiFetch(url, { ...options, headers });
  if (res.status === 401) {
    handleUnauthorized();
    showToast('Sessão expirada. Faça login novamente.', 'error');
  }
  return res;
}

async function parseError(res) {
  try {
    const text = await res.text();
    try {
      const json = JSON.parse(text);
      if (typeof json === 'object' && json !== null) {
        return Object.values(json).join(', ') || json.message;
      }
      return json.message || text;
    } catch {
      return text;
    }
  } catch {
    return 'Erro desconhecido';
  }
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  let icon = 'info';
  if (type === 'success') icon = 'check-circle';
  if (type === 'error') icon = 'warning-circle';

  toast.innerHTML = `<i class="ph ph-${icon}"></i> <span>${escapeHtml(message)}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 4000);
}

function escapeHtml(str) {
  if (!str) return '';
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

