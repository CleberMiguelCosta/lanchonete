const API_URL = "/api/lanchonetes";

const form = document.getElementById("lanchoneteForm");
const formTitle = document.getElementById("form-title");
const formSubtitle = document.getElementById("form-subtitle");
const idInput = document.getElementById("id");
const nomeInput = document.getElementById("nome");
const enderecoInput = document.getElementById("endereco");
const cidadeInput = document.getElementById("cidade");
const btnSalvar = document.getElementById("btnSalvar");
const btnCancelar = document.getElementById("btnCancelar");
const btnAtualizar = document.getElementById("btnAtualizar");
const tabelaBody = document.getElementById("tabelaBody");
const totalCount = document.getElementById("totalCount");
const statusMsg = document.getElementById("statusMsg");

let statusTimer;

function mostrarStatus(mensagem, isError = false) {
  clearTimeout(statusTimer);
  statusMsg.textContent = mensagem;
  statusMsg.classList.toggle("error", isError);
  statusMsg.classList.add("visible");
  statusTimer = setTimeout(() => {
    statusMsg.classList.remove("visible");
  }, 2800);
}

function modoCriacao() {
  form.reset();
  idInput.value = "";
  formTitle.textContent = "Nova lanchonete";
  formSubtitle.textContent = "Preencha os dados e salve no cadastro.";
  btnSalvar.textContent = "Salvar";
  btnCancelar.hidden = true;
}

function modoEdicao(lanchonete) {
  idInput.value = lanchonete.id;
  nomeInput.value = lanchonete.nome;
  enderecoInput.value = lanchonete.endereco;
  cidadeInput.value = lanchonete.cidade;
  formTitle.textContent = "Editar lanchonete";
  formSubtitle.textContent = `Atualizando o registro #${lanchonete.id}.`;
  btnSalvar.textContent = "Atualizar";
  btnCancelar.hidden = false;
  nomeInput.focus();
}

function renderTabela(lista) {
  totalCount.textContent = String(lista.length);

  if (!lista.length) {
    tabelaBody.innerHTML = `
      <tr class="empty-row">
        <td colspan="5">Nenhuma lanchonete cadastrada ainda.</td>
      </tr>
    `;
    return;
  }

  tabelaBody.innerHTML = lista
    .map(
      (item) => `
      <tr data-id="${item.id}">
        <td>${item.id}</td>
        <td>${escapeHtml(item.nome)}</td>
        <td>${escapeHtml(item.endereco)}</td>
        <td>${escapeHtml(item.cidade)}</td>
        <td>
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

async function listarLanchonetes() {
  const response = await fetch(API_URL);
  if (!response.ok) {
    throw new Error("Falha ao carregar a lista.");
  }
  const data = await response.json();
  renderTabela(data);
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

    await listarLanchonetes();
    modoCriacao();
    mostrarStatus(editando ? "Lanchonete atualizada." : "Lanchonete cadastrada.");
  } catch (error) {
    mostrarStatus(error.message, true);
  } finally {
    btnSalvar.disabled = false;
  }
}

async function excluirLanchonete(id) {
  const confirmado = window.confirm(`Excluir a lanchonete #${id}?`);
  if (!confirmado) {
    return;
  }

  try {
    const response = await fetch(`${API_URL}/${id}`, { method: "DELETE" });
    if (!response.ok) {
      throw new Error("Não foi possível excluir.");
    }

    if (idInput.value === String(id)) {
      modoCriacao();
    }

    await listarLanchonetes();
    mostrarStatus("Lanchonete excluída.");
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
    await excluirLanchonete(id);
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
    } catch (error) {
      mostrarStatus(error.message, true);
    }
  }
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
  mostrarStatus(error.message, true);
});
