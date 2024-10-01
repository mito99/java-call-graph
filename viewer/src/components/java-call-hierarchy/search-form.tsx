import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Search } from 'lucide-react';
import { ChangeEvent, FormEvent } from 'react';

interface SearchFormProps {
  searchQuery: string;
  handleSearch: (e: FormEvent<HTMLFormElement>) => void;
  handleSearchInputChange: (e: ChangeEvent<HTMLInputElement>) => void;
}

export function SearchForm({ searchQuery, handleSearch, handleSearchInputChange }: SearchFormProps) {
  return (
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
  )
}