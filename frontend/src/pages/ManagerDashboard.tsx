import { useEffect, useState } from "react";
import axios from "axios";
import { api } from "../services/api";
import ExportMenu from "../components/ExportMenu";

type LeaveRequest = {
  id: number;
  dateDebut: string;
  dateFin: string;
  nombreJours: number;
  motif: string;
  niveauValidation: number;
  agent: {
    id: number;
    nom: string;
    prenom: string;
    service?: { nom: string } | null;
  };
};

type ValidationHistory = {
  validationId: number;
  leaveRequestId: number;
  agentId: number;
  agentNom: string;
  agentPrenom: string;
  dateDebut: string;
  dateFin: string;
  nombreJours: number;
  niveau: number;
  decision: "APPROUVEE" | "REFUSEE";
  commentaire: string | null;
  dateDecision: string;
};

function ManagerDashboard({
  activeSection,
  onPendingCountChange,
}: {
  activeSection: string;
  onPendingCountChange: (count: number) => void;
}) {
  const [requests, setRequests] = useState<LeaveRequest[]>([]);
  const [history, setHistory] = useState<ValidationHistory[]>([]);
  const [comments, setComments] = useState<Record<number, string>>({});
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error">("success");
  const [historyView, setHistoryView] = useState<"all" | "agent">("all");
  const [selectedHistoryAgentId, setSelectedHistoryAgentId] = useState<number | null>(null);
  const [historyAgentSearch, setHistoryAgentSearch] = useState("");

  const showMessage = (text: string, type: "success" | "error") => {
    setMessage(text);
    setMessageType(type);
  };

  const errorMessage = (error: unknown, fallback: string) =>
    axios.isAxiosError(error)
      ? error.response?.data?.detail ?? fallback
      : fallback;

  const loadRequests = async () => {
    try {
      const response = await api.get("/leave-requests");
      setRequests(response.data);
      onPendingCountChange(response.data.length);
    } catch (error) {
      onPendingCountChange(0);
      showMessage(errorMessage(error, "Impossible de charger les demandes."), "error");
    }
  };

  const loadHistory = async () => {
    try {
      const response = await api.get("/validations/my");
      setHistory(response.data);
    } catch (error) {
      showMessage(errorMessage(error, "Impossible de charger l'historique."), "error");
    }
  };

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void Promise.all([loadRequests(), loadHistory()]);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (activeSection === "#manager-history-agent") {
      setHistoryView("agent");
      setSelectedHistoryAgentId(null);
      setHistoryAgentSearch("");
    } else if (activeSection === "#manager-history-all") {
      setHistoryView("all");
      setSelectedHistoryAgentId(null);
      setHistoryAgentSearch("");
    }
  }, [activeSection]);

  const decide = async (id: number, decision: "approve" | "reject") => {
    try {
      await api.post(`/leave-requests/${id}/${decision}`, {
        commentaire: comments[id] || null,
      });
      setComments((current) => ({ ...current, [id]: "" }));
      showMessage(
        decision === "approve" ? "Demande approuvée avec succès." : "Demande refusée.",
        decision === "approve" ? "success" : "error"
      );
      await Promise.all([loadRequests(), loadHistory()]);
    } catch (error) {
      showMessage(errorMessage(error, "Impossible de traiter la demande."), "error");
    }
  };

  const today = new Date();
  const todayString = [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, "0"),
    String(today.getDate()).padStart(2, "0"),
  ].join("-");
  const urgentLimit = new Date(today);
  urgentLimit.setDate(urgentLimit.getDate() + 3);
  const urgentLimitString = [
    urgentLimit.getFullYear(),
    String(urgentLimit.getMonth() + 1).padStart(2, "0"),
    String(urgentLimit.getDate()).padStart(2, "0"),
  ].join("-");
  const urgentRequests = requests.filter(
    (request) => request.dateDebut >= todayString && request.dateDebut <= urgentLimitString
  );
  const historyAgents = Array.from(
    new Map(
      history.map((item) => [
        item.agentId,
        { id: item.agentId, name: `${item.agentPrenom} ${item.agentNom}` },
      ])
    ).values()
  );
  const filteredHistoryAgents = historyAgents.filter((agent) =>
    agent.name.toLowerCase().startsWith(historyAgentSearch.trim().toLowerCase())
  );
  const displayedHistory = selectedHistoryAgentId === null
    ? history
    : history.filter((item) => item.agentId === selectedHistoryAgentId);

  return (
    <div className="container">
      <h1>Espace responsable</h1>

      {activeSection === "#manager-dashboard" && <section id="manager-dashboard" className="card">
        <p className="dashboard-subtitle">
          Les demandes affichées sont celles qui vous sont assignées.
        </p>
        <div className="dashboard-kpis">
          <div className="dashboard-kpi">
            <span>Demandes à traiter</span>
            <strong>{requests.length}</strong>
          </div>
          <div className="dashboard-kpi dashboard-kpi-warning">
            <span>Demandes urgentes</span>
            <strong>{urgentRequests.length}</strong>
          </div>
          <div className="dashboard-kpi">
            <span>Dernier niveau attendu</span>
            <strong>
              {requests.length ? `Niveau ${requests[0].niveauValidation}` : "Aucun"}
            </strong>
          </div>
        </div>
        <p className="dashboard-note">
          Une demande est urgente lorsque son début est prévu dans les 3 prochains jours.
        </p>
      </section>}

      {activeSection === "#manager-requests" && <section id="manager-requests" className="card">
        <h2>Demandes à traiter</h2>

        {message && <p className={`message ${messageType}`}>{message}</p>}

        {requests.length === 0 ? (
          <p>Aucune demande à traiter.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Agent</th>
                <th>Service</th>
                <th>Période</th>
                <th>Jours</th>
                <th>Motif</th>
                <th>Niveau</th>
                <th>Commentaire</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {requests.map((request) => (
                <tr key={request.id}>
                  <td>{request.agent.prenom} {request.agent.nom}</td>
                  <td>{request.agent.service?.nom ?? "-"}</td>
                  <td>{request.dateDebut} → {request.dateFin}</td>
                  <td>{request.nombreJours}</td>
                  <td>{request.motif || "-"}</td>
                  <td>
                    Niveau {request.niveauValidation}
                    {urgentRequests.some((urgent) => urgent.id === request.id) && (
                      <span className="urgent-badge">Urgente</span>
                    )}
                  </td>
                  <td>
                    <input
                      value={comments[request.id] ?? ""}
                      onChange={(event) => setComments((current) => ({
                        ...current,
                        [request.id]: event.target.value,
                      }))}
                      placeholder="Optionnel"
                    />
                  </td>
                  <td className="actions">
                    <button
                      className="btn-approve"
                      onClick={() => decide(request.id, "approve")}
                    >
                      Accepter
                    </button>
                    <button
                      className="btn-reject"
                      onClick={() => decide(request.id, "reject")}
                    >
                      Refuser
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>}

      {activeSection.startsWith("#manager-history") && <section id="manager-history" className="card manager-history-card">
        <h2>Historique de mes validations</h2>
        <p className="history-page-intro">
          Retrouvez vos décisions de validation et consultez-les par agent lorsque nécessaire.
        </p>

        {historyView === "agent" && selectedHistoryAgentId === null && (
          <div className="history-agent-picker">
            {historyAgents.length === 0 ? (
              <>
                <h3>Sélectionner un agent</h3>
                <p>Aucun agent ne possède encore de validation dans votre historique.</p>
              </>
            ) : (
              <>
                <label className="history-agent-search-label">
                  Rechercher un agent
                  <input
                    className="history-agent-search"
                    type="search"
                    value={historyAgentSearch}
                    onChange={(event) => setHistoryAgentSearch(event.target.value)}
                    placeholder="Saisissez les premières lettres..."
                    aria-label="Rechercher un agent par son nom"
                  />
                </label>
                <h3>Sélectionner un agent</h3>
                {filteredHistoryAgents.length === 0 ? (
                  <p>Aucun agent ne correspond à votre recherche.</p>
                ) : (
                  <div className="history-agent-list">
                    {filteredHistoryAgents.map((agent) => (
                      <button
                        type="button"
                        className="history-agent-button"
                        key={agent.id}
                        onClick={() => setSelectedHistoryAgentId(agent.id)}
                      >
                        {agent.name}
                      </button>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>
        )}

        {historyView === "agent" && selectedHistoryAgentId !== null && (
          <div className="history-selected-agent">
            <button
              type="button"
              className="history-back-button"
              onClick={() => setSelectedHistoryAgentId(null)}
            >
              ← Liste des agents
            </button>
            <h3>
              Historique de {historyAgents.find((agent) => agent.id === selectedHistoryAgentId)?.name}
            </h3>
          </div>
        )}

        {(historyView === "all" || selectedHistoryAgentId !== null) && displayedHistory.length === 0 ? (
          <p>Aucune validation effectuée.</p>
        ) : (historyView === "all" || selectedHistoryAgentId !== null) && (
          <>
          <table>
            <thead>
              <tr>
                <th>Agent</th>
                <th>Période</th>
                <th>Jours</th>
                <th>Niveau</th>
                <th>Décision</th>
                <th>Commentaire</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {displayedHistory.map((item) => (
                <tr key={item.validationId}>
                  <td>{item.agentPrenom} {item.agentNom}</td>
                  <td>{item.dateDebut} → {item.dateFin}</td>
                  <td>{item.nombreJours}</td>
                  <td>Niveau {item.niveau}</td>
                  <td>
                    <span className={`status ${item.decision === "APPROUVEE" ? "VALIDEE" : "REFUSEE"}`}>
                      {item.decision}
                    </span>
                  </td>
                  <td>{item.commentaire || "-"}</td>
                  <td>{new Date(item.dateDecision).toLocaleString("fr-FR")}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <ExportMenu
            filename={selectedHistoryAgentId === null ? "toute-l-historique" : "historique-agent"}
            columns={["Agent", "Période", "Jours", "Niveau", "Décision", "Commentaire", "Date"]}
            rows={displayedHistory.map((item) => [
              `${item.agentPrenom} ${item.agentNom}`,
              `${item.dateDebut} → ${item.dateFin}`,
              item.nombreJours,
              `Niveau ${item.niveau}`,
              item.decision,
              item.commentaire || "-",
              new Date(item.dateDecision).toLocaleString("fr-FR"),
            ])}
          />
          </>
        )}
      </section>}
    </div>
  );
}

export default ManagerDashboard;
