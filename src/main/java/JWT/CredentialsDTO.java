package JWT;

public class CredentialsDTO {
    private String username;
    private String password;
    private long groupId;

    public CredentialsDTO(String username, String password, long groupId) {
        this.username = username;
        this.password = password;
        this.groupId = groupId;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public long getGroupId() {
        return groupId;
    }
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

}
