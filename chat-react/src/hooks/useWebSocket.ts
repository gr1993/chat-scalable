import { useRef } from "react";
import type { CompatClient } from "@stomp/stompjs";
import { getWebSocketClient } from "@/common/socketClient";
import { useStrictEffect } from '@/hooks/useStrictEffect';

export function useWebSocket(onConnect?: (client: CompatClient) => void) {
  const clientRef = useRef<CompatClient | null>(null);
  const retryTimerRef = useRef<NodeJS.Timeout | null>(null);

  useStrictEffect(() => {
    let retryCount = 0;
    const interval = 100;

    const tryConnect = () => {
      const client = getWebSocketClient();
      if (client && client.connected) {
        clientRef.current = client;
        onConnect?.(client);
      } else {
        console.log('웹소켓 연결 대기')
        retryCount++;
        retryTimerRef.current = setTimeout(tryConnect, interval);
      }
    }

    tryConnect();

    return () => {
      if (retryTimerRef.current) {
        clearTimeout(retryTimerRef.current);
      }
    };
  }, []);

  return clientRef;
}