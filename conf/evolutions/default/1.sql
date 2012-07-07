# --- First database schema

# --- !Ups

-- USERS
-- =====
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


-- BOARDS : the boards ...
-- ======
create table boards (
  id                        bigint not null primary key,
  name                      varchar(255) not null unique,
  data                      text not null -- "serialized" version of the board
);

create sequence boards_seq start with 1000;


-- TODO : db structure for rooms and games ... not dones yet
---- ROOMS : should remain a small list ...
---- =====
--create table rooms (
--  id                        bigint not null primary key,
--  name                      varchar(255) not null unique
--);
--create sequence rooms_seq start with 1000;
--
--
---- GAMES : one game on a board in a room
---- =====
--create table games (
--  id                        bigint not null primary key,
--  name                      varchar(255) not null unique,
--  created_on                timestamp not null,
--
--  room_id                   bigint not null,
--  board_id                  bigint not null,
--  FOREIGN KEY (room_id) REFERENCES rooms (id),
--  FOREIGN KEY (board_id) REFERENCES boards (id)
--);
--
--create sequence games_seq start with 1000;


# --- !Downs

-- TODO : db structure for rooms and games ... not dones yet
--drop sequence if exists games_seq;
--drop table if exists games;
--
--drop sequence if exists rooms_seq;
--drop table if exists rooms;

drop sequence if exists boards_seq;
drop table if exists boards;

drop sequence if exists users_seq;
drop table if exists users;
