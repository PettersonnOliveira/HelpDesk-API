// Estado Global
const API_BASE = '';
let currentUser = null;
let currentToken = null;
let allTickets = [];
let activeFilter = 'ALL';
let activeTicketForComments = null;

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
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

async function handleLogin(event) {
  event.preventDefault();
  const email = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value;
  const btn = document.getElementById('btn-login');

  btn.disabled = true;
  btn.innerHTML = '<i class="ph ph-spinner ph-spin"></i> Entrando...';

  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (!res.ok) {
      const err = await parseError(res);
      throw new Error(err || 'Falha ao autenticar');
    }

    const data = await res.json();
    saveSession(data.token, data.user);
    showToast('Login realizado com sucesso!', 'success');
    renderDashboard();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<span>Acessar Painel</span> <i class="ph ph-arrow-right"></i>';
  }
}

async function handleRegister(event) {
  event.preventDefault();
  const name = document.getElementById('reg-name').value.trim();
  const email = document.getElementById('reg-email').value.trim();
  const password = document.getElementById('reg-password').value;
  const btn = document.getElementById('btn-register');

  btn.disabled = true;
  btn.innerHTML = '<i class="ph ph-spinner ph-spin"></i> Cadastrando...';

  try {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, password })
    });

    if (!res.ok) {
      const err = await parseError(res);
      throw new Error(err || 'Falha ao registrar');
    }

    const data = await res.json();
    saveSession(data.token, data.user);
    showToast('Conta criada com sucesso!', 'success');
    renderDashboard();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<span>Cadastrar e Entrar</span> <i class="ph ph-check"></i>';
  }
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
                <option value="OPEN" ${ticket.status === 'OPEN' ? 'selected' : ''}>Aberto</option>
                <option value="IN_PROGRESS" ${ticket.status === 'IN_PROGRESS' ? 'selected' : ''}>Em Andamento</option>
                <option value="CLOSED" ${ticket.status === 'CLOSED' ? 'selected' : ''}>Finalizado</option>
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
  switch (status) {
    case 'OPEN':
      return `<span class="status-badge badge-open"><i class="ph ph-circle"></i> Aberto</span>`;
    case 'IN_PROGRESS':
      return `<span class="status-badge badge-in-progress"><i class="ph ph-hourglass-high"></i> Em Andamento</span>`;
    case 'CLOSED':
      return `<span class="status-badge badge-closed"><i class="ph ph-check"></i> Finalizado</span>`;
    default:
      return `<span class="status-badge">${status || 'N/A'}</span>`;
  }
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
    const payload = {
      title,
      description,
      userId: currentUser.id
    };

    const res = await authFetch(`${API_BASE}/tickets`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
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

// ======================= HELPERS =======================
async function authFetch(url, options = {}) {
  const headers = options.headers || {};
  if (currentToken) {
    headers['Authorization'] = `Bearer ${currentToken}`;
  }
  const res = await fetch(url, { ...options, headers });
  if (res.status === 401 || res.status === 403) {
    showToast('Sessão expirada. Faça login novamente.', 'error');
    logout();
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

