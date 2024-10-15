'use client'

import { Slider } from "@/components/ui/slider"
import { toast } from "@/hooks/use-toast"
import { useEffect, useState } from 'react'
import { Label } from "../ui/label"
import { HierarchyDialog } from "./dialog"
import { SearchForm } from "./search-form"
import { SearchResults } from "./search-results"

// モックデータ
export interface SearchResult {
  id: number;
  packageName: string;
  className: string;
  methodName: string;
  descriptor: string;
  accessModifier: string;
}

export class SearchQuery {
  value: string = ""
  limit: number = 25

  constructor(value: string) {
    this.value = value
  }

  get packageName(): string {
    const [fullClassName, ] = this.value.split('#');
    return fullClassName.split('.').slice(0, -1).join('.') || "";
  }

  get className(): string {
    const [fullClassName, ] = this.value.split('#');
    return fullClassName.split('.').slice(-1)[0] || "";
  }

  get methodName(): string {
    const [, methodName] = this.value.split('#');
    return methodName || "";
  }
}

export function JavaCallHierarchyComponent() {
  const [searchQuery, setSearchQuery] = useState(new SearchQuery(""))
  const [searchResults, setSearchResults] = useState<SearchResult[]>([])
  const [selectedItem, setSelectedItem] = useState<SearchResult | null>(null)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [hopCount, setHopCount] = useState(3)
  // const [isCopied, setIsCopied] = useState(false)

  const handleSearch = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    try {
      const params = new URLSearchParams();
      params.append("packageName", searchQuery.packageName);
      params.append("className", searchQuery.className);
      params.append("methodName", searchQuery.methodName);
      params.append("limit", searchQuery.limit.toString());
      const response = await fetch(`/api/neo4j/methods?${params.toString()}`);
      const results = await response.json()

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
    setSearchQuery((prevState)=>{
      prevState.value = e.target.value
      return prevState
    })
  }

  const handleSearchLimitChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery((prevState)=>{
      prevState.limit = parseInt(e.target.value)
      return prevState
    })
  }

  const handleItemClick = (item: SearchResult) => {
    setSelectedItem(item)
  }


  useEffect(() => {
    
  }, [selectedItem]);

  // const generateTreeString = (hierarchy: typeof mockHierarchy) => {
  //   let treeString = ''
  //   hierarchy.forEach((item, index) => {
  //     const isLast = index === hierarchy.length - 1 || hierarchy[index + 1].level <= item.level
  //     const prefix = item.level > 0 ? '│   '.repeat(item.level - 1) : ''
  //     const connector = item.level > 0 ? (isLast ? '└── ' : '├── ') : ''
  //     treeString += `${prefix}${connector}${item.modifier} ${item.name}\n`
  //   })
  //   return treeString
  // }

  // const handleCopyToClipboard = () => {
  //   const treeString = generateTreeString(filteredHierarchy)
  //   navigator.clipboard.writeText(treeString).then(() => {
  //     setIsCopied(true)
  //     toast({
  //       title: "Copied to clipboard",
  //       description: "The tree structure has been copied to your clipboard.",
  //     })
  //     setTimeout(() => setIsCopied(false), 2000)
  //   }).catch(err => {
  //     console.error('Failed to copy: ', err)
  //     toast({
  //       title: "Copy failed",
  //       description: "Failed to copy the tree structure. Please try again.",
  //       variant: "destructive",
  //     })
  //   })
  // }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Java Call Hierarchy Viewer</h1>
      
      <SearchForm 
        searchQuery={searchQuery}
        handleSearch={handleSearch}
        handleSearchInputChange={handleSearchInputChange}
        handleSearchLimitChange={handleSearchLimitChange}
      />

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

      <SearchResults 
        searchResults={searchResults}
        handleItemClick={handleItemClick}
      />

      <HierarchyDialog 
        isDialogOpen={isDialogOpen}
        setIsDialogOpen={setIsDialogOpen}
        selectedItem={selectedItem}
        hopCount={hopCount}
        filteredHierarchy={[]}
        handleCopyToClipboard={()=>{}}
        isCopied={false}
      />
    </div>
  )
}