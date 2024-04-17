import {
  DashboardEvent,
  DashboardTileEvent,
  DashboardTileExploreEvent,
  DrillMenuEvent,
  DrillModalExploreEvent,
  EnvClientDialogEvent,
  ExploreEvent,
  LookEvent,
  LookerEmbedSDK,
  PageChangedEvent,
  PagePropertiesChangedEvent,
  SessionStatus,
  SessionTokenRequest,
} from '@looker/embed-sdk'

function App() {
  const loadDashboard = () => {
    // Initialize (authentication) by cookieless login
    // First URL is the Looker instance to be used, second and third urls are
    // for local server API (back-end).
    LookerEmbedSDK.initCookieless(
      'gcpm244.cloud.looker.com',
      'http://localhost:3000/acquire-embed-session',
      'http://localhost:3000/generate-embed-tokens',
    )

    // Once authentication completes, we create a dashboard with any ID from the
    // Looker instance being used. The dashboard is attached to the "dashboard"
    // div and it is built
    const dashboardId = 27
    LookerEmbedSDK.createDashboardWithId(dashboardId)
      .appendTo('#dashboard')
      .on('dashboard:loaded', (event: DashboardEvent) => {
        console.log(`Dashboard loaded: ${event}`)
      })
      // .on('dashboard:run:start', (event: DashboardEvent) => {})
      // .on('dashboard:run:complete', (event: DashboardEvent) => {})
      // .on('dashboard:run:download', (event: DashboardEvent) => {})
      // .on('dashboard:edit:start', (event: DashboardEvent) => {})
      // .on('dashboard:edit:cancel', (event: DashboardEvent) => {})
      // .on('dashboard:save:complete', (event: DashboardEvent) => {})
      // .on('dashboard:delete:complete', (event: DashboardEvent) => {})
      // .on('dashboard:tile:start', (event: DashboardTileEvent) => {})
      // .on('dashboard:tile:complete', (event: DashboardTileEvent) => {})
      // .on('dashboard:tile:download', (event: DashboardTileEvent) => {})
      // .on('dashboard:tile:explore', (event: DashboardTileExploreEvent) => ({ cancel: true }))
      // .on('dashboard:tile:view', (event: DashboardTileExploreEvent) => ({ cancel: true }))
      // .on('dashboard:filters:changed', (event: DashboardEvent) => {})
      // .on('look:ready', (event: LookEvent) => {})
      // .on('look:run:start', (event: LookEvent) => {})
      // .on('look:run:complete', (event: LookEvent) => {})
      // .on('look:save:complete', (event: LookEvent) => {})
      // .on('look:delete:complete', (event: LookEvent) => {})
      // .on('explore:ready', (event: ExploreEvent) => {})
      // .on('explore:run:start', (event: ExploreEvent) => {})
      // .on('explore:run:complete', (event: ExploreEvent) => {})
      // .on('explore:state:changed', (event: ExploreEvent) => {})
      // .on('drillmenu:click', (event: DrillMenuEvent) => ({ cancel: true }))
      // .on('drillmodal:download', (event: DrillModalExploreEvent) => {})
      // .on('drillmodal:explore', (event: DrillModalExploreEvent) => ({ cancel: true }))
      // .on('page:changed', (event: PageChangedEvent) => {})
      // .on('page:properties:changed', (event: PagePropertiesChangedEvent) => {})
      // .on('session:tokens', (event: SessionTokenRequest) => {})
      // .on('session:status', (event: SessionStatus) => {})
      // .on('env:client:dialog', (event: EnvClientDialogEvent) => {})
      .build()
      .connect()
      .then(() => {
        console.log('Done')
      })
      .catch((error: any) => {
        console.error(error)
      })
  }

  // The component returned is the div where the dashboard will be rendered and
  // a button with "Load Dashboard", that when clicked will call the above
  // function.
  return (
    <div className="App">
      <button className="btn" onClick={loadDashboard}>
        Load Dashboard
      </button>
      <div id="dashboard" className="embed-dashboard"></div>
    </div>
  )
}

export default App
