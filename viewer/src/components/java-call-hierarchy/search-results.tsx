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
          <div className='flex flex-row flex-wrap'>
            <div className='flex-1'>
              <p>{item.className}</p>
              <p className='text-sm text-gray-500'>{item.packageName}</p>
            </div>
            <div className='flex-1'>
              <p>{item.methodName}</p>
              <p className='text-sm text-gray-500'>{item.descriptor}</p>
              <p className='text-sm text-gray-500'>{item.accessModifier}</p>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}