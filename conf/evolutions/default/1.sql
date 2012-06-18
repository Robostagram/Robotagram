# --- First database schema

# --- !Ups

create table users (
  id                        bigint not null primary key,
  name                      varchar(255) not null unique,
  email                     varchar(255) not null unique,
  password                  varchar(255) not null,
  created_on                timestamp not null,
  activation_token          varchar(255) not null,
  activated_on              timestamp
);

create sequence users_seq start with 1000;

# --- !Downs

drop sequence if exists users_seq;
drop table if exists users;
