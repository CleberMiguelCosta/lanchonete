function pageMeta(route) {
  const op = ["dashboard", "pedidos", "cardapio", "produtos", "categorias"];
  const group = op.includes(route) ? t("crumbOp") : t("crumbGestao");
  return { title: t(route), crumb: `${group} / ${t(route)}` };
}

const MODULO_LABEL = {
  DASHBOARD: "Início",
  PEDIDOS: "Pedidos",
  CARDAPIO: "Cardápio",
  PRODUTOS: "Produtos",
  CATEGORIAS: "Categorias",
  LANCHONETES: "Lanchonetes",
  CLIENTES: "Clientes",
  RELATORIOS: "Relatórios",
  CONFIGURACOES: "Configurações",
  USUARIOS: "Usuários",
};

const ROUTE_MODULO = {
  dashboard: "DASHBOARD",
  pedidos: "PEDIDOS",
  cardapio: "CARDAPIO",
  produtos: "PRODUTOS",
  categorias: "CATEGORIAS",
  lanchonetes: "LANCHONETES",
  clientes: "CLIENTES",
  relatorios: "RELATORIOS",
  configuracoes: "CONFIGURACOES",
  usuarios: "USUARIOS",
};

const PERFIL_LABEL = {
  ADMINISTRADOR: "Administrador",
  GESTOR: "Gestor",
  USUARIO: "Usuário",
};

const STATUS_LABEL = {
  AGUARDANDO: "Aguardando",
  EM_PREPARACAO: "Em preparação",
  PRONTO: "Pronto",
  SAIU_PARA_ENTREGA: "Saiu para entrega",
  CONCLUIDO: "Concluído",
  CANCELADO: "Cancelado",
};

const TIPO_LABEL = {
  BALCAO: "Balcão",
  RETIRADA: "Retirada",
  DELIVERY: "Delivery",
};

const NEXT_STATUS = {
  AGUARDANDO: "EM_PREPARACAO",
  EM_PREPARACAO: "PRONTO",
  PRONTO: "SAIU_PARA_ENTREGA",
  SAIU_PARA_ENTREGA: "CONCLUIDO",
};

const state = {
  route: "dashboard",
  user: null,
  usuarios: [],
  lanchonetes: [],
  produtos: [],
  categorias: [],
  clientes: [],
  pedidos: [],
  dashboard: null,
  unidadeId: Number(localStorage.getItem("unidadeId")) || null,
  search: "",
  usuarioBusca: "",
};

const els = {
  app: document.getElementById("app"),
  sidebar: document.getElementById("sidebar"),
  backdrop: document.getElementById("backdrop"),
  btnMenu: document.getElementById("btnMenu"),
  btnCollapse: document.getElementById("btnCollapse"),
  pageTitle: document.getElementById("pageTitle"),
  breadcrumbs: document.getElementById("breadcrumbs"),
  unitName: document.getElementById("unitName"),
  unitMeta: document.getElementById("unitMeta"),
  globalSearch: document.getElementById("globalSearch"),
  notifBadge: document.getElementById("notifBadge"),
  btnNotif: document.getElementById("btnNotif"),
  notifPanel: document.getElementById("notifPanel"),
  notifList: document.getElementById("notifList"),
  btnNotifPedidos: document.getElementById("btnNotifPedidos"),
  btnProfile: document.getElementById("btnProfile"),
  profileMenu: document.getElementById("profileMenu"),
  profileName: document.getElementById("profileName"),
  profileRole: document.getElementById("profileRole"),
  avatarInicials: document.getElementById("avatarInicials"),
  toast: document.getElementById("toast"),
  modal: document.getElementById("modal"),
  modalTitle: document.getElementById("modalTitle"),
  modalBody: document.getElementById("modalBody"),
  modalFoot: document.getElementById("modalFoot"),
  modalClose: document.getElementById("modalClose"),
};

let toastTimer;

