# Simple Cookieless Embed Demo

## Overview

This TypeScript-based web app contains a backend server for generating tokens (via the Looker API) that are used to authenticate each cookieless session request. The front end, leveraging the Looker Embed SDK, will call the appropriate backend endpoints and use the responses to embed a Looker dashboard.

## Installation

- `cd backend && npm install`
- Change _.env.example_ to _.env_ and fill out the target Looker instance base url, and the Looker API client ID and secret
- In _utils.ts_, change the permissions and models values accordingly, to match what the embed user should have access to
- `npm run dev` to start backend server
- `cd ..`
- `cd frontend && npm install`
- In _App.tsx_:
  - Change the first argument to `.initCookieless` to the appropriate target Looker instance
  - Change the `dashboardId` value to one the embed user should have access to
- `npm run dev` to start frontend server
- Navigate to _localhost:[port of frontend server]_ and click the "Load Dashboard" button to load the embedded dashboard
