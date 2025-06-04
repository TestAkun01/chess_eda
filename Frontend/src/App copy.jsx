import React, { useState, useEffect, useRef } from "react";
import ChessBoard from "./components/ChessBoard";
import { Chess } from "chess.js";
import user from "./assets/user.jpeg";
import axios from "axios";
import SockJS from "sockjs-client";
import Stomp from "stompjs";

const App = () => {
  const [game, setGame] = useState(new Chess());
  const [board, setBoard] = useState([]);
  const [matchId, setMatchId] = useState(null);
  const dragStartPos = useRef(null);
  const stompClientRef = useRef(null);

  const refreshBoard = (chessInstance) => {
    const newBoard = chessInstance.board().map((row) =>
      row.map((piece) => {
        if (!piece) return null;
        return piece.color === "w" ? piece.type.toUpperCase() : piece.type;
      })
    );
    setBoard(newBoard);
  };

  const handleDropSquare = (e, toPos) => {
    const from = dragStartPos.current;
    const move = game.move({ from, to: toPos, promotion: "q" });
    if (!move) return; // invalid move, jangan lanjut

    axios
      .post(`http://localhost:8080/api/game/${matchId}/move`, {
        fen: game.fen(),
        id: matchId,
        from: from,
        to: toPos,
      })
      .then((response) => {
        // Setelah kirim langkah ke server, jangan update UI langsung,
        // tunggu update dari WebSocket supaya sinkron realtime
      })
      .catch((err) => {
        console.error("Error sending move:", err);
        // Bisa rollback move di game.undo() kalau mau
      });
  };

  const handleDragStart = (e, fromPos) => {
    dragStartPos.current = fromPos;
  };

  const handleSquareClick = (row, col) => {
    console.log("Clicked:", row, col);
  };

  // Load game data awal
  useEffect(() => {
    axios
      .get("http://localhost:8080/api/game/1")
      .then((response) => {
        setMatchId(response.data.id);
        const fen = response.data?.fen;
        if (fen && fen !== "startpos") {
          try {
            const newGame = new Chess();
            newGame.load(fen);
            setGame(newGame); // perbarui state game
            console.log(game.isCheckmate());
            refreshBoard(newGame);
          } catch (e) {
            console.error("FEN invalid:", fen);
          }
        } else {
          refreshBoard(game); // posisi startpos
        }
      })
      .catch((err) => {
        console.error("Gagal mengambil data game:", err);
      });
  }, []);

  // Setup WebSocket subscription saat matchId siap
  useEffect(() => {
    if (!matchId) return;

    const socket = new SockJS("http://localhost:8080/ws");
    const stompClient = Stomp.over(socket);
    stompClientRef.current = stompClient;

    stompClient.connect(
      {},
      () => {
        console.log("Connected to WebSocket");

        stompClient.subscribe(`/topic/game/${matchId}`, (message) => {
          const fen = message.body;
          if (!fen) return;

          try {
            const newGame = new Chess();
            newGame.load(fen);
            setGame(newGame);
            refreshBoard(newGame);
          } catch (e) {
            console.error("Invalid FEN from WebSocket:", fen);
          }
        });
      },
      (error) => {
        console.error("WebSocket connection error:", error);
      }
    );

    return () => {
      if (stompClient) {
        stompClient.disconnect(() => {
          console.log("Disconnected WebSocket");
        });
      }
    };
  }, [matchId]);

  return (
    <div className="flex justify-center items-center w-screen">
      <div className="flex flex-col gap-[10px]">
        <div className="flex gap-[10px]">
          <img className="h-[60px] rounded-[8px]" src={user} alt="" />
          <div className="gap-[1px] flex flex-col">
            <span className="font-bold">Nama</span>
            <span>Rating: 100</span>
          </div>
        </div>

        <div>
          <ChessBoard
            board={board}
            onSquareClick={handleSquareClick}
            onDragStart={handleDragStart}
            onDropSquare={handleDropSquare}
          />
        </div>

        <div className="flex gap-[10px]">
          <img className="h-[60px] rounded-[8px]" src={user} alt="" />
          <div className="gap-[1px] flex flex-col">
            <span className="font-bold">Nama</span>
            <span>Rating: 100</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default App;