function money(v) {
  return Number(v || 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function escapeHtml(v) {
  return String(v ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}

function toast(msg, type = "success") {
  clearTimeout(toastTimer);
  els.toast.textContent = msg;
  els.toast.className = `toast show ${type}`;
  toastTimer = setTimeout(() => els.toast.classList.remove("show"), 2800);
}

function unidadeAtual() {
  return state.lanchonetes.find((l) => l.id === state.unidadeId) || state.lanchonetes[0] || null;
}

function syncUnidade() {
  const u = unidadeAtual();
  if (u) {
    state.unidadeId = u.id;
    localStorage.setItem("unidadeId", String(u.id));
    els.unitName.textContent = u.nome;
    els.unitMeta.textContent = `${u.cidade} · ${t("unitActive")}`;
  } else {
    state.unidadeId = null;
    els.unitName.textContent = t("noUnit");
    els.unitMeta.textContent = t("registerUnit");
  }
}

function applyTheme(theme = localStorage.getItem("uiTheme") || "light") {
  const value = theme === "dark" ? "dark" : "light";
  localStorage.setItem("uiTheme", value);
  document.documentElement.setAttribute("data-theme", value);
}

function applyLanguage(lang = localStorage.getItem("uiLang") || "pt") {
  const supported = ["pt", "en", "de", "fr"];
  const value = supported.includes(lang) ? lang : "pt";
  localStorage.setItem("uiLang", value);
  document.documentElement.setAttribute("lang", value === "pt" ? "pt-BR" : value);

  document.querySelectorAll("[data-i18n]").forEach((el) => {
    el.textContent = t(el.dataset.i18n);
  });
  document.querySelectorAll("[data-i18n-placeholder]").forEach((el) => {
    el.setAttribute("placeholder", t(el.dataset.i18nPlaceholder));
  });

  if (state.route) {
    const meta = pageMeta(state.route);
    els.pageTitle.textContent = meta.title;
    els.breadcrumbs.textContent = meta.crumb;
  }
  syncUnidade();
}

function temPermissao(modulo) {
  if (!state.user) return false;
  if (state.user.perfil === "ADMINISTRADOR") return true;
  return (state.user.permissoes || []).includes(modulo);
}

function syncUserUi() {
  if (!state.user) return;
  const nome = state.user.nomeCompleto || state.user.username;
  els.profileName.textContent = nome.split(" ")[0];
  els.profileRole.textContent = PERFIL_LABEL[state.user.perfil] || state.user.perfil;
  els.avatarInicials.textContent = nome
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase() || "")
    .join("") || "U";

  document.querySelectorAll(".nav-link[data-route]").forEach((btn) => {
    const route = btn.dataset.route;
    const modulo = ROUTE_MODULO[route];
    btn.hidden = modulo ? !temPermissao(modulo) : false;
  });

  document.querySelectorAll("[data-admin-only]").forEach((el) => {
    el.hidden = state.user.perfil !== "ADMINISTRADOR";
  });
}

function renderNotificacoes() {
  const andamento = (state.pedidos || []).filter((p) =>
    ["AGUARDANDO", "EM_PREPARACAO", "PRONTO", "SAIU_PARA_ENTREGA"].includes(p.status)
  );
  els.notifBadge.hidden = andamento.length === 0;
  els.notifBadge.textContent = String(andamento.length);

  if (!andamento.length) {
    els.notifList.innerHTML = `<div class="empty"><strong>Tudo em dia</strong><p>Nenhum pedido pendente no momento.</p></div>`;
    return;
  }

  els.notifList.innerHTML = andamento.slice(0, 6).map((p) => `
    <button type="button" class="list-soft-item" data-go="pedidos" style="width:100%;cursor:pointer;border:1px solid var(--border);background:#f8fafc;text-align:left">
      <div>
        <strong>${escapeHtml(p.numero)}</strong><br>
        <small>${escapeHtml(p.nomeCliente)} · ${STATUS_LABEL[p.status]}</small>
      </div>
      <strong>${money(p.total)}</strong>
    </button>
  `).join("");
}

function togglePanel(panel, btn) {
  const open = panel.hidden;
  els.notifPanel.hidden = true;
  els.profileMenu.hidden = true;
  els.btnNotif.setAttribute("aria-expanded", "false");
  els.btnProfile.setAttribute("aria-expanded", "false");
  if (open) {
    panel.hidden = false;
    btn.setAttribute("aria-expanded", "true");
  }
}

function openModal(title, bodyHtml, footHtml) {
  els.modalTitle.textContent = title;
  els.modalBody.innerHTML = bodyHtml;
  els.modalFoot.innerHTML = footHtml || "";
  els.modal.hidden = false;
  els.modal.setAttribute("aria-hidden", "false");
  els.backdrop.hidden = false;
  els.backdrop.setAttribute("aria-hidden", "false");
  document.body.classList.add("modal-open");
}

function closeModal() {
  els.modal.hidden = true;
  els.modal.setAttribute("aria-hidden", "true");
  els.modalBody.innerHTML = "";
  els.modalFoot.innerHTML = "";
  document.body.classList.remove("modal-open");
  if (!els.sidebar.classList.contains("is-open")) {
    els.backdrop.hidden = true;
    els.backdrop.setAttribute("aria-hidden", "true");
  }
}

function fecharMenus() {
  els.notifPanel.hidden = true;
  els.profileMenu.hidden = true;
  els.btnNotif.setAttribute("aria-expanded", "false");
  els.btnProfile.setAttribute("aria-expanded", "false");
}

async function executarAcaoPerfil(action) {
  fecharMenus();
  if (action === "senha") {
    openAlterarSenhaModal(false);
    return;
  }
  if (action === "usuarios") {
    navigate("usuarios");
    return;
  }
  if (action === "logout") {
    try {
      await API.auth.logout();
    } catch (_) {
      /* ignore */
    }
    window.location.href = "/login.html";
  }
}

function emptyState(title, text, actionLabel, actionAttr = "") {
  return `
    <div class="empty card">
      <strong>${escapeHtml(title)}</strong>
      <p>${escapeHtml(text)}</p>
      ${actionLabel ? `<button type="button" class="btn btn-primary" ${actionAttr}>${escapeHtml(actionLabel)}</button>` : ""}
    </div>
  `;
}

function deltaLabel(atual, anterior, invertido = false) {
  if (!anterior && !atual) return `<span class="kpi-foot">Sem comparação</span>`;
  const diff = atual - anterior;
  const pct = anterior ? Math.round((diff / anterior) * 100) : 100;
  const up = invertido ? diff < 0 : diff >= 0;
  const sinal = diff >= 0 ? "+" : "";
  return `<span class="kpi-foot ${up ? "up" : "down"}">${sinal}${pct}% vs ontem</span>`;
}

async function loadAll() {
  const [lanchonetes, produtos, categorias, clientes, pedidos, dashboard] = await Promise.all([
    API.lanchonetes.list(),
    API.produtos.list(),
    API.categorias.list(),
    API.clientes.list(),
    API.pedidos.list(),
    API.dashboard(),
  ]);
  state.lanchonetes = lanchonetes;
  state.produtos = produtos;
  state.categorias = categorias;
  state.clientes = clientes;
  state.pedidos = pedidos;
  state.dashboard = dashboard;
  if (temPermissao("USUARIOS")) {
    try {
      state.usuarios = await API.usuarios.list(state.usuarioBusca);
    } catch (_) {
      state.usuarios = [];
    }
  }
  syncUnidade();
  syncUserUi();
  renderNotificacoes();
}

function navigate(route) {
  const modulo = ROUTE_MODULO[route];
  if (modulo && !temPermissao(modulo)) {
    toast("Você não tem permissão para este módulo.", "error");
    route = "dashboard";
  }
  state.route = route;
  els.notifPanel.hidden = true;
  els.profileMenu.hidden = true;
  document.querySelectorAll(".nav-link").forEach((btn) => {
    btn.classList.toggle("is-active", btn.dataset.route === route);
  });
  document.querySelectorAll(".view").forEach((view) => {
    const active = view.dataset.view === route;
    view.hidden = !active;
    view.classList.toggle("is-active", active);
  });
  const meta = pageMeta(route);
  els.pageTitle.textContent = meta.title;
  els.breadcrumbs.textContent = meta.crumb;
  els.sidebar.classList.remove("is-open");
  if (window.innerWidth <= 860) els.backdrop.hidden = true;
  render();
}

function matchesSearch(text) {
  if (!state.search) return true;
  return String(text).toLowerCase().includes(state.search.toLowerCase());
}

function renderDashboard() {
  const d = state.dashboard || {};
  const maxFat = Math.max(...(d.faturamentoSemana || []).map((p) => Number(p.valor) || 0), 1);
  const statusEntries = Object.entries(d.pedidosPorStatus || {});
  const statusTotal = statusEntries.reduce((acc, [, n]) => acc + Number(n), 0) || 1;

  document.getElementById("view-dashboard").innerHTML = `
    <div class="dash-grid">
      <section class="card panel">
        <div class="panel-head">
          <div>
            <h2>Faturamento da semana</h2>
            <p>Comparativo dos últimos 7 dias</p>
          </div>
        </div>
        <div class="chart">
          ${(d.faturamentoSemana || []).map((p) => {
            const h = Math.max(8, Math.round((Number(p.valor) / maxFat) * 140));
            return `<div class="chart-bar" title="${money(p.valor)}"><span style="height:${h}px"></span><small>${escapeHtml(p.dia)}</small></div>`;
          }).join("") || '<div class="empty">Sem dados de faturamento</div>'}
        </div>
      </section>
      <section class="card panel">
        <div class="panel-head">
          <div>
            <h2>Status dos pedidos</h2>
            <p>Distribuição atual</p>
          </div>
        </div>
        <div class="status-bars">
          ${statusEntries.map(([status, qtd]) => `
            <div class="status-row">
              <span>${STATUS_LABEL[status] || status}</span>
              <div class="status-track"><div class="status-fill" style="width:${Math.round((qtd / statusTotal) * 100)}%"></div></div>
              <strong>${qtd}</strong>
            </div>
          `).join("")}
        </div>
      </section>
    </div>

    <div class="dash-grid-2">
      <section class="card panel">
        <div class="panel-head">
          <div>
            <h2>Mais vendidos</h2>
            <p>Produtos com maior volume</p>
          </div>
        </div>
        <div class="list-soft">
          ${(d.produtosMaisVendidos || []).length
            ? d.produtosMaisVendidos.map((p) => `
                <div class="list-soft-item">
                  <div><strong>${escapeHtml(p.nome)}</strong><br><small>Item do cardápio</small></div>
                  <strong>${p.quantidade} un.</strong>
                </div>`).join("")
            : emptyState("Sem vendas ainda", "Os produtos mais vendidos aparecerão aqui.", null)}
        </div>
      </section>
      <section class="card panel">
        <div class="panel-head">
          <div>
            <h2>Últimos pedidos</h2>
            <p>Atividade recente</p>
          </div>
          <button type="button" class="btn btn-secondary btn-sm" data-go="pedidos">Ver todos</button>
        </div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Pedido</th><th>Cliente</th><th>Status</th><th>Total</th></tr></thead>
            <tbody>
              ${(d.ultimosPedidos || []).length
                ? d.ultimosPedidos.map((p) => `
                  <tr>
                    <td>${escapeHtml(p.numero)}</td>
                    <td>${escapeHtml(p.nomeCliente)}</td>
                    <td><span class="badge-status st-${p.status}">${STATUS_LABEL[p.status]}</span></td>
                    <td>${money(p.total)}</td>
                  </tr>`).join("")
                : `<tr><td colspan="4" class="empty">Nenhum pedido registrado.</td></tr>`}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  `;
}

function renderPedidos() {
  const lista = state.pedidos.filter((p) =>
    matchesSearch(`${p.numero} ${p.nomeCliente} ${p.status} ${p.tipoAtendimento}`)
  );

  document.getElementById("view-pedidos").innerHTML = `
    <div class="toolbar">
      <div class="filters">
        <select class="select" id="filtroStatus">
          <option value="">Todos os status</option>
          ${Object.entries(STATUS_LABEL).map(([k, v]) => `<option value="${k}">${v}</option>`).join("")}
        </select>
      </div>
      <div class="toolbar-actions">
        <button type="button" class="btn btn-primary" id="btnNovoPedido">Novo pedido</button>
      </div>
    </div>
    <div class="orders-board" id="ordersBoard">
      ${lista.length ? lista.map(orderCard).join("") : emptyState("Fila vazia", "Crie o primeiro pedido para começar a operação.", "Novo pedido", 'id="btnNovoPedidoEmpty"')}
    </div>
  `;

  const filtro = document.getElementById("filtroStatus");
  filtro?.addEventListener("change", () => {
    const board = document.getElementById("ordersBoard");
    const filtered = lista.filter((p) => !filtro.value || p.status === filtro.value);
    board.innerHTML = filtered.length
      ? filtered.map(orderCard).join("")
      : emptyState("Nenhum pedido", "Nada encontrado para este filtro.", null);
  });
}

function orderCard(p) {
  const next = NEXT_STATUS[p.status];
  const hora = p.criadoEm ? new Date(p.criadoEm).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }) : "--";
  return `
    <article class="card order-card" data-id="${p.id}">
      <div class="order-top">
        <div>
          <strong>${escapeHtml(p.numero)}</strong>
          <div class="muted">${escapeHtml(p.nomeCliente)} · ${hora}</div>
        </div>
        <span class="badge-status st-${p.status}">${STATUS_LABEL[p.status]}</span>
      </div>
      <div class="muted">${TIPO_LABEL[p.tipoAtendimento] || p.tipoAtendimento} · ${escapeHtml(p.lanchoneteNome || "")}</div>
      <div class="product-meta">
        <span class="price">${money(p.total)}</span>
        <div class="row-actions">
          ${next ? `<button class="btn btn-secondary btn-sm" data-action="advance" data-next="${next}">Avançar</button>` : ""}
          ${p.status !== "CANCELADO" && p.status !== "CONCLUIDO" ? `<button class="btn btn-ghost btn-sm" data-action="cancel">Cancelar</button>` : ""}
        </div>
      </div>
    </article>
  `;
}

