-- =========================================================================
-- Baseras School Portal — initial schema
-- Compatible with PostgreSQL and H2 (in PostgreSQL mode).
-- Seed data is loaded by Java DataSeeder (BCrypt hashes are computed at boot).
-- =========================================================================

CREATE TABLE users (
    id                    varchar(64) PRIMARY KEY,
    username              varchar(64) NOT NULL UNIQUE,
    password_hash         varchar(255) NOT NULL,
    role                  varchar(20)  NOT NULL,
    full_name             varchar(255) NOT NULL,
    email                 varchar(255),
    roll_number           varchar(64),
    employee_id           varchar(64),
    class_id              varchar(64),
    must_reset_password   boolean      NOT NULL DEFAULT false,
    created_at            timestamp with time zone NOT NULL,
    deleted_at            timestamp with time zone
);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_class ON users(class_id);

CREATE TABLE classes (
    id                varchar(64) PRIMARY KEY,
    name              varchar(16)  NOT NULL,
    section           varchar(8)   NOT NULL,
    class_teacher_id  varchar(64),
    UNIQUE (name, section)
);

CREATE TABLE subjects (
    id    varchar(64) PRIMARY KEY,
    name  varchar(64) NOT NULL,
    code  varchar(16) NOT NULL UNIQUE
);

CREATE TABLE academic_sessions (
    id          varchar(64) PRIMARY KEY,
    label       varchar(32) NOT NULL UNIQUE,
    start_date  date NOT NULL,
    end_date    date NOT NULL,
    is_active   boolean NOT NULL
);

CREATE TABLE exam_types (
    id        varchar(64) PRIMARY KEY,
    name      varchar(64) NOT NULL,
    weight    double precision NOT NULL,
    sequence  int NOT NULL
);

CREATE TABLE students (
    id                varchar(64) PRIMARY KEY,
    user_id           varchar(64) NOT NULL UNIQUE,
    roll_number       varchar(64) NOT NULL UNIQUE,
    admission_number  varchar(64) NOT NULL,
    first_name        varchar(64) NOT NULL,
    middle_name       varchar(64),
    last_name         varchar(64) NOT NULL,
    dob               date NOT NULL,
    gender            varchar(16) NOT NULL,
    class_id          varchar(64) NOT NULL,
    parent_name       varchar(128),
    parent_phone      varchar(32),
    parent_email      varchar(128),
    address           text,
    photo_url         varchar(512),
    blood_group       varchar(8)
);
CREATE INDEX idx_students_class ON students(class_id);

CREATE TABLE teachers (
    id            varchar(64) PRIMARY KEY,
    user_id       varchar(64) NOT NULL UNIQUE,
    employee_id   varchar(64) NOT NULL UNIQUE,
    first_name    varchar(64) NOT NULL,
    last_name     varchar(64) NOT NULL,
    email         varchar(128) NOT NULL,
    phone         varchar(32),
    qualification varchar(128),
    joined_on     date,
    photo_url     varchar(512)
);

CREATE TABLE teacher_subjects (
    teacher_id  varchar(64) NOT NULL,
    subject_id  varchar(64) NOT NULL,
    PRIMARY KEY (teacher_id, subject_id)
);

CREATE TABLE teaching_assignments (
    id          varchar(64) PRIMARY KEY,
    teacher_id  varchar(64) NOT NULL,
    class_id    varchar(64) NOT NULL,
    subject_id  varchar(64) NOT NULL,
    session_id  varchar(64) NOT NULL,
    UNIQUE (teacher_id, class_id, subject_id, session_id)
);

CREATE TABLE notices (
    id              varchar(64) PRIMARY KEY,
    title           varchar(255) NOT NULL,
    body            text NOT NULL,
    audience        varchar(32) NOT NULL,
    class_id        varchar(64),
    published_at    timestamp with time zone NOT NULL,
    created_by      varchar(64) NOT NULL,
    created_by_name varchar(255) NOT NULL,
    version         int NOT NULL DEFAULT 1,
    deleted_at      timestamp with time zone
);
CREATE INDEX idx_notices_audience ON notices(audience);

CREATE TABLE notice_attachments (
    notice_id varchar(64) NOT NULL,
    url       varchar(512) NOT NULL
);

