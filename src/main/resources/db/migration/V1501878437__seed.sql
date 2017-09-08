DO $$
BEGIN
  CREATE SCHEMA IF NOT EXISTS event_schema;

  IF NOT exists(SELECT * FROM information_schema.tables WHERE table_schema = 'event_schema' AND table_name = 'event') THEN
    CREATE TABLE event_schema.event(
      event_id UUID NOT NULL,
      event_name TEXT NOT NULL,
      event_raised TIMESTAMP NOT NULL,
      event_applies TIMESTAMP NOT NULL,
      event_description TEXT,
      event_sender_id UUID NOT NULL,
      aggregate_id UUID NOT NULL,
      event JSONB NOT NULL,
      instance UUID NOT NULL
    );
    CREATE INDEX event_instance_aggregate_idx ON event_schema.event (instance, aggregate_id, event_raised, event_applies);
  END IF;

  IF NOT exists(SELECT * FROM information_schema.tables WHERE table_schema = 'event_schema' AND table_name = 'snapshot') THEN
    CREATE TABLE event_schema.snapshot(
      aggregate JSONB NOT NULL,
      taken TIMESTAMP NOT NULL,
      instance UUID NOT NULL
    );
    CREATE INDEX snapshot_instance_idx ON event_schema.snapshot (instance);
  END IF;
END $$;