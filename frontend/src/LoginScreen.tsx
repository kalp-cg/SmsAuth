import { useCallback, useEffect, useRef, useState } from 'react';
import { useAuth, AUTH_API } from './AuthContext';

// ─── Types ────────────────────────────────────────────────────────────────────
type Step = 'phone' | 'otp';

interface Banner {
  ok: boolean;
  text: string;
}

// ─── Constants ────────────────────────────────────────────────────────────────
const RESEND_SECONDS = 30;
const OTP_LENGTH = 6;

// ─── Component ────────────────────────────────────────────────────────────────
export default function LoginScreen() {
  const { login } = useAuth();

  const [step, setStep] = useState<Step>('phone');
  const [phone, setPhone] = useState('');
  const [countryCode, setCountryCode] = useState('+91');
  const [otp, setOtp] = useState<string[]>(Array(OTP_LENGTH).fill(''));
  const [loading, setLoading] = useState(false);
  const [banner, setBanner] = useState<Banner | null>(null);
  const [countdown, setCountdown] = useState(0);
  const [devOtp, setDevOtp] = useState<string | null>(null);

  const otpRefs = useRef<(HTMLInputElement | null)[]>([]);
  const countdownRef = useRef<ReturnType<typeof setInterval>>(undefined);

  // ── Countdown timer ──
  useEffect(() => {
    if (countdown <= 0) {
      if (countdownRef.current) clearInterval(countdownRef.current);
      return;
    }
    countdownRef.current = setInterval(() => {
      setCountdown((c) => {
        if (c <= 1) {
          clearInterval(countdownRef.current!);
          return 0;
        }
        return c - 1;
      });
    }, 1000);
    return () => { if (countdownRef.current) clearInterval(countdownRef.current); };
  }, [countdown]);

  // ── Full phone number ──
  const fullPhone = `${countryCode}${phone.replace(/\D/g, '')}`;

  // ── Send OTP ──
  const handleSendOtp = useCallback(async () => {
    const cleaned = phone.replace(/\D/g, '');
    if (cleaned.length < 7) {
      setBanner({ ok: false, text: 'Please enter a valid phone number.' });
      return;
    }

    setLoading(true);
    setBanner(null);

    try {
      const res = await fetch(`${AUTH_API}/auth/send-otp`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phoneNumber: fullPhone }),
      });
      const data = await res.json();

      if (!res.ok) {
        throw new Error(data.error || `Request failed (${res.status})`);
      }

      // If dev mode returns OTP in response, save it for display
      if (data.otp) {
        setDevOtp(data.otp);
      }

      setBanner({ ok: true, text: '✅ OTP sent to your phone!' });
      setStep('otp');
      setCountdown(RESEND_SECONDS);
      setOtp(Array(OTP_LENGTH).fill(''));

      // Focus first OTP input after transition
      setTimeout(() => otpRefs.current[0]?.focus(), 200);
    } catch (err) {
      setBanner({ ok: false, text: `❌ ${(err as Error).message}` });
    } finally {
      setLoading(false);
    }
  }, [phone, fullPhone]);

  // ── OTP input handlers ──
  const handleOtpChange = (index: number, value: string) => {
    if (!/^\d*$/.test(value)) return;

    const newOtp = [...otp];

    if (value.length > 1) {
      // Handle paste: distribute digits across boxes
      const digits = value.split('').filter((c) => /\d/.test(c)).slice(0, OTP_LENGTH);
      digits.forEach((d, i) => {
        if (index + i < OTP_LENGTH) newOtp[index + i] = d;
      });
      setOtp(newOtp);
      const nextIdx = Math.min(index + digits.length, OTP_LENGTH - 1);
      otpRefs.current[nextIdx]?.focus();
      return;
    }

    newOtp[index] = value;
    setOtp(newOtp);

    if (value && index < OTP_LENGTH - 1) {
      otpRefs.current[index + 1]?.focus();
    }
  };

  const handleOtpKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      otpRefs.current[index - 1]?.focus();
    }
    if (e.key === 'Enter') {
      handleVerify();
    }
  };

  // ── Verify OTP ──
  const handleVerify = useCallback(async () => {
    const code = otp.join('');
    if (code.length !== OTP_LENGTH) {
      setBanner({ ok: false, text: 'Please enter the complete 6-digit code.' });
      return;
    }

    setLoading(true);
    setBanner(null);

    try {
      const res = await fetch(`${AUTH_API}/auth/verify-otp`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phoneNumber: fullPhone, otp: code }),
      });
      const data = await res.json();

      if (!res.ok) {
        throw new Error(data.error || `Verification failed (${res.status})`);
      }

      // Success — log the user in
      login(data.token, data.user);
    } catch (err) {
      setBanner({ ok: false, text: `❌ ${(err as Error).message}` });
    } finally {
      setLoading(false);
    }
  }, [otp, fullPhone, login]);

  // ── Resend OTP ──
  const handleResend = async () => {
    setDevOtp(null);
    await handleSendOtp();
  };

  // ── Go back to phone step ──
  const goBack = () => {
    setStep('phone');
    setBanner(null);
    setOtp(Array(OTP_LENGTH).fill(''));
    setDevOtp(null);
  };

  // ── Render ──
  return (
    <div className="login-backdrop">
      <div className="login-container">
        {/* Branding */}
        <div className="login-brand">
          <div className="login-logo-icon">📱</div>
          <div className="login-logo-text">
            SMS<span>Gate</span>
          </div>
          <p className="login-tagline">Your personal SMS infrastructure</p>
        </div>

        {/* Card */}
        <div className="login-card">
          <div className="login-card-inner">
            {step === 'phone' ? (
              /* ── Step 1: Phone Number ── */
              <div className="login-step" key="phone-step">
                <div className="login-step-header">
                  <h2>Welcome back</h2>
                  <p>Enter your phone number to continue</p>
                </div>

                <div className="login-phone-group">
                  <select
                    className="login-country-select"
                    value={countryCode}
                    onChange={(e) => setCountryCode(e.target.value)}
                    id="country-code-select"
                  >
                    <option value="+91">🇮🇳 +91</option>
                    <option value="+1">🇺🇸 +1</option>
                    <option value="+44">🇬🇧 +44</option>
                    <option value="+971">🇦🇪 +971</option>
                    <option value="+61">🇦🇺 +61</option>
                    <option value="+86">🇨🇳 +86</option>
                    <option value="+81">🇯🇵 +81</option>
                    <option value="+49">🇩🇪 +49</option>
                    <option value="+33">🇫🇷 +33</option>
                    <option value="+55">🇧🇷 +55</option>
                  </select>
                  <input
                    className="login-phone-input"
                    type="tel"
                    placeholder="9978 879 407"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSendOtp()}
                    id="login-phone-input"
                    autoFocus
                  />
                </div>

                {banner && (
                  <div className={`result-banner ${banner.ok ? 'success' : 'error'}`}>
                    {banner.text}
                  </div>
                )}

                <button
                  className="btn btn-primary btn-lg btn-full login-submit"
                  onClick={handleSendOtp}
                  disabled={loading}
                  id="send-otp-btn"
                >
                  {loading ? (
                    <>
                      <span className="spinner" /> Sending...
                    </>
                  ) : (
                    'Send OTP'
                  )}
                </button>

                <p className="login-footer-text">
                  We'll send a 6-digit code to your phone via SMS
                </p>
              </div>
            ) : (
              /* ── Step 2: OTP Verification ── */
              <div className="login-step" key="otp-step">
                <div className="login-step-header">
                  <h2>Verify your number</h2>
                  <p>
                    Enter the 6-digit code sent to{' '}
                    <strong>
                      {countryCode} {phone}
                    </strong>
                  </p>
                </div>

                {/* Dev OTP hint */}
                {devOtp && (
                  <div className="dev-otp-hint">
                    Dev mode OTP: <strong>{devOtp}</strong>
                  </div>
                )}

                {/* OTP Boxes */}
                <div className="otp-boxes">
                  {otp.map((digit, i) => (
                    <input
                      key={i}
                      ref={(el) => { otpRefs.current[i] = el; }}
                      className="otp-box"
                      type="text"
                      inputMode="numeric"
                      maxLength={OTP_LENGTH}
                      value={digit}
                      onChange={(e) => handleOtpChange(i, e.target.value)}
                      onKeyDown={(e) => handleOtpKeyDown(i, e)}
                      onFocus={(e) => e.target.select()}
                      id={`otp-box-${i}`}
                      autoComplete="one-time-code"
                    />
                  ))}
                </div>

                {banner && (
                  <div className={`result-banner ${banner.ok ? 'success' : 'error'}`}>
                    {banner.text}
                  </div>
                )}

                <button
                  className="btn btn-primary btn-lg btn-full login-submit"
                  onClick={handleVerify}
                  disabled={loading || otp.join('').length !== OTP_LENGTH}
                  id="verify-otp-btn"
                >
                  {loading ? (
                    <>
                      <span className="spinner" /> Verifying...
                    </>
                  ) : (
                    'Verify and Login'
                  )}
                </button>

                {/* Resend / back row */}
                <div className="otp-actions">
                  <button className="btn-link" onClick={goBack}>
                    ← Change number
                  </button>
                  {countdown > 0 ? (
                    <span className="otp-countdown">
                      Resend in <strong>{countdown}s</strong>
                    </span>
                  ) : (
                    <button
                      className="btn-link"
                      onClick={handleResend}
                      disabled={loading}
                    >
                      Resend OTP
                    </button>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Footer */}
        <div className="login-powered">
          Powered by <strong>Kalp SMS Bridge</strong> · Local SMS infrastructure
        </div>
      </div>
    </div>
  );
}
