package model;

/** One row of the "My Grades" table. */
public class Grade {

    private final String subjectCode;
    private final String subjectName;
    private final String schoolYear;
    private final String gradingPeriod;
    private final String grade;
    private final String remarks;

    public Grade(String subjectCode, String subjectName, String schoolYear,
                 String gradingPeriod, String grade, String remarks) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.schoolYear = schoolYear;
        this.gradingPeriod = gradingPeriod;
        this.grade = grade;
        this.remarks = remarks;
    }

    public String getSubjectCode() { return subjectCode; }
    public String getSubjectName() { return subjectName; }
    public String getSchoolYear() { return schoolYear; }
    public String getGradingPeriod() { return gradingPeriod; }
    public String getGrade() { return grade; }
    public String getRemarks() { return remarks; }
}