function renderProdutosCards(targetId, title, subtitle, showAdd = true) {
  const u = unidadeAtual();
  let lista = state.produtos;
  if (targetId === "view-cardapio" && u) {
    lista = lista.filter((p) => p.lanchoneteId === u.id && p.disponivel);
  }
  lista = lista.filter((p) => matchesSearch(`${p.nome} ${p.descricao || ""} ${p.categoriaNome || ""}`));

  document.getElementById(targetId).innerHTML = `
    <div class="toolbar">
      <div>
        <h2 style="margin:0;font-family:var(--display);font-size:1.1rem">${title}</h2>
        <p class="muted" style="margin:.2rem 0 0">${subtitle}</p>
      </div>
      <div class="toolbar-actions">
        ${showAdd ? `<button type="button" class="btn btn-primary" id="btnNovoProduto">Adicionar produto</button>` : ""}
      </div>
    </div>
    <div class="products-grid">
      ${lista.length ? lista.map(productCard).join("") : emptyState("Nenhum produto", "Cadastre itens para montar o cardápio.", showAdd ? "Adicionar produto" : null, showAdd ? 'id="btnNovoProdutoEmpty"' : "")}
    </div>
  `;
}

function productCard(p) {
  const media = p.imagemUrl
    ? `<img src="${escapeHtml(p.imagemUrl)}" alt="${escapeHtml(p.nome)}">`
    : escapeHtml(p.nome.slice(0, 1).toUpperCase());
  return `
    <article class="card product-card" data-id="${p.id}" data-lanchonete="${p.lanchoneteId}">
      <div class="product-media">${media}</div>
      <div class="product-body">
        <div class="product-meta">
          <span class="pill">${escapeHtml(p.categoriaNome || "Sem categoria")}</span>
          <span class="pill ${p.disponivel ? "on" : "off"}">${p.disponivel ? "Ativo" : "Inativo"}</span>
        </div>
        <h3>${escapeHtml(p.nome)}</h3>
        <p>${escapeHtml(p.descricao || "Sem descrição")}</p>
        <div class="product-meta">
          <span class="price">${money(p.preco)}</span>
          <div class="row-actions">
            <button class="btn btn-secondary btn-sm" data-action="edit">Editar</button>
            <button class="btn btn-danger btn-sm" data-action="delete">Excluir</button>
          </div>
        </div>
      </div>
    </article>
  `;
}

function renderCategorias() {
  const lista = state.categorias.filter((c) => matchesSearch(`${c.nome} ${c.descricao || ""}`));
  document.getElementById("view-categorias").innerHTML = `
    <div class="toolbar">
      <p class="muted" style="margin:0">Organize o cardápio por categorias.</p>
      <button type="button" class="btn btn-primary" id="btnNovaCategoria">Nova categoria</button>
    </div>
    <section class="card panel">
      <div class="table-wrap">
        <table>
          <thead><tr><th>Nome</th><th>Descrição</th><th>Status</th><th>Ações</th></tr></thead>
          <tbody>
            ${lista.length ? lista.map((c) => `
              <tr data-id="${c.id}">
                <td>${escapeHtml(c.nome)}</td>
                <td>${escapeHtml(c.descricao || "—")}</td>
                <td><span class="pill ${c.ativa ? "on" : "off"}">${c.ativa ? "Ativa" : "Inativa"}</span></td>
                <td class="row-actions">
                  <button class="btn btn-secondary btn-sm" data-action="edit">Editar</button>
                  <button class="btn btn-danger btn-sm" data-action="delete">Excluir</button>
                </td>
              </tr>`).join("") : `<tr><td colspan="4" class="empty">Nenhuma categoria cadastrada.</td></tr>`}
          </tbody>
        </table>
      </div>
    </section>
  `;
}

