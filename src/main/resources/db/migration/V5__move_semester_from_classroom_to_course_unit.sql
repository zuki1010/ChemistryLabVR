ALTER TABLE public."Classroom"
    DROP COLUMN term;

ALTER TABLE public."CourseUnit"
    ADD COLUMN semester SMALLINT
        CHECK (semester IS NULL OR semester IN (1, 2));
