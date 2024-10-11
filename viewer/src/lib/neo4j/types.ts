export interface MethodNode {
  name: string;
  parameters: string[];
  returnType: string;
}

export interface ClassNode {
  name: string;
  methods: MethodNode[];
}

export interface Relationship {
  name: string;
}
