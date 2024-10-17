import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { SearchResult } from './index';

interface HierarchyDialogProps {
  selectedItem: SearchResult | null;
  isDialogOpen: boolean;
  setIsDialogOpen: (open: boolean) => void;
  callingMethodTree: CallingMethodTree | null;
}

import { CallingMethodTree } from "@/lib/neo4j";
import { cn } from "@/lib/utils";
import { ChevronDown } from 'lucide-react';
import React, { useState } from 'react';

const indentColors = [
  'bg-red-200',
  'bg-yellow-200',
  'bg-green-200',
  'bg-blue-200',
  'bg-indigo-200',
  'bg-purple-200',
  'bg-pink-200',
]

const MethodNode: React.FC<{ method: CallingMethodTree; depth: number }> = ({ method, depth }) => {
  const [detailsExpanded, setDetailsExpanded] = useState(false)
  const indentColor = indentColors[depth % indentColors.length]

  return (
    <div className={`relative ${depth > 0 ? 'ml-4' : ''}`}>
      <div className={`absolute left-0 top-0 bottom-0 w-1 ${indentColor}`} />
      <div
        className={`flex items-center cursor-pointer hover:bg-gray-100 p-1 rounded ${indentColor}`}
        onClick={() => setDetailsExpanded(!detailsExpanded)}
      >
        {method.children && Object.keys(method.children).length > 0 ? (
          <ChevronDown className="w-4 h-4 mr-1" />
        ) : (
          <span className="w-4 h-4 mr-1" />
        )}
        <span className="font-medium">{`${method.className}#${method.methodName}`}</span>
      </div>
      {detailsExpanded && (
        <div className="ml-4 mt-2 text-sm">
          <p><span className="font-semibold">Package:</span> {method.packageName}</p>
          <p><span className="font-semibold">Access Modifier:</span> {method.accessModifier}</p>
          <p><span className="font-semibold break-all">Descriptor:</span> {method.descriptor}</p>
          <p><span className="font-semibold">Method Digest:</span> {method.methodDigest}</p>
        </div>
      )}
      {method.children && Object.keys(method.children).length > 0 ? (
        Object.entries(method.children).map(([key, child]) => (
          <MethodNode key={key} method={child} depth={depth + 1} />
        ))
      ) : null}
    </div>
  )
}

export function HierarchyDialog({
  selectedItem,
  isDialogOpen,
  setIsDialogOpen,
  callingMethodTree,
}: HierarchyDialogProps) {

  return (
    <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
      <DialogContent className={cn("w-[90vw] h-[90vh] flex flex-col")}>
        <DialogHeader>
          <DialogDescription>
            <span className="font-semibold">{selectedItem?.packageName}</span>
            <span className="text-gray-500">{selectedItem?.moduleName}</span>
          </DialogDescription>
          <DialogTitle>
            <p>{selectedItem?.className}#{selectedItem?.methodName}</p>
            <p className="text-gray-500 text-sm break-all">{selectedItem?.descriptor}</p>
          </DialogTitle>
        </DialogHeader>
        <div className="p-1 overflow-y-auto">
          {callingMethodTree && (
            <MethodNode 
            method={callingMethodTree} 
            key={callingMethodTree.methodDigest} 
            depth={0} 
            />
          )}
        </div>        
      </DialogContent>
    </Dialog>
  )
}