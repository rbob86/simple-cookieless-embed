// For capturing the user data you want in the cookieless session.
public class EmbedUser {
    private String externalUserId; // "embed-user-1"
    private String firstName; // "Johnny"
    private String lastName; // "Embed"
    private Integer sessionLength; // 3600
    private Boolean forceLogoutLogin; // true
    private String externalGroupId; // "embed-group-1"
    private List<Long> groupIds; // []
    private List<String> permissions; // [ "access_data", "see_user_dashboards", "explore", ... ]
    private List<String> models; // ["Runner", "thelook", "geo-test"]
    private Map<String, String> userAttributes; // e.g. { "locale": "en_US" }

    public EmbedUser(String externalUserId,
            String firstName,
            String lastName,
            Integer sessionLength,
            Boolean forceLogoutLogin,
            String externalGroupId,
            List<Long> groupIds,
            List<String> permissions,
            List<String> models,
            Map<String, String> userAttributes) {
        this.externalUserId = externalUserId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sessionLength = sessionLength;
        this.forceLogoutLogin = forceLogoutLogin;
        this.externalGroupId = externalGroupId;
        this.groupIds = groupIds;
        this.permissions = permissions;
        this.models = models;
        this.userAttributes = userAttributes;
    }
}