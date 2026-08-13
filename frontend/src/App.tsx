import { useEffect, useState } from "react";

import LoginPage from "./pages/LoginPage";
import AgentDashboard from "./pages/AgentDashboard";
import ManagerDashboard from "./pages/ManagerDashboard";
import AdminDashboard from "./pages/AdminDashboard";
import { api } from "./services/api";

import "./App.css";

const INACTIVITY_TIMEOUT_MS = 5 * 60 * 1000;

type User = {
  userId: number;
  email: string;
  role: "AGENT" | "RESPONSABLE" | "ADMIN";
  token: string;
};

function App() {
  const [user, setUser] = useState<User | null>(() => {
    const savedUser = localStorage.getItem("user");

    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [canRequestLeave, setCanRequestLeave] = useState(false);
  const [activeSection, setActiveSection] = useState("");
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [adminAgentsExpanded, setAdminAgentsExpanded] = useState(false);
  const [superiorName, setSuperiorName] = useState("");
  const [pendingRequestsCount, setPendingRequestsCount] = useState(0);

  useEffect(() => {
    if (user) {
      localStorage.setItem("user", JSON.stringify(user));
    } else {
      localStorage.removeItem("user");
    }
  }, [user]);

  useEffect(() => {
    const updateActiveSection = () => {
      setActiveSection(window.location.hash);
      if (window.location.hash !== "#admin-agents") {
        setSidebarOpen(false);
      }
    };

    updateActiveSection();
    window.addEventListener("hashchange", updateActiveSection);

    return () => window.removeEventListener("hashchange", updateActiveSection);
  }, []);

  useEffect(() => {
    if (!user) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setCanRequestLeave(false);
      setSuperiorName("");
      setPendingRequestsCount(0);
      setSidebarOpen(false);
      return;
    }

    if (user.role === "ADMIN") {
      setCanRequestLeave(false);
      setSuperiorName("");
      setPendingRequestsCount(0);
      return;
    }

    api.get("/agents/me")
      .then((response) => {
        const superior = response.data.responsableDirect;
        setSuperiorName(superior ? `${superior.prenom} ${superior.nom}` : "");
        setCanRequestLeave(user.role === "AGENT" || Boolean(superior));
      })
      .catch(() => {
        setCanRequestLeave(false);
        setSuperiorName("");
        setPendingRequestsCount(0);
      });
  }, [user]);

  useEffect(() => {
    if (!user) {
      return;
    }

    let timeoutId: number;

    const logoutAfterInactivity = () => {
      window.clearTimeout(timeoutId);
      localStorage.removeItem("user");
      setSidebarOpen(false);
      setActiveSection("");
      setUser(null);
      window.history.replaceState(null, "", window.location.pathname);
    };

    const resetInactivityTimer = () => {
      window.clearTimeout(timeoutId);
      timeoutId = window.setTimeout(logoutAfterInactivity, INACTIVITY_TIMEOUT_MS);
    };

    const activityEvents = ["mousemove", "mousedown", "keydown", "touchstart", "scroll"];
    activityEvents.forEach((eventName) => {
      window.addEventListener(eventName, resetInactivityTimer, { passive: true });
    });
    resetInactivityTimer();

    return () => {
      window.clearTimeout(timeoutId);
      activityEvents.forEach((eventName) => {
        window.removeEventListener(eventName, resetInactivityTimer);
      });
    };
  }, [user]);

  const logout = () => {
    setSidebarOpen(false);
    setActiveSection("");
    window.history.replaceState(null, "", window.location.pathname);
    setUser(null);
  };

  if (!user) {
    return (
      <LoginPage
        onLogin={(loggedUser) => {
          setSidebarOpen(false);
          setActiveSection("");
          window.history.replaceState(null, "", window.location.pathname);
          setUser(loggedUser);
        }}
      />
    );
  }

  const navigation = user.role === "AGENT"
    ? [
        { label: "Tableau de bord", href: "#agent-balance" },
        { label: "Nouvelle demande", href: "#agent-request" },
        { label: "Mes demandes", href: "#agent-requests" },
      ]
    : user.role === "RESPONSABLE"
      ? [
          { label: "Tableau de bord", href: "#manager-dashboard" },
          { label: "Demandes à traiter", href: "#manager-requests" },
          { label: "Historique", href: "#manager-history" },
          ...(canRequestLeave
            ? [
                { label: "Nouvelle demande", href: "#agent-request" },
                { label: "Mes demandes", href: "#agent-requests" },
              ]
            : []),
        ]
      : [
          { label: "Tableau de bord", href: "#admin-dashboard" },
          { label: "Agents", href: "#admin-agents" },
          { label: "Services", href: "#admin-services" },
        ];
  const adminAgentSubsections = [
    "#admin-create-agent",
    "#admin-balance",
    "#admin-agents-list",
  ];
  const selectedSection = adminAgentSubsections.includes(activeSection)
    ? activeSection
    : navigation.some((item) => item.href === activeSection && item.href !== "#admin-agents")
      ? activeSection
      : navigation[0].href;

  return (
    <div className={`app-shell ${sidebarOpen ? "sidebar-open" : ""}`}>
      <aside className={`sidebar ${sidebarOpen ? "open" : "closed"}`}>
        <div className="brand">
          <span className="brand-mark">GC</span>
          <span>Gestion des congés</span>
        </div>
        <nav className="sidebar-nav" aria-label="Navigation principale">
          {navigation.map((item) => {
            const isAdminAgents = user.role === "ADMIN" && item.href === "#admin-agents";

            return (
              <div className="nav-group" key={item.label}>
                {isAdminAgents ? (
                  <button
                    className={`nav-item nav-toggle ${
                      adminAgentSubsections.includes(selectedSection) ? "active" : ""
                    }`}
                    aria-expanded={adminAgentsExpanded}
                    onClick={() => setAdminAgentsExpanded((isExpanded) => !isExpanded)}
                  >
                    <span>{item.label}</span>
                    <span className="nav-arrow" aria-hidden="true">›</span>
                  </button>
                ) : (
                  <a
                    className={`nav-item ${selectedSection === item.href ? "active" : ""}`}
                    href={item.href}
                    onClick={() => setSidebarOpen(false)}
                  >
                    <span>{item.label}</span>
                    {item.href === "#manager-requests" && pendingRequestsCount > 0 && (
                      <span className="nav-count" aria-label={`${pendingRequestsCount} demande(s) à traiter`}>
                        {pendingRequestsCount > 99 ? "99+" : pendingRequestsCount}
                      </span>
                    )}
                  </a>
                )}
                {isAdminAgents && adminAgentsExpanded && (
                  <div className="sidebar-subnav">
                    <a href="#admin-create-agent" onClick={() => setSidebarOpen(false)}>
                      Créer un agent
                    </a>
                    <a href="#admin-balance" onClick={() => setSidebarOpen(false)}>
                      Ajuster le solde
                    </a>
                    <a href="#admin-agents-list" onClick={() => setSidebarOpen(false)}>
                      Liste des agents
                    </a>
                  </div>
                )}
              </div>
            );
          })}
        </nav>
        <button className="sidebar-logout" onClick={logout}>
          Déconnexion
        </button>
      </aside>

      {sidebarOpen && (
        <button
          className="sidebar-overlay"
          aria-label="Fermer le menu"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <div className="app-content">
        <header className="topbar">
          <div>
            <button
              className="menu-button"
              aria-label={sidebarOpen ? "Fermer le menu" : "Ouvrir le menu"}
              aria-expanded={sidebarOpen}
              onClick={() => setSidebarOpen((isOpen) => !isOpen)}
            >
              ☰
            </button>
            {superiorName && <span>Responsable supérieur : {superiorName}</span>}
            <strong>{user.role}</strong>
            <span>{user.email}</span>
          </div>
        </header>

      {user.role === "RESPONSABLE" && !selectedSection.startsWith("#agent-") && (
        <ManagerDashboard
          activeSection={selectedSection}
          onPendingCountChange={setPendingRequestsCount}
        />
      )}

      {user.role === "AGENT" ||
      (user.role === "RESPONSABLE" && canRequestLeave && selectedSection.startsWith("#agent-")) ? (
        <AgentDashboard activeSection={selectedSection} />
      ) : null}

      {user.role === "ADMIN" && <AdminDashboard activeSection={selectedSection} />}
      </div>
    </div>
  );
}

export default App;
