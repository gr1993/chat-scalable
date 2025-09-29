import type { CompatClient } from "@stomp/stompjs";

type SubscriptionMap = Map<string, any>;

class SubscriptionManager {
  private subscriptions: SubscriptionMap = new Map();

  subscribe(client: CompatClient, destination: string, callback: any) {
    if (this.subscriptions.has(destination)) {
      return;
    }

    const subscription = client.subscribe(destination, callback);
    this.subscriptions.set(destination, subscription);
  }

  unsubscribe(destination: string) {
    const subscription = this.subscriptions.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(destination);
    }
  }

  clearAll() {
    for (const [dest, sub] of this.subscriptions) {
      sub.unsubscribe();
    }
    this.subscriptions.clear();
  }
}

export const subscriptionManager = new SubscriptionManager();