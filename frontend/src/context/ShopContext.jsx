import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { productApi, apiError } from '../services/api'
import { useAuth } from './AuthContext'

const ShopContext = createContext(null)
export function ShopProvider({ children }) {
  const { user } = useAuth()
  const [products, setProducts] = useState([])
  const [loadingProducts, setLoadingProducts] = useState(true)
  const [productsError, setProductsError] = useState('')
  const [cart, setCart] = useState(() => {
    try { return JSON.parse(localStorage.getItem('lumiere-cart')) || [] }
    catch { return [] }
  })

  const refreshProducts = useCallback(async () => {
    setLoadingProducts(true)
    try {
      const list = await productApi.getAll()
      setProducts(list)
      setProductsError('')
      return list
    } catch (error) {
      setProductsError(apiError(error))
      setProducts([])
      throw error
    } finally { setLoadingProducts(false) }
  }, [])

  useEffect(() => { refreshProducts().catch(() => {}) }, [refreshProducts, user?.role, user?.id])
  useEffect(() => { localStorage.setItem('lumiere-cart', JSON.stringify(cart)) }, [cart])

  function addToCart(product) {
    if (product.status !== 'Active' || product.stock < 1) return
    setCart(current => {
      const found = current.find(item => item.id === product.id)
      if (found) {
        if (found.quantity >= product.stock) return current
        return current.map(item => item.id === product.id ? { ...product, quantity: item.quantity + 1 } : item)
      }
      return [...current, { ...product, quantity: 1 }]
    })
  }
  function updateQuantity(id, quantity) {
    setCart(current => quantity <= 0 ? current.filter(p => p.id !== id) :
      current.map(p => p.id === id ? { ...p, quantity: Math.max(1, Math.min(quantity, products.find(x => x.id === id)?.stock ?? p.stock)) } : p))
  }
  const removeFromCart = id => setCart(current => current.filter(item => item.id !== id))
  const clearCart = () => setCart([])
  async function saveProduct(product) {
    const { id, deleted, ...input } = product
    const saved = id ? await productApi.update(id, input) : await productApi.create(input)
    await refreshProducts()
    return saved
  }
  async function deleteProduct(id) {
    await productApi.remove(id)
    await refreshProducts()
  }
  const value = useMemo(() => ({ products, loadingProducts, productsError, refreshProducts, cart,
    addToCart, updateQuantity, removeFromCart, clearCart, saveProduct, deleteProduct }),
    [products, loadingProducts, productsError, refreshProducts, cart])
  return <ShopContext.Provider value={value}>{children}</ShopContext.Provider>
}
export const useShop = () => useContext(ShopContext)
