# --- First database schema

# --- !Ups

create table users (
  id                        bigint not null primary key,
  name                      varchar(255) not null unique,
  password                  varchar(255) not null
);

create sequence users_seq start with 1000;

# --- !Downs

drop sequence if exists users_seq;
drop table if exists users;
