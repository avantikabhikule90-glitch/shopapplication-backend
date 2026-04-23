require('dotenv').config();
const express = require('express');
const nodemailer = require('nodemailer');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const cors = require('cors');
const mongoose = require('mongoose');

const app = express();
app.use(express.json());
app.use(cors());

// Connect MongoDB
mongoose.connect(process.env.MONGODB_URI)
  .then(() => console.log('MongoDB connected'))
  .catch(err => console.error('MongoDB error:', err));

// ── SCHEMAS ──────────────────────────────────────────────────────
const userSchema = new mongoose.Schema({
  email: { type: String, required: true, unique: true },
  passwordHash: String,
  points: { type: Number, default: 0 },
  createdAt: { type: Date, default: Date.now }
});

const orderSchema = new mongoose.Schema({
  userEmail: String,
  items: [{ name: String, price: Number, qty: Number }],
  total: Number,
  pointsUsed: { type: Number, default: 0 },
  discount: { type: Number, default: 0 },
  finalTotal: Number,
  status: { type: String, default: 'Pending' },
  pointsEarned: { type: Number, default: 0 },
  createdAt: { type: Date, default: Date.now }
});

const User = mongoose.model('User', userSchema);
const Order = mongoose.model('Order', orderSchema);

const otpStore = {};

// ── EMAIL TRANSPORTER ─────────────────────────────────────────────
const transporter = nodemailer.createTransport({
  host: 'smtp.gmail.com',
  port: 465,
  secure: true,
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS,
  },
  tls: { rejectUnauthorized: false }
});

function generateOTP() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

// ── ROUTES ────────────────────────────────────────────────────────

// Health check
app.get('/', (req, res) => {
  res.json({ status: 'ShopApplication backend is running!' });
});

// Route 1: Send OTP
app.post('/auth/send-otp', async (req, res) => {
  try {
    const { email } = req.body;
    if (!email || !email.includes('@'))
      return res.status(400).json({ success: false, message: 'Invalid email' });
    const otp = generateOTP();
    otpStore[email] = { otp, expiry: Date.now() + 10 * 60 * 1000 };
    await transporter.sendMail({
      from: '"ShopApplication" <' + process.env.EMAIL_USER + '>',
      to: email,
      subject: 'Your ShopApplication OTP Code',
      html:
        '<div style="font-family:Arial;padding:30px;background:#fff;">' +
        '<h2 style="color:#E8501A">ShopApplication</h2>' +
        '<p style="font-size:16px">Your OTP verification code:</p>' +
        '<div style="background:#E8501A;color:#fff;font-size:40px;font-weight:bold;' +
        'text-align:center;padding:25px;border-radius:12px;letter-spacing:10px;margin:20px 0">' +
        otp + '</div>' +
        '<p style="color:#999;font-size:14px">Valid for 10 minutes only. Do not share this code.</p>' +
        '</div>'
    });
    res.json({ success: true, message: 'OTP sent to ' + email });
  } catch (error) {
    console.error('OTP ERROR:', error.message);
    res.status(500).json({ success: false, message: error.message });
  }
});

// Route 2: Verify OTP
app.post('/auth/verify-otp', (req, res) => {
  try {
    const { email, otp } = req.body;
    const record = otpStore[email];
    if (!record) return res.status(400).json({ success: false, message: 'OTP not found' });
    if (Date.now() > record.expiry) {
      delete otpStore[email];
      return res.status(400).json({ success: false, message: 'OTP expired' });
    }
    if (record.otp !== otp) return res.status(400).json({ success: false, message: 'Wrong OTP' });
    delete otpStore[email];
    res.json({ success: true, message: 'OTP verified' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Route 3: Check if user exists
app.post('/auth/check-user', async (req, res) => {
  try {
    const { email } = req.body;
    const user = await User.findOne({ email });
    res.json({ success: true, exists: !!user });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Route 4: Create Password
app.post('/auth/create-password', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!password || password.length < 6)
      return res.status(400).json({ success: false, message: 'Password too short' });
    const passwordHash = await bcrypt.hash(password, 10);
    await User.findOneAndUpdate({ email }, { email, passwordHash }, { upsert: true, new: true });
    const token = jwt.sign({ email }, process.env.JWT_SECRET, { expiresIn: '7d' });
    res.json({ success: true, token });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Route 5: Login with password
app.post('/auth/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    const user = await User.findOne({ email });
    if (!user) return res.status(400).json({ success: false, message: 'User not found' });
    const valid = await bcrypt.compare(password, user.passwordHash);
    if (!valid) return res.status(400).json({ success: false, message: 'Wrong password' });
    const token = jwt.sign({ email }, process.env.JWT_SECRET, { expiresIn: '7d' });
    res.json({ success: true, token });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Route 6: Place Order
app.post('/orders/place', async (req, res) => {
  try {
    const { email, items, total, pointsUsed } = req.body;
    const user = await User.findOne({ email });
    if (!user) return res.status(400).json({ success: false, message: 'User not found' });
    let discount = 0;
    if (pointsUsed > 0 && user.points >= pointsUsed) {
      discount = pointsUsed;
      user.points -= pointsUsed;
    }
    const finalTotal = total - discount;
    const pointsEarned = Math.floor(finalTotal * 0.1);
    user.points += pointsEarned;
    await user.save();
    const order = await Order.create({
      userEmail: email, items, total, pointsUsed, discount, finalTotal, pointsEarned, status: 'Pending'
    });
    res.json({ success: true, orderId: order._id, pointsEarned, totalPoints: user.points, discount });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Route 7: Get User Orders
app.get('/orders/user/:email', async (req, res) => {
  try {
    const orders = await Order.find({ userEmail: req.params.email }).sort({ createdAt: -1 });
    res.json({ success: true, orders });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Route 8: Get All Orders (Shop Owner)
app.get('/orders/all', async (req, res) => {
  try {
    const orders = await Order.find().sort({ createdAt: -1 });
    res.json({ success: true, orders });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Route 9: Update Order Status (Shop Owner)
app.post('/orders/update-status', async (req, res) => {
  try {
    const { orderId, status } = req.body;
    await Order.findByIdAndUpdate(orderId, { status });
    res.json({ success: true, message: 'Status updated to ' + status });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Route 10: Get User Points
app.get('/user/points/:email', async (req, res) => {
  try {
    const user = await User.findOne({ email: req.params.email });
    if (!user) return res.status(400).json({ success: false, message: 'User not found' });
    res.json({ success: true, points: user.points });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

app.listen(process.env.PORT || 3000, '0.0.0.0', () => {
  console.log('Backend running on port ' + (process.env.PORT || 3000));
});