function renderLanchonetes() {
  const lista = state.lanchonetes.filter((l) => matchesSearch(`${l.nome} ${l.endereco} ${l.cidade}`));
  document.getElementById("view-lanchonetes").innerHTML = `
    <div class="split-2">
      <form class="card panel form-grid" id="formLanchonete">
        <input type="hidden" id="lanchoneteId">
        <div class="panel-head"><div><h3 id="formLanchoneteTitle">Nova lanchonete</h3><p>Cadastre unidades da operação</p></div></div>
        <label class="field"><span>Nome</span><input id="lNome" required maxlength="120"></label>
        <label class="field"><span>Endereço</span><input id="lEndereco" required maxlength="200"></label>
        <label class="field"><span>Cidade</span><input id="lCidade" required maxlength="100"></label>
        <div class="toolbar-actions">
          <button class="btn btn-primary" type="submit">Salvar</button>
          <button class="btn btn-ghost" type="button" id="btnCancelLanchonete" hidden>Cancelar</button>
        </div>
      </form>
      <section class="card panel">
        <div class="panel-head"><div><h3>Unidades</h3><p>${lista.length} cadastrada(s)</p></div></div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Nome</th><th>Endereço</th><th>Cidade</th><th>Ações</th></tr></thead>
            <tbody>
              ${lista.length ? lista.map((l) => `
                <tr data-id="${l.id}">
                  <td>${escapeHtml(l.nome)}${state.unidadeId === l.id ? ' <span class="pill on">Ativa</span>' : ""}</td>
                  <td>${escapeHtml(l.endereco)}</td>
                  <td>${escapeHtml(l.cidade)}</td>
                  <td class="row-actions">
                    <button class="btn btn-secondary btn-sm" data-action="activate">Usar</button>
                    <button class="btn btn-ghost btn-sm" data-action="edit">Editar</button>
                    <button class="btn btn-danger btn-sm" data-action="delete">Excluir</button>
                  </td>
                </tr>`).join("") : `<tr><td colspan="4" class="empty">Nenhuma lanchonete cadastrada.</td></tr>`}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  `;
}

function renderClientes() {
  const lista = state.clientes.filter((c) => matchesSearch(`${c.nome} ${c.telefone || ""} ${c.email || ""}`));
  document.getElementById("view-clientes").innerHTML = `
    <div class="toolbar">
      <p class="muted" style="margin:0">Base de clientes para atendimento e delivery.</p>
      <button type="button" class="btn btn-primary" id="btnNovoCliente">Novo cliente</button>
    </div>
    <section class="card panel">
      <div class="table-wrap">
        <table>
          <thead><tr><th>Nome</th><th>Telefone</th><th>E-mail</th><th>Ações</th></tr></thead>
          <tbody>
            ${lista.length ? lista.map((c) => `
              <tr data-id="${c.id}">
                <td>${escapeHtml(c.nome)}</td>
                <td>${escapeHtml(c.telefone || "—")}</td>
                <td>${escapeHtml(c.email || "—")}</td>
                <td class="row-actions">
                  <button class="btn btn-secondary btn-sm" data-action="edit">Editar</button>
                  <button class="btn btn-danger btn-sm" data-action="delete">Excluir</button>
                </td>
              </tr>`).join("") : `<tr><td colspan="4" class="empty">Nenhum cliente cadastrado.</td></tr>`}
          </tbody>
        </table>
      </div>
    </section>
  `;
}

function renderRelatorios() {
  const d = state.dashboard || {};
  document.getElementById("view-relatorios").innerHTML = `
    <div class="grid-kpi">
      <article class="card kpi"><div class="kpi-label">Faturamento hoje</div><div class="kpi-value">${money(d.faturamentoDia)}</div></article>
      <article class="card kpi"><div class="kpi-label">Pedidos hoje</div><div class="kpi-value">${d.quantidadePedidos || 0}</div></article>
      <article class="card kpi"><div class="kpi-label">Ticket médio</div><div class="kpi-value">${money(d.ticketMedio)}</div></article>
      <article class="card kpi"><div class="kpi-label">Produtos ativos</div><div class="kpi-value">${state.produtos.filter((p) => p.disponivel).length}</div></article>
    </div>
    <section class="card panel">
      <div class="panel-head"><div><h2>Resumo operacional</h2><p>Indicadores consolidados a partir dos pedidos reais</p></div></div>
      <div class="list-soft">
        <div class="list-soft-item"><div><strong>Clientes cadastrados</strong></div><strong>${state.clientes.length}</strong></div>
        <div class="list-soft-item"><div><strong>Categorias</strong></div><strong>${state.categorias.length}</strong></div>
        <div class="list-soft-item"><div><strong>Unidades</strong></div><strong>${state.lanchonetes.length}</strong></div>
        <div class="list-soft-item"><div><strong>Pedidos totais</strong></div><strong>${state.pedidos.length}</strong></div>
      </div>
    </section>
  `;
}

function renderUsuarios() {
  if (!temPermissao("USUARIOS")) {
    document.getElementById("view-usuarios").innerHTML = emptyState("Acesso restrito", "Somente administradores gerenciam usuários.", null);
    return;
  }
  const lista = state.usuarios;
  document.getElementById("view-usuarios").innerHTML = `
    <div class="toolbar">
      <input class="input" id="buscaUsuario" placeholder="Buscar por nome, CPF ou e-mail" value="${escapeHtml(state.usuarioBusca)}" style="max-width:320px">
      <button type="button" class="btn btn-primary" id="btnNovoUsuario">Novo usuário</button>
    </div>
    <section class="card panel">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>Login</th>
              <th>Perfil</th>
              <th>Status</th>
              <th>Último acesso</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            ${lista.length ? lista.map((u) => `
              <tr data-id="${u.id}">
                <td>
                  <strong>${escapeHtml(u.nomeCompleto)}</strong><br>
                  <small class="muted">${escapeHtml(u.email)} · ${escapeHtml(u.cpf)}</small>
                </td>
                <td>${escapeHtml(u.username)}</td>
                <td><span class="pill">${PERFIL_LABEL[u.perfil] || u.perfil}</span></td>
                <td><span class="pill ${u.status === "ATIVO" ? "on" : "off"}">${u.status === "ATIVO" ? "Ativo" : "Inativo"}</span></td>
                <td>${u.ultimoAcesso ? new Date(u.ultimoAcesso).toLocaleString("pt-BR") : "—"}</td>
                <td class="row-actions">
                  <button class="btn btn-secondary btn-sm" data-action="edit">Editar</button>
                  <button class="btn btn-ghost btn-sm" data-action="toggle">${u.status === "ATIVO" ? "Bloquear" : "Ativar"}</button>
                  <button class="btn btn-ghost btn-sm" data-action="reset">Senha</button>
                  <button class="btn btn-danger btn-sm" data-action="delete">Excluir</button>
                </td>
              </tr>
            `).join("") : `<tr><td colspan="6" class="empty">Nenhum usuário encontrado.</td></tr>`}
          </tbody>
        </table>
      </div>
    </section>
  `;
}

