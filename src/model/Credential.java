package model;

/** One row of the "Credentials" table. */
public class Credential {

    private final String credentialType;
    private final String issueDate;
    private final String status;

    public Credential(String credentialType, String issueDate, String status) {
        this.credentialType = credentialType;
        this.issueDate = issueDate;
        this.status = status;
    }

    public String getCredentialType() { return credentialType; }
    public String getIssueDate() { return issueDate; }
    public String getStatus() { return status; }
}
