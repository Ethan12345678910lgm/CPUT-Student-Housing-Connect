import React, { useEffect, useState, useCallback } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { login as authenticate } from "../../services/authService";
import AppName from "../../components/shared/AppName";

const roles = [
  { id: "student", label: "Student" },
  { id: "landlord", label: "Landlord" },
  { id: "admin", label: "Administrator" },
];

function Login({ defaultRole = "" }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [role, setRole] = useState(defaultRole);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(location.state?.message || "");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const resolveRedirect = useCallback(
      (resolvedRole) => {
        switch (resolvedRole) {
          case "student":
            return "/student/dashboard";
          case "landlord":
            return "/landlord/dashboard";
          case "admin":
            return "/admin/dashboard";
          default:
            return location.state?.from?.pathname || "/";
        }
      },
      [location.state]
  );

  useEffect(() => {
    if (defaultRole && role !== defaultRole) {
      setRole(defaultRole);
    }
  }, [defaultRole, role]);

  useEffect(() => {
    if (location.state?.message) {
      setError(location.state.message);
    }
  }, [location.state]);

  const handleSubmit = async (event) => {
    event.preventDefault();

    setError("");
    if (!role) {
      setError("Select the type of account you're signing in with to continue.");
      return;
    }

    setIsSubmitting(true);
    try {
      const authenticatedUser = await authenticate(role, email, password);
      if (!authenticatedUser) {
        setError("Unable to log in. Please try again.");
        return;
      }
      const destination = resolveRedirect(authenticatedUser.role || role);
      navigate(destination, { replace: true });
    } catch (authError) {
      let message = authError?.message || "Unable to authenticate with the supplied credentials.";
      if (authError?.code === "TIMEOUT" || authError?.code === "NETWORK") {
        message =
            "We could not reach the authentication service. Ensure the Spring Boot API is running. " +
            "If it fell back to a different port, update REACT_APP_API_BASE_URL (or VITE_API_BASE_URL) " +
            "to point to that port, or restart the server with an available port.";
      }
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
      <div className="auth-page gradient-sky">
        <div className="auth-shell">
          <section className="auth-card">
            <div className="auth-card-header">
              <AppName />
              <h1>Welcome back to your housing hub</h1>
              <p>
                Choose your role and sign in to continue matching with verified listings, managing
                applications or verifying accommodation quality.
              </p>
            </div>
            <ul style={{ listStyle: "none", padding: 0, margin: 0, display: "grid", gap: "12px" }}>
              <li>One login for students, landlords and administrators.</li>
              <li>Consistent, responsive design across every page.</li>
              <li>Protected with modern authentication services.</li>
            </ul>
          </section>

          <section className="auth-card light">
            <div className="auth-card-header">
              <h1>Secure sign in</h1>
              <p>Use your role-based credentials to access the experience tailored to you.</p>
            </div>

            <form className="auth-form" onSubmit={handleSubmit}>
              <div className="form-field">
                <span>I am signing in as</span>
                <div className="auth-roles">
                  {roles.map((option) => (
                      <label
                          key={option.id}
                          className={`auth-role${role === option.id ? " selected" : ""}`}
                      >
                        <input
                            className="role-input"
                            type="radio"
                            name="role"
                            value={option.id}
                            checked={role === option.id}
                            onChange={(event) => setRole(event.target.value)}
                        />
                        {option.label}
                      </label>
                  ))}
                </div>
              </div>

              <label className="form-field">
                <span>Email address</span>
                <input
                    type="email"
                    className="input"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                    placeholder="you@example.com"
                    required
                />
              </label>

              <label className="form-field">
                <span>Password</span>
                <div className="password-field">
                  <input
                      type={showPassword ? "text" : "password"}
                      value={password}
                      onChange={(event) => setPassword(event.target.value)}
                      className="input"
                      placeholder="Enter your password"
                      required
                  />
                  <button
                      type="button"
                      className="password-toggle"
                      onClick={() => setShowPassword((previous) => !previous)}
                  >
                    {showPassword ? "Hide" : "Show"}
                  </button>
                </div>
              </label>

              {error && <div className="alert error">{error}</div>}

              <button
                  type="submit"
                  className="btn-primary"
                  disabled={isSubmitting}
              >
                {isSubmitting ? "Signing in..." : "Login"}
              </button>
            </form>

            <div className="helper-text" style={{ marginTop: 24 }}>
              <h3 style={{ margin: "0 0 8px", fontSize: 16, fontWeight: 600 }}>Need demo credentials?</h3>
              <ul style={{ listStyle: "none", padding: 0, margin: 0, display: "grid", gap: 4 }}>
                <li>Student — student@cput.ac.za / Student1234</li>
                <li>Landlord — sipho.mbeki@rentconnect.co.za / Landlord1234</li>
                <li>Admin — admin@cput-housing.co.za / Admin1234</li>
              </ul>
              <p style={{ marginTop: 18 }}>
                New here? <Link to="/signup">Create an account</Link>
              </p>
            </div>
          </section>
        </div>
      </div>
  );
}

export default Login;
