import { useState } from 'react';

function ChatWidget({ baseUrl }) {
  const [messages, setMessages] = useState([
    { role: 'agent', text: 'Bonjour ! Je suis LudoBot. Pose-moi une question sur les jeux de table.' }
  ]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const [error, setError] = useState(null);

  const sendMessage = async () => {
    if (!input.trim()) {
      return;
    }

    const userMessage = { role: 'user', text: input };
    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setSending(true);
    setError(null);

    try {
      const response = await fetch(`${baseUrl}/api/agent/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId: '1', message: input })
      });

      if (!response.ok) {
        throw new Error('Erreur serveur');
      }

      const data = await response.json();
      const agentReply = {
        role: 'agent',
        text: data.reply || 'Je n’ai pas compris, peux-tu reformuler ?',
        reasoning: data.reasoning || []
      };

      setMessages((prev) => [...prev, agentReply]);
    } catch (e) {
      setError('La requête a échoué. Vérifie le backend.');
    } finally {
      setSending(false);
    }
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    sendMessage();
  };

  return (
    <div className="chat-widget">
      <h2>LudoBot</h2>
      <div className="chat-messages">
        {messages.map((message, index) => (
          <div key={index} className={`chat-message ${message.role}`}>
            <div className="chat-role">{message.role === 'agent' ? 'LudoBot' : 'Vous'}</div>
            <div>{message.text}</div>
            {message.reasoning && message.reasoning.length > 0 && (
              <div className="chat-reasoning">
                <strong>Raisonnement :</strong>
                <ul>
                  {message.reasoning.map((step, idx) => (
                    <li key={idx}>{step}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        ))}
      </div>

      <form className="chat-form" onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Demande une recommandation ou pose une question..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          disabled={sending}
        />
        <button type="submit" disabled={sending}>Envoyer</button>
      </form>

      {error && <div className="chat-error">{error}</div>}
    </div>
  );
}

export default ChatWidget;
