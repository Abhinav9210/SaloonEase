import { useEffect, useState } from 'react'
import { Search } from 'lucide-react'
import { useAppDispatch, useAppSelector } from '../../app/hooks'
import { fetchApprovedSalons, searchSalons } from '../../features/salon/salonSlice'
import SalonCard from '../../components/common/SalonCard'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import Navbar from '../../components/layout/Navbar'
import Footer from '../../components/layout/Footer'

const SalonsListPage = () => {
  const dispatch = useAppDispatch()
  const { salons, searchResults, loading, totalPages } = useAppSelector(s => s.salon)
  const [query, setQuery] = useState('')
  const [page, setPage] = useState(0)
  const [searched, setSearched] = useState(false)

  useEffect(() => {
    dispatch(fetchApprovedSalons({ page, size: 12 }))
  }, [page])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (query.trim()) {
      dispatch(searchSalons({ query }))
      setSearched(true)
    } else {
      setSearched(false)
    }
  }

  const displaySalons = searched ? searchResults : salons

  return (
    <div className="min-h-screen bg-dark-900">
      <Navbar />
      <div className="pt-24 pb-16">
        <div className="page-container mb-8">
          <h1 className="text-3xl font-display font-bold text-white mb-2">Browse Salons</h1>
          <p className="text-slate-400">Find the perfect salon near you</p>

          <form onSubmit={handleSearch} className="flex gap-2 mt-6 max-w-lg">
            <div className="relative flex-1">
              <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" />
              <input value={query} onChange={e => setQuery(e.target.value)}
                placeholder="Search by name or city..."
                className="input-field pl-10 py-3" />
            </div>
            <button type="submit" className="btn-primary px-5 py-3">Search</button>
            {searched && (
              <button type="button" onClick={() => { setSearched(false); setQuery('') }}
                className="btn-secondary py-3 px-4 text-sm">Clear</button>
            )}
          </form>
        </div>

        <div className="page-container">
          {loading ? (
            <LoadingSpinner size="lg" text="Loading salons..." />
          ) : (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
                {displaySalons.map(salon => <SalonCard key={salon.id} salon={salon} />)}
              </div>
              {displaySalons.length === 0 && (
                <div className="py-20 text-center">
                  <p className="text-slate-500">No salons found.</p>
                </div>
              )}
              {!searched && totalPages > 1 && (
                <div className="flex justify-center gap-2 mt-10">
                  {Array.from({ length: Math.min(totalPages, 10) }).map((_, i) => (
                    <button key={i} onClick={() => setPage(i)}
                      className={`w-9 h-9 rounded-lg text-sm font-medium transition-all ${
                        i === page ? 'bg-primary-600 text-white' : 'text-slate-400 hover:bg-white/5'
                      }`}>{i + 1}</button>
                  ))}
                </div>
              )}
            </>
          )}
        </div>
      </div>
      <Footer />
    </div>
  )
}

export default SalonsListPage
