# OTP Auth Service

Simple phone number authentication using OTP.

## Flow
1. `POST /auth/send-otp` with a phone number.
2. Service stores hashed OTP in MongoDB.
3. Service sends SMS via LocalSMS gateway (`/message`).
4. `POST /auth/verify-otp` with phone number + OTP.
5. On success, service creates a session token.

## Setup
1. Copy `.env.example` to `.env`.
2. Install dependencies:
   - `npm install`
3. Start service:
   - `npm run start`

## Endpoints

### Send OTP
`POST /auth/send-otp`

Body:
```json
{
  "phoneNumber": "+919998979407"
}
```

### Verify OTP
`POST /auth/verify-otp`

Body:
```json
{
  "phoneNumber": "+919998979407",
  "otp": "123456"
}
```

### Current user
`GET /auth/me`

Header:
`Authorization: Bearer <token>`

### Health
`GET /health`
