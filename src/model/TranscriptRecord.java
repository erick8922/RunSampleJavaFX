package model;

/** One row of the "Transcript" table. */
public class TranscriptRecord {

    private final String schoolYear;
    private final String issueDate;
    private final String remarks;
    private final String status;

    public TranscriptRecord(String schoolYear, String issueDate, String remarks, String status) {
        this.schoolYear = schoolYear;
        this.issueDate = issueDate;
        this.remarks = remarks;
        this.status = status;
    }

    public String getSchoolYear() { return schoolYear; }
    public String getIssueDate() { return issueDate; }
    public String getRemarks() { return remarks; }
    public String getStatus() { return status; }
}
