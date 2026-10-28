package model;

/** One row of the "My Subjects" table. */
public class EnrolledSubject {

    private final String subjectCode;
    private final String subjectName;
    private final String units;
    private final String section;
    private final String schoolYear;
    private final String gradeLevel;
    private final String status;

    public EnrolledSubject(String subjectCode, String subjectName, String units, String section,
                           String schoolYear, String gradeLevel, String status) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.units = units;
        this.section = section;
        this.schoolYear = schoolYear;
        this.gradeLevel = gradeLevel;
        this.status = status;
    }

    public String getSubjectCode() { return subjectCode; }
    public String getSubjectName() { return subjectName; }
    public String getUnits() { return units; }
    public String getSection() { return section; }
    public String getSchoolYear() { return schoolYear; }
    public String getGradeLevel() { return gradeLevel; }
    public String getStatus() { return status; }
}
