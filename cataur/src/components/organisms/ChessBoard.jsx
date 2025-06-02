// organisms/ChessBoard.jsx
import React from "react";
import BoardRow from "../molecules/BoardRow";

const ChessBoard = ({ board, onSquareClick, onDragStart, onDropSquare }) => {
  return (
    <div style={{ border: "2px solid black", display: "inline-block" }}>
      {board.map((row, rowIndex) => (
        <BoardRow
          key={rowIndex}
          row={row}
          rowIndex={rowIndex}
          onSquareClick={onSquareClick}
          onDragStart={onDragStart}
          onDropSquare={onDropSquare}
        />
      ))}
    </div>
  );
};

export default ChessBoard;
