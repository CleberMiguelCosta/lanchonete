const msg = document.getElementById("loginMsg");
const forms = {
  login: document.getElementById("formLogin"),
  criar: document.getElementById("formCriar"),
  recuperar: document.getElementById("formRecuperar"),
  redefinir: document.getElementById("formRedefinir"),
};

function setMsg(text, type = "") {
  msg.textContent = text;
  msg.className = `login-msg ${type}`;
}

function showTab(tab) {
  document.querySelectorAll(".login-tabs button").forEach((btn) => {
    const active = btn.dataset.tab === tab;
    btn.classList.toggle("is-active", active);
    btn.setAttribute("aria-selected", String(active));
  });

  Object.entries(forms).forEach(([key, form]) => {
    const active = key === tab;
    form.hidden = !active;
    form.classList.toggle("is-active", active);
  });

  setMsg("");
}

document.querySelectorAll(".login-tabs button").forEach((btn) => {
  btn.addEventListener("click", () => showTab(btn.dataset.tab));
});

forms.login.addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(forms.login).entries());
  try {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "same-origin",
      body: JSON.stringify(data),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || "Falha no login");
    }
    const user = await res.json();
    if (user.deveAlterarSenha) {
      sessionStorage.setItem("forcePasswordChange", "1");
    }
    window.location.href = "/";
  } catch (err) {
    setMsg(err.message, "error");
  }
});

forms.criar.addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(forms.criar).entries());
  if (data.senha !== data.confirmarSenha) {
    setMsg("As senhas não conferem.", "error");
    return;
  }
  try {
    const res = await fetch("/api/auth/registro", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    const body = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(body.message || "Não foi possível criar a conta");
    setMsg("Conta criada com sucesso. Faça login.", "ok");
    forms.criar.reset();
    showTab("login");
    forms.login.username.value = data.username;
  } catch (err) {
    setMsg(err.message, "error");
  }
});

forms.recuperar.addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(forms.recuperar).entries());
  try {
    const res = await fetch("/api/auth/recuperar-senha", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    const body = await res.json();
    if (!res.ok) throw new Error(body.message || "Falha na recuperação");
    setMsg(body.tokenDev ? `${body.message} Token (dev): ${body.tokenDev}` : body.message, "ok");
    if (body.tokenDev) {
      forms.redefinir.token.value = body.tokenDev;
      showTab("redefinir");
      setMsg("Use o token abaixo e defina sua nova senha.", "ok");
    }
  } catch (err) {
    setMsg(err.message, "error");
  }
});

forms.redefinir.addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(forms.redefinir).entries());
  try {
    const res = await fetch("/api/auth/redefinir-senha", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    const body = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(body.message || "Falha ao redefinir");
    setMsg("Senha redefinida. Faça login.", "ok");
    showTab("login");
  } catch (err) {
    setMsg(err.message, "error");
  }
});

showTab("login");

fetch("/api/auth/me", { credentials: "same-origin" })
  .then((r) => { if (r.ok) window.location.href = "/"; })
  .catch(() => {});
