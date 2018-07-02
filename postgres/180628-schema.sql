-- Drop table

-- DROP TABLE public.message

CREATE TABLE message (
  id serial NOT NULL,
  message varchar NOT NULL,
  link varchar NOT NULL,
  protocol varchar NOT NULL,
  target varchar NOT NULL DEFAULT 'default'::character varying,
  created timestamp NOT NULL DEFAULT now(),
  CONSTRAINT message_pk PRIMARY KEY (id),
  CONSTRAINT message_un UNIQUE (link)
)
WITH (
  OIDS=FALSE
) ;
CREATE UNIQUE INDEX message_link_idx ON message USING btree (link) ;

-- Drop table

-- DROP TABLE public.subscriber

CREATE TABLE subscriber (
  id serial NOT NULL,
  telegram_id int4 NOT NULL,
  CONSTRAINT subscriber_pk PRIMARY KEY (id),
  CONSTRAINT subscriber_un UNIQUE (telegram_id)
)
WITH (
  OIDS=FALSE
) ;
CREATE UNIQUE INDEX subscriber_telegram_id_idx ON subscriber USING btree (telegram_id) ;

-- Drop table

-- DROP TABLE public.notified

CREATE TABLE notified (
  id serial NOT NULL,
  subscriber int4 NOT NULL,
  message int4 NOT NULL,
  time_notified timestamp NOT NULL,
  CONSTRAINT notified_pk PRIMARY KEY (id),
  CONSTRAINT notified_un UNIQUE (subscriber, message),
  CONSTRAINT notified_message_fk FOREIGN KEY (message) REFERENCES message(id) ON DELETE CASCADE,
  CONSTRAINT notified_subscriber_fk FOREIGN KEY (subscriber) REFERENCES subscriber(id) ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
) ;

-- Drop table

-- DROP TABLE public.subscription

CREATE TABLE subscription (
  id serial NOT NULL,
  subscriber int4 NOT NULL,
  protocol varchar NOT NULL,
  target varchar NOT NULL DEFAULT 'default'::character varying,
  "notify" bool NOT NULL DEFAULT true,
  CONSTRAINT subscription_pk PRIMARY KEY (id),
  CONSTRAINT subscription_un UNIQUE (subscriber, protocol, target),
  CONSTRAINT subscription_subscriber_fk FOREIGN KEY (subscriber) REFERENCES subscriber(id) ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
) ;

-- Drop table

-- DROP TABLE public.channel

CREATE TABLE channel (
  id serial NOT NULL,
  "name" varchar NOT NULL,
  owner int4 NOT NULL,
  CONSTRAINT channel_pk PRIMARY KEY (id),
  CONSTRAINT channel_subscriber_fk FOREIGN KEY (owner) REFERENCES subscriber(id) ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
) ;

-- Drop table

-- DROP TABLE public.channel_subscription

CREATE TABLE channel_subscription (
  id serial NOT NULL,
  channel int4 NOT NULL,
  subscription int4 NOT NULL,
  CONSTRAINT channel_subscription_pk PRIMARY KEY (id),
  CONSTRAINT channel_subscription_un UNIQUE (channel, subscription),
  CONSTRAINT channel_subscription_channel_fk FOREIGN KEY (channel) REFERENCES channel(id) ON DELETE CASCADE,
  CONSTRAINT channel_subscription_subscription_fk FOREIGN KEY (subscription) REFERENCES subscription(id) ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
) ;
