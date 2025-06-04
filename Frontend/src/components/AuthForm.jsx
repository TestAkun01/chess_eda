import React from "react";

const AuthForm = ({
  title,
  onSubmit,
  error,
  isLoading,
  buttonText,
  setUsername,
  setPassword,
  username,
  password,
}) => (
  <div className="flex justify-center items-center h-screen">
    <form
      onSubmit={onSubmit}
      className="flex flex-col gap-4 p-6 bg-white rounded shadow w-80"
    >
      <h2 className="text-xl font-bold text-center">{title}</h2>
      {error && <div className="text-red-500 text-center">{error}</div>}
      <input
        className="border p-2 rounded"
        placeholder="Username"
        type="text"
        onChange={(e) => setUsername(e.target.value)}
        value={username}
        disabled={isLoading}
      />
      <input
        className="border p-2 rounded"
        type="password"
        placeholder="Password"
        onChange={(e) => setPassword(e.target.value)}
        value={password}
        disabled={isLoading}
      />
      <button
        className={`p-2 rounded text-white ${
          title === "Register"
            ? "bg-green-500 disabled:bg-green-300"
            : "bg-blue-500 disabled:bg-blue-300"
        }`}
        type="submit"
        disabled={isLoading}
      >
        {isLoading ? `Processing...` : buttonText}
      </button>
    </form>
  </div>
);

export default AuthForm;
