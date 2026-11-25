package controller;

import dao.GradeDAO;
import dao.StudentDAO;
import dao.EnrollmentDAO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import model.Grade;
import model.Student;
import model.EnrolledSubject;

import util.UserSession;

public class GradesController {

    @FXML private Label studentNameLabel;
    @FXML private Label studentNumberLabel;
    @FXML private Label gradeSectionLabel;
    @FXML private Label schoolYearLabel;
    @FXML private Label generalAverageLabel;
    @FXML private Label overallRemarksLabel;

    @FXML private ComboBox<SchoolYearOption> schoolYearCombo;

    @FXML private TableView<ReportCardRow> gradesTable;

    @FXML private TableColumn<ReportCardRow, String> subjectColumn;
    @FXML private TableColumn<ReportCardRow, String> firstQuarterColumn;
    @FXML private TableColumn<ReportCardRow, String> secondQuarterColumn;
    @FXML private TableColumn<ReportCardRow, String> thirdQuarterColumn;
    @FXML private TableColumn<ReportCardRow, String> fourthQuarterColumn;
    @FXML private TableColumn<ReportCardRow, String> finalGradeColumn;
    @FXML private TableColumn<ReportCardRow, String> remarksColumn;

    private int studentId;

    private List<EnrolledSubject> enrollments = new ArrayList<>();

    private List<Grade> allGrades = new ArrayList<>();

    @FXML
    private void initialize() {

        gradesTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        gradesTable.setPlaceholder(
                new Label("No grades available for this school year.")
        );

        setupColumns();
        util.WrapCells.apply(subjectColumn);
        util.WrapCells.apply(firstQuarterColumn);
        util.WrapCells.apply(secondQuarterColumn);
        util.WrapCells.apply(thirdQuarterColumn);
        util.WrapCells.apply(fourthQuarterColumn);
        util.WrapCells.apply(finalGradeColumn);
        util.WrapCells.apply(remarksColumn);

        schoolYearCombo.valueProperty().addListener(
                (observable, oldValue, newValue) -> {

                    if (newValue != null) {
                        displayReportCard(newValue);
                    }
                }
        );

        loadStudentData();
    }

    private void setupColumns() {

        subjectColumn.setCellValueFactory(
                cell -> cell.getValue().subjectProperty()
        );

        firstQuarterColumn.setCellValueFactory(
                cell -> cell.getValue().q1Property()
        );

        secondQuarterColumn.setCellValueFactory(
                cell -> cell.getValue().q2Property()
        );

        thirdQuarterColumn.setCellValueFactory(
                cell -> cell.getValue().q3Property()
        );

        fourthQuarterColumn.setCellValueFactory(
                cell -> cell.getValue().q4Property()
        );

        finalGradeColumn.setCellValueFactory(
                cell -> cell.getValue().finalGradeProperty()
        );

        remarksColumn.setCellValueFactory(
                cell -> cell.getValue().remarksProperty()
        );
    }

    private void loadStudentData() {

        try {

            int userId = UserSession.getUserId();

            Student student =
                    new StudentDAO().findByUserId(userId);

            if (student == null) {

                gradesTable.setPlaceholder(
                        new Label("Student record not found.")
                );

                return;
            }

            studentId = student.getStudentId();

            studentNameLabel.setText(student.getFullName());

            studentNumberLabel.setText(
                    student.getStudentNo() == null
                    ? "-" : student.getStudentNo()
            );

            // Load all enrollment history
            enrollments =
                    new EnrollmentDAO().findByStudentId(studentId);

            // Load all grades from all school years
            allGrades =
                    new GradeDAO().findByStudentId(studentId);

            loadSchoolYearOptions();

        } catch (Exception e) {

            e.printStackTrace();

            gradesTable.setPlaceholder(
                    new Label("Unable to load student report card.")
            );
        }
    }