function renderConfig() {
  const u = unidadeAtual();
  const theme = localStorage.getItem("uiTheme") || "light";
  const lang = localStorage.getItem("uiLang") || "pt";
  document.getElementById("view-configuracoes").innerHTML = `
    <div class="split-2">
      <section class="card panel form-grid">
        <div class="panel-head"><div><h3>${t("cfgUnitTitle")}</h3><p>${t("cfgUnitDesc")}</p></div></div>
        <label class="field">
          <span>${t("cfgUnitLabel")}</span>
          <select id="cfgUnidade" class="select">
            ${state.lanchonetes.length
              ? state.lanchonetes.map((l) => `<option value="${l.id}" ${u && u.id === l.id ? "selected" : ""}>${escapeHtml(l.nome)}</option>`).join("")
              : `<option value="">${t("cfgNoUnit")}</option>`}
          </select>
        </label>
        <button type="button" class="btn btn-primary" id="btnSalvarCfg">${t("cfgSaveUnit")}</button>
      </section>
      <section class="card panel form-grid">
        <div class="panel-head"><div><h3>${t("cfgAccountTitle")}</h3><p>${t("cfgAccountDesc")}</p></div></div>
        <div class="toolbar-actions">
          <button type="button" class="btn btn-secondary" id="btnCfgSenha">${t("cfgChangePassword")}</button>
          ${temPermissao("USUARIOS") ? `<button type="button" class="btn btn-secondary" id="btnCfgUsuarios">${t("cfgManageUsers")}</button>` : ""}
          <button type="button" class="btn btn-ghost" id="btnCfgLogout">${t("cfgLogout")}</button>
        </div>
      </section>
    </div>
    <section class="card panel" style="margin-top:0.9rem">
      <div class="panel-head"><div><h3>${t("cfgAppearanceTitle")}</h3><p>${t("cfgAppearanceDesc")}</p></div></div>
      <div class="form-grid two" style="padding:0 0 .4rem">
        <label class="field">
          <span>${t("cfgTheme")}</span>
          <select id="cfgTheme" class="select">
            <option value="light" ${theme === "light" ? "selected" : ""}>${t("cfgThemeLight")}</option>
            <option value="dark" ${theme === "dark" ? "selected" : ""}>${t("cfgThemeDark")}</option>
          </select>
        </label>
        <label class="field">
          <span>${t("cfgLanguage")}</span>
          <select id="cfgLang" class="select">
            <option value="pt" ${lang === "pt" ? "selected" : ""}>Português (Brasil)</option>
            <option value="en" ${lang === "en" ? "selected" : ""}>English</option>
            <option value="de" ${lang === "de" ? "selected" : ""}>Deutsch</option>
            <option value="fr" ${lang === "fr" ? "selected" : ""}>Français</option>
          </select>
        </label>
      </div>
      <div class="theme-preview" id="themePreview">
        <button type="button" class="theme-option ${theme === "light" ? "is-active" : ""}" data-theme-pick="light">${t("cfgThemeLight")}</button>
        <button type="button" class="theme-option ${theme === "dark" ? "is-active" : ""}" data-theme-pick="dark">${t("cfgThemeDark")}</button>
      </div>
      <button type="button" class="btn btn-primary" id="btnSalvarUi" style="margin-top:.8rem">${t("cfgSaveAppearance")}</button>
    </section>
  `;
}

function render() {
  switch (state.route) {
    case "dashboard": renderDashboard(); break;
    case "pedidos": renderPedidos(); break;
    case "cardapio": renderProdutosCards("view-cardapio", "Cardápio", "Itens disponíveis na unidade ativa", true); break;
    case "produtos": renderProdutosCards("view-produtos", "Produtos", "Catálogo completo com disponibilidade e categorias", true); break;
    case "categorias": renderCategorias(); break;
    case "lanchonetes": renderLanchonetes(); break;
    case "clientes": renderClientes(); break;
    case "relatorios": renderRelatorios(); break;
    case "usuarios": renderUsuarios(); break;
    case "configuracoes": renderConfig(); break;
    default: break;
  }
}

function usuarioFormHtml(usuario = null) {
  const perms = Object.keys(MODULO_LABEL);
  const selected = new Set(usuario?.permissoes || []);
  return `
    <form class="form-grid" id="modalForm">
      <div class="form-grid two">
        <label class="field"><span>Nome completo</span><input name="nomeCompleto" required value="${escapeHtml(usuario?.nomeCompleto || "")}"></label>
        <label class="field"><span>CPF</span><input name="cpf" required value="${escapeHtml(usuario?.cpf || "")}" placeholder="00000000000"></label>
      </div>
      <div class="form-grid two">
        <label class="field"><span>E-mail</span><input name="email" type="email" required value="${escapeHtml(usuario?.email || "")}"></label>
        <label class="field"><span>Telefone</span><input name="telefone" value="${escapeHtml(usuario?.telefone || "")}"></label>
      </div>
      <div class="form-grid two">
        <label class="field"><span>Usuário (login)</span><input name="username" required value="${escapeHtml(usuario?.username || "")}"></label>
        <label class="field"><span>Perfil</span>
          <select name="perfil" required>
            ${Object.entries(PERFIL_LABEL).map(([k, v]) => `<option value="${k}" ${usuario?.perfil === k ? "selected" : ""}>${v}</option>`).join("")}
          </select>
        </label>
      </div>
      ${usuario ? "" : `
      <div class="form-grid two">
        <label class="field"><span>Senha</span><input name="senha" type="password" minlength="6" placeholder="Deixe vazio para gerar"></label>
        <label class="field"><span>Confirmar senha</span><input name="confirmarSenha" type="password" minlength="6"></label>
      </div>
      <label class="perm-item"><input type="checkbox" name="gerarSenhaProvisoria" checked> Gerar senha provisória automaticamente</label>
      <label class="perm-item"><input type="checkbox" name="enviarEmailAcesso" checked> Enviar dados de acesso por e-mail</label>
      `}
      <label class="field"><span>Status</span>
        <select name="status">
          <option value="ATIVO" ${usuario?.status !== "INATIVO" ? "selected" : ""}>Ativo</option>
          <option value="INATIVO" ${usuario?.status === "INATIVO" ? "selected" : ""}>Inativo</option>
        </select>
      </label>
      <div>
        <p class="muted" style="margin:0 0 .45rem">Módulos liberados</p>
        <div class="perm-grid">
          ${perms.map((m) => `
            <label class="perm-item">
              <input type="checkbox" name="permissoes" value="${m}" ${selected.has(m) || (!usuario && m !== "USUARIOS") ? "checked" : ""}>
              ${MODULO_LABEL[m]}
            </label>
          `).join("")}
        </div>
      </div>
    </form>
  `;
}

function openUsuarioModal(usuario = null) {
  openModal(
    usuario ? "Editar usuário" : "Novo usuário",
    usuarioFormHtml(usuario),
    `<button class="btn btn-ghost" type="button" data-close>Cancelar</button>
     <button class="btn btn-primary" type="button" id="btnSalvarUsuario">Salvar</button>`
  );

  document.getElementById("btnSalvarUsuario").onclick = async () => {
    const form = document.getElementById("modalForm");
    const fd = new FormData(form);
    const permissoes = fd.getAll("permissoes");
    const payload = {
      nomeCompleto: String(fd.get("nomeCompleto") || "").trim(),
      cpf: String(fd.get("cpf") || "").trim(),
      email: String(fd.get("email") || "").trim(),
      telefone: String(fd.get("telefone") || "").trim(),
      username: String(fd.get("username") || "").trim(),
      perfil: fd.get("perfil"),
      status: fd.get("status"),
      permissoes,
      gerarSenhaProvisoria: fd.get("gerarSenhaProvisoria") === "on",
      enviarEmailAcesso: fd.get("enviarEmailAcesso") === "on",
      senha: String(fd.get("senha") || ""),
      confirmarSenha: String(fd.get("confirmarSenha") || ""),
    };
    try {
      if (usuario) {
        await API.usuarios.update(usuario.id, payload);
        toast("Usuário atualizado.");
      } else {
        const criado = await API.usuarios.create(payload);
        toast(criado.senhaProvisoria
          ? `Usuário criado. Senha provisória: ${criado.senhaProvisoria}`
          : "Usuário criado.");
      }
      closeModal();
      await refresh();
      navigate("usuarios");
    } catch (err) {
      toast(err.message, "error");
    }
  };
}

