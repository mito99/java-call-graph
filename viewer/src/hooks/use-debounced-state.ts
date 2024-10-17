import { useEffect, useState } from "react";

interface UseDebouncedStateOptions {
  delay?: number;
}

export function useDebouncedState<T>(
  initialValue: T,
  options: UseDebouncedStateOptions = { delay: 300 }
): [T, (value: T) => void] {
  const [value, setValue] = useState(initialValue);
  const [debouncedValue, setDebouncedValue] = useState(initialValue);

  useEffect(() => {
    const delay = value ? 0 : options.delay;
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => clearTimeout(handler);
  }, [value, options.delay]);

  return [debouncedValue, setValue];
}
