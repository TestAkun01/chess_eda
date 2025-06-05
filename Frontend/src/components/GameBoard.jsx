import { Chessboard } from "react-chessboard";
import { useState, useEffect } from "react";

const GameBoard = ({
  gameId,
  game,
  gameFen,
  playerColor,
  handleMove,
  isMatchmaking,
  handleMatchmaking,
  handleSurrender,
  isPlayerTurn = true,
  gameStatus = "",
  setGameStatus,
}) => {
  const [selectedSquare, setSelectedSquare] = useState(null);
  const [possibleMoves, setPossibleMoves] = useState([]);
  const [lastMove, setLastMove] = useState(null);
  const [capturedPieces, setCapturedPieces] = useState({
    white: [],
    black: [],
  });

  // Update game status whenever game changes
  useEffect(() => {
    if (game) {
      updateGameStatus();
      updateCapturedPieces();
    }
  }, [game, gameFen]);

  const updateGameStatus = () => {
    if (!game) return;

    if (game.isCheckmate()) {
      const winner = game.turn() === "w" ? "Black" : "White";
      setGameStatus(`Checkmate! ${winner} wins!`);
    } else if (game.isCheck()) {
      const playerInCheck = game.turn() === "w" ? "White" : "Black";
      setGameStatus(`${playerInCheck} is in check!`);
    } else if (game.isDraw()) {
      if (game.isStalemate()) {
        setGameStatus("Draw by stalemate!");
      } else if (game.isThreefoldRepetition()) {
        setGameStatus("Draw by threefold repetition!");
      } else if (game.isInsufficientMaterial()) {
        setGameStatus("Draw by insufficient material!");
      } else {
        setGameStatus("Draw by 50-move rule!");
      }
    } else {
      setGameStatus("");
    }
  };

  const updateCapturedPieces = () => {
    if (!game) return;

    // Get all pieces currently on board
    const board = game.board();
    const piecesOnBoard = {};

    board.forEach((row) => {
      row.forEach((square) => {
        if (square) {
          const key = `${square.color}${square.type}`;
          piecesOnBoard[key] = (piecesOnBoard[key] || 0) + 1;
        }
      });
    });

    // Starting pieces count
    const startingPieces = {
      wp: 8,
      wr: 2,
      wn: 2,
      wb: 2,
      wq: 1,
      wk: 1,
      bp: 8,
      br: 2,
      bn: 2,
      bb: 2,
      bq: 1,
      bk: 1,
    };

    const captured = { white: [], black: [] };

    Object.keys(startingPieces).forEach((pieceKey) => {
      const onBoard = piecesOnBoard[pieceKey] || 0;
      const capturedCount = startingPieces[pieceKey] - onBoard;

      for (let i = 0; i < capturedCount; i++) {
        const [color, type] = pieceKey;
        if (color === "w") {
          captured.black.push(type);
        } else {
          captured.white.push(type);
        }
      }
    });

    setCapturedPieces(captured);
  };

  const getPieceSymbol = (piece) => {
    const symbols = {
      p: "♟",
      r: "♜",
      n: "♞",
      b: "♝",
      q: "♛",
      k: "♚",
    };
    return symbols[piece] || piece;
  };

  const onSquareClick = (square) => {
    if (!game || !isPlayerTurn) return;

    if (selectedSquare === square) {
      // Deselect if clicking same square
      setSelectedSquare(null);
      setPossibleMoves([]);
      return;
    }

    if (selectedSquare && possibleMoves.includes(square)) {
      // Make move
      const moveResult = handleMove(selectedSquare, square, null);
      if (moveResult !== false) {
        setLastMove({ from: selectedSquare, to: square });
      }
      setSelectedSquare(null);
      setPossibleMoves([]);
    } else {
      // Select new square
      const piece = game.get(square);
      if (piece && piece.color === game.turn() && isPlayerTurn) {
        setSelectedSquare(square);
        const moves = game.moves({ square, verbose: true });
        setPossibleMoves(moves.map((move) => move.to));
      } else {
        setSelectedSquare(null);
        setPossibleMoves([]);
      }
    }
  };

  const onPieceDrop = (sourceSquare, targetSquare) => {
    if (!game || !isPlayerTurn) return false;

    const moveResult = handleMove(sourceSquare, targetSquare, null);
    if (moveResult !== false) {
      setLastMove({ from: sourceSquare, to: targetSquare });
      setSelectedSquare(null);
      setPossibleMoves([]);
      return true;
    }
    return false;
  };

  const onPromotionPieceSelect = (
    piece,
    promoteFromSquare,
    promoteToSquare
  ) => {
    if (piece) {
      setLastMove({ from: promoteFromSquare, to: promoteToSquare });
      handleMove(promoteFromSquare, promoteToSquare, piece);
      return true;
    }
    return false;
  };

  const getSquareStyles = () => {
    const styles = {};

    // Highlight selected square
    if (selectedSquare) {
      styles[selectedSquare] = {
        backgroundColor: "rgba(255, 255, 0, 0.4)",
      };
    }

    // Highlight possible moves
    possibleMoves.forEach((square) => {
      styles[square] = {
        background: "radial-gradient(circle, #00ff00 36%, transparent 30%)",
        borderRadius: "50%",
      };
    });

    // Highlight last move
    if (lastMove) {
      styles[lastMove.from] = {
        ...styles[lastMove.from],
        backgroundColor: "rgba(255, 255, 0, 0.2)",
      };
      styles[lastMove.to] = {
        ...styles[lastMove.to],
        backgroundColor: "rgba(255, 255, 0, 0.2)",
      };
    }

    // Highlight check
    if (game && game.isCheck()) {
      const kingSquare = game
        .board()
        .flat()
        .find(
          (square) =>
            square && square.type === "k" && square.color === game.turn()
        );
      if (kingSquare) {
        // Find king position
        for (let i = 0; i < 8; i++) {
          for (let j = 0; j < 8; j++) {
            const square = game.board()[i][j];
            if (square && square.type === "k" && square.color === game.turn()) {
              const squareName = String.fromCharCode(97 + j) + (8 - i);
              styles[squareName] = {
                ...styles[squareName],
                backgroundColor: "rgba(255, 0, 0, 0.4)",
              };
            }
          }
        }
      }
    }

    return styles;
  };

  const MaterialDifference = ({ captured, color }) => (
    <div
      className={`text-sm p-2 rounded ${
        color === "white" ? "bg-gray-100" : "bg-gray-700 text-white"
      }`}
    >
      <div className="font-semibold mb-1">Captured by {color}:</div>
      <div className="flex flex-wrap gap-1">
        {captured.length > 0 ? (
          captured.map((piece, index) => (
            <span key={index} className="text-lg">
              {getPieceSymbol(piece)}
            </span>
          ))
        ) : (
          <span className="text-gray-500 text-xs">None</span>
        )}
      </div>
    </div>
  );

  return (
    <div className="flex-1">
      {gameId && game ? (
        <div className="border rounded p-4 bg-gray-50">
          <h2 className="text-lg font-semibold mb-3">Your Game</h2>

          {/* Game Status Alert */}
          {gameStatus && !gameStatus.includes("Looking") && (
            <div
              className={`mb-4 p-3 rounded-md text-center font-semibold ${
                gameStatus.includes("Checkmate") ||
                gameStatus.includes("wins") ||
                gameStatus.includes("won")
                  ? "bg-red-100 text-red-800"
                  : gameStatus.includes("check")
                  ? "bg-yellow-100 text-yellow-800"
                  : gameStatus.includes("started")
                  ? "bg-green-100 text-green-800"
                  : "bg-blue-100 text-blue-800"
              }`}
            >
              {gameStatus}
            </div>
          )}

          {!isPlayerTurn && game && !gameStatus && (
            <div className="mb-4 p-3 bg-orange-100 text-orange-800 rounded-md text-center">
              <span className="font-semibold">
                Waiting for opponent's move...
              </span>
            </div>
          )}

          {/* Player Info */}
          <div className="mb-4 grid grid-cols-2 gap-4">
            <div className="text-sm">
              <p>
                You are:{" "}
                <span className="font-semibold capitalize">{playerColor}</span>
              </p>
              <p>
                Current turn:{" "}
                <span className="font-semibold">
                  {game.turn() === "w" ? "White" : "Black"}
                </span>
              </p>
            </div>
            <div className="text-sm">
              <p>
                Move:{" "}
                <span className="font-semibold">
                  {Math.ceil(game.history().length / 2)}
                </span>
              </p>
              {game.isCheck() && (
                <p className="text-red-600 font-semibold">⚠️ Check!</p>
              )}
            </div>
          </div>

          {/* Captured Pieces */}
          <div className="mb-4 space-y-2">
            <MaterialDifference captured={capturedPieces.white} color="white" />
            <MaterialDifference captured={capturedPieces.black} color="black" />
          </div>

          {/* Chess Board */}
          <div className="mb-4">
            <Chessboard
              position={gameFen}
              onSquareClick={onSquareClick}
              onPieceDrop={onPieceDrop}
              onPromotionPieceSelect={onPromotionPieceSelect}
              boardWidth={400}
              boardOrientation={playerColor || "white"}
              customBoardStyle={{
                borderRadius: "4px",
                boxShadow: "0 2px 10px rgba(0, 0, 0, 0.1)",
                opacity: isPlayerTurn ? 1 : 0.7,
              }}
              customSquareStyles={getSquareStyles()}
              promotionToSquare={null}
              showPromotionDialog={true}
              arePiecesDraggable={isPlayerTurn}
            />
          </div>

          {/* Game Instructions */}
          <div className="mb-4 p-3 bg-blue-50 rounded-md">
            <h4 className="text-sm font-semibold text-blue-800 mb-2">
              How to play:
            </h4>
            <ul className="text-xs text-blue-700 space-y-1">
              <li>• Click a piece to see possible moves (green dots)</li>
              <li>• Click destination square to move</li>
              <li>• Yellow highlights show your last move</li>
              <li>• Red highlight shows king in check</li>
              <li>• You can also drag and drop pieces</li>
            </ul>
          </div>

          {/* Action Buttons */}
          <div className="space-y-2">
            <button
              onClick={handleSurrender}
              className="w-full py-2 px-4 rounded-md text-white bg-red-500 hover:bg-red-600 transition-colors"
              disabled={
                gameStatus.includes("Checkmate") || gameStatus.includes("Draw")
              }
            >
              Surrender
            </button>

            {selectedSquare && (
              <button
                onClick={() => {
                  setSelectedSquare(null);
                  setPossibleMoves([]);
                }}
                className="w-full py-1 px-4 rounded-md text-gray-600 bg-gray-200 hover:bg-gray-300 transition-colors text-sm"
              >
                Cancel Selection
              </button>
            )}
          </div>
        </div>
      ) : (
        <div className="border rounded p-4 bg-gray-50">
          <h2 className="text-lg font-semibold mb-3">Matchmaking</h2>
          <button
            onClick={handleMatchmaking}
            disabled={isMatchmaking}
            className={`w-full py-2 px-4 rounded-md text-white transition-colors ${
              isMatchmaking
                ? "bg-gray-400 cursor-not-allowed"
                : "bg-green-500 hover:bg-green-600"
            }`}
          >
            {isMatchmaking ? "Finding Match..." : "Find Match"}
          </button>
        </div>
      )}
    </div>
  );
};

export default GameBoard;
