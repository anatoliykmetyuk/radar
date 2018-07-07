-- Drop table

-- DROP TABLE public.message

CREATE TABLE message (
  id serial NOT NULL,
  link varchar NOT NULL,
  "text" varchar NOT NULL,
  format varchar NOT NULL,
  target varchar NULL,
  created_at timestamp NOT NULL DEFAULT now(),
  notification_sent bool NOT NULL DEFAULT false,
  CONSTRAINT message_pk PRIMARY KEY (id),
  CONSTRAINT message_un UNIQUE (link)
)
WITH (
  OIDS=FALSE
) ;
CREATE INDEX message_created_at_idx ON message USING btree (created_at) ;
CREATE UNIQUE INDEX message_link_idx ON message USING btree (link) ;

-- Drop table

-- DROP TABLE public.credentials

CREATE TABLE credentials (
  id serial NOT NULL,
  target varchar NOT NULL,
  login varchar NOT NULL,
  password varchar NOT NULL,
  CONSTRAINT credentials_pk PRIMARY KEY (id),
  CONSTRAINT credentials_un UNIQUE (target)
)
WITH (
  OIDS=FALSE
) ;
CREATE UNIQUE INDEX credentials_target_idx ON credentials USING btree (target) ;
