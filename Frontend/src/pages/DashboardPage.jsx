import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { Chess } from "chess.js";
import useWebSocket from "./../hooks/useWebSocket";
import ChatBox from "./../components/ChatBox";
import GameBoard from "./../components/GameBoard";
import Piece from "chessjs/lib/piece";

const DashboardPage = () => {
  const [user, setUser] = useState(null);
  const [messages, setMessages] = useState([]);
  const [gameId, setGameId] = useState(null);
  const [game, setGame] = useState(null);
  const [gameFen, setGameFen] = useState(
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
  );
  const [isMatchmaking, setIsMatchmaking] = useState(false);
  const [playerColor, setPlayerColor] = useState(null);
  const { stompClient, error, isConnected, reconnect } = useWebSocket(
    "http://localhost:8080/ws"
  );
  const navigate = useNavigate();

  // Fetch user data
  useEffect(() => {
    axios
      .get("http://localhost:8080/api/auth/me", { withCredentials: true })
      .then((res) => setUser(res.data))
      .catch(() => navigate("/login"));
  }, [navigate]);

  // Fetch initial chat messages and subscribe to chat
  useEffect(() => {
    let subscribed = false;

    const fetchMessages = async () => {
      try {
        const res = await axios.get("http://localhost:8080/api/chat/messages", {
          withCredentials: true,
        });
        setMessages(res.data.messages);
      } catch (error) {
        console.error("Error fetching messages:", error);
      }
    };

    fetchMessages();

    if (stompClient && stompClient.connected && !subscribed) {
      stompClient.subscribe("/topic/messages", (message) => {
        const response = JSON.parse(message.body);
        const newMsg = response.data;
        setMessages((prev) => [...prev, newMsg]);
      });
      subscribed = true;
    }

    return () => {
      subscribed = false;
    };
  }, [stompClient]);

  // Subscribe to matchmaking event
  useEffect(() => {
    let subscription;
    if (stompClient && stompClient.connected && user) {
      subscription = stompClient.subscribe(
        `/topic/matchmaking/${user.userId}`,
        (message) => {
          try {
            const gameEvent = JSON.parse(message.body);
            const newGame = new Chess(
              gameEvent.fen ||
                "rnbqkbnr/pppppppp/5n1f/8/8/5N1F/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            );
            setGameId(gameEvent.id);
            setGame(newGame);
            setGameFen(newGame.fen());
            const color =
              gameEvent.playerWhite.id === user.userId ? "white" : "black";
            setPlayerColor(color);
            setIsMatchmaking(false);
          } catch (error) {
            console.error("Error parsing matchmaking message:", error);
          }
        }
      );
    }
    return () => {
      if (subscription) {
        subscription.unsubscribe();
      }
    };
  }, [stompClient, user]);

  // Game-specific WebSocket subscriptions
  useEffect(() => {
    let moveSubscription, finishSubscription;
    if (gameId && stompClient && stompClient.connected) {
      moveSubscription = stompClient.subscribe(
        `/topic/game/${gameId}/move`,
        (message) => {
          const board = JSON.parse(message.body);
          const newGame = new Chess(board.fen);
          setGame(newGame);
          setGameFen(board.fen);
        }
      );

      finishSubscription = stompClient.subscribe(
        `/topic/game/${gameId}/finish`,
        (message) => {
          const finishEvent = JSON.parse(message.body);
          alert(
            `Game Over! Winner: ${finishEvent.winnerId}, Reason: ${finishEvent.reason}`
          );
          setGameId(null);
          setGame(null);
          setGameFen(
            "rnbqkbnr/pppppppp/5n1f/8/8/5N1F/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          );
          setPlayerColor(null);
        }
      );
    }
    return () => {
      if (moveSubscription) moveSubscription.unsubscribe();
      if (finishSubscription) finishSubscription.unsubscribe();
    };
  }, [gameId, stompClient]);

  // Handle matchmaking
  const handleMatchmaking = async () => {
    setIsMatchmaking(true);
    try {
      const response = await axios.post(
        "http://localhost:8080/api/game/matchmaking/join",
        {},
        { withCredentials: true }
      );
    } catch (error) {
      console.error("Error starting matchmaking:", error);
      setIsMatchmaking(false);
    }
  };

  const handleMove = async (sourceSquare, targetSquare, promotion) => {
    if (!game || !playerColor || !user) return;

    const currentTurn = game.turn();
    if (
      (playerColor === "white" && currentTurn !== "w") ||
      (playerColor === "black" && currentTurn !== "b")
    ) {
      console.error("Not your turn!");
      return;
    }
    let promotionFE = null;
    let promotionBE = null;
    if (promotion !== undefined) {
      promotionFE = promotion.toLowerCase()[1];
      promotionBE =
        promotion[0] === "w" ? promotion[1] : promotion[1].toLowerCase();
    }
    try {
      const move = game.move({
        from: sourceSquare,
        to: targetSquare,
        promotion: promotionFE,
      });

      if (move) {
        setGameFen(game.fen());
        await axios.post(
          "http://localhost:8080/api/game/move",
          {
            gameId,
            from: sourceSquare,
            to: targetSquare,
            playerId: user.id,
            promotion: promotionBE,
          },
          { withCredentials: true }
        );
      } else {
        console.error("Invalid move");
      }
    } catch (error) {
      console.error("Error making move:", error);
    }
  };

  const handleSurrender = async () => {
    if (!gameId || !user) return;
    if (window.confirm("Are you sure you want to surrender?")) {
      try {
        await axios.post(
          `http://localhost:8080/api/game/surrender`,
          { playerId: user.id, gameId },
          { withCredentials: true }
        );
      } catch (error) {
        console.error("Error surrendering:", error);
      }
    }
  };

  return (
    <div className="p-4 max-w-4xl mx-auto">
      {user ? (
        <>
          <h1 className="text-2xl font-bold mb-4">Welcome, {user.username}</h1>
          {!isConnected && (
            <div className="mb-4 text-red-500 text-center">
              WebSocket disconnected.{" "}
              <button
                onClick={reconnect}
                className="underline text-blue-500 hover:text-blue-700"
              >
                Reconnect
              </button>
            </div>
          )}
          <div className="flex flex-col md:flex-row gap-4">
            <ChatBox
              messages={messages}
              stompClient={stompClient}
              user={user}
            />
            <GameBoard
              gameId={gameId}
              game={game}
              gameFen={gameFen}
              playerColor={playerColor}
              handleMove={handleMove}
              isMatchmaking={isMatchmaking}
              handleMatchmaking={handleMatchmaking}
              handleSurrender={handleSurrender}
            />
          </div>
        </>
      ) : (
        <p className="text-center text-gray-500">Loading...</p>
      )}
    </div>
  );
};

export default DashboardPage;
