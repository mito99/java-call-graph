'use client'

import { Slider } from "@/components/ui/slider"
import { toast } from "@/hooks/use-toast"
import { CallingMethodTree } from "@/lib/neo4j"
import { useState } from 'react'
import { Label } from "../ui/label"
import { HierarchyDialog } from "./dialog"
import { SearchForm } from "./search-form"
import { SearchResults } from "./search-results"

// モックデータ
export interface SearchResult {
  methodDigest: string;
  packageName: string;
  moduleName: string;
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
  const [callingMethodTree, setCallingMethodTree] = useState<CallingMethodTree | null>(null)
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

  const handleItemClick = async (item: SearchResult) => {
    setSelectedItem(item);

    const params = new URLSearchParams();
    params.append("methodDigest", item.methodDigest ?? "");
    params.append("hopCount", hopCount.toString()); 
    const response = await fetch(`/api/neo4j/called?${params.toString()}`);
    const methods = await response.json()
    console.log(methods);

    setCallingMethodTree(methods as CallingMethodTree);
    setIsDialogOpen(true);
  }

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
        callingMethodTree={callingMethodTree}
        isDialogOpen={isDialogOpen}
        setIsDialogOpen={setIsDialogOpen}
        selectedItem={selectedItem}
      />
    </div>
  )
}