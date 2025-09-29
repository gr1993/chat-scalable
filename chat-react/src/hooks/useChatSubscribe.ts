import { useEffect } from "react";
import { useWebSocket } from "@/hooks/useWebSocket";
import { subscriptionManager } from "@/common/subscriptionManager";

export function useChatSubscribe(destination: string, callback: any) {
  const clientRef = useWebSocket((client) => {
    subscriptionManager.subscribe(client, destination, callback);
  });

  useEffect(() => {
    return () => {
      subscriptionManager.unsubscribe(destination);
    };
  }, [destination]);

  return clientRef;
}