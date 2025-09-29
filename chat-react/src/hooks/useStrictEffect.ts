import { useEffect, useRef } from 'react';

const ESCAPE_STRICT_MODE = import.meta.env.VITE_ESCAPE_STRICT_MODE;

export function useStrictEffect(effect: () => void | (() => void), deps: any[]) {
  const isMounted = useRef(false);

  useEffect(() => {
    if (ESCAPE_STRICT_MODE !== 'Y' || isMounted.current) {
      return effect();
    } else {
      isMounted.current = true;
    }
  }, deps);
}