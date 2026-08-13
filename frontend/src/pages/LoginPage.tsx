import { useState } from "react";
import type { FormEvent } from "react";
import { api } from "../services/api";

type LoginResponse = {
  userId: number;
  email: string;
  role: "AGENT" | "RESPONSABLE" | "ADMIN";
  token: string;
};

type Props = {
  onLogin: (user: LoginResponse) => void;
};

function LoginPage({ onLogin }: Props) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    try {
      const response = await api.post<LoginResponse>("/auth/login", {
        email,
        password,
      });

      onLogin(response.data);
    } catch (error) {
      console.error(error);
      setMessage("Email ou mot de passe incorrect.");
    }
  };

  return (
    <div className="login-page">

      <div className="login-card">

        <h1>Gestion des congés</h1>

        <p>
          Connectez-vous à votre espace
        </p>

        <form onSubmit={handleSubmit}>

          <label>
            Adresse email

            <input
              type="email"
              value={email}
              onChange={(e) =>
                setEmail(e.target.value)
              }
              placeholder="nom@entreprise.tn"
              required
            />
          </label>

          <label>
            Mot de passe

            <input
              type="password"
              value={password}
              onChange={(e) =>
                setPassword(e.target.value)
              }
              placeholder="password"
              required
            />
          </label>

          <button type="submit">
            Se connecter
          </button>

        </form>

        {message && (
          <p className="message error">
            {message}
          </p>
        )}

      </div>

    </div>
  );
}

export default LoginPage;
