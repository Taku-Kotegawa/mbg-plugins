create database mbg;
\c mbg;


drop table if exists normal_table cascade;
create table normal_table (
    username varchar (128),
    status varchar(1) not null,
    version bigint default 1 not null,
    created_by varchar(255) not null,
    created_date timestamp(0) without time zone not null,
    last_modified_by varchar(255) not null,
    last_modified_date timestamp(0) without time zone not null,
    password varchar (88) not null,
    first_name varchar (128) not null,
    last_name varchar (128),
    email varchar (128) not null,
    department varchar(255),
    url varchar (255),
    profile text,
    image_uuid varchar(255),
    allowed_ip varchar(255),
    api_key varchar(255),
    constraint normal_table_pkey primary key (username)
);

comment on table normal_table is '通常テーブル';
comment on column normal_table.username is 'ユーザid';
comment on column normal_table.status is 'ステータス';
comment on column normal_table.version is 'バージョン';
comment on column normal_table.created_by is '作成者';
comment on column normal_table.created_date is '作成日時';
comment on column normal_table.last_modified_by is '最終更新者';
comment on column normal_table.last_modified_date is '最終更新日時';
comment on column normal_table.password is 'パスワード';
comment on column normal_table.first_name is '名';
comment on column normal_table.last_name is '姓';
comment on column normal_table.email is 'メールアドレス';
comment on column normal_table.department is '所属部門';
comment on column normal_table.url is 'url';
comment on column normal_table.profile is 'プロフィール';
comment on column normal_table.image_uuid is 'イメージファイルUUID';
comment on column normal_table.allowed_ip is 'ログイン許可ipアドレス';
comment on column normal_table.api_key is 'api key';


-- https://www.postgresql.jp/document/15/html/datatype-numeric.html
drop table if exists nokey_with_blob cascade;
create table nokey_with_blob (
  field1 smallint
, field2 integer
, field3 bigint
, field4 decimal(10, 3)
, field5 numeric
, field6 numeric(11)
, field7 numeric(11, 4)
, field8 smallserial
, field9 serial
, field10 bigserial
, field11 money
, field12 varchar(100)
, field13 char(10)
, field14 text
, field15 bytea
, field16 timestamp
, field17 timestamp with time zone
, field18 time
, field19 time with time zone
, field20 date
, field21 interval
, field22 boolean
, field23 uuid

);

comment on table nokey_with_blob is '主キーなし、BLOB含む';
comment on column nokey_with_blob.field1 is 'smallint';
comment on column nokey_with_blob.field2 is 'integer';
comment on column nokey_with_blob.field3 is 'bigint';
comment on column nokey_with_blob.field4 is 'decimal(10,3)';
comment on column nokey_with_blob.field5 is 'numeric';
comment on column nokey_with_blob.field6 is 'numeric(11)';
comment on column nokey_with_blob.field7 is 'numeric(11,4)';
comment on column nokey_with_blob.field8 is 'smallserial';
comment on column nokey_with_blob.field9 is 'serial';
comment on column nokey_with_blob.field10 is 'bigserial';
comment on column nokey_with_blob.field11 is 'money';
comment on column nokey_with_blob.field12 is 'varchar(100)';
comment on column nokey_with_blob.field13 is 'char(10)';
comment on column nokey_with_blob.field14 is 'text';
comment on column nokey_with_blob.field15 is 'bytea';
comment on column nokey_with_blob.field16 is 'timestamp';
comment on column nokey_with_blob.field17 is 'timestamp with time zone';
comment on column nokey_with_blob.field18 is 'time';
comment on column nokey_with_blob.field19 is 'time with time zone';
comment on column nokey_with_blob.field20 is 'date';
comment on column nokey_with_blob.field21 is 'interval';
comment on column nokey_with_blob.field22 is 'boolean';
comment on column nokey_with_blob.field23 is 'uuid';


drop table if exists complexkey_table cascade;
create table complexkey_table (
    username varchar (128),
    status varchar(1) not null,
    version bigint default 1 not null,
    created_by varchar(255) not null,
    created_date timestamp(0) without time zone not null,
    last_modified_by varchar(255) not null,
    last_modified_date timestamp(0) without time zone not null,
    first_name varchar (128) not null,
    last_name varchar (128),
    constraint complexkey_table_pkey primary key (username, status, version)
);

comment on table complexkey_table is '複合主キー';
comment on column complexkey_table.username is 'ユーザid';
comment on column complexkey_table.status is 'ステータス';
comment on column complexkey_table.version is 'バージョン';
comment on column complexkey_table.created_by is '作成者';
comment on column complexkey_table.created_date is '作成日時';
comment on column complexkey_table.last_modified_by is '最終更新者';
comment on column complexkey_table.last_modified_date is '最終更新日時';
comment on column complexkey_table.first_name is '名';
comment on column complexkey_table.last_name is '姓';
