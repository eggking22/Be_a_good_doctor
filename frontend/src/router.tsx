import { createBrowserRouter, Navigate } from 'react-router-dom'

import ClinicHomePage from '@/pages/ClinicHomePage'

const routes = [
  {
    path: '/',
    element: <Navigate replace to="/game" />,
  },
  {
    path: '/game',
    element: <ClinicHomePage />,
  },
  {
    path: '*',
    element: <Navigate replace to="/game" />,
  },
]

export const router = createBrowserRouter(routes)
