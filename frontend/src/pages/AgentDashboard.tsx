import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import axios from "axios";
import { api } from "../services/api";
import ExportMenu from "../components/ExportMenu";

type Balance = {
  joursAcquis: number;
  joursConsommes: number;
};

type DashboardSummary = {
  nom: string;
  prenom: string;
  email: string;
  dateEmbauche: string | null;
  moisAcquis: number;
  joursTravailles: number;
  joursAcquis: number;
  joursConsommes: number;
  joursDisponibles: number;
  annee: number;
};

type LeaveRequest = {
  id: number;
  dateDebut: string;
  dateFin: string;
  nombreJours: number;
  motif: string;
  statut: string;
  niveauValidation: number;

  agent: {
    id: number;
  };
};

type Validation = {
  niveau: number;
  decision: "APPROUVEE" | "REFUSEE";
  commentaire: string | null;
  dateDecision: string;
  validateur: {
    nom: string;
    prenom: string;
  };
};

function AgentDashboard({ activeSection }: { activeSection: string }) {
  const [agentId, setAgentId] = useState<number | null>(null);

  const [balance, setBalance] = useState<Balance | null>(null);
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [requests, setRequests] = useState<LeaveRequest[]>([]);
  const [histories, setHistories] = useState<Record<number, Validation[]>>({});

  const [dateDebut, setDateDebut] = useState("");
  const [dateFin, setDateFin] = useState("");
  const [motif, setMotif] = useState("");

  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error">("success");
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  const showMessage = (text: string, type: "success" | "error") => {
    setMessage(text);
    setMessageType(type);
  };

  const loadData = async () => {
    if (!agentId) return;

    try {
      setLoadError("");
      const [summaryResponse, requestsResponse] =
        await Promise.all([
          api.get("/agents/me/dashboard"),
          api.get("/leave-requests/my")
]);

      setSummary(summaryResponse.data);
      setBalance({
        joursAcquis: summaryResponse.data.joursAcquis,
        joursConsommes: summaryResponse.data.joursConsommes,
      });

      const loadedRequests = requestsResponse.data as LeaveRequest[];
      setRequests(loadedRequests);

      const historyEntries = await Promise.all(
        loadedRequests.map(async (request) => {
          try {
            const response = await api.get(`/leave-requests/${request.id}/history`);
            return [request.id, response.data as Validation[]] as const;
          } catch {
            return [request.id, []] as const;
          }
        })
      );
      setHistories(Object.fromEntries(historyEntries));
    } catch (error) {
      console.error(error);
      setLoadError(
        axios.isAxiosError(error)
          ? error.response?.data?.detail ?? "Impossible de charger votre espace agent."
          : "Impossible de charger votre espace agent."
      );
    } finally {
      setLoading(false);
    }
  };

   useEffect(() => {
     const loadAgent = async () => {
       try {
         const response = await api.get("/agents/me");

         setAgentId(response.data.id);
       } catch (error) {
         console.error(error);
         setLoadError(
           axios.isAxiosError(error)
             ? error.response?.data?.detail ?? "Session invalide ou agent introuvable."
             : "Session invalide ou agent introuvable."
         );
         setLoading(false);
       }
     };

     loadAgent();
   }, []);


   useEffect(() => {
     if (agentId) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      loadData();
     }
   // eslint-disable-next-line react-hooks/exhaustive-deps
   }, [agentId]);

  const createRequest = async (event: FormEvent) => {
    event.preventDefault();

    setMessage("");

    const today = new Date();
    const todayString = [
      today.getFullYear(),
      String(today.getMonth() + 1).padStart(2, "0"),
      String(today.getDate()).padStart(2, "0"),
    ].join("-");
    const currentYear = String(today.getFullYear());

    if (dateDebut < todayString) {
      showMessage("Durée passée : la date de début ne peut pas être antérieure à aujourd'hui", "error");
      return;
    }

    if (dateFin < dateDebut) {
      showMessage("Erreur de saisie : la date de fin doit être après la date de début", "error");
      return;
    }

    if (!dateDebut.startsWith(currentYear) || !dateFin.startsWith(currentYear)) {
      showMessage("Période non autorisée : les demandes pour une année future ne sont pas encore disponibles", "error");
      return;
    }

    try {
      await api.post("/leave-requests", {
        dateDebut,
        dateFin,
        motif,
      });

      setDateDebut("");
      setDateFin("");
      setMotif("");

      showMessage("Demande créée avec succès.", "success");

      await loadData();
    } catch (error) {
      console.error(error);
      showMessage(
        axios.isAxiosError(error)
          ? error.response?.data?.detail ?? "Impossible de créer la demande."
          : "Impossible de créer la demande.",
        "error"
      );
    }
  };

  const cancelRequest = async (id: number) => {
    try {
      await api.post(`/leave-requests/${id}/cancel`);

      showMessage("Demande annulée.", "success");

      await loadData();
    } catch (error) {
      console.error(error);
      showMessage("Impossible d'annuler cette demande.", "error");
    }
  };

  if (loading) {
    return (
      <div className="container">
        <p>Chargement...</p>
      </div>
    );
  }

  if (loadError || !balance) {
    return (
      <div className="container">
        <section className="card">
          <h1>Espace Agent</h1>
          <p className="error-message">
            {loadError || "Les données de votre espace sont indisponibles."}
          </p>
        </section>
      </div>
    );
  }

  const disponible =
    balance.joursAcquis - balance.joursConsommes;
  const today = new Date();
  const todayString = [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, "0"),
    String(today.getDate()).padStart(2, "0"),
  ].join("-");
  const pendingRequests = requests.filter((request) => request.statut === "EN_ATTENTE");
  const nextRequest = requests
    .filter((request) => request.dateDebut >= todayString && request.statut !== "ANNULEE")
    .sort((first, second) => first.dateDebut.localeCompare(second.dateDebut))[0];

  return (


    <div className="container">

      <h1>Espace Agent</h1>
      <p className="dashboard-subtitle">
        {summary?.prenom} {summary?.nom} · {summary?.email} ·
        Embauché le {summary?.dateEmbauche ?? "-"}
      </p>

      {/* SOLDE */}

      {activeSection === "#agent-balance" && <section id="agent-balance" className="card">
        <h2>Mon solde de congés</h2>

        <div className="dashboard-kpis">
          <div className="dashboard-kpi">
            <span>Demandes en attente</span>
            <strong>{pendingRequests.length}</strong>
          </div>
          <div className="dashboard-kpi">
            <span>Prochain congé</span>
            <strong>{nextRequest?.dateDebut ?? "Aucun"}</strong>
          </div>
          <div className="dashboard-kpi dashboard-kpi-primary">
            <span>Solde</span>
            <strong>{disponible} jours</strong>
          </div>
        </div>

        <div className="balance">

          <div>
            <strong>{balance.joursAcquis}</strong>
            <span>Jours acquis</span>
          </div>

          <div>
            <strong>{balance.joursConsommes}</strong>
            <span>Jours consommés</span>
          </div>

          <div>
            <strong>{summary?.moisAcquis}</strong>
            <span>Mois acquis</span>
          </div>

          <div>
            <strong>{summary?.joursTravailles}</strong>
            <span>Jours travaillés</span>
          </div>

        </div>
      </section>}

      {/* NOUVELLE DEMANDE */}

      {activeSection === "#agent-request" && <section id="agent-request" className="card">
        <h2>Nouvelle demande</h2>

        <form onSubmit={createRequest}>

          <label>
            Date de début

            <input
              type="date"
              value={dateDebut}
              onChange={(e) =>
                setDateDebut(e.target.value)
              }
              required
            />
          </label>

          <label>
            Date de fin

            <input
              type="date"
              value={dateFin}
              onChange={(e) =>
                setDateFin(e.target.value)
              }
              required
            />
          </label>

          <label>
            Motif

            <input
              type="text"
              value={motif}
              onChange={(e) =>
                setMotif(e.target.value)
              }
              placeholder="Motif du congé"
            />
          </label>

          <button type="submit">
            Envoyer la demande
          </button>

        </form>

        {message && <p className={`message ${messageType}`}>{message}</p>}
      </section>}

      {/* DEMANDES */}

      {activeSection === "#agent-requests" && <section id="agent-requests" className="card">

        <h2>Mes demandes</h2>

        {requests.length === 0 ? (

          <p>Aucune demande.</p>

        ) : (
          <>
          <table>

            <thead>
              <tr>
                <th>Début</th>
                <th>Fin</th>
                <th>Jours</th>
                <th>Motif</th>
                <th>Statut</th>
                <th>Progression</th>
                <th>Historique</th>
                <th>Action</th>
              </tr>
            </thead>

            <tbody>

              {requests.map((request) => (

                <tr key={request.id}>

                  <td>{request.dateDebut}</td>

                  <td>{request.dateFin}</td>

                  <td>{request.nombreJours}</td>

                  <td>{request.motif}</td>

                  <td>
                    <span className={`status ${request.statut}`}>
                      {request.statut}
                    </span>
                  </td>

                  <td>
                    {request.statut === "EN_ATTENTE"
                      ? `En attente niveau ${request.niveauValidation}`
                      : "Terminé"}
                  </td>

                  <td>
                    <details>
                      <summary>
                        {histories[request.id]?.length ?? 0} décision(s)
                      </summary>
                      {histories[request.id]?.length ? (
                        <ul className="history-list">
                          {histories[request.id].map((validation) => (
                            <li className="history-item" key={`${request.id}-${validation.niveau}`}>
                              <div className="history-item-header">
                                <strong>Niveau {validation.niveau}</strong>
                                <span className={`status ${validation.decision === "APPROUVEE" ? "VALIDEE" : "REFUSEE"}`}>
                                  {validation.decision}
                                </span>
                              </div>
                              <div className="history-item-detail">
                                <span>Validateur</span>
                                <strong>{validation.validateur.prenom} {validation.validateur.nom}</strong>
                              </div>
                              <div className="history-item-detail">
                                <span>Date</span>
                                <strong>{new Date(validation.dateDecision).toLocaleString("fr-FR")}</strong>
                              </div>
                              <div className="history-comment">
                                <span>Commentaire du responsable</span>
                                <p>{validation.commentaire || "Aucun commentaire"}</p>
                              </div>
                            </li>
                          ))}
                        </ul>
                      ) : (
                        <p>Aucune décision enregistrée.</p>
                      )}
                    </details>
                  </td>

                  <td>

                    {request.statut ===
                      "EN_ATTENTE" && (

                      <button
                        className="btn-cancel"
                        onClick={() => cancelRequest(request.id)}
                      >
                        Annuler
                      </button>

                    )}

                  </td>

                </tr>

              ))}

            </tbody>

          </table>

          <ExportMenu
            filename="mes-demandes"
            columns={["Début", "Fin", "Jours", "Motif", "Statut", "Progression"]}
            rows={requests.map((request) => [
              request.dateDebut,
              request.dateFin,
              request.nombreJours,
              request.motif || "-",
              request.statut,
              request.statut === "EN_ATTENTE"
                ? `En attente niveau ${request.niveauValidation}`
                : "Terminé",
            ])}
          />
          </>
        )}

      </section>}

    </div>
  );
}

export default AgentDashboard;
