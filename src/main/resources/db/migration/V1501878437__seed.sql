DO $$
BEGIN
  CREATE SCHEMA IF NOT EXISTS eventstore;

  IF NOT exists(SELECT * FROM information_schema.tables WHERE table_schema = 'eventstore' AND table_name = 'meter_events') THEN
    CREATE TABLE eventstore.meter_events(
      event_id UUID,
      event_name TEXT NOT NULL,
      event_raised TIMESTAMP NOT NULL,
      sender_id UUID NOT NULL,
      sender_type TEXT NOT NULL,
      aggregate_id UUID,
      payload_type TEXT NOT NULL,
      payload BYTEA NOT NULL
    );
  END IF;
END $$