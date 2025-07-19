import { useState } from 'react'
import { Search, X, Loader2 } from 'lucide-react'

interface SearchInputProps {
  value: string
  onChange: (value: string) => void
  placeholder?: string
  isLoading?: boolean
  disabled?: boolean
  className?: string
  onClear?: () => void
}

const SearchInput: React.FC<SearchInputProps> = ({
  value,
  onChange,
  placeholder = "Search...",
  isLoading = false,
  disabled = false,
  className = "",
  onClear
}) => {
  const [isFocused, setIsFocused] = useState(false)

  const handleClear = () => {
    onChange('')
    onClear?.()
  }

  return (
    <div className={`relative ${className}`}>
      <div className="relative">
        <Search className={`absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 transition-colors duration-200 ${
          isFocused ? 'text-blue-500' : 'text-gray-400'
        }`} />
        
        <input
          type="text"
          className={`
            w-full pl-10 pr-10 py-2.5 
            border border-gray-300 rounded-lg 
            focus:ring-2 focus:ring-blue-500 focus:border-blue-500 
            transition-all duration-200 
            placeholder-gray-400 
            ${disabled ? 'bg-gray-50 cursor-not-allowed' : 'bg-white'}
            ${isLoading ? 'pr-12' : value ? 'pr-10' : 'pr-4'}
          `}
          placeholder={placeholder}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          disabled={disabled}
        />

        {/* Loading spinner */}
        {isLoading && (
          <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
            <Loader2 className="h-4 w-4 animate-spin text-blue-500" />
          </div>
        )}

        {/* Clear button */}
        {!isLoading && value && (
          <button
            onClick={handleClear}
            className="absolute right-3 top-1/2 transform -translate-y-1/2 p-1 rounded-full hover:bg-gray-100 transition-colors duration-200"
            disabled={disabled}
          >
            <X className="h-3 w-3 text-gray-400 hover:text-gray-600" />
          </button>
        )}
      </div>

      {/* Search suggestions or results count could go here */}
      {value && !isLoading && (
        <div className="absolute top-full left-0 right-0 mt-1 text-xs text-gray-500 px-3">
          {value.length < 2 ? 'Type 2+ characters to search' : ''}
        </div>
      )}
    </div>
  )
}

export default SearchInput
