import { createBrowserRouter, Navigate } from 'react-router-dom'

import ClinicHomePage from '@/pages/ClinicHomePage'
import MainMenuPage from '@/pages/MainMenuPage'

const routes = [
  {
    path: '/',
    element: <MainMenuPage />,
  },
  {
    path: '/game',
    element: <ClinicHomePage />,
  },
  {
    path: '*',
    element: <Navigate replace to="/" />,
  },
]

export const router = createBrowserRouter(routes)
