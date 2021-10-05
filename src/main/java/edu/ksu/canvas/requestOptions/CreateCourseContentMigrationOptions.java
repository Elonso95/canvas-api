package edu.ksu.canvas.requestOptions;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class CreateCourseContentMigrationOptions extends CreateContentMigrationOptions<CreateCourseContentMigrationOptions> {

    private final String destinationCourseId;

    /**
     * Constructs object to hold API options for the creating a course content migration.
     *
     * @param destinationCourseId      The id of the destination course
     * @param migrationType            Course copy content
     */
    public CreateCourseContentMigrationOptions(String destinationCourseId, MigrationType migrationType) {
        super(migrationType);
        this.destinationCourseId = destinationCourseId;
    }

    /**
     * Constructs object to hold API options for the creating a course content migration.
     *
     * @param destinationCourseId      The id of the destination course
     * @param sourceCourseId           The id of the source course
     * @param migrationType            Course copy content
     * @param selectiveImport          Whether user wants to select specific items or to copy the whole course content
     * @param selectedData             Dynamic list of items that will be copied from the original course. See SelectiveData API for more info
     */
    public CreateCourseContentMigrationOptions(String destinationCourseId, String sourceCourseId, MigrationType migrationType, boolean selectiveImport, String... selectedData) {
        super(sourceCourseId, migrationType);
        this.selectiveImport(selectiveImport);
        this.destinationCourseId = destinationCourseId;
        for(String item : selectedData) {
            addSingleItem(item, "1");
        }
    }

    public String getDestinationCourseId() {
        return destinationCourseId;
    }
    
    public CreateCourseContentMigrationOptions selectiveImport(boolean selectiveImport) {
        addSingleItem("selective_import", Boolean.toString(selectiveImport));
        return this;
    }

    public CreateCourseContentMigrationOptions questionBank(Integer questionBankId) {
        addSingleItem("settings[question_bank_id]", questionBankId.toString());
        return this;
    }

    public CreateCourseContentMigrationOptions questionBankName(String questionBankName) {
        addSingleItem("settings[question_bank_name]", questionBankName);
        return this;
    }

    public CreateCourseContentMigrationOptions overwriteQuizzes(Boolean overwriteQuizzes) {
        addSingleItem("settings[overwrite_quizzes]", overwriteQuizzes.toString());
        return this;
    }

    public CreateCourseContentMigrationOptions insertIntoModule(Integer insertIntoModuleId) {
        addSingleItem("settings[insert_into_module_id]", insertIntoModuleId.toString());
        return this;
    }

    public CreateCourseContentMigrationOptions insertIntoModuleType(String insertIntoModuleType) {
        addSingleItem("settings[insert_into_module_type]", insertIntoModuleType);
        return this;
    }

    public CreateCourseContentMigrationOptions insertIntoModulePosition(Integer insertIntoModulePosition) {
        addSingleItem("settings[insert_into_module_position]", insertIntoModulePosition.toString());
        return this;
    }

    public CreateCourseContentMigrationOptions moveToAssignmentGroup(Integer moveToAssignmentGroup) {
        addSingleItem("settings[move_to_assignment_group_id]", moveToAssignmentGroup.toString());
        return this;
    }

    public CreateCourseContentMigrationOptions shiftDates(Boolean shiftDates) {
        addSingleItem("date_shift_options[shift_dates]", shiftDates.toString());
        return this;
    }

    public CreateCourseContentMigrationOptions removeDates(Boolean removeDates) {
        addSingleItem("date_shift_options[remove_dates]", removeDates.toString());
        return this;
    }

    public CreateCourseContentMigrationOptions oldStartDate(Date oldStartDate) {
        addSingleItem("date_shift_options[old_start_date]", oldStartDate.toString());
        return this;
    }

    public CreateCourseContentMigrationOptions oldEndDate(Date oldEndDate) {
        addSingleItem("date_shift_options[old_end_date]", oldEndDate.toString());
        return this;
    }

    public CreateCourseContentMigrationOptions newStartDate(Date newStartDate) {
        addSingleItem("date_shift_options[new_start_date]", newStartDate.toString());
        return this;
    }

    public CreateCourseContentMigrationOptions newEndDate(Date newEndDate) {
        addSingleItem("date_shift_options[new_end_date]", newEndDate.toString());
        return this;
    }

    public CreateCourseContentMigrationOptions daySubstitutions(Integer dayX, Integer newDay) {
        addSingleItem("date_shift_options[day_substitutions]["+dayX+"]", newDay.toString());
        return this;
    }

}