const SOCKET_URL = import.meta.env.VITE_API_URL + "/ws";

let socket: WebSocket | null = null;

export function connectWebSocket(): WebSocket {
  if (socket && socket.readyState !== WebSocket.CLOSED) {
    return socket;
  }
  
  socket = new WebSocket(SOCKET_URL);

  socket.onopen = () => {
    console.log("WebSocket 연결됨");
  };

  socket.onclose = (event) => {
    console.log("WebSocket 연결 종료", event.reason || "");
  };

  socket.onerror = (error) => {
    console.error("WebSocket 에러", error);
  };

  return socket;
}

export function getWebSocket(): WebSocket | null {
  return socket;
}

export function disconnectWebSocket() {
  if (socket) {
    console.log("WebSocket 수동 연결 종료");
    socket.close();
    socket = null;
  }
}