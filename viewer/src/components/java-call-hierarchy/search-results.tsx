import { SearchResult } from './index';

interface SearchResultsProps {
  searchResults: SearchResult[];
  handleItemClick: (item: SearchResult) => void;
}

export function SearchResults({ searchResults, handleItemClick }: SearchResultsProps) {
  return (
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
  )
}