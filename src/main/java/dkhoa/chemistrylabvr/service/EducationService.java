package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.dto.request.education.EducationDtos.*;
import dkhoa.chemistrylabvr.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EducationService {
    private final NamedParameterJdbcTemplate db;
    private final ObjectMapper mapper;

    public List<Map<String, Object>> courses(UUID user, boolean admin) {
        String sql = admin
                ? "SELECT c.* FROM public.\"Course\" c WHERE c.status <> 'ARCHIVED' ORDER BY c.grade_level,c.title"
                : "SELECT DISTINCT c.* FROM public.\"Course\" c LEFT JOIN public.\"Classroom\" cl ON cl.course_id=c.id LEFT JOIN public.\"Enrollment\" e ON e.classroom_id=cl.id AND e.student_id=:user AND e.status='ACTIVE' WHERE c.owner_id=:user OR c.status='PUBLISHED' OR (c.status='PUBLISHED' AND e.id IS NOT NULL) ORDER BY c.grade_level,c.title";
        return db.queryForList(sql, new MapSqlParameterSource("user", user));
    }

    @Transactional
    public Map<String, Object> saveCourse(Long id, CourseInput in, UUID user, boolean admin) {
        require(!blank(in.title()) && in.title().trim().length()<=180 && in.gradeLevel() != null && in.gradeLevel() >= 1 && in.gradeLevel() <= 12,
                "Tên học phần và khối lớp (1–12) là bắt buộc");
        require(value(in.subject(),"Hóa học").length()<=80,"Môn học tối đa 80 ký tự");
        String status = status(in.status());
        if (id == null) {
            Long newId = db.queryForObject("INSERT INTO public.\"Course\"(owner_id,title,subject,grade_level,description,status) VALUES (:owner,:title,:subject,:grade,:description,:status) RETURNING id",
                    params("owner", user, "title", in.title().trim(), "subject", value(in.subject(), "Hóa học"), "grade", in.gradeLevel(), "description", value(in.description(), ""), "status", status), Long.class);
            return course(newId);
        }
        Map<String, Object> old = course(id);
        requireOwner(old, user, admin);
        db.update("UPDATE public.\"Course\" SET title=:title,subject=:subject,grade_level=:grade,description=:description,status=:status,updated_at=CURRENT_TIMESTAMP WHERE id=:id",
                params("id", id, "title", in.title().trim(), "subject", value(in.subject(), "Hóa học"), "grade", in.gradeLevel(), "description", value(in.description(), ""), "status", status));
        return course(id);
    }

    public List<Map<String, Object>> units(Long courseId, UUID user, boolean admin) {
        Map<String, Object> c = course(courseId); requireOwnerOrEnrolledCourse(c, courseId, user, admin);
        boolean owner=admin||Objects.equals(String.valueOf(c.get("owner_id")),user.toString());
        return db.queryForList("SELECT * FROM public.\"CourseUnit\" WHERE course_id=:id AND status<>'ARCHIVED' AND (:owner OR status='PUBLISHED') ORDER BY display_order,id", params("id", courseId,"owner",owner));
    }

    @Transactional
    public Map<String, Object> saveUnit(Long courseId, Long id, UnitInput in, UUID user, boolean admin) {
        Map<String, Object> c = course(courseId); requireOwner(c, user, admin);
        require(!blank(in.title())&&in.title().trim().length()<=180, "Tiêu đề chủ đề là bắt buộc và tối đa 180 ký tự");
        require(value(in.summary(),"").length()<=500,"Tóm tắt tối đa 500 ký tự");
        int order = in.displayOrder() == null ? 1 : in.displayOrder(); require(order > 0, "Thứ tự phải lớn hơn 0");
        require(in.semester()==null||in.semester()==1||in.semester()==2,"Học kỳ chỉ nhận giá trị 1, 2 hoặc để trống");
        if (in.parentId() != null) {
            Map<String, Object> parent = unit(in.parentId());
            require(Objects.equals(number(parent.get("course_id")), courseId), "Chủ đề cha phải thuộc cùng học phần");
            require(!Objects.equals(in.parentId(), id), "Không thể chọn chính chủ đề làm chủ đề cha");
            if(id!=null){Integer cycle=db.queryForObject("WITH RECURSIVE lineage(id,parent_id) AS (SELECT id,parent_id FROM public.\"CourseUnit\" WHERE id=:parent UNION SELECT p.id,p.parent_id FROM public.\"CourseUnit\" p JOIN lineage l ON p.id=l.parent_id) SELECT count(*) FROM lineage WHERE id=:current",params("parent",in.parentId(),"current",id),Integer.class);require(cycle==null||cycle==0,"Không thể đặt chủ đề con làm chủ đề cha");}
        }
        if (id == null) {
            Long newId = db.queryForObject("INSERT INTO public.\"CourseUnit\"(course_id,parent_id,title,summary,content,display_order,semester,status) VALUES (:course,:parent,:title,:summary,:content,:ord,:semester,:status) RETURNING id",
                    params("course", courseId, "parent", in.parentId(), "title", in.title().trim(), "summary", value(in.summary(), ""), "content", value(in.content(), ""), "ord", order, "semester", in.semester(), "status", status(in.status())), Long.class);
            return unit(newId);
        }
        Map<String, Object> old = unit(id); require(Objects.equals(number(old.get("course_id")), courseId), "Chủ đề không thuộc học phần này");
        db.update("UPDATE public.\"CourseUnit\" SET parent_id=:parent,title=:title,summary=:summary,content=:content,display_order=:ord,semester=:semester,status=:status,updated_at=CURRENT_TIMESTAMP WHERE id=:id",
                params("id", id, "parent", in.parentId(), "title", in.title().trim(), "summary", value(in.summary(), ""), "content", value(in.content(), ""), "ord", order, "semester", in.semester(), "status", status(in.status())));
        return unit(id);
    }

    public List<Map<String, Object>> classrooms(UUID user, boolean admin) {
        String sql = admin
                ? "SELECT cl.*,c.title course_title,c.grade_level,u.\"fullName\" lecturer_name,(SELECT count(*) FROM public.\"Enrollment\" e WHERE e.classroom_id=cl.id AND e.status='ACTIVE') student_count FROM public.\"Classroom\" cl JOIN public.\"Course\" c ON c.id=cl.course_id JOIN public.\"User\" u ON u.id=cl.lecturer_id ORDER BY cl.created_at DESC"
                : "SELECT cl.*,c.title course_title,c.grade_level,u.\"fullName\" lecturer_name,(SELECT count(*) FROM public.\"Enrollment\" e WHERE e.classroom_id=cl.id AND e.status='ACTIVE') student_count FROM public.\"Classroom\" cl JOIN public.\"Course\" c ON c.id=cl.course_id JOIN public.\"User\" u ON u.id=cl.lecturer_id LEFT JOIN public.\"Enrollment\" e ON e.classroom_id=cl.id AND e.student_id=:user AND e.status='ACTIVE' WHERE cl.lecturer_id=:user OR e.id IS NOT NULL ORDER BY cl.created_at DESC";
        return db.queryForList(sql, params("user", user));
    }

    @Transactional
    public Map<String, Object> createClassroom(ClassroomInput in, UUID user, boolean admin) {
        Map<String, Object> c = course(in.courseId()); require(admin||"PUBLISHED".equals(c.get("status"))||Objects.equals(String.valueOf(c.get("owner_id")),user.toString()),"Bạn không có quyền dùng học phần này"); require("PUBLISHED".equals(c.get("status")),"Chỉ tạo lớp từ học phần đã xuất bản");
        require(!blank(in.name()) && in.name().trim().length()<=120 && !blank(in.academicYear()) && in.academicYear().trim().length()<=20, "Tên lớp hoặc năm học không hợp lệ");
        String code;
        do { code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT); }
        while (!db.queryForList("SELECT id FROM public.\"Classroom\" WHERE join_code=:code", params("code", code)).isEmpty());
        Long id = db.queryForObject("INSERT INTO public.\"Classroom\"(course_id,lecturer_id,name,academic_year,join_code) VALUES (:course,:lecturer,:name,:year,:code) RETURNING id",
                params("course", in.courseId(), "lecturer", user, "name", in.name().trim(), "year", in.academicYear().trim(), "code", code), Long.class);
        return classroom(id);
    }

    @Transactional
    public Map<String,Object> updateClassroom(Long id,ClassroomInput in,UUID user,boolean admin){
        Map<String,Object> old=classroom(id);requireOwner(old,user,admin);
        require(!blank(in.name())&&in.name().trim().length()<=120&&!blank(in.academicYear())&&in.academicYear().trim().length()<=20,"Tên lớp hoặc năm học không hợp lệ");
        Long courseId=number(old.get("course_id"));
        if(in.courseId()!=null&&!Objects.equals(in.courseId(),courseId)){
            require(db.queryForList("SELECT id FROM public.\"Enrollment\" WHERE classroom_id=:id",params("id",id)).isEmpty()&&db.queryForList("SELECT id FROM public.\"ClassAssignment\" WHERE classroom_id=:id",params("id",id)).isEmpty(),"Không thể đổi học phần sau khi đã có học viên hoặc bài tập trong lớp");
            Map<String,Object> next=course(in.courseId());require("PUBLISHED".equals(next.get("status")),"Chỉ chuyển lớp sang học phần đã xuất bản");courseId=in.courseId();
        }
        db.update("UPDATE public.\"Classroom\" SET course_id=:course,name=:name,academic_year=:year,updated_at=CURRENT_TIMESTAMP WHERE id=:id",params("id",id,"course",courseId,"name",in.name().trim(),"year",in.academicYear().trim()));
        return classroom(id);
    }

    @Transactional public Map<String,Object> setClassroomActive(Long id,boolean active,UUID user,boolean admin){Map<String,Object> cl=classroom(id);requireOwner(cl,user,admin);db.update("UPDATE public.\"Classroom\" SET is_active=:active,updated_at=CURRENT_TIMESTAMP WHERE id=:id",params("id",id,"active",active));return classroom(id);}

    @Transactional public void removeStudent(Long classroomId,UUID studentId,UUID user,boolean admin){Map<String,Object> cl=classroom(classroomId);requireOwner(cl,user,admin);int changed=db.update("UPDATE public.\"Enrollment\" SET status='REMOVED' WHERE classroom_id=:class AND student_id=:student AND status='ACTIVE'",params("class",classroomId,"student",studentId));require(changed>0,"Học viên chưa tham gia lớp này");}

    @Transactional
    public Map<String, Object> joinClass(String code, UUID user) {
        require(!blank(code), "Hãy nhập mã lớp");
        List<Map<String, Object>> rows = db.queryForList("SELECT id,is_active FROM public.\"Classroom\" WHERE upper(join_code)=upper(:code)", params("code", code.trim()));
        require(!rows.isEmpty() && Boolean.TRUE.equals(rows.getFirst().get("is_active")), "Mã lớp không tồn tại hoặc lớp đã đóng");
        Long classroomId = number(rows.getFirst().get("id"));
        require(db.queryForList("SELECT id FROM public.\"Enrollment\" WHERE classroom_id=:class AND student_id=:student AND status='ACTIVE'", params("class", classroomId, "student", user)).isEmpty(), "Bạn đã tham gia lớp này");
        db.update("INSERT INTO public.\"Enrollment\"(classroom_id,student_id) VALUES (:class,:student) ON CONFLICT (classroom_id,student_id) DO UPDATE SET status='ACTIVE',joined_at=CURRENT_TIMESTAMP",
                params("class", classroomId, "student", user));
        return classroom(classroomId);
    }

    public List<Map<String, Object>> roster(Long classroomId, UUID user, boolean admin) {
        requireOwner(classroom(classroomId),user,admin);
        return db.queryForList("SELECT e.id enrollment_id,e.status,e.joined_at,u.id student_id,u.email,u.\"fullName\" full_name FROM public.\"Enrollment\" e JOIN public.\"User\" u ON u.id=e.student_id WHERE e.classroom_id=:id AND e.status='ACTIVE' ORDER BY u.\"fullName\"", params("id", classroomId));
    }

    @Transactional
    public Map<String, Object> saveAssignment(Long id, AssignmentInput in, UUID user, boolean admin) {
        require(!blank(in.title()) && !blank(in.type()), "Tiêu đề và loại bài tập là bắt buộc");
        String type = in.type().trim().toUpperCase(Locale.ROOT); require(Set.of("QUIZ", "PRACTICE").contains(type), "Loại bài phải là QUIZ hoặc PRACTICE");
        require(in.maxScore() != null && in.maxScore() > 0, "Điểm tối đa phải lớn hơn 0");
        require(in.courseUnitId()!=null,"Bài tập cần thuộc một chủ đề");
        Map<String,Object> selectedUnit=unit(in.courseUnitId()); require(courseOwned(number(selectedUnit.get("course_id")),user,admin)||"PUBLISHED".equals(selectedUnit.get("status")),"Bạn không có quyền dùng chủ đề này");
        String state = status(in.status());
        if("PUBLISHED".equals(state)) require("PUBLISHED".equals(selectedUnit.get("status")),"Hãy xuất bản chủ đề trước khi xuất bản bài tập");
        if (id != null) {
            Map<String,Object> old=assignment(id); requireOwner(old,user,admin);
            require("DRAFT".equals(old.get("status"))||"ARCHIVED".equals(state), "Chỉ được sửa bài tập ở trạng thái nháp; bài đã xuất bản chỉ được lưu trữ");
            if("ARCHIVED".equals(state)){db.update("UPDATE public.\"Assignment\" SET status='ARCHIVED',updated_at=CURRENT_TIMESTAMP WHERE id=:id",params("id",id));return assignment(id);}
        }
        if ("QUIZ".equals(type)) validateQuiz(in.questions(), in.maxScore());
        else {require(in.targetReactionId() != null, "Bài thực hành cần chọn phản ứng mục tiêu");require(!db.queryForList("SELECT id FROM public.\"Reaction\" WHERE id=:id AND is_deleted=FALSE",params("id",in.targetReactionId())).isEmpty(),"Phản ứng mục tiêu không tồn tại hoặc đã lưu trữ");}
        Long assignmentId;
        if (id == null) {
            assignmentId=db.queryForObject("INSERT INTO public.\"Assignment\"(category_id,course_unit_id,author_id,title,type,description,target_reaction_id,max_score,status,unity_scene_key,unity_config_json) VALUES (NULL,:unit,:author,:title,:type,:description,:reaction,:score,:status,:scene,CAST(:config AS jsonb)) RETURNING id",
                    params("unit",in.courseUnitId(),"author",user,"title",in.title().trim(),"type",type,"description",value(in.description(),""),"reaction",in.targetReactionId(),"score",in.maxScore(),"status",state,"scene",value(in.unitySceneKey(),"default"),"config",mapper.writeValueAsString(in.unityConfig()==null?Map.of():in.unityConfig())),Long.class);
        } else {
            assignmentId=id;
            db.update("UPDATE public.\"Assignment\" SET course_unit_id=:unit,title=:title,type=:type,description=:description,target_reaction_id=:reaction,max_score=:score,status=:status,unity_scene_key=:scene,unity_config_json=CAST(:config AS jsonb),updated_at=CURRENT_TIMESTAMP WHERE id=:id",
                    params("id",id,"unit",in.courseUnitId(),"title",in.title().trim(),"type",type,"description",value(in.description(),""),"reaction",in.targetReactionId(),"score",in.maxScore(),"status",state,"scene",value(in.unitySceneKey(),"default"),"config",mapper.writeValueAsString(in.unityConfig()==null?Map.of():in.unityConfig())));
            db.update("DELETE FROM public.\"AssignmentChemical\" WHERE assignment_id=:id",params("id",id));
            db.update("DELETE FROM public.\"QuizQuestion\" WHERE assignment_id=:id",params("id",id));
        }
        if ("PRACTICE".equals(type)) saveChemicals(assignmentId,in.allowedChemicals());
        if ("QUIZ".equals(type)) saveQuestions(assignmentId,in.questions());
        return assignment(assignmentId, true);
    }

    @Transactional
    public Map<String, Object> dispatch(Long assignmentId, DispatchInput in, UUID user, boolean admin) {
        Map<String,Object> assignment=assignment(assignmentId); requireOwner(assignment,user,admin);
        require("PUBLISHED".equals(assignment.get("status")), "Chỉ giao được bài đã xuất bản");
        Map<String,Object> classroom=classroom(in.classroomId()); requireOwner(classroom,user,admin); require(Boolean.TRUE.equals(classroom.get("is_active")),"Không thể giao bài vào lớp đã đóng");
        int attempts=in.maxAttempts()==null?1:in.maxAttempts(); require(attempts>=1&&attempts<=20,"Số lần làm phải từ 1 đến 20");
        require(in.opensAt()==null||in.dueAt()==null||!in.opensAt().isAfter(in.dueAt()),"Thời điểm mở bài phải trước hạn nộp");
        Long id=db.queryForObject("INSERT INTO public.\"ClassAssignment\"(classroom_id,assignment_id,opens_at,due_at,max_attempts) VALUES (:class,:assignment,:opens,:due,:attempts) ON CONFLICT (classroom_id,assignment_id) DO UPDATE SET opens_at=EXCLUDED.opens_at,due_at=EXCLUDED.due_at,max_attempts=EXCLUDED.max_attempts,status='ASSIGNED' RETURNING id",
                params("class",in.classroomId(),"assignment",assignmentId,"opens",in.opensAt(),"due",in.dueAt(),"attempts",attempts),Long.class);
        return classAssignment(id);
    }

    public List<Map<String,Object>> classAssignments(Long classroomId, UUID user, boolean admin) {
        classroomForManagerOrMember(classroomId,user,admin);
        return db.queryForList("SELECT ca.*,a.title,a.type,a.description,a.max_score,a.status assignment_status,cu.title unit_title,(SELECT count(*) FROM public.\"Submission\" s WHERE s.class_assignment_id=ca.id) attempt_count FROM public.\"ClassAssignment\" ca JOIN public.\"Assignment\" a ON a.id=ca.assignment_id LEFT JOIN public.\"CourseUnit\" cu ON cu.id=a.course_unit_id WHERE ca.classroom_id=:id AND ca.status<>'ARCHIVED' ORDER BY ca.assigned_at DESC",params("id",classroomId));
    }

    public Map<String,Object> assignment(Long id, boolean includeAnswers, UUID user, boolean admin) {
        Map<String,Object> row=assignment(id);
        if (includeAnswers) requireOwner(row,user,admin);
        return assignmentDetails(id,includeAnswers);
    }

    @Transactional
    public Map<String,Object> startAttempt(Long classAssignmentId, UUID user) {
        Map<String,Object> ca=classAssignment(classAssignmentId);
        db.queryForObject("SELECT id FROM public.\"ClassAssignment\" WHERE id=:id FOR UPDATE",params("id",classAssignmentId),Long.class);
        ca=classAssignment(classAssignmentId);
        requireMember(number(ca.get("classroom_id")),user,"Chỉ học viên trong lớp mới được làm bài");
        require(Boolean.TRUE.equals(classroom(number(ca.get("classroom_id"))).get("is_active")),"Lớp học đã đóng");
        require("ASSIGNED".equals(ca.get("status")),"Bài này đã đóng");
        Instant now=Instant.now(); Instant opens=instant(ca.get("opens_at")); Instant due=instant(ca.get("due_at"));
        require(opens==null||!now.isBefore(opens),"Bài tập chưa mở"); require(due==null||!now.isAfter(due),"Đã quá hạn nộp bài");
        Long assignmentId=number(ca.get("assignment_id"));
        require("PUBLISHED".equals(assignment(assignmentId).get("status")),"Bài tập không còn được mở");
        List<Map<String,Object>> inProgress=db.queryForList("SELECT * FROM public.\"Submission\" WHERE class_assignment_id=:ca AND user_id=:user AND status='IN_PROGRESS' ORDER BY attempt_number DESC LIMIT 1",params("ca",classAssignmentId,"user",user));
        if(!inProgress.isEmpty()) {
            Map<String,Object> result=new LinkedHashMap<>();
            result.put("attempt",inProgress.getFirst());
            result.put("assignment",assignmentDetails(assignmentId,false));
            return result;
        }
        Integer used=db.queryForObject("SELECT count(*) FROM public.\"Submission\" WHERE class_assignment_id=:ca AND user_id=:user AND status<>'CANCELLED'",params("ca",classAssignmentId,"user",user),Integer.class);
        require(used < ((Number)ca.get("max_attempts")).intValue(),"Bạn đã dùng hết số lần làm bài");
        int next=used+1;
        Long id=db.queryForObject("INSERT INTO public.\"Submission\"(user_id,assignment_id,class_assignment_id,score,execution_time_seconds,action_logs_json,answers_json,attempt_number,status,submitted_at) VALUES (:user,:assignment,:ca,NULL,NULL,'[]','[]'::jsonb,:attempt,'IN_PROGRESS',NULL) RETURNING id",
                params("user",user,"assignment",assignmentId,"ca",classAssignmentId,"attempt",next),Long.class);
        Map<String,Object> result=new LinkedHashMap<>(); result.put("attempt",submission(id));
        result.put("assignment",assignmentDetails(assignmentId,false));
        return result;
    }

    @Transactional
    public Map<String,Object> submitQuiz(Long attemptId, QuizSubmitInput input, UUID user) {
        submission(attemptId);
        db.queryForObject("SELECT id FROM public.\"Submission\" WHERE id=:id FOR UPDATE",params("id",attemptId),Long.class);
        Map<String,Object> sub=submission(attemptId); require(Objects.equals(String.valueOf(sub.get("user_id")),user.toString()),"Đây không phải bài làm của bạn");
        require("IN_PROGRESS".equals(sub.get("status")),"Bài làm không ở trạng thái đang làm");
        Map<String,Object> a=assignment(number(sub.get("assignment_id"))); require("QUIZ".equals(a.get("type")),"Đây không phải bài quiz");
        List<AnswerInput> answers=input==null||input.answers()==null?List.of():input.answers();
        Set<Long> seen=new HashSet<>(); BigDecimal earned=BigDecimal.ZERO;
        List<Map<String,Object>> saved=new ArrayList<>();
        for(AnswerInput answer:answers){
            require(answer!=null&&answer.questionId()!=null&&answer.optionId()!=null,"Câu trả lời không hợp lệ");
            require(seen.add(answer.questionId()),"Không gửi câu trả lời trùng cho một câu hỏi");
            List<Map<String,Object>> match=db.queryForList("SELECT q.points,o.is_correct FROM public.\"QuizQuestion\" q JOIN public.\"QuizOption\" o ON o.question_id=q.id WHERE q.id=:question AND q.assignment_id=:assignment AND o.id=:option",params("question",answer.questionId(),"assignment",sub.get("assignment_id"),"option",answer.optionId()));
            require(!match.isEmpty(),"Câu hỏi hoặc lựa chọn không thuộc bài quiz này");
            if(Boolean.TRUE.equals(match.getFirst().get("is_correct"))) earned=earned.add((BigDecimal)match.getFirst().get("points"));
            saved.add(Map.of("questionId",answer.questionId(),"optionId",answer.optionId()));
        }
        Integer questionCount=db.queryForObject("SELECT count(*) FROM public.\"QuizQuestion\" WHERE assignment_id=:id",params("id",sub.get("assignment_id")),Integer.class);
        require(questionCount!=null&&seen.size()==questionCount,"Cần trả lời tất cả câu hỏi trước khi nộp");
        BigDecimal total=db.queryForObject("SELECT COALESCE(sum(points),0) FROM public.\"QuizQuestion\" WHERE assignment_id=:id",params("id",sub.get("assignment_id")),BigDecimal.class);
        require(total!=null&&total.signum()>0,"Quiz chưa có câu hỏi để chấm");
        BigDecimal score=earned.multiply(BigDecimal.valueOf(((Number)a.get("max_score")).longValue())).divide(total,2,RoundingMode.HALF_UP);
        db.update("UPDATE public.\"Submission\" SET answers_json=CAST(:answers AS jsonb),score=:score,status='GRADED',submitted_at=CURRENT_TIMESTAMP WHERE id=:id",params("id",attemptId,"answers",mapper.writeValueAsString(saved),"score",score));
        return submission(attemptId);
    }

    @Transactional
    public Map<String,Object> submitPractice(Long attemptId, PracticeSubmitInput input, UUID user) {
        submission(attemptId);
        db.queryForObject("SELECT id FROM public.\"Submission\" WHERE id=:id FOR UPDATE",params("id",attemptId),Long.class);
        Map<String,Object> sub=submission(attemptId); require(Objects.equals(String.valueOf(sub.get("user_id")),user.toString()),"Đây không phải bài làm của bạn");
        require("IN_PROGRESS".equals(sub.get("status")),"Bài làm không ở trạng thái đang làm");
        Map<String,Object> a=assignment(number(sub.get("assignment_id"))); require("PRACTICE".equals(a.get("type")),"Đây không phải bài thực hành");
        int seconds=input==null||input.executionTimeSeconds()==null?0:input.executionTimeSeconds(); require(seconds>=0,"Thời gian thực hành không hợp lệ");
        String logs=mapper.writeValueAsString(input==null||input.actionLogs()==null?List.of():input.actionLogs());
        db.update("UPDATE public.\"Submission\" SET execution_time_seconds=:seconds,action_logs_json=:logs,status='SUBMITTED',submitted_at=CURRENT_TIMESTAMP WHERE id=:id",params("id",attemptId,"seconds",seconds,"logs",logs));
        return submission(attemptId);
    }

    public List<Map<String,Object>> attempts(Long classAssignmentId, UUID user, boolean admin) {
        Map<String,Object> ca=classAssignment(classAssignmentId); Map<String,Object> cl=classroom(number(ca.get("classroom_id"))); requireOwner(cl,user,admin);
        return db.queryForList("SELECT s.id,s.user_id,u.\"fullName\" student_name,s.attempt_number,s.status,s.score,s.execution_time_seconds,s.feedback,s.started_at,s.submitted_at FROM public.\"Submission\" s JOIN public.\"User\" u ON u.id=s.user_id WHERE s.class_assignment_id=:id ORDER BY s.submitted_at DESC NULLS LAST",params("id",classAssignmentId));
    }

    @Transactional
    public Map<String,Object> grade(Long attemptId, GradeInput in, UUID user, boolean admin) {
        Map<String,Object> sub=submission(attemptId); Map<String,Object> cl=classroom(number(sub.get("classroom_id"))); requireOwner(cl,user,admin);
        Map<String,Object> a=assignment(number(sub.get("assignment_id")));
        require(in.score()!=null&&in.score().signum()>=0&&in.score().compareTo(BigDecimal.valueOf(((Number)a.get("max_score")).longValue()))<=0,"Điểm phải nằm trong khoảng 0 đến điểm tối đa");
        db.update("UPDATE public.\"Submission\" SET score=:score,feedback=:feedback,status='GRADED' WHERE id=:id",params("id",attemptId,"score",in.score(),"feedback",value(in.feedback(),"")));
        return submission(attemptId);
    }

    public List<Map<String,Object>> myAssignments(UUID user) {
        return db.queryForList("SELECT ca.id class_assignment_id,ca.opens_at,ca.due_at,ca.max_attempts,ca.status dispatch_status,a.id assignment_id,a.title,a.type,a.description,a.max_score,cl.name classroom_name,cu.title unit_title,(SELECT count(*) FROM public.\"Submission\" s WHERE s.class_assignment_id=ca.id AND s.user_id=:user) attempts FROM public.\"ClassAssignment\" ca JOIN public.\"Classroom\" cl ON cl.id=ca.classroom_id JOIN public.\"Enrollment\" e ON e.classroom_id=cl.id AND e.student_id=:user AND e.status='ACTIVE' JOIN public.\"Assignment\" a ON a.id=ca.assignment_id LEFT JOIN public.\"CourseUnit\" cu ON cu.id=a.course_unit_id WHERE ca.status='ASSIGNED' AND cl.is_active=TRUE AND a.status='PUBLISHED' ORDER BY ca.due_at NULLS LAST,ca.assigned_at DESC",params("user",user));
    }

    public List<Map<String,Object>> authoredAssignments(UUID user,boolean admin){
        String sql=admin?"SELECT a.*,cu.title unit_title,c.title course_title FROM public.\"Assignment\" a LEFT JOIN public.\"CourseUnit\" cu ON cu.id=a.course_unit_id LEFT JOIN public.\"Course\" c ON c.id=cu.course_id WHERE a.status<>'ARCHIVED' ORDER BY a.updated_at DESC":"SELECT a.*,cu.title unit_title,c.title course_title FROM public.\"Assignment\" a LEFT JOIN public.\"CourseUnit\" cu ON cu.id=a.course_unit_id LEFT JOIN public.\"Course\" c ON c.id=cu.course_id WHERE a.author_id=:user AND a.status<>'ARCHIVED' ORDER BY a.updated_at DESC";
        return db.queryForList(sql,params("user",user));
    }

    @Transactional public void archiveAssignment(Long id,UUID user,boolean admin){Map<String,Object> a=assignment(id);requireOwner(a,user,admin);db.update("UPDATE public.\"Assignment\" SET status='ARCHIVED',updated_at=CURRENT_TIMESTAMP WHERE id=:id",params("id",id));db.update("UPDATE public.\"ClassAssignment\" SET status='ARCHIVED' WHERE assignment_id=:id",params("id",id));}

    public Map<String,Object> attemptDetails(Long attemptId,UUID user,boolean admin){
        Map<String,Object> sub=submission(attemptId); Map<String,Object> ca=classAssignment(number(sub.get("class_assignment_id"))); Map<String,Object> cl=classroom(number(ca.get("classroom_id")));
        boolean teacher=admin||Objects.equals(String.valueOf(cl.get("lecturer_id")),user.toString());
        require(teacher||Objects.equals(String.valueOf(sub.get("user_id")),user.toString()),"Bạn không có quyền xem bài làm này");
        sub.put("assignment",assignmentDetails(number(sub.get("assignment_id")),teacher));
        if(teacher){sub.put("answers",parseJson(sub.get("answers_json")));sub.put("actionLogs",parseJson(sub.get("action_logs_json")));}
        return sub;
    }

    public Map<String,Object> getAssignment(Long id,UUID user,boolean admin){
        Map<String,Object> a=assignment(id);
        boolean author=admin||Objects.equals(String.valueOf(a.get("author_id")),user.toString());
        require(author,"Học viên chỉ nhận nội dung quiz khi bắt đầu lượt làm bài");
        return assignmentDetails(id,author);
    }

    private Map<String,Object> assignmentDetails(Long id,boolean includeAnswers){
        Map<String,Object> row=assignment(id);
        row.put("unity_config_json",parseJson(row.get("unity_config_json")));
        List<Map<String,Object>> chemicals=db.queryForList("SELECT ac.chemical_id,ac.allowed_quantity,c.name,c.formula,c.state,c.unity_asset_id FROM public.\"AssignmentChemical\" ac JOIN public.\"Chemical\" c ON c.id=ac.chemical_id WHERE ac.assignment_id=:id ORDER BY c.name",params("id",id));
        List<Map<String,Object>> questions=db.queryForList("SELECT id,prompt,points,display_order FROM public.\"QuizQuestion\" WHERE assignment_id=:id ORDER BY display_order",params("id",id));
        for(Map<String,Object> q:questions){String sql="SELECT id,content,display_order"+(includeAnswers?",is_correct":"")+" FROM public.\"QuizOption\" WHERE question_id=:id ORDER BY display_order";q.put("options",db.queryForList(sql,params("id",q.get("id"))));}
        row.put("allowedChemicals",chemicals);row.put("questions",questions);return row;
    }

    private Object parseJson(Object value){
        if(value==null)return Map.of();
        try{return mapper.readValue(value.toString(),Object.class);}catch(Exception ex){return value.toString();}
    }

    private void saveChemicals(Long assignmentId,List<ChemicalLimit> chemicals){
        if(chemicals==null)return; Set<Long> ids=new HashSet<>();
        for(ChemicalLimit c:chemicals){ require(c!=null&&c.chemicalId()!=null&&c.allowedQuantity()!=null&&c.allowedQuantity().signum()>0,"Hóa chất thực hành cần số lượng dương"); require(ids.add(c.chemicalId()),"Không thêm trùng hóa chất trong bài");
            require(!db.queryForList("SELECT id FROM public.\"Chemical\" WHERE id=:id AND is_deleted=FALSE",params("id",c.chemicalId())).isEmpty(),"Hóa chất không tồn tại hoặc đã lưu trữ");
            db.update("INSERT INTO public.\"AssignmentChemical\"(assignment_id,chemical_id,allowed_quantity) VALUES (:assignment,:chemical,:quantity)",params("assignment",assignmentId,"chemical",c.chemicalId(),"quantity",c.allowedQuantity())); }
    }
    private void validateQuiz(List<QuizQuestionInput> questions,int maxScore){ require(questions!=null&&!questions.isEmpty(),"Quiz phải có câu hỏi"); BigDecimal sum=BigDecimal.ZERO;
        for(QuizQuestionInput q:questions){require(q!=null&&!blank(q.prompt()),"Nội dung câu hỏi là bắt buộc"); require(q.points()!=null&&q.points().signum()>0,"Điểm câu hỏi phải lớn hơn 0"); require(q.options()!=null&&q.options().size()>=2,"Mỗi câu hỏi cần ít nhất hai lựa chọn"); require(q.options().stream().filter(Objects::nonNull).filter(QuizOptionInput::correct).count()==1,"Mỗi câu hỏi phải có đúng một đáp án đúng"); require(q.options().stream().allMatch(o->o!=null&&!blank(o.content())),"Lựa chọn không được để trống"); sum=sum.add(q.points()); }
        require(sum.signum()>0,"Tổng điểm câu hỏi phải lớn hơn 0");
    }
    private void saveQuestions(Long assignmentId,List<QuizQuestionInput> questions){if(questions==null)return;int order=1;for(QuizQuestionInput q:questions){Long id=db.queryForObject("INSERT INTO public.\"QuizQuestion\"(assignment_id,prompt,points,display_order) VALUES (:assignment,:prompt,:points,:ord) RETURNING id",params("assignment",assignmentId,"prompt",q.prompt().trim(),"points",q.points(),"ord",order++),Long.class);int n=1;for(QuizOptionInput o:q.options())db.update("INSERT INTO public.\"QuizOption\"(question_id,content,is_correct,display_order) VALUES (:question,:content,:correct,:ord)",params("question",id,"content",o.content().trim(),"correct",o.correct(),"ord",n++));}}

    private Map<String,Object> course(Long id){return one("SELECT * FROM public.\"Course\" WHERE id=:id",id,"Không tìm thấy học phần");}
    private Map<String,Object> unit(Long id){return one("SELECT * FROM public.\"CourseUnit\" WHERE id=:id",id,"Không tìm thấy chủ đề");}
    private Map<String,Object> classroom(Long id){return one("SELECT * FROM public.\"Classroom\" WHERE id=:id",id,"Không tìm thấy lớp học");}
    private Map<String,Object> assignment(Long id){List<Map<String,Object>> rows=db.queryForList("SELECT a.*,cu.course_id,cu.title unit_title,c.title course_title FROM public.\"Assignment\" a LEFT JOIN public.\"CourseUnit\" cu ON cu.id=a.course_unit_id LEFT JOIN public.\"Course\" c ON c.id=cu.course_id WHERE a.id=:id",params("id",id));if(rows.isEmpty())throw new AppException("Không tìm thấy bài tập",HttpStatus.NOT_FOUND);return rows.getFirst();}
    private Map<String,Object> assignment(Long id,boolean include){return assignmentDetails(id,include);}
    private Map<String,Object> classAssignment(Long id){return one("SELECT * FROM public.\"ClassAssignment\" WHERE id=:id",id,"Không tìm thấy bài tập được giao");}
    private Map<String,Object> submission(Long id){return one("SELECT * FROM public.\"Submission\" WHERE id=:id",id,"Không tìm thấy lần làm bài");}
    private Map<String,Object> one(String sql,Long id,String message){List<Map<String,Object>> rows=db.queryForList(sql,params("id",id));if(rows.isEmpty())throw new AppException(message,HttpStatus.NOT_FOUND);return rows.getFirst();}
    private boolean courseOwned(Long id,UUID user,boolean admin){Map<String,Object> c=course(id);return admin||Objects.equals(String.valueOf(c.get("owner_id")),user.toString());}
    private void requireOwner(Map<String,Object> row,UUID user,boolean admin){if(admin)return;Object owner=row.get("owner_id");if(owner==null)owner=row.get("lecturer_id");if(owner==null)owner=row.get("author_id");require(Objects.equals(String.valueOf(owner),user.toString()),"Bạn không có quyền quản lý nội dung này");}
    private void requireOwnerOrEnrolledCourse(Map<String,Object> c,Long courseId,UUID user,boolean admin){if(admin||Objects.equals(String.valueOf(c.get("owner_id")),user.toString()))return;require(!db.queryForList("SELECT e.id FROM public.\"Enrollment\" e JOIN public.\"Classroom\" cl ON cl.id=e.classroom_id WHERE cl.course_id=:course AND e.student_id=:user AND e.status='ACTIVE'",params("course",courseId,"user",user)).isEmpty(),"Bạn không có quyền xem học phần này");}
    private void requireMember(Long classId,UUID user,String message){require(!db.queryForList("SELECT id FROM public.\"Enrollment\" WHERE classroom_id=:class AND student_id=:user AND status='ACTIVE'",params("class",classId,"user",user)).isEmpty(),message);}
    private void classroomForManagerOrMember(Long id,UUID user,boolean admin){Map<String,Object> c=classroom(id);if(admin||Objects.equals(String.valueOf(c.get("lecturer_id")),user.toString()))return;requireMember(id,user,"Bạn không thuộc lớp này");}
    private static String status(String s){String v=blank(s)?"DRAFT":s.trim().toUpperCase(Locale.ROOT);require(Set.of("DRAFT","PUBLISHED","ARCHIVED").contains(v),"Trạng thái không hợp lệ");return v;}
    private static boolean blank(String s){return s==null||s.isBlank();}
    private static String value(String v,String fallback){return v==null?fallback:v.trim();}
    private static Long number(Object n){return n==null?null:((Number)n).longValue();}
    private static Instant instant(Object d){if(d==null)return null;if(d instanceof java.sql.Timestamp t)return t.toInstant();if(d instanceof Instant i)return i;return Instant.parse(d.toString());}
    private static void require(boolean ok,String message){if(!ok)throw new AppException(message,HttpStatus.BAD_REQUEST);}
    private static MapSqlParameterSource params(Object... pairs){MapSqlParameterSource p=new MapSqlParameterSource();for(int i=0;i<pairs.length;i+=2)p.addValue((String)pairs[i],pairs[i+1]);return p;}
}


