import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import axios from "axios";
import { api } from "../services/api";
import ExportMenu from "../components/ExportMenu";

type Agent = {
  id: number;
  nom: string;
  prenom: string;
  dateEmbauche: string;
  statut: "ACTIF" | "SUSPENDU";

  user: {
    email: string;
    role: "AGENT" | "RESPONSABLE" | "ADMIN";
  };

  service: {
    id: number;
    nom: string;
  } | null;

  responsableDirect?: {
    id: number;
    nom: string;
    prenom: string;
  } | null;
};

type Service = {
  id: number;
  nom: string;
};



function AdminDashboard({ activeSection }: { activeSection: string }) {
  const [agents, setAgents] = useState<Agent[]>([]);

  const [nom, setNom] = useState("");
  const [prenom, setPrenom] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [serviceId, setServiceId] = useState("");
  const [role, setRole] = useState<"AGENT" | "RESPONSABLE">("AGENT");
  const [responsableDirectId, setResponsableDirectId] = useState("");

  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error">("success");

  const showMessage = (text: string, type: "success" | "error") => {
    setMessage(text);
    setMessageType(type);
  };

    const [services, setServices] = useState<Service[]>([]);
  const [serviceNom, setServiceNom] = useState("");
  const [balanceAgentId, setBalanceAgentId] = useState("");
  const [balanceAdjustment, setBalanceAdjustment] = useState("0");

  const loadAgents = async () => {
    try {
      const response = await api.get("/admin/agents");
      setAgents(response.data);
    } catch (error) {
      console.error(error);
      showMessage("Erreur lors du chargement des agents.", "error");
    }
  };

const loadServices = async () => {
  try {
    const response = await api.get("/admin/services");
    setServices(response.data);
  } catch (error) {
    console.error(error);
    showMessage("Erreur lors du chargement des services.", "error");
  }
};

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadAgents();
    loadServices();
  }, []);


    const createService = async (event: FormEvent) => {
      event.preventDefault();

      try {
        await api.post("/admin/services", {
          nom: serviceNom,
        });

        setServiceNom("");
        showMessage("Service créé avec succès.", "success");

        await loadServices();
      } catch (error) {
        console.error(error);
        showMessage(
          axios.isAxiosError(error)
            ? error.response?.data?.detail ?? "Impossible de créer le service."
            : "Impossible de créer le service.",
          "error"
        );
      }
    };
  const createAgent = async (event: FormEvent) => {
    event.preventDefault();

    setMessage("");

    try {
      await api.post("/admin/agents", {
        nom,
        prenom,
        email,
        password,
        serviceId: Number(serviceId),
        role,
        responsableDirectId: responsableDirectId
          ? Number(responsableDirectId)
          : null,
      });

      setNom("");
      setPrenom("");
      setEmail("");
      setPassword("");
      setRole("AGENT");
      setResponsableDirectId("");

      showMessage("Agent créé avec succès.", "success");

      await loadAgents();
    } catch (error) {
      console.error(error);

      if (axios.isAxiosError(error)) {
        showMessage(
          error.response?.data?.detail ??
            "Impossible de créer l'agent.",
          "error"
        );
      } else {
        showMessage("Une erreur est survenue.", "error");
      }
    }
  };

  const updateManager = async (agentId: number, managerId: string) => {
    try {
      await api.put(`/admin/agents/${agentId}/manager`, {
        responsableDirectId: managerId ? Number(managerId) : null,
      });
      showMessage("Hiérarchie mise à jour.", "success");
      await loadAgents();
    } catch (error) {
      if (axios.isAxiosError(error)) {
        showMessage(error.response?.data?.detail ?? "Impossible de modifier la hiérarchie.", "error");
      } else {
        showMessage("Impossible de modifier la hiérarchie.", "error");
      }
    }
  };

  const updateBalance = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await api.put(`/admin/agents/${balanceAgentId}/balance`, {
        annee: new Date().getFullYear(),
        joursAjustement: Number(balanceAdjustment),
      });
      showMessage("Ajustement de solde enregistré.", "success");
      setBalanceAdjustment("0");
    } catch (error) {
      showMessage(
        axios.isAxiosError(error)
          ? error.response?.data?.detail ?? "Impossible de modifier le solde."
          : "Impossible de modifier le solde.",
        "error"
      );
    }
  };

  const updateStatus = async (agentId: number, statut: "ACTIF" | "SUSPENDU") => {
    try {
      await api.put(`/admin/agents/${agentId}/status`, { statut });
      showMessage("Statut de l'agent mis à jour.", "success");
      await loadAgents();
    } catch (error) {
      showMessage(
        axios.isAxiosError(error)
          ? error.response?.data?.detail ?? "Impossible de modifier le statut."
          : "Impossible de modifier le statut.",
        "error"
      );
    }
  };

  return (
    <div id="admin-dashboard" className="container">
      <h1>Espace administrateur</h1>

      {activeSection === "#admin-dashboard" && <section className="balance admin-summary">
        <div>
          <strong>{agents.length}</strong>
          <span>Agents et responsables</span>
        </div>
        <div>
          <strong>{agents.filter((agent) => agent.user.role === "RESPONSABLE").length}</strong>
          <span>Responsables</span>
        </div>
        <div>
          <strong>{services.length}</strong>
          <span>Services</span>
        </div>
      </section>}

        {activeSection === "#admin-services" && <section id="admin-services" className="card">
          <h2>Ajouter un service</h2>

          <form onSubmit={createService}>
            <label>
              Nom du service

              <input
                type="text"
                value={serviceNom}
                onChange={(e) => setServiceNom(e.target.value)}
                required
              />
            </label>

            <button type="submit">
              Créer le service
            </button>
          </form>

          <h2 className="subsection-title">Services existants</h2>
          {services.length === 0 ? (
            <p>Aucun service créé.</p>
          ) : (
            <div className="service-list">
              {services.map((service) => (
                <span className="service-chip" key={service.id}>
                  {service.nom}
                </span>
              ))}
            </div>
          )}
        </section>}

      {activeSection === "#admin-create-agent" && <section id="admin-create-agent" className="card">
        <h2>Ajouter un agent</h2>

        <div className="subsection-anchor">
        <form onSubmit={createAgent}>
          <label>
            Prénom
            <input
              type="text"
              value={prenom}
              onChange={(e) => setPrenom(e.target.value)}
              required
            />
          </label>

          <label>
            Nom
            <input
              type="text"
              value={nom}
              onChange={(e) => setNom(e.target.value)}
              required
            />
          </label>

          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </label>

          <label>
            Mot de passe initial
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>

          <label>
            Rôle
            <select
              value={role}
              onChange={(e) => setRole(e.target.value as "AGENT" | "RESPONSABLE")}
              required
            >
              <option value="AGENT">Agent</option>
              <option value="RESPONSABLE">Responsable</option>
            </select>
          </label>

          <label>
            Service
            <select
              value={serviceId}
              onChange={(e) => setServiceId(e.target.value)}
              required
            >
              <option value="">
                Choisir un service
              </option>

              {services.map((service) => (
                <option
                  key={service.id}
                  value={service.id}
                >
                  {service.nom}
                </option>
              ))}
            </select>
          </label>

          <label>
            Responsable direct
            <select
              value={responsableDirectId}
              onChange={(e) => setResponsableDirectId(e.target.value)}
            >
              <option value="">Aucun responsable</option>
              {agents
                .filter((agent) => agent.user.role === "RESPONSABLE")
                .map((agent) => (
                  <option key={agent.id} value={agent.id}>
                    {agent.prenom} {agent.nom}
                  </option>
                ))}
            </select>
          </label>

          <button type="submit">
            Créer l'agent
          </button>
        </form>

        {message && (
          <p className={`message ${messageType}`}>
            {message}
          </p>
        )}
        </div>
      </section>}

      {activeSection === "#admin-balance" && <section id="admin-balance" className="card">
        <h2>Ajuster le solde annuel</h2>
        <p className="form-help">
          Cet ajustement corrige exceptionnellement l’acquisition mensuelle sans la remplacer.
        </p>
        <form onSubmit={updateBalance}>
          <label>
            Agent
            <select
              value={balanceAgentId}
              onChange={(event) => setBalanceAgentId(event.target.value)}
              required
            >
              <option value="">Choisir un agent</option>
              {agents.map((agent) => (
                <option key={agent.id} value={agent.id}>
                  {agent.prenom} {agent.nom}
                </option>
              ))}
            </select>
          </label>
          <label>
            Ajustement en jours
            <input
              type="number"
              min="-30"
              max="30"
              step="0.5"
              value={balanceAdjustment}
              onChange={(event) => setBalanceAdjustment(event.target.value)}
              required
            />
          </label>
          <button type="submit">Enregistrer l’ajustement</button>
        </form>

        {message && (
          <p className={`message ${messageType}`}>
            {message}
          </p>
        )}
      </section>}

      {activeSection === "#admin-agents-list" && <section id="admin-agents-list" className="card">
        <h2>Agents</h2>

        {agents.length === 0 ? (
          <p>Aucun agent.</p>
        ) : (
          <>
          <table className="admin-agents-table">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Email</th>
                <th>Rôle</th>
                <th>Statut</th>
                <th>Service</th>
                <th>Responsable direct</th>
                <th>Date d'embauche</th>
              </tr>
            </thead>

            <tbody>
              {agents.map((agent) => (
                <tr key={agent.id}>
                  <td>
                    {agent.prenom} {agent.nom}
                  </td>

                  <td>
                    {agent.user.email}
                  </td>

                  <td>{agent.user.role}</td>

                  <td>
                    <select
                      className={`agent-status-select ${agent.statut}`}
                      value={agent.statut ?? "ACTIF"}
                      onChange={(event) => updateStatus(
                        agent.id,
                        event.target.value as "ACTIF" | "SUSPENDU"
                      )}
                    >
                      <option value="ACTIF">Actif</option>
                      <option value="SUSPENDU">Suspendu</option>
                    </select>
                  </td>

                  <td>
                    {agent.service?.nom ?? "-"}
                  </td>

                  <td>
                    <select
                      value={agent.responsableDirect?.id ?? ""}
                      onChange={(e) => updateManager(agent.id, e.target.value)}
                    >
                      <option value="">Aucun</option>
                      {agents
                        .filter((candidate) =>
                          candidate.user.role === "RESPONSABLE" && candidate.id !== agent.id
                        )
                        .map((candidate) => (
                          <option key={candidate.id} value={candidate.id}>
                            {candidate.prenom} {candidate.nom}
                          </option>
                        ))}
                    </select>
                  </td>

                  <td>
                    {agent.dateEmbauche}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <ExportMenu
            filename="agents-et-responsables"
            columns={["Nom", "Email", "Rôle", "Statut", "Service", "Responsable direct", "Date d'embauche"]}
            rows={agents.map((agent) => [
              `${agent.prenom} ${agent.nom}`,
              agent.user.email,
              agent.user.role,
              agent.statut,
              agent.service?.nom ?? "-",
              agent.responsableDirect
                ? `${agent.responsableDirect.prenom} ${agent.responsableDirect.nom}`
                : "-",
              agent.dateEmbauche,
            ])}
          />
          </>
        )}
      </section>}
    </div>
  );
}

export default AdminDashboard;
