import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import useWebSocket from "./../hooks/useWebSocket";
import AuthForm from "./../components/AuthForm";

const LoginPage = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const { stompClient, error, setError } = useWebSocket(
    "http://localhost:8080/ws"
  );
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const response = await axios.post(
        "http://localhost:8080/api/auth/login",
        { username, password },
        { withCredentials: true }
      );

      const eventId = response.data.eventId;

      if (stompClient && stompClient.connected) {
        stompClient.subscribe(`/topic/auth/login/${eventId}`, (message) => {
          const response = JSON.parse(message.body);

          if (response.token) {
            document.cookie = `token=${response.token}; Path=/; Max-Age=86400; Secure; SameSite=None`;
            setIsLoading(false);
            navigate("/");
          } else {
            setIsLoading(false);
            setError(response || "Login failed");
          }
        });
      } else {
        setIsLoading(false);
        setError("WebSocket not connected");
      }
    } catch (err) {
      setIsLoading(false);
      setError("Failed to initiate login");
    }
  };

  return (
    <AuthForm
      title="Login"
      onSubmit={handleLogin}
      error={error}
      isLoading={isLoading}
      buttonText="Login"
      setUsername={setUsername}
      setPassword={setPassword}
      username={username}
      password={password}
    />
  );
};

export default LoginPage;
