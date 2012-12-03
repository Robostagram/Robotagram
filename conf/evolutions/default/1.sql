# --- First database schema

# --- !Ups

-- USERS
-- =====
create table users (
  id                        bigint not null primary key,
  name                      varchar(255) not null unique,
  email                     varchar(255) not null unique,
  password                  varchar(255) not null,
  isAdmin                   boolean not null default 'false',
  created_on                timestamp not null,
  activation_token          varchar(255) not null,
  activated_on              timestamp,
  locale                    varchar(5)
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


-- ROOMS : should remain a small list ...
-- =====
create table rooms (
  id                        bigint not null primary key,
  name                      varchar(255) not null unique
);
create sequence rooms_seq start with 1000;


-- GAMES : one game on a board in a room
-- =====
create table games (
  id                        varchar(127) not null primary key, -- uuid seems like not the way to go ... why ?
  created_on                timestamp not null,
  valid_until               timestamp not null,
  goal_symbol               varchar(127) not null, --store it as string until we do better
  goal_color                varchar(127) not null, --store it as string until we do better
  -- robots .. should be done better ...
  robot_blue_x              int not null,
  robot_blue_y              int not null,
  robot_red_x               int not null,
  robot_red_y               int not null,
  robot_green_x             int not null,
  robot_green_y             int not null,
  robot_yellow_x            int not null,
  robot_yellow_y            int not null,
  -- FK
  room_id                   bigint not null,
  board_id                  bigint not null,
  phase                     varchar(32) not null,
  FOREIGN KEY (room_id) REFERENCES rooms (id),
  FOREIGN KEY (board_id) REFERENCES boards (id)
);

-- SCORES : validated solutions for a player in a game
-- ======
create table scores (
    id                      bigint not null primary key,
    submitted_on            timestamp not null,
    solution                text not null, --the json of the submitted solution,
    score                   int,
    -- FK
    game_id                 varchar(127) not null,
    user_id                 bigint not null,
    FOREIGN KEY (game_id) REFERENCES games (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
create sequence scores_seq start with 1000;




# --- !Downs
drop sequence if exists scores_seq;
drop table if exists scores;

drop table if exists games;

drop sequence if exists rooms_seq;
drop table if exists rooms;

drop sequence if exists boards_seq;
drop table if exists boards;

drop sequence if exists users_seq;
drop table if exists users;
