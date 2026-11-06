import { Routes, Route, useLocation, Navigate } from 'react-router-dom'
import { useLayoutEffect } from 'react'
import { AuthProvider, useAuth } from './context/AuthContext'
import { ShopProvider } from './context/ShopContext'
import Header from './components/Header'
import Footer from './components/Footer'
import AdminLayout from './components/AdminLayout'
import Home from './pages/Home'
import Shop from './pages/Shop'
import ProductDetail from './pages/ProductDetail'
import Cart from './pages/Cart'
import Checkout from './pages/Checkout'
import Auth from './pages/Auth'
import Orders from './pages/Orders'
import About from './pages/About'
import Contact from './pages/Contact'
import AdminDashboard from './pages/AdminDashboard'
import AdminProducts from './pages/AdminProducts'
import AdminProductForm from './pages/AdminProductForm'
import AdminUsers from './pages/AdminUsers'
import AdminOrders from './pages/AdminOrders'

function ScrollToTop() {
  const { pathname } = useLocation()
  useLayoutEffect(() => { window.scrollTo({ top: 0, left: 0, behavior: 'instant' }) }, [pathname])
  return null
}
function RequireAdmin({ children }) {
  const { user, checking } = useAuth()
  const location = useLocation()
  if (checking) return <p className="container-page py-16">Checking administrator access...</p>
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }}/>
  if (user.role !== 'Admin') return <Navigate to="/" replace />
  return children
}
function StoreLayout() {
  return <><Header/><Routes>
    <Route path="/" element={<Home/>}/>
    <Route path="/shop" element={<Shop/>}/>
    <Route path="/products/:id" element={<ProductDetail/>}/>
    <Route path="/cart" element={<Cart/>}/>
    <Route path="/checkout" element={<Checkout/>}/>
    <Route path="/login" element={<Auth/>}/>
    <Route path="/register" element={<Auth register/>}/>
    <Route path="/orders" element={<Orders/>}/>
    <Route path="/about" element={<About/>}/>
    <Route path="/contact" element={<Contact/>}/>
    <Route path="*" element={<Home/>}/>
  </Routes><Footer/></>
}
function AppRoutes() {
  return <><ScrollToTop/><Routes>
    <Route path="/admin" element={<RequireAdmin><AdminLayout/></RequireAdmin>}>
      <Route index element={<AdminDashboard/>}/>
      <Route path="products" element={<AdminProducts/>}/>
      <Route path="products/new" element={<AdminProductForm/>}/>
      <Route path="products/:id/edit" element={<AdminProductForm/>}/>
      <Route path="users" element={<AdminUsers/>}/>
      <Route path="orders" element={<AdminOrders/>}/>
    </Route>
    <Route path="*" element={<StoreLayout/>}/>
  </Routes></>
}
export default function App() {
  return <AuthProvider><ShopProvider><AppRoutes/></ShopProvider></AuthProvider>
}
