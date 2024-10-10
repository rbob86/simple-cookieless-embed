export type LookerUserPermission =
  | 'access_data'
  | 'see_lookml_dashboards'
  | 'see_looks'
  | 'see_user_dashboards'
  | 'explore'
  | 'create_table_calculations'
  | 'create_custom_fields'
  | 'can_create_forecast'
  | 'save_content'
  | 'send_outgoing_webhook'
  | 'send_to_s3'
  | 'send_to_sftp'
  | 'schedule_look_emails'
  | 'schedule_external_look_emails'
  | 'send_to_integration'
  | 'create_alerts'
  | 'download_with_limit'
  | 'download_without_limit'
  | 'see_sql'
  | 'clear_cache_refresh'
  | 'see_drill_overlay'
  | 'embed_browse_spaces'
  | 'embed_save_shared_space'

export interface LookerEmbedUser {
  external_user_id: string
  first_name?: string
  last_name?: string
  session_length: number
  force_logout_login: boolean
  permissions: LookerUserPermission[]
  models: string[]
  group_ids?: string[]
  external_group_id?: string
  user_attributes?: { [key: string]: any }
  user_timezone?: string | null
  access_filters?: { [key: string]: any }
}