CREATE TABLE notice_versions (
    id              varchar(64) PRIMARY KEY,
    notice_id       varchar(64) NOT NULL,
    version         int NOT NULL,
    title           varchar(255) NOT NULL,
    body            text NOT NULL,
    audience        varchar(32) NOT NULL,
    class_id        varchar(64),
    edited_by       varchar(64) NOT NULL,
    edited_by_name  varchar(255) NOT NULL,
    edited_at       timestamp with time zone NOT NULL
);

CREATE TABLE posts (
    id            varchar(64) PRIMARY KEY,
    type          varchar(32) NOT NULL,
    title         varchar(255) NOT NULL,
    body          text NOT NULL,
    image_url     varchar(512),
    apply_enabled boolean NOT NULL,
    status        varchar(16) NOT NULL,
    created_at    timestamp with time zone NOT NULL
);

CREATE TABLE applications (
    id                 varchar(64) PRIMARY KEY,
    post_id            varchar(64) NOT NULL,
    applicant_name     varchar(128) NOT NULL,
    applicant_email    varchar(128) NOT NULL,
    applicant_phone    varchar(32) NOT NULL,
    applicant_message  text,
    resume_url         varchar(512),
    status             varchar(32) NOT NULL,
    submitted_at       timestamp with time zone NOT NULL
);

CREATE TABLE alumni (
    id                varchar(64) PRIMARY KEY,
    name              varchar(128) NOT NULL,
    batch_year        int NOT NULL,
    photo_url         varchar(512),
    current_position  varchar(255),
    achievements      text,
    linkedin_url      varchar(512)
);

CREATE TABLE marks (
    id              varchar(64) PRIMARY KEY,
    student_id      varchar(64) NOT NULL,
    subject_id      varchar(64) NOT NULL,
    exam_type_id    varchar(64) NOT NULL,
    session_id      varchar(64) NOT NULL,
    marks_obtained  double precision,
    max_marks       double precision NOT NULL,
    status          varchar(16) NOT NULL,
    entered_by      varchar(64),
    submitted_at    timestamp with time zone,
    published_at    timestamp with time zone,
    version         int NOT NULL DEFAULT 1,
    UNIQUE (student_id, subject_id, exam_type_id, session_id)
);
CREATE INDEX idx_marks_class_lookup ON marks(exam_type_id, session_id);

CREATE TABLE marks_audit (
    id                varchar(64) PRIMARY KEY,
    marks_id          varchar(64) NOT NULL,
    action            varchar(32) NOT NULL,
    old_marks         double precision,
    new_marks         double precision,
    old_status        varchar(16),
    new_status        varchar(16),
    performed_by      varchar(64) NOT NULL,
    performed_by_name varchar(255) NOT NULL,
    performed_at      timestamp with time zone NOT NULL
);

CREATE TABLE result_publications (
    id                  varchar(64) PRIMARY KEY,
    class_id            varchar(64) NOT NULL,
    exam_type_id        varchar(64) NOT NULL,
    session_id          varchar(64) NOT NULL,
    status              varchar(16) NOT NULL,
    published_by        varchar(64),
    published_at        timestamp with time zone,
    total_subjects      int NOT NULL,
    submitted_subjects  int NOT NULL,
    UNIQUE (class_id, exam_type_id, session_id)
);

CREATE TABLE assessments (
    id              varchar(64) PRIMARY KEY,
    teacher_id      varchar(64) NOT NULL,
    class_id        varchar(64) NOT NULL,
    subject_id      varchar(64) NOT NULL,
    session_id      varchar(64) NOT NULL,
    title           varchar(255) NOT NULL,
    description     text,
    attachment_url  varchar(512),
    due_date        date NOT NULL,
    created_at      timestamp with time zone NOT NULL,
    deleted_at      timestamp with time zone
);

CREATE TABLE fee_heads (
    id        varchar(64) PRIMARY KEY,
    name      varchar(64) NOT NULL,
    recurring boolean NOT NULL
);

