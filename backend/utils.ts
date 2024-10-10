import { LookerEmbedUser } from './types'

export const user: LookerEmbedUser = {
  external_user_id: 'embed-user-1',
  first_name: 'Johnny',
  last_name: 'Embed',
  session_length: 3600,
  force_logout_login: true,
  external_group_id: 'embed-group-1',
  group_ids: [],
  permissions: [
    'access_data',
    'see_looks',
    'see_user_dashboards',
    'explore',
    'save_content',
    'embed_browse_spaces',
    'download_without_limit',
    'create_alerts',
  ],
  models: ['Runner', 'thelook', 'geo-test'],
  user_attributes: { locale: 'en_US' },
}
