package dkhoa.chemistrylabvr.dto.request.education;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class EducationDtos {
    private EducationDtos() {}

    public record CourseInput(String title, String subject, Integer gradeLevel, String description, String status) {}
    public record UnitInput(Long parentId, String title, String summary, String content, Integer displayOrder, Integer semester, String status) {}
    public record ClassroomInput(Long courseId, String name, String academicYear) {}
    public record JoinInput(String joinCode) {}
    public record ChemicalLimit(Long chemicalId, BigDecimal allowedQuantity) {}
    public record QuizOptionInput(String content, boolean correct) {}
    public record QuizQuestionInput(String prompt, BigDecimal points, List<QuizOptionInput> options) {}
    public record AssignmentInput(Long courseUnitId, String title, String type, String description,
                                  Integer maxScore, Long targetReactionId, List<ChemicalLimit> allowedChemicals,
                                  List<QuizQuestionInput> questions, String status, String unitySceneKey,
                                  Map<String, Object> unityConfig) {}
    public record DispatchInput(Long classroomId, Instant opensAt, Instant dueAt, Integer maxAttempts) {}
    public record AnswerInput(Long questionId, Long optionId) {}
    public record QuizSubmitInput(List<AnswerInput> answers) {}
    public record PracticeSubmitInput(Integer executionTimeSeconds, List<Map<String, Object>> actionLogs) {}
    public record GradeInput(BigDecimal score, String feedback) {}
}
