import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Search } from 'lucide-react';
import { ChangeEvent, FormEvent } from 'react';
import { SearchQuery } from ".";

interface SearchFormProps {
  searchQuery: SearchQuery;
  handleSearch: (e: FormEvent<HTMLFormElement>) => void;
  handleSearchInputChange: (e: ChangeEvent<HTMLInputElement>) => void;
}

export function SearchForm({ searchQuery, handleSearch, handleSearchInputChange}: SearchFormProps) {
  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    handleSearch(e);
  };

  return (
    <form onSubmit={handleSubmit} className="mb-4">
      <div className="flex gap-2">
        <Input
          type="text"
          placeholder="Search by package or class name"
          value={searchQuery.value}
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