    private void loadSchoolYearOptions() {

        // Remove duplicate enrollment records.
        Map<String, SchoolYearOption> options =
                new LinkedHashMap<>();

        for (EnrolledSubject enrollment : enrollments) {

            String gradeLevel = enrollment.getGradeLevel();
            String schoolYear = enrollment.getSchoolYear();

            if (gradeLevel == null || schoolYear == null) {
                continue;
            }

            String key = gradeLevel + "|" + schoolYear;

            SchoolYearOption existing = options.get(key);

            boolean isActive =
                    "active".equalsIgnoreCase(enrollment.getStatus());

            if (existing == null) {

                options.put(
                        key,
                        new SchoolYearOption(
                                gradeLevel,
                                schoolYear,
                                isActive
                        )
                );

            } else if (isActive) {

                existing.setCurrent(true);
            }
        }

        List<SchoolYearOption> optionList =
                new ArrayList<>(options.values());

        schoolYearCombo.setItems(
                FXCollections.observableArrayList(optionList)
        );

        if (optionList.isEmpty()) {

            schoolYearCombo.setPromptText(
                    "No enrollment history found"
            );

            gradesTable.setPlaceholder(
                    new Label("No enrollment records found.")
            );

            return;
        }

        // Prefer the active enrollment.
        SchoolYearOption selected = null;

        for (SchoolYearOption option : optionList) {

            if (option.isCurrent()) {
                selected = option;
                break;
            }
        }

        // If there is no active enrollment, choose the latest record.
        if (selected == null) {
            selected = optionList.get(0);
        }

        schoolYearCombo.setValue(selected);
    }

    private void displayReportCard(SchoolYearOption selected) {

        if (selected == null) {
            return;
        }

        String selectedYear = selected.getSchoolYear();
        String selectedGrade = selected.getGradeLevel();

        schoolYearLabel.setText(selectedYear);

        // Find the section for the selected school year and grade.
        String section = "-";

        for (EnrolledSubject enrollment : enrollments) {

            if (selectedYear.equals(enrollment.getSchoolYear())
                    && selectedGrade.equals(enrollment.getGradeLevel())) {

                section = enrollment.getSection() == null
                        ? "-" : enrollment.getSection();

                break;
            }
        }

        gradeSectionLabel.setText(
                selectedGrade + " - " + section
        );

        // Create rows from enrolled subjects first.
        Map<String, ReportCardRow> subjectMap =
                new LinkedHashMap<>();

        for (EnrolledSubject enrollment : enrollments) {

            if (!selectedYear.equals(enrollment.getSchoolYear())
                    || !selectedGrade.equals(enrollment.getGradeLevel())) {
                continue;
            }

            String code = enrollment.getSubjectCode();

            subjectMap.putIfAbsent(
                    code,
                    new ReportCardRow(
                            code,
                            enrollment.getSubjectName()
                    )
            );
        }

        // Insert grades into their corresponding subjects.
        for (Grade grade : allGrades) {

            if (!selectedYear.equals(grade.getSchoolYear())) {
                continue;
            }

            ReportCardRow row =
                    subjectMap.get(grade.getSubjectCode());

            if (row == null) {
                continue;
            }

            switch (grade.getGradingPeriod()) {

                case "1st Quarter":
                    row.setQ1(grade.getGrade());
                    break;

                case "2nd Quarter":
                    row.setQ2(grade.getGrade());
                    break;

                case "3rd Quarter":
                    row.setQ3(grade.getGrade());
                    break;

                case "4th Quarter":
                    row.setQ4(grade.getGrade());
                    break;

                default:
                    break;
            }
        }

        List<ReportCardRow> rows =
                new ArrayList<>(subjectMap.values());

        double total = 0;
        int completedSubjects = 0;

        for (ReportCardRow row : rows) {

            row.calculateFinalGrade();

            if (row.hasCompleteGrades()) {

                total += Double.parseDouble(row.getFinalGrade());
                completedSubjects++;
            }
        }

        gradesTable.setItems(
                FXCollections.observableArrayList(rows)
        );

        // Only calculate average when every subject is complete.
        if (!rows.isEmpty()
                && completedSubjects == rows.size()) {

            double average = total / completedSubjects;

            generalAverageLabel.setText(
                    String.format("%.2f", average)
            );

            overallRemarksLabel.setText(
                    average >= 75 ? "PASSED" : "FAILED"
            );

        } else {

            generalAverageLabel.setText("-");

            overallRemarksLabel.setText(
                    "INCOMPLETE GRADES"
            );
        }
    }

