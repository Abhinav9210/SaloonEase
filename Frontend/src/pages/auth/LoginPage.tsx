import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { Eye, EyeOff, Scissors, Mail, Lock, Loader2 } from 'lucide-react'
import { useAppDispatch, useAppSelector } from '../../app/hooks'
import { login } from '../../features/auth/authSlice'

const LoginPage = () => {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const location = useLocation()
  const { loading } = useAppSelector(s => s.auth)
  const [form, setForm] = useState({ email: '', password: '' })
  const [showPass, setShowPass] = useState(false)

  const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname || null

  const roleDestMap: Record<string, string> = {
    CUSTOMER: '/dashboard/customer',
    OWNER: '/dashboard/owner',
    BARBER: '/dashboard/barber',
    ADMIN: '/dashboard/admin',
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const res = await dispatch(login(form))
    if (login.fulfilled.match(res)) {
      const role: string = res.payload.role
      const dest = from || roleDestMap[role] || '/'
      navigate(dest)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-mesh relative overflow-hidden">
      <div className="glow-orb w-96 h-96 bg-primary-600 -top-48 -left-48" />
      <div className="glow-orb w-96 h-96 bg-accent -bottom-48 -right-48" />

      <div className="w-full max-w-md animate-slide-up relative z-10">
        <div className="text-center mb-8">
          <div className="inline-flex w-14 h-14 rounded-2xl items-center justify-center mb-4"
            style={{ background: 'linear-gradient(135deg,#7c3aed,#06b6d4)' }}>
            <Scissors size={26} className="text-white" />
          </div>
          <h1 className="text-2xl font-display font-bold text-white">Welcome back</h1>
          <p className="text-slate-400 text-sm mt-1">Sign in to your SalonEase account</p>
        </div>

        <div className="glass-card p-8 rounded-2xl">
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="text-xs text-slate-400 font-medium mb-1.5 block">Email</label>
              <div className="relative">
                <Mail size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" />
                <input type="email" placeholder="you@example.com" value={form.email}
                  onChange={e => setForm({ ...form, email: e.target.value })}
                  className="input-field pl-10" required />
              </div>
            </div>
            <div>
              <label className="text-xs text-slate-400 font-medium mb-1.5 block">Password</label>
              <div className="relative">
                <Lock size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" />
                <input type={showPass ? 'text' : 'password'} placeholder="••••••••" value={form.password}
                  onChange={e => setForm({ ...form, password: e.target.value })}
                  className="input-field pl-10 pr-10" required />
                <button type="button" onClick={() => setShowPass(!showPass)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300">
                  {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>
            <button type="submit" disabled={loading} className="btn-primary w-full py-3">
              {loading ? <Loader2 size={18} className="animate-spin" /> : 'Sign In'}
            </button>
          </form>

          <div className="mt-6 space-y-3">
            <div className="flex items-center gap-3">
              <div className="flex-1 h-px bg-white/5" />
              <span className="text-xs text-slate-600">or login as demo</span>
              <div className="flex-1 h-px bg-white/5" />
            </div>
            <div className="grid grid-cols-3 gap-2">
              {(['CUSTOMER', 'OWNER', 'BARBER'] as const).map(role => (
                <button key={role}
                  onClick={() => setForm({ email: `demo${role.toLowerCase()}@salon.com`, password: 'password123' })}
                  className="text-xs px-2 py-2 rounded-lg text-slate-400 hover:text-white border border-white/5 hover:border-primary-600/40 hover:bg-primary-600/10 transition-all">
                  {role}
                </button>
              ))}
            </div>
          </div>
        </div>

        <p className="text-center text-sm text-slate-500 mt-5">
          Don't have an account?{' '}
          <Link to="/register" className="text-primary-400 hover:text-primary-300 font-medium">Create one</Link>
        </p>
      </div>
    </div>
  )
}

export default LoginPage