CREATE TABLE fee_structures (
    id           varchar(64) PRIMARY KEY,
    class_id     varchar(64) NOT NULL,
    session_id   varchar(64) NOT NULL,
    fee_head_id  varchar(64) NOT NULL,
    amount       decimal(12,2) NOT NULL,
    UNIQUE (class_id, session_id, fee_head_id)
);

CREATE TABLE student_fees (
    id          varchar(64) PRIMARY KEY,
    student_id  varchar(64) NOT NULL,
    session_id  varchar(64) NOT NULL,
    total_due   decimal(12,2) NOT NULL,
    total_paid  decimal(12,2) NOT NULL DEFAULT 0,
    status      varchar(16) NOT NULL,
    UNIQUE (student_id, session_id)
);

CREATE TABLE fee_payments (
    id              varchar(64) PRIMARY KEY,
    student_fee_id  varchar(64) NOT NULL,
    amount          decimal(12,2) NOT NULL,
    paid_on         date NOT NULL,
    payment_mode    varchar(32) NOT NULL,
    receipt_no      varchar(64) NOT NULL,
    marked_by       varchar(64) NOT NULL,
    marked_by_name  varchar(255) NOT NULL,
    remarks         text,
    deleted_at      timestamp with time zone
);

CREATE TABLE attendance (
    id                varchar(64) PRIMARY KEY,
    student_id        varchar(64) NOT NULL,
    attendance_date   date NOT NULL,
    status            varchar(16) NOT NULL,
    marked_by         varchar(64) NOT NULL,
    session_id        varchar(64) NOT NULL,
    remarks           text,
    UNIQUE (student_id, attendance_date)
);
CREATE INDEX idx_attendance_date ON attendance(attendance_date);

CREATE TABLE timetable (
    id           varchar(64) PRIMARY KEY,
    class_id     varchar(64) NOT NULL,
    session_id   varchar(64) NOT NULL,
    day_of_week  int NOT NULL,
    period       int NOT NULL,
    start_time   time NOT NULL,
    end_time     time NOT NULL,
    subject_id   varchar(64) NOT NULL,
    teacher_id   varchar(64) NOT NULL,
    UNIQUE (class_id, session_id, day_of_week, period)
);

CREATE TABLE notifications (
    id          varchar(64) PRIMARY KEY,
    user_id     varchar(64) NOT NULL,
    type        varchar(32) NOT NULL,
    title       varchar(255) NOT NULL,
    body        text NOT NULL,
    link        varchar(512),
    read_at     timestamp with time zone,
    created_at  timestamp with time zone NOT NULL
);
CREATE INDEX idx_notifications_user ON notifications(user_id);

CREATE TABLE audit_log (
    id              varchar(64) PRIMARY KEY,
    actor_user_id   varchar(64) NOT NULL,
    actor_name      varchar(255) NOT NULL,
    action          varchar(64) NOT NULL,
    entity_type     varchar(64) NOT NULL,
    entity_id       varchar(64) NOT NULL,
    metadata        text,
    created_at      timestamp with time zone NOT NULL
);
CREATE INDEX idx_audit_created ON audit_log(created_at);

CREATE TABLE staff_directory (
    id            varchar(64) PRIMARY KEY,
    name          varchar(128) NOT NULL,
    designation   varchar(128) NOT NULL,
    department    varchar(128) NOT NULL,
    photo_url     varchar(512),
    bio           text,
    display_order int NOT NULL
);

CREATE TABLE infrastructure_gallery (
    id            varchar(64) PRIMARY KEY,
    category      varchar(32) NOT NULL,
    image_url     varchar(512) NOT NULL,
    caption       varchar(255) NOT NULL,
    display_order int NOT NULL
);

CREATE TABLE principal_message (
    id              varchar(64) PRIMARY KEY,
    principal_name  varchar(128) NOT NULL,
    photo_url       varchar(512),
    message         text NOT NULL,
    updated_at      timestamp with time zone NOT NULL
);

CREATE TABLE login_attempts (
    id           varchar(64) PRIMARY KEY,
    ip_address   varchar(64) NOT NULL,
    username     varchar(64) NOT NULL,
    success      boolean NOT NULL,
    attempted_at timestamp with time zone NOT NULL
);
CREATE INDEX idx_login_attempts_ip_at ON login_attempts(ip_address, attempted_at);