    // Dropdown item
    public static class SchoolYearOption {

        private final String gradeLevel;
        private final String schoolYear;
        private boolean current;

        public SchoolYearOption(
                String gradeLevel,
                String schoolYear,
                boolean current) {

            this.gradeLevel = gradeLevel;
            this.schoolYear = schoolYear;
            this.current = current;
        }

        public String getGradeLevel() {
            return gradeLevel;
        }

        public String getSchoolYear() {
            return schoolYear;
        }

        public boolean isCurrent() {
            return current;
        }

        public void setCurrent(boolean current) {
            this.current = current;
        }

        @Override
        public String toString() {

            return gradeLevel + " - SY " + schoolYear
                    + (current ? " (Current)" : "");
        }
    }

    // Report card table row
    public static class ReportCardRow {

        private final SimpleStringProperty subject;

        private final SimpleStringProperty q1 =
                new SimpleStringProperty("-");

        private final SimpleStringProperty q2 =
                new SimpleStringProperty("-");

        private final SimpleStringProperty q3 =
                new SimpleStringProperty("-");

        private final SimpleStringProperty q4 =
                new SimpleStringProperty("-");

        private final SimpleStringProperty finalGrade =
                new SimpleStringProperty("-");

        private final SimpleStringProperty remarks =
                new SimpleStringProperty("Incomplete");

        public ReportCardRow(String code, String name) {
            subject = new SimpleStringProperty(name);
        }

        public void setQ1(String value) {
            q1.set(value);
        }

        public void setQ2(String value) {
            q2.set(value);
        }

        public void setQ3(String value) {
            q3.set(value);
        }

        public void setQ4(String value) {
            q4.set(value);
        }

        private boolean valid(String value) {

            if (value == null || value.equals("-")) {
                return false;
            }

            try {
                double grade = Double.parseDouble(value);

                return grade >= 0 && grade <= 100;

            } catch (NumberFormatException e) {
                return false;
            }
        }

        public boolean hasCompleteGrades() {

            return valid(q1.get())
                    && valid(q2.get())
                    && valid(q3.get())
                    && valid(q4.get());
        }

        public void calculateFinalGrade() {

            if (!hasCompleteGrades()) {

                finalGrade.set("-");
                remarks.set("Incomplete");

                return;
            }

            double average = (
                    Double.parseDouble(q1.get())
                    + Double.parseDouble(q2.get())
                    + Double.parseDouble(q3.get())
                    + Double.parseDouble(q4.get())
            ) / 4.0;

            int result = (int) Math.round(average);

            finalGrade.set(String.valueOf(result));

            remarks.set(
                    result >= 75 ? "Passed" : "Failed"
            );
        }

        public String getFinalGrade() {
            return finalGrade.get();
        }

        public SimpleStringProperty subjectProperty() {
            return subject;
        }

        public SimpleStringProperty q1Property() {
            return q1;
        }

        public SimpleStringProperty q2Property() {
            return q2;
        }

        public SimpleStringProperty q3Property() {
            return q3;
        }

        public SimpleStringProperty q4Property() {
            return q4;
        }

        public SimpleStringProperty finalGradeProperty() {
            return finalGrade;
        }

        public SimpleStringProperty remarksProperty() {
            return remarks;
        }
    }
}