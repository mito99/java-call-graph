import { Globe, Lock, Shield, Unlock } from 'lucide-react';

interface ModifierIconProps {
  modifier: string;
}

export function ModifierIcon({ modifier }: ModifierIconProps) {
  switch (modifier) {
    case 'public':
      return <Globe className="w-4 h-4 text-green-500" />
    case 'private':
      return <Lock className="w-4 h-4 text-red-500" />
    case 'protected':
      return <Shield className="w-4 h-4 text-yellow-500" />
    default:
      return <Unlock className="w-4 h-4 text-gray-500" />
  }
}