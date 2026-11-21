create table lesson (
    id varchar(40) primary key,
    track varchar(20) not null check (track in ('FRONTEND', 'JAVA', 'INTEGRATION')),
    position integer not null unique,
    title varchar(120) not null,
    objective varchar(240) not null,
    body varchar(3000) not null,
    example varchar(3000) not null
);

create table question (
    id bigint primary key,
    lesson_id varchar(40) not null references lesson(id),
    prompt varchar(600) not null,
    option_a varchar(400) not null,
    option_b varchar(400) not null,
    option_c varchar(400) not null,
    correct_option varchar(1) not null check (correct_option in ('A', 'B', 'C')),
    explanation varchar(1000) not null,
    source_type varchar(24),
    source_title varchar(180),
    source_url varchar(600),
    reference_url varchar(600),
    check ((source_type is null and source_title is null and source_url is null and reference_url is null)
        or (source_type is not null and source_type in ('OFFICIAL_GUIDE', 'CANDIDATE_ACCOUNT', 'COMMUNITY')
            and source_title is not null and source_url is not null and reference_url is not null))
);

create index question_lesson_idx on question(lesson_id);
