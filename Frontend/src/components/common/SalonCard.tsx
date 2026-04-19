import { Star, Scissors, MapPin, Clock } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import type { Salon } from '../../types'

interface Props { salon: Salon }

const SalonCard = ({ salon }: Props) => {
  const navigate = useNavigate()
  const heroImage = salon.images?.[0] || null

  return (
    <div
      onClick={() => navigate(`/salons/${salon.id}`)}
      className="block group cursor-pointer"
    >
      <div className="glass-card-hover rounded-2xl overflow-hidden h-full">
        {/* Image */}
        <div className="h-44 overflow-hidden relative bg-dark-700">
          {heroImage ? (
            <img src={heroImage} alt={salon.name}
              className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" />
          ) : (
            <div className="w-full h-full flex items-center justify-center"
              style={{ background: 'linear-gradient(135deg, rgba(124,58,237,0.15), rgba(6,182,212,0.1))' }}>
              <Scissors size={40} className="text-primary-600/40" />
            </div>
          )}
          {/* Booking fee badge */}
          <div className="absolute top-3 right-3 px-2.5 py-1 rounded-lg text-xs font-semibold text-white"
            style={{ background: 'rgba(10,15,30,0.8)', backdropFilter: 'blur(8px)', border: '1px solid rgba(255,255,255,0.1)' }}>
            ₹{salon.minimumBookingFee} min
          </div>
          {salon.status !== 'APPROVED' && (
            <div className="absolute top-3 left-3 badge-pending">{salon.status}</div>
          )}
        </div>

        {/* Content */}
        <div className="p-4">
          <h3 className="text-white font-semibold text-base leading-snug group-hover:text-primary-300 transition-colors line-clamp-1">
            {salon.name}
          </h3>
          <div className="flex items-center gap-1 mt-1.5">
            <MapPin size={11} className="text-slate-500" />
            <span className="text-xs text-slate-400 truncate">{salon.city}{salon.state ? `, ${salon.state}` : ''}</span>
          </div>
          {salon.description && (
            <p className="text-xs text-slate-500 mt-2 line-clamp-2 leading-relaxed">{salon.description}</p>
          )}
          <div className="flex items-center justify-between mt-3 pt-3 border-t border-white/5">
            <div className="flex items-center gap-1">
              <Star size={13} className="text-yellow-400 fill-yellow-400" />
              <span className="text-sm font-semibold text-white">{salon.rating?.toFixed(1) ?? '0.0'}</span>
              <span className="text-xs text-slate-500">({salon.totalReviews ?? 0})</span>
            </div>
            {salon.openTime && (
              <div className="flex items-center gap-1 text-xs text-slate-500">
                <Clock size={11} />
                {salon.openTime} – {salon.closeTime}
              </div>
            )}
          </div>
          {/* Book Now — plain div, not button inside link */}
          <div
            onClick={(e) => { e.stopPropagation(); navigate(`/salons/${salon.id}/book`) }}
            className="mt-3 w-full btn-primary py-2 text-xs text-center"
          >
            Book Now
          </div>
        </div>
      </div>
    </div>
  )
}

export default SalonCard
