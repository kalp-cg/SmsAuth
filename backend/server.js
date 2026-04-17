const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');

const app = express();
const PORT = process.env.PORT || 3001;

app.use(cors());
app.use(bodyParser.json());

// In-memory queue
let messages = [];

// 1. Endpoint for YOUR frontend to queue an SMS
app.post('/api/messages/send', (req, res) => {
    const { phoneNumbers, message } = req.body;
    
    if (!phoneNumbers || !message) {
        return res.status(400).json({ error: 'phoneNumbers and message are required' });
    }

    const newMessages = phoneNumbers.map(number => ({
        id: uuidv4(),
        phoneNumber: number,
        message: message,
        status: 'pending', // pending, sent, failed
        createdAt: new Date().toISOString()
    }));

    messages.push(...newMessages);
    
    console.log(`[QUEUE] Added ${newMessages.length} messages to the queue. Total pending: ${messages.filter(m => m.status === 'pending').length}`);
    res.json({ success: true, queued: newMessages.length });
});

// 2. Endpoint for ANDROID APP to poll for pending messages
app.get('/api/messages/pending', (req, res) => {
    const pending = messages.filter(m => m.status === 'pending');
    res.json(pending);
});

// 3. Endpoint for ANDROID APP to report status back after sending
app.post('/api/messages/status', (req, res) => {
    const { id, status, error } = req.body;
    
    const msgIndex = messages.findIndex(m => m.id === id);
    if (msgIndex !== -1) {
        messages[msgIndex].status = status;
        if (error) messages[msgIndex].error = error;
        console.log(`[STATUS] Message ${id} marked as ${status}`);
        res.json({ success: true });
    } else {
        res.status(404).json({ error: 'Message not found' });
    }
});

// 4. Endpoint to view all messages (for debugging)
app.get('/api/messages', (req, res) => {
    res.json(messages);
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`=========================================`);
    console.log(`🚀 RELAY CLOUD SERVER RUNNING ON PORT ${PORT}`);
    console.log(`=========================================`);
    console.log(`\nWaiting for Android app to connect...`);
});
