import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { apiError } from '../services/api'

export default function Auth({ register = false }) {
  const { user, login, register: createAccount } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  if (user) return <Navigate to={location.state?.from || (user.role === 'Admin' ? '/admin' : '/orders')} replace/>
  const submit = async event => {
    event.preventDefault()
    setBusy(true); setError('')
    try {
      const account = register
        ? await createAccount(form)
        : await login({ email: form.email, password: form.password })
      navigate(location.state?.from || (account.role === 'Admin' ? '/admin' : '/orders'), { replace: true })
    } catch (err) { setError(apiError(err)) }
    finally { setBusy(false) }
  }
  return <main className="container-page flex min-h-[65vh] items-center justify-center py-14">
    <form onSubmit={submit} className="w-full max-w-md rounded-3xl border border-line p-8 shadow-soft">
      <p className="eyebrow text-center text-rose">Lumière account</p>
      <h1 className="mt-3 text-center font-display text-4xl">{register ? 'Create your account' : 'Welcome back'}</h1>
      <p className="mt-3 text-center text-sm text-muted">{register ? 'Register as a customer to start shopping.' : 'Sign in to your account or admin workspace.'}</p>
      {register && <input required className="field mt-7" placeholder="Full name" autoComplete="name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })}/>}
      <input required type="email" className="field mt-5" placeholder="Email address" autoComplete="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })}/>
      <input required type="password" minLength={register ? 8 : undefined} className="field mt-3" placeholder="Password" autoComplete={register ? 'new-password' : 'current-password'} value={form.password} onChange={e => setForm({ ...form, password: e.target.value })}/>
      {error && <p role="alert" className="mt-4 text-sm text-rose">{error}</p>}
      <button disabled={busy} className="btn-primary mt-6 w-full disabled:opacity-60">{busy ? 'Please wait...' : register ? 'Create account' : 'Sign in'}</button>
      <p className="mt-5 text-center text-xs text-muted">{register ? 'Already have an account?' : 'New to Lumière?'} <Link className="font-semibold text-rose" state={location.state} to={register ? '/login' : '/register'}>{register ? 'Sign in' : 'Create an account'}</Link></p>
    </form>
  </main>
}
