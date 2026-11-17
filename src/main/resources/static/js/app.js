const API_URL = "/api/lanchonetes";

const FOOD_IMAGES = [
  "/images/hero.jpg",
  "/images/burger.jpg",
  "/images/sandwich.jpg",
  "/images/cheese.jpg",
  "/images/hotdog.jpg",
  "/images/plate.jpg",
];

const FALLBACK_IMAGE = "/images/hero.jpg";

const form = document.getElementById("lanchoneteForm");
const formTitle = document.getElementById("form-title");
const formSubtitle = document.getElementById("form-subtitle");
const formModeLabel = document.getElementById("form-mode-label");
const idInput = document.getElementById("id");
const nomeInput = document.getElementById("nome");
const enderecoInput = document.getElementById("endereco");
const cidadeInput = document.getElementById("cidade");
const btnSalvar = document.getElementById("btnSalvar");
const btnCancelar = document.getElementById("btnCancelar");
const btnAtualizar = document.getElementById("btnAtualizar");
const tabelaBody = document.getElementById("tabelaBody");
const totalCount = document.getElementById("totalCount");
const shownCount = document.getElementById("shownCount");
const statusMsg = document.getElementById("statusMsg");
const buscaInput = document.getElementById("busca");
const tableWrap = document.getElementById("tableWrap");
const emptyState = document.getElementById("emptyState");
const emptySearch = document.getElementById("emptySearch");
const confirmDialog = document.getElementById("confirmDialog");
const confirmText = document.getElementById("confirm-text");

let statusTimer;
let cache = [];
let pendingDeleteId = null;

function imagemLanche(id) {
  const idx = Math.abs(Number(id) || 0) % FOOD_IMAGES.length;
  return FOOD_IMAGES[idx];
}

function mostrarStatus(mensagem, isError = false) {
  clearTimeout(statusTimer);
  statusMsg.textContent = mensagem;
  statusMsg.classList.toggle("error", isError);
  statusMsg.classList.add("visible");
  statusTimer = setTimeout(() => {
    statusMsg.classList.remove("visible");
  }, 2800);
}

function setTableState(state) {
  tableWrap.dataset.state = state;
  emptyState.hidden = state !== "empty";
  emptySearch.hidden = state !== "no-results";
}

function modoCriacao() {
  form.reset();
  idInput.value = "";
  formModeLabel.textContent = "Novo";
  formTitle.textContent = "Cadastrar";
  formSubtitle.textContent = "Nome, endereço e cidade.";
  btnSalvar.querySelector("span").textContent = "Salvar";
  btnCancelar.hidden = true;
}

function modoEdicao(lanchonete) {
  idInput.value = lanchonete.id;
  nomeInput.value = lanchonete.nome;
  enderecoInput.value = lanchonete.endereco;
  cidadeInput.value = lanchonete.cidade;
  formModeLabel.textContent = "Editar";
  formTitle.textContent = `Unidade #${lanchonete.id}`;
  formSubtitle.textContent = "Altere os dados e salve.";
  btnSalvar.querySelector("span").textContent = "Atualizar";
  btnCancelar.hidden = false;
  nomeInput.focus();
}

function filtrarLista(lista) {
  const termo = buscaInput.value.trim().toLowerCase();
  if (!termo) return lista;
  return lista.filter((item) => {
    const blob = `${item.nome} ${item.cidade} ${item.endereco}`.toLowerCase();
    return blob.includes(termo);
  });
}

function renderTabela(lista) {
  const filtrada = filtrarLista(lista);
  totalCount.textContent = String(lista.length);
  if (shownCount) {
    shownCount.textContent = String(filtrada.length);
  }

  if (!lista.length) {
    tabelaBody.innerHTML = "";
    setTableState("empty");
    return;
  }

  if (!filtrada.length) {
    tabelaBody.innerHTML = "";
    setTableState("no-results");
    return;
  }

  setTableState("ready");
  tabelaBody.innerHTML = filtrada
    .map(
      (item) => `
      <tr data-id="${item.id}">
        <td>
          <div class="unit-cell">
            <img class="unit-thumb" src="${imagemLanche(item.id)}" alt="" width="40" height="40" loading="lazy" decoding="async" onerror="this.onerror=null;this.src='${FALLBACK_IMAGE}'">
            <div>
              <div class="unit-name">${escapeHtml(item.nome)}</div>
              <div class="unit-id">#${item.id}</div>
            </div>
          </div>
        </td>
        <td>${escapeHtml(item.endereco)}</td>
        <td class="city">${escapeHtml(item.cidade)}</td>
        <td class="col-actions">
          <div class="row-actions">
            <button type="button" class="btn btn-edit" data-action="edit">Editar</button>
            <button type="button" class="btn btn-delete" data-action="delete">Excluir</button>
          </div>
        </td>
      </tr>
    `
    )
    .join("");
}

