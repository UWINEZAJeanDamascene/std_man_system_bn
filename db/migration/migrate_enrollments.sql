-- Migration: convert a legacy many-to-many join table enrollments (student_id, course_id)
-- into an entity-backed table with a surrogate id and metadata columns.
-- This script is written for SQL Server. BACK UP your database before running.

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRAN;

-- 1) Create the new table shape we want (if it doesn't already exist)
IF OBJECT_ID('dbo.enrollments_new', 'U') IS NULL
BEGIN
  CREATE TABLE dbo.enrollments_new (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrolled_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    status VARCHAR(64),
    CONSTRAINT uq_enr_student_course UNIQUE (student_id, course_id)
  );
END

-- 2) If an old enrollments table exists (legacy join table), copy data into enrollments_new
IF OBJECT_ID('dbo.enrollments', 'U') IS NOT NULL
BEGIN
  -- Copy student/course pairs into new table; use ISNULL to handle missing enrolled_at
  INSERT INTO dbo.enrollments_new (student_id, course_id, enrolled_at, status)
  SELECT student_id, course_id, ISNULL(enrolled_at, SYSUTCDATETIME()), NULL
  FROM dbo.enrollments;

  -- Rename old table to enrollments_old and new table into enrollments
  -- sp_rename is used because SQL Server doesn't support RENAME TABLE
  EXEC sp_rename 'dbo.enrollments', 'enrollments_old';
  EXEC sp_rename 'dbo.enrollments_new', 'enrollments';
END
ELSE
BEGIN
  -- No old table; simply rename the new table into place
  IF OBJECT_ID('dbo.enrollments', 'U') IS NULL
  BEGIN
    EXEC sp_rename 'dbo.enrollments_new', 'enrollments';
  END
END

-- 3) Add foreign key constraints now if the referenced tables are present and have an id PK.
-- Check existence of referenced tables before adding constraints to avoid errors.
IF OBJECT_ID('dbo.enrollments', 'U') IS NOT NULL
BEGIN
  IF OBJECT_ID('dbo.students', 'U') IS NOT NULL AND OBJECT_ID('dbo.courses', 'U') IS NOT NULL
  BEGIN
    -- Ensure constraints do not already exist
    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'fk_enr_student' AND parent_object_id = OBJECT_ID('dbo.enrollments'))
    BEGIN
      ALTER TABLE dbo.enrollments
        ADD CONSTRAINT fk_enr_student FOREIGN KEY (student_id) REFERENCES dbo.students(id);
    END
    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'fk_enr_course' AND parent_object_id = OBJECT_ID('dbo.enrollments'))
    BEGIN
      ALTER TABLE dbo.enrollments
        ADD CONSTRAINT fk_enr_course FOREIGN KEY (course_id) REFERENCES dbo.courses(id);
    END
  END
END

COMMIT;

-- Notes:
-- - This version is for SQL Server: it uses IDENTITY, SYSUTCDATETIME, OBJECT_ID checks and sp_rename.
-- - If the ALTER TABLE to add foreign keys fails, inspect the referenced tables with:
--     EXEC sp_help 'dbo.students';
--     EXEC sp_help 'dbo.courses';
-- - After validating the migration you can DROP TABLE dbo.enrollments_old to reclaim space.
