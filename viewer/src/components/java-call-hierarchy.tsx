'use client'

import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Slider } from "@/components/ui/slider"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { toast } from "@/hooks/use-toast"
import { Check, ChevronRight, Circle, Copy, Globe, Lock, Search, Shield, Unlock } from 'lucide-react'
import { useState } from 'react'

// モックデータ
interface SearchResult {
  id: number;
  packageName: string;
  className: string;
}

const mockSearchResults: SearchResult[] = [
  { id: 1, packageName: 'com.example', className: 'ExampleClass' },
  { id: 2, packageName: 'com.example.util', className: 'UtilityClass' },
  // ... more mock data
]

const mockHierarchy = [
  { level: 0, name: 'ExampleClass.mainMethod()', modifier: 'public' },
  { level: 1, name: 'UtilityClass.helperMethod()', modifier: 'private' },
  { level: 2, name: 'AnotherClass.subMethod()', modifier: 'protected' },
  { level: 3, name: 'FourthClass.deepMethod()', modifier: 'public' },
  { level: 4, name: 'FifthClass.deeperMethod()', modifier: 'private' },
  { level: 5, name: 'SixthClass.evenDeeperMethod()', modifier: 'protected' },
  { level: 6, name: 'SeventhClass.veryDeepMethod()', modifier: 'public' },
  // ... more mock hierarchy data
]

const ModifierIcon = ({ modifier }: { modifier: string }) => {
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

export function JavaCallHierarchyComponent() {
  const [searchQuery, setSearchQuery] = useState('')
  const [searchResults, setSearchResults] = useState<SearchResult[]>([])
  const [selectedItem, setSelectedItem] = useState<SearchResult | null>(null)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [hopCount, setHopCount] = useState(3)
  const [isCopied, setIsCopied] = useState(false)

  const handleSearch = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    try {
      const results = mockSearchResults.filter(item => 
        item.packageName.toLowerCase().includes(searchQuery.toLowerCase()) || 
        item.className.toLowerCase().includes(searchQuery.toLowerCase())
      )
      setSearchResults(results)
      if (results.length === 0) {
        toast({
          title: "No results found",
          description: "Try adjusting your search query.",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error('Search error:', error)
      toast({
        title: "Search failed",
        description: "An error occurred while searching. Please try again.",
        variant: "destructive",
      })
    }
  }

  const handleSearchInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value)
  }

  const handleItemClick = (item: SearchResult) => {
    setSelectedItem(item)
    setIsDialogOpen(true)
  }

  const filteredHierarchy = mockHierarchy.filter(item => item.level < hopCount)

  const generateTreeString = (hierarchy: typeof mockHierarchy) => {
    let treeString = ''
    hierarchy.forEach((item, index) => {
      const isLast = index === hierarchy.length - 1 || hierarchy[index + 1].level <= item.level
      const prefix = item.level > 0 ? '│   '.repeat(item.level - 1) : ''
      const connector = item.level > 0 ? (isLast ? '└── ' : '├── ') : ''
      treeString += `${prefix}${connector}${item.modifier} ${item.name}\n`
    })
    return treeString
  }

  const handleCopyToClipboard = () => {
    const treeString = generateTreeString(filteredHierarchy)
    navigator.clipboard.writeText(treeString).then(() => {
      setIsCopied(true)
      toast({
        title: "Copied to clipboard",
        description: "The tree structure has been copied to your clipboard.",
      })
      setTimeout(() => setIsCopied(false), 2000)
    }).catch(err => {
      console.error('Failed to copy: ', err)
      toast({
        title: "Copy failed",
        description: "Failed to copy the tree structure. Please try again.",
        variant: "destructive",
      })
    })
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Java Call Hierarchy Viewer</h1>
      
      <form onSubmit={handleSearch} className="mb-4">
        <div className="flex gap-2">
          <Input
            type="text"
            placeholder="Search by package or class name"
            value={searchQuery}
            onChange={handleSearchInputChange}
            className="flex-grow"
            aria-label="Search query"
          />
          <Button type="submit">
            <Search className="mr-2 h-4 w-4" /> Search
          </Button>
        </div>
      </form>

      {searchResults.length > 0 && (
        <div className="mb-4">
          <Label htmlFor="hop-count" className="block text-sm font-medium text-gray-700 mb-1">
            Hop Count: {hopCount}
          </Label>
          <Slider
            id="hop-count"
            min={1}
            max={10}
            step={1}
            value={[hopCount]}
            onValueChange={(value) => setHopCount(value[0])}
            className="max-w-md"
          />
        </div>
      )}

      <div className="bg-white shadow rounded-lg">
        {searchResults.map((item) => (
          <div
            key={item.id}
            className="p-4 border-b last:border-b-0 hover:bg-gray-50 cursor-pointer"
            onClick={() => handleItemClick(item)}
          >
            <h2 className="font-semibold">{item.className}</h2>
            <p className="text-sm text-gray-600">{item.packageName}</p>
          </div>
        ))}
      </div>

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
    </div>
  )
}