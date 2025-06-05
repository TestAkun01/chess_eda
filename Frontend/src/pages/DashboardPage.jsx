import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { Chess } from "chess.js";
import useWebSocket from "./../hooks/useWebSocket";
import ChatBox from "./../components/ChatBox";
import GameBoard from "./../components/GameBoard";

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
  const [gameStatus, setGameStatus] = useState("");
  const [isPlayerTurn, setIsPlayerTurn] = useState(false);
  const [isResuming, setIsResuming] = useState(false);

  const { stompClient, error, isConnected, reconnect } = useWebSocket(
    "http://localhost:8080/ws"
  );
  const navigate = useNavigate();

  // Check if it's player's turn
  useEffect(() => {
    if (game && playerColor) {
      const currentTurn = game.turn();
      setIsPlayerTurn(
        (playerColor === "white" && currentTurn === "w") ||
          (playerColor === "black" && currentTurn === "b")
      );
    }
  }, [game, playerColor, gameFen]);

  // Fetch user data dan check for active game
  useEffect(() => {
    const initializeUser = async () => {
      try {
        // Fetch user data
        const userRes = await axios.get("http://localhost:8080/api/auth/me", {
          withCredentials: true,
        });
        setUser(userRes.data);

        console.log("TEST +++++++++++++++++++++++++++++");
        await checkForActiveGame();
      } catch (error) {
        console.error("Error initializing user:", error);
        navigate("/login");
      }
    };

    initializeUser();
  }, [navigate]);

  // Function untuk check active game
  const checkForActiveGame = async () => {
    try {
      setIsResuming(true);
      const response = await axios.get(
        "http://localhost:8080/api/game/resume",
        {
          withCredentials: true,
        }
      );
      console.log(response.data);
      console.log(response.data.data.hasActiveGame);
      if (response.data.data.hasActiveGame) {
        const gameData = response.data.data.game;
        console.log("Resuming active game:", gameData);

        // Setup game state
        const resumedGame = new Chess(gameData.fen);
        setGameId(gameData.id);
        setGame(resumedGame);
        setGameFen(gameData.fen);

        // Determine player color
        const color =
          gameData.playerWhite.id === user?.userId ? "white" : "black";
        setPlayerColor(color);
        setGameStatus(`Resumed game as ${color}. Welcome back!`);

        // Clear status after 3 seconds
        setTimeout(() => setGameStatus(""), 3000);
      } else {
        console.log("No active game found");
      }
    } catch (error) {
      console.error("Error checking for active game:", error);
    } finally {
      setIsResuming(false);
    }
  };

  // Handle page unload (user leaving)
  useEffect(() => {
    const handleBeforeUnload = async (event) => {
      if (gameId && user) {
        try {
          // Send disconnect signal
          await axios.post(
            "http://localhost:8080/api/game/disconnect",
            {},
            { withCredentials: true }
          );
        } catch (error) {
          console.error("Error disconnecting:", error);
        }
      }
    };

    window.addEventListener("beforeunload", handleBeforeUnload);

    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [gameId, user]);

  // Subscribe to game reconnection events
  useEffect(() => {
    let opponentDisconnectSubscription;

    if (gameId && stompClient && stompClient.connected && user) {
      opponentDisconnectSubscription = stompClient.subscribe(
        `/topic/game/${gameId}/reconnect/${user.userId}`,
        (message) => {
          try {
            const reconnectData = JSON.parse(message.body);

            setGameStatus("Opponent Reconnected!");

            setTimeout(() => setGameStatus(""), 2000);
          } catch (error) {
            console.error("Error processing reconnection:", error);
          }
        }
      );
    }

    return () => {
      if (opponentDisconnectSubscription) {
        opponentDisconnectSubscription.unsubscribe();
      }
    };
  }, [gameId, stompClient, user]);

  // Subscribe to opponent reconnection events
  useEffect(() => {
    let opponentDisconnectSubscription;

    if (gameId && stompClient && stompClient.connected && user) {
      opponentDisconnectSubscription = stompClient.subscribe(
        `/topic/game/${gameId}/disconnect/${user.userId}`,
        (message) => {
          try {
            const data = JSON.parse(message.body);
            setGameStatus("Opponent disconnected!");
            setTimeout(() => setGameStatus(""), 2000);
          } catch (error) {
            console.error("Error processing opponent reconnection:", error);
          }
        }
      );
    }

    return () => {
      if (opponentDisconnectSubscription) {
        opponentDisconnectSubscription.unsubscribe();
      }
    };
  }, [gameId, stompClient, user]);

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
      const subscription = stompClient.subscribe(
        "/topic/messages",
        (message) => {
          const response = JSON.parse(message.body);
          const newMsg = response.data;
          setMessages((prev) => [...prev, newMsg]);
        }
      );
      subscribed = true;

      return () => {
        if (subscription) {
          subscription.unsubscribe();
        }
      };
    }
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
            console.log("Matchmaking event received:", gameEvent);

            const initialFen =
              gameEvent.fen ||
              "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            const newGame = new Chess(initialFen);

            setGameId(gameEvent.id);
            setGame(newGame);
            setGameFen(newGame.fen());

            const color =
              gameEvent.playerWhite.id === user.userId ? "white" : "black";
            setPlayerColor(color);
            setIsMatchmaking(false);
            setGameStatus("Game started! Good luck!");

            if (Notification.permission === "granted") {
              new Notification("Chess Game", {
                body: `Match found! You are playing as ${color}.`,
                icon: "/chess-icon.png",
              });
            }
          } catch (error) {
            console.error("Error parsing matchmaking message:", error);
            setIsMatchmaking(false);
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
          try {
            const board = JSON.parse(message.body);
            console.log("Move received:", board);

            const newGame = new Chess(board.fen);
            setGame(newGame);
            setGameFen(board.fen);
          } catch (error) {
            console.error("Error processing move:", error);
          }
        }
      );

      finishSubscription = stompClient.subscribe(
        `/topic/game/${gameId}/finish`,
        (message) => {
          try {
            const finishEvent = JSON.parse(message.body);
            console.log("Game finished:", finishEvent);

            let statusMessage = "";
            if (finishEvent.reason === "CHECKMATE") {
              const winner =
                finishEvent.winnerId === user.userId ? "You" : "Opponent";
              statusMessage = `${winner} won by checkmate!`;
            } else if (finishEvent.reason === "SURRENDER") {
              const winner =
                finishEvent.winnerId === user.userId ? "You" : "Opponent";
              statusMessage = `${winner} won by surrender!`;
            } else if (finishEvent.reason === "DRAW") {
              statusMessage = "Game ended in a draw!";
            } else {
              statusMessage = `Game Over! Winner: ${finishEvent.winnerId}, Reason: ${finishEvent.reason}`;
            }

            setGameStatus(statusMessage);

            if (Notification.permission === "granted") {
              new Notification("Chess Game Ended", {
                body: statusMessage,
                icon: "/chess-icon.png",
              });
            }

            // Reset game state after delay
            setTimeout(() => {
              setGameId(null);
              setGame(null);
              setGameFen(
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
              );
              setPlayerColor(null);
              setGameStatus("");
            }, 5000);
          } catch (error) {
            console.error("Error processing game finish:", error);
          }
        }
      );
    }

    return () => {
      if (moveSubscription) moveSubscription.unsubscribe();
      if (finishSubscription) finishSubscription.unsubscribe();
    };
  }, [gameId, stompClient, user]);

  // Request notification permission
  useEffect(() => {
    if (Notification.permission === "default") {
      Notification.requestPermission();
    }
  }, []);

  // Handle matchmaking
  const handleMatchmaking = async () => {
    if (isMatchmaking) return;

    setIsMatchmaking(true);
    setGameStatus("Looking for opponent...");

    try {
      const response = await axios.post(
        "http://localhost:8080/api/game/matchmaking/join",
        {},
        { withCredentials: true }
      );
      console.log("Matchmaking request sent:", response.data);
    } catch (error) {
      console.error("Error starting matchmaking:", error);
      setIsMatchmaking(false);
      setGameStatus("Failed to start matchmaking. Please try again.");
      setTimeout(() => setGameStatus(""), 3000);
    }
  };

  const handleMove = async (sourceSquare, targetSquare, promotion) => {
    if (!game || !playerColor || !user || !gameId) {
      console.error("Game not ready for moves");
      return false;
    }

    const currentTurn = game.turn();
    if (!isPlayerTurn) {
      console.error("Not your turn!");
      setGameStatus("It's not your turn!");
      setTimeout(() => setGameStatus(""), 2000);
      return false;
    }

    let promotionFE = null;
    let promotionBE = null;
    if (promotion !== undefined) {
      if (typeof promotion === "string") {
        if (promotion.length === 2) {
          promotionFE = promotion[1].toLowerCase();
          promotionBE =
            promotion[0] === "w"
              ? promotion[1].toUpperCase()
              : promotion[1].toLowerCase();
        } else {
          promotionFE = promotion.toLowerCase();
          promotionBE =
            currentTurn === "w"
              ? promotion.toUpperCase()
              : promotion.toLowerCase();
        }
      }
    }

    try {
      const testGame = new Chess(game.fen());
      const move = testGame.move({
        from: sourceSquare,
        to: targetSquare,
        promotion: promotionFE,
      });

      if (!move) {
        console.error("Invalid move");
        setGameStatus("Invalid move!");
        setTimeout(() => setGameStatus(""), 2000);
        return false;
      }

      setGame(testGame);
      setGameFen(testGame.fen());

      await axios.post(
        "http://localhost:8080/api/game/move",
        {
          gameId,
          from: sourceSquare,
          to: targetSquare,
          playerId: user.userId,
          promotion: promotionBE,
        },
        { withCredentials: true }
      );

      console.log("Move sent successfully:", {
        from: sourceSquare,
        to: targetSquare,
        promotion: promotionBE,
      });
      return true;
    } catch (error) {
      console.error("Error making move:", error);
      setGame(new Chess(gameFen));
      setGameStatus("Failed to send move. Please try again.");
      setTimeout(() => setGameStatus(""), 3000);
      return false;
    }
  };

  const handleSurrender = async () => {
    if (!gameId || !user) return;

    if (
      window.confirm(
        "Are you sure you want to surrender? This will end the game immediately."
      )
    ) {
      try {
        setGameStatus("Surrendering...");

        await axios.post(
          "http://localhost:8080/api/game/surrender",
          {
            playerId: user.userId,
            gameId,
          },
          { withCredentials: true }
        );

        console.log("Surrender request sent");
      } catch (error) {
        console.error("Error surrendering:", error);
        setGameStatus("Failed to surrender. Please try again.");
        setTimeout(() => setGameStatus(""), 3000);
      }
    }
  };

  const handleCancelMatchmaking = async () => {
    if (!isMatchmaking) return;

    try {
      setIsMatchmaking(false);
      setGameStatus("");
      console.log("Matchmaking cancelled");
    } catch (error) {
      console.error("Error cancelling matchmaking:", error);
    }
  };

  // Manual resume game function
  const handleResumeGame = async () => {
    await checkForActiveGame();
  };

  return (
    <div className="p-4 max-w-6xl mx-auto">
      {user ? (
        <>
          <div className="mb-6">
            <h1 className="text-3xl font-bold mb-2">
              Welcome, {user.username}
            </h1>

            {/* Connection Status */}
            {!isConnected && (
              <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded-md">
                <div className="flex items-center justify-between">
                  <span>
                    ⚠️ WebSocket disconnected. Real-time features may not work.
                  </span>
                  <button
                    onClick={reconnect}
                    className="ml-4 px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600 transition-colors"
                  >
                    Reconnect
                  </button>
                </div>
              </div>
            )}

            {/* Resume Game Button */}
            {!gameId && !isResuming && (
              <div className="mb-4">
                <button
                  onClick={handleResumeGame}
                  className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
                >
                  Check for Active Game
                </button>
              </div>
            )}

            {/* Resuming Status */}
            {isResuming && (
              <div className="mb-4 p-3 bg-blue-100 border border-blue-400 text-blue-700 rounded-md">
                <span>🔄 Checking for active games...</span>
              </div>
            )}

            {gameStatus && (
              <div
                className={`mb-4 p-3 rounded-md ${
                  gameStatus.includes("won") ||
                  gameStatus.includes("checkmate") ||
                  gameStatus.includes("Resumed") ||
                  gameStatus.includes("Reconnected")
                    ? "bg-green-100 border border-green-400 text-green-700"
                    : gameStatus.includes("Failed") ||
                      gameStatus.includes("Invalid")
                    ? "bg-red-100 border border-red-400 text-red-700"
                    : gameStatus.includes("Looking") ||
                      gameStatus.includes("started")
                    ? "bg-blue-100 border border-blue-400 text-blue-700"
                    : "bg-yellow-100 border border-yellow-400 text-yellow-700"
                }`}
              >
                <div className="flex items-center">
                  <span className="font-medium">{gameStatus}</span>
                  {gameStatus.includes("Looking") && (
                    <button
                      onClick={handleCancelMatchmaking}
                      className="ml-4 px-2 py-1 text-sm bg-gray-500 text-white rounded hover:bg-gray-600 transition-colors"
                    >
                      Cancel
                    </button>
                  )}
                </div>
              </div>
            )}
          </div>

          <div className="flex flex-col lg:flex-row gap-6">
            <div className="lg:w-1/3">
              <ChatBox
                messages={messages}
                stompClient={stompClient}
                user={user}
              />
            </div>

            <div className="lg:w-2/3">
              <GameBoard
                gameId={gameId}
                game={game}
                gameFen={gameFen}
                playerColor={playerColor}
                handleMove={handleMove}
                isMatchmaking={isMatchmaking}
                handleMatchmaking={handleMatchmaking}
                handleSurrender={handleSurrender}
                isPlayerTurn={isPlayerTurn}
                gameStatus={gameStatus}
                setGameStatus={setGameStatus}
              />
            </div>
          </div>
        </>
      ) : (
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
            <p className="text-gray-500">Loading your dashboard...</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default DashboardPage;
