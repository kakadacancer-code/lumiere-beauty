import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { orderApi, apiError } from '../services/api'
export default function Orders() {
  const { user } = useAuth()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  useEffect(() => {
    if (!user) { setLoading(false); return }
    let alive = true
    orderApi.getMyOrders().then(list => { if (alive) setOrders(list) })
      .catch(err => { if (alive) setError(apiError(err)) })
      .finally(() => { if (alive) setLoading(false) })
    return () => { alive = false }
  }, [user?.id])
  return <main className="container-page min-h-[55vh] py-16">
    <p className="eyebrow text-rose">Your account</p><h1 className="mt-3 font-display text-4xl">Order history</h1>
    {!user ? <div className="mt-8 rounded-2xl border border-line p-8 text-center"><p className="text-muted">Sign in to view your orders.</p><Link to="/login" className="btn-primary mt-5">Sign in</Link></div>
      : loading ? <p className="mt-8 text-muted">Loading your orders...</p>
      : error ? <p role="alert" className="mt-8 text-rose">{error}</p>
      : orders.length === 0 ? <p className="mt-8 text-muted">You haven't placed an order yet. <Link className="text-rose underline" to="/shop">Browse products</Link></p>
      : <div className="mt-8 space-y-5">{orders.map(o => <article key={o.id} className="rounded-2xl border border-line bg-white p-6">
          <div className="flex flex-wrap justify-between gap-3"><div><h2 className="font-semibold">Order #{o.id} · {o.status}</h2><p className="mt-1 text-xs text-muted">{new Date(o.createdAt).toLocaleString()}</p></div><strong>${Number(o.total).toFixed(2)}</strong></div>
          <div className="mt-4 space-y-2">{o.items.map(item => <p key={item.productId} className="flex justify-between text-sm text-muted"><span>{item.productName} × {item.quantity}</span><span>${Number(item.lineTotal).toFixed(2)}</span></p>)}</div>
          <p className="mt-4 border-t border-line pt-3 text-xs text-muted">{o.paymentMethod} · {o.city} · {o.address}</p>
        </article>)}</div>}
  </main>
}
