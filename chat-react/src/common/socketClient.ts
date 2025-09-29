import SockJS from "sockjs-client";
import { CompatClient, Stomp, type Frame } from "@stomp/stompjs";

const SOCKET_URL = import.meta.env.VITE_API_URL + "/ws";
const USE_SOCKJS = import.meta.env.VITE_USE_SOCKJS === "true";

let sharedClient: CompatClient | null = null;

export function connectWebSocket(callback?: (client: CompatClient, sessionId: string) => void) {
  if (sharedClient) return sharedClient;

  let client: CompatClient;

  if (USE_SOCKJS) {
    const socket = new SockJS(SOCKET_URL);
    client = Stomp.over(socket);

    client.connect({}, () => {
      console.log("STOMP 연결됨 (SockJS 사용)");
      sharedClient = client;

      // SockJS에서 세션ID 추출
      const url = (socket as any)._transport.url;
      const parts = url.split('/');
      const extractedSessionId = parts[parts.length - 2];

      callback?.(client, extractedSessionId);
    });

  } else {
    // WebSocket URL은 ws:// 또는 wss:// 로 시작해야 함
    // 예: http://localhost:8080 → ws://localhost:8080
    const wsUrl = SOCKET_URL.replace(/^http/, "ws");
    const socket = new WebSocket(wsUrl);
    client = Stomp.over(socket);
    client.debug = () => {}; // 디버그 로그 끄기

    client.connect({}, (frame: Frame) => {
      console.log("STOMP 연결됨 (순수 WebSocket 사용)");
      sharedClient = client;

      // 순수 WebSocket에서는 SockJS처럼 세션ID 직접 추출 불가
      // WebFlux 서버에서 직접 헤더에 추가해 보내도록 구현
      const sessionId = frame.headers.session || '';
      callback?.(client, sessionId);
    });
  }
  return client;
}

export function getWebSocketClient(): CompatClient | null {
  return sharedClient;
}

export function disconnectWebSocket() {
  if (sharedClient?.connected) {
    sharedClient.disconnect(() => {
      console.log("STOMP 연결 종료");
      sharedClient = null;
    });
  }
}