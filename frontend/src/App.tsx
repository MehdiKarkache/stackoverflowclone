import { BrowserRouter, Route, Routes } from 'react-router'
import { AuthProvider } from './auth/AuthContext'
import Layout from './components/Layout'
import RequireAuth from './components/RequireAuth'
import AskPage from './pages/AskPage'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import NotFoundPage from './pages/NotFoundPage'
import QuestionPage from './pages/QuestionPage'
import RegisterPage from './pages/RegisterPage'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<HomePage />} />
            <Route path="questions/:id" element={<QuestionPage />} />
            <Route path="ask" element={<RequireAuth><AskPage /></RequireAuth>} />
            <Route path="questions/:id/edit" element={<RequireAuth><AskPage /></RequireAuth>} />
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<RegisterPage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
