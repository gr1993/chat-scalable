
import Layout from './layout/Layout';

import { BrowserRouter, Routes, Route } from 'react-router-dom';

import { connectWebSocket, disconnectWebSocket } from './common/socketClient';
import { useStrictEffect } from './hooks/useStrictEffect';

import GlobalStyle from './components/GlobalStyle'
import RequireAuth from './components/RequireAuth';

import LoginPage from './pages/LoginPage';
import ChatRooms from './pages/ChatRooms';
import ChatView from './pages/ChatView';

import { useAppStore } from './store/useAppStore';

function App() {
  const { setWsSessionId } = useAppStore();

  useStrictEffect(() => {
    connectWebSocket((client, sessionId) => {
      setWsSessionId(sessionId);
    });
    return () => disconnectWebSocket();
  }, []);

  return (
    <>
      <GlobalStyle />
      <BrowserRouter>
        <Layout>
          <Routes>
            <Route path="/" element={<LoginPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/room" element={<RequireAuth><ChatRooms /></RequireAuth>} />
            <Route path="/chat" element={<RequireAuth><ChatView /></RequireAuth>} />
          </Routes>
        </Layout>
      </BrowserRouter>
    </>
  )
}

export default App
