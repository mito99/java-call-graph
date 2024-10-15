export interface MethodNode {
  methodName: string;
  className: string;
  packageName: string;
  descriptor: string;
  accessModifier: string;
  methodDigest: string;
}

export interface ClassNode {
  name: string;
  methods: MethodNode[];
}

export interface Relationship {
  name: string;
}
