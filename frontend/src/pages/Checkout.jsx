import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useShop } from '../context/ShopContext'
import { useAuth } from '../context/AuthContext'
import { orderApi, apiError } from '../services/api'

export default function Checkout() {
  const { cart, clearCart, refreshProducts } = useShop()
  const { user, checking } = useAuth()
  const [placed, setPlaced] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [form, setForm] = useState({ name: '', email: '', phone: '', address: '', city: '', paymentMethod: 'Cash on delivery' })
  const total = cart.reduce((s, p) => s + Number(p.price) * p.quantity, 0)
  if (checking) return <main className="container-page py-20">Checking your account...</main>
  if (!user) return <Navigate to="/login" replace state={{ from: '/checkout' }}/>
  if (placed) return <main className="container-page py-24 text-center">
    <p className="eyebrow text-rose">Thank you</p><h1 className="mt-3 font-display text-4xl">Order #{placed.id} received</h1>
    <p className="mt-4 text-muted">Your order was saved in PostgreSQL. Total: ${Number(placed.total).toFixed(2)}. No online payment has been processed.</p>
    <Link to="/orders" className="btn-primary mt-8">View your orders</Link>
  </main>
  if (!cart.length) return <main className="container-page py-24 text-center"><h1 className="font-display text-3xl">Your bag is empty</h1><Link to="/shop" className="btn-primary mt-6">Shop now</Link></main>
  const submit = async e => {
    e.preventDefault()
    setError('');setBusy(true)
    try {
      const order = await orderApi.create({
        ...form, email: form.email || user.email, name: form.name || user.name,
        items: cart.map(item => ({ productId: item.id, quantity: item.quantity })),
      })
      setPlaced(order)
      clearCart()
      refreshProducts().catch(() => {})
    } catch (err) { setError(apiError(err)) }
    finally { setBusy(false) }
  }
  const change = e => setForm({ ...form, [e.target.name]: e.target.value })
  return <main className="container-page max-w-3xl py-12">
    <p className="eyebrow text-rose">Almost yours</p><h1 className="mt-3 font-display text-4xl">Checkout</h1>
    <form onSubmit={submit} className="mt-8 grid gap-8 md:grid-cols-[1fr_260px]">
      <div className="space-y-4"><h2 className="font-semibold">Contact & delivery</h2>
        <input required className="field" name="name" value={form.name} onChange={change} placeholder={`Full name (${user.name})`}/>
        <input required type="email" className="field" name="email" value={form.email} onChange={change} placeholder={`Email (${user.email})`}/>
        <input required className="field" name="phone" value={form.phone} onChange={change} placeholder="Phone number"/>
        <input required className="field" name="address" value={form.address} onChange={change} placeholder="Delivery address"/>
        <input required className="field" name="city" value={form.city} onChange={change} placeholder="City"/>
        <label className="block text-sm font-medium">Payment method<select className="field mt-2" name="paymentMethod" value={form.paymentMethod} onChange={change}><option>Cash on delivery</option><option>Bank transfer (demo)</option></select></label>
        {error && <p role="alert" className="rounded-lg bg-red-50 p-3 text-sm text-rose">{error}</p>}
        <button disabled={busy} className="btn-primary w-full disabled:opacity-60">{busy ? 'Submitting order...' : 'Place order'}</button>
        <p className="text-xs text-muted">This is a training checkout. No bank transfer or payment is collected.</p>
      </div>
      <aside className="h-fit rounded-2xl bg-cream p-5"><h2 className="font-display text-xl">Summary</h2>
        {cart.map(p => <div key={p.id} className="mt-3 flex justify-between gap-3 text-xs"><span>{p.name} × {p.quantity}</span><span>${(p.price * p.quantity).toFixed(2)}</span></div>)}
        <div className="mt-5 flex justify-between border-t border-line pt-4 font-semibold"><span>Estimated total</span><span>${total.toFixed(2)}</span></div>
        <p className="mt-2 text-xs text-muted">Server confirms prices and stock at checkout.</p>
      </aside>
    </form>
  </main>
}
