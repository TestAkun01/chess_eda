// atoms/Square.jsx
import React from "react";
import "./Square.css";

const Square = ({
  color,
  children,
  onClick,
  isHighlighted,
  onDragOver,
  onDrop,
}) => {
  return (
    <div
      className={`square ${color} ${isHighlighted ? "highlighted" : ""}`}
      onClick={onClick}
      onDragOver={onDragOver}
      onDrop={onDrop}
    >
      {children}
    </div>
  );
};

export default Square;