function escapeHtml(texto) {
  return String(texto)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

async function listarLanchonetes({ silent = false } = {}) {
  if (!silent) {
    setTableState("loading");
  }

  const response = await fetch(API_URL);
  if (!response.ok) {
    throw new Error("Falha ao carregar a lista.");
  }

  cache = await response.json();
  renderTabela(cache);
}

async function salvarLanchonete(event) {
  event.preventDefault();

  const payload = {
    nome: nomeInput.value.trim(),
    endereco: enderecoInput.value.trim(),
    cidade: cidadeInput.value.trim(),
  };

  if (!payload.nome || !payload.endereco || !payload.cidade) {
    mostrarStatus("Preencha todos os campos.", true);
    return;
  }

  const id = idInput.value;
  const editando = Boolean(id);
  const url = editando ? `${API_URL}/${id}` : API_URL;
  const method = editando ? "PUT" : "POST";

  btnSalvar.disabled = true;

  try {
    const response = await fetch(url, {
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw new Error(editando ? "Não foi possível atualizar." : "Não foi possível salvar.");
    }

    await listarLanchonetes({ silent: true });
    modoCriacao();
    mostrarStatus(editando ? "Atualizado." : "Salvo.");
  } catch (error) {
    mostrarStatus(error.message, true);
  } finally {
    btnSalvar.disabled = false;
  }
}

function pedirExclusao(id, nome) {
  pendingDeleteId = id;
  confirmText.textContent = `Remover “${nome}” (#${id})?`;
  confirmDialog.showModal();
}

async function excluirLanchonete(id) {
  try {
    const response = await fetch(`${API_URL}/${id}`, { method: "DELETE" });
    if (!response.ok) {
      throw new Error("Não foi possível excluir.");
    }

    if (idInput.value === String(id)) {
      modoCriacao();
    }

    await listarLanchonetes({ silent: true });
    mostrarStatus("Excluído.");
  } catch (error) {
    mostrarStatus(error.message, true);
  }
}

tabelaBody.addEventListener("click", async (event) => {
  const botao = event.target.closest("button[data-action]");
  if (!botao) {
    return;
  }

  const linha = botao.closest("tr[data-id]");
  const id = linha?.dataset.id;
  if (!id) {
    return;
  }

  if (botao.dataset.action === "delete") {
    const nome = linha.querySelector(".unit-name")?.textContent?.trim() || `registro ${id}`;
    pedirExclusao(id, nome);
    return;
  }

  if (botao.dataset.action === "edit") {
    try {
      const response = await fetch(`${API_URL}/${id}`);
      if (!response.ok) {
        throw new Error("Não foi possível carregar o registro.");
      }
      const lanchonete = await response.json();
      modoEdicao(lanchonete);
      form.scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (error) {
      mostrarStatus(error.message, true);
    }
  }
});

confirmDialog.addEventListener("close", async () => {
  if (confirmDialog.returnValue !== "confirm" || !pendingDeleteId) {
    pendingDeleteId = null;
    return;
  }

  const id = pendingDeleteId;
  pendingDeleteId = null;
  await excluirLanchonete(id);
});

buscaInput.addEventListener("input", () => {
  renderTabela(cache);
});

form.addEventListener("submit", salvarLanchonete);
btnCancelar.addEventListener("click", modoCriacao);
btnAtualizar.addEventListener("click", async () => {
  try {
    await listarLanchonetes();
    mostrarStatus("Lista atualizada.");
  } catch (error) {
    mostrarStatus(error.message, true);
  }
});

listarLanchonetes().catch((error) => {
  setTableState("empty");
  mostrarStatus(error.message, true);
});