function openAlterarSenhaModal(obrigatorio = false) {
  openModal(
    obrigatorio ? "Altere sua senha provisória" : "Alterar senha",
    `<form class="form-grid" id="modalFormSenha">
      <label class="field"><span>Senha atual</span><input name="senhaAtual" type="password" required autocomplete="current-password"></label>
      <label class="field"><span>Nova senha</span><input name="novaSenha" type="password" required minlength="6" autocomplete="new-password"></label>
      <label class="field"><span>Confirmar nova senha</span><input name="confirmarSenha" type="password" required minlength="6" autocomplete="new-password"></label>
    </form>`,
    `<button class="btn btn-ghost" type="button" data-close ${obrigatorio ? "hidden" : ""}>Cancelar</button>
     <button class="btn btn-primary" type="button" id="btnSalvarSenha">Salvar senha</button>`
  );

  const salvar = async () => {
    const form = document.getElementById("modalFormSenha");
    if (!form) return;
    const data = Object.fromEntries(new FormData(form).entries());
    if (!data.senhaAtual || !data.novaSenha || !data.confirmarSenha) {
      toast("Preencha todos os campos.", "error");
      return;
    }
    if (data.novaSenha !== data.confirmarSenha) {
      toast("A confirmação da senha não confere.", "error");
      return;
    }
    try {
      await API.auth.alterarSenha(data);
      sessionStorage.removeItem("forcePasswordChange");
      toast("Senha atualizada.");
      closeModal();
      els.backdrop.hidden = true;
      els.backdrop.setAttribute("aria-hidden", "true");
      state.user = await API.auth.me();
      syncUserUi();
    } catch (err) {
      toast(err.message, "error");
    }
  };

  document.getElementById("btnSalvarSenha")?.addEventListener("click", (e) => {
    e.preventDefault();
    e.stopPropagation();
    salvar();
  });
  document.getElementById("modalFormSenha")?.addEventListener("submit", (e) => {
    e.preventDefault();
    salvar();
  });
}

function produtoFormHtml(produto = null) {
  const u = unidadeAtual();
  return `
    <form class="form-grid" id="modalForm">
      <label class="field"><span>Lanchonete</span>
        <select name="lanchoneteId" required>
          ${state.lanchonetes.map((l) => `<option value="${l.id}" ${(produto?.lanchoneteId || u?.id) === l.id ? "selected" : ""}>${escapeHtml(l.nome)}</option>`).join("")}
        </select>
      </label>
      <label class="field"><span>Nome</span><input name="nome" required maxlength="120" value="${escapeHtml(produto?.nome || "")}"></label>
      <label class="field"><span>Descrição</span><textarea name="descricao" rows="3" maxlength="500">${escapeHtml(produto?.descricao || "")}</textarea></label>
      <div class="form-grid two">
        <label class="field"><span>Preço</span><input name="preco" type="number" min="0" step="0.01" required value="${produto ? Number(produto.preco).toFixed(2) : ""}"></label>
        <label class="field"><span>Categoria</span>
          <select name="categoriaId">
            <option value="">Sem categoria</option>
            ${state.categorias.map((c) => `<option value="${c.id}" ${produto?.categoriaId === c.id ? "selected" : ""}>${escapeHtml(c.nome)}</option>`).join("")}
          </select>
        </label>
      </div>
      <label class="field"><span>URL da imagem</span><input name="imagemUrl" maxlength="500" value="${escapeHtml(produto?.imagemUrl || "")}" placeholder="https://..."></label>
      <label class="field"><span>Disponibilidade</span>
        <select name="disponivel">
          <option value="true" ${produto?.disponivel !== false ? "selected" : ""}>Ativo</option>
          <option value="false" ${produto?.disponivel === false ? "selected" : ""}>Inativo</option>
        </select>
      </label>
    </form>
  `;
}

function openProdutoModal(produto = null) {
  if (!state.lanchonetes.length) {
    toast("Cadastre uma lanchonete primeiro.", "error");
    navigate("lanchonetes");
    return;
  }
  openModal(
    produto ? "Editar produto" : "Novo produto",
    produtoFormHtml(produto),
    `<button class="btn btn-ghost" type="button" data-close>Cancelar</button>
     <button class="btn btn-primary" type="button" id="btnSalvarProduto">${produto ? "Atualizar" : "Salvar"}</button>`
  );
  document.getElementById("btnSalvarProduto").onclick = async () => {
    const form = document.getElementById("modalForm");
    const data = Object.fromEntries(new FormData(form).entries());
    const payload = {
      nome: data.nome.trim(),
      descricao: data.descricao.trim(),
      preco: Number(data.preco),
      categoriaId: data.categoriaId ? Number(data.categoriaId) : null,
      imagemUrl: data.imagemUrl.trim(),
      disponivel: data.disponivel === "true",
    };
    try {
      if (produto) {
        await API.produtos.update(Number(data.lanchoneteId), produto.id, payload);
        toast("Produto atualizado.");
      } else {
        await API.produtos.create(Number(data.lanchoneteId), payload);
        toast("Produto criado.");
      }
      closeModal();
      await refresh();
    } catch (e) {
      toast(e.message, "error");
    }
  };
}

function openPedidoModal() {
  const u = unidadeAtual();
  if (!u) {
    toast("Cadastre/selecione uma lanchonete.", "error");
    navigate("lanchonetes");
    return;
  }
  const produtosUnidade = state.produtos.filter((p) => p.lanchoneteId === u.id && p.disponivel);
  if (!produtosUnidade.length) {
    toast("Cadastre produtos disponíveis nesta unidade.", "error");
    navigate("produtos");
    return;
  }
  openModal(
    "Novo pedido",
    `<form class="form-grid" id="modalForm">
      <label class="field"><span>Cliente cadastrado (opcional)</span>
        <select name="clienteId">
          <option value="">Balcão / avulso</option>
          ${state.clientes.map((c) => `<option value="${c.id}">${escapeHtml(c.nome)}</option>`).join("")}
        </select>
      </label>
      <label class="field"><span>Nome do cliente</span><input name="nomeCliente" required placeholder="Nome para o pedido"></label>
      <label class="field"><span>Tipo de atendimento</span>
        <select name="tipoAtendimento" required>
          ${Object.entries(TIPO_LABEL).map(([k, v]) => `<option value="${k}">${v}</option>`).join("")}
        </select>
      </label>
      <label class="field"><span>Produto</span>
        <select name="lancheId" required>
          ${produtosUnidade.map((p) => `<option value="${p.id}">${escapeHtml(p.nome)} · ${money(p.preco)}</option>`).join("")}
        </select>
      </label>
      <label class="field"><span>Quantidade</span><input type="number" name="quantidade" min="1" value="1" required></label>
      <label class="field"><span>Observação</span><textarea name="observacao" rows="2" maxlength="500"></textarea></label>
    </form>`,
    `<button class="btn btn-ghost" type="button" data-close>Cancelar</button>
     <button class="btn btn-primary" type="button" id="btnSalvarPedido">Criar pedido</button>`
  );

  const clienteSelect = document.querySelector('#modalForm [name="clienteId"]');
  const nomeInput = document.querySelector('#modalForm [name="nomeCliente"]');
  clienteSelect.addEventListener("change", () => {
    const c = state.clientes.find((x) => String(x.id) === clienteSelect.value);
    if (c) nomeInput.value = c.nome;
  });

  document.getElementById("btnSalvarPedido").onclick = async () => {
    const form = document.getElementById("modalForm");
    const data = Object.fromEntries(new FormData(form).entries());
    const payload = {
      lanchoneteId: u.id,
      clienteId: data.clienteId ? Number(data.clienteId) : null,
      nomeCliente: data.nomeCliente.trim(),
      tipoAtendimento: data.tipoAtendimento,
      observacao: data.observacao.trim(),
      itens: [{ lancheId: Number(data.lancheId), quantidade: Number(data.quantidade) }],
    };
    try {
      await API.pedidos.create(payload);
      toast("Pedido criado.");
      closeModal();
      await refresh();
      navigate("pedidos");
    } catch (e) {
      toast(e.message, "error");
    }
  };
}

function openCategoriaModal(cat = null) {
  openModal(
    cat ? "Editar categoria" : "Nova categoria",
    `<form class="form-grid" id="modalForm">
      <label class="field"><span>Nome</span><input name="nome" required maxlength="120" value="${escapeHtml(cat?.nome || "")}"></label>
      <label class="field"><span>Descrição</span><textarea name="descricao" rows="3" maxlength="300">${escapeHtml(cat?.descricao || "")}</textarea></label>
      <label class="field"><span>Status</span>
        <select name="ativa">
          <option value="true" ${cat?.ativa !== false ? "selected" : ""}>Ativa</option>
          <option value="false" ${cat?.ativa === false ? "selected" : ""}>Inativa</option>
        </select>
      </label>
    </form>`,
    `<button class="btn btn-ghost" type="button" data-close>Cancelar</button>
     <button class="btn btn-primary" type="button" id="btnSalvarCategoria">Salvar</button>`
  );
  document.getElementById("btnSalvarCategoria").onclick = async () => {
    const data = Object.fromEntries(new FormData(document.getElementById("modalForm")).entries());
    const payload = { nome: data.nome.trim(), descricao: data.descricao.trim(), ativa: data.ativa === "true" };
    try {
      if (cat) await API.categorias.update(cat.id, payload);
      else await API.categorias.create(payload);
      toast(cat ? "Categoria atualizada." : "Categoria criada.");
      closeModal();
      await refresh();
    } catch (e) {
      toast(e.message, "error");
    }
  };
}

function openClienteModal(cli = null) {
  openModal(
    cli ? "Editar cliente" : "Novo cliente",
    `<form class="form-grid" id="modalForm">
      <label class="field"><span>Nome</span><input name="nome" required maxlength="120" value="${escapeHtml(cli?.nome || "")}"></label>
      <label class="field"><span>Telefone</span><input name="telefone" maxlength="30" value="${escapeHtml(cli?.telefone || "")}"></label>
      <label class="field"><span>E-mail</span><input name="email" type="email" maxlength="120" value="${escapeHtml(cli?.email || "")}"></label>
    </form>`,
    `<button class="btn btn-ghost" type="button" data-close>Cancelar</button>
     <button class="btn btn-primary" type="button" id="btnSalvarCliente">Salvar</button>`
  );
  document.getElementById("btnSalvarCliente").onclick = async () => {
    const data = Object.fromEntries(new FormData(document.getElementById("modalForm")).entries());
    const payload = { nome: data.nome.trim(), telefone: data.telefone.trim(), email: data.email.trim() };
    try {
      if (cli) await API.clientes.update(cli.id, payload);
      else await API.clientes.create(payload);
      toast(cli ? "Cliente atualizado." : "Cliente criado.");
      closeModal();
      await refresh();
    } catch (e) {
      toast(e.message, "error");
    }
  };
}

async function refresh() {
  await loadAll();
  render();
}

document.querySelector(".nav").addEventListener("click", (e) => {
  const btn = e.target.closest(".nav-link");
  if (btn) navigate(btn.dataset.route);
});

els.btnCollapse.addEventListener("click", () => {
  els.app.classList.toggle("is-collapsed");
});

els.btnMenu.addEventListener("click", () => {
  els.sidebar.classList.add("is-open");
  els.backdrop.hidden = false;
  els.backdrop.setAttribute("aria-hidden", "false");
});

els.backdrop.addEventListener("click", () => {
  els.sidebar.classList.remove("is-open");
  closeModal();
  els.backdrop.hidden = true;
  els.backdrop.setAttribute("aria-hidden", "true");
});

els.modalClose.addEventListener("click", () => {
  closeModal();
  if (!els.sidebar.classList.contains("is-open")) {
    els.backdrop.hidden = true;
    els.backdrop.setAttribute("aria-hidden", "true");
  }
});

els.modal.addEventListener("click", (e) => {
  if (e.target === els.modal || e.target.matches("[data-close]")) {
    closeModal();
    if (!els.sidebar.classList.contains("is-open")) {
      els.backdrop.hidden = true;
      els.backdrop.setAttribute("aria-hidden", "true");
    }
  }
});

document.addEventListener("keydown", (e) => {
  if (e.key === "Escape" && !els.modal.hidden) {
    closeModal();
    if (!els.sidebar.classList.contains("is-open")) {
      els.backdrop.hidden = true;
      els.backdrop.setAttribute("aria-hidden", "true");
    }
  }
});

els.globalSearch.addEventListener("input", (e) => {
  state.search = e.target.value.trim();
  render();
});

els.btnNotif.addEventListener("click", (e) => {
  e.preventDefault();
  e.stopPropagation();
  renderNotificacoes();
  togglePanel(els.notifPanel, els.btnNotif);
});

els.btnNotifPedidos?.addEventListener("click", (e) => {
  e.preventDefault();
  e.stopPropagation();
  fecharMenus();
  navigate("pedidos");
});

els.btnProfile.addEventListener("click", (e) => {
  e.preventDefault();
  e.stopPropagation();
  togglePanel(els.profileMenu, els.btnProfile);
});

els.profileMenu.addEventListener("click", async (e) => {
  e.preventDefault();
  e.stopPropagation();
  const btn = e.target.closest("button[data-action]");
  if (!btn) return;
  await executarAcaoPerfil(btn.dataset.action);
});

els.notifPanel.addEventListener("click", (e) => {
  e.stopPropagation();
  const go = e.target.closest("[data-go]");
  if (go) {
    fecharMenus();
    navigate(go.dataset.go);
  }
});

document.addEventListener("click", (e) => {
  const insideProfile = e.target.closest(".profile-wrap");
  const insideNotif = e.target.closest(".notif-wrap");
  if (!insideProfile && !insideNotif) {
    fecharMenus();
  }
});

