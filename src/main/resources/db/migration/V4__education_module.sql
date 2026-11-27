CREATE TABLE public."Course" (
    id BIGSERIAL PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES public."User"(id) ON DELETE RESTRICT,
    title VARCHAR(180) NOT NULL,
    subject VARCHAR(80) NOT NULL DEFAULT 'Hóa học',
    grade_level SMALLINT NOT NULL CHECK (grade_level BETWEEN 1 AND 12),
    description TEXT NOT NULL DEFAULT '',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public."CourseUnit" (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES public."Course"(id) ON DELETE CASCADE,
    parent_id BIGINT REFERENCES public."CourseUnit"(id) ON DELETE SET NULL,
    title VARCHAR(180) NOT NULL,
    summary VARCHAR(500) NOT NULL DEFAULT '',
    content TEXT NOT NULL DEFAULT '',
    display_order INTEGER NOT NULL DEFAULT 1 CHECK (display_order > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_course_unit_course_order ON public."CourseUnit"(course_id, display_order);

CREATE TABLE public."Classroom" (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES public."Course"(id) ON DELETE RESTRICT,
    lecturer_id UUID NOT NULL REFERENCES public."User"(id) ON DELETE RESTRICT,
    name VARCHAR(120) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    term VARCHAR(40) NOT NULL DEFAULT '',
    join_code VARCHAR(16) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_classroom_lecturer ON public."Classroom"(lecturer_id, is_active);

CREATE TABLE public."Enrollment" (
    id BIGSERIAL PRIMARY KEY,
    classroom_id BIGINT NOT NULL REFERENCES public."Classroom"(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES public."User"(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'LEFT', 'REMOVED')),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (classroom_id, student_id)
);
CREATE INDEX ix_enrollment_student ON public."Enrollment"(student_id, status);

ALTER TABLE public."Assignment"
    ALTER COLUMN category_id DROP NOT NULL;

ALTER TABLE public."Assignment"
    ADD COLUMN course_unit_id BIGINT REFERENCES public."CourseUnit"(id) ON DELETE SET NULL,
    ADD COLUMN author_id UUID REFERENCES public."User"(id) ON DELETE SET NULL,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN unity_scene_key VARCHAR(120) NOT NULL DEFAULT 'default',
    ADD COLUMN unity_config_json JSONB NOT NULL DEFAULT '{}'::jsonb;

CREATE TABLE public."ClassAssignment" (
    id BIGSERIAL PRIMARY KEY,
    classroom_id BIGINT NOT NULL REFERENCES public."Classroom"(id) ON DELETE CASCADE,
    assignment_id BIGINT NOT NULL REFERENCES public."Assignment"(id) ON DELETE RESTRICT,
    opens_at TIMESTAMPTZ,
    due_at TIMESTAMPTZ,
    max_attempts SMALLINT NOT NULL DEFAULT 1 CHECK (max_attempts BETWEEN 1 AND 20),
    status VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED' CHECK (status IN ('ASSIGNED', 'CLOSED', 'ARCHIVED')),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (classroom_id, assignment_id),
    CHECK (opens_at IS NULL OR due_at IS NULL OR opens_at <= due_at)
);
CREATE INDEX ix_class_assignment_classroom ON public."ClassAssignment"(classroom_id, status);

CREATE TABLE public."QuizQuestion" (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL REFERENCES public."Assignment"(id) ON DELETE CASCADE,
    prompt TEXT NOT NULL,
    points NUMERIC(7,2) NOT NULL DEFAULT 1 CHECK (points > 0),
    display_order INTEGER NOT NULL DEFAULT 1 CHECK (display_order > 0),
    UNIQUE (assignment_id, display_order)
);

CREATE TABLE public."QuizOption" (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES public."QuizQuestion"(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 1 CHECK (display_order > 0),
    UNIQUE (question_id, display_order)
);

ALTER TABLE public."Submission"
    ADD COLUMN class_assignment_id BIGINT REFERENCES public."ClassAssignment"(id) ON DELETE SET NULL,
    ADD COLUMN attempt_number SMALLINT NOT NULL DEFAULT 1 CHECK (attempt_number > 0),
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED'
        CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'GRADED', 'CANCELLED')),
    ADD COLUMN started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN feedback TEXT NOT NULL DEFAULT '',
    ADD COLUMN answers_json JSONB NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE public."Submission"
    ALTER COLUMN score DROP NOT NULL,
    ALTER COLUMN execution_time_seconds DROP NOT NULL,
    ALTER COLUMN submitted_at DROP NOT NULL,
    ALTER COLUMN action_logs_json SET DEFAULT '[]',
    ALTER COLUMN action_logs_json DROP NOT NULL;
CREATE UNIQUE INDEX uq_submission_attempt
    ON public."Submission"(class_assignment_id, user_id, attempt_number)
    WHERE class_assignment_id IS NOT NULL;
CREATE INDEX ix_submission_assignment_student
    ON public."Submission"(assignment_id, user_id, submitted_at DESC);
