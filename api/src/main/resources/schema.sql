drop schema if exists todo cascade;
create schema todo;

drop table if exists todo.task;
create table todo.task
(
    id         uuid primary key,
    name       text not null,
    start_time timestamp default now(),
    end_time   timestamp
)