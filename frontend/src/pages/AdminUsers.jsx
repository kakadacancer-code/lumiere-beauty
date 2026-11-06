import { useEffect, useMemo, useState } from 'react'
import { Search, UserPlus, Trash2 } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { adminUserApi, apiError } from '../services/api'

export default function AdminUsers() {
  const { user: signedIn } = useAuth()
  const [users, setUsers] = useState([])
  const [query, setQuery] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'Customer' })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const load = async () => { try { setUsers(await adminUserApi.getAll());setError('') } catch (err) { setError(apiError(err)) } }
  useEffect(() => { load() }, [])
  const filtered = useMemo(() => users.filter(u => `${u.name} ${u.email} ${u.role}`.toLowerCase().includes(query.toLowerCase())), [users, query])
  const addUser = async event => {
    event.preventDefault();setError('');setMessage('');setBusy(true)
    try {
      await adminUserApi.create(form)
      setForm({ name: '', email: '', password: '', role: 'Customer' });setShowForm(false)
      setMessage('Account created in PostgreSQL. Share the initial password securely.')
      await load()
    } catch (err) { setError(apiError(err)) } finally { setBusy(false) }
  }
  const updateUser = async (id, changes) => {
    setError('');setMessage('')
    try { await adminUserApi.update(id, changes);await load() }
    catch (err) { setError(apiError(err)) }
  }
  const removeUser = async user => {
    if (!window.confirm(`Delete ${user.email}? Accounts with orders must be suspended instead.`)) return
    setError('');setMessage('')
    try { await adminUserApi.remove(user.id);await load() }
    catch (err) { setError(apiError(err)) }
  }
  return <div>
    <div className="flex flex-wrap items-end justify-between gap-4"><div><p className="eyebrow text-sage">Store access</p><h1 className="mt-2 font-display text-4xl text-wine">User management</h1><p className="mt-2 text-sm text-muted">Manage real customer and admin accounts.</p></div><button className="btn-primary" onClick={() => setShowForm(v => !v)}><UserPlus size={16}/> Add user</button></div>
    <div className="mt-7 grid gap-4 sm:grid-cols-3">{[
      ['Total accounts',users.length],['Administrators',users.filter(x => x.role === 'Admin').length],['Active accounts',users.filter(x => x.status === 'Active').length]
    ].map(([title, value]) => <div key={title} className="rounded-2xl border border-line bg-white p-5"><p className="text-xs text-muted">{title}</p><p className="mt-2 font-display text-3xl text-wine">{value}</p></div>)}</div>
    {showForm && <form onSubmit={addUser} className="mt-6 rounded-2xl border border-line bg-white p-6"><h2 className="font-display text-2xl text-wine">Add an account</h2><p className="mt-1 text-xs text-muted">Create a customer or administrator with an initial password (at least 8 characters).</p>
      <div className="mt-5 grid gap-4 md:grid-cols-2">
        <input required className="field" placeholder="Full name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })}/>
        <input required type="email" className="field" placeholder="Email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })}/>
        <input required type="password" minLength={8} className="field" placeholder="Initial password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })}/>
        <select className="field" value={form.role} onChange={e => setForm({ ...form, role: e.target.value })}><option>Customer</option><option>Admin</option></select>
      </div>
      <div className="mt-5 flex gap-3"><button disabled={busy} className="btn-primary">{busy ? 'Creating...' : 'Create account'}</button><button type="button" className="btn-outline" onClick={() => setShowForm(false)}>Cancel</button></div>
    </form>}
    {error && <p role="alert" className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-rose">{error}</p>}
    {message && <p className="mt-4 rounded-lg bg-cream p-3 text-sm text-sage">{message}</p>}
    <label className="relative mt-7 block max-w-xl"><Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted"/><input className="field !pl-11" placeholder="Search name, email or role" value={query} onChange={e => setQuery(e.target.value)}/></label>
    <div className="mt-5 overflow-hidden rounded-2xl border border-line bg-white"><div className="hidden gap-4 bg-cream px-5 py-4 text-xs font-semibold text-muted md:grid md:grid-cols-[1.3fr_1.4fr_1fr_1fr_50px]"><span>User</span><span>Email</span><span>Role</span><span>Status</span><span>Delete</span></div>
    {filtered.map(u => <div key={u.id} className="grid gap-3 border-t border-line p-4 md:grid-cols-[1.3fr_1.4fr_1fr_1fr_50px] md:items-center md:gap-4"><div><p className="text-sm font-semibold">{u.name}</p><p className="text-xs text-muted">ID #{u.id}</p></div><p className="break-all text-xs text-muted">{u.email}</p>
      <select disabled={u.id === signedIn?.id} aria-label={`Role for ${u.name}`} className="field !h-10 text-xs" value={u.role} onChange={e => updateUser(u.id, { role: e.target.value })}><option>Customer</option><option>Admin</option></select>
      <select disabled={u.id === signedIn?.id} aria-label={`Status for ${u.name}`} className="field !h-10 text-xs" value={u.status} onChange={e => updateUser(u.id, { status: e.target.value })}><option>Active</option><option>Suspended</option></select>
      <button disabled={u.id === signedIn?.id} title="Delete account" onClick={() => removeUser(u)} className="grid h-10 w-10 place-items-center rounded-xl bg-cream text-rose disabled:opacity-40"><Trash2 size={16}/></button>
    </div>)}
    {filtered.length === 0 && <p className="p-10 text-center text-sm text-muted">No matching accounts.</p>}</div>
    <p className="mt-4 text-xs leading-6 text-muted">All updates are saved to PostgreSQL. Passwords are hashed on the server; suspended users cannot sign in. You cannot change or delete your own admin account.</p>
  </div>
}
