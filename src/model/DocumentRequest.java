package model;

/** One row of the "Document Requests" table. */
public class DocumentRequest {

    private final String documentType;
    private final String purpose;
    private final String status;
    private final String requestedAt;

    public DocumentRequest(String documentType, String purpose, String status, String requestedAt) {
        this.documentType = documentType;
        this.purpose = purpose;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public String getDocumentType() { return documentType; }
    public String getPurpose() { return purpose; }
    public String getStatus() { return status; }
    public String getRequestedAt() { return requestedAt; }
}
