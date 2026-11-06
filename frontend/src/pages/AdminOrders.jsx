import { useEffect, useState } from 'react'
import { orderApi, apiError } from '../services/api'
export default function AdminOrders() {
  const [orders, setOrders] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  useEffect(() => {
    orderApi.getAllForAdmin().then(setOrders).catch(e => setError(apiError(e))).finally(() => setLoading(false))
  }, [])
  return <div><p className="eyebrow text-rose">Sales</p><h1 className="mt-2 font-display text-4xl">Customer orders</h1>
    <p className="mt-2 text-sm text-muted">Orders saved in PostgreSQL, newest first.</p>
    {loading && <p className="mt-8 text-muted">Loading orders...</p>}
    {error && <p className="mt-8 text-rose">{error}</p>}
    {!loading && !error && orders.length === 0 && <p className="mt-8 text-muted">No customer orders yet.</p>}
    <div className="mt-7 space-y-4">{orders.map(o => <article key={o.id} className="rounded-2xl border border-line bg-white p-5">
      <div className="flex flex-wrap justify-between gap-4"><div><h2 className="font-semibold">#{o.id} · {o.name}</h2><p className="mt-1 text-xs text-muted">{o.email} · {o.phone} · {new Date(o.createdAt).toLocaleString()}</p></div><strong>${Number(o.total).toFixed(2)}</strong></div>
      <p className="mt-3 text-sm text-muted">{o.address}, {o.city} · {o.paymentMethod} · {o.status}</p>
      <div className="mt-3 border-t border-line pt-3">{o.items.map(item => <p className="text-xs text-muted" key={item.productId}>{item.productName} × {item.quantity} (${Number(item.lineTotal).toFixed(2)})</p>)}</div>
    </article>)}</div>
  </div>
}
