create table employee
(
    id          bigint       not null    constraint employee_pkey primary key,
    firstname   varchar(255) not null,
    lastname    varchar(255) not null
);