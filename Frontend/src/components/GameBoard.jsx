import { Chessboard } from "react-chessboard";

const GameBoard = ({
  gameId,
  game,
  gameFen,
  playerColor,
  handleMove,
  isMatchmaking,
  handleMatchmaking,
  handleSurrender,
}) => {
  const onPromotionPieceSelect = (
    piece,
    promoteFromSquare,
    promoteToSquare
  ) => {
    if (piece) {
      handleMove(promoteFromSquare, promoteToSquare, piece);
      return true;
    }
    return false;
  };

  const onPieceDrop = (sourceSquare, targetSquare) => {
    handleMove(sourceSquare, targetSquare, undefined);
    return true;
  };
  return (
    <div className="flex-1">
      {gameId && game ? (
        <div className="border rounded p-4 bg-gray-50">
          <h2 className="text-lg font-semibold mb-3">Your Game</h2>
          <div className="mb-2">
            <p className="text-sm">
              You are: <span className="font-semibold">{playerColor}</span>
            </p>
            <p className="text-sm">
              Current turn:{" "}
              <span className="font-semibold">
                {game.turn() === "w" ? "White" : "Black"}
              </span>
            </p>
          </div>
          <Chessboard
            position={gameFen}
            onPieceDrop={onPieceDrop}
            onPromotionPieceSelect={onPromotionPieceSelect}
            boardWidth={400}
            boardOrientation={playerColor || "white"}
            customBoardStyle={{
              borderRadius: "4px",
              boxShadow: "0 2px 10px rgba(0, 0, 0, 0.1)",
            }}
          />
          <button
            onClick={handleSurrender}
            className="mt-4 w-full py-2 px-4 rounded-md text-white bg-red-500 hover:bg-red-600"
          >
            Surrender
          </button>
        </div>
      ) : (
        <div className="border rounded p-4 bg-gray-50">
          <h2 className="text-lg font-semibold mb-3">Matchmaking</h2>
          <button
            onClick={handleMatchmaking}
            disabled={isMatchmaking}
            className={`w-full py-2 px-4 rounded-md text-white ${
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
