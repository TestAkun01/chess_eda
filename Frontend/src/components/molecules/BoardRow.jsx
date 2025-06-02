// molecules/BoardRow.jsx
import React from "react";
import Square from "../atoms/Square";
import Piece from "../atoms/Piece";

const BoardRow = ({
  row,
  rowIndex,
  onSquareClick,
  onDragStart,
  onDropSquare,
}) => {
  return (
    <div style={{ display: "flex" }}>
      {row.map((square, colIndex) => {
        const color = (rowIndex + colIndex) % 2 === 0 ? "white" : "black";
        const squarePos = String.fromCharCode(97 + colIndex) + (8 - rowIndex); // e.g. "e4"

        return (
          <Square
            key={colIndex}
            color={color}
            onClick={() => onSquareClick(rowIndex, colIndex)}
            onDragOver={(e) => e.preventDefault()}
            onDrop={(e) => onDropSquare(e, squarePos)}
          >
            {square && (
              <Piece
                type={square}
                position={squarePos}
                onDragStart={onDragStart}
              />
            )}
          </Square>
        );
      })}
    </div>
  );
};

export default BoardRow;
