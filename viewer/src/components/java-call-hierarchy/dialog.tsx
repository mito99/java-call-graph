import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { Check, ChevronRight, Circle, Copy } from 'lucide-react'
import { SearchResult } from './index'
import { ModifierIcon } from './modifier-icon'

interface HierarchyDialogProps {
  isDialogOpen: boolean;
  setIsDialogOpen: (open: boolean) => void;
  selectedItem: SearchResult | null;
  hopCount: number;
  filteredHierarchy: { level: number; name: string; modifier: string }[];
  handleCopyToClipboard: () => void;
  isCopied: boolean;
}

export function HierarchyDialog({
  isDialogOpen,
  setIsDialogOpen,
  selectedItem,
  hopCount,
  filteredHierarchy,
  handleCopyToClipboard,
  isCopied
}: HierarchyDialogProps) {
  return (
    <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
      <DialogContent className="sm:max-w-[700px] max-h-[80vh] flex flex-col">
        <DialogHeader>
          <DialogTitle>{selectedItem?.className}</DialogTitle>
          <DialogDescription>{selectedItem?.packageName}</DialogDescription>
        </DialogHeader>
        <div className="mt-4 flex-grow overflow-hidden">
          <div className="flex justify-between items-center mb-2">
            <h3 className="font-semibold">Call Hierarchy (Hop Count: {hopCount})</h3>
            <Button
              onClick={handleCopyToClipboard}
              variant="outline"
              size="sm"
              className="flex items-center"
            >
              {isCopied ? (
                <>
                  <Check className="w-4 h-4 mr-2" />
                  Copied
                </>
              ) : (
                <>
                  <Copy className="w-4 h-4 mr-2" />
                  Copy Tree
                </>
              )}
            </Button>
          </div>
          <div 
            className="p-4 bg-gray-50 rounded-lg overflow-auto max-h-[calc(80vh-200px)]"
            style={{ maxWidth: 'calc(100vw - 64px)' }}
            role="tree"
            aria-label="Call Hierarchy Tree"
          >
            <div className="inline-block min-w-full">
              {filteredHierarchy.map((item, index) => (
                <div
                  key={index}
                  className="flex items-center group mb-2"
                  style={{ paddingLeft: `${item.level * 20}px` }}
                  role="treeitem"
                  aria-selected={false}
                >
                  {item.level > 0 && (
                    <div className="mr-2 w-6 h-6 flex items-center justify-center">
                      <div className="w-px h-full bg-gray-300 group-hover:bg-blue-500 transition-colors duration-200"></div>
                    </div>
                  )}
                  <div className="flex items-center bg-white rounded-lg p-2 shadow-sm group-hover:shadow-md transition-shadow duration-200 w-full">
                    {item.level === 0 ? (
                      <Circle className="w-4 h-4 mr-2 text-blue-500 flex-shrink-0" />
                    ) : (
                      <ChevronRight className="w-4 h-4 mr-2 text-gray-400 group-hover:text-blue-500 transition-colors duration-200 flex-shrink-0" />
                    )}
                    <TooltipProvider>
                      <Tooltip>
                        <TooltipTrigger>
                          <ModifierIcon modifier={item.modifier} />
                        </TooltipTrigger>
                        <TooltipContent>
                          <p>{item.modifier}</p>
                        </TooltipContent>
                      </Tooltip>
                    </TooltipProvider>
                    <span className="text-sm font-medium group-hover:text-blue-600 transition-colors duration-200 whitespace-nowrap ml-2">
                      {item.name}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}