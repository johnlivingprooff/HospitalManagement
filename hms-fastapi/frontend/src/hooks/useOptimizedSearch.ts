import { useQuery } from 'react-query'
import { useDebounce } from './useDebounce'
import api from '../lib/api'

interface UseSearchOptions {
  endpoint: string
  searchKey: string
  debounceMs?: number
  enabled?: boolean
  cacheTime?: number
  staleTime?: number
}

/**
 * Custom hook for optimized search with debouncing and caching
 * @param searchTerm - The current search term
 * @param options - Configuration options
 * @returns Query result with optimized caching
 */
export function useOptimizedSearch<T>(searchTerm: string, options: UseSearchOptions) {
  const {
    endpoint,
    searchKey,
    debounceMs = 300,
    enabled = true,
    cacheTime = 10 * 60 * 1000, // 10 minutes
    staleTime = 5 * 60 * 1000,  // 5 minutes
  } = options

  // Debounce the search term to avoid excessive API calls
  const debouncedSearchTerm = useDebounce(searchTerm, debounceMs)

  return useQuery<T[]>(
    [searchKey, debouncedSearchTerm],
    async () => {
      const params = new URLSearchParams()
      if (debouncedSearchTerm.trim()) {
        params.append('search', debouncedSearchTerm.trim())
      }
      
      const queryString = params.toString()
      const url = queryString ? `${endpoint}?${queryString}` : endpoint
      
      const response = await api.get(url)
      return response.data
    },
    {
      enabled: enabled && debouncedSearchTerm.length >= 0,
      cacheTime,
      staleTime,
      keepPreviousData: true, // Keep previous results while loading new ones
      refetchOnWindowFocus: false, // Don't refetch when window gains focus
    }
  )
}

/**
 * Hook for client-side filtering with optimized performance
 * @param data - Array of data to filter
 * @param searchTerm - Search term
 * @param searchFields - Fields to search in
 * @param additionalFilters - Additional filter functions
 * @returns Filtered data
 */
export function useClientSearch<T>(
  data: T[] | undefined,
  searchTerm: string,
  searchFields: (keyof T)[],
  additionalFilters?: ((item: T) => boolean)[]
): T[] {
  const debouncedSearchTerm = useDebounce(searchTerm, 150)

  if (!data) return []

  return data.filter(item => {
    // Search term matching
    const searchMatch = debouncedSearchTerm === '' || searchFields.some(field => {
      const value = item[field]
      if (typeof value === 'string') {
        return value.toLowerCase().includes(debouncedSearchTerm.toLowerCase())
      }
      if (typeof value === 'number') {
        return value.toString().includes(debouncedSearchTerm)
      }
      // Handle nested objects
      if (typeof value === 'object' && value !== null) {
        return JSON.stringify(value).toLowerCase().includes(debouncedSearchTerm.toLowerCase())
      }
      return false
    })

    // Additional filters
    const additionalMatch = !additionalFilters || additionalFilters.every(filter => filter(item))

    return searchMatch && additionalMatch
  })
}
