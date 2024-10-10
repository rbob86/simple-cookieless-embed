import express, { Request, Response } from 'express'
import cors from 'cors'
import dotenv from 'dotenv'
import { LookerNodeSDK, NodeSettings } from '@looker/sdk-node'
import { environmentPrefix } from '@looker/sdk'
import { user } from './utils'

dotenv.config()

const app = express()
const port = 3000
const session_reference_store: { [key: string]: string } = {}
// Simulating storing the token data (in this case, only for this particular user)
// This should be stored securely, for each user, in a backend session cookie, a distriuted cache, etc.
const session_key = 'johnny-embed'

app.use(
  cors({
    origin: 'http://localhost:5173',
    credentials: true,
  }),
)
app.use(express.json())

/**
 * Uses the session to save or restore the session reference token.  If a session reference
 * token already exists and is still active, requests for a session will join the existing
 * session. Acquire session is called when an iframe is created.
 */
app.get('/acquire-embed-session', async (req: Request, res: Response) => {
  console.log('acquiring session')
  try {
    const userAgent = req.headers['user-agent']
    // Mirror signed URL use case of multiple iframes, where subsequent iframes inherit the cookie from the first session
    const session_reference_token = session_reference_store[session_key] ?? ''
    const request = {
      ...user,
      session_reference_token,
    }
    const sdk = LookerNodeSDK.init40(new NodeSettings(environmentPrefix))
    const response = await sdk.ok(
      sdk.acquire_embed_cookieless_session(request, {
        headers: {
          'User-Agent': userAgent,
        },
      }),
    )

    // Example Response:
    //
    // {
    //   "authentication_token": [authentication token],
    //   "authentication_token_ttl": 30,
    //   "navigation_token": [navigation token],
    //   "navigation_token_ttl": 304,
    //   "api_token": [api token],
    //   "api_token_ttl": 304,
    //   "session_reference_token": [session reference token],
    //   "session_reference_token_ttl": 2792
    // }

    const {
      authentication_token,
      authentication_token_ttl,
      navigation_token,
      navigation_token_ttl,
      session_reference_token_ttl,
      api_token,
      api_token_ttl,
    } = response

    // Must be stored securely and not exposed in browser
    session_reference_store[session_key] =
      response.session_reference_token as string

    res.json({
      authentication_token,
      authentication_token_ttl,
      navigation_token,
      navigation_token_ttl,
      session_reference_token_ttl,
      api_token,
      api_token_ttl,
    })
  } catch (err: any) {
    res.status(400).send({ message: err.message })
    console.error('embed session acquire failed', { err })
  }
})

/**
 * Uses the session to save or restore the session reference token. Looker
 * triggers calls to this endpoint periodically.
 */
app.put('/generate-embed-tokens', async function (req: Request, res: Response) {
  console.log('generating embed token')
  try {
    const session_reference_token = session_reference_store[session_key]
    const { api_token, navigation_token } = req.body as any

    const tokens = await generateEmbedTokens(
      req.headers['user-agent']!,
      session_reference_token,
      api_token,
      navigation_token,
    )
    res.json(tokens)
  } catch (err: any) {
    res.status(400).send({ message: err.message })
  }
})

async function generateEmbedTokens(
  userAgent: string,
  session_reference_token: string,
  api_token: string,
  navigation_token: string,
) {
  if (!session_reference_token) {
    console.error('embed session generate tokens failed')
    // If missing session reference -> treat as expired session
    return {
      session_reference_token_ttl: 0,
    }
  }

  try {
    const sdk = LookerNodeSDK.init40(new NodeSettings(environmentPrefix))
    const response = await sdk.ok(
      sdk.generate_tokens_for_cookieless_session(
        {
          api_token,
          navigation_token,
          session_reference_token: session_reference_token || '',
        },
        {
          headers: {
            'User-Agent': userAgent,
          },
        },
      ),
    )

    return {
      api_token: response.api_token,
      api_token_ttl: response.api_token_ttl,
      navigation_token: response.navigation_token,
      navigation_token_ttl: response.navigation_token_ttl,
      session_reference_token_ttl: response.session_reference_token_ttl,
    }
  } catch (error: any) {
    if (error.message?.includes('Invalid input tokens provided')) {
      // Currently the Looker UI does not know how to handle bad tokens. This
      // should not happen but if it does expire the session. If the token is
      // bad there is not much that that the Looker UI can do.
      return {
        session_reference_token_ttl: 0,
      }
    }

    console.error('embed session generate tokens failed', { error })
    throw error
  }
}

app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`)
})
