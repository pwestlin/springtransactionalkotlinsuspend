create table person
(
    id   bigint      not null
        constraint person_pk
            primary key,
    name varchar(30) not null
        unique
);

create table address
(
    id        bigint      not null
        constraint address_pk
            primary key,
    city      varchar(30) not null,
    person_id bigint      not null,
    CONSTRAINT person_fk
        FOREIGN KEY (person_id)
            REFERENCES person (id)
);
