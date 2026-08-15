import { useRef, useState } from "react";
import type { FormEvent, PointerEvent } from "react";
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
  const loginPageRef = useRef<HTMLDivElement>(null);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");

  const handlePointerMove = (event: PointerEvent<HTMLDivElement>) => {
    const bounds = event.currentTarget.getBoundingClientRect();
    const centerX = bounds.left + bounds.width / 2;
    const centerY = bounds.top + bounds.height / 2;
    const offsetX = (event.clientX - centerX) * 0.05;
    const offsetY = (event.clientY - centerY) * 0.05;

    loginPageRef.current?.style.setProperty("--cursor-x", `${offsetX}px`);
    loginPageRef.current?.style.setProperty("--cursor-y", `${offsetY}px`);
  };

  const handlePointerLeave = () => {
    loginPageRef.current?.style.setProperty("--cursor-x", "0px");
    loginPageRef.current?.style.setProperty("--cursor-y", "0px");
  };

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
    <div
      ref={loginPageRef}
      className="login-page"
      onPointerMove={handlePointerMove}
      onPointerLeave={handlePointerLeave}
    >

      <div className="login-particles" aria-hidden="true">
        <span className="login-particle" />
        <span className="login-particle" />
        <span className="login-particle" />
        <span className="login-particle" />
        <span className="login-particle" />
        <span className="login-particle" />
        <span className="login-particle" />
        <span className="login-particle" />
        <span className="login-particle" />
      </div>

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
