// atoms/Piece.jsx
import React from "react";

const Piece = ({ type, position, onDragStart }) => {
  const color = type === type.toUpperCase() ? "w" : "b"; 
  const pieceType = type.toLowerCase(); 
  const filename = `${color}${pieceType}.svg`; 
  const pieceFile = `http://localhost:5173/src/assets/pieces/${filename}`;

  return (
    <img
      src={pieceFile}
      alt={type}
      style={{ width: "100%", height: "100%", objectFit: "contain", cursor: "grab" }}
      draggable
      onDragStart={(e) => onDragStart(e, position)}
    />
  );
};

export default Piece;
