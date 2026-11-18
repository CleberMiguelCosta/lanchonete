const API = {
  async request(url, options = {}) {
    const response = await fetch(url, {
      credentials: "same-origin",
      headers: { "Content-Type": "application/json", ...(options.headers || {}) },
      ...options,
    });

    if (response.status === 401 && !url.includes("/api/auth/login")) {
      window.location.href = "/login.html";
      throw new Error("Sessão expirada");
    }

    if (!response.ok) {
      let message = "Falha na requisição.";
      try {
        const data = await response.json();
        message = data.message || data.error || message;
      } catch (_) {
        try {
          const text = await response.text();
          if (text) message = text;
        } catch (__) {
          /* ignore */
        }
      }
      throw new Error(message);
    }
    if (response.status === 204) return null;
    return response.json();
  },

  auth: {
    me: () => API.request("/api/auth/me"),
    logout: () => API.request("/api/auth/logout", { method: "POST", body: "{}" }),
    alterarSenha: (body) => API.request("/api/auth/alterar-senha", { method: "POST", body: JSON.stringify(body) }),
  },

  dashboard: () => API.request("/api/dashboard"),
  lanchonetes: {
    list: () => API.request("/api/lanchonetes"),
    get: (id) => API.request(`/api/lanchonetes/${id}`),
    create: (body) => API.request("/api/lanchonetes", { method: "POST", body: JSON.stringify(body) }),
    update: (id, body) => API.request(`/api/lanchonetes/${id}`, { method: "PUT", body: JSON.stringify(body) }),
    remove: (id) => API.request(`/api/lanchonetes/${id}`, { method: "DELETE" }),
  },
  produtos: {
    list: () => API.request("/api/produtos"),
    listByLanchonete: (id) => API.request(`/api/lanchonetes/${id}/lanches`),
    create: (lanchoneteId, body) =>
      API.request(`/api/lanchonetes/${lanchoneteId}/lanches`, { method: "POST", body: JSON.stringify(body) }),
    update: (lanchoneteId, id, body) =>
      API.request(`/api/lanchonetes/${lanchoneteId}/lanches/${id}`, { method: "PUT", body: JSON.stringify(body) }),
    remove: (lanchoneteId, id) =>
      API.request(`/api/lanchonetes/${lanchoneteId}/lanches/${id}`, { method: "DELETE" }),
  },
  categorias: {
    list: () => API.request("/api/categorias"),
    create: (body) => API.request("/api/categorias", { method: "POST", body: JSON.stringify(body) }),
    update: (id, body) => API.request(`/api/categorias/${id}`, { method: "PUT", body: JSON.stringify(body) }),
    remove: (id) => API.request(`/api/categorias/${id}`, { method: "DELETE" }),
  },
  clientes: {
    list: () => API.request("/api/clientes"),
    create: (body) => API.request("/api/clientes", { method: "POST", body: JSON.stringify(body) }),
    update: (id, body) => API.request(`/api/clientes/${id}`, { method: "PUT", body: JSON.stringify(body) }),
    remove: (id) => API.request(`/api/clientes/${id}`, { method: "DELETE" }),
  },
  pedidos: {
    list: () => API.request("/api/pedidos"),
    create: (body) => API.request("/api/pedidos", { method: "POST", body: JSON.stringify(body) }),
    updateStatus: (id, status) =>
      API.request(`/api/pedidos/${id}/status`, { method: "PUT", body: JSON.stringify({ status }) }),
    remove: (id) => API.request(`/api/pedidos/${id}`, { method: "DELETE" }),
  },
  usuarios: {
    list: (q = "") => API.request(`/api/usuarios${q ? `?q=${encodeURIComponent(q)}` : ""}`),
    create: (body) => API.request("/api/usuarios", { method: "POST", body: JSON.stringify(body) }),
    update: (id, body) => API.request(`/api/usuarios/${id}`, { method: "PUT", body: JSON.stringify(body) }),
    status: (id, status) => API.request(`/api/usuarios/${id}/status`, { method: "PUT", body: JSON.stringify({ status }) }),
    resetSenha: (id, enviarEmail = true) =>
      API.request(`/api/usuarios/${id}/redefinir-senha`, { method: "POST", body: JSON.stringify({ enviarEmail }) }),
    remove: (id) => API.request(`/api/usuarios/${id}`, { method: "DELETE" }),
    acessos: () => API.request("/api/usuarios/auditoria/acessos"),
    atividades: () => API.request("/api/usuarios/auditoria/atividades"),
  },
};
