import { useState } from "react";
import axios from "axios";

const ChatBox = ({ messages, stompClient, user }) => {
  const [newMessage, setNewMessage] = useState("");

  const handleertekanan = async (e) => {
    e.preventDefault();
    if (!newMessage.trim() || !stompClient || !stompClient.connected) return;

    try {
      await axios.post(
        "http://localhost:8080/api/chat/send",
        { message: newMessage },
        { withCredentials: true }
      );
      setNewMessage("");
    } catch (error) {
      console.error("Error sending message:", error);
    }
  };

  return (
    <div className="flex-1 border rounded p-4 bg-gray-50">
      <h2 className="text-lg font-semibold mb-3">Global Chat</h2>
      <div className="h-64 overflow-y-auto mb-3 p-3 bg-white border rounded">
        {messages.length === 0 ? (
          <p className="text-gray-500">No messages</p>
        ) : (
          messages.map((msg) => (
            <div key={msg.id} className="mb-2">
              <span className="font-semibold">{msg.sender.username}: </span>
              <span>{msg.message}</span>
            </div>
          ))
        )}
      </div>
      <form onSubmit={handleertekanan} className="flex">
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          placeholder="Type a message..."
          className="flex-1 p-2 border rounded-l-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={!stompClient || !stompClient.connected}
        />
        <button
          type="submit"
          className="bg-blue-500 text-white px-4 py-2 rounded-r-md hover:bg-blue-600 disabled:bg-blue-300"
          disabled={!stompClient || !stompClient.connected}
        >
          Send
        </button>
      </form>
    </div>
  );
};

export default ChatBox;