document.getElementById("content").addEventListener("click", async (e) => {
  const go = e.target.closest("[data-go]");
  if (go) {
    navigate(go.dataset.go);
    return;
  }

  if (e.target.closest("#btnNovoUsuario")) {
    openUsuarioModal();
    return;
  }

  if (e.target.id === "buscaUsuario") return;

  const userBtn = e.target.closest("#view-usuarios [data-action]");
  if (userBtn) {
    const id = Number(userBtn.closest("tr").dataset.id);
    const user = state.usuarios.find((u) => u.id === id);
    try {
      if (userBtn.dataset.action === "edit") openUsuarioModal(user);
      if (userBtn.dataset.action === "toggle") {
        await API.usuarios.status(id, user.status === "ATIVO" ? "INATIVO" : "ATIVO");
        toast(user.status === "ATIVO" ? "Usuário bloqueado." : "Usuário ativado.");
        await refresh();
        navigate("usuarios");
      }
      if (userBtn.dataset.action === "reset") {
        const result = await API.usuarios.resetSenha(id, true);
        toast(`Nova senha provisória: ${result.senhaProvisoria}`);
      }
      if (userBtn.dataset.action === "delete") {
        if (!confirm("Excluir este usuário?")) return;
        await API.usuarios.remove(id);
        toast("Usuário excluído.");
        await refresh();
        navigate("usuarios");
      }
    } catch (err) {
      toast(err.message, "error");
    }
    return;
  }

  if (e.target.closest("#btnNovoPedido, #btnNovoPedidoEmpty")) {
    openPedidoModal();
    return;
  }
  if (e.target.closest("#btnNovoProduto, #btnNovoProdutoEmpty")) {
    openProdutoModal();
    return;
  }
  if (e.target.closest("#btnNovaCategoria")) {
    openCategoriaModal();
    return;
  }
  if (e.target.closest("#btnNovoCliente")) {
    openClienteModal();
    return;
  }
  if (e.target.closest("#btnSalvarCfg")) {
    const raw = document.getElementById("cfgUnidade")?.value;
    if (!raw) {
      toast(t("cfgNeedUnit"), "error");
      return;
    }
    const id = Number(raw);
    state.unidadeId = id;
    localStorage.setItem("unidadeId", String(id));
    syncUnidade();
    toast(t("cfgSavedUnit"));
    return;
  }

  const themePick = e.target.closest("[data-theme-pick]");
  if (themePick) {
    const picked = themePick.dataset.themePick;
    const select = document.getElementById("cfgTheme");
    if (select) select.value = picked;
    document.querySelectorAll("[data-theme-pick]").forEach((btn) => {
      btn.classList.toggle("is-active", btn.dataset.themePick === picked);
    });
    applyTheme(picked);
    return;
  }

  if (e.target.closest("#btnSalvarUi")) {
    const theme = document.getElementById("cfgTheme")?.value || "light";
    const lang = document.getElementById("cfgLang")?.value || "pt";
    applyTheme(theme);
    applyLanguage(lang);
    toast(t("cfgSavedAppearance"));
    render();
    return;
  }

  if (e.target.closest("#btnCfgSenha")) {
    openAlterarSenhaModal(false);
    return;
  }

  if (e.target.closest("#btnCfgUsuarios")) {
    navigate("usuarios");
    return;
  }

  if (e.target.closest("#btnCfgLogout")) {
    await executarAcaoPerfil("logout");
    return;
  }

  const orderBtn = e.target.closest(".order-card [data-action]");
  if (orderBtn) {
    const card = orderBtn.closest(".order-card");
    const id = Number(card.dataset.id);
    try {
      if (orderBtn.dataset.action === "advance") {
        await API.pedidos.updateStatus(id, orderBtn.dataset.next);
        toast("Status atualizado.");
      }
      if (orderBtn.dataset.action === "cancel") {
        await API.pedidos.updateStatus(id, "CANCELADO");
        toast("Pedido cancelado.");
      }
      await refresh();
    } catch (err) {
      toast(err.message, "error");
    }
    return;
  }

  const productBtn = e.target.closest(".product-card [data-action]");
  if (productBtn) {
    const card = productBtn.closest(".product-card");
    const id = Number(card.dataset.id);
    const lanchoneteId = Number(card.dataset.lanchonete);
    const produto = state.produtos.find((p) => p.id === id);
    if (productBtn.dataset.action === "edit") openProdutoModal(produto);
    if (productBtn.dataset.action === "delete") {
      if (!confirm("Excluir este produto?")) return;
      try {
        await API.produtos.remove(lanchoneteId, id);
        toast("Produto excluído.");
        await refresh();
      } catch (err) {
        toast(err.message, "error");
      }
    }
    return;
  }

  const catBtn = e.target.closest("#view-categorias [data-action]");
  if (catBtn) {
    const id = Number(catBtn.closest("tr").dataset.id);
    const cat = state.categorias.find((c) => c.id === id);
    if (catBtn.dataset.action === "edit") openCategoriaModal(cat);
    if (catBtn.dataset.action === "delete") {
      if (!confirm("Excluir categoria?")) return;
      try {
        await API.categorias.remove(id);
        toast("Categoria excluída.");
        await refresh();
      } catch (err) {
        toast(err.message, "error");
      }
    }
    return;
  }

  const cliBtn = e.target.closest("#view-clientes [data-action]");
  if (cliBtn) {
    const id = Number(cliBtn.closest("tr").dataset.id);
    const cli = state.clientes.find((c) => c.id === id);
    if (cliBtn.dataset.action === "edit") openClienteModal(cli);
    if (cliBtn.dataset.action === "delete") {
      if (!confirm("Excluir cliente?")) return;
      try {
        await API.clientes.remove(id);
        toast("Cliente excluído.");
        await refresh();
      } catch (err) {
        toast(err.message, "error");
      }
    }
    return;
  }

  const lanBtn = e.target.closest("#view-lanchonetes [data-action]");
  if (lanBtn) {
    const id = Number(lanBtn.closest("tr").dataset.id);
    const item = state.lanchonetes.find((l) => l.id === id);
    if (lanBtn.dataset.action === "activate") {
      state.unidadeId = id;
      localStorage.setItem("unidadeId", String(id));
      syncUnidade();
      toast("Unidade ativada.");
      render();
    }
    if (lanBtn.dataset.action === "edit") {
      document.getElementById("lanchoneteId").value = item.id;
      document.getElementById("lNome").value = item.nome;
      document.getElementById("lEndereco").value = item.endereco;
      document.getElementById("lCidade").value = item.cidade;
      document.getElementById("formLanchoneteTitle").textContent = "Editar lanchonete";
      document.getElementById("btnCancelLanchonete").hidden = false;
    }
    if (lanBtn.dataset.action === "delete") {
      if (!confirm("Excluir lanchonete e seus lanches?")) return;
      try {
        await API.lanchonetes.remove(id);
        toast("Lanchonete excluída.");
        await refresh();
      } catch (err) {
        toast(err.message, "error");
      }
    }
  }
});

document.getElementById("content").addEventListener("input", async (e) => {
  if (e.target.id !== "buscaUsuario") return;
  state.usuarioBusca = e.target.value.trim();
  try {
    state.usuarios = await API.usuarios.list(state.usuarioBusca);
    renderUsuarios();
  } catch (err) {
    toast(err.message, "error");
  }
});

document.getElementById("content").addEventListener("change", (e) => {
  if (e.target.id === "cfgTheme") {
    applyTheme(e.target.value);
    document.querySelectorAll("[data-theme-pick]").forEach((btn) => {
      btn.classList.toggle("is-active", btn.dataset.themePick === e.target.value);
    });
    return;
  }
  if (e.target.id === "cfgLang") {
    applyLanguage(e.target.value);
    render();
  }
});

document.getElementById("content").addEventListener("submit", async (e) => {
  if (e.target.id !== "formLanchonete") return;
  e.preventDefault();
  const id = document.getElementById("lanchoneteId").value;
  const payload = {
    nome: document.getElementById("lNome").value.trim(),
    endereco: document.getElementById("lEndereco").value.trim(),
    cidade: document.getElementById("lCidade").value.trim(),
  };
  try {
    if (id) await API.lanchonetes.update(Number(id), payload);
    else await API.lanchonetes.create(payload);
    toast(id ? "Lanchonete atualizada." : "Lanchonete criada.");
    await refresh();
  } catch (err) {
    toast(err.message, "error");
  }
});

document.getElementById("content").addEventListener("click", (e) => {
  if (e.target.id === "btnCancelLanchonete") {
    e.target.hidden = true;
    document.getElementById("formLanchonete").reset();
    document.getElementById("lanchoneteId").value = "";
    document.getElementById("formLanchoneteTitle").textContent = "Nova lanchonete";
  }
});

(async function init() {
  closeModal();
  els.backdrop.hidden = true;
  els.backdrop.setAttribute("aria-hidden", "true");
  els.sidebar.classList.remove("is-open");
  applyTheme();
  applyLanguage();

  try {
    state.user = await API.auth.me();
    syncUserUi();
    await loadAll();
    navigate("dashboard");
    applyLanguage();
    if (state.user.deveAlterarSenha || sessionStorage.getItem("forcePasswordChange") === "1") {
      openAlterarSenhaModal(true);
    }
  } catch (err) {
    if (!String(err.message).includes("Sessão")) {
      toast(err.message, "error");
    }
  }
})();
