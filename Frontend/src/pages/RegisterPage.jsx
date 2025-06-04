import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import useWebSocket from "./../hooks/useWebSocket";
import AuthForm from "./../components/AuthForm";

const RegisterPage = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const { stompClient, error, setError } = useWebSocket(
    "http://localhost:8080/ws"
  );
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const response = await axios.post(
        "http://localhost:8080/api/auth/register",
        { username, password },
        { withCredentials: true }
      );

      const eventId = response.data.eventId;

      if (stompClient && stompClient.connected) {
        const subscription = stompClient.subscribe(
          `/topic/auth/register/${eventId}`,
          (message) => {
            const response = JSON.parse(message.body);
            if (response.message === "User registered successfully") {
              setIsLoading(false);
              navigate("/login");
            } else {
              setIsLoading(false);
              setError(response || "Registration failed");
            }
            subscription.unsubscribe();
          }
        );

        setTimeout(() => {
          if (isLoading) {
            setIsLoading(false);
            setError("Registration timed out");
            subscription.unsubscribe();
          }
        }, 10000);
      } else {
        setIsLoading(false);
        setError("WebSocket not connected");
      }
    } catch (err) {
      setIsLoading(false);
      setError("Failed to initiate registration: " + err.message);
    }
  };

  return (
    <AuthForm
      title="Register"
      onSubmit={handleRegister}
      error={error}
      isLoading={isLoading}
      buttonText="Register"
      setUsername={setUsername}
      setPassword={setPassword}
      username={username}
      password={password}
    />
  );
};

export default RegisterPage;
