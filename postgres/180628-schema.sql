CREATE TABLE public.fbevents (
  id       serial    NOT NULL
, month    varchar   NOT NULL
, "date"   varchar   NOT NULL
, name     varchar   NOT NULL
, link     varchar   NOT NULL
, details  varchar   NOT NULL
, created  timestamp NOT NULL
, notified boolean   NOT NULL

, CONSTRAINT fbevents_pk PRIMARY KEY (id)
);
