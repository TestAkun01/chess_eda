import { useState, useEffect, useCallback } from "react";
import SockJS from "sockjs-client";
import Stomp from "stompjs";

const useWebSocket = (url, maxRetries = 3, reconnectDelay = 2000) => {
  const [stompClient, setStompClient] = useState(null);
  const [error, setError] = useState("");
  const [isConnected, setIsConnected] = useState(false);

  const connectWebSocket = useCallback(
    (retryCount = 0) => {
      const socket = new SockJS(url);
      const client = Stomp.over(socket);

      client.connect(
        {},
        (frame) => {
          console.log("Connected to WebSocket:", frame);
          setStompClient(client);
          setIsConnected(true);
          setError("");
        },
        (err) => {
          console.error("WebSocket connection error:", err);
          if (retryCount < maxRetries) {
            setTimeout(
              () => connectWebSocket(retryCount + 1),
              reconnectDelay * (retryCount + 1)
            );
          } else {
            setError("Failed to connect to WebSocket after retries");
            setIsConnected(false);
          }
        }
      );

      return client;
    },
    [url, maxRetries, reconnectDelay]
  );

  const reconnect = useCallback(() => {
    if (!isConnected) {
      console.log("Attempting to reconnect WebSocket...");
      connectWebSocket();
    }
  }, [isConnected, connectWebSocket]);

  useEffect(() => {
    const client = connectWebSocket();

    return () => {
      if (client && client.connected) {
        client.disconnect(() => {
          console.log("Disconnected from WebSocket");
          setIsConnected(false);
        });
      }
    };
  }, [connectWebSocket]);

  return { stompClient, error, setError, isConnected, reconnect };
};

export default useWebSocket